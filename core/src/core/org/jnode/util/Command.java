/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.util;


/**
 * @author epr
 */
public abstract class Command {

    /**
     * Is this command finished yet?
     */
    private boolean finished = false;

    /**
     * Has this command finished?
     *
     * @return boolean
     */
    public final boolean isFinished() {
        return finished;
    }

    /**
     * Mark this command as finished.
     * Notify all waiting threads.
     */
    protected final synchronized void notifyFinished() {
        finished = true;
        notifyAll();
    }

    /**
     * Block the current thread, until this command has finished.
     *
     * @param timeout
     * @throws InterruptedException This thread was interrupted
     * @throws TimeoutException     A timeout occurred.
     */
    public synchronized void waitUntilFinished(long timeout)
        throws InterruptedException, TimeoutException {
        final long start = System.currentTimeMillis();
        while (!finished) {
            wait(timeout);
            if ((timeout > 0) && (!finished)) {
                if (System.currentTimeMillis() >= start + timeout) {
                    throw new TimeoutException("timeout");
                }
            }
        }
    }
}
