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

import java.util.Random;

/** adds sequence tags to notifications so that they can be properly
 *  reordered by receivers.
 *
 *  An <em>output sequencer</em> can be used to publish notifications
 *  in a logical sequence.  Notifications passed through an output
 *  sequencer can then be delivered in the correct order of
 *  publication to subscribers by using an <em>input sequencers</em>
 *  on the subscribers' side.  See {@link NotificationInputSequencer}
 *  for more documentation on input/output sequencers.
 *
 *  An output sequencer can be used as a wrapper to a Siena interface,
 *  or as an independent notification processor.  In the first case, a
 *  publisher would publish notifications directly through the output
 *  sequencer, while in the second case, a publisher would use the
 *  sequencer to tag notifications to be later published.
 *
 *  <p>Example:
 *  <code><pre>
 *      Siena siena;
 *      Notification n;
 *      // ...
 *      // siena = new ...
 *      // n = new Notification();
 *      // n.putAttribute("foo", "bar") ...
 *      // n = ...
 *      // ...
 *      NotificationOutputSequencer sequencer(siena);
 *      sequencer.publish(n);
 *      // ...
 *  </pre></code>
 *
 *  @see NotificationInputSequencer 
 *  @see Notifiable
 **/
public class NotificationOutputSequencer {
    private long latest_sent;
    private Siena siena;
    private String id;

    public NotificationOutputSequencer(Siena s) {
	latest_sent = -1;
	siena = s;
	id = SienaId.getId();
    }

    public NotificationOutputSequencer() {
	latest_sent = -1;
	siena = null;
	id = SienaId.getId();
    }

    public void publish(Notification n) throws SienaException {
	siena.publish(tagNotification(n));
    }

    synchronized public Notification tagNotification(Notification n) {
	n.putAttribute(NotificationSequencer.SEQ_ID, id);
	n.putAttribute(NotificationSequencer.SEQ_NUM, ++latest_sent);
	return n;
    }
}
