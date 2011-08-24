package org.jnode.vm.facade;

/**
 * An {@link ObjectFilter} that accepts all objects.
 * Call {@link NoObjectFilter#INSTANCE} to get the singleton.  
 * 
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class NoObjectFilter implements ObjectFilter {
    public static final NoObjectFilter INSTANCE = new NoObjectFilter();
    
    private NoObjectFilter() {        
    }
    
    /**
     * {@inheritDoc}
     * <br>This implementation always returns true.
     */
    @Override
    public final boolean accept(String className) {
        return true;
    }
}
