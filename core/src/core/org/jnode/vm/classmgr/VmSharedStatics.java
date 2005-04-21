/*
 * $Id$
 */
package org.jnode.vm.classmgr;

import org.jnode.assembler.ObjectResolver;
import org.jnode.vm.VmArchitecture;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VmSharedStatics extends VmStatics {

    private static final int SIZE = 1 << 16;

    /**
     * @param arch
     * @param resolver
     */
    public VmSharedStatics(VmArchitecture arch, ObjectResolver resolver) {
        super(arch, resolver, SIZE);
    }

}
