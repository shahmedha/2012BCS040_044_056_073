//
//  This file is part of Siena, a wide-area event notification system.
//  See http://www.inf.usi.ch/carzaniga/siena/
//
//  Author: Antonio Carzaniga (firstname.lastname@usi.ch)
//  See the file AUTHORS for full details. 
//
//  Copyright (C) 2008 Antonio Carzaniga
//  Copyright (C) 1998-2000 University of Colorado
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

import java.util.LinkedList;
import java.io.InterruptedIOException;
import java.io.IOException;
import java.io.EOFException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/** receives packets through a TCP port.
 *
 *  Uses persistent TCP connections to receive one or more packets.
 **/
public class KAPacketReceiver 
    extends InetPacketReceiver implements PacketReceiver {

    private Socket []		queue;
    private int			first;
    private int			size;

    public static final String	Schema = "ka";
    public static final int	DefaultPort = 1971;

    ServerSocket		port;

    /** controls the accept-close workaround.
     *
     *  See {@link TCPPacketReceiver#AcceptPollingInterval} for an
     *  explanation of this parameter.
     **/
    static public int		AcceptPollingInterval = 10000;

    /** default value for the receive timeout.
     *
     *  @see #receive_timeout
     **/
    static public int		DefaultReceiveTimeout = 10000;

    /** limits the duration of blocking input when receiving packets.
     *
     *  A positive value defines a timeout for receive operations for
     *  this receiver.  This parameter affects new connections only.
     *  A value &lt;=0 means infinite timeout.
     **/
    public int			receive_timeout = DefaultReceiveTimeout;

    /** default limit for active connections.
     *
     *  @see #setMaxActiveConnections(int)
     **/
    static public int		DefaultMaxActiveConnections = 20;

    private void closeConnection(Socket s) {
	try {
	    OutputStream os = s.getOutputStream();
	    os.write(0);
	    os.close();
	} catch (IOException ex) {
	    // I don't know exactly what I am supposed to do in this case.
	    // ...work in progress...
	    System.err.println("KAPacketReceiver: error closing connection.");
	} 
	try {
	    // I don't know exactly what I am supposed to do in this case.
	    // ...work in progress...
	    s.close();
	} catch (IOException ex) {
	    System.err.println("KAPacketReceiver: error closing connection.");
	}
    }

    /** limits the number of active connections.
     *
     *  This receiver will maintain at most <code>m</code> active
     *  connections.  If a new connection is accepted when
     *  <code>m</code> connections are active, the new accepted
     *  connections will replace the oldest active connection.
     *
     *  <p>When the limit is lowered and <em>x &gt; m</em> connections
     *  are active, the oldest <em>x - m</em> connections are closed.
     **/
    synchronized public void setMaxActiveConnections(int m) {
	if (m != queue.length) {
	    Socket [] nq = new Socket[m];
	    while(size > m) {
		closeConnection(queue[first]);
		queue[first] = null;
		--size;
		first = (first + 1) % queue.length;
	    }
	    for(int i=0; i < size; ++i) {
		nq[i] = queue[first];
		first = (first + 1) % queue.length;
	    }
	    first = 0;
	}
    }

    /** creates a receiver listening to the a random port.
     *
     *  @exception IOException if an I/O error occurs when opening the
     *		socket port.
     **/
    public KAPacketReceiver() throws IOException {
	this(new ServerSocket(0));
    }

    /** creates a receiver listening to the given port.
     *
     *  @param port_number must be a valid TCP port number, or it can
     *         be 0 in which case a random port is used
     *
     *  @exception IOException if an I/O error occurs when opening the
     *		socket.  typically, when the given port is already in use.
     **/
    public KAPacketReceiver(int port_number) throws IOException {
	this(new ServerSocket(port_number));
    }

    /** creates a receiver listening to the given port with a given
     *  maximum queue for TCP connections.
     *
     *  @param port_number must be a valid TCP port number, or it can
     *		be 0 in which case a random port is used
     *
     *  @exception IOException if an I/O error occurs when opening the
     *		socket.  typically, when the given port is already in use.
     **/
    public KAPacketReceiver(int port_number, int qsize) throws IOException {
	this(new ServerSocket(port_number, qsize));
    }

    /** creates a receiver listening to the given port.
     *
     *  @param s server socket used to accept connections.
     *
     *  @exception UnknownHostException if an error occurrs while
     *		resolving the hostname for this host.
     **/
    public KAPacketReceiver(ServerSocket s) throws IOException {
	queue = new Socket[DefaultMaxActiveConnections];
	size = first = 0;

	port = s;
	//
	// this timout is necessary to make shutdown() work properly
	// under some platforms (e.g., jdk-1.3+linux)
	//
	if (AcceptPollingInterval > 0) 
	    port.setSoTimeout(AcceptPollingInterval);
	setHostName(guessMyIPAddress(port).getHostAddress());
    }

    /** explicitly sets the address of this packet receiver.
     *
     *  This method allows users to set the host name or IP address
     *  that this receiver advertises as its externally-visible
     *  address.  This might be necessary in the cases in which the
     *  Java VM can not reliably figure out such an address by itself.
     **/
    synchronized public void setHostName(String hostname) {
	my_address = (Schema + Separator + hostname + Separator 
		      + Integer.toString(port.getLocalPort())).getBytes();
    }

    private Socket accept_new_connection() throws IOException {
	// I have to use an auxiliary variable (p) because port is a
	// volatile variable, as it can be annulled by a call to
	// shutdown().  I could synchronize this entire method, but I
	// don't want to do that because I don't want to have the
	// receiver completely locked while I am stuck in accept().
	ServerSocket p = port;
	// a perhaps safer way to play this is as follows:
	// ServerSocket p = port;
	// synchronized(this) { p = port; }
	// but if I understand the semantics of Java, there is no
	// danger of getting corrupt values when accessing a volatile
	// variable.
	if (p == null) 
	    return null;
	Socket s = p.accept();
	s.setTcpNoDelay(true);
	s.setReceiveBufferSize(65536);	// I made this up...
	if (receive_timeout > 0)
	    s.setSoTimeout(receive_timeout);
	return s;
    }

    synchronized private Socket get_connection_from_queue() {
	if (size == 0) return null;
	Socket sock = queue[first];
	queue[first] = null;
	first = (first + 1) % queue.length;
	--size;
	return sock;
    }

    public int receive(byte [] buf) throws PacketReceiverException {
	// this method consists of two parts: first we obtain a live
	// connection, second we read a packet from that connection.
	Socket sock;
	while (true) {
	    do {
		// here we try to obtain a connection.  We first see
		// if we have one in the pool (queue) of connections.
		sock = get_connection_from_queue();
		if (sock == null) {
		    try {
			sock = accept_new_connection();
			if (sock == null) 
			    throw new PacketReceiverClosed();
		    } catch (java.net.SocketTimeoutException ex) {
			// we waited on accept() for the set maximum
			// time.  We simply continue in this loop.  We
			// might find a new connection in the queue or
			// fall back into accepting new connections.
		    } catch (InterruptedIOException ex) {
			// same idea as above
		    } catch (IOException ex) {
			// an error occurred while we were accepting
			// connections.  In this case we shutdown the
			// receiver.
			shutdown();
			throw new PacketReceiverClosed();
		    }
		}
	    } while (sock == null);
	    try {
		int res = receive(sock, buf);
		if (res >= 0) {
		    push_back(sock);
		    return res;
		}
	    } catch (EOFException ex) {
		// in case of error, we simply throw away the
		// connection we got, and continue the loop, as long
		// as port != null
	    } catch (InterruptedIOException ex) {
		// same as above, we throw away the connection and
		// continue the loop.
	    } catch (Exception ex) {
		closeConnection(sock);
		throw new PacketReceiverException(ex.toString());
	    }
	    closeConnection(sock);
	}
    }

    /** <em>not yet implemented</em>.
     **/
    public int receive(byte[] buf, long timeout) {
	//
	// not yet implemented
	//
	return -1;
    }

    synchronized public void shutdown() throws PacketReceiverException {
	if (port == null) return;
	try {
	    port.close();
	    //
	    // this should terminate all the connection handlers
	    // attached to that port.  They should receive an
	    // IOException on accept() ...they should, but on some
	    // implementations of the JVM they don't.  In
	    // particular, jvm-1.3rc1-linux-i386 is buggy.
	    //
	} catch (IOException ex) {
	    throw new PacketReceiverException(ex.toString());
	    //
	    // what can I do here? ...work in progress...
	    //
	}
	port = null;
	while (size > 0) {
	    closeConnection(queue[first]);
	    queue[first] = null;
	    --size;
	    first = (first + 1) % queue.length;
	}
    }

    public static int receive(Socket sock, byte [] buf) 
	throws InterruptedException, IOException {
	InputStream stream = sock.getInputStream();
	int res = 0;
	int len = 0;
	int len1 = 0;
	int pos = 0;
	//
	// we read the msb and lsb of the length.  We break if
	// we can't read them or if the resulting length is 0
	//
	len = stream.read();
	if (len < 0) return -1;
	len1 = stream.read();
	if (len1 < 0) return -1;
	len = (len << 8) | (len1 & 0xff);
	if (len < 0) return -1;
	pos = 0;
	while(pos < len) {
	    if ((res = stream.read(buf, pos, len - pos)) < 0) 
		return -1;
	    pos += res;
	}
	return len;
    }

    synchronized private void push_back(Socket sock) {
	if (port == null) {
	    closeConnection(sock);
	} else if (size < queue.length) {
	    queue[(first + size) % queue.length] = sock;
	    ++size;
	} else {
	    //
	    // here I could choose which one to replace more
	    // intelligently, but for now I will just replace the first
	    //
	    closeConnection(queue[first]);
	    queue[first] = sock;
	    first = (first + 1) % queue.length;
	}
    }
}
