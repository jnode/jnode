/* class SyncThread
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
import charva.awt.event.GarbageCollectionEvent;
import charva.awt.event.SyncEvent;

/**
 * The purpose of this class it to speed up redrawing of the screen
 * after the closing (or opening) of a window and the subsequent opening 
 * of a new window, by delaying the calling of Toolkit.sync() until 
 * the new window has had a chance to be drawn.<p>
 *
 * Note that it is not _guaranteed_ that the new window will be drawn in 
 * time, but it probably will be.<p>
 * 
 * This thread loops forever, reading SyncEvents off the SyncQueue
 * (a SyncEvent is generated whenever a window closes and the underlying
 * windows have been redrawn).
 * After reading the event off the queue, this thread sleeps for 50 msec
 * and then puts the event onto the EventQueue, from which it will be 
 * picked up by the active window. If there are more events on the SyncQueue
 * when the thread awakes, the SyncQueue is drained.<p>
 *
 * This thread also processes GarbageCollectionEvents; when it receives
 * a GarbageCollectionEvent, it calls System.gc() (after waiting 50 msec
 * to give the AWT event-handler time to complete).
 */
class SyncThread
    extends Thread
{
    SyncThread(SyncQueue syncQueue_, EventQueue eventQueue_) {
	_syncQueue = syncQueue_;
	_eventQueue = eventQueue_;
    }

    public void run() {
	for (;;) {
	    AWTEvent evt = _syncQueue.getNextEvent();
        // Sync thread exit point
        if(evt == null) break;
	    try {
		sleep(50); 
	    }
	    catch (InterruptedException e) {
		System.err.println("SyncThread: sleep interrupted!");
	    }

	    if (evt instanceof SyncEvent) {
		_eventQueue.postEvent(evt);

		/* If there any more SyncEvents on the queue, drain the queue;
		 * there is no point in putting more than one SyncEvent on the
		 * EventQueue.
		 */
		while (_syncQueue.isEmpty() == false) {
		    if (_syncQueue.getNextEvent() instanceof GarbageCollectionEvent) {
			System.gc();
			break;
		    }
		}
	    } /* //LS
	    else
		System.gc();	// it was a GarbageCollectionEvent
        */
	}
    }

    private SyncQueue _syncQueue;
    private EventQueue _eventQueue;
}
