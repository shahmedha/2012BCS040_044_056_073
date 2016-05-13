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
test_description Testing NotificationSequencer...
#
cleanup_files='test.out test.expected'
#
# -------------------- TEST BEGINS HERE --------------------
#
rm -f test.out
#
# no shuffling: ordered sequence; 1 thread
#
$JAVA sienatest.TestNotificationSequencer 1 0 1 a b c d e f g h i j k l m n o >> test.out; echo >> test.out
$JAVA sienatest.TestNotificationSequencer 1 0 1 This program is free software\; you can redistribute it and/or modify it under the terms of the GNU General Public License  as published by the Free Software Foundation\; either version 2 of the License, or \(at your option\) any later version >> test.out; echo >> test.out
#
# no shuffling: ordered sequence; 5 thread
#
$JAVA sienatest.TestNotificationSequencer 1 0 5 a b c d e f g h i j k l m n o >> test.out; echo >> test.out
$JAVA sienatest.TestNotificationSequencer 1 0 5 This program is free software\; you can redistribute it and/or modify it under the terms of the GNU General Public License  as published by the Free Software Foundation\; either version 2 of the License, or \(at your option\) any later version >> test.out; echo >> test.out
#
# out of sequence 3 out of 8 times, displacement between 1 and 3; 5 threads
#
$JAVA sienatest.TestNotificationSequencer 8 4 5 a b c d e f g h i j k l m n o >> test.out; echo >> test.out
$JAVA sienatest.TestNotificationSequencer 8 4 5 This program is free software\; you can redistribute it and/or modify it under the terms of the GNU General Public License  as published by the Free Software Foundation\; either version 2 of the License, or \(at your option\) any later version >> test.out; echo >> test.out
#
cat > test.expected <<EOF
 a b c d e f g h i j k l m n o
 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version
 a b c d e f g h i j k l m n o
 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version
 a b c d e f g h i j k l m n o
 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version
EOF
if diff test.out test.expected; then
    test_passed
else
    test_failed 'see test.out and test.expected for details'
fi
