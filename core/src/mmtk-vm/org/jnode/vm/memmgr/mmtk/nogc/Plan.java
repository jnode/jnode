/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.vm.memmgr.mmtk.nogc;

import org.jnode.vm.annotation.Inline;
import org.jnode.vm.memmgr.HeapHelper;
import org.jnode.vm.scheduler.VmProcessor;
import org.mmtk.plan.NoGC;
import org.vmmagic.pragma.Uninterruptible;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class Plan extends NoGC implements Uninterruptible {

    /**
     * <code>true</code> if built with GCSpy
     */
    public static final boolean WITH_GCSPY = false;

    /** The heap helper */
    private final HeapHelper heapHelper;

    /**
     * Initialize this instance.
     * 
     * @param helper
     */
    public Plan(HeapHelper helper) {
        this.heapHelper = helper;
    }

    /**
     * Gets the plan instance associated with the current processor.
     * 
     * @return the plan instance for the current processor
     */
    @Inline
    public static Plan getInstance() {
        return (Plan) VmProcessor.current().getHeapData();
    }

    /**
     * @return Returns the heapHelper.
     */
    @Inline
    public final HeapHelper getHeapHelper() {
        return heapHelper;
    }
}
