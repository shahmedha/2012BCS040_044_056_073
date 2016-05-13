//
//  This file is part of Siena, a wide-area event notification system.
//  See http://www.inf.usi.ch/carzaniga/siena/
//
//  Author: Giovanni Toffetti Carughi
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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Iterator;

import siena.dvdrp.DistanceVector;
import siena.dvdrp.PredicatesTable;
import siena.dvdrp.PredicatesTableEntry;
import siena.dvdrp.TwoPrioritiesQueue;

/**
 * implementation of a heartbeat thread for queuing distance vector messages to
 * neighbor nodes.
 * 
 */

/*
 * big change: we no longer use the heartbeat thread for
 * HierarchicalDispatcher (SFFHeartbeat). We implement the same logic
 * for SFF table update with locking as for PADs
 */
public class DVHeartbeat implements Runnable {

    public static int DEFAULT_NUM_CYCLES = 5; // number of periods to wait to
    // clean up DVEntries
    public static int DEFAULT_CHOKE_PERIOD = 250; // in milliseconds
    protected int chokePeriod = DEFAULT_CHOKE_PERIOD;
    private int numCycles = DEFAULT_NUM_CYCLES;
    private volatile DVDRPDispatcher server = null;
    private boolean doDiscovery = false;
    public static final byte DV = 1;
    public static final byte CLEANUP = 2;
    public static final byte UDV = 3;
    public static final byte PAD = 4;
    public static final byte MULTICAST = 5;
    public static int MAX_PREDICATES_PER_PAD = 800;

    protected volatile boolean keepRunning;
    protected Thread localThread;
    protected TaskQueue queue = new TaskQueue();

    /*
     * sequence number sent out at each predicate update notification. No need
     * to be thread-safe with this one, it is accessed by a single thread
     */
    public long filterSeqNo = 0;

    public DVHeartbeat(DVDRPDispatcher server, int chokePeriod) {

	// super(server, chokePeriod);
	this.server = server;
	this.chokePeriod = chokePeriod;
    }

    public DVHeartbeat(DVDRPDispatcher server) {
	// super(server);
	this.server = server;
    }

    public void startHeartbeat() {
	keepRunning = true;
	localThread = new Thread(this);
	localThread.start();
    }

    public void stopHeartbeat() {
	keepRunning = false;
	synchronized (queue) {
	    queue.clear();
	}
	localThread.interrupt();
    }

    /**
     * Init discovers what listener the server is using. This is used to decide
     * the tasks that are going to be performed
     * 
     */
    public void init() {
	// we're trying to discover whether we're using multiradio or not
	// we do this by checking the type of the listener

	while (server.listener == null) { // wait for a listener to be set
	    try {
		Thread.sleep(1000);
	    } catch (InterruptedException e) {
		// TODO Auto-generated catch block e.printStackTrace(); }
	    }
	}

	// Logging.prlnlog("DVHEartbeat listener address: "
	// + new String(server.listener.address()), Logging.DEBUG);
	if (server.discoveryManager != null) {
	    doDiscovery = true;
	    // set the sender factory
	    // Logging.prlnlog(new String(server.my_identity)
	    // + " setting sender factory", Logging.DEBUG);
	    // server.ifmanager.setSenderFactory(new PlasticSenderFactory(
	    // server.listener));

	    // set multicast to be performed ASAP
	    queue.put(Long.valueOf(System.currentTimeMillis()), Integer
		      .valueOf(DVHeartbeat.MULTICAST));
	    // we use multicast to propagate heartbeat DVs instead of the
	    // regular interface queues
	} else {
	    // set DV to be sent out with the configured period
	    queue.put(Long.valueOf(System.currentTimeMillis()
				   + SFFHeartbeat.randPoissonDelta(server.dvDispatchPeriod)),
		      Integer.valueOf(DVHeartbeat.DV));
	}
	// every 5 times the configured period is over we do a cleanup
	queue.put(Long.valueOf(System.currentTimeMillis()
			       + (numCycles * SFFHeartbeat
				  .randPoissonDelta(server.dvDispatchPeriod))), Integer
		  .valueOf(DVHeartbeat.CLEANUP));
    }

    public void run() {
	/*
	 * We need to find out what exactly this thread needs to do: - whether
	 * it is just sending DV and UDVs - or if we are using MRN and multicast
	 * as well
	 */
	this.init();
	SENPPacket sp = SENPPacket.allocate();
	sp.id = server.my_identity;
	// sp.to = destination;
	sp.handler = server.listener.address();
	while (keepRunning) {
	    // Logging.prlnlog(new String(server.my_identity)
	    // + " heartbeat run queue: " + queue, Logging.DEBUG);
	    if (!queue.isEmpty()) {
		long nextTaskTime;
		synchronized (queue) {
		    // check what is the next task
		    nextTaskTime = (queue.keySet().iterator().next()).longValue();
		}
		try {
		    long sleepTime = nextTaskTime - System.currentTimeMillis();
		    if (sleepTime > 0) {
			// Logging
			// .prlnlog(
			// new String(server.my_identity)
			// + " heartbeat sleeping till next task, for millis:"
			// + (sleepTime), Logging.DEBUG);
			Thread.sleep(sleepTime);
		    }
		} catch (InterruptedException e) {
		    // we get awakened whether there's a new task in the
		    // queue
		    // or whether we shut down
		    // in the latter case the queue should have been emptied
		    // and
		    // we
		    // return
		    // Logging.prlnlog(new String(server.my_identity)
		    // + " heartbeat got awakened", Logging.DEBUG);
		    if (queue.isEmpty() && !keepRunning) {
			return;
		    }
		    // otherwise we go back restarting the cycle
		    continue;
		}
		// we get here after eventually sleeping
		int task;
		long taskTime;
		synchronized (queue) {
		    taskTime = (queue.keySet().iterator().next()).longValue();
		    // here we wake up from the sleep and have to execute a task
		    task = (queue.get(Long.valueOf(taskTime))).intValue();
		}
		// Logging.prlnlog(new String(server.my_identity)
		// + " heartbeat executing task " + task, Logging.DEBUG);
		switch (task) {
		case (DVHeartbeat.DV):
		    // set next task
		    queue.put(Long.valueOf(System.currentTimeMillis() + SFFHeartbeat.randPoissonDelta(server.dvDispatchPeriod)),
			     Integer.valueOf(DVHeartbeat.DV));
		    // send out DV to all neighbors

		    synchronized (server.neighbors) {
			Iterator<byte[]> i = server.neighbors.keySet().iterator();
			if (i.hasNext()) {
			    while (i.hasNext() && keepRunning) {
				byte[] key = i.next();
				sp.cost = (server.neighbors.get(key)).interf.getCost();
				sp.to = key;
				sp.method = SENP.DV;
				sp.distanceVector = server.fwTable;
				sp.ttl = (byte) DVDRPDispatcher.DEFAULT_TTL_FOR_UDV;
				try {
				    server.queuePacket(sp,
						       TwoPrioritiesQueue.LOW);
				} catch (Exception e) {
				    e.printStackTrace(Logging.err);
				    Logging.prlnerr(new String(server.my_identity) + " could not queue DV packet: " + e.getMessage(), Logging.ERROR);
				    // TODO: come up with a fix in case DVs do
				    // not fit packets
				}
			    }
			}
		    }
		    break;
		case (DVHeartbeat.UDV):
		    // switch DistanceVector with ForwardingTable
		    synchronized (server) {
			synchronized (server.updatedDV) {
			    DistanceVector temp = server.fwTable;
			    server.fwTable = server.distanceVector;
			    server.distanceVector = temp;
			    // update new DistanceVector with the changes in the
			    // last choke period
			    server.updateDistanceVector(server.updatedDV);
			    // send out UDV to all neighbors

			    sp.method = SENP.UDV;
			    // sp.distanceVector = server.updatedDV;
			    DistanceVector dv = new DistanceVector();
			    sp.ttl = (byte) DVDRPDispatcher.DEFAULT_TTL_FOR_UDV;
			    synchronized (server.neighbors) {
				Iterator<byte[]> i = server.neighbors.keySet()
				    .iterator();
				while (i.hasNext() && keepRunning) {
				    byte[] key = i.next();
				    dv.clear();
				    // add to the packet DV only information
				    // that
				    // (supposedly)
				    // was not received from the current packet
				    // recipient
				    // we use nextHop to discriminate

				    Iterator<byte[]> j = server.updatedDV
					.getEntryIdsIterator();
				    while (j.hasNext()) {
					byte[] entryId = j.next();
					if (!Arrays.equals(key, server.updatedDV.getEntry(entryId).getNextHopId())) {
					    dv.addEntry(entryId,server.updatedDV.getEntry(entryId));
					}
				    }
				    // send only if DV contains something
				    if (dv.size() > 0) {
					// add dv to packet
					sp.distanceVector = dv;
					// Logging.prlnlog(new String(
					// server.my_identity)
					// + " heartbeat queuing UDV to "
					// + new String(key), Logging.DEBUG);
					sp.to = key;
					sp.cost = (server.neighbors.get(key)).interf.getCost();
					// BUGFIX: try encoding the packet and
					// catch
					// exceptions
					// if buffer is full
					try {
					    server.queuePacket(sp, TwoPrioritiesQueue.HIGH);
					} catch (ArrayIndexOutOfBoundsException outta) {
					    // this should not happen
					    outta.printStackTrace(Logging.err);
					    Logging.prlnerr(new String(server.my_identity) + " could not queue UDV packet: " + outta.getMessage(), Logging.ERROR);
					}
				    }
				}
			    }
			    // clean up Updated DV
			    server.updatedDV.clear();
			}
		    }
		    break;

		case (DVHeartbeat.PAD):
		    sp.distanceVector = null;
		    // switch DistanceVector with ForwardingTable
		    // System.err.println(new String(server.my_identity) +
		    // " start");
		    // System.err.println("pred: " + server.predicates);
		    // System.err.println("predFwd: " + server.predFwTable);
		    // System.err.println("update: " +
		    // server.updatedPredicates);

		    // updates local SFF FWDTable
		    synchronized (server) {
			server.updateFWTable();
		    }

		    /*
		     * keep a temp reference to the local predicates
		     */
		    PredicatesTableEntry localUpdatePred = server.updatedPredicates
			.getEntry(server.my_identity);

		    /*
		     * lock all
		     */
		    synchronized (server.predicatesUpdateLock) {
			synchronized (server.predicatesFWDLock) {

			    server.predicates.w.lock();
			    server.updatedPredicates.w.lock();
			    server.predFwTable.w.lock();
			    try {
				PredicatesTable temp = server.predFwTable;
				server.predFwTable = server.predicates;
				server.predicates = temp;
				// update new predicates table with the changes
				// in
				// the
				// last choke period
				server
				    .updatePredicates(server.updatedPredicates);
				// clean up Updated predicates
				server.updatedPredicates.clear();
			    } finally {
				server.predicates.w.unlock();
				server.updatedPredicates.w.unlock();
				server.predFwTable.w.unlock();
			    }

			}
		    }

		    if (localUpdatePred != null) {
			// send out local PAD to all neighbors
			// remote PADs are immediately forwarded

			sp.method = SENP.PAD;
			sp.ttl = (byte) DVDRPDispatcher.DEFAULT_TTL_FOR_UDV;

			/*
			 * because of the incremental updates, we check whether
			 * the cleanUp flag was set. If it is, we first need to
			 * send out an empty PAD, to have all our previous
			 * predicates cleared
			 */

			if (localUpdatePred.cleanUp) {

			    sp.predicate = new PredicatesTableEntry();
			    sp.predicate.setDest(server.my_identity);
			    sp.predicate.setFiltersSeqNo(++filterSeqNo);

			    synchronized (server.neighbors) {
				Iterator<byte[]> i = server.neighbors.keySet()
				    .iterator();
				while (i.hasNext() && keepRunning) {
				    byte[] key = i.next();

				    // Logging.prlnlog(new String(
				    // server.my_identity)
				    // + " heartbeat queuing empty PAD to "
				    // + new String(key), Logging.DEBUG);
				    sp.to = key;

				    try {
					server.queuePacket(sp,
							   TwoPrioritiesQueue.HIGH);
				    } catch (ArrayIndexOutOfBoundsException outta) {
					// this should not happen, as
					// predicates are empty
					// outta.printStackTrace(Logging.err);
					Logging
					    .prlnerr(
						     new String(
								server.my_identity)
						     + " could not queue PAD packet: "
						     + outta
						     .getMessage(),
						     Logging.ERROR);
				    }
				}
			    }

			}

			/*
			 * Now we send out the actual update
			 */
			sp.predicate = localUpdatePred;
			// remember the sequenceNo
			sp.predicate.setFiltersSeqNo(++filterSeqNo);
			synchronized (server.neighbors) {
			    Iterator<byte[]> i = server.neighbors.keySet()
				.iterator();
			    while (i.hasNext() && keepRunning) {
				byte[] key = i.next();

				Logging.prlnlog(new String(server.my_identity)
						+ " heartbeat queuing PAD to "
						+ new String(key), Logging.DEBUG);
				sp.to = key;
				/*
				 * try encoding the packet and catch exceptions
				 * if buffer is full
				 */
				try {
				    server.queuePacket(sp,
						       TwoPrioritiesQueue.HIGH);
				} catch (ArrayIndexOutOfBoundsException outta) {
				    // outta.printStackTrace(Logging.err);
				    // Logging
				    // .prlnerr(
				    // new String(
				    // server.my_identity)
				    // + " could not queue PAD packet: "
				    // + outta
				    // .getMessage(),
				    // Logging.ERROR);
				    // split PAD in packet into smaller
				    // chunks
				    // and
				    // send each separately
				    // fix seqNo (splitPAD takes care of adding
				    // 1)
				    --filterSeqNo;
				    splitPAD(sp);
				}

			    }
			}

		    }
		    // System.err.println(new String(server.my_identity) +
		    // " end");
		    // System.err.println("pred: " + server.predicates);
		    // System.err.println("predFwd: " + server.predFwTable);
		    // System.err.println("update: " +
		    // server.updatedPredicates);

		    break;
		case (DVHeartbeat.CLEANUP):
		    synchronized (server.neighbors) {
			Iterator<byte[]> i = server.neighbors.keySet()
			    .iterator();
			while (i.hasNext() && keepRunning) {
			    byte[] key = i.next();
			    server.cleanUpDVEntry(key);
			}
		    }
		    // queue next clean up task
		    queue
			.put(
			     Long
			     .valueOf(System.currentTimeMillis()
				      + (DVHeartbeat.DEFAULT_NUM_CYCLES * SFFHeartbeat
					 .randPoissonDelta(server.dvDispatchPeriod))),
			     Integer.valueOf(DVHeartbeat.CLEANUP));
		    break;
		case (DVHeartbeat.MULTICAST):
		    // we use the built-in multicast to send out distance
		    // vectors and implement discovery

		    // TODO: set right cost
		    // sp.cost = server.ifmanager. interf.getCost();
		    sp.method = SENP.CNF;
		    sp.handler = server.listener.address();
		    sp.id = server.my_identity;
		    sp.ttl = (byte) 2;
		    int length = sp.encode();

		    System.out.println(new String(server.my_identity)
				       + " sending multicast message: " + sp);
		    DatagramPacket packet;
		    try {
			packet = new DatagramPacket(
						    sp.buf,
						    length,
						    InetAddress
						    .getByName(DiscoveryManager.DEFAULT_DISCOVERY_MULTICAST_ADDRESS),
						    DiscoveryManager.DEFAULT_DISCOVERY_MULTICAST_PORT);
			server.discoveryManager.ms.send(packet);
		    } catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }

		    System.out.println(new String(server.my_identity)
				       + " message sent.");

		    // set multicast to be performed next time
		    // TODO: maybe we should use a different period than DV
		    // / 6
		    queue.put(Long.valueOf(System.currentTimeMillis()
					   + (SFFHeartbeat.randPoissonDelta(5000))), Integer
			      .valueOf(DVHeartbeat.MULTICAST));
		    break;
		case (SFFHeartbeat.UpdateFWTable):
		    // switch DistanceVector with ForwardingTable
		    synchronized (server) {
			server.updateFWTable();
		    }

		    break;
		}
		// remove task from queue
		queue.remove(Long.valueOf(taskTime));
		// Logging.prlnlog(new String(server.my_identity)
		// + " heartbeat done completing task " + task,
		// Logging.DEBUG);
	    } else {
		try {
		    synchronized (queue) {
			queue.wait();
		    }
		} catch (InterruptedException e) {
		    // a UDV was added to the queue
		    // restart the cycle
		    // Logging
		    // .prlnlog(
		    // new String(server.my_identity)
		    // + " heartbeat interrupted while waiting on empty queue",
		    // Logging.DEBUG);
		    continue;
		}
	    }
	}
	SENPPacket.recycle(sp);
    }

    private void splitPAD(SENPPacket sp) {
	PredicatesTableEntry whole = sp.predicate;
	PredicatesTableEntry temp = new PredicatesTableEntry();
	temp.setDest(whole.getDest());
	while (!whole.filters.isEmpty()) {
	    temp.filters.clear();
	    if (whole.filters.size() > MAX_PREDICATES_PER_PAD) {
		temp.filters.addAll(whole.filters.subList(0,
							  MAX_PREDICATES_PER_PAD));
	    } else {
		temp.filters.addAll(whole.filters);
	    }
	    // remove sent filters
	    whole.filters.removeAll(temp.filters);
	    // remember the sequenceNo
	    temp.setFiltersSeqNo(++filterSeqNo);
	    synchronized (server.neighbors) {
		Iterator<byte[]> i = server.neighbors.keySet().iterator();
		while (i.hasNext() && keepRunning) {
		    byte[] key = i.next();

		    Logging.prlnlog(new String(server.my_identity)
				    + " heartbeat queuing PAD to " + new String(key),
				    Logging.DEBUG);
		    sp.to = key;
		    sp.predicate = temp;

		    try {
			server.queuePacket(sp, TwoPrioritiesQueue.HIGH);
		    } catch (Exception e) {
			e.printStackTrace(Logging.err);
			Logging.prlnerr(new String(server.my_identity)
					+ " could not queue split PAD packet: "
					+ e.getMessage(), Logging.ERROR);
		    }
		}
	    }
	}
    }

    // @override
    public void queueUpdate(byte method) {
	// first we check if another UDV was scheduled to happen within this
	// choke period
	synchronized (queue) {
	    if (queue.containsValue(Integer.valueOf(method))) {
		Logging.prlnlog(new String(server.my_identity)
				+ " heartbeat not queueing " + method
				+ "  because already queued", Logging.DEBUG);
		return;
	    } else {
		Logging.prlnlog(new String(server.my_identity)
				+ " heartbeat queueing " + method + " in "
				+ chokePeriod + " millis", Logging.DEBUG);
		// otherwise we queue it
		queue.put(Long
			  .valueOf(System.currentTimeMillis() + chokePeriod),
			  Integer.valueOf(method));
		Logging.prlnlog(new String(server.my_identity)
				+ " heartbeat queue: " + queue, Logging.DEBUG);
		// then we wake up any waiting thread
		queue.notifyAll();
		localThread.interrupt();
	    }
	}
    }

    // public void old_run() {
    // boolean doDiscovery = false;
    // Method multicast = null;
    // while (server.listener == null) {
    // // wait for a listener to be set
    // try {
    // Thread.sleep(1000);
    // } catch (InterruptedException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // }
    //
    // try {
    // if (Class.forName("siena.comm.MultiRadioPacketReceiver")
    // .isInstance(server.listener)) {
    // doDiscovery = true;
    // Class[] argTypes = new Class[1];
    // argTypes[0] = Object.class;
    // try {
    // multicast = Class.forName(
    // "siena.comm.MultiRadioPacketReceiver")
    // .getDeclaredMethod("multicast", argTypes);
    // } catch (SecurityException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // } catch (NoSuchMethodException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // } catch (ClassNotFoundException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // System.out.println(new String(server.my_identity)
    // + " found MULTIRADIO listener");
    // }
    // } catch (ClassNotFoundException e1) {
    // // do nothing serious
    // // we want Siena to work even without the MultiRadio dependencies
    // Logging
    // .prlnerr(new String(server.my_identity)
    // + " started without including the PLASTIC MultiRadio package");
    // } catch (NoClassDefFoundError ndfe) {
    // // do nothing serious
    // // we want Siena to work even without the MultiRadio dependencies
    // Logging
    // .prlnerr(new String(server.my_identity)
    // + " started without including the PLASTIC MultiRadio package");
    // }
    //
    // while (keepRunning) {
    // // Logging.prlnlog(new String(server.my_identity) + "
    // // DVHeartbeatThread cycle");
    // if (doDiscovery) {
    // // we use the built-in multicast to send out distance
    // // vectors and implement discovery
    // SENPPacket sp = SENPPacket.allocate();
    // try {
    // sp.id = server.my_identity;
    // // sp.to = destination;
    // sp.handler = server.listener.address();
    // // TODO: set right cost
    // // sp.cost = server.ifmanager. interf.getCost();
    // sp.method = SENP.DV;
    // sp.distanceVector = server.distanceVector;
    // sp.ttl = (byte) DVDRPDispatcher.DEFAULT_TTL_FOR_UDV;
    // int length = sp.encode();
    // byte[] packet = new byte[length];
    // for (int i = 0; i < length; i++) {
    // packet[i] = sp.buf[i];
    // }
    // System.out.println(new String(server.my_identity)
    // + " sending multicast message: " + sp);
    // // mrpr.multicast(packet);
    // Object[] args = new Object[1];
    // args[0] = packet;
    // multicast.invoke(server.listener, args);
    // System.out.println(new String(server.my_identity)
    // + " message sent.");
    // } catch (IllegalArgumentException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // } catch (IllegalAccessException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // } catch (InvocationTargetException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // } finally {
    // SENPPacket.recycle(sp);
    // }
    // if (keepRunning) {
    // try {
    // Thread.sleep(DVHeartbeatThread.randPoissonDelta(5000));
    // } catch (InterruptedException ie) {
    // // do nothing, we exit the cycle
    // }
    // }
    // } else {
    // try {
    // Iterator i = server.neighbors.keySet().iterator();
    // if (i.hasNext()) {
    // // Logging.prlnlog(new String(server.my_identity) + "
    // // DVHeartbeatThread hasNext");
    // // TODO: which policy should we adopt w.r.t.
    // // ConcurrentModificationException?
    // while (i.hasNext() && keepRunning) {
    // byte[] key = (byte[]) i.next();
    // server.queueDistanceVector(key, 2);
    // /*
    // * once every NumCycles times we clean up the
    // * distance vector to get rid of old entries
    // */
    // if (numCycles == 0) {
    // server.cleanUpDVEntry(key);
    // }
    //
    // // sleep
    // try {
    // Thread
    // .sleep(DVHeartbeatThread
    // .randPoissonDelta(server.dvDispatchPeriod));
    // } catch (InterruptedException ie) {
    // // do nothing
    // }
    // }
    // } else {
    // try {
    // Thread.sleep(DVHeartbeatThread
    // .randPoissonDelta(server.dvDispatchPeriod));
    // } catch (InterruptedException ie) {
    // // do nothing
    // }
    // }
    // } catch (ConcurrentModificationException cme) {
    // // Thrown if the neighbors table has been updated while we
    // // iterate
    // // do nothing, simply restart the iteration
    // }
    // }
    // if (numCycles == 0) {
    // numCycles = DEFAULT_NUM_CYCLES;
    // } else {
    // numCycles--;
    // }
    // }
    // Logging.prlnlog(new String(server.my_identity)
    // + " DVHeartbeatThread cycle terminated");
    // }

    // public DVDRPDispatcher getServer() {
    // return server;
    // }
    //
    // public void setServer(DVDRPDispatcher server) {
    // this.server = server;
    // }
}
