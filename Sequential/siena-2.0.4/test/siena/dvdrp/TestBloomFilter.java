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
import siena.Notification;
import siena.Op;

/**
 * @author tof
 * 
 */
public class TestBloomFilter extends TestCase {

	private BSet a,b,c,d,e;
	
	protected void setUp() throws Exception {
        super.setUp();		
    }
	
	public void testCoverage(){
		a = new BSet();
        b = new BSet();
        c = new BSet();
        d = new BSet();
        e = new BSet();
		Filter f = new Filter();
		Filter g = new Filter();
		Notification h = new Notification();
		Notification i = new Notification();
		f.addConstraint("Alert", new AttributeConstraint(Op.EQ, "red"));
		f.addConstraint("TextMessage", new AttributeConstraint(Op.SS, "base"));
		f.addConstraint("Severity", new AttributeConstraint(Op.GT, 5));
		g.addConstraint("Alert", new AttributeConstraint(Op.EQ, "red"));
		h.putAttribute("Alert", "red");
		h.putAttribute("TextMessage", "All your bases are belong to us");
		h.putAttribute("Severity", 7);		
		i.putAttribute("Alert", "red");
		i.putAttribute("TextMessage", "Base attack");
		i.putAttribute("Severity", 4);
		a.encodeFilter(f);
		b.encodeFilter(g);
		c.encodeNotification(h);
		d.encodeNotification(i);
		e.encodeNotification(i);
		System.out.println("a: " + a);
		System.out.println("b: " + b);
		System.out.println("c: " + c);
		System.out.println("d: " + d);
		assertTrue(a.covers(a));
		assertTrue(a.covers(b));		
		assertFalse(b.covers(a));		
		assertTrue(c.covers(a));
		assertTrue(c.covers(b));
		assertTrue(d.covers(a));
		assertTrue(d.equals(e));	
	}

    public void testBSetBV(){
	siena.Filter f = new siena.Filter();
	f.addConstraint("弘揚純善純美的中國傳統服飾", true);
	BSetBV bv = new BSetBV(f);
    }
}
