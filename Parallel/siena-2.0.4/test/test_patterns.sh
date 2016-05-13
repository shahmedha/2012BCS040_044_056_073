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
test_description Testing patterns with HierarchicalDispatcher...
#
cleanup_files="test.out test.expected sender.log raddr"
#
crcv=ka:3000:localhost
rm -f test.out test.expected sender.log
#
echo "looking at 2 patterns a b ..."
#
$JAVA sienatest.TestPatterns $crcv:raddr 2 a b >> test.out &
client=$!
cleanup_procs="$cleanup_procs $!"
#
sleep 3
handler=`cat raddr`
#
$JAVA siena.Sender 200 >> sender.log 2>&1 <<EOF 
senp{ to="$handler" method="PUB"} event{ x="a" n=1}
senp{ to="$handler" method="PUB"} event{ x="b" n=2}
senp{ to="$handler" method="PUB"} event{ x="b" n=3}
senp{ to="$handler" method="PUB"} event{ x="a" n=4}
senp{ to="$handler" method="PUB"} event{ x="a" n=5}
senp{ to="$handler" method="PUB"} event{ x="x" n=6}
senp{ to="$handler" method="PUB"} event{ x="a" n=7}
senp{ to="$handler" method="PUB"} event{ x="y" n=8}
senp{ to="$handler" method="PUB"} event{ x="b" n=9}
senp{ to="$handler" method="PUB"} event{ x="a" n=10}
EOF
cat >> test.expected <<EOF
pattern:
0 { n=1 x="a"}
1 { n=2 x="b"}
pattern:
0 { n=7 x="a"}
1 { n=9 x="b"}
unsubscribing.
EOF
#
wait $client
#
echo "looking at 1 pattern a b ..."
#
$JAVA sienatest.TestPatterns $crcv:raddr 1 a b >> test.out &
client=$!
#
sleep 3
handler=`cat raddr`
#
$JAVA siena.Sender 200 >> sender.log 2>&1 <<EOF 
senp{ to="$handler" method="PUB"} event{ x="a" n=51}
senp{ to="$handler" method="PUB"} event{ x="b" n=52}
senp{ to="$handler" method="PUB"} event{ x="b" n=53}
senp{ to="$handler" method="PUB"} event{ x="a" n=54}
senp{ to="$handler" method="PUB"} event{ x="a" n=55}
senp{ to="$handler" method="PUB"} event{ x="x" n=56}
senp{ to="$handler" method="PUB"} event{ x="a" n=57}
senp{ to="$handler" method="PUB"} event{ x="y" n=58}
senp{ to="$handler" method="PUB"} event{ x="b" n=59}
senp{ to="$handler" method="PUB"} event{ x="a" n=510}
EOF
cat >> test.expected <<EOF
pattern:
0 { n=51 x="a"}
1 { n=52 x="b"}
unsubscribing.
EOF
#
wait $client
#
echo "looking at 2 patterns a b c ..."
$JAVA sienatest.TestPatterns $crcv:raddr 2 a b c >> test.out &
client=$!
#
sleep 3
handler=`cat raddr`
#
$JAVA siena.Sender >> sender.log 2>&1 200 <<EOF 
senp{ to="$handler" method="PUB"} event{ x="a" n=11}
senp{ to="$handler" method="PUB"} event{ x="a" n=12}
senp{ to="$handler" method="PUB"} event{ x="b" n=13}
senp{ to="$handler" method="PUB"} event{ x="a" n=14}
senp{ to="$handler" method="PUB"} event{ x="b" n=15}
senp{ to="$handler" method="PUB"} event{ x="c" n=16}
senp{ to="$handler" method="PUB"} event{ x="b" n=17}
senp{ to="$handler" method="PUB"} event{ x="c" n=18}
senp{ to="$handler" method="PUB"} event{ x="a" n=19}
senp{ to="$handler" method="PUB"} event{ x="a" n=20}
senp{ to="$handler" method="PUB"} event{ x="a" n=21}
senp{ to="$handler" method="PUB"} event{ x="y" n=22}
senp{ to="$handler" method="PUB"} event{ x="b" n=23}
senp{ to="$handler" method="PUB"} event{ x="z" n=24}
senp{ to="$handler" method="PUB"} event{ x="c" n=25}
senp{ to="$handler" method="PUB"} event{ x="c" n=26}
EOF
cat >> test.expected <<EOF
pattern:
0 { n=14 x="a"}
1 { n=15 x="b"}
2 { n=16 x="c"}
pattern:
0 { n=21 x="a"}
1 { n=23 x="b"}
2 { n=25 x="c"}
unsubscribing.
EOF
#
wait $client
#
echo "looking at 1 pattern a b a c ..."
$JAVA sienatest.TestPatterns $crcv:raddr 1 a b a c >> test.out &
client=$!
#
sleep 3
handler=`cat raddr`
#
$JAVA siena.Sender 200 >> sender.log 2>&1 <<EOF 
senp{ to="$handler" method="PUB"} event{ x="b" n=30}
senp{ to="$handler" method="PUB"} event{ x="a" n=31}
senp{ to="$handler" method="PUB"} event{ x="b" n=32}
senp{ to="$handler" method="PUB"} event{ x="a" n=33}
senp{ to="$handler" method="PUB"} event{ x="b" n=34}
senp{ to="$handler" method="PUB"} event{ x="x" n=35}
senp{ to="$handler" method="PUB"} event{ x="a" n=36}
senp{ to="$handler" method="PUB"} event{ x="c" n=37}
senp{ to="$handler" method="PUB"} event{ x="a" n=38}
senp{ to="$handler" method="PUB"} event{ x="a" n=39}
senp{ to="$handler" method="PUB"} event{ x="a" n=21}
senp{ to="$handler" method="PUB"} event{ x="y" n=22}
senp{ to="$handler" method="PUB"} event{ x="b" n=23}
senp{ to="$handler" method="PUB"} event{ x="z" n=24}
senp{ to="$handler" method="PUB"} event{ x="c" n=25}
senp{ to="$handler" method="PUB"} event{ x="c" n=26}
EOF
cat >> test.expected <<EOF
pattern:
0 { n=33 x="a"}
1 { n=34 x="b"}
2 { n=36 x="a"}
3 { n=37 x="c"}
unsubscribing.
EOF
#
wait
#
if diff test.out test.expected; then
    test_passed
else
    test_failed 'see test.out and test.expected for details'
fi
