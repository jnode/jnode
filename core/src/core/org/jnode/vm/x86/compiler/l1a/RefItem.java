/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.Label;
import org.jnode.assembler.x86.AbstractX86Stream;
import org.jnode.assembler.x86.Register;
import org.jnode.vm.classmgr.VmConstString;
import org.jnode.vm.x86.compiler.X86CompilerConstants;
import org.jnode.vm.x86.compiler.X86CompilerHelper;

/**
 * @author Patrik Reali
 */
 final class RefItem extends Item implements X86CompilerConstants {

	private Register reg;
	private VmConstString value;

	// generate unique labels for writeStatics (should use current label)
	private long labelCounter;

	/**
	 * @param kind
	 * @param reg
	 * @param val
	 * @param offsetToFP
	 */
	private RefItem(int kind, Register reg, VmConstString val, int offsetToFP) {
		super(kind, JvmType.REFERENCE, offsetToFP);
		this.reg = reg;
		this.value = val;
	}

	Register getRegister() {
		myAssert(getKind() == Kind.REGISTER);
		return reg;
	}
	
	VmConstString getValue() {
		myAssert(getKind() == Kind.CONSTANT);
		return value;
	}
	
	/**
	 * load item with register reg. Assumes that reg is properly allocated
	 * 
	 * @param ec current emitter context
	 * @param reg register to load the item to
	 */
	void loadTo(EmitterContext ec, Register reg) {
		myAssert(reg != null);
		AbstractX86Stream os = ec.getStream();
		X86RegisterPool pool = ec.getPool();
		myAssert(!pool.isFree(reg));

		switch (getKind()) {
			case Kind.REGISTER:
				if (this.reg != reg) {
					release(ec);
					os.writeMOV(INTSIZE, reg, this.reg);
				}
				break;
				
			case Kind.LOCAL:
				os.writeMOV(INTSIZE, reg, FP, getOffsetToFP());
				break;
				
			case Kind.CONSTANT:
				if (value == null) {
					os.writeMOV_Const(reg, value);
				} else {
					X86CompilerHelper helper = ec.getHelper();
					Label l = new Label(Long.toString(labelCounter++));
					helper.writeGetStaticsEntry(l, reg, value);
				}
				break;
				
			case Kind.FREGISTER:
				notImplemented();
				break;
				
			case Kind.STACK:
				if (VirtualStack.checkOperandStack) {
					final VirtualStack stack = ec.getVStack();
					stack.popFromOperandStack(this);
				}
				os.writePOP(reg);
				break;

		}
		kind = Kind.REGISTER;
		this.reg = reg;
	}
	
	/* (non-Javadoc)
	 * @see org.jnode.vm.x86.compiler.l1a.Item#load()
	 */
	void load(EmitterContext ec) {
		if (kind != Kind.REGISTER) {
			final X86RegisterPool pool = ec.getPool();
			Register r = (Register)pool.request(JvmType.INT, this);
			if (r == null) {
				final VirtualStack vstack = ec.getVStack();
				vstack.push(ec);
				r = (Register)pool.request(JvmType.INT, this);
			}
			myAssert(r != null);
			loadTo(ec, r);	
		}
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.x86.compiler.l1a.Item#loadToFPU()
	 */
	void loadToFPU(EmitterContext ec) {
		// TODO Auto-generated method stub
		notImplemented();

	}

	/**
	 * Load item into the given register (only for Category 1 items), if its kind
	 * matches the mask.
	 * 
	 * @param t0 the destination register
	 */
	void loadToIf(EmitterContext ec, int mask, Register t0) {
		if ((getKind() & mask) > 0)
			loadTo(ec, t0);
	}
	
	/* (non-Javadoc)
	 * @see org.jnode.vm.x86.compiler.l1a.Item#clone()
	 */
	Item clone(EmitterContext ec) {
		Item res = null;
		switch (getKind()) {
			case Kind.REGISTER:
				final X86RegisterPool pool = ec.getPool();
				final Register r = (Register)pool.request(JvmType.INT);
				res = createRegister(r);
				pool.transferOwnerTo(r, res);
				break;
				
			case Kind.LOCAL:
				res = createLocal(getOffsetToFP());
				break;
				
			case Kind.CONSTANT:
				res = createConst(value);
				break;
				
			case Kind.FREGISTER:
				//TODO
				notImplemented();
				break;
			
			case Kind.STACK:
				AbstractX86Stream os = ec.getStream();
				os.writePUSH(Register.SP, 0);
				res = createStack();
				if (VirtualStack.checkOperandStack) {
					final VirtualStack stack = ec.getVStack();
					stack.pushOnOperandStack(res);
				}
				break;
		}
		return res;
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.x86.compiler.l1a.Item#push()
	 */
	void push(EmitterContext ec) {
		final AbstractX86Stream os = ec.getStream();
		
		switch (getKind()) {
			case Kind.REGISTER:
				os.writePUSH(reg);
				break;
				
			case Kind.LOCAL:
				os.writePUSH(FP, offsetToFP);
				break;
				
			case Kind.CONSTANT:
				if (value == null) {
					os.writePUSH_Const(null);
				} else {
					X86CompilerHelper helper = ec.getHelper();
					Label l = new Label(Long.toString(labelCounter++));
					helper.writePushStaticsEntry(l, value);
				}
				break;
				
			case Kind.FREGISTER:
				//TODO
				notImplemented();
				break;
				
			case Kind.STACK:
				//nothing to do
				if (VirtualStack.checkOperandStack) {
					final VirtualStack stack = ec.getVStack();
				
					if (kind == Kind.STACK) {
						// the item is not really pushed and popped
						// but this checks that it is really the top
						// element
						stack.popFromOperandStack(this);
					}
				}
				break;

		}
		release(ec);
		kind = Kind.STACK;
		
		if (VirtualStack.checkOperandStack) {
			final VirtualStack stack = ec.getVStack();
			stack.pushOnOperandStack(this);
		}
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.x86.compiler.l1a.Item#release()
	 */
	void release(EmitterContext ec) {
		final X86RegisterPool pool = ec.getPool();

		switch (getKind()) {
			case Kind.REGISTER:
				pool.release(reg);
				break;
				
			case Kind.LOCAL:
				// nothing to do
				break;
				
			case Kind.CONSTANT:
				// nothing to do
				break;
				
			case Kind.FREGISTER:
				notImplemented();
				break;
				
			case Kind.STACK:
				//nothing to do
				break;

		}
	}

	/**
	 * @see org.jnode.vm.x86.compiler.l1a.Item#spill(EmitterContext, Register)
	 */
	void spill(EmitterContext ec, Register reg) {
		myAssert((getKind() == Kind.REGISTER) && (this.reg == reg));
		X86RegisterPool pool = ec.getPool();
		Register r = (Register)pool.request(JvmType.REFERENCE);
		myAssert(r != null);
		loadTo(ec, r);
		pool.transferOwnerTo(r, this);
	}
	
	/* (non-Javadoc)
	 * @see org.jnode.vm.x86.compiler.l1a.Item#uses(org.jnode.assembler.x86.Register)
	 */
	boolean uses(Register reg) {
		return ((kind == Kind.REGISTER) && this.reg.equals(reg));
	}

	static RefItem createRegister(Register reg) {
		return new RefItem(Kind.REGISTER, reg, null, 0);
	}
	
	static RefItem createConst(VmConstString value) {
		return new RefItem(Kind.CONSTANT, null, value, 0);
	}

	static RefItem createLocal(int offsetToFP) {
		return new RefItem(Kind.LOCAL, null, null, offsetToFP);
	}
	
	static RefItem createStack() {
		return new RefItem(Kind.STACK, null, null, 0);
	}


}
