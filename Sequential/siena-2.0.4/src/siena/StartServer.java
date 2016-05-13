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

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;

import siena.comm.KAPacketReceiver;
import siena.comm.MultiPacketReceiver;
import siena.comm.PacketReceiver;
import siena.comm.TCPPacketReceiver;
import siena.comm.UDPPacketReceiver;

/**
 * a utility class that can be used to run a <code>HierarchicalDispatcher</code>
 * as a stand-alone Siena server.
 * 
 * <code>StartServer</code> accepts some command-line parameters to set
 * various options of the dispatcher (such as its listener port, its identity
 * etc.).
 * <p>
 * 
 * The complete syntax of the command-line options is:
 * <p>
 * 
 * <code>StartServer</code> [<code>-master</code> <em>address</em>] [<code>-id</code> <em>identity</em>] [<code>-host</code>
 *   <em>address</em>] [<code>-tcp</code> |
 * <code>-udp</code> | <code>-ka</code> ] [<code>-port</code> <em>port</em>] [<code>-monitor</code> <em>hostname</em>] [<code>-err</code>
 *   <code>off</code> |
 * <code>-</code> | <em>filename</em>] [<code>-log</code> <code>off</code> |
 * <code>-</code> | <em>filename</em>] [<code>-fail-delay</code> <em>millisec</em>] [<code>-fail-count</code> <em>num</em>] [<code>-output-threads</code> <em>num</em>] [<code>-store</code> <em>filename</em>] [<code>-store-timeout</code> <em>millisec</em>] [<code>-store-counter</code> <em>number</em>] [<code>-choke</code> <em>number</em>] [<code>-noSFF</code>]
 * 
 * <p>
 * <dl>
 * <dt><code>-master</code> <em>address</em>
 * <dd> sets the master server for this server
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
 * <dt><code>-tcp</code>
 * <dd> uses a {@link TCPPacketReceiver TCP receiver}. This is the default
 * receiver.
 * 
 * <dt><code>-udp</code>
 * <dd> uses a {@link UDPPacketReceiver UDP receiver}
 * 
 * <dt><code>-ka</code>
 * <dd> uses a {@link UDPPacketReceiver Keep-Alive receiver}.
 * 
 * <dt><code>-port</code> <em>port</em>
 * <dd> port number for the packet receiver of this Siena server
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
 * <dt><code>-noSFF</code>
 * <dd> turns off usage of Siena Fast Forwarding
 * 
 * <dt><code>-choke</code> <em>number</em>
 * <dd> sets the rate in milliseconds at which SFF table is updated
 * </dl>
 */
public class StartServer {

	private static final int R_TCP = 0;
	private static final int R_UDP = 1;
	private static final int R_KA = 2;

	static void printUsage() {
		System.err
				.println("usage: StartServer [options...]\noptions:\n\t[-master <address>]\n\t[-id identity]\n\t[-host address]\n\t[-port port]\n\t[-udp|-ka|-tcp]\n\t[-monitor hostname]\n\t[-log - | <filename>]\n\t[-err - | off | <filename>]\n\t[-fail-delay <millisec>]\n\t[-fail-count <number>]\n\t[-threads <number>]\n\t[-store <filename>]\n\t[-store-timeout <number>]\n\t[-store-count <number>]");
		System.exit(1);
	}

	public static void main(String argv[]) {
		try {
			String master = null;
			String store = null;
			long store_t = -1;
			int store_c = -1;
			int rtype = R_TCP;
			int port = -1;
			HierarchicalDispatcher siena = null;
			MultiPacketReceiver multiReceiver = new MultiPacketReceiver();
			String monitor = null;
			String identity = null;
			String host = null;
			PrintStream debugfile = null;
			int thread_count = -1;
			int max_failout = 2;
			long max_timeout = 5000;			

			for (int i = 0; i < argv.length; i++) {
				if (argv[i].equals("-master")) {
					if (++i >= argv.length)
						printUsage();
					master = argv[i];
				} else if (argv[i].equals("-store")) {
					if (++i >= argv.length)
						printUsage();
					store = argv[i];
				} else if (argv[i].equals("-port")) {
					if (++i >= argv.length)
						printUsage();
					try {
						port = Integer.parseInt(argv[i]);
					} catch (NumberFormatException ex) {
						System.err.println("StartServer: Invalid port number.");
						printUsage();
					}
				} else if (argv[i].equals("-store-count")) {
					if (++i >= argv.length)
						printUsage();
					try {
						store_c = Integer.parseInt(argv[i]);
					} catch (NumberFormatException ex) {
						System.err.println("StartServer: Invalid fail count.");
						printUsage();
					}
//				} else if (argv[i].equals("-choke")) {
//					if (++i >= argv.length)
//						printUsage();
//					try {
//						choke = Integer.parseInt(argv[i]);
//					} catch (NumberFormatException ex) {
//						System.err
//								.println("StartServer: Invalid choke period.");
//						printUsage();
//					}
				} else if (argv[i].equals("-store-timeout")) {
					if (++i >= argv.length)
						printUsage();
					try {
						store_t = Long.parseLong(argv[i]);
					} catch (NumberFormatException ex) {
						System.err.println("StartServer: Invalid fail count.");
						printUsage();
					}
				} else if (argv[i].equals("-fail-count")) {
					if (++i >= argv.length)
						printUsage();
					try {
						max_failout = Integer.parseInt(argv[i]);
					} catch (NumberFormatException ex) {
						System.err.println("StartServer: Invalid fail count.");
						printUsage();
					}
				} else if (argv[i].equals("-threads")) {
					if (++i >= argv.length)
						printUsage();
					try {
						thread_count = Integer.parseInt(argv[i]);
					} catch (NumberFormatException ex) {
						System.err
								.println("StartServer: Invalid thread count.");
						printUsage();
					}
				} else if (argv[i].equals("-fail-delay")) {
					if (++i >= argv.length)
						printUsage();
					try {
						max_timeout = Long.parseLong(argv[i]);
					} catch (NumberFormatException ex) {
						System.err.println("StartServer: Invalid fail delay.");
						printUsage();
					}
				} else if (argv[i].equals("-monitor")) {
					if (++i >= argv.length)
						printUsage();
					monitor = argv[i];
					Monitor.setAddress(InetAddress.getByName(monitor));
//				} else if (argv[i].equals("-noSFF")) {
//					sff = false;
				} else if (argv[i].equals("-tcp")) {
					rtype = R_TCP;
				} else if (argv[i].equals("-udp")) {
					rtype = R_UDP;
				} else if (argv[i].equals("-ka")) {
					rtype = R_KA;
				} else if (argv[i].equals("-id")) {
					if (++i >= argv.length)
						printUsage();
					identity = argv[i];
				} else if (argv[i].equals("-host")) {
					if (++i >= argv.length)
						printUsage();
					host = argv[i];
				} else if (argv[i].equals("-err")) {
					if (++i >= argv.length)
						printUsage();
					if (argv[i].equals("-")) {
						Logging.setErrorStream(System.out);
					} else if (argv[i].equals("off")) {
						Logging.setErrorStream(null);
					} else {
						Logging.setErrorStream(new PrintStream(
								new FileOutputStream(argv[i])));
					}
				} else if (argv[i].equals("-log")) {
					if (++i >= argv.length)
						printUsage();
					if (argv[i].equals("-")) {
						Logging.setLogStream(System.out);
					} else if (argv[i].equals("off")) {
						Logging.setLogStream(null);
					} else {
						Logging.setLogStream(new PrintStream(
								new FileOutputStream(argv[i])));
					}
				} else {
					printUsage();
				}
			}

			if (identity == null) {
				siena = new HierarchicalDispatcher();
			} else {
				siena = new HierarchicalDispatcher(identity);
			}
			siena.MaxFailedConnectionsNumber = max_failout;
			siena.MaxFailedConnectionsDuration = max_timeout;

			port = (port == -1) ? SENP.DEFAULT_PORT : port;
			PacketReceiver receiver;
			switch (rtype) {
			case R_TCP: {
				TCPPacketReceiver r;
				r = new TCPPacketReceiver(port);
				if (host != null)
					r.setHostName(host);
				receiver = r;
				break;
			}
			case R_UDP: {
				UDPPacketReceiver r;
				r = new UDPPacketReceiver(port);
				if (host != null)
					r.setHostName(host);
				receiver = r;
				break;
			}
			case R_KA: {
				KAPacketReceiver r;
				r = new KAPacketReceiver(port);
				if (host != null)
					r.setHostName(host);
				receiver = r;
				break;
			}
			default:
				receiver = null;
			}

//			siena.setChoke(choke);
//			if (sff) {
//				Logging.prlnlog(identity + " starting with SFF");
//			}
//			siena.sff = sff;

			multiReceiver.addDefaultReceiver(receiver, thread_count);	

			if (thread_count < 0) {
				siena.setReceiver(multiReceiver);
			} else {
				siena.setReceiver(multiReceiver, thread_count);
			}
			if (master != null)
				siena.setMaster(master);
			if (store != null)
				siena.initStore(store);
			if (store_t >= 0)
				siena.setStoreRefreshTimeout(store_t);
			if (store_c >= 0)
				siena.setStoreRefreshCounter(store_c);
			if (thread_count == 0)
				siena.run();
		} catch (Exception e) {
			System.err.println(e.toString());
			System.exit(1);
		}
	}
}
