package org.jnode.vm.facade;


/**
 * An {@link ObjectFilter} that accepts objects whose class name contains
 * the string given by {@link #className}.
 * 
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class SimpleObjectFilter implements ObjectFilter {
    private String[] className = null;

    /**
     * Sets the className filter. Any class whose full name contains a value
     * from <code>classNameFilter</code> array will be accepted. Other classes
     * will be ignored and no statistics will be computed and displayed for
     * them.
     * @param className
     */
    public void setClassName(String[] className) {
        this.className = ((className != null) && (className.length > 0)) ? className : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accept(String className) {
        if (className == null) {
            return true;
        }
        
        for (String f : this.className) {
            if (className.contains(f)) {
                return true;
            }
        }
        
        return false;
    }
}
