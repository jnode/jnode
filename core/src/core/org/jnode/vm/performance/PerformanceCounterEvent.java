/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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
 
package org.jnode.vm.performance;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class PerformanceCounterEvent implements
    Comparable<PerformanceCounterEvent> {

    /**
     * Identifier of this event
     */
    private final String id;
    private final String description;

    /**
     * Initialize this instance.
     *
     * @param preset
     */
    protected PerformanceCounterEvent(PresetEvent preset) {
        this(preset.name(), preset.getDescription());
    }

    /**
     * Initialize this instance.
     *
     * @param id
     */
    protected PerformanceCounterEvent(String id) {
        this(id, id);
    }

    /**
     * Initialize this instance.
     *
     * @param id
     */
    protected PerformanceCounterEvent(String id, String description) {
        this.id = id;
        this.description = description;
    }

    /**
     * Gets the language independent identifier of this event.
     *
     * @return the event id
     */
    public final String getId() {
        return id;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof PerformanceCounterEvent) {
            return (compareTo((PerformanceCounterEvent) obj) == 0);
        }
        return false;
    }

    public int hashCode() {
        return id.hashCode();
    }

    public String toString() {
        return id;
    }

    public int compareTo(PerformanceCounterEvent o) {
        return id.compareTo(o.id);
    }
}
