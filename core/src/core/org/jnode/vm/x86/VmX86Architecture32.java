/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
package org.jnode.vm.x86;

import org.jnode.vm.VmProcessor;
import org.jnode.vm.classmgr.VmStatics;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class VmX86Architecture32 extends VmX86Architecture {

    /** Size of an object reference */
    public static final int SLOT_SIZE = 4;

    /**
     * 
     */
    public VmX86Architecture32() {
        super();
    }

    /**
     * @param compiler
     */
    public VmX86Architecture32(String compiler) {
        super(compiler);
    }

    /**
     * Gets the name of this architecture.
     * 
     * @return name
     */
    public final String getName() {
        return "x86";
    }

    /**
     * Gets the size in bytes of an object reference.
     * 
     * @return Size of reference, always 4 here
     */
    public final int getReferenceSize() {
        return SLOT_SIZE;
    }

    /**
     * Create a processor instance for this architecture.
     * 
     * @return The processor
     */
    public VmProcessor createProcessor(int id, VmStatics statics) {
        return new VmX86Processor32(id, this, statics, null);
    }

}
