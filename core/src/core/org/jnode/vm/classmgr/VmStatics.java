/*
 * $Id$
 */
package org.jnode.vm.classmgr;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class VmStatics {

	static int staticFieldCount;
	
	public static final void dumpStatistics() {
		System.out.println("#static fields " + staticFieldCount);
	}
	
	public static void main(String[] args) {
		dumpStatistics();
	}
}
