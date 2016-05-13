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

/** a selection for a sequence of <code>Notification</code>s.
   
    A <code>Pattern</code> is a sequence of <code>Filter</code>s,
    matched by a sequence of notifications, each one matching the
    corresponding filter in the <code>Pattern</code>.  <p>

    For example a pattern <em>[file = "hosts"]; [file = "passwd"]</em>
    is matched by two events <em>e<sub>1</sub></em> and
    <em>e<sub>2</sub></em> such that <em>e<sub>1</sub></em> matches
    filter <em>[file = "hosts"]</em>, and <em>e<sub>1</sub></em> is
    followed by <em>e<sub>2</sub></em>, and <em>e<sub>2</sub></em>
    matches filter <em>[file = "passwd"].  <p>

    @see AttributeConstraint
    @see Filter
    @see Notification
    @author Antonio Carzaniga
**/
public class Pattern {
    //
    // this should be private...
    //
    public Filter	filters[] = null;

    /** creates a pattern with the given array of filters.
     */
    public Pattern(Filter p[]) {
	filters = new Filter[p.length];
	int i = p.length;
	while(--i >= 0)
	    filters[i] = p[i];
    }

    /** creates a (deep) copy of a given pattern.
     */
    public Pattern(Pattern p) {
	filters = new Filter[p.filters.length];
	int i = p.filters.length;
	while(--i >= 0)
	    filters[i] = new Filter(p.filters[i]);
    }
}
