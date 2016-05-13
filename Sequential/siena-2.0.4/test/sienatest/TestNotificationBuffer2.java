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


class TNBProducer implements Runnable {
    Notifiable buffer;
    int count;

    public TNBProducer(Notifiable b, int c) {
	buffer = b;
	count = c;
    }

    public void run() {
	try {
	    while(count-- > 0) {
		Notification e = new Notification();
		e.putAttribute("x",10);
		e.putAttribute("y",count);
		buffer.notify(e);
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
};

class TNBConsumer implements Runnable {
    NotificationBuffer buf;
    int count;

    public TNBConsumer(NotificationBuffer b, int c) {
	buf = b;
	count = c;
    }

    public void run() {
	try {
	    Notification e;
	    while(count-- > 0) {
		e = buf.getNotification(-1);
		System.out.println(e.getAttribute("y").intValue());
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

};

public class TestNotificationBuffer2 {
    public static void main(String args[]) {
	try {
	    int count = Integer.decode(args[0]).intValue();
	    NotificationBuffer buf = new NotificationBuffer();
	    TNBProducer producer = new TNBProducer(buf, count);
	    TNBConsumer consumer = new TNBConsumer(buf, count);
	    new Thread(consumer).start();
	    new Thread(producer).start();
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
