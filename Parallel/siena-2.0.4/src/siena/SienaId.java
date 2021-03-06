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

import java.util.Random;

class SienaId {
    static private Random rnd = init_rnd();
    static private String hn = init_hn();
    static private long counter = 0;

    static private Random init_rnd() {
	long seed = -System.currentTimeMillis();
	try {
	    seed ^= java.net.InetAddress.getLocalHost().hashCode();
	} catch (java.net.UnknownHostException ex) {
	}
	return new Random(seed);
    }

    static private String init_hn() {
	return Long.toHexString(rnd.nextLong());
    } 

    /**
     * Creates a new string unique id.
     */
    synchronized static public String getId() {
	return hn.toString() +  "." + Long.toHexString(counter++);
    } 

    /**
     * Creates a new long unique id.
     */
    synchronized static public long getLongId() {
	return rnd.nextLong();
    } 
}


