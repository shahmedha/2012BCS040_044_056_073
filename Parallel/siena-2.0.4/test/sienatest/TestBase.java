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


public class TestBase {
    public static HierarchicalDispatcher	hdsiena		= null;
    public static ThinClient			tcsiena		= null;
    public static Siena				siena		= null;

    private static int				scount		= 0;
    private static Object			scountLock = new Object();

    public static void addSubscriber() {
	synchronized (scountLock) {
	    scount++;
	}
    }
    
    public static void removeSubscriber() {
	synchronized (scountLock) {
	    if (scount > 0) {
		--scount;
	    } else {
		scount = 0;
	    }
	    if (scount == 0) scountLock.notifyAll();
	}
    }
    
    public static void waitSubscribers() throws InterruptedException {
	while (true) {
	    synchronized (scountLock) {
		if (scount != 0) {
		    scountLock.wait();
		} else {
		    return;
		}
	    }
	}
    }
}
