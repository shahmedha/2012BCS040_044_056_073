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

public class TestPacketSender {
    static PacketSenderFactory sender_factory = new GenericSenderFactory();

    public static void main(String argv[]) {
	try {
	    PacketSender sender = null;
	    String inl;
	    int sleepytime = -1;
	    int i = 0;
	    boolean quiet = false;

	    if (i < argv.length && argv[0].equals("-quiet")) {
		quiet = true;
		++i;
	    }
	    switch (argv.length - i) {
	    case 2: sleepytime = Integer.parseInt(argv[i + 1]);
	    case 1: 
		sender = sender_factory.createPacketSender(argv[i]);
		break;
	    default:
		System.out.println("usage: TestPacketSender <address> [<millisecs>]");
		System.exit(1);
	    }

	    BufferedReader in
		= new BufferedReader(new InputStreamReader(System.in));

	    if (quiet) {
		while ((inl = in.readLine()) != null) {
		    sender.send(inl.getBytes());
		    if (sleepytime > 0) 
			Thread.sleep(sleepytime);
		}
	    } else {
		while ((inl = in.readLine()) != null) {
		    System.out.print("#");
		    sender.send(inl.getBytes());
		    if (sleepytime > 0) 
			Thread.sleep(sleepytime);
		}
		System.out.println(".");
	    }
	}
	catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
    }
}
