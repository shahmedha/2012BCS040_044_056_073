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

/**  Value of an attribute in an event notification.  
 *
 *   An <code>AttributeValue</code> is a container for a typed vaule of
 *   an attribute in a notification.  An <code>AttributeValue</code> can
 *   be of type <code>String</code>, <code>byte[]</code>,
 *   <code>int</code>, <code>long</code>, <code>double</code>, and
 *   <code>boolean</code>.  <p>
 *
 **/
public interface Value {
    //
    // the following integer constants correspond to the type
    // identifiers defined in siena.AttributeValue.  As for
    // siena.fwd.Constraint, this is a temporary solution.  In the
    // not-so-distant future, we should either use
    // siena.AttributeValue, or perhaps define a separate, clearer
    // data interface.
    //

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

    //
    // other types here ...work in progress...
    //
    public int getType();

    public int intValue();
    public long longValue();
    public double doubleValue();
    public boolean booleanValue();
    public String stringValue();
    public byte[] byteArrayValue();
}
