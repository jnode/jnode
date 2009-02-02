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
 
package org.jnode.vm.bytecode;

import org.jnode.util.BootableArrayList;
import org.jnode.vm.VmSystemObject;
import java.util.Set;
import java.util.HashSet;

/**
 * A Basic block of instructions.
 * <p/>
 * A basic block has 0-n predecessors and 0-m successors. The only exit
 * of a basic block is the last instruction.
 *
 * @author epr
 * @author Madhu Siddalingaiah
 */
public class BasicBlock extends VmSystemObject {

    private final int startPC;
    private int endPC;
    private boolean startOfExceptionHandler;
    private TypeStack startStack;
    private BootableArrayList<BasicBlock> entryBlocks = new BootableArrayList<BasicBlock>();

    /**
     * Create a new instance
     *
     * @param startPC                 The first bytecode address of this block
     * @param endPC                   The first bytecode address after this block
     * @param startOfExceptionHandler
     */
    public BasicBlock(int startPC, int endPC, boolean startOfExceptionHandler) {
        this.startPC = startPC;
        this.endPC = endPC;
        this.startOfExceptionHandler = startOfExceptionHandler;
    }

    /**
     * Create a new instance
     *
     * @param startPC The first bytecode address of this block
     */
    public BasicBlock(int startPC) {
        this(startPC, -1, false);
    }

    /**
     * Gets the first bytecode address after this basic block
     *
     * @return The end pc
     */
    public final int getEndPC() {
        return endPC;
    }

    /**
     * @param endPC The endPC to set.
     */
    public final void setEndPC(int endPC) {
        this.endPC = endPC;
    }

    /**
     * @param startOfExceptionHandler The startOfExceptionHandler to set.
     */
    public final void setStartOfExceptionHandler(boolean startOfExceptionHandler) {
        this.startOfExceptionHandler = startOfExceptionHandler;
    }

    /**
     * Gets the first bytecode address of this basic block
     *
     * @return The start pc
     */
    public final int getStartPC() {
        return startPC;
    }

    /**
     * Does this block contain a given bytecode address?
     *
     * @param address
     * @return boolean
     */
    public final boolean contains(int address) {
        return ((address >= startPC) && (address < endPC));
    }

    /**
     * @return String
     * @see java.lang.Object#toString()
     */
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append(startPC);
        buf.append('-');
        buf.append(endPC);
        buf.append(" [entry:");
        buf.append(startStack);
        buf.append(';');
        addEntryBlockInfo(buf);
        buf.append(']');
        if (startOfExceptionHandler) {
            buf.append(" (EH)");
        }
        return buf.toString();
    }

    private void addEntryBlockInfo(StringBuilder buf) {
        boolean first = true;
        for (BasicBlock bb : entryBlocks) {
            if (first) {
                first = false;
            } else {
                buf.append(',');
            }
            buf.append(bb.getStartPC());
            buf.append('-');
            buf.append(bb.getEndPC());
        }
    }

    /**
     * Is this block the start of an exception handler?
     *
     * @return boolean
     */
    public final boolean isStartOfExceptionHandler() {
        return startOfExceptionHandler;
    }

    /**
     * Gets the stack types at the start of this basic block.
     *
     * @return Returns the startStack.
     * @see org.jnode.vm.JvmType
     */
    public final TypeStack getStartStack() {
        return this.startStack;
    }

    /**
     * Set the stack types at the start of this basic block.
     *
     * @param startStack The startStack to set.
     * @see org.jnode.vm.JvmType
     */
    public final void setStartStack(TypeStack startStack) {
        this.startStack = startStack;
    }

    /**
     * Add a list of block that can can transfer execution to this block.
     *
     * @param entryBlock
     */
    public final void addEntryBlock(BasicBlock entryBlock) {
        if (entryBlock != null) {
            this.entryBlocks.add(entryBlock);
        }
    }

    public boolean isLive() {
        Set<BasicBlock> checked = new HashSet<BasicBlock>();
        checked.add(this);
        return isLive(checked);
    }

    private boolean isLive(Set<BasicBlock> checked) {
        if (startPC == 0)
            return true;

        if (entryBlocks != null) {
            for (BasicBlock bb : entryBlocks) {
                if (!checked.contains(bb)) {
                    checked.add(bb);
                    if (bb.isLive(checked)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
