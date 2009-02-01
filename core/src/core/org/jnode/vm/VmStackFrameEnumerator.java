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
 
package org.jnode.vm;

import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.classmgr.VmCompiledCode;
import org.jnode.vm.classmgr.VmMethod;
import org.vmmagic.unboxed.Address;

@MagicPermission
final class VmStackFrameEnumerator {

    /**
     * Stack frame reader
     */
    private final VmStackReader reader;
    /**
     * Address of current stack frame
     */
    private Address framePtr;
    /**
     * Index in address map of current stack frame method (deals with inlined methods)
     */
    private int codeIndex;
    /**
     * Compiled code for the current frame
     */
    private VmCompiledCode cc;

    /**
     * Initialize this instance.
     *
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
     *
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
     *
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
     *
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
     *
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
     *
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
