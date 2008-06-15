package org.jnode.apps.vmware.disk.handler.sparse;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 * 
 */
public class GrainTable extends EntryArray {
    public GrainTable(int[] entries) {
        super(entries);
    }
}
