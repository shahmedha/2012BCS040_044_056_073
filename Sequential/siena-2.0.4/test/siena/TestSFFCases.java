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

import java.io.BufferedReader;
import java.io.FileReader;

import junit.framework.TestCase;
import siena.comm.GenericSenderFactory;
import siena.comm.MultiPacketReceiver;
import siena.comm.PacketSender;
import siena.comm.PacketSenderFactory;
import siena.comm.TCPPacketReceiver;

public class TestSFFCases extends TestCase {

	PacketSenderFactory psf = new GenericSenderFactory();

	int serverNo = 2;

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
			// ips[i] = new InterestedParty("Client" + i);
		}
		System.out.println("End Setup");
	}

	public void test() {
		System.out.println("Up and running");

		try {
			link(dispatchers[0], dispatchers[1]);
			FileReader fstream = new FileReader(
					"/tmp/S3_SFF.log");
			BufferedReader in = new BufferedReader(fstream);
			String line;
			while ((line = in.readLine()) != null) {
				System.out.println(line);
				if (line.indexOf("{") != -1) {
					String name = line.substring(0, line.indexOf("{") - 1);
					InterestedParty ip = new InterestedParty(name);
					String filterString = line.substring(line.indexOf("{"),
							line.length());
					String[] filters = filterString.split("\\} \\{");
					for (int i = 0; i < filters.length; i++) {
						SENPBuffer pckt = new SENPBuffer(("{ "
								+ filters[i].replaceAll("[\\{\\}]", "") + "}")
								.getBytes());
						f = pckt.decodeFilter();
						System.out.println(name + " subscribing for: " + f);
						dispatchers[0].subscribe(f, ip);
					}
				}
			}
			in.close();

			// We give time to subscriptions to go around
			Thread.sleep(10000);
			SENPBuffer pckt2 = new SENPBuffer(
					"{ background=true comput=true desktop=true florianfreundt=true msgId=\"S6C32m1\"}"
							.getBytes());
			Notification n = pckt2.decodeNotification();
			dispatchers[1].publish(n);
			Thread.sleep(1000);
			//assertEquals(1, ips[0].nCount);

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
