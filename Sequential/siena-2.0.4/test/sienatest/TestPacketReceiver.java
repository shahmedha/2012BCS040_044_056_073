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

class TestPacketReceiverOutput implements Runnable {
    PacketReceiver r;

    public TestPacketReceiverOutput(PacketReceiver x) {
	r = x;
    }

    public void run() {
	try {
	    byte[] buf = new byte[65536];
	    int res;
	    while((res = r.receive(buf)) >= 0) {
		String s = new String(buf, 0, res);
		if (s.equals("---FINE---")) {
		    r.shutdown();
		    Thread.sleep(500);
		    System.exit(0);
		}
		synchronized (System.out) {
		    System.out.println(s);
		}
	    }
	} catch (PacketReceiverClosed ex) {
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
}

class TestPacketReceiverPerf implements Runnable {
    PacketReceiver r;
    static int counter = 0;
    static long start = 0;
    static long stop = 0;

    public TestPacketReceiverPerf(PacketReceiver x) {
	r = x;
	counter = 0;
    }

    static synchronized void increment_counter() {
	if (start == 0)
	    start = System.currentTimeMillis();
	++counter;
    }

    public void run() {
	try {
	    byte[] buf = new byte[65536];
	    int res;
	    while((res = r.receive(buf)) >= 0) {
		//
		// pretty ugly code uh... well, it's the most
		// efficient implementation of (new String(buf, 0,
		// res).equals("---FINE---") that I can think of
		//
		if (res == 10 
		    && buf[0] == 45 && buf[1] == 45 && buf[2] == 45 
		    && buf[3] == 70 && buf[4] == 73 && buf[5] == 78 
		    && buf[6] == 69 
		    && buf[7] == 45 && buf[8] == 45 && buf[9] == 45) {
		    if (counter != 0) {
			System.out.println(counter + " packets in " 
					   + (stop - start) + " ms");
		    }
		    r.shutdown();
		    Thread.sleep(500);
		    System.exit(0);
		} else {
		    //
		    // I must get the time every time, since every message
		    // could be the last one.
		    //
		    stop = System.currentTimeMillis();
		    increment_counter();
		}
	    }
	} catch (PacketReceiverClosed ex) {
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
}

public class TestPacketReceiver {
    public static void main(String argv[]) {
	if (argv.length < 2 || argv.length > 3 ) {
	    System.out.println("usage: TestPacketReceiver [-perf] <receiver-initializer> <threads>");
	    System.exit(1);
	}
	try {
	    int i = 0;
	    boolean perf = false;
	    PacketReceiver receiver;

	    if (argv[i].equals("-perf")) {
		++i;
		perf = true;
	    }

	    receiver = ReceiverFactory.createReceiver(argv[i]);
	    int threads = Integer.decode(argv[++i]).intValue();

	    if (perf) {
		while(threads-- > 0) 
		    (new Thread(new TestPacketReceiverPerf(receiver))).start();
	    } else {
		while(threads-- > 0) 
		    (new Thread(new TestPacketReceiverOutput(receiver))).start();
	    }
	}
	catch (Exception e) {
	    e.printStackTrace();
	    System.exit(2);
	}
    }
}
