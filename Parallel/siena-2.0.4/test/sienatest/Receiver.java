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
package sienatest;

import siena.comm.PacketReceiver;
import siena.comm.PacketReceiverClosed;

public class Receiver {

    public static void main(String[] args) {
	try {
	    if (args.length < 1) {
		System.out.println("usage: Receiver <pkt-receiver>");
		System.exit(2);
	    }

	    PacketReceiver r = siena.ReceiverFactory.createReceiver(args[0]);

	    int res;
	    byte[] buf = new byte[65536];
	    while((res = r.receive(buf)) >= 0) {
		String s = new String(buf, 0, res);
		if (s.equals("---FINE---")) {
		    r.shutdown();
		    Thread.sleep(500);
		    System.exit(0);
		}
		System.out.println(s);
	    }
	} catch (PacketReceiverClosed ex) {
	} catch (Exception ex) {
	    System.out.println("exception: " + ex);
	    ex.printStackTrace();
	    System.exit(1);
	}
    }
}
