/*
 * $Id$
 */
package org.jnode.vm.classmgr;

import org.jnode.vm.VmAddress;
import org.jnode.vm.VmSystemObject;
import org.jnode.vm.compiler.CompiledMethod;
import org.jnode.vm.compiler.NativeCodeCompiler;

/**
 * List of compiled methods.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class CompiledCodeList extends VmSystemObject {

    private VmCompiledCode[] list = new VmCompiledCode[16000];

    private int nextId = 1; // Leave entry 0 open

    /**
     * Create a new compiled method which is added to the internal list of
     * compiled methods.
     * 
     * @param optLevel
     * @param magic
     * @return
     */
    public synchronized VmCompiledCode createCompiledCode(CompiledMethod cm,
            VmMethod method, NativeCodeCompiler compiler, VmByteCode bytecode,
            VmAddress nativeCode, Object compiledCode, int size,
            VmCompiledExceptionHandler[] eTable,
            VmAddress defaultExceptionHandler, VmAddressMap addressTable) {
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
     * Create a new compiled method which is added to the internal list of
     * compiled methods.
     * 
     * @param optLevel
     * @param magic
     * @return
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
     * @return
     */
    public final VmCompiledCode get(int cmid) {
        if ((cmid >= 0) && (cmid < list.length)) {
            return list[cmid];
        } else {
            return null;
        }
    }
    
    /**
     * Gets the number of method in the list.
     * @return
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
