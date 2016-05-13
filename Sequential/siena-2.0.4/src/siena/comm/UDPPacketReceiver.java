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

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.EOFException;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.Socket;

/** receives packets through a UDP port.
 **/
public class UDPPacketReceiver 
    extends InetPacketReceiver 
    implements PacketReceiver {

    public static final String	Schema		= "udp";
    public static final int	DefaultPort	= 1970;

    private DatagramSocket	port;
    private DatagramPacket	packet;

    /** controls the accept-close workaround.
     *
     *  See {@link TCPPacketReceiver#AcceptPollingInterval} for an
     *  explanation of this parameter.
     **/
    public static int		AcceptPollingInterval = 3000;

    /** create a receiver listening to the given UDP port.
     *
     *  @param port_number must be a valid UDP port number, or it can
     *         be 0 in which case a random port is used
     **/
    public UDPPacketReceiver(int port_number) throws IOException {
	port = new DatagramSocket(port_number);
	port.setReceiveBufferSize(65535);
	//
	// this timout is necessary to make shutdown() work properly
	// under some platforms (e.g., jdk-1.3+linux)
	//
	if (AcceptPollingInterval > 0) 
	    port.setSoTimeout(AcceptPollingInterval);
	packet = new DatagramPacket(new byte[1], 1);
	setHostName(guessMyIPAddress(port).getHostAddress());
    }

    public UDPPacketReceiver(DatagramSocket s) throws UnknownHostException,
						      IOException {
	port = s;
	//
	// this timout is necessary to make shutdown() work properly
	// under some platforms (e.g., jdk-1.3+linux)
	//
	if (AcceptPollingInterval > 0) 
	    port.setSoTimeout(AcceptPollingInterval);
	packet = new DatagramPacket(new byte[1], 1);
	setHostName(guessMyIPAddress(port).getHostAddress());
    }

    /** explicitly set the address of this packet receiver.
     *
     *  This method allows to set the host name or IP address
     *  explicitly.  This might be necessary in the cases in which the
     *  java VM can not figure that out reliably.
     **/
    synchronized public void setHostName(String hostname) {
	my_address = (Schema + Separator + hostname + Separator +
		      Integer.toString(port.getLocalPort())).getBytes();
    }

    synchronized public void shutdown() {
	if (port == null) return;
	port.close();
	port = null;
    }

    synchronized private DatagramSocket getPort() {
	return port;
    }

    public int receive(byte[] buf) throws PacketReceiverException {
	DatagramSocket p;
	if ((p=port) == null) throw(new PacketReceiverClosed());
	while ((p = port) != null) {
	    try {
		synchronized (packet) {
		    packet.setData(buf);
		    packet.setLength(buf.length);
		    p.receive(packet);
		    return packet.getLength();
		}
	    } catch (InterruptedIOException ex) {
		//
		// happily loop... this is necessary for the
		// accept-close workaround
		//
	    } catch (EOFException ex) {
		//
		// happily loop... this is necessary for the
		// accept-close workaround
		//
	    } catch (IOException ex) {
		if (port != null) 
		    throw(new PacketReceiverException(ex.toString()));
	    }
	}
	throw new PacketReceiverClosed();
    }

    /** <em>not yet implemented</em>.
     **/
    public int receive(byte[] buf, long timeout) {
	//
	// not yet implemented
	//
	return -1;
    }
}
