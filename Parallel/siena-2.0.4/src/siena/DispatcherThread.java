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

import siena.dvdrp.Buffer;

import siena.comm.PacketSenderException;

/**
 * implementation of a dispatcher thread to handle packet queue to neighbor
 * nodes.
 * 
 */
public class DispatcherThread extends Thread {

	private volatile boolean keepRunning;

	private volatile HierarchicalDispatcher server;

	private volatile NeighborNode node;

	public void startDispatcher() {
		keepRunning = true;
	}

	public void stopDispatcher() {
		keepRunning = false;
		this.interrupt();
	}

	public void run() {
		SENPPacket pck = SENPPacket.allocate();
//		int length = 0;
//		int prority = 0;
		Buffer buf = null;
		while (keepRunning) {
			// SENPPacket pckt = (SENPPacket) node.queue.next();
			// we get a null packet only at shutdown time
			// if (pckt != null){
			// server.sendPacket(pckt);
			// }
			try {
				buf = node.queue.next();
				node.send(buf.bytes, buf.getSize());
				Logging.prlnlog(new String(server.my_identity)
				+ " sent packet " + new String(buf.bytes,0,buf.getSize()), Logging.INFO);
			} catch (PacketSenderException e) {
				//e.printStackTrace();
				Logging.prlnerr(new String(server.my_identity)
				+ " could not send packet to " + new String(node.identity) + " reason: " + e.getMessage(), Logging.WARN);
				// if packet could not be sent then see whether we
				// can reroute
				if (buf != null) {
					pck.buf = buf.bytes;
					pck.init(buf.getSize());
					try {
						pck.decode();
					} catch (SENPInvalidFormat e1) {
						// this should not happen
						e1.printStackTrace();
					}

					/* uncomment not to drop packets.
					 * Amir found out we lose much less packets by not using this
					 * 
					 */
					//server.rerouteDRPPacket(pck);
					
					
					//SENPPacket.recycle(pck);
				}
			}
			finally {
				if (buf != null) {
					node.queue.recycle(buf);
				}
			}
		}
		SENPPacket.recycle(pck);
	}

	public DispatcherThread(HierarchicalDispatcher server, NeighborNode node) {
		super();
		this.server = server;
		this.node = node;
	}

}
