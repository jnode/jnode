/*
 * $Id$
 */
package org.jnode.vm.bytecode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.jnode.vm.classmgr.VmByteCode;
import org.jnode.vm.classmgr.VmConstIMethodRef;
import org.jnode.vm.classmgr.VmConstMethodRef;
import org.jnode.vm.classmgr.VmInterpretedExceptionHandler;
import org.jnode.vm.classmgr.VmMethod;

/**
 * Bytecode visitor, used to determine the start addresses of basic blocks.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Madhu Siddalingaiah
 */
public class BasicBlockFinder extends BytecodeVisitorSupport implements BytecodeFlags {

	private final ArrayList blocks = new ArrayList();
	private byte[] opcodeFlags;
	private boolean nextIsStartOfBB;
	private int curAddress;

	/**
	 * Create all determined basic blocks
	 * 
	 * @return
	 */
	public BasicBlock[] createBasicBlocks() {
		// Sort the blocks on start PC
		Collections.sort(blocks, BasicBlockComparator.INSTANCE);
		// Create the array
		final BasicBlock[] list = (BasicBlock[]) blocks.toArray(new BasicBlock[blocks.size()]);
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
		final VmByteCode bc = method.getBytecode();
		final int length = bc.getLength();
		opcodeFlags = new byte[length];
		// The first instruction is always the start of a BB.
		startBB(0);
		// The exception handler also start a basic block
		for (int i = 0; i < bc.getNoExceptionHandlers(); i++) {
			VmInterpretedExceptionHandler eh = bc.getExceptionHandler(i);
			startTryBlock(eh.getStartPC());
			startTryBlockEnd(eh.getEndPC());
			startException(eh.getHandlerPC());
		}
	}

	/**
	 * @param address
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifeq(int)
	 */
	public void visit_ifeq(int address) {
		addBranch(address);
		condYieldPoint(address);
	}

	/**
	 * @param address
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifne(int)
	 */
	public void visit_ifne(int address) {
		addBranch(address);
		condYieldPoint(address);
	}

	/**
	 * @param address
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iflt(int)
	 */
	public void visit_iflt(int address) {
		addBranch(address);
		condYieldPoint(address);
	}

	/**
	 * @param address
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifge(int)
	 */
	public void visit_ifge(int address) {
		addBranch(address);
		condYieldPoint(address);
	}

	/**
	 * @param address
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifgt(int)
	 */
	public void visit_ifgt(int address) {
		addBranch(address);
		condYieldPoint(address);
	}

	/**
	 * @param address
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifle(int)
	 */
	public void visit_ifle(int address) {
		addBranch(address);
		condYieldPoint(address);
	}

	/**
	 * @param address
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmpeq(int)
	 */
	public void visit_if_icmpeq(int address) {
		addBranch(address);
		condYieldPoint(address);
	}

	/**
	 * @param address
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmpne(int)
	 */
	public void visit_if_icmpne(int address) {
		addBranch(address);
		condYieldPoint(address);
	}

	/**
	 * @param address
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmplt(int)
	 */
	public void visit_if_icmplt(int address) {
		addBranch(address);
		condYieldPoint(address);
	}

	/**
	 * @param address
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmpge(int)
	 */
	public void visit_if_icmpge(int address) {
		addBranch(address);
		condYieldPoint(address);
	}

	/**
	 * @param address
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmpgt(int)
	 */
	public void visit_if_icmpgt(int address) {
		addBranch(address);
		condYieldPoint(address);
	}

	/**
	 * @param address
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmple(int)
	 */
	public void visit_if_icmple(int address) {
		addBranch(address);
		condYieldPoint(address);
	}

	/**
	 * @param address
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_acmpeq(int)
	 */
	public void visit_if_acmpeq(int address) {
		addBranch(address);
		condYieldPoint(address);
	}

	/**
	 * @param address
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_acmpne(int)
	 */
	public void visit_if_acmpne(int address) {
		addBranch(address);
		condYieldPoint(address);
	}

	/**
	 * @param address
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_goto(int)
	 */
	public void visit_goto(int address) {
		addBranch(address);
		condYieldPoint(address);
	}

	/**
	 * @param address
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_jsr(int)
	 */
	public void visit_jsr(int address) {
		addBranch(address);
		condYieldPoint(address);
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
			addBranch(addresses[i]);
			condYieldPoint(addresses[i]);
		}
		addBranch(defValue);
		condYieldPoint(defValue);
	}

	/**
	 * @param defValue
	 * @param matchValues
	 * @param addresses
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lookupswitch(int, int[], int[])
	 */
	public void visit_lookupswitch(int defValue, int[] matchValues, int[] addresses) {
		for (int i = 0; i < addresses.length; i++) {
			addBranch(addresses[i]);
			condYieldPoint(addresses[i]);
		}
		addBranch(defValue);
		condYieldPoint(defValue);
	}

	/**
	 * @param address
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifnull(int)
	 */
	public void visit_ifnull(int address) {
		addBranch(address);
		condYieldPoint(address);
	}

	/**
	 * @param address
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifnonnull(int)
	 */
	public void visit_ifnonnull(int address) {
		addBranch(address);
		condYieldPoint(address);
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_athrow()
	 */
	public void visit_athrow() {
		endBB();
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_areturn()
	 */
	public void visit_areturn() {
		endBB();
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dreturn()
	 */
	public void visit_dreturn() {
		endBB();
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_freturn()
	 */
	public void visit_freturn() {
		endBB();
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ireturn()
	 */
	public void visit_ireturn() {
		endBB();
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lreturn()
	 */
	public void visit_lreturn() {
		endBB();
	}

	/**
	 * @param index
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ret(int)
	 */
	public void visit_ret(int index) {
		endBB();
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_return()
	 */
	public void visit_return() {
		endBB();
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_invokeinterface(org.jnode.vm.classmgr.VmConstIMethodRef, int)
	 */
	public void visit_invokeinterface(VmConstIMethodRef methodRef, int count) {
		yieldPoint();
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_invokespecial(org.jnode.vm.classmgr.VmConstMethodRef)
	 */
	public void visit_invokespecial(VmConstMethodRef methodRef) {
		yieldPoint();
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_invokestatic(org.jnode.vm.classmgr.VmConstMethodRef)
	 */
	public void visit_invokestatic(VmConstMethodRef methodRef) {
		yieldPoint();
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_invokevirtual(org.jnode.vm.classmgr.VmConstMethodRef)
	 */
	public void visit_invokevirtual(VmConstMethodRef methodRef) {
		yieldPoint();
	}

	/**
	 * Add branching information (to the given target) to the basic blocks information.
	 * 
	 * @param target
	 */
	private final void addBranch(int target) {
		startBB(target);
		endBB();
	}

	/**
	 * Mark the end of a basic block
	 */
	private final void endBB() {
		nextIsStartOfBB = true;
	}

	/**
	 * @param address
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#startInstruction(int)
	 */
	public void startInstruction(int address) {
		curAddress = address;
		super.startInstruction(address);
		opcodeFlags[address] |= F_START_OF_INSTRUCTION;
		if (nextIsStartOfBB) {
			startBB(address);
			nextIsStartOfBB = false;
		}
	}

	/**
	 * Mark the start of a basic block
	 * 
	 * @param address
	 */
	private final void startBB(int address) {
		if ((opcodeFlags[address] & F_START_OF_BASICBLOCK) == 0) {
			opcodeFlags[address] |= F_START_OF_BASICBLOCK;
			blocks.add(new BasicBlock(address));
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
	private final void startException(int address) {
		opcodeFlags[address] |= F_START_OF_EXCEPTIONHANDLER;
		startBB(address);
	}

	/**
	 * Mark the start of a try-catch block
	 * 
	 * @param address
	 */
	private final void startTryBlock(int address) {
		opcodeFlags[address] |= F_START_OF_TRYBLOCK;
		startBB(address);
	}

	/**
	 * Mark the end of a try-catch block
	 * 
	 * @param address
	 */
	private final void startTryBlockEnd(int address) {
		opcodeFlags[address] |= F_START_OF_TRYBLOCKEND;
		startBB(address);
	}

	/**
	 * Mark a yieldpoint.
	 */
	private final void yieldPoint() {
		opcodeFlags[curAddress] |= F_YIELDPOINT;
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
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 * @return int
		 */
		public int compare(Object o1, Object o2) {
			final int sp1 = ((BasicBlock) o1).getStartPC();
			final int sp2 = ((BasicBlock) o2).getStartPC();
			return sp1 - sp2;
		}
	}

}
