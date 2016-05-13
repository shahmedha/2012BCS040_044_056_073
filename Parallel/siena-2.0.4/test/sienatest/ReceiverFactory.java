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

import java.io.FileOutputStream;
import java.io.IOException;

import siena.comm.KAPacketReceiver;
import siena.comm.PacketReceiver;
import siena.comm.SSLPacketReceiver;
import siena.comm.TCPPacketReceiver;
import siena.comm.UDPPacketReceiver;

public class ReceiverFactory {
    static public PacketReceiver createReceiver(String handler)
	throws IOException {
	int pos = handler.indexOf(":");
	if (pos < 0) return null;
	String schema = handler.substring(0,pos);
	int pos2 = handler.indexOf(":", pos + 1);
	String ports;
	String host = null;
	String addressfile = null;
	if (pos2 < 0) {
	    ports = handler.substring(pos + 1, handler.length());
	} else {
	    int pos3 = handler.indexOf(":", pos2 + 1);
	    ports = handler.substring(pos + 1, pos2);
	    if (pos3 < 0) {
		host =  handler.substring(pos2 + 1, handler.length());
	    } else {
		host =  handler.substring(pos2 + 1, pos3);
		addressfile = handler.substring(pos3 + 1, handler.length());
	    }
	}	    

	int port = Integer.decode(ports).intValue();
	PacketReceiver r;
	if (schema.equals(KAPacketReceiver.Schema)) {
	    KAPacketReceiver res = new KAPacketReceiver(port);
	    if (host != null) res.setHostName(host);
	    r = res;
	} else if (schema.equals(TCPPacketReceiver.Schema)) {
	    TCPPacketReceiver res = new TCPPacketReceiver(port);
	    if (host != null) res.setHostName(host);
	    r = res;
	} else if (schema.equals(UDPPacketReceiver.Schema)) {
	    UDPPacketReceiver res = new UDPPacketReceiver(port);
	    if (host != null) res.setHostName(host);
	    r = res;
	} else if (schema.equals(SSLPacketReceiver.Schema)) {
	    SSLPacketReceiver res = new SSLPacketReceiver(port);
	    if (host != null) res.setHostName(host);
	    r = res;
	} else {
	    System.out.print("ReceiverFactory: unknown receiver handler: "
			     + handler);
	    return null;
	}
	if (addressfile != null) {
	    FileOutputStream fw  = new FileOutputStream(addressfile);
	    fw.write(r.address());
	    fw.close(); 
	    fw = null;
	}
	return r;
    }
}
