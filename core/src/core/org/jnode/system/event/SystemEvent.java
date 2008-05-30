/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.system.event;

/**
 * @author epr
 */
public class SystemEvent {

    protected final int id;
    private final long time;
    private boolean consumed;

    /**
     * Create a new system event
     *
     * @param id
     * @param time
     */
    public SystemEvent(int id, long time) {
        this.id = id;
        this.time = time;
    }

    /**
     * Create a new system event
     */
    public SystemEvent() {
        this(-1, System.currentTimeMillis());
    }

    /**
     * Create a new system event
     *
     * @param id
     */
    public SystemEvent(int id) {
        this(id, System.currentTimeMillis());
    }

    /**
     * @return int
     */
    final public int getId() {
        return id;
    }

    /**
     * @return long
     */
    final public long getTime() {
        return time;
    }

    /**
     * Mark this event as being consumed.
     */
    final public void consume() {
        consumed = true;
    }

    /**
     * Has this event been consumed.
     *
     * @return boolean
     */
    final public boolean isConsumed() {
        return consumed;
    }
}
