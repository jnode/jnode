/*
 * $Id$
 */
package org.jnode.vm.memmgr;

import org.jnode.vm.VmSystemObject;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class GCStatistics extends VmSystemObject {
	
	long lastGCTime;
	long lastMarkDuration;
	long lastSweepDuration;
	long lastFreedBytes;
	long lastMarkedObjects;

	public String toString() {
		return "lastGCTime        " + lastGCTime + "\n" +
			   "lastMarkDuration  " + lastMarkDuration + "\n" +
			   "lastSweepDuration " + lastSweepDuration + "\n" +
			   "lastMarkedObjects " + lastMarkedObjects + "\n" +
			   "lastFreedBytes    " + lastFreedBytes;
	}
	
}
