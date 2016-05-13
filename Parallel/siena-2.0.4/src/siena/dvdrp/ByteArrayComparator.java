//
//  This file is part of Siena, a wide-area event notification system.
//  See http://www.inf.usi.ch/carzaniga/siena/
//
//  Author: Giovanni Toffetti Carughi
//  See the file AUTHORS for full details. 
//
//  Copyright (C) 2007, 2011 Giovanni Toffetti Carughi
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

import java.util.Arrays;
import java.util.Comparator;

public class ByteArrayComparator implements Comparator<byte[]> {
    public int compare(byte[] arg0, byte[] arg1) throws ClassCastException {
	if (arg0 instanceof byte[] && arg1 instanceof byte[]) {
	    if (Arrays.equals(arg0, arg1)) {
		return 0;
	    } else {
		byte[] a = arg0;
		byte[] b = arg1;
		for (int i = 0; i < a.length && i < b.length; i++) {
		    if (a[i] < b[i]) {
			return -1;
		    } else if (a[i] > b[i]) {
			return 1;
		    }
		}
		// if we got here the shorter string comes first
		if (a.length < b.length) {
		    return -1;
		} else {
		    return 1;
		}
	    }
	} else {
	    throw (new ClassCastException("Unsupported types"));
	}
    }
}
