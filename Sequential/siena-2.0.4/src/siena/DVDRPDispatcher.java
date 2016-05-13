//
//  This file is part of Siena, a wide-area event notification system.
//  See http://www.inf.usi.ch/carzaniga/siena/
//
//  Authors:
// 	Amir Malekpour (malekpoa@usi.ch)
//	Giovanni Toffetti Carughi
//  See the file AUTHORS for full details. 
//
//  Copyright (C) 2007, 2011 Giovanni Toffetti Carughi
//
//  Siena is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//  
//  Siena is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//  
//  You should have received a copy of the GNU General Public License
//  along with Siena.  If not, see <http://www.gnu.org/licenses/>.
//
package siena;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import siena.comm.InvalidSenderException;
import siena.comm.MultiPacketReceiver;
import siena.comm.PacketReceiver;
import siena.comm.PacketSenderException;
import siena.dvdrp.BSetBV;
import siena.dvdrp.DVEntry;
import siena.dvdrp.DistanceVector;
import siena.dvdrp.OrderedByteArrayMap;
import siena.dvdrp.OrderedByteArraySet;
import siena.dvdrp.PredicatesTable;
import siena.dvdrp.PredicatesTableEntry;
import siena.dvdrp.QueueFullException;
import siena.dvdrp.TwoPrioritiesBufferQueue;
import siena.dvdrp.TwoPrioritiesQueue;
import siena.dvdrp.TwoPrioritiesStaticBufferQueue;

/**
 * implementation of a Siena-DV/DRP event notification service.
 * 
 * This is an implementation of a Siena DV/DRP event notification service (it
 * extends <code>HierarchicalDispatcher</code>). A <code>DVDRPDispatcher</code>
 * can serve as a Siena event service for local (same Java VM) clients as well
 * as remote clients. A <code>DVDRPDispatcher</code>s can also be combined in a
 * distributed architecture with other dispatchers. Every dispatcher can be
 * connected to a <em>neighbor</em> dispatcher, thereby forming a generic
 * network topology. The network of dispatchers is assembled incrementally and
 * can be modified by issuing configuration requests to a dispatcher.
 * 
 * <p>
 * A <code>DVDRPDispatcher</code> uses a {@link PacketReceiver} to receive
 * notifications, subscriptions and unsubscriptions from external clients and
 * from its <em>neighbor</em> dispatcher. In order to receive and process
 * external requests, a <code>DVDRPDispatcher</code> can either use a pool of
 * internal threads, or it can use users' threads. See
 * {@link #DefaultThreadCount}, {@link #setReceiver(PacketReceiver)}, and
 * {@link #setReceiver(PacketReceiver, int)}
 * 
 * @see Siena
 * @see ThinClient
 */

public class DVDRPDispatcher extends HierarchicalDispatcher implements Siena,
								       Runnable {

    public static int DEFAULT_WAIT_FOR_REROUTE = 15000; // (15 seconds)
    public static int DEFAULT_TTL_FOR_UDV = 2;
    public static int DEFAULT_TTL_FOR_RECOVERY = 5;
    public static int DEFAULT_MAX_COST = Integer.MAX_VALUE;
    public static long DEFAULT_DVDISPATCH_PERIOD = 300000; // 5 minutes
    public long dvDispatchPeriod = DEFAULT_DVDISPATCH_PERIOD; // actual sleep
    public int chokePeriod = -1;

    // time,
    // overridden by
    // constructor

    // the actual DV
    protected DistanceVector distanceVector = new DistanceVector();

    // a wrapper to keep track of DV entries that were updated
    protected DistanceVector updatedDV = new DistanceVector();

    // the table storing all predicates (all subscriptions for entire net)
    protected PredicatesTable predicates = new PredicatesTable();

    // a wrapper to keep local changes
    protected PredicatesTable updatedPredicates = new PredicatesTable();

    // // the forwarding table
    protected DistanceVector fwTable = new DistanceVector();

    // // the predicate forwarding table
    protected PredicatesTable predFwTable = new PredicatesTable();

    protected Timer timer;

    Map<byte[], NeighborNode> neighbors = new OrderedByteArrayMap<NeighborNode>();

    private TreeSet<byte[]> waitingForConnection = new OrderedByteArraySet();

    public Object predicatesUpdateLock = new Object();
    public Object predicatesFWDLock = new Object();

    // used for multicast discovery of other nodes
    DiscoveryManager discoveryManager = null;

    // overridden heartbeat
    protected DVHeartbeat heartbeat;

    public DVDRPDispatcher() {
	super();
	initLocalEntries();
    }

    public DVDRPDispatcher(String id) {
	super(id);
	initLocalEntries();
    }

    public void startHeartbeat() {
	if (chokePeriod != -1) {
	    heartbeat = new DVHeartbeat(this, chokePeriod);
	} else {
	    heartbeat = new DVHeartbeat(this);
	}
	heartbeat.startHeartbeat();
	timer = new Timer();
    }

    public void doDiscovery() {
	// first thing we do is we start our local multicast listener
	discoveryManager = new DiscoveryManager(this);
	discoveryManager.start();
    }

    protected void processRequest(SENPPacket req) {
	// long time = System.nanoTime();
	Logging.prlnlog(new String(my_identity) + " [" + Thread.currentThread().getId() + "] processRequest: " + req, Logging.INFO); 
	if (req == null) {
	    Logging.prlnerr("processRequest: null request", Logging.WARN);
	    return;
	}
	if (req.ttl <= 0)
	    return;
	req.ttl--;
	try {
	    switch (req.method) {
	    case SENP.PUB:
		publish(req);
		break;
	    case SENP.SUB:
		subscribe(req);
		++cleanup_rounds;
		break;
	    case SENP.BYE:
		req.pattern = null;
		req.filter = null;
		unsubscribe(req);
		++cleanup_rounds;
		break;
	    case SENP.UNS:
		unsubscribe(req);
		++cleanup_rounds;
		break;
	    case SENP.SUS:
		suspend(req);
		++cleanup_rounds;
		break;
	    case SENP.RES:
		resume(req);
		++cleanup_rounds;
		break;
	    case SENP.MAP:
		map(req);
		++cleanup_rounds;
		break;
	    case SENP.CNF:
		configure(req);
		break;
	    case SENP.DV:
		/*
		 * Here starts an ugly workaround to prevent threads
		 * from getting all stuck trying to connect to the
		 * same neighbor.  Especially useful for PLASTIC
		 * protocol that has to go through a daemon and
		 * different lookups
		 */
		// if it's not a self -multicasted message
		if (!Arrays.equals(req.id, my_identity)) {
		    // first we add the neighbor to the neighbors list
		    if (!neighbors.containsKey(req.id)) {
			boolean doConfigure = false;
			synchronized (waitingForConnection) {
			    if (!waitingForConnection.contains(req.id)) {
				waitingForConnection.add(req.id);
				doConfigure = true;
			    }
			}
			if (doConfigure) {
			    configure(req);
			    if (neighbors.containsKey(req.id)) {
				synchronized (waitingForConnection) {
				    waitingForConnection.remove(req.id);
				}
			    }
			}
		    }
		    // then we update the local distance vector
		    handleUDV(req);
		}
		// TODO: ask whether we need ++cleanup_rounds;
		break;
	    case SENP.UDV:
		// we update the local distance vector
		handleUDV(req);
		// TODO: ask whether we need ++cleanup_rounds;
		break;
	    case SENP.PAD:
		handlePAD(req);
		break;
	    case SENP.DRP:
		handleDRP(req);
		// TODO: ask whether we need ++cleanup_rounds;
		break;
	    case SENP.OFF:
		shutdown();
		//
		// BEGIN_UNOFFICIAL_PATCH
		try {
		    Thread.sleep(500);
		} catch (Exception ex) {
		}
		;
		System.exit(0);
		// END_UNOFFICIAL_PATCH
		//
		break;
	    case SENP.NOP:
		break;
	    default:
		Logging.prlnerr("processRequest: unknown method: " + req,
				Logging.WARN);
		//
		// can't handle this request (yet)
		// ...work in progress...
		//
	    }
	    //
	    // here I see if it's time to clean up tables and save
	    // subscriptions
	    //
	    long current_time = System.currentTimeMillis();
	    if (cleanup_rounds >= cleanup_max_r
		|| current_time - cleanup_time > cleanup_max_t) {
		Logging.prlnlog("cleaning up contacts and interfaces tables",
				Logging.DEBUG);
		synchronized (this) {
		    ifmanager.cleanup();
		    contacts.cleanup();
		    if (storeFileName != null)
			saveSubscriptions(storeFileName);
		    cleanup_time = current_time;
		    cleanup_rounds = 0;
		}
	    }
	} catch (Exception ex) {
	    Logging.exerr(ex);
	    //
	    // log something here ...work in progress...
	    //
	    Logging.prlnerr(new String(my_identity) + " [" + Thread.currentThread().getId() + "] Offending message: " + req.toString(), Logging.WARN);
	}
    }

    /**
     * @param req
     */

    protected void handleUDV(SENPPacket req) {
	DistanceVector dv = null;
	// default ttl to propagate DV
	// int ttl = DVDRPDispatcher.DEFAULT_TTL_FOR_UDV;
	boolean isNeighbor = false;
	synchronized (neighbors) {
	    isNeighbor = neighbors.containsKey(req.id);
	}
	if ((dv = req.distanceVector) != null && isNeighbor) {
	    if (Logging.severity == Logging.DEBUG) {
		Logging.prlnlog( new String(my_identity) + " [" +  Thread.currentThread().getId() +  "] updateDistanceVector: vector from neighbor " + new String(req.id) +  "\n input vector content: \n" +  req.distanceVector);
	    }
	    // get interface cost
	    int interfaceCost = (neighbors.get(req.id)).interf.getCost();
	    /* add a DVEntry for the neighbor */

	    if (distanceVector.getEntry(req.id) == null) {
		DVEntry entry = new DVEntry();
		entry.setDest(req.id);
		entry.setDist(interfaceCost);
		entry.setNextHopId(req.id);
		// add to updatedDV to be sent out
		updatedDV.addEntry(req.id, entry);
		// synchronized (distanceVector) {
		distanceVector.addEntry(req.id, entry);
		// }
		if (Logging.severity == Logging.DEBUG) {
		    Logging.prlnlog(new String(my_identity) + " [" + Thread.currentThread().getId() +  "] updateDistanceVector: adding DVEntry for neighbor " + new String(req.id));
		}
	    }
	    /* update timestamp for the DVEntry of this neighbor */
	    distanceVector.getEntry(req.id).setEntryTS(
						       System.currentTimeMillis());
	    Iterator<byte[]> ite = dv.getEntryIdsIterator();
	    /*
	     * iterate on each entry and: compare whether the neighbor knows a
	     * cheaper way to a subscriber
	     */
	    while (ite.hasNext()) {
		// the key to the DV table is the destination ID
		byte[] curId = ite.next();
		if (Logging.severity == Logging.DEBUG) {
		    Logging.prlnlog( new String(my_identity) + " [" +  Thread.currentThread().getId() +  "] updateDistanceVector: iterating on entry " + new String(curId));
		}
		// if the DVEntry represents the current node do nothing
		if (!Arrays.equals(curId, my_identity)) {
		    DVEntry localEntry = distanceVector.getEntry(curId);
		    DVEntry externalEntry = dv.getEntry(curId);
		    if (localEntry != null && externalEntry != null) {
			if (Logging.severity == Logging.DEBUG) {
			    Logging.prlnlog(new String(my_identity) + " [" + Thread.currentThread().getId() + "] updateDistanceVector: entry " + new String(curId) +  " was already present");
			}
			// synchronized (localEntry) {
			if ((externalEntry.getDist() + interfaceCost) < localEntry
			    .getDist()
			    && (externalEntry.getDist() != DEFAULT_MAX_COST)) {
			    if (Logging.severity == Logging.DEBUG) {
				Logging.prlnlog(new String(my_identity) + "[" +  Thread.currentThread().getId() +  "] updateDistanceVector: entry " + new String(curId) +  " provides a shorter path");
			    }
			    // this path is shorter than the one we had
			    // update entry to use packet sender as next
			    // hop

			    localEntry.setNextHopId(req.id);
			    // update distance
			    localEntry.setDist(externalEntry.getDist()
					       + interfaceCost);
			    localEntry.setEntryTS(System.currentTimeMillis());
			    // update DV copy to be sent to neighbors
			    updatedDV.addEntry(curId, localEntry);

			} else if ((externalEntry.getDist() + interfaceCost) == localEntry
				   .getDist()
				   && (externalEntry.getDist() != DEFAULT_MAX_COST)
				   && (!Arrays.equals(req.id, externalEntry
						      .getDest()))) {
			    /*
			     * We add some randomness to try and avoid routing
			     * always through the first node who provided info
			     * about a destination
			     */
			    if (Math.round(Math.random()) == 1) {
				localEntry.setNextHopId(req.id);
				localEntry.setEntryTS(System
						      .currentTimeMillis());
			    }
			} else {
			    if (Logging.severity == Logging.DEBUG) {
				Logging.prlnlog(new String(my_identity) + " [" +  Thread.currentThread() .getId() +  "] updateDistanceVector: entry " + new String(curId) +  " does not provide a shorter path");
			    }
			    /*
			     * update of current DVEntry if this was our nextHop
			     * to reach destination and the distance changed
			     */

			    if (Arrays
				.equals(req.id, localEntry.getNextHopId())
				&& localEntry.getDist() != (externalEntry
							    .getDist() + interfaceCost)) {
				// this means that our best known route
				// to
				// destination needs to be updated
				if (Logging.severity == Logging.DEBUG) {
				    Logging.prlnlog( new String( my_identity) + " [" +  Thread .currentThread() .getId() +  "] updateDistanceVector: entry " + new String(curId) +  " was the nextHop and its distance changed");
				}
				if (externalEntry.getDist() != DEFAULT_MAX_COST) {
				    localEntry.setDist(externalEntry.getDist()
						       + interfaceCost);
				    /*
				     * in this case we can rely on heartbeats to
				     * propagate the changed distance and we
				     * don't notify anybody
				     */

				} else {
				    localEntry.setDist(DEFAULT_MAX_COST);
				    /*
				     * in this case we need to signal to our
				     * neighbors the broken path
				     */
				    if (Logging.severity == Logging.DEBUG) {
					Logging.prlnlog( new String( my_identity) + " [" +  Thread .currentThread() .getId() +  "] updateDistanceVector updated DV after MAX_INT: " + distanceVector);
				    }
				    // update DV to be sent to neighbors
				    updatedDV.addEntry(localEntry.getDest(),
						       localEntry);
				    // we decrease the ttl
				    // ttl = (int) req.ttl - 1;
				}
				localEntry.setEntryTS(System
						      .currentTimeMillis());
			    } else if (externalEntry.getDist() == DEFAULT_MAX_COST) {
				/*
				 * this is the case in which we receive the
				 * notification that our neighbor lost the way
				 * to somewhere but he was not on our path to
				 * destination. If we know a path that is
				 * shorter than MAX_INT we send back the route
				 * we know of, otherwise we don't reply
				 */
				if (Logging.severity == Logging.DEBUG) {
				    Logging.prlnlog( new String( my_identity) + " [" +  Thread .currentThread() .getId() +  "] updateDistanceVector: entry " + new String(curId) +  " has distance set to MAX_INT");
				}
				if (localEntry.getDist() != DEFAULT_MAX_COST) {
				    if (Logging.severity == Logging.DEBUG) {
					Logging.prlnlog( new String( my_identity) + " [" +  Thread .currentThread() .getId() +  "] updateDistanceVector: sent the route we know of for entry " +  new String( curId));
				    }
				    queueDistanceVectorUpdateTo(localEntry,
								req.id,
								DVDRPDispatcher.DEFAULT_TTL_FOR_UDV);
				} else {
				    // TODO: check this else branch
				}
			    }
			}
		    } else {
			// we add the new entry
			if (Logging.severity == Logging.DEBUG) {
			    Logging.prlnlog( new String(my_identity) + " [" + Thread.currentThread().getId() + "] updateDistanceVector: entry " + new String(curId) +  " is new and we add it");
			}

			externalEntry.setNextHopId(req.id);
			// update distance
			externalEntry.setDist(externalEntry.getDist()
					      + interfaceCost);
			externalEntry.setEntryTS(System.currentTimeMillis());
			// update table
			distanceVector.addEntry(externalEntry.getDest(),
						externalEntry);
			// update DV copy to be sent to neighbors
			updatedDV.addEntry(curId, externalEntry);

		    }
		}

	    }
	    /*
	     * Finally, if we updated something in the local DV, we send the
	     * updated info to neighbor nodes
	     */
	    if (updatedDV != null && updatedDV.getEntryIdsIterator().hasNext()) {
		if (Logging.severity == Logging.DEBUG) {
		    Logging.prlnlog(new String(my_identity) + " [" + Thread.currentThread().getId() +  "] updateDistanceVector queuing updated DV to neighbors: " + updatedDV);
		}
		heartbeat.queueUpdate(DVHeartbeat.UDV);
	    }

	    if (Logging.severity == Logging.DEBUG) {
		Logging.prlnlog(new String(my_identity) + " [" + Thread.currentThread().getId() +  "] updateDistanceVector finished, resulting distance vector:\n" + distanceVector);
	    }

	} else {
	    Logging.prlnerr(new String(my_identity) + " [" + Thread.currentThread().getId() + "] received UDV from unknown neighbor: " +  req.id, Logging.WARN);
	}

    }

    /**
     * This method is invoked by a single thread 'DVHeartbeat.chocke' seconds
     * after an update to the network topology
     */
    protected void updateDistanceVector(DistanceVector update) {
	if (update != null) {
	    synchronized (distanceVector) {
		synchronized (update) {
		    for (Iterator<byte[]> i = update.getEntryIdsIterator(); i.hasNext();) {
			byte[] name = i.next();
			distanceVector.addEntry(name, new DVEntry(update.getEntry(name)));
		    }
		}
	    }
	}
    }

    /**
     * This method is invoked by a single thread 'DVHeartbeat.choke' seconds
     * after an update to the predicates in the network (including local)
     */
    protected void updatePredicates(PredicatesTable update) {
	if (update != null) {
	    if (Logging.severity == Logging.DEBUG) {
		Logging.prlnlog(new String(my_identity) + " [" +  Thread.currentThread().getId() +  "] updatePredicates start table: " + predicates);
		Logging.prlnlog(new String(my_identity) + " [" +  Thread.currentThread().getId() +  "] updatePredicates update table: " + update);
	    }

	    synchronized (predicates) {
		synchronized (updatedPredicates) {
		    for (Iterator<byte[]> i = update.getEntryIdsIterator(); 
			 i.hasNext();) {
			byte[] name = i.next();
			PredicatesTableEntry u_entry = update.getEntry(name);
			/*
			 * Incremental update. If cleanup then we
			 * delete previous filters. Otherwise we just
			 * add
			 */
			if (u_entry.cleanUp) {
			    predicates.addEntry(name, new PredicatesTableEntry(u_entry));
			} else {
			    PredicatesTableEntry localEntry 
				= predicates.getEntry(name);
			    if (localEntry == null) {
				// it's a new entry, we just copy
				predicates.addEntry(name, u_entry);
			    } else {
				// update the seqNo
				localEntry.setFiltersSeqNo(u_entry.getFiltersSeqNo());
				// add filters
				localEntry.addFilters(u_entry.filters);
			    }
			}
		    }
		}
	    }

	    if (Logging.severity == Logging.DEBUG) {
		Logging.prlnlog(new String(my_identity) + " [" +  Thread.currentThread().getId() +  "] updatePredicates result table: " +  predicates);
	    }
	}
    }

    protected void handlePAD(SENPPacket req) {
	boolean triggerFwdTableUpdate = false;
	boolean forward = false;

	if (Logging.severity == Logging.DEBUG) {
	    Logging.prlnlog(new String(my_identity) + " [" + Thread.currentThread().getId() + "] handlePAD: packet from neighbor " +  new String(req.id) + " PAD content: " + req.predicate);
	}

	PredicatesTableEntry p = null;
	synchronized (predicatesUpdateLock) {
	    // get write locks
	    predicates.w.lock();
	    updatedPredicates.w.lock();
	    try {
		if ((p = req.predicate) != null
		    && neighbors.containsKey(req.id)) {
		    /*
		     * update predicates by taking the ones with the latest seq
		     * No.
		     */

		    // the key to the table is the destination ID
		    byte[] curId = p.getDest();

		    PredicatesTableEntry localEntry = predicates
			.getEntry(curId);
		    if (localEntry != null) {
			if (Logging.severity == Logging.DEBUG) {
			    Logging.prlnlog(new String(my_identity) + " [" + Thread.currentThread().getId() + "] handlePAD: entry " +  new String(curId) +  " was already present");
			}

			/*
			 * here we consider predicates and update them if they
			 * are newer than the ones we knew about
			 */
			if (p.getFiltersSeqNo() > localEntry.getFiltersSeqNo()) {
			    if (Logging.severity == Logging.DEBUG) {
				Logging.prlnlog( new String(my_identity) + " [" + Thread.currentThread().getId() + "] handlePAD: entry " +  new String(curId) +  " contains newer predicates");
			    }
			    if (p.getFiltersSeqNo() > localEntry
				.getFiltersSeqNo() + 1) {
				if (Logging.severity <= Logging.WARN) {
				    Logging.prlnlog( new String(my_identity) + " [" + Thread.currentThread().getId() + "] handlePAD: entry " +  new String(curId) +  " has too high seqNo (" + p.getFiltersSeqNo() + " vs. " + (localEntry .getFiltersSeqNo() + 1) + "): requeuing for later processing", Logging.WARN);
				}
				try {
				    ((MultiPacketReceiver) this.listener).inputQueue
					.add(req.buf, req.length());
				} catch (Exception e) {
				    e.printStackTrace(Logging.err);
				}
				return;
			    }
			    /*
			     * if it's a cleanup packet we delete everything
			     */
			    if (p.cleanUp) {
				if (Logging.severity == Logging.DEBUG) {
				    Logging.prlnlog(my_identity + "[" +  Thread .currentThread() .getId() +  "] handlePAD: entry " + new String(curId) +  " has cleanUp TRUE: deleting old filters");
				}

				localEntry.filters.clear();
				// also we have to set the cleanUp flag in the
				// updatedPredicates
				// just substituting with the empty set should
				// be enough
				updatedPredicates.addEntry(p.getDest(), p);

			    } else {
				if (Logging.severity == Logging.DEBUG) {
				    Logging.prlnlog( new String(my_identity) +  " [" +  Thread.currentThread().getId() + "] handlePAD: entry " + new String(curId) +  " will be updated");
				}
				/* else we just add */
				localEntry.addFilters(p.getFilters());

				if (updatedPredicates.getEntry(p.getDest()) == null) {
				    updatedPredicates.addEntry(p.getDest(), p);
				} else {
				    // update delta for read-only table
				    // update
				    updatedPredicates.getEntry(p.getDest())
					.addFilters(p.getFilters());
				}
			    }

			    triggerFwdTableUpdate = true;

			    /*
			     * update seq No for both write copy and delta
			     * update
			     */
			    localEntry.copyFiltersSeqNo(p);
			    updatedPredicates.getEntry(p.getDest())
				.copyFiltersSeqNo(p);

			    // forward packet to neighbors
			    forward = true;
			} else {
			    if (Logging.severity == Logging.DEBUG) {
				Logging.prlnlog(new String(my_identity) + "[" +  Thread.currentThread() .getId() + "] handlePAD: entry " + new String(curId) +  " does not contain newer predicates, it won't be forwarded");
			    }
			}

		    } else {
			/* add a entry for the neighbor */

			PredicatesTableEntry entry = new PredicatesTableEntry();
			entry.setDest(p.getDest());
			entry.addFilters(p.filters);
			entry.copyFiltersSeqNo(p);
			predicates.addEntry(p.getDest(), entry);

			/*
			 * add reference to the new predicates for the update
			 */
			updatedPredicates.addEntry(p.getDest(), p);
			triggerFwdTableUpdate = true;

			if (Logging.severity == Logging.DEBUG) {
			    Logging.prlnlog(new String(my_identity) + " [" +  Thread.currentThread() .getId() +  "] handlePAD: adding a new entry copying predicates for " + new String(p.getDest()));
			}

			// forward the packet
			forward = true;

		    }
		    if (triggerFwdTableUpdate) {
			/*
			 * we schedule an update of the predicates FW table
			 */
			heartbeat.queueUpdate(DVHeartbeat.PAD);
		    }
		    if (forward) {
			byte[] sender = req.id;
			req.id = my_identity;
			Iterator<byte[]> i = neighbors.keySet().iterator();
			while (i.hasNext()) {
			    byte[] key = i.next();
			    if (!Arrays.equals(key, p.getDest())
				&& !Arrays.equals(key, sender)) {
				Logging.prlnlog(new String(my_identity)
						+ " forwarding PAD to "
						+ new String(key), Logging.DEBUG);
				req.to = key;
				// set TTL again otherwise it gets to 0
				req.ttl = (byte) DVDRPDispatcher.DEFAULT_TTL_FOR_UDV;
				queuePacket(req, TwoPrioritiesQueue.HIGH);
			    }

			}
		    }

		} else {
		    Logging.prlnerr( new String(my_identity) + " [" +  Thread.currentThread().getId() +  "] received PAD from unknown neighbor: " +  new String(req.id), Logging.WARN);
		}
	    } finally {
		predicates.w.unlock();
		updatedPredicates.w.unlock();
	    }
	}
    }

    private void queueDistanceVectorUpdateTo(DVEntry localEntry, byte[] id,
					     int ttl) {
	DistanceVector dv = new DistanceVector();
	dv.addEntry(localEntry.getDest(), localEntry);
	queueDistanceVectorUpdateTo(dv, id, ttl);
    }

    private void queueDistanceVectorUpdateTo(DistanceVector dv, byte[] id,
					     int ttl) {

	SENPPacket pkt = SENPPacket.allocate();
	try {
	    pkt.method = SENP.UDV;
	    pkt.ttl = (byte) ttl;
	    pkt.id = my_identity;
	    pkt.to = id;
	    pkt.distanceVector = dv;
	    // TODO: deal with concurrent access
	    queuePacket(pkt, 1);
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} finally {
	    SENPPacket.recycle(pkt);
	}
    }

    /**
     * Stops the server instance
     */

    public synchronized void shutdown() {
	Logging.prlnlog(new String(my_identity) + " shutdown invoked");
	// DV/DRP specific stuff
	if (heartbeat != null) {
	    heartbeat.stopHeartbeat();
	    heartbeat = null;
	}
	if (timer != null) {
	    timer.cancel();
	    timer = null;
	}
	if (discoveryManager != null) {
	    discoveryManager.keepRunning = false;
	    discoveryManager.ms.close();
	}
	synchronized (neighbors) {
	    Iterator<byte[]> i = neighbors.keySet().iterator();
	    synchronized (i) {
		while (i.hasNext()) {
		    (neighbors.get(i.next())).shutdown();
		}
	    }
	    neighbors.clear();
	}
	// shutdown the rest
	super.shutdown();
    }

    /**
     * Adds a package addressed to a subscriber containing the whole local
     * distance vector to the queue of outgoing DV packages consumed by
     * <code>DispatcherThread</code>.
     * 
     * @param destination
     *            the Subscriber to which the DV will be sent
     * @param priority
     *            the priority of the message in the queue (1 high, 2 low)
     * 
     * @see DispatcherThread
     */
    public void queueDistanceVector(byte[] destination, int priority) {
	SENPPacket sp = SENPPacket.allocate();
	try {
	    sp.id = my_identity;
	    sp.to = destination;
	    sp.handler = listener.address();
	    sp.cost = (neighbors.get(destination)).interf.getCost();
	    sp.method = SENP.DV;
	    sp.distanceVector = this.fwTable;
	    sp.ttl = (byte) DVDRPDispatcher.DEFAULT_TTL_FOR_UDV;
	    queuePacket(sp, priority);
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} finally {
	    SENPPacket.recycle(sp);
	}
    }

    void queuePacket(SENPPacket req, int priority)
	throws ArrayIndexOutOfBoundsException {
	if (neighbors.get(req.to) != null) {
	    Logging.prlnlog(new String(my_identity) + " [" + Thread.currentThread().getId() + "] queuePacket queuing pckt: " + req.toString(), Logging.INFO);
	    try {
		(neighbors.get(req.to)).notify(req, priority);
		return;
	    } catch (ArrayIndexOutOfBoundsException outb) {
		Logging.prlnerr(new String(my_identity) + " could not encode packet (too long?) " + req.toString(), Logging.WARN);
		throw outb;
	    } catch (QueueFullException qfe) {
		Logging.prlnerr(new String(my_identity) + " could not queue packet " + req.toString() + " for " + new String(req.to) + ": " + qfe.getMessage(), Logging.WARN);
		return;
	    }
	} else {
	    // We get here only if network topology changed while we were
	    // preparing
	    // the packet
	    Logging.prlnerr(new String(my_identity) + " queuePacket invoked towards MISSING/UNREACHABLE neighbor " + req.toString(), Logging.WARN);
	    // We try to re-route it if it's a DRP packet
	    // FIXME: now that we realize a packet can't go one way when queuing
	    // we
	    // should directly reroute. No wait needed
	    if (req.method == SENP.DRP && req.ttl > 0) {
		try {
		    timer.schedule(new DRPPacketRerouter(this, req),
				   DEFAULT_WAIT_FOR_REROUTE);
		} catch (IllegalStateException ise) {
		    Logging.prlnerr(new String(my_identity) + " cannot schedule re-send, reason: " + ise.getMessage() + " " + req.toString(), Logging.WARN);
		}
	    }
	}
    }

    public void rerouteDRPPacket(SENPPacket req) {
	// We decrease TTL at each round to discard undeliverable packets
	req.ttl--;
	if (req.ttl > 0) {
	    partitionRecipients(req);
	}
	// otherwise packet gets dropped
    }

    protected void publish(SENPPacket req) {
	// set method to DRP for correct handling
	req.method = SENP.DRP;
	byte[] sender = null;
	if (req.id != null) {
	    sender = new byte[req.id.length];
	    System.arraycopy(req.id, 0, sender, 0, req.id.length);
	}
	// 1. find final recipients and add them to the recipient list of the
	// package
	// long time = System.nanoTime();
	boolean localNodeIsRecipient = findRecipientsAndForward(req);
	// long partitionTime = System.nanoTime();
	// set method back to PUB for local handling
	req.method = SENP.PUB;
	if (localNodeIsRecipient) {
	    req.id = sender;
	    super.publish(req);
	}
    }

    /**
     * Finds the list of interested recipients given a DRP package. It adds the
     * list to the recipients field of the package
     * 
     * @param req
     *            the DRP package for which recipients have to be identified
     * 
     * @see SENPPacket
     */

    private boolean findRecipientsAndForward(SENPPacket req) {
	Logging.prlnlog(new String(my_identity) + "[" + Thread.currentThread().getId() +  "] findRecipientsAndForward, predFwTable: " +  predFwTable, Logging.DEBUG);
	// long start = System.nanoTime();
	boolean localReceivers = false;
	req.bloomFilter = new BSetBV(req.event);
	req.recipients = new OrderedByteArraySet();
	Map<byte[], OrderedByteArraySet> recipientPartitions = new OrderedByteArrayMap<OrderedByteArraySet>();
	Iterator<byte[]> ite = predFwTable.getEntryIdsIterator();
	while (ite.hasNext()) {
	    byte[] name = ite.next();
	    PredicatesTableEntry entry = predFwTable.getEntry(name);
	    // do matching and partitioning at once
	    if (entry.covers(req.bloomFilter)) {
		if (!Arrays.equals(my_identity, name)) {
		    req.recipients.add(name);
		    Logging.prlnlog(new String(my_identity) + "[" + Thread.currentThread().getId() + "] adding recipient " + new String(name), Logging.DEBUG);
		    byte[] nextHop = fwTable.getEntry(name).getNextHopId();
		    // When routing information is propagated with UDP transport and links are
		    // unreliable, routing table can be incomplete due to lost link-state messages.
		    // In such cases we might end up with no "next hop" to reach a broker 
		    // in the network.
		    if(nextHop == null) {
		    	Logging.prlnlog(my_identity + " did not find any route to broker " + 
		    			new String(name) + " (lost route update?).", Logging.WARN);
		    	continue;
		    }
		    if (recipientPartitions.containsKey(nextHop)) {
			// then we add the current recipient to the already
			// existing partition
			recipientPartitions.get(nextHop).add(name);
		    } else {
			// we create a new partition and add the recipient
			OrderedByteArraySet partition;
			partition = new OrderedByteArraySet();
			partition.add(name);
			recipientPartitions.put(nextHop, partition);
		    }
		} else {
		    localReceivers = true;
		}
	    } else {
		Logging.prlnlog(new String(my_identity) + "[" + Thread.currentThread().getId() + "] no match for recipient " + new String(name), Logging.DEBUG);
	    }
	}
	// there's no need to send out the Bloom Filter on a notification
	req.bloomFilter = null;
	// long partitonend = System.nanoTime();
	queuePartitionedPacket(req, recipientPartitions);
	return localReceivers;
    }

    /**
     * This method is invoked each time a DRP request is received. The request
     * is eventually forwarded to local subscribers or neighbor servers
     * 
     * @param req
     *            the DRP request *
     * 
     * @see SENPPacket
     */

    protected void handleDRP(SENPPacket req) {
	// check if the message is also intended for local recipients
	boolean local = req.recipients.contains(my_identity);
	// first thing we do is forward the message to the following
	// neighbors performing dynamic recipient partitioning
	// long start = System.nanoTime();
	partitionRecipients(req);
	// long partition = System.nanoTime();
	// long prePub = partition;
	// long postPub = partition;

	// then we check whether we are in the recipient list
	if (local) {
	    // if we are one of the intended recipients we handle the request as
	    // we would with any
	    // PUB request: forward to master and to our known subscribers

	    // here we also check whether the Bloom Filter representation of the
	    // event
	    // is still covered by the local Bloom Filters. We use it to
	    // separate false
	    // positives due to Bloom Filter representation from false positives
	    // due to latency
	    BSetBV packetBF = new BSetBV(req.event);
	    PredicatesTableEntry localEntry = predFwTable.getEntry(my_identity);
	    boolean publish = false;
	    if (localEntry != null) {
		// synchronized (localEntry) {
		if (localEntry.covers(packetBF)) {
		    publish = true;
		}
		// }
	    }
	    // prePub = System.nanoTime();
	    if (publish) {
		// set method to PUB so that packet gets interpreted as a
		// publication
		req.method = SENP.PUB;
		super.publish(req);
		req.method = SENP.DRP;
	    } else {
		Logging.prlnlog( new String(my_identity) +  " no matching BF false positive: msgId=" +  req.event.getAttribute("msgId"), Logging.INFO);
	    }
	    // postPub = System.nanoTime();

	}
    }

    /**
     * Partitions the list of recipients of a DRP package and forwards the
     * package accordingly.
     * 
     * @param req
     *            the DRP package whose recipients have to be partitioned
     * 
     * @see SENPPacket
     */

    protected void partitionRecipients(SENPPacket req) {
	// here we access the DV table using the recipient id and
	// partition the set of recipients into subsets grouping by
	// interface
	if (req.recipients != null) {
	    Iterator<byte[]> i = req.recipients.iterator();
	    Map<byte[], OrderedByteArraySet> recipientPartitions = new OrderedByteArrayMap<OrderedByteArraySet>();
	    while (i.hasNext()) {
		byte[] name = i.next();
		if (!Arrays.equals(name, my_identity)) {
		    // we check whether we know a way to the destination
		    if (fwTable.getEntry(name) != null) {
			byte[] nextHop;
			nextHop = fwTable.getEntry(name).getNextHopId();
			if (recipientPartitions.containsKey(nextHop)) {
			    // then we add the current recipient to the already
			    // existing partition
			    recipientPartitions.get(nextHop).add(name);
			} else {
			    // we create a new partition and add the recipient
			    OrderedByteArraySet partition;
			    partition = new OrderedByteArraySet();
			    partition.add(name);
			    recipientPartitions.put(nextHop, partition);
			}
		    } else {
			// one of the intended recipients is unknown to us
			// this should not happen
			// TODO: fix this, find a way to reroute the packet
			Logging.prlnerr(new String(my_identity) + " [" + Thread.currentThread().getId() + "] partitionRecipients (destination unknown) cannot route to " + name + " pckt: " + req.toString(),Logging.WARN);
		    }

		}

	    }
	    queuePartitionedPacket(req, recipientPartitions);
	}
    }

    /**
     * Partitions the list of recipients of a DRP package and forwards the
     * package accordingly.
     * 
     * @param req
     *            the DRP package whose recipients have to be partitioned
     * 
     * @see SENPPacket
     */

    protected void partitionRecipientsAnyCast(SENPPacket req) {
	// here we access the DV table using the recipient id and
	// partition the set of recipients into subsets grouping by
	// interface
	boolean localNodeIsRecipient = false;
	byte[] sender = new byte[req.id.length];
	System.arraycopy(req.id, 0, sender, 0, req.id.length);
	if (req.recipients != null) {
	    Iterator<byte[]> i = req.recipients.iterator();
	    Map<byte[], OrderedByteArraySet> recipientPartitions = new OrderedByteArrayMap<OrderedByteArraySet>();
	    while (i.hasNext()) {
		byte[] name = i.next();
		if (!Arrays.equals(name, my_identity)) {
		    // we check wether we know a way to the destination
		    if (fwTable.getEntry(name) != null) {
			byte[] nextHop;
			synchronized (fwTable.getEntry(name)) {
			    nextHop = fwTable.getEntry(name).getNextHopId();
			}
			if (recipientPartitions.containsKey(nextHop)) {
			    // then we add the current recipient to the already
			    // existing
			    // partition
			    recipientPartitions.get(nextHop).add(name);
			} else {
			    // we create a new partition and add the recipient
			    OrderedByteArraySet partition;
			    partition = new OrderedByteArraySet();
			    partition.add(name);
			    recipientPartitions.put(nextHop, partition);
			}
		    } else {
			// one of the intended recipients is unknown to us
			// this should not happen
			// TODO: fix this, find a way to reroute the packet
			Logging.prlnerr( new String(my_identity) + " [" +  Thread.currentThread().getId() +  "] partitionRecipients cannot route to " + name + " pckt: " +  req.toString(), Logging.ERROR);
		    }
		} else {
		    // if current node was in recipient list we will restore
		    // the recipient field in the packet in the end
		    localNodeIsRecipient = true;
		}
	    }
	    if (recipientPartitions.size() != 0) {
		int mcc = req.mcc;
		int share = -1;
		if (mcc != 0) {
		    share = (int) Math.round(Math.ceil(mcc
						       / recipientPartitions.size()));
		}
		// then we forward on the interested interfaces
		req.id = my_identity;
		req.method = SENP.DRP;
		Iterator<byte[]> j = recipientPartitions.keySet().iterator();
		while (j.hasNext()) {
		    byte[] nextHop = j.next();
		    req.recipients = recipientPartitions.get(nextHop);
		    req.to = nextHop;
		    boolean send = true;
		    if (share != -1) {
			if (mcc >= share) {
			    // in this case we assign a share of the mcc to
			    // the
			    // packet
			    req.mcc = (byte) share;
			    mcc -= share;
			} else if (mcc > 0) {
			    // in this case we assign what's left to the
			    // packet
			    req.mcc = (byte) mcc;
			    mcc = 0;
			} else {
			    // in this case we don't send the packet
			    send = false;
			}
		    }
		    if (send) {
			try {
			    queuePacket(req, TwoPrioritiesBufferQueue.HIGH);
			} catch (Exception e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
			}
		    }
		}
	    }
	    if (localNodeIsRecipient) {
		req.recipients.clear();
		req.recipients.add(my_identity);
		req.id = sender;
	    }
	}
    }

    private void queuePartitionedPacket(SENPPacket req,
					Map<byte[], OrderedByteArraySet> recipientPartitions) {
	if (recipientPartitions.size() != 0) {
	    // then we forward on the interested interfaces
	    req.id = my_identity;
	    req.method = SENP.DRP;
	    Iterator<byte[]> j = recipientPartitions.keySet().iterator();
	    while (j.hasNext()) {
		byte[] nextHop = j.next();
		req.recipients = recipientPartitions.get(nextHop);
		req.to = nextHop;
		try {
		    queuePacket(req, TwoPrioritiesQueue.HIGH);
		} catch (Exception e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }
	}
    }

    protected void configure(SENPPacket req) {
	Logging.prlnlog(new String(my_identity) + " [" + Thread.currentThread().getId() + "] reconfigure: received reconfiguration request from " + new String(req.id), Logging.DEBUG);
	if (req.ttl == 0 | req.id == null) {
	    Logging.prlnlog(new String(my_identity) + " reconfigure: ttl = " + req.ttl + " or id = " + new String(req.id), Logging.WARN);
	}
	if (req.handler == null) {
	    //
	    // we interpret req.handler == null as a disconnection
	    // from the given neighbor id.
	    //
	    Logging.prlnlog(new String(my_identity) + " [" + Thread.currentThread().getId() + "] reconfigure: disconnecting from " + new String(req.id), Logging.DEBUG);

	    brokenLink(req.id);
	} else {
	    Logging.prlnlog(new String(my_identity) + " [" + Thread.currentThread().getId() + "] reconfigure: adding neighbor " + new String(req.id) + " address: " + new String(req.handler), Logging.INFO);

	    synchronized (neighbors) {
		NeighborNode s = neighbors.get(req.id);
		try {
		    if (s == null) {
			// s is a new neighbor
			s = new NeighborNode(req.id, 
					     ifmanager.get(req.handler), 
					     this);
			if (req.cost != 0) {
			    s.interf.setCost(req.cost);
			} else {
			    s.interf.setCostFromSender();
			}
			s.addRef();
			neighbors.put(req.id, s);
		    } else {
			// s is an existing neighbor, so we update its
			// connection handler
			s.mapHandler(ifmanager.get(req.handler));
			// and update the link cost
			if (req.cost != 0) {
			    s.interf.setCost(req.cost);
			} else {
			    s.interf.setCostFromSender();
			}
		    }
		    // then we send the new neighbor our distance
		    // vector with priority 1
		    queueDistanceVector(req.id, 1);
		} catch (InvalidSenderException ex) {
		    Logging.prlnerr("configure: failed reconfiguration request: " + ex, Logging.ERROR);
		}
	    }
	}
    }

    private synchronized void brokenLink(byte[] nodeId) {
	/*
	 * we update the ex-neighbor DVEntry plus all entries that had it as a
	 * next hop
	 */
	synchronized (distanceVector) {
	    DVEntry dve = distanceVector.getEntry(nodeId);
	    if (dve != null) {
		dve.setDist(DEFAULT_MAX_COST);
		// notify neighbors of the lost link
		// this.queueDistanceVectorUpdateExceptTo(dve, nodeId);
		updatedDV.addEntry(nodeId, dve);
	    }
	    Iterator<byte[]> j = distanceVector.getEntryIdsIterator();
	    while (j.hasNext()) {
		byte[] id = j.next();
		if (Arrays.equals(distanceVector.getEntry(id).getNextHopId(),
				  nodeId)) {
		    distanceVector.getEntry(id).setDist(DEFAULT_MAX_COST);
		    // queueDistanceVectorUpdateExceptTo(distanceVector.getEntry(id),
		    // nodeId);
		    updatedDV.addEntry(id, distanceVector.getEntry(id));
		}
	    }
	    heartbeat.queueUpdate(DVHeartbeat.UDV);

	    // we remove the entry from neighbors
	    NeighborNode nn = neighbors.get(nodeId);
	    if (nn != null) {
		nn.shutdown();
		neighbors.remove(nodeId);
	    }
	}
    }

    protected void unsubscribe(Filter f, Subscriber s, SENPPacket req) {
	super.unsubscribe(f, s, req);
	if (sff) {
	    if (f != null) {
		checkForPredicatesUpdate(f, false);
	    } else {
		subscriptionsMap.remove(s.getKey());
		recomputeLocalPredicates();
	    }			
	} else {
	    // TODO: We need to find a way to remove the appropriate
	    // subscriptions
	    // from the local DVEntry.
	    // for the moment we recompute the DVEntry
	    recomputeLocalPredicates();
	}
    }

    public void recomputeLocalPredicates() {
	Logging.prlnlog(new String(my_identity) + " recomputeLocalPredicates", Logging.DEBUG);
	synchronized (predicatesUpdateLock) {
	    // get write locks
	    predicates.w.lock();
	    updatedPredicates.w.lock();
	    try {
		PredicatesTableEntry localEntry = predicates
		    .getEntry(my_identity);
		PredicatesTableEntry temp = new PredicatesTableEntry();
		temp.setDest(my_identity);

		/*
		 * all filters are going to be cleaned up, first in the write
		 * table, then in the read table. We use a temporary table: - we
		 * will copy its content to the write table - we will switch the
		 * pointer to the new table when the read table update happens
		 */
		// we delete current filters
		localEntry.getFilters().clear();

		if (sff) {
		    /*
		     * Clean localEntry and add BSets one at a time considering
		     * coverage
		     */
		    // synchronized (localEntry.filters) {
		    synchronized (subscriptionsMap) {
			for (SimplePredicate filters : subscriptionsMap
				 .values()) {
			    for (siena.fwd.Filter f : filters) {
				/*
				 * if entry does not cover bf (that is bf is
				 * more general), bf is added and the covered
				 * filters have to be removed
				 */
				if (!localEntry
				    .covers(((Filter) f).bloomFilter)) {
				    localEntry.addBSet(((Filter) f));
				    temp.addBSet((Filter) f);
				}
			    }
			}
		    }
		    // }

		} else {
		    // synchronized (localEntry) {
		    // synchronized (subscriptions) {
		    try {
			Iterator<Subscription> i;
			i = subscriptions.rootsIterator();
			while (i.hasNext()) {
			    Subscription sub = i.next();
			    localEntry.getFilters().add(new BSetBV(sub.filter));
			    temp.getFilters().add(new BSetBV(sub.filter));
			}
		    } catch (ConcurrentModificationException cme) {
			// This should not happen
			cme.printStackTrace();
		    }
		}

		// }
		//
		// }

		/*
		 * We substitute the update since we restarted from scratch. we
		 * set the clear flag so that the read only copy gets also
		 * cleared at the next switch
		 */
		temp.cleanUp = true;
		updatedPredicates.addEntry(my_identity, temp);
		if (Logging.severity == Logging.DEBUG) {
		    Logging.prlnlog( new String(my_identity) +  " recomputeLocalPredicates, new localEntry: " + localEntry, Logging.DEBUG);
		    Logging.prlnlog( new String(my_identity) +  " recomputeLocalPredicates, new update table: " + updatedPredicates, Logging.DEBUG);
		}
	    } finally {
		predicates.w.unlock();
		updatedPredicates.w.unlock();
	    }
	}

	// queue update to neighbors
	heartbeat.queueUpdate(DVHeartbeat.PAD);

    }

    protected void subscribe(Filter f, Subscriber s, SENPPacket req)
	throws SienaException {
	if (sff) {
	    // manage SFFTable update
	    SimplePredicate actorSubscriptions = null;
	    synchronized (subscriptionsMap) {
		actorSubscriptions = subscriptionsMap.get(s);
		if (actorSubscriptions == null) {
		    actorSubscriptions = new SimplePredicate();
		    subscriptionsMap.put(s, actorSubscriptions);
		}

		// synchronized (actorSubscriptions) {
		actorSubscriptions.add(f);
		// }
		// Logging.prlnlog(new String(my_identity) + " adding for " +
		// s.getKey() + " filter " + f);
	    }
	    // take care of refs or contactTable gets cleared
	    s.addRef();
	    // was: call to old SFF table update
	    // heartbeat.queueSubscriptionUpdate();
	    // now: use PAD
	    heartbeat.queueUpdate(DVHeartbeat.PAD);
	    // check if local predicates need to be updated
	    checkForPredicatesUpdate(f, true);
	} else {
	    Subscription sub = subscriptions.insert_subscription(f, s);

	    if (sub == null)
		return;

	    if (subscriptions.is_root(sub)) {
		synchronized (predicatesUpdateLock) {
		    // get write locks
		    predicates.w.lock();
		    updatedPredicates.w.lock();
		    try {

			/*
			 * we add the subscription information to a temporary
			 * entry
			 */
			PredicatesTableEntry tempEntry = new PredicatesTableEntry();
			tempEntry.setDest(my_identity);
			// add filter to DVEntry

			BSetBV bf = new BSetBV(f);
			tempEntry.getFilters().add(bf);
			Logging.prlnlog( new String(my_identity) + " " +  Thread.currentThread() +  " BF for filter " + f + " is: " + bf, Logging.DEBUG);

			/*
			 * we update the delta
			 */
			if (updatedPredicates.getEntry(my_identity) == null) {
			    updatedPredicates.addEntry(my_identity, tempEntry);
			} else {
			    updatedPredicates.getEntry(my_identity).addFilters(
									       tempEntry.filters);
			}
			/*
			 * we update the write table of predicates
			 */
			if (predicates.getEntry(my_identity) == null) {
			    predicates.addEntry(my_identity, tempEntry);
			} else {
			    predicates.getEntry(my_identity).addFilters(
									tempEntry.filters);
			}
			heartbeat.queueUpdate(DVHeartbeat.PAD);
		    } finally {
			predicates.w.unlock();
			updatedPredicates.w.unlock();
		    }
		}
	    }
	}
    }

    private void checkForPredicatesUpdate(Filter f, boolean add) {
	if (Logging.severity == Logging.DEBUG) {
	    Logging.prlnlog(new String(my_identity) + " [" + Thread.currentThread().getId() + "] checkForPredicatesUpdate: " + f +  " add: " + add, Logging.DEBUG);
	}

	if (f.bloomFilter == null) {
	    BSetBV bf = new BSetBV(f);
	    f.setBloomFilter(bf);
	}

	synchronized (predicatesUpdateLock) {
	    // get write locks
	    predicates.w.lock();
	    updatedPredicates.w.lock();
	    try {

		if (add) {
		    /*
		     * bf addition: that is, if the bf is covered by any in the
		     * predicates there's no need to update
		     */

		    /*
		     * we use an incremental approach. Main idea: - add new BFs
		     * to a temporary table - add them to the updatedPred - add
		     * them to the write table
		     */

		    PredicatesTableEntry temp = new PredicatesTableEntry();
		    temp.setDest(my_identity);

		    PredicatesTableEntry localEntry = predicates
			.getEntry(my_identity);

		    if (!localEntry.covers(f.bloomFilter)) {
			/*
			 * in this case it means the filter we're trying to add
			 * is not covered by a more general one in the
			 * localEntry: it's expressing a new interest. We need
			 * to add it, but we first need to check whether it is
			 * more general than some other filter we already have,
			 * in this case we need to remove the latter
			 */

			if (Logging.severity == Logging.DEBUG) {
			    Logging.prlnlog( new String( my_identity) + " [" +  Thread .currentThread() .getId() +  "] checkForPredicatesUpdate new predicate needs to be added", Logging.DEBUG);
			}

			/*
			 * check it is not more general than some other filter
			 */

			/*
			 * we need to test whether the given filter is more
			 * general than the ones in the DVEntry
			 */
			for (BSetBV lf : localEntry.filters) {
			    if (lf.covers(f.bloomFilter)) {
				if (Logging.severity == Logging.DEBUG) {
				    Logging.prlnlog( new String( my_identity) + "[" +  Thread .currentThread().getId() +  "] checkForPredicatesUpdate recomputing entry, new filter is more general than one we had" + f, Logging.DEBUG);
				}
				recomputeLocalPredicates();
				return;
			    }
			}

			/*
			 * if it wasn't more general we just add it
			 */

			temp.addBSet(f);
			// add to write table
			localEntry.addBSet(f);
			// queue update of read table and notification to
			// neighbors
			if (updatedPredicates.getEntry(my_identity) == null) {
			    updatedPredicates.addEntry(my_identity, temp);
			} else {
			    updatedPredicates.getEntry(my_identity).addFilters(
									       temp.filters);
			}
			heartbeat.queueUpdate(DVHeartbeat.PAD);
			if (Logging.severity == Logging.DEBUG) {
			    Logging.prlnlog( new String( my_identity) + " [" +  Thread .currentThread() .getId() +  "] checkForPredicatesUpdate adding filter which was not covered: " + f, Logging.DEBUG);
			}
		    } else {
			if (Logging.severity == Logging.DEBUG) {
			    Logging.prlnlog( new String( my_identity) + " [" +  Thread .currentThread() .getId() +  "] checkForPredicatesUpdate new predicate is already covered, do nothing", Logging.DEBUG);
			}
		    }

		} else {
		    /*
		     * bf removal: - if the bf was in the predicates then we
		     * must recompute it - else nothing changes
		     */
		    PredicatesTableEntry localEntry = predicates
			.getEntry(my_identity);
		    if (Logging.severity == Logging.DEBUG) {
			Logging.prlnlog(new String(my_identity) + " [" +  Thread.currentThread() .getId() +  "] checkForPredicatesUpdate BF removal: recomputing predicates", Logging.DEBUG);
		    }

		    if (localEntry.filters.contains(f.bloomFilter)) {
			// call entry re-computation
			recomputeLocalPredicates();
		    }

		}
	    } finally {
		predicates.w.unlock();
		updatedPredicates.w.unlock();
	    }
	}

    }

    private void initLocalEntries() {
	DVEntry localEntry = new DVEntry();
	// we set common parameters
	localEntry.setDest(my_identity);
	localEntry.setDist(0); // distance = 0
	// TODO: or should we leave it to null?
	localEntry.setNextHopId(my_identity);
	synchronized (distanceVector) {
	    distanceVector.addEntry(my_identity, localEntry);
	}
	fwTable.addEntry(my_identity, new DVEntry(localEntry));

	PredicatesTableEntry localPEntry = new PredicatesTableEntry();
	// we set common parameters
	localPEntry.setDest(my_identity);
	synchronized (predicates) {
	    predicates.addEntry(my_identity, localPEntry);
	}
	predFwTable
	    .addEntry(my_identity, new PredicatesTableEntry(localPEntry));
    }

    /**
     * Note: this method was taken as is from HierarchicalDispatcher plus we
     * added removal from neighbors. The other option would be to convert it to
     * a method that returns boolean in HierarchicalDispatcher and change all
     * its invocations
     */
    synchronized void removeUnreachableSubscriber(Subscriber sub) {
	if (MaxFailedConnectionsNumber < 0 && MaxFailedConnectionsDuration < 0)
	    return;
	if (sub != null) {
	    boolean to_remove = false;
	    if (MaxFailedConnectionsNumber >= 0
		&& MaxFailedConnectionsDuration >= 0) {
		//
		// pay attention to both time and count (conjunction)
		//
		if (sub.getMillisSinceGood() > MaxFailedConnectionsDuration
		    && sub.getFailedAttempts() > MaxFailedConnectionsNumber)
		    to_remove = true;
	    } else if (MaxFailedConnectionsNumber >= 0) {
		if (sub.getFailedAttempts() > MaxFailedConnectionsNumber)
		    to_remove = true;
	    } else if (MaxFailedConnectionsDuration >= 0) {
		if (sub.getMillisSinceGood() > MaxFailedConnectionsDuration)
		    to_remove = true;
	    }
	    if (to_remove) {
		Logging.prlnlog(new String(my_identity) +  " removing unreachable subscriber " +  sub.toString(), Logging.INFO);
		brokenLink(sub.getIdentity());
		super.unsubscribe(null, sub, null);
	    }
	}
    }

    public void cleanUpDVEntry(byte[] destId) {
	Logging.prlnlog(new String(my_identity) + " cleaning up " + new String(destId) +  " DV entry", Logging.DEBUG);
	DVEntry dve = distanceVector.getEntry(destId);
	/*
	 * TODO: uncomment the rest to cleanup DV: removed for permanent
	 * subscriptions
	 */
	if (dve != null
	    && dve.getEntryTS() < (System.currentTimeMillis() - (DVHeartbeat.DEFAULT_NUM_CYCLES * this.dvDispatchPeriod))) {
	    synchronized (dve) {
		distanceVector.removeEntry(destId);
		fwTable.removeEntry(destId);
		Logging.prlnlog(new String(my_identity) + " removing DV entry: " + destId, Logging.DEBUG);
	    }
	}
    }

    public void setHeartbeat(long heartbeat) {
	this.dvDispatchPeriod = heartbeat;
    }

    public void setChoke(int chokePeriod) {
	this.chokePeriod = chokePeriod;
    }

    public synchronized void setReceiver(PacketReceiver r, int threads) {
	// TODO Auto-generated method stub
	super.setReceiver(r, threads);
    }

    public synchronized void setReceiver(PacketReceiver r) {
	// TODO Auto-generated method stub
	super.setReceiver(r);
    }

    @Override
	protected void subscribe(SENPPacket req) throws InvalidSenderException,
							SienaException {
	if (req.filter == null && req.pattern == null) {
	    //
	    // null filters/patterns are not allowed in subscriptions
	    // this is a design choice, we could accept null filters
	    // with the semantics of the universal filter: one that
	    // matches every notification
	    //
	    Logging.prlnerr("subscribe: null filter/pattern in subscription");
	    return;
	}

	// Monitor.subscribe(req.id, my_identity);
	synchronized (this) {
	    Subscriber s = map_subscriber(req);
	    if (s == null) {
		Logging.prlnerr("subscribe: unknown subscriber: "
				+ req.toString());
		return;
	    }
	    if (req.filter != null) {
		subscribe(req.filter, s, req);
	    }
	    // else {
	    // //
	    // // must be req.pattern != null
	    // //
	    // super.subscribe(req.pattern, s);
	    // }
	}

    }

}

class NeighborNode extends RemoteSubscriber {

    final int threads = 1;

    int queueSize = 1000;

    HierarchicalDispatcher server;

    TwoPrioritiesStaticBufferQueue queue;

    DispatcherThread[] dispatchers = new DispatcherThread[threads];

    @Override
	public synchronized boolean notify(SENPPacket req) {
	    try {
		return notify(req, TwoPrioritiesQueue.HIGH);
	    } catch (QueueFullException e) {
		Logging.prlnerr(new String(server.my_identity) +  " could not queue packet " + req.toString() +  " for " + new String(req.to) + ": " +  e.getMessage(), Logging.WARN);
	    }
	    return false;
	}

    public synchronized boolean notify(SENPPacket req, int priority)
	throws ArrayIndexOutOfBoundsException, QueueFullException {
	int length = req.encode();
	queue.add(req.buf, length, priority);
	return true;
    }

    public NeighborNode(byte[] id, Interface interf,
			HierarchicalDispatcher server) {
	super(id, interf);
	this.server = server;
	queue = new TwoPrioritiesStaticBufferQueue(queueSize, 5);
	for (int i = 0; i < threads; i++) {
	    dispatchers[i] = new DispatcherThread(server, this);
	    dispatchers[i].startDispatcher();
	    dispatchers[i].start();
	}
    }

    public NeighborNode(byte[] id, Interface interf,
			HierarchicalDispatcher server, int queueSize) {
	super(id, interf);
	this.queueSize = queueSize;
	queue = new TwoPrioritiesStaticBufferQueue(queueSize, 5);
	this.server = server;
	for (int i = 0; i < threads; i++) {
	    dispatchers[i] = new DispatcherThread(server, this);
	    dispatchers[i].startDispatcher();
	    dispatchers[i].start();
	}
    }

    public void shutdown() {
	queue.clear();
	// queue = null;
	for (int i = 0; i < threads; i++) {
	    dispatchers[i].stopDispatcher();
	    dispatchers[i] = null;
	}
	super.shutdown();
    }

    public void send(byte[] msg) throws PacketSenderException {
	if (msg != null && this.interf != null) {
	    // System.out.println("Sending2 packet " + new String(msg));
	    this.interf.send(msg);

	    // PROBE 
	    // Logging.prlnlog(new String(server.my_identity)+ " X-OUT-X " + new String(msg).substring(0,200), Logging.ERROR);
	}
    }

    public void send(byte[] msg, int length) throws PacketSenderException {
	if (msg != null && this.interf != null) {
	    // System.out.println("Sending packet " + new String(msg,0,length));
	    this.interf.send(msg, length);
      
	    // PROBE 
	    //Logging.prlnlog(new String(server.my_identity)+ " X-OUT-X " + new String(msg).substring(0,200), Logging.ERROR);
	}
    }
}

class DRPPacketRerouter extends TimerTask {
    DVDRPDispatcher server = null;

    SENPPacket packet = null;

    public DRPPacketRerouter(DVDRPDispatcher server, SENPPacket packet) {
	this.server = server;
	this.packet = SENPPacket.allocate();
	this.packet.copyOf(packet);
    }

    public void run() {
	server.rerouteDRPPacket(packet);
	SENPPacket.recycle(packet);
    }
}
