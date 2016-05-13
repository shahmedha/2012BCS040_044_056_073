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


public class TwoPrioritiesBufferQueue extends AbstractTwoPrioritiesBufferQueue implements TwoPrioritiesQueue {

	BufferQueue high;

	BufferQueue low;

	public static int DEFAULT_SIZE_HIGH = 100;

	public static int DEFAULT_SIZE_LOW = 15;

	protected int high_size = DEFAULT_SIZE_HIGH;

	protected int low_size = DEFAULT_SIZE_LOW;

	public TwoPrioritiesBufferQueue() {
		high = new BufferQueueImpl(high_size);
		low = new BufferQueueImpl(low_size);
	}

	public TwoPrioritiesBufferQueue(int high_size, int low_size) {
		this.high_size = high_size;
		this.low_size = low_size;
		high = new BufferQueueImpl(high_size);
		low = new BufferQueueImpl(low_size);
	}
	
}
