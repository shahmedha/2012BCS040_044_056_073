//
//  This file is part of Siena, a wide-area event notification system.
//  See http://www.inf.usi.ch/carzaniga/siena/
//
//  Author: Giovanni Toffetti Carughi
//  See the file AUTHORS for full details. 
//
//  Copyright (C) 2007, 2011 Giovanni Toffetti Carughi
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
import java.net.ServerSocket;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import siena.SENP;

public class KAZipPacketReceiver extends KAPacketReceiver {

	public static final String Schema = "kazip";

	public KAZipPacketReceiver() throws IOException {
		super();
		// TODO Auto-generated constructor stub
	}	

	public KAZipPacketReceiver(int port_number, int qsize) throws IOException {
		super(port_number, qsize);
		// TODO Auto-generated constructor stub
	}

	public KAZipPacketReceiver(int port_number) throws IOException {
		super(port_number);
		// TODO Auto-generated constructor stub
	}

	public KAZipPacketReceiver(ServerSocket s) throws IOException {
		super(s);		
	}
	
	



	@Override
	public synchronized void setHostName(String hostname) {
		my_address = (Schema + Separator + hostname + Separator 
			      + Integer.toString(port.getLocalPort())).getBytes();
	}

	@Override
	public int receive(byte[] buf) throws PacketReceiverException {
		byte[] zipBuf = new byte[SENP.MaxPacketLen];
		int len = super.receive(zipBuf);
		Inflater decompresser = new Inflater();		
		decompresser.setInput(zipBuf, 0, len);
		int resultLength = 0;
		try {
			resultLength = decompresser.inflate(buf);
		} catch (DataFormatException e) {			
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		decompresser.end();
		return resultLength;
	}

	@Override
	public byte[] address() {
		// TODO Auto-generated method stub
		return super.address();
	}
	
	

}
