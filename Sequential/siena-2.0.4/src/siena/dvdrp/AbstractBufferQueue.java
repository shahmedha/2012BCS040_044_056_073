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

public abstract class AbstractBufferQueue implements PacketQueue {
	protected PoolAndQueue Q;
	
	public void add(byte[] buf) throws Exception {
		add(buf, buf.length);
	}

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

	public Buffer next() {
		return Q.dequeue_buffer();
	}

	public void recycle(Buffer buf) {
		try {
			Q.recycle_buffer(buf);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean hasNext() {
		return Q.hasNext();
	}

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

	public void clear() {
		// TODO Auto-generated method stub

	}

}