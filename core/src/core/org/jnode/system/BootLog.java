/*
 * $Id$
 */
package org.jnode.system;

import java.io.PrintStream;

import org.jnode.vm.Unsafe;

/**
 * Logging class used during bootstrap.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class BootLog {
	
	public static final int DEBUG = 1;
	public static final int INFO = 2;
	public static final int WARN = 3;
	public static final int ERROR = 4;
	public static final int FATAL = 5;
	
	/**
	 * Log a debug message
	 * @param msg
	 */
	public static void debug(String msg) {
		log(DEBUG, System.out, msg, null);
	}

	/**
	 * Log a debug message
	 * @param msg
	 * @param ex
	 */
	public static void debug(String msg, Throwable ex) {
		log(DEBUG, System.out, msg, ex);
	}

	/**
	 * Log an error message
	 * @param msg
	 */
	public static void error(String msg) {
		log(ERROR, System.err, msg, null);
	}

	/**
	 * Log an error message
	 * @param msg
	 * @param ex
	 */
	public static void error(String msg, Throwable ex) {
		log(ERROR, System.err, msg, ex);
	}

	/**
	 * Log an fatal message
	 * @param msg
	 */
	public static void fatal(String msg) {
		log(FATAL, System.err, msg, null);
	}

	/**
	 * Log an fatal message
	 * @param msg
	 * @param ex
	 */
	public static void fatal(String msg, Throwable ex) {
		log(FATAL, System.err, msg, ex);
	}

	/**
	 * Log an info message
	 * @param msg
	 */
	public static void info(String msg) {
		log(INFO, System.out, msg, null);
	}

	/**
	 * Log an info message
	 * @param msg
	 * @param ex
	 */
	public static void info(String msg, Throwable ex) {
		log(INFO, System.out, msg, ex);
	}

	/**
	 * Log an warning message
	 * @param msg
	 * @param ex
	 */
	public static void warn(String msg, Throwable ex) {
		log(WARN, System.out, msg, ex);
	}

	/**
	 * Log an warning message
	 * @param msg
	 */
	public static void warn(String msg) {
		log(WARN, System.out, msg, null);
	}

	/**
	 * Log an error message
	 * @param level
	 * @param ps
	 * @param msg
	 * @param ex
	 */
	public static void log(int level, PrintStream ps, String msg, Throwable ex) {
		if (ps != null) {
			if (msg != null) {
				ps.println(msg);
			}
			if (ex != null) {
				ex.printStackTrace(ps);
			}
		} else {
			if (msg != null) {
				Unsafe.debug(msg);
			}
			if (ex != null) {
				Unsafe.debug(ex.toString());
			}
		}
	}
}
