/*
 * $Id$
 */
package org.jnode.vm;

import org.jnode.vm.classmgr.VmCompiledCode;
import org.jnode.vm.classmgr.VmMethod;
import org.vmmagic.unboxed.Address;

public final class VmStackFrameEnumerator {

    /** Stack frame reader */
    private final VmStackReader reader;
    /** Address of current stack frame */
    private Address framePtr;
    /** Index in address map of current stack frame method (deals with inlined methods) */
    private int codeIndex;
    /** Compiled code for the current frame */
    private VmCompiledCode cc;
    
    /**
     * Initialize this instance.
     * @param reader
     * @param framePtr
     * @param instrPtr
     */
    public VmStackFrameEnumerator(VmStackReader reader, Address framePtr, Address instrPtr) {
        this.reader = reader;
        reset(framePtr, instrPtr);
    }
    
    /**
     * Initialize this instance.
     * Set the enumerator to enumerate the stack of the current thread.
     * @param reader
     */
    public VmStackFrameEnumerator(VmStackReader reader) {
        this.reader = reader;
        final Address curFrame = VmMagic.getCurrentFrame();
        reset(reader.getPrevious(curFrame), reader.getReturnAddress(curFrame));
    }
    
    /**
     * Reset the enumerator to the given pointers.
     * 
     * @param framePtr
     * @param instrPtr
     */
    public final void reset(Address framePtr, Address instrPtr) {
        this.framePtr = framePtr;
        initializeCodeIndex(instrPtr);
    }
    
    /**
     * Is the current frameptr valid.
     * @return
     */
    public final boolean isValid() {
        return reader.isValid(framePtr);
    }
    
    /**
     * Move to the next stack position.
     */
    public final void next() {
        if (reader.isValid(framePtr)) {
            if (cc != null) {
                final int newIndex = cc.getAddressMap().getCallSiteIndex(codeIndex);
                if (newIndex >= 0) {
                    codeIndex = newIndex;
                    return;
                }
            }
            final Address nextIP = reader.getReturnAddress(framePtr).add(-1);
            this.framePtr = reader.getPrevious(framePtr);
            initializeCodeIndex(nextIP);
        }
    }
    
    /**
     * Gets the method at the current stack position.
     * @return
     */
    public final VmMethod getMethod() {
        VmMethod m = null;
        if (cc != null) {
            m = cc.getAddressMap().getMethodAtIndex(codeIndex);
        }
        if (m == null) {
            m = cc.getMethod();
        }
        if (m == null) {
            m = reader.getMethod(framePtr);
        }
        return m;
    }
    
    /**
     * Gets the method at the current stack position.
     * @return
     */
    public final int getProgramCounter() {
        if (cc != null) {
            return cc.getAddressMap().getProgramCounterAtIndex(codeIndex);
        } else {
            return -1;
        }
    }
    
    /**
     * Gets the line number at the current stack position.
     * @return
     */
    public final int getLineNumber() {
        return getMethod().getBytecode().getLineNr(getProgramCounter());
    }
    
    private void initializeCodeIndex(Address instrPtr) {
        cc = reader.getCompiledCode(framePtr);
        if (cc != null) {
            codeIndex = cc.getAddressMapIndex(instrPtr);
        } else {
            codeIndex = -1;
        }        
    }    
}
