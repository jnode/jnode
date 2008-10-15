package org.jnode.test.mauve.compare;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.jnode.test.mauve.Result;

/**
 * 
 * @author fabien
 *
 * @param <T>
 */
public class Comparison<T extends Result> implements Comparable<Comparison<T>>, Iterable<Comparison<?>> {
    private final String name;
    private final Set<Comparison<?>> children = new TreeSet<Comparison<?>>();
    
    Comparison(T result) {
        this.name = result.getName();
    }
    
    public final String getName() {
        return name;
    }
    
    public final Comparison<?> get(String name) {
        Comparison<?> result = null;
        
        for (Comparison<?> r : this) {
            if (r.getName().equals(name)) {
                result = r;
                break;
            }
        }
        
        return result;
    }
    
    public int getProgression() {
        int progression = 0;
        
        for (Comparison<?> r : this) {
            progression += r.getProgression();
        }
        
        return progression;
    }
    
    public final void add(Comparison<?> child) {
        children.add(child);
    }
    
    @Override
    public int compareTo(Comparison<T> o) {
        return name.compareTo(o.getName());
    }

    @Override
    public final Iterator<Comparison<?>> iterator() {
        return children.iterator();
    }
    
}
