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
 
package org.jnode.fs.ext2.cache;

import java.util.EventObject;

/**
 * Event used to notify a CacheListener about events occuring to the cache
 * (currently used only when an element is removed from the cache).
 * 
 * @author Andras Nagy
 */
public class CacheEvent extends EventObject {
    public static final int REMOVED = 0;
    private int eventType;

    public CacheEvent(Object source) {
        super(source);
    }

    public CacheEvent(Object source, int type) {
        super(source);
        this.eventType = type;
    }

    /**
     * Returns the eventType.
     * 
     * @return int
     */
    public int getEventType() {
        return eventType;
    }

}
