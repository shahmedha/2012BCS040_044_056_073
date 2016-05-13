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

/**
 * a simple implementation of a priority queue with only two priorities.
 * 
 * The queue is used to hold DV and UDV messages to be sent by
 * <code>siena.DispatcherThread</code>
 * 
 */
public class TwoPrioritiesArrayQueue implements TwoPrioritiesQueue {
	public static int DEFAULT_SIZE_HIGH = 100;

	public static int DEFAULT_SIZE_LOW = 15;

	//We store a byte array and the length to be sent
	private Object[][] high_packets;

	private Object[][] low_packets;

	private int high_head, high_tail, high_fill, high_size;

	private int low_head, low_tail, low_fill, low_size;

	private volatile boolean release = false;

	public static int HIGH = 1;

	public static int LOW = 2;

	public TwoPrioritiesArrayQueue() {
		this.high_size = DEFAULT_SIZE_HIGH;
		this.low_size = DEFAULT_SIZE_LOW;
		high_packets = new Object[high_size][2];
		low_packets = new Object[low_size][2];
		for (int i = 0; i < high_size; i++) {
			high_packets[i][0] = new byte[SENP.MaxPacketLen];			
		}
		for (int i = 0; i < low_size; i++) {
			low_packets[i][0] = new byte[SENP.MaxPacketLen];
		}
		high_head = high_tail = high_fill = 0;
		low_head = low_tail = low_fill = 0;
	}
	
	public TwoPrioritiesArrayQueue(int high_size, int low_size) {
		this.high_size = high_size;
		this.low_size = low_size;
		high_packets = new Object[high_size][2];
		low_packets = new Object[low_size][2];
		for (int i = 0; i < high_size; i++) {
			high_packets[i][0] = new byte[SENP.MaxPacketLen];			
		}
		for (int i = 0; i < low_size; i++) {
			low_packets[i][0] = new byte[SENP.MaxPacketLen];
		}
		high_head = high_tail = high_fill = 0;
		low_head = low_tail = low_fill = 0;
	}

//	synchronized public void add(SENPPacket pck, int priority) throws Exception {
//		int length = pck.encode();
//		this.add(pck.buf, length, priority);
//	}
	
	
	/* (non-Javadoc)
	 * @see siena.dvdrp.TwoPrioritiesQueue#add(byte[], int)
	 */
	synchronized public void add(byte[] obj, int priority) throws Exception {
		this.add(obj, obj.length, priority);
	}
	
	/* (non-Javadoc)
	 * @see siena.dvdrp.TwoPrioritiesQueue#add(byte[], int, int)
	 */
	synchronized public void add(byte[] obj, int length, int priority) throws Exception {
		if (obj != null) {
//			Logging.prlnlog("Queue size: " + high_fill + " , " + high_head
//					+ " , " + high_tail, Logging.DEBUG);
			if (priority == HIGH) {
				if (high_tail != high_head || high_fill == 0) {
					// copy byte per byte
					System.arraycopy(obj, 0, high_packets[high_tail][0], 0, length);
					high_packets[high_tail][1] = Integer.valueOf(length);
					high_tail = (high_tail == high_size -1) ? 0
							: high_tail+1;
					high_fill++;
				} else {
					throw new Exception("High priority queue is full: "
							+ "Queue size: " + high_fill + " , " + high_head
							+ " , " + high_tail);
				}
			} else if (priority == LOW) {
				if (low_tail != low_head || low_fill == 0) {
//					 copy byte per byte
					System.arraycopy(obj, 0, low_packets[low_tail][0], 0, length);		
					low_packets[low_tail][1] = Integer.valueOf(length);
					low_tail = (low_tail == low_size -1) ? 0 : low_tail+1;					
					low_fill++;
				} else {
					throw new Exception("Low priority queue is full");
				}
			}
//			System.out.println("Queue size end: " + high_fill + " , " + high_head
//					+ " , " + high_tail);
			notifyAll();
		} else {
			throw new Exception("Trying to queue null packet");
		}

	}

	/* (non-Javadoc)
	 * @see siena.dvdrp.TwoPrioritiesQueue#next()
	 */
	synchronized public Object next() {
		while ((high_fill == 0 && low_fill == 0) && !release) {
			try {
				wait();
			} catch (InterruptedException ie) {
				// we continue but we'll eventually get out, we're probably
				// stopping the server
			}
		}
		if (high_fill != 0) {
			int current = high_head;
			// move to next
			high_head = (high_head == high_size-1) ? 0 : high_head+1;
			high_fill--;
			notifyAll();
			return high_packets[current];
		} else if (low_fill != 0) {
			int current = low_head;
			// move to next
			low_head = (low_head == low_size-1) ? 0 : low_head+1;
			low_fill--;
			notifyAll();
			return low_packets[current];
		}
		notifyAll();
		return null;
	}

	/* (non-Javadoc)
	 * @see siena.dvdrp.TwoPrioritiesQueue#clear()
	 */
	synchronized public void clear() {
		high_head = high_tail = high_fill = 0;
		low_head = low_tail = low_fill = 0;
		release = true;
		notifyAll();
	}

	public int next(byte[] buf) {
		// TODO Auto-generated method stub
		return 0;
	}

}