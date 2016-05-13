//
//  This file is part of Siena, a wide-area event notification system.
//  See http://www.inf.usi.ch/carzaniga/siena/
//
//  Author: Antonio Carzaniga (firstname.lastname@usi.ch)
//  See the file AUTHORS for full details. 
//
//  Copyright (C) 2008 Antonio Carzaniga
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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

class KAPacketSender extends InetPacketSender implements PacketSender {
    private Socket		socket;
    private OutputStream	os;
    private InputStream		is;
    private long		last_conn = -1;

    static public int		DefaultSoTimeout = 5000;
    private int			so_timeout = DefaultSoTimeout;

    static public int		DefaultDisconnectTimeout = 5000;
    private int			disconnect_timeout = DefaultSoTimeout;

    String schema() { return KAPacketReceiver.Schema; }
    int default_port() { return KAPacketReceiver.DefaultPort; }

    public KAPacketSender(String h) 
	throws InvalidSenderException, java.net.UnknownHostException {
	super(h);
    }

    Socket connect() throws IOException {
	Socket s = new Socket(ip_address, port);
	s.setTcpNoDelay(true);
	s.setSoTimeout(so_timeout);
	s.setSendBufferSize(65536);
	return s;
    }

    private void activate_connection() throws IOException {
	if (socket != null)
	    return;
	try {
	    socket = connect();
	    os = socket.getOutputStream();
	    is = socket.getInputStream();
	} catch (IOException ex) {
	    shutdown();
	    throw ex;
	}
    }

    synchronized public void send(byte[] packet, int offset, int len) 
	throws PacketSenderException {
	int retries;
	retries = 2;
	do {
	    try {
		// we first establish a connection, if we don't have
		// one already
		activate_connection();
		//
		// here I'm attempting to send the packet.  But before
		// I do that I check that the connection has not been
		// closed by the receiver.  The receiver does that by
		// writing one byte back to me (the sender), so what I
		// do is simply to check that is.available() == 0
		// before sending.
		//
		if (is.available() == 0) {
		    os.write((len & 0xff00) >>> 8);
		    os.write(len & 0xff);
		    if (len > 0)
			os.write(packet, offset, len);
		    os.flush();
		    return;
		} else {
		    //
		    // if the receiver has closed this connection, I
		    // close this end of the connection and iterate as
		    // usual (see retries variable).
		    //
		    shutdown();
		    --retries;
		}
	    } catch (IOException ex) {
		shutdown();
		if (--retries == 0)
		    throw new PacketSenderException(ex.toString());
	    }
	} while (retries > 0); 
	throw new PacketSenderException("connector closed");
    }
    
    synchronized public void shutdown() {
	if (os != null)
	    try { os.close(); } catch (IOException exx) {}
	if (is != null)
	    try { is.close(); } catch (IOException exx) {}
	if (socket != null)
	    try { socket.close(); } catch (IOException exx) {}
	socket = null;
	os = null;
	is = null;
    }

    public void send(byte[] packet) throws PacketSenderException {
	send(packet, 0, packet.length);
    }

    public void send(byte[] packet, int len) throws PacketSenderException {
	send(packet, 0, len);
    }
}
