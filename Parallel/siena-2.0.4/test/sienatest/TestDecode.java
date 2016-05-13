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

public class TestDecode {

    public static void main(String[] args) {
	SENPBuffer t = new SENPBuffer();
	try {
	    int pos = 0;
	    int res;

	    while ((res = System.in.read(t.buf, pos, SENP.MaxPacketLen - pos)) > 0) 
		pos += res;
	    t.init(pos);

	    int ttype;
	    if ((ttype = t.nextToken()) != SENPBuffer.T_STR) {
		System.err.println("Error: expecting string");
		System.err.println(ttype);
		System.exit(1);
	    }
	    System.out.write(t.copy_sval());
	}
	catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
    }
}
