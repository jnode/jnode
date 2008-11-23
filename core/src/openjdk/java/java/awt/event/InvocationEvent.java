/*
 * Copyright 1998-2006 Sun Microsystems, Inc.  All Rights Reserved.
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

package java.awt.event;

import java.awt.ActiveEvent;
import java.awt.AWTEvent;

/**
 * An event which executes the <code>run()</code> method on a <code>Runnable
 * </code> when dispatched by the AWT event dispatcher thread. This class can
 * be used as a reference implementation of <code>ActiveEvent</code> rather
 * than declaring a new class and defining <code>dispatch()</code>.<p>
 *
 * Instances of this class are placed on the <code>EventQueue</code> by calls
 * to <code>invokeLater</code> and <code>invokeAndWait</code>. Client code
 * can use this fact to write replacement functions for <code>invokeLater
 * </code> and <code>invokeAndWait</code> without writing special-case code
 * in any <code>AWTEventListener</code> objects.
 *
 * @author	Fred Ecks
 * @author	David Mendenhall
 *
 * @see		java.awt.ActiveEvent
 * @see		java.awt.EventQueue#invokeLater
 * @see		java.awt.EventQueue#invokeAndWait
 * @see		AWTEventListener
 *
 * @since 	1.2
 */
public class InvocationEvent extends AWTEvent implements ActiveEvent {

    /**
     * Marks the first integer id for the range of invocation event ids.
     */
    public static final int INVOCATION_FIRST = 1200;

    /**
     * The default id for all InvocationEvents.
     */
    public static final int INVOCATION_DEFAULT = INVOCATION_FIRST;

    /**
     * Marks the last integer id for the range of invocation event ids.
     */
    public static final int INVOCATION_LAST = INVOCATION_DEFAULT;

    /**
     * The Runnable whose run() method will be called.
     */
    protected Runnable runnable;

    /**
     * The (potentially null) Object whose notifyAll() method will be called
     * immediately after the Runnable.run() method returns.
     */
    protected Object notifier;

    /**
     * Set to true if dispatch() catches Throwable and stores it in the
     * exception instance variable. If false, Throwables are propagated up
     * to the EventDispatchThread's dispatch loop.
     */
    protected boolean catchExceptions;

    /**
     * The (potentially null) Exception thrown during execution of the
     * Runnable.run() method. This variable will also be null if a particular
     * instance does not catch exceptions.
     */
    private Exception exception = null;

    /**
     * The (potentially null) Throwable thrown during execution of the
     * Runnable.run() method. This variable will also be null if a particular
     * instance does not catch exceptions.
     */
    private Throwable throwable = null;

    /**
     * The timestamp of when this event occurred.
     *
     * @serial
     * @see #getWhen
     */
    private long when;

    /*
     * JDK 1.1 serialVersionUID.
     */
    private static final long serialVersionUID = 436056344909459450L;

    /**
     * Constructs an <code>InvocationEvent</code> with the specified
     * source which will execute the runnable's <code>run</code>
     * method when dispatched.
     * <p>This is a convenience constructor.  An invocation of the form
     * <tt>InvocationEvent(source, runnable)</tt>
     * behaves in exactly the same way as the invocation of
     * <tt>{@link #InvocationEvent(Object, Runnable, Object, boolean) InvocationEvent}(source, runnable, null, false)</tt>.
     * <p> This method throws an <code>IllegalArgumentException</code>
     * if <code>source</code> is <code>null</code>.
     *
     * @param source	the <code>Object</code> that originated the event
     * @param runnable	the <code>Runnable</code> whose <code>run</code>
     *                  method will be executed
     * @throws IllegalArgumentException if <code>source</code> is null
     *
     * @see     #InvocationEvent(Object, Runnable, Object, boolean)
     */
    public InvocationEvent(Object source, Runnable runnable) {
        this(source, runnable, null, false);
    }

    /**
     * Constructs an <code>InvocationEvent</code> with the specified
     * source which will execute the runnable's <code>run</code>
     * method when dispatched.  If notifier is non-<code>null</code>,
     * <code>notifyAll()</code> will be called on it
     * immediately after <code>run</code> returns.
     * <p>An invocation of the form <tt>InvocationEvent(source,
     * runnable, notifier, catchThrowables)</tt>
     * behaves in exactly the same way as the invocation of
     * <tt>{@link #InvocationEvent(Object, int, Runnable, Object, boolean) InvocationEvent}(source, InvocationEvent.INVOCATION_DEFAULT, runnable, notifier, catchThrowables)</tt>.
     * <p>This method throws an <code>IllegalArgumentException</code>
     * if <code>source</code> is <code>null</code>.
     *
     * @param source		the <code>Object</code> that originated
     *                          the event
     * @param runnable		the <code>Runnable</code> whose
     *                          <code>run</code> method will be
     *                          executed
     * @param notifier		the Object whose <code>notifyAll</code>
     *                          method will be called after
     *                          <code>Runnable.run</code> has returned
     * @param catchThrowables	specifies whether <code>dispatch</code>
     *                          should catch Throwable when executing
     *                          the <code>Runnable</code>'s <code>run</code>
     *                          method, or should instead propagate those
     *                          Throwables to the EventDispatchThread's
     *                          dispatch loop
     * @throws IllegalArgumentException if <code>source</code> is null
     *
     * @see     #InvocationEvent(Object, int, Runnable, Object, boolean)
     */
    public InvocationEvent(Object source, Runnable runnable, Object notifier,
                           boolean catchThrowables) {
	this(source, INVOCATION_DEFAULT, runnable, notifier, catchThrowables);
    }

    /**
     * Constructs an <code>InvocationEvent</code> with the specified
     * source and ID which will execute the runnable's <code>run</code>
     * method when dispatched.  If notifier is non-<code>null</code>,
     * <code>notifyAll</code> will be called on it 
     * immediately after <code>run</code> returns.
     * <p>Note that passing in an invalid <code>id</code> results in
     * unspecified behavior. This method throws an
     * <code>IllegalArgumentException</code> if <code>source</code>
     * is <code>null</code>.
     *
     * @param source		the <code>Object</code> that originated
     *                          the event
     * @param id		the ID for the event
     * @param runnable		the <code>Runnable</code> whose
     *                          <code>run</code> method will be executed
     * @param notifier		the <code>Object</code> whose <code>notifyAll</code>
     *                          method will be called after
     *                          <code>Runnable.run</code> has returned
     * @param catchThrowables	specifies whether <code>dispatch</code>
     *                          should catch Throwable when executing the
     *                          <code>Runnable</code>'s <code>run</code>
     *                          method, or should instead propagate those
     *                          Throwables to the EventDispatchThread's
     *                          dispatch loop
     * @throws IllegalArgumentException if <code>source</code> is null
     */
    protected InvocationEvent(Object source, int id, Runnable runnable, 
                              Object notifier, boolean catchThrowables) {
        super(source, id);
	this.runnable = runnable;
	this.notifier = notifier;
	this.catchExceptions = catchThrowables;
        this.when = System.currentTimeMillis();
    }

    /**
     * Executes the Runnable's <code>run()</code> method and notifies the
     * notifier (if any) when <code>run()</code> returns.
     */
    public void dispatch() {
	if (catchExceptions) {
	    try {
		runnable.run();
	    } 
	    catch (Throwable t) {
                if (t instanceof Exception) {
                    exception = (Exception) t;
                }
                throwable = t;
	    }
	}
	else {
	    runnable.run();
	}

	if (notifier != null) {
	    synchronized (notifier) {
		notifier.notifyAll();
	    }
	}
    }

    /**
     * Returns any Exception caught while executing the Runnable's <code>run()
     * </code> method.
     *
     * @return	A reference to the Exception if one was thrown; null if no
     *		Exception was thrown or if this InvocationEvent does not
     *		catch exceptions
     */
    public Exception getException() {
	return (catchExceptions) ? exception : null;
    }

    /**
     * Returns any Throwable caught while executing the Runnable's <code>run()
     * </code> method.
     *
     * @return	A reference to the Throwable if one was thrown; null if no
     *		Throwable was thrown or if this InvocationEvent does not
     *		catch Throwables
     * @since 1.5
     */
    public Throwable getThrowable() {
	return (catchExceptions) ? throwable : null;
    }

    /**
     * Returns the timestamp of when this event occurred.
     *
     * @return this event's timestamp
     * @since 1.4
     */
    public long getWhen() {
        return when;
    }

    /**
     * Returns a parameter string identifying this event.
     * This method is useful for event-logging and for debugging.
     *
     * @return  A string identifying the event and its attributes
     */
    public String paramString() {
        String typeStr;
	switch(id) {
            case INVOCATION_DEFAULT:
	        typeStr = "INVOCATION_DEFAULT";
		break;
            default:
	        typeStr = "unknown type";
	}
	return typeStr + ",runnable=" + runnable + ",notifier=" + notifier +
	    ",catchExceptions=" + catchExceptions + ",when=" + when;
    }
}
