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
 
package org.jnode.vm.compiler.ir;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.jnode.vm.bytecode.BytecodeFlags;
import org.jnode.vm.bytecode.BytecodeVisitorSupport;
import org.jnode.vm.classmgr.VmByteCode;
import org.jnode.vm.classmgr.VmInterpretedExceptionHandler;
import org.jnode.vm.classmgr.VmMethod;

/**
 * @author Madhu Siddalingaiah
 */
public class IRBasicBlockFinder<T> extends BytecodeVisitorSupport implements Comparator<IRBasicBlock> {
    private boolean nextIsStartOfBB;

    private IRBasicBlock currentBlock;

    private byte[] opcodeFlags;
    private byte[] branchFlags;

    private final ArrayList<IRBasicBlock<T>> blocks = new ArrayList<IRBasicBlock<T>>();
    private final HashMap<Integer, Integer> branchTargets = new HashMap<Integer, Integer>();
    private VmByteCode byteCode;
    private static final byte CONDITIONAL_BRANCH = 1;
    private static final byte UNCONDITIONAL_BRANCH = 2;

    /**
     * Create all determined basic blocks
     *
     * @return
     */
    public IRBasicBlock<T>[] createBasicBlocks() {
        // Sort the blocks on start PC
        Collections.sort(blocks, this);
        // Create the array
        final IRBasicBlock<T>[] list = (IRBasicBlock<T>[]) blocks.toArray(new IRBasicBlock[blocks.size()]);
        // Set the EndPC's and flags
        final byte[] opcodeFlags = this.opcodeFlags;
        final byte[] branchFlags = this.branchFlags;
        final int len = opcodeFlags.length;
        int bbIndex = 0;
        for (int i = 0; i < len; i++) {
            if (isStartOfBB(i)) {
                final int start = i;
                // Find the end of the BB
                boolean nextIsSuccessor = true;
                if ((branchFlags[i] & UNCONDITIONAL_BRANCH) != 0) {
                    nextIsSuccessor = false;
                }
                i++;
                while ((i < len) && (!isStartOfBB(i))) {
                    if ((branchFlags[i] & UNCONDITIONAL_BRANCH) != 0) {
                        nextIsSuccessor = false;
                    }
                    i++;
                }
                // the BB
                final IRBasicBlock<T> bb = list[bbIndex++];
                if (nextIsSuccessor && bbIndex < list.length) {
                    bb.addSuccessor(list[bbIndex]);
                }
                if (bb.getStartPC() != start) {
                    throw new AssertionError("bb.getStartPC() != start");
                }
                bb.setEndPC(i);
                bb.setStartOfExceptionHandler(isStartOfException(start));
                i--;
            }
        }
        // TODO this is O(n^2), but it works...
        for (Map.Entry<Integer, Integer> entry : branchTargets.entrySet()) {
            final int from = entry.getKey();
            final int to = entry.getValue();
            IRBasicBlock<T> pred = findBB(list, from);
            IRBasicBlock<T> succ = findBB(list, to);
            if (pred == null || succ == null) {
                throw new AssertionError("unable to find BB!");
            }
            pred.addSuccessor(succ);
        }
        if (bbIndex != list.length) {
            throw new AssertionError("bbIndex != list.length");
        }
        return list;
    }

    private IRBasicBlock<T> findBB(IRBasicBlock<T>[] blocks, int address) {
        for (IRBasicBlock<T> b : blocks) {
            if (b.contains(address)) {
                return b;
            }
        }
        return null;
    }

    /**
     * @param method
     * @see org.jnode.vm.bytecode.BytecodeVisitor#startMethod(org.jnode.vm.classmgr.VmMethod)
     */
    public void startMethod(VmMethod method) {
        final VmByteCode bc = method.getBytecode();
        byteCode = bc;
        final int length = bc.getLength();
        opcodeFlags = new byte[length];
        branchFlags = new byte[length];

        // The first instruction is always the start of a BB.
        this.currentBlock = startBB(0);
        currentBlock.setStackOffset(bc.getNoLocals());
        // The exception handler also start a basic block
        for (int i = 0; i < bc.getNoExceptionHandlers(); i++) {
            VmInterpretedExceptionHandler eh = bc.getExceptionHandler(i);
            IRBasicBlock tryBlock = startTryBlock(eh.getStartPC());
            IRBasicBlock endTryBlock = startTryBlockEnd(eh.getEndPC());
            IRBasicBlock catchBlock = startException(eh.getHandlerPC());
        }
    }

    /* (non-Javadoc)
      * @see org.jnode.vm.bytecode.BytecodeVisitor#endMethod()
      */
    public void endMethod() {
        VmByteCode bc = byteCode;
        // TODO add catch blocks to try successors
        for (int i = 0; i < bc.getNoExceptionHandlers(); i++) {
            VmInterpretedExceptionHandler eh = bc.getExceptionHandler(i);
        }
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifeq(int)
     */
    public void visit_ifeq(int address) {
        addBranch(address, CONDITIONAL_BRANCH);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifne(int)
     */
    public void visit_ifne(int address) {
        addBranch(address, CONDITIONAL_BRANCH);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iflt(int)
     */
    public void visit_iflt(int address) {
        addBranch(address, CONDITIONAL_BRANCH);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifge(int)
     */
    public void visit_ifge(int address) {
        addBranch(address, CONDITIONAL_BRANCH);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifgt(int)
     */
    public void visit_ifgt(int address) {
        addBranch(address, CONDITIONAL_BRANCH);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifle(int)
     */
    public void visit_ifle(int address) {
        addBranch(address, CONDITIONAL_BRANCH);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmpeq(int)
     */
    public void visit_if_icmpeq(int address) {
        addBranch(address, CONDITIONAL_BRANCH);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmpne(int)
     */
    public void visit_if_icmpne(int address) {
        addBranch(address, CONDITIONAL_BRANCH);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmplt(int)
     */
    public void visit_if_icmplt(int address) {
        addBranch(address, CONDITIONAL_BRANCH);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmpge(int)
     */
    public void visit_if_icmpge(int address) {
        addBranch(address, CONDITIONAL_BRANCH);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmpgt(int)
     */
    public void visit_if_icmpgt(int address) {
        addBranch(address, CONDITIONAL_BRANCH);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmple(int)
     */
    public void visit_if_icmple(int address) {
        addBranch(address, CONDITIONAL_BRANCH);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_acmpeq(int)
     */
    public void visit_if_acmpeq(int address) {
        addBranch(address, CONDITIONAL_BRANCH);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_acmpne(int)
     */
    public void visit_if_acmpne(int address) {
        addBranch(address, CONDITIONAL_BRANCH);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_goto(int)
     */
    public void visit_goto(int address) {
        addBranch(address, UNCONDITIONAL_BRANCH);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_jsr(int)
     */
    public void visit_jsr(int address) {
        // TODO Not sure about this, the next block I believe it NOT a
        // direct successor. This will have to be tested.
        //addBranch(address);
    }

    /**
     * @param defValue
     * @param lowValue
     * @param highValue
     * @param addresses
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_tableswitch(int, int, int, int[])
     */
    public void visit_tableswitch(int defValue, int lowValue, int highValue, int[] addresses) {
        for (int i = 0; i < addresses.length; i++) {
            // Next block could be successor, e.g. switch could fall through
            addBranch(addresses[i], CONDITIONAL_BRANCH);
        }
        // Same for default case
        addBranch(defValue, CONDITIONAL_BRANCH);
    }

    /**
     * @param defValue
     * @param matchValues
     * @param addresses
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lookupswitch(int, int[], int[])
     */
    public void visit_lookupswitch(int defValue, int[] matchValues, int[] addresses) {
        for (int i = 0; i < addresses.length; i++) {
            // Next block could be successor, e.g. switch could fall through
            addBranch(addresses[i], CONDITIONAL_BRANCH);
        }
        // Same for default case
        addBranch(defValue, CONDITIONAL_BRANCH);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifnull(int)
     */
    public void visit_ifnull(int address) {
        addBranch(address, CONDITIONAL_BRANCH);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifnonnull(int)
     */
    public void visit_ifnonnull(int address) {
        addBranch(address, CONDITIONAL_BRANCH);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_athrow()
     */
    public void visit_athrow() {
        endBB(UNCONDITIONAL_BRANCH);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_areturn()
     */
    public void visit_areturn() {
        endBB(UNCONDITIONAL_BRANCH);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dreturn()
     */
    public void visit_dreturn() {
        endBB(UNCONDITIONAL_BRANCH);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_freturn()
     */
    public void visit_freturn() {
        endBB(UNCONDITIONAL_BRANCH);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ireturn()
     */
    public void visit_ireturn() {
        endBB(UNCONDITIONAL_BRANCH);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lreturn()
     */
    public void visit_lreturn() {
        endBB(UNCONDITIONAL_BRANCH);
    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ret(int)
     */
    public void visit_ret(int index) {
        // Not sure about this either, this needs testing
        endBB(UNCONDITIONAL_BRANCH);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_return()
     */
    public void visit_return() {
        endBB(UNCONDITIONAL_BRANCH);
    }

    /**
     * Add branching information (to the given target) to the basic blocks information.
     *
     * @param target
     */
    private final void addBranch(int target, byte flags) {
        IRBasicBlock pred = this.currentBlock;
        IRBasicBlock succ = startBB(target);
        branchTargets.put(new Integer(getInstructionAddress()), new Integer(target));
        endBB(flags);
    }

    /**
     * Mark the start of a basic block
     *
     * @param address
     */
    private final IRBasicBlock<T> startBB(int address) {
        IRBasicBlock<T> next = null;
        if ((opcodeFlags[address] & BytecodeFlags.F_START_OF_BASICBLOCK) == 0) {
            opcodeFlags[address] |= BytecodeFlags.F_START_OF_BASICBLOCK;
            next = new IRBasicBlock<T>(address);
            blocks.add(next);
        } else {
            for (IRBasicBlock<T> bb : blocks) {
                if (bb.getStartPC() == address) {
                    next = bb;
                    break;
                }
            }
        }
        return next;
    }

    /**
     * Mark the end of a basic block
     */
    private final void endBB(byte flags) {
        nextIsStartOfBB = true;
        int address = getInstructionAddress();
        branchFlags[address] |= flags;
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#startInstruction(int)
     */
    public void startInstruction(int address) {
        super.startInstruction(address);
        opcodeFlags[address] |= BytecodeFlags.F_START_OF_INSTRUCTION;
        if (nextIsStartOfBB || isStartOfBB(address)) {
            this.currentBlock = startBB(address);
            nextIsStartOfBB = false;
        }
    }

    private final boolean isStartOfBB(int address) {
        return ((opcodeFlags[address] & BytecodeFlags.F_START_OF_BASICBLOCK) != 0);
    }

    private final boolean isStartOfException(int address) {
        return ((opcodeFlags[address] & BytecodeFlags.F_START_OF_EXCEPTIONHANDLER) != 0);
    }

    /**
     * Mark the start of a exception handler
     *
     * @param address
     */
    private final IRBasicBlock startException(int address) {
        opcodeFlags[address] |= BytecodeFlags.F_START_OF_EXCEPTIONHANDLER;
        return startBB(address);
    }

    /**
     * Mark the start of a try-catch block
     *
     * @param address
     */
    private final IRBasicBlock startTryBlock(int address) {
        opcodeFlags[address] |= BytecodeFlags.F_START_OF_TRYBLOCK;
        return startBB(address);
    }

    /**
     * Mark the end of a try-catch block
     *
     * @param address
     */
    private final IRBasicBlock startTryBlockEnd(int address) {
        opcodeFlags[address] |= BytecodeFlags.F_START_OF_TRYBLOCKEND;
        return startBB(address);
    }

    public int compare(IRBasicBlock o1, IRBasicBlock o2) {
        final int sp1 = ((IRBasicBlock) o1).getStartPC();
        final int sp2 = ((IRBasicBlock) o2).getStartPC();
        return sp1 - sp2;
    }
}
