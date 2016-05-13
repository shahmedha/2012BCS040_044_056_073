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
import siena.comm.MultiPacketReceiver;
import siena.comm.PacketSender;
import siena.comm.PacketSenderFactory;
import siena.comm.TCPPacketReceiver;

/**
 * 
 * @author tof
 * 
 * Network topology:
 * 
 * _____L4___siena3____L5___ / \ dest---L1---siena1---L2----siena2
 * \__________L3__________/
 * 
 * 
 * Steps: - set filters on destination - config destination to know siena1
 * (enable L1) - config siena1 to know siena2 (enable L2) - config destination
 * to know siena2 (enable L3) - config siena3 to know siena and siena2 (enable
 * L4 and L5)
 * 
 * In the end: - siena2 should pick L3 to send notifications to destination -
 * siena3 can pick either siena1 or siena2 but it should do message partitioning
 */

public class TestDVDRPDispatcher extends TestCase {

	PacketSenderFactory psf = new GenericSenderFactory();

	int serverNo = 4;

	DVDRPDispatcher[] dispatchers = new DVDRPDispatcher[serverNo];

	InterestedParty[] ips = new InterestedParty[serverNo];

	SENPPacket pack;

	Filter f, g;

	protected void setUp() throws Exception {
		Logging.setLogStream(System.out);
		Logging.setErrorStream(System.out);
		Logging.setSeverity(Logging.DEBUG);
		System.out.println("Setup");
		for (int i = 0; i < serverNo; i++) {
			dispatchers[i] = new DVDRPDispatcher("Server" + i);
			dispatchers[i].sff = true;
			MultiPacketReceiver multiReceiver = new MultiPacketReceiver();
			TCPPacketReceiver rec = new TCPPacketReceiver(SENP.DEFAULT_PORT + i);
			multiReceiver.addDefaultReceiver(rec, 2);
			dispatchers[i].setReceiver(multiReceiver);
			dispatchers[i].startHeartbeat();
			ips[i] = new InterestedParty("Client" + i);
		}
		System.out.println("End Setup");
	}

	public void testUpdateDistanceVector() {
		System.out.println("Up and running");

		try {

			link(dispatchers[0], dispatchers[1]);
			link(dispatchers[1], dispatchers[2]);
			link(dispatchers[0], dispatchers[2]);
			link(dispatchers[3], dispatchers[2]);
			link(dispatchers[3], dispatchers[1]);
			Thread.sleep(1000); // main sleeps 2 secs
			// we create a filter
			f = new Filter();
			f.addConstraint("Alert", new AttributeConstraint(Op.EQ, "red"));
			f.addConstraint("TextMessage", new AttributeConstraint(Op.SS,
					"base"));
			g = new Filter(f);
			f.addConstraint("Severity", new AttributeConstraint(Op.GT, 5));
			dispatchers[0].subscribe(f, ips[0]);
			dispatchers[0].subscribe(g, ips[0]);
			System.out.println("Server0 DV: " + dispatchers[0].distanceVector);
			dispatchers[1].subscribe(f, ips[1]);
			dispatchers[2].subscribe(f, ips[2]);
			dispatchers[3].subscribe(g, ips[3]);
			System.out.println("S1 DV: " + dispatchers[1].distanceVector);
			System.out.println("S1 pred: " + dispatchers[1].predicates);
			// We give time to subscriptions to go around
			Thread.sleep(3000);
			System.err.println("S3 DV: " + dispatchers[3].distanceVector);
			System.err.println("S3 pred: " + dispatchers[3].predicates);
			// then we send a notification through siena3 and see where it goes
			Notification n = new Notification();
			n.putAttribute("Alert", "red");
			n.putAttribute("TextMessage", "All your bases are belong to us");
			n.putAttribute("Severity", 7);
			n.putAttribute("msgId", "first");
			Thread.sleep(2000); // main sleeps 500 millisecs			
			dispatchers[3].publish(n);
			Notification n2 = new Notification();
			n2.putAttribute("Alert", "red");
			n2.putAttribute("TextMessage", "base is ok");			
			n2.putAttribute("msgId", "second");
			dispatchers[2].publish(n2);
			Notification n3 = new Notification();
			n3.putAttribute("Alert", "red");
			n3.putAttribute("TextMessage", "base is ok");			
			n3.putAttribute("msgId", "third FP");			
			n3.putAttribute("TextMessage", "this is a false positive");
			dispatchers[2].publish(n3);
			Thread.sleep(2000); // main sleeps 2 secs
			Filter h = new Filter(g);
			h.addConstraint("Severity", new AttributeConstraint(Op.GT, 7));
			// Here we subscribe ip2 from siena1 with filter h that is already
			// covered by f
			dispatchers[1].subscribe(h, ips[2]);
			// Here we unsubscribe ip2 from siena1
			dispatchers[1].unsubscribe(f, ips[2]);
			Thread.sleep(5000); // main sleeps 2 secs
			System.out.println("S0 DV: " + dispatchers[0].distanceVector);
			System.out.println("S1 DV: " + dispatchers[1].distanceVector);
			System.out.println("S2 DV: " + dispatchers[2].distanceVector);
			System.out.println("S3 DV: " + dispatchers[3].distanceVector);
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

	public class InterestedParty implements Notifiable {

		public int nCount = 0;

		private String id;

		public InterestedParty(String id) {
			this.id = id;
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
