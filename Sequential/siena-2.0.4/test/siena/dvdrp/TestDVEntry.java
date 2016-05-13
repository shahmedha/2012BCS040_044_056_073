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
import siena.AttributeConstraint;
import siena.Filter;
import siena.Op;

public class TestDVEntry extends TestCase {
	public void testDVEntry(){
		DVEntry d = new DVEntry();
		BSetBV b = new BSetBV();
		BSetBV c = new BSetBV();
		d.setDest("Destination".getBytes());
		d.setDist(5);
		d.setNextHopId("NextHop".getBytes());
		Filter f = new Filter();
		f.addConstraint("Alert", new AttributeConstraint(Op.EQ, "red"));
		f.addConstraint("TextMessage", new AttributeConstraint(Op.SS, "base"));
		b.encodeFilter(f);
		f.addConstraint("Severity", new AttributeConstraint(Op.GT, 5));		
		d.setEntryTS(System.currentTimeMillis());
		System.out.println(d);
	}
}
