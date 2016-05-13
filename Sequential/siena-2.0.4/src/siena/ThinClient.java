//
//  This file is part of Siena, a wide-area event notification system.
//  See http://www.inf.usi.ch/carzaniga/siena/
//
//  Author: Antonio Carzaniga (firstname.lastname@usi.ch)
//  See the file AUTHORS for full details. 
//
//  Copyright (C) 1998-2002 University of Colorado
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

import siena.comm.*;

import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/** <em>thin</em> interface to the Siena event notification service.
 * 
 *  <code>ThinClient</code> does not provide an event notification
 *  service by itself, but rather functions as a connection to an
 *  external Siena server.  Therefore, a <code>ThinClient</code>
 *  object must be configured (constructed) with the handler of its
 *  external Siena server.  <p>
 *  
 *  In the simplest case, a <code>ThinClient</code> can be used only to
 *  publish notifications.  For example:
 *  
 *  <code><pre>
 *      ThinClient siena;
 *      siena = new ThinClient("ka:siena.dot.org:2345");
 *      Notification n = new Notification();
 *      n.putAttribute("name", "Antonio");
 *      siena.publish(n);
 *  </pre></code>
 *  
 *  <code>ThinClient</code> implements the {@link Siena} interface, so
 *  it can also be used to subscribe and unsubscribe.
 *  <code>ThinClient</code> uses a {@link PacketReceiver} to receive
 *  notifications from the server.
 *  
 *  @see HierarchicalDispatcher 
 *  @see Siena
 *  @see PacketReceiver
 **/
public class ThinClient implements Siena, Runnable {
    private byte[]		sndbuf		= new byte[SENP.MaxPacketLen];
    private byte[]		master_id	= null;
    private byte[]		master_handler	= null;
    private PacketSender	master		= null;
    private PacketReceiver	listener	= null;
    private String		my_id		= null;
    private Map<String,Notifiable> subscribers	= null;

    private SENPPacket		pkt = new SENPPacket();

    static PacketSenderFactory	default_sender_factory 
						= new GenericSenderFactory();

    /** default packet-sender factory for ThinClient interfaces
     *
     *  every new ThinClient uses this factory to create its
     *  connection to its master server
     **/
    static public void setDefaultPacketSenderFactory(PacketSenderFactory f) {
	default_sender_factory = f;
    }

    /** number of threads handling external packets.
     *
     *	The default value of <code>ReceiverThreads</code> is 4.  This
     *	value is used as the default number of receiver threads.
     *
     *	@see #setReceiver(PacketReceiver) 
     **/
    public  int			ReceiverThreads			= 4;

    /** creates a thin client connected to a Siena server.
     *
     *	@param server the uri of the server to connect to (e.g.,
     *		"ka:host.domain.net:7654")
     **/
    public ThinClient(String server) throws InvalidSenderException {
	this(server, SienaId.getId());
    }

    /** creates a thin client connected to a given Siena server, with
     *	the given Siena identity.
     *
     *  Notice that the string "\000" is a reserved identity, and
     *  therefore should never be used with this method.  See {@link
     *  HierarchicalDispatcher#HierarchicalDispatcher(String)} for
     *  more information
     *
     *	@param server the uri of the server to connect to (e.g.,
     *		"ka:host.domain.net:7654")
     *
     *  @param id the identity of this client.  Note that it is
     *		necessary that Siena identities be unique within a Siena
     *		network.  Here the client is responsible to make sure that the
     *		given identity does not conflicts with others.
     **/
    public ThinClient(String server, String id) throws InvalidSenderException {
	master = default_sender_factory.createPacketSender(server);
	my_id = id;
	subscribers = new HashMap<String,Notifiable>();
    }

    /** returns the identity of this client.
     *
     *  every object in a Siena network has a unique identifier.  This
     *  method returns the identifier of this client.  
     **/
    synchronized public String getIdentity() {
	return my_id;
    }

    private synchronized byte[] mapSubscriber(Notifiable n) {
	if (n == null) return null;
	String newid = my_id + Integer.toString(n.hashCode());
	if (subscribers.put(newid, n) == null) {
	    //
	    // first time we see this subscriber, so we notify the monitor
	    //
	    Monitor.add_node(newid.getBytes(), Monitor.ObjectNode);
	}
	return newid.getBytes();
    }

    private synchronized byte[] haveSubscriber(Notifiable n) {
	if (n == null) return null;
	String newid = my_id + Integer.toString(n.hashCode());
	if (subscribers.containsKey(newid)) {
	    return newid.getBytes();
	} else {
	    return null;
	}
    }

    private synchronized void removeSubscriber(Notifiable n) {
	if (n != null) {
	    String sid = my_id + Integer.toString(n.hashCode());
	    subscribers.remove(sid);
	    Monitor.remove_node(sid.getBytes());
	}
    }

    private synchronized Notifiable mapSubscriber(byte[] id) {
	if (id == null) return null;
	return subscribers.get(new String(id));
    }

    /** sets the <em>packet receiver</em> for this server.  
     *
     *	A <em>packet receiver</em> accepts notifications,
     *	subscriptions, and other requests on some communication
     *	channel.  <code>setReceiver</code> will shut down any
     *	previously activated receiver for this dispatcher.  This
     *	method does not guarantee a transactional switch to a new
     *	receiver.  This means that some requests might get lost while
     *	the server has closed the old port and before it reopens the
     *	new port.
     *
     *  <p>This method simply calls {@link #setReceiver(PacketReceiver,
     *  int)} using {@link #ReceiverThreads} as a default value.
     *
     *	@param r is the receiver 
     *      		   
     *  @see #shutdown()
     *  @see #setReceiver(PacketReceiver, int) 
     **/
    public void setReceiver(PacketReceiver r) {
	setReceiver(r, ReceiverThreads);
    }

    /** sets the <em>packet receiver</em> for this server.  
     *
     *	A <em>packet receiver</em> accepts notifications,
     *	subscriptions, and other requests on some communication
     *	channel.  <code>setReceiver</code> will shut down any
     *	previously activated receiver for this dispatcher.  This
     *	method does not guarantee a transactional switch to a new
     *	receiver.  This means that some requests might get lost while
     *	the server has closed the old port and before it reopens the
     *	new port.
     *       
     *	@param r is the receiver 
     *  @param threads is the number of threads associated with the
     *         receiver, and therefore to the whole server.
     *      		   
     *  @see #shutdown()
     **/
    synchronized public void setReceiver(PacketReceiver r, int threads) {
	if (listener != null) {
	    try {
		listener.shutdown();
	    } catch (PacketReceiverException ex) {
		Logging.exerr(ex);
	    }
	    //
	    // this should send a PacketReceiverClosed exception to
	    // every thread that is waiting for packets on the old
	    // listener, which will make them exit normally.  However,
	    // because of bugs in the JVM, or because of bad
	    // implementations of packetReceiver, this might not be
	    // true.  ...work in progress...
	    //
	}
	listener = r;

	if (master != null && !subscribers.isEmpty()) {
	    pkt.init();
	    pkt.method = SENP.MAP;
	    pkt.id = my_id.getBytes();
	    pkt.handler = listener.address();
	    try {
		master.send(pkt.buf, pkt.encode());
	    } catch (Exception ex) {
		Logging.exerr(ex);
		//
		// I should really do something here
		// ...work in progress...
		//
	    }
	}
	//
	// now fires off the threads that listen to this port
	//
	while (threads-- > 0) 
	    (new Thread(this)).start();
    }

    public void run() {
	SENPPacket rpkt = new SENPPacket();
	int res;
	while(true) {
	    try {
		PacketReceiver r = listener;
		if (r == null) return;
		res = r.receive(rpkt.buf);
		rpkt.init(res); 
		rpkt.decode();
		if (rpkt.ttl > 0) {
		    if (rpkt.method ==  SENP.PUB) {
			Notifiable n = mapSubscriber(rpkt.to);
			if (n != null) {
			    try {
				if (rpkt.event != null) {
				    n.notify(rpkt.event);
				} else if (rpkt.events != null) {
				    n.notify(rpkt.events);
				}
			    } catch (SienaException ex) {
				Logging.prlnlog("exception thrown by subscriber:" + ex.toString());
			    }
			} else {
			    Logging.prlnlog("ThinClient: warning: unknown id: " + new String(rpkt.id));
			}
		    } else {
			Logging.prlnerr("ThinClient: warning: unable to handle method: " + rpkt.method);
		    }
		}
	    } catch (PacketReceiverClosed ex) {
		if (ex.getIOException() != null) 
		    Logging.prlnerr("error in packet receiver: " 
				    + ex.toString());
		return;
	    } catch (PacketReceiverFatalError ex) {
		Logging.prlnerr("fatal error in packet receiver: " 
				+ ex.toString());
		return;
	    } catch (PacketReceiverException ex) {
		//
		// non fatal error: just log it and loop
		//
		Logging.prlnlog("non-fatal error in packet receiver: " 
				+ ex.toString());
	    } catch (SENPInvalidFormat ex) {
		Logging.prlnlog("invalid packet format: " + ex.toString());
	    }
	}
    }

    /** suspends the delivery of notifications for a subscriber.
     *
     *	This causes the Siena server to stop sending notification to
     *	the given subscriber.  The server correctly maintains all the
     *	existing subscriptions so that the flow of notification can be
     *	later resumed (with {@link #resume(Notifiable)}). 
     *
     *	@see #resume(Notifiable) 
     **/
    synchronized public void suspend(Notifiable n) throws SienaException {
	pkt.id = haveSubscriber(n);
	if (pkt.id == null) return;
	pkt.method = SENP.SUS;
	pkt.to = master_handler;
	pkt.id = mapSubscriber(n);
	pkt.handler = listener.address();
	master.send(pkt.buf, pkt.encode());
    }

    /** resumes the delivery of notifications for a subscriber.
     *
     *	This causes the Siena (master) server to resume sending
     *	notification to the given subscriber.
     *
     *	@see #suspend(Notifiable)
     **/
    synchronized public void resume(Notifiable n) throws SienaException {
	pkt.id = haveSubscriber(n);
	if (pkt.id == null) return;
	pkt.method = SENP.RES;
	pkt.to = master_handler;
	pkt.handler = listener.address();
	master.send(pkt.buf, pkt.encode());
    }

    /** returns the <em>handler</em> of the Siena server associated
     *	with this Siena interface.
     *
     *	@return URI of the master server.
     *
     *	@see #ThinClient(String)
     **/
    synchronized public String getServer() {
	if (master_handler == null) return null;
	return new String(master_handler);
    }

    synchronized private void unsubscribeAll() {
	if (master != null) {
	    pkt.init();
	    pkt.method = SENP.BYE;
	    pkt.to = master_handler;
	    for(String id : subscribers.keySet()) {
		try {
		    pkt.id = id.getBytes();
		    master.send(pkt.buf, pkt.encode());
		} catch (Exception ex) {
		    Logging.exerr(ex);
		    //
		    // what should I do here?
		    // ...work in progress...
		    //
		}
	    }
	    subscribers.clear();
	    master = null;
	    master_handler = null;
	}
    }

    synchronized public void publish(Notification n) throws SienaException {
	if (n == null) return;

	pkt.init();
	pkt.event = n;
	pkt.method = SENP.PUB;
	pkt.id = my_id.getBytes();
	pkt.to = master_handler;
	master.send(pkt.buf, pkt.encode());
    }

    public void subscribe(Filter f, Notifiable n) throws SienaException {
	if (n == null) return;
	if (f == null) {
	    //
	    // null filters are not allowed in subscriptions this is a
	    // design choice, we could accept null filters with the
	    // semantics of the universal filter: one that matches
	    // every notification
	    //
	    throw (new SienaException("null filter"));
	}
	if (listener == null) {
	    try {
		setReceiver(new TCPPacketReceiver(0));
	    } catch (IOException ex) {
		throw (new SienaException(ex.toString()));
	    }
	}
	pkt.init();
	pkt.filter = f;
	pkt.method = SENP.SUB;
	pkt.id = mapSubscriber(n);
	pkt.handler = listener.address();
	pkt.to = master_handler;
	master.send(pkt.buf, pkt.encode());
    }

    public void subscribe(Pattern p, Notifiable n) throws SienaException {
	if (n == null) return;
	if (p == null) {
	    //
	    // null patterns are not allowed in subscriptions 
	    //
	    throw (new SienaException("null pattern"));
	}
	if (listener == null) {
	    try {
		setReceiver(new TCPPacketReceiver(0));
	    } catch (IOException ex) {
		throw (new SienaException(ex.toString()));
	    }
	}
	pkt.init();
	pkt.pattern = p;
	pkt.method = SENP.SUB;
	pkt.id = mapSubscriber(n);
	pkt.handler = listener.address();
	pkt.to = master_handler;
	master.send(pkt.buf, pkt.encode());
    }

    public void unsubscribe(Filter f, Notifiable n) throws SienaException {
	if (n == null || listener == null) return;

	pkt.init();
	pkt.id = haveSubscriber(n);
	if (pkt.id != null) {
	    pkt.handler = listener.address();
	    pkt.to = master_handler;
	    if (f == null) {
		removeSubscriber(n);
		pkt.method = SENP.BYE;
	    } else {
		pkt.method = SENP.UNS;
		pkt.filter = f;
	    }
	    master.send(pkt.buf, pkt.encode());
	}
    }

    public void unsubscribe(Pattern p, Notifiable n) throws SienaException {
	if (n == null || listener == null) return;

	pkt.init();
	pkt.id = haveSubscriber(n);
	if (pkt.id != null) {
	    pkt.handler = listener.address();
	    pkt.to = master_handler;
	    if (p == null) {
		removeSubscriber(n);
		pkt.method = SENP.BYE;
	    } else {
		pkt.method = SENP.UNS;
		pkt.pattern = p;
	    }
	    master.send(pkt.buf, pkt.encode());
	}
    }

    /** closes this dispatcher.
     *
     *  If this dispatcher has an active listener then closes the
     *  active listener.  It also unsubscribes everything with its
     *  master server.
     *      
     *  @see #setReceiver(PacketReceiver) 
     **/
    synchronized public void shutdown() {
	unsubscribeAll();
	if (listener != null) 
	    try {
		listener.shutdown();
	    } catch (PacketReceiverException ex) {
		Logging.exerr(ex);
	    }
	listener = null;
    }

    synchronized public void advertise(Filter f, String id) 
	throws SienaException {
	if (id == null) return;
	//
	// I haven't thought about what to do here.
	//
	if (f == null) {
	    //
	    // I haven't thought about what to do here.
	    //
	    throw (new SienaException("null filter"));
	}
	pkt.init();
	pkt.filter = f;
	pkt.method = SENP.ADV;
	pkt.id = id.getBytes();
	pkt.handler = listener.address();
	pkt.to = master_handler;
	master.send(pkt.buf, pkt.encode());
    }

    synchronized public void unadvertise(Filter f, String id) 
	throws SienaException {
	if (id == null) return;
	//
	// I haven't thought about what to do here.
	//
	pkt.init();
	pkt.id = id.getBytes();
	pkt.handler = listener.address();
	pkt.to = master_handler;
	pkt.method = SENP.UNA;
	pkt.filter = f;
	// 
	// should I have a special method for UNA all (when f == null)?
	// ...work in progress...
	//
	master.send(pkt.buf, pkt.encode());
    }

    public void unadvertise(String id) throws SienaException {
	unadvertise(null, id);
    }

    public void unsubscribe(Notifiable n) throws SienaException {
	unsubscribe((Filter)null, n);
    }
}
