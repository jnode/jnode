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
 
package org.jnode.vm.memmgr.def;

import org.jnode.vm.ObjectVisitor;
import org.jnode.vm.classmgr.VmNormalClass;
import org.jnode.vm.classmgr.VmType;
import org.vmmagic.pragma.Uninterruptible;

/**
 * @author Martin Husted Hartvig (hagar@jnode.org)
 */
final class HeapStatisticsVisitor extends ObjectVisitor implements
    Uninterruptible {

    private final DefHeapStatistics heapStatistics;

    public HeapStatisticsVisitor(DefHeapStatistics heapStatistics) {
        this.heapStatistics = heapStatistics;
    }

    /**
     * Count the visited object.
     *
     * @param object
     * @return boolean
     */

    public final boolean visit(Object object) {
        int size = 0;
        if (!heapStatistics.contains(object.getClass().getName())) {
            final VmType<?> type = VmType.fromClass(object.getClass());
            size = (type instanceof VmNormalClass ? ((VmNormalClass<?>) type)
                .getObjectSize() : 0);
        }

        heapStatistics.add(object.getClass().getName(), size);

        return true;
    }
}
