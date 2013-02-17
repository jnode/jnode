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

import java.util.List;

import org.jnode.vm.classmgr.CompiledCodeList;
import org.jnode.vm.classmgr.VmSharedStatics;
import org.jnode.vm.objects.Counter;
import org.jnode.vm.objects.CounterGroup;
import org.jnode.vm.objects.Statistic;

/**
 * Interface with the Virtual Machine.
 *
 * @author Fabien DUMINY (fduminy at jnode.org)
 */
public interface Vm {

    /**
     * @return Returns the statics.
     */
    VmSharedStatics getSharedStatics();

    /**
     * @see org.jnode.vm.objects.Statistics#getStatistics()
     */
    Statistic[] getStatistics();

    /**
     * Gets or creates a counter with a given name.
     *
     * @param name
     * @return The counter
     */
    Counter getCounter(String name);

    /**
     * Gets or creates a counter group with a given name.
     *
     * @param name
     * @return The counter group
     */
    CounterGroup getCounterGroup(String name);

    /**
     * @return Returns the architecture.
     */
    VmArchitecture getArch();

    /**
     * @return Returns the heapManager.
     */
    VmHeapManager getHeapManager();

    /**
     * @return Returns the bootstrap.
     */
    boolean isBootstrap();

    /**
     * Gets the list of compiled methods.
     *
     * @return Returns the compiledMethods.
     */
    CompiledCodeList getCompiledMethods();

    /**
     * Gets the version of the current VM.
     *
     * @return Returns the version.
     */
    String getVersion();

    /**
     * Does this VM run in debug mode.
     *
     * @return Returns the debugMode.
     */
    boolean isDebugMode();

    /**
     * Returns the number of available processors currently available to the
     * virtual machine. This number may change over time; so a multi-processor
     * program want to poll this to determine maximal resource usage.
     *
     * @return the number of processors available, at least 1
     */
    int availableProcessors();

    /**
     * @return a copy of the processors list
     */
    List<VmProcessor> getProcessors();

    /**
     * @param vmThreadVisitor
     */
    void accept(VmThreadVisitor vmThreadVisitor);
}
