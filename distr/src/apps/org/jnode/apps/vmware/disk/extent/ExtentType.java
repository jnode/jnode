package org.jnode.apps.vmware.disk.extent;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 * 
 */
public enum ExtentType {
    FLAT, SPARSE, ZERO, VMFS, VMFSSPARSE, VMFSRDM, VMFSRAW
}
