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

import java.util.Map;
import java.util.TreeMap;
import java.util.Iterator;

class Attribute implements siena.fwd.Attribute {
    private Map.Entry<String,AttributeValue> entry;

    public Attribute(Map.Entry<String,AttributeValue> e) {
	entry = e;
    }
    public String getName() {
	return entry.getKey();
    }
    public siena.fwd.Value getValue() {
	return entry.getValue();
    }
}

class AttributeIterator implements Iterator<siena.fwd.Attribute> {
    private Iterator<Map.Entry<String,AttributeValue>> i;

    public AttributeIterator(Iterator<Map.Entry<String,AttributeValue>> x) {
	i = x;
    }
    public boolean hasNext() {
	return i.hasNext();
    }
    public siena.fwd.Attribute next() {
	return new Attribute(i.next());
    }
    public void remove() {
	i.remove();
    }
}

/** an event notification
 *
 *  The primary data entity used within Siena.  A notification is
 *  structured as a set of named and typed attributes.  Attribute
 *  names are strings. <p>
 *
 *  A valid attribute name must begin with a letter
 *  (<code>'a'</code>-<code>'z'</code>,
 *  <code>'A'</code>-<code>'Z'</code>) or an underscore character
 *  (<code>'_'</code>), and may contain only letters, underscores,
 *  digits (<code>'0'</code>-<code>'9'</code>), the dot character
 *  (<code>'.'</code>), the forward slash character
 *  (<code>'/'</code>), and the dollar sign
 *  (<code>'$'</code>). Attribute names must be unique within a
 *  <code>Notification</code>.  <p>
 *
 *  Example:
 *  <p>
 *  <pre><code>
 *      Notification alert = new Notification();
 *      alert.putAttribute("threat", "virus");
 *      alert.putAttribute("name", "melissa");
 *      alert.putAttribute("total_infected", 25);
 *      alert.putAttribute("os/name", "win32");
 *      alert.putAttribute("os/version", "98");
 *  </pre></code>
 *
 *  @see AttributeValue 
 *  @see Filter
 *  @see Siena#publish(Notification) 
 **/
public class Notification  implements java.io.Serializable,
				      siena.fwd.Message {
    static final long serialVersionUID = 1L;
    Map<String,AttributeValue> attributes;

    /** constructs an empty notification.
     **/
    public Notification() { 
	attributes = new TreeMap<String,AttributeValue>();
    }
    
    /** creates a deep copy of a given notification.  
     **/
    public Notification(Notification n) { 
	attributes = new TreeMap<String,AttributeValue>();
	for(Map.Entry<String,AttributeValue> e: n.attributes.entrySet()) {
	    attributes.put(e.getKey(), new AttributeValue(e.getValue()));
	}
    }

    private void writeObject(java.io.ObjectOutputStream out) 
	throws java.io.IOException {
	SENPBuffer b = new SENPBuffer();
	b.encodeSimple(this);
	out.writeInt(b.length());
	out.write(b.buf, 0, b.length());
    }

    private void readObject(java.io.ObjectInputStream in)
	throws java.io.IOException, java.lang.ClassNotFoundException {
	int len = in.readInt();
	SENPBuffer b = new SENPBuffer();
	in.readFully(b.buf, 0, len);
	b.init(len);

	Notification n;
	try {
	    n = b.decodeNotification();
	} catch (SENPInvalidFormat ex) {
	    throw new java.io.InvalidObjectException(ex.toString());
	}
	attributes = new TreeMap<String,AttributeValue>();
	for(Map.Entry<String,AttributeValue> e : n.attributes.entrySet()) {
	    attributes.put(e.getKey(), new AttributeValue(e.getValue()));
	}
    }

    public static Notification parseNotification(String s) 
	throws SENPInvalidFormat {
	return (new SENPBuffer(s.getBytes())).decodeNotification();
    }

    public Iterator<siena.fwd.Attribute> iterator() {
 	return new AttributeIterator(attributes.entrySet().iterator());
    }

    /** set the value of an attribute.  
     *
     *  Add the attribute if that is not present.
     *  @param name attribute name. 
     *  @param value String value.  
     **/
    public void putAttribute(String name, String value) {
	attributes.put(name, new AttributeValue(value));
    }

    /** sets the value of an attribute.  
     *
     *  Add the attribute if that is not present.
     *  @param name attribute name. 
     *  @param value byte array value. 
     **/
    public void putAttribute(String name, byte[] value) {
	attributes.put(name, new AttributeValue(value));
    }

    /** set the value of an attribute.  
     *
     *  Add the attribute if that is not present.
     *  @param name attribute name. 
     *  @param value integer value. 
     **/
    public void putAttribute(String name, long value) {
	attributes.put(name, new AttributeValue(value));
    }

    /** set the value of an attribute.  
     *
     *  Add the attribute if that is not present.
     *  @param name attribute name. 
     *  @param value double value. 
     **/
    public void putAttribute(String name, double value) {
	attributes.put(name, new AttributeValue(value));
    }

    /** set the value of an attribute.  
     *
     *  Add the attribute if that is not present.
     *  @param name attribute name.
     *  @param value boolean value. 
     **/
    public void putAttribute(String name, boolean value) {
	attributes.put(name, new AttributeValue(value));
    }

    /** set the value of an attribute.  
     *
     *  Add the attribute if that is not present.
     *  @param name attribute name.
     *  @param value value. 
     **/
    public void putAttribute(String name, AttributeValue value) {
	attributes.put(name, value);
    }

    /** returns the value of an attribute or <code>null</code> if
     *  that attribute does not exist in this notification.
     *  
     *  @param name attribute name.  
     **/
    public AttributeValue getAttribute(String name) {
	return attributes.get(name);
    }

    /** remove an attribute from this notification.
     *  
     *  @return the removed attribute value, or <code>null</code> if
     *  the given attribute does not exist in this notification.
     *
     *  @param name attribute name.  
     **/
    public AttributeValue removeAttribute(String name) {
	return attributes.remove(name);
    }

    /** returns the number of attributes in this notification.
     **/
    public int size() {
	return attributes.size();
    }

    /** removes every attribute from this notification.
     **/
    public void clear() {
	attributes.clear();
    }

    /** returns an iterator for the set of attribute names of this 
     *	notification.
     **/
    public Iterator<String> attributeNamesIterator() {
	return attributes.keySet().iterator();
    }

    public String toString() {
	SENPBuffer b = new SENPBuffer();
	b.encodeSimple(this);
	return new String(b.buf, 0, b.length());
    }
}
