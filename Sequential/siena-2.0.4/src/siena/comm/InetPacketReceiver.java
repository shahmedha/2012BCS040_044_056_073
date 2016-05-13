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
import java.net.ServerSocket;
import java.net.DatagramSocket;
import java.io.IOException;
import java.net.UnknownHostException;

/** base class for IP-based packet receivers
 **/
abstract class InetPacketReceiver {
    static final String		Separator = ":";
    byte[]			my_address;

    /** external address of this packet receiver.
     *
     *  Uses the following schema syntax:<br>
     *
     *  <em>schema</em><code>:</code><em>host</em><code>:</code><em>port</em>
     * 
     *  @see PacketReceiver#address()
     **/
    public byte[] address() {
	return my_address;
    }

    protected static InetAddress guessMyIPAddress() 
	throws UnknownHostException {
	return InetAddress.getLocalHost();
    }

    protected static InetAddress guessMyIPAddress(ServerSocket s) 
    throws UnknownHostException {
	InetAddress res = s.getInetAddress();
	if (res != null) {
	    byte [] addr = res.getAddress();
	    if (addr[0] != 0 || addr[1] != 0 || addr[2] != 0 || addr[3] != 0)
		return res;
	}
	return InetAddress.getLocalHost();
    }

    protected static InetAddress guessMyIPAddress(DatagramSocket s) 
    throws UnknownHostException {
	InetAddress res = s.getInetAddress();
	if (res != null) {
	    byte [] addr = res.getAddress();
	    if (addr[0] == 0 &&  addr[1] == 0 && addr[2] == 0 &&  addr[3] == 0)
		return res;
	}
	return InetAddress.getLocalHost();
    }
}
