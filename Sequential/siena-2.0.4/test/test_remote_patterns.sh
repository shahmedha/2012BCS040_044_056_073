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
test_description Testing multiple distributed patterns...
#
cleanup_files='test.out test.expected sender.out raddr'
. $srcdir/util.sh
#
sserver 0 ka 2222
sserver 1 ka 2223 0
sserver 2 ka 2224 0
sserver 3 ka 2225 2
#
crcv=tcp:7070:localhost
$JAVA sienatest.Receiver $crcv:raddr > test.out &
clientpid=$!
sleep 6
handler=`cat raddr`
#
echo "executing test..."
#
$JAVA siena.Sender 400 <<EOF > sender.log
#
# -------------------- TEST BEGINS HERE --------------------
#
senp{ to="$siena3" method="SUB" handler="$handler" id="c/1"} pattern{filter{x=1}filter{x=2}}
senp{ to="$siena0" method="SUB" handler="$handler" id="c/2"} pattern{filter{x any}filter{x=2}}
senp{ to="$siena1" method="SUB" handler="$handler" id="c/3"} pattern{filter{x>2}filter{x=1}filter{x=2}}
senp{ to="$siena1" method="SUB" handler="$handler" id="c/4"} filter{x=5}
#
senp{ to="$siena0" method="PUB"} event{x=4 n=1}
senp{ to="$siena1" method="PUB"} event{x=3 n=2}
senp{ to="$siena2" method="PUB"} event{x=2 n=3}
senp{ to="$siena3" method="PUB"} event{x=1 n=4}
senp{ to="$siena0" method="PUB"} event{x=5 n=5}
senp{ to="$siena1" method="PUB"} event{x=1 n=6}
senp{ to="$siena2" method="PUB"} event{x=2 n=7}
senp{ to="$siena3" method="PUB"} event{x=5 n=8}
senp{ to="$siena0" method="PUB"} event{x=1 n=9}
senp{ to="$siena1" method="PUB"} event{x=5 n=10}
senp{ to="$siena2" method="PUB"} event{y=2 n=11}
senp{ to="$siena2" method="PUB"} event{x=2 n=12}
senp{ to="$siena2" method="PUB"} event{x=2 n=13}
senp{ to="$siena3" method="PUB"} event{x=3 n=14}
#
senp{ to="$siena0" method="SUB" handler="$handler" id="c/5"} pattern{filter{x any}filter{y=2}}
senp{ to="$siena1" method="SUB" handler="$handler" id="c/3"} filter{x=1}
senp{ to="$siena3" method="SUB" handler="$handler" id="c/1"} pattern{filter{x >= 1 x < 5}filter{x=2}}
#
senp{ to="$siena0" method="PUB"} event{x=4 n=15}
senp{ to="$siena1" method="PUB"} event{x=3 n=16}
senp{ to="$siena2" method="PUB"} event{x=2 n=17}
senp{ to="$siena3" method="PUB"} event{x=1 n=18}
senp{ to="$siena0" method="PUB"} event{x=5 n=19}
senp{ to="$siena1" method="PUB"} event{x=1 n=20}
senp{ to="$siena2" method="PUB"} event{x=2 n=21}
senp{ to="$siena3" method="PUB"} event{x=5 n=22}
senp{ to="$siena0" method="PUB"} event{x=1 n=23}
senp{ to="$siena1" method="PUB"} event{x=5 n=24}
senp{ to="$siena2" method="PUB"} event{y=2 n=25}
senp{ to="$siena2" method="PUB"} event{x=2 n=26}
senp{ to="$siena2" method="PUB"} event{x=2 n=27}
senp{ to="$siena3" method="PUB"} event{x=3 n=28}
#
senp{ to="$siena0" method="SUB" handler="$handler" id="c/5"} pattern{filter{x=2}filter{y=2}}
senp{ to="$siena3" method="SUB" handler="$handler" id="c/6"} pattern{filter{x >= 1 x < 5}filter{x=2}}
#
senp{ to="$siena0" method="PUB"} event{x=4 n=29}
senp{ to="$siena1" method="PUB"} event{x=3 n=30}
senp{ to="$siena2" method="PUB"} event{x=2 n=31}
senp{ to="$siena3" method="PUB"} event{x=1 n=32}
senp{ to="$siena0" method="PUB"} event{x=5 n=33}
senp{ to="$siena1" method="PUB"} event{x=1 n=34}
senp{ to="$siena2" method="PUB"} event{x=2 n=35}
senp{ to="$siena3" method="PUB"} event{x=5 n=36}
senp{ to="$siena0" method="PUB"} event{x=1 n=37}
senp{ to="$siena1" method="PUB"} event{x=5 n=38}
senp{ to="$siena2" method="PUB"} event{y=2 n=39}
senp{ to="$siena2" method="PUB"} event{x=2 n=40}
senp{ to="$siena2" method="PUB"} event{x=2 n=41}
senp{ to="$siena3" method="PUB"} event{x=3 n=42}
#
senp{ to="$siena3" method="BYE" handler="$handler" id="c/1"}
#
senp{ to="$siena0" method="PUB"} event{x=4 n=129}
senp{ to="$siena1" method="PUB"} event{x=3 n=130}
senp{ to="$siena2" method="PUB"} event{x=2 n=131}
senp{ to="$siena3" method="PUB"} event{x=1 n=132}
senp{ to="$siena0" method="PUB"} event{x=5 n=133}
senp{ to="$siena1" method="PUB"} event{x=1 n=134}
senp{ to="$siena2" method="PUB"} event{x=2 n=135}
senp{ to="$siena3" method="PUB"} event{x=5 n=136}
senp{ to="$siena0" method="PUB"} event{x=1 n=137}
senp{ to="$siena1" method="PUB"} event{x=5 n=138}
senp{ to="$siena2" method="PUB"} event{y=2 n=139}
senp{ to="$siena2" method="PUB"} event{x=2 n=140}
senp{ to="$siena2" method="PUB"} event{x=2 n=141}
senp{ to="$siena3" method="PUB"} event{x=3 n=142}
#
senp{ to="$siena1" method="UNS" handler="$handler" id="c/3"} pattern{filter{x any}filter{x any}filter{x=2}}
#
senp{ to="$siena0" method="PUB"} event{x=4 n=229}
senp{ to="$siena1" method="PUB"} event{x=3 n=230}
senp{ to="$siena2" method="PUB"} event{x=2 n=231}
senp{ to="$siena3" method="PUB"} event{x=1 n=232}
senp{ to="$siena0" method="PUB"} event{x=5 n=233}
senp{ to="$siena1" method="PUB"} event{x=1 n=234}
senp{ to="$siena2" method="PUB"} event{x=2 n=235}
senp{ to="$siena3" method="PUB"} event{x=5 n=236}
senp{ to="$siena0" method="PUB"} event{x=1 n=237}
senp{ to="$siena1" method="PUB"} event{x=5 n=238}
senp{ to="$siena2" method="PUB"} event{y=2 n=239}
senp{ to="$siena2" method="PUB"} event{x=2 n=240}
senp{ to="$siena2" method="PUB"} event{x=2 n=241}
senp{ to="$siena3" method="PUB"} event{x=3 n=242}
#
senp{ to="$siena0" method="UNS" handler="$handler" id="c/2"} pattern{filter{x any}filter{x=2}}
senp{ to="$siena0" method="UNS" handler="$handler" id="c/5"} pattern{filter{x any}filter{y=2}}
senp{ to="$siena3" method="UNS" handler="$handler" id="c/6"} pattern{filter{x >= 1 x < 5}filter{x=2}}
#
senp{ to="$siena0" method="PUB"} event{x=4 n=329}
senp{ to="$siena1" method="PUB"} event{x=3 n=330}
senp{ to="$siena2" method="PUB"} event{x=2 n=331}
senp{ to="$siena3" method="PUB"} event{x=1 n=332}
senp{ to="$siena0" method="PUB"} event{x=5 n=333}
senp{ to="$siena1" method="PUB"} event{x=1 n=334}
senp{ to="$siena2" method="PUB"} event{x=2 n=335}
senp{ to="$siena3" method="PUB"} event{x=5 n=336}
senp{ to="$siena0" method="PUB"} event{x=1 n=337}
senp{ to="$siena1" method="PUB"} event{x=5 n=338}
senp{ to="$siena2" method="PUB"} event{y=2 n=339}
senp{ to="$siena2" method="PUB"} event{x=2 n=340}
senp{ to="$siena2" method="PUB"} event{x=2 n=341}
senp{ to="$siena3" method="PUB"} event{x=3 n=342}
#
senp{ to="$siena1" method="BYE" handler="$handler" id="c/3"}
senp{ to="$siena1" method="UNS" handler="$handler" id="c/4"} filter{x=5}
#
senp{ to="$siena0" method="PUB"} event{x=4 n=429}
senp{ to="$siena1" method="PUB"} event{x=3 n=430}
senp{ to="$siena2" method="PUB"} event{x=2 n=431}
senp{ to="$siena3" method="PUB"} event{x=1 n=432}
senp{ to="$siena0" method="PUB"} event{x=5 n=433}
senp{ to="$siena1" method="PUB"} event{x=1 n=434}
senp{ to="$siena2" method="PUB"} event{x=2 n=435}
senp{ to="$siena3" method="PUB"} event{x=5 n=436}
senp{ to="$siena0" method="PUB"} event{x=1 n=437}
senp{ to="$siena1" method="PUB"} event{x=5 n=438}
senp{ to="$siena2" method="PUB"} event{y=2 n=439}
senp{ to="$siena2" method="PUB"} event{x=2 n=440}
senp{ to="$siena2" method="PUB"} event{x=2 n=441}
senp{ to="$siena3" method="PUB"} event{x=3 n=442}
EOF
#
#
cat <<EOF > test.expected
senp{method="PUB" id="\000" to="c/2"} events{ event{x=3 n=2} event{x=2 n=3}}
senp{method="PUB" id="\000" to="c/4"} event{x=5 n=5}
senp{method="PUB" id="\000" to="c/2"} events{ event{x=1 n=6} event{x=2 n=7}}
senp{method="PUB" id="\000" to="c/1"} events{ event{x=1 n=6} event{x=2 n=7}}
senp{method="PUB" id="\000" to="c/3"} events{ event{x=5 n=5} event{x=1 n=6} event{x=2 n=7}}
senp{method="PUB" id="\000" to="c/4"} event{x=5 n=8}
senp{method="PUB" id="\000" to="c/4"} event{x=5 n=10}
senp{method="PUB" id="\000" to="c/1"} events{ event{x=1 n=9} event{x=2 n=12}}
senp{method="PUB" id="\000" to="c/2"} events{ event{x=5 n=10} event{x=2 n=12}}
#
senp{method="PUB" id="\000" to="c/2"} events{ event{x=3 n=16} event{x=2 n=17}}
senp{method="PUB" id="\000" to="c/5"} events{ event{x=5 n=24} event{y=2 n=25}}
senp{method="PUB" id="\000" to="c/1"} events{ event{x=3 n=16} event{x=2 n=17}}
senp{method="PUB" id="\000" to="c/3"} event{x=1 n=18}
senp{method="PUB" id="\000" to="c/4"} event{x=5 n=19}
senp{method="PUB" id="\000" to="c/3"} event{x=1 n=20}
senp{method="PUB" id="\000" to="c/2"} events{ event{x=1 n=20} event{x=2 n=21}}
senp{method="PUB" id="\000" to="c/1"} events{ event{x=1 n=20} event{x=2 n=21}}
senp{method="PUB" id="\000" to="c/3"} events{ event{x=5 n=19} event{x=1 n=20} event{x=2 n=21}}
senp{method="PUB" id="\000" to="c/4"} event{x=5 n=22}
senp{method="PUB" id="\000" to="c/4"} event{x=5 n=24}
senp{method="PUB" id="\000" to="c/3"} event{x=1 n=23}
senp{method="PUB" id="\000" to="c/1"} events{ event{x=1 n=23} event{x=2 n=26}}
senp{method="PUB" id="\000" to="c/2"} events{ event{x=5 n=24} event{x=2 n=26}}
#
senp{method="PUB" id="\000" to="c/2"} events{ event{x=3 n=30} event{x=2 n=31}}
senp{method="PUB" id="\000" to="c/5"} events{ event{x=5 n=38} event{y=2 n=39}}
senp{method="PUB" id="\000" to="c/1"} events{ event{x=3 n=30} event{x=2 n=31}}
senp{method="PUB" id="\000" to="c/6"} events{ event{x=3 n=30} event{x=2 n=31}}
senp{method="PUB" id="\000" to="c/3"} event{x=1 n=32}
senp{method="PUB" id="\000" to="c/4"} event{x=5 n=33}
senp{method="PUB" id="\000" to="c/3"} event{x=1 n=34}
senp{method="PUB" id="\000" to="c/2"} events{ event{x=1 n=34} event{x=2 n=35}}
senp{method="PUB" id="\000" to="c/1"} events{ event{x=1 n=34} event{x=2 n=35}}
senp{method="PUB" id="\000" to="c/6"} events{ event{x=1 n=34} event{x=2 n=35}}
senp{method="PUB" id="\000" to="c/3"} events{ event{x=5 n=33} event{x=1 n=34} event{x=2 n=35}}
senp{method="PUB" id="\000" to="c/4"} event{x=5 n=36}
senp{method="PUB" id="\000" to="c/4"} event{x=5 n=38}
senp{method="PUB" id="\000" to="c/3"} event{x=1 n=37}
senp{method="PUB" id="\000" to="c/1"} events{ event{x=1 n=37} event{x=2 n=40}}
senp{method="PUB" id="\000" to="c/6"} events{ event{x=1 n=37} event{x=2 n=40}}
senp{method="PUB" id="\000" to="c/2"} events{ event{x=5 n=38} event{x=2 n=40}}
#
senp{method="PUB" id="\000" to="c/2"} events{ event{x=3 n=130} event{x=2 n=131}}
senp{method="PUB" id="\000" to="c/5"} events{ event{x=5 n=138} event{y=2 n=139}}
senp{method="PUB" id="\000" to="c/6"} events{ event{x=3 n=130} event{x=2 n=131}}
senp{method="PUB" id="\000" to="c/3"} event{x=1 n=132}
senp{method="PUB" id="\000" to="c/4"} event{x=5 n=133}
senp{method="PUB" id="\000" to="c/3"} event{x=1 n=134}
senp{method="PUB" id="\000" to="c/2"} events{ event{x=1 n=134} event{x=2 n=135}}
senp{method="PUB" id="\000" to="c/6"} events{ event{x=1 n=134} event{x=2 n=135}}
senp{method="PUB" id="\000" to="c/3"} events{ event{x=5 n=133} event{x=1 n=134} event{x=2 n=135}}
senp{method="PUB" id="\000" to="c/4"} event{x=5 n=136}
senp{method="PUB" id="\000" to="c/4"} event{x=5 n=138}
senp{method="PUB" id="\000" to="c/3"} event{x=1 n=137}
senp{method="PUB" id="\000" to="c/6"} events{ event{x=1 n=137} event{x=2 n=140}}
senp{method="PUB" id="\000" to="c/2"} events{ event{x=5 n=138} event{x=2 n=140}}
#
senp{method="PUB" id="\000" to="c/2"} events{ event{x=3 n=230} event{x=2 n=231}}
senp{method="PUB" id="\000" to="c/5"} events{ event{x=5 n=238} event{y=2 n=239}}
senp{method="PUB" id="\000" to="c/6"} events{ event{x=3 n=230} event{x=2 n=231}}
senp{method="PUB" id="\000" to="c/3"} event{x=1 n=232}
senp{method="PUB" id="\000" to="c/4"} event{x=5 n=233}
senp{method="PUB" id="\000" to="c/3"} event{x=1 n=234}
senp{method="PUB" id="\000" to="c/2"} events{ event{x=1 n=234} event{x=2 n=235}}
senp{method="PUB" id="\000" to="c/6"} events{ event{x=1 n=234} event{x=2 n=235}}
senp{method="PUB" id="\000" to="c/4"} event{x=5 n=236}
senp{method="PUB" id="\000" to="c/4"} event{x=5 n=238}
senp{method="PUB" id="\000" to="c/3"} event{x=1 n=237}
senp{method="PUB" id="\000" to="c/6"} events{ event{x=1 n=237} event{x=2 n=240}}
senp{method="PUB" id="\000" to="c/2"} events{ event{x=5 n=238} event{x=2 n=240}}
#
#
senp{method="PUB" id="\000" to="c/3"} event{x=1 n=332}
senp{method="PUB" id="\000" to="c/4"} event{x=5 n=333}
senp{method="PUB" id="\000" to="c/3"} event{x=1 n=334}
senp{method="PUB" id="\000" to="c/4"} event{x=5 n=336}
senp{method="PUB" id="\000" to="c/4"} event{x=5 n=338}
senp{method="PUB" id="\000" to="c/3"} event{x=1 n=337}
EOF
sleep 5
kill $clientpid
wait $clientpid
if $JAVA siena.Compare test.out test.expected; then
    test_passed
else
    test_failed 'see test.out, test.expected, and sender.log for details'
fi
