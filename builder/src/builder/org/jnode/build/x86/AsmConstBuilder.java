/*
 * $Id$
 */
package org.jnode.build.x86;

import org.jnode.build.AbstractAsmConstBuilder;
import org.jnode.vm.VmArchitecture;
import org.jnode.vm.x86.VmX86Architecture;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class AsmConstBuilder extends AbstractAsmConstBuilder {

	private final VmArchitecture arch = new VmX86Architecture();
	
	protected VmArchitecture getArchitecture() {
		return arch;
	}

}
