/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.vm.memmgr.def;

import org.jnode.vm.Unsafe;
import org.jnode.vm.scheduler.Monitor;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class GCThread extends Thread {

    /**
     * The manager
     */
    private final GCManager manager;

    /**
     * Monitor for synchronizing access to my fields
     */
    private final Monitor heapMonitor;

    /**
     * Is a GC run requested?
     */
    private boolean runNeeded;

    /**
     * Is the GC currently active
     */
    private boolean gcActive;

    /**
     * Initialize this instance.
     *
     * @param manager
     */
    public GCThread(GCManager manager, Monitor heapMonitor) {
        super("gc-thread");
        this.manager = manager;
        this.heapMonitor = heapMonitor;
    }

    /**
     * Trigger a GC run.
     *
     * @param waitToFinish If true, block until the run is ready, if false, return
     *                     immediately.
     */
    public final void trigger(boolean waitToFinish) {
        if (runNeeded && !waitToFinish) {
            return;
        }
        heapMonitor.enter();
        try {
            runNeeded = true;
            heapMonitor.NotifyAll();
            if (waitToFinish) {
                while (runNeeded || gcActive) {
                    try {
                        heapMonitor.Wait(0L);
                    } catch (InterruptedException ex) {
                        // Ignore
                    }
                }
            }
        } finally {
            heapMonitor.exit();
        }
    }

    /**
     * Continue to GC.
     *
     * @see java.lang.Runnable#run()
     */
    public final void run() {
        while (true) {
            try {
                heapMonitor.enter();
                try {
                    while (!runNeeded) {
                        heapMonitor.Wait(0L);
                    }
                    gcActive = true;
                    runNeeded = false;
                } finally {
                    heapMonitor.exit();
                }

                // Now do the actual GC
                manager.gc();

                // Notify that we're ready
                gcActive = false;
                heapMonitor.enter();
                try {
                    heapMonitor.NotifyAll();
                } finally {
                    heapMonitor.exit();
                }
            } catch (Throwable ex) {
                try {
                    Unsafe.debug(ex.getMessage());
                    Unsafe.debug('\n');
                    Unsafe.debugStackTrace(ex);
                    Unsafe.die("GCThread failed");
                } catch (Throwable ex2) {
                    // Ignore
                }
            }
        }
    }
}
 
