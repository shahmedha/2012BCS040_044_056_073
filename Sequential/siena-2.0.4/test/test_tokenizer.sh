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
test_description "Testing tokenizer..."
#
cleanup_files='test.out test.expected'
#
$JAVA siena.TestTokenizer <<EOF > test.out 2>&1
,./?';:[]\\|\`~@#\$%^&()

	,	,
{} = != < > <= >= * *< >*
any true false a _a uno.due tre/quattro.cinque
_a1.2.3 c/2/3/4.5_ x2.2
uno$ due$tre _$/$.$
"A" "Antonio" "Ciao Mamma" 
",./?';:[]\\|\`~@#\$%^&()"
"\"" "\n" "\\\\" "\\ta\\tb\\tc"
0 1 321 -0 -1 -123
9223372036854775807 -9223372036854775808
9223372036854775806 -9223372036854775807
12345678901 -12345678901
98765432109 -98765432109
0.0 0.234 -0.432 -0.0 7654.8765 -3333.444
0.0e2 0.234e3 -0.432e6 -0.0e7 7654.8765e2 -3333.444e12
0.0e-2 234e-3 -0.432e-1 -0.0e-7 7654.8765e-12 -3333.444e-13
EOF
cat > test.expected <<EOF
{
}
op: =
op: !=
op: <
op: >
op: <=
op: >=
op: *
op: *<
op: >*
id: any
id: true
id: false
id: a
id: _a
id: uno.due
id: tre/quattro.cinque
id: _a1.2.3
id: c/2/3/4.5_
id: x2.2
id: uno$
id: due$tre
id: _$/$.$
str: A
str: Antonio
str: Ciao Mamma
str: ,./?';:[]|\`~@#$%^&()
str: "
str: 

str: \\
str: 	a	b	c
int: 0
int: 1
int: 321
int: 0
int: -1
int: -123
int: 9223372036854775807
int: -9223372036854775808
int: 9223372036854775806
int: -9223372036854775807
int: 12345678901
int: -12345678901
int: 98765432109
int: -98765432109
double: 0.0
double: 0.234
double: -0.432
double: -0.0
double: 7654.8765
double: -3333.444
double: 0.0
double: 234.0
double: -432000.0
double: -0.0
double: 765487.65
double: -3.333444E15
double: 0.0
double: 0.234
double: -0.0432
double: -0.0
double: 7.6548765E-9
double: -3.333444E-10
EOF
if diff test.out test.expected  ; then
    test_passed
else
    test_failed 'see test.out and test.expected for details.'
fi
