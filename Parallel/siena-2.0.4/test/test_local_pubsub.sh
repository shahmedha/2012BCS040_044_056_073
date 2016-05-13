#!/bin/sh
#
#  This file is part of Siena, a wide-area event notification system.
#  See http://www.inf.usi.ch/carzaniga/siena/
#
#  Author: Antonio Carzaniga (firstname.lastname@usi.ch)
#  See the file AUTHORS for full details. 
#
#  Copyright (C) 2006 University of Colorado
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
test_description Testing local pub/sub operations...
#
cleanup_files='test.out test.expected'
#
# -------------------- TEST BEGINS HERE --------------------
#
$JAVA sienatest.TestLocalPubSub 7 2 8 > test.out
cat > test.expected <<EOF
2:{ extra="whatever" x=2}
7:{ extra="whatever" x=7}
8:{ extra="whatever" x=8}
1:{ extra="whatever" x=1}
6:{ extra="whatever" x=6}
7:{ extra="whatever" x=7}
0:{ extra="whatever" x=0}
5:{ extra="whatever" x=5}
6:{ extra="whatever" x=6}
4:{ extra="whatever" x=4}
5:{ extra="whatever" x=5}
3:{ extra="whatever" x=3}
4:{ extra="whatever" x=4}
2:{ extra="whatever" x=2}
3:{ extra="whatever" x=3}
1:{ extra="whatever" x=1}
2:{ extra="whatever" x=2}
0:{ extra="whatever" x=0}
1:{ extra="whatever" x=1}
0:{ extra="whatever" x=0}
EOF
if diff test.out test.expected; then
    test_passed
else
    test_failed 'see test.out and test.expected for details'
fi
