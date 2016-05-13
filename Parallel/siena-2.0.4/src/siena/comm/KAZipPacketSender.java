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

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;

import siena.Logging;
import siena.SENP;

public class KAZipPacketSender extends KAPacketSender {

	byte[] zipBuf = new byte[SENP.MaxPacketLen];
		

	public KAZipPacketSender(String h) throws InvalidSenderException,
			java.net.UnknownHostException {
		super(h);
		// Compressor with highest level of compression		 		
	}

	@Override
	public synchronized void send(byte[] packet, int offset, int len)
			throws PacketSenderException {		
		Deflater compressor = new Deflater(Deflater.BEST_COMPRESSION); 
		// Give the compressor the data to compress
		compressor.setInput(packet,offset,len);
		compressor.finish();
		int compressedDataLength = compressor.deflate(zipBuf);
		//Logging.prlnlog("Compressed from: " + len + " to: " + compressedDataLength, Logging.DEBUG);
		super.send(zipBuf, 0, compressedDataLength);
	}

}
