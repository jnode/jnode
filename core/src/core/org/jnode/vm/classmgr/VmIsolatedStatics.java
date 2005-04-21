/*
 * $Id$
 */
package org.jnode.vm.classmgr;

import org.jnode.assembler.ObjectResolver;
import org.jnode.vm.VmArchitecture;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VmIsolatedStatics extends VmStatics {

    /** Size of the statics table */
    private static final int SIZE = 1 << 15;

    /**
     * @param arch
     * @param resolver
     */
    public VmIsolatedStatics(VmArchitecture arch, ObjectResolver resolver) {
        super(arch, resolver, SIZE);
    }
}
