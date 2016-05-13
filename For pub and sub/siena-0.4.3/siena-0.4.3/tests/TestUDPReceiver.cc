// -*- C++ -*-
//
//  This file is part of Siena, a wide-area event notification system.
//  See http://www.cs.colorado.edu/serl/siena/
//
//  Author: Antonio Carzaniga <carzanig@cs.colorado.edu>
//  See the file AUTHORS for full details. 
//
//  Copyright (C) 1998-2001 University of Colorado
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
// $Id: TestUDPReceiver.cc,v 1.2 2003/07/09 20:43:17 carzanig Exp $
//
#include <iostream>
#include <exception>

#include <siena/Comm.h>

int main (int argc, char *argv[])
{
#define BUFLEN 1024
    char buf[BUFLEN];
    try {
	const char * hostname = NULL;
	int port = 0;
	switch(argc) {
	case 3: hostname = argv[2];
	case 2: port = atoi(argv[1]); break;
	default:
	    cerr << "usage: " << argv[0] << " <port> [host]" << endl;
	    return 1;
	}
	
	UDPReceiver receiver(port, hostname);
	size_t res;
	do {
	    res = receiver.receive(buf, BUFLEN);
	    buf[res] = '\0';
	    cout << buf << endl;
	} while (strcmp(buf, "THE END") != 0);
	receiver.shutdown();
    } catch (exception &ex) {
	cout << "error: " << ex.what() << endl;
    }
    return 0;
}

