#!/bin/sh
#
#  This file is part of Siena, a wide-area event notification system.
#  See http://www.inf.usi.ch/carzaniga/siena/
#
#  Author: Antonio Carzaniga (firstname.lastname@usi.ch)
#  See the file AUTHORS for full details. 
#
#  Copyright (C) 1998-2002 University of Colorado
#
#  Siena is free software: you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation, either version 3 of the License, or
#  (at your option) any later version.
#  
#  Siena is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#  
#  You should have received a copy of the GNU General Public License
#  along with Siena.  If not, see <http://www.gnu.org/licenses/>.
#
. ./config.sh
#
test_description Testing remote NotificationSequencer...
#
cleanup_files='test.out test.expected'
. $srcdir/util.sh
#
sserver 0 ka 2222
sleep 3
#
# SequencerSubscriber <server> [count] [timeout] [receiver-spec] [threads]
#
$JAVA sienatest.SequencerSubscriber $siena0 300 50000 ka:7070:localhost 6 \
    > test.out &
clientpid=$!
sleep 3
#
# -------------------- TEST BEGINS HERE --------------------
#
# no shuffling: ordered sequence; 1 thread
#
# MultiSender <server> [count] [threads] [odds] [skew]
#
$JAVA sienatest.MultiSender $siena0 300 5 3 20
#
if wait $clientpid; then
    test_passed
else
    test_failed 'see test.out and test.expected for details'
fi
