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

/** an elementary constraint over an attribute in a message.
 *  
 *  A constraint imposes a condition over the value of an attribute,
 *  and implicitly on the existance of an attribute with the same
 *  name.  A constraint is defined by a triple <em>(n,op,v)</em>, with
 *  a name <em>n</em>, a comparison operator <em>op</em>, and a
 *  comparison value <em>v</em>.  Such a constraint is satisfied by a
 *  message <em>m</em> if and only if there exists an attribute
 *  <em>a</em> in <em>m</em> such that <em>a.getName()</em> equals
 *  <em>n</em>, and <em>a.getValue()</em> matches <em>v</em> according
 *  to operator <em>op</em>.
 *
 *  @see Filter
 *  @see Value
 **/
public interface Constraint extends Attribute {
    //
    // an operator identifier takes one of the following values.  The
    // following code is copied verbatim from siena.Op.  This is a
    // temporary solution.  In the not-so-distant future, I plan to
    // either use siena.Op, or perhaps to define a clearer data
    // interface.
    //
    /** equality operator */
    public static final short	EQ		= 1;
    /** less than operator */
    public static final short	LT		= 2;
    /** greater than operator */
    public static final short	GT		= 3;
    /** greater o equal operator */
    public static final short	GE		= 4;
    /** less or equal operator */
    public static final short	LE		= 5;
    /** has prefix operator (for strings only, e.g., "software" PF "soft") 

	<em>x Op.PF y</em> iff <em>x</em> begins with the prefix <em>y</em> 
    */
    public static final short	PF		= 6;
    /** has suffix operator (for strings only, e.g., "software" SF "ware") 

	<em>x Op.SF y</em> iff <em>x</em> ends with the suffix <em>y</em>
     */
    public static final short	SF		= 7;
    /** <em>any</em> operator */
    public static final short	ANY		= 8;
    /** not equal operator */
    public static final short	NE		= 9;
    /** substring operator (for strings only, e.g., "software" SS "war") 

	<em>x Op.SS y</em> iff <em>x</em> contains the substring <em>y</em>
     */
    public static final short	SS		= 10;

    /** the comparison operator 
     * 
     *  valid values are defined in {@link siena.Op}
     **/
    public short getOperator();
}
