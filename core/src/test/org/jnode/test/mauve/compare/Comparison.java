package org.jnode.test.mauve.compare;

import java.util.Set;
import java.util.TreeSet;

import org.jnode.test.mauve.Result;

/**
 * Abstract class for the result of the comparison of 2 {@link Result}s
 * 
 * @author fabien
 *
 * @param <T>
 */
public abstract class Comparison<T extends Result> implements Comparable<Comparison<T>> {
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
        
        for (Comparison<?> r : children) {
            if (r.getName().equals(name)) {
                result = r;
                break;
            }
        }
        
        return result;
    }
    
    public abstract void accept(ComparisonVisitor visitor);
    
    public int getProgression() {
        int progression = 0;
        
        for (Comparison<?> r : children) {
            progression += r.getProgression();
        }
        
        return progression;
    }
    
    public final void add(Comparison<?> child) {
        children.add(child);
    }
    
    @Override
    public final int compareTo(Comparison<T> o) {
        // regressions have negative progression
        // we sort from bigger regression to bigger progression
        int result = getProgression() - o.getProgression();
        
        if (result == 0) {
            result = getName().compareTo(o.getName());
        }
        
        return result;
    }

    protected final void acceptChildren(ComparisonVisitor visitor) {
        for (Comparison<?> cmp : children) {
            cmp.accept(visitor);
        }
    }    
}
