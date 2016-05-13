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

import siena.comm.GenericSenderFactory;
import siena.comm.PacketSender;
import siena.comm.PacketSenderFactory;

public class Sender {
    static PacketSenderFactory sender_factory = new GenericSenderFactory();
    public static void main(String[] args) {
	try {
	    BufferedReader in
		= new BufferedReader(new InputStreamReader(System.in));
	    SENPPacket packet = new SENPPacket();
	    String inl;
	    int usec = 500;
	    if (args.length > 0) {
		usec = Integer.decode(args[0]).intValue();
	    }
	    while ((inl = in.readLine()) != null) {
		byte [] pkt = inl.getBytes();
		if (pkt.length > 0 && pkt[0] != 0x23 /* '#' */) {
		    packet.init(pkt);
		    packet.decode();
		    // skip blank lines and comments
		    if (packet.to == null) {
			System.err.println("bad input format");
			System.exit(1);
		    }
		    try {
			//
			// here I create, use, and destroy a sender
			// for each packet.  Perhaps a sender cache
			// would work be more efficient, but it would
			// leave a lot of connectors (namely KA
			// connectors) hanging there with no traffic,
			// which might delay some packets, thereby
			// screwing up my tests that depend on that...
			//
			PacketSender server;
			server = sender_factory.createPacketSender(new String(packet.to));
			server.send(pkt);
			server.shutdown();
			server = null;
		    } catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		    }
		    Thread.sleep(usec);
		}
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
    }
}
