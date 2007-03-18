package org.jnode.apps.vmware.disk.descriptor;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 *
 */
public enum AdapterType {
	ide,
	busLogic,
	lsiLogic,
	legacyLogic,
	legacyESX
}
