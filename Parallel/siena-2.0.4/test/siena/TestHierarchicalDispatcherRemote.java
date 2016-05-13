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

import junit.framework.TestCase;
import siena.comm.GenericSenderFactory;
import siena.comm.InvalidSenderException;
import siena.comm.MultiPacketReceiver;
import siena.comm.PacketSender;
import siena.comm.PacketSenderFactory;
import siena.comm.TCPPacketReceiver;

/**
 * 
 * @author tof
 * 
 * Network topology:
 *          S0
 *         |  |
 *     S1---   S2
 *     |
 *     S3
 * Steps: - set filters on destination - config destination to know siena1
 * (enable L1) - config siena1 to know siena2 (enable L2) - config destination
 * to know siena2 (enable L3) - config siena3 to know siena and siena2 (enable
 * L4 and L5)
 * 
 * In the end: - siena2 should pick L3 to send notifications to destination -
 * siena3 can pick either siena1 or siena2 but it should do message partitioning
 */

public class TestHierarchicalDispatcherRemote extends TestCase {

	PacketSenderFactory psf = new GenericSenderFactory();

	int serverNo = 4;

	HierarchicalDispatcher[] dispatchers = new HierarchicalDispatcher[serverNo];

	RemoteInterestedParty[] ips = new RemoteInterestedParty[serverNo];

	SENPPacket pack;

	Filter f, g;

	protected void setUp() throws Exception {
		Logging.setLogStream(System.out);
		Logging.setErrorStream(System.out);
		Logging.setSeverity(Logging.DEBUG);
		System.out.println("Setup");
		for (int i = 0; i < serverNo; i++) {
			dispatchers[i] = new HierarchicalDispatcher("Server" + i);
			dispatchers[i].sff = false;
			MultiPacketReceiver multiReceiver = new MultiPacketReceiver();
			TCPPacketReceiver rec = new TCPPacketReceiver(SENP.DEFAULT_PORT + i);
			multiReceiver.addDefaultReceiver(rec, 2);
//			dispatchers[i].setChoke(100);
			dispatchers[i].setReceiver(multiReceiver);
			dispatchers[i].startHeartbeat();			
			ips[i] = new RemoteInterestedParty("tcp:localhost:"+(SENP.DEFAULT_PORT + i),"Client" + i);
			// set master
			if(i == 1 || i == 2){
				dispatchers[i].setMaster("tcp:localhost:"+ SENP.DEFAULT_PORT);
			} else if(i == 3){
				dispatchers[i].setMaster("tcp:localhost:"+ (SENP.DEFAULT_PORT + 1));
			}
		}
		
		
		System.out.println("End Setup");
	}

	public void testUpdateDistanceVector() {
		System.out.println("Up and running");

		try {			
			Thread.sleep(1000); // main sleeps 2 secs
			// we create a filter
			f = new Filter();
			f.addConstraint("Alert", new AttributeConstraint(Op.EQ, "red"));
			f.addConstraint("TextMessage", new AttributeConstraint(Op.SS,
					"base"));
			g = new Filter(f);
			f.addConstraint("Severity", new AttributeConstraint(Op.GT, 5));
			ips[0].subscribe(f);			
			ips[0].subscribe(g);
//			Filter t = new Filter();
//			t.addConstraint("Sender",  new AttributeConstraint(Op.EQ, "tof"));
//			ips[0].subscribe(t);
//			System.out.println("Server0 DV: " + dispatchers[0].distanceVector);
			ips[1].subscribe(f);
			ips[2].subscribe(f);
			ips[3].subscribe(g);
//			System.out.println("S1 DV: " + dispatchers[1].distanceVector);
			// We give time to subscriptions to go around
			Thread.sleep(3000);
//			System.err.println("S2 DV: " + dispatchers[2].distanceVector);
			// then we send a notification through siena3 and see where it goes
			Notification n = new Notification();
			n.putAttribute("Alert", "red");
			n.putAttribute("TextMessage", "All your bases are belong to us");
			n.putAttribute("Severity", 7);
			n.putAttribute("msgId", "first");
			Thread.sleep(2000); // main sleeps 500 millisecs			
			ips[3].publish(n);
			Notification n2 = new Notification();
			n2.putAttribute("Alert", "red");
			n2.putAttribute("TextMessage", "base is ok");
			n2.putAttribute("msgId", "second");
			ips[2].publish(n2);
			Notification n3 = new Notification();
			n3.putAttribute("Sender", "tof");
			ips[1].publish(n3);
			Thread.sleep(2000); // main sleeps 2 secs
			Filter h = new Filter(g);
			h.addConstraint("Severity", new AttributeConstraint(Op.GT, 7));
			// Here we subscribe ip2 from siena1 with filter h that is already
			// covered by f
			ips[2].subscribe(h);			
			// Here we unsubscribe ip2 from siena1
			ips[0].unsubscribe(f);
			Thread.sleep(5000); // main sleeps 2 secs
//			System.out.println("S0 DV: " + dispatchers[0].distanceVector);
//			System.out.println("S1 DV: " + dispatchers[1].distanceVector);
//			System.out.println("S2 DV: " + dispatchers[2].distanceVector);
//			System.out.println("S3 DV: " + dispatchers[3].distanceVector);
			System.out.println("Shutdown");
			tearDown();
			assertEquals(2, ips[0].nCount);
			assertEquals(1, ips[1].nCount);
			assertEquals(1, ips[2].nCount);
			assertEquals(2, ips[3].nCount);
		} catch (InterruptedException ie) {
			// do nothing
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected void tearDown() throws Exception {
		// Logging.setLogStream(null);
		for (int i = 0; i < serverNo; i++) {
			dispatchers[i].shutdown();
		}
		super.tearDown();
	}

	public void link(HierarchicalDispatcher source, HierarchicalDispatcher dest) {
		pack = SENPPacket.allocate();
		pack.id = source.my_identity;
		pack.handler = source.listener.address();
		pack.method = SENP.CNF;
		pack.ttl = SENP.DefaultTtl;
		pack.cost = 1;
		try {
			PacketSender sd = psf.createPacketSender(new String(dest.listener
					.address()));
			sd.send(pack.buf, pack.encode());
		} catch (Exception e) {
			e.printStackTrace();
		}
		SENPPacket.recycle(pack);
	}

	public class RemoteInterestedParty extends ThinClient implements Notifiable {

		public int nCount = 0;

		private String id;

		public RemoteInterestedParty(String server, String id) throws InvalidSenderException {
			super(server,id);
			this.id = id;
		}

		public void unsubscribe(Filter f) throws SienaException {
			unsubscribe(f,this);			
		}

		public void subscribe(Filter f) throws SienaException {
			subscribe(f,this);			
		}

		public void notify(Notification n) throws SienaException {
			System.err.println(id + " got notification :" + n.toString());
			nCount++;
		}

		public void notify(Notification[] s) throws SienaException {
			// TODO Auto-generated method stub

		}

	}

}
