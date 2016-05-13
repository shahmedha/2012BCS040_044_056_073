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
package siena;

import junit.framework.TestCase;
import siena.dvdrp.BSetBV;
import siena.dvdrp.DVEntry;
import siena.dvdrp.DistanceVector;
import siena.dvdrp.OrderedByteArraySet;
import siena.dvdrp.PredicatesTableEntry;

public class TestSENPEncodeDecode extends TestCase {
	
	SENPPacket req = new SENPPacket();
	DVEntry d = new DVEntry();
	DVEntry d2 = new DVEntry();
	PredicatesTableEntry p = new PredicatesTableEntry();
	SENPPacket drp = new SENPPacket();
	SENPPacket pad = new SENPPacket();
	
	/**
	 * Tests serialization and de-serialization of distance vectors and bloom filters
	 */
	
	protected void setUp() throws Exception {
		//here we create a predicate 'd' with 2 filters, then we add it to a distance vector table		
		d.setDest("Destination".getBytes());
		d.setDist(5);
		d.setNextHopId("NextHop".getBytes());
		p.setDest("Destination".getBytes());
		d2.setDest("Destination2".getBytes());
		d2.setDist(5);
		d2.setNextHopId("NextHop".getBytes());
		Filter f = new Filter();
		f.addConstraint("Alert", new AttributeConstraint(Op.EQ, "red"));
		f.addConstraint("TextMessage", new AttributeConstraint(Op.SS, "base"));
		BSetBV b = new BSetBV(f);		
		f.addConstraint("Severity", new AttributeConstraint(Op.GT, 5));
		f.addConstraint("A1", new AttributeConstraint(Op.EQ, "XXX"));
		f.addConstraint("A2", new AttributeConstraint(Op.EQ, "YYY"));
		f.addConstraint("A3", new AttributeConstraint(Op.EQ, "ZZZ"));
		f.addConstraint("A4", new AttributeConstraint(Op.EQ, "XXX"));
		BSetBV c = new BSetBV(f);	
		p.getFilters().add(b);
		p.getFilters().add(c);
		p.setFiltersSeqNo(System.currentTimeMillis());
		d.setEntryTS(System.currentTimeMillis());
		System.out.println(d);
		System.out.println(p);
		SENPPacket spkt = new SENPPacket();		
		spkt.filter = f;
		spkt.method = SENP.SUB;
		spkt.to = "Destination".getBytes();
		spkt.id = "Sender".getBytes();
		spkt.encode();
		DistanceVector dv = new DistanceVector();
		dv.addEntry("Destination".getBytes(), d);
		dv.addEntry("Destination2".getBytes(), d2);		
		System.out.println("Get Entry: " + dv.getEntry("Destination".getBytes()));
		System.out.println(spkt.toString());
		
		pad.method = SENP.PAD;
		pad.to = "Destination".getBytes();
		pad.id = "Sender".getBytes();
		pad.predicate = p;
		System.out.println("Get Entry: " + dv.getEntry("Destination".getBytes()));
		System.out.println(pad.toString());		
		
		req.to = "Destination".getBytes();
		req.id = "Sender".getBytes();
		req.method = SENP.DV;
		req.distanceVector = dv;		
		req.encode();
		System.out.println(req.toString());
		// here we prepare another packet with a DRP request (contains a bloom filter and a list of recipients in the header)
		drp = new SENPPacket();
		drp.to = "Destination".getBytes();
		drp.id = "Sender".getBytes();
		drp.method = SENP.DRP;
		drp.bloomFilter = c;
		drp.recipients = new OrderedByteArraySet();
		drp.recipients.add("Calvin".getBytes());
		drp.recipients.add("Hobbes".getBytes());
		drp.recipients.add("Susie".getBytes());
		drp.encode();
		System.out.println(drp.toString());
	}
	
	public void testSENPEncodeDecode(){
		
		SENPPacket res = new SENPPacket();
		res.init(req.buf);
		SENPPacket res2 = new SENPPacket();
		res2.init(drp.buf);
		SENPPacket res3 = new SENPPacket();
		res3.init(pad.buf);
		try{
		res.decode();
		res2.decode();
		res3.decode();
		} catch (SENPInvalidFormat sif) {
			System.err.println(sif.getMessage());	
			sif.printStackTrace();
		}
		byte[] eid = (byte[]) res.distanceVector.getEntryIdsIterator().next();
		System.out.println(res.distanceVector);
		DVEntry e = res.distanceVector.getEntry(eid);
		d.setEntryTS(0);	
		System.out.println("d: " + d.toString());
		System.out.println("e: " + e.toString());
		assertEquals(d.toString(),e.toString());		
		assertEquals(res3.predicate.toString(), pad.predicate.toString());
		System.out.println(res2.recipients);
		System.out.println(drp.recipients);
		assertTrue(res2.recipients.equals(drp.recipients));
		
	}

}

