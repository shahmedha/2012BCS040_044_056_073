//
//  This file is part of Siena, a wide-area event notification system.
//  See http://www.inf.usi.ch/carzaniga/siena/
//
//  Author: Antonio Carzaniga (firstname.lastname@usi.ch)
//  See the file AUTHORS for full details. 
//
//  Copyright (C) 1998-2003 University of Colorado
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

public class SequencerSubscriber implements Notifiable {
    int counter;
    Siena siena;

    static String AttrName = "sequencer_counter";

    public SequencerSubscriber(Siena s, int c) throws Exception {
	siena = s;
	counter = c;
    }

    public void notify(Notification n) {
	synchronized(this) {
	    int c =  n.getAttribute(AttrName).intValue();
	    if (counter != c) {
		System.out.println("Error: received " + c + " instead of " + counter);
		try { 
		    siena.shutdown(); 
		} catch (SienaException ex) {
		    ex.printStackTrace();
		}
		System.exit(1);
	    }
	    if (--counter == 0) {
		try { 
		    siena.shutdown(); 
		} catch (SienaException ex) {
		    ex.printStackTrace();
		}
		System.exit(0);
	    }
	}
    }

    public void notify(Notification [] n) {
    }

    public static void main(String[] args) throws Exception {
	ThinClient tc = null;
	int count = 30;
	long timeout = 20000;
	int threadcount = 4;
	PacketReceiver pr = null;

	switch (args.length) {
	case 5: Integer.parseInt(args[4]);
	case 4: pr = ReceiverFactory.createReceiver(args[3]);
	case 3: timeout = Long.parseLong(args[2]);
	case 2: count = Integer.parseInt(args[1]);
	case 1: tc = new ThinClient(args[0]);
	    break;
	default:
	    System.err.println("usage: SequencerSubscriber <server> [count] [timeout] [receiver-spec] [threads]");
	    System.exit(1);
	}
	if (pr != null) 
	    tc.setReceiver(pr,threadcount);

	Logging.setLogStream(System.err);

	NotificationInputSequencer s;
	s = new NotificationInputSequencer(new SequencerSubscriber(tc, count));
	Filter f = new Filter();
	f.addConstraint(AttrName, Op.GT, 0);
	tc.subscribe(f, s);

	if (timeout > 0) {
	    Thread.sleep(timeout);
	    System.out.println("SequenceSubscriber: timed out");
	    tc.shutdown(); 
	    System.exit(1);
	}
    }
}
