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


/**
 * <description>
 *
 * @author epr
 */
public final class VmInterpretedExceptionHandler extends AbstractExceptionHandler {

    private final char startPC;
    private final char endPC;
    private final char handlerPC;

    /**
     * Create a new instance
     *
     * @param cp
     * @param startPC
     * @param endPC
     * @param handlerPC
     * @param classIndex
     */
    public VmInterpretedExceptionHandler(VmCP cp, int startPC, int endPC, int handlerPC, int classIndex) {
        this(cp.getConstClass(classIndex), startPC, endPC, handlerPC);
    }

    /**
     * Create a new instance
     *
     * @param catchType
     * @param startPC
     * @param endPC
     * @param handlerPC
     */
    public VmInterpretedExceptionHandler(VmConstClass catchType, int startPC, int endPC, int handlerPC) {
        super(catchType);
        this.startPC = (char) startPC;
        this.endPC = (char) endPC;
        this.handlerPC = (char) handlerPC;
    }

    /**
     * Returns the endPC.
     *
     * @return int
     */
    public int getEndPC() {
        return endPC;
    }

    /**
     * Returns the handlerPC.
     *
     * @return int
     */
    public int getHandlerPC() {
        return handlerPC;
    }

    /**
     * Returns the startPC.
     *
     * @return int
     */
    public int getStartPC() {
        return startPC;
    }

    /**
     * Is the given PC between start and end.
     *
     * @param pc
     * @return True if the given pc is between start (inclusive) and end (inclusive), false otherwise
     */
    public boolean isInScope(int pc) {
        return (pc >= startPC) && (pc <= endPC);
    }
}
