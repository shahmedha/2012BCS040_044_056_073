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
import java.util.HashMap;

/** generic packet sender factory.
 *
 *  This factory recognizes all the receiver's schemas defined by the
 *  core Siena implementation ("tcp" for {@link TCPPacketReceiver},
 *  "udp" for {@link UDPPacketReceiver}, "ka" for {@link
 *  KAPacketReceiver}, and "ssl" for {@link SSLPacketReceiver}).
 *
 *  <p>In addition to the <em>native</em> schemas, this factory allows
 *  new schemas to be registered with their specific factory (see
 *  {@link #registerFactory(String,PacketSenderFactory)}.
 **/
public class GenericSenderFactory implements PacketSenderFactory {
    private static HashMap<String,PacketSenderFactory> factories = new HashMap<String,PacketSenderFactory>();

    /** returns a new packet sender based on the external address
     *  (i.e., handler or url) of the intended receiver.
     *
     *  <p>This method processes the input handler as follows:
     *  <ol>
     *
     *  <li>it extracts the schema identifier from the complete handler
     *
     *  <li>it attempts to match the schema with one of Siena's
     *  "native" schemas, and to create a corresponding sender
     *
     *  <li>if the given schema does not match any native schema, then
     *  this method tries to find a registered factory for that schema
     *
     *  <li>if a matching registered schema is found, the remaining
     *  part of the handler (without the ":" character) is passed to
     *  the associated factory to create a sender object;
     *
     *  </ol>
     **/
    public PacketSender createPacketSender(String handler) 
	throws InvalidSenderException {
	int pos = handler.indexOf(":");
	if (pos < 0) throw (new InvalidSenderException("can't find schema"));
	String schema = handler.substring(0,pos);
	try {
	    if (schema.equals(TCPPacketReceiver.Schema)) {
		return new TCPPacketSender(handler.substring(pos+1, 
							     handler.length()));
	    } else if (schema.equals(KAPacketReceiver.Schema)) {
		return new KAPacketSender(handler.substring(pos+1, 
							    handler.length()));
	    } else if (schema.equals(KAZipPacketReceiver.Schema)) {
			return new KAZipPacketSender(handler.substring(pos+1, 
								    handler.length()));		    
	    } else if (schema.equals(UDPPacketReceiver.Schema)) {
		return new UDPPacketSender(handler.substring(pos + 1, 
							     handler.length()));
	    } else if (schema.equals(SSLPacketReceiver.Schema)) {
		return new SSLPacketSender(handler.substring(pos + 1, 
							     handler.length()));
	    } else if (schema.equals("senp")) {
		System.out.println("Warning: the \"senp\" schema is deprecated\nPlease consider using the new \"tcp\" schema");
		return new TCPPacketSender(handler.substring(pos+1, 
							     handler.length()));
	    } else if (schema.equals("udp+senp")) {
		System.out.println("Warning: the \"udp+senp\" schema is deprecated\nPlease consider using the new \"udp\" schema");
		return new UDPPacketSender(handler.substring(pos + 1, 
							     handler.length()));
	    } else if (schema.equals("ka+senp")) {
		System.out.println("Warning: the \"ka+senp\" schema is deprecated\nPlease consider using the new \"ka\" schema");
		return new KAPacketSender(handler.substring(pos+1, 
							    handler.length()));

	    } else { 
		PacketSenderFactory f;
		synchronized (factories) {
		    f = factories.get(schema);
		}
		if (f == null) {
		    throw (new InvalidSenderException("unknown schema: " 
						      + schema));
		} else {
		    return f.createPacketSender(handler);
		}

	    }
	} catch (IOException ex) {
	    throw new InvalidSenderException(ex.getMessage());
	}

    }

    /** extends this factory by registering new packet sender factories.
     *
     *  A <code>GenericSenderFactory</code> can be extended by
     *  registering new {@link PacketSenderFactory} implementations.
     *  Notice that <code>GenericSenderFactory</code> will delegate
     *  the instantiation of a specific PacketSender to a registered
     *  factory by passing it the handler string.  See {@link
     *  #createPacketSender(String)} for more details on how
     *  <code>GenericSenderFactory</code> processes handlers.
     *
     *  <p>Notice that the factory register is static, therefore
     *  registration and de-registration will affect every instance of
     *  this class.
     *
     *  @param schema schema name
     *
     *  @param f schema-specific factory 
     **/
    public static void registerFactory(String schema, PacketSenderFactory f) {
	synchronized(factories) {
	    factories.put(schema, f);
	}
    }

    /** removes a previously registered factory.
     *
     *  @see #registerFactory(String,PacketSenderFactory)
     **/
    public static void removeFactory(String schema) {
	synchronized(factories) {
	    factories.remove(schema);
	}
    }
}
