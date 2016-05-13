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
test_description "Testing multiple distributed subscriptions and unsubscriptions..."
#
. $srcdir/util.sh
#
# starting up servers...
#
sserver 0 ka 2270
sserver 1 ka 2271 0
sserver 2 ka 2272 0
sserver 3 ka 2273 1
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
echo "executing test..." 
#
$JAVA siena.Sender 500 <<EOF
#
# -------------------- TEST BEGINS HERE --------------------
#
senp{ to="$siena3" method="SUB" handler="$handler" id="c/1"}filter{x any}
senp{ to="$siena0" method="SUB" handler="$handler" id="c/1a"}filter{x any}
senp{ to="$siena0" method="SUB" handler="$handler" id="c/1b"}filter{x any}
senp{ to="$siena0" method="SUB" handler="$handler" id="c/2"}filter{y any}
senp{ to="$siena1" method="SUB" handler="$handler" id="c/3"}filter{z any}
#
# @1 {x = 1 y = 1} --> 1, 1a, 1b, 2
#
senp{ to="$siena1" method="PUB"} event{ x=1 y=1 ref=1}
#
# @2 {x = 1 z = 3} --> 1, 1a, 1b, 3
#
senp{ to="$siena2" method="PUB"} event{x = 1 z = 3 ref=2}
#
senp{ to="$siena0" method="SUB" handler="$handler" id="c/4"}filter{x > 0 y > 0}
senp{ to="$siena0" method="SUB" handler="$handler" id="c/5"}filter{y > 0 z > 0}
senp{ to="$siena2" method="SUB" handler="$handler" id="c/6"}filter{x > 0 z > 0}
#
# @1 {x = 1 y = 2} --> 1, 1a, 1b, 2, 4
#
senp{ to="$siena1" method="PUB"} event{x = 1 y = 2 ref=3}
#
# @2 {x = 1 y = 3} --> 1, 1a, 1b, 2, 4
#
senp{ to="$siena2" method="PUB"} event{x = 1 y = 3 ref=4}
#
# @0 {z = 3} --> 3
#
senp{ to="$siena0" method="PUB"} event{z = 3 T=null ref=5}
#
senp{ to="$siena1" method="SUB" handler="$handler" id="c/7"}filter{x > 0 y > 0 z > 0}
#
# @2 {x = 1 y = 2 z = 3} --> 1, 1a, 1b, 2, 3, 4, 5, 6, 7
#
senp{ to="$siena2" method="PUB"} event{x = 1 y = 2 z = 3 ref=6}
#
senp{ to="$siena0" method="UNS" handler="$handler" id="c/5"}filter{z > 0}
#
# @1 {y = 2 z = 3} --> 2, 3
#
senp{ to="$siena1" method="PUB"} event{x = null y = 2 z = 3 ref=7}
#
senp{ to="$siena0" method="SUB" handler="$handler" id="c/8"}filter{y>0 z>0}
#
# @0 {y = 2 z = 3} --> 2, 3, 8
#
senp{ to="$siena0" method="PUB"} event{y = 2 z = 3 ref=8}
#
senp{ to="$siena0" method="SUB" handler="$handler" id="c/9"}filter{y>0 x>0}
#
# @2 {y = 2 z = 3} --> 1, 1a, 1b, 2, 4, 9
#
senp{ to="$siena2" method="PUB"} event{x = 2 y = 3 ref=9}
#
senp{ to="$siena0" method="UNS" handler="$handler" id="c/4"}filter{y any x any}
#
# @1 {x = 2 y = 3} --> 1, 2, 9
#
senp{ to="$siena1" method="PUB"} event{x = 2 y = 3 ref=10}
#
# @0 10 <-- {x>0 y>0 z>0 a="A"}
#
senp{ to="$siena0" method="SUB" handler="$handler" id="c/10"}filter{x>0 y>0 z>0 a="A"}
#
# @0 11 <-- {x>0 y>0 z>0 a="B"}
#
senp{ to="$siena0" method="SUB" handler="$handler" id="c/11"}filter{x>0 y>0 z>0 a="B"}
#
# @0 {x=2 y=3 z=4 a="A"} --> 1, 1a, 1b, 2, 3, 6, 7, 8, 9, 10
#
senp{ to="$siena0" method="PUB"} event{x=2 y=3 z=4 a="A" ref=11}
#
# @2 {x=2 y=3 z=4 a="B"} --> 1, 2, 3, 6, 7, 8, 9, 11
#
senp{ to="$siena2" method="PUB"} event{x=2 y=3 z=4 a="B" ref=12}
#
# @1 7 -X- {x>0 y>0 z>0} (in fact exactly x>0 y>0 z>0)
#
senp{ to="$siena1" method="UNS" handler="$handler" id="c/7"}filter{x>0 y>0 z>0}
#
# @2 {x=2 y=3 z=4 a="A"} --> 1, 2, 3, 6, 8, 9, 10
#
senp{ to="$siena2" method="PUB"} event{x=2 y=3 z=4 a="A" ref=13}
#
# @1 {x=2 y=3 z=4} --> 1, 2, 3, 6, 8, 9, 10
#
senp{ to="$siena1" method="PUB"} event{x=2 y=3 z=4}
#
# @0 12 <-- {a any}
#
senp{ to="$siena0" method="SUB" handler="$handler" id="c/12"}filter{a any}
#
# @0 {x=2 a="A"} --> 1, 12
#
senp{ to="$siena0" method="PUB"} event{x=2 a="A"}
#
# @0 {x=2 y=5 a="A"} --> 1, 2, 9, 12
#
senp{ to="$siena0" method="PUB"} event{x=2 y=5 a="A"}
#
# @0 1 -X- {x any} (in fact exactly x any)
#
senp{ to="$siena3" method="UNS" handler="$handler" id="c/1"}filter{x any}
#
# @1 {x=2 z=7 a="X"} --> 3, 6, 12
#
senp{ to="$siena1" method="PUB"} event{x=2 z=7 a="X"}
#
# @0 13 <-- {z any}
#
senp{ to="$siena0" method="SUB" handler="$handler" id="c/13"}filter{z any}
#
# @2 {x=2 z=7 a="X"} --> 1a, 1b, 3, 6, 12, 13
#
senp{ to="$siena2" method="PUB"} event{x=2 z=7 a="X"}
#
# @1 {x=2 z=7 a="X"} --> 3, 6, 12, 13
#
senp{ to="$siena1" method="PUB"} event{x=2 z=7 a="X"}
#
# @0 14 <-- {z any}
#
senp{ to="$siena0" method="SUB" handler="$handler" id="c/14"}filter{z any}
#
# @1 {z=7} --> 3, 13, 14
#
senp{ to="$siena1" method="PUB"} event{ z=7 }
#
# @0 {z=7} --> 3, 13, 14
#
senp{ to="$siena0" method="PUB"} event{ z=7 }
#
# @1 3 -X- {z any}
#
senp{ to="$siena1" method="UNS" handler="$handler" id="c/3"}filter{z any}
#
# @1 {z=7} --> 13, 14
#
senp{ to="$siena1" method="PUB"} event{ z=7 }
#
#
# @2 {z=7} --> 13, 14
#
senp{ to="$siena2" method="PUB"} event{ z=7 }
#
# @0 13 -X- {z any}
#
senp{ to="$siena0" method="UNS" handler="$handler" id="c/13"}filter{z any}
#
# @2 {z=7} --> 14
#
senp{ to="$siena2" method="PUB"} event{ z=7 }
#
# @0 14 -X- {z any}
#
senp{ to="$siena0" method="UNS" handler="$handler" id="c/14"}filter{z any}
#
# @2 {z=8} --> 
#
senp{ to="$siena2" method="PUB"} event{ z=8 }
#
# @1 {x=2 z=7 a="Y"} --> 6, 12
#
senp{ to="$siena1" method="PUB"} event{x=2 z=7 a="Y"}
#
# @2 6 -X-
#
senp{ to="$siena2" method="BYE" handler="$handler" id="c/6" }
#
# @1 {x=2 z=7 a="Y"} --> 12
#
senp{ to="$siena1" method="PUB"} event{x=2 z=7 a="Y"}
#
# @1 15 <-- { x>0 y>0 }
#
senp{ to="$siena1" method="SUB" handler="$handler" id="c/15" }filter{ x>0 y>0 }
#
# @2 {x=2 y=17} --> 15, 2, 9 
#
senp{ to="$siena2" method="PUB"} event{x=2 y=17}
#
# @0 2 -X-
#
senp{ to="$siena0" method="BYE" handler="$handler" id="c/2" }
#
# @0 9 -X-
#
senp{ to="$siena0" method="BYE" handler="$handler" id="c/9" }
#
# @2 {x=2 y=18} --> 15 
#
senp{ to="$siena2" method="PUB"} event{x=2 y=18}
#
# @1 {x=2 y=18 z=1 a="A"} --> 15, 10, 8, 12
#
senp{ to="$siena2" method="PUB"} event{x=2 y=18 z=1 a="A"}
#
# @0 10 -X- {x > 0} (actually {x>0 y>0 z>0 a="A"})
#
senp{to="$siena0" method="UNS" handler="$handler" id="c/10"}filter{x>0}
#
# @0 12 -X-
#
senp{to="$siena0" method="BYE" handler="$handler" id="c/12"}
#
# @2 {x=2 y=20 z=1 a="A"} --> 15, 8
#
senp{ to="$siena2" method="PUB"} event{x=2 y=20 z=1 a="A"}
#
# @0 8 -X-
#
senp{to="$siena0" method="BYE" handler="$handler" id="c/8"}
#
# @0 11 -X-
#
senp{to="$siena0" method="BYE" handler="$handler" id="c/11"}
#
# @2 {x=2 y=20 z=1 a="A"} --> 15
#
senp{ to="$siena2" method="PUB"} event{x=2 y=20 z=1 a="A"}
#
# @1 {x=2 y=21 z=1 a="A"} --> 15
#
senp{ to="$siena1" method="PUB"} event{x=2 y=21 z=1 a="A"}
#
# @2 15 -X-
#
senp{to="$siena1" method="BYE" handler="$handler" id="c/15"}
#
# @2 {x=2 y=20 z=1 a="A"} --> 1a 1b
#
senp{ to="$siena2" method="PUB"} event{x=20 y=20 z=1 a="A"}
#
#
# @0 1a -X- {x any}
#
senp{to="$siena0" method="UNS" handler="$handler" id="c/1a"}filter{x any}
#
# @2 {x=20 y=20 z=20} --> 1b
#
senp{ to="$siena2" method="PUB"} event{x=20 y=20 z=20}
#
#
# @0 1b -X- {x any}
#
senp{to="$siena0" method="UNS" handler="$handler" id="c/1b"}filter{x any}
#
# @2 {x=20 y=20 z=20} -->
#
senp{ to="$siena2" method="PUB"} event{x=20 y=20 z=20}
EOF
#
#
# AAAAAA
#
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
senp{version=1 method="PUB" id="\000" to="c/1"} event{ x=1 y=2 ref=3}
senp{version=1 method="PUB" id="\000" to="c/1a"} event{ x=1 y=2 ref=3}
senp{version=1 method="PUB" id="\000" to="c/1b"} event{ x=1 y=2 ref=3}
senp{version=1 method="PUB" id="\000" to="c/2"} event{ x=1 y=2 ref=3}
senp{version=1 method="PUB" id="\000" to="c/4"} event{ x=1 y=2 ref=3}
senp{version=1 method="PUB" id="\000" to="c/1"} event{ x=1 y=3 ref=4}
senp{version=1 method="PUB" id="\000" to="c/1a"} event{ x=1 y=3 ref=4}
senp{version=1 method="PUB" id="\000" to="c/1b"} event{ x=1 y=3 ref=4}
senp{version=1 method="PUB" id="\000" to="c/2"} event{ x=1 y=3 ref=4}
senp{version=1 method="PUB" id="\000" to="c/4"} event{ x=1 y=3 ref=4}
senp{version=1 method="PUB" id="\000" to="c/3"} event{ z=3 T=null ref=5}
#
senp{version=1 method="PUB" id="\000" to="c/1"} event{ x=1 y=2 z=3 ref=6}
senp{version=1 method="PUB" id="\000" to="c/1a"} event{ x=1 y=2 z=3 ref=6}
senp{version=1 method="PUB" id="\000" to="c/1b"} event{ x=1 y=2 z=3 ref=6}
senp{version=1 method="PUB" id="\000" to="c/2"} event{ x=1 y=2 z=3 ref=6}
senp{version=1 method="PUB" id="\000" to="c/3"} event{ x=1 y=2 z=3 ref=6}
senp{version=1 method="PUB" id="\000" to="c/4"} event{ x=1 y=2 z=3 ref=6}
senp{version=1 method="PUB" id="\000" to="c/5"} event{ x=1 y=2 z=3 ref=6}
senp{version=1 method="PUB" id="\000" to="c/6"} event{ x=1 y=2 z=3 ref=6}
senp{version=1 method="PUB" id="\000" to="c/7"} event{ x=1 y=2 z=3 ref=6}
#
senp{version=1 method="PUB" id="\000" to="c/2"} event{ x=null y=2 z=3 ref=7}
senp{version=1 method="PUB" id="\000" to="c/3"} event{ x=null y=2 z=3 ref=7}
senp{version=1 method="PUB" id="\000" to="c/1a"} event{ x=null y=2 z=3 ref=7}
senp{version=1 method="PUB" id="\000" to="c/1b"} event{ x=null y=2 z=3 ref=7}
senp{version=1 method="PUB" id="\000" to="c/1"} event{ x=null y=2 z=3 ref=7}
#
senp{version=1 method="PUB" id="\000" to="c/2"} event{ y=2 z=3 ref=8}
senp{version=1 method="PUB" id="\000" to="c/3"} event{ y=2 z=3 ref=8}
senp{version=1 method="PUB" id="\000" to="c/8"} event{ y=2 z=3 ref=8}
#
senp{version=1 method="PUB" id="\000" to="c/1"} event{ x=2 y=3 ref=9}
senp{version=1 method="PUB" id="\000" to="c/1a"} event{ x=2 y=3 ref=9}
senp{version=1 method="PUB" id="\000" to="c/1b"} event{ x=2 y=3 ref=9}
senp{version=1 method="PUB" id="\000" to="c/2"} event{ x=2 y=3 ref=9}
senp{version=1 method="PUB" id="\000" to="c/4"} event{ x=2 y=3 ref=9}
senp{version=1 method="PUB" id="\000" to="c/9"} event{ x=2 y=3 ref=9}
#
senp{version=1 method="PUB" id="\000" to="c/1"} event{ x=2 y=3 ref=10}
senp{version=1 method="PUB" id="\000" to="c/1a"} event{ x=2 y=3 ref=10}
senp{version=1 method="PUB" id="\000" to="c/1b"} event{ x=2 y=3 ref=10}
senp{version=1 method="PUB" id="\000" to="c/2"} event{ x=2 y=3 ref=10}
senp{version=1 method="PUB" id="\000" to="c/9"} event{ x=2 y=3 ref=10}
#
senp{version=1 method="PUB" id="\000" to="c/1"} event{ a="A" x=2 y=3 z=4 ref=11}
senp{version=1 method="PUB" id="\000" to="c/1a"} event{ a="A" x=2 y=3 z=4 ref=11}
senp{version=1 method="PUB" id="\000" to="c/1b"} event{ a="A" x=2 y=3 z=4 ref=11}
senp{version=1 method="PUB" id="\000" to="c/10"} event{ a="A" x=2 y=3 z=4 ref=11}
senp{version=1 method="PUB" id="\000" to="c/2"} event{ a="A" x=2 y=3 z=4 ref=11}
senp{version=1 method="PUB" id="\000" to="c/3"} event{ a="A" x=2 y=3 z=4 ref=11}
senp{version=1 method="PUB" id="\000" to="c/6"} event{ a="A" x=2 y=3 z=4 ref=11}
senp{version=1 method="PUB" id="\000" to="c/7"} event{ a="A" x=2 y=3 z=4 ref=11}
senp{version=1 method="PUB" id="\000" to="c/8"} event{ a="A" x=2 y=3 z=4 ref=11}
senp{version=1 method="PUB" id="\000" to="c/9"} event{ a="A" x=2 y=3 z=4 ref=11}
senp{version=1 method="PUB" id="\000" to="c/1"} event{ a="B" x=2 y=3 z=4 ref=12}
senp{version=1 method="PUB" id="\000" to="c/1a"} event{ a="B" x=2 y=3 z=4 ref=12}
senp{version=1 method="PUB" id="\000" to="c/1b"} event{ a="B" x=2 y=3 z=4 ref=12}
senp{version=1 method="PUB" id="\000" to="c/11"} event{ a="B" x=2 y=3 z=4 ref=12}
senp{version=1 method="PUB" id="\000" to="c/2"} event{ a="B" x=2 y=3 z=4 ref=12}
senp{version=1 method="PUB" id="\000" to="c/3"} event{ a="B" x=2 y=3 z=4 ref=12}
senp{version=1 method="PUB" id="\000" to="c/6"} event{ a="B" x=2 y=3 z=4 ref=12}
senp{version=1 method="PUB" id="\000" to="c/7"} event{ a="B" x=2 y=3 z=4 ref=12}
senp{version=1 method="PUB" id="\000" to="c/8"} event{ a="B" x=2 y=3 z=4 ref=12}
senp{version=1 method="PUB" id="\000" to="c/9"} event{ a="B" x=2 y=3 z=4 ref=12}
senp{version=1 method="PUB" id="\000" to="c/1"} event{ a="A" x=2 y=3 z=4 ref=13}
senp{version=1 method="PUB" id="\000" to="c/1a"} event{ a="A" x=2 y=3 z=4 ref=13}
senp{version=1 method="PUB" id="\000" to="c/1b"} event{ a="A" x=2 y=3 z=4 ref=13}
senp{version=1 method="PUB" id="\000" to="c/10"} event{ a="A" x=2 y=3 z=4 ref=13}
senp{version=1 method="PUB" id="\000" to="c/2"} event{ a="A" x=2 y=3 z=4 ref=13}
senp{version=1 method="PUB" id="\000" to="c/3"} event{ a="A" x=2 y=3 z=4 ref=13}
senp{version=1 method="PUB" id="\000" to="c/6"} event{ a="A" x=2 y=3 z=4 ref=13}
senp{version=1 method="PUB" id="\000" to="c/8"} event{ a="A" x=2 y=3 z=4 ref=13}
senp{version=1 method="PUB" id="\000" to="c/9"} event{ a="A" x=2 y=3 z=4 ref=13}
senp{version=1 method="PUB" id="\000" to="c/1"} event{ x=2 y=3 z=4}
senp{version=1 method="PUB" id="\000" to="c/1a"} event{ x=2 y=3 z=4}
senp{version=1 method="PUB" id="\000" to="c/1b"} event{ x=2 y=3 z=4}
senp{version=1 method="PUB" id="\000" to="c/2"} event{ x=2 y=3 z=4}
senp{version=1 method="PUB" id="\000" to="c/3"} event{ x=2 y=3 z=4}
senp{version=1 method="PUB" id="\000" to="c/6"} event{ x=2 y=3 z=4}
senp{version=1 method="PUB" id="\000" to="c/8"} event{ x=2 y=3 z=4}
senp{version=1 method="PUB" id="\000" to="c/9"} event{ x=2 y=3 z=4}
senp{version=1 method="PUB" id="\000" to="c/1"} event{ a="A" x=2}
senp{version=1 method="PUB" id="\000" to="c/1a"} event{ a="A" x=2}
senp{version=1 method="PUB" id="\000" to="c/1b"} event{ a="A" x=2}
senp{version=1 method="PUB" id="\000" to="c/12"} event{ a="A" x=2}
senp{version=1 method="PUB" id="\000" to="c/1"} event{ a="A" x=2 y=5}
senp{version=1 method="PUB" id="\000" to="c/1a"} event{ a="A" x=2 y=5}
senp{version=1 method="PUB" id="\000" to="c/1b"} event{ a="A" x=2 y=5}
senp{version=1 method="PUB" id="\000" to="c/12"} event{ a="A" x=2 y=5}
senp{version=1 method="PUB" id="\000" to="c/2"} event{ a="A" x=2 y=5}
senp{version=1 method="PUB" id="\000" to="c/9"} event{ a="A" x=2 y=5}
senp{version=1 method="PUB" id="\000" to="c/1a"} event{ a="X" x=2 z=7}
senp{version=1 method="PUB" id="\000" to="c/1b"} event{ a="X" x=2 z=7}
senp{version=1 method="PUB" id="\000" to="c/12"} event{ a="X" x=2 z=7}
senp{version=1 method="PUB" id="\000" to="c/3"} event{ a="X" x=2 z=7}
senp{version=1 method="PUB" id="\000" to="c/6"} event{ a="X" x=2 z=7}
senp{version=1 method="PUB" id="\000" to="c/1a"} event{ a="X" x=2 z=7}
senp{version=1 method="PUB" id="\000" to="c/1b"} event{ a="X" x=2 z=7}
senp{version=1 method="PUB" id="\000" to="c/12"} event{ a="X" x=2 z=7}
senp{version=1 method="PUB" id="\000" to="c/13"} event{ a="X" x=2 z=7}
senp{version=1 method="PUB" id="\000" to="c/3"} event{ a="X" x=2 z=7}
senp{version=1 method="PUB" id="\000" to="c/6"} event{ a="X" x=2 z=7}
senp{version=1 method="PUB" id="\000" to="c/1a"} event{ a="X" x=2 z=7}
senp{version=1 method="PUB" id="\000" to="c/1b"} event{ a="X" x=2 z=7}
senp{version=1 method="PUB" id="\000" to="c/12"} event{ a="X" x=2 z=7}
senp{version=1 method="PUB" id="\000" to="c/13"} event{ a="X" x=2 z=7}
senp{version=1 method="PUB" id="\000" to="c/3"} event{ a="X" x=2 z=7}
senp{version=1 method="PUB" id="\000" to="c/6"} event{ a="X" x=2 z=7}
senp{version=1 method="PUB" id="\000" to="c/13"} event{ z=7}
senp{version=1 method="PUB" id="\000" to="c/3"} event{ z=7}
senp{version=1 method="PUB" id="\000" to="c/14"} event{ z=7}
senp{version=1 method="PUB" id="\000" to="c/13"} event{ z=7}
senp{version=1 method="PUB" id="\000" to="c/3"} event{ z=7}
senp{version=1 method="PUB" id="\000" to="c/14"} event{ z=7}
senp{version=1 method="PUB" id="\000" to="c/13"} event{ z=7}
senp{version=1 method="PUB" id="\000" to="c/14"} event{ z=7}
senp{version=1 method="PUB" id="\000" to="c/13"} event{ z=7}
senp{version=1 method="PUB" id="\000" to="c/14"} event{ z=7}
senp{version=1 method="PUB" id="\000" to="c/14"} event{ z=7}
senp{version=1 method="PUB" id="\000" to="c/12"} event{ a="Y" x=2 z=7}
senp{version=1 method="PUB" id="\000" to="c/1a"} event{ a="Y" x=2 z=7}
senp{version=1 method="PUB" id="\000" to="c/1b"} event{ a="Y" x=2 z=7}
senp{version=1 method="PUB" id="\000" to="c/6"} event{ a="Y" x=2 z=7}
senp{version=1 method="PUB" id="\000" to="c/1a"} event{ a="Y" x=2 z=7}
senp{version=1 method="PUB" id="\000" to="c/1b"} event{ a="Y" x=2 z=7}
senp{version=1 method="PUB" id="\000" to="c/12"} event{ a="Y" x=2 z=7}
senp{version=1 method="PUB" id="\000" to="c/1a"} event{ x=2 y=17}
senp{version=1 method="PUB" id="\000" to="c/1b"} event{ x=2 y=17}
senp{version=1 method="PUB" id="\000" to="c/15"} event{ x=2 y=17}
senp{version=1 method="PUB" id="\000" to="c/2"} event{ x=2 y=17}
senp{version=1 method="PUB" id="\000" to="c/9"} event{ x=2 y=17}
senp{version=1 method="PUB" id="\000" to="c/15"} event{ x=2 y=18}
senp{version=1 method="PUB" id="\000" to="c/1a"} event{ x=2 y=18}
senp{version=1 method="PUB" id="\000" to="c/1b"} event{ x=2 y=18}
senp{version=1 method="PUB" id="\000" to="c/1a"} event{ x=2 y=18 z=1 a="A"}
senp{version=1 method="PUB" id="\000" to="c/1b"} event{ x=2 y=18 z=1 a="A"}
senp{version=1 method="PUB" id="\000" to="c/15"} event{ x=2 y=18 z=1 a="A"}
senp{version=1 method="PUB" id="\000" to="c/10"} event{ x=2 y=18 z=1 a="A"}
senp{version=1 method="PUB" id="\000" to="c/12"} event{ x=2 y=18 z=1 a="A"}
senp{version=1 method="PUB" id="\000" to="c/8"} event{ x=2 y=18 z=1 a="A"}
senp{version=1 method="PUB" id="\000" to="c/1a"} event{ x=2 y=20 z=1 a="A"}
senp{version=1 method="PUB" id="\000" to="c/1b"} event{ x=2 y=20 z=1 a="A"}
senp{version=1 method="PUB" id="\000" to="c/15"} event{ x=2 y=20 z=1 a="A"}
senp{version=1 method="PUB" id="\000" to="c/8"} event{ x=2 y=20 z=1 a="A"}
senp{version=1 method="PUB" id="\000" to="c/15"} event{ x=2 y=20 z=1 a="A"}
senp{version=1 method="PUB" id="\000" to="c/1a"} event{ x=2 y=20 z=1 a="A"}
senp{version=1 method="PUB" id="\000" to="c/1b"} event{ x=2 y=20 z=1 a="A"}
senp{version=1 method="PUB" id="\000" to="c/15"} event{ x=2 y=21 z=1 a="A"}
senp{version=1 method="PUB" id="\000" to="c/1a"} event{ x=2 y=21 z=1 a="A"}
senp{version=1 method="PUB" id="\000" to="c/1b"} event{ x=2 y=21 z=1 a="A"}
senp{version=1 method="PUB" id="\000" to="c/1a"} event{ x=20 y=20 z=1 a="A"}
senp{version=1 method="PUB" id="\000" to="c/1b"} event{ x=20 y=20 z=1 a="A"}
senp{version=1 method="PUB" id="\000" to="c/1b"} event{ x=20 y=20 z=20}
EOF
sleep 4
kill $cleanup_procs
cleanup_procs=''
if $JAVA siena.Compare test.out test.expected
then
    test_passed
else
    test_failed 'see test.out, test.expected, and test.log for detail'
fi
