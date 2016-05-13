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
test_description "Testing filters..."
#
cleanup_files='test.out'
#
# --------------- TEST BEGINS HERE -----------------
#
if $JAVA siena.TestFilter > test.out 2>&1 <<EOF
yes
senp{}filter{x any}
senp{}event{x=null}
no
senp{}filter{x="null"}
senp{}event{x=null}
yes
senp{}filter{x != "null"}
senp{}event{x=null}
no
senp{}filter{x < 10}
senp{}event{x=null}
yes
senp{}filter{x=null}
senp{}event{x=null}
no
senp{}filter{x != null}
senp{}event{x=null}
yes
senp{}filter{x any}
senp{}event{x=10}
yes
senp{}filter{x any}
senp{}event{x="abc"}
yes
senp{}filter{x any}
senp{}event{x = false}
yes
senp{}filter{x any}
senp{}event{x = true}
yes
senp{}filter{x any}
senp{}event{x = -10}
yes
senp{}filter{x any}
senp{}event{x = 20}
yes
senp{}filter{x any}
senp{}event{x = -1.234}
yes
senp{}filter{x any}
senp{}event{x = 5.678}
yes
senp{}filter{x any}
senp{}event{x = "xyz246"}
yes
senp{}filter{x any}
senp{}event{x = ""}
yes
senp{}filter{x = ""}
senp{}event{x = ""}
no
senp{}filter{x = ""}
senp{}event{x = "1"}
no
senp{}filter{x = "1"}
senp{}event{x = ""}
yes
senp{}filter{x=10}
senp{}event{x=10}
yes
senp{}filter{x > 2}
senp{}event{x=10}
yes
senp{}filter{x != 2}
senp{}event{x=10}
yes
senp{}filter{x < 20}
senp{}event{x=10}
yes
senp{}filter{x <= 20}
senp{}event{x=10}
yes
senp{}filter{x >= 2}
senp{}event{x=10}
no
senp{}filter{x < 10}
senp{}event{x=10}
no
senp{}filter{x > 10}
senp{}event{x=10}
no
senp{}filter{x != 10}
senp{}event{x=10}
no
senp{}filter{x >= 11}
senp{}event{x=10}
no
senp{}filter{x <= 9}
senp{}event{x=10}
no
senp{}filter{x ="10"}
senp{}event{x=10}
no
senp{}filter{x = false}
senp{}event{x=10}
no
senp{}filter{x = true}
senp{}event{x=10}
yes
senp{}filter{x = true}
senp{}event{x=true}
yes
senp{}filter{x >* "ab"}
senp{}event{x="abc"}
yes
senp{}filter{x *< "bc"}
senp{}event{x="abc"}
yes
senp{}filter{x != "xyz"}
senp{}event{x="abc"}
yes
senp{}filter{x < "azz"}
senp{}event{x="abc"}
yes
senp{}filter{x <= "abz"}
senp{}event{x="abc"}
yes
senp{}filter{x > "aba"}
senp{}event{x="abc"}
yes
senp{}filter{x >= "aba"}
senp{}event{x="abc"}
yes
senp{}filter{x * "b"}
senp{}event{x="abc"}
yes
senp{}filter{x="abc"}
senp{}event{x="abc"}
no
senp{}filter{x < "aac"}
senp{}event{x="abc"}
no
senp{}filter{x > "acc"}
senp{}event{x="abc"}
no
senp{}filter{x * "ba"}
senp{}event{x="abc"}
no
senp{}filter{x >* "ac"}
senp{}event{x="abc"}
no
senp{}filter{x *< "cc"}
senp{}event{x="abc"}
no
senp{}filter{x != "abc"}
senp{}event{x="abc"}
no
senp{}filter{x >= "abd"}
senp{}event{x="abc"}
no
senp{}filter{x <= "abb"}
senp{}event{x="abc"}
yes
senp{}filter{x != 10}
senp{}event{x = 0}
no
senp{}filter{x != 10}
senp{}event{x = 10}
yes
senp{}filter{x != 10.5}
senp{}event{x = 0}
yes
senp{}filter{x != 10.5}
senp{}event{x = 10}
no
senp{}filter{x != 10.5}
senp{}event{x = 10.5}
yes
senp{}filter{x != "xyz"}
senp{}event{x = "xyz246"}
no
senp{}filter{x != "xyz"}
senp{}event{x = "xyz"}
yes
senp{}filter{x != true}
senp{}event{x = false}
no
senp{}filter{x != true}
senp{}event{x = true}
yes
senp{}filter{x > "xyz"}
senp{}event{x = "xza"}
no
senp{}filter{x > "xyz"}
senp{}event{x = "xaz"}
yes
senp{}filter{x > 10}
senp{}event{x = 20}
no
senp{}filter{x > 10}
senp{}event{x = 5}
yes
senp{}filter{name="X"}
senp{}event{x=5 name="X"}
no
senp{}filter{name="X"}
senp{}event{x=5 name="Y"}
yes
senp{}filter{x>10 x<20}
senp{}event{x=15}
no
senp{}filter{x>10 x<20}
senp{}event{x=5}
yes
senp{}filter{name * "a" name * "o" name * "i"}
senp{}event{name="antonio"}
no
senp{}filter{name * "a" name * "x" name * "i"}
senp{}event{name="antonio"}
yes
senp{}filter{x=10 name * "a" name * "o" name * "i"}
senp{}event{name="antonio" x=10}
no
senp{}filter{name * "a" name * "x" name * "i"}
senp{}event{name="antonio" x=20}
yes
senp{}filter{x>10 x<30 name * "a" name * "o" name * "i"}
senp{}event{name="antonio" x=20}
no
senp{}filter{x>10 x<30 name * "a" name * "o" name * "i"}
senp{}event{name="antonio" x=30}
yes
senp{}filter{ a ="A" x >0 y >0 z >0}
senp{}event{a="A" ref=11 x=2 y=3 z=4}
yes
senp{}filter{ observation_timestamp > 0}
senp{}event{ observation_timestamp=995649482594}
EOF
then
    test_passed 
else
    test_failed test.out
fi
