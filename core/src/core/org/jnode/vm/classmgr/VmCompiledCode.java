/*
 * $Id$
 *
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
 
package org.jnode.vm.classmgr;

import org.jnode.util.NumberUtils;
import org.jnode.vm.VmAddress;
import org.jnode.vm.annotation.KernelSpace;
import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.annotation.Uninterruptible;
import org.jnode.vm.compiler.NativeCodeCompiler;
import org.vmmagic.unboxed.Address;

/**
 * @author epr
 */
@MagicPermission
public final class VmCompiledCode extends AbstractCode {

    /**
     * Address of native code of this method
     */
    private final VmAddress nativeCode;

    /**
     * Size in bytes of native code
     */
    private int nativeCodeSize1;

    /**
     * Address of the default exception handler (only for compiled methods)
     */
    private final VmAddress defaultExceptionHandler;

    /**
     * Compiled code of this method
     */
    private final Object compiledCode1;

    /**
     * Exception handler table
     */
    private final VmCompiledExceptionHandler[] eTable;

    /**
     * Mapping between PC's and addresses
     */
    private final VmAddressMap addressTable;

    /**
     * The compiler used to generate this code
     */
    private final NativeCodeCompiler compiler;

    /**
     * Next in linked list
     */
    private VmCompiledCode next;

    /**
     * Unique id of this compiled code
     */
    private final int id;

    /**
     * The method this code is generated for
     */
    private final VmMethod method;

    /**
     * Create a new instance
     *
     * @param bytecode
     * @param nativeCode
     * @param compiledCode
     * @param size
     * @param eTable
     * @param defaultExceptionHandler
     * @param addressTable
     */
    VmCompiledCode(int id, VmMethod method, NativeCodeCompiler compiler, VmByteCode bytecode,
                   VmAddress nativeCode, Object compiledCode, int size,
                   VmCompiledExceptionHandler[] eTable,
                   VmAddress defaultExceptionHandler, VmAddressMap addressTable) {
        this.id = id;
        this.method = method;
        this.compiler = compiler;
        this.nativeCode = nativeCode;
        this.compiledCode1 = compiledCode;
        this.eTable = eTable;
        this.nativeCodeSize1 = size;
        this.defaultExceptionHandler = defaultExceptionHandler;
        this.addressTable = addressTable;
        if (bytecode != null) {
            bytecode.lock();
        }
        if (addressTable != null) {
            addressTable.lock();
            /*
             * if (bytecode != null) { if
             * (bytecode.getMethod().getDeclaringClass().getName().equals(
             * "org.jnode.vm.TryCatchNPETest")) {
             * addressTable.writeTo(System.out); } }
             */
        }
    }

    /**
     * Returns the defaultExceptionHandler.
     *
     * @return Object
     */
    public VmAddress getDefaultExceptionHandler() {
        return defaultExceptionHandler;
    }

    /**
     * Gets the length of the native code in bytes.
     *
     * @return the length
     */
    public int getSize() {
        return nativeCodeSize1;
    }

    /**
     * Get the number of exception handlers
     *
     * @return the number of exception handlers
     */
    public int getNoExceptionHandlers() {
        return (eTable == null) ? 0 : eTable.length;
    }

    /**
     * Get the handler PC of the exception handler at a given index
     *
     * @param index
     * @return The handler
     */
    public VmCompiledExceptionHandler getExceptionHandler(int index) {
        if (eTable != null) {
            return eTable[index];
        } else {
            throw new IndexOutOfBoundsException("eTable is null; index "
                + index);
        }
    }

    /**
     * Gets address map index for the given instruction pointer.
     *
     * @param instrPtr
     * @return The index, or -1 is not found.
     */
    public final int getAddressMapIndex(Address instrPtr) {
        final Address codeAddr = Address.fromAddress(nativeCode);
        final int offset = instrPtr.toWord().sub(codeAddr.toWord()).toInt();
        return addressTable.getIndexForOffset(offset);
    }

    /**
     * Gets the address of the start of the native code.
     *
     * @return The address
     */
    final VmAddress getNativeCode() {
        return nativeCode;
    }

    final Object getCompiledCode() {
        return compiledCode1;
    }

    /**
     * Does this method contain the given address?
     *
     * @param codePtr
     * @return boolean
     */
    public boolean contains(Address codePtr) {
        final Address start = Address.fromAddress(nativeCode);
        final Address end = start.add(nativeCodeSize1);

        return codePtr.GE(start) && codePtr.LT(end);
    }

    public String toString() {
        if (compiledCode1 instanceof byte[]) {
            return NumberUtils.hex((byte[]) compiledCode1);
        } else {
            return super.toString();
        }
    }

    /**
     * Gets the compiler that generated this code.
     *
     * @return Returns the compiler.
     */
    public final NativeCodeCompiler getCompiler() {
        return this.compiler;
    }

    /**
     * @return Returns the next.
     */
    final VmCompiledCode getNext() {
        return this.next;
    }

    /**
     * @param next The next to set.
     */
    final void setNext(VmCompiledCode next) {
        if (this.next != null) {
            throw new SecurityException("Cannot set next twice");
        }
        this.next = next;
    }

    /**
     * Do a lookup of the compiled code that has the given magic value.
     *
     * @param magic
     * @return The compiled code found in the list, or null if not found.
     */
    final VmCompiledCode lookup(int magic) {
        VmCompiledCode c = this;
        while (c != null) {
            if (c.compiler.getMagic() == magic) {
                return c;
            }
            c = c.next;
        }
        return null;
    }

    /**
     * @return Returns the id.
     */
    public final int getId() {
        return id;
    }

    /**
     * @return Returns the method.
     */
    @KernelSpace
    @Uninterruptible
    public final VmMethod getMethod() {
        return method;
    }

    /**
     * @return Returns the addressTable.
     *         Can be null.
     */
    public final VmAddressMap getAddressMap() {
        return addressTable;
    }
}
