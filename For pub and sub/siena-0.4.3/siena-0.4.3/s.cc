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
// $Id: subscriber.cc,v 1.1 2003/07/09 20:15:55 carzanig Exp $
//



#include <iterator>
#include <vector>
#include <iostream>
#include <fstream>
#include <string>
#include <sstream>
#include <string.h>
#include <stdlib.h>

#include <iostream>
#include <exception>
						// include Siena headers
#include <siena/Siena.h>
#include <siena/ThinClient.h>
#include <time.h>
#include <ctime>

static void print_usage(const char * progname) {
    cerr << "usage: " << progname 
	 << " [-udp] [-port <num>] [-host <host>] <uri-master>" << endl;
}

int main (int argc, char *argv[])
{
    bool udp = false;
    unsigned short port = 1973;
    const char * master = NULL;
    const char * thishost = NULL;
    
    try {
        int i;
	for(i=1; i < argc; ++i) {		// parse cmd-line params
	    if(strcmp(argv[i], "-udp")==0) {
		udp = true;
	    } else if (strcmp(argv[i], "-port")==0) {
		if (++i < argc) {
		    port = atoi(argv[i]);
		} else {
		    print_usage(argv[0]);
		    return 1;
		}
	    } else if (strcmp(argv[i], "-host")==0) {
		if (++i < argc) {
		    thishost = argv[i];
		} else {
		    print_usage(argv[0]);
		    return 1;
		}
	    } else {
		master = argv[i];
	    }
	}
	if (master == NULL) {
	    print_usage(argv[0]);
	    return 1;
	}

	ThinClient siena(master);		// create interface to
	Receiver * r;				// given master server
	if (udp) {
	    r = new UDPReceiver(port, thishost);// create receiver for 
	} else {				// this interface
	    r = new TCPReceiver(port, thishost);
	}
	int ns=0;
	siena.set_receiver(r);	
		// set receiver
	cout<< "Enter no. of subscriptions:";
	cin>>ns;
	cout<< "no. of subscriptions:"<<ns;
	Filter f[ns];
	string line;
	string book_name;
	int id;
	string auth;
	int price;	
	int limit;
	  int rating;
	  string genre;
	  int edition;
	  string payment_mode;
	  int year;
	cout<<"hhfjh";
	ifstream myfile ("example1.txt");
	if (myfile.is_open())
	{	
		cout<<"hhhh";
	   int k=0;
	   while(getline (myfile,line) && k<ns){
	    
	       std::istringstream iss(line);
	      std::vector<string> tokens;
	      
	      tokens.clear();
	      copy(istream_iterator<string>(iss),
		     istream_iterator<string>(),
		     back_inserter(tokens));
	      book_name=tokens.at(0);
	      id=atoi(tokens.at(1).c_str());
	      auth=tokens.at(2);
	      price=atoi(tokens.at(3).c_str());
	      limit=atoi(tokens.at(4).c_str());
		rating=atoi(tokens.at(5).c_str());
		genre=tokens.at(6);
		edition=atoi(tokens.at(7).c_str());
		payment_mode=tokens.at(8);
		year=atoi(tokens.at(9).c_str());
					// create subscription filter
	//cout<<"\nBook Name:"<< book_name<<"  Id:"<<id<<"  Author:" <<auth<<"  Price:"<<price<<endl;
	f[k].add_constraint("Book_name", SX_eq, book_name);
	f[k].add_constraint("ID", SX_eq, (int)id);
	f[k].add_constraint("Author", SX_eq, auth);
	f[k].add_constraint("Price",SX_lt, (int)price);	
	f[k].add_constraint("Limit_in_days", SX_eq, (int)limit);
	f[k].add_constraint("Rating", SX_eq, (int)rating);
	f[k].add_constraint("Genre", SX_eq, genre);
	f[k].add_constraint("Edition", SX_eq, (int)edition);
	f[k].add_constraint("Payment_Mode", SX_eq, payment_mode);
	f[k].add_constraint("Year_of_publish", SX_eq, (int)year);

	

	siena.subscribe(f[k]);	
		// subscribe
	    
	
	//while complete
	
	k++;
	}
	 myfile.close();
	}
	 else
		 cout << "Unable to open file"; 
	for(int k=0;k<ns;k++)
	{
	Notification * n;
	// read incoming notifications	
        n = siena.get_notification();
	if (n != NULL)  
	{
		cout << " Book Name:"<< (*n)["Book_name"].string_value()
		     << " ID:"       << (*n)["ID"].int_value()
		     << " Author:"   << (*n)["Author"].string_value()
		     << " Price:"    << (*n)["Price"].int_value()
                     << " Limit_in_days:"    << (*n)["Limit_in_days"].int_value()
		     << " Rating:"    << (*n)["Rating"].int_value()
	             << " Genre:"    << (*n)["Genre"].int_value()
		     << " Edition:"    << (*n)["Edition"].int_value()
		     << " Payment_Mode:"    << (*n)["Payment_Mode"].int_value()
		     << " Year_of_publish:"       << (*n)["Year_of_publish"].int_value();
	
		delete(n);	
        }
	//cout<<"\nK:"<<k<<endl;
		// unsubscribe and shutdown 
	}
	siena.unsubscribe();		
	siena.shutdown();			// interface
	delete(r);
    } catch (exception &ex) {
	cout << "error: " << ex.what() << endl;
    }
    return 0;
}
