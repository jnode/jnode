/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.vm.facade;

/**
 * Interface with a VM's thread.
 *
 * @author Fabien DUMINY (fduminy at jnode.org)
 */
public interface VmThread {

    /**
     * Visit all objects on the stack and register state of this thread.
     *
     * @param visitor
     * @param heapManager
     * @return {@code true} if the last visit returned {@code true} or no visit was
     *         made, {@code false} otherwise.
     */
    boolean accept(ObjectVisitor visitor, VmHeapManager heapManager);

    /**
     * Get the Thread to which this VmThread belongs.
     *
     * @return The java thread
     */
    Thread asThread();

}
