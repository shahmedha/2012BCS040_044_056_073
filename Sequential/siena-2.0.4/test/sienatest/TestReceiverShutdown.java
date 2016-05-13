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

import siena.comm.PacketReceiver;
import siena.comm.PacketReceiverClosed;

public class TestReceiverShutdown implements Runnable {
    PacketReceiver r;
    static int counter = 0;

    static synchronized void increment_counter() { ++counter; }
    static synchronized void decrement_counter() { --counter; }

    static synchronized boolean is_quiet() { return counter == 0; }

    public void run() {
	increment_counter();
	byte[] buf = new byte[65536];
	while(true) 
	    try {
		r.receive(buf);
	    } catch (PacketReceiverClosed ex) {
		decrement_counter();
		return;
	    } catch (Exception ex) {	
		System.out.println("Exception: " + ex);
		System.out.println("Continuing...");
	    }
    }

    public static void main(String argv[]) {
	if (argv.length != 3) {
	    System.out.println("usage: TestReceiverShutdown <milliseconds> <threads> [udp|ka|ssl|tcp]");
	    System.exit(1);
	}
	try {
	    PacketReceiver pr;

	    int msec = Integer.decode(argv[0]).intValue();
	    int tcount = Integer.decode(argv[1]).intValue();
	    pr = ReceiverFactory.createReceiver(argv[2]);
	    TestReceiverShutdown x = new TestReceiverShutdown();
	    x.r = pr;
	    while(tcount-- > 0)
		(new Thread(x)).start();
	    Thread.sleep(msec);
	    pr.shutdown();
	    Thread.sleep(msec);
	    if (is_quiet()) {
		System.exit(0);
	    } else {
		System.exit(1);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(2);
	}
    }
}
