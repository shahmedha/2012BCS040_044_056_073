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
package siena;

import java.util.Iterator;
import java.util.LinkedList;

/** a disjunction of filters.
 **/
public class FilterList implements siena.fwd.Predicate {
    private LinkedList<siena.fwd.Filter> filters 
	= new LinkedList<siena.fwd.Filter>();

    public Iterator<siena.fwd.Filter> iterator() {
	return filters.iterator();
    }

    public void add(siena.fwd.Filter c) {
	filters.add(c);
    }

    public void clear() {
	filters.clear();
    }
}
