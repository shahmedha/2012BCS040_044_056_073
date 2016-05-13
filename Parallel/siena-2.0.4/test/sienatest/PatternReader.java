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


public class PatternReader implements Notifiable {

    Pattern	pattern;
    int		count;

    public PatternReader(int c, Pattern p) throws SienaException {
	count = c;
	pattern = p;
	TestBase.siena.subscribe(p,this);
	TestBase.addSubscriber();
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
		    TestBase.siena.unsubscribe(pattern, this);
		    TestBase.removeSubscriber();
		} catch (SienaException ex) {
		    ex.printStackTrace();
		}
	    }
    }
}

