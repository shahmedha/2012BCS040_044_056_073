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
test_rcv_shutdown() {
    test_description Testing shutdown on $1 receiver with $2 threads...
    if $JAVA sienatest.TestReceiverShutdown 5000 $2 $1:2323 ; then
	test_passed_continue
    else
	test_failed 'see test.log for details'
    fi
    return 0
}
#
# -------------------- TEST BEGINS HERE --------------------
#
test_rcv_shutdown ka 1
test_rcv_shutdown tcp 1
test_rcv_shutdown udp 1
test_rcv_shutdown ka 4
test_rcv_shutdown tcp 4
test_rcv_shutdown udp 4
test_rcv_shutdown ka 16
test_rcv_shutdown tcp 16
test_rcv_shutdown udp 16
