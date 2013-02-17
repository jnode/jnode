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
 
package org.jnode.vm.objects;


/**
 * @author epr
 */
public abstract class Statistic extends VmSystemObject {

    private final String name;
    private final String description;

    public Statistic(String name) {
        this(name, null);
    }

    public Statistic(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public abstract Object getValue();

    /**
     * Gets the name of this statistic
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Convert to a String representation
     *
     * @return String
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return name;
    }

    /**
     * Gets the description of this statistic
     *
     * @return The description
     */
    public String getDescription() {
        return description;
    }
}
