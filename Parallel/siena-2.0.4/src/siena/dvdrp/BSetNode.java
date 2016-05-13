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

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class BSetNode {
	
	public static final Comparator<? super BSetNode> OnesComparator = new BSetOnesComparator();
	public BSet bset;
	public DVEntry[] recipients;
	
	// prevent duplicates using set
	public Set<DVEntry> tempRecipients = new HashSet<DVEntry>();
	
	public BSetNode(BSet bf, DVEntry dve) {
		bset = bf;
		this.add(dve);
	}

	public void add(DVEntry dve) {
		tempRecipients.add(dve);		
	}

	public void consolidate(){
		recipients = new DVEntry[tempRecipients.size()];
		int i = 0;
		for (DVEntry rec : tempRecipients){
			recipients[i] = rec;
			i++;
		}
		tempRecipients.clear();
	}
	
	
	
}


class BSetOnesComparator implements Comparator<BSetNode>{

	public int compare(BSetNode arg0, BSetNode arg1) {
		if (arg0.bset.bits.cardinality() > arg1.bset.bits.cardinality()){
			return 1;
		} else if (arg0.bset.bits.cardinality() < arg1.bset.bits.cardinality()){
			return -1;
		}
		return 0;
	}
	
}