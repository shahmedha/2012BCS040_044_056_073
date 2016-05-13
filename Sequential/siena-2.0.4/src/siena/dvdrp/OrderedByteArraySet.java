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

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * an ordered Map using byte arrays as keys.
 *  
 */

public class OrderedByteArraySet extends TreeSet<byte[]> implements Set<byte[]> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public OrderedByteArraySet(){
		super(new ByteArrayComparator());
	}
	
	
	/**
	 * for debugging purposes only
	 */	
	public String toString(){
		String result = "";
		Iterator<byte[]> i = this.iterator();
		while(i.hasNext()){			
			result = result.concat(new String(i.next())).concat(" ");
		}
		return result;
	}

	

}
