/*
 * $Id$
 */
package org.jnode.util;

import java.util.Map;
import java.util.TreeMap;

public class CounterGroup extends Statistic {

    /** All statistics */
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
     * @see org.jnode.util.Statistic#getValue()
     */
    @Override
    public Object getValue() {
        return statistics;
    }

    /**
     * @see org.jnode.util.Statistic#toString()
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
        sb.append("}");
        return sb.toString();
    }

    /**
     * Gets of create a counter with a given name.
     * @param name
     * @return The counter
     */
    public final Counter getCounter(String name) {
        Counter cnt = (Counter)getStatistic(name);
        if (cnt == null) {
            synchronized (this) {
                cnt = (Counter)getStatistic(name);
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
     * @param name
     * @return The counter group
     */
    public final CounterGroup getCounterGroup(String name) {
        CounterGroup cnt = (CounterGroup)getStatistic(name);
        if (cnt == null) {
            synchronized (this) {
                cnt = (CounterGroup)getStatistic(name);
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
