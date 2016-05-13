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

import siena.comm.GenericSenderFactory;
import siena.comm.PacketSender;
import siena.comm.PacketSenderFactory;

public class TestSenderPerformance {
    static PacketSenderFactory sender_factory = new GenericSenderFactory();

    public static void main(String argv[]) {
	if (argv.length != 3) {
	    System.out.println("usage: TestSenderPerformance <address> <packet-count> <packet-length>");
	    System.exit(1);
	}
	try {
	    PacketSender sender = sender_factory.createPacketSender(argv[0]);

	    int count = Integer.decode(argv[1]).intValue();
	    int size =  Integer.decode(argv[2]).intValue();

	    byte[] buf = new byte[size];

	    long start = System.currentTimeMillis();
	    
	    for(int i = 0; i < count; ++i) 
		sender.send(buf);

	    sender.send(new byte[1]);
	    
	    long elapsed = System.currentTimeMillis() - start;

	    System.out.println("ms=" + elapsed + " count=" + count);
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
