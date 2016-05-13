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

public class TestPatterns {

    public static void main(String argv[]) {
	if (argv.length < 2) {
	    System.out.println("usage: TestPatterns <receiver-handler> <count> f1 [f2...]");
	    System.exit(1);
	}
	try {
	    PacketReceiver r = ReceiverFactory.createReceiver(argv[0]);
	    int count = Integer.decode(argv[1]).intValue();

	    HierarchicalDispatcher hd = new HierarchicalDispatcher();
	    hd.setReceiver(r);

	    TestBase.siena = hd;

	    Filter p[] = new Filter[argv.length - 2];

	    for(int i = 2; i < argv.length; ++i) {
		p[i-2] = new Filter();
		p[i-2].addConstraint("x", Op.EQ, argv[i]);
	    }

	    PatternReader pr = new PatternReader(count, new Pattern(p));
	    TestBase.waitSubscribers();
	    Thread.sleep(10000);
	    hd.shutdown();
	    Thread.sleep(3000);
	    System.exit(0);
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
