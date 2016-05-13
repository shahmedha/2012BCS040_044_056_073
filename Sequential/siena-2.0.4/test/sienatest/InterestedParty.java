package siena;
// -*- Java -*-
//
//  This file is part of Siena, a wide-area event notification system.
//  See http://www.inf.usi.ch/carzaniga/siena
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
// this is an example of an interested party, that is, a consumer of
// notifications
//

public class InterestedParty implements Notifiable {
    public void notify(Notification e) {
        System.out.println("I just got this event:");
        System.out.println(e.toString());
    };

    public void notify(Notification [] s) { }

    public static void main(String[] args) {
	if(args.length != 1) {
	    System.err.println("Usage: InterestedParty <server-address>");
	    System.exit(1);
	}
	
	HierarchicalDispatcher siena;
	try {
	    siena = new HierarchicalDispatcher();
	    siena.setMaster(args[0]);

	    Filter f = new Filter();
	    f.addConstraint("name", "Antonio"); // name = "Antonio"
	    f.addConstraint("age", Op.GT, 18);	// age > 18
	
	    InterestedParty party = new InterestedParty();
	    
	    System.out.println("subscribing for " + f.toString());
	    try {
		siena.subscribe(f, party);
		try {
		    Thread.sleep(300000);	// sleeps for five minutes
		} catch (java.lang.InterruptedException ex) {
		    System.out.println("interrupted"); 
		}
		System.out.println("unsubscribing");
		siena.unsubscribe(f, party);
	    } catch (SienaException ex) {
		System.err.println("Siena error:" + ex.toString());
	    }
	    System.out.println("shutting down.");
	    siena.shutdown();
	    System.exit(0);
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	} 
    };
}
