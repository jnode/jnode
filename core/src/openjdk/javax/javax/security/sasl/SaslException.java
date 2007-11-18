/*
 * Copyright 1999-2003 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package javax.security.sasl;

import java.io.IOException;

/**
 * This class represents an error that has occurred when using SASL.
 *
 * @since 1.5
 *
 * @author Rosanna Lee
 * @author Rob Weltman
 */

public class SaslException extends IOException {
    /**
     * The possibly null root cause exception.
     * @serial
     */
    // Required for serialization interoperability with JSR 28
    private Throwable _exception;

    /**
     * Constructs a new instance of <tt>SaslException</tt>.
     * The root exception and the detailed message are null.
     */
    public SaslException () {
	super();
    }

    /**
     * Constructs a new instance of <tt>SaslException</tt> with a detailed message.
     * The root exception is null.
     * @param detail A possibly null string containing details of the exception.
     *
     * @see java.lang.Throwable#getMessage
     */
    public SaslException (String detail) {
	super(detail);
    }

    /**
     * Constructs a new instance of <tt>SaslException</tt> with a detailed message
     * and a root exception.
     * For example, a SaslException might result from a problem with
     * the callback handler, which might throw a NoSuchCallbackException if
     * it does not support the requested callback, or throw an IOException
     * if it had problems obtaining data for the callback. The
     * SaslException's root exception would be then be the exception thrown
     * by the callback handler.
     *
     * @param detail A possibly null string containing details of the exception.
     * @param ex A possibly null root exception that caused this exception.
     *
     * @see java.lang.Throwable#getMessage
     * @see #getCause
     */
    public SaslException (String detail, Throwable ex) {
	super(detail);
	if (ex != null) {
	    initCause(ex);
	}
    }

    /*
     * Override Throwable.getCause() to ensure deserialized object from
     * JSR 28 would return same value for getCause() (i.e., _exception).
     */
    public Throwable getCause() {
	return _exception;
    }

    /*
     * Override Throwable.initCause() to match getCause() by updating
     * _exception as well.
     */
    public Throwable initCause(Throwable cause) {
	super.initCause(cause);
	_exception = cause;
	return this;
    }

    /**
     * Returns the string representation of this exception.
     * The string representation contains
     * this exception's class name, its detailed messsage, and if
     * it has a root exception, the string representation of the root
     * exception. This string representation
     * is meant for debugging and not meant to be interpreted
     * programmatically.
     * @return The non-null string representation of this exception.
     * @see java.lang.Throwable#getMessage
     */
    // Override Throwable.toString() to conform to JSR 28
    public String toString() {
	String answer = super.toString();
	if (_exception != null && _exception != this) {
	    answer += " [Caused by " + _exception.toString() + "]";
	}
	return answer;
    }

    /** Use serialVersionUID from JSR 28 RI for interoperability */ 
    private static final long serialVersionUID = 4579784287983423626L;
}
