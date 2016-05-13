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

import junit.framework.TestCase;

public class TestOrderedByteArrayMap extends TestCase {

		OrderedByteArrayMap<DVEntry> map = new OrderedByteArrayMap<DVEntry>();
		DVEntry a = new DVEntry();
		DVEntry b = new DVEntry();
		DVEntry c = new DVEntry();

		@Override
		protected void setUp() throws Exception {
			// TODO Auto-generated method stub
			super.setUp();			
			a.setDest("S6".getBytes());
			a.setNextHopId("S10".getBytes());
			b.setDest("S10".getBytes());
			b.setNextHopId("S10".getBytes());
			c.setDest("S11".getBytes());
			c.setNextHopId("S11".getBytes());
			map.put("S6".getBytes(), a);
			map.put("S10".getBytes(), b);
			map.put("S10".getBytes(), c);
		}
		
		public void testOrderedByteArrayMap(){
			System.out.println(map.get("S10".getBytes()));
			System.out.println(map.get("S6".getBytes()));
		}
}
