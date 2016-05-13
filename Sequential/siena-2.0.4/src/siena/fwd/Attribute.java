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

import java.util.Iterator;

/** interface of an attribute in a message.
 * 
 *  An attribute consists of a name and a value.
 *  @see siena.fwd.Message
 */
public interface Attribute {
    /** returns the name of this attribute 
     */
    public String getName();
    /** returns the value of this attribute. 
     *
     *  @see siena.fwd.Value
     */
    public Value getValue();
}
