//
//  This file is part of Siena, a wide-area event notification system.
//  See http://www.inf.usi.ch/carzaniga/siena/
//
//  Author: Antonio Carzaniga (firstname.lastname@usi.ch)
//  See the file AUTHORS for full details. 
//
//  Copyright (C) 1998-2002 University of Colorado
//
//  Siena is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//  
//  Siena is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//  
//  You should have received a copy of the GNU General Public License
//  along with Siena.  If not, see <http://www.gnu.org/licenses/>.
//
package siena;

/** malformed SENP packet **/
public class SENPInvalidFormat extends SienaException {
    static final long serialVersionUID = 1L;

    int		expected_type;
    String	expected_value;
    int		stream_position;

    SENPInvalidFormat(String v, int pos) {
	super("expecting: `" + v + "' at stream position " + pos);
	expected_value = v;
	stream_position = pos;
    }
    SENPInvalidFormat(int t, String v, int pos) {
	this(v, pos);
	expected_type = t;
    }
    SENPInvalidFormat(int t, int pos) {
	super("at stream position " + pos);
	expected_type = t;
	stream_position = pos;
    }
    
    public int getExpectedType() {
	return expected_type;
    }

    public String getExpectedValue() {
	return expected_value;
    }

    public int getStreamPosition() {
	return stream_position;
    }
}
