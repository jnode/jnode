/*
 * $Id$
 */
package org.jnode.vm.classmgr;

import org.jnode.util.NumberUtils;
import org.jnode.vm.VmAddress;
import org.jnode.vm.compiler.NativeCodeCompiler;
import org.vmmagic.unboxed.Address;

/**
 * @author epr
 */
public final class VmCompiledCode extends AbstractCode {

    /** Address of native code of this method */
    private final VmAddress nativeCode;

    /** Size in bytes of native code */
    private int nativeCodeSize1;

    /** Address of the default exception handler (only for compiled methods) */
    private final VmAddress defaultExceptionHandler;

    /** Compiled code of this method */
    private final Object compiledCode1;

    /** Exception handler table */
    private final VmCompiledExceptionHandler[] eTable;

    /** Mapping between PC's and addresses */
    private final VmAddressMap addressTable;

    /** The compiler used to generate this code */
    private final NativeCodeCompiler compiler;

    /** Next in linked list */
    private VmCompiledCode next;

    /** Magic of compiler */
    private final int magic;

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
    public VmCompiledCode(NativeCodeCompiler compiler, VmByteCode bytecode,
            VmAddress nativeCode, Object compiledCode, int size,
            VmCompiledExceptionHandler[] eTable,
            VmAddress defaultExceptionHandler, VmAddressMap addressTable) {
        this.compiler = compiler;
        this.magic = compiler.getMagic();
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
            /*if (bytecode != null) {
                if (bytecode.getMethod().getDeclaringClass().getName().equals(
                        "org.jnode.vm.TryCatchNPETest")) {
                    addressTable.writeTo(System.out);
                }
            }*/
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
            return eTable[ index];
        } else {
            throw new IndexOutOfBoundsException("eTable is null; index "
                    + index);
        }
    }

    /**
     * Gets the linenumber and optional method info (for inlined methods) of a given address.
     * 
     * @param address
     * @return The linenumber for the given pc, or -1 is not found.
     */
    public String getLocationInfo(VmMethod expectedMethod, Address address) {
    	final Address codeAddr = Address.fromAddress(nativeCode);
        final int offset = (int) address.toWord().sub(codeAddr.toWord()).toInt();
        return addressTable.getLocationInfo(expectedMethod, offset);
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
     * @param next
     *            The next to set.
     */
    final void setNext(VmCompiledCode next) {
        if (this.next != null) { throw new SecurityException(
                "Cannot set next twice"); }
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
            if (c.magic == magic) { return c; }
            c = c.next;
        }
        return null;
    }
}