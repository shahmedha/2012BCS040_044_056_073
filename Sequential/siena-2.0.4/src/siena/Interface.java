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
package siena;

import java.io.IOException;

import siena.comm.PacketSender;
import siena.comm.PacketSenderException;

public class Interface {
	public final byte[] address;

	private final PacketSender sender;

	private int refs;

	private int cost = 1;

	Interface(byte[] h, PacketSender s) {
		address = h.clone();
		sender = s;
		refs = 0;
	}

	synchronized public void add_ref() {
		++refs;
	}

	synchronized public void remove_ref() {
		if (refs > 0)
			--refs;
	}

	synchronized public boolean has_no_refs() {
		return refs == 0;
	}

	public void writeTo(SENPWriter out) throws IOException {
		out.write(0x48 /* 'H' */);
		out.write(address);
		out.write(0x0a /* '\n' */);
	}

	public void send(byte[] packet) throws PacketSenderException {
		sender.send(packet);
	}

	public void send(byte[] packet, int len) throws PacketSenderException {
		sender.send(packet, len);
	}

	public void send(byte[] packet, int offset, int len)
			throws PacketSenderException {
		sender.send(packet, offset, len);
	}

	public void shutdown() throws PacketSenderException {
		sender.shutdown();
	}

	public int getCost() {
		return cost;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

	public void setCostFromSender() {
		this.cost = sender.getCost();
	}
}