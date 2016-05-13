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
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Iterator;

import siena.comm.InvalidSenderException;
import siena.dvdrp.OrderedByteArrayMap;

public class DiscoveryManager extends Thread {

    //	OrderedByteArrayMap<> remoteNodes = new OrderedByteArrayMap();
    //	OrderedByteArrayMap localNodes = new OrderedByteArrayMap();

	IFManager ifmanager = new IFManager();

	public static String DEFAULT_DISCOVERY_MULTICAST_ADDRESS = "224.7.7.7";
	public static int DEFAULT_DISCOVERY_MULTICAST_PORT = 8077;

	DVDRPDispatcher server;
	boolean keepRunning = true;
	InetSocketAddress multicastGroup
	    = new InetSocketAddress(DEFAULT_DISCOVERY_MULTICAST_ADDRESS,
				    DEFAULT_DISCOVERY_MULTICAST_PORT);

	// Receiver multicast socket
	public MulticastSocket ms;

	/*
	 * All we do is listen on a multicast socket for incoming connection
	 * messages NOTE: this only allows one instance of DVDRPServer per machine
	 * 
	 * @see java.lang.Thread#run()
	 */
	public DiscoveryManager(DVDRPDispatcher server) {
		super();
		this.server = server;
	}

	public void run() {

		try {
			ms = new MulticastSocket(DEFAULT_DISCOVERY_MULTICAST_PORT);
			ms.joinGroup(multicastGroup.getAddress());
			// avoid sending messages to itself
			ms.setLoopbackMode(false);
		} catch (SocketException se) {
			// some devices do not support this
			// do nothing
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SENPPacket req = SENPPacket.allocate();

		while (keepRunning) {

			DatagramPacket recv = new DatagramPacket(req.buf, req.buf.length);
			try {
				ms.receive(recv);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			req.init(recv.getData().length);
			req.buf = recv.getData();
			try {
				req.decode();
				// if it's a self message don't even consider it
				if (!Arrays.equals(req.handler, server.listener.address())) {
					Logging.prlnlog("Received multicast request: "
							+ new String(req.buf), Logging.DEBUG);
					server.processRequest(req);
				}
			} catch (SENPInvalidFormat e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		SENPPacket.recycle(req);
	}

	protected void finalize() throws Throwable {
		if (ms != null) {
			ms.leaveGroup(multicastGroup.getAddress());
			ms.close();
		}
		// if (ds != null) {
		// ds.close();
		// }
		super.finalize();
	}

}
