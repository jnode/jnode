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

package org.jnode.vm.bytecode;

import java.util.Comparator;
import java.util.TreeMap;
import org.jnode.system.BootLog;
import org.jnode.vm.JvmType;
import org.jnode.vm.classmgr.VmByteCode;
import org.jnode.vm.classmgr.VmConstClass;
import org.jnode.vm.classmgr.VmConstFieldRef;
import org.jnode.vm.classmgr.VmConstIMethodRef;
import org.jnode.vm.classmgr.VmConstMethodRef;
import org.jnode.vm.classmgr.VmConstString;
import org.jnode.vm.classmgr.VmInterpretedExceptionHandler;
import org.jnode.vm.classmgr.VmMethod;

/**
 * Bytecode visitor, used to determine the start addresses of basic blocks.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Madhu Siddalingaiah
 */
public class BasicBlockFinder extends BytecodeVisitorSupport implements BytecodeFlags {

    private static final boolean debug = false;
    private final TreeMap<Integer, BasicBlock> blocks = new TreeMap<Integer, BasicBlock>();
    private byte[] opcodeFlags;
    private boolean nextIsStartOfBB;
    private boolean nextFollowsTypeStack;
    private int curAddress;
    private final TypeStack tstack = new TypeStack();
    private BasicBlock current;
    private VmMethod method;

    /**
     * Create all determined basic blocks
     *
     * @return
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
     * @see org.jnode.vm.bytecode.BytecodeVisitor#startMethod(org.jnode.vm.classmgr.VmMethod)
     */
    public void startMethod(VmMethod method) {
        this.method = method;
        final VmByteCode bc = method.getBytecode();
        final int length = bc.getLength();
        opcodeFlags = new byte[length];
        // The first instruction is always the start of a BB.
        startBB(0, true, tstack);
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
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifeq(int)
     */
    public void visit_ifeq(int address) {
        tstack.pop(JvmType.INT);
        addBranch(address, true);
        condYieldPoint(address);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifne(int)
     */
    public void visit_ifne(int address) {
        tstack.pop(JvmType.INT);
        addBranch(address, true);
        condYieldPoint(address);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iflt(int)
     */
    public void visit_iflt(int address) {
        tstack.pop(JvmType.INT);
        addBranch(address, true);
        condYieldPoint(address);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifge(int)
     */
    public void visit_ifge(int address) {
        tstack.pop(JvmType.INT);
        addBranch(address, true);
        condYieldPoint(address);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifgt(int)
     */
    public void visit_ifgt(int address) {
        tstack.pop(JvmType.INT);
        addBranch(address, true);
        condYieldPoint(address);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifle(int)
     */
    public void visit_ifle(int address) {
        tstack.pop(JvmType.INT);
        addBranch(address, true);
        condYieldPoint(address);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmpeq(int)
     */
    public void visit_if_icmpeq(int address) {
        tstack.pop(JvmType.INT);
        tstack.pop(JvmType.INT);
        addBranch(address, true);
        condYieldPoint(address);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmpne(int)
     */
    public void visit_if_icmpne(int address) {
        tstack.pop(JvmType.INT);
        tstack.pop(JvmType.INT);
        addBranch(address, true);
        condYieldPoint(address);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmplt(int)
     */
    public void visit_if_icmplt(int address) {
        tstack.pop(JvmType.INT);
        tstack.pop(JvmType.INT);
        addBranch(address, true);
        condYieldPoint(address);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmpge(int)
     */
    public void visit_if_icmpge(int address) {
        tstack.pop(JvmType.INT);
        tstack.pop(JvmType.INT);
        addBranch(address, true);
        condYieldPoint(address);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmpgt(int)
     */
    public void visit_if_icmpgt(int address) {
        tstack.pop(JvmType.INT);
        tstack.pop(JvmType.INT);
        addBranch(address, true);
        condYieldPoint(address);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmple(int)
     */
    public void visit_if_icmple(int address) {
        tstack.pop(JvmType.INT);
        tstack.pop(JvmType.INT);
        addBranch(address, true);
        condYieldPoint(address);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_acmpeq(int)
     */
    public void visit_if_acmpeq(int address) {
        tstack.pop(JvmType.REFERENCE);
        tstack.pop(JvmType.REFERENCE);
        addBranch(address, true);
        condYieldPoint(address);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_acmpne(int)
     */
    public void visit_if_acmpne(int address) {
        tstack.pop(JvmType.REFERENCE);
        tstack.pop(JvmType.REFERENCE);
        addBranch(address, true);
        condYieldPoint(address);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_goto(int)
     */
    public void visit_goto(int address) {
        // No change
        addBranch(address, false);
        condYieldPoint(address);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_jsr(int)
     */
    public void visit_jsr(int address) {
        tstack.push(JvmType.RETURN_ADDRESS);
        addBranch(address, false);
        condYieldPoint(address);
        tstack.pop();
    }

    /**
     * @param defValue
     * @param lowValue
     * @param highValue
     * @param addresses
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_tableswitch(int, int, int, int[])
     */
    public void visit_tableswitch(int defValue, int lowValue, int highValue, int[] addresses) {
        tstack.pop(JvmType.INT);
        for (int i = 0; i < addresses.length; i++) {
            addBranch(addresses[i], true);
            condYieldPoint(addresses[i]);
        }
        addBranch(defValue, false);
        condYieldPoint(defValue);
    }

    /**
     * @param defValue
     * @param matchValues
     * @param addresses
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lookupswitch(int, int[], int[])
     */
    public void visit_lookupswitch(int defValue, int[] matchValues, int[] addresses) {
        tstack.pop(JvmType.INT);
        for (int i = 0; i < addresses.length; i++) {
            addBranch(addresses[i], true);
            condYieldPoint(addresses[i]);
        }
        addBranch(defValue, false);
        condYieldPoint(defValue);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifnull(int)
     */
    public void visit_ifnull(int address) {
        tstack.pop(JvmType.REFERENCE);
        addBranch(address, true);
        condYieldPoint(address);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifnonnull(int)
     */
    public void visit_ifnonnull(int address) {
        tstack.pop(JvmType.REFERENCE);
        addBranch(address, true);
        condYieldPoint(address);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_athrow()
     */
    public void visit_athrow() {
        tstack.pop(JvmType.REFERENCE);
        endBB(false);
        // Reference is actually pushed on the stack, but that is handled
        // by the startException blocks.
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_areturn()
     */
    public void visit_areturn() {
        tstack.pop(JvmType.REFERENCE);
        endBB(false);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dreturn()
     */
    public void visit_dreturn() {
        tstack.pop(JvmType.DOUBLE);
        endBB(false);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_freturn()
     */
    public void visit_freturn() {
        tstack.pop(JvmType.FLOAT);
        endBB(false);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ireturn()
     */
    public void visit_ireturn() {
        if (debug) {
            BootLog.debug("ireturn at " + curAddress + "; " + tstack);
        }
        tstack.pop(JvmType.INT);
        endBB(false);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lreturn()
     */
    public void visit_lreturn() {
        tstack.pop(JvmType.LONG);
        endBB(false);
    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ret(int)
     */
    public void visit_ret(int index) {
        // No change
        endBB(false);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_return()
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
    private final void addBranch(int target, boolean conditional) {
        startBB(target, true, this.tstack);
        endBB(conditional);
    }

    /**
     * Mark the end of a basic block
     */
    private final void endBB(boolean nextFollowsTypeStack) {
        this.nextIsStartOfBB = true;
        this.nextFollowsTypeStack = nextFollowsTypeStack;
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#startInstruction(int)
     */
    public void startInstruction(int address) {
        if (debug) {
            BootLog.debug("#" + address + "\t" + tstack);
        }
        curAddress = address;
        super.startInstruction(address);
        opcodeFlags[address] |= F_START_OF_INSTRUCTION;
        if (nextIsStartOfBB) {
            if (debug) BootLog.debug("\tnextIsStartOfBB\t" + nextFollowsTypeStack);
            startBB(address, nextFollowsTypeStack, this.tstack);
            nextIsStartOfBB = false;
            nextFollowsTypeStack = true;
        }
        if (isStartOfBB(address)) {
            this.current = blocks.get(address);
            if (debug) BootLog.debug("\tcurrent\t" + current);
            final TypeStack bbTStack = current.getStartStack();
            if (bbTStack != null) {
                if (debug) BootLog.debug("\tcopyFrom\t" + bbTStack);
                this.tstack.copyFrom(bbTStack);
            } else if (!nextFollowsTypeStack) {
                if (debug) BootLog.debug("\tclear");
                this.tstack.clear();
            } else {
                if (debug) BootLog.debug("\tsetStartStack");
                current.setStartStack(tstack);
            }
        }
        if (debug) {
            BootLog.debug("#" + address + "\t" + tstack);
        }
    }

    /**
     * Mark the start of a basic block
     *
     * @param address
     */
    private final void startBB(int address, boolean setTypeStack, TypeStack tstack) {
        if ((opcodeFlags[address] & F_START_OF_BASICBLOCK) == 0) {
            opcodeFlags[address] |= F_START_OF_BASICBLOCK;
            final BasicBlock bb = new BasicBlock(address);
            blocks.put(address, bb);
            if (setTypeStack) {
                bb.addEntryBlock(current);
                bb.setStartStack(new TypeStack(tstack));
            }
        } else if (setTypeStack) {
            // Verify stack
            final BasicBlock bb = blocks.get(address);
            final TypeStack bbTStack = bb.getStartStack();
            if (bbTStack == null) {
                bb.setStartStack(tstack);
            } else if (!tstack.equals(bbTStack)) {
                if (debug) {
                    BootLog.warn("TypeStack is different in " + method + ";" + tstack + " vs. " + bbTStack + " in " +
                        bb + " at address " + this.curAddress);
                }
                //throw new VerifyError("TypeStack is different; " + tstack + " vs. " + bbTStack + " in " + bb);
            }
            // Add entry block
            bb.addEntryBlock(current);
        }

    }

    private final boolean isStartOfBB(int address) {
        return ((opcodeFlags[address] & F_START_OF_BASICBLOCK) != 0);
    }

    private final boolean isStartOfException(int address) {
        return ((opcodeFlags[address] & F_START_OF_EXCEPTIONHANDLER) != 0);
    }

    /**
     * Mark the start of a exception handler
     *
     * @param address
     */
    private final void startException(int address, TypeStack tstack) {
        opcodeFlags[address] |= F_START_OF_EXCEPTIONHANDLER;
        //System.out.println("startException: " + tstack);
        startBB(address, true, tstack);
    }

    /**
     * Mark the start of a try-catch block
     *
     * @param address
     */
    private final void startTryBlock(int address) {
        opcodeFlags[address] |= F_START_OF_TRYBLOCK;
        //startBB(address, false, null);
    }

    /**
     * Mark the end of a try-catch block
     *
     * @param address
     */
    private final void startTryBlockEnd(int address) {
        opcodeFlags[address] |= F_START_OF_TRYBLOCKEND;
        //startBB(address, false, null);
    }

    /**
     * Mark a conditional yieldpoint.
     */
    private final void condYieldPoint(int target) {
        if (target < curAddress) {
            opcodeFlags[curAddress] |= F_YIELDPOINT;
        }
    }

    /**
     * Compare basic blocks on their start PC.
     *
     * @author Ewout Prangsma (epr@users.sourceforge.net)
     */
    static class BasicBlockComparator implements Comparator {
        static final BasicBlockComparator INSTANCE = new BasicBlockComparator();

        /**
         * @param o1
         * @param o2
         * @return int
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(Object o1, Object o2) {
            final int sp1 = ((BasicBlock) o1).getStartPC();
            final int sp2 = ((BasicBlock) o2).getStartPC();
            return sp1 - sp2;
        }
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_aaload()
     */
    public void visit_aaload() {
        tstack.pop(JvmType.INT);
        tstack.pop(JvmType.REFERENCE);
        tstack.push(JvmType.REFERENCE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_aastore()
     */
    public void visit_aastore() {
        tstack.pop(JvmType.REFERENCE);
        tstack.pop(JvmType.INT);
        tstack.pop(JvmType.REFERENCE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_aconst_null()
     */
    public void visit_aconst_null() {
        tstack.push(JvmType.REFERENCE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_aload(int)
     */
    public void visit_aload(int index) {
        tstack.push(JvmType.REFERENCE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_anewarray(org.jnode.vm.classmgr.VmConstClass)
     */
    public void visit_anewarray(VmConstClass clazz) {
        tstack.pop(JvmType.INT);
        tstack.push(JvmType.REFERENCE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_arraylength()
     */
    public void visit_arraylength() {
        tstack.pop(JvmType.REFERENCE);
        tstack.push(JvmType.INT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_astore(int)
     */
    public void visit_astore(int index) {
        tstack.pop(JvmType.REFERENCE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_baload()
     */
    public void visit_baload() {
        tstack.pop(JvmType.INT);
        tstack.pop(JvmType.REFERENCE);
        tstack.push(JvmType.INT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_bastore()
     */
    public void visit_bastore() {
        tstack.pop(JvmType.INT);
        tstack.pop(JvmType.INT);
        tstack.pop(JvmType.REFERENCE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_caload()
     */
    public void visit_caload() {
        tstack.pop(JvmType.INT);
        tstack.pop(JvmType.REFERENCE);
        tstack.push(JvmType.INT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_castore()
     */
    public void visit_castore() {
        tstack.pop(JvmType.INT);
        tstack.pop(JvmType.INT);
        tstack.pop(JvmType.REFERENCE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_checkcast(org.jnode.vm.classmgr.VmConstClass)
     */
    public void visit_checkcast(VmConstClass clazz) {
        tstack.pop(JvmType.REFERENCE);
        tstack.push(JvmType.REFERENCE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_d2f()
     */
    public void visit_d2f() {
        tstack.pop(JvmType.DOUBLE);
        tstack.push(JvmType.FLOAT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_d2i()
     */
    public void visit_d2i() {
        tstack.pop(JvmType.DOUBLE);
        tstack.push(JvmType.INT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_d2l()
     */
    public void visit_d2l() {
        tstack.pop(JvmType.DOUBLE);
        tstack.push(JvmType.LONG);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dadd()
     */
    public void visit_dadd() {
        tstack.pop(JvmType.DOUBLE);
        tstack.pop(JvmType.DOUBLE);
        tstack.push(JvmType.DOUBLE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_daload()
     */
    public void visit_daload() {
        tstack.pop(JvmType.INT);
        tstack.pop(JvmType.REFERENCE);
        tstack.push(JvmType.DOUBLE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dastore()
     */
    public void visit_dastore() {
        tstack.pop(JvmType.DOUBLE);
        tstack.pop(JvmType.INT);
        tstack.pop(JvmType.REFERENCE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dcmpg()
     */
    public void visit_dcmpg() {
        tstack.pop(JvmType.DOUBLE);
        tstack.pop(JvmType.DOUBLE);
        tstack.push(JvmType.INT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dcmpl()
     */
    public void visit_dcmpl() {
        tstack.pop(JvmType.DOUBLE);
        tstack.pop(JvmType.DOUBLE);
        tstack.push(JvmType.INT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dconst(double)
     */
    public void visit_dconst(double value) {
        tstack.push(JvmType.DOUBLE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ddiv()
     */
    public void visit_ddiv() {
        tstack.pop(JvmType.DOUBLE);
        tstack.pop(JvmType.DOUBLE);
        tstack.push(JvmType.DOUBLE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dload(int)
     */
    public void visit_dload(int index) {
        tstack.push(JvmType.DOUBLE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dmul()
     */
    public void visit_dmul() {
        tstack.pop(JvmType.DOUBLE);
        tstack.pop(JvmType.DOUBLE);
        tstack.push(JvmType.DOUBLE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dneg()
     */
    public void visit_dneg() {
        tstack.pop(JvmType.DOUBLE);
        tstack.push(JvmType.DOUBLE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_drem()
     */
    public void visit_drem() {
        tstack.pop(JvmType.DOUBLE);
        tstack.pop(JvmType.DOUBLE);
        tstack.push(JvmType.DOUBLE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dstore(int)
     */
    public void visit_dstore(int index) {
        tstack.pop(JvmType.DOUBLE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dsub()
     */
    public void visit_dsub() {
        tstack.pop(JvmType.DOUBLE);
        tstack.pop(JvmType.DOUBLE);
        tstack.push(JvmType.DOUBLE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup_x1()
     */
    public void visit_dup_x1() {
        final int tv1 = tstack.pop();
        final int tv2 = tstack.pop();
        tstack.push(tv1);
        tstack.push(tv2);
        tstack.push(tv1);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup_x2()
     */
    public void visit_dup_x2() {
        final int tv1 = tstack.pop();
        final int tv2 = tstack.pop();
        if (JvmType.getCategory(tv2) == 1) {
            final int tv3 = tstack.pop();
            tstack.push(tv1);
            tstack.push(tv3);
            tstack.push(tv2);
            tstack.push(tv1);
        } else {
            tstack.push(tv1);
            tstack.push(tv2);
            tstack.push(tv1);
        }
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup()
     */
    public void visit_dup() {
        final int tv1 = tstack.pop();
        tstack.push(tv1);
        tstack.push(tv1);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup2_x1()
     */
    public void visit_dup2_x1() {
        final int tv1 = tstack.pop();
        final int tv2 = tstack.pop();
        if (JvmType.getCategory(tv1) == 1) {
            final int tv3 = tstack.pop();
            tstack.push(tv2);
            tstack.push(tv1);
            tstack.push(tv3);
            tstack.push(tv2);
            tstack.push(tv1);
        } else {
            tstack.push(tv1);
            tstack.push(tv2);
            tstack.push(tv1);
        }
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup2_x2()
     */
    public void visit_dup2_x2() {
        final int tv1 = tstack.pop();
        final int tv2 = tstack.pop();
        final int cat1 = JvmType.getCategory(tv1);
        final int cat2 = JvmType.getCategory(tv2);
        if ((cat1 == 2) && (cat2 == 2)) {
            // Form 4
            tstack.push(tv1);
            tstack.push(tv2);
            tstack.push(tv1);
        } else {
            final int tv3 = tstack.pop();
            final int cat3 = JvmType.getCategory(tv3);
            if ((cat1 == 1) && (cat2 == 1) && (cat3 == 3)) {
                // Form 3
                tstack.push(tv2);
                tstack.push(tv1);
                tstack.push(tv3);
                tstack.push(tv2);
                tstack.push(tv1);
            } else if ((cat1 == 2) && (cat2 == 1) && (cat3 == 1)) {
                // Form 2
                tstack.push(tv1);
                tstack.push(tv3);
                tstack.push(tv2);
                tstack.push(tv1);
            } else {
                // Form 4
                final int tv4 = tstack.pop();
                tstack.push(tv2);
                tstack.push(tv1);
                tstack.push(tv4);
                tstack.push(tv3);
                tstack.push(tv2);
                tstack.push(tv1);
            }
        }
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup2()
     */
    public void visit_dup2() {
        final int tv1 = tstack.pop();
        if (JvmType.getCategory(tv1) == 1) {
            final int tv2 = tstack.pop();
            tstack.push(tv2);
            tstack.push(tv1);
            tstack.push(tv2);
            tstack.push(tv1);
        } else {
            tstack.push(tv1);
            tstack.push(tv1);
        }
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_f2d()
     */
    public void visit_f2d() {
        tstack.pop(JvmType.FLOAT);
        tstack.push(JvmType.DOUBLE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_f2i()
     */
    public void visit_f2i() {
        tstack.pop(JvmType.FLOAT);
        tstack.push(JvmType.INT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_f2l()
     */
    public void visit_f2l() {
        tstack.pop(JvmType.FLOAT);
        tstack.push(JvmType.LONG);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fadd()
     */
    public void visit_fadd() {
        tstack.pop(JvmType.FLOAT);
        tstack.pop(JvmType.FLOAT);
        tstack.push(JvmType.FLOAT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_faload()
     */
    public void visit_faload() {
        tstack.pop(JvmType.INT);
        tstack.pop(JvmType.REFERENCE);
        tstack.push(JvmType.FLOAT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fastore()
     */
    public void visit_fastore() {
        tstack.pop(JvmType.FLOAT);
        tstack.pop(JvmType.INT);
        tstack.pop(JvmType.REFERENCE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fcmpg()
     */
    public void visit_fcmpg() {
        tstack.pop(JvmType.FLOAT);
        tstack.pop(JvmType.FLOAT);
        tstack.push(JvmType.INT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fcmpl()
     */
    public void visit_fcmpl() {
        tstack.pop(JvmType.FLOAT);
        tstack.pop(JvmType.FLOAT);
        tstack.push(JvmType.INT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fconst(float)
     */
    public void visit_fconst(float value) {
        tstack.push(JvmType.FLOAT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fdiv()
     */
    public void visit_fdiv() {
        tstack.pop(JvmType.FLOAT);
        tstack.pop(JvmType.FLOAT);
        tstack.push(JvmType.FLOAT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fload(int)
     */
    public void visit_fload(int index) {
        tstack.push(JvmType.FLOAT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fmul()
     */
    public void visit_fmul() {
        tstack.pop(JvmType.FLOAT);
        tstack.pop(JvmType.FLOAT);
        tstack.push(JvmType.FLOAT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fneg()
     */
    public void visit_fneg() {
        tstack.pop(JvmType.FLOAT);
        tstack.push(JvmType.FLOAT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_frem()
     */
    public void visit_frem() {
        tstack.pop(JvmType.FLOAT);
        tstack.pop(JvmType.FLOAT);
        tstack.push(JvmType.FLOAT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fstore(int)
     */
    public void visit_fstore(int index) {
        tstack.pop(JvmType.FLOAT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fsub()
     */
    public void visit_fsub() {
        tstack.pop(JvmType.FLOAT);
        tstack.pop(JvmType.FLOAT);
        tstack.push(JvmType.FLOAT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_getfield(org.jnode.vm.classmgr.VmConstFieldRef)
     */
    public void visit_getfield(VmConstFieldRef fieldRef) {
        tstack.pop(JvmType.REFERENCE);
        tstack.push(JvmType.SignatureToType(fieldRef.getSignature()));
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_getstatic(org.jnode.vm.classmgr.VmConstFieldRef)
     */
    public void visit_getstatic(VmConstFieldRef fieldRef) {
        tstack.push(JvmType.SignatureToType(fieldRef.getSignature()));
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2b()
     */
    public void visit_i2b() {
        tstack.pop(JvmType.INT);
        tstack.push(JvmType.INT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2c()
     */
    public void visit_i2c() {
        tstack.pop(JvmType.INT);
        tstack.push(JvmType.INT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2d()
     */
    public void visit_i2d() {
        tstack.pop(JvmType.INT);
        tstack.push(JvmType.DOUBLE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2f()
     */
    public void visit_i2f() {
        tstack.pop(JvmType.INT);
        tstack.push(JvmType.FLOAT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2l()
     */
    public void visit_i2l() {
        tstack.pop(JvmType.INT);
        tstack.push(JvmType.LONG);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2s()
     */
    public void visit_i2s() {
        tstack.pop(JvmType.INT);
        tstack.push(JvmType.INT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iadd()
     */
    public void visit_iadd() {
        tstack.pop(JvmType.INT);
        tstack.pop(JvmType.INT);
        tstack.push(JvmType.INT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iaload()
     */
    public void visit_iaload() {
        tstack.pop(JvmType.INT);
        tstack.pop(JvmType.REFERENCE);
        tstack.push(JvmType.INT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iand()
     */
    public void visit_iand() {
        tstack.pop(JvmType.INT);
        tstack.pop(JvmType.INT);
        tstack.push(JvmType.INT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iastore()
     */
    public void visit_iastore() {
        tstack.pop(JvmType.INT);
        tstack.pop(JvmType.INT);
        tstack.pop(JvmType.REFERENCE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iconst(int)
     */
    public void visit_iconst(int value) {
        tstack.push(JvmType.INT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_idiv()
     */
    public void visit_idiv() {
        tstack.pop(JvmType.INT);
        tstack.pop(JvmType.INT);
        tstack.push(JvmType.INT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iinc(int, int)
     */
    public void visit_iinc(int index, int incValue) {
        // No change
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iload(int)
     */
    public void visit_iload(int index) {
        tstack.push(JvmType.INT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_imul()
     */
    public void visit_imul() {
        tstack.pop(JvmType.INT);
        tstack.pop(JvmType.INT);
        tstack.push(JvmType.INT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ineg()
     */
    public void visit_ineg() {
        tstack.pop(JvmType.INT);
        tstack.push(JvmType.INT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_instanceof(org.jnode.vm.classmgr.VmConstClass)
     */
    public void visit_instanceof(VmConstClass clazz) {
        tstack.pop(JvmType.REFERENCE);
        tstack.push(JvmType.INT);
    }

    private final void popArguments(VmConstMethodRef methodRef) {
        final int[] types = JvmType.getArgumentTypes(methodRef.getSignature());
        for (int i = types.length - 1; i >= 0; i--) {
            tstack.pop(types[i]);
        }
    }

    private final void pushReturnValue(VmConstMethodRef methodRef) {
        final int type = JvmType.getReturnType(methodRef.getSignature());
        if (type != JvmType.VOID) {
            tstack.push(type);
        }
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_invokeinterface(org.jnode.vm.classmgr.VmConstIMethodRef, int)
     */
    public void visit_invokeinterface(VmConstIMethodRef methodRef, int count) {
        popArguments(methodRef);
        tstack.pop(JvmType.REFERENCE);
        pushReturnValue(methodRef);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_invokespecial(org.jnode.vm.classmgr.VmConstMethodRef)
     */
    public void visit_invokespecial(VmConstMethodRef methodRef) {
        popArguments(methodRef);
        tstack.pop(JvmType.REFERENCE);
        pushReturnValue(methodRef);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_invokestatic(org.jnode.vm.classmgr.VmConstMethodRef)
     */
    public void visit_invokestatic(VmConstMethodRef methodRef) {
        popArguments(methodRef);
        pushReturnValue(methodRef);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_invokevirtual(org.jnode.vm.classmgr.VmConstMethodRef)
     */
    public void visit_invokevirtual(VmConstMethodRef methodRef) {
        popArguments(methodRef);
        tstack.pop(JvmType.REFERENCE);
        pushReturnValue(methodRef);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ior()
     */
    public void visit_ior() {
        tstack.pop(JvmType.INT);
        tstack.pop(JvmType.INT);
        tstack.push(JvmType.INT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_irem()
     */
    public void visit_irem() {
        tstack.pop(JvmType.INT);
        tstack.pop(JvmType.INT);
        tstack.push(JvmType.INT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ishl()
     */
    public void visit_ishl() {
        tstack.pop(JvmType.INT);
        tstack.pop(JvmType.INT);
        tstack.push(JvmType.INT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ishr()
     */
    public void visit_ishr() {
        tstack.pop(JvmType.INT);
        tstack.pop(JvmType.INT);
        tstack.push(JvmType.INT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_istore(int)
     */
    public void visit_istore(int index) {
        tstack.pop(JvmType.INT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_isub()
     */
    public void visit_isub() {
        tstack.pop(JvmType.INT);
        tstack.pop(JvmType.INT);
        tstack.push(JvmType.INT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iushr()
     */
    public void visit_iushr() {
        tstack.pop(JvmType.INT);
        tstack.pop(JvmType.INT);
        tstack.push(JvmType.INT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ixor()
     */
    public void visit_ixor() {
        tstack.pop(JvmType.INT);
        tstack.pop(JvmType.INT);
        tstack.push(JvmType.INT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_l2d()
     */
    public void visit_l2d() {
        tstack.pop(JvmType.LONG);
        tstack.push(JvmType.DOUBLE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_l2f()
     */
    public void visit_l2f() {
        tstack.pop(JvmType.LONG);
        tstack.push(JvmType.FLOAT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_l2i()
     */
    public void visit_l2i() {
        tstack.pop(JvmType.LONG);
        tstack.push(JvmType.INT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ladd()
     */
    public void visit_ladd() {
        tstack.pop(JvmType.LONG);
        tstack.pop(JvmType.LONG);
        tstack.push(JvmType.LONG);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_laload()
     */
    public void visit_laload() {
        tstack.pop(JvmType.INT);
        tstack.pop(JvmType.REFERENCE);
        tstack.push(JvmType.LONG);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_land()
     */
    public void visit_land() {
        tstack.pop(JvmType.LONG);
        tstack.pop(JvmType.LONG);
        tstack.push(JvmType.LONG);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lastore()
     */
    public void visit_lastore() {
        tstack.pop(JvmType.LONG);
        tstack.pop(JvmType.INT);
        tstack.pop(JvmType.REFERENCE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lcmp()
     */
    public void visit_lcmp() {
        tstack.pop(JvmType.LONG);
        tstack.pop(JvmType.LONG);
        tstack.push(JvmType.INT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lconst(long)
     */
    public void visit_lconst(long value) {
        tstack.push(JvmType.LONG);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ldc(org.jnode.vm.classmgr.VmConstClass)
     */
    public void visit_ldc(VmConstClass value) {
        tstack.push(JvmType.REFERENCE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ldc(org.jnode.vm.classmgr.VmConstString)
     */
    public void visit_ldc(VmConstString value) {
        tstack.push(JvmType.REFERENCE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ldiv()
     */
    public void visit_ldiv() {
        tstack.pop(JvmType.LONG);
        tstack.pop(JvmType.LONG);
        tstack.push(JvmType.LONG);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lload(int)
     */
    public void visit_lload(int index) {
        tstack.push(JvmType.LONG);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lmul()
     */
    public void visit_lmul() {
        tstack.pop(JvmType.LONG);
        tstack.pop(JvmType.LONG);
        tstack.push(JvmType.LONG);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lneg()
     */
    public void visit_lneg() {
        tstack.pop(JvmType.LONG);
        tstack.push(JvmType.LONG);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lor()
     */
    public void visit_lor() {
        tstack.pop(JvmType.LONG);
        tstack.pop(JvmType.LONG);
        tstack.push(JvmType.LONG);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lrem()
     */
    public void visit_lrem() {
        tstack.pop(JvmType.LONG);
        tstack.pop(JvmType.LONG);
        tstack.push(JvmType.LONG);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lshl()
     */
    public void visit_lshl() {
        tstack.pop(JvmType.INT);
        tstack.pop(JvmType.LONG);
        tstack.push(JvmType.LONG);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lshr()
     */
    public void visit_lshr() {
        tstack.pop(JvmType.INT);
        tstack.pop(JvmType.LONG);
        tstack.push(JvmType.LONG);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lstore(int)
     */
    public void visit_lstore(int index) {
        tstack.pop(JvmType.LONG);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lsub()
     */
    public void visit_lsub() {
        tstack.pop(JvmType.LONG);
        tstack.pop(JvmType.LONG);
        tstack.push(JvmType.LONG);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lushr()
     */
    public void visit_lushr() {
        tstack.pop(JvmType.INT);
        tstack.pop(JvmType.LONG);
        tstack.push(JvmType.LONG);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lxor()
     */
    public void visit_lxor() {
        tstack.pop(JvmType.LONG);
        tstack.pop(JvmType.LONG);
        tstack.push(JvmType.LONG);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_monitorenter()
     */
    public void visit_monitorenter() {
        tstack.pop(JvmType.REFERENCE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_monitorexit()
     */
    public void visit_monitorexit() {
        tstack.pop(JvmType.REFERENCE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_multianewarray(org.jnode.vm.classmgr.VmConstClass, int)
     */
    public void visit_multianewarray(VmConstClass clazz, int dimensions) {
        for (int i = 0; i < dimensions; i++) {
            tstack.pop(JvmType.INT);
        }
        tstack.push(JvmType.REFERENCE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_new(org.jnode.vm.classmgr.VmConstClass)
     */
    public void visit_new(VmConstClass clazz) {
        tstack.push(JvmType.REFERENCE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_newarray(int)
     */
    public void visit_newarray(int type) {
        tstack.pop(JvmType.INT);
        tstack.push(JvmType.REFERENCE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_nop()
     */
    public void visit_nop() {
        // No change
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_pop()
     */
    public void visit_pop() {
        tstack.pop();
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_pop2()
     */
    public void visit_pop2() {
        final int tv1 = tstack.pop();
        if (JvmType.getCategory(tv1) == 1) {
            tstack.pop();
        }
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_putfield(org.jnode.vm.classmgr.VmConstFieldRef)
     */
    public void visit_putfield(VmConstFieldRef fieldRef) {
        tstack.pop(JvmType.SignatureToType(fieldRef.getSignature()));
        tstack.pop(JvmType.REFERENCE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_putstatic(org.jnode.vm.classmgr.VmConstFieldRef)
     */
    public void visit_putstatic(VmConstFieldRef fieldRef) {
        tstack.pop(JvmType.SignatureToType(fieldRef.getSignature()));
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_saload()
     */
    public void visit_saload() {
        tstack.pop(JvmType.INT);
        tstack.pop(JvmType.REFERENCE);
        tstack.push(JvmType.INT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_sastore()
     */
    public void visit_sastore() {
        tstack.pop(JvmType.INT);
        tstack.pop(JvmType.INT);
        tstack.pop(JvmType.REFERENCE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_swap()
     */
    public void visit_swap() {
        final int tv1 = tstack.pop();
        final int tv2 = tstack.pop();
        tstack.push(tv1);
        tstack.push(tv2);
    }
}
