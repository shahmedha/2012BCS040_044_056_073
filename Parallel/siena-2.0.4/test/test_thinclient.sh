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
if [ -z "$srcdir" ]
then
    srcdir=.
fi
#
if [ -r ./config.sh ]
then
    . ./config.sh
elif [ -r $srcdir/static_config.sh ]
then
    . $srcdir/static_config.sh
fi
#
test_description "Testing ThinClient..."
#
#. $srcdir/util.sh
sserver () {
    id=$1
    shift
    conn=$1
    shift
    port=$1
    shift
    case "$1" in
	'' )
	    master=''
	    ;;
	[0-9]*)
	    eval "master=\$siena$1"
	    master="-master $master"
	    shift
	    ;;
    esac
    extra="$@"

    echo "starting server $id with $conn on port $port $master $extra..."

    $JAVA siena.StartDVDRPServer -id S$id -receiver $conn:localhost:$port -log - $extra > server$id.log 2>&1 &
    cleanup_procs="$cleanup_procs $!"
    eval "siena${id}_pid=$!"
    cleanup_files="$cleanup_files server$id.log server$id.store"
    sleep 3
    eval "siena$id=$conn:localhost:$port"
}
#
sserver 0 ka 2170
#
subaddr1=ka:9876:localhost
subaddr2=ka:9875:localhost
#
rm -f test.out.[12] test.expected.[12]
cleanup_files='server.log test.out.1 test.expected.1 test.out.2 test.expected.2 publisher.log'
#
sleep 2
#
echo "starting up thin client (subscriber) 1..."
#
$JAVA siena.TestThinClient $siena0 sub $subaddr1 1 2 > test.out.1 2>&1 &
cleanup_procs="$cleanup_procs $!"
#
sleep 2
#
echo "starting up thin client (subscriber) 2..."
#
$JAVA siena.TestThinClient $siena0 sub $subaddr2 2 3 4 > test.out.2 2>&1 &
cleanup_procs="$cleanup_procs $!"
#
sleep 2
#
echo "starting up thin client (publisher)..."
#
$JAVA siena.TestThinClient $siena0 pub 1 2 1 2 0 3 5 1 3 4 1 2 > publisher.log 2>&1 
#
cat > test.expected.1 <<EOF
ThinClientSub: { x="1" y=1}
ThinClientSub: { x="2" y=2}
ThinClientSub: { x="1" y=3}
ThinClientSub: { x="2" y=4}
ThinClientSub: { x="1" y=8}
ThinClientSub: { x="1" y=11}
ThinClientSub: { x="2" y=12}
EOF
cat > test.expected.2 <<EOF
ThinClientSub: { x="2" y=2}
ThinClientSub: { x="2" y=4}
ThinClientSub: { x="3" y=6}
ThinClientSub: { x="3" y=9}
ThinClientSub: { x="4" y=10}
ThinClientSub: { x="2" y=12}
EOF
#
(sleep 2; kill $cleanup_procs &)
wait $cleanup_procs
cleanup_procs=''
sleep 1
if diff test.out.1 test.expected.1 && diff test.out.2 test.expected.2 ; then
    test_passed
else
    test_failed 'see test.out.1, test.expected.1, test.out.2, and test.expected.2' 
fi
