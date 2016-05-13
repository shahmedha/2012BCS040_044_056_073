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

public class StaticBufferQueue extends AbstractBufferQueue implements BufferQueue {

	public static String BUFFER_POOL_SIZE = "BUFFER_POOL_SIZE";
	
	public StaticBufferQueue() {
		Q = new StaticBufferPoolAndQueue();
	}

	public StaticBufferQueue(int size) {
		Q = new StaticBufferPoolAndQueue(size);
	}

}

class StaticBufferPoolAndQueue implements PoolAndQueue {
	
	protected int QueueSize = 50;

	protected Buffer[] queue;
	
	protected int head;

	protected int tail;

	private static Buffer[] free = new Buffer[0];

	private static volatile int free_count;

	public static int PoolSize = 5000;
	
	static {
		/*
		 * we init poolSize using a system property if available 
		 */
		if (System.getProperty(StaticBufferQueue.BUFFER_POOL_SIZE) != null){
			PoolSize = Integer.parseInt(System.getProperty(StaticBufferQueue.BUFFER_POOL_SIZE));
			Logging.prlnlog("Initializing system queues to " + PoolSize + "packets", Logging.DEBUG);
		}
	}

	public StaticBufferPoolAndQueue() {
		queue = new Buffer[QueueSize];
		synchronized (free) {
			if (free.length == 0) {
				free = new Buffer[PoolSize];
				for (int i = 0; i < PoolSize; ++i) {
					free[i] = new Buffer();
				}
				free_count = PoolSize;
			}
		}
		head = tail = 0;
	}

	public StaticBufferPoolAndQueue(int queueSize) {
		QueueSize = queueSize;
		queue = new Buffer[QueueSize];
		synchronized (this) {
			if (free.length == 0) {
				free = new Buffer[PoolSize];
				for (int i = 0; i < PoolSize; ++i) {
					free[i] = new Buffer();
				}
				free_count = PoolSize;
			}
		}
		head = tail = 0;
	}

	/**
	 * @override
	 */
	public void enqueue_buffer(Buffer buf) throws QueueFullException {
		// Logging.prlnlog("Enqueue Queue free: " + free_count + " size: " +
		// QueueSize + " head: " + head + " tail: " + tail, Logging.DEBUG);
		synchronized (queue) {
			if ((tail + 1) % QueueSize == head) {
				queue.notifyAll();
				throw new QueueFullException(
						"Queue has reached its max size limit (" + QueueSize
								+ ")");
			}
			queue[tail] = buf;
			tail = (tail + 1) % QueueSize;
			queue.notifyAll();
		}
	}

	/**
	 * @override
	 */
	public Buffer dequeue_buffer() {
		// Logging.prlnlog("Dequeue Queue free: " + free_count + " size: " +
		// QueueSize + " head: " + head + " tail: " + tail, Logging.DEBUG);
		synchronized (queue) {
			while (tail == head)
				try {
					queue.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
				}
			Buffer buf = queue[head];
			head = (head + 1) % QueueSize;
			queue.notifyAll();
			return buf;
		}
	}

	/**
	 * @override
	 */
	public void recycle_buffer(Buffer buf) throws InterruptedException {
		// Logging.prlnlog("Recycle Queue free: " + free_count + " size: " +
		// QueueSize + " head: " + head + " tail: " + tail, Logging.DEBUG);
		synchronized (free) {
			while (free_count >= PoolSize)
				free.wait();
			free[free_count] = buf;
			++free_count;
			free.notifyAll();
		}
	}

	// public Buffer get_buffer() throws InterruptedException {
	// synchronized (free) {
	// while (free_count == 0)
	// free.wait();
	// Buffer buf = free[free_count - 1];
	// --free_count;
	// free.notifyAll();
	// return buf;
	// }
	// }

	/**
	 * @override
	 */
	// GT: changed so that it throws an exception if queue is full
	public Buffer get_buffer() throws QueueFullException {
		// Logging.prlnlog("GetBuffer Queue free: " + free_count + " size: " +
		// QueueSize + " head: " + head + " tail: " + tail, Logging.DEBUG);
		synchronized (free) {
			if (free_count == 0) {
				free.notifyAll();
				throw new QueueFullException("No more free packets in pool");
			}
			Buffer buf = free[free_count - 1];
			--free_count;
			free.notifyAll();
			return buf;
		}
	}

	/**
	 * @override
	 */
	public boolean hasNext() {
		synchronized (queue) {
			if (head == tail)
				return false;
			return true;
		}
	}
}
