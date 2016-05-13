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

/** creates PacketSenders based on the external address of their
 *  corresponding receivers.
 *
 *  Creates PacketSender objects based on the external representation
 *  (handler) of their intended receivers.  In other words, given the
 *  {@link PacketReceiver#address() address of a receiver}, it returns
 *  a PacketSender object that can be used to send packets to that
 *  receiver.
 **/
public interface PacketSenderFactory {
    /** creates a PacketSender object based on the external address of
     *  its intended receiver.
     *
     *  Creates a {@link PacketSender} object based on the external
     *  string representation (handler) of its intended receivers.
     *  Given the {@link PacketReceiver#address() address of a
     *  receiver}, returns a PacketSender object that can be used to
     *  send packets to that receiver.
     *  
     *  <p>Implementations may be used through a {@link
     *  GenericSenderFactory} (see {@link
     *  GenericSenderFactory#registerFactory(String,PacketSenderFactory)}
     *
     *  @param handler receiver's address
     **/
    public PacketSender createPacketSender(String handler) 
	throws InvalidSenderException;
}
