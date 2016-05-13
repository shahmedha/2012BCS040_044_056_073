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
import java.io.EOFException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/** receives packets through a TCP port.
 *
 *  Receives packets through one-time connections to a TCP port.
 *  Accepts connections to a local port, reads one packet from the
 *  accepted socket, and closes the socket.
 **/
public class TCPPacketReceiver 
    extends InetPacketReceiver 
    implements PacketReceiver {

    public static final String	Schema		= "tcp";
    public static final int	DefaultPort	= 1970;

    private ServerSocket	port;
    
    /** controls the accept-close workaround.
     *
     *  The semantics of <code>java.net.ServerSocket.accept()</code>
     *  is broken, in the sense that there is no reliable way to
     *  interrupt a thread blocking on <code>accept()</code>, which is
     *  necessary to make {@link #shutdown()} work properly.  On some
     *  platforms, such as JDK 1.3 on Sparc Solaris, it is possible to
     *  interrupt <code>accept()</code> by closing the server socket
     *  (in which case <code>accept()</code> raises an exception),
     *  however that semantics is not defined in Sun's JDK (or at
     *  least it is not documented).  And in fact, other
     *  implementations, such as Sun's JDK v.1.3 for i386 Linux, don't
     *  behave that way.
     *
     *  <p>Bottom line: we need a workaround to break out of
     *  <code>accept()</code> in a reliable way.  The only workaround
     *  I was able to figure out consists in setting a timeout on
     *  <code>accept()</code> with <code>setSoTimeout(x)</code>.  In
     *  this case, <code>accept()</code> either returns with an
     *  accepted connection within <code>x</code> milliseconds, or it
     *  raises an <code>InterruptedIOException</code>, in which case,
     *  we simply exit if <code>shutdown()</code> was called, or loop
     *  otherwise.
     *  
     *  <p>Intuitively, lower values of
     *  <code>AcceptPollingInterval</code> make this receiver more
     *  responsive to {@link #shutdown()}, with the disadvantage of
     *  wasting cycles when the receiver remains active without much
     *  incoming traffic.  High values have the opposite advantages
     *  and disadvantages.  
     *
     *  <p>A value of <code>0</code> means <em>infinite</em> timeout.
     *  This value is appropriate for platforms that break out of
     *  <code>accept()</code> when you close the server socket.  As
     *  far as I know, these are JDK-1.3 on sparc+Solaris, win32, and
     *  Mac OS X.
     *
     *  <p>I think that in some cases you can "activate" the special
     *  semantic of <code>close()</code> on platforms that don't
     *  normally exhibit that, by simply setting a timeout on the
     *  server socket.  This would allow us to use a very high value
     *  for <code>AcceptPollingInterval</code>.  I'll leave these
     *  esoteric methods to the extreme programmers...
     *
     *  <p>If you figured out a better way to reliably break out of
     *  <code>java.net.ServerSocket.accept()</code>, and if you are
     *  willing to share your ideas, please contact Antonio Carzaniga
     *  &lt;(firstname.lastname@usi.ch)&gt;.
     **/
    public static int		AcceptPollingInterval = 3000;

    /** create a receiver listening to the given port.
     *
     *  @param port_number must be a valid TCP port number, or it can
     *         be 0 in which case a random port is used
     *
     *  @exception if an I/O error occurs when opening the socket.
     *             typically, when the given port is already in use.
     **/
    public TCPPacketReceiver(int port_number) throws IOException {
	this(new ServerSocket(port_number));
    }

    /** create a receiver listening to the given port with a given
     *  maximum queue for TCP connections.
     *
     *  @param port_number must be a valid TCP port number, or it can
     *         be 0 in which case a random port is used
     *
     *  @exception if an I/O error occurs when opening the socket.
     *             typically, when the given port is already in use.
     **/
    public TCPPacketReceiver(int port_number, int qsize) throws IOException {
	this (new ServerSocket(port_number, qsize));
    }

    public TCPPacketReceiver(ServerSocket s) throws IOException,
						    UnknownHostException {
	port = s;
	//
	// this timout is necessary to make shutdown() work properly
	// under some platforms (e.g., jdk-1.3+linux)
	//
	if (AcceptPollingInterval > 0) 
	    port.setSoTimeout(AcceptPollingInterval);
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
	}
	port = null;
    }

    private Socket accept_connection() throws PacketReceiverException {
	    ServerSocket p;
	    //
	    // I use this variable to avoid a race condition that may
	    // occurr if someone calls shutdown() after port == null,
	    // and before port.accept().
	    //

	    while((p = port) != null) {
		try {
		    return p.accept();
		} catch (EOFException ex) {
		    //
		    // we simply ignore this exception and continue
		    // the loop as long as port != null
		    //
		} catch (InterruptedIOException ex) {
		    //
		    // we simply ignore this exception and continue
		    // the loop as long as port != null
		    //
		} catch (IOException ex) {
		    throw new PacketReceiverException(ex.toString());
		}
	    }
	    throw(new PacketReceiverClosed());
    }

    public int receive(byte[] buf) throws PacketReceiverException {
	try {
	    Socket sock = accept_connection();
	    java.io.InputStream input = sock.getInputStream();
	    
	    int offset = 0;
	    int res;
	    
	    try {
		while((res = input.read(buf, offset, buf.length - offset)) >= 0)
		    offset += res;
		sock.close();
	    } catch (Exception ex) {
		throw(new PacketReceiverException(ex.toString()));
	    }
	    return offset;
	} catch (java.io.IOException ex) {
	    //
	    // port closed.
	    //
	    throw new PacketReceiverClosed(ex);
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
}
