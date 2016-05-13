//
//  This file is part of Siena, a wide-area event notification system.
//  See http://www.inf.usi.ch/carzaniga/siena/
//  Authors:
//	Antonio Carzaniga (firstname.lastname@usi.ch)
//	Amir Malekpour (malekpoa@usi.ch)
//  See the file AUTHORS for full details. 
//
//  Copyright (C) 2011 Antonio Carzaniga
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

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import siena.comm.KAPacketReceiver;
import siena.comm.KAZipPacketReceiver;
import siena.comm.MultiPacketReceiver;
import siena.comm.PacketReceiver;
import siena.comm.TCPPacketReceiver;
import siena.comm.UDPPacketReceiver;
import siena.dvdrp.StaticBufferQueue;

class InvalidArguments extends Exception {
    static final long serialVersionUID = 1L;
    InvalidArguments(String e) {
	super(e);
    }
}

/**
 * a utility class that can be used to run a <code>DVDRPDispatcher</code> as a
 * stand-alone Siena server.
 * 
 * <code>StartDVDRPServer</code> accepts some command-line parameters to set
 * various options of the dispatcher (such as its listener port, its identity
 * etc.).
 * <p>
 * 
 * The complete syntax of the command-line options is:
 * <p>
 * 
 * <code>StartDVDRPServer</code> [<code>-id</code> <em>identity</em>]
 * [<code>-host</code> <em>address</em>] | [<code>-receiver</code>
 * <em>receiver-spec</em> | [<code>-monitor</code> <em>hostname</em>]
 * [<code>-err</code> <code>off</code> | <code>-</code> |
 * <em>filename</em>] [<code>-log</code> <code>off</code> |
 * <code>-</code> | <em>filename</em>] [<code>-fail-delay</code>
 * <em>millisec</em>] [<code>-fail-count</code> <em>num</em>]
 * [<code>-store</code> <em>filename</em>]
 * [<code>-store-timeout</code> <em>millisec</em>]
 * [<code>-store-counter</code> <em>number</em>]
 * [<code>-heartbeat</code> <em>millisec</em>]
 * [<code>-do-discovery</code>] [<code>-log-level
 * DEBUG|INFO|WARN|ERROR|FATAL</code>] [<code>-choke
 * <em>millisec</em></code>] [<code>-no-sff </code>]
 * [<code>-queue-packets</code> <em>number of initialized
 * packets</em>]
 * 
 * <p>
 * <dl>
 * 
 * <dt><code>-id</code> <em>identity</em>
 * <dd> explicitly sets the identity of this server
 * 
 * <dt><code>-host</code> <em>address</em>
 * <dd> explicitly sets the host address for the receiver of this server. This
 * option is provided in case the JVM can not reliably determine its own host
 * address (see {@link TCPPacketReceiver#setHostName(String)},
 * {@link UDPPacketReceiver#setHostName(String)}, and {@link
 * KAPacketReceiver#setHostName(String)})
 * 
 * <dt><code>-receiver <em>receiver specification</em></code>

 * <dd> creates and adds a packet receiver to this router.  The router
 * may have multiple packet receivers.  The receiver is created based
 * on the receiver specification, which is a string with the following
 * format:
 * <em>type</em><code>:</code>[<em>hostname</em>]<code>:</code>[<em>port</em>][<code>:</code><em>threads</em>]
 *
 * where <em>type</em> can be either <code>tcp</code> for a 
 * {@link TCPPacketReceiver TCP receiver}, <code>udp</code> for a 
 * {@link UDPPacketReceiver UDP receiver}, <code>ka</code> for a
 * {@link KAPacketReceiver Keep-Alive receiver}, and <code>kazip</code> for a
 * {@link KAZipPacketReceiver Keep-Alive Zip receiver}.
 * 
 * <dt><code>-monitor</code> <em>hostname</em>
 * <dd>
 * 
 * <dt><code>-err</code> <code>off</code> | <code>-</code> |
 * <em>filename</em>
 * <dd> redirects the error stream. <code>-</code> means standard output.
 * <code>off</code> turns off error reporting. The default is to send error
 * messages to <code>System.err</code>.
 * 
 * <dt><code>-log</code> <code>off</code> | <code>-</code> |
 * <em>filename</em>
 * <dd> redirects the logging stream. <code>-</code> means standard output.
 * <code>off</code> turns off error reporting. By default logging is turned
 * off.
 * 
 * <dt><code>-fail-delay</code> <em>millisec</em>
 * <dd> sets {@link HierarchicalDispatcher#MaxFailedConnectionsDuration}
 * 
 * <dt><code>-fail-count</code> <em>number</em>
 * <dd> sets {@link HierarchicalDispatcher#MaxFailedConnectionsNumber}
 * 
 * <dt><code>-threads</code> <em>number</em>
 * <dd> sets {@link HierarchicalDispatcher#DefaultThreadCount}
 * 
 * <dt><code>-store</code> <em>filename</em>
 * <dd> activates the subscription storage system with the given file. See
 * {@link HierarchicalDispatcher#initStore(String)}
 * 
 * <dt><code>-store-timeout</code> <em>millisec</em>
 * <dd> calls {@link HierarchicalDispatcher#setStoreRefreshTimeout(long)}
 * 
 * <dt><code>-store-counter</code> <em>number</em>
 * <dd> calls {@link HierarchicalDispatcher#setStoreRefreshCounter(int)}
 * 
 * <dt><code>-heartbeat</code> <em>millisec</em>
 * <dd> sets the heartbeat time to keep communication alive between neighbor
 * servers
 * 
 * <dt><code>-choke</code> <em>number</em>
 * <dd> sets the rate in milliseconds at which distance vector updates are
 * published
 * 
 * <dt><code>-log-level </code> <em>DEBUG|INFO|WARN|ERROR|FATAL</em>
 * <dd> defines threshold of logged messages (a la log4j)
 * 
 * <dt><code>-no-sff</code>
 * <dd> turns off usage of Siena Fast Forwarding
 *  
 * <dt><code>-queue-packets <em> number </em></code>
 * <dd> sets the max number os packets to initialize at startup
 * 
 * </dl>
 */
public class StartDVDRPServer {
    private static String identity = null;
    private static final int DEFAULT_NUMBER_OF_RECEIVER_THREADS = 5;

    static void printUsage() {
	System.err.println("usage: StartServer [options...]\n" +
			   "options:\n" +
			   "\t[-id identity]\n" +
			   "\t[-receiver transport:host:port:threadcount] where\n" +
			   "\t\ttransport is one of [tcp|udp|ka]\n" +
			   "\t\tthreadcount is the number of receiving threads and is optional\n" +
			   "\t[-monitor hostname]\n" +
			   "\t[-log - | <filename>]\n" +
			   "\t[-err - | off | <filename>]\n" +
			   "\t[-fail-delay <millisec>]\n" +
			   "\t[-fail-count <number>]\n" +
			   "\t[-threads <number>]\n" +
			   "\t[-store <filename>]\n" +
			   "\t[-store-timeout <number>]\n" +
			   "\t[-store-count <number>]"
			   + "\n" +
			   "\t[-heartbeat <millisec>]\n" +
			   "\n" +
			   "\t[-doDiscovery]\n" +
			   "\t[-log-level [DEBUG|INFO|WARN|ERROR|FATAL]] \n" +
			   "\t[-queuePackets <number of packets>]");
    }

    public static void main(String argv[]) {
	try {
	    // System.out.println("Initializing server");
	    String store = null;
	    long store_t = -1;
	    int store_c = -1;
	    DVDRPDispatcher siena = null;
	    MultiPacketReceiver multiReceiver = null;
	    String monitor = null;
	    int thread_count = -1;
	    int max_failout = 2;
	    long max_timeout = 5000;
	    long heartbeat = DVDRPDispatcher.DEFAULT_DVDISPATCH_PERIOD;
	    int choke = DVHeartbeat.DEFAULT_CHOKE_PERIOD;
	    boolean sff = true;
	    boolean discovery = false;
	    int severity = Logging.INFO; // default logging severity
	    // threshold
	    int i;

	    for (i = 0; i < argv.length; i++) {
		if (argv[i].equals("-store")) {
		    if (++i >= argv.length) 
			throw (new InvalidArguments(""));

		    store = argv[i];
		} else if (argv[i].equals("-store-count")) {
		    if (++i >= argv.length) 
			throw (new InvalidArguments("missing store-count value"));
		    try {
			store_c = Integer.parseInt(argv[i]);
		    } catch (NumberFormatException ex) {
			throw (new InvalidArguments("invalid store-count value"));
		    }
		} else if (argv[i].equals("-heartbeat")) {
		    if (++i >= argv.length) 
			throw (new InvalidArguments("missing hearbeat value"));

		    try {
			heartbeat = Long.parseLong(argv[i]);
		    } catch (NumberFormatException ex) {
			throw (new InvalidArguments("invalid heartbeat value"));
		    }
		} else if (argv[i].equals("-choke")) {
		    if (++i >= argv.length) {
			printUsage();
			throw (new InvalidArguments("missing choke value"));
		    }
		    try {
			choke = Integer.parseInt(argv[i]);
		    } catch (NumberFormatException ex) {
			throw (new InvalidArguments("invalid choke value"));
		    }

		} else if (argv[i].equals("-store-timeout")) {
		    if (++i >= argv.length) 
			throw (new InvalidArguments("missing store-timeout value"));
		    try {
			store_t = Long.parseLong(argv[i]);
		    } catch (NumberFormatException ex) {
			throw (new InvalidArguments("invalid store-timeout value"));
		    }
		} else if (argv[i].equals("-fail-count")) {
		    if (++i >= argv.length) 
			throw (new InvalidArguments("missing fail-count value"));
		    try {
			max_failout = Integer.parseInt(argv[i]);
		    } catch (NumberFormatException ex) {
			throw (new InvalidArguments("invalid fail-count value"));
		    }
		} else if (argv[i].equals("-threads")) {
		    if (++i >= argv.length) {
			throw (new InvalidArguments("missing threads value"));
		    }
		    try {
			thread_count = Integer.parseInt(argv[i]);
		    } catch (NumberFormatException ex) {
			throw (new InvalidArguments("invalid threads value"));
		    }
		} else if (argv[i].equals("-fail-delay")) {
		    if (++i >= argv.length) {
			printUsage();
			throw (new InvalidArguments("missing fail-delay value"));
		    }
		    try {
			max_timeout = Long.parseLong(argv[i]);
		    } catch (NumberFormatException ex) {
			throw (new InvalidArguments("invalid fail-delay value"));
		    }
		} else if (argv[i].equals("-monitor")) {
		    if (++i >= argv.length) {
			throw (new InvalidArguments("missing monitor value"));
		    }
		    monitor = argv[i];
		    Monitor.setAddress(InetAddress.getByName(monitor));
		} else if (argv[i].equals("-id")) {
		    if (++i >= argv.length) {
			throw (new InvalidArguments("missing id value"));
		    }
		    identity = argv[i];
		} else if (argv[i].equals("-no-sff")) {
		    sff = false;
		} else if (argv[i].equals("-err")) {
		    if (++i >= argv.length) 
			throw (new InvalidArguments("missing error filename"));
		    if (argv[i].equals("-")) {
			Logging.setErrorStream(System.out);
		    } else if (argv[i].equals("off")) {
			Logging.setErrorStream(null);
		    } else {
			Logging.setErrorStream(new PrintStream(new FileOutputStream(argv[i])));
		    }
		} else if (argv[i].equals("-log")) {
		    if (++i >= argv.length) 
			throw (new InvalidArguments("missing log filename"));
		    if (argv[i].equals("-")) {
			Logging.setLogStream(System.out);
		    } else if (argv[i].equals("off")) {
			Logging.setLogStream(null);
		    } else {
			Logging.setLogStream(new PrintStream(new FileOutputStream(argv[i])));
		    }
		} else if (argv[i].equals("-queue-packets")) {
		    if (++i >= argv.length) 
			throw (new InvalidArguments("missing queue-packets value"));
		    try {
			System.setProperty(StaticBufferQueue.BUFFER_POOL_SIZE,
					   argv[i]);
		    } catch (NumberFormatException ex) {
			throw (new InvalidArguments("invalid queue-packets value"));
		    }
		} else if (argv[i].equals("-log-level")) {
		    if (++i >= argv.length) 
			throw (new InvalidArguments("missing log-level value"));
		    if (argv[i].equals("DEBUG")) {
			severity = Logging.DEBUG;
		    } else if (argv[i].equals("INFO")) {
			severity = Logging.INFO;
		    } else if (argv[i].equals("WARN")) {
			severity = Logging.WARN;
		    } else if (argv[i].equals("ERROR")) {
			severity = Logging.ERROR;
		    } else if (argv[i].equals("FATAL")) {
			severity = Logging.FATAL;
		    } else {
			throw (new InvalidArguments("invalid log-level value: " +argv[i]));
		    }
		    Logging.setSeverity(severity);
		} else if (argv[i].equals("-do-discovery")) {
		    discovery = true;
		} else if (argv[i].equals("-receiver")) {
		    if (++i >= argv.length)
			throw (new InvalidArguments("expecting receiver specification"));
		    if (multiReceiver == null)
			multiReceiver = new MultiPacketReceiver();
		    add_receiver(multiReceiver, argv[i]);
		} else if (argv[i].equals("-help") || argv[i].equals("-h")) {
		    printUsage();
		    return;
		} else {
			throw (new InvalidArguments("unknown argument: " + argv[i]));
		}
	    }

	    if (identity == null) {
		siena = new DVDRPDispatcher();
	    } else {
		siena = new DVDRPDispatcher(identity);
	    }
	    siena.MaxFailedConnectionsNumber = max_failout;
	    siena.MaxFailedConnectionsDuration = max_timeout;

	    if (store != null)
		siena.initStore(store);
	    if (store_t >= 0)
		siena.setStoreRefreshTimeout(store_t);
	    if (store_c >= 0)
		siena.setStoreRefreshCounter(store_c);
	    if (heartbeat != DVDRPDispatcher.DEFAULT_DVDISPATCH_PERIOD)
		siena.setHeartbeat(heartbeat);
	    siena.chokePeriod = choke;
			
	    if (discovery)
		siena.doDiscovery();

	    if (thread_count < 0) {
		siena.setReceiver(multiReceiver);
	    } else {
		siena.setReceiver(multiReceiver, thread_count);
	    }

	    if (sff) {
		Logging.prlnlog(new String(siena.my_identity) + " starting with SFF", Logging.DEBUG);
	    }
	    siena.sff = sff;
	    siena.startHeartbeat();
	    if (thread_count == 0)
		siena.run();
	} catch (InvalidArguments e) {
	    System.err.println("invalid arguments: " + e.toString());
	    System.exit(1);
	} catch (Exception e) {
	    System.err.println(e.toString());
	    System.exit(1);
	}
    }

    private static void add_receiver(MultiPacketReceiver rcv, String recv_spec) 
	throws InvalidArguments {
	String [] specv = recv_spec.split(":");
	if (specv.length < 3) 
	    throw (new InvalidArguments("invalid receiver specification: " + recv_spec));
	int threads;
	if (specv.length > 3) {
	    try {
		threads = Integer.parseInt(specv[3]);
	    } catch (NumberFormatException ex) {
		throw (new InvalidArguments("invalid thread count in receiver specification: " + recv_spec));
	    }
	} else {
	    threads = StartDVDRPServer.DEFAULT_NUMBER_OF_RECEIVER_THREADS;
	}
	int port = SENP.DEFAULT_PORT;
	try {
	    port = Integer.parseInt(specv[2]);
	} catch (NumberFormatException ex) {
	    throw (new InvalidArguments("invalid port number in receiver specification: " + recv_spec));
	}

	PacketReceiver receiver = null;
	try {
	    if (specv[0].equals("tcp")) {
		TCPPacketReceiver r = new TCPPacketReceiver(port);
		if (!specv[1].equals("")) 
		    r.setHostName(specv[1]);
		receiver = r;
	    } else if (specv[0].equals("udp")) {
		UDPPacketReceiver r = new UDPPacketReceiver(port);
		if (!specv[1].equals("")) 
		    r.setHostName(specv[1]);
		receiver = r;
	    } else if (specv[0].equals("ka")) {
		KAPacketReceiver r = new KAPacketReceiver(port);
		if (!specv[1].equals("")) 
		    r.setHostName(specv[1]);
		receiver = r;
	    } else if (specv[0].equals("kazip")) {
		KAZipPacketReceiver r = new KAZipPacketReceiver(port);
		if (!specv[1].equals("")) 
		    r.setHostName(specv[1]);
		receiver = r;
	    } else {
		throw (new InvalidArguments("invalid receiver type in receiver specification: " + recv_spec));
	    }
	} catch (java.io.IOException ex) {
	    throw (new InvalidArguments("could not create receiver: " + ex));
	}
	if (!rcv.hasDefaultReceiver()) {
	    rcv.addDefaultReceiver(receiver, threads);
	} else {
	    rcv.addReceiver(receiver, threads);
	}
    }
}

