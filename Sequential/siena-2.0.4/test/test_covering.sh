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
test_description "Testing covering relations..."
#
# to be completed with test cases involving multiple attributes... 
# ...work in progress...
#
# --------------- TEST BEGINS HERE -----------------
#
if $JAVA siena.TestCovering <<EOF
yes
senp{}filter{x != null}
senp{}filter{x=10}
yes
senp{}filter{x any}
senp{}filter{x = null}
yes
senp{}filter{x != null}
senp{}filter{x > 10}
yes
senp{}filter{x != null}
senp{}filter{x < 10}
no
senp{}filter{x != null}
senp{}filter{x=null}
yes
senp{}filter{x any}
senp{}filter{x=10}
yes
senp{}filter{x=10}
senp{}filter{x=10}
yes
senp{}filter{x > 2}
senp{}filter{x=10}
yes
senp{}filter{x != 2}
senp{}filter{x=10}
yes
senp{}filter{x < 20}
senp{}filter{x=10}
yes
senp{}filter{x <= 20}
senp{}filter{x=10}
yes
senp{}filter{x >= 2}
senp{}filter{x=10}
no
senp{}filter{x < 10}
senp{}filter{x=10}
no
senp{}filter{x > 10}
senp{}filter{x=10}
no
senp{}filter{x != 10}
senp{}filter{x=10}
no
senp{}filter{x >= 11}
senp{}filter{x=10}
no
senp{}filter{x <= 9}
senp{}filter{x=10}
no
senp{}filter{x ="10"}
senp{}filter{x=10}
no
senp{}filter{x = false}
senp{}filter{x=10}
no
senp{}filter{x = true}
senp{}filter{x=10}
yes
senp{}filter{x any}
senp{}filter{x="abc"}
yes
senp{}filter{x >* "ab"}
senp{}filter{x="abc"}
yes
senp{}filter{x *< "bc"}
senp{}filter{x="abc"}
yes
senp{}filter{x != "xyz"}
senp{}filter{x="abc"}
yes
senp{}filter{x < "azz"}
senp{}filter{x="abc"}
yes
senp{}filter{x <= "abz"}
senp{}filter{x="abc"}
yes
senp{}filter{x > "aba"}
senp{}filter{x="abc"}
yes
senp{}filter{x >= "aba"}
senp{}filter{x="abc"}
yes
senp{}filter{x * "b"}
senp{}filter{x="abc"}
yes
senp{}filter{x="abc"}
senp{}filter{x="abc"}
no
senp{}filter{x < "aac"}
senp{}filter{x="abc"}
no
senp{}filter{x > "acc"}
senp{}filter{x="abc"}
no
senp{}filter{x * "ba"}
senp{}filter{x="abc"}
no
senp{}filter{x >* "ac"}
senp{}filter{x="abc"}
no
senp{}filter{x *< "cc"}
senp{}filter{x="abc"}
no
senp{}filter{x != "abc"}
senp{}filter{x="abc"}
no
senp{}filter{x >= "abd"}
senp{}filter{x="abc"}
no
senp{}filter{x <= "abb"}
senp{}filter{x="abc"}
yes
senp{}filter{x any}
senp{}filter{x = false}
yes
senp{}filter{x any}
senp{}filter{x = true}
yes
senp{}filter{x any}
senp{}filter{x > false}
yes
senp{}filter{x any}
senp{}filter{x > true}
yes
senp{}filter{x any}
senp{}filter{x < false}
yes
senp{}filter{x any}
senp{}filter{x < true}
yes
senp{}filter{x any}
senp{}filter{x <= false}
yes
senp{}filter{x any}
senp{}filter{x <= true}
yes
senp{}filter{x any}
senp{}filter{x >= true}
yes
senp{}filter{x any}
senp{}filter{x >= false}
yes
senp{}filter{x any}
senp{}filter{x != false}
yes
senp{}filter{x any}
senp{}filter{x != true}
yes
senp{}filter{x any}
senp{}filter{x = -10}
yes
senp{}filter{x any}
senp{}filter{x = 20}
yes
senp{}filter{x any}
senp{}filter{x > -10}
yes
senp{}filter{x any}
senp{}filter{x > 20}
yes
senp{}filter{x any}
senp{}filter{x < -10}
yes
senp{}filter{x any}
senp{}filter{x < 20}
yes
senp{}filter{x any}
senp{}filter{x <= 20}
yes
senp{}filter{x any}
senp{}filter{x <= -10}
yes
senp{}filter{x any}
senp{}filter{x >= 20}
yes
senp{}filter{x any}
senp{}filter{x >= -10}
yes
senp{}filter{x any}
senp{}filter{x != -10}
yes
senp{}filter{x any}
senp{}filter{x != 20}
yes
senp{}filter{x any}
senp{}filter{x = -1.234}
yes
senp{}filter{x any}
senp{}filter{x = 5.678}
yes
senp{}filter{x any}
senp{}filter{x > -1.234}
yes
senp{}filter{x any}
senp{}filter{x > 5.678}
yes
senp{}filter{x any}
senp{}filter{x < -1.234}
yes
senp{}filter{x any}
senp{}filter{x < 5.678}
yes
senp{}filter{x any}
senp{}filter{x <= 5.678}
yes
senp{}filter{x any}
senp{}filter{x <= -1.234}
yes
senp{}filter{x any}
senp{}filter{x >= 5.678}
yes
senp{}filter{x any}
senp{}filter{x >= -1.234}
yes
senp{}filter{x any}
senp{}filter{x != -1.234}
yes
senp{}filter{x any}
senp{}filter{x != 5.678}
yes
senp{}filter{x any}
senp{}filter{x = "xyz246"}
yes
senp{}filter{x any}
senp{}filter{x = ""}
yes
senp{}filter{x any}
senp{}filter{x > "xyz246"}
yes
senp{}filter{x any}
senp{}filter{x > ""}
yes
senp{}filter{x any}
senp{}filter{x < "xyz246"}
yes
senp{}filter{x any}
senp{}filter{x < ""}
yes
senp{}filter{x any}
senp{}filter{x <= ""}
yes
senp{}filter{x any}
senp{}filter{x <= "xyz246"}
yes
senp{}filter{x any}
senp{}filter{x >= ""}
yes
senp{}filter{x any}
senp{}filter{x >= "xyz246"}
yes
senp{}filter{x any}
senp{}filter{x != "xyz246"}
yes
senp{}filter{x any}
senp{}filter{x != ""}
yes
senp{}filter{x any}
senp{}filter{x *< ""}
yes
senp{}filter{x any}
senp{}filter{x *< "xyz246"}
yes
senp{}filter{x any}
senp{}filter{x >* ""}
yes
senp{}filter{x any}
senp{}filter{x >* "xyz246"}
yes
senp{}filter{x any}
senp{}filter{x * "xyz246"}
yes
senp{}filter{x any}
senp{}filter{x * ""}
yes
senp{}filter{x != 10}
senp{}filter{x = 0}
yes
senp{}filter{x != 10}
senp{}filter{x != 10}
yes
senp{}filter{x != 10}
senp{}filter{x < 0}
yes
senp{}filter{x != 10}
senp{}filter{x < 10}
yes
senp{}filter{x != 10}
senp{}filter{x <= 9}
yes
senp{}filter{x != 10}
senp{}filter{x > 120}
yes
senp{}filter{x != 10}
senp{}filter{x > 10}
yes
senp{}filter{x != 10}
senp{}filter{x >= 11}
no
senp{}filter{x != 10}
senp{}filter{x any}
no
senp{}filter{x != 10}
senp{}filter{x = 10}
no
senp{}filter{x != 10}
senp{}filter{x != 30}
no
senp{}filter{x != 10}
senp{}filter{x > 0}
no
senp{}filter{x != 10}
senp{}filter{x < 20}
no
senp{}filter{x != 10}
senp{}filter{x <= 10}
no
senp{}filter{x != 10}
senp{}filter{x > 1}
no
senp{}filter{x != 10}
senp{}filter{x >= 10}
yes
senp{}filter{x != 10.5}
senp{}filter{x = 0}
yes
senp{}filter{x != 10.5}
senp{}filter{x = 10}
yes
senp{}filter{x != 10.5}
senp{}filter{x != 10.5}
yes
senp{}filter{x != 10.5}
senp{}filter{x < 0}
yes
senp{}filter{x != 10.5}
senp{}filter{x < 10}
yes
senp{}filter{x != 10.5}
senp{}filter{x <= 10}
yes
senp{}filter{x != 10.5}
senp{}filter{x > 120}
yes
senp{}filter{x != 10.5}
senp{}filter{x > 11}
yes
senp{}filter{x != 10.5}
senp{}filter{x >= 11}
no
senp{}filter{x != 10.5}
senp{}filter{x any}
no
senp{}filter{x != 10.5}
senp{}filter{x = 10.5}
no
senp{}filter{x != 10.5}
senp{}filter{x != 30}
no
senp{}filter{x != 10.5}
senp{}filter{x > 0}
no
senp{}filter{x != 10.5}
senp{}filter{x < 20}
no
senp{}filter{x != 10.5}
senp{}filter{x <= 11}
no
senp{}filter{x != 10.5}
senp{}filter{x > 1}
no
senp{}filter{x != 10.5}
senp{}filter{x >= 10}
yes
senp{}filter{x != "xyz"}
senp{}filter{x = "xyz246"}
yes
senp{}filter{x != "xyz"}
senp{}filter{x > "xyz246"}
yes
senp{}filter{x != "xyz"}
senp{}filter{x < "xxyyzz"}
yes
senp{}filter{x != "xyz"}
senp{}filter{x <= "xyy"}
yes
senp{}filter{x != "xyz"}
senp{}filter{x >= "xyza"}
yes
senp{}filter{x != "xyz"}
senp{}filter{x != "xyz"}
yes
senp{}filter{x != "xyz"}
senp{}filter{x *< "xyZ"}
yes
senp{}filter{x != "xyz"}
senp{}filter{x >* "xyz246"}
yes
senp{}filter{x != "xyz"}
senp{}filter{x * "xyh"}
no
senp{}filter{x != "xyz"}
senp{}filter{x any}
no
senp{}filter{x != "xyz"}
senp{}filter{x = "xyz"}
no
senp{}filter{x != "xyz"}
senp{}filter{x > "xyac"}
no
senp{}filter{x != "xyz"}
senp{}filter{x < "zxy"}
no
senp{}filter{x != "xyz"}
senp{}filter{x <= "xyz"}
no
senp{}filter{x != "xyz"}
senp{}filter{x >= "xyz"}
no
senp{}filter{x != "xyz"}
senp{}filter{x != "abc"}
no
senp{}filter{x != "xyz"}
senp{}filter{x *< "yz"}
no
senp{}filter{x != "xyz"}
senp{}filter{x >* "xy"}
no
senp{}filter{x != "xyz"}
senp{}filter{x * "x"}
yes
senp{}filter{x != true}
senp{}filter{x = false}
yes
senp{}filter{x != true}
senp{}filter{x != true}
yes
senp{}filter{x != true}
senp{}filter{x < true}
yes
senp{}filter{x != true}
senp{}filter{x <= false}
no
senp{}filter{x != true}
senp{}filter{x >= false}
no
senp{}filter{x != true}
senp{}filter{x any}
no
senp{}filter{x != true}
senp{}filter{x = true}
no
senp{}filter{x != true}
senp{}filter{x != false}
no
senp{}filter{x != true}
senp{}filter{x > false}
no
senp{}filter{x != true}
senp{}filter{x <= true}
no
senp{}filter{x != true}
senp{}filter{x >= true}
yes
senp{}filter{x > "xyz"}
senp{}filter{x = "xza"}
yes
senp{}filter{x > "xyz"}
senp{}filter{x > "zzxyz246"}
yes
senp{}filter{x > "xyz"}
senp{}filter{x >= "xyza"}
yes
senp{}filter{x > "xyz"}
senp{}filter{x > "xzzzz"}
yes
senp{}filter{x > "xyz"}
senp{}filter{x >* "za"}
no
senp{}filter{x > "xyz"}
senp{}filter{x any}
no
senp{}filter{x > "xyz"}
senp{}filter{x = "xaz"}
no
senp{}filter{x > "xyz"}
senp{}filter{x > "xyac"}
no
senp{}filter{x > "xyz"}
senp{}filter{x < "zzz"}
no
senp{}filter{x > "xyz"}
senp{}filter{x <= "zzz"}
no
senp{}filter{x > "xyz"}
senp{}filter{x >= "xyz"}
no
senp{}filter{x > "xyz"}
senp{}filter{x *< "x"}
no
senp{}filter{x > "xyz"}
senp{}filter{x >* "x"}
no
senp{}filter{x > "xyz"}
senp{}filter{x >* "xyz"}
no
senp{}filter{x > "xyz"}
senp{}filter{x * "y"}
no
senp{}filter{x > "xyz"}
senp{}filter{x * "a"}
yes
senp{}filter{x > 10}
senp{}filter{x = 20}
yes
senp{}filter{x > 10}
senp{}filter{x > 15}
yes
senp{}filter{x > 10}
senp{}filter{x >= 11}
no
senp{}filter{x > 10}
senp{}filter{x any}
no
senp{}filter{x > 10}
senp{}filter{x = 5}
no
senp{}filter{x > 10}
senp{}filter{x > 5}
no
senp{}filter{x > 10}
senp{}filter{x < 20}
no
senp{}filter{x > 10}
senp{}filter{x <= 10}
no
senp{}filter{x > 10}
senp{}filter{x >= 0}
no
senp{}filter{x > 10}
senp{}filter{x != 10}
yes
senp{}filter{Action ="Login" From ="antonio" Scope ="com.eventdesktop.eventlet.messenger" Type ="Log"}
senp{}filter{Action ="Login" From ="antonio" Scope ="com.eventdesktop.eventlet.messenger" Type ="Log"}
yes
senp{}filter{x >= 1 x < 5}
senp{}filter{x = 1}
yes
senp{}filter{age >= 0 name ="Antonio"}
senp{}filter{age > 21 name ="Antonio"}
EOF
then
    test_passed
else 
    test_failed
fi
