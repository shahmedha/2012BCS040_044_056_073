//
//  This file is part of Siena, a wide-area event notification system.
//  See http://www.inf.usi.ch/carzaniga/siena/
//
//  Author: Giovanni Toffetti Carughi
//  See the file AUTHORS for full details. 
//
//  Copyright (C) 2007, 2011 Giovanni Toffetti Carughi
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
package siena.dvdrp;

import junit.framework.Test;
import junit.framework.TestSuite;
import siena.TestDVDRPDispatcher;
import siena.TestSENPEncodeDecode;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for siena.dvdrp");
		//$JUnit-BEGIN$
		suite.addTestSuite(TestHash.class);
		suite.addTestSuite(TestDVEntry.class);
		suite.addTestSuite(TestBloomFilter.class);
		suite.addTestSuite(TestSENPEncodeDecode.class);
		suite.addTestSuite(TestDVDRPDispatcher.class);
		//suite.addTestSuite(TestDVDRPDispatcherDiscovery.class);
		//suite.addTestSuite(TestDVDRPDispatcherMRN.class);
		//$JUnit-END$
		return suite;
	}
	
	public static void main(String args[]) throws Exception {
		junit.textui.TestRunner.run(suite());
	} 

}
