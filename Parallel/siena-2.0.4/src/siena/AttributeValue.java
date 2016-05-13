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

/**
 *   Value of an attribute in an event notification.  
 *
 *   An <code>AttributeValue</code> is a container for a typed vaule of
 *   an attribute in a notification.  An <code>AttributeValue</code> can
 *   be of type <code>String</code>, <code>byte[]</code>,
 *   <code>int</code>, <code>long</code>, <code>double</code>, and
 *   <code>boolean</code>.  <p>
 *
 *   Example: 
 *
 *   <pre><code> 
 *       AttributeValue v = new
 *       AttributeValue("Antonio"); 
 *       System.out.println(v.stringValue());
 *       Notification e = new Notification(); 
 *       e.putAttribute("name", v);
 *   </pre></code>
 *
 *   @see Notification
 *   @see AttributeConstraint
 **/
public class AttributeValue implements java.io.Serializable,
				       siena.fwd.Value {
    static final long serialVersionUID = 1L;

    /** <em>null</em> type, the default type of a Siena attribute */
    public static final int	NULL		= 0;

    /** string of bytes */
    public static final int	BYTEARRAY	= 1;

    /** string of bytes
     *
     *  an alias to <code>BYTEARRAY</code> 
     *	provided only for backward compatibility 
     **/
    public static final int	STRING		= 1;

    /** integer type.  
     *  
     *  corresponds to the Java <code>long</code> type. 
     **/
    public static final int	LONG		= 2;

    /** integer type.  
     *
     *	corresponds to the Java <code>int</code> type. 
     **/
    public static final int	INT		= 2;

    /** double type.  
     *
     *	corresponds to the Java <code>double</code> type. 
     **/
    public static final int	DOUBLE		= 3;

    /** boolean type.  
     *
     *	corresponds to the Java <code>boolean</code> type. 
     **/
    public static final int	BOOL		= 4;

    private	int		type;

    private	byte[]		sval;
    private	long		ival;
    private	double		dval;
    private	boolean		bval;
    // other types here...

    public AttributeValue() {
	type = NULL;
    }

    public AttributeValue(AttributeValue x) {
	if (x == null) {
	    type = NULL;
	    return;
	}
	type = x.type;
	switch(type) {
	case INT: ival = x.ival; break;
	case BOOL: bval = x.bval; break;
	case DOUBLE: dval = x.dval; break;
	case BYTEARRAY: sval = x.sval.clone(); break;
	}
    }

    public AttributeValue(String s) {
	if (s == null) {
	    type = NULL;
	    return;
	}
	type = BYTEARRAY;
	sval = s.getBytes();
    }

    public AttributeValue(byte[] s) {
	type = BYTEARRAY;
	sval = s.clone();
    }

    public AttributeValue(long i) {
	type = LONG;
	ival = i;
	sval = null;
    }

    public AttributeValue(boolean b) {
	type = BOOL;
	bval = b;
	sval = null;
    }

    public AttributeValue(double d) {
	type = DOUBLE;
	dval = d;
	sval = null;
    }
    //
    // other types here ...work in progress...
    //

    public int getType() {
	return type;
    }

    public int intValue() {
	switch(type) {
	case LONG: return (int)ival;
	case BOOL: return bval ? 1 : 0;
	case DOUBLE: return (int)dval;
	    //
	    // perhaps I should use Integer.decode() instead of
	    // Integer.valueOf().  Anybody knows the difference?
	    //
	case BYTEARRAY: return Integer.valueOf(new String(sval)).intValue();
	default:
	    return 0; // should throw an exception here
	              // ...work in progress...
	}
    }

    public long longValue() {
	switch(type) {
	case LONG: return ival;
	case BOOL: return bval ? 1 : 0;
	case DOUBLE: return (int)dval;
	    //
	    // Same as above. What's the difference between
	    // Long.valueOf() and Long.decode()?
	    //
	case BYTEARRAY: return Long.valueOf(new String(sval)).longValue();
	default:
	    return 0; // should throw an exception here 
	              // ...work in progress...
	}
    }

    public double doubleValue() {
	switch(type) {
	case LONG: return ival;
	case BOOL: return bval ? 1 : 0;
	case DOUBLE: return dval;
	case BYTEARRAY: return Double.valueOf(new String(sval)).doubleValue();
	default:
	    return 0; // should throw an exception here 
	              // ...work in progress...
	}
    }

    public boolean booleanValue() {
	switch(type) {
	case LONG: return ival != 0;
	case BOOL: return bval;
	case DOUBLE: return dval != 0;
	case BYTEARRAY: return Boolean.valueOf(new String(sval)).booleanValue();
	default:
	    return false; // should throw an exception here 
	                  // ...work in progress...
	}
    }

    public String stringValue() {
	switch(type) {
	case LONG: return String.valueOf(ival);
	case BOOL: return String.valueOf(bval);
	case DOUBLE: return String.valueOf(dval);
	case BYTEARRAY: return new String(sval);
	default:
	    return ""; // should throw an exception here 
	               // ...work in progress...
	}
    }

    public byte[] byteArrayValue() {
	switch(type) {
	case LONG: return String.valueOf(ival).getBytes();
	case BOOL: return String.valueOf(bval).getBytes();
	case DOUBLE: return String.valueOf(dval).getBytes();
	case BYTEARRAY: return sval;
	default:
	    return null; // should throw an exception here 
	                 // ...work in progress...
	}
    }

    public boolean isEqualTo(AttributeValue x) {
        switch(type) {
        case BYTEARRAY: return java.util.Arrays.equals(sval, x.sval);
        case LONG: return ival == x.longValue();
        case DOUBLE: return dval == x.doubleValue();
        case BOOL: return bval == x.booleanValue();
        default: return false;
        }
    }

    public String toString() {
	SENPBuffer b = new SENPBuffer();
	b.encode(this);
	return new String(b.buf, 0, b.length());
    }

    public int hashCode() {
	return this.toString().hashCode();
    }
}



