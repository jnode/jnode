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

package org.mmtk.vm;

import org.jnode.vm.Unsafe;
import org.jnode.vm.memmgr.HeapHelper;
import org.mmtk.plan.NoGC;
import org.vmmagic.pragma.Uninterruptible;

/**
 * $Id$
 * 
 * @author <a href="http://cs.anu.edu.au/~Steve.Blackburn">Steve Blackburn</a>
 * @author Perry Cheng
 * @author <a href="http://www.cs.ukc.ac.uk/~rej">Richard Jones</a>
 * @version $Revision$
 * @date $Date$
 */
public class Plan extends NoGC implements Uninterruptible {

    /**
     * <code>true</code> if built with GCSpy
     */
    public static final boolean WITH_GCSPY = false;

    /** The heap helper */
    private final HeapHelper heapHelper;
    
    /**
     * Initialize this instance.
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
    public static Plan getInstance() {
        return (Plan)Unsafe.getCurrentProcessor().getHeapData();
    }

    /**
     * @return Returns the heapHelper.
     */
    public final HeapHelper getHeapHelper() {
        return heapHelper;
    }    
}
