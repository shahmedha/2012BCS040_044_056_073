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

/** generic packet receiver factory.
 *
 *  This factory serves as a generic switch for {@link
 *  PacketReceiverFactory} implementations.
 **/
public class GenericReceiverFactory implements PacketReceiverFactory {
    private static HashMap<String,PacketReceiverFactory> factories = new HashMap<String,PacketReceiverFactory>();

    /** returns a new packet receiver based on an external string
     *  representation.
     *
     *  This factory handles string representations of the form:
     *  <em>schema</em><code>:</code><em>schema-specific handler</em>
     *  by dispatching the handler (including the schema name and the
     *  colon character) to another factory that has been registered
     *  for that schema.  If no factory is registered for that schema,
     *  this method throws an {@link InvalidReceiverException}.
     *
     *  @exception InvalidReceiverException when the handler is
     *             unknown or malformed
     **/
    public PacketReceiver createPacketReceiver(String handler) 
	throws InvalidReceiverException {
	int pos = handler.indexOf(":");
	if (pos < 0) throw (new InvalidReceiverException("can't find schema"));
	String schema = handler.substring(0,pos);
	PacketReceiverFactory f;
	synchronized(factories) {
	    f = factories.get(schema);
	}
	if (f == null) {
	    throw (new InvalidReceiverException("unknown schema: " + schema));
	} else {
	    return f.createPacketReceiver(handler);
	}
    }

    /** extends this factory by registering a new packet receiver factory.
     *
     *  A <code>GenericReceiverFactory</code> can be extended by
     *  registering new {@link PacketReceiverFactory} implementations.
     *  Notice that <code>GenericReceiverFactory</code> will delegate
     *  the instantiation of a specific PacketReceiver to a registered
     *  factory by passing if the handler string.  See {@link
     *  #createPacketReceiver(String)} for more details on how
     *  <code>GenericReceiverFactory</code> processes handlers.
     *
     *  <p>Notice that the factory register is static, therefore
     *  registration and de-registration will affect every instance of
     *  this class.
     *
     *  @param schema schema name
     *
     *  @param f schema-specific factory 
     **/
    public static void registerFactory(String schema, PacketReceiverFactory f) {
	synchronized(factories) {
	    factories.put(schema, f);
	}
    }

    /** removes a previously registered factory.
     *
     *  @see #registerFactory(String,PacketReceiverFactory)
     **/
    public static void removeFactory(String schema) {
	synchronized(factories) {
	    factories.remove(schema);
	}
    }
}
