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


public class TestThinClient implements Notifiable {

    public static ThinClient	thinclient; 

    public void notify(Notification e) {
	System.out.println("ThinClientSub: " + e.toString());
    }

    public void notify(Notification p[]) { }

    static public void main(String argv[]) {
	try {
	    if (argv.length < 4) {
		System.err.println("usage: TestThinClient <server> <pub>|<sub address> <value1> [value2...]");
		System.exit(-1);
	    }

	    ThinClient siena = new ThinClient(argv[0]);
	    
	    if (argv[1].equals("sub")) {
		Filter f = new Filter();
		TestThinClient ttc = new TestThinClient();
		siena.setReceiver(ReceiverFactory.createReceiver(argv[2]));
		for(int i = 3; i < argv.length; ++i) {
		    f.clear();
		    f.addConstraint("x", Op.EQ, argv[i]);
		    siena.subscribe(f, ttc);
		} 
	    } else if (argv[1].equals("pub")) {
		Notification e = new Notification();
		for(int i = 2; i < argv.length; ++i) {
		    e.putAttribute("x", argv[i]);
		    e.putAttribute("y", i - 1);
		    siena.publish(e);
		    Thread.sleep(500);
		}
		System.exit(0);
	    } else {
		siena.shutdown();
		System.err.println("usage: TestThinClient <server> pub|sub");
		System.exit(2);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
    }
}
