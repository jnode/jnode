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

import java.util.Map;
import java.util.TreeMap;


public class CounterGroup extends Statistic {

    /**
     * All statistics
     */
    private transient Map<String, Statistic> statistics;

    /**
     * @param name
     * @param description
     */
    public CounterGroup(String name, String description) {
        super(name, description);
    }

    /**
     * @param name
     */
    public CounterGroup(String name) {
        super(name);
    }

    /**
     * @see org.jnode.vm.objects.Statistic#getValue()
     */
    @Override
    public Object getValue() {
        return statistics;
    }

    /**
     * @see org.jnode.vm.objects.Statistic#toString()
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getName());
        sb.append(" {");
        if (statistics != null) {
            for (Statistic s : statistics.values()) {
                sb.append('\n');
                sb.append(s);
            }
        }
        sb.append('}');
        return sb.toString();
    }

    /**
     * Gets of create a counter with a given name.
     *
     * @param name
     * @return The counter
     */
    public final Counter getCounter(String name) {
        Counter cnt = (Counter) getStatistic(name);
        if (cnt == null) {
            synchronized (this) {
                cnt = (Counter) getStatistic(name);
                if (cnt == null) {
                    cnt = new Counter(name, name);
                    addStatistic(name, cnt);
                }
            }
        }
        return cnt;
    }

    /**
     * Gets of create a counter group with a given name.
     *
     * @param name
     * @return The counter group
     */
    public final CounterGroup getCounterGroup(String name) {
        CounterGroup cnt = (CounterGroup) getStatistic(name);
        if (cnt == null) {
            synchronized (this) {
                cnt = (CounterGroup) getStatistic(name);
                if (cnt == null) {
                    cnt = new CounterGroup(name, name);
                    addStatistic(name, cnt);
                }
            }
        }
        return cnt;
    }

    private Statistic getStatistic(String name) {
        if (statistics != null) {
            return statistics.get(name);
        } else {
            return null;
        }
    }

    private void addStatistic(String name, Statistic stat) {
        if (statistics == null) {
            statistics = new TreeMap<String, Statistic>();
        }
        statistics.put(name, stat);
    }

}
