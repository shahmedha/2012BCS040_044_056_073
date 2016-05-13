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
package siena;

import java.io.IOException;
import java.io.OutputStream;
import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import siena.dvdrp.BSet;
import siena.dvdrp.BSetBV;
import siena.dvdrp.Base64;
import siena.dvdrp.DVEntry;
import siena.dvdrp.DistanceVector;
import siena.dvdrp.OrderedByteArraySet;
import siena.dvdrp.PredicatesTable;
import siena.dvdrp.PredicatesTableEntry;

public class SENP {
    public static final byte ProtocolVersion = 1;

    public static final byte[] Version = { 0x76, 0x65, 0x72, 0x73, 0x69, 0x6f,
					   0x6e }; // version

    public static final byte[] To = { 0x74, 0x6F }; // to

    public static final byte[] Method = { 0x6d, 0x65, 0x74, 0x68, 0x6f, 0x64 }; // method

    public static final byte[] Id = { 0x69, 0x64 }; // id

    public static final byte[] Handler = { 0x68, 0x61, 0x6e, 0x64, 0x6c, 0x65,
					   0x72 }; // handler

    public static final byte[] Cost = { 0x63, 0x6f, 0x73, 0x74 }; // cost

    public static final byte[] Ttl = { 0x74, 0x74, 0x6c }; // ttl

    public static final byte[] Mcc = { 0x6d, 0x63, 0x63 }; // mcc

    public static final int DefaultTtl = 30;

    public static final int MaxPacketLen = 65536;
    //public static final int MaxPacketLen = 1024;

    public static final byte NOP = 0;

    public static final byte PUB = 1;

    public static final byte SUB = 2;

    public static final byte UNS = 3;

    public static final byte ADV = 4;

    public static final byte UNA = 5;

    public static final byte HLO = 6;

    public static final byte BYE = 7;

    public static final byte SUS = 8;

    public static final byte RES = 9;

    public static final byte MAP = 10;

    public static final byte CNF = 11;

    public static final byte OFF = 12;

    public static final byte UDV = 13; // send distance vector update

    public static final byte DV = 14; // send complete distance vector

    public static final byte DRP = 15; // send notification with Dynamic Recipient Partitioning
	
    public static final byte PAD = 16; // predicate advertisement

    public static final byte[][] Methods = { { 0x4E, 0x4F, 0x50 }, // NOP
					     { 0x50, 0x55, 0x42 }, // PUB
					     { 0x53, 0x55, 0x42 }, // SUB
					     { 0x55, 0x4E, 0x53 }, // UNS
					     { 0x41, 0x44, 0x56 }, // ADV
					     { 0x55, 0x4E, 0x41 }, // UNA
					     { 0x48, 0x4C, 0x4F }, // HLO
					     { 0x42, 0x59, 0x45 }, // BYE
					     { 0x53, 0x55, 0x53 }, // SUS
					     { 0x52, 0x45, 0x53 }, // RES
					     { 0x4D, 0x41, 0x50 }, // MAP
					     { 0x43, 0x4e, 0x46 }, // CNF
					     { 0x4F, 0x46, 0x46 }, // OFF
					     { 0x55, 0x44, 0x56 }, // UDV
					     { 0x44, 0x56 }, // DV
					     { 0x44, 0x52, 0x50 }, // DRP
					     { 0x50, 0x41, 0x44 } // PAD
    };

    //
    // WARNING: don't mess up the order of operators in this array
    // it must correspond to the definitions of
    // Op.EQ, Op.LT, etc.
    // 
    public static final byte[][] operators = { { 0x3f }, // ?
					       { 0x3d }, // "="
					       { 0x3c }, // "<"
					       { 0x3e }, // ">"
					       { 0x3e, 0x3d }, // ">="
					       { 0x3c, 0x3d }, // "<="
					       { 0x3e, 0x2a }, // ">*"
					       { 0x2a, 0x3c }, // "*<"
					       { 0x61, 0x6e, 0x79 }, // any,
					       { 0x21, 0x3d }, // "!="
					       { 0x2a } // "*"
    };

    //
    // default port numbers
    //
    public static final int CLIENT_PORT = 1936;

    public static final int SERVER_PORT = 1969;

    public static final int DEFAULT_PORT = 1969;

    public static final byte[] KwdSeparator = { 0x20 }; // ' '

    public static final byte[] KwdSenp = { 0x73, 0x65, 0x6e, 0x70 }; // senp

    public static final byte[] KwdEvent = { 0x65, 0x76, 0x65, 0x6e, 0x74 }; // event

    public static final byte[] KwdEvents = { 0x65, 0x76, 0x65, 0x6e, 0x74, 0x73 }; // events

    public static final byte[] KwdFilter = { 0x66, 0x69, 0x6c, 0x74, 0x65, 0x72 }; // filter

    public static final byte[] KwdPattern = { 0x70, 0x61, 0x74, 0x74, 0x65,
					      0x72, 0x6e }; // pattern

    public static final byte[] KwdDistanceVector = { 0x64, 0x69, 0x73, 0x74,
						     0x61, 0x6e, 0x63, 0x65, 0x56, 0x65, 0x63, 0x74, 0x6f, 0x72 }; // distanceVector
	
    public static final byte[] KwdDVEntry = { 0x64, 0x56, 0x45, 0x6e, 0x74,
					      0x72, 0x79 }; // dVEntry
	
    public static final byte[] KwdPred = { 0x70, 0x72, 0x65, 0x64}; // pred
	
    public static final byte[] KwdBSet = { 0x42, 0x53, 0x65, 0x74 }; // BSet

    public static final byte[] KwdLParen = { 0x7b }; // {

    public static final byte[] KwdRParen = { 0x7d }; // }

    public static final byte[] KwdEquals = { 0x3d }; // =

    public static final byte[] KwdTrue = { 0x74, 0x72, 0x75, 0x65 }; // true

    public static final byte[] KwdFalse = { 0x66, 0x61, 0x6c, 0x73, 0x65 }; // false

    public static final byte[] KwdNull = { 0x6e, 0x75, 0x6c, 0x6c }; // null

    public static final byte[] KwdRecList = { 0x72, 0x65, 0x63, 0x4c, 0x69,
					      0x73, 0x74 }; // recList

    public static boolean match(byte[] x, byte[] y) {
	if (x == null && y == null)
	    return true;
	if (x == null || y == null || x.length != y.length)
	    return false;
	for (int i = 0; i < x.length; ++i)
	    if (x[i] != y[i])
		return false;
	return true;
    }
}

class SENPWBuffer {
    public byte[] buf;

    protected int pos;

    protected int last;

    public SENPWBuffer(byte[] b) {
	buf = b;
	pos = 0;
	last = 0;
    }

    public SENPWBuffer(byte[] b, int l) {
	buf = b;
	pos = 0;
	last = l;
    }

    public void init() {
	pos = 0;
	last = 0;
    }

    public void init(int len) {
	pos = 0;
	last = len;
    }

    public void init(byte[] b) {
	pos = 0;
	last = 0;
	append(b);
	pos = 0;
    }

    public void append(byte b) {
	buf[pos++] = b;
    }

    public void append(int x) {
	buf[pos++] = (byte) x;
    }

    public void append(byte[] bytes) {
	for (int i = 0; i < bytes.length; ++i)
	    buf[pos++] = bytes[i];
	if (last < pos)
	    last = pos;
    }

    public void append(String s) {
	append(s.getBytes());
    }

    public int length() {
	return last;
    }

    public int current_pos() {
	return pos;
    }

    protected void encode_octal(byte x) {
	buf[pos++] = (byte) (((x >> 6) & 3) + 0x30);
	buf[pos++] = (byte) (((x >> 3) & 7) + 0x30);
	buf[pos++] = (byte) ((x & 7) + 0x30);
    }

    protected void encode_decimal(long x) {
	byte[] tmp = new byte[20]; // Log(MAX_LONG)+1
	int p = 0;
	boolean negative = (x < 0);
	if (negative)
	    x = -x;

	do {
	    tmp[p++] = (byte) (x % 10 + 0x30 /* '0' */);
	    x /= 10;
	} while (x > 0);
	if (negative)
	    buf[pos++] = 0x2d; /* '-' */
	while (p-- > 0)
	    buf[pos++] = tmp[p];
    }

    void encode(byte[] bv) {
	buf[pos++] = 0x22 /* '"' */;
	for (int i = 0; i < bv.length; ++i) {
	    switch (bv[i]) {
	    case 11 /* '\v' */:
		buf[pos++] = 0x5c /* '\\' */;
		buf[pos++] = 0x76 /* 'v' */;
		break;
	    case 12 /* '\f' */:
		buf[pos++] = 0x5c /* '\\' */;
		buf[pos++] = 0x66 /* 'f' */;
		break;
	    case 13 /* '\r' */:
		buf[pos++] = 0x5c /* '\\' */;
		buf[pos++] = 0x72 /* 'r' */;
		break;
	    case 10 /* '\n' */:
		buf[pos++] = 0x5c /* '\\' */;
		buf[pos++] = 0x6e /* 'n' */;
		break;
	    case 9 /* '\t' */:
		buf[pos++] = 0x5c /* '\\' */;
		buf[pos++] = 0x74 /* 't' */;
		break;
	    case 8 /* '\b' */:
		buf[pos++] = 0x5c /* '\\' */;
		buf[pos++] = 0x62 /* 'b' */;
		break;
	    case 7 /* '\a' */:
		buf[pos++] = 0x5c /* '\\' */;
		buf[pos++] = 0x61 /* 'a' */;
		break;
	    case 0x22 /* '"' */:
		buf[pos++] = 0x5c /* '\\' */;
		buf[pos++] = 0x22 /* '"' */;
		break;
	    case 0x5c /* '\\' */:
		buf[pos++] = 0x5c /* '\\' */;
		buf[pos++] = 0x5c /* '\\' */;
		break;
	    default:
		if (bv[i] < 0x20 || bv[i] >= 0x7F) {
		    //
		    // here I handle other non-printable characters with
		    // the \xxx octal notation ...work in progress...
		    //
		    buf[pos++] = 0x5c;
		    encode_octal(bv[i]);
		} else {
		    buf[pos++] = bv[i];
		}
	    }
	}
	buf[pos++] = 0x22 /* '"' */;
	if (pos > last)
	    last = pos;
    }

    public void encode(AttributeValue a) {
	switch (a.getType()) {
	case AttributeValue.LONG:
	    encode_decimal(a.longValue());
	    break;
	case AttributeValue.BOOL:
	    append(a.booleanValue() ? SENP.KwdTrue : SENP.KwdFalse);
	    break;
	case AttributeValue.DOUBLE:
	    append(Double.toString(a.doubleValue()));
	    break;
	case AttributeValue.BYTEARRAY:
	    encode(a.byteArrayValue());
	    break;
	case AttributeValue.NULL:
	    append(SENP.KwdNull);
	    break;
	default:
	    // should throw an exception here
	    // ...work in progress...
	}
	if (pos > last)
	    last = pos;
    }

    public void encode(Notification e) {
	append(SENP.KwdEvent);
	encodeSimple(e);
    }

    public void encodeSimple(Notification e) {
	append(SENP.KwdLParen);
	Iterator<String> i = e.attributeNamesIterator();
	while (i.hasNext()) {
	    append(SENP.KwdSeparator);
	    String name = i.next();
	    append(name);
	    append(SENP.KwdEquals);
	    encode(e.getAttribute(name));
	}
	append(SENP.KwdRParen);
	if (pos > last)
	    last = pos;
    }

    public void encode(Pattern p) {
	append(SENP.KwdPattern);
	append(SENP.KwdLParen);
	for (int i = 0; i < p.filters.length; ++i) {
	    append(SENP.KwdSeparator);
	    encode(p.filters[i]);
	}
	append(SENP.KwdRParen);
	if (pos > last)
	    last = pos;
    }

    public void encode(DistanceVector d) {
	append(SENP.KwdDistanceVector);
	append(SENP.KwdLParen);
	byte[] entryId = null;
	synchronized (d) {
	    for (Iterator<byte[]> i = d.getEntryIdsIterator(); i.hasNext();) {
		entryId = i.next();
		append(SENP.KwdSeparator);
		encode(d.getEntry(entryId));
	    }
	}
		
	append(SENP.KwdRParen);
    }

    public void encode(DVEntry entry) {
	append(SENP.KwdDVEntry);
	append(SENP.KwdLParen);
	append(entry.getDest());
	append(SENP.KwdSeparator);
	append(String.valueOf(entry.getDist()).getBytes());
	append(SENP.KwdSeparator);
	append(entry.getNextHopId());		
	append(SENP.KwdRParen);
    }
	
    protected void encode(PredicatesTableEntry entry) {
	append(SENP.KwdPred);
	append(SENP.KwdLParen);
	append(SENP.KwdSeparator);
	append(entry.getDest());		
	append(SENP.KwdSeparator);
	append(String.valueOf(entry.getFiltersSeqNo()).getBytes());		
	for(BSetBV b : entry.filters)
	    encodeBloomFilter(b);
	append(SENP.KwdRParen);		
    }

    public void encodeBloomFilter(BSetBV bf) {
	append(SENP.KwdSeparator);
	append(SENP.KwdBSet);
	append(SENP.KwdEquals);
	// TOF: remove if we go back to Bset
	buf[pos++] = 0x22 /* '"' */;
	encodeBSetBV(bf.bv);
	// TOF: remove if we go back to Bset
	buf[pos++] = 0x22 /* '"' */;

    }

    // public void encodeBSet(BitSet bits){
    // byte[] array = new byte[BSet.BLOOM_FILTER_SIZE / 8];
    // for (int i=0; i < array.length; i++ ){
    // int n = 0;
    // for (int j=0; j<8; j++){
    // if (bits.get((i*8)+j)){
    // n = n | (1 << j);
    // }
    // }
    // append(Integer.toHexString(n).getBytes());
    // append(SENP.KwdSeparator);
    // }
    // }

    public void encodeBSet(BitSet bits) {
	buf[pos++] = 0x22 /* '"' */;
	byte[] byteBuf = new byte[BSet.BLOOM_FILTER_SIZE / 8];
	for (int i = 0; i < BSet.BLOOM_FILTER_SIZE / 8; i++) {
	    int n = 0;
	    for (int j = 0; j < 8; j++) {
		if (bits.get((i * 8) + j)) {
		    n = n | (1 << j);
		}
	    }
	    byteBuf[i] = (byte) n;
	}

	// System.out.println("original byteBuf : " + Arrays.toString(byteBuf));
	append(Base64.encodeBytes(byteBuf));
	buf[pos++] = 0x22 /* '"' */;
    }

    public void encodeBSetBV(long[] bv) {
	for(int i = 0; i < bv.length; ++i) {
	    buf[pos++] = 0x3A /* ':' */;

	    long v = bv[i];
	    if (v != 0) {
		boolean non_zero = false;
		for (int rs = Long.SIZE - 4; rs >= 0; rs -= 4) {
		    long cv = (v >> rs) & 0xfL;
		    //			System.out.println("[>" + rs + "](" + cv + ")");
		    if (cv != 0 || non_zero) {
			non_zero = true;
			if (cv < 10) {
			    buf[pos++] = (byte)(0x30 + cv); /* '0' + cv */
			} else {
			    buf[pos++] = (byte)(0x41 + cv - 10); /* 'A' + cv - 10 */
			}
		    }
		}
	    }
	}
	last = pos;
    }

    public void encode(Notification[] s) {
	append(SENP.KwdEvents);
	append(SENP.KwdLParen);
	for (int i = 0; i < s.length; ++i) {
	    append(SENP.KwdSeparator);
	    encode(s[i]);
	}
	append(SENP.KwdRParen);
	if (pos > last)
	    last = pos;
    }

    public void encode(Filter f) {
	append(SENP.KwdFilter);
	encodeSimple(f);
    }

    public void encodeSimple(Filter f) {
	append(SENP.KwdLParen);
	Iterator<String> i = f.constraintNamesIterator();
	while (i.hasNext()) {
	    String name = i.next();
	    Iterator<AttributeConstraint> j = f.constraintsIterator(name);
	    while (j.hasNext()) {
		append(SENP.KwdSeparator);
		append(name + " ");
		encode(j.next());
	    }
	}
	append(SENP.KwdRParen);
	if (pos > last)
	    last = pos;
    }

    public void encode(AttributeConstraint a) {
	append(SENP.operators[a.op]);
	if (a.op == Op.ANY)
	    return;
	encode(a.value);
	if (pos > last)
	    last = pos;
    }
}

class SENPBuffer extends SENPWBuffer {
    public SENPBuffer() {
	super(new byte[SENP.MaxPacketLen]);
    }

    public SENPBuffer(byte[] b) {
	super(b, b.length);
	sval_last = 0;
    }

    public void init() {
	super.init();
	sval_last = 0;
    }

    public void init(int len) {
	super.init(len);
	sval_last = 0;
    }

    public void init(byte[] b) {
	super.init(b);
	sval_last = 0;
    }

    //
    // WARNING: now, since Java doesn't have byte literals in the form
    // of ascii characters like good old 'a' '*' '\n' etc. I'll have
    // to use the corresponding decimal values. Which is the
    // Right(tm) way of doing that, according to some clowns out
    // there...
    //

    //
    // token types
    //
    public static final int T_EOF = -1;

    public static final int T_UNKNOWN = -2;

    //
    // keywords
    //
    public static final int T_ID = -3;

    public static final int T_STR = -4;

    public static final int T_INT = -5;

    public static final int T_DOUBLE = -6;

    public static final int T_BOOL = -7;

    public static final int T_OP = -8;

    public static final int T_LPAREN = -9;

    public static final int T_RPAREN = -10;

    public short oval;

    public long ival;

    public boolean bval;

    public double dval;

    private int sval_last = 0;

    private byte[] sval_buf;

    private int nextByte() {
	if (++pos >= last)
	    return -1;
	return buf[pos];
    }

    private int currByte() {
	if (pos >= last)
	    return -1;
	return buf[pos];
    }

    private void pushBack() {
	if (pos > 0)
	    pos--;
    }

    private boolean isCurrentFirstIdentChar() {
	if (pos >= last)
	    return false;
	return (buf[pos] >= 0x41 && buf[pos] <= 0x5a) // 'A' -- 'Z'
	    || (buf[pos] >= 0x61 && buf[pos] <= 0x7a) // 'a' -- 'z'
	    || buf[pos] == 0x5f; // '_'
    }

    private boolean isCurrentIdentChar() {
	if (pos >= last)
	    return false;
	return (buf[pos] >= 0x41 && buf[pos] <= 0x5a) // 'A' -- 'Z'
	    || (buf[pos] >= 0x61 && buf[pos] <= 0x7a) // 'a' -- 'z'
	    || (buf[pos] >= 0x30 && buf[pos] <= 0x39) // '0' -- '9'
	    || buf[pos] == 0x5f || buf[pos] == 0x24 // '_', '$'
	    || buf[pos] == 0x2e || buf[pos] == 0x2f; // '.', '/'
    }

    byte read_octal() {
	/* '0' -- '7' */
	byte nb = 0;
	int i = 3;
	do {
	    nb = (byte) (nb * 8 + currByte() - 0x30);
	} while (--i > 0 && ++pos < last && buf[pos] >= 0x30
		 && buf[pos] <= 0x37);
	return nb;
    }

    int read_string() {
	//
	// here buf[pos] == '"'
	//
	if (sval_buf == null || last - pos > sval_buf.length)
	    sval_buf = new byte[last - pos];
	sval_last = 0;
	while (++pos < last)
	    switch (buf[pos]) {
	    case 0x22 /* '"' */:
		++pos;
		return T_STR;
	    case 0x5c /* '\\' */:
		if (++pos >= last)
		    return T_UNKNOWN;
		switch (buf[pos]) {
		case 0x76 /* 'v' */:
		    sval_buf[sval_last++] = 0x0b /* '\v' */;
		    break;
		case 0x66 /* 'f' */:
		    sval_buf[sval_last++] = 0x0c /* '\f' */;
		    break;
		case 0x72 /* 'r' */:
		    sval_buf[sval_last++] = 0x0d /* '\r' */;
		    break;
		case 0x6e /* 'n' */:
		    sval_buf[sval_last++] = 0x0a /* '\n' */;
		    break;
		case 0x74 /* 't' */:
		    sval_buf[sval_last++] = 0x09 /* '\t' */;
		    break;
		case 0x62 /* 'b' */:
		    sval_buf[sval_last++] = 0x08 /* '\b' */;
		    break;
		case 0x61 /* 'a' */:
		    sval_buf[sval_last++] = 0x07 /* '\a' */;
		    break;
		default:
		    if (buf[pos] >= 0x30 && buf[pos] <= 0x37) {
			sval_buf[sval_last++] = read_octal();
		    } else {
			sval_buf[sval_last++] = buf[pos];
		    }
		}
		break;
	    default:
		sval_buf[sval_last++] = buf[pos];
	    }
	return T_UNKNOWN;
    }

    int read_id() {
	sval_last = 0;
	if (sval_buf == null || last - pos > sval_buf.length)
	    sval_buf = new byte[last - pos];
	do {
	    sval_buf[sval_last++] = buf[pos++];
	} while (isCurrentIdentChar());
	return T_ID;
    }

    int read_int() {
	boolean negative = false;
	//
	// here buf[pos] is either a digit or '-'
	//
	if (buf[pos] == 0x2d /* '-' */) {
	    negative = true;
	    ival = 0;
	    if (++pos >= last || buf[pos] < 0x30 || buf[pos] > 0x39)
		return T_UNKNOWN;
	} else {
	    ival = buf[pos] - 0x30;
	    if (++pos >= last || buf[pos] < 0x30 || buf[pos] > 0x39)
		return T_INT;
	}
	do {
	    ival = ival * 10 + buf[pos] - 0x30;
	} while (++pos < last && buf[pos] >= 0x30 && buf[pos] <= 0x39);
	if (negative)
	    ival = -ival;
	return T_INT;
    }

    int read_number() {
	boolean negative = false;
	//
	// here buf[pos] is either a digit or '-'
	//
	if (buf[pos] == 0x2d /* '-' */) {
	    negative = true;
	    if (++pos >= last || buf[pos] < 0x30 || buf[pos] > 0x39)
		return T_UNKNOWN;
	}
	int type;
	if (read_int() == T_UNKNOWN)
	    return T_UNKNOWN;
	type = T_INT;
	dval = ival;
	if (pos < last && buf[pos] == 0x2e /* '.' */) {
	    type = T_DOUBLE;
	    if (++pos >= last || buf[pos] < 0x30 || buf[pos] > 0x39) {
		return T_UNKNOWN;
	    } else {
		dval += read_decimal();
	    }
	}
	if (pos < last)
	    if (buf[pos] == 101 /* 'e' */|| buf[pos] == 69) /* 'E' */{
		type = T_DOUBLE;
		if (++pos >= last
		    || ((buf[pos] < 0x30 || buf[pos] > 0x39) && buf[pos] != 0x2d /* '-' */))
		    return T_UNKNOWN;
		if (read_int() == T_UNKNOWN)
		    return T_UNKNOWN;
		dval *= java.lang.Math.pow(10, ival);
	    }
	if (negative) {
	    if (type == T_INT) {
		ival = -ival;
	    } else {
		dval = -dval;
	    }
	}
	return type;
    }

    double read_decimal() {
	//
	// here buf[pos] is a digit
	//
	long intpart = 0;
	long divisor = 1;
	do {
	    intpart = intpart * 10 + (buf[pos] - 0x30);
	    divisor *= 10;
	} while (++pos < last && buf[pos] >= 0x30 && buf[pos] <= 0x39);
	return (1.0 * intpart) / divisor;
    }

    public int nextToken() {
	while (true) {
	    switch (currByte()) {
	    case -1:
		return T_EOF;
	    case 0x22 /* '"' */:
		return read_string();
	    case 123 /* '{' */:
		++pos;
		return T_LPAREN;
	    case 125 /* '}' */:
		++pos;
		return T_RPAREN;
	    case 33 /* '!' */:
		switch (nextByte()) {
		case 0x3d /* '=' */:
		    oval = Op.NE;
		    ++pos;
		    return T_OP;
		default:
		    return T_UNKNOWN;
		}
	    case 42 /* '*' */:
		switch (nextByte()) {
		case 60 /* '<' */:
		    oval = Op.SF;
		    ++pos;
		    return T_OP;
		default:
		    oval = Op.SS;
		    return T_OP;
		}
	    case 0x3d /* '=' */:
		oval = Op.EQ;
		++pos;
		return T_OP;
	    case 62 /* '>' */:
		switch (nextByte()) {
		case 42 /* '*' */:
		    oval = Op.PF;
		    ++pos;
		    return T_OP;
		case 0x3d /* '=' */:
		    oval = Op.GE;
		    ++pos;
		    return T_OP;
		default:
		    oval = Op.GT;
		    return T_OP;
		}
	    case 60 /* '<' */:
		switch (nextByte()) {
		case 0x3d /* '=' */:
		    oval = Op.LE;
		    ++pos;
		    return T_OP;
		default:
		    oval = Op.LT;
		    return T_OP;
		}
	    default:
		if ((buf[pos] >= 0x30 && buf[pos] <= 0x39) /* '0' -- '9' */
		    || buf[pos] == 0x2d /* '-' */) {
		    return read_number();
		} else if (isCurrentFirstIdentChar()) {
		    return read_id();
		} else {
		    //
		    // I simply ignore characters that I don't understand
		    //
		    ++pos;
		}
	    }
	}
    }

    public boolean match_sval(byte[] y) {
	if (sval_last == 0 && y == null)
	    return true;
	if (sval_buf == null || sval_last == 0 || y == null
	    || sval_last != y.length)
	    return false;
	for (int i = 0; i < sval_last; ++i)
	    if (sval_buf[i] != y[i])
		return false;
	return true;
    }

    public byte[] copy_sval() {
	byte[] res = new byte[sval_last];
	for (int i = 0; i < sval_last; ++i)
	    res[i] = sval_buf[i];
	return res;
    }

    public String sval_string() {
	return new String(sval_buf, 0, sval_last);
    }

    AttributeValue decodeAttribute() throws SENPInvalidFormat {
	switch (nextToken()) {
	case T_ID:
	    if (match_sval(SENP.KwdTrue))
		return new AttributeValue(true);
	    if (match_sval(SENP.KwdFalse))
		return new AttributeValue(false);
	    if (match_sval(SENP.KwdNull))
		return new AttributeValue();
	    return new AttributeValue(copy_sval());
	case T_STR:
	    return new AttributeValue(copy_sval());
	case T_INT:
	    return new AttributeValue(ival);
	case T_BOOL:
	    return new AttributeValue(bval);
	case T_DOUBLE:
	    return new AttributeValue(dval);
	default:
	    throw (new SENPInvalidFormat("<int>, <string>, <bool> or <double>",
					 pos));
	}
    }

    static final String ErrAttrName = "<attribute-name>";

    static final String ErrParam = "`event' or `filter' or `pattern' or `events'";

    static final String ErrEvent = "`event'";

    static final String ErrFilter = "`filter'";

    static final String ErrDVEntry = "`DVEntry'";

    static final String ErrBSet = "`BSet'";

    AttributeConstraint decodeAttributeConstraint() throws SENPInvalidFormat {
	switch (nextToken()) {
	case T_ID:
	    if (match_sval(SENP.operators[Op.ANY])) {
		return new AttributeConstraint(Op.ANY, (AttributeValue) null);
	    } else {
		throw (new SENPInvalidFormat(T_OP, pos));
	    }
	case T_OP: {
	    short op = oval;
	    return new AttributeConstraint(op, decodeAttribute());
	}
	default:
	    throw (new SENPInvalidFormat(T_OP, pos));
	}
    }

    Notification decodeNotification() throws SENPInvalidFormat {
	return decodeNotification(null);
    }

    Notification decodeNotification(Notification e) throws SENPInvalidFormat {
	if (nextToken() != T_LPAREN)
	    throw (new SENPInvalidFormat(T_LPAREN, new String(SENP.KwdLParen),
					 pos));
	int ttype;
	if (e == null){			
	    e = new Notification();
	}
	while ((ttype = nextToken()) != T_RPAREN) {
	    if (ttype != T_ID && ttype != T_STR)
		throw (new SENPInvalidFormat(T_ID, ErrAttrName, pos));
	    String name = sval_string();
	    if (nextToken() != T_OP || oval != Op.EQ)
		throw (new SENPInvalidFormat(T_OP, new String(SENP.KwdEquals),
					     pos));
	    e.putAttribute(name, decodeAttribute());
	}
	return e;
    }

    Notification[] decodeNotifications() throws SENPInvalidFormat {
	if (nextToken() != T_LPAREN)
	    throw (new SENPInvalidFormat(T_LPAREN, new String(SENP.KwdLParen),
					 pos));
	LinkedList<Notification> l = new LinkedList<Notification>();
	int ttype;

	while ((ttype = nextToken()) != T_RPAREN) {
	    if (ttype != T_ID && !match_sval(SENP.KwdEvent))
		throw (new SENPInvalidFormat(T_ID, ErrEvent, pos));
	    l.addLast(decodeNotification());
	}
	Notification[] res = new Notification[l.size()];
	int i = 0;
	for (Notification n: l) {
	    res[i] = n;
	    ++i;
	}
	return res;
    }

    Pattern decodePattern() throws SENPInvalidFormat {
	if (nextToken() != T_LPAREN)
	    throw (new SENPInvalidFormat(T_LPAREN, new String(SENP.KwdLParen),
					 pos));
	LinkedList<Filter> l = new LinkedList<Filter>();
	int ttype;

	while ((ttype = nextToken()) != T_RPAREN) {
	    if (ttype != T_ID && ttype != T_STR && !match_sval(SENP.KwdFilter))
		throw (new SENPInvalidFormat(T_ID, ErrFilter, pos));
	    l.addLast(decodeFilter());
	}
	Filter[] ff = new Filter[l.size()];
	int i = 0;
	for (Filter f : l) {
	    ff[i] = f;
	    ++i;
	}
	return new Pattern(ff);
    }

    Filter decodeFilter() throws SENPInvalidFormat {
	return decodeFilter(null);
    }

    Filter decodeFilter(Filter f) throws SENPInvalidFormat {
	if (nextToken() != T_LPAREN)
	    throw (new SENPInvalidFormat(T_LPAREN, new String(SENP.KwdLParen),
					 pos));
	if (f == null)
	    f = new Filter();
	int ttype;
	while ((ttype = nextToken()) != T_RPAREN) {
	    if (ttype != T_ID && ttype != T_STR)
		throw (new SENPInvalidFormat(T_ID, ErrAttrName, pos));

	    f.addConstraint(sval_string(), decodeAttributeConstraint());
	}
	return f;
    }

    // public void encode(byte[] id, DVEntry entry) throws IOException {
    // out.write(id);
    // out.write(SENP.KwdLParen);
    // out.write(entry.getDest());
    // out.write(SENP.KwdSeparator);
    // out.write(entry.getDist());
    // out.write(SENP.KwdSeparator);
    // out.write(entry.getNextHopId());
    // out.write(SENP.KwdSeparator);
    // out.write(String.valueOf(entry.getPredicatesTS()).getBytes());
    // out.write(SENP.KwdSeparator);
    // Iterator i = entry.getPredicates().iterator();
    // out.write(SENP.KwdLParen);
    // while(i.hasNext()){
    // BSet b = (BSet) i.next();
    // //TODO: find a better way to encode BSets
    // out.write(b.toString().getBytes());
    // out.write(SENP.KwdSeparator);
    // }
    // out.write(SENP.KwdRParen);
    // out.write(SENP.KwdRParen);
    //      
    // }
    //    

    DistanceVector decodeDistanceVector() throws SENPInvalidFormat {
	if (nextToken() != T_LPAREN)
	    throw (new SENPInvalidFormat(T_LPAREN, new String(SENP.KwdLParen),
					 pos));
	DistanceVector dv = new DistanceVector();
	int ttype;
	while ((ttype = nextToken()) != T_RPAREN) {			
	    if (ttype != T_ID && ttype != T_STR && !match_sval(SENP.KwdDVEntry))
		throw (new SENPInvalidFormat(T_ID, ErrDVEntry, pos));
	    decodeDVEntry(dv);			
	}
	return dv;
    }

    void decodeDVEntry(DistanceVector dv) throws SENPInvalidFormat {
	if (nextToken() != T_LPAREN)
	    throw (new SENPInvalidFormat(T_LPAREN, new String(SENP.KwdLParen),
					 pos));
	DVEntry entry = new DVEntry();
	nextToken(); // get to dest
	entry.setDest(copy_sval());
	nextToken(); // get to distance
	entry.setDist((int) ival);
	nextToken(); // get to nextHop
	entry.setNextHopId(copy_sval());
	nextToken();
	dv.addEntry(entry.getDest(), entry);
    }
	
    PredicatesTableEntry decodePredicate() throws SENPInvalidFormat {
	if (nextToken() != T_LPAREN)
	    throw (new SENPInvalidFormat(T_LPAREN, new String(SENP.KwdLParen),
					 pos));
	PredicatesTableEntry entry = new PredicatesTableEntry();
	nextToken(); // get to dest
	entry.setDest(copy_sval());		
	nextToken(); // read filters timestamp
	entry.setFiltersSeqNo(ival);
	int ttype;
	while ((ttype = nextToken()) != T_RPAREN) {
	    if (ttype != T_ID && ttype != T_STR && !match_sval(SENP.KwdBSet))
		throw (new SENPInvalidFormat(T_ID, ErrBSet, pos));
	    BSetBV b = decodeDVEntryBSetBV();
	    entry.filters.add(b);
	}
	/*
	 * we set the cleanUp flag to true, so we trigger filter deletion on packet handling
	 */
	if (entry.filters.isEmpty()){
	    entry.cleanUp = true;
	}
	return entry;
    }

    BSet decodeBSet() throws SENPInvalidFormat {
	if (currByte() != 0x22) { /* '"' */
	    throw (new SENPInvalidFormat(0x22, "\"", pos));
	}
	read_string();
	byte[] byteBuf;
	try {
	    byteBuf = Base64.decode(this.sval_string());
	} catch (IOException ex) {
	    throw (new SENPInvalidFormat("base64-data", pos));
	}

	BSet b = new BSet();
	b.bits = new BitSet(BSet.BLOOM_FILTER_SIZE);
	for (int i = 0; i < byteBuf.length; i++) {
	    byte v = byteBuf[i];
	    for (int j = 0; j < 8; j++) {
		if ((v & (1 << j)) != 0)
		    b.bits.set(i * 8 + j);
	    }
	}
	pos--;
	if (currByte() != 0x22) { /* '"' */
	    System.out.println("CurrByte: " + currByte());
	    throw (new SENPInvalidFormat(0x22, String.valueOf("\""), pos));
	}
	pos++;
	return b;
    }

    BSetBV decodeBSetBV() throws SENPInvalidFormat {
	if (currByte() != 0x3A) /* ':' */
	    throw (new SENPInvalidFormat(0x3A, ":", pos));
	    
	BSetBV bset = new BSetBV();
	long v = 0, c;
	int i = 0;
	for(;;) {
	    c = nextByte();
	    if (c >= 0x30 && c <= 0x39) { /* '0' -- '9' */
		c = (c - 0x30) & 15;
		v = (v << 4) | c;
	    } else if (c >= 0x41 && c <= 0x46) { /* 'A' -- 'F' */
		c = (c - 0x41 + 10) & 15;
		v = (v << 4) | c;
	    } else if (c == 0x3A) /* ':' */ {
		bset.bv[i++] = v;
		v = 0;
	    } else {
		bset.bv[i] = v;
		return bset;
	    }
	}
    }
	

    /*
     * this is a workaround to reuse decodeBSet: SENPPacket.decode() consumes
     * the `=' character, while decodeDVEntry does not
     */
    BSet decodeDVEntryBSet() throws SENPInvalidFormat {
	if (currByte() != 0x3d) { /* '=' */
	    System.err.println("Found: " + currByte());
	    throw (new SENPInvalidFormat(0x3d, "=", pos));
	}
	pos++;
	return decodeBSet();
    }
	
    /*
     * this is a workaround to reuse decodeBSet: SENPPacket.decode() consumes
     * the `=' character, while decodeDVEntry does not
     */
    BSetBV decodeDVEntryBSetBV() throws SENPInvalidFormat {
	if (currByte() != 0x3d) { /* '=' */
	    System.err.println("Found: " + currByte());
	    throw (new SENPInvalidFormat(0x3d, "=", pos));
	}
	pos++;
	pos++;
	BSetBV res = decodeBSetBV();
	pos++;
	return res;
    }

    OrderedByteArraySet decodeRecipientsList() throws SENPInvalidFormat {
	if (nextToken() != T_LPAREN)
	    throw (new SENPInvalidFormat(T_LPAREN, new String(SENP.KwdLParen),
					 pos));

	OrderedByteArraySet recipients = new OrderedByteArraySet();
	while (nextToken() != T_RPAREN) {
	    recipients.add(copy_sval());
	}
	return recipients;
    }

}

class SENPWriter {
    private OutputStream out;

    SENPWriter(OutputStream o) {
	out = o;
    }

    public void write(byte b) throws IOException {
	out.write(b);
    }

    public void flush() throws IOException {
	out.flush();
    }

    public void close() throws IOException {
	out.close();
    }

    public void write(int b) throws IOException {
	out.write((byte) b);
    }

    public void write(byte[] buf, int off, int len) throws IOException {
	out.write(buf, off, len);
    }

    public void write(byte[] bytes) throws IOException {
	out.write(bytes);
    }

    public void write(String s) throws IOException {
	byte[] b = s.getBytes();
	out.write(b, 0, b.length);
    }

    protected void encode_octal(byte x) throws IOException {
	out.write((byte) (((x >> 6) & 3) + 0x30));
	out.write((byte) (((x >> 3) & 7) + 0x30));
	out.write((byte) ((x & 7) + 0x30));
    }

    protected void encode_decimal(long x) throws IOException {
	byte[] tmp = new byte[20]; // Log(MAX_LONG)+1
	int p = 0;
	boolean negative = (x < 0);
	if (negative)
	    x = -x;

	do {
	    tmp[p++] = (byte) (x % 10 + 0x30 /* '0' */);
	    x /= 10;
	} while (x > 0);
	if (negative)
	    out.write(0x2d); /* '-' */
	while (p-- > 0)
	    out.write(tmp[p]);
    }

    void encode(byte[] bv) throws IOException {
	out.write(0x22 /* '"' */);
	for (int i = 0; i < bv.length; ++i) {
	    switch (bv[i]) {
	    case 11 /* '\v' */:
		out.write(0x5c /* '\\' */);
		out.write(0x76 /* 'v' */);
		break;
	    case 12 /* '\f' */:
		out.write(0x5c /* '\\' */);
		out.write(0x66 /* 'f' */);
		break;
	    case 13 /* '\r' */:
		out.write(0x5c /* '\\' */);
		out.write(0x72 /* 'r' */);
		break;
	    case 10 /* '\n' */:
		out.write(0x5c /* '\\' */);
		out.write(0x6e /* 'n' */);
		break;
	    case 9 /* '\t' */:
		out.write(0x5c /* '\\' */);
		out.write(0x74 /* 't' */);
		break;
	    case 8 /* '\b' */:
		out.write(0x5c /* '\\' */);
		out.write(0x62 /* 'b' */);
		break;
	    case 7 /* '\a' */:
		out.write(0x5c /* '\\' */);
		out.write(0x61 /* 'a' */);
		break;
	    case 0x22 /* '"' */:
		out.write(0x5c /* '\\' */);
		out.write(0x22 /* '"' */);
		break;
	    case 0x5c /* '\\' */:
		out.write(0x5c /* '\\' */);
		out.write(0x5c /* '\\' */);
		break;
	    default:
		if (bv[i] < 0x20 || bv[i] >= 0x7F) {
		    //
		    // here I handle other non-printable characters with
		    // the \xxx octal notation ...work in progress...
		    //
		    out.write(0x5c);
		    encode_octal(bv[i]);
		} else {
		    out.write(bv[i]);
		}
	    }
	}
	out.write(0x22 /* '"' */);
    }

    public void encode(AttributeValue a) throws IOException {
	switch (a.getType()) {
	case AttributeValue.LONG:
	    encode_decimal(a.longValue());
	    break;
	case AttributeValue.BOOL:
	    out.write(a.booleanValue() ? SENP.KwdTrue : SENP.KwdFalse);
	    break;
	case AttributeValue.DOUBLE:
	    out.write(Double.toString(a.doubleValue()).getBytes());
	    break;
	case AttributeValue.BYTEARRAY:
	    encode(a.byteArrayValue());
	    break;
	case AttributeValue.NULL:
	    out.write(SENP.KwdNull);
	    break;
	default:
	    // should throw an exception here
	    // ...work in progress...
	}
    }

    public void encode(Notification e) throws IOException {
	out.write(SENP.KwdEvent);
	out.write(SENP.KwdLParen);
	Iterator<String> i = e.attributeNamesIterator();
	while (i.hasNext()) {
	    out.write(SENP.KwdSeparator);
	    String name = i.next();
	    out.write(name.getBytes());
	    out.write(SENP.KwdEquals);
	    encode(e.getAttribute(name));
	}
	out.write(SENP.KwdRParen);
    }

    public void encode(Pattern p) throws IOException {
	out.write(SENP.KwdPattern);
	out.write(SENP.KwdLParen);
	for (int i = 0; i < p.filters.length; ++i) {
	    out.write(SENP.KwdSeparator);
	    encode(p.filters[i]);
	}
	out.write(SENP.KwdRParen);
    }

    public void encode(Notification[] s) throws IOException {
	out.write(SENP.KwdEvents);
	out.write(SENP.KwdLParen);
	for (int i = 0; i < s.length; ++i) {
	    out.write(SENP.KwdSeparator);
	    encode(s[i]);
	}
	out.write(SENP.KwdRParen);
    }

    public void encode(Filter f) throws IOException {
	out.write(SENP.KwdFilter);
	encodeSimple(f);
    }

    public void encodeSimple(Filter f) throws IOException {
	out.write(SENP.KwdLParen);
	Iterator<String> i = f.constraintNamesIterator();
	while (i.hasNext()) {
	    String name = i.next();
	    Iterator<AttributeConstraint> j = f.constraintsIterator(name);
	    while (j.hasNext()) {
		out.write(SENP.KwdSeparator);
		out.write(name.getBytes());
		out.write(SENP.KwdSeparator);
		encode(j.next());
	    }
	}
	out.write(SENP.KwdRParen);
    }

    public void encode(AttributeConstraint a) throws IOException {
	out.write(SENP.operators[a.op]);
	if (a.op == Op.ANY)
	    return;
	encode(a.value);
    }
}

class SENPPacket extends SENPBuffer {
    private SENPPacket next;

    public byte version;

    public byte method;

    public byte ttl;

    public byte mcc; /*
		      * multicast count: to be split each time a recipient
		      * partition is done
		      */

    public int cost = 0; // default value for NULL value

    public byte[] to;

    public byte[] id;

    public byte[] handler;

    public Notification event;

    public Filter filter;

    public Pattern pattern;

    public Notification[] events;

    public DistanceVector distanceVector;
	
    public PredicatesTableEntry predicate;

    public BSetBV bloomFilter;

    // I could have used a List or a Set but I want to enforce the correct
    // instantiation
    // using the right Comparator
    public OrderedByteArraySet recipients;

    public SENPPacket() {
	super();
	init();
    }

    public void init(int len) {
	pos = 0;
	last = len;
	cost = 0;
	next = null;
	version = SENP.ProtocolVersion;
	method = SENP.NOP;
	ttl = SENP.DefaultTtl;
	to = null;
	id = null;
	handler = null;

	event = null;
	filter = null;
	pattern = null;
	events = null;
	recipients = null;
	distanceVector = null;
	bloomFilter = null;
	predicate = null;
    }

    public void init(byte[] b) {
	super.init(b);
	next = null;
	version = SENP.ProtocolVersion;
	method = SENP.NOP;
	ttl = SENP.DefaultTtl;
	to = null;
	id = null;
	handler = null;
	cost = 0;
	event = null;
	filter = null;
	pattern = null;
	events = null;
	recipients = null;
	distanceVector = null;
	bloomFilter = null;
	predicate = null;
    }

    public void init() {
	pos = 0;
	last = 0;

	next = null;
	version = SENP.ProtocolVersion;
	method = SENP.NOP;
	ttl = SENP.DefaultTtl;
	mcc = 0;
	cost = 0;
	to = null;
	id = null;
	handler = null;

	event = null;
	filter = null;
	pattern = null;
	events = null;
	recipients = null;
	distanceVector = null;
	bloomFilter = null;
	predicate = null;
    }

    public void copyOf(SENPPacket p) {
	this.init();

	next = null;
	version = SENP.ProtocolVersion;
	method = p.method;
	ttl = p.ttl;
	mcc = p.mcc;
	to = p.to;
	id = p.id;
	handler = p.handler;

	event = p.event;
	filter = p.filter;
	pattern = p.pattern;
	events = p.events;
	recipients = p.recipients;
	distanceVector = p.distanceVector;
	bloomFilter = p.bloomFilter;
	predicate = p.predicate;
	// System.out.println("Copy of packet: this = " + this + " copy of: " +
	// p);
	// Exception e = new Exception();
	// e.printStackTrace();
    }

    public String toString() {
	encode();		
	return new String(buf, 0, length());
    }

    static private SENPPacket packet_cache = null;

    synchronized static public SENPPacket allocate() {
	SENPPacket res;
	if (packet_cache == null) {
	    res = new SENPPacket();
	} else {
	    res = packet_cache;
	    packet_cache = packet_cache.next;
	    res.init();
	}
	return res;
    }

    synchronized static public void recycle(SENPPacket p) {
	p.next = packet_cache;
	packet_cache = p;
    }

    public int encode() throws ArrayIndexOutOfBoundsException {
	pos = 0;
	last = 0;
	append(SENP.KwdSenp);
	append(SENP.KwdLParen);

	append(SENP.Version);
	append(SENP.KwdEquals);
	encode_decimal(version);
	append(SENP.KwdSeparator);

	append(SENP.Method);
	append(SENP.KwdEquals);
	encode(SENP.Methods[method]);
	append(SENP.KwdSeparator);

	append(SENP.Ttl);
	append(SENP.KwdEquals);
	encode_decimal(ttl);

	if (mcc != 0) {
	    append(SENP.KwdSeparator);
	    append(SENP.Mcc);
	    append(SENP.KwdEquals);
	    encode_decimal(mcc);
	}

	if (id != null) {
	    append(SENP.KwdSeparator);
	    append(SENP.Id);
	    append(SENP.KwdEquals);
	    encode(id);
	}

	if (to != null) {
	    append(SENP.KwdSeparator);
	    append(SENP.To);
	    append(SENP.KwdEquals);
	    encode(to);
	}

	if (handler != null) {
	    append(SENP.KwdSeparator);
	    append(SENP.Handler);
	    append(SENP.KwdEquals);
	    encode(handler);
	}

	if (cost != 0) {
	    append(SENP.KwdSeparator);
	    append(SENP.Cost);
	    append(SENP.KwdEquals);
	    append(Integer.toString(cost).getBytes());
	}

	//		// insert bloom filter in header for subscriptions
	//		if (bloomFilter != null) {
	//			append(SENP.KwdSeparator);
	//			encodeBloomFilter(bloomFilter);
	//		}

	// insert list of recipients for DRP packages
	if (recipients != null && recipients.size() > 0) {
	    append(SENP.KwdSeparator);
	    append(SENP.KwdRecList);
	    append(SENP.KwdEquals);
	    append(SENP.KwdLParen);
	    for(byte[] r : recipients) {
		append(r);
		append(SENP.KwdSeparator);
	    }
	    append(SENP.KwdRParen);
	}

	append(SENP.KwdRParen);
		
	//		switch(method){
	//		case SENP.PUB:
	//			break;
	//		case SENP.DRP:
	//			break;
	//		case 
	//		
	//		}
		
	if (event != null) {
	    append(SENP.KwdSeparator);
	    encode(event);
	} else if (filter != null) {
	    append(SENP.KwdSeparator);
	    encode(filter);
	} else if (pattern != null) {
	    append(SENP.KwdSeparator);
	    encode(pattern);
	} else if (events != null) {
	    append(SENP.KwdSeparator);
	    encode(events);
	} else if (distanceVector != null) {
	    append(SENP.KwdSeparator);
	    encode(distanceVector);
	} else if (predicate != null) {
	    append(SENP.KwdSeparator);
	    encode(predicate);
	}
	if (pos > last)
	    last = pos;
	return last;
    }

	

    public void decode() throws SENPInvalidFormat {
	int ttype;

	pos = 0;
	if (nextToken() != T_ID || !match_sval(SENP.KwdSenp))
	    throw (new SENPInvalidFormat(T_ID, new String(SENP.KwdSenp), pos));

	if (nextToken() != T_LPAREN)
	    throw (new SENPInvalidFormat(T_LPAREN, new String(SENP.KwdLParen),
					 pos));

	while ((ttype = nextToken()) != T_RPAREN) {
	    if (ttype != T_ID)
		throw (new SENPInvalidFormat(T_ID, ErrAttrName, pos));

	    if (nextToken() != T_OP || oval != Op.EQ)
		throw (new SENPInvalidFormat(T_OP, new String(SENP.KwdEquals),
					     pos));

	    if (match_sval(SENP.Method)) {
		switch (nextToken()) {
		case T_ID:
		case T_STR:
		    for (byte mi = 0; mi < SENP.Methods.length; ++mi)
			if (match_sval(SENP.Methods[mi])) {
			    method = mi;
			    break;
			}
		    break;
		default:
		    throw (new SENPInvalidFormat(T_ID, "expecting method", pos));
		}
	    } else if (match_sval(SENP.Ttl)) {
		if (nextToken() == T_INT) {
		    ttl = (byte) ival;
		} else {
		    throw (new SENPInvalidFormat(T_INT, "expecting ttl value",
						 pos));
		}
	    } else if (match_sval(SENP.Mcc)) {
		if (nextToken() == T_INT) {
		    mcc = (byte) ival;
		} else {
		    throw (new SENPInvalidFormat(T_INT, "expecting mcc value",
						 pos));
		}
	    } else if (match_sval(SENP.Cost)) {
		if (nextToken() == T_INT) {
		    cost = (int) this.ival;
		} else {
		    throw (new SENPInvalidFormat(T_STR, "expecting cost value",
						 pos));
		}
	    } else if (match_sval(SENP.Version)) {
		if (nextToken() == T_INT) {
		    version = (byte) ival;
		} else {
		    throw (new SENPInvalidFormat(T_INT,
						 "expecting version value", pos));
		}
	    } else if (match_sval(SENP.Id)) {
		if (nextToken() == T_STR) {
		    id = copy_sval();
		} else {
		    throw (new SENPInvalidFormat(T_STR, "expecting id value",
						 pos));
		}
	    } else if (match_sval(SENP.To)) {
		if (nextToken() == T_STR) {
		    to = copy_sval();
		} else {
		    throw (new SENPInvalidFormat(T_STR, "expecting to value",
						 pos));
		}
	    } else if (match_sval(SENP.Handler)) {
		if (nextToken() == T_STR) {
		    handler = copy_sval();
		} else {
		    throw (new SENPInvalidFormat(T_STR,
						 "expecting handler value", pos));
		}
		// add else here to parse Bloom filters in header and recipient
		// list
	    } else if (match_sval(SENP.KwdRecList)) {
		recipients = decodeRecipientsList();
	    } else {
		throw (new SENPInvalidFormat(T_STR, "unknown header field "
					     + sval_string(), pos));
	    }
	}
	//
	// now reads the optional parameter: either a filter or an event
	// or a distance vector
	//
	switch (nextToken()) {
	case T_EOF:
	    return;
	case T_ID:
	    if (match_sval(SENP.KwdFilter)) {
		filter = decodeFilter();
	    } else if (match_sval(SENP.KwdEvent)) {
		event = decodeNotification();
	    } else if (match_sval(SENP.KwdEvents)) {
		events = decodeNotifications();
	    } else if (match_sval(SENP.KwdPattern)) {
		pattern = decodePattern();
	    } else if (match_sval(SENP.KwdDistanceVector)) {
		distanceVector = decodeDistanceVector();
	    } else if (match_sval(SENP.KwdPred)) {
		predicate = decodePredicate();
	    }
	    return;
	default:
	    throw (new SENPInvalidFormat(ErrParam, pos));
	}
    }
}
