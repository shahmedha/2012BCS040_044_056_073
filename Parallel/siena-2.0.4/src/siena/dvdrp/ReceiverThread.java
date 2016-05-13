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
package siena.dvdrp;

import siena.Logging;
import siena.SENP;
import siena.comm.PacketReceiver;
import siena.comm.PacketReceiverClosed;
import siena.comm.PacketReceiverException;
import siena.comm.PacketReceiverFatalError;

public class ReceiverThread extends Thread {

	PacketReceiver receiver;
	PacketQueue inputQueue;
	boolean keepRunning = true;

	public ReceiverThread(PacketReceiver receiver, PacketQueue inputQueue) {
		super();
		this.receiver = receiver;
		this.inputQueue = inputQueue;
	}

	public void run() {
		byte[] packet = new byte[SENP.MaxPacketLen];
		while (keepRunning) {
			try {
				int length = receiver.receive(packet);
				if (Logging.severity == Logging.DEBUG) {
					Logging.prlnlog("Received packet" + length + " : "
							+ new String(packet, 0, length), Logging.DEBUG);
				}
				inputQueue.add(packet, length);
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
				Logging.prlnerr("non-fatal error in packet receiver: "
						+ ex.toString());
			} catch (QueueFullException ex) {
				Logging.prlnerr("Could not add packet to input queue: "
						+ ex.toString());
			} catch (Exception e) {
				Logging.prlnerr(e.getMessage());
			}
		}
	}

	public void shutdown() {
		keepRunning = false;
	}

}
