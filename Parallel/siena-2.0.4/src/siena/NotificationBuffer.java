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

import java.util.LinkedList;

interface ObjectBuffer {
    public void put(Object o);
    public boolean isEmpty();
    public Object get();
    public int size();
}

class StaticBuffer implements ObjectBuffer {
    int count = 0;
    int first = 0;
    Object [] objs;
    public StaticBuffer(int dim) {
	objs = new Object[dim];
    }
    
    public void put(Object o) {
	if (count == objs.length) {
	    objs[first] = o;
	    first = (first + 1) % objs.length;
	} else {
	    objs[(first + count) % objs.length] = o;
	    ++count;
	}
    }

    public boolean isEmpty() {
	return count == 0;
    } 

    public Object get() {
	if (count == 0) return null;
	Object res = objs[first];
	objs[first] = null;
	first = (first + 1) % objs.length;
	--count;
	return res;
    }
    public int size() {
	return count;
    } 
}

class DynamicBuffer implements ObjectBuffer {
    LinkedList<Object> objs;

    public DynamicBuffer() {
	objs = new LinkedList<Object>();
    }
    
    public void put(Object o) {
	objs.addLast(o);
    }

    public boolean isEmpty() {
	return objs.isEmpty();
    } 

    public Object get() {
	if (objs.isEmpty()) return null;
	return objs.removeFirst();
    }

    public int size() {
	return objs.size();
    }
}

/** a "mailbox" for notifications
 *
 *  functions as a proxy notifiable.  It receives and stores
 *  notifications and sequences of notifications from Siena.
 *  Notifications and sequences can be retrieved synchronously
 *  (blocking) or asynchronously (non-blocking).
 *
 *  <p>Example:
 *  <code><pre>
 *      Siena siena;
 *      Filter f;
 *      // ...
 *      // siena = new ...
 *      // f = new Filter();
 *      // f.addConstraint ...
 *      // ...
 *      NotificationBuffer queue = new NotificationBuffer();
 *      siena.subscribe(f, queue);
 *      Notification n = queue.getNotification(-1); // infinite timeout
 *      System.out.println(n.toString());
 *  </pre></code>
 *
 *  <p>Notice that <code>NotificationBuffer</code> handles notifications and
 *  sequences (of notifications) separately.
 *
 *  @see Notification 
 *  @see Notifiable
 **/
public class NotificationBuffer
 implements Notifiable {
    private ObjectBuffer notifications;
    private ObjectBuffer sequences;

    /** constructs an empty NotificationBuffer with unlimited capacity
     *
     *  this NotificationBuffer will grow dynamically to store any number of
     *  notifications and sequences.
     **/
    public NotificationBuffer() {
	notifications = new DynamicBuffer();
	sequences = new DynamicBuffer();
    }
    
    /** constructs an empty NotificationBuffer with limited capacity
     *
     *  this NotificationBuffer will hold up to <em>dimension</em>
     *  notifications and <em>dimension</em> sequences.  If more
     *  notifications (or sequences) are received, stored
     *  notifications (or sequences) will be discarded on a
     *  first-in-first-out basis.
     *
     *  @param dimension maximum capacity.  Must be &gt; 0
     **/
    public NotificationBuffer(int dimension) {
	notifications = new StaticBuffer(dimension);
	sequences = new StaticBuffer(dimension);
    }
    
    /** number of available notifications
     *
     *  @return count of available notifications
     **/
    public int notificationsCount() {
	return notifications.size();
    }

    /** the number of available sequences of notifications
     *
     *  @return count of available sequences
     **/
    public int sequencesCount() {
	return sequences.size();
    }

    public void notify(Notification n) {
	synchronized (notifications) {
	    notifications.put(new Notification(n));
	    notifications.notify();
	}
    }
    
    public void notify(Notification[] s) {
	synchronized (sequences) {
	    sequences.put(s);
	    sequences.notify();
	}
    }

    /** attempts to extract a notification (non-blocking)
     *
     *  attempts to extract a notification.  This method returns
     *  immediately.  It returns the first available notification or
     *  <code>null</code> if none is available.
     *
     *  @return first available notification or <code>null</code> 
     **/
    public Notification getNotification() {
	synchronized (notifications) {
	    return (Notification)notifications.get();
	}
    }

    /** attempts to extract a notification (blocking)
     *
     *  attempts to extract a notification.  This method might block
     *  if no notification is available.  If the given
     *  <em>timeout</em> is &gt; 0, this method blocks for at most
     *  <em>timeout</em> milliseconds, otherwise (when
     *  <em>timeout</em> &lt;= 0) it blocks until a notification
     *  becomes available.  It returns the first available
     *  notification or <code>null</code> in case the timeout expired.
     *
     *  @return first available notification or <code>null</code> 
     **/
    public Notification getNotification(long timeout) 
	throws InterruptedException {
	synchronized (notifications) {
	    if (timeout > 0) {
		if (notifications.isEmpty())
		    notifications.wait(timeout);
	    } else {
		while(notifications.isEmpty())
		    notifications.wait();
	    }
	    return (Notification)notifications.get();
	}
    }

    public Notification [] getSequence() throws InterruptedException {
	synchronized (sequences) {
	    return (Notification[]) sequences.get();
	}
    }

    /** attempts to extract a sequence of notification (non-blocking)
     *
     *  attempts to extract a sequence of notifications.  This method
     *  might block if no notification is available.  If the given
     *  <em>timeout</em> is &gt; 0, this method blocks for at most
     *  <em>timeout</em> milliseconds, otherwise (when
     *  <em>timeout</em> &gt;= 0) it blocks until a notification
     *  becomes available.  It returns the first available
     *  notification or <code>null</code> in case the timeout expired.
     *
     *  @return first available notification or <code>null</code> 
     **/
    public Notification [] getSequence(long timeout) 
	throws InterruptedException {
	synchronized (sequences) {
	    if (timeout > 0) {
		if (sequences.isEmpty())
		    sequences.wait(timeout);
	    } else {
		while(sequences.isEmpty())
		    sequences.wait();
	    }
	    return (Notification [])sequences.get();
	}
    }
}
