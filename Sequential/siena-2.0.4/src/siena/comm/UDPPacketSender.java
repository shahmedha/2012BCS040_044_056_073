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
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.io.IOException;

/** UDP packet sender
 **/
class UDPPacketSender extends InetPacketSender implements PacketSender {
    private DatagramSocket	socket;
    private DatagramPacket	packet;

    String schema() { return UDPPacketReceiver.Schema; }
    int default_port() { return 1970; }

    public UDPPacketSender(String h) 
	throws InvalidSenderException, 
	       java.net.UnknownHostException,
	       java.net.SocketException {

	super(h);
	socket = new DatagramSocket();
	socket.setSendBufferSize(65535); // pulled out of my...
	//
	// looks like I have to use a buffer in the constructor even
	// though I will never use that buffer to send data, go figure...
	//
	packet = new DatagramPacket(new byte[1], 1, ip_address, port);
    }

    synchronized public void shutdown() {
	if (socket == null) return;
	socket.close();
	socket = null;
    }

    synchronized public void send(byte[] buf) throws PacketSenderException {
	if (socket == null) 
	    throw new PacketSenderException("sender shut down");
	try {
	    synchronized (packet) {
		packet.setData(buf, 0, buf.length);
		packet.setLength(buf.length);
		socket.send(packet);
	    }
	} catch (IOException ex) {
	    throw new PacketSenderException(ex.getMessage());
	}
    }

    synchronized public void send(byte[] buf, int offset, int len)
	throws PacketSenderException {
	if (socket == null) 
	    throw new PacketSenderException("sender shut down");
	try {
	    synchronized (packet) {
		packet.setData(buf, offset, len);
		packet.setLength(len);
		socket.send(packet);
	    }
	} catch (IOException ex) {
	    throw new PacketSenderException(ex.getMessage());
	}
    }

    synchronized public void send(byte[] buf, int len)
	throws PacketSenderException {
	if (socket == null) 
	    throw new PacketSenderException("sender shut down");
	try {
	    synchronized (packet) {
		packet.setData(buf, 0, len);
		packet.setLength(len);
		socket.send(packet);
	    }
	} catch (IOException ex) {
	    throw new PacketSenderException(ex.getMessage());
	}
    }
}
