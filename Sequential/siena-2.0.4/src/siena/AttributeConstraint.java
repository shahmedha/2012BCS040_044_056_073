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

import java.lang.Integer;
import java.lang.String;

/** an elementary predicate over an attribute in an event notification.
 *  
 *  <code>AttributeConstraint</code>s are the basic elements of a
 *  {@link Filter}.  <p>
 *  
 *  An <code>AttributeConstraint</code> is defined by an
 *  <em>operator</em> and a <em>value</em>, and it is associated with
 *  an attribute <em>name</em>.  Applying an
 *  <code>AttributeConstraint</code> with operator <em>op</em> and
 *  value <em>v</em> to an attribute value <em>x</em> means computing
 *  <em>x op v</em>.  <p>
 *  
 *  The operators provided by <code>AttributeConstraint</code> are
 *  defined in {@link Op}.  They are <em>equal</em>,
 *  <em>not-equal</em>, <em>less-than</em>, <em>less-or-equal</em>,
 *  <em>greater-than</em>,<em>greater-or-equal</em>,
 *  <em>has-substring</em>, <em>has-prefix</em>, <em>has-suffix</em>,
 *  and <em>any</em>.
 *  
 *  @see Op 
 *  @see Filter 
 **/
public class AttributeConstraint {
    /** the comparison value */
    public AttributeValue	value;

    /** the comparison operator 
     * 
     *  valid values are defined in {@link Op}
     **/
    public short		op;

    /** creates a (deep) copy of an attribute constraint */ 
    public AttributeConstraint(AttributeConstraint c) {
	value = new AttributeValue(c.value);
	op = c.op;
    }

    /** create an equality constraint with the given value */ 
    public AttributeConstraint(AttributeValue v) {
	value = v;
	op = Op.EQ;
    }

    /** create a constraint with the given string value */ 
    public AttributeConstraint(short o, String s) {
	value = new AttributeValue(s);
	op = o;
    }

    /** create a constraint with the given byte[] value */ 
    public AttributeConstraint(short o, byte[] s) {
	value = new AttributeValue(s);
	op = o;
    }

    /** create a constraint with the given int value */ 
    public AttributeConstraint(short o, int i) {
	value = new AttributeValue(i);
	op = o;
    }

    /** create a constraint with the given long value */ 
    public AttributeConstraint(short o, long i) {
	value = new AttributeValue(i);
	op = o;
    }

    /** create a constraint with the given long value */ 
    public AttributeConstraint(short o, double d) {
	value = new AttributeValue(d);
	op = o;
    }

    /** create a constraint with the given boolean value */ 
    public AttributeConstraint(short o, boolean b) {
	value = new AttributeValue(b);
	op = o;
    }

    /** create a constraint with the given value */ 
    public AttributeConstraint(short o, AttributeValue x) {
	value = new AttributeValue(x);
	op = o;
    }

    public boolean isEqualTo(AttributeConstraint x) {
	//
	// this is a conservative implementation.
	// 
	return op == x.op && (op == Op.ANY || value.isEqualTo(x.value));
    }

    public String toString() {
	SENPBuffer b = new SENPBuffer();
	b.encode(this);
	return new String(b.buf, 0, b.length());
    }

    public int hashCode() {
	return toString().hashCode();
    }
}
