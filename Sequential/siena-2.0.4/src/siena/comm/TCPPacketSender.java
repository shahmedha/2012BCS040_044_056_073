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

/** packet sender based on one-time TCP connections.
 *
 *  Sends packets trough TCP connections.  For each packet, this
 *  sender attempts to establish a TCP connection to the peer
 *  receiver.  The connection is then closed immediately after the
 *  packet is sent.
 **/
class TCPPacketSender extends InetPacketSender implements PacketSender {
    String schema() { return TCPPacketReceiver.Schema; }
    int default_port() { return 1969; }

    public TCPPacketSender(String h) 
	throws InvalidSenderException, java.net.UnknownHostException {
	super(h);
    }

    public void send(byte[] packet) throws PacketSenderException {
	try {
	    Socket s = new Socket(ip_address, port);
	    s.setSendBufferSize(65535); // pulled out of my...
	    s.getOutputStream().write(packet);
	    s.close();
	} catch (IOException ex) {
	    throw new PacketSenderException(ex.getMessage());
	}
    }

    public void send(byte[] packet, int offset, int len) 
	throws PacketSenderException {
	try {
	    Socket s = new Socket(ip_address, port);
	    s.setSendBufferSize(65535);
	    s.getOutputStream().write(packet, offset, len);
	    s.close();
	} catch (IOException ex) {
	    throw new PacketSenderException(ex.getMessage());
	}
    }

    public void send(byte[] packet, int len) throws PacketSenderException {
	try {
	    Socket s = new Socket(ip_address, port);
	    s.setSendBufferSize(65535);
	    s.getOutputStream().write(packet, 0, len);
	    s.close();
	} catch (IOException ex) {
	    throw new PacketSenderException(ex.getMessage());
	}
    }

    public void shutdown() {
	//
	// there is no state here, so shutdown() is a no-op.
	//
    }
}
