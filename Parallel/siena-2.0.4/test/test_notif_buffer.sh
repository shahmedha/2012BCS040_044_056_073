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
test_description Testing NotificationBuffer locally...
#
cleanup_files='test.out test.expected'
#
# -------------------- TEST BEGINS HERE --------------------
#
$JAVA sienatest.TestNotificationBuffer2 10 > test.out
cat > test.expected <<EOF
9
8
7
6
5
4
3
2
1
0
EOF
if diff test.out test.expected; then
    test_passed_continue
else
    test_failed 'see test.out and test.expected for details'
fi
#
test_description Testing NotificationBuffer through local server...
#
if $JAVA sienatest.TestNotificationBuffer; then
    test_passed
else
    test_failed
fi
