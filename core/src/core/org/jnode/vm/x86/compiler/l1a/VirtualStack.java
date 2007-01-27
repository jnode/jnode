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
 
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.x86.X86Assembler;
import org.jnode.assembler.x86.X86Register;
import org.jnode.vm.JvmType;
import org.jnode.vm.Vm;
import org.jnode.vm.bytecode.TypeStack;
import org.jnode.vm.x86.compiler.AbstractX86StackManager;

/**
 * @author Patrik Reali
 */

// TODO: work with Items to keep track of each item's stack level until popped
// and ensure consistency and correctness
final class VirtualStack {

	// explicitely check that elements on the operant stack
	// are popped in the appropriate order
	static final boolean checkOperandStack = true;

	// explicitely check that elements on the FPU stack
	// are popped in the appropriate order
	static final boolean checkFpuStack = true;

	// the virtual stack
	private Item[] stack;

	// top of stack; stack[tos] is not part of the stack!
	private int tos;

	final ItemStack operandStack;

	final FPUStack fpuStack = new FPUStack();

	/**
	 * 
	 * Constructor; create and initialize stack with default size
	 */
	VirtualStack(X86Assembler os) {
		this.operandStack = checkOperandStack ? new ItemStack(Item.Kind.STACK,
				Integer.MAX_VALUE) : null;
		reset();
	}

	void reset() {
		stack = new Item[8];
		tos = 0;
		if (checkOperandStack) {
			operandStack.reset();
		}
	}

	int TOS() {
		return tos;
	}

	boolean isEmpty() {
		return (tos == 0);
	}

	/**
	 * Increase stack size
	 */
	private void growStack() {
		final Item[] tmp = new Item[stack.length * 2];
		System.arraycopy(stack, 0, tmp, 0, stack.length);
		stack = tmp;
	}

	/**
	 * Pop top item from stack. If no item on the stack, return UNKNOWN item
	 * (avoiding this requires knowing the stack contents across basic blocks)
	 * 
	 * Use pop as far as possible, but the brain-dead implementation of all dup
	 * opcodes (from the 2nd edition of the spec) allows popping elements
	 * without knowing their type.
	 * 
	 * @return top item
	 */
	Item pop() {
		// do not autocreate item: if no type available, just fail; avoid this
		// case in the bytecode visitor
		// if (tos == 0)
		// //TODO: support items across basic blocks
		// pushStack(Item.UNKNOWN);
		tos--;
		final Item i = stack[tos];
		stack[tos] = null;
		return i;
	}

	/**
	 * Equals to pop, but also pops of the operand stack if the popped item is
	 * on the stack.
	 * 
	 * @return
	 */
	Item pop1() {
		final Item i = pop();
		if (checkOperandStack && i.isStack()) {
			operandStack.pop(i);
		}
		return i;
	}

	/**
	 * Pop top item from stack, check its type also. If none is present, create
	 * a new stack item with the given type
	 * 
	 * @param type
	 * @return pop the top of stack item
	 * @exception VerifyError
	 *                if the type does not correspond
	 */
	Item pop(int type) {
		// if (tos == 0) {
		// // the item requested in not on the virtual stack
		// // but already on the operand stack (it was pushed
		// // outside the current basic block)
		// // thus create a new stack item
		// Item it = createStack(type);
		// if (checkOperandStack) {
		// // insert at the begin of stack
		// // even if the vstack is empty, there
		// // may still be items popped from vstack
		// // that are not popped from operand stack
		// prependToOperandStack(it);
		// }
		// return it;
		// // pushStack(type);
		// }
		tos--;
		final Item i = stack[tos];
		stack[tos] = null;
		if (i.getType() != type)
			throw new VerifyError("Expected:" + Integer.toString(type)
					+ " Actual:" + Integer.toString(i.getType()));
		return i;
	}

	/**
	 * Pop an item of the stack. If the type is different from INT, an exception
	 * is thrown.
	 * 
	 * @return
	 */
	final IntItem popInt() {
		// testing in pop and casting here: test is just redundant
		return (IntItem) pop(JvmType.INT);
	}

	/**
	 * Pop an item of the stack. If the type is different from LONG, an
	 * exception is thrown.
	 * 
	 * @return
	 */
	final LongItem popLong() {
		// testing in pop and casting here: test is just redundant
		return (LongItem) pop(JvmType.LONG);
	}

	/**
	 * Pop an item of the stack. If the type is different from REFERENCE, an
	 * exception is thrown.
	 * 
	 * @return
	 */
	final RefItem popRef() {
		// testing in pop and casting here: test is just redundant
		return (RefItem) pop(JvmType.REFERENCE);
	}

	/**
	 * Pop an item of the stack. If the type is different from FLOAT, an
	 * exception is thrown.
	 * 
	 * @return
	 */
	final FloatItem popFloat() {
		// testing in pop and casting here: test is just redundant
		return (FloatItem) pop(JvmType.FLOAT);
	}

	/**
	 * Pop an item of the stack. If the type is different from REFERENCE, an
	 * exception is thrown.
	 * 
	 * @return
	 */
	final DoubleItem popDouble() {
		// testing in pop and casting here: test is just redundant
		return (DoubleItem) pop(JvmType.DOUBLE);
	}

	/**
	 * Push item on stack. If the item is on the FPU stack, it is also pushed on
	 * fpuStack.
	 */
	void push(Item item) {
		if (Vm.VerifyAssertions)
			Vm._assert(item.getKind() > 0, "Kind > 0");
		if ((item.isStack()) && (tos > 0)) {
			if (Vm.VerifyAssertions)
				Vm._assert(stack[tos - 1].isStack(),
						"stack[ tos - 1].isStack()");
		}

		if (tos == stack.length) {
			growStack();
		}

		stack[tos++] = item;
	}

	/**
	 * Push on vstack and operand stack (special case for old-style code, to be
	 * eventually removed)
	 */
	void push1(Item item) {
		push(item);
		if (checkOperandStack && (item.getKind() == Item.Kind.STACK)) {
			operandStack.push(item);
		}
	}

	/**
	 * Does this stack contain the given item.
	 * 
	 * @param item
	 * @return
	 */
	final boolean contains(Item item) {
		for (int i = 0; i < tos; i++) {
			if (stack[i] == item)
				return true;
		}
		return false;
	}

	/**
	 * load every instance of local with given index into a register (used to
	 * avoid aliasing problems)
	 * 
	 * @param offsetToFP
	 */
	void loadLocal(EmitterContext ec, int offsetToFP) {
		for (int i = 0; i < tos; i++) {
			final Item item = stack[i];
			if (item.isLocal() && item.isAtOffset(offsetToFP)) {
				item.load(ec);
			}
		}

	}

	/**
	 * Push all items on the virtual stack to the actual stack.
	 * 
	 * @param ec
	 */
	final int push(EmitterContext ec) {
		int i = 0;
		while ((i < tos) && (stack[i].getKind() == Item.Kind.STACK)) {
			i++;
		}
		int cnt = 0;
		while (i < tos) {
			final Item item = stack[i];
			if (Vm.VerifyAssertions)
				Vm._assert(item.getKind() != Item.Kind.STACK,
						"item.getKind() != Item.Kind.STACK");
			item.push(ec);
			i++;
			cnt++;
		}
		return cnt;
	}

	/**
	 * Push items on the virtual stack to the actual stack until there are no
	 * more volative registers in use on the stack.
	 * 
	 * @param ec
	 */
	// final int pushAllVolatile(EmitterContext ec) {
	// int i = 0;
	// while ((i < tos) && stack[i].isStack()) {
	// i++;
	// }
	// int cnt = 0;
	// final int max = findTopVolatileRegisterIndex(ec.getPool());
	// while (i <= max) {
	// final Item item = stack[i];
	// Item.assertCondition(!item.isStack(), "!item.isStack()");
	// item.push(ec);
	// i++;
	// cnt++;
	// }
	// return cnt;
	// }
//	/**
//	 * Find the largest index that contains a volatile register.
//	 * 
//	 * @return
//	 */
//	private final int findTopVolatileRegisterIndex(X86RegisterPool pool) {
//		for (int i = tos - 1; i >= 0; i--) {
//			if (stack[i].usesVolatileRegister(pool)) {
//				return i;
//			}
//		}
//		return -1;
//	}

	// private void prependToOperandStack(Item item) {
	// os.log("prepend");
	// Item.myAssert(item.getKind() == Item.Kind.STACK);
	//
	// if (operandTos == operandStack.length) growOperandStack();
	//
	// for (int i = operandTos; i > 0; i--)
	// operandStack[ i] = operandStack[ i - 1];
	//
	// operandTos++;
	// operandStack[ 0] = item;
	// }
	//

	boolean uses(X86Register reg) {
		for (int i = 0; i < tos; i++) {
			if (stack[i].uses(reg)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Create a typestack reflecting the state of this stack.
	 * 
	 * @return
	 */
	TypeStack asTypeStack() {
		final TypeStack tstack = new TypeStack();
		for (int i = 0; i < tos; i++) {
			tstack.push(stack[i].getType());
		}
		return tstack;
	}

	/**
	 * Push items (kind = STACK) on stack for each type in the given typestack.
	 * 
	 * @param tstack
	 *            May be null or empty
	 */
	final void pushAll(ItemFactory ifac, TypeStack tstack) {
		if ((tstack != null) && !tstack.isEmpty()) {
			final int size = tstack.size();
			for (int i = 0; i < size; i++) {
				final int type = tstack.getType(i);
				final Item item = ifac.createStack(type);
				push(item);
				if (VirtualStack.checkOperandStack) {
					operandStack.push(item);
				}
			}
		}
	}

	final AbstractX86StackManager createStackMgr(X86RegisterPool pool,
			ItemFactory ifac) {
		return new StackManagerImpl(pool, ifac);
	}

	final void initializeStackMgr(AbstractX86StackManager stackMgr, EmitterContext ec) {
		((StackManagerImpl)stackMgr).setEmitterContext(ec);
	}

	public String toString() {
		if (tos == 0) {
			return "EMPTY";
		}
		final StringBuilder buf = new StringBuilder();
		for (int i = 0; i < tos; i++) {
			if (i != 0) {
				buf.append(',');
			}
			buf.append('(');
			buf.append(JvmType.toString(stack[i].getType()));
			buf.append(',');
			buf.append(Item.Kind.toString(stack[i].getKind()));
			buf.append(')');
		}
		buf.append("TOS");
		return buf.toString();
	}

	final void visitItems(ItemVisitor visitor) {
		for (int i = 0; i < tos; i++) {
			visitor.visit(stack[i]);
		}
	}

	final class StackManagerImpl implements AbstractX86StackManager {

		private EmitterContext ec;

		private final X86RegisterPool pool;

		private final ItemFactory ifac;

		public StackManagerImpl(X86RegisterPool pool, ItemFactory ifac) {
			this.pool = pool;
			this.ifac = ifac;
		}			

		/**
		 * @see org.jnode.vm.x86.compiler.AbstractX86StackManager#writePUSH(int,
		 *      org.jnode.assembler.x86.Register)
		 */
		public void writePUSH(int jvmType, X86Register.GPR reg) {
			final Item item = ifac.createReg(ec, jvmType, reg);
            final boolean ok = pool.request(reg, item);
			if (Vm.VerifyAssertions)
				Vm._assert(ok, "request");
			push(item);
		}

		/**
		 * @see org.jnode.vm.x86.compiler.AbstractX86StackManager#writePUSH64(int,
		 *      org.jnode.assembler.x86.Register,
		 *      org.jnode.assembler.x86.Register)
		 */
		public void writePUSH64(int jvmType, X86Register.GPR lsbReg,
				X86Register.GPR msbReg) {
			final Item item = ifac.createReg(ec, jvmType, lsbReg, msbReg);
            final boolean lsbOk = pool.request(lsbReg, item);
            final boolean msbOk = pool.request(msbReg, item);
			if (Vm.VerifyAssertions) {
				Vm._assert(lsbOk, "request-lsb");
				Vm._assert(msbOk, "request-msb");
			}
			push(item);
		}

		/**
		 * @see org.jnode.vm.x86.compiler.AbstractX86StackManager#writePUSH64(int,
		 *      GPR64)
		 */
		public void writePUSH64(int jvmType, X86Register.GPR64 reg) {
			final Item item = ifac.createReg(ec, jvmType, reg);
            final boolean ok = pool.request(reg, item); 
			if (Vm.VerifyAssertions)
				Vm._assert(ok, "request");
			push(item);
		}

		public final void setEmitterContext(EmitterContext ec) {
			this.ec = ec;
		}
	}
}
