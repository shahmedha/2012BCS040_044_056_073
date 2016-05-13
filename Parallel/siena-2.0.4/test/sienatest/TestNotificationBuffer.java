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


class TNBPublisher implements Runnable {
    Siena siena;

    public TNBPublisher(Siena s) {
	siena = s;
    }

    public void run() {
	try {
	    Thread.sleep(1500);
	    Notification e = new Notification();
	    e.putAttribute("x",10);
	    e.putAttribute("y",5);
	    siena.publish(e);
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
};


public class TestNotificationBuffer {
    static NotificationBuffer buf;
    static HierarchicalDispatcher hd = new HierarchicalDispatcher();
    
    static void run_test1() throws SienaException, InterruptedException {
	Filter f = new Filter();
	f.addConstraint("x", Op.EQ, 10);

	hd.subscribe(f, buf);

	Notification e = new Notification();
	e.putAttribute("x", 10);
	for(int i = 0; i < 3; ++i) {
	    e.putAttribute("y", i);
	    hd.publish(e);
	}
	Thread.sleep(200);
	int ires;
	if ((ires = buf.notificationsCount()) != 3) {
	    System.out.println("error: buf.size() == " + ires + ", expecting 3");
	    System.exit(1);
	}
	Notification nres;
	nres = buf.getNotification();
	if (nres == null) {
	    System.out.println("error: buf.getNotifcation() == null");
	    System.exit(1);
	}

	if (nres.getAttribute("y").intValue() != 0) {
	    System.out.println("error: nres[y] != 0");
	    System.exit(1);
	}

	buf.getNotification();
	nres = buf.getNotification();
	if (nres == null) {
	    System.out.println("error: buf.getNotifcation() == null (#2)");
	    System.exit(1);
	}

	nres = buf.getNotification();
	if (nres != null) {
	    System.out.println("error: buf.getNotifcation() != null");
	    System.exit(1);
	}

	(new Thread(new TNBPublisher(hd))).start();
	nres = buf.getNotification(1000);
	if (nres != null) {
	    System.out.println("error: buf.getNotifcation() != null (#2)");
	    System.exit(1);
	}
	nres = buf.getNotification(1000);
	if (nres == null) {
	    System.out.println("error: buf.getNotifcation() == null (#3)");
	    System.exit(1);
	}
    }

    public static void main(String argv[]) {
	try {
	    buf = new NotificationBuffer();
	    run_test1();
	    buf = new NotificationBuffer(10);
	    run_test1();

	    buf = new NotificationBuffer(20);
	    run_test1();

	    buf = new NotificationBuffer(2);

	    Filter f = new Filter();
	    f.addConstraint("x", Op.EQ, 10);

	    hd.subscribe(f, buf);

	    Notification e = new Notification();
	    e.putAttribute("x", 10);
	    for(int i = 10; i < 14; ++i) {
		e.putAttribute("y", i);
		hd.publish(e);
	    }

	    Thread.sleep(200);
	    Notification nres = buf.getNotification(1000);
	    if (nres == null) {
		System.out.println("error: buf.getNotifcation() == null (#4)");
		System.exit(1);
	    }
	    int ires;
	    if ((ires = nres.getAttribute("y").intValue()) != 12) {
		System.out.println("error: buf.getAttribute() != 12 (== " + ires + ")");
		System.exit(1);
	    }

	    Thread.sleep(200);

	    Thread.sleep(1000);
	    hd.shutdown();
	    Thread.sleep(300);
	    System.exit(0);
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
