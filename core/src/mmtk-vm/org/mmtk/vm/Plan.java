/*
 *  This file is part of the Jikes RVM project (http://jikesrvm.org).
 *
 *  This file is licensed to You under the Common Public License (CPL);
 *  You may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/cpl1.0.php
 *
 *  See the COPYRIGHT.txt file distributed with this work for information
 *  regarding copyright ownership.
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
