/*
 * $Id$
 *
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
 
package org.jnode.vm.compiler.ir.quad;

import org.jnode.vm.compiler.ir.IRBasicBlock;

/**
 * @author Madhu Siddalingaiah
 */
public abstract class BranchQuad<T> extends Quad<T> {
    private IRBasicBlock<T> targetBlock;

    /**
     * @param address
     */
    public BranchQuad(int address, IRBasicBlock<T> block, int targetAddress) {
        super(address, block);
        for (IRBasicBlock<T> succ : block.getSuccessors()) {
            if (succ.getStartPC() == targetAddress) {
                targetBlock = succ;
                break;
            }
        }
        if (targetBlock == null) {
            throw new AssertionError("unable to find target block!");
        }
    }

    /**
     * @return the start address of the target block
     */
    public int getTargetAddress() {
        return targetBlock.getStartPC();
    }

    /**
     * @return the target block
     */
    public IRBasicBlock<T> getTargetBlock() {
        return targetBlock;
    }
}
