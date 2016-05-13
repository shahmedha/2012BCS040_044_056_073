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

public class BufferQueueImpl implements PacketQueue, BufferQueue {
	protected BufferPoolAndQueue Q;

	public BufferQueueImpl() {
		Q = new BufferPoolAndQueue();
	}

	public BufferQueueImpl(int size) {
		Q = new BufferPoolAndQueue(size);
	}

	/* (non-Javadoc)
	 * @see siena.dvdrp.BufferQueue#add(byte[])
	 */
	public void add(byte[] buf) throws Exception {
		add(buf, buf.length);
	}

	/* (non-Javadoc)
	 * @see siena.dvdrp.BufferQueue#next(byte[])
	 */
	public int next(byte[] buf) {
		Buffer q_buf;
		try {
			q_buf = Q.dequeue_buffer();
			for (int i = 0; i < q_buf.getSize(); ++i)
				buf[i] = q_buf.bytes[i];
			int length = q_buf.getSize();
			Q.recycle_buffer(q_buf);
			return length;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see siena.dvdrp.BufferQueue#next()
	 */
	public Buffer next() {
		return Q.dequeue_buffer();
	}

	/* (non-Javadoc)
	 * @see siena.dvdrp.BufferQueue#recycle(siena.dvdrp.Buffer)
	 */
	public void recycle(Buffer buf) {
		try {
			Q.recycle_buffer(buf);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see siena.dvdrp.BufferQueue#hasNext()
	 */
	public boolean hasNext() {
		return Q.hasNext();
	}

	/* (non-Javadoc)
	 * @see siena.dvdrp.BufferQueue#add(byte[], int)
	 */
	public void add(byte[] buf, int length) throws QueueFullException {
		Buffer new_buf = Q.get_buffer();
		for (int i = 0; i < length; ++i)
			new_buf.bytes[i] = buf[i];
		new_buf.setSize(length);
		try{
		Q.enqueue_buffer(new_buf);
		} catch (QueueFullException qfe) {
			// we have to put back the packet in the free pool
			try {
				Q.recycle_buffer(new_buf);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// then we let the exception up
			throw qfe;
		}
	}

	/* (non-Javadoc)
	 * @see siena.dvdrp.BufferQueue#clear()
	 */
	public void clear() {
		// TODO Auto-generated method stub

	}

}

class BufferPoolAndQueue {

	protected int QueueSize = 50;

	protected Buffer[] queue;

	protected Buffer[] free;

	protected int free_count;

	protected int head;

	protected int tail;

	public BufferPoolAndQueue() {
		queue = new Buffer[QueueSize];
		free = new Buffer[QueueSize];
		for (int i = 0; i < QueueSize; ++i) {
			free[i] = new Buffer();
		}
		head = tail = 0;
		free_count = QueueSize;
	}

	public BufferPoolAndQueue(int size) {
		QueueSize = size;
		queue = new Buffer[QueueSize];
		free = new Buffer[QueueSize];
		for (int i = 0; i < QueueSize; ++i) {
			free[i] = new Buffer();
		}
		head = tail = 0;
		free_count = QueueSize;

	}

	public void enqueue_buffer(Buffer buf) throws QueueFullException {
		// Logging.prlnlog("Enqueue Queue free: " + free_count + " size: " +
		// QueueSize + " head: " + head + " tail: " + tail, Logging.DEBUG);
		synchronized (queue) {
			try {
				while ((tail + 1) % QueueSize == head)
					queue.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			queue[tail] = buf;
			tail = (tail + 1) % QueueSize;
			queue.notifyAll();
		}
	}

	public Buffer dequeue_buffer() {
		// Logging.prlnlog("Dequeue Queue free: " + free_count + " size: " +
		// QueueSize + " head: " + head + " tail: " + tail, Logging.DEBUG);
		synchronized (queue) {
			while (tail == head)
				try {
					queue.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			Buffer buf = queue[head];
			head = (head + 1) % QueueSize;
			queue.notifyAll();
			return buf;
		}
	}

	public void recycle_buffer(Buffer buf) throws InterruptedException {
		// Logging.prlnlog("Recycle Queue free: " + free_count + " size: " +
		// QueueSize + " head: " + head + " tail: " + tail, Logging.DEBUG);
		synchronized (free) {
			while (free_count >= QueueSize)
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

	// GT: changed so that it throws an exception if queue is full
	public Buffer get_buffer() throws QueueFullException {
		// Logging.prlnlog("GetBuffer Queue free: " + free_count + " size: " +
		// QueueSize + " head: " + head + " tail: " + tail, Logging.DEBUG);
		synchronized (free) {
			if (free_count == 0) {
				free.notifyAll();
				throw new QueueFullException("Queue full");
			}
			Buffer buf = free[free_count - 1];
			--free_count;
			free.notifyAll();
			return buf;
		}
	}

	public boolean hasNext() {
		synchronized (queue) {
			if (head == tail)
				return false;
			return true;
		}
	}
}
