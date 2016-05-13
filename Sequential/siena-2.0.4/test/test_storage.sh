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
test_description "Testing subscription storage function..."
#
. $srcdir/util.sh
#
# starting up server...
#
sserver 0 ka 2070 -store server0.store -store-count 1
#
cleanup_files='test.out test.expected test.log'
#
chandler=tcp:7070:localhost
echo "starting client on receiver $chandler..."
$JAVA sienatest.Receiver $chandler:raddr > test.out &
cleanup_files="$cleanup_files raddr"
cleanup_procs="$cleanup_procs $!"
sleep 4
handler=`cat raddr`
#
$JAVA siena.Sender 500 <<EOF
#
# -------------------- TEST BEGINS HERE --------------------
#
senp{ to="$siena0" method="SUB" handler="$handler" id="c/1"}filter{x any}
senp{ to="$siena0" method="SUB" handler="$handler" id="c/1a"}filter{x any}
senp{ to="$siena0" method="SUB" handler="$handler" id="c/1b"}filter{x any}
senp{ to="$siena0" method="SUB" handler="$handler" id="c/2"}filter{y any}
senp{ to="$siena0" method="SUB" handler="$handler" id="c/3"}filter{z any}
#
# @1 {x = 1 y = 1} --> 1, 1a, 1b, 2
#
senp{ to="$siena0" method="PUB"} event{ x=1 y=1 ref=1}
#
# @2 {x = 1 z = 3} --> 1, 1a, 1b, 3
#
senp{ to="$siena0" method="PUB"} event{x = 1 z = 3 ref=2}
#
# I need 3 more requests to trigger the storage functions 
#
senp{ to="$siena0" method="PUB"} event{ref=-1}
senp{ to="$siena0" method="PUB"} event{ref=-2}
senp{ to="$siena0" method="PUB"} event{ref=-3}
senp{ to="$siena0" method="PUB"} event{ref=-4}
EOF
#
# now I'm going to kill the server
#
echo killing server 0 \(pid: $siena0_pid\)
kill $siena0_pid
wait $siena0_pid
echo STORE:
cat server0.store
#
# starting up server again...
#
sleep 2
sserver 0 ka 2070 -store server0.store -store-count 1
echo restarted server 0: "$siena0"
#
sleep 4
#
$JAVA siena.Sender 500 <<EOF
#
# @1 {x = 7} --> 1, 1a, 1b
#
senp{ to="$siena0" method="PUB"} event{x = 7 ref=3}
#
# @2 {z = "Ciao"} --> 3
#
senp{ to="$siena0" method="PUB"} event{z = "Ciao" ref=4}
EOF
#
# AAAAAA
#
cat <<EOF > test.expected
senp{version=1 method="PUB" id="\000" to="c/1"} event{ x=1 y=1 ref=1}
senp{version=1 method="PUB" id="\000" to="c/1a"} event{ x=1 y=1 ref=1}
senp{version=1 method="PUB" id="\000" to="c/1b"} event{ x=1 y=1 ref=1}
senp{version=1 method="PUB" id="\000" to="c/2"} event{ x=1 y=1 ref=1}
#
senp{version=1 method="PUB" id="\000" to="c/1"} event{ x=1 z=3 ref=2}
senp{version=1 method="PUB" id="\000" to="c/1a"} event{ x=1 z=3 ref=2}
senp{version=1 method="PUB" id="\000" to="c/1b"} event{ x=1 z=3 ref=2}
senp{version=1 method="PUB" id="\000" to="c/3"} event{ x=1 z=3 ref=2}
#
senp{version=1 method="PUB" id="\000" to="c/1"} event{ x = 7 ref = 3}
senp{version=1 method="PUB" id="\000" to="c/1a"} event{ x = 7 ref = 3}
senp{version=1 method="PUB" id="\000" to="c/1b"} event{ x = 7 ref = 3}
senp{version=1 method="PUB" id="\000" to="c/3"} event{ z="Ciao" ref = 4}
EOF
sleep 4
kill $cleanup_procs
cleanup_procs=''
if $JAVA siena.Compare test.out test.expected
then
    test_passed
else
    test_failed 'see test.out, test.expected, test.log, and server0.log for detail'
fi
