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

public class TestEncodePerformance {

    public static void main(String[] argv) {
	try {
	    if (argv.length != 1) {
		System.out.println("usage: TestEncodePerformance <count>");
		System.exit(1);
	    }
	    int count = Integer.parseInt(argv[0]);
	    int i;
	    SENPPacket pkt = new SENPPacket();

	    java.io.BufferedInputStream in 
		= new java.io.BufferedInputStream(System.in);

	    int res = in.read(pkt.buf, 0, siena.SENP.MaxPacketLen);
	    pkt.init(res);

	    long start = System.currentTimeMillis();

	    System.out.println(new String(pkt.buf, 0, res));

	    for(i = 0; i<count ; ++i) 
		pkt.decode();

	    System.out.println("decoding: " 
			       + (System.currentTimeMillis() - start) 
			       + " / " + count);

	    start = System.currentTimeMillis();

	    int b = 0;

	    for(i = 0; i<count ; ++i) 
		b = pkt.encode();

	    System.out.println(new String(pkt.buf, 0, b));

	    System.out.println("encoding: " 
			       + (System.currentTimeMillis() - start) 
			       + " / " + count);
	}
	catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
    }
}
