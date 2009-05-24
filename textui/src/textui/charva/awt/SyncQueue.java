/* class SyncQueue
 *
 * Copyright (C) 2001  R M Pitman
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

import charva.awt.event.AWTEvent;

/**
 * This class is used to coalesce several "sync" events together, 
 * if possible. This speeds up redrawing.
 * The queue also handles GarbageCollectionEvents.
 */
class SyncQueue extends java.util.LinkedList<AWTEvent>
{
    private static final long serialVersionUID = 1L;

    private static SyncQueue _instance;
    private boolean stopped = false;

    private SyncQueue() {
	super();
    }

    public static SyncQueue getInstance() {
	if (_instance == null) {
	    _instance = new SyncQueue();

	    /* Start a thread to read from the SyncQueue. Make it a daemon
	     * thread so that the program will exit when the main thread 
	     * ends.
	     */
	    SyncThread thr = new SyncThread(_instance, EventQueue.getInstance());
	    thr.setDaemon(true);
	    thr.setName("sync thread");
	    thr.start();
	}
	return _instance;
    }

    public synchronized void stop(){
        stopped = true;
        notifyAll();
        _instance = null;
    }

    public synchronized void postEvent(AWTEvent evt_) {
	_instance.addLast(evt_);
	_instance.notifyAll();	    // wake up the dequeueing thread
    }

    public synchronized AWTEvent getNextEvent() {
        /* If the queue is empty, block until another thread enqueues
         * an event.
         */
        while (super.size() == 0 && !stopped) {
            try { wait(); }
            catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }      
	return stopped ? null :(AWTEvent) _instance.removeFirst();
    }

    public synchronized boolean isEmpty() {
	return (_instance.size() == 0);
    }
}
