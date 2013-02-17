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
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class Counter extends Statistic implements Comparable<Counter> {

    private static final String is = "=";

    private int counter = 0;

    public Counter(String name) {
        this(name, null, 0);
    }

    public Counter(String name, String description) {
        this(name, description, 0);
    }

    public Counter(String name, int value) {
        this(name, null, value);
    }

    public Counter(String name, String description, int value) {
        super(name, description);
        this.counter = value;
    }

    /**
     * Gets the counter of this statistic
     *
     * @return the counter
     */
    public int get() {
        return counter;
    }

    public Object getValue() {
        return counter;
    }

    /**
     * Increment the counter of this statistic by 1.
     */
    public void inc() {
        counter++;
    }

    /**
     * Reset the counter to 0.
     */
    public void reset() {
        counter = 0;
    }

    /**
     * Add <i>increment</i> to the counter of this statistic.
     */
    public void add(int increment) {
        counter += increment;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Counter o) {
        if (o.counter < this.counter) {
            return 1;
        } else if (o.counter > this.counter) {
            return -1;
        }
        return 0;
    }

    /**
     * Convert to a String representation
     *
     * @return String
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuilder sb = new StringBuilder(getName());
        sb.append(is);
        sb.append(counter);

        return sb.toString();
    }
}
