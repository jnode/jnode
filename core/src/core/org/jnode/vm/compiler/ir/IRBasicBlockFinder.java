/*
 * $Id$
 */
package org.jnode.vm.compiler.ir;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.jnode.vm.bytecode.BasicBlockFinder;
import org.jnode.vm.bytecode.BytecodeVisitorSupport;
import org.jnode.vm.classmgr.VmByteCode;
import org.jnode.vm.classmgr.VmInterpretedExceptionHandler;
import org.jnode.vm.classmgr.VmMethod;

/**
 * @author Madhu Siddalingaiah
 * 
 */
// TODO simpify to use existing CFG from l1

public class IRBasicBlockFinder extends BytecodeVisitorSupport implements Comparator {
	private boolean nextIsStartOfBB;

	private IRBasicBlock currentBlock;

	private byte[] opcodeFlags;

	private boolean nextIsSuccessor;
	private final ArrayList blocks = new ArrayList();

	/**
	 * Create all determined basic blocks
	 * 
	 * @return
	 */
	public IRBasicBlock[] createBasicBlocks() {
		// Sort the blocks on start PC
		Collections.sort(blocks, this);
		// Create the array
		final IRBasicBlock[] list = (IRBasicBlock[])blocks.toArray(new IRBasicBlock[blocks.size()]);
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
				final IRBasicBlock bb = list[bbIndex++];
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
	 * @param method
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#startMethod(org.jnode.vm.classmgr.VmMethod)
	 */
	public void startMethod(VmMethod method) {
		final VmByteCode bc = method.getBytecode();
		final int length = bc.getLength();
		opcodeFlags = new byte[length];
		// The first instruction is always the start of a BB.
		this.currentBlock = startBB(0);
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
	}

	/**
	 * @param address
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifne(int)
	 */
	public void visit_ifne(int address) {
		addBranch(address);
	}

	/**
	 * @param address
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iflt(int)
	 */
	public void visit_iflt(int address) {
		addBranch(address);
	}

	/**
	 * @param address
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifge(int)
	 */
	public void visit_ifge(int address) {
		addBranch(address);
	}

	/**
	 * @param address
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifgt(int)
	 */
	public void visit_ifgt(int address) {
		addBranch(address);
	}

	/**
	 * @param address
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifle(int)
	 */
	public void visit_ifle(int address) {
		addBranch(address);
	}

	/**
	 * @param address
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmpeq(int)
	 */
	public void visit_if_icmpeq(int address) {
		addBranch(address);
	}

	/**
	 * @param address
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmpne(int)
	 */
	public void visit_if_icmpne(int address) {
		addBranch(address);
	}

	/**
	 * @param address
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmplt(int)
	 */
	public void visit_if_icmplt(int address) {
		addBranch(address);
	}

	/**
	 * @param address
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmpge(int)
	 */
	public void visit_if_icmpge(int address) {
		addBranch(address);
	}

	/**
	 * @param address
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmpgt(int)
	 */
	public void visit_if_icmpgt(int address) {
		addBranch(address);
	}

	/**
	 * @param address
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmple(int)
	 */
	public void visit_if_icmple(int address) {
		addBranch(address);
	}

	/**
	 * @param address
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_acmpeq(int)
	 */
	public void visit_if_acmpeq(int address) {
		addBranch(address);
	}

	/**
	 * @param address
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_acmpne(int)
	 */
	public void visit_if_acmpne(int address) {
		addBranch(address);
	}

	/**
	 * @param address
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_goto(int)
	 */
	public void visit_goto(int address) {
		addBranch(address);
		nextIsSuccessor = false;
	}

	/**
	 * @param address
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_jsr(int)
	 */
	public void visit_jsr(int address) {
		addBranch(address);
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
		}
		addBranch(defValue);
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
		}
		addBranch(defValue);
	}

	/**
	 * @param address
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifnull(int)
	 */
	public void visit_ifnull(int address) {
		addBranch(address);
	}

	/**
	 * @param address
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifnonnull(int)
	 */
	public void visit_ifnonnull(int address) {
		addBranch(address);
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_athrow()
	 */
	public void visit_athrow() {
		endBB();
		nextIsSuccessor = false;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_areturn()
	 */
	public void visit_areturn() {
		endBB();
		nextIsSuccessor = false;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dreturn()
	 */
	public void visit_dreturn() {
		endBB();
		nextIsSuccessor = false;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_freturn()
	 */
	public void visit_freturn() {
		endBB();
		nextIsSuccessor = false;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ireturn()
	 */
	public void visit_ireturn() {
		endBB();
		nextIsSuccessor = false;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lreturn()
	 */
	public void visit_lreturn() {
		endBB();
		nextIsSuccessor = false;
	}

	/**
	 * @param index
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ret(int)
	 */
	public void visit_ret(int index) {
		endBB();
		nextIsSuccessor = false;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_return()
	 */
	public void visit_return() {
		endBB();
		nextIsSuccessor = false;
	}

	/**
	 * Add branching information (to the given target) to the basic blocks information.
	 * 
	 * @param target
	 */
	private final void addBranch(int target) {
		IRBasicBlock pred = this.currentBlock;
		IRBasicBlock succ = startBB(target);
		pred.addSuccessor(succ);
		succ.addPredecessor(pred);
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
		super.startInstruction(address);
		opcodeFlags[address] |= BasicBlockFinder.F_START_OF_INSTRUCTION;
		if (nextIsStartOfBB) {
			IRBasicBlock pred = this.currentBlock;
			this.currentBlock = startBB(address);
			if (nextIsSuccessor) {
				pred.addSuccessor(this.currentBlock);
				this.currentBlock.addPredecessor(pred);
			}
			nextIsSuccessor = true;
			nextIsStartOfBB = false;
		} else if (address != currentBlock.getStartPC() &&
			(opcodeFlags[address] & BasicBlockFinder.F_START_OF_BASICBLOCK) != 0 && nextIsSuccessor) {

			int n = blocks.size();
			for (int i=0; i<n; i+=1) {
				IRBasicBlock bb = (IRBasicBlock) blocks.get(i);
				if (bb.getStartPC() == address) {
					IRBasicBlock pred = this.currentBlock;
					this.currentBlock = bb;
					pred.addSuccessor(this.currentBlock);
					this.currentBlock.addPredecessor(pred);
					break;
				}
			}
		}
	}

	/**
	 * Mark the start of a basic block
	 * 
	 * @param address
	 */
	private final IRBasicBlock startBB(int address) {
		IRBasicBlock next = null;
		if ((opcodeFlags[address] & BasicBlockFinder.F_START_OF_BASICBLOCK) == 0) {
			opcodeFlags[address] |= BasicBlockFinder.F_START_OF_BASICBLOCK;
			next = new IRBasicBlock(address);
			blocks.add(next);
		} else {
			int n = blocks.size();
			for (int i=0; i<n; i+=1) {
				IRBasicBlock bb = (IRBasicBlock) blocks.get(i);
				if (bb.getStartPC() == address) {
					next = bb;
					break;
				}
			}
		}
		return next;
	}

	private final boolean isStartOfBB(int address) {
		return ((opcodeFlags[address] & BasicBlockFinder.F_START_OF_BASICBLOCK) != 0);
	}

	private final boolean isStartOfException(int address) {
		return ((opcodeFlags[address] & BasicBlockFinder.F_START_OF_EXCEPTIONHANDLER) != 0);
	}

	/**
	 * Mark the start of a exception handler
	 * 
	 * @param address
	 */
	private final void startException(int address) {
		opcodeFlags[address] |= BasicBlockFinder.F_START_OF_EXCEPTIONHANDLER;
		startBB(address);
	}

	/**
	 * Mark the start of a try-catch block
	 * 
	 * @param address
	 */
	private final void startTryBlock(int address) {
		opcodeFlags[address] |= BasicBlockFinder.F_START_OF_TRYBLOCK;
		startBB(address);
	}

	/**
	 * Mark the end of a try-catch block
	 * 
	 * @param address
	 */
	private final void startTryBlockEnd(int address) {
		opcodeFlags[address] |= BasicBlockFinder.F_START_OF_TRYBLOCKEND;
		startBB(address);
	}

	public int compare(Object o1, Object o2) {
		final int sp1 = ((IRBasicBlock) o1).getStartPC();
		final int sp2 = ((IRBasicBlock) o2).getStartPC();
		return sp1 - sp2;
	}
}
