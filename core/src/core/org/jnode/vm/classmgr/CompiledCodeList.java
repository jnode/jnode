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
 
package org.jnode.vm.classmgr;

import org.jnode.vm.VmAddress;
import org.jnode.annotation.KernelSpace;
import org.jnode.annotation.Uninterruptible;
import org.jnode.vm.compiler.CompiledMethod;
import org.jnode.vm.compiler.NativeCodeCompiler;
import org.jnode.vm.objects.VmSystemObject;

/**
 * List of compiled methods.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class CompiledCodeList extends VmSystemObject {

    private VmCompiledCode[] list = new VmCompiledCode[16000];

    private int nextId = 1; // Leave entry 0 open

    /**
     * Create a new VmCompiledCode for a compiled method.  The result will be added 
     * to the internal list of compiled methods.
     * 
     * @param cm an existing CompilerMethod or {@code null}.
     * @param method the system method descriptor
     * @param compiler the compiler used to compile the method
     * @param bytecode the method's bytecodes
     * @param nativeCode the native code address
     * @param compiledCode
     * @param size
     * @param eTable
     * @param defaultExceptionHandler
     * @param addressTable
     * @return the resulting VmCompiledCode object
     */
    public synchronized VmCompiledCode createCompiledCode(CompiledMethod cm,
                                                          VmMethod method, NativeCodeCompiler compiler,
                                                          VmByteCode bytecode,
                                                          VmAddress nativeCode, Object compiledCode, int size,
                                                          VmCompiledExceptionHandler[] eTable,
                                                          VmAddress defaultExceptionHandler,
                                                          VmAddressMap addressTable) {
        final int ccid;
        if (cm != null) {
            ccid = cm.getCompiledCodeId();
        } else {
            ccid = createId();
        }
        final VmCompiledCode cc = new VmCompiledCode(ccid, method, compiler, bytecode,
            nativeCode, compiledCode, size, eTable,
            defaultExceptionHandler, addressTable);
        list[ccid] = cc;
        return cc;
    }

    /**
     * Allocate an id (actually an index for the compiler code list) for a new method.
     * If necessary the code list is grown to accomadate the method.
     *
     * @return the method id
     */
    public synchronized int createId() {
        final int cmid = nextId++;
        if (cmid >= list.length) {
            growArray(cmid * 2);
        }
        return cmid;
    }

    /**
     * Gets a compiled method with a given id.
     *
     * @param cmid
     * @return the compiled method or {@code null}.
     */
    @KernelSpace
    @Uninterruptible
    public final VmCompiledCode get(int cmid) {
        if ((cmid >= 0) && (cmid < list.length)) {
            return list[cmid];
        } else {
            return null;
        }
    }

    /**
     * Gets the number of method in the list.
     *
     * @return the number of methods.
     */
    public final int size() {
        return nextId;
    }

    private final void growArray(int newLength) {
        final VmCompiledCode[] newList = new VmCompiledCode[newLength];
        System.arraycopy(this.list, 0, newList, 0, list.length);
        this.list = newList;
    }

}
