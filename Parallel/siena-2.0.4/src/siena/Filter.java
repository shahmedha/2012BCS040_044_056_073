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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import siena.dvdrp.BSet;
import siena.dvdrp.BSetBV;

class Constraint implements siena.fwd.Constraint {
    private String name;
    private AttributeConstraint constraint;    

    public Constraint(String n, AttributeConstraint c) {
	name = n;
	constraint = c;
    }
    public String getName() {
	return name;
    }
    public siena.fwd.Value getValue() {
	return constraint.value;
    }
    public short getOperator() {
	return constraint.op;
    }
}

class ConstraintIterator implements Iterator<siena.fwd.Constraint> {
    private Iterator<Map.Entry<String,Set<AttributeConstraint>>> entry_itr;
    private Map.Entry<String,Set<AttributeConstraint>> entry;
    private Iterator<AttributeConstraint> c_itr;

    public ConstraintIterator(Iterator<Map.Entry<String,Set<AttributeConstraint>>> x) {
	entry_itr = x;
	entry = null;
	c_itr = null;
    }
    public boolean hasNext() {
	return (c_itr != null && c_itr.hasNext()) || entry_itr.hasNext();
    }
    public siena.fwd.Constraint next() {
	if (c_itr == null || !c_itr.hasNext()) {
	    entry = entry_itr.next();
	    c_itr = entry.getValue().iterator();
	}
	return new Constraint(entry.getKey(), c_itr.next());
    }
    public void remove() {
	if (c_itr == null) {
	    entry = entry_itr.next();
	    c_itr = entry_itr.next().getValue().iterator();
	}
	c_itr.remove();
	if (entry.getValue().isEmpty()) {
	    entry_itr.remove();
	    c_itr = null;
	}
    }
}

/** a selection predicate defined over notifications.
 *
 *  The form of a <code>Filter</code> is a conjunction of
 *  <em>constraints</em> (see {@link AttributeConstraint}).  Each
 *  constraint poses an elementary condition on a specific attribute
 *  of the event.  For example a contraint<em>[price &lt; 10]</em>
 *  requires that the event contain an attribute named "price" whose
 *  value is a number less than 10.  A <code>Filter</code> can have
 *  more that one constraint for an attribute.  For example, a
 *  <code>Filter</code> can express things like <em>[model="custom",
 *  price &gt; 10, price &lt;= 20]</em>.<p>
 * 
 *  Every <em>constraint</em> in a <code>Filter</code> implicitly
 *  expresses an existential quantifier over the event.  The filter
 *  of the previous example (<em>[model="custom", price &gt; 10,
 *  price &lt;= 20]</em>) requires that an event contain an attribute
 *  named "model", whose value is the string "custom", and an
 *  attribute named "price" whose value is a number between 10 and 20
 *  (20 included).<p>
 * 
 *  The valid syntax for attribute names is the same for {@link
 *  Notification}.<p>
 * 
 *  @see AttributeConstraint
 *  @see Notification
 **/
public class Filter implements java.io.Serializable,
			       siena.fwd.Filter {
    static final long serialVersionUID = 1L;
    //
    // what I really need here is a multimap, but there is no such
    // thing in java.util, so I'm going to implement it as a map of
    // lists
    //
    Map<String, Set<AttributeConstraint>>  constraints;
    
    public BSetBV bloomFilter;

    /** creates an empty filter.  
     *
     *	creates a filter with no constraints. Notice that an empty
     *	filter does not match any notification.  
     **/
    public Filter() { 
	constraints = new HashMap<String, Set<AttributeConstraint>>();
    }

    /** creates a (deep) copy of a given filter.  
     **/
    public Filter(Filter f) {
	//
	// again, since Java doesn't have decent collections
	// (multimaps), I have to do this really horrible copy.
	//
	constraints = new HashMap<String,Set<AttributeConstraint> >();
	for(Map.Entry<String,Set<AttributeConstraint>> e: 
		f.constraints.entrySet()) {
	    Set<AttributeConstraint> alist = new HashSet<AttributeConstraint>();
	    constraints.put(e.getKey(),alist); 
	    for(AttributeConstraint c: e.getValue())
		alist.add(new AttributeConstraint(c));
	}
    }

    public Filter(String s) throws SENPInvalidFormat {
	constraints = new HashMap<String,Set<AttributeConstraint>>();
	SENPBuffer b = new SENPBuffer(s.getBytes());
	b.decodeFilter(this);
    }

    public static Filter parseFilter(String s) throws SENPInvalidFormat {
	return (new SENPBuffer(s.getBytes())).decodeFilter();
    }

    private void writeObject(java.io.ObjectOutputStream out) 
	throws java.io.IOException {
	SENPBuffer b = new SENPBuffer();
	b.encodeSimple(this);
	out.writeInt(b.length());
	out.write(b.buf, 0, b.length());
    }

    private void readObject(java.io.ObjectInputStream in)
	throws java.io.IOException, java.lang.ClassNotFoundException {
	int len = in.readInt();
	SENPBuffer b = new SENPBuffer();
	in.readFully(b.buf, 0, len);
	b.init(len);

	Filter f;
	try {
	    f = b.decodeFilter();
	} catch (SENPInvalidFormat ex) {
	    throw new java.io.InvalidObjectException(ex.toString());
	}
	constraints = new HashMap<String,Set<AttributeConstraint>>();
	for(Map.Entry<String,Set<AttributeConstraint>> e:
		f.constraints.entrySet()) {
	    Set<AttributeConstraint> alist = new HashSet<AttributeConstraint>();
	    constraints.put(e.getKey(),alist); 
	    for(AttributeConstraint c: e.getValue()) {
		alist.add(new AttributeConstraint(c));
	    }
	}
    }

    /** <code>true</code> <em>iff</em> this filter contains no constraints
     **/
    public boolean isEmpty() {
	return constraints.isEmpty();
    }

    /** puts a constraint <em>a</em> on attribute <em>name</em>. 
     *
     *	Example:
     *	<pre><code>
     *	    Filter f = new Filter();
     *	    AttributeConstraint a;
     *	    a = new AttrbuteConstraint(Op.SS, "soft") 
     *	    f.addConstraint("subject", a);
     *	</pre></code>
     **/
    public void addConstraint(String name, AttributeConstraint a) {
	Set<AttributeConstraint> s = constraints.get(name);
	if (s == null) {
	    s = new HashSet<AttributeConstraint>();
	    constraints.put(name,s);
	}
	s.add(a);
    }

    /** puts a constraint on attribute <em>name</em> using
     *	comparison operator <em>op</em> and a <code>String</code>
     *	argument <em>sval</em>.
     *
     *	<pre><code>
     *	    Filter f = new Filter();
     *	    f.addConstraint("subject", Op.SS, "soft");
     *	</pre></code> 
     **/
    public void addConstraint(String s, short op, String sval) {
	addConstraint(s,new AttributeConstraint(op, sval));
    }

    /** puts a constraint on attribute <em>name</em> using
     *  comparison operator <em>op</em> and a <code>byte[]</code>
     *  argument <em>sval</em>.
     **/
    public void addConstraint(String s, short op, byte[] sval) {
	addConstraint(s,new AttributeConstraint(op, sval));
    }

    /** puts a constraint on attribute <em>name</em> using comparison
     *  operator <em>op</em> and a <code>long</code> argument
     *  <em>lval</em>.
     **/
    public void addConstraint(String s, short op, long lval) {
	addConstraint(s,new AttributeConstraint(op, lval));
    }

    /** puts a constraint on attribute <em>name</em> using comparison
     *	operator <em>op</em> and a <code>boolean</code> argument
     *	<em>bval</em>.
     *
     *  <pre><code>
     *      Filter f = new Filter();
     *	    f.addConstraint("failed", Op.EQ, true);
     *  </pre></code> 
     **/
    public void addConstraint(String s, short op, boolean bval) {
	addConstraint(s,new AttributeConstraint(op, bval));
    }

    /** puts a constraint on attribute <em>name</em> using comparison
     *  operator <em>op</em> and a <code>double</code> argument
     * <em>dval</em>.
     **/
    public void addConstraint(String s, short op, double dval) {
	addConstraint(s,new AttributeConstraint(op, dval));
    }

    /** puts a constraint on attribute <em>name</em> using the
     *  equality operator and a <code>String</code> argument
     *  <em>sval</em>.
     *  
     *  Example:
     *  <pre><code>
     *      Filter f = new Filter();
     *      f.addConstraint("name", "Antonio");
     *  </pre></code> 
     **/
    public void addConstraint(String s, String sval) {
	addConstraint(s,new AttributeConstraint(Op.EQ, sval));
    }

    /** puts a constraint on attribute <em>name</em> using the
     *  equality operator and a <code>byte[]</code> argument
     *  <em>sval</em>.
     **/
    public void addConstraint(String s, byte[] sval) {
	addConstraint(s,new AttributeConstraint(Op.EQ, sval));
    }

    /** puts a constraint on attribute <em>name</em> using the
     *  equality operator and a <code>boolean</code> argument
     *  <em>bval</em>.
     **/
    public void addConstraint(String s, boolean bval) {
	addConstraint(s,new AttributeConstraint(Op.EQ, bval));
    }

    /** puts a constraint on attribute <em>name</em> using the
     *  equality operator and a <code>long</code> argument
     *  <em>lval</em>.
     **/
    public void addConstraint(String s, long lval) {
	addConstraint(s,new AttributeConstraint(Op.EQ, lval));
    }

    /** puts a constraint on attribute <em>name</em> using the
     *  equality operator and a <code>double</code> argument
     *  <em>dval</em>.
     **/
    public void addConstraint(String s, double dval) {
	addConstraint(s,new AttributeConstraint(Op.EQ, dval));
    }

    /** returns true if this filter contains at least one constraint
     *  for the specified attribute
     **/
    public boolean containsConstraint(String s) {
	return constraints.containsKey(s);
    }

    /** removes all the constraints for the specified attribute.
     *  
     *  @return true if any constraints existed and has been removed
     **/
    public boolean removeConstraints(String s) {
	return constraints.remove(s) != null;
    }

    /** removes all constraints.
     **/
    public void clear() {
	constraints.clear();
    }

    /** returns an iterator for the set of attribute (constraint)
     *  names of this <code>Filter</code>.
     **/
    public Iterator<String> constraintNamesIterator() {
	return constraints.keySet().iterator();
    }

    /** returns an iterator for the set of constraints over attribute
     *  <em>name</em> of this <code>Filter</code>.
     **/
    public Iterator<AttributeConstraint> constraintsIterator(String name) {
	Set<AttributeConstraint> s = constraints.get(name);
	if (s == null) return null;
	return s.iterator();
    }

    public String toString() {
	SENPBuffer b = new SENPBuffer();
	b.encodeSimple(this);
	return new String(b.buf, 0, b.length());
    }

    public Iterator<siena.fwd.Constraint> iterator() {
 	return new ConstraintIterator(constraints.entrySet().iterator());
    }

	public void setBloomFilter(BSetBV bf) {
		this.bloomFilter = bf;		
	}
}
