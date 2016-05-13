//
//  This file is part of Siena, a wide-area event notification system.
//  See http://www.inf.usi.ch/carzaniga/siena/
//
//  Authors: Antonio Carzaniga and Giovanni Toffetti Carughi
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

import java.util.BitSet;
import java.util.Iterator;

import siena.AttributeConstraint;
import siena.AttributeValue;
import siena.Filter;
import siena.Notification;
import siena.Op;

/**
 * a Bloom Filter implementation using a fixed-size BitSet.
 * 
 * Bloom filters are used as compact-size representations of content-based
 * <code>Filter</code>s (conjunctions of <code>AttributeConstraint</code>s)
 * as well as <code>Notifications</code> (messages) and their attributes.
 * 
 */
public class BSet implements Comparable<BSet> {

    public static int BLOOM_FILTER_SIZE = 256;

    // TODO: discuss visibility
    // I need to directly set bits only when I decode a packet
    public BitSet bits = new BitSet(BLOOM_FILTER_SIZE);

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
    //

    public boolean covers(BitSet other) {
	for (int i = other.nextSetBit(0); i >= 0; i = other.nextSetBit(i + 1)) {
	    if (!this.bits.get(i)) {
		return false;
	    }
	}
	return true;
    }

    // public boolean covers(BitSet other) {
    // /*
    // * logical AND between this and other. Only common bits are left
    // */
    // BitSet temp = (BitSet) other.clone();
    // temp.and(this.bits);
    // // return whether the number of bits is the same
    // return (other.cardinality() == temp.cardinality());
    // }
	
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
    //

    public boolean covers(BSet other) {
	return this.covers(other.bits);
    }

    void setBit(int position) {
	this.bits.set(position);
    }

    public String toString() {
	return this.bits.toString();
    }

    public boolean equals(BSet b) {
	return this.bits.equals(b.bits);
    }

    public void encodeFilter(Filter f) {
	if (f.isEmpty())
	    return;
	Iterator<String> i = f.constraintNamesIterator();
	while (i.hasNext()) {
	    String name = i.next();
	    Iterator<AttributeConstraint> j = f.constraintsIterator(name);
	    while (j.hasNext()) {
		AttributeConstraint cons = j.next();
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

    public void encodeEquals(String name, AttributeValue value) {
	Hash h7 = new Hash(7);
	Hash h3 = new Hash(3);
	// TODO: fix char encoding issues here
	h7.add(name.getBytes());
	h3.add(name.getBytes());
	h7.add('=');
	h3.add('=');
	switch (value.getType()) {
	case AttributeValue.STRING:
	    h7.add('s');
	    h3.add('s');
	    setBit(h7.add(value.byteArrayValue()) % BSet.BLOOM_FILTER_SIZE);
	    setBit(h3.add(value.byteArrayValue()) % BSet.BLOOM_FILTER_SIZE);
	    break;
	case AttributeValue.INT:
	    h7.add('i');
	    h3.add('i');
	    setBit(h7.add(value.intValue()) % BSet.BLOOM_FILTER_SIZE);
	    setBit(h3.add(value.intValue()) % BSet.BLOOM_FILTER_SIZE);
	    break;
	case AttributeValue.DOUBLE:
	    h7.add('d');
	    h3.add('d');
	    setBit(h7.addDouble(value.doubleValue()) % BSet.BLOOM_FILTER_SIZE);
	    setBit(h3.addDouble(value.doubleValue()) % BSet.BLOOM_FILTER_SIZE);
	    break;
	case AttributeValue.BOOL:
	    if (value.booleanValue()) {
		setBit(h7.add('T') % BSet.BLOOM_FILTER_SIZE);
		setBit(h3.add('T') % BSet.BLOOM_FILTER_SIZE);
	    } else {
		setBit(h7.add('F') % BSet.BLOOM_FILTER_SIZE);
		setBit(h3.add('F') % BSet.BLOOM_FILTER_SIZE);
	    }
	    break;
	case AttributeValue.NULL:
	    setBit(h7.add('*') % BSet.BLOOM_FILTER_SIZE);
	    setBit(h3.add('*') % BSet.BLOOM_FILTER_SIZE);
	    break;
	}

    }

    public void encodeExists(String name, AttributeValue value) {

	Hash h7 = new Hash(7);
	Hash h3 = new Hash(3);
	// TODO: fix char encoding issues here
	h7.add(name.getBytes());
	h3.add(name.getBytes());
	h7.add('*');
	h3.add('*');
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
	setBit(h7.add(c) % BSet.BLOOM_FILTER_SIZE);
	setBit(h3.add(c) % BSet.BLOOM_FILTER_SIZE);

    }

    public BSet(Notification n) {
	super();
	this.encodeNotification(n);
    }

    public BSet(Filter f) {
	super();
	this.encodeFilter(f);
    }

    public BSet() {
	super();
    }

    public BSet(BSet other) {
	super();
	this.bits = (BitSet) other.bits.clone();
    }

    public int compareTo(BSet other) {
	for (int i = 0; i < BSet.BLOOM_FILTER_SIZE; i++) {
	    if (this.bits.get(i) && !other.bits.get(i)) {
		return 1;
	    } else if (!this.bits.get(i) && other.bits.get(i)) {
		return -1;
	    }
	}
	return 0;
    }
}
