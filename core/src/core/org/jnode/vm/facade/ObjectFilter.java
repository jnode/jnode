package org.jnode.vm.facade;

/**
 * This interface is used to filter objects founds on heap.
 * 
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public interface ObjectFilter {
    /**
     * Does this filter accept the provided class ?
     * @param className The class of the object.
     * @return true if the filter accept the provided class.
     */
    boolean accept(String className);
}
