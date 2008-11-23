/*
 * Copyright 1999 Sun Microsystems, Inc.  All Rights Reserved.
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
package javax.swing.text;

import java.util.Vector;

/**
 * A queue of text layout tasks. 
 *
 * @author  Timothy Prinzing
 * @see     AsyncBoxView
 * @since   1.3 
 */
public class LayoutQueue {

    Vector tasks;
    Thread worker;

    static LayoutQueue defaultQueue;

    /**
     * Construct a layout queue.
     */
    public LayoutQueue() {
	tasks = new Vector();
    }

    /**
     * Fetch the default layout queue.
     */
    public static LayoutQueue getDefaultQueue() {
	if (defaultQueue == null) {
	    defaultQueue = new LayoutQueue();
	}
	return defaultQueue;
    }

    /**
     * Set the default layout queue.
     *
     * @param q the new queue.
     */
    public static void setDefaultQueue(LayoutQueue q) {
	defaultQueue = q;
    }

    /**
     * Add a task that is not needed immediately because
     * the results are not believed to be visible.
     */
    public synchronized void addTask(Runnable task) {
	if (worker == null) {
	    worker = new LayoutThread();
	    worker.start();
	}
	tasks.addElement(task);
	notifyAll();
    }

    /**
     * Used by the worker thread to get a new task to execute
     */
    protected synchronized Runnable waitForWork() {
	while (tasks.size() == 0) {
	    try {
		wait();
	    } catch (InterruptedException ie) {
		return null;
	    }
	}
	Runnable work = (Runnable) tasks.firstElement();
	tasks.removeElementAt(0);
	return work;
    }

    /**
     * low priority thread to perform layout work forever
     */
    class LayoutThread extends Thread {
	
	LayoutThread() {
	    super("text-layout");
	    setPriority(Thread.MIN_PRIORITY);
	}
	
        public void run() {
	    Runnable work;
	    do {
		work = waitForWork();
		if (work != null) {
		    work.run();
		}
	    } while (work != null);
	}


    }

}
