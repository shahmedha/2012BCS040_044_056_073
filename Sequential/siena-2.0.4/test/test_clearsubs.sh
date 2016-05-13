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
test_description Testing HierarchicalDispatcher.clearSubscriptions...
#
. $srcdir/util.sh
#
cleanup_files='test.out test.expected'
#
sserver 0 ka 3333
#
sleep 3
#
clientid=tcs0000
$JAVA sienatest.TestClearSubscriptions "$siena0" "$clientid" ka:3232:localhost 3 > test.out 2>&1 &
clientpid=$!
cleanup_procs="$cleanup_procs $clientpid"
#
sleep 4
#
echo "executing test..."
#
$JAVA siena.Sender 500 <<EOF 
senp{ to="$siena0" method="PUB"} event{ x="a" y=1 ref=1}
senp{ to="$siena0" method="PUB"} event{ x="a" y=1 ref=2}
senp{ to="$siena0" method="PUB"} event{ x="a" y=1 ref=3}
senp{ to="$siena0" method="PUB"} event{ x="a" y=1 ref=4}
senp{ to="$siena0" method="PUB"} event{ x="a" y=1 ref=5}
EOF
echo "waiting for client to terminate..."
wait $clientpid
cat > test.expected <<EOF
{ ref=1 x="a" y=1}
{ ref=2 x="a" y=1}
{ ref=3 x="a" y=1}
subscriptions cleared
EOF
if diff test.out test.expected ; then
    test_passed
else
    test_failed 'see test.out, test.expected, and test.log for details' 
fi
