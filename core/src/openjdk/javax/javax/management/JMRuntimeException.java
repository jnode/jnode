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

package javax.management; 


/**
 * Runtime exceptions emitted by JMX implementations.
 *
 * @since 1.5
 */
public class JMRuntimeException extends RuntimeException   { 
     
    /* Serial version */
    private static final long serialVersionUID = 6573344628407841861L;

    /**
     * Default constructor.
     */
    public JMRuntimeException() {
	super();
    }
    
    /**
     * Constructor that allows a specific error message to be specified.
     *
     * @param message the detail message.
     */
    public JMRuntimeException(String message) {
	super(message);
    }
    
    /**
     * Constructor with a nested exception.  This constructor is
     * package-private because it arrived too late for the JMX 1.2
     * specification.  A later version may make it public.
     */
    JMRuntimeException(String message, Throwable cause) {
	super(message);

	/* Make a best effort to set the cause, but if we don't
	   succeed, too bad, you don't get that useful debugging
	   information.  We jump through hoops here so that we can
	   work on platforms prior to J2SE 1.4 where the
	   Throwable.initCause method was introduced.  If we change
	   the public interface of JMRuntimeException in a future
	   version we can add getCause() so we don't need to do this.  */
	try {
	    java.lang.reflect.Method initCause =
		Throwable.class.getMethod("initCause",
					  new Class[] {Throwable.class});
	    initCause.invoke(this, new Object[] {cause});
	} catch (Exception e) {
	    // OK: just means we won't have debugging info
	}
    }
}
