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

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * logging and error reporting facility for Siena.
 * 
 * <code>Logging</code> allows you to redirect error and log messages to
 * specific streams.
 */
public class Logging {
    static PrintStream log = null;
    static PrintStream err = System.err;

    public static int DEBUG = 1;
    public static int INFO = 2;
    public static int WARN = 3;
    public static int ERROR = 4;
    public static int FATAL = 5;
    public static int severity = 2; // default logged msg severity

    /**
     * GT: used finer time granularity
     */
    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS Z");

    static private String time() {
	/**
	 * GT: used finer time granularity
	 */
	return sdf.format(new Date()) + " ";
    }

    synchronized static void exerr(Exception ex) {
	if (err != null) {
	    err.print(time());
	    ex.printStackTrace(err);
	}
    }

    synchronized static void exlog(Exception ex) {
	if (log != null) {
	    log.print(time());
	    ex.printStackTrace(log);
	}
    }

    synchronized static void prerr(String s) {
	if (err != null)
	    err.print(time() + s);
    }

    synchronized static void prlog(String s, int sev) {
	if (sev >= severity) {
	    if (log != null)
		log.print(time() + s);
	}
    }

    synchronized static void prlog(String s) {
	if (log != null)
	    log.print(time() + s);
    }

    synchronized static void prlnerr(String s, int sev) {
	if (sev >= severity) {
	    if (err != null)
		err.println(time() + s);
	}
    }

    public synchronized static void prlnerr(String s) {
	if (err != null)
	    err.println(time() + s);
    }

    public synchronized static void prlnlog(String s, int sev) {
	if (sev >= severity) {
	    if (log != null)
		log.println(time() + s);
	}
    }

    synchronized static void prlnlog(String s) {
	if (log != null)
	    log.println(time() + s);
    }
	
    synchronized static void setSeverity(int sev){
	severity = sev;
    }

    /**
     * sets a log and debug stream. <code>null</code> means no log and debug
     * output.
     * 
     * @param s
     *            the new debug output stream
     * @see #getLogStream()
     */
    synchronized static public void setLogStream(PrintStream s) {
	log = s;
    }

    /**
     * the current debug output stream. <code>null</code> means no debug
     * output.
     * 
     * @return the current debug output stream.
     * @see #setLogStream(PrintStream)
     */
    synchronized static public PrintStream getLogStream() {
	return log;
    }

    /**
     * sets an error output stream. <code>null</code> means no error output.
     * 
     * @param s
     *            the new error output stream
     * @see #getErrorStream()
     */
    synchronized static public void setErrorStream(PrintStream s) {
	err = s;
    }

    /**
     * the current error output stream. <code>null</code> means no error
     * output.
     * 
     * @return the current error output stream.
     * @see #setErrorStream(PrintStream)
     */
    synchronized static public PrintStream getErrorStream() {
	return err;
    }
}
