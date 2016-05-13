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
package siena.comm;

import java.net.InetAddress;
import java.net.Socket;
import java.io.IOException;
import java.net.UnknownHostException;

/** base class for IP-based packet senders
 **/
abstract class InetPacketSender {
    protected InetAddress	ip_address;
    protected int		port;

    static final String		Separator = ":";

    InetPacketSender(String h) 
	throws InvalidSenderException, UnknownHostException {
	//
	// this pos0 business is to maintain backward compatibility
	// with handlers of the form schema://host:port I'll get rid
	// of it as soon as I get rid of the deprecated uri stuff.
	//
	int pos0;
	if (h.indexOf("//") == 0) {
	    pos0 = 2;
	    System.out.println("Warning: the use of \"//\" in Siena handlers is deprecated\nPlease consider using the new form " + schema() + ":<host>:<port>");
	} else {
	    pos0 = 0;
	}

	int port_end_pos = -1;
	int host_end_pos = h.indexOf(Separator, pos0);

	if (host_end_pos < 0) {
	    port = default_port();
	    host_end_pos = h.length();
	} else {
	    port_end_pos = h.indexOf(Separator, host_end_pos + 1);
	    if (port_end_pos < 0) port_end_pos = h.length();

	    if (host_end_pos + 1 < port_end_pos) {
		port = Integer.decode(h.substring(host_end_pos + 1,
						  port_end_pos)).intValue();
	    } else {
		port = default_port();
	    }
	}
	String hostname = h.substring(pos0, host_end_pos);
	ip_address = InetAddress.getByName(hostname);
    }

    abstract String schema();
    abstract int default_port();

    public String toString() {
	return schema() + Separator + ip_address + Separator + port;
    }
    
    /** returns cost of packet transmissions 
     *  @see PacketSender#getCost()
     **/
     public int getCost(){
     	return 1;
     }
}
