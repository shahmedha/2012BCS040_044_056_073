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
package siena.dvdrp;

import java.util.LinkedList;


/**
 * a simple implementation of a priority queue with only two priorities.
 * 
 * The queue is used to hold DV and UDV messages to be sent by
 * <code>siena.DispatcherThread</code>
 * 
 */
public class TwoPrioritiesListQueue {
    private LinkedList<Object> l1 = new LinkedList<Object>();
    private LinkedList<Object> l2 = new LinkedList<Object>();
    private volatile boolean release = false;
    public static int HIGH = 1;
    public static int LOW = 2;
	
    synchronized public void add(Object obj, int priority) {
	if (obj != null){
	    if (priority == HIGH){
		l1.add(obj);
	    } else if (priority == LOW){
		l2.add(obj);
	    }
	}
	notifyAll();
    }
	
    synchronized public Object next() {
	while((l1.size() == 0 && l2.size() == 0) && !release) {
	    try{
		wait();
	    } catch (InterruptedException ie) {
		// we continue but we'll eventually get out, we're probably stopping the server				
	    }
	}
	if (l1.size() > 0 ){
	    return l1.removeFirst();				
	} else if (l2.size() > 0) {
	    return l2.removeFirst();
	}
	return null;
    }
	
    synchronized public void clear(){
	l1.clear();
	l2.clear();
	release = true;
	notifyAll();
    }
}