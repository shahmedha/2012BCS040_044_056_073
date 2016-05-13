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

/** hook for the output function for matching interfaces.
 *
 *  The matching function of the forwarding table doesn't actually
 *  produce an output.  Instead, it delegates the processing of
 *  matching interfaces to a specialized <em>match handler</em>.  This
 *  base class defines the interface of such a handler.  Users of the
 *  forwarding table must implement this interface and pass it to the
 *  matching function.
 *
 *  @see MatchMessageHandler
 **/
public interface MatchHandler {

    /** output function.
     *
     *  This function is called within
     *  the @link{ForwardingTable#match(Message, MatchHandler)}
     *  matching function of the forwarding table.  The parameter is
     *  the interface identifier of the matching interface.  This
     *  function may explicitly cause the matching function to
     *  terminate by returning true.
     **/
    public boolean output(Object ifx);
}
