/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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

package org.jnode.vm.scheduler;

/**
 * Simple counting semaphore.
 * See: http://en.wikipedia.org/wiki/Semaphore_(programming)
 *
 * @author ewout
 */
public final class Semaphore {

    private int count;

    /**
     * create and initialise semaphore to n
     */
    public Semaphore(int n) {
        this.count = n;
    }

    /**
     * Decrements the value of semaphore by 1. If the value becomes negative,
     * the process executing wait() is blocked. This is also called "wait".
     */
    public synchronized void down() {
        while (count == 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                //empty
            }
        }
        count--;
    }

    /**
     * Increment the value of the semaphore by 1.
     * Unlock waiting threads.
     * This operation is also called
     * "signal"
     */
    public synchronized void up() {
        count++;
        notify(); // notify blocked processes that we're done
    }
}
