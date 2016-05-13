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

import siena.comm.PacketReceiver;

public class TestShutdown {
    public static void main(String argv[]) {
	if (argv.length != 2) {
	    System.out.println("usage: TestShutdown <milliseconds> <receiver-handler>");
	    System.exit(1);
	}
	try {
	    HierarchicalDispatcher siena;
	    int msec = Integer.decode(argv[0]).intValue();
	    PacketReceiver pr = ReceiverFactory.createReceiver(argv[1]);
	    siena = new HierarchicalDispatcher();
	    siena.setReceiver(pr);
	    Thread.sleep(msec);
	    siena.shutdown();
	    Thread.sleep(msec);
	    System.exit(0);
	}
	catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
    }
}
