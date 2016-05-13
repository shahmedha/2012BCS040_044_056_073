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

class TestSubscriber implements Notifiable {
    int counter;

    public TestSubscriber(int c) throws SienaException {
	counter = c;
	Filter f = new Filter();
	f.addConstraint("x", counter);
	TestLocalPubSub.siena.subscribe(f, this);
    } 

    public void notify(Notification e[]) throws SienaException { }

    public void notify(Notification e) throws SienaException {
	System.out.println(Integer.toString(counter)+ ":" + e.toString());
	
	AttributeValue a = e.getAttribute("x");
	if (a == null) {
	    System.err.println("Error: attribute `x' is missing.");
	} else if (a.getType() != AttributeValue.INT 
		   || a.intValue() != counter) {
	    System.err.println("Error: unexpected value for attribute `x'.");
	} else { 
	    if (counter > 0) {
		Filter fu = new Filter();
		fu.addConstraint("x", counter);
		TestLocalPubSub.siena.unsubscribe(fu, this);
		counter--;
		
		Filter f = new Filter();
		f.addConstraint("x", counter);
		TestLocalPubSub.siena.subscribe(f,this);
	    } else {
		TestLocalPubSub.siena.unsubscribe(this);
	    }
	}
    }
}

public class TestLocalPubSub {

    static public HierarchicalDispatcher siena;

    static public void main(String argv[]) {
	try {
	    if (argv.length == 0) {
		System.out.println("usage: TestLocalPubSub <n1> [...]");
		System.exit(-1);
	    }
	    siena = new HierarchicalDispatcher();
	    Notifiable [] subscribers = new Notifiable[argv.length];
	    
	    for(int i = 0; i < argv.length; ++i) 
		subscribers[i] = new TestSubscriber(Integer.decode(argv[i]).intValue());
	    
	    Notification e = new Notification();
	    e.putAttribute("extra", "whatever"); 
	    for (int i = 0; i < 10; i++)
		for (int j = 0; j < 10; j++) {
		    Thread.sleep(100);
		    e.putAttribute("x", j);
		    siena.publish(e);
		}
	    for(int i = 0; i < argv.length; ++i) 
		siena.unsubscribe(subscribers[i]);
	    siena.shutdown();
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
