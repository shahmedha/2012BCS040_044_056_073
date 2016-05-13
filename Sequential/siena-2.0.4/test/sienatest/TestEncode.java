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

public class TestEncode {

    public static void main(String[] args) {
	SENPBuffer pkt = new SENPBuffer();
	try {
	    java.io.BufferedInputStream in 
		= new java.io.BufferedInputStream(System.in);
	    byte [] buf = new byte[SENP.MaxPacketLen];
	    int res = in.read(buf, 0, siena.SENP.MaxPacketLen);
	    byte [] buf1 = new byte[res];
	    while(--res >= 0) 
		buf1[res] = buf[res];

	    pkt.encode(new AttributeValue(buf1));
	    System.out.print(new String(pkt.buf, 0, pkt.length()));
	}
	catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
    }
}
