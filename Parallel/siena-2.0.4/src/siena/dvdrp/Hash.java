//
//  This file is part of Siena, a wide-area event notification system.
//  See http://www.inf.usi.ch/carzaniga/siena/
//
//  Author: Antonio Carzaniga
//  See the file AUTHORS for full details. 
//
//  Copyright (C) 2011 Antonio Carzaniga
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
package siena.dvdrp;

/**
 * a configurable hashing class.
 * 
 */
public class Hash {
	
    // Note: in C they were all unsigned int. In Java we have no unsigned types, thus we could have used long
    private int X = 7; // default value
    private int P = 8191; // 2^13 - 1;
    private int h = 0;
	
    public Hash(int x){
	this.X = x;
    }
	

    //	 INPUT: string = c[1]c[2]c[3] ... c[N]
    //	 OUTPUT: c[1]*X^N + c[2]*X^(N-1) + ... + c[N-1]*X + c[N] mod P
	
    public int add(byte[] value){		
	for (int i = 0; i < value.length; i++){
	    int v = (value[i] < 0) ? value[i] + 256 : value[i];
	    h = (X*h + v) % P;
	}
	return h;
    }
	

    public int add(int value){
	h = (X * h + value) % P;		
	return h;
    }
	
    // was:
    //	    unsigned int add_double(double f) throw() {
    //		//
    //		// very quick and dirty hack
    //		//
    //		h = (X*h + static_cast<unsigned long>(f)) % P;
    //		return h;
    //	    }
    //	};
	
    public int addDouble(double value){
	//
	// very quick and dirty hack
	//
	h = (int)((X*h + (long) value ) % P);		
	return h;
    }
}
