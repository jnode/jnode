/* class InvocationEvent
 *
 * Copyright (C) 2001, 2002  R M Pitman
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package charva.awt.event;


/**
 * An event which executes the <code>run()</code> method on a 
 * <code>Runnable</code> when dispatched by the AWT event dispatcher thread.
 */
public class InvocationEvent extends AWTEvent
{
    private static final long serialVersionUID = 1L;
    /**
     * Constructs an InvocationEvent with the specified source which
     * will execute the Runnable's run() method when dispatched by
     * the AWT dispatch thread.
     *
     * @param source_ the source of this event.
     * @param runnable_ the Runnable whose run() method will be called.
     * when the run() method has returned.
     */
    public InvocationEvent(Object source_, Runnable runnable_) {
	this(source_, runnable_, null);
    }

    /**
     * Constructs an InvocationEvent with the specified source which
     * will execute the Runnable's run() method when dispatched by
     * the AWT dispatch thread. After the run() method has returned,
     * the notifier's notifyAll() method will be called.
     *
     * @param source_ the source of this event.
     * @param runnable_ the Runnable whose run() method will be called.
     * @param notifier_ the object whose notifyAll() method will be called
     * when the run() method has returned.
     */
    public InvocationEvent(Object source_, Runnable runnable_, 
	    Object notifier_) {
	super(source_, AWTEvent.INVOCATION_EVENT);
	_runnable = runnable_;
	_notifier = notifier_;
    }

    /**
     * Executes the runnable's run() method and then (if the notifier
     * is non-null) calls the notifier's notifyAll() method.
     */
    public void dispatch() {
	_runnable.run();

	if (_notifier != null) {
	    synchronized (_notifier) {
		_notifier.notifyAll();
	    }
	}
    }

    //====================================================================
    // INSTANCE VARIABLES

    /** The Runnable whose run() method will be called.
     */
    protected Runnable _runnable;

    /** The (possibly null) object whose notifyAll() method will be called
     * as soon as the Runnable's run() method has returned.
     */
    protected Object _notifier;
}
