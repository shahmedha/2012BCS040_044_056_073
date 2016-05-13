//
//  This file is part of Siena, a wide-area event notification system.
//  See http://www.inf.usi.ch/carzaniga/siena/
//
//  Author: Antonio Carzaniga
//  See the file AUTHORS for full details. 
//
//  Copyright (C) 2011 Antonio Carzaniga
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

import siena.AttributeConstraint;
import siena.AttributeValue;
import siena.Filter;
import siena.Notification;
import siena.Op;

/**
 * a Bloom Filter implementation using a compact bitvector implemented
 * as an array of long values.
 */
public class BSetBV implements Comparable<BSetBV> {

    public static final int M = 256;
    public long[] bv = new long[M / Long.SIZE];

    public boolean covers(BSetBV other) {
	for(int i = 0; i < bv.length; ++i)
	    if ((this.bv[i] & other.bv[i]) != other.bv[i])
		return false;
	return true;
    }

    public void setBit(int bit_pos) {
	bit_pos %= M;
	bv[bit_pos / Long.SIZE] |= (1L << (bit_pos % Long.SIZE));
    }

    public String toString() {
	byte[] str = new byte[M];
	for(int pos = 0; pos < M; ++pos) {
	    if ((bv[pos / Long.SIZE] & (1L << pos % Long.SIZE)) == 0)
		str[M - pos - 1] = 48; /* '0' */
	    else 
		str[M - pos - 1] = 49; /* '1' */
	}
	return new String(str);
    }

    public boolean equals(Object o) {
	if (o == null || !(o instanceof BSetBV))
	    return false;
	BSetBV other = (BSetBV)o;
	for(int i = 0; i < bv.length; ++i)
	    if (this.bv[i] != other.bv[i])
		return false;
	return true;
    }

    public void encodeFilter(Filter f) {
	if (f.isEmpty())
	    return;
	Iterator<String> i = f.constraintNamesIterator();
	while (i.hasNext()) {
	    String name = i.next();
	    // System.out.println("Constraint name: " + name);
	    Iterator<siena.AttributeConstraint> j = f.constraintsIterator(name);
	    while (j.hasNext()) {
		AttributeConstraint cons = j.next();
		// System.out.println("Constraint cons: " + cons.value);
		encodeConstraint(name, cons);
	    }
	}
    }

    public void encodeNotification(Notification n) {
	if (n == null || n.size() == 0)
	    return;
	Iterator<String> i = n.attributeNamesIterator();
	while (i.hasNext()) {
	    String name = i.next();
	    encodeNotificationAttribute(name, n.getAttribute(name));
	}
    }

    public void encodeNotificationAttribute(String name, AttributeValue value) {
	if (value.getType() != AttributeValue.NULL) {
	    encodeEquals(name, value);
	}
	encodeExists(name, value);
    }

    public void encodeConstraint(String name, AttributeConstraint cons) {
	if ((cons.op == Op.EQ) && (cons.value.getType() != AttributeValue.NULL)) {
	    encodeEquals(name, cons.value);
	}
	encodeExists(name, cons.value);
    }

    private static final int K = 10;

    public void encodeEquals(String name, AttributeValue value) {
	Hash [] h = new Hash[K];
	for (int i = 0; i < K; ++i)
	    h[i] = new Hash(i+2);

	// TODO: fix char encoding issues here
	
	for (int i = 0; i < K; ++i)
	    h[i].add(name.getBytes());
	for (int i = 0; i < K; ++i)
	    h[i].add('=');

	switch (value.getType()) {
	case AttributeValue.STRING:
	    for (int i = 0; i < K; ++i)
		h[i].add('s');
	    for (int i = 0; i < K; ++i)
		setBit(h[i].add(value.byteArrayValue()));
	    break;
	case AttributeValue.INT:
	    for (int i = 0; i < K; ++i)
		h[i].add('i');
	    for (int i = 0; i < K; ++i)
		setBit(h[i].add(value.intValue()));
	    break;
	case AttributeValue.DOUBLE:
	    for (int i = 0; i < K; ++i)
		h[i].add('d');
	    for (int i = 0; i < K; ++i)
		setBit(h[i].addDouble(value.doubleValue()));
	    break;
	case AttributeValue.BOOL:
	    if (value.booleanValue()) {
		for (int i = 0; i < K; ++i)
		    setBit(h[i].add('T'));
	    } else {
		for (int i = 0; i < K; ++i)
		    setBit(h[i].add('F'));
	    }
	    break;
	case AttributeValue.NULL:
	    for (int i = 0; i < K; ++i)
		setBit(h[i].add('*'));
	    break;
	}
    }

    public void encodeExists(String name, AttributeValue value) {
	Hash [] h = new Hash[K];
	for (int i = 0; i < K; ++i)
	    h[i] = new Hash(i+2);

	for (int i = 0; i < K; ++i) {
	    h[i].add(name.getBytes());
	    h[i].add('*');
	}
	char c;
	switch (value.getType()) {
	case AttributeValue.STRING:
	    c = 's';
	    break;
	case AttributeValue.INT:
	case AttributeValue.DOUBLE:
	    c = 'n';
	    break;
	case AttributeValue.BOOL:
	    c = 'b';
	    break;
	case AttributeValue.NULL: // TODO: was "anytype", check whether NULL
	    // is meant to be the same
	default:
	    c = '*';
	    break;
	}
	// TODO: we can also
	// arrange for different
	// sizes using the
	// actual byte array
	// size instead of a
	// fixed size
	for (int i = 0; i < K; ++i) 
	    setBit(h[i].add(c));
    }

    public BSetBV(Notification n) {
	super();
	this.encodeNotification(n);
    }

    public BSetBV(Filter f) {
	super();
	this.encodeFilter(f);
    }

    public BSetBV() {
	super();
    }

    public BSetBV(BSetBV other) {
	super();
	System.arraycopy(other.bv, 0, this.bv, 0, bv.length);
    }

    public int compareTo(BSetBV other) {
	for (int i = 0; i < bv.length; ++i) {
	    if (this.bv[i] < other.bv[i]) {
		return -1;
	    } else if (this.bv[i] > other.bv[i]) {
		return -1;
	    }
	}
	return 0;
    }
}
