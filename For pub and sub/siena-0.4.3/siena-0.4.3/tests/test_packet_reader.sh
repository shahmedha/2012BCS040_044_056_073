#!/bin/sh
#
#  This file is part of Siena, a wide-area event notification system.
#  See http://www.cs.colorado.edu/serl/siena/
#
#  Author: Antonio Carzaniga <carzanig@cs.colorado.edu>
#  See the file AUTHORS for full details. 
#
#  Copyright (C) 1998-2001 University of Colorado
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
# $Id: test_packet_reader.sh,v 1.1 2003/07/09 20:16:11 carzanig Exp $
#
echo Testing SENP.decode_packet...
#
# --------------- TEST BEGINS HERE -----------------
#
./TestPacketReader > test.out <<EOF
senp{}
senp{} filter{}
senp{} event{}
senp{method="NOP"}
senp{method="PUB"}
senp{method="SUB"}
senp{method="UNS"}
senp{method="ADV"}
senp{method="UNA"}
senp{method="HLO"}
senp{method="BYE"}
senp{method="SUS"}
senp{method="RES"}
senp{method="MAP"}
senp{method="WHO"}
senp{method="INF"}
senp{method="BLA"}
senp{ttl=0}
senp{ttl=1}
senp{ttl=21}
senp{ttl=31}
senp{ttl=-1}
senp{a="a" b="x" c="ciao"}
senp{filter="a" senp="x" event="ciao"}
senp{version=1 method="NOP" ttl=30}
senp{version=1 method="SUB" ttl=30 id="ciao" handler="pippo"} filter{ x=10}
senp{version=1 method="UNS" ttl=30 id="ciao"} filter{ x any}
senp{version=1 method="PUB" id="pippotto" ttl=30} event{ x="100"}
senp{version=1 method="ADV" id="qwqw" ttl=30} filter{ x!="null"}
senp{version=1 method="UNA" id="xxx" ttl=30} filter{ x>10}
senp{version=1 method="BYE" id="mamma" ttl=30}
senp{version=1 method="PUB" id="a" ttl=30} event{version=1 method="SUB" ttl=30}
senp{version=1 method="PUB" id="a" ttl=30} events{ event{a=1} event{a=2}}
senp{version=1 method="SUB" id="a"} filter{version=1 method="SUB" ttl=30}
senp{method="SUB" id="a"} pattern{filter{a=10}filter{a<10}}
senp{version=1 method="SUB" ttl=30} filter{ x<10 x>0}
senp{version=1 method="SUB" ttl=30} filter{ x<10}
senp{version=1 method="SUB" ttl=30} filter{ x!="null"}
senp{version=1 method="SUB" ttl=30} filter{ x="null"}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x=10}
senp{version=1 method="SUB" ttl=30} filter{ x=10}
senp{version=1 method="SUB" ttl=30} filter{ x=10}
senp{version=1 method="SUB" ttl=30} filter{ x>2}
senp{version=1 method="SUB" ttl=30} filter{ x=10}
senp{version=1 method="SUB" ttl=30} filter{ x!=2 x<100 y>12 y<14 y!=13}
senp{version=1 method="SUB" ttl=30} filter{ x=10 y=23}
senp{version=1 method="SUB" ttl=30} filter{ x<20}
senp{version=1 method="SUB" ttl=30} filter{ x=10}
senp{version=1 method="SUB" ttl=30} filter{ x<=20}
senp{version=1 method="SUB" ttl=30} filter{ x=10}
senp{version=1 method="SUB" ttl=30} filter{ x>=2}
senp{version=1 method="SUB" ttl=30} filter{ x=10}
senp{version=1 method="SUB" ttl=30} filter{ x<10}
senp{version=1 method="SUB" ttl=30} filter{ x=10}
senp{version=1 method="SUB" ttl=30} filter{ x>10}
senp{version=1 method="SUB" ttl=30} filter{ x=10}
senp{version=1 method="SUB" ttl=30} filter{ x!=10}
senp{version=1 method="SUB" ttl=30} filter{ x=10}
senp{version=1 method="SUB" ttl=30} filter{ x>=11}
senp{version=1 method="SUB" ttl=30} filter{ x=10}
senp{version=1 method="SUB" ttl=30} filter{ x<=9}
senp{version=1 method="SUB" ttl=30} filter{ x=10}
senp{version=1 method="SUB" ttl=30} filter{ x="10"}
senp{version=1 method="SUB" ttl=30} filter{ x=10}
senp{version=1 method="SUB" ttl=30} filter{ x=false}
senp{version=1 method="SUB" ttl=30} filter{ x=10}
senp{version=1 method="SUB" ttl=30} filter{ x=true}
senp{version=1 method="SUB" ttl=30} filter{ x=10}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x>*"ab"}
senp{version=1 method="SUB" ttl=30} filter{ x="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x*<"bc"}
senp{version=1 method="SUB" ttl=30} filter{ x="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x<"azz"}
senp{version=1 method="SUB" ttl=30} filter{ x="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x<="abz"}
senp{version=1 method="SUB" ttl=30} filter{ x="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x>"aba"}
senp{version=1 method="SUB" ttl=30} filter{ x="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x>="aba"}
senp{version=1 method="SUB" ttl=30} filter{ x="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x*"b"}
senp{version=1 method="SUB" ttl=30} filter{ x="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x<"aac"}
senp{version=1 method="SUB" ttl=30} filter{ x="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x>"acc"}
senp{version=1 method="SUB" ttl=30} filter{ x="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x*"ba"}
senp{version=1 method="SUB" ttl=30} filter{ x="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x>*"ac"}
senp{version=1 method="SUB" ttl=30} filter{ x="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x*<"cc"}
senp{version=1 method="SUB" ttl=30} filter{ x="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x!="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x>="abd"}
senp{version=1 method="SUB" ttl=30} filter{ x="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x<="abb"}
senp{version=1 method="SUB" ttl=30} filter{ x="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x=false}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x=true}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x>false}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x>true}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x<false}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x<true}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x<=false}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x<=true}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x>=true}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x>=false}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x!=false}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x!=true}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x=-10}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x=20}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x>-10}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x>20}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x<-10}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x<20}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x<=20}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x<=-10}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x>=20}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x>=-10}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x!=-10}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x!=20}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ a=1 x=-1.235}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x=5.678}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x>-1.2345}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x>5.678}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x<-1.234}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x<5.678}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x<=5.678}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x<=-1.234}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x>=5.678}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x>=-1.234}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x!=-1.234}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x!=5.678}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x="xyz246"}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x=""}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x>"xyz246"}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x>""}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x<"xyz246"}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x<""}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x<=""}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x<="xyz246"}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x>=""}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x>="xyz246"}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz246"}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x!=""}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x*<""}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x*<"xyz246"}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x>*""}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x>*"xyz246"}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x*"xyz246"}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x*""}
senp{version=1 method="SUB" ttl=30} filter{ x!=10}
senp{version=1 method="SUB" ttl=30} filter{ x=0}
senp{version=1 method="SUB" ttl=30} filter{ x!=10}
senp{version=1 method="SUB" ttl=30} filter{ x!=10}
senp{version=1 method="SUB" ttl=30} filter{ x!=10}
senp{version=1 method="SUB" ttl=30} filter{ x<0}
senp{version=1 method="SUB" ttl=30} filter{ x!=10}
senp{version=1 method="SUB" ttl=30} filter{ x<10}
senp{version=1 method="SUB" ttl=30} filter{ x!=10}
senp{version=1 method="SUB" ttl=30} filter{ x<=9}
senp{version=1 method="SUB" ttl=30} filter{ x!=10}
senp{version=1 method="SUB" ttl=30} filter{ x>120}
senp{version=1 method="SUB" ttl=30} filter{ x!=10}
senp{version=1 method="SUB" ttl=30} filter{ x>10}
senp{version=1 method="SUB" ttl=30} filter{ x!=10}
senp{version=1 method="SUB" ttl=30} filter{ x>=11}
senp{version=1 method="SUB" ttl=30} filter{ x!=10}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x!=10}
senp{version=1 method="SUB" ttl=30} filter{ x=10}
senp{version=1 method="SUB" ttl=30} filter{ x!=10}
senp{version=1 method="SUB" ttl=30} filter{ x!=30}
senp{version=1 method="SUB" ttl=30} filter{ x!=10}
senp{version=1 method="SUB" ttl=30} filter{ x>0}
senp{version=1 method="SUB" ttl=30} filter{ x!=10}
senp{version=1 method="SUB" ttl=30} filter{ x<20}
senp{version=1 method="SUB" ttl=30} filter{ x!=10}
senp{version=1 method="SUB" ttl=30} filter{ x<=10}
senp{version=1 method="SUB" ttl=30} filter{ x!=10}
senp{version=1 method="SUB" ttl=30} filter{ x>1}
senp{version=1 method="SUB" ttl=30} filter{ x!=10}
senp{version=1 method="SUB" ttl=30} filter{ x>=10}
senp{version=1 method="SUB" ttl=30} filter{ x!=10.5}
senp{version=1 method="SUB" ttl=30} filter{ x=0}
senp{version=1 method="SUB" ttl=30} filter{ x!=10.5}
senp{version=1 method="SUB" ttl=30} filter{ x=10}
senp{version=1 method="SUB" ttl=30} filter{ x!=10.5}
senp{version=1 method="SUB" ttl=30} filter{ x!=10.5}
senp{version=1 method="SUB" ttl=30} filter{ x!=10.5}
senp{version=1 method="SUB" ttl=30} filter{ x<0}
senp{version=1 method="SUB" ttl=30} filter{ x!=10.5}
senp{version=1 method="SUB" ttl=30} filter{ x<10}
senp{version=1 method="SUB" ttl=30} filter{ x!=10.5}
senp{version=1 method="SUB" ttl=30} filter{ x<=10}
senp{version=1 method="SUB" ttl=30} filter{ x!=10.5}
senp{version=1 method="SUB" ttl=30} filter{ x>120}
senp{version=1 method="SUB" ttl=30} filter{ x!=10.5}
senp{version=1 method="SUB" ttl=30} filter{ x>11}
senp{version=1 method="SUB" ttl=30} filter{ x!=10.5}
senp{version=1 method="SUB" ttl=30} filter{ x>=11}
senp{version=1 method="SUB" ttl=30} filter{ x!=10.5}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x!=10.5}
senp{version=1 method="SUB" ttl=30} filter{ x=10.5}
senp{version=1 method="SUB" ttl=30} filter{ x!=10.5}
senp{version=1 method="SUB" ttl=30} filter{ x!=30}
senp{version=1 method="SUB" ttl=30} filter{ x!=10.5}
senp{version=1 method="SUB" ttl=30} filter{ x>0}
senp{version=1 method="SUB" ttl=30} filter{ x!=10.5}
senp{version=1 method="SUB" ttl=30} filter{ x<20}
senp{version=1 method="SUB" ttl=30} filter{ x!=10.5}
senp{version=1 method="SUB" ttl=30} filter{ x<=11}
senp{version=1 method="SUB" ttl=30} filter{ x!=10.5}
senp{version=1 method="SUB" ttl=30} filter{ x>1}
senp{version=1 method="SUB" ttl=30} filter{ x!=10.5}
senp{version=1 method="SUB" ttl=30} filter{ x>=10}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x="xyz246"}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x>"xyz246"}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x<"xxyyzz"}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x<="xyy"}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x>="xyza"}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x*<"xyZ"}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x>*"xyz246"}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x*"xyh"}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x>"xyac"}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x<"zxy"}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x<="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x>="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x!="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x*<"yz"}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x>*"xy"}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x*"x"}
senp{version=1 method="SUB" ttl=30} filter{ x!=true}
senp{version=1 method="SUB" ttl=30} filter{ x=false}
senp{version=1 method="SUB" ttl=30} filter{ x!=true}
senp{version=1 method="SUB" ttl=30} filter{ x!=true}
senp{version=1 method="SUB" ttl=30} filter{ x!=true}
senp{version=1 method="SUB" ttl=30} filter{ x<true}
senp{version=1 method="SUB" ttl=30} filter{ x!=true}
senp{version=1 method="SUB" ttl=30} filter{ x<=false}
senp{version=1 method="SUB" ttl=30} filter{ x!=true}
senp{version=1 method="SUB" ttl=30} filter{ x>=false}
senp{version=1 method="SUB" ttl=30} filter{ x!=true}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x!=true}
senp{version=1 method="SUB" ttl=30} filter{ x=true}
senp{version=1 method="SUB" ttl=30} filter{ x!=true}
senp{version=1 method="SUB" ttl=30} filter{ x!=false}
senp{version=1 method="SUB" ttl=30} filter{ x!=true}
senp{version=1 method="SUB" ttl=30} filter{ x>false}
senp{version=1 method="SUB" ttl=30} filter{ x!=true}
senp{version=1 method="SUB" ttl=30} filter{ x<=true}
senp{version=1 method="SUB" ttl=30} filter{ x!=true}
senp{version=1 method="SUB" ttl=30} filter{ x>=true}
senp{version=1 method="SUB" ttl=30} filter{ x>"xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x="xza"}
senp{version=1 method="SUB" ttl=30} filter{ x>"xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x>"zzxyz246"}
senp{version=1 method="SUB" ttl=30} filter{ x>"xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x>="xyza"}
senp{version=1 method="SUB" ttl=30} filter{ x>"xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x>"xzzzz"}
senp{version=1 method="SUB" ttl=30} filter{ x>"xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x>*"za"}
senp{version=1 method="SUB" ttl=30} filter{ x>"xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x>"xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x="xaz"}
senp{version=1 method="SUB" ttl=30} filter{ x>"xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x>"xyac"}
senp{version=1 method="SUB" ttl=30} filter{ x>"xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x<"zzz"}
senp{version=1 method="SUB" ttl=30} filter{ x>"xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x<="zzz"}
senp{version=1 method="SUB" ttl=30} filter{ x>"xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x>="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x>"xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x*<"x"}
senp{version=1 method="SUB" ttl=30} filter{ x>"xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x>*"x"}
senp{version=1 method="SUB" ttl=30} filter{ x>"xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x>*"xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x>"xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x*"y"}
senp{version=1 method="SUB" ttl=30} filter{ x>"xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x*"a"}
senp{version=1 method="SUB" ttl=30} filter{ x>10}
senp{version=1 method="SUB" ttl=30} filter{ x=20}
senp{version=1 method="SUB" ttl=30} filter{ x>10}
senp{version=1 method="SUB" ttl=30} filter{ x>15}
senp{version=1 method="SUB" ttl=30} filter{ x>10}
senp{version=1 method="SUB" ttl=30} filter{ x>=11}
senp{version=1 method="SUB" ttl=30} filter{ x>10}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x>10}
senp{version=1 method="SUB" ttl=30} filter{ x=5}
senp{version=1 method="SUB" ttl=30} filter{ x>10}
senp{version=1 method="SUB" ttl=30} filter{ x>5}
senp{version=1 method="SUB" ttl=30} filter{ x>10}
senp{version=1 method="SUB" ttl=30} filter{ x<20}
senp{version=1 method="SUB" ttl=30} filter{ x>10}
senp{version=1 method="SUB" ttl=30} filter{ x<=10}
senp{version=1 method="SUB" ttl=30} filter{ x>10}
senp{version=1 method="SUB" ttl=30} filter{ x>=0}
senp{version=1 method="SUB" ttl=30} filter{ x>10}
senp{version=1 method="SUB" ttl=30} filter{ x!=10}
senp{version=1 method="SUB" ttl=30} filter{ Action="Login" From="antonio" Scope="com.eventdesktop.eventlet.messenger" Type="Log"}
senp{version=1 method="SUB" ttl=30} filter{ Action="Login" From="antonio" Scope="com.eventdesktop.eventlet.messenger" Type="Log"}
senp{version=1 method="SUB" ttl=30} filter{ x>=1 x<5}
senp{version=1 method="SUB" ttl=30} filter{ x=1}
EOF
cat > test.expected <<EOF
senp{version=1 method="NOP" ttl=30}
senp{version=1 method="NOP" ttl=30} filter{}
senp{version=1 method="NOP" ttl=30} event{}
senp{version=1 method="NOP" ttl=30}
senp{version=1 method="PUB" ttl=30}
senp{version=1 method="SUB" ttl=30}
senp{version=1 method="UNS" ttl=30}
senp{version=1 method="ADV" ttl=30}
senp{version=1 method="UNA" ttl=30}
senp{version=1 method="HLO" ttl=30}
senp{version=1 method="BYE" ttl=30}
senp{version=1 method="SUS" ttl=30}
senp{version=1 method="RES" ttl=30}
senp{version=1 method="MAP" ttl=30}
senp{version=1 method="WHO" ttl=30}
senp{version=1 method="INF" ttl=30}
senp{version=1 method="NOP" ttl=30}
senp{version=1 method="NOP" ttl=0}
senp{version=1 method="NOP" ttl=1}
senp{version=1 method="NOP" ttl=21}
senp{version=1 method="NOP" ttl=30}
senp{version=1 method="NOP" ttl=0}
senp{version=1 method="NOP" ttl=30}
senp{version=1 method="NOP" ttl=30}
senp{version=1 method="NOP" ttl=30}
senp{version=1 method="SUB" ttl=30 id="ciao" handler="pippo"} filter{ x=10}
senp{version=1 method="UNS" ttl=30 id="ciao"} filter{ x any}
senp{version=1 method="PUB" ttl=30 id="pippotto"} event{ x="100"}
senp{version=1 method="ADV" ttl=30 id="qwqw"} filter{ x!="null"}
senp{version=1 method="UNA" ttl=30 id="xxx"} filter{ x>10}
senp{version=1 method="BYE" ttl=30 id="mamma"}
senp{version=1 method="PUB" ttl=30 id="a"} event{ method="SUB" ttl=30 version=1}
senp{version=1 method="PUB" ttl=30 id="a"} events{event{ a=1}event{ a=2}}
senp{version=1 method="SUB" ttl=30 id="a"} filter{ method="SUB" ttl=30 version=1}
senp{version=1 method="SUB" ttl=30 id="a"} pattern{filter{ a=10}filter{ a<10}}
senp{version=1 method="SUB" ttl=30} filter{ x<10 x>0}
senp{version=1 method="SUB" ttl=30} filter{ x<10}
senp{version=1 method="SUB" ttl=30} filter{ x!="null"}
senp{version=1 method="SUB" ttl=30} filter{ x="null"}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x=10}
senp{version=1 method="SUB" ttl=30} filter{ x=10}
senp{version=1 method="SUB" ttl=30} filter{ x=10}
senp{version=1 method="SUB" ttl=30} filter{ x>2}
senp{version=1 method="SUB" ttl=30} filter{ x=10}
senp{version=1 method="SUB" ttl=30} filter{ x!=2 x<100 y>12 y<14 y!=13}
senp{version=1 method="SUB" ttl=30} filter{ x=10 y=23}
senp{version=1 method="SUB" ttl=30} filter{ x<20}
senp{version=1 method="SUB" ttl=30} filter{ x=10}
senp{version=1 method="SUB" ttl=30} filter{ x<=20}
senp{version=1 method="SUB" ttl=30} filter{ x=10}
senp{version=1 method="SUB" ttl=30} filter{ x>=2}
senp{version=1 method="SUB" ttl=30} filter{ x=10}
senp{version=1 method="SUB" ttl=30} filter{ x<10}
senp{version=1 method="SUB" ttl=30} filter{ x=10}
senp{version=1 method="SUB" ttl=30} filter{ x>10}
senp{version=1 method="SUB" ttl=30} filter{ x=10}
senp{version=1 method="SUB" ttl=30} filter{ x!=10}
senp{version=1 method="SUB" ttl=30} filter{ x=10}
senp{version=1 method="SUB" ttl=30} filter{ x>=11}
senp{version=1 method="SUB" ttl=30} filter{ x=10}
senp{version=1 method="SUB" ttl=30} filter{ x<=9}
senp{version=1 method="SUB" ttl=30} filter{ x=10}
senp{version=1 method="SUB" ttl=30} filter{ x="10"}
senp{version=1 method="SUB" ttl=30} filter{ x=10}
senp{version=1 method="SUB" ttl=30} filter{ x=false}
senp{version=1 method="SUB" ttl=30} filter{ x=10}
senp{version=1 method="SUB" ttl=30} filter{ x=true}
senp{version=1 method="SUB" ttl=30} filter{ x=10}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x>*"ab"}
senp{version=1 method="SUB" ttl=30} filter{ x="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x*<"bc"}
senp{version=1 method="SUB" ttl=30} filter{ x="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x<"azz"}
senp{version=1 method="SUB" ttl=30} filter{ x="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x<="abz"}
senp{version=1 method="SUB" ttl=30} filter{ x="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x>"aba"}
senp{version=1 method="SUB" ttl=30} filter{ x="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x>="aba"}
senp{version=1 method="SUB" ttl=30} filter{ x="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x*"b"}
senp{version=1 method="SUB" ttl=30} filter{ x="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x<"aac"}
senp{version=1 method="SUB" ttl=30} filter{ x="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x>"acc"}
senp{version=1 method="SUB" ttl=30} filter{ x="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x*"ba"}
senp{version=1 method="SUB" ttl=30} filter{ x="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x>*"ac"}
senp{version=1 method="SUB" ttl=30} filter{ x="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x*<"cc"}
senp{version=1 method="SUB" ttl=30} filter{ x="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x!="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x>="abd"}
senp{version=1 method="SUB" ttl=30} filter{ x="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x<="abb"}
senp{version=1 method="SUB" ttl=30} filter{ x="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x=false}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x=true}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x>false}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x>true}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x<false}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x<true}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x<=false}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x<=true}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x>=true}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x>=false}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x!=false}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x!=true}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x=-10}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x=20}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x>-10}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x>20}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x<-10}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x<20}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x<=20}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x<=-10}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x>=20}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x>=-10}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x!=-10}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x!=20}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ a=1 x=-1.235}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x=5.678}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x>-1.2345}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x>5.678}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x<-1.234}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x<5.678}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x<=5.678}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x<=-1.234}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x>=5.678}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x>=-1.234}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x!=-1.234}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x!=5.678}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x="xyz246"}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x=""}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x>"xyz246"}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x>""}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x<"xyz246"}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x<""}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x<=""}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x<="xyz246"}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x>=""}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x>="xyz246"}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz246"}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x!=""}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x*<""}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x*<"xyz246"}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x>*""}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x>*"xyz246"}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x*"xyz246"}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x*""}
senp{version=1 method="SUB" ttl=30} filter{ x!=10}
senp{version=1 method="SUB" ttl=30} filter{ x=0}
senp{version=1 method="SUB" ttl=30} filter{ x!=10}
senp{version=1 method="SUB" ttl=30} filter{ x!=10}
senp{version=1 method="SUB" ttl=30} filter{ x!=10}
senp{version=1 method="SUB" ttl=30} filter{ x<0}
senp{version=1 method="SUB" ttl=30} filter{ x!=10}
senp{version=1 method="SUB" ttl=30} filter{ x<10}
senp{version=1 method="SUB" ttl=30} filter{ x!=10}
senp{version=1 method="SUB" ttl=30} filter{ x<=9}
senp{version=1 method="SUB" ttl=30} filter{ x!=10}
senp{version=1 method="SUB" ttl=30} filter{ x>120}
senp{version=1 method="SUB" ttl=30} filter{ x!=10}
senp{version=1 method="SUB" ttl=30} filter{ x>10}
senp{version=1 method="SUB" ttl=30} filter{ x!=10}
senp{version=1 method="SUB" ttl=30} filter{ x>=11}
senp{version=1 method="SUB" ttl=30} filter{ x!=10}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x!=10}
senp{version=1 method="SUB" ttl=30} filter{ x=10}
senp{version=1 method="SUB" ttl=30} filter{ x!=10}
senp{version=1 method="SUB" ttl=30} filter{ x!=30}
senp{version=1 method="SUB" ttl=30} filter{ x!=10}
senp{version=1 method="SUB" ttl=30} filter{ x>0}
senp{version=1 method="SUB" ttl=30} filter{ x!=10}
senp{version=1 method="SUB" ttl=30} filter{ x<20}
senp{version=1 method="SUB" ttl=30} filter{ x!=10}
senp{version=1 method="SUB" ttl=30} filter{ x<=10}
senp{version=1 method="SUB" ttl=30} filter{ x!=10}
senp{version=1 method="SUB" ttl=30} filter{ x>1}
senp{version=1 method="SUB" ttl=30} filter{ x!=10}
senp{version=1 method="SUB" ttl=30} filter{ x>=10}
senp{version=1 method="SUB" ttl=30} filter{ x!=10.5}
senp{version=1 method="SUB" ttl=30} filter{ x=0}
senp{version=1 method="SUB" ttl=30} filter{ x!=10.5}
senp{version=1 method="SUB" ttl=30} filter{ x=10}
senp{version=1 method="SUB" ttl=30} filter{ x!=10.5}
senp{version=1 method="SUB" ttl=30} filter{ x!=10.5}
senp{version=1 method="SUB" ttl=30} filter{ x!=10.5}
senp{version=1 method="SUB" ttl=30} filter{ x<0}
senp{version=1 method="SUB" ttl=30} filter{ x!=10.5}
senp{version=1 method="SUB" ttl=30} filter{ x<10}
senp{version=1 method="SUB" ttl=30} filter{ x!=10.5}
senp{version=1 method="SUB" ttl=30} filter{ x<=10}
senp{version=1 method="SUB" ttl=30} filter{ x!=10.5}
senp{version=1 method="SUB" ttl=30} filter{ x>120}
senp{version=1 method="SUB" ttl=30} filter{ x!=10.5}
senp{version=1 method="SUB" ttl=30} filter{ x>11}
senp{version=1 method="SUB" ttl=30} filter{ x!=10.5}
senp{version=1 method="SUB" ttl=30} filter{ x>=11}
senp{version=1 method="SUB" ttl=30} filter{ x!=10.5}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x!=10.5}
senp{version=1 method="SUB" ttl=30} filter{ x=10.5}
senp{version=1 method="SUB" ttl=30} filter{ x!=10.5}
senp{version=1 method="SUB" ttl=30} filter{ x!=30}
senp{version=1 method="SUB" ttl=30} filter{ x!=10.5}
senp{version=1 method="SUB" ttl=30} filter{ x>0}
senp{version=1 method="SUB" ttl=30} filter{ x!=10.5}
senp{version=1 method="SUB" ttl=30} filter{ x<20}
senp{version=1 method="SUB" ttl=30} filter{ x!=10.5}
senp{version=1 method="SUB" ttl=30} filter{ x<=11}
senp{version=1 method="SUB" ttl=30} filter{ x!=10.5}
senp{version=1 method="SUB" ttl=30} filter{ x>1}
senp{version=1 method="SUB" ttl=30} filter{ x!=10.5}
senp{version=1 method="SUB" ttl=30} filter{ x>=10}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x="xyz246"}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x>"xyz246"}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x<"xxyyzz"}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x<="xyy"}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x>="xyza"}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x*<"xyZ"}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x>*"xyz246"}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x*"xyh"}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x>"xyac"}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x<"zxy"}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x<="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x>="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x!="abc"}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x*<"yz"}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x>*"xy"}
senp{version=1 method="SUB" ttl=30} filter{ x!="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x*"x"}
senp{version=1 method="SUB" ttl=30} filter{ x!=true}
senp{version=1 method="SUB" ttl=30} filter{ x=false}
senp{version=1 method="SUB" ttl=30} filter{ x!=true}
senp{version=1 method="SUB" ttl=30} filter{ x!=true}
senp{version=1 method="SUB" ttl=30} filter{ x!=true}
senp{version=1 method="SUB" ttl=30} filter{ x<true}
senp{version=1 method="SUB" ttl=30} filter{ x!=true}
senp{version=1 method="SUB" ttl=30} filter{ x<=false}
senp{version=1 method="SUB" ttl=30} filter{ x!=true}
senp{version=1 method="SUB" ttl=30} filter{ x>=false}
senp{version=1 method="SUB" ttl=30} filter{ x!=true}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x!=true}
senp{version=1 method="SUB" ttl=30} filter{ x=true}
senp{version=1 method="SUB" ttl=30} filter{ x!=true}
senp{version=1 method="SUB" ttl=30} filter{ x!=false}
senp{version=1 method="SUB" ttl=30} filter{ x!=true}
senp{version=1 method="SUB" ttl=30} filter{ x>false}
senp{version=1 method="SUB" ttl=30} filter{ x!=true}
senp{version=1 method="SUB" ttl=30} filter{ x<=true}
senp{version=1 method="SUB" ttl=30} filter{ x!=true}
senp{version=1 method="SUB" ttl=30} filter{ x>=true}
senp{version=1 method="SUB" ttl=30} filter{ x>"xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x="xza"}
senp{version=1 method="SUB" ttl=30} filter{ x>"xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x>"zzxyz246"}
senp{version=1 method="SUB" ttl=30} filter{ x>"xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x>="xyza"}
senp{version=1 method="SUB" ttl=30} filter{ x>"xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x>"xzzzz"}
senp{version=1 method="SUB" ttl=30} filter{ x>"xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x>*"za"}
senp{version=1 method="SUB" ttl=30} filter{ x>"xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x>"xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x="xaz"}
senp{version=1 method="SUB" ttl=30} filter{ x>"xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x>"xyac"}
senp{version=1 method="SUB" ttl=30} filter{ x>"xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x<"zzz"}
senp{version=1 method="SUB" ttl=30} filter{ x>"xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x<="zzz"}
senp{version=1 method="SUB" ttl=30} filter{ x>"xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x>="xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x>"xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x*<"x"}
senp{version=1 method="SUB" ttl=30} filter{ x>"xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x>*"x"}
senp{version=1 method="SUB" ttl=30} filter{ x>"xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x>*"xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x>"xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x*"y"}
senp{version=1 method="SUB" ttl=30} filter{ x>"xyz"}
senp{version=1 method="SUB" ttl=30} filter{ x*"a"}
senp{version=1 method="SUB" ttl=30} filter{ x>10}
senp{version=1 method="SUB" ttl=30} filter{ x=20}
senp{version=1 method="SUB" ttl=30} filter{ x>10}
senp{version=1 method="SUB" ttl=30} filter{ x>15}
senp{version=1 method="SUB" ttl=30} filter{ x>10}
senp{version=1 method="SUB" ttl=30} filter{ x>=11}
senp{version=1 method="SUB" ttl=30} filter{ x>10}
senp{version=1 method="SUB" ttl=30} filter{ x any}
senp{version=1 method="SUB" ttl=30} filter{ x>10}
senp{version=1 method="SUB" ttl=30} filter{ x=5}
senp{version=1 method="SUB" ttl=30} filter{ x>10}
senp{version=1 method="SUB" ttl=30} filter{ x>5}
senp{version=1 method="SUB" ttl=30} filter{ x>10}
senp{version=1 method="SUB" ttl=30} filter{ x<20}
senp{version=1 method="SUB" ttl=30} filter{ x>10}
senp{version=1 method="SUB" ttl=30} filter{ x<=10}
senp{version=1 method="SUB" ttl=30} filter{ x>10}
senp{version=1 method="SUB" ttl=30} filter{ x>=0}
senp{version=1 method="SUB" ttl=30} filter{ x>10}
senp{version=1 method="SUB" ttl=30} filter{ x!=10}
senp{version=1 method="SUB" ttl=30} filter{ Action="Login" From="antonio" Scope="com.eventdesktop.eventlet.messenger" Type="Log"}
senp{version=1 method="SUB" ttl=30} filter{ Action="Login" From="antonio" Scope="com.eventdesktop.eventlet.messenger" Type="Log"}
senp{version=1 method="SUB" ttl=30} filter{ x>=1 x<5}
senp{version=1 method="SUB" ttl=30} filter{ x=1}
EOF
if diff test.out test.expected; then
    rm -f test.out test.expected
    echo PASSED.
    exit 0
else
    exit 1 
fi