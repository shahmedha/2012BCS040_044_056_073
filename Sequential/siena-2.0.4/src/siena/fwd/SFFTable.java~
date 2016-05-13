//
//  This file is part of Siena, a wide-area event notification system.
//  See http://www.inf.usi.ch/carzaniga/siena/
//
//  Author: Giovanni Toffetti Carughi
//          Antonio Carzaniga (firstname.lastname@usi.ch)
//          
//  See the file AUTHORS for full details. 
//
//  Copyright (C) 2008 Antonio Carzaniga
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
package siena.fwd;

import java.util.Collection;
import java.util.Map;
import java.util.AbstractMap;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

// This class implements a family of hash functions for strings.  That
// is, a parametric hash function defined as follows:
//
//       INPUT: string = c[1]c[2]c[3] ... c[N]
//       OUTPUT: c[1]*X^N + c[2]*X^(N-1) + ... + c[N-1]*X + c[N] mod P
// 
// where c[i] are the bytes in the input string, and the result is a
// polynomial of X, modulo a prime P.  The base of the polynomial X is
// our parameter.
//
class Hash {
    // my choice for the prime modulus P and the bases X are based on
    // the following considerations:
    //
    // 0. P must be prime.
    // 
    // 1. P should be a large value so that H will have a large
    //    domain, which in turn means that, when used to produce hash
    //    values in a restricted domain using something like H() mod
    //    Max, then the distribution of H over the 0--Max doman will
    //    be as uniform as possible.
    // 
    // 2. P must be less than (Integer.MAX_VALUE - 255)/X.  This is
    //    because, in computing h, we don't want X*h + c[i] to roll
    //    over Integer.MAX_VALUE and into negative values when we
    //    compute h = (X*h + c[i]) % P.  Therefore, we limit X to a
    //    maximum value of 10.
    // 
    private static final int P = 214748329;
        
    /** computes a hash function based on a polynomial base x.  x must
     *  be between 2 and 10.
     */
    public static int hash(int X, byte[] c) {           
	int h = 0;
	for (int i = 0; i < c.length; i++) {
	    h = (X*h + c[i]) % P;
	}
	return h;
    }
}

// This class is not used in the algorithm, but I left it in the code
// on purpose.  This is what I have used to test the best Bloom-filter
// parameters, which then lead to the specialized implementation of
// BloomFilter128.
// 
class BloomFilterV {
    private static final int M = 128;
    private long[] bv = new long[M / Long.SIZE];


    private boolean contains(int bit_pos) {
	bit_pos %= M;
	return (bv[bit_pos / Long.SIZE] & (1L << (bit_pos % Long.SIZE))) != 0;
    }

    private void add(int bit_pos) {
	bit_pos %= M;
	bv[bit_pos / Long.SIZE] |= (1L << (bit_pos % Long.SIZE));
    }

    public void add(String name) {
	byte [] bytes = name.getBytes();
	add(Hash.hash(7, bytes));
	add(Hash.hash(3, bytes));
    }

    public boolean contains(String name) {
	byte [] bytes = name.getBytes();
	return contains(Hash.hash(7, bytes)) 
	    && contains(Hash.hash(3, bytes));
    }

    public boolean covers(BloomFilterV y) {
	for(int i = 0; i < bv.length; ++i)
	    if ((this.bv[i] & y.bv[i]) != y.bv[i])
		return false;
	return true;
    }
}

// BloomFilter128 is my implementation of the Bloom filters used in
// the name-set-based negative check used in the SFF algorithm.  This
// implementation is crucial for performance, so it is intended to be
// very compact and very efficient.  Furthermore, the implementation
// is tuned to a Bloom filter that, experimentally, is the best in a
// common scenario.
// 
class BloomFilter128 {
    private static final int M = 128;
    private long bv0 = 0;
    private long bv1 = 0;

    private boolean contains(int bit_pos) {
	bit_pos %= M;
	if (bit_pos / Long.SIZE == 0) {
	    return (bv0 & (1L << bit_pos)) != 0;
	} else {
	    return (bv1 & (1L << (bit_pos % Long.SIZE))) != 0;
	}
    }

    private void add(int bit_pos) {
	bit_pos %= M;
	if (bit_pos / Long.SIZE == 0) {
	    bv0 |= (1L << bit_pos);
	} else {
	    bv1 |= (1L << (bit_pos % Long.SIZE));
	}
    }

    public void add(String name) {
	byte [] bytes = name.getBytes();
	add(Hash.hash(7, bytes));
	add(Hash.hash(3, bytes));
    }

    public boolean contains(String name) {
	byte [] bytes = name.getBytes();
	return contains(Hash.hash(7, bytes)) && contains(Hash.hash(3, bytes));
    }

    public boolean covers(BloomFilter128 y) {
	return (this.bv0 & y.bv0) == y.bv0 && (this.bv1 & y.bv1) == y.bv1;
    }
}

// a conjunction of constraints in the data structures of
// the SFFTable implementation.
// 
class SFFFilter {
    // descriptor of the interface associated with this filter 
    // 
    // The current implementation is not capable of recognizing two
    // identical filters.  Therefore, every filter will generate a
    // filter descriptor like this, and therefore a filter descriptor
    // is associated with a single interface.  This is why this is
    // simply a pointer to an interface descriptor.
    // 
    // <p>At some point, we might want to be able to figure out that
    // F1==F2, and therefore to allow each filter to refer to more
    // than one interfaces.  In that case, we will have to maintain a
    // set of pointers to interface descriptors.
    // 
    SFFInterface ifx;

    // number of constraints composing this filter.  Notice that the
    // implicit but very reasonable restriction of this declaration is
    // that we do not allow more than 127 constraints per filter.
    //   
    byte size = 0;
   
    // Bloom signature for this filter: set of attrubute names
    // referred in this filter.
    // 
    BloomFilter128 bset = new BloomFilter128();

    SFFFilter(SFFInterface i) {
	ifx = i;
    }
}

// descriptor of a (single) constraint in the SFFTable. 
// 
// It is essentially a set of pointers to the filters in which this
// constraint appears.
// 
class SFFConstraint {
    // I'll give the list of filters an initial capacity of 4.  This
    // is completely out of my A-word, but... well, it seems about
    // right to me.
    List<SFFFilter> filters = new ArrayList<SFFFilter>(4);
}

// ConstraintIndex is the primary, generic constraint index for the
// SFFTable.  The index applies to equality and inequality constraints
// over a totally ordered domain (i.e., a Comparable in Java).
// 
// NOTE on the class declaration:
//
// Okay, it took me a bit to figure out this whole Comparator
// business, so here's some documentation.
// 
// First of all, this index stores basic equality and inequality
// constraints for any totally-ordered type T.  For example, if T is
// integer, then it stores things like x<10, x!=20, x>-2, or x=23.
// The idea is trivial for equality constraints, so I won't even talk
// about it.  The idea for inequalities (less-than and greater-than)
// is also simple, but it requires two sorting orders, which is what
// brings us to this Comparator thing.
// 
// The main idea is to store the values of all the inequality
// constraints in a sorted map.  The map is sorted according to the
// comparison value.  So, for example, x>10, x>20, and x>30, will be
// stored in the gt_map container precisely in this order.  Then, when
// it comes time to matching a value x=K, we use the nice headMap(K)
// method of sorted sets, which gives us "a view of the portion of
// this map whose keys are strictly less than K".  So, say K is 25,
// which means we are matching an attribute x=25, then we get x>10 and
// x>20, which are in fact the constraints matched by x=25.
// 
// So, greater-than constraints work well with the natural ordering
// and with the headMap method.  The problem is less-tna constraints.
// It is tempting to use the same natural ordering together with the
// tailMap(K) method, but unfortunately this method returns the tail
// map *including* x<K, which should not match x=K.
//
// So, we create a sorted map with a reverse order, which means that
// we must pass a Comparator<T> to the constructor of
// TreeMap<T,SFFConstraint>.  One way to do this would be to use an
// inner class as follows:
//
// class ConstraintIndex<T extends Comparable<? super T>> {
//    ...
//    class ReverseOrder implements Comparator<T> {
//	public int compare(T x, T y) {
//	    return y.compareTo(x);
//	}
//    }
//    ...
//    public ConstraintIndex() {
//	...
//	lt_map = new TreeMap<T,SFFConstraint>(new ReverseOrder());
//      ...
//    }
//
// However, this is not too good, as it forces us to create a
// ReverseOrder object (at lest its v-table) for each index, even if
// in reality we only need one for each template parameter T.
// (Needless to say, C++ may be a bit more complex at first, but it is
// way better in this respect.)  So, my trick here is to use the index
// itself as a T-specific comparator, since we already have the object
// and its v-table.  This is why ConstraintIndex implements
// Comparator<T>.
// 
class ConstraintIndex<T extends Comparable<? super T>> implements Comparator<T> {
    private Map<T,SFFConstraint> eq_map;
    private Map<T,SFFConstraint> ne_map;
    private SortedMap<T,SFFConstraint> lt_map;
    private SortedMap<T,SFFConstraint> gt_map;
    private SFFConstraint any_c;

    public ConstraintIndex() {
	eq_map = new TreeMap<T,SFFConstraint>();
	ne_map = new TreeMap<T,SFFConstraint>();
	lt_map = new TreeMap<T,SFFConstraint>(this);
	gt_map = new TreeMap<T,SFFConstraint>();
	any_c = null;
    }

    public int compare(T x, T y) {
	return y.compareTo(x);
    }

    public SFFConstraint add_eq(T v) {
	SFFConstraint res = eq_map.get(v);
	if (res == null) {
	    res = new SFFConstraint();
	    eq_map.put(v, res);
	}
	return res;
    }

    public SFFConstraint add_ne(T v) {
	SFFConstraint res = ne_map.get(v);
	if (res == null) {
	    res = new SFFConstraint();
	    ne_map.put(v, res);
	}
	return res;
    }

    public SFFConstraint add_lt(T v) {
	SFFConstraint res = lt_map.get(v);
	if (res == null) {
	    res = new SFFConstraint();
	    lt_map.put(v, res);
	}
	return res;
    }

    public SFFConstraint add_gt(T v) {
	SFFConstraint res = gt_map.get(v);
	if (res == null) {
	    res = new SFFConstraint();
	    gt_map.put(v, res);
	}
	return res;
    }

    public SFFConstraint add_any() {
	if (any_c == null) {
	    any_c = new SFFConstraint();
	}
	return any_c;
    }

    public boolean match(T s, ConstraintProcessor p) {
	if (any_c != null)
	    if (p.process_constraint(any_c))
		return true;

	SFFConstraint c = eq_map.get(s);
	if (c != null) {
	    if (p.process_constraint(c))
		return true;
	} 

	boolean done_comparing_keys = false;
	for(Map.Entry<T,SFFConstraint> entry: ne_map.entrySet()) {
	    if (done_comparing_keys || !entry.getKey().equals(s)) {
		if(p.process_constraint(entry.getValue())) 
		    return true;
	    } else {
		done_comparing_keys = true;
	    }
	}

	for(SFFConstraint k: gt_map.headMap(s).values()) {
	    if(p.process_constraint(k))
		return true;
	}

	for(SFFConstraint k: lt_map.headMap(s).values()) {
	    if(p.process_constraint(k))
		return true;
	}
	return false;
    }
}

// index of boolean constraints.
//
class BooleanConstraintIndex {
    SFFConstraint true_constraint;
    SFFConstraint false_constraint;
    SFFConstraint any_constraint;

    public SFFConstraint add_eq(boolean v) {
	if (v) {
	    if (true_constraint == null) {
		true_constraint = new SFFConstraint();
	    }
	    return true_constraint;
	} else {
	    if (false_constraint == null) {
		false_constraint = new SFFConstraint();
	    }
	    return false_constraint;
	} 
    }

    public SFFConstraint add_ne(boolean v) {
	return add_eq(!v);
    }

    public SFFConstraint add_any() {
	if (any_constraint == null) {
	    any_constraint = new SFFConstraint();
	}
	return any_constraint;
    }

    public boolean match(boolean v, ConstraintProcessor p) {
	if (any_constraint != null)
	    if (p.process_constraint(any_constraint))
		return true;

	SFFConstraint c = v ? true_constraint : false_constraint;
	if (c != null) {
	    if (p.process_constraint(c))
		return true;
	} 
	return false;
    }
}

// index of string constraints.
//
// This data structure supports <em>equals</em>, <em>less-than</em>,
// <em>greater-than</em>, <em>prefix</em>, <em>suffix</em>, and
// <em>substring</em> constraints on strings.  The current
// implementation is trivially simple and therefore not too smart
// about substring, prefix, and suffix constraints.
// ...work in progress...
// 
class StringIndex extends ConstraintIndex<String> {
    private Map<String,SFFConstraint> pf_map;
    private Map<String,SFFConstraint> sf_map;
    private Map<String,SFFConstraint> ss_map;

    public StringIndex() {
	super();
	pf_map = new TreeMap<String,SFFConstraint>();
	sf_map = new TreeMap<String,SFFConstraint>();
	ss_map = new TreeMap<String,SFFConstraint>();
    }

    public SFFConstraint add_pf(String s) {
	SFFConstraint res = pf_map.get(s);
	if (res == null) {
	    res = new SFFConstraint();
	    pf_map.put(s, res);
	}
	return res;
    }

    public SFFConstraint add_sf(String s) {
	SFFConstraint res = sf_map.get(s);
	if (res == null) {
	    res = new SFFConstraint();
	    sf_map.put(s, res);
	}
	return res;
    }

    public SFFConstraint add_ss(String s) {
	SFFConstraint res = ss_map.get(s);
	if (res == null) {
	    res = new SFFConstraint();
	    ss_map.put(s, res);
	}
	return res;
    }

    public boolean match(String s, ConstraintProcessor p) {
	if (super.match(s,p))
	    return true;

	for(Map.Entry<String,SFFConstraint> entry: ss_map.entrySet()) {
	    if (s.indexOf(entry.getKey()) >= 0) {
		if(p.process_constraint(entry.getValue())) 
		    return true;
	    } 
	}
	for(Map.Entry<String,SFFConstraint> entry: pf_map.entrySet()) {
	    if (s.startsWith(entry.getKey())) {
		if(p.process_constraint(entry.getValue())) 
		    return true;
	    }
	}
	for(Map.Entry<String,SFFConstraint> entry: sf_map.entrySet()) {
	    if (s.endsWith(entry.getKey())) {
		if(p.process_constraint(entry.getValue())) 
		    return true;
	    } 
	}
	return false;
    }
}

// a per-name constraint index in SFFTable.  An SFFAttribute holds all
// the type-specific indexes containing all the constraints that refer
// to the same attribute name.
// 
class SFFAttribute {
	
    /* index of <em>anytype</em> constraints */
    SFFConstraint any_value_any_type;
    /* index of <em>integer</em> constraints */
    ConstraintIndex<Integer> int_index;
    /* index of <em>double</em> constraints */
    ConstraintIndex<Double> double_index;
    /* index of <em>string</em> constraints */
    // StringIndex str_index;
    // TODO: implement correct index
    StringIndex str_index;
    /* index of <em>boolean</em> constraints */
    // BoolIndex bool_index;
    // TODO: implement correct index
    BooleanConstraintIndex bool_index;

    //
    // more constraints can be added here (for example, for Time)
    // ...to be continued...
    //

    /*
     * adds the given constraints to the appropriate constraint index associated
     * with this attribute name.
     */
    SFFConstraint add_constraint(Constraint c)
	throws BadConstraintException {
	switch (c.getValue().getType()) {
	case Value.NULL:
	    switch (c.getOperator()) {
	    case Constraint.ANY:
		if (any_value_any_type == null)
		    any_value_any_type = new SFFConstraint();
		return any_value_any_type;
	    default:
		throw (new BadConstraintException("bad operator for anytype."));
	    }
	case Value.INT:
	    switch (c.getOperator()) {
	    case Constraint.ANY:
		return int_index.add_any();
	    case Constraint.EQ:
		return int_index.add_eq(c.getValue().intValue());
	    case Constraint.GT:
		return int_index.add_gt(c.getValue().intValue());
	    case Constraint.GE:
		return int_index.add_gt(c.getValue().intValue() - 1);
	    case Constraint.LT:
		return int_index.add_lt(c.getValue().intValue());
	    case Constraint.LE:
		return int_index.add_lt(c.getValue().intValue() + 1);
	    case Constraint.NE:
		return int_index.add_ne(c.getValue().intValue());
	    default:
		throw (new BadConstraintException("bad operator for int."));
	    }			
	case Value.STRING:
	    switch (c.getOperator()) {
	    case Constraint.ANY:
		return str_index.add_any();
	    case Constraint.EQ:
		return str_index.add_eq(c.getValue().stringValue());
	    case Constraint.GT:
		return str_index.add_gt(c.getValue().stringValue());
	    case Constraint.LT:
		return str_index.add_lt(c.getValue().stringValue());
	    case Constraint.PF:
		return str_index.add_pf(c.getValue().stringValue());
	    case Constraint.SF:
		return str_index.add_sf(c.getValue().stringValue());
	    case Constraint.SS:
		return str_index.add_ss(c.getValue().stringValue());
		//			case Constraint.RE:
		//				return str_index.add_re(c.getValue().stringValue());
	    case Constraint.NE:
		return str_index.add_ne(c.getValue().stringValue());
	    default:
		throw (new BadConstraintException("bad operator for string."));
	    }			
	case Value.BOOL:
	    switch (c.getOperator()) {
	    case Constraint.ANY:
		return bool_index.add_any();
	    case Constraint.EQ:
		return bool_index.add_eq(c.getValue().booleanValue());
	    case Constraint.NE:
		return bool_index.add_ne(c.getValue().booleanValue());
	    default:
		throw (new BadConstraintException("bad operator for boolean."));
	    }			
	case Value.DOUBLE:
	    switch (c.getOperator()) {
	    case Constraint.ANY:
		return double_index.add_any();
	    case Constraint.EQ:
		return double_index.add_eq(c.getValue().doubleValue());
	    case Constraint.GT:
		return double_index.add_gt(c.getValue().doubleValue());
	    case Constraint.LT:
		return double_index.add_lt(c.getValue().doubleValue());
	    case Constraint.NE:
		return double_index.add_ne(c.getValue().doubleValue());
	    default:
		throw (new BadConstraintException("bad operator for double."));
	    }			
	default:
	    throw (new BadConstraintException("bad type."));
	}
    }

    /* constructor */
    SFFAttribute() {
	int_index = new ConstraintIndex<Integer>();
	/* index of <em>double</em> constraints */
	double_index = new ConstraintIndex<Double>();
	/* index of <em>string</em> constraints */
	// StringIndex str_index;
	// TODO: implement correct index
	str_index = new StringIndex();
	/* index of <em>boolean</em> constraints */
	// BoolIndex bool_index;
	// TODO: implement correct index
	bool_index = new BooleanConstraintIndex();
    }
}


// Stores the association between user-supplied interface identifier
// and interface identifier used internally by the forwarding table.
// 
// The reason for maintaining internal interface identifiers (as
// opposed to simple pointers to the user-supplied interface object)
// is that we want to use progressive numeric identifiers so as to be
// able to use an array-based interface mask.  This design decision is
// questionable, so we might abandon this SFFInterface link in the
// future.
// 
// 
class SFFInterface {
    Object ifx_object;		// user-defined interface object 

    int id;			// internal identifier used by the
				// matching function

    SFFInterface(Object xif, int xid) {
    	this.ifx_object = xif;
    	this.id = xid;
    }
}

// generic interface mask.  It amounts to a Set of interface
// identifiers.  In the future we might change this into a set of
// interface objects.  I.e.,
// interface IFMask {
//     public boolean contains(Object ifx);
//     public void add(Object ifx);
//     public int size();
// }
// 
interface IFMask {
    public boolean contains(int ifx);
    public void add(int ifx);
    public int size();
}

// this implementation of the interface mask is based on a bitvector.
// I haven't run specific performance tests, but I'd guess it's very
// efficient when there aren't too may interfaces (say < 1000).
// Another implementation could be based directly on a HashMap or
// similar structure.
//
class BitVectorMask implements IFMask {
    byte bv[];
    private int weight;

    public BitVectorMask(int bit_size) {
	int byte_size = bit_size / Byte.SIZE;
	if (bit_size % Byte.SIZE > 0) 
	    ++byte_size;
	bv = new byte[byte_size];
	weight = 0;
    }

    public boolean contains(int bit_pos) {
	return (bv[bit_pos / Byte.SIZE] & (1 << (bit_pos % Byte.SIZE))) != 0;
    }

    public void add(int bit_pos) {
	byte mask = 1;
	mask <<= (bit_pos % Byte.SIZE);
	if ((bv[bit_pos / Byte.SIZE] & mask) == 0) {
	    bv[bit_pos / Byte.SIZE] |= mask;
	    ++weight;
	}
    }

    public int size() {
	return weight;
    }

    public void clear() {
	for(int i = 0; i < bv.length; ++i)
	    bv[i] = 0;
	weight = 0;
    }
}

class CountersMap {
    static final int INITAL_TABLE_SIZE = 1 << 10;

    private int [] counters;
    private SFFFilter [] keys;
    private int table_size;
    private int size_mask;
    private int load;

    public CountersMap() {
	create_table(INITAL_TABLE_SIZE);
    }

    public int plus_one(SFFFilter k) {
	int h = System.identityHashCode(k) & size_mask;
	int step = (~h | 1) & size_mask;
	do {
	    if (keys[h] == null) {// we look for an empty slot in the table
		keys[h] = k;
		counters[h] = 1;
		++load;
		if (load * 2 > table_size)
		    rehash();
		return 1;
	    }			// or we return the counter if we find it
	    if (keys[h] == k)
		return ++counters[h];
				// or we iterate if there is a collision
	    h = (h + step) & size_mask;
	} while (true);
    }

    private void create_table(int size) {
	table_size = size;
	keys = new SFFFilter[size];
	counters = new int[size];
	size_mask = size - 1;
    }

    private void rehash() {
	int [] old_counters = counters;
	SFFFilter [] old_keys = keys;
	int old_table_size = table_size;

	create_table(table_size << 1); // we double the table size

	// TODO: we should check that the size is not already too big...

	for(int i = 0; i < old_table_size; ++i) {
	    SFFFilter k = old_keys[i];
	    if (k != null) {
		// See the documentation of plus_one().
		int h = System.identityHashCode(k) & size_mask;
		int step = (~h | 1) & size_mask;

		while (keys[h] != null && keys[h] != k) {
		    h = (h + step) & size_mask;
		}
		keys[h] = k;
		counters[h] = old_counters[i];
	    }
	}
    }
}

// implements the filter- and interface- matching phase of the SFF
// matching algorithm.  This matcher object is called by the
// type-specific indexes with a matched constraint.  This processor
// then maintains a set of counters, and also deals with the interface
// mask.
class ConstraintProcessor {
    // table of counters for partially-matched filters.
    //
    // Each filter considered in the matching process has an
    // associated counter that records the number of constraints
    // matched so far for that filter.
    //
    //   Map<SFFFilter,Integer> fmap = new HashMap<SFFFilter, Integer>();
    CountersMap fmap = new CountersMap();


    // set of interfaces that can be ignored in the matching process.
    //
    // An interface can be ignored by the matching process if it has already
    // been matched and processed or if it was excluded by the pre-processing
    // function.
    //
    IFMask if_mask;

    // output processor
    //
    MatchHandler processor;

    // total number of interfaces we want to match.
    // 
    // we maintain this value so that we can stop the matching process
    // immediately once we have matched all interfaces.
    //
    int target;

    // Bloom filter representing the set of attribute names in the
    // message being evaluated.
    // 
    // see process_constraint() below for some documentation on the
    // use of this variable.
    //
    BloomFilter128 bset = new BloomFilter128();

    public ConstraintProcessor(int if_count, 
			       MatchHandler p,
			       IFMask mask, 
			       Message m) {
	target = if_count;
	processor = p;
	if_mask = mask;
		
	for (Attribute a: m) 
	    bset.add(a.getName());
    }

    boolean process_constraint(SFFConstraint c) {
	//
	// we look at every filter in which the constraint appears
	//		
	for (SFFFilter f : c.filters) {
	    if (bset.covers(f.bset)) {
		// bset represents the set of attributes names in the
		// message. f.bset is the set of constraint names in
		// the filter.  This Bloom-filter coverage test
		// provides a negative check against filter f. In
		// essence, the message must contain all the attribute
		// names referenced in f, otherwise it doesn't even
		// make sense for us to count the matches for that
		// filter, since it will never match.
		if (!(if_mask.contains(f.ifx.id))) {
		    // this second check avoids further processing if
		    // the filter is in a predicate associated with an
		    // interface that was already matched or that was
		    // excluded by the interface mask.
		    if (fmap.plus_one(f) >= f.size) {
			if_mask.add(f.ifx.id);
			if (processor.output(f.ifx.ifx_object))
			    return true;
			if (if_mask.size() >= target)
			    return true;
			//
			// we immediately return true if the processing
			// function returns true or if we matched all
			// possible interfaces
			//
		    }
		}
	    }
	}
	return false;
    }
}

class MMHWrapper implements MatchHandler {
    Message m;
    MatchMessageHandler h;

    public MMHWrapper(MatchMessageHandler mmh, Message mx) {
	h = mmh;
	m = mx;
    }

    public boolean output(Object ifx) {
	return h.output(ifx, m);
    }
}

/** implementation of the Siena Fast Forwarding (SFF) algorithm.
 *
 *  This implementation of the forwarding table and its matching
 *  algorithm is based on the data structure and algorithm described
 *  in A.&nbsp;Carzaniga and A.L.&nbsp;Wolf, "Forwarding in a
 *  Content-Based Network". Proceedings of ACM SIGCOMM 2003.  A more
 *  complete and advanced implementation is available in C++ <a
 *  href="http://www.inf.usi.ch/carzaniga/siena/forwarding/">here</a>.
 */
public class SFFTable implements ForwardingTable {
    private Map<String, SFFAttribute> attributes;

    // Total number of interfaces associated with a predicate in the
    // forwarding table.
    //
    private int if_count;

    public SFFTable() {
	attributes =  new HashMap<String, SFFAttribute>();
	if_count = 0;
    }

    public void consolidate() {
	// right now we don't implement any consolidation.
	// this is for future improvements to the constraint indexes.
    }

    public void clear() {
	attributes.clear();
	if_count = 0;
    }

    public void match(Message m, MatchMessageHandler p) {
	MMHWrapper w = new MMHWrapper(p, m);
	match(m, w);
    }

    public void match(Message m, MatchHandler p) {
	//
	// first we construct a constraint matcher object with a new
	// interface mask.  In this implementation, we could push the
	// creation of the mask into the constraint processor.
	// However, the idea is to be able to pass to this method a
	// set of interfaces that are excluded from forwarding by the
	// caller code (e.g., because they are not on the broadcast
	// tree of the originator of the message, as in CBCB.)
	//
	IFMask mask = new BitVectorMask(if_count);
	ConstraintProcessor cp = new ConstraintProcessor(if_count, p, mask, m);
	// 
	// pre-processing to be implemented at some point
	// ...work in progress...
	// 

	for (Attribute a: m) {
	    SFFAttribute adescr = attributes.get(a.getName());
	    if (adescr != null) {
		if (adescr.any_value_any_type != null) {
		    if (cp.process_constraint(adescr.any_value_any_type)) {
			return;
		    }
		}
		Value val = a.getValue();
		switch (val.getType()) {
		case Value.INT:
		    if (adescr.int_index.match(val.intValue(), cp))
			return;
		    if (adescr.double_index.match(val.doubleValue(), cp))
			return;
		    break;
		case Value.STRING:
		    if (adescr.str_index.match(val.stringValue(), cp))
			return;
		    break;
		case Value.BOOL:
		    if (adescr.bool_index.match(val.booleanValue(), cp))
			return;
		    break;
		case Value.DOUBLE:
		    if (adescr.double_index.match(val.doubleValue(), cp))
			return;
		    if (adescr.int_index.match((int) val.doubleValue(), cp))
			return;
		    break;
		default:
		    // Unknown type: this is a malformed attribute. In
		    // this case we simply move along with the matching
		    // function. Obviously, we could (1) throw an
		    // exception, (2) print an error message, or (3) ask
		    // the user (i.e., the programmer) to supply an error
		    // callback function and call that function. This is
		    // largely a semantic issue. Notice that returning
		    // immediately (from the matching function) would not
		    // be a good idea, since we might have already matched
		    // some predicates, and therefore we might end up
		    // excluding some other predicates based only on the
		    // ordering of attributes.
		    // 
		    break;
		}
		if (mask.size() >= if_count)
		    return;
	    }
	}
    }

    // This is the main method that builds the SFFTable.  It
    // associates an interface object with a predicate. 
    //
    // ifconfig is conceptually simple, but it requires a bit of
    // attention to manage the compilation of the selectivity
    // table.  At this point, this implementation does not even
    // use a selectivity table, so the algorithm becomes very
    // simple.
    //
    public void ifconfig(Object ifx, Predicate p) 
	throws BadConstraintException {
	SFFInterface i = new SFFInterface(ifx, if_count++);   
	for (Filter fx: p) {
	    SFFFilter f = new SFFFilter(i);
	    for (Constraint cx: fx) {
		String name = cx.getName(); 
		// first we grab an attribute descriptor corresponding
		// to this attribute name from the main index,
		// creating one if none is there already
		SFFAttribute a = attributes.get(name);
		if (a == null) {
		    a = new SFFAttribute();
		    attributes.put(name, a);
		}
		// then we get the constraint descriptor, creating one
		// if necessary.  All of this is done by the
		// SFFAttribute add_constraint() method.
	    	SFFConstraint c = a.add_constraint(cx);
		// finally, we add the constraint descriptor to the
		// filter descriptor.
		c.filters.add(f);
		f.bset.add(name);
		++f.size;
	    }
	}
    }

	@Override
	public String toString() {
		return attributes.toString();
	}
    
    
}
