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
 
package org.jnode.vm.compiler;

import org.jnode.vm.bytecode.BasicBlock;
import org.jnode.vm.bytecode.BytecodeVisitor;
import org.jnode.vm.classmgr.VmType;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class CompilerBytecodeVisitor extends BytecodeVisitor {

    /**
     * The given basic block is about to start.
     */
    public abstract void startBasicBlock(BasicBlock bb);

    /**
     * The started basic block has finished.
     */
    public abstract void endBasicBlock();

    /**
     * A try block is about to start
     */
    public abstract void startTryBlock();

    /**
     * A try block has finished
     */
    public abstract void endTryBlock();

    /**
     * Emit a yieldpoint.
     */
    public abstract void yieldPoint();

    /**
     * Push the given VmType on the stack.
     */
    public abstract void visit_ldc(VmType<?> value);

    /**
     * Load an array from a given local index that is just stored
     * at the same index.
     *
     * @param index
     */
    public void visit_aloadStored(int index) {
        visit_aload(index);
    }

    /**
     * Load an int from a given local index that is just stored
     * at the same index.
     *
     * @param index
     */
    public void visit_iloadStored(int index) {
        visit_iload(index);
    }

    /**
     * Load a long from a given local index that is just stored
     * at the same index.
     *
     * @param index
     */
    public void visit_lloadStored(int index) {
        visit_lload(index);
    }

    /**
     * Load a float from a given local index that is just stored
     * at the same index.
     *
     * @param index
     */
    public void visit_floadStored(int index) {
        visit_fload(index);
    }

    /**
     * Load a double from a given local index that is just stored
     * at the same index.
     *
     * @param index
     */
    public void visit_dloadStored(int index) {
        visit_dload(index);
    }
}
