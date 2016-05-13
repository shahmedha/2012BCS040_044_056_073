#!/bin/sh
#
#  This file is part of Siena, a wide-area event notification system.
#  See http://www.cs.colorado.edu/serl/dot/siena.html
#
#  Author: Antonio Carzaniga <carzanig@cs.colorado.edu>
#  See the file AUTHORS for full details. 
#
#  Copyright (C) 1998-2000 University of Colorado
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
# $Id: test_tokenizer.sh,v 1.1 2003/07/09 20:16:11 carzanig Exp $
#
echo Testing SENPTokenizer...
./TestTokenizer <<EOF > test.out 2>&1
,./?';:[]\\|\`~@#\$%^&()

	,	,
{} = != < > <= >= * *< >*
any true false a _a uno.due tre/quattro.cinque
_a1.2.3 c/2/3/4.5_ x2.2
uno$ due$tre _$/$.$
"A" "Antonio" "Ciao Mamma" "true" "false" "any"
",./?';:[]\\|\`~@#\$%^&()"
"\"" "\n" "\\\\" "\\ta\\tb\\tc"
0 1 321 -0 -1 -123
2147418112 -2147418113
2147418113 -2147418114
12345678 -12345678
98765432 -98765432
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
str: true
str: false
str: any
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
int: 2147418112
int: -2147418113
int: 2147418113
int: -2147418114
int: 12345678
int: -12345678
int: 98765432
int: -98765432
double: 0
double: 0.234
double: -0.432
double: -0
double: 7654.88
double: -3333.44
double: 0
double: 234
double: -432000
double: -0
double: 765488
double: -3.33344e+15
double: 0
double: 0.234
double: -0.0432
double: -0
double: 7.65488e-09
double: -3.33344e-10
EOF
if diff test.out test.expected; then
    rm -f test.out test.expected
    echo PASSED.
    exit 0
else
    echo FAILED
    exit 1
fi
