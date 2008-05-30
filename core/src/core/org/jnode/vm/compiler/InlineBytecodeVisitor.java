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

import org.jnode.vm.classmgr.VmMethod;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class InlineBytecodeVisitor extends CompilerBytecodeVisitor {

    /**
     * An inlined method header will follow next.
     *
     * @param inlinedMethod
     * @param newMaxLocals  The new maxlocals count.
     */
    public abstract void startInlinedMethodHeader(VmMethod inlinedMethod, int newMaxLocals);

    /**
     * An inlined method code will follow next.
     *
     * @param inlinedMethod
     * @param newMaxLocals  The new maxlocals count.
     */
    public abstract void startInlinedMethodCode(VmMethod inlinedMethod, int newMaxLocals);

    /**
     * An inlined method has ended.
     *
     * @param previousMethod The method we're continuing with
     */
    public abstract void endInlinedMethod(VmMethod previousMethod);

    /**
     * Leave the values on the stack and jump to the end of the inlined method.
     */
    public abstract void visit_inlinedReturn(int jvmType);
}
