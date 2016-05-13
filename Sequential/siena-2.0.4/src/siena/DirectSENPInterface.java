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

/** low-level interface to the Siena event notification service.
 **/
public class DirectSENPInterface {

    private PacketSender	master;
    private byte[]		master_handler;

    PacketSenderFactory		sender_factory; 
    static PacketSenderFactory	default_sender_factory 
						= new GenericSenderFactory();

    /** sets the packet-sender factory associated with this
     *  SENP interface
     *
     *  the packet sender factory is used every time this interface is
     *  connected to a new master server through {@link
     *  #setServer(String)}
     *
     *  @see #setDefaultPacketSenderFactory(PacketSenderFactory)
     **/
    public void setPacketSenderFactory(PacketSenderFactory f) {
	sender_factory = f;
    }

    /** default packet-sender factory for DireactSENPInterface
     *  objects
     *
     *  every new object is assigned this factory
     *
     *  @see #setPacketSenderFactory(PacketSenderFactory)
     **/
    static public void setDefaultPacketSenderFactory(PacketSenderFactory f) {
	default_sender_factory = f;
    }

    /** creates and connects to a given Siena server.
     *
     *	@param server the uri of the server to connect to 
     *                (e.g., "ka:host.domain.net:7654")
     **/
    public DirectSENPInterface(String server) throws InvalidSenderException {
	sender_factory = default_sender_factory;
	master = sender_factory.createPacketSender(server);
	master_handler = server.getBytes();
    }

    /** connectes to a given Siena server.
     *
     *	@param server the uri of the server to connect to 
     *                (e.g., "ka:host.domain.net:7654")
     **/
    public void setServer(String server) throws InvalidSenderException {
	master = sender_factory.createPacketSender(server);
	master_handler = server.getBytes();
    }

    /** suspends the delivery of notifications for a given subscriber.
     *
     *	This causes the Siena server to stop sending notification to
     *	the given subscriber.  The server correctly maintains all the
     *	existing subscriptions so that the flow of notification can be
     *	later resumed (with {@link #resume(String)}). 
     *
     *	@param id identity of the subscriber
     *	@see #resume(String) 
     **/
    public void suspend(String id) throws SienaException {
	if (id == null) return;
	SENPPacket sus = new SENPPacket();
	sus.id = id.getBytes();
	sus.method = SENP.SUS;
	sus.to = master_handler;
	sus.encode();
	master.send(sus.buf, 0, sus.length());
    }

    /** resumes the delivery of notifications for a given subscriber
     *	
     *	This causes the Siena (master) server to resume sending
     *	notification to the given subscriber.
     *
     *	@see #suspend(String)
     **/
    public void resume(String id) throws SienaException {
	if (id == null) return;
	SENPPacket sus = new SENPPacket();
	sus.id = id.getBytes();
	sus.method = SENP.RES;
	sus.to = master_handler;
	sus.encode();
	master.send(sus.buf, 0, sus.length());
    }

    public void unsubscribeAll(String id) throws SienaException {
	if (id == null) return;
	SENPPacket pkt = new SENPPacket();
	pkt.id = id.getBytes();
	pkt.method = SENP.BYE;
	pkt.to = master_handler;
	pkt.encode();
	master.send(pkt.buf, 0, pkt.length());
    }

    public void publish(Notification n) throws SienaException {
	if (n == null) return;
	SENPPacket req = new SENPPacket();
	req.event = n;
	req.method = SENP.PUB;
	req.to = master_handler;
	req.encode();
	master.send(req.buf, 0, req.length());
    }

    public void subscribe(Filter f, String id, String handler)
	throws SienaException {
	if (handler == null || f == null || id == null) return;
	SENPPacket req = new SENPPacket();
	req.filter = f;
	req.method = SENP.SUB;
	req.id = id.getBytes();
	req.handler = handler.getBytes();
	req.to = master_handler;
	req.encode();
	master.send(req.buf, 0, req.length());
    }

    public void subscribe(Pattern p, String id, String handler)
	throws SienaException {
	if (handler == null || p == null || id == null) return;
	SENPPacket req = new SENPPacket();
	req.pattern = p;
	req.method = SENP.SUB;
	req.id = id.getBytes();
	req.handler = handler.getBytes();
	req.to = master_handler;
	req.encode();
	master.send(req.buf, 0, req.length());
    }

    public void unsubscribe(Filter f, String id, String handler)
	throws SienaException {
	if (handler == null || f == null || id == null) return;
	SENPPacket req = new SENPPacket();
	req.filter = f;
	req.method = SENP.UNS;
	req.id = id.getBytes();
	req.handler = handler.getBytes();
	req.to = master_handler;
	req.encode();
	master.send(req.buf, 0, req.length());
    }

    public void unsubscribe(Pattern p, String id, String handler)
	throws SienaException {
	if (handler == null || p == null || id == null) return;
	SENPPacket req = new SENPPacket();
	req.pattern = p;
	req.method = SENP.UNS;
	req.id = id.getBytes();
	req.handler = handler.getBytes();
	req.to = master_handler;
	req.encode();
	master.send(req.buf, 0, req.length());
    }

    public void mapHandler(String id, String handler)
	throws SienaException {
	if (handler == null || id == null) return;
	SENPPacket req = new SENPPacket();
	req.method = SENP.MAP;
	req.id = id.getBytes();
	req.handler = handler.getBytes();
	req.to = master_handler;
	req.encode();
	master.send(req.buf, 0, req.length());
    }

    /** switch to a different master server (possibly null)
     **/
    public void configure(String handler)
	throws SienaException {
	SENPPacket req = new SENPPacket();
	req.method = SENP.CNF;
	req.id = null;
	if (handler != null) 
	    req.handler = handler.getBytes();
	req.to = master_handler;
	req.encode();
	master.send(req.buf, 0, req.length());
    }

    public void shutdown()
	throws SienaException {
	SENPPacket req = new SENPPacket();
	req.method = SENP.OFF;
	req.id = null;
	req.handler = null;
	req.to = master_handler;
	req.encode();
	master.send(req.buf, 0, req.length());
    }

    public void advertise(Filter f, String id) throws SienaException {
	if (id == null || f == null) return;

	SENPPacket req = new SENPPacket();
	req.filter = f;
	req.method = SENP.ADV;
	req.id = id.getBytes();
	req.to = master_handler;
	req.encode();
	master.send(req.buf, 0, req.length());
    }

    public void unadvertise(Filter f, String id) throws SienaException {
	if (id == null) return;
	SENPPacket req = new SENPPacket();
	req.id = id.getBytes();
	req.to = master_handler;
	req.method = SENP.UNA;
	req.filter = f;
	// 
	// should I have a special method for UNA all (when f == null)?
	// ...work in progress...
	//
	req.encode();
	master.send(req.buf, 0, req.length());
    }

    public void unadvertiseAll(String id) throws SienaException {
	unadvertise(null, id);
    }

    static public void processNotification(Notifiable n,
					   PacketReceiver r) 
	throws SienaException {
	SENPPacket req = new SENPPacket();
	int res;
	res = r.receive(req.buf);
	if (res > 0) {
	    req.init(res);
	    req.decode();
	    if (req.ttl > 0 && req.method ==  SENP.PUB) {
		if (req.event != null) {
		    n.notify(req.event);
		} else if (req.events != null) {
		    n.notify(req.events);
		}
	    }
	}
    }
}
