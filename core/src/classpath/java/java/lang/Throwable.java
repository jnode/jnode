/*
 * java.lang.Throwable
 * 
 * (c) 1997 George David Morrison
 * 
 * API version: 1.0.2
 * 
 * History: 01FEB1997 George David Morrison Initial version
 */

package java.lang;

import java.io.PrintStream;
import java.io.PrintWriter;

import org.jnode.vm.Unsafe;
import org.jnode.vm.VmSystem;

public class Throwable {

    private static final int MAX_ELEMENTS = 20;
    
	private String detailMessage;
	private Object[] backtrace;
	private Throwable cause;

	public String toString() {
		if (detailMessage != null) {
			return getClass().getName() + ": " + detailMessage;
		} else {
			return getClass().getName() + ": ";
		}
	}

	public String getMessage() {
		return detailMessage;
	}

	public Throwable fillInStackTrace() {
		backtrace = VmSystem.getStackTrace(Unsafe.getCurrentProcessor().getCurrentThread());
		return this;
	}

	public void printStackTrace() {
		PrintStream s = System.err;
		if (s != null) {
			printStackTrace(System.err);
		} else {
			final Object[] st = this.backtrace;
			if (st != null) {
				final int cnt = st.length;
				final int max = Math.min(cnt, MAX_ELEMENTS);
				for (int i = 0; i < max; i++) {
					final Object o = st[i];
					if (o != null) {
						Unsafe.debug(o.toString());
					} else {
						Unsafe.debug("null");
					}
				}
				if (cnt > max) {
				    Unsafe.debug("...");
				}
			}
		}
	}

	public void printStackTrace(PrintStream s) {
		s.println(this);
		final Object[] st = this.backtrace;
		final Throwable cause = this.cause;
		if (st != null) {
			final int cnt = st.length;
			final int max = Math.min(cnt, MAX_ELEMENTS);
			//s.print("Trace(" + cnt + "): ");
			for (int i = 0; i < max; i++) {
				final Object ste = st[i];
				if ((ste != null) && (ste instanceof char[])) {
					s.println((char[]) ste);
				} else {
					s.println(ste);
				}
				//s.println();
			}
			if (cnt > max) {
			    s.println("...");
			}
		} else {
			s.print("No stacktrace!");
		}
		if (cause != null) {
			s.print("Caused by: ");
			cause.printStackTrace(s);
		}
	}

	public void printStackTrace(PrintWriter s) {
		s.println(this);
		final Object[] st = this.backtrace;
		final Throwable cause = this.cause;
		if (st != null) {
			final int cnt = st.length;
			final int max = Math.min(cnt, MAX_ELEMENTS);
			//s.print("Trace(" + cnt + "): ");
			for (int i = 0; i < max; i++) {
				final Object ste = st[i];
				if ((ste != null) && (ste instanceof char[])) {
					s.println((char[]) ste);
				} else {
					s.println(ste);
				}
				//s.println();
			}
			if (cnt > max) {
			    s.println("...");
			}
		} else {
			s.print("No stacktrace!");
		}
		if (cause != null) {
			s.print("Caused by: ");
			cause.printStackTrace(s);
		}
	}

	public Throwable(String message, Throwable cause) {
		this.detailMessage = message;
		this.cause = cause;
		fillInStackTrace();
	}

	public Throwable(String message) {
		this(message, null);
	}

	public Throwable(Throwable cause) {
		this(cause.getMessage(), cause);
	}

	public Throwable() {
		this("", null);
	}

	/**
	 * Returns the cause.
	 * 
	 * @return Throwable
	 */
	public Throwable getCause() {
		return cause;
	}

	/**
	 * Initialize the cause of this Throwable. This may only be called once during the object
	 * lifetime, including implicitly by chaining constructors.
	 * 
	 * @param cause
	 *            the cause of this Throwable, may be null
	 * @return this
	 * @throws IllegalArgumentException
	 *             if cause is this (a Throwable can't be its own cause!)
	 * @throws IllegalStateException
	 *             if the cause has already been set
	 * @since 1.4
	 */
	public Throwable initCause(Throwable cause) {
		if (cause == this)
			throw new IllegalArgumentException();
		if (this.cause != null)
			throw new IllegalStateException();
		this.cause = cause;
		return this;
	}
	
	public StackTraceElement[] getStackTrace() {
	    throw new RuntimeException("Not implemented yet");
	}
}
