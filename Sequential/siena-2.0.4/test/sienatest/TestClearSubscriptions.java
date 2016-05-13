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

public class TestClearSubscriptions {

    static HierarchicalDispatcher dispatcher; 

    public static void main(String argv[]) {
	try {
	    if (argv.length < 4) {
		System.err.println("usage: TestClearSubscriptions <server> <my-id> <receiver-handler> <count>");
		System.exit(1);
	    }

	    dispatcher = new HierarchicalDispatcher(argv[1]);
	    PacketReceiver receiver = ReceiverFactory.createReceiver(argv[2]);
	    int count = Integer.parseInt(argv[3]);
	    dispatcher.setReceiver(receiver);
	    dispatcher.setMaster(argv[0]);

	    Thread.sleep(500);
	    dispatcher.clearSubscriptions();
	    Thread.sleep(500);

	    NotificationBuffer buf = new NotificationBuffer();
	    Notification n;
	    Filter f = new Filter();
	    f.addConstraint("x", Op.EQ, "a");
	    dispatcher.subscribe(f, buf);
	    while(count > 0) {
		n = buf.getNotification(0);
		System.out.println(n.toString());
		--count;
	    }

	    Thread.sleep(200);
	    dispatcher.clearSubscriptions();
	    System.out.println("subscriptions cleared");
	    Thread.sleep(500);

	    n = buf.getNotification(3000);
	    if (n != null) {
		System.out.println("EXTRA: " + n.toString());
	    }
	    dispatcher.shutdown();
	    Thread.sleep(500);
	    System.exit(0);
	}
	catch (Exception e) {
	    e.printStackTrace();
	    System.exit(2);
	}
    }
}
