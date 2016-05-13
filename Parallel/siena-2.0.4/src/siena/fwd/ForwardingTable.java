//
//  This file is part of Siena, a wide-area event notification system.
//  See http://www.inf.usi.ch/carzaniga/siena/
//
//  Author: Antonio Carzaniga (firstname.lastname@usi.ch)
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

/** a generic forwarding table.
 */
public interface ForwardingTable {

    public void consolidate();

    /** clears the forwarding table.
     *
     *  Removes every association of predicates and interfaces.  After
     *  the execution of this method, the forwarding table is ready to
     *  be rebuilt from scratch.
     */
    public void clear();
	
    /** matches a message against the proedicates in the table.
     *
     *  For each matching predicate, this method delegates the
     *  execution of a specific action to the given match handler.
     *
     *  @see MatchMessageHandler 
     */
    public void match(Message m, MatchMessageHandler p);

    /** matches a message against the proedicates in the table.
     *
     *  For each matching predicate, this method delegates the
     *  execution of a specific action to the given match handler.
     *
     *  @see MatchHandler 
     */
    public void match(Message m, MatchHandler p);

    /** associates an interface with a predicate
     * 
     *  This is the main method that builds the forwarding table.  It
     *  associates an interface object with a predicate.  Notice that
     *  this forwarding table is a dictionary-type data structure,
     *  which means that this method may be called only once for each
     *  inteface object.  If you need to modify the predicate
     *  associated with an interface, you must rebuild the entire
     *  forwarding table.
     */
    public void ifconfig(Object ifx, Predicate p) 
	throws BadConstraintException;
}
