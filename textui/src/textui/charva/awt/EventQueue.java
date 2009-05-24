/* class EventQueue
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

package charva.awt;

import java.util.LinkedList;

import charva.awt.event.AWTEvent;
import charva.awt.event.InvocationEvent;
import charva.awt.event.SyncEvent;

/**
 * The EventQueue class queues "AWT" events, which are used to trigger
 * drawing actions.  They can be enqueued using the postEvent() method 
 * by any thread (e.g the keyboard reading thread enqueues KeyEvents), 
 * but only the event-dispatching thread should call getNextEvent(), 
 * because only the event-dispatching thread should do any drawing.
 * This is because the ncurses library is not re-entrant.<p>
 *
 * SyncEvents are enqueued with a lower priority than other events, and
 * if multiple SyncEvents are found on the queue they are coalesced into
 * a single SyncEvent.
 */
public class EventQueue
{

    /**
     * The constructor cannot be called from outside the class, making
     * this an example of the Singleton pattern.
     */
    private EventQueue() {
    }

    public synchronized static EventQueue getInstance() {
	if (_instance == null) {
	    _instance = new EventQueue();
	}
	return _instance;
    }

    public synchronized void postEvent(AWTEvent evt_) {
	addLast(evt_);
	notifyAll();	    // wake up the dequeueing thread
    }

    public synchronized AWTEvent getNextEvent() {

        /* If the queue is empty, block until another thread enqueues
         * an event.
         */
        while (queuesAreEmpty()) {
            try { wait(); }
            catch (InterruptedException e) {
                e.printStackTrace();	// should never happen
            }
        }      
	return (AWTEvent) removeFirst();
    }

    /** Causes the runnable's run() method to be called in the
     * AWT dispatch thread.
     */
    public static void invokeLater(Runnable runnable_) {
	getInstance().postEvent(
		new InvocationEvent(Toolkit.getDefaultToolkit(), runnable_));
    }

    /** Returns true if both the high-priority queue and the low-priority
     * queue are empty. It is not necessary to make this synchronized 
     * because it is called from a synchronized method.
     */
    private boolean queuesAreEmpty() {
	return ((_lowPriorityQueue.size() == 0) && 
		(_highPriorityQueue.size() == 0));
    }

    /** Enqueue the event onto one of two queues, depending on its type.
     */
    private void addLast(AWTEvent evt_) {
	if (evt_ instanceof SyncEvent)
	    _lowPriorityQueue.addLast(evt_);
	else
	    _highPriorityQueue.addLast(evt_);
    }

    /** This is called only if at least one of the queues is non-empty.
     */
    private Object removeFirst() {
	if (_highPriorityQueue.size() > 0)
	    return _highPriorityQueue.removeFirst();
	else {

/* FOR DEBUGGING
if (_lowPriorityQueue.size() > 1) 
    System.err.println("Coalescing " + _lowPriorityQueue.size() + " SyncEvents on queue");
else
    System.err.println("1 SyncEvent");
*/

	    /* Coalesce multiple SyncEvents into one.
	     */
	    Object obj = null;
	    while (_lowPriorityQueue.size() > 0)
		obj = _lowPriorityQueue.removeFirst();

	    return obj;
	}
    }

    //====================================================================
    // INSTANCE VARIABLES

    private LinkedList<AWTEvent> _lowPriorityQueue = new LinkedList<AWTEvent>();

    private LinkedList<AWTEvent> _highPriorityQueue = new LinkedList<AWTEvent>();

    //====================================================================
    // STATIC VARIABLES

    private static volatile EventQueue _instance = null;
}
