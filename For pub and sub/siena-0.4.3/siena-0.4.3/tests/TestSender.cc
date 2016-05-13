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
// $Id: TestSender.cc,v 1.2 2003/07/09 20:43:16 carzanig Exp $
//
#include <unistd.h> // for usleep()

#include <iostream>
#include <exception>

#include <siena/Comm.h>

int main (int argc, char *argv[])
{
    char buf[1024];
    try {
	if (argc != 2) {
	    cerr << "usage: " << argv[0] << " <uri>" << endl;
	    return 1;
	}

	Sender * sender = Sender::create_sender(argv[1]);

	while (cin.getline(buf, 1024)) {
	    buf[1023]='\0';	    
	    sender->send(buf, strlen(buf));
	    cout << '.' << flush;
	    usleep(200);
	}
	sender->shutdown();
	cout << endl;
	delete(sender);
    } catch (exception &ex) {
	cout << "error: " << ex.what() << endl;
    }
    return 0;
}

