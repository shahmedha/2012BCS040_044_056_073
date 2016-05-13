//
//  This file is part of Siena, a wide-area event notification system.
//  See http://www.inf.usi.ch/carzaniga/siena/
//
//  Author: Antonio Carzaniga (firstname.lastname@usi.ch)
//  See the file AUTHORS for full details. 
//
//  Copyright (C) 1998-2002 University of Colorado
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
package siena;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Compare {

    public static List readPackets(String filename) 
	throws java.io.IOException, siena.SENPInvalidFormat {
	List<SENPPacket> l = new LinkedList<SENPPacket>();
	BufferedReader file = new BufferedReader(new FileReader(filename));
	String inl;
	SENPPacket p;
	while ((inl = file.readLine()) != null) {
	    byte[] pkt = inl.getBytes();
	    if (pkt.length > 0 && pkt[0] != 0x23 /* '#' */) {
		//		System.out.println(inl);
		// skip blank lines and comments
		p = new SENPPacket();
		p.init(pkt);
		p.decode();
		l.add(p);
	    }
	}
	return l;
    }

    public static boolean equals(Notification a, Notification b) {
	if (a.size() != b.size()) return false;

	Iterator<String> ai, bi;
	ai = a.attributeNamesIterator();
	bi = b.attributeNamesIterator();
	while(ai.hasNext()) {
	    String aname = ai.next();
	    String bname = bi.next();
	    //
	    // I know that the set of attribute names is ordered!
	    //
	    if(!aname.equals(bname)) {
		return false;
	    } else {
		AttributeValue aav = a.getAttribute(aname);
		AttributeValue bav = b.getAttribute(aname);
		if (!Covering.apply_operator(Op.EQ,aav,bav)) 
		    return false;
	    }
	}
	return true;
    }

    public static boolean equals(Filter a, Filter b) {
	//
	// this is meant to be a syntactical equality, but I have no
	// time to implement it that way now, so I'm relying on the
	// covering relations (semantic equivalence).
	//
	// Notice that syntactical equality is a stronger condition.
	//
	return Covering.covers(a,b) && Covering.covers(b,a);
    }

    public static boolean equals(Pattern a, Pattern b) {
	if (a.filters.length != b.filters.length) return false;
	for(int i = 0; i < a.filters.length; ++i)
	    if (!equals(a.filters[i], b.filters[i])) return false;
	return true;
    }

    public static boolean equals(Notification [] a, Notification [] b) {
	if (a.length != b.length) return false;
	for(int i = 0; i < a.length; ++i)
	    if (!equals(a[i], b[i])) return false;
	return true;
    }

    public static boolean equals(byte[] a, byte[] b) {
	if (a == null) return b == null;
	if (b == null || b.length != a.length) return false;
	for(int i=0; i<a.length; ++i) 
	    if (a[i] != b[i]) return false;
	return true;
    }

    public static boolean equals(SENPPacket a, SENPPacket b) {

	if (a.method != b.method) return false;
	if (!equals(a.id, b.id)) return false;
	if (!equals(a.to, b.to)) return false;

	if (a.filter != null || b.filter != null) {
	    if (b.filter == null || a.filter == null) return false;
	    return equals(a.filter, b.filter);
	} else if (a.event != null || b.event != null) {
	    if (b.event == null || a.event == null) return false;
	    return equals(a.event, b.event);
	} else if (a.events != null || b.events != null) {
	    if (b.events == null || a.events == null) return false;
	    return equals(a.events, b.events);
	} else if (a.pattern != null || b.pattern != null) {
	    if (b.pattern == null || a.pattern == null) return false;
	    return equals(a.pattern, b.pattern);
	}
	return true;
    }
    
    public static void main(String[] args) {
	boolean ok = true;
	try {
	    if (args.length != 2) {
		System.err.println("usage: Compare <actual> <expected>\n");
		System.exit(1);
	    }
	    List actual, expected;
	    actual = readPackets(args[0]);
	    expected = readPackets(args[1]);

	    boolean found;
	    Iterator ei, ai;
	    SENPPacket ep, ap;

	    ei = expected.iterator();
	    while(ei.hasNext()) {
		ep = (SENPPacket)ei.next();
		ai = actual.iterator();
		found=false;
		while(ai.hasNext()) {
		    ap = (SENPPacket)ai.next();
		    if (equals(ap, ep)) {
			found=true;
			ai.remove();
			break;
		    }
		}
		if (!found) {
		    if (ok) {
			ok = false;
			System.out.println(">>> MISSING >>>");
		    }
		    System.out.println(ep.toString());
		}
	    }
	    if (!actual.isEmpty()) {
		ok = false;
		System.out.println("<<< EXTRA <<<");
		ai = actual.iterator();
		while(ai.hasNext()) {
		    ap = (SENPPacket)ai.next();
		    System.out.println(ap.toString());
		}
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
	System.exit(ok ? 0 : 1);
    }
}
