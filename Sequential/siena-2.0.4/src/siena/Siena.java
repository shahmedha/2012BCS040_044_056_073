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

/** interface of the <em><b>Siena</b></em> event notification service.
 *  
 *  implementations of this interface are access points to the Siena
 *  service.  Applications use them to publish, subscribe,
 *  unsubscribe, etc.  Applications should {@link #shutdown()} a Siena
 *  service access point when it is no longer needed.
 **/
public interface Siena {
    /** publish a notification.
     *
     *  @param n The notification to publish.
     *  @see Notification
     **/
    public void publish(Notification n) throws SienaException;

    /** subscribes for events matching Filter <b>f</b>.
     *
     *  <p>Notice that given the distributed nature of some
     *  implementations of Siena, there exist race conditions that
     *  might affect the semantics of subscriptions.  Specifically, a
     *  subscriber might miss some notifications published before (or
     *  while) the subscription is processed by Siena.
     * 
     *  @param n is the subscriber
     *  @param f is the subscription
     *  @see #unsubscribe 
     **/
    public void subscribe(Filter f, Notifiable n) throws SienaException;

    /** subscribes for sequences of events matching pattern <b>p</b>.
     *
     *  <p>Notice that given the distributed nature of some
     *  implementations of Siena interface, there exist race
     *  conditions that might affect the semantics of subscriptions.
     *  A subscriber might miss some notifications published before or
     *  while the subscription is processed by Siena. 
     *
     *  <p>Also, keep in mind that the current implementation of Siena
     *  does not enforce any temporal order for the delivery of
     *  notifications.  This limitation might affect the recognition
     *  of patterns.  For example, two notifications <em>x</em> and
     *  <em>y</em>, generated at time <em>t<sub>x</sub></em> and
     *  <em>t<sub>y</sub></em> respectively, with
     *  <em>t<sub>x</sub></em> &lt; <em>t<sub>y</sub></em>, in that
     *  order matching a pattern <em>P=(f<sub>x</sub>
     *  f<sub>y</sub>)</em>, might in fact reach the subscriber at
     *  times <em>T<sub>x</sub></em> and <em>T<sub>y</sub></em>, with
     *  <em>T<sub>x</sub></em> &gt; <em>T<sub>y</sub></em>, in which
     *  case pattern <em>P</em> would not be matched.
     *
     *  @param n is the subscriber
     *  @param p is the subscription pattern
     *  @see #unsubscribe 
     **/
    public void subscribe(Pattern p, Notifiable n) throws SienaException;

    /** cancels the subscriptions, posted by <b>n</b>, whose filter
     *  <b>f'</b> is covered by filter <b>f</b>.
     *
     *  <p>Unsubscriptions might incurr in the same kind of race
     *  conditions as subscriptions.  Siena will stop sending
     *  notifications to the subscriber only after it has completed
     *  the processing of the unsubscriptions.  Due to the distributed
     *  nature of some implementations of Siena, this might result in
     *  some additional ``unsolicited'' notifications.
     *
     *  @param n is the subscriber
     *  @see #subscribe 
     **/
    public void unsubscribe(Filter f, Notifiable n) throws SienaException;

    /** cancels the subscriptions, posted by <b>n</b>, whose pattern
     *  <b>p'</b> is covered by pattern <b>p</b>.
     *
     *  <p>Unsubscriptions might incurr in the same kind of race
     *  conditions as subscriptions.  Siena will stop sending
     *  notifications to the subscriber only after it has completed
     *  the processing of the unsubscription.  Due to the distributed
     *  nature of some implementations of Siena, this might result in
     *  some additional ``unsolicited'' notifications.
     *
     *  @param n is the subscriber
     *  @see #subscribe
     **/
    public void unsubscribe(Pattern p, Notifiable n) throws SienaException;

    /** cancels <i>all</i> the subscriptions posted by <b>n</b>.
     *
     *  @param n is the subscriber
     *  @see #subscribe
     **/
    public void unsubscribe(Notifiable n) throws SienaException;

    /** advertises a set of notifications.  
     *
     * Tells Siena that the object identified by <b>id</b> might
     * publish notifications matching the advertisement filter
     * <b>f</b>.
     *
     * @param f advertisement filter.  Notice that this filter is
     *          interpreted differently than a subscription filter.
     *          For more information, consult the <a
     *          href="http://www.inf.usi.ch/carzaniga/siena/index.html#documents">Siena
     *          documentation</a>.
     *
     * @param id identifier of the publisher
     * @see #unadvertise 
     **/
    public void advertise(Filter f, String id) throws SienaException;

    /** cancel previous advertisements.  
     *
     * Cancels those regarding publisher <b>id</b>, whose
     * advertisement filter <b>f'</b> is covered by advertisement
     * filter <b>f</b>.
     *
     * @param f advertisement filter.  Notice that this filter is
     *          interpreted differently than a subscription filter.
     *          For more information, consult the <a
     *          href="http://www.inf.usi.ch/carzaniga/siena/index.html#documents">Siena documentation</a>.
     *
     * @param id identifier of the publisher
     * @see #unadvertise 
     **/
    public void unadvertise(Filter f, String id) throws SienaException;

    /** cancel <em>all</em> previous advertisements for object <b>id</b>.
     *
     * @param id identifier of the publisher
     * @see #unadvertise 
     **/
    public void unadvertise(String id) throws SienaException;


    /** suspends the delivery of notifications to the given subscriber
     *  <code>n</code>.
     *
     *  @param n subscriber to be suspended
     *  @see #resume 
     **/
    public void suspend(Notifiable n) throws SienaException;

    /** resumes the delivery of notifications to the given subscriber
     *  <code>n</code>.
     *
     *  @param n subscriber to be resumed
     *  @see #resume 
     **/
    public void resume(Notifiable n) throws SienaException;

    /** closes this Siena service access point.
     *  
     *  This method releases any system resources associated with the
     *  access point.  In case this access point is connected to other
     *  Siena servers, this method will properly disconnect it.
     **/
    public void shutdown() throws SienaException;
}

