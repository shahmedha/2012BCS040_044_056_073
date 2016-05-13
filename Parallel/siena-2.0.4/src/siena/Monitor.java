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
package siena;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;

/** Monitor is a logging facility that can be used in conjunction with
 *  the <a href="http://www.inf.usi.ch/carzaniga/siena/smon/">Siena
 *  Monitor</a>.
 **/
public class Monitor {
    private static final int		CmdLineMaxLen	= 1024;
    private static final int		DefaultPort	= 1996;

    private static DatagramSocket	socket		= null; 
    private static DatagramPacket	packet		= null; 
    private static byte[]		data		= null;
    private static int			len;

    private static boolean initialize() {
	String hostname;
	try {
	    hostname = System.getProperty("SienaMonitor");
	} catch (SecurityException ex) {
	    return false;
	}
	if (hostname == null) 
	    return false;
	InetAddress address;
	int port;
	String portnumber;
	try {
	    portnumber = System.getProperty("SienaMonitorPort");
	} catch (SecurityException ex) { 
	    portnumber = null;
	}

	try {
	    address = InetAddress.getByName(hostname);
	    if (portnumber != null) {
		port = Integer.parseInt(portnumber);
	    } else {
		port = DefaultPort;
	    }
	    setAddress(address, port);
	    return active;
	} catch (IOException ex) {
	    Logging.prlnerr("error initializing monitor for hostname "
			    + hostname);
	    Logging.prlnerr(ex.toString());
	    return false;
	}
    }
    /** sets the address of the Siena  Monitor.
     *
     *  uses the default port.
     **/
    public static void setAddress(InetAddress address) {
	setAddress(address, DefaultPort);
    }

    /** sets address and port for the Siena Monitor
     **/
    public static void setAddress(InetAddress address, int port) {
	try {
	    data = new byte[CmdLineMaxLen];
	    len = 0;
	    socket = new DatagramSocket();
	    packet = new DatagramPacket(data, len, address, port);
	    active = true;
	} catch (IOException ex) {
	    Logging.prlnerr("error initializing monitor for address " 
			    + address);
	    Logging.prlnerr(ex.toString());
	    active = false;
	}
    }

    private static boolean active = initialize();

    private static final byte	SEP			= 0x20;

    private static final byte[]	Add 
    = { 0x61, 0x64, 0x64, 0x5f, 0x6e, 0x6f, 0x64, 0x65 };
    private static final byte[]	Remove 
    = { 0x72, 0x65, 0x6d, 0x6f, 0x76, 0x65, 0x5f, 0x6e, 0x6f, 0x64, 0x65 };
    private static final byte[]	Connect 
    = { 0x63, 0x6f, 0x6e, 0x6e, 0x65, 0x63, 0x74 };
    private static final byte[]  Disconnect 
    = { 0x64, 0x69, 0x73, 0x63, 0x6f, 0x6e, 0x6e, 0x65, 0x63, 0x74 };
    private static final byte[]  Notify 
    = { 0x6e, 0x6f, 0x74, 0x69, 0x66, 0x79 };
    private static final byte[]  Subscribe 
    = { 0x73, 0x75, 0x62, 0x73, 0x63, 0x72, 0x69, 0x62, 0x65 };
    private static final byte[]  Unsubscribe 
    = { 0x75, 0x6e, 0x73, 0x75, 0x62, 0x73, 0x63, 0x72, 0x69, 0x62, 0x65 };

    /** a node representing a Siena server **/
    public static final byte[] SienaNode 
    = { 0x73, 0x69, 0x65, 0x6e, 0x61 };
    /** a node representing a thin client **/
    public static final byte[] ThinClientNode 
    = { 0x74, 0x68, 0x69, 0x6e, 0x63, 0x6c, 0x69, 0x65, 0x6e, 0x74 };
    /** a node representing a generic object **/
    public static final byte[] ObjectNode 
    = { 0x6f, 0x62, 0x6a, 0x65, 0x63, 0x74 };
    /** a smiley face **/
    public static final byte[] AntoNode 
    = { 0x61, 0x6e, 0x74, 0x6f };
    //
    // to be continued with more icons ...work in progress...
    //

    private static void shipout() {
	try {
	    packet.setLength(len);
	    socket.send(packet);
	    len = 0;
	} catch (IOException ex) {
	    Logging.prlnerr("error sending log message to monitor: "
			    + socket.getInetAddress() 
			    + ":" + socket.getPort());
	    Logging.prlnerr(ex.toString());
	}
    }

    private static boolean append(byte[] x) {
	int i = 0; 
	while(i < x.length) {
	    if (len == CmdLineMaxLen) return false;
	    data[len++] = x[i++];
	}
	if (len == CmdLineMaxLen) return false;
	data[len++] = SEP;
	return true;
    }

    /** Signal a notification to the monitor.
     *
     * @param sender id of the sender 
     * @param receiver id of the receiver
     **/
    synchronized public static void notify(byte[] sender, byte[] receiver) {
	if (!active) return;
	if (!append(Notify)) return;
	if (!append(sender)) return;
	if (!append(receiver)) return;
	shipout();
    }

    /** Signal a subscription to the monitor.
     *
     * @param sender id of the subscriber 
     * @param receiver id of the receiver node
     **/
    synchronized public static void subscribe(byte[] sender, byte[] receiver) {
	if (!active) return;
	if (!append(Subscribe)) return;
	if (!append(sender)) return;
	if (!append(receiver)) return;
	shipout();
    }

    /** Signal an unsubscription to the monitor.
     *
     * @param sender id of the subscriber 
     * @param receiver id of the receiver node
     **/
    synchronized public static void unsubscribe(byte[] sender, 
						byte[] receiver) {
	if (!active) return;
	if (!append(Unsubscribe)) return;
	if (!append(sender)) return;
	if (!append(receiver)) return;
	shipout();
    }

    /** Signal a connection to the monitor.
     *
     * @param n1 id of the first node 
     * @param n2 id of the second node
     **/
    synchronized public static void connect(byte[] n1, byte[] n2) {
	if (!active) return;
	if (!append(Connect)) return;
	if (!append(n1)) return;
	if (!append(n2)) return;
	shipout();
    }

    /** Signal a disconnection to the monitor.
     *
     * @param n1 id of the first node 
     * @param n2 id of the second node
     **/
    synchronized public static void disconnect(byte[] n1, byte[] n2) {
	if (!active) return;
	if (!append(Disconnect)) return;
	if (!append(n1)) return;
	if (!append(n2)) return;
	shipout();
    }

    /** Signal a new "node" to the monitor.
     *
     * @param node id of the new node 
     * @param ntype type of the new node
     **/
    synchronized public static void add_node(byte[] node, byte[] ntype) {
	if (!active) return;
	if (!append(Add)) return;
	if (!append(node)) return;
	if (!append(ntype)) return;
	shipout();
    }

    /** Signal the creation of a a new node to the monitor
     *
     *  uses the Siena server icond, by default
     *
     * @param node id of the new node 
     **/
    public static void add_node(byte[] node) {
	if (!active) return;
	if (!append(Add)) return;
	if (!append(node)) return;
	shipout();
    }

    /** Signal the removal of a a new node
     *
     * @param node id of the removed node 
     **/
    public static void remove_node(byte[] node) {
	if (!active) return;
	if (!append(Remove)) return;
	if (!append(node)) return;
	shipout();
    }
}




