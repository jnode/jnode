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
 
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.Label;
import org.jnode.vm.bytecode.TypeStack;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.x86.compiler.X86CompilerHelper;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class InlinedMethodInfo {

    private final InlinedMethodInfo previous;

    private final VmMethod inlinedMethod;

    private TypeStack exitStack;

    private final Label endOfInlineLabel;

    private TypeStack outerMethodStack;

    private final String previousLabelPrefix;

    /**
     * Initialize this instance.
     *
     * @param inlinedMethod
     */
    public InlinedMethodInfo(InlinedMethodInfo previous, VmMethod inlinedMethod, Label endOfInlineLabel,
                             String previousLabelPrefix) {
        this.previous = previous;
        this.inlinedMethod = inlinedMethod;
        this.endOfInlineLabel = endOfInlineLabel;
        this.previousLabelPrefix = previousLabelPrefix;
    }

    final void setExitStack(VirtualStack vstack) {
        this.exitStack = vstack.asTypeStack();
    }

    /**
     * @return Returns the endOfInlineLabel.
     */
    final Label getEndOfInlineLabel() {
        return endOfInlineLabel;
    }

    /**
     * Push the stack elements of the outer method stack.
     *
     * @param vstack
     */
    final void pushOuterMethodStack(ItemFactory ifac, VirtualStack vstack) {
        vstack.pushAll(ifac, outerMethodStack);
    }

    /**
     * Push the stack elements of the outer method stack and the exit stack.
     *
     * @param vstack
     */
    final void pushExitStack(ItemFactory ifac, VirtualStack vstack) {
        vstack.reset();
        //vstack.pushAll(outerMethodStack);
        vstack.pushAll(ifac, exitStack);
    }

    /**
     * @param outerMethodStack The outerMethodStack to set.
     */
    final void setOuterMethodStack(TypeStack outerMethodStack) {
        this.outerMethodStack = outerMethodStack;
    }

    /**
     * Push the return value of the inlined method on the current
     * vstack.
     *
     * @param helper
     */
    final void pushReturnValue(X86CompilerHelper helper) {
        helper.pushReturnValue(inlinedMethod.getSignature());
    }

    /**
     * @return Returns the previous.
     */
    final InlinedMethodInfo getPrevious() {
        return previous;
    }

    /**
     * @return Returns the labelPrefix.
     */
    final String getPreviousLabelPrefix() {
        return previousLabelPrefix;
    }
}
