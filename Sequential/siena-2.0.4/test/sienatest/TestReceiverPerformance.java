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

import siena.comm.KAPacketReceiver;
import siena.comm.PacketReceiver;
import siena.comm.TCPPacketReceiver;
import siena.comm.UDPPacketReceiver;

public class TestReceiverPerformance {

    public static void main(String argv[]) {
	if (argv.length != 1 && argv.length != 2 ) {
	    System.out.println("usage: TestReceiverPerformance [-udp|-ka] <port>");
	    System.exit(1);
	}
	try {
	    PacketReceiver receiver;
	    if (argv[0].equals("-udp")) {
		receiver = new UDPPacketReceiver(Integer.decode(argv[1]).intValue());
	    } else if (argv[0].equals("-ka")) {
		receiver = new KAPacketReceiver(Integer.decode(argv[1]).intValue());
	    } else {
		receiver = new TCPPacketReceiver(Integer.decode(argv[0]).intValue());
	    }

	    int count = 0;

	    byte[] buf = new byte[65536];

	    receiver.receive(buf);

	    long start = System.currentTimeMillis();
	    
	    while(receiver.receive(buf) > 1)
		++count;

	    long elapsed = System.currentTimeMillis() - start;

	    System.out.println("ms=" + elapsed + " count=" + count);
	}
	catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}
