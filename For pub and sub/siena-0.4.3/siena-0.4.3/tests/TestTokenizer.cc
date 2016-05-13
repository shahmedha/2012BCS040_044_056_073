// -*- C++ -*-
//
//  This file is part of Siena, a wide-area event notification system.
//  See http://www.cs.colorado.edu/serl/dot/siena.html
//
//  Author: Antonio Carzaniga <carzanig@cs.colorado.edu>
//  See the file AUTHORS for full details. 
//
//  Copyright (C) 1998-2000 University of Colorado
//
//  This program is free software; you can redistribute it and/or
//  modify it under the terms of the GNU General Public License
//  as published by the Free Software Foundation; either version 2
//  of the License, or (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307,
//  USA, or send email to serl@cs.colorado.edu.
//
//
// $Id: TestTokenizer.cc,v 1.2 2003/07/09 20:43:17 carzanig Exp $
//
#include <iostream>
#include <exception>

#include <siena/SENP.h>

//
// this gives you access to the Siena headers (and to the Siena namespace)
//
#include "SENPTokenizer.h"

int main (int argc, char *argv[])
{
    string line;
    char buf[1024];

    try {
	while(cin.getline(buf, 1024)) {
	    buf[1023]='\0';
	    string line(buf);
	    SENPTokenizer t(line);
	    int tok;
	    while((tok = t.nextToken()) != SENPTokenizer::T_EOF) {
		switch(tok) {
		case SENPTokenizer::T_INT:
		    cout << "int: " << t.ival << endl;
		    break;
		case SENPTokenizer::T_DOUBLE:
		    cout << "double: " << t.dval << endl;
		    break;
		case SENPTokenizer::T_STR:
		    cout << "str: " << t.sval << endl;
		    break;
		case SENPTokenizer::T_ID:
		    cout << "id: " << t.sval << endl;
		    break;
		case SENPTokenizer::T_BOOL:
		    cout << "bool: " << t.bval << endl;
		    break;
		case SENPTokenizer::T_OP:
		    cout << "op: " + SENP::Operators[t.oval] << endl;
		    break;
		case SENPTokenizer::T_LPAREN:
		    cout << "{" << endl;
		    break;
		case SENPTokenizer::T_RPAREN:
		    cout << "}" << endl;
		    break;
		case SENPTokenizer::T_UNKNOWN:
		    cout << "unknown" << endl;
		    break;
		}
	    }
	}
    } catch (exception &ex) {
	cerr << "error: " << ex.what() << endl;
    }
}

