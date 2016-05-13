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

/**
   Siena selection operators.

   Op defines a set of constants that represent the selection
   operators offered by Siena.  For example, <code>Op.EQ</code>
   represents the ``equality'' operator, <code>Op.GT</code> represents
   the ``greater than'' operator, <code>Op.LE</code> represents
   the ``less or equal'' operator, etc.
   <p>
   
   Op also offers a convenient translation function that returns
   operator codes based on their textual representation.  For example,
   <code>Op.op("=")</code> returns <code>Op.EQ</code>,
   <code>Op.op("&gt;")</code> returns <code>Op.GT</code>, ,
   <code>Op.op("&lt;=")</code> returns <code>Op.LE</code>, etc.

   @see AttributeConstraint
   @see Filter
 **/
public class Op {
    //
    // op is one of these:
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

    /** string representation of operators */ 
    public static final String	operators[] 
	= { null, "=", "<", ">", ">=", "<=", ">*", "*<", "any", "!=", "*" };

    /**
       returns the operator corresponding to the given string representation.

       @param strop string representation of the operator (e.g. "=" returns 
              <code>Operator.EQ</code>)

       @return operator code or 0 if the string is not a valid representation
    **/
    public static short op(String strop) {
	for(short i = 1; i < operators.length; ++i)
	    if (operators[i].equals(strop)) return i;
	return 0;
    }
}

