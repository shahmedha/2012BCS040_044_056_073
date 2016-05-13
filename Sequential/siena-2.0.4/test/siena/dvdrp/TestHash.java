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

import junit.framework.TestCase;

public class TestHash extends TestCase {

	private Hash h7 = new Hash(7);
	private Hash h3 = new Hash(3);

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testHashFunctions() {
		assertEquals(7777, h7.add(7777)); // 7777*7%8191 = 5293
		assertEquals(7777, h3.add(7777)); // 7777*3%8191 = 6949	
		assertEquals(5293, h7.add(0)); // 7777*7%8191 = 5293
		assertEquals(6949, h3.add(0)); // 7777*3%8191 = 6949		
		assertEquals(6320, h7.add("ok".getBytes())); // ((5293*7+111)%8191)*7+107)%8191=6320
	}	
	
}