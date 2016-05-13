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
import java.net.Socket;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

/** receives packets through an SSL connection.
 *
 *  Communicates through SSL connections.  This receiver uses the SSL
 *  layer provided by the <a
 *  href="http://java.sun.com/products/jsse/">Java Secure Socket
 *  Extension</a> (JSSE) v.1.0.2.
 *
 *  <p>This class uses the default settings of your JSSE installation.
 *  Specific configurations are possible by {@link
 *  #setServerSocketFactory(SSLServerSocketFactory) setting the socket
 *  factory}.  You should refer to the JSSE documentation to install
 *  and configure your JSSE layer.
 **/
public class SSLPacketReceiver extends KAPacketReceiver {
    public static final String	Schema = "ssl";

    static ServerSocketFactory	ssl_factory 
	= SSLServerSocketFactory.getDefault();

    Socket accept_connection() throws IOException {
	Socket s = port.accept();
	s.setReceiveBufferSize(65535); // I made this up...
	return s;
    }

    /** external address of this packet receiver.
     *
     *  Uses the following schema syntax:<br>
     *
     *  <code>ssl:</code><em>host</em><code>:</code><em>port</em>
     * 
     *  @see PacketReceiver#address()
     **/
    public byte[] address() {
	return my_address;
    }

    synchronized public void setHostName(String hostname) {
	my_address = (Schema + Separator + hostname + Separator 
		      + Integer.toString(port.getLocalPort())).getBytes();
    }

    public SSLPacketReceiver(SSLServerSocket s) throws IOException {
	super(s);
    }
    
    public SSLPacketReceiver(int pnumber) throws IOException {
	super(ssl_factory.createServerSocket(pnumber));
    }
    
    public SSLPacketReceiver(int pnumber, int qsize) throws IOException {
	super(ssl_factory.createServerSocket(pnumber,qsize));
    }

    public String[] getEnabledCipherSuites() {
	return ((SSLServerSocket)port).getEnabledCipherSuites();
    }

    public boolean getEnableSessionCreation() {
	return ((SSLServerSocket)port).getEnableSessionCreation();
    }

    public boolean getNeedClientAuth() {
	return ((SSLServerSocket)port).getNeedClientAuth();
    }

    public String[] getSupportedCipherSuites() {
	return ((SSLServerSocket)port).getSupportedCipherSuites();
    }

    public boolean getUseClientMode() {
	return ((SSLServerSocket)port).getUseClientMode();
    }

    public void setEnabledCipherSuites(String[] suites) {
	((SSLServerSocket)port).setEnabledCipherSuites(suites);
    }

    public void setEnableSessionCreation(boolean flag) {
	((SSLServerSocket)port).setEnableSessionCreation(flag);
    }

    public void setNeedClientAuth(boolean flag) {
	((SSLServerSocket)port).setNeedClientAuth(flag);
    }

    public void setUseClientMode(boolean flag) {
	((SSLServerSocket)port).setUseClientMode(flag);
    }

    /** allows user-defined SSL socket fatories.
     *
     *  This method can be used to customize the SSL layer.  Please,
     *  refer to the JSSE documentation for more information on SSL
     *  configuration methods.
     **/
    static public void setServerSocketFactory(SSLServerSocketFactory s) {
	ssl_factory = s;
    }
}
