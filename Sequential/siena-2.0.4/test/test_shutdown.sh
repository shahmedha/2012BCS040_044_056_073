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
test_shutdown () {
    test_description "Testing shutdown function with $1..."
    $JAVA sienatest.TestShutdown 1000 $1:2222 &
    pid=$!
    cleanup_procs='$!'
#
# runs the killer process (kills the server after some time);
#
    (sleep 7; kill $server > /dev/null 2>&1 &)
#
# see what happened...
#
    wait $pid
    res="$?"
    cleanup_procs=''
    case "$res" in
	0 | 127 ) 
	    test_passed_continue
	    return 0
	    ;;
	*)
	    test_failed 'see test.log for details'
	    ;;
    esac
}
#
test_shutdown ka
test_shutdown udp
test_shutdown tcp
exit 0
