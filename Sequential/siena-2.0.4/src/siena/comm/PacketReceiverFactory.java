//
//  This file is part of Siena, a wide-area event notification system.
//  See http://www.inf.usi.ch/carzaniga/siena/
//
//  Authors: John Giacomoni
//           Antonio Carzaniga (firstname.lastname@usi.ch)
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

/** creates PacketReceiver based on an external string representation.
 **/
public interface PacketReceiverFactory {
    /** creates a PacketReceiver object based on an external string
     *  representation.
     **/
    public PacketReceiver createPacketReceiver(String handler) 
	throws InvalidReceiverException;
}
