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

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class TestCovering {

    public static void main(String[] args) {
	try {
	    BufferedReader in
		= new BufferedReader(new InputStreamReader(System.in));
	    String sexpect;
	    String sf1;
	    String sf2;
	    SENPPacket f1 = new SENPPacket();
	    SENPPacket f2 = new SENPPacket();
	    boolean expected;

	    while ((sexpect = in.readLine()) != null) {
		expected = sexpect.equals("yes");
		sf1 = in.readLine(); 
		if (sf1 == null) {
		    System.err.println("bad input format");
		    System.exit(1);
		}
		f1.init(sf1.getBytes());
		f1.decode();

		sf2 = in.readLine(); 
		if (sf2 == null) {
		    System.err.println("bad input format");
		    System.exit(1);
		}
		f2.init(sf2.getBytes());
		f2.decode();

		if (f1.filter == null || f2.filter == null) {
		    System.err.println("bad input format");
		    System.exit(1);
		}
		if (expected != Covering.covers(f1.filter, f2.filter)) {
		    System.err.println(sf1 + " " + sf2);
		    System.err.println("Error: expecting " + sexpect);
		    System.exit(1);
		}
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
    }
}
