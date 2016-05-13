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
import java.net.InetAddress;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

class SSLPacketSender extends KAPacketSender {
    static SocketFactory	ssl_factory = SSLSocketFactory.getDefault();

    String get_schema() { return SSLPacketReceiver.Schema; }
    int get_default_port() { return 1972; }

    Socket connect() throws IOException {
	return ssl_factory.createSocket(ip_address, port);
    }

    public SSLPacketSender(String h) 
	throws InvalidSenderException, java.net.UnknownHostException {
	super(h);
    }

    public static void setSSLSocketFactory(SSLSocketFactory f) {
	ssl_factory = f;
    }
}

