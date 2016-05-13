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

/** abstract packet receiver.
 *
 *  <p>Encapsulates a passive acceptor of packets.  This acceptor is
 *  <em>passive</em> in the sense that it uses the caller's thread to
 *  accept and assemble the incoming packet.
 *  <code>PacketReceiver</code>s, together with their corresponding
 *  <code>PacketSender</code>s, form the communication layer
 *  underneath the distributed network of Siena components.
 *
 *  <p>The implementations of Siena (see {@link
 *  siena.HierarchicalDispatcher} and {@link siena.ThinClient}) use a
 *  <code>PacketReceiver</code> as their acceptor of external
 *  subscriptions, notifications, etc.
 *
 *  @see PacketSender
 **/
public interface PacketReceiver {

    /** external address for this receiver.
     *
     *  An external address must allow other applications to contact
     *  this receiver.  Every implementation of this interface must
     *  define an address format that is compatible (i.e., that does
     *  not conflict) with every other implementation.  Notice also
     *  that every implementation of a <code>PacketReceiver</code>
     *  must have a corresponding <code>PacketSender</code>, and that
     *  <code>PacketSenderFactory</code> must understand its address
     *  to construct the corresponding <code>PacketSender</code>.
     *
     *  <p>In order to allow separate implementations to share the
     *  same global address space, every implementation must be
     *  associated with a unique <em>schema</em>.  A <em>schema</em>
     *  is simply an identifier for a specitic receiver/sender
     *  implementation. An external address must therefore have the
     *  following general syntax:<br>
     *  
     *  <em>address</em> ::=
     *  <em>schema</em><code><b>:</b><code><em>schema-dependent-part</em><br>
     *
     *  <em>schema</em> ::= a character string not containing the ":"
     *  character<br>
     *
     *  <em>schema-dependent-part</em> ::= implementation-dependent
     *  part.  Every implementation is free to define an appropriate
     *  syntax for this part of the address string.
     *
     *  @see PacketSender
     *  @see PacketSenderFactory
     **/
    public byte[] address();

    /** receives a packet in the given buffer.
     *
     *  This method must be reentrant.  Siena clients and servers must
     *  be able to use several threads to receive packets from the
     *  same receiver.
     *
     *  @return the number of bytes read into the buffer.  The return
     *          value <em>must not be negative</em>.  On error conditions,
     *          this method must throw an exception.
     *
     *  @exception PacketReceiverException in case an error occurrs
     *		while reading.
     **/
    public int receive(byte[] packet) throws PacketReceiverException;

    /** receives a packet in the given buffer, with the given timeout.
     *
     *
     *  This method must be reentrant.  Siena clients and servers must
     *  be able to use several threads to receive packets from the
     *  same receiver.
     *
     *  @return the number of bytes read into the buffer.  The return
     *          value <em>must not be negative</em>.  On error conditions,
     *          this method must throw an exception.
     *
     *  @exception PacketReceiverException in case an error occurrs
     *		while reading. 
     *
     *  @exception TimeoutExpired in case the timout expires before a
     *		packet is available
     **/
    public int receive(byte[] packet, long timeout) 
	throws PacketReceiverException, TimeoutExpired;

    /** closes the receiver.
     **/
    public void shutdown() throws PacketReceiverException;
}
