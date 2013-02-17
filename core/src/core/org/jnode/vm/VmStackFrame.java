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
 
package org.jnode.vm;

import org.jnode.vm.classmgr.VmByteCode;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.objects.VmSystemObject;

/**
 * A VmFrame is the execution frame (locals & stack) for a method during
 * execution. Note that this class is not meant to be a normal java class,
 * instead it is a record structure that maps directly on how a method frame is
 * push on the stack by the compiler. Don't add any methods, since during
 * runtime instances of this class will have no header and thus no virtual
 * method table.
 */
public final class VmStackFrame extends VmSystemObject {

    public static final int MAGIC_MASK = 0xFFFFFF00;
    public static final int MAGIC_COMPILED = 0x21A52F00;

    /**
     * The method executing in this frame
     */
    private final VmMethod sfMethod;
    private final int programCounter;

    /**
     * Initialize this instance.
     *
     * @param src
     * @param reader
     */
    VmStackFrame(VmMethod method, int programCounter) {
        this.sfMethod = method;
        this.programCounter = programCounter;
    }

    /**
     * @return Returns the method.
     */
    public final VmMethod getMethod() {
        return this.sfMethod;
    }

    /**
     * Gets the line number of the current instruction of this frame.
     *
     * @return The line number, or -1 if not found.
     */
    public final String getLocationInfo() {
        int lineNo = -1;
        if (sfMethod != null) {
            final VmByteCode bc = sfMethod.getBytecode();
            if (bc != null) {
                lineNo = bc.getLineNr(programCounter);
            }
        }
        if (lineNo >= 0) {
            return String.valueOf(lineNo);
        } else {
            return "?";
        }
    }

    /**
     * Convert to a String representation.
     *
     * @see java.lang.Object#toString()
     */
    public String toString() {
        final VmMethod method = sfMethod;
        final VmType<?> vmClass = (method == null) ? null : method.getDeclaringClass();
        final String cname = (vmClass == null) ? "<unknown class>" : vmClass.getName();
        final String mname = (method == null) ? "<unknown method>" : method.getName();
        final String location = getLocationInfo();

        return cname + '!' + mname + " (" + location + ')';
    }
}
