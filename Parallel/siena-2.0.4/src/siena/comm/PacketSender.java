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

/** packet sender.
 *
 *  Abstraction of a primitive communication mechanism for sending
 *  packets.  Packets are chunks of bytes.  A packet sender sends
 *  packets to a specific destination.  Every implementation of Siena
 *  uses one or more packet senders to communicate with remote clients
 *  and servers.
 *
 *  <p>Packet senders are constructed by {@link PacketSenderFactory}
 *  using the URI of the corresponding receiver as a parameter.
 *
 *  <p>This version of Siena includes only a simple implementation on
 *  top of TCP/IP.  Future versions will include support for
 *  encapsulation into other protocols, such as SMTP and HTTP.
 *
 *  @see PacketReceiver
 **/
public interface PacketSender {
    /** sends a packet. **/
    public void		send(byte[] packet) throws PacketSenderException;

    /** sends a packet. **/
    public void		send(byte[] packet, int len) 
	throws PacketSenderException;

    /** sends a packet. **/
    public void		send(byte[] packet, int offset, int len) 
	throws PacketSenderException;

    /** closes this sender. **/
    public void		shutdown() throws PacketSenderException;
    
    /** returns cost of packet transmissions 
     **/
     public int getCost();
}
