/*
 * $Id: BasicBlockFinder.java 5709 2010-01-03 11:46:38Z lsantha $
 *
 * Copyright (C) 2003-2010 JNode.org
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

import java.util.TreeMap;
import org.jnode.bootlog.BootLogInstance;
import org.jnode.vm.JvmType;
import org.jnode.vm.classmgr.VmByteCode;
import org.jnode.vm.classmgr.VmInterpretedExceptionHandler;
import org.jnode.vm.classmgr.VmMethod;

/**
 * Bytecode visitor, used to determine the start addresses of basic blocks.
 *
 * @author Levente S\u00e1ntha
 */
public class DeadBlockFinder extends BytecodeVisitorSupport implements BytecodeFlags {
    private static final boolean debug = false;
    private final TreeMap<Integer, BasicBlock> blocks = new TreeMap<Integer, BasicBlock>();
    private byte[] opcodeFlags;
    private boolean nextIsStartOfBB;
    private boolean nextFollowsTypeStack;
    private boolean nextIsRetTarget;
    private int curAddress;
    private BasicBlock current;

    /**
     * Create all determined basic blocks
     *
     * @return the basic blocks.
     */
    public BasicBlock[] createBasicBlocks() {
        // Create the array
        final BasicBlock[] list = blocks.values().toArray(new BasicBlock[blocks.size()]);
        // Set the EndPC's and flags
        final byte[] opcodeFlags = this.opcodeFlags;
        final int len = opcodeFlags.length;
        int bbIndex = 0;
        for (int i = 0; i < len; i++) {
            if (isStartOfBB(i)) {
                final int start = i;
                // Find the end of the BB
                i++;
                while ((i < len) && (!isStartOfBB(i))) {
                    i++;
                }
                // the BB
                final BasicBlock bb = list[bbIndex++];
                if (bb.getStartPC() != start) {
                    throw new AssertionError("bb.getStartPC() != start");
                }
                bb.setEndPC(i);
                bb.setStartOfExceptionHandler(isStartOfException(start));
                i--;
            }
        }
        if (bbIndex != list.length) {
            throw new AssertionError("bbIndex != list.length");
        }
        return list;
    }

    /**
     * Get the per-opcode bytecode flags.
     *
     * @return byte[]
     */
    public final byte[] getOpcodeFlags() {
        return opcodeFlags;
    }

    /**
     * @param method
     * @see BytecodeVisitor#startMethod(org.jnode.vm.classmgr.VmMethod)
     */
    public void startMethod(VmMethod method) {
        final VmByteCode bc = method.getBytecode();
        final int length = bc.getLength();
        opcodeFlags = new byte[length];
        // The first instruction is always the start of a BB.
        startBB(0, true);
        // The exception handler also start a basic block
        final TypeStack ehTStack = new TypeStack();
        ehTStack.push(JvmType.REFERENCE);
        for (int i = 0; i < bc.getNoExceptionHandlers(); i++) {
            VmInterpretedExceptionHandler eh = bc.getExceptionHandler(i);
            startTryBlock(eh.getStartPC());
            startTryBlockEnd(eh.getEndPC());
            startException(eh.getHandlerPC(), ehTStack);
        }
    }

    /**
     * @param address
     * @see BytecodeVisitor#startInstruction(int)
     */
    public void startInstruction(int address) {
        if (debug) {
            BootLogInstance.get().debug("#" + address);
        }
        curAddress = address;
        super.startInstruction(address);
        opcodeFlags[address] |= F_START_OF_INSTRUCTION;
        boolean next_is_rt = nextIsRetTarget;
        if (nextIsRetTarget) {
            opcodeFlags[address] |= F_RET_TARGET;
            nextIsRetTarget = false;
        }
        boolean next_is_bb = nextIsStartOfBB;
        if (nextIsStartOfBB) {
            if (debug) BootLogInstance.get().debug("\tnextIsStartOfBB\t" + nextFollowsTypeStack);
            startBB(address, nextFollowsTypeStack);
            nextIsStartOfBB = false;
            nextFollowsTypeStack = true;
        }
        if (isStartOfBB(address)) {
            if (!next_is_bb) {
                BasicBlock bb = blocks.get(address);
                bb.addEntryBlock(current);
                current = bb;
            } else {
                current = blocks.get(address);
            }

            if (next_is_rt) {
                current.setRetTarget(true);
            }

            if (debug) BootLogInstance.get().debug("\tcurrent\t" + current);
        }
        if (debug) {
            BootLogInstance.get().debug("#" + address);
        }
    }

    /**
     * Mark the start of a basic block
     *
     * @param address
     */
    private void startBB(int address, boolean setTypeStack) {
        if ((opcodeFlags[address] & F_START_OF_BASICBLOCK) == 0) {
            opcodeFlags[address] |= F_START_OF_BASICBLOCK;
            final BasicBlock bb = new BasicBlock(address);
            blocks.put(address, bb);
            if (setTypeStack) {
                bb.addEntryBlock(current);
            }
        } else if (setTypeStack) {
            final BasicBlock bb = blocks.get(address);
            // Add entry block
            bb.addEntryBlock(current);
        }
    }

    /**
     * Mark the end of a basic block
     */
    private void endBB(boolean nextFollowsTypeStack) {
        this.nextIsStartOfBB = true;
        this.nextFollowsTypeStack = nextFollowsTypeStack;
    }

    /**
     * @param address
     * @see BytecodeVisitor#visit_ifeq(int)
     */
    public void visit_ifeq(int address) {
        addBranch(address, true);
        condYieldPoint(address);
    }

    /**
     * @param address
     * @see BytecodeVisitor#visit_ifne(int)
     */
    public void visit_ifne(int address) {
        addBranch(address, true);
        condYieldPoint(address);
    }

    /**
     * @param address
     * @see BytecodeVisitor#visit_iflt(int)
     */
    public void visit_iflt(int address) {
        addBranch(address, true);
        condYieldPoint(address);
    }

    /**
     * @param address
     * @see BytecodeVisitor#visit_ifge(int)
     */
    public void visit_ifge(int address) {
        addBranch(address, true);
        condYieldPoint(address);
    }

    /**
     * @param address
     * @see BytecodeVisitor#visit_ifgt(int)
     */
    public void visit_ifgt(int address) {
        addBranch(address, true);
        condYieldPoint(address);
    }

    /**
     * @param address
     * @see BytecodeVisitor#visit_ifle(int)
     */
    public void visit_ifle(int address) {
        addBranch(address, true);
        condYieldPoint(address);
    }

    /**
     * @param address
     * @see BytecodeVisitor#visit_if_icmpeq(int)
     */
    public void visit_if_icmpeq(int address) {
        addBranch(address, true);
        condYieldPoint(address);
    }

    /**
     * @param address
     * @see BytecodeVisitor#visit_if_icmpne(int)
     */
    public void visit_if_icmpne(int address) {
        addBranch(address, true);
        condYieldPoint(address);
    }

    /**
     * @param address
     * @see BytecodeVisitor#visit_if_icmplt(int)
     */
    public void visit_if_icmplt(int address) {
        addBranch(address, true);
        condYieldPoint(address);
    }

    /**
     * @param address
     * @see BytecodeVisitor#visit_if_icmpge(int)
     */
    public void visit_if_icmpge(int address) {
        addBranch(address, true);
        condYieldPoint(address);
    }

    /**
     * @param address
     * @see BytecodeVisitor#visit_if_icmpgt(int)
     */
    public void visit_if_icmpgt(int address) {
        addBranch(address, true);
        condYieldPoint(address);
    }

    /**
     * @param address
     * @see BytecodeVisitor#visit_if_icmple(int)
     */
    public void visit_if_icmple(int address) {
        addBranch(address, true);
        condYieldPoint(address);
    }

    /**
     * @param address
     * @see BytecodeVisitor#visit_if_acmpeq(int)
     */
    public void visit_if_acmpeq(int address) {
        addBranch(address, true);
        condYieldPoint(address);
    }

    /**
     * @param address
     * @see BytecodeVisitor#visit_if_acmpne(int)
     */
    public void visit_if_acmpne(int address) {
        addBranch(address, true);
        condYieldPoint(address);
    }

    /**
     * @param address
     * @see BytecodeVisitor#visit_goto(int)
     */
    public void visit_goto(int address) {
        // No change
        addBranch(address, false);
        condYieldPoint(address);
    }

    /**
     * @param address
     * @see BytecodeVisitor#visit_jsr(int)
     */
    public void visit_jsr(int address) {
        addBranch(address, false);
        nextIsRetTarget = true;
        condYieldPoint(address);
    }

    /**
     * @param defValue
     * @param lowValue
     * @param highValue
     * @param addresses
     * @see BytecodeVisitor#visit_tableswitch(int, int, int, int[])
     */
    public void visit_tableswitch(int defValue, int lowValue, int highValue, int[] addresses) {
        for (int address : addresses) {
            addBranch(address, true);
            condYieldPoint(address);
        }
        addBranch(defValue, false);
        condYieldPoint(defValue);
    }

    /**
     * @param defValue
     * @param matchValues
     * @param addresses
     * @see BytecodeVisitor#visit_lookupswitch(int, int[], int[])
     */
    public void visit_lookupswitch(int defValue, int[] matchValues, int[] addresses) {
        for (int address : addresses) {
            addBranch(address, true);
            condYieldPoint(address);
        }
        addBranch(defValue, false);
        condYieldPoint(defValue);
    }

    /**
     * @param address
     * @see BytecodeVisitor#visit_ifnull(int)
     */
    public void visit_ifnull(int address) {
        addBranch(address, true);
        condYieldPoint(address);
    }

    /**
     * @param address
     * @see BytecodeVisitor#visit_ifnonnull(int)
     */
    public void visit_ifnonnull(int address) {
        addBranch(address, true);
        condYieldPoint(address);
    }

    /**
     * @see BytecodeVisitor#visit_athrow()
     */
    public void visit_athrow() {
        endBB(false);
        // Reference is actually pushed on the stack, but that is handled
        // by the startException blocks.
    }

    /**
     * @see BytecodeVisitor#visit_areturn()
     */
    public void visit_areturn() {
        endBB(false);
    }

    /**
     * @see BytecodeVisitor#visit_dreturn()
     */
    public void visit_dreturn() {
        endBB(false);
    }

    /**
     * @see BytecodeVisitor#visit_freturn()
     */
    public void visit_freturn() {
        endBB(false);
    }

    /**
     * @see BytecodeVisitor#visit_ireturn()
     */
    public void visit_ireturn() {
        if (debug) {
            BootLogInstance.get().debug("ireturn at " + curAddress);
        }
        endBB(false);
    }

    /**
     * @see BytecodeVisitor#visit_lreturn()
     */
    public void visit_lreturn() {
        endBB(false);
    }

    /**
     * @param index
     * @see BytecodeVisitor#visit_ret(int)
     */
    public void visit_ret(int index) {
        // No change
        endBB(false);
    }

    /**
     * @see BytecodeVisitor#visit_return()
     */
    public void visit_return() {
        // No change
        endBB(false);
    }

    /**
     * Add branching information (to the given target) to the basic blocks information.
     *
     * @param target
     */
    private void addBranch(int target, boolean conditional) {
        startBB(target, true);
        endBB(conditional);
    }

    private boolean isStartOfBB(int address) {
        return ((opcodeFlags[address] & F_START_OF_BASICBLOCK) != 0);
    }

    private boolean isStartOfException(int address) {
        return ((opcodeFlags[address] & F_START_OF_EXCEPTIONHANDLER) != 0);
    }

    /**
     * Mark the start of a exception handler
     *
     * @param address
     */
    private void startException(int address, TypeStack tstack) {
        opcodeFlags[address] |= F_START_OF_EXCEPTIONHANDLER;
        //System.out.println("startException: " + tstack);
        startBB(address, true);
    }

    /**
     * Mark the start of a try-catch block
     *
     * @param address
     */
    private void startTryBlock(int address) {
        opcodeFlags[address] |= F_START_OF_TRYBLOCK;
        //startBB(address, false, null);
    }

    /**
     * Mark the end of a try-catch block
     *
     * @param address
     */
    private void startTryBlockEnd(int address) {
        opcodeFlags[address] |= F_START_OF_TRYBLOCKEND;
        //startBB(address, false, null);
    }

    /**
     * Mark a conditional yieldpoint.
     */
    private void condYieldPoint(int target) {
        if (target < curAddress) {
            opcodeFlags[curAddress] |= F_YIELDPOINT;
        }
    }
}
