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
 
package org.jnode.vm;

import java.nio.ByteOrder;

import org.jnode.system.ResourceManager;
import org.jnode.vm.classmgr.TypeSizeInfo;
import org.jnode.vm.classmgr.VmStatics;
import org.jnode.vm.compiler.IMTCompiler;
import org.jnode.vm.compiler.NativeCodeCompiler;

/**
 * Class describing a specific system architecture.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class VmArchitecture extends VmSystemObject {

	/**
	 * Gets the name of this architecture.
	 * This name is the programmers name used to identify packages,
	 * class name extensions etc.
	 * 
	 * @return Name
	 */
	public abstract String getName();

	/**
	 * Gets the full name of this architecture, including operating mode.
	 * 
	 * @return Name
	 */
	public abstract String getFullName();

	/**
	 * Gets the byte ordering of this architecture.
	 * @return ByteOrder
	 */
	public abstract ByteOrder getByteOrder();
	
	/**
	 * Gets the size in bytes of an object reference.
	 * 
	 * @return Reference size
	 */
	public abstract int getReferenceSize();
    
    /**
     * Gets the type size information of this architecture.
     * @return
     */
    public abstract TypeSizeInfo getTypeSizeInfo();

	/**
	 * Gets the stackreader for this architecture.
	 * 
	 * @return Stack reader
	 */
	public abstract VmStackReader getStackReader();

	/**
	 * Gets all compilers for this architecture.
	 * 
	 * @return The compilers, sorted by optimization level, from least optimizations to most
	 *         optimizations.
	 */
	public abstract NativeCodeCompiler[] getCompilers();

	/**
	 * Gets all test compilers for this architecture.
	 * This can be used to test new compilers in a running system.
	 * 
	 * @return The compilers, sorted by optimization level, from least optimizations to most
	 *         optimizations, or null for no test compilers.
	 */
	public abstract NativeCodeCompiler[] getTestCompilers();

	/**
	 * Gets the compiler of IMT's.
	 * @return
	 */
	public abstract IMTCompiler getIMTCompiler();
	
	/**
	 * Find and start all processors in the system.
	 * All all discovered processors to the given list.
	 * The bootstrap processor is already on the given list.
	 */
	protected abstract void initializeProcessors(ResourceManager rm);
	
	/**
	 * Call this method to register a processor found in {@link #initializeProcessors(ResourceManager)}.
	 * @param cpu
	 */
	protected final void addProcessor(VmProcessor cpu) {
	    Vm.getVm().addProcessor(cpu);
	}

	/**
	 * Create a processor instance for this architecture.
	 * 
	 * @return The processor
	 */
	protected abstract VmProcessor createProcessor(int id, VmStatics statics);

}
