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
public class FastArrayQueue extends ArrayQueue {
	public static int DEFAULT_SIZE = 100;

	// We store a byte array and the length to be sent
	private Object[][] packets;

	private int head, tail, size;

	private volatile boolean release = false;

	// private Object enqueueCondition = new Object();
	//
	// private Object dequeueCondition = new Object();

	Mutex queueLock = new Mutex();

	public FastArrayQueue() {
		this.size = DEFAULT_SIZE;
		packets = new Object[size][3];
		// for (int i = 0; i < size; i++) {
		// packets[i][0] = new byte[SENP.MaxPacketLen];
		// packets[i][1] = new Integer(0);
		// }
		head = tail = 0;
	}

	public FastArrayQueue(int size) {
		this.size = size;
		packets = new Object[size][3];
		// for (int i = 0; i < size; i++) {
		// packets[i][0] = new byte[SENP.MaxPacketLen];
		// packets[i][1] = new Integer(0);
		// }
		head = tail = 0;
	}

	public void add(byte[] obj) throws Exception {
		this.add(obj, obj.length);
	}

	public void add(byte[] obj, int length) throws Exception {
		if (obj != null) {
//			Logging.prlnlog(Thread.currentThread().getId()
//					+ " Adding to queue " + new String(obj, 0, length) + " "
//					+ this + " size: " + head + " , " + tail, Logging.DEBUG);
			// System.out.println(Thread.currentThread().getId()
			// + " Adding to queue " + new String(obj, 0, length) + " "
			// + this + " size: " + head + " , " + tail);
			int insertAt = -1;
			Mutex bufLock = null;
			// if indexes are equal queue is empty, we can add
			synchronized (queueLock) {
				while ((tail % size == (head - 1) % size) && !release
						&& queueLock.isLocked()) {
					try {
						queueLock.wait();
					} catch (InterruptedException ie) {
						// we continue but we'll eventually get out, we're
						// probably
						// stopping the server
						ie.printStackTrace();
					}
				}	
				System.out.println(Thread.currentThread().getId()
						+ " add before lock - Queue : " + head + " , " + tail);
				queueLock.lock();
			}
			if (tail % size != (head - 1) % size) {
				insertAt = tail;
				tail = (tail + 1) % size;
				System.out.println(Thread.currentThread().getId()
						+ " after add - Queue : " + head + " , " + tail);
				// check if buffer was inited
				if (packets[insertAt][0] == null) {
					packets[insertAt][0] = new byte[SENP.MaxPacketLen];
					packets[insertAt][1] = new Integer(0);
					packets[insertAt][2] = new Mutex();
				}
				// lock the buffer
				bufLock = (Mutex) packets[insertAt][2];
				bufLock.lock();
			} else {
				// queue is still full!!!
				// release the lock
				queueLock.unlock();
//				System.out.println(Thread.currentThread().getId()
//						+ " add after unlock - Queue : " + head + " , " + tail);
				throw new Exception("Queue is full " + Thread.currentThread().getId());				
			}
			// release the lock
			queueLock.unlock();
			if (bufLock != null) {
				// do actual copy
				// copy byte per byte
				System.arraycopy(obj, 0, packets[insertAt][0], 0, length);
				packets[insertAt][1] = Integer.valueOf(length);
				// release the buffer lock
				bufLock.unlock();
//				Logging.prlnlog(Thread.currentThread().getId()
//						+ " Added pack, notifyingAll. queue size: " + head
//						+ " , " + tail, Logging.DEBUG);
			}

		} else {
			new Exception("Trying to queue null packet").printStackTrace();
		}

	}

	public int next(byte[] buf) {
		int current = -1;
		Mutex bufLock = null;
		
		synchronized (queueLock) {
			while (head == tail && !queueLock.isLocked() && !release) {
				try {
					queueLock.wait();
				} catch (InterruptedException ie) {
					// we continue but we'll eventually get out, we're
					// probably
					// stopping the server
					ie.printStackTrace();
				}
			}
			// lock
			queueLock.lock();
		}

		System.out.println(Thread.currentThread().getId()
				+ " next synch - Queue : " + head + " , " + tail);
		if (head != tail) {
				current = head;
				// move to next
				head = (head == size - 1) ? 0 : head + 1;
				System.out.println(Thread.currentThread().getId()
						+ " next modified - Queue : " + head + " , " + tail);
				// lock buffer
				bufLock = (Mutex) packets[current][2];
				bufLock.lock();				
		}
		// release queue lock
		queueLock.unlock();
		if (bufLock != null) {
			// do actual copy
			System.arraycopy((byte[]) packets[current][0], 0, buf, 0,
					((Integer) packets[current][1]).intValue());
			int res = ((Integer) packets[current][1]).intValue();
			// unlock buffer
			bufLock.unlock();
			return res;
		} else {
			return 0;
		}

	}

	// private synchronized int put() {
	// int insertAt = -1;
	// while (tail == (head -1) && !release) {
	// try {
	// wait();
	// } catch (InterruptedException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// if (tail != (head -1)) {
	// insertAt = tail;
	// tail = (tail == size - 1) ? 0 : tail + 1;
	// } else {
	// new Exception("High priority queue is full: " + "Queue : "
	// + head + " , " + tail).printStackTrace();
	// }
	// this.notifyAll();
	// return insertAt;
	// }

	synchronized public void clear() {
		head = tail = 0;
		release = true;		
		this.notifyAll();
	}

}

class Mutex extends Object {

	private boolean locked = false;

	synchronized void lock() {
		while (locked) {
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		locked = true;	
		System.out.println("locked by: " + Thread.currentThread().getId());
	}

	synchronized void unlock() {
		System.out.println("unlock called by: " + Thread.currentThread().getId());
		if (!locked) {
			new Exception("Mutex PANIC!!!" + + Thread.currentThread().getId()).printStackTrace();
		} else {
			locked = false;
			notifyAll();
		}
	}
	
	boolean isLocked(){
		return locked;
	}
	

}

class queueHelper extends Object {

	private boolean locked = false;

	synchronized void lock() {
		while (locked) {
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		locked = true;	
		System.out.println("locked by: " + Thread.currentThread().getId());
	}

	synchronized void unlock() {
		System.out.println("unlock called by: " + Thread.currentThread().getId());
		if (!locked) {
			new Exception("Mutex PANIC!!!" + + Thread.currentThread().getId()).printStackTrace();
		} else {
			locked = false;
			notifyAll();
		}
	}
	
	boolean isLocked(){
		return locked;
	}
	

}
