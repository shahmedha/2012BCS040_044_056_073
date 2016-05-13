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

class TNSSubscriber implements Notifiable {

    public void notify(Notification e) {
	System.out.print(" " + e.getAttribute("foobar").stringValue());
    }

    public void notify(Notification s[]) {
	// I never subscribe for patterns anyway. 
    }
} 

class TNSPublisher implements Runnable {
    int curr;
    Notification [] seq;
    Notifiable client;

    public TNSPublisher(Notification [] s, Notifiable c) {
	curr = 0;
	seq = s;
	client = c;
    }

    public void run() {
	Notification n;
	while (true) {
	    synchronized (this) {
		if (curr == seq.length) return;
		n = seq[curr++];
	    }
	    try { 
		//		System.out.println(n);
		client.notify(n); 
	    } catch (SienaException ex) {
		System.out.println(ex);
	    }
	}
    }
}

public class TestNotificationSequencer {

    public static void main(String argv[]) throws Exception {
	Notifiable n = new TNSSubscriber();
	NotificationOutputSequencer nos = new NotificationOutputSequencer();
	NotificationInputSequencer nis = new NotificationInputSequencer(n);

	int maxlevel = Integer.parseInt(argv[0]);
	int zerolevel = Integer.parseInt(argv[1]);
	int threadcount = Integer.parseInt(argv[2]);
	int i;
	Notification [] seq = new Notification[argv.length - 3];
	for(i = 3; i < argv.length; ++i) {
	    seq[i-3] = new Notification();
	    seq[i-3].putAttribute("foobar", argv[i]);
	    seq[i-3] = nos.tagNotification(seq[i-3]);
	}
	//
	// now we shuffle stuff around
	//
	Random rnd = new Random(System.currentTimeMillis());
	int j;
	Notification tmp;
	for(i = 1; i < seq.length; ++i) {
	    j = rnd.nextInt(maxlevel) + 1;
	    if (j < zerolevel && j + i < seq.length) {
		j += i;
		tmp = seq[i];
		seq[i] = seq[j];
		seq[j] = tmp;
		i = j;
	    }
	}
	//
	// and now we fire up the publishers
	//
	Runnable r = new TNSPublisher(seq, nis);
	for(i = 0; i < threadcount; ++i)
	    (new Thread(r)).start();
    }
}
