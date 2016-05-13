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

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

public class BSetTable {
	
	Map<BSet,BSetNode> tempMap =  new TreeMap<BSet,BSetNode>();
	public BSetNode[] table;
	
	public void consolidate(){
		table = new BSetNode[tempMap.size()];
		int i = 0;
		for(BSetNode node : tempMap.values()){
			node.consolidate();
			table[i] = node;
			i++;
		}
		Arrays.sort(table, BSetNode.OnesComparator);
	}
	
	public void add(BSet bf, DVEntry dve){
		BSetNode node;
		if (tempMap.containsKey(bf)){
			node = tempMap.get(bf);
			node.add(dve);
		} else {
			node = new BSetNode(bf,dve);
			tempMap.put(bf, node);
		}
	}

	public void clear() {
		tempMap.clear();
		table = null;
	}
	

}
