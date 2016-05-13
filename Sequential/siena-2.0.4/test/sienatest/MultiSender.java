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

import java.util.Random;

public class MultiSender implements Runnable {
    static int guard = 0;
    static int counter = 30;
    static NotificationOutputSequencer s;
    static ThinClient tc;
    static int odds = 0;
    static long skew = 1;

    private static Random rnd = new Random(System.currentTimeMillis());

    static synchronized boolean flip_coin() {
	return rnd.nextInt(odds) == 1;
    }

    static synchronized Notification getNotification() {
	if (counter == 0) return null;
	Notification n = new Notification();
	n.putAttribute(SequencerSubscriber.AttrName, counter--);
	return s.tagNotification(n);
    }

    static synchronized void tend() {
	if (--guard <= 0) tc.shutdown();
    }

    public void run() {
	try {
	    Notification n;
	    int c;
	    while((n = getNotification()) != null) {
		if (flip_coin()) // here we purposely screw up the
				 // publication order
		    Thread.sleep(100);
		tc.publish(n);
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	tend();
    }

    public static void main(String[] args) throws Exception {
	    
	    counter = Integer.parseInt(args[1]);
	    int threadcount = 5;
	    switch (args.length) {
	    case 5: skew = Long.parseLong(args[4]);
	    case 4: odds = Integer.parseInt(args[3]);
	    case 3: threadcount = Integer.parseInt(args[2]);
	    case 2: counter = Integer.parseInt(args[1]);
	    case 1: tc = new ThinClient(args[0]);
		break;
	    default:
		System.err.println("usage: MultiSender <server> [count] [threads] [odds] [skew]");
		System.exit(1);
	    }
	    s = new NotificationOutputSequencer();
	    guard = threadcount;
	    for(int i = 0; i < threadcount; ++i)
		(new Thread(new MultiSender())).start();
    }
}
