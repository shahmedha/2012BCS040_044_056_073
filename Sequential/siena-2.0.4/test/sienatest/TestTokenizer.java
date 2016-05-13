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

//
//  this test must be withing package siena because Tokenizer is
//  private
//
package siena;

public class TestTokenizer {

    public static void main(String[] args) {
	try {
	    int res;
	    SENPBuffer t = new SENPBuffer();

	    while ((res = System.in.read(t.buf, 0, SENP.MaxPacketLen)) > 0) {
		t.init(res);
		int type;
		while((type = t.nextToken()) != SENPPacket.T_EOF) {
		    switch(type) {
		    case SENPPacket.T_INT:
			System.out.println("int: " + Long.toString(t.ival));
			break;
		    case SENPPacket.T_DOUBLE:
			System.out.println("double: "+Double.toString(t.dval));
			break;
		    case SENPPacket.T_STR:
			System.out.println("str: " + t.sval_string());
			break;
		    case SENPPacket.T_ID:
			System.out.println("id: " + t.sval_string());
			break;
		    case SENPPacket.T_BOOL:
			System.out.println("bool: " + t.bval);
			break;
		    case SENPPacket.T_OP:
			System.out.println("op: " + new String(SENP.operators[t.oval]));
			break;
		    case SENPPacket.T_LPAREN:
			System.out.println("{");
			break;
		    case SENPPacket.T_RPAREN:
			System.out.println("}");
			break;
		    case SENPPacket.T_UNKNOWN:
			System.out.println("unknown");
			break;
		    } 
		}
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
}
