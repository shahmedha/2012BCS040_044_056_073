#!/bin/sh
#
#  This file is part of Siena, a wide-area event notification system.
#  See http://www.cs.colorado.edu/serl/siena/
#
#  Author: Antonio Carzaniga <carzanig@cs.colorado.edu>
#  See the file AUTHORS for full details. 
#
#  Copyright (C) 1998-2001 University of Colorado
#
#  This program is free software; you can redistribute it and/or
#  modify it under the terms of the GNU General Public License
#  as published by the Free Software Foundation; either version 2
#  of the License, or (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with this program; if not, write to the Free Software
#  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307,
#  USA, or send email to serl@cs.colorado.edu.
#
#
# $Id: test_thinclient.sh,v 1.1 2003/07/09 20:16:11 carzanig Exp $
#
port=2323
#
cat > test.expected <<EOF
EOF
#
echo Testing ThinClient with TCPReceiver...
#
./TestTCPReceiver $port localhost > test.out 2>&1 &
receiverpid=$!
sleep 2
#
./TestSender tcp:localhost:$port < test.in
#
echo waiting for TCP receiver to terminate...
# (sleep 5; kill $receiverpid &)
wait $receiverpid
#
diff test.out test.expected || exit 1 
#
echo Testing Sender and UDPReceiver...
#
./TestUDPReceiver $port localhost > test.out 2>&1 &
receiverpid=$!
sleep 2
#
./TestSender udp:localhost:$port < test.in
#
echo waiting for UDP receiver to terminate...
# (sleep 5; kill $receiverpid &)
wait $receiverpid
#
if diff test.out test.expected; then
    rm -f test.out test.expected
    echo PASSED.
    exit 0
else
    exit 1 
fi
