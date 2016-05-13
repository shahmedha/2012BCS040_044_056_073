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


class PatternReader2 implements Notifiable {

    Pattern	pattern;
    int		count;
    Siena	siena;

    public PatternReader2(int c, Pattern p, Siena s) throws SienaException {
	siena = s;
	count = c;
	pattern = p;
	siena.subscribe(p,this);
    }

    public void notify(Notification e) {
	System.out.println("notification:" + e.toString());
    }

    public void notify(Notification p[]) {
	System.out.println("pattern:");
	for(int i = 0; i < p.length; ++i)
	    System.out.println(i + " " + p[i].toString());
	if (count > 0)
	    if (--count == 0) {
		try {
		    System.out.println("unsubscribing.");
		    siena.unsubscribe(pattern, this);
		    TestPatterns2.tcs.shutdown();
		    try { Thread.sleep(1000); 
		    } catch (InterruptedException ex) {}
		    System.exit(0);
		} catch (SienaException ex) {
		    ex.printStackTrace();
		}
	    }
    }
}

public class TestPatterns2 {
    static public ThinClient tcs;

    public static void main(String argv[]) {
	if (argv.length < 3) {
	    System.out.println("usage: TestPatterns2 <server-address> <receiver> <count> f1 [f2...]");
	    System.exit(1);
	}
	try {
	    tcs = new ThinClient(argv[0]);
 	    tcs.setReceiver(ReceiverFactory.createReceiver(argv[1]));
	    int count = Integer.decode(argv[2]).intValue();

	    Filter p[] = new Filter[argv.length - 3];

	    for(int i = 3; i < argv.length; ++i) {
		p[i-3] = new Filter();
		p[i-3].addConstraint("x", Op.EQ, argv[i]);
	    }

	    PatternReader2 pr = new PatternReader2(count, new Pattern(p), tcs);
	    Thread.sleep(40000);
	    tcs.shutdown();
	    Thread.sleep(3000);
	    System.exit(0);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(2);
	}
    }
}
