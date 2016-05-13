//
//  This file is part of Siena, a wide-area event notification system.
//  See http://www.inf.usi.ch/carzaniga/siena/
//
//  Author: Giovanni Toffetti Carughi and Antonio Carzaniga
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
package siena.comm;

import java.util.ArrayList;
import java.util.Iterator;

import siena.Logging;
import siena.dvdrp.PacketQueue;
import siena.dvdrp.ReceiverThread;
import siena.dvdrp.StaticBufferQueue;

public class MultiPacketReceiver implements PacketReceiver {

	PacketReceiver defaultReceiver;
	int queuesize = 4000;

	ArrayList<PacketReceiver> receivers = new ArrayList<PacketReceiver>();

	public PacketQueue inputQueue;
	
	public MultiPacketReceiver() {
		super();
		inputQueue = new StaticBufferQueue(queuesize);
	}

	public MultiPacketReceiver(int queuesize) {
		super();		
		inputQueue = new StaticBufferQueue(queuesize);
	}

	public byte[] address() {
		return defaultReceiver.address();
	}

	public int receive(byte[] packet) throws PacketReceiverException {
		return inputQueue.next(packet);
	}

	public int receive(byte[] packet, long timeout)
			throws PacketReceiverException, TimeoutExpired {
		// TODO Auto-generated method stub
		return 0;
	}

	public void shutdown() throws PacketReceiverException {
		if (!receivers.isEmpty()) {
			Iterator<PacketReceiver> i = receivers.iterator();
			while (i.hasNext()) {
				PacketReceiver rec = i.next();
				rec.shutdown();
			}
		}
	}

	public void addReceiver(PacketReceiver rec, int threads) {		
		Logging.prlnlog("Adding receiver: " + rec, Logging.DEBUG);
		receivers.add(rec);
		for (int i = 0; i < threads; i++) {
			ReceiverThread recThread = new ReceiverThread(rec, inputQueue);
			recThread.start();
			Logging.prlnlog("Started receiver thread for receiver: " + rec, Logging.DEBUG);
		}		
		Logging.prlnlog("Receivers: " + receivers, Logging.DEBUG);
	}
	
	public void addDefaultReceiver(PacketReceiver rec, int threads) {
		this.defaultReceiver = rec;
		this.addReceiver(rec, threads);		
	}
	
	public boolean hasDefaultReceiver(){
		if (defaultReceiver == null){
			return false;
		}
		return true;
	}

	public PacketReceiver getDefaultReceiver() {		
		return defaultReceiver;
	}

}
