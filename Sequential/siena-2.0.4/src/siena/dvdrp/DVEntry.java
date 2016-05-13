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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import siena.Filter;

/**
 * a <code>DistanceVector</code> entry.
 * 
 * The <code>DistanceVector</code> contains a <code>DVEntry</code> for each
 * <code>DVDRPDispatcher</code> having subscriptions. A <code>DVEntry</code>
 * contains the identifier of the recipient, the interface to reach it, the
 * number of hops, the subscription predicates encoded as Bloom Filters, a local
 * timestamp, and a remote predicate timestamp.
 * 
 */

public class DVEntry {
	private byte[] dest; // identifier of the destination node

	private int dist; // distance in hops

	// TODO: FIX: the protocol actually provides a list of alternative next-hops
	// ordered by ascending distance to destination
	private byte[] nextHopId; // identifier of the next-hop node on the
	// shortest path to destination

//	private byte[] secondaryHopId;
	
	/*
	 * This is where we store an alternative interface to be used in case the
	 * link with a neighbor breaks. NOTE: we should use a different data
	 * structure to keep this info that is only neighbor-related (e.g. the
	 * neighbor map in DVDRPDispatcher), but for sake of simplicity for the
	 * moment we keep it here.
	 */

	// private int secondaryHopDist = Integer.MAX_VALUE; // distance in hops
	// using secondary path
	
	private long entryTS; // set by current host to track old entries

	public DVEntry() {
		super();
	}

	public int getDist() {
		return dist;
	}

	public void setDist(int dist) {
		this.dist = dist;
	}

	public long getEntryTS() {
		return entryTS;
	}

	public void setEntryTS(long entryTS) {
		this.entryTS = entryTS;
	}

	public byte[] getDest() {
		return dest;
	}

	public void setDest(byte[] dest) {
		this.dest = dest;
	}

	public byte[] getNextHopId() {
		return nextHopId;
	}

	public void setNextHopId(byte[] nextHopId) {
//		if (!Arrays.equals(this.nextHopId, nextHopId)) {
//			this.secondaryHopId = this.nextHopId;
//		}
		this.nextHopId = nextHopId;
	}

	public synchronized String toString() {
		StringBuffer out = new StringBuffer();
		out.append(new String(dest)).append(" | ").append(dist).append(" | ")
				.append(new String(nextHopId)).append(" | ");
//		if (secondaryHopId != null) {
//			out.append(new String(secondaryHopId)).append("(2nd) | ");
//		}
		out.append(entryTS).append("\n");
//		Iterator<BSetBV> i = filters.iterator();
//		while (i.hasNext()) {
//			BSetBV b = (BSetBV) i.next();
//			out.append(b.toString()).append("\n");
//		}
		return out.toString();
	}

	/*
	 * public int getSecondaryHopDist() { return secondaryHopDist; }
	 * 
	 * public void setSecondaryHopDist(int secondaryHopDist) {
	 * this.secondaryHopDist = secondaryHopDist; }
	 * 
	 * public byte[] getSecondaryHopId() { return secondaryHopId; }
	 * 
	 * 
	 * 
	 * public void switchPrimaryAndSecondary(){ byte[] temp =
	 * getSecondaryHopId(); int tempD = getSecondaryHopDist();
	 * setSecondaryHopId(getNextHopId()); setSecondaryHopDist(getDist());
	 * setNextHopId(temp); setDist(tempD); }
	 */

//	public void setSecondaryHopId(byte[] secondaryHopId) {
//		if (!Arrays.equals(secondaryHopId, nextHopId)
//				&& !Arrays.equals(secondaryHopId, this.secondaryHopId)) {
//			this.secondaryHopId = secondaryHopId;
//		}
//	}

//	public byte[] getSecondaryHopId() {
//		return secondaryHopId;
//	}

	
//	public int getMemOccupation() {
//		int res = 0;
//		res += this.dest.length;
//		res += 4; // account for dist = int = 32 bits
//		res += filters.size() * BSet.BLOOM_FILTER_SIZE;
//		// nextHopId
//		res += this.nextHopId.length;
//		// filterTS
//		res += 8; // long = 8 bytes
//		// entryTS
//		res += 8;
//		return res;
//	}

	public DVEntry(DVEntry other) {
		super();		
		this.setDest(other.getDest());
		this.setDist(other.getDist());
		this.setEntryTS(other.getEntryTS());
		this.nextHopId = other.getNextHopId();
//		this.secondaryHopId = other.getSecondaryHopId();		
	}

	

}
