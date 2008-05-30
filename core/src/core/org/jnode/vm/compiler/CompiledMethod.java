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

package org.jnode.vm.compiler;

import org.jnode.assembler.NativeStream;
import org.jnode.vm.Vm;
import org.jnode.vm.classmgr.VmAddressMap;
import org.jnode.vm.classmgr.VmMethod;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class CompiledMethod {

    private NativeStream.ObjectRef codeStart;
    private NativeStream.ObjectRef codeEnd;
    private NativeStream.ObjectRef defExceptionHandler;
    private CompiledExceptionHandler[] exceptionHandlers;
    private final VmAddressMap addressTable;
    private final int optLevel;
    private int ccId = -1;

    /**
     * Initialize this instance
     */
    public CompiledMethod(int optLevel) {
        this.addressTable = new VmAddressMap();
        this.optLevel = optLevel;
    }

    /**
     * @return NativeStream.ObjectRef
     */
    public NativeStream.ObjectRef getCodeEnd() {
        return codeEnd;
    }

    /**
     * @return NativeStream.ObjectRef
     */
    public NativeStream.ObjectRef getCodeStart() {
        return codeStart;
    }

    /**
     * @return CompiledExceptionHandler[]
     */
    public CompiledExceptionHandler[] getExceptionHandlers() {
        return exceptionHandlers;
    }

    /**
     * Sets the codeEnd.
     *
     * @param codeEnd The codeEnd to set
     */
    public void setCodeEnd(NativeStream.ObjectRef codeEnd) {
        this.codeEnd = codeEnd;
    }

    /**
     * Sets the codeStart.
     *
     * @param codeStart The codeStart to set
     */
    public void setCodeStart(NativeStream.ObjectRef codeStart) {
        this.codeStart = codeStart;
    }

    /**
     * Sets the exceptionHandlers.
     *
     * @param exceptionHandlers The exceptionHandlers to set
     */
    public void setExceptionHandlers(CompiledExceptionHandler[] exceptionHandlers) {
        this.exceptionHandlers = exceptionHandlers;
    }

    /**
     * @return NativeStream.ObjectRef
     */
    public NativeStream.ObjectRef getDefExceptionHandler() {
        return defExceptionHandler;
    }

    /**
     * Sets the defExceptionHandler.
     *
     * @param defExceptionHandler The defExceptionHandler to set
     */
    public void setDefExceptionHandler(
        NativeStream.ObjectRef defExceptionHandler) {
        this.defExceptionHandler = defExceptionHandler;
    }

    /**
     * Add an address-pc mapping
     *
     * @param pc
     * @param offset
     */
    public final void add(VmMethod method, int pc, int offset, int inlineDepth) {
        addressTable.add(method, pc, offset, inlineDepth);
    }

    /**
     * Gets the mapping between address and PC
     *
     * @return Address map
     */
    public VmAddressMap getAddressTable() {
        return addressTable;
    }

    /**
     * Gets the optimization level used to create this compiled method.
     *
     * @return Returns the optimization level.
     */
    final int getOptimizationLevel() {
        return this.optLevel;
    }

    /**
     * Gets (creates if needed) a compiled code id.
     *
     * @return
     */
    public final int getCompiledCodeId() {
        if (ccId < 0) {
            ccId = Vm.getCompiledMethods().createId();
        }
        return ccId;
    }
}
