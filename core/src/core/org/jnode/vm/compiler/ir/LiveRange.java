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
 
package org.jnode.vm.compiler.ir;

/**
 * @author Madhu Siddalingaiah
 */
public class LiveRange<T> implements Comparable<LiveRange<T>> {
    private Variable<T> variable;
    private int assignAddress;
    private int lastUseAddress;

    public LiveRange(Variable<T> v) {
        this.variable = v;
        this.assignAddress = v.getAssignAddress();
        this.lastUseAddress = v.getLastUseAddress();
    }

    public boolean interferesWith(LiveRange<T> other) {
        return lastUseAddress > other.getAssignAddress() ||
            other.lastUseAddress > assignAddress;
    }

    public Variable<T> getVariable() {
        return variable;
    }

    public int compareTo(LiveRange<T> other) {
        return assignAddress - other.getVariable().getAssignAddress();
    }

    public String toString() {
        String leader = variable.toString() + ": " +
            assignAddress + "-" + lastUseAddress;
        Location loc = getLocation();
        if (loc == null) {
            return leader;
        }
        return leader + " (" + loc + ")";
    }

    /**
     * @return the address at which the variable is assigned
     */
    public int getAssignAddress() {
        return assignAddress;
    }

    /**
     * @return the address at which the variable is last used
     */
    public int getLastUseAddress() {
        return lastUseAddress;
    }

    /**
     * @return the assigned register location
     */
    public Location<T> getLocation() {
        return variable.getLocation();
    }

    /**
     * @param loc the assigned register location
     */
    public void setLocation(Location<T> loc) {
        variable.setLocation(loc);
    }
}
