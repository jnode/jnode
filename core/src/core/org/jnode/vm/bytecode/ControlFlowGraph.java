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

import java.util.Iterator;

import org.jnode.util.ObjectArrayIterator;
import org.jnode.vm.classmgr.VmByteCode;

/**
 * Determine and maintain the flow of control through a bytecode method.
 *
 * @author epr
 */
public class ControlFlowGraph implements Iterable<BasicBlock> {

    private final byte[] opcodeFlags;
    private final BasicBlock[] bblocks;

    /**
     * Create a new instance
     *
     * @param bytecode
     */
    public ControlFlowGraph(VmByteCode bytecode) {

        // First determine the basic blocks
        final BasicBlockFinder bbf = new BasicBlockFinder();
        BytecodeParser.parse(bytecode, bbf);
        this.bblocks = bbf.createBasicBlocks();
        this.opcodeFlags = bbf.getOpcodeFlags();
    }

    /**
     * Create an iterator to iterate over all basic blocks.
     *
     * @return An iterator that will return instances of BasicBlock.
     */
    public Iterator<BasicBlock> iterator() {
        return new ObjectArrayIterator<BasicBlock>(bblocks);
    }

    /**
     * Gets the number of basic blocks in this graph
     *
     * @return count of basic blocks
     */
    public int getBasicBlockCount() {
        return bblocks.length;
    }

    /**
     * Gets the basic block that contains the given address.
     *
     * @param pc
     * @return the BasicBlock or {@code null}.
     */
    public BasicBlock getBasicBlock(int pc) {
        final int max = bblocks.length;
        for (int i = 0; i < max; i++) {
            final BasicBlock bb = bblocks[i];
            if (bb.contains(pc)) {
                return bb;
            }
        }
        return null;
    }

    /**
     * Is a given combination of opcode flags set for a given bytecode address.
     *
     * @param address
     * @param flags
     * @return boolean
     * @see BytecodeFlags
     */
    public final boolean isOpcodeFlagSet(int address, int flags) {
        return ((opcodeFlags[address] & flags) == flags);
    }

    /**
     * Get the opcode flags set for a given bytecode address.
     *
     * @param address
     * @return int
     * @see BytecodeFlags
     */
    public final int getOpcodeFlags(int address) {
        return opcodeFlags[address];
    }
}
