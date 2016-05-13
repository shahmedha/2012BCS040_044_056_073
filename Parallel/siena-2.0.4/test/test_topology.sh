#!/bin/sh
#
#  This file is part of Siena, a wide-area event notification system.
#  See http://www.inf.usi.ch/carzaniga/siena/
#
#  Author: Amir Malekpour (malekpoa@usi.ch)
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
test_description "Testing DVDRPControl and broker topology..."
#
startserver () {
	id=$1
	hndlr=$2
    echo "   starting server $id on $hndlr"
    $JAVA siena.StartDVDRPServer -id $id -receiver $hndlr -log - > server$id.log 2>&1 &
    sleep 2
    echo $! >> server.pids
}
#
killservers () {
    if test -r server.pids; then
	echo shutting down running servers...
	xargs -n 1 kill < server.pids
	sleep 2
	rm -f server.pids topology.info server[0-9]*.log
    fi
}


# Lets first define some broker handlers
b1_id=B1
b1_hndlr=ka:localhost:7011
b2_id=B2
b2_hndlr=ka:localhost:7012
b3_id=B3
b3_hndlr=ka:localhost:7013
b4_id=B4
b4_hndlr=ka:localhost:7014
b5_id=B5
b5_hndlr=ka:localhost:7015

rm -f server.pids

# start the brokers
startserver $b1_id $b1_hndlr
startserver $b2_id $b2_hndlr
startserver $b3_id $b3_hndlr
startserver $b4_id $b4_hndlr
startserver $b5_id $b5_hndlr

# Now we put the topology into a temp config file just to test
# if DVDRPControl properly reads config from a file 
echo "# We also add some random comment to test commenting in the config file 
$b1_hndlr connect $b2_id $b2_hndlr 67
$b2_hndlr connect $b1_id $b1_hndlr 67
$b2_hndlr connect $b3_id $b3_hndlr 6
$b3_hndlr connect $b2_id $b2_hndlr 6

   # Empty lines in the file to test that DVDRPControl won't panic ...

$b3_hndlr connect $b4_id $b4_hndlr 32
$b4_hndlr connect $b3_id $b3_hndlr 32
$b4_hndlr connect $b5_id $b5_hndlr 9
$b5_hndlr connect $b4_id $b4_hndlr 9" > temp.top

# Invoke the tool with our conf file then remove the temp file
$JAVA siena.DVDRPControl -f temp.top > DVDRPControl.log 2>&1 

sleep 3
rm temp.top
#
subaddr1=ka:9114:localhost
subaddr2=ka:9115:localhost
#
rm -f test.out.[12] test.expected.[12]
cleanup_files='serverB1.log serverB2.log serverB3.log serverB4.log serverB5.log \
test.out.1 test.expected.1 test.out.2 test.expected.2 \
publisher.log DVDRPControl.log'
#
sleep 2
#
echo "starting up thin client (subscriber) 1..."
#
$JAVA siena.TestThinClient $b4_hndlr sub $subaddr1 1 2 > test.out.1 2>&1 &
cleanup_procs="$cleanup_procs $!"
#
sleep 2
#
echo "starting up thin client (subscriber) 2..."
#
$JAVA siena.TestThinClient $b5_hndlr sub $subaddr2 2 3 4 > test.out.2 2>&1 &
cleanup_procs="$cleanup_procs $!"
#
sleep 3
#
echo "starting up thin client (publisher)..."
#
$JAVA siena.TestThinClient $b1_hndlr pub 1 2 1 2 0 3 5 1 3 4 1 2 > publisher.log 2>&1 
#
killservers
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
#
