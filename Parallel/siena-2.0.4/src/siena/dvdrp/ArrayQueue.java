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
public class ArrayQueue implements PacketQueue {
	public static int DEFAULT_SIZE = 100;
	
	//We store a byte array and the length to be sent
	private Object[][] packets;

	private int head, tail, fill, size;

	private volatile boolean release = false;

	public ArrayQueue() {
		this.size = DEFAULT_SIZE;		
		packets = new Object[size][2];		
		for (int i = 0; i < size; i++) {
			packets[i][0] = new byte[SENP.MaxPacketLen];			
		}		
		head = tail = fill = 0;		
	}
	
	public ArrayQueue(int size) {
		this.size = size;		
		packets = new Object[size][2];		
		for (int i = 0; i < size; i++) {
			packets[i][0] = new byte[SENP.MaxPacketLen];			
		}		
		head = tail = fill = 0;		
	}	
	
	
	/* (non-Javadoc)
	 * @see siena.dvdrp.PacketQueue#add(byte[])
	 */
	synchronized public void add(byte[] obj) throws Exception {
		this.add(obj, obj.length);
	}
	
	/* (non-Javadoc)
	 * @see siena.dvdrp.PacketQueue#add(byte[], int)
	 */
	synchronized public void add(byte[] obj, int length) throws Exception {
		if (obj != null) {
//			Logging.prlnlog("Queue size: " + fill + " , " + head
//					+ " , " + tail, Logging.DEBUG);
				while(fill == size - 1){
					this.wait();
				}
				if (fill != size -1) {
					// copy byte per byte
					System.arraycopy(obj, 0, packets[tail][0], 0, length);
					packets[tail][1] = Integer.valueOf(length);
					tail = (tail == size -1) ? 0
							: tail+1;
					fill++;
				} else {
					throw new Exception("High priority queue is full: "
							+ "Queue size: " + fill + " , " + head
							+ " , " + tail);
				}
			
//			System.out.println("Queue size end: " + fill + " , " + head
//					+ " , " + tail);
			notifyAll();
		} else {
			throw new Exception("Trying to queue null packet");
		}

	}

	/* (non-Javadoc)
	 * @see siena.dvdrp.PacketQueue#next(byte[])
	 */
	public synchronized int next(byte[] buf) {
		while (fill == 0 && !release) {
			try {
				wait();
			} catch (InterruptedException ie) {
				// we continue but we'll eventually get out, we're probably
				// stopping the server
			}
		}
		if (fill != 0) {
			int current = head;
			// move to next
			head = (head == size-1) ? 0 : head+1;
			fill--;
			int length = ((Integer)packets[current][1]).intValue();
//			 copy byte per byte
			System.arraycopy((byte[]) packets[current][0], 0, buf, 0, length);
			notifyAll();
			return length;
		} 
		notifyAll();
		return 0;
	}
	
	
	public synchronized int next(byte[] buf, long timeout) {
		if (fill == 0 && !release) {
			try {
				wait(timeout);
			} catch (InterruptedException ie) {
				// we continue but we'll eventually get out, we're probably
				// stopping the server
			}
		}
		if (fill != 0) {
			int current = head;
			// move to next
			head = (head == size-1) ? 0 : head+1;
			fill--;
			int length = ((Integer)packets[current][1]).intValue();
//			 copy byte per byte
			System.arraycopy((byte[]) packets[current][0], 0, buf, 0, length);
			notifyAll();
			return length;
		} 
		notifyAll();
		return 0;
	}

	/* (non-Javadoc)
	 * @see siena.dvdrp.PacketQueue#clear()
	 */
	synchronized public void clear() {
		head = tail = fill = 0;		
		release = true;
		notifyAll();
	}

}