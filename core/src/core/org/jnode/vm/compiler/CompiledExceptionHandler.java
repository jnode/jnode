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

import java.io.PrintStream;
import org.jnode.assembler.NativeStream;
import org.jnode.assembler.UnresolvedObjectRefException;
import org.jnode.util.NumberUtils;

/**
 * @author epr
 */
public class CompiledExceptionHandler {

    private NativeStream.ObjectRef startPc;
    private NativeStream.ObjectRef endPc;
    private NativeStream.ObjectRef handler;

    /**
     * @return NativeStream.ObjectRef
     */
    public NativeStream.ObjectRef getEndPc() {
        return endPc;
    }

    /**
     * @return NativeStream.ObjectRef
     */
    public NativeStream.ObjectRef getHandler() {
        return handler;
    }

    /**
     * @return NativeStream.ObjectRef
     */
    public NativeStream.ObjectRef getStartPc() {
        return startPc;
    }

    /**
     * Sets the endPc.
     *
     * @param endPc The endPcO to set
     */
    public void setEndPc(NativeStream.ObjectRef endPc) {
        this.endPc = endPc;
    }

    /**
     * Sets the handler.
     *
     * @param handler The handler to set
     */
    public void setHandler(NativeStream.ObjectRef handler) {
        this.handler = handler;
    }

    /**
     * Sets the startPc.
     *
     * @param startPc The startPc to set
     */
    public void setStartPc(NativeStream.ObjectRef startPc) {
        this.startPc = startPc;
    }

    public void writeTo(PrintStream out, int startOffset)
        throws UnresolvedObjectRefException {
        out.println("start:   0x" + NumberUtils.hex(startPc.getOffset() - startOffset));
        out.println("end:     0x" + NumberUtils.hex(endPc.getOffset() - startOffset));
        out.println("handler: 0x" + NumberUtils.hex(handler.getOffset() - startOffset));
    }
}
