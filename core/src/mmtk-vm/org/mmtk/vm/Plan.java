/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 
package org.mmtk.vm;

import org.jnode.vm.annotation.Inline;
import org.jnode.vm.memmgr.HeapHelper;
import org.mmtk.plan.NoGC;
import org.vmmagic.pragma.Uninterruptible;

/**
 * Stub implementation of Plan.
 * 
 * The build process will replace this class with 
 * the actual implementation.
 */
public final class Plan extends NoGC implements Uninterruptible {
    
    /**
     * Gets the plan instance associated with the current processor.
     * 
     * @return the plan instance for the current processor
     */
    @Inline
    public static Plan getInstance() 
    {
        return null;
    }

    /**
     * @return Returns the heapHelper.
     */
    public final HeapHelper getHeapHelper() {
        return null;
    }    
}
