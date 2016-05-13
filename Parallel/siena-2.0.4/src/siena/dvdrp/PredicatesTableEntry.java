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

public class PredicatesTableEntry {
    // identifier of the destination node
    private byte[] dest; 

    // list of predicates encoded as Bloom Filters
    public ArrayList<BSetBV> filters = new ArrayList<BSetBV>();

    // set by subscriber to let us distinguish between new and old
    // subscriptions
    private long filtersSeqNo;
	
    // We use incremental updates.  If this flag is set to true, then
    // the entry needs to be emptied
    public boolean cleanUp = false;

    public PredicatesTableEntry() {
	super();
    }

    public List<BSetBV> getFilters() {
	return filters;
    }

    public synchronized void addFilters(List<BSetBV> predicates) {
	this.filters.addAll(predicates);
    }

    public long getFiltersSeqNo() {
	return filtersSeqNo;
    }

    public void setFiltersSeqNo(long seqNo) {
	this.filtersSeqNo = seqNo;
    }

    public byte[] getDest() {
	return dest;
    }

    public void setDest(byte[] dest) {
	this.dest = dest;
    }

    public synchronized String toString() {
	StringBuffer out = new StringBuffer();
	out.append(new String(dest)).append(" | seqNo: ");
	out.append(filtersSeqNo).append(" | pred #: ").append(filters.size());
	if (cleanUp){
	    out.append(" | cleanup = true");
	}
	out.append("\n");
	return out.toString();
    }

	
    //
    // NOTICE: this "covers" relation is defined
    // in the sense of the Bloom filter coverage,
    // i.e., b1 covers b2 if all the 1-bit
    // positions of b2 are also 1-bit positions of
    // b1. In other words, if ((b1 & b2) == b2).
    // Because of the semantics of the Bloom
    // filter encoding, this relation is exactly
    // the opposite of the usual covering
    // relations.
    // Returns true is the filters contained are
    // more "general" (that is contain less '1s')
    // than the parameter filter

    public boolean covers(BSetBV bloomFilter) {
	for(BSetBV b : filters)
	    if (bloomFilter.covers(b))
		return true;
	return false;
    }
	
    public void copyFiltersSeqNo(PredicatesTableEntry externalEntry) {
	this.filtersSeqNo = externalEntry.filtersSeqNo;
    }

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

    public PredicatesTableEntry(PredicatesTableEntry other) {
	super();
	this.copyFiltersSeqNo(other);
	this.setDest(other.getDest());		
	//		this.secondaryHopId = other.getSecondaryHopId();
	for (BSetBV bf : other.getFilters()) {			
	    this.filters.add(new BSetBV(bf));
	}
    }

    public synchronized void addBSet(Filter f) {
	/*
	 * There's 3 cases: 
	 *
	 * A - the new filter is more general than one or more of the
	 * contained filters (in this case we need to remove the more
	 * specific filters)
	 * 
	 * B - there are no more-specific filters, and we simply add
	 * the new one
	 * 
	 * C - the new filter f is more specific than an existing one:
	 * nothing needs to be done.
	 */
	int N = filters.size();
	BSetBV new_bf = f.bloomFilter;
	for (int i = 0; i < N; ++i) {
	    BSetBV cur_bf = filters.get(i);
	    if(new_bf.covers(cur_bf))	// case C
		return;	
	    if (cur_bf.covers(new_bf)) {	// case A
		filters.set(i, new_bf); // we replace the first covering filter
		if (++i < N) {
		    cur_bf = filters.get(i);
		    while(true) {
			if (cur_bf.covers(new_bf)) {
			    if (i < --N) {
				cur_bf = filters.get(N);
				filters.set(i,cur_bf);
				filters.remove(N);
			    } else 
				break;
			} else {
			    if (++i < N) {
				cur_bf = filters.get(i);
			    } else
				break;
			}
		    }
		}
		return;
	    }
	}
	filters.add(new_bf);		// case B
    }

}
