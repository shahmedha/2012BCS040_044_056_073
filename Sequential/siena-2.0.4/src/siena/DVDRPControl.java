//
//  This file is part of Siena, a wide-area event notification system.
//  See http://www.inf.usi.ch/carzaniga/siena/
//
//  Authors: Amir Malekpour 
//           Antonio Carzaniga (firstname.lastname@usi.ch)
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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import siena.SENPPacket;
import siena.comm.PacketSenderFactory;
import siena.comm.GenericSenderFactory;
import siena.comm.PacketSender;
import siena.comm.InvalidSenderException;

/**
 * a command-line utility class that can be used to control a
 * running {@link siena.DVDRPDispatcher}.
 * 
 * The complete syntax of the command-line options is:
 * <p>
 * 
 * <code>DVDRPControl</code> <em>router-address</em>
 * <em>command</em> [command options...]
 * 
 * <p> 
 *
 * <em>router-address</em> is the address of the router.  See
 * {@link siena.comm.PacketReceiver#address()} for details on the
 * format of router addresses.  <p> The following commands are
 * available: <dl>
 * 
 * <dt><code>connect</code> <em>&lt;neighbor-identity&gt;</em>
 * <em>&lt;neighbor-address&gt;</em> <em>[link cost]</em>
 * 
 * <dd>instruct the router to connect to the given neighbor router.
 * In this case, the router will add the neighbor to its tables and
 * share routing information with it.  An optional link cost can be
 * specified.  The default cost is 1.
 * <p>
 *
 * If the router is already connected to the given neighbor
 * (identity), then this command can be used to modify the link-level
 * address of the neighbor and and/or the corresponding link cost.
 * 
 * <dt><code>disconnect</code> <em>neightbor-identity</em>
 * <dd>instruct the router to disconnect to the given neighbor router.
 * This means that the router will remove the given neighbor from its
 * tables.
 * </dl>
 */
public class DVDRPControl {
    private static void connect_command(PacketSender ps, String [] argv) {
	// router-addr connect <neighbor_id> <neighbor_handler> [cost]
	String neighbor_id;
	String neighbor_address;
	int cost = ps.getCost();

	switch (argv.length) {
	case 4: 
	    neighbor_id = argv[2];
	    neighbor_address = argv[3];
	    cost = ps.getCost();
	    break;
	case 5: 
	    neighbor_id = argv[2];
	    neighbor_address = argv[3];
	    try {
		cost = Integer.parseInt(argv[4]);
	    } catch (NumberFormatException ex) {
		System.err.println("Bad cost specification: " + argv[4]);
		return;
	    }
	    break;
	default:
	    System.err.println("Error: too few or too many arguments for connect.");
	    print_usage();
	    return;
	}

	SENPPacket pack = SENPPacket.allocate();
	pack.id = neighbor_id.getBytes();
	pack.handler = neighbor_address.getBytes();
	pack.method = SENP.CNF;
	pack.ttl = SENP.DefaultTtl;
	pack.cost = cost;
	try {
	    ps.send(pack.buf, pack.encode());
	    System.out.println("Command was sent.");
	} catch (Exception e) {
	    System.err.println("Error sending 'connect' command to " 
			       + argv[0] + ": " + e);
	}
	SENPPacket.recycle(pack);
    }

    private static void disconnect_command(PacketSender ps, String [] argv) {
	// router-addr disconnect <neighbor_id>
	String neighbor_id;

	switch (argv.length) {
	case 3: 
	    neighbor_id = argv[2];
	    break;
	default:
	    System.err.println("Error: too few or too many arguments for disconnect.");
	    print_usage();
	    return;
	}

	SENPPacket pack = SENPPacket.allocate();
	pack.id = neighbor_id.getBytes();
	pack.handler = null;
	pack.method = SENP.CNF;
	pack.ttl = SENP.DefaultTtl;
	try {
	    ps.send(pack.buf, pack.encode());
	    System.out.println("Command was sent.");
	} catch (Exception e) {
	    System.err.println("Error sending 'disconnect' command to " 
			       + argv[0] + ": " + e);
	}
	SENPPacket.recycle(pack);
    }

    static void print_usage() {
	System.err.println("usage:\n" +
				"  DVDRPControl <router-address> <command> [options...]\n" +
				"  DVDRPControl -f <config-file>\n" + 
				"  commands:\n" +
			    "  \tconnect <identity> <handler> [cost]\n" +
				"  \tdisconnect <identity>\n" +
				"  When you can specify a configuration file there must be one command per line.");			
    }
    
    public static void process_command(String tokens[]) {
    	if (tokens.length < 2) {
    	    print_usage();
    	    System.exit(1);
    	}
    	
    	PacketSenderFactory psf = new GenericSenderFactory();
    	PacketSender packet_sender = null;
    	try {
    	    packet_sender = psf.createPacketSender(tokens[0]);
    	} catch (InvalidSenderException ex) {
    	    System.err.println("invalid router address: " + tokens[0]);
    	    System.exit(2);
    	}
    	String command = tokens[1];

    	if (command.equals("connect")) {
    	    connect_command(packet_sender, tokens);
    	} else if (command.equals("disconnect")) {
    	    disconnect_command(packet_sender, tokens);
    	} else {
    	    print_usage();
    	    System.exit(1);
    	}
    }


    public static void main(String argv[]) {
		if (argv.length < 2) {
		    print_usage();
		    System.exit(1);
		}
		
		if (argv[0].equals("-f") && argv.length > 1) {
			process_config_file(argv);
		} else {
		    process_command(argv);
		}
    }

	private static void process_config_file(String[] argv) {
		try{
			FileInputStream fstream = new FileInputStream(argv[1]);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				strLine = strLine.trim();
				if(strLine.equals("") || strLine.charAt(0) == '#') // Lines beginning with '#' are skipped
					continue;
				String[] tokens  = strLine.split("\\s");
				process_command(tokens);
			}
			in.close();
		} catch (Exception e) {
			System.err.println("Error reading configuration file " + e.getMessage());
		}
	}
}
