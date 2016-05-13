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
import java.util.Map;


/**
 * an implementation of a distance vector
 * 
 *  
 */

/*It is merely an OrderedByteArrayMap, but we wrap it with a class to enforce 
 * correct typing*/

public class DistanceVector {	
	public Map<byte[],DVEntry> entries = new OrderedByteArrayMap<DVEntry>();
	
	public synchronized void addEntry(byte[] id, DVEntry entry){
		// TODO: prevent null keys and values from being set
		entries.put(id, entry);
	}
	
	public synchronized void removeEntry(byte[] id){
		entries.remove(id);		 
	}
	
	public DVEntry getEntry(byte[] id){
		return entries.get(id);
	}
	
	public Iterator<byte[]> getEntryIdsIterator(){
		return entries.keySet().iterator();
	}
	
	/**
	 * for debugging purposes only
	 */	
	public synchronized String toString(){
		String result = "";
		Iterator<byte[]> i = this.getEntryIdsIterator();
		while(i.hasNext()){	
			byte[] key = i.next();
			result = result.concat("key: " + new String(key) + " - ");
			result = result.concat(getEntry(key).toString());
		}
		return result;
	}
	
	public boolean containsOtherThan(byte[] id){
		if (entries.keySet().size() > 1){
			return true;
		} else {
			// than it should contain a single entry
			if (entries.keySet().contains(id)){
				return false;
			}
			return true;
		}
	}
	public synchronized void clear(){
		entries.clear();	
	}

	public int size() {
		// TODO Auto-generated method stub
		return entries.size();
	}
	
}
