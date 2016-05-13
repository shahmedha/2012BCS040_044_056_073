//
//  This file is part of Siena, a wide-area event notification system.
//  See http://www.inf.usi.ch/carzaniga/siena/
//
//  Author: Antonio Carzaniga (firstname.lastname@usi.ch)
//  See the file AUTHORS for full details. 
//
//  Copyright (C) 1998-2003 University of Colorado
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

import java.util.HashMap;
import java.util.Iterator;

class SequencerRecord {
    Notification n;
    long seq_num;
    long timestamp;
    
    SequencerRecord() {
	n = null;
	seq_num = 0;
	timestamp = 0; 
    }

    void assign(Notification nx, long sx) {
	n = nx;
	seq_num = sx;
	timestamp = System.currentTimeMillis();
    }

    void clear() {
	n = null;
	seq_num = 0;
	timestamp = 0;
    }
}

class NotificationSequencer {
    static String SEQ_NUM = "seq_num$$";
    static String SEQ_ID = "seq_id$$";
}

class Sequencer {
    private SequencerRecord [] buf;
    private long latest_sent;
    private int first;
    private int count;
    private boolean notifier_loop_busy;
    private long latency;
    long time_stamp;

    Sequencer(int size, long l) {
	buf = new SequencerRecord[size];
	for(int i = 0; i < size; ++i)
	    buf[i] = new SequencerRecord();
	first = 0;
	count = 0;
	notifier_loop_busy = false;
	latency = l;
	latest_sent = -1;
	time_stamp = -1;
    }
    
    void insert(Notification n, long seq) {
	//
	// PRECONDITION: count < buf.length and synchronized(this)
	//
	SequencerRecord tmp;
	int i = (first + count) % buf.length; // i = last;
	int j;
	buf[i].assign(n, seq);
	while(i != first) {
	    j = ((i == 0) ? buf.length : i) - 1;
	    if (buf[i].seq_num > buf[j].seq_num)
		break;
	    tmp = buf[i];
	    buf[i] = buf[j];
	    buf[j] = tmp;
	    i = j;
	}
	++count;
    }

    void notify_first(Notifiable client) {
	//
	// PRECONDITION: count > 0 and synchronized(this)
	//
	try { 
	    client.notify(buf[first].n);

	} catch (SienaException ex) { 
				// I'm not sure about ignoring this 
				// but I also don't know what to do with it
	}			// ...design decision pending...
	latest_sent = buf[first].seq_num;
	buf[first].clear();	// this is probably useless
	first = (first + 1) % buf.length;
	--count;
    }

    synchronized public boolean isEmpty() {
	return count == 0;
    }

    synchronized public void notify(Notifiable client, Notification n, 
				    long seqn) throws SienaException {
	// I drop events with seq# in the past
	if (seqn <= latest_sent) 
	    return;

	// I immediately notify events that come in
	// in the right order (no gaps in sequence #).
	if (seqn == latest_sent + 1) {
	    latest_sent = seqn;
	    try { 
		client.notify(n);
		if (notifier_loop_busy)
		    this.notify();
	    } catch (SienaException ex) {
		if (notifier_loop_busy)
		    this.notify();
		throw(ex);
	    }
	    return;
	}
	// we get here if n is out of order, therefore we must
	// stick n into the buffer.  If the buffer is full we free
	// a position by notifying the first notification in the
	// buffer.
	if (count == buf.length) {
	    notify_first(client);
	}
	insert(n, seqn);	// in any case we insert n in the buffer

	if (notifier_loop_busy) {
	    this.notify();
	    return;
	}
	notifier_loop_busy = true; // here we get into the notifier loop
	while (count > 0) {
	    long slack;
	    if (buf[first].seq_num == (latest_sent + 1) || 
		(slack = System.currentTimeMillis() - buf[first].timestamp)
		>= latency) {
		notify_first(client);
	    } else {
		try {
		    this.wait(latency - slack);
		} catch (InterruptedException ex) {
		    // I'm not sure about ignoring this exception
		    // but I also don't know what to do with it
		}   // ...design decision pending...
	    } 
	}
	notifier_loop_busy = false;
    }
}

/** a notifiable wrapper that attempts to deliver notifications in the
 *  order they were published.
 *
 *  An <em>input sequencer</em> can be used to provide
 *  sequence-ordered delivery of notifications to a subscriber.  An
 *  input sequencer works, on the subscriber side, together with one
 *  or more <em>output sequencers</em>, each one associated with a
 *  publisher.  An ouput sequencer identifies a <em>logical
 *  sequence</em> of notifications.  A publisher that intends to
 *  publish notifications within a sequence must publish them through
 *  an output sequencer (see {@link
 *  NotificationOutputSequencer#publish(Notification)} or {@link
 *  NotificationOutputSequencer#tagNotification(Notification)}).
 *  Notifications published through an output sequencer are
 *  <em>tagged</em> with a <em>sequence id</em>, identifying that
 *  output sequencer, and therefore the logical sequence, and a
 *  <em>sequence number</em>, identifying a particular message within
 *  the same logical sequence.
 *
 *  <p>An input sequencer acts as a wrapper for a subscriber,
 *  intercepting notifications destined to the subscriber, and
 *  forwarding them on to the subscriber so as to guarantee the
 *  sequential ordering of messages within each sequence.  In
 *  particular, an input sequencer maintains a table containing a
 *  buffer and some additional information for each active sequence.
 *  The input sequencer then processes incoming notifications as
 *  follows:
 *
 *  <ul> 
 *
 *  <li>notifications that are not associated with any sequence are
 *  immediately passed on to the subscriber;
 *
 *  <li>notifications that are associated with a sequence <em>S</em>
 *  and that have a sequence number that does not create any gaps with
 *  previously notified notifications in <em>S</em>, are also
 *  immediately passed on the the subscriber;
 *
 *  <li>notifications that are associated with a sequence <em>S</em>
 *  and that have a sequence number that creates a gap with previously
 *  notified notifications in <em>S</em> are stored in a queue ordered
 *  by sequence number.
 *
 *  <li>the first notification stored in the queue is passed on to the
 *  subscriber as soon as the gaps in the notifications sequence are
 *  correctly filled, or as soon as the notification buffer is full,
 *  or after a configurable amount of time, measured since the time
 *  the notification was received and inserted into the buffer.
 *
 *  <li>notifications that have a sequence number lower than the one
 *  of the latest notification passed to the subscriber are dropped.
 *
 *  </ul>
 *
 *  <p>In order to support multiple sequences, an input sequencer
 *  maintains a buffer and a counter for each sequence.  The buffer
 *  and couter for sequence <em>S</em> are created as soon as the
 *  sequencer receives the first notification tagged with sequence id
 *  <em>S</em>.  Since the sequencer can not determine when a sequence
 *  is no longer active, the sequencer deallocates buffers and
 *  counters by running a garbage-collection function at regular
 *  intervals.  This fuction destroys sequence buffers and counters
 *  that have not been used for a configurable amount of time (see
 *  {@link NotificationInputSequencer#setCleanupInterval(int)} and
 *  {@link NotificationInputSequencer#setCleanupTimeout(long)}.)
 *
 *  <p>Example:
 *  <code><pre>
 *      Siena siena;
 *      Filter f;
 *      Notifiable n;
 *      // ...
 *      // siena = new ...
 *      // f = new Filter();
 *      // f.addConstraint ...
 *      // n = ...
 *      // ...
 *      NotificationInputSequencer sequencer;
 *      sequencer = new NotificationInputSequencer(n);
 *      siena.subscribe(f, sequencer);
 *      // ...
 *  </pre></code>
 *
 *  @see NotificationOutputSequencer 
 *  @see Notifiable
 **/
public class NotificationInputSequencer implements Notifiable {
    /** default size for notification buffers.
     *
     *  Initial value is 50.
     *
     *  @see #setBufferSize(int)
     **/
    public static int		DefaultBufferSize = 50;

    /** default maximum latency for buffered notifications
     *
     *  Initial value is 10000ms (i.e., 10 seconds).
     *
     *  @see #setLatency(long)
     **/
    public static long		DefaultLatency = 10000;

    /** default interval before sequencer cleans up its buffers table.
     *
     *  The sequencer uses a cleanup function to remove the state
     *  associated with sequences that are considered dead.  This
     *  cleanup function is executed periodically, when the sequencer
     *  sees a new sequece id.  This parameter determines the number
     *  of new sequence ids seen before the cleanup function is
     *  triggered.
     *
     *  <p>A negative value means <em>infinite</em>, that means that
     *  the cleanup function is never executed. Initial value is 16.
     *
     *  @see #setCleanupInterval(int)
     **/
    public static int		DefaultCleanupInterval = 16;

    /** default timeout before a sequence is considered dead.
     *
     *  Initial value is 600000 (i.e., 10 minutes).
     *
     *  @see #setCleanupTimeout(long)
     **/
    public static long		DefaultCleanupTimeout = 600000;

    private Notifiable client;
    private HashMap<String,Sequencer> sequencers;
    private int bufsize;
    private long latency;
    private int cleanup_interval;
    private long cleanup_timeout;
    private int cleanup_counter;

    /** creates an input sequencer that wraps the given notifiable 
     **/
    public NotificationInputSequencer(Notifiable c) {
	client = c;
	sequencers = new HashMap<String,Sequencer>();
	bufsize = DefaultBufferSize;
	latency = DefaultLatency;
	cleanup_counter = 0;
	cleanup_interval = DefaultCleanupInterval;
	cleanup_timeout = DefaultCleanupTimeout;
    }
    
    /** sets the maximum latency for queued notifications 
     *
     *  Notifications that would create a gap in their sequence are
     *  buffered by the sequencer.  The sequencer then waits for other
     *  notifications to come in to fill the sequence gap.  This
     *  parameter determines the maximum amount of time, in
     *  milliseconds, that a sequencer would wait before passing the
     *  notification on to the subscriber.
     *
     *  @see #DefaultLatency
     **/
    synchronized public void setLatency(long l) {
	latency = l;
    }

    /** sets the size of sequence buffers 
     *
     *  The sequencer stores out-of-sequence notifications in sequence
     *  buffers, waiting for notifications to come in to fill the
     *  sequence gaps.  This parameter determines the size of sequence
     *  buffers.
     *
     *  @see #DefaultBufferSize
     **/
    synchronized public void setBufferSize(int s) {
	bufsize = s;
    }

    /** sets the number of new sequences seen before a table cleanup 
     *
     *  The sequencer runs a periodic cleanup function to deallocate
     *  buffers and counters for inactive sequences.  A sequence is
     *  considered inactive is no messages are received for that
     *  sequence for more than a configurable amount of time (see
     *  {@link #setCleanupTimeout(long)}.)  This parameter determines
     *  how often this cleanup function is executed by the sequencer.
     *  In particular, the cleanup function is executed before a new
     *  sequence is activated, every <em>i</em> new sequences.
     *
     *  @see #DefaultCleanupInterval
     **/
    synchronized public void setCleanupInterval(int i) { 
	cleanup_interval = i;
    }

    /** sets the amount of idle time before a sequence buffer is
     *  considered inactive.
     *
     *  A sequence is considered inactive when no notifications are
     *  received for that sequence in more than <em>T</em>
     *  milliseconds.  This method sets the value of <em>T</em> for
     *  this sequencer.  The buffers and counters associated with
     *  inactive sequences are periodically garbage-collected and
     *  destroyed.
     *
     *  @see #DefaultCleanupTimeout
     **/
    synchronized public void setCleanupTimeout(long t) { 
	cleanup_timeout = t;
    }

    synchronized private void sequencers_cleanup() {
	long now = System.currentTimeMillis();
	Iterator<Sequencer> i = sequencers.values().iterator();
	while(i.hasNext()) {
	    if (i.next().time_stamp < now - cleanup_timeout)
		i.remove();
	}
    }

    synchronized private Sequencer get_sequencer(String id) {
	Sequencer s = sequencers.get(id);
	if (s == null) { 	// before creating a new sequencer, we 
				// see if it's time to garbage-collect
	    if (cleanup_interval > 0) {
		cleanup_counter = (cleanup_counter + 1) % cleanup_interval;
		if (cleanup_counter == 0)
		    sequencers_cleanup();
	    }

	    s = new Sequencer(bufsize, latency);
	    sequencers.put(id, s);
	}
	s.time_stamp = System.currentTimeMillis();
	return s;
    }

    public void notify(Notification n) throws SienaException {
	AttributeValue v;
	v = n.removeAttribute(NotificationSequencer.SEQ_ID);
	if (v == null || v.getType() != AttributeValue.STRING) {
	    client.notify(n);	// I immediately notify events that do not
	    return;		// have a sequence id
	}
	String id = v.stringValue();

	v = n.removeAttribute(NotificationSequencer.SEQ_NUM);
	if (v == null || v.getType() != AttributeValue.LONG) {
	    client.notify(n);	// I immediately notify events that do not
	    return;		// have a sequence number at all
	}
	get_sequencer(id).notify(client, n, v.longValue());
    }

    public void notify(Notification[] s) throws SienaException {
	client.notify(s);
    }
}
