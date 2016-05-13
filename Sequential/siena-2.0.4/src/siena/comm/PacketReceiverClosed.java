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

/** packet receiver has been closed */
public class PacketReceiverClosed extends PacketReceiverException {
    static final long serialVersionUID = 1L;
    public IOException ioex;

    public PacketReceiverClosed(IOException ex) {
	super("packet receiver has been closed: " + ex.toString());
	ioex = ex;
    }

    public PacketReceiverClosed() {
	super("packet receiver has been closed");
	ioex = null;
    }

    /** IOException that caused this PacketReceiverClosed exception
     * 
     *  @return IOException that caused this PacketReceiverClosed
     *          exception or <code>null</code>
     **/
    public IOException getIOException() {
	return ioex;
    }
}
