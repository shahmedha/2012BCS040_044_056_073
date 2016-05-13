package siena;
// -*- Java -*-
//
//  This file is part of Siena, a wide-area event notification system.
//  See http://www.inf.usi.ch/carzaniga/siena/
//
//  Author: Antonio Carzaniga (firstname.lastname@usi.ch)
//  See the file AUTHORS for full details. 
//
//  Copyright (C) 1998-1999 University of Colorado
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

//
// this is an example of an object of interest, that is, a producer of
// notifications
//

class SimpleNotif implements Notifiable {
    Siena siena;

    public SimpleNotif(Siena s) {
	siena = s;
    }

    public void notify(Notification e) {
	System.out.println("local notifiable: " + e.toString());
	 try {
	     siena.unsubscribe(this);
	 } catch (SienaException ex) {
	     ex.printStackTrace();
	 }
    }

    public void notify(Notification [] s) { }
}

public class ObjectOfInterest {
    public static void main(String[] args) {
	try {
	    HierarchicalDispatcher siena;
	    siena = new HierarchicalDispatcher();

	    switch(args.length) {
	    case 1: siena.setMaster(args[0]); 
	    case 0: break;
	    default:
		System.err.println("Usage: ObjectOfInterest [server-address]");
		System.exit(1);
	    }
	    
	    Filter f = new Filter();
	    f.addConstraint("name", Op.ANY, "");
	    
	    siena.subscribe(f, new SimpleNotif(siena));
	    
	    Notification e = new Notification();
	    e.putAttribute("name", "Antonio");
	    e.putAttribute("age", 30);
	    e.putAttribute("nationality", "italian");

	    System.out.println("publishing " + e.toString());
	    try {
		siena.publish(e);
	    } catch (SienaException ex) {
		System.err.println("Siena error:" + ex.toString());
	    }
	    System.out.println("shutting down.");
	    siena.shutdown();
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
    }
}
