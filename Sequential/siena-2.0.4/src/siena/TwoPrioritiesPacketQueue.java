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

/**
 * a simple implementation of a priority queue with only two priorities.
 * 
 * The queue is used to hold DV and UDV messages to be sent by
 * <code>siena.DispatcherThread</code>
 * 
 */
public class TwoPrioritiesPacketQueue {
	public static int DEFAULT_SIZE_HIGH = 100;

	public static int DEFAULT_SIZE_LOW = 5;

	private SENPPacket[] high_packets = new SENPPacket[DEFAULT_SIZE_HIGH];

	private SENPPacket[] low_packets = new SENPPacket[DEFAULT_SIZE_LOW];

	private int high_head, high_tail, high_fill;

	private int low_head, low_tail, low_fill;

	private volatile boolean release = false;

	public static int HIGH = 1;

	public static int LOW = 2;

	public TwoPrioritiesPacketQueue() {
		for (int i = 0; i < DEFAULT_SIZE_HIGH; i++) {
			high_packets[i] = new SENPPacket();
		}
		for (int i = 0; i < DEFAULT_SIZE_LOW; i++) {
			low_packets[i] = new SENPPacket();
		}
		high_head = high_tail = high_fill = 0;
		low_head = low_tail = low_fill = 0;
	}

	synchronized public void add(SENPPacket obj, int priority) throws Exception {
		if (obj != null) {
//			System.out.println("Queue size: " + high_fill + " , " + high_head
//					+ " , " + high_tail);
			if (priority == HIGH) {
				if (high_tail != high_head || high_fill == 0) {
					high_packets[high_tail].copyOf(obj);
					high_tail = (high_tail == DEFAULT_SIZE_HIGH -1) ? 0
							: high_tail+1;
					high_fill++;
				} else {
					throw new Exception("High priority queue is full: "
							+ "Queue size: " + high_fill + " , " + high_head
							+ " , " + high_tail);
				}
			} else if (priority == LOW) {
				if (low_tail != low_head || low_fill == 0) {
					low_packets[low_tail].copyOf(obj);
					low_tail = (low_tail == DEFAULT_SIZE_LOW -1) ? 0 : low_tail+1;
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
			high_head = (high_head == DEFAULT_SIZE_HIGH-1) ? 0 : high_head+1;
			high_fill--;
			return high_packets[current];
		} else if (low_fill != 0) {
			int current = low_head;
			// move to next
			low_head = (low_head == DEFAULT_SIZE_LOW-1) ? 0 : low_head+1;
			low_fill--;
			return low_packets[current];
		}
		return null;
	}

	synchronized public void clear() {
		high_head = high_tail = high_fill = 0;
		low_head = low_tail = low_fill = 0;
		release = true;
		notifyAll();
	}

}