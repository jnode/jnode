/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.x86.Register;

/**
 * @author Patrik Reali
 */

// TODO: work with Items to keep track of each item's stack level until popped and ensure consistency and correctness

final class VirtualStack {
	Item[] stack;
	
	// top of stack; stack[tos] is not part of the stack!
	int	tos;
	
	/**
	 * 
	 * Constructor; create and initialize stack with default size 
	 */
	VirtualStack() {
		stack = new Item[8];
		tos = 0;
	}
	
	int TOS() {
		return tos;
	}
	
	/**
	 * Increase stack size
	 */
	private void growStack() {
		Item[] tmp = new Item[stack.length * 2];
		System.arraycopy(stack, 0, tmp, 0, stack.length);
		stack = tmp;
	}

	/**
	 * Pop top item from stack.
	 * If no item on the stack, return UNKNOWN item (avoiding
	 * this requires knowing the stack contents across basic blocks)
	 * 
	 * Use popItem as far as possible, but the brain-dead implementation
	 * of all dup opcodes (from the 2nd edition of the spec) allows popping
	 * elements without knowing their type.
	 * 
	 * @return top item
	 */
	Item popItem() {
		if (tos == 0)
			//TODO: support items across basic blocks
			pushStack(Item.UNKNOWN);
		tos--;
		Item i = stack[tos];
		stack[tos] = null;
		return i;
	}

	/**
	 * Pop top item from stack, check its type also.
	 * 
	 * @param type
	 * @return
	 * @exception VerifyError if the type does not correspond
	 */
	Item popItem(int type) {
		if (tos == 0)
			pushStack(type);
		tos--;
		Item i = stack[tos];
		stack[tos] =null;
		if (i.getType() != type)
			throw new VerifyError();
		return i;
	}
	
	IntItem popInt() {
		return (IntItem)popItem(Item.INT);
	}
	
	RefItem popRef() {
		return (RefItem)popItem(Item.REFERENCE);
	}

	void pushItem(Item item) {
		if (tos == stack.length) 
			growStack();
		stack[tos++] = item;		
	}
	
	//TODO: deprecated
	void pushStack(int type) {
		Item res = null;
		switch (type) {
			case Item.INT:
				res = IntItem.createStack();
				break;
			case Item.REFERENCE:
				res = RefItem.createStack();
				break;
			case Item.LONG:
				res = LongItem.createStack();
				break;
			case Item.FLOAT:
				res = FloatItem.createStack();
				break;
			case Item.DOUBLE:
				res = DoubleItem.createStack();
				break;			
		}
		pushItem(res);
	}
	
	/**
	 * load every instance of local with given index into a register
	 * (used to avoid aliasing problems) 
	 * 
	 * @param index
	 */
	void loadLocal(EmitterContext ec, int index) {
		for (int i = 0; i < tos; i++) {
			final Item item = stack[i];
			if ((item.getKind() == Item.LOCAL) && (item.getOffsetToFP() == index))
					item.load(ec);
		}
		
	}
	
	void push(EmitterContext ec) {
		int i = 0;
		while ((i < tos) && (stack[i].getKind() == Item.STACK))
			i++;
		while (i < tos) {
			Item item = stack[i];
			Item.myAssert (item.getKind() != Item.STACK);
			item.push(ec);
			i++;
		}
	}

	/**
	 * Allocate register. If not free, then free it (it must be somewhere in the stack!)
	 * 
	 * @param context
	 * @param reg
	 */
	 void requestRegister(EmitterContext context, Register reg) {
	 	final X86RegisterPool pool = context.getPool();
	 	if (!pool.isFree(reg)) {
			// TODO Auto-generated method stub
	 		Item.notImplemented();
	 	}
	 	if (!pool.request(reg)) {
	 		Item.notImplemented();
	 	}
	 	
	}

}
