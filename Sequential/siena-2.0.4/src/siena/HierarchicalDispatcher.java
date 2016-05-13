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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import siena.comm.GenericSenderFactory;
import siena.comm.InvalidSenderException;
import siena.comm.PacketReceiver;
import siena.comm.PacketReceiverClosed;
import siena.comm.PacketReceiverException;
import siena.comm.PacketReceiverFatalError;
import siena.comm.PacketSender;
import siena.comm.PacketSenderException;
import siena.comm.PacketSenderFactory;
import siena.comm.TCPPacketReceiver;
import siena.fwd.BadConstraintException;
import siena.fwd.MatchMessageHandler;
import siena.fwd.Message;
import siena.fwd.SFFTable;

//
// this is the abstraction of the subscriber used by the
// HierarchicalDispatcher.  It represents remote as well as local
// notifiable objects.  In addition to that, this object keeps track
// of failed attempts to contact the notifiable object so that
// HierarchicalDispatcher can periodically clean up its subscriber
// tables
//
abstract class Subscriber {
    public short failed_attempts = 0;

    public long first_error_time = 0;

    protected boolean suspended = false;

    int refcount = 0;

    abstract public String getKey();

    abstract public boolean isLocal();

    byte[] identity;

    public byte[] getIdentity() {
	return identity;
    }

    synchronized public void addRef() {
	++refcount;
    }

    synchronized public void removeRef() {
	if (refcount > 0)
	    --refcount;
    }

    synchronized public boolean hasNoRefs() {
	return refcount == 0;
    }

    synchronized public void mapHandler(Interface i) {
    }

    synchronized public void suspend() {
	suspended = true;
    }

    synchronized public void resume() {
	suspended = false;
    }

    //
    // returns false whenever the notification mechanism failed, and
    // therefore this notifiable could be considered unreachable (am I
    // making sense? this sentence might sound a bit "italian", but I
    // don't have time to fix it now)
    //
    abstract public boolean notify(SENPPacket pkt);

    abstract public void notify(Notification n);

    abstract public void notify(Notification[] s);

    protected void handle_notify_error(Exception ex) {
	if (failed_attempts == 0)
	    first_error_time = System.currentTimeMillis();
	++failed_attempts;
	Logging.prlnerr("error (" + failed_attempts + ") notifying "
			+ toString() + ": " + ex.toString());
	// TOF
	ex.printStackTrace(Logging.err);
    }

    synchronized public long getMillisSinceGood() {
	if (suspended || failed_attempts == 0)
	    return 0;
	return System.currentTimeMillis() - first_error_time;
    }

    synchronized public short getFailedAttempts() {
	if (suspended)
	    return 0;
	return failed_attempts;
    }

    public void shutdown() {
    }

    public String toString() {
	return getKey().toString();
    }

    public void writeIdentityTo(SENPWriter out) throws IOException {
	//
	// defined by subclasses as needed
	//
    }

    public void writeContactTo(SENPWriter out) throws IOException {
	//
	// defined by subclasses as needed
	//
    }
}

class LocalSubscriber extends Subscriber {
    private Notifiable localobj;

    public LocalSubscriber(Notifiable n) {
	localobj = n;
	identity = localobj.toString().getBytes();
    }

    public String getKey() {
	return localobj.toString();
    }

    @Override
	public byte[] getIdentity() {
	return localobj.toString().getBytes();
    }

    public boolean isLocal() {
	return true;
    }

    public int hashCode() {
	return localobj.hashCode();
    }

    synchronized public void mapHandler(Interface i) {
	//
	// this should probably be signaled as an error---I don't
	// think it makes sense to remap a local notifiable.
	//
    }

    synchronized public boolean notify(SENPPacket pkt) {
	if (suspended)
	    return true;
	try {
	    localobj.notify(pkt.event);
	    failed_attempts = 0;
	    return true;
	} catch (Exception ex) {
	    handle_notify_error(ex);
	    return false;
	}
    }

    synchronized public void notify(Notification n) {
	if (suspended)
	    return;
	try {
	    localobj.notify(n);
	    failed_attempts = 0;
	} catch (Exception ex) {
	    handle_notify_error(ex);
	}
    }

    synchronized public void notify(Notification[] s) {
	if (suspended)
	    return;
	try {
	    //
	    // here I purposely do not duplicate the sequence for
	    // efficiency reasons. Clients should never modify
	    // objects passed through notify().
	    //
	    localobj.notify(s);
	    failed_attempts = 0;
	} catch (Exception ex) {
	    handle_notify_error(ex);
	}
    }
}

class RemoteSubscriber extends Subscriber {
    private String identity_s = null;

    public Interface interf; // I couldn't figure out a better

    // identifier that would not clash
    // with Java keywords (interface, if,
    // int)
    private SENPPacket spkt;

    public RemoteSubscriber(byte[] id, Interface i) {
	identity = id;
	identity_s = new String(id);
	interf = i;
	interf.add_ref();
	spkt = new SENPPacket();
    }

    public byte[] getIdentity() {
	return identity;
    }

    public String getKey() {
	return identity_s;
    }

    synchronized public void mapHandler(Interface i) {
	if (interf != null)
	    interf.remove_ref();
	interf = i;
	interf.add_ref();
	suspended = false;
	first_error_time = 0;
	failed_attempts = 0;
    }

    public boolean isLocal() {
	return false;
    }

    public int hashCode() {
	return identity.hashCode();
    }

    synchronized public boolean notify(SENPPacket pkt) {
	if (suspended)
	    return true;
	try {
	    interf.send(pkt.buf, pkt.encode());
	    failed_attempts = 0;
	    return true;
	} catch (PacketSenderException ex) {
	    handle_notify_error(ex);
	    return false;
	}
    }

    synchronized public void notify(Notification n) {
	if (suspended)
	    return;
	try {
	    spkt.init();
	    spkt.id = HierarchicalDispatcher.master_id;
	    spkt.method = SENP.PUB;
	    spkt.event = n;
	    spkt.to = identity;
	    interf.send(spkt.buf, spkt.encode());
	    failed_attempts = 0;
	} catch (PacketSenderException ex) {
	    handle_notify_error(ex);
	}
    }

    synchronized public void notify(Notification[] s) {
	if (suspended)
	    return;
	try {
	    spkt.init();
	    spkt.id = HierarchicalDispatcher.master_id;
	    spkt.method = SENP.PUB;
	    spkt.events = s;
	    spkt.to = identity;
	    interf.send(spkt.buf, spkt.encode());
	    failed_attempts = 0;
	} catch (PacketSenderException ex) {
	    handle_notify_error(ex);
	}
    }

    public void writeIdentityTo(SENPWriter out) throws IOException {
	out.write(0x53 /* 'S' */);
	out.write(identity);
	out.write(0x0a /* '\n' */);
    }

    public void writeContactTo(SENPWriter out) throws IOException {
	out.write(0x49 /* 'I' */);
	out.write(identity);
	out.write(0x0a /* '\n' */);
	interf.writeTo(out);
    }

    public void shutdown() {
	if (interf != null) {
	    interf.remove_ref();
	    interf = null;
	}
    }

    protected void finalize() throws Throwable {
	shutdown();
    }
}

/*
class SubscriberIterator {
    private Iterator<Subscriber> si;

    SubscriberIterator(Iterator<Subscriber> i) {
	si = i;
    }

    public boolean hasNext() {
	return si.hasNext();
    }

    public Subscriber next() {
	return si.next();
    }
}

class SSet {
    protected Set<Subscriber> subs;

    public SSet() {
	subs = new HashSet<Subscriber>();
    }

    public boolean addAll(SSet s) {
	return subs.addAll(s.subs);
    }

    public boolean add(Subscriber s) {
	return subs.add(s);
    }

    public boolean contains(Subscriber s) {
	return subs.contains(s);
    }

    public boolean remove(Subscriber s) {
	return subs.remove(s);
    }

    public boolean isEmpty() {
	return subs.isEmpty();
    }

    public SubscriberIterator iterator() {
	return new SubscriberIterator(subs.iterator());
    }
}
*/

class RefSSet extends HashSet<Subscriber> {
    static final long serialVersionUID = 1L;

    public RefSSet() {
	super();
    }

    public boolean addAll(Set<Subscriber> s) {
	boolean result = false;
	for (Subscriber sub : this) {
	    if (super.add(sub)) {
		sub.addRef();
		result = true;
	    }
	}
	return result;
    }

    public boolean add(Subscriber s) {
	if (super.add(s)) {
	    s.addRef();
	    return true;
	} else {
	    return false;
	}
    }

    public boolean remove(Subscriber s) {
	if (super.remove(s)) {
	    s.removeRef();
	    return true;
	} else {
	    return false;
	}
    }

    protected void finalize() throws Throwable {
	for (Subscriber s : this)
	    s.removeRef();
    }
}

class Subscription {
    public Set<Subscription> preset;

    public Set<Subscription> postset;

    public final Filter filter;

    public RefSSet subscribers;

    public Subscription(Filter f) {
	preset = new HashSet<Subscription>();
	postset = new HashSet<Subscription>();
	filter = new Filter(f);
	subscribers = new RefSSet();
    }

    public Subscription(Filter f, Subscriber s) {
	preset = new HashSet<Subscription>();
	postset = new HashSet<Subscription>();
	filter = new Filter(f);
	subscribers = new RefSSet();
	subscribers.add(s);
    }

    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append("SUB: " + Integer.toString(hashCode()));
	sb.append("\npreset:");
	for (Subscription s : preset) 
	    sb.append(" " + Integer.toString(s.hashCode()));
	sb.append("\npostset:");
	for (Subscription s : postset) 
	    sb.append(" " + Integer.toString(s.hashCode()));
	sb.append("\n" + filter.toString());
	sb.append("\nsubscribers:");
	for (Subscriber s : subscribers)
	    sb.append(" " + Integer.toString(s.hashCode()));
	sb.append("\n");
	return sb.toString();
    }

    public void writeTo(SENPWriter out) throws IOException {
	out.write(0x46 /* 'F' */);
	out.encodeSimple(filter);
	out.write(0x0a /* '\n' */);
	Iterator<Subscriber> si;
	for (si = subscribers.iterator(); si.hasNext();)
	    si.next().writeIdentityTo(out);
    }
}

class Poset {
    private int mods_since_save = 0;

    private int time_since_save = 0;

    private Set<Subscription> roots;

    public Poset() {
	roots = new HashSet<Subscription>();
    }

    public void clear() {
	roots.clear();
    }

    public boolean is_root(Subscription s) {
	return s.preset.isEmpty();
    }

    private boolean empty() {
	return roots.isEmpty();
    }

    public Iterator<Subscription> rootsIterator() {
	return roots.iterator();
    }

    private void insert(Subscription s, Collection<Subscription> pre, Collection<Subscription> post) {
	//
	// inserts new_sub into the poset between pre and post. the
	// connections are rearranged in order to maintain the
	// properties of the poset
	//
	if (pre.isEmpty()) {
	    roots.add(s);
	    roots.removeAll(post);
	} else {
	    for(Subscription pre_s : pre) {
		for(Subscription post_s : post) 
		    disconnect(pre_s, post_s);
		connect(pre_s, s);
	    }
	}
	for(Subscription post_s : post) 
	    connect(s, post_s);

	++mods_since_save;
    }

    private void disconnect(Subscription x, Subscription y) {
	if (x.postset.remove(y))
	    y.preset.remove(x);
    }

    private void connect(Subscription x, Subscription y) {
	if (x.postset.add(y))
	    y.preset.add(x);
    }

    public Set<Subscription> remove(Subscription s) {
	//
	// removes s from the poset returning the set of root
	// subscription uncovered by s
	//
	Set<Subscription> result = new HashSet<Subscription>();
	//
	// 1. disconnect s from every successor of s but maintains s.postset
	//
	for(Subscription y : s.postset)
	    y.preset.remove(s);

	if (s.preset.isEmpty()) {
	    //
	    // 2.1 if s is a root subscription, adds as a root every
	    // successor that remains with an empty preset, i.e.,
	    // every subscription that was a successor of s and that
	    // is now a root subscription...
	    //
	    for(Subscription y : s.postset)
		if (y.preset.isEmpty()) {
		    roots.add(y);
		    result.add(y);
		}
	    roots.remove(s);
	} else {
	    //
	    // 2.2 disconnects every predecessor of s thereby reconnecting
	    // predecessors to successors. A predecessor X is re-connected
	    // to a successor Y only if X does not have an immediate
	    // successor X' that covers Y (see is_indirect_successor).
	    //
	    for(Subscription x : s.preset) {
		x.postset.remove(s);
		for(Subscription y : s.postset)
		    if (!is_indirect_successor(x, y))
			connect(x, y);
	    }
	}
	return result;
    }

    private boolean is_indirect_successor(Subscription x, Subscription y) {
	//
	// says whether x indirectly covers y.
	//
	Iterator<Subscription> i = x.postset.iterator();
	while (i.hasNext())
	    if (Covering.covers((i.next()).filter, y.filter))
		return true;
	return false;
    }

    private Set<Subscription> predecessors(Filter f, Subscriber s) {
	//
	// computes the set of immediate predecessors of filter f that
	// do not contain subscriber s. If the poset contains any
	// subscription covering f that already contains s, then this
	// function returns null. Otherwise, it returns the
	// collection of predecessors of f. If the poset contains a
	// filter f'=f, the result will be a set containing that
	// (only) subscription.
	// 
	LinkedList<Subscription> to_visit = new LinkedList<Subscription>();
	Set<Subscription> visited = new HashSet<Subscription>();
	Subscription sub, y;
	Iterator<Subscription> i = roots.iterator();
	boolean found_lower;

	while (i.hasNext()) {
	    sub = i.next();
	    if (Covering.covers(sub.filter, f)) {
		if (sub.subscribers.contains(s)) {
		    return null;
		} else {
		    to_visit.addLast(sub);
		}
	    }
	}
	Set<Subscription> result = new HashSet<Subscription>();

	ListIterator<Subscription> li;
	while ((li = to_visit.listIterator()).hasNext()) {
	    sub = li.next();
	    li.remove();
	    i = sub.postset.iterator();
	    found_lower = false;
	    while (i.hasNext()) {
		y = i.next();
		if (visited.add(y)) {
		    if (Covering.covers(y.filter, f)) {
			found_lower = true;
			if (sub.subscribers.contains(s)) {
			    return null;
			} else {
			    to_visit.addLast(y);
			}
		    }
		} else if (!found_lower) {
		    if (Covering.covers(y.filter, f))
			found_lower = true;
		}
	    }
	    if (!found_lower)
		result.add(sub);
	}
	return result;
    }

    public Set<Subscriber> matchingSubscribers(Notification e) {
	//
	// computes the set of subscribers that are interested in e.
	// This includes the subscribers of all the subscriptions in
	// the poset that match e
	//
	Set<Subscriber> result = new HashSet<Subscriber>();
	Iterator<Subscription> i = roots.iterator();
	LinkedList<Subscription> to_visit = new LinkedList<Subscription>();
	Set<Subscription> visited = new HashSet<Subscription>();
	Subscription sub;

	while (i.hasNext()) {
	    sub = i.next();
	    if (Covering.apply(sub.filter, e)) {
		to_visit.addLast(sub);
		result.addAll(sub.subscribers);
	    }
	}

	ListIterator<Subscription> li;
	while ((li = to_visit.listIterator()).hasNext()) {
	    sub = li.next();
	    li.remove();
	    i = sub.postset.iterator();
	    while (i.hasNext()) {
		Subscription y = i.next();
		if (visited.add(y) && Covering.apply(y.filter, e)) {
		    to_visit.addLast(y);
		    result.addAll(y.subscribers);
		}
	    }
	}
	return result;
    }

    public Set<Subscription> successors(Filter f, 
					Collection<Subscription> pred) {
	//
	// given a filter f and a set pred, the set of the immediate
	// predecessors of f in the poset, computes the set of
	// immediate successors of f in the poset.
	//
	// Idea: I must walk through the sub-poset of this poset
	// that is covered by the set of predecessors (the whole poset
	// if there are no predecessors), looking for filters f1, f2,
	// ..., fn that are covered by f. I do that by using a queue
	// of filters to_visit.
	// 
	// to_visit is initialized with the element of pred or with
	// the root filters. For every f' in to_visit, if f' is covered
	// by f then I remove f' from to_visit and I add it to the
	// result. If f' is not covered by f, then I add all its
	// successors that I haven't visited to to_visit.
	//
	LinkedList<Subscription> to_visit = new LinkedList<Subscription>();
	Set<Subscription> visited = new HashSet<Subscription>();
	Subscription sub, y;
	Iterator<Subscription> i;
	//
	// initialize to_visit
	//
	if (pred == null || pred.isEmpty()) {
	    to_visit.addAll(roots);
	} else {
	    i = pred.iterator();
	    while (i.hasNext()) {
		to_visit.addLast(i.next());
	    }
	}
	visited.addAll(to_visit);
	Set<Subscription> result = new HashSet<Subscription>();
	ListIterator<Subscription> li;
	while ((li = to_visit.listIterator()).hasNext()) {
	    sub = li.next();
	    li.remove();
	    if (Covering.covers(f, sub.filter)) {
		result.add(sub);
	    } else {
		i = sub.postset.iterator();
		while (i.hasNext()) {
		    y = i.next();
		    if (visited.add(y)) {
			to_visit.addLast(y);
		    }
		}
	    }
	}

	return result;
    }

    public Subscription insert_subscription(Filter f, Subscriber s) {
	//
	// inserts a subscription in the poset
	//
	Set<Subscription> pred = predecessors(f, s);
	Subscription sub;
	if (pred == null)
	    return null;

	if (pred.size() == 1) {
	    sub = pred.iterator().next();
	    if (Covering.covers(f, sub.filter)) {
		//
		// pred contains exactly f, so simply add s to the set
		// of subscribers
		//
		sub.subscribers.add(s);
		clear_subposet(sub, s);
		return sub;
	    }
	}
	sub = new Subscription(f, s);
	insert(sub, pred, successors(f, pred));
	clear_subposet(sub, s);
	return sub;
    }

    public void clear_subposet(Subscription start, Subscriber s) {
	//
	// removes subscriber s from all the subscriptions covered by
	// start, excluding start itself. This also removes
	// subscriptions that remain with no subscribers.
	//
	LinkedList<Subscription> to_visit = new LinkedList<Subscription>();
	Set<Subscription> visited = new HashSet<Subscription>();

	to_visit.addAll(start.postset);

	ListIterator<Subscription> li;
	while ((li = to_visit.listIterator()).hasNext()) {
	    Subscription sub = li.next();
	    li.remove();
	    if (visited.add(sub)) {
		if (sub.subscribers.remove(s)) {
		    if (sub.subscribers.isEmpty())
			remove(sub);
		} else {
		    to_visit.addAll(sub.postset);
		}
	    }
	}
    }

    public Set<Subscription> to_remove(Filter f, Subscriber s) {
	//
	// removes subscriber s from the subscriptions covered by f.
	// If f==null, it removes s from all the subscriptions in the
	// poset. Returns the set of empty subscriptions, i.e., those
	// that remain with no subscribers.
	//
	Set<Subscription> result = new HashSet<Subscription>();
	LinkedList<Subscription> to_visit = new LinkedList<Subscription>();
	Subscription sub;

	if (f == null) {
	    //
	    // f==null ==> universal filter (same thing as BYE)
	    // so my starting point is the set of root subscriptions
	    //
	    to_visit.addAll(roots);
	} else {
	    Set<Subscription> pred = predecessors(f, s);
	    if (pred != null && pred.size() == 1) {
		sub = pred.iterator().next();
		if (Covering.covers(f, sub.filter)) {
		    //
		    // pred contains exactly f, so remove s and see if
		    // f remains with no subscribers
		    //
		    if (sub.subscribers.remove(s)) {
			if (sub.subscribers.isEmpty())
			    result.add(sub);
		    }

		    return result;
		}
	    }
	    to_visit.addAll(successors(f, pred));
	}

	Set<Subscription> visited = new HashSet<Subscription>();
	ListIterator<Subscription> li;

	while ((li = to_visit.listIterator()).hasNext()) {
	    sub = li.next();
	    li.remove();
	    if (visited.add(sub)) {
		if (sub.subscribers.remove(s)) {
		    if (sub.subscribers.isEmpty())
			result.add(sub);
		} else {
		    to_visit.addAll(sub.postset);
		}
	    }
	}
	return result;
    }

    public void writeTo(SENPWriter out) throws IOException {
	//
	// prints the poset
	//
	LinkedList<Subscription> to_visit = new LinkedList<Subscription>();
	Set<Subscription> visited = new HashSet<Subscription>();

	to_visit.addAll(roots);

	ListIterator<Subscription> li;
	while ((li = to_visit.listIterator()).hasNext()) {
	    Subscription sub = li.next();
	    li.remove();
	    if (visited.add(sub)) {
		sub.writeTo(out);
		to_visit.addAll(sub.postset);
	    }
	}
    }
}

class EmptyPatternException extends SienaException {
    static final long serialVersionUID = 1L;
    // ...work in progress...
}

//
// we implement pattern recognition with PatternMatcher. A
// PatternMatcher is a ``parser'' of event sequences. A
// PatternMatcher receives notifications (single events) from Siena
// and holds a list of subscribers. When a PatternMatcher recognizes
// a pattern, it notifies the list of subscribers passing the matching
// sequence of events.
//
class PatternMatcher implements Notifiable {
    public RefSSet subscribers = null;

    public Pattern pattern = null;

    private LinkedList<Notification> notifications = null;

    public PatternMatcher(Pattern p) {
	pattern = new Pattern(p);
	notifications = new LinkedList<Notification>();
	subscribers = new RefSSet();
    }

    public void notify(Notification s[]) {
    }

    public void notify(Notification n) {
	//
	// This method receives the elementary components of a pattern
	// and does the matching.
	//
	// WARNING: this is my own naive matching algorithm. It is
	// certainly not optimized and it might not even be correct!
	// I should look up Knuth-Morris-Pratt matching algorithm,
	// but I have no time now...
	//
	// ...work in progress... bigtime.
	//
	int curr = notifications.size();
	notifications.addLast(new Notification(n));
	//
	// pattern.length must be > 0 (see constructor)
	//
	if (Covering.apply(pattern.filters[curr], n)) {
	    if (++curr == pattern.filters.length) {
		//
		// MATCHED! builds the array of notifications,
		//
		Notification sequence[] = new Notification[curr];
		while (--curr >= 0)
		    sequence[curr] = notifications.removeLast();
		//
		// notify subscribers
		//
		for(Subscriber s : subscribers) {
		    s.notify(sequence);
		}
	    }
	} else {
	    //
	    // no match here, I've got to backtrack... try to cut the
	    // head of the queue of notifications and match a
	    // shorter (most recent) prefix of filters.
	    //
	    for (notifications.removeFirst(); !notifications.isEmpty(); notifications
		     .removeFirst()) {
		ListIterator<Notification> li = notifications.listIterator();
		for (curr = 0; li.hasNext(); ++curr)
		    if (!Covering.apply(pattern.filters[curr], li.next()))
			break;
		if (!li.hasNext()) {
		    //
		    // found a shorter prefix
		    //
		    return;
		}
	    }
	}
    }
}

class BadStorageFormat extends SienaException {
    static final long serialVersionUID = 1L;
    BadStorageFormat(String s) {
	super(s);
    }
}

//
// this class is not intended for general use outside
// HierarchicalDispatcher. In fact, I could have implemented
// everything directly within HierarchicalDispatcher. In particular,
// it is HierarchicalDispatcher that performs the necessary
// concurrency control for ContactsTable.
//
class ContactsTable {
    protected HashMap<String, Subscriber> contacts = new HashMap<String, Subscriber>();

    //
    // Note on Concurrency Control:
    //
    // in general, all these methods should be synchronized, but in
    // this case I prefer to control concurrency directly at the
    // HierarchicalDispatcher level.
    //
    public void clear() {
	contacts.clear();
    }

    public String toString() {
	return contacts.toString();
    }

    public void writeTo(SENPWriter out) throws IOException {
	for (Subscriber s : contacts.values())
	    s.writeContactTo(out);
    }

    public void remove(Subscriber s) {
	if (contacts.remove(s.getKey()) != null)
	    Logging.prlnlog(" removing " + s + " from contacts list",
			    Logging.DEBUG);
    }

    public void put(Subscriber s) {
	contacts.put(s.getKey(), s);
    }

    /*
    public void put(Notifiable n) {
	contacts.put(n.toString(), n);
    }
    */
    public Subscriber get(String id) {
	return contacts.get(id);
    }

    // public Subscriber get(Notifiable n) {
    // return (Subscriber) contacts.get(n);
    // }

    public void cleanup() {
	Map.Entry<String, Subscriber> e;
	for (Iterator<Map.Entry<String, Subscriber>> i = contacts.entrySet().iterator(); i.hasNext();) {
	    e = i.next();
	    Subscriber sub = e.getValue();
	    if (sub.hasNoRefs()) {
		sub.shutdown();
		Logging.prlnlog("contacts cleanup: removing subscriber "
				+ e.getKey(), Logging.DEBUG);
		i.remove();
		// assuming that this actually removes the
		// mapping from the original map---not
		// from the set returned by entrySet()
	    }
	}
    }

    // TODO: fix, deal with thread safety
    public Iterator<String> getKeysIterator() {
	return this.contacts.keySet().iterator();
    }
}

//
// this class is not intended for general use outside
// HierarchicalDispatcher. In fact, I could have implemented
// everything directly within HierarchicalDispatcher. In particular,
// it is HierarchicalDispatcher that performs the necessary
// concurrency control for IFManager.
//
class IFManager {
    static private PacketSenderFactory default_factory = new GenericSenderFactory();

    private PacketSenderFactory sender_factory = default_factory;

    private HashMap<String,Interface> interfaces = new HashMap<String,Interface>();

    void setSenderFactory(PacketSenderFactory f) {
	sender_factory = f;
    }

    static void setDefaultSenderFactory(PacketSenderFactory f) {
	default_factory = f;
    }

    //
    // Note on Concurrency Control:
    //
    // in general, all these methods should be synchronized, but in
    // this case I prefer to control concurrency directly at the
    // HierarchicalDispatcher level.
    //
    public Interface get(String address) throws InvalidSenderException {
	Interface i = interfaces.get(address);
	if (i == null) {
	    PacketSender s = sender_factory.createPacketSender(address);
	    i = new Interface(address.getBytes(), s);
	    interfaces.put(address, i);
	}
	return i;
    }

    public Interface get(byte[] address) throws InvalidSenderException {
	String address_s = new String(address);
	Interface i = interfaces.get(address_s);
	if (i == null) {
	    PacketSender s = sender_factory.createPacketSender(address_s);
	    i = new Interface(address, s);
	    interfaces.put(address_s, i);
	}
	return i;
    }

    public void cleanup() {
	for (Iterator< Map.Entry<String,Interface> > i = interfaces.entrySet().iterator(); i.hasNext();) {
	    Map.Entry<String,Interface> e = i.next();
	    Interface interf = e.getValue();
	    if (interf.has_no_refs()) {
		try {
		    interf.shutdown();
		} catch (PacketSenderException ex) {
		    Logging.prlnerr("error closing packet sender "
				    + interf.toString() + ": " + ex.toString());
		}
		Logging.prlnlog("interfaces cleanup: removing interface " + e);
		i.remove();
		// assuming that this actually removes the
		// mapping from the original map---not
		// from the set returned by entrySet()
	    }
	}
    }
}

/**
 * implementation of a Siena event notification service.
 * 
 * This is the primary implementation of the Siena event notification service. A
 * <code>HierarchicalDispatcher</code> can serve as a Siena event service for
 * local (same Java VM) clients as well as remote clients.
 * <code>HierarchicalDispatcher</code>s can also be combined in a distributed
 * architecture with other dispatchers. Every dispatcher can be connected to a
 * <em>master</em> dispatcher, thereby forming a hierarchical structure. The
 * hierarchy of dispatchers is assembled incrementally as new dispatchers are
 * created and connected to a master that already belongs to the hierarchy.
 * 
 * <p>
 * A <code>HierarchicalDispatcher</code> uses a {@link PacketReceiver} to
 * receive notifications, subscriptions and unsubscriptions from external
 * clients and from its <em>master</em> dispatcher. In order to receive and
 * process external requests, a <code>HierarchicalDispatcher</code> can either
 * use a pool of internal threads, or it can use users' threads. See
 * {@link #DefaultThreadCount}, {@link #setReceiver(PacketReceiver)}, and
 * {@link #setReceiver(PacketReceiver, int)}
 * 
 * @see Siena
 * @see ThinClient
 */
public class HierarchicalDispatcher implements Siena, Runnable {
    protected int cleanup_max_r = 5;

    protected int cleanup_rounds = 0;

    protected long cleanup_time = 0;

    protected long cleanup_max_t = 10000;

    protected String storeFileName = null;

    protected Poset subscriptions = new Poset();

    protected ContactsTable contacts = new ContactsTable();

    protected IFManager ifmanager = new IFManager();

    static final byte[] master_id = { 0x00 };

    private byte[] master_address = null;

    protected Interface master_interface = null;

    // a wrapper to use queues towards the master
    protected NeighborNode master = null;

    protected PacketReceiver listener = null;

    protected byte[] my_identity = null;

    protected List<PatternMatcher> matchers = new LinkedList<PatternMatcher>();

    protected SENPPacket spkt = new SENPPacket();

    protected byte[] sndbuf = new byte[SENP.MaxPacketLen];

    public int chokePeriod = -1;

    protected SFFHeartbeat heartbeat;

    protected SFFTable sffTable = new SFFTable();

    protected Map<Subscriber, SimplePredicate> subscriptionsMap = new HashMap<Subscriber, SimplePredicate>();

    protected boolean sff = false;

    /**
     * configures the subscription storage mechanism.
     * 
     * This server will refresh its database of subscriptions after processing
     * an incoming request, whenever it has processed more than <em>n</em>
     * subscription or unsubscription requests, or <em>t</em> milliseconds have
     * passed since the last refresh. requests that may be processed without
     * refreshing the subscription storage.
     * 
     * <p>
     * The default timeout is 10000 (i.e., 10 seconds).
     */
    synchronized public void setStoreRefreshTimeout(long t) {
	cleanup_max_t = t;
    }

    /**
     * configures the subscription storage mechanism.
     * 
     * Determines the maximum number of subscription or unsubscription requests
     * that may be processed without refreshing the subscription store. See
     * {@link #setStoreRefreshTimeout(long)} for a more formal description of
     * the semantics of this parameter.
     * 
     * <p>
     * The default counter value is 5.
     */
    synchronized public void setStoreRefreshCounter(int c) {
	cleanup_max_r = c;
    }

    /**
     * initializes the subscription storage mechanism.
     * 
     * The server implements a periodic persistent storage mechanism for
     * subscriptions and contact information for remote clients. This method
     * defines the storage file name. If a storage file already exists, the
     * server attempts to load previously saved subscriptions from that file.
     * This mechanism is periodic in the sense that the server flushes its
     * tables to a file at configurable intervals. See
     * {@link #setStoreRefreshTimeout(long)} and
     * {@link #setStoreRefreshCounter(int)} for information on how to configure
     * the refresh rate of the storage mechanism.
     * 
     * @exception IOException
     *                on errors opening the given store file
     * 
     * @exception SienaException
     *                wrong format for the store file
     */
    synchronized public void initStore(String fn) throws IOException,
							 SienaException {
	storeFileName = fn;
	File f = new File(storeFileName);
	if (!f.createNewFile())
	    loadSubscriptions(storeFileName);
    }

    /**
     * saves remote subscriptions and client information to a file.
     * 
     * Saves the all the contact information regarding remote subscribers, and
     * all their subscriptions into the given file. This method is implicitly
     * called at configurable intervals by the storage function of the server.
     * See {@link #initStore(String)} for information on how to initialize the
     * automatic storage mechanism, and {@link #setStoreRefreshTimeout(long)}
     * and {@link #setStoreRefreshCounter(int)} for information on how to
     * configure the refresh rate of the storage mechanism.
     * 
     * @exception IOException
     *                on errors writing into the given store file
     */
    synchronized public void saveSubscriptions(String fname) throws IOException {
	SENPWriter out = new SENPWriter(new FileOutputStream(fname, false));
	contacts.writeTo(out);
	subscriptions.writeTo(out);
	out.close();
    }

    /**
     * loads remote subscriptions and client information from a file.
     * 
     * This method can be called to load subscriptions and contact information
     * from a file. This method is implicitly called by
     * {@link #initStore(String)} if a storage file already exists.
     * 
     * @exception IOException
     *                on errors reading from the given store file
     * 
     * @exception SienaException
     *                wrong format for the store file
     */
    synchronized public void loadSubscriptions(String fname)
	throws IOException, SienaException {
	BufferedReader in = new BufferedReader(new FileReader(fname));
	SENPBuffer sb = new SENPBuffer();
	String inl = in.readLine();
	int lineno = 1;

	while (inl != null) {
	    switch (inl.charAt(0)) {
	    case 'I': {
		String iname = inl.substring(1);
		inl = in.readLine();
		lineno++;
		if (inl == null || inl.charAt(0) != 'H')
		    throw new BadStorageFormat(lineno
					       + ": expecting 'H' line in subscription store");
		String handler = inl.substring(1);
		contacts.put(new RemoteSubscriber(iname.getBytes(), 
						  ifmanager.get(handler)));
		break;
	    }
	    case 'F': {
		sb.init(inl.substring(1).getBytes());
		Filter f;
		try {
		    f = sb.decodeFilter();
		} catch (SENPInvalidFormat ex) {
		    throw new BadStorageFormat(lineno + ": " + ex.getMessage());
		}
		while ((inl = in.readLine()) != null && inl.charAt(0) == 'S') {
		    lineno++;
		    //
		    // sanity check here
		    //
		    Subscriber s = contacts.get(inl.substring(1));
		    if (s == null) {
			throw new BadStorageFormat(lineno
						   + ": unknown subscriber: " + inl.substring(1));
		    } else {
			subscribe(f, s, null);
		    }
		}
		lineno++;

		//
		// here I read either an eof or a non-S line, so I
		// must go back to the main loop
		//
		continue;
	    }
	    default:
		throw new BadStorageFormat(lineno
					   + ": expecting subscription store");
	    }
	    inl = in.readLine();
	    lineno++;
	}
    }

    /**
     * sets the packet-sender factory associated with this
     * HierarchicalDispatcher
     * 
     * @see #setDefaultPacketSenderFactory(PacketSenderFactory)
     */
    synchronized public void setPacketSenderFactory(PacketSenderFactory f) {
	ifmanager.setSenderFactory(f);
    }

    /**
     * default packet-sender factory for HierarchicalDispatcher interfaces
     * 
     * every new HierarchicalDispatcher objects is assigned this factory
     * 
     * @see #setPacketSenderFactory(PacketSenderFactory)
     */
    static public void setDefaultPacketSenderFactory(PacketSenderFactory f) {
	IFManager.setDefaultSenderFactory(f);
    }

    /**
     * default number of threads handling external requests.
     * 
     * Every HierarchicalDispatcher creates a pool of threads to read and
     * process incoming requests. This parameter determines the default number
     * of threads in the pool. The initial default value is 5. See
     * {@link #setReceiver(PacketReceiver, int)} for the semantics of this
     * value. <code>DefaultThreadCount</code> is used to create threads upon
     * call to {@link #setReceiver(PacketReceiver)}.
     * 
     * @see #setReceiver(PacketReceiver)
     * @see #setReceiver(PacketReceiver,int)
     * @see #setMaster(String)
     */
    public int DefaultThreadCount = 5;

    /**
     * number of failed notifications before a subscriber is implicitly
     * disconnected.
     * 
     * The default value of <code>MaxFailedConnectionNumber</code> is 2.
     * 
     * HierachicalDispatcher implements a garbage collection mechanism for
     * unreachable subscribers. This mechanism implicitly unsubscribes a client
     * when the dispatcher fails to connect to the client for a given number of
     * times and after a given number of milliseconds. More formally, the
     * dispatcher considers the sequence of consecutive failed connections not
     * followed by any successful connection. This sequence is charachterized by
     * two parameters: its <em>length</em> and its <em>duration</em>.
     * <p>
     * 
     * <code>MaxFailedConnectionsNumber</code> represents the upper bound to the
     * length of the sequence, while <code>MaxFailedConnectionsDuration</code>
     * represents the upper bound to the duration of the sequence. For both
     * parameters, a negative value means <em>infinity</em>.
     * <code>removeUnreachableSubscriber()</code> removes all the subscriptions
     * of those subscribers that have not been reachable for more than
     * <code>MaxFailedConnectionsNumber</code> times and more than
     * <code>MaxFailedConnectionsDuration</code> milliseconds. Formally, a
     * subscriber that has not been reachable for <em>T</em> milliseconds for
     * <em>N</em> notifications will be removed according to the following
     * conditions:
     * 
     * <pre>
     * <code>
     * if (MaxFailedConnectionsNumber &gt;= 0 
     *      &amp;&amp; MaxFailedConnectionsDuration &gt;= 0) {
     *      if (T &gt; MaxFailedConnectionsDuration 
     *          &amp;&amp; N &gt; MaxFailedConnectionsNumber)
     *          remove it!
     * } else if (MaxFailedConnectionsNumber &gt;= 0) {
     *      if (N &gt; MaxFailedConnectionsNumber)
     *          remove it!
     * } else if (MaxFailedConnectionsDuration &gt;= 0) {
     *     if (T &gt; MaxFailedConnectionsDuration)
     * 	remove it!
     * }
     * </code>
     * </pre>
     * 
     * @see #MaxFailedConnectionsDuration
     */
    public int MaxFailedConnectionsNumber = 2;

    /**
     * milliseconds before automatic unsubscription is activated.
     * 
     * The default value of <code>MaxFailedConnectionsDuration</code> is 5000
     * (i.e., 5 seconds).
     * 
     * @see #MaxFailedConnectionsNumber
     */
    public long MaxFailedConnectionsDuration = 5000;

    /**
     * creates a dispatcher with a specific <em>identity</em>.
     * 
     * Every object involved in Siena communications must have a unique
     * identity. Strictly speaking, with the hierarchical protocol (used by this
     * implementation) the identity of an object must be unique within the group
     * of clients of the same master server. However, because client-master
     * connections can be dynamically reconfigured, it is best to use
     * globally-unique identities.
     * 
     * <p>
     * Also, notice that the hierarchical protocol reserves the identity "\000"
     * for its internal use. Therefore, the string "\000" must never be used as
     * an identity.
     * 
     * @param id
     *            identity given to the dispatcher.
     */
    public HierarchicalDispatcher(String id) {
	my_identity = id.getBytes();
	Monitor.add_node(my_identity, Monitor.SienaNode);
    }

    public void startHeartbeat() {
	if (chokePeriod != -1) {
	    heartbeat = new SFFHeartbeat(this, chokePeriod);
	} else {
	    heartbeat = new SFFHeartbeat(this);
	}
	heartbeat.startHeartbeat();
    }

    /**
     * creates a dispatcher.
     */
    public HierarchicalDispatcher() {
	my_identity = SienaId.getId().getBytes();
	Monitor.add_node(my_identity, Monitor.SienaNode);
    }

    /**
     * process a single request, using the caller's thread.
     * 
     * The default value of <code>MaxFailedConnectionsDuration</code> is 5000
     * (i.e., 5 seconds).
     * 
     * @see #DefaultThreadCount
     * @see #setReceiver(PacketReceiver)
     * @see #setReceiver(PacketReceiver, int)
     */
    public void processOneRequest() throws SienaException {
	SENPPacket req = SENPPacket.allocate();
	try {
	    int res;
	    req.init();
	    res = listener.receive(req.buf);
	    Logging.prlnlog(new String(req.buf, 0, res));
	    req.init(res);
	    req.decode();
	    if (req != null)
		processRequest(req);
	} finally {
	    SENPPacket.recycle(req);
	}
    }

    public void run() {
	SENPPacket req = SENPPacket.allocate();
	int res;
	Logging.prlnlog(new String(my_identity) + " is up and running",
			Logging.DEBUG);
	while (true) {
	    try {
		res = listener.receive(req.buf);
		if (res != 0) {
		    req.init(res);
		    req.decode();
		    processRequest(req);
		}
	    } catch (SENPInvalidFormat ex) {
		Logging.prlnerr(new String(my_identity)
				+ " received an invalid request: " + ex.toString());
		Logging.prlnerr("invalid request: " + req.toString());
	    } catch (PacketReceiverClosed ex) {
		if (ex.getIOException() != null)
		    Logging.prlnerr("error in packet receiver: "
				    + ex.toString());
		SENPPacket.recycle(req);
		return;
	    } catch (PacketReceiverFatalError ex) {
		Logging.prlnerr("fatal error in packet receiver: "
				+ ex.toString());
		SENPPacket.recycle(req);
		return;
	    } catch (PacketReceiverException ex) {
		//
		// non fatal error: just log it and loop
		//
		Logging.prlnlog("non-fatal error in packet receiver: "
				+ ex.toString());
	    }
	}

    }

    synchronized void removeUnreachableSubscriber(Subscriber sub) {
	if (MaxFailedConnectionsNumber < 0 && MaxFailedConnectionsDuration < 0)
	    return;

	boolean to_remove = false;
	if (MaxFailedConnectionsNumber >= 0
	    && MaxFailedConnectionsDuration >= 0) {
	    //
	    // pay attention to both time and count (conjunction)
	    //
	    if (sub.getMillisSinceGood() > MaxFailedConnectionsDuration
		&& sub.getFailedAttempts() > MaxFailedConnectionsNumber)
		to_remove = true;
	} else if (MaxFailedConnectionsNumber >= 0) {
	    if (sub.getFailedAttempts() > MaxFailedConnectionsNumber)
		to_remove = true;
	} else if (MaxFailedConnectionsDuration >= 0) {
	    if (sub.getMillisSinceGood() > MaxFailedConnectionsDuration)
		to_remove = true;
	}
	if (to_remove) {
	    Logging
		.prlnlog("removing unreachable subscriber "
			 + sub.toString());
	    unsubscribe(null, sub, null);
	}
    }

    /**
     * sets the <em>packet receiver</em> for this server.
     * 
     * A <em>packet receiver</em> accepts notifications, subscriptions, and
     * other requests on some communication channel. <code>setReceiver</code>
     * will shut down any previously activated receiver for this dispatcher.
     * This method does not guarantee a transactional switch to a new receiver.
     * This means that some requests might get lost while the server has closed
     * the old port and before it reopens the new port.
     * 
     * <p>
     * This method simply calls {@link #setReceiver(PacketReceiver, int)} using
     * {@link #DefaultThreadCount} as a default value.
     * 
     * @param r
     *            is the receiver
     * 
     * @see #shutdown()
     * @see #setReceiver(PacketReceiver, int)
     */
    public void setReceiver(PacketReceiver r) {
	setReceiver(r, DefaultThreadCount);
    }

    /**
     * sets the <em>packet receiver</em> for this server.
     * 
     * A <em>packet receiver</em> accepts notifications, subscriptions, and
     * other requests on some communication channel. <code>setReceiver</code>
     * will shut down any previously activated receiver for this dispatcher.
     * This method does not guarantee a transactional switch to a new receiver.
     * This means that some requests might get lost while the server has closed
     * the old port and before it reopens the new port.
     * 
     * @param r
     *            the packet receiver
     * @param threads
     *            is the number of threads associated with the receiver, and
     *            therefore to the whole server. A positive value causes this
     *            dispatcher to create threads. A value of 0 causes the
     *            dispatcher not to create any thread, In this case, the
     *            application must explicitly call {@link #processOneRequest()}.
     * 
     * @see #shutdown()
     * @see #setMaster(String)
     */
    synchronized public void setReceiver(PacketReceiver r, int threads) {
	if (listener != null) {
	    try {
		listener.shutdown();
	    } catch (PacketReceiverException ex) {
		Logging.exerr(ex);
		//
		// ...work in progress...
		//
	    }
	    //
	    // this should send a PacketReceiverClosed exception to
	    // every thread that is waiting for packets on the old
	    // listener, which will make them exit normally. However,
	    // because of bugs in the JVM, or because of bad
	    // implementations of packetReceiver, this might not be
	    // true. ...work in progress...
	    //
	}
	listener = r;

	if (master_interface != null) {
	    spkt.init();
	    spkt.method = SENP.MAP;
	    spkt.id = my_identity;
	    spkt.handler = listener.address();
	    try {
		master_interface.send(spkt.buf, spkt.encode());
	    } catch (Exception ex) {
		Logging.prlnerr("error sending MAP packet to master "
				+ master_interface.toString() + ": " + ex.toString());
		Logging.exerr(ex);
		//
		// I should really do something here
		// ...work in progress...
		//
	    }
	}
	//
	// now fires off the reader threads for this dispatcher
	//
	while (threads-- > 0) {
	    Thread t = new Thread(this);
	    //
	    // Perhaps I should set t as a deamon thread...
	    //
	    t.start();
	}
    }

    /**
     * connects this dispatcher to a <em>master</em> dispatcher.
     * 
     * If this dispatcher is already connected to a master dispatcher,
     * <code>setMaster</code> disconnects the old one and connects the new one,
     * thereby unsubscribing all the top-level subscriptions and resubscribing
     * with the new one. This method should be used only when this dispatcher
     * <em>needs</em> to switch to a different master, it is not necessary (it
     * is in fact very inefficient) to set the master before every subscription
     * or notification.
     * 
     * <p>
     * This method does not guarantee a transactional switch. This means that
     * some notifications might be lost when the server has detached from the
     * old master and before it re-subscribes with the new master_interface.
     * 
     * <p>
     * If this dispatcher does not have a <em>packet receiver</em> associated
     * with it, <code>setMaster</code> implicitly sets up one for the
     * dispatcher. The default receiver is a <code>TCPPacketReceiver</code>
     * listening to a randomly allocated port. If you are not happy with the
     * default decision, you should call <code>setReceiver()</code> before you
     * call <code>setMaster</code>.
     * 
     * <p>
     * When <code>address</code> is null, simply disconnect from the current
     * master server.
     * 
     * @param address
     *            is the external identifier of the master dispatcher (e.g.,
     *            <code>"ka:host.domain.edu:8765"</code>), or null to disconnect
     *            from the ccurrent master server.
     * @see #setReceiver(PacketReceiver)
     * @see #shutdown()
     */
    synchronized public void setMaster(String address)
	throws InvalidSenderException, java.io.IOException,
	       PacketSenderException {

	if (address == null) {
	    disconnectMaster();
	    master = null;
	    return;
	}

	byte[] new_address = address.getBytes();
	Interface new_interface = ifmanager.get(new_address);

	disconnectMaster();
	boolean new_listener = false;
	if (listener == null) {
	    setReceiver(new TCPPacketReceiver(0));
	    new_listener = true;
	}
	if (master_interface != null)
	    master_interface.remove_ref();
	master_interface = new_interface;
	master_interface.add_ref();
	master_address = new_address;
	master = new NeighborNode(master_id, master_interface, this, 5000);

	//
	// sends all the top-level subscriptions to the new master
	//
	for (Iterator<Subscription> i = subscriptions.rootsIterator(); i.hasNext();) {
	    Subscription s = i.next();
	    try {
		spkt.init();
		spkt.method = SENP.SUB;
		spkt.ttl = SENP.DefaultTtl;
		spkt.id = my_identity;
		spkt.handler = listener.address();
		spkt.filter = s.filter;
		// master_interface.send(spkt.buf, spkt.encode());
		master.notify(spkt);
	    } catch (Exception ex) {
		Logging.prlnerr("error sending SUB packet to master "
				+ master_interface.toString() + ": " + ex.toString());
		Logging.exerr(ex);
		//
		// of course I should do something here...
		// ...work in progress...
		//
	    }
	}
    }

    /**
     * suspends the connection with the <em>master</em> server of this
     * dispatcher.
     * 
     * This causes the <em>master</em> server to stop sending notification to
     * this dispatcher. The master correctly maintains all the existing
     * subscriptions so that the flow of notification can be later resumed (see
     * {@link #resumeMaster() resumeMaster}. This operation can be used when
     * this dispatcher, that is this virtual machine, is going to be temporarily
     * disconnected from the network or somehow unreachable from its master
     * server.
     * 
     * @see #resumeMaster()
     */
    synchronized public void suspendMaster() {
	try {
	    spkt.init();
	    spkt.method = SENP.SUS;
	    spkt.to = master_address;
	    spkt.id = my_identity;
	    spkt.handler = listener.address();
	    master_interface.send(spkt.buf, spkt.encode());
	} catch (Exception ex) {
	    Logging.prlnerr("error sending SUS packet to master "
			    + master_interface.toString() + ": " + ex.toString());
	    //
	    // of course I should do something here...
	    // ...work in progress...
	    //
	}
    }

    /**
     * resumes the connection with the <em>master</em> server.
     * 
     * This causes the <em>master</em> server to resume sending notification to
     * this dispatcher.
     * 
     * @see #suspendMaster()
     */
    synchronized public void resumeMaster() {
	try {
	    spkt.init();
	    spkt.method = SENP.RES;
	    spkt.to = master_address;
	    spkt.id = my_identity;
	    spkt.handler = listener.address();
	    master_interface.send(spkt.buf, spkt.encode());
	} catch (Exception ex) {
	    Logging.prlnerr("error sending RES packet to master "
			    + master_interface.toString() + ": " + ex.toString());
	    //
	    // of course I should do something here...
	    // ...work in progress...
	    //
	}
    }

    /**
     * returns the identity of this dispatcher.
     * 
     * every object in a Siena network has a unique identifier. This method
     * returns the identifier of this dispatcher.
     * 
     * @see #HierarchicalDispatcher(String)
     */
    synchronized public String getIdentity() {
	return new String(my_identity);
    }

    /**
     * returns the address of the master server associated with this dispatcher.
     * 
     * @return address of the master server or <code>null</code> if the master
     *         server is not set
     * 
     * @see #setMaster(String)
     */
    synchronized public String getMaster() {
	if (master_address == null)
	    return null;
	return new String(master_address);
    }

    /**
     * returns the listener associated with this dispatcher.
     * 
     * @return receiver of this dispatcher (possibly null)
     * 
     * @see #setReceiver(PacketReceiver)
     */
    synchronized public PacketReceiver getReceiver() {
	return listener;
    }

    synchronized private void disconnectMaster() {
	if (master_interface != null) {
	    try {
		spkt.init();
		spkt.method = SENP.BYE;
		spkt.id = my_identity;
		spkt.to = master_address;
		master_interface.send(spkt.buf, spkt.encode());
	    } catch (PacketSenderException ex) {
		Logging.prlnerr("error sending BYE packet to master "
				+ master_interface.toString() + ": " + ex.toString());
		//
		// well, what would you do in this case?
		// ...work in progress...
		//
	    }
	    master_interface = null;
	    master_address = null;
	}
    }

    protected void processRequest(SENPPacket req) {
	if (req == null) {
	    Logging.prlnerr("processRequest: null request");
	    return;
	}

	if (req.ttl <= 0)
	    return;
	req.ttl--;
	try {
	    switch (req.method) {
	    case SENP.PUB:
		publish(req);
		break;
	    case SENP.SUB:
		subscribe(req);
		++cleanup_rounds;
		break;
	    case SENP.BYE:
		req.pattern = null;
		req.filter = null;
		unsubscribe(req);
		++cleanup_rounds;
		break;
	    case SENP.UNS:
		unsubscribe(req);
		++cleanup_rounds;
		break;
	    case SENP.SUS:
		suspend(req);
		++cleanup_rounds;
		break;
	    case SENP.RES:
		resume(req);
		++cleanup_rounds;
		break;
	    case SENP.MAP:
		map(req);
		++cleanup_rounds;
		break;
	    case SENP.CNF:
		configure(req);
		break;
	    case SENP.OFF:
		shutdown();
		//
		// BEGIN_UNOFFICIAL_PATCH
		try {
		    Thread.sleep(500);
		} catch (Exception ex) {
		}
		;
		System.exit(0);
		// END_UNOFFICIAL_PATCH
		//
		break;
	    case SENP.NOP:
		break;
	    default:
		Logging.prlnerr("processRequest: unknown method: " + req);
		//
		// can't handle this request (yet)
		// ...work in progress...
		//
	    }
	    //
	    // here I see if it's time to clean up tables and save
	    // subscriptions
	    //
	    long current_time = System.currentTimeMillis();
	    if (cleanup_rounds >= cleanup_max_r
		|| current_time - cleanup_time > cleanup_max_t) {
		Logging.prlnlog("cleaning up contacts and interfaces tables");
		synchronized (this) {
		    ifmanager.cleanup();
		    contacts.cleanup();
		    if (storeFileName != null)
			saveSubscriptions(storeFileName);
		    cleanup_time = current_time;
		    cleanup_rounds = 0;
		}
	    }
	} catch (Exception ex) {
	    Logging.exerr(ex);
	    //
	    // log something here ...work in progress...
	    //
	}
    }

    synchronized protected void configure(SENPPacket req) {
	if (req.ttl == 0)
	    return;
	if (req.handler == null) {
	    Logging.prlnlog("reconfigure: disconnecting from master");
	    disconnectMaster();
	} else {
	    try {
		String new_master = new String(req.handler);
		Logging.prlnlog("reconfigure: switching to master "
				+ new_master);
		setMaster(new_master);
	    } catch (Exception ex) {
		Logging.prlnerr("configure: failed reconfiguration request: "
				+ ex.toString());
	    }
	}
    }

    protected void map(SENPPacket req) {
	if (req.id == null || req.ttl == 0 || req.handler == null)
	    return;
	synchronized (this) {
	    Subscriber s = contacts.get(new String(req.id));
	    if (s != null)
		try {
		    if (!s.isLocal())
			s.mapHandler(ifmanager.get(req.handler));
		} catch (InvalidSenderException ex) {
		    Logging.prlnerr("error while mapping handler "
				    + new String(req.handler) + ": " + ex.toString());
		    Logging.prlnerr("client not remapped");
		}
	}
    }

    protected void suspend(SENPPacket req) {
	if (req.id == null || req.ttl == 0)
	    return;
	synchronized (this) {
	    //
	    // I'm not 100% sure this synchronization is really necessary.
	    //
	    Subscriber s = contacts.get(new String(req.id));
	    if (s != null)
		s.suspend();
	}
    }

    protected void resume(SENPPacket req) {
	if (req.id == null || req.ttl == 0)
	    return;
	synchronized (this) {
	    //
	    // I'm not 100% sure this synchronization is really necessary.
	    //
	    Subscriber s = contacts.get(new String(req.id));
	    if (s != null)
		s.resume();
	}
    }

    protected void publish(SENPPacket req) {
	if (req.event == null) {
	    Logging.prlnerr("Warning: null event in PUB message: " + req);
	    return;
	}
	byte[] sender = req.id;
	// TOF: dunno what this is for
	// if (sender != null && !SENP.match(sender, master_id)) {
	// Monitor.notify(sender, my_identity);
	// }
	//
	// Because we want to track the hop count in the analysis,
	// here we make the broker put the ttl in the attributes of
	// the message. Later the client can figure out the hop count
	// using ttl
	// req.event.putAttribute("ttl", new AttributeValue(req.ttl));
	//
	// first forward to master
	//
	if (master != null && req.ttl > 0
	    && (sender == null || !SENP.match(sender, master_id))) {
	    req.id = my_identity;
	    req.to = master_id;
	    try {
		// master_interface.send(req.buf, req.encode());
		master.notify(req);
	    } catch (Exception ex) {
		Logging.prlnerr("error sending PUB packet to master "
				+ master_interface.toString() + ": " + ex.toString());
	    }
	}
	//
	// then find all the interested subscribers
	//
	Iterator<Subscriber> i;
	long start = System.nanoTime();
	if (sff) {
	    List<Subscriber> recipients = new ArrayList<Subscriber>();
	    ListMatchMessageHandler matchHandler = new ListMatchMessageHandler( subscriptionsMap);
	    matchHandler.setRecipients(recipients);
	    sffTable.match(req.event, matchHandler);
	    i = recipients.iterator();
	} else {
	    // synchronized (subscriptions) {
	    i = subscriptions.matchingSubscribers(req.event).iterator();
	    // }
	}
	long matchingTime = System.nanoTime();
	// Added to log false positives
	if (req.recipients != null && req.recipients.contains(my_identity)
	    && !i.hasNext()) {
	    Logging.prlnlog(new String(my_identity) + " false positive on msgId=" + req.event.getAttribute("msgId"), Logging.WARN);
	    Logging.prlnlog(new String(my_identity) + " fwdTable on FP: " + sffTable, Logging.INFO);
	}
	req.id = master_id;
	while (i.hasNext()) {
	    Subscriber s = i.next();
	    byte[] s_identity = s.getIdentity();
	    if (Logging.severity >= Logging.INFO) {
		Logging.prlnlog(new String(my_identity) + " ["
				+ Thread.currentThread().getId()
				+ "] publish recipient: " + new String(s_identity)
				// + " sender: " + new String(sender)
				+ " msgId = " + req.event.getAttribute("msgId"),
				Logging.INFO);
	    }
	    if (s.isLocal()) {
		//
		// always notifies local subscribers
		//
		Monitor.notify(my_identity, s_identity);
		if (s_identity != null) {
		    // Logging.prlnlog(new String(my_identity) + " ["
		    // + Thread.currentThread().getId()
		    // + "] notifying local " + new String(s_identity));
		}
		if (!s.notify(req))
		    removeUnreachableSubscriber(s);
	    } else if (req.ttl > 0
		       && (sender == null || !SENP.match(sender, s_identity))) {
		//
		// avoid notifying the sender or anyone else if ttl expired
		//
		req.to = s_identity;
		if (s_identity != null) {
		    Monitor.notify(my_identity, s_identity);
		    Logging.prlnlog(new String(my_identity) + " ["
				    + Thread.currentThread().getId()
				    + "] notifying remote " + new String(s_identity),
				    Logging.INFO);
		}
		if (!s.notify(req))
		    removeUnreachableSubscriber(s);
	    } else {
		// TOF NOTE: somehow breaks log analysis
		// added TOF: log both sender and subscriber ID
		if (Logging.severity >= Logging.INFO) {
		    Logging.prlnlog(new String(my_identity) + " ["
				    + Thread.currentThread().getId()
				    + "] not delivering to " + new String(s_identity)
				    // + " sender: " + new String(sender)
				    + " msgId = " + req.event.getAttribute("msgId"),
				    Logging.INFO);
		}
	    }
	}
    }

    protected void subscribe(SENPPacket req) throws InvalidSenderException,
						    SienaException {
	if (req.filter == null && req.pattern == null) {
	    //
	    // null filters/patterns are not allowed in subscriptions
	    // this is a design choice, we could accept null filters
	    // with the semantics of the universal filter: one that
	    // matches every notification
	    //
	    Logging.prlnerr("subscribe: null filter/pattern in subscription");
	    return;
	}

	// Monitor.subscribe(req.id, my_identity);
	synchronized (this) {
	    Subscriber s = map_subscriber(req);
	    if (s == null) {
		Logging.prlnerr("subscribe: unknown subscriber: "
				+ req.toString());
		return;
	    }
	    if (req.filter != null) {
		subscribe(req.filter, s, req);
	    } else {
		//
		// must be req.pattern != null
		//
		subscribe(req.pattern, s);
	    }
	}
    }

    synchronized protected void unsubscribe(Filter f, Subscriber s,
					    SENPPacket req) {
	if (sff) {
	    synchronized (subscriptionsMap) {
		if (subscriptionsMap.containsKey(s)) {
		    if (f != null) {
			SimplePredicate actorSubscriptions = subscriptionsMap
			    .get(s);
			synchronized (actorSubscriptions) {
			    actorSubscriptions.remove(f);
			}
			// take care of refs
			s.removeRef();
		    } else {
			// take care of refs
			s.refcount = 0;
			subscriptionsMap.remove(s);
		    }
		} else {
		    return;
		}
	    }
	} else {
	    //
	    // for global unsubscriptions (f == null)
	    // removes all the patterns as well
	    // 
	    if (f == null)
		unsubscribe((Pattern) null, s);
	    Set<Subscription> to_remove = subscriptions.to_remove(f, s);
	    if (to_remove.isEmpty())
		return;

	    if (req == null)
		req = spkt;

	    if (master_interface != null && req.ttl > 0) {
		req.id = my_identity;
		req.handler = listener.address();
		req.to = master_id;
	    }

	    Set<Subscription> new_roots = null;
	    for (Subscription sub : to_remove) {
		if (new_roots == null)
		    new_roots = new HashSet<Subscription>();
		new_roots.addAll(subscriptions.remove(sub));
		if (master_interface != null && req.ttl > 0) {
		    try {
			req.method = SENP.UNS;
			req.filter = sub.filter;
			master.notify(req);
		    } catch (Exception ex) {
			Logging.prlnerr("error sending UNS packet to master "
					+ master_interface.toString() + ": "
					+ ex.toString());
		    }
		}
	    }

	    if (new_roots != null && master_interface != null && req.ttl > 0) {
		for (Subscription nr : new_roots) {
		    try {
			req.method = SENP.SUB;
			req.filter = nr.filter;
			master.notify(req);
		    } catch (Exception ex) {
			Logging.prlnerr("error sending SUB packet to master "
					+ master_interface.toString() + ": " 
					+ ex.toString());
		    }
		}
	    }
	    //
	    // here I could avoid removing this contact, since this would
	    // be caught sooner or later by the periodic cleanup
	    //
	    if (s.hasNoRefs())
		contacts.remove(s);
	}
    }

    protected void unsubscribe(SENPPacket req) throws InvalidSenderException,
						      SienaException {
	if (req.id == null)
	    return;
	synchronized (this) {
	    Subscriber s = contacts.get(new String(req.id));
	    if (s == null)
		return;
	    if (req.pattern != null) {
		Monitor.unsubscribe(req.id, my_identity);
		unsubscribe(req.pattern, s);
	    } else {
		Monitor.unsubscribe(req.id, my_identity);
		unsubscribe(req.filter, s, req);
	    }
	}
    }

    protected Subscriber map_subscriber(SENPPacket req)
	throws InvalidSenderException {
	if (req.id == null)
	    return null;
	synchronized (this) {
	    Subscriber s = contacts.get(new String(req.id));
	    if (s == null) {
		if (req.handler != null) {
		    s = new NeighborNode(req.id, ifmanager.get(req.handler),
					 this, 500);
		    contacts.put(s);
		}
	    } else if (req.handler != null) {
		s.mapHandler(ifmanager.get(req.handler));
	    }
	    return s;
	}
    }

    synchronized private Subscriber map_subscriber(Notifiable notifiable) {
	Subscriber s = contacts.get(notifiable.toString());
	if (s == null) {
	    s = new LocalSubscriber(notifiable);
	    contacts.put(s);
	}
	return s;
    }

    /**
     * closes this dispatcher.
     * 
     * If this dispatcher has an active listener then closes the active
     * listener. If this dispatcher has a master server, then cancels every
     * subscription with the master server.
     * <p>
     * 
     * Some implementations of the Java VM do not respond correctly to
     * shutdown(). In particular, Sun's jvm1.3rc1-linux is known not to work
     * correctly. Sun's jvm1.3-solaris, jvm1.3-win32, and jvm1.2.2-linux work
     * correctly.
     * 
     * @see #setReceiver(PacketReceiver)
     * @see #setMaster(String)
     */
    synchronized public void shutdown() {
	disconnectMaster();
	if (listener != null)
	    try {
		listener.shutdown();
	    } catch (PacketReceiverException ex) {
		Logging.prlnerr("error shutting down packet receiver: "
				+ ex.toString());
	    }
	Monitor.remove_node(my_identity);
    }

    /**
     * removes all subscriptions from any notifiable.
     * 
     * clears all the subscriptions from local or remote clients. In case this
     * server has a master server, it instructs the master server to clear all
     * the subscriptions associated with this dispatcher. This method is also
     * useful in case this dispatcher re-uses both a listener and an identity
     * from a previous dispatcher that crashed or did not otherwise shutdown
     * properly.
     * 
     * @see #shutdown()
     */
    synchronized public void clearSubscriptions() throws SienaException {
	subscriptions.clear();
	contacts.clear();
	if (master_interface != null) {
	    SENPPacket spkt = SENPPacket.allocate();
	    spkt.method = SENP.BYE;
	    spkt.id = my_identity;
	    spkt.to = master_address;
	    try {
		master_interface.send(spkt.buf, spkt.encode());
	    } finally {
		SENPPacket.recycle(spkt);
	    }
	}
    }

    public void publish(Notification e) throws SienaException {
	Logging.prlnlog(new String(my_identity) + " publish method invoked ", Logging.DEBUG);
	SENPPacket spkt = SENPPacket.allocate();
	spkt.event = e;
	spkt.method = SENP.PUB;
	spkt.id = null;
	try {
	    publish(spkt);
	} finally {
	    SENPPacket.recycle(spkt);
	}
    }

    public void suspend(Notifiable n) throws SienaException {
	if (n == null)
	    return;
	synchronized (this) {
	    Subscriber s = contacts.get(n.toString());
	    if (s != null)
		s.suspend();
	}
    }

    public void resume(Notifiable n) throws SienaException {
	if (n == null)
	    return;
	synchronized (this) {
	    Subscriber s = contacts.get(n.toString());
	    if (s != null)
		s.resume();
	}
    }

    public void subscribe(Filter f, Notifiable n) throws SienaException {
	if (f == null) {
	    //
	    // null filters are not allowed in subscriptions this is a
	    // design choice, we could accept null filters with the
	    // semantics of the universal filter: one that matches
	    // every notification
	    //
	    Logging.prlnerr("subscribe: null filter in subscription");
	    return;
	}

	synchronized (this) {
	    Subscriber s = map_subscriber(n);
	    if (s == null) {
		Logging.prlnerr("subscribe: unknown local subscriber");
		return;
	    }
	    subscribe(f, s, null);
	}
    }

    synchronized protected void subscribe(Filter f, Subscriber s, SENPPacket req)
	throws SienaException {

	SimplePredicate actorSubscriptions = null;
	synchronized (subscriptionsMap) {
	    actorSubscriptions = subscriptionsMap.get(s);
	    if (actorSubscriptions == null) {
		actorSubscriptions = new SimplePredicate();
		subscriptionsMap.put(s, actorSubscriptions);
	    }

	    synchronized (actorSubscriptions) {
		actorSubscriptions.add(f);
	    }
	}
	// take care of refs or contactTable gets cleared
	s.addRef();
	heartbeat.queueSubscriptionUpdate();

	Subscription sub = subscriptions.insert_subscription(f, s);
	if (sub == null)
	    return;

	if (subscriptions.is_root(sub) && master_interface != null
	    && (req == null || req.ttl > 0)) {
	    try {
		if (req == null) {
		    req = spkt;
		    req.init();
		}
		req.method = SENP.SUB;
		req.id = my_identity;
		req.handler = listener.address();
		req.to = master_id;
		req.filter = f;
		// master_interface.send(req.buf, req.encode());
		master.notify(req);
	    } catch (Exception ex) {
		Logging.prlnerr("error sending SUB packet to master "
				+ master_interface.toString() + ": " + ex.toString());
		//
		// log something here ...work in progress...
		//
	    }
	}
    }

    synchronized public void subscribe(Pattern p, Notifiable n)
	throws SienaException {
	Subscriber s = map_subscriber(n);
	if (s == null)
	    return;
	subscribe(p, s);
    }

    synchronized private void subscribe(Pattern p, Subscriber s)
	throws SienaException {
	//
	// warning: this is a naive implementation.
	// ...work in progress...
	//
	PatternMatcher m;

	ListIterator<PatternMatcher> li = matchers.listIterator();
	while (li.hasNext()) {
	    m = li.next();
	    if (Covering.covers(m.pattern, p)) {
		if (m.subscribers.contains(s))
		    return;
		if (Covering.covers(p, m.pattern)) {
		    m.subscribers.add(s);
		    return;
		}
	    } else if (Covering.covers(p, m.pattern)) {
		if (m.subscribers.remove(s)) {
		    if (m.subscribers.isEmpty()) {
			for (int j = 0; j < m.pattern.filters.length; ++j)
			    unsubscribe(m.pattern.filters[j], m);
			//
			// FIXME: this is a bug! in fact this
			// li.remove() is executed right before a
			// li.hasNext()---the problem is the screwed
			// up semantics of iterators in Java... which
			// may make sense for a generic iterator, but
			// is incredibly stupid for a list iterator:
			// it should be possible to call li.remove()
			// and assume that li shifted to the next
			// element (so that li.hasNext() would work as
			// expected). This demonstrates once again
			// that defining collections and iterators
			// with generic classes is not always a good
			// idea ... well, enough flames for now.
			// Anyway, this needs to be fixed!
			//
			li.remove();
		    }
		}
	    }
	}

	m = new PatternMatcher(p);
	m.subscribers.add(s);
	for (int i = 0; i < p.filters.length; ++i)
	    subscribe(p.filters[i], m);
	matchers.add(m);
    }

    synchronized public void unsubscribe(Filter f, Notifiable n) {
	Subscriber s = contacts.get(n.toString());
	if (s == null)
	    return;
	unsubscribe(f, s, null);
    }

    synchronized public void unsubscribe(Pattern p, Notifiable n) {
	Subscriber s = map_subscriber(n);
	if (s == null)
	    return;
	unsubscribe(p, s);
    }

    synchronized private void unsubscribe(Pattern p, Subscriber s) {
	//
	// warning: this is a naive implementation.
	// ...work in progress...
	//
	PatternMatcher m;

	ListIterator<PatternMatcher> li = matchers.listIterator();
	while (li.hasNext()) {
	    m = li.next();
	    if (p == null || Covering.covers(p, m.pattern)) {
		if (m.subscribers.remove(s)) {
		    if (m.subscribers.isEmpty()) {
			for (int j = 0; j < m.pattern.filters.length; ++j)
			    unsubscribe(m.pattern.filters[j], m);
			//
			// FIXME: this is a bug! see same comment above
			//
			li.remove();
		    }
		}
	    }
	}
	if (s.hasNoRefs())
	    contacts.remove(s);
    }

    public synchronized void updateFWTable() {
	sffTable.clear();
	for (Subscriber s : subscriptionsMap.keySet()) {
	    try {
		sffTable.ifconfig(s, subscriptionsMap.get(s));
	    } catch (BadConstraintException e) {
		e.printStackTrace(Logging.err);
	    }
	}
	sffTable.consolidate();
    }

    /**
     * this method has no effect.
     * 
     * Method not implemented in this Siena server.
     */
    public void advertise(Filter f, String id) throws SienaException {
    };

    /**
     * this method has no effect.
     * 
     * Method not implemented in this Siena server.
     */
    public void unadvertise(Filter f, String id) throws SienaException {
    };

    /**
     * this method has no effect.
     * 
     * Method not implemented in this Siena server.
     */
    public void unadvertise(String id) throws SienaException {
    };

    synchronized public void unsubscribe(Notifiable n) throws SienaException {
	unsubscribe((Filter) null, n);
    }

    public void setChoke(int chokePeriod) {
	this.chokePeriod = chokePeriod;
    }
}

class ListMatchMessageHandler implements MatchMessageHandler {

    List<Subscriber> recipients;

    Map<Subscriber,SimplePredicate> subs;

    public ListMatchMessageHandler(Map<Subscriber,SimplePredicate> subs) {
	super();
	this.subs = subs;
    }

    public void setRecipients(List<Subscriber> recipients) {
	this.recipients = recipients;
    }

    public boolean output(Object id, Message n) {
	if (id != null) {
	    if (id != null) {
		recipients.add((Subscriber) id);
	    } else {
		Logging.prlnerr("ListMatchMessageHandler.output NULL subscriber" + " for " + n);
	    }
	} else {
	    Logging.prlnerr("ListMatchMessageHandler.output invoked on null recipient");
	}
	return false;
    }

}

class SimplePredicate implements siena.fwd.Predicate {
    private LinkedList<siena.fwd.Filter> filters = new LinkedList<siena.fwd.Filter>();

    public Iterator<siena.fwd.Filter> iterator() {
	return filters.iterator();
    }

    public void remove(Filter f) {
	filters.remove(f);

    }

    public void add(siena.fwd.Filter c) {
	filters.add(c);
    }
	
    public String toString(){
	StringBuffer buf = new StringBuffer();
	for (siena.fwd.Filter filter : filters) {
	    buf.append(filter + " ");
	}
	buf.append("\n");
	return(buf.toString());
    }
}
