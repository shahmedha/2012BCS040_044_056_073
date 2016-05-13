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

/** interface implemented by event consumers.  
 *
 *    Every object that wants to receive event notifications from
 *    Siena must implement this interface.  Siena calls
 *    <code>notify(Notification)</code> on a subscriber to notify a
 *    single event to it.  Siena calls <code>notify(Notification
 *    [])</code> to notify a sequence of events.<p>
 *
 *    Example:
 *    <pre><code>
 *    class SimpleSubscriber implements Notifiable {
 *
 *        public void notify(Notification e) {
 *            System.out.println("I got this notification: " + e.toString());
 *        }
 *
 *        public void notify(Notification s[]) {
 *            // I never subscribe for patterns anyway. 
 *        }
 *    } 
 *    </pre></code>
 *
 *   @see Notification
 *   @see Siena 
 **/
public interface Notifiable {

    /** sends a <code>Notification</code> to this <code>Notifable</code>
     *
     *  Since version 1.0.1 of the Siena API it is safe to modify the
     *  Notification object received through this method.  Note that:
     *  <ol>
     * 
     *  <li><em>any</em> previous version of the Siena API assumes that
     *      clients <em>do not modify</em> these notifications;
     *
     *  <li>the current solution incurrs in an unnecessary cost by
     *      having to duplicate every notification.  Therefore, it
     *      is a <em>temporary solution</em>.  The plan is to
     *      implement <em>immutable</em> notifications and to pass
     *      those to subscribers.
     *
     *  </ol>
     *  necessary duplication of notifications can be expensive,
     *  especially if the same notification must be copied to numerous
     *  subscribers.
     *
     *  @param n notification passed to the notifiable
     *  @see Siena#subscribe(Filter,Notifiable) 
     **/
    public void notify(Notification n) throws SienaException;

    /** sends a sequence of <code>Notification</code>s to this 
     *  <code>Notifable</code>
     *
     *  Since version 1.0.1 of the Siena API it is safe to modify the
     *  Notification objects received through this method.  Please
     *  read the notes in the above documentation of {@link
     *  #notify(Notification)}, which apply to this method as well.
     *
     *  @param s sequence of notifications passed to the notifiable
     *  @see Siena#subscribe(Pattern,Notifiable) 
     **/
    public void notify(Notification s[]) throws SienaException;
}
