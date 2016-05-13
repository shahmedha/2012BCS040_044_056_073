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
cleanup_files='test.in test.encoded test.decoded'
#
test_description "Testing SENP encoding and decoding..."
#
# this creates a binary file.  Sure, I could do something more
# "scientific" here...
#
gzip > test.in <<EOF
Most people who bother with the matter at all would admit that the
English language is in a bad way, but it is generally assumed that we
cannot by conscious action do anything about it. Our civilization is
decadent and our language -- so the argument runs -- must inevitably
share in the general collapse. It follows that any struggle against
the abuse of language is a sentimental archaism, like preferring
candles to electric light or hansom cabs to aeroplanes. Underneath
this lies the half-conscious belief that language is a natural growth
and not an instrument which we shape for our own purposes.

Now, it is clear that the decline of a language must ultimately have
political and economic causes: it is not due simply to the bad
influence of this or that individual writer. But an effect can become
a cause, reinforcing the original cause and producing the same effect
in an intensified form, and so on indefinitely. A man may take to
drink because he feels himself to be a failure, and then fail all the
more completely because he drinks. It is rather the same thing that is
happening to the English language. It becomes ugly and inaccurate
because our thoughts are foolish, but the slovenliness of our language
makes it easier for us to have foolish thoughts. The point is that the
process is reversible. Modern English, especially written English, is
full of bad habits which spread by imitation and which can be avoided
if one is willing to take the necessary trouble. If one gets rid of
these habits one can think more clearly, and to think clearly is a
necessary first step toward political regeneration: so that the fight
against bad English is not frivolous and is not the exclusive concern
of professional writers. I will come back to this presently, and I
hope that by that time the meaning of what I have said here will have
become clearer. Meanwhile, here are five specimens of the English
language as it is now habitually written.
EOF
$JAVA siena.TestEncode < test.in > test.encoded 2> /dev/null
$JAVA siena.TestDecode < test.encoded > test.decoded 2> /dev/null
if diff test.decoded test.in > /dev/null; then
    test_passed
else
    test_failed 'see test.in, test.decoded, and test.encoded for details'
fi
