/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.x86.AbstractX86Stream;
import org.jnode.assembler.x86.Register;
import org.jnode.vm.x86.compiler.X86CompilerConstants;

/**
 * @author Patrik Reali
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
final class LongItem extends Item  implements X86CompilerConstants {

	private Register lsb;
	private Register msb;
	private long value;

	/**
	 * @param kind
	 * @param offsetToFP
	 * @param lsb
	 * @param msb
	 * @param val
	 */
	private LongItem(int kind, int offsetToFP, Register lsb, Register msb, long val) {
		super(kind, offsetToFP);

		this.lsb = lsb;
		this.msb = msb;
		this.value = val;
	}


	/**
	 * Get the JVM type of this item
	 * @return the JVM type
	 */
	int getType() { return JvmType.LONG; }
	
    /**
     * Return the current item's computational type category (JVM Spec, p. 83).
     * In practice, this is the number of double words needed by the item (1 or
     * 2)
     * 
     * @return computational type category
     */
    final int getCategory() {
        return 2;
    }

    /**
     * ?
     * @param ec
     * @param lsb
     * @param msb
     */
	private final void loadTo(EmitterContext ec, Register lsb, Register msb) {
		AbstractX86Stream os = ec.getStream();
		X86RegisterPool pool = ec.getPool();
		os.log("LongItem.log called "+Integer.toString(kind));		
		myAssert(lsb != msb);
		myAssert(lsb != null);
		myAssert(msb != null);
		switch (kind) {
			case Kind.REGISTER:
				// invariant: (msb != lsb) && (this.msb != this.lsb)
				if (msb != this.lsb) {
					// generic case; avoid only if msb is lsb' (value overwriting)
					// invariant: (msb != this.lsb) && (msb != lsb) && (this.msb != this.lsb)
					// msb <- msb'
					// lsb <- lsb'
					if (msb != this.msb) {
						os.writeMOV(INTSIZE, msb, this.msb);
						if (lsb != this.msb) {
							pool.release(this.msb);				
						}
					}
					if (lsb != this.lsb) {
						// invariant: (msb != this.lsb) && (lsb != this.lsb) && (msb != lsb) && (this.msb != this.lsb)
						os.writeMOV(INTSIZE, lsb, this.lsb);
						//if (msb != this.lsb) {	<- enforced by first if()
							pool.release(this.lsb);
						//}
					}
				} else if (lsb != this.msb){
					// generic case, assignment sequence inverted; avoid only if lsb is msb' (overwriting)
					// invariant: (msb == this.lsb) && (lsb != this.msb) 
					// lsb <- lsb'
					// msb <- msb'
					// if (lsb != this.lsb) {  <- always true, because msb == this.lsb
						os.writeMOV(INTSIZE, lsb, this.lsb);
					//	if (msb != this.lsb) {	<- always false, because of invariant
					//		pool.release(this.lsb);
					//	}
					// }					
					// if (msb != this.msb) { <- always true, because of invariant
						os.writeMOV(INTSIZE, msb, this.msb);
					//	if (lsb != this.msb) { <- always true, because of invariant
							pool.release(this.msb);				
					//	}
					// }
				} else {
					// invariant: (msb == this.lsb) && (lsb == this.msb)
					// swap registers
					//TODO: handle allocation failure
					Register reg = pool.request(JvmType.INT);
					os.writeMOV(INTSIZE, reg, this.lsb);
					os.writeMOV(INTSIZE, this.lsb, this.msb);
					os.writeMOV(INTSIZE, this.msb, reg);
					pool.release(reg);
				}
				break;
				
			case Kind.LOCAL:
				os.writeMOV(INTSIZE, lsb, FP, offsetToFP);
				os.writeMOV(INTSIZE, msb, FP, offsetToFP+4);
				break;
				
			case Kind.CONSTANT:
			    final int lsbv = (int) (value & 0xFFFFFFFFL);
			    final int msbv = (int) ((value >>> 32) & 0xFFFFFFFFL);

				os.writeMOV_Const(lsb, lsbv);
				os.writeMOV_Const(msb, msbv);
				break;
				
			case Kind.FREGISTER:
				//TODO
				notImplemented();
				break;
				
			case Kind.STACK:
				if (VirtualStack.checkOperandStack) {
					final VirtualStack stack = ec.getVStack();
					stack.popFromOperandStack(this);
				}
				os.writePOP(lsb);
				os.writePOP(msb);
				break;

		}
		kind = Kind.REGISTER;
		this.lsb = lsb;
		this.msb = msb;
	}
	
	/**
	 * @see org.jnode.vm.x86.compiler.l1a.Item#load(EmitterContext)
	 */
	final void load(EmitterContext ec) {
		if (kind != Kind.REGISTER) {
			X86RegisterPool pool = ec.getPool();
			
			final Register l = pool.request(JvmType.INT, this);
			final Register r = pool.request(JvmType.INT, this);
			myAssert(r != null);
			myAssert(l != null);
			loadTo(ec, l, r);
		}
	}

	/**
	 * @see org.jnode.vm.x86.compiler.l1a.Item#clone()
	 */
	Item clone(EmitterContext ec) {
		Item res = null;
		switch (getKind()) {
			case Kind.REGISTER:
				//TODO
				notImplemented();
				break;
				
			case Kind.LOCAL:
				//TODO
				notImplemented();
				break;
				
			case Kind.CONSTANT:
				//TODO
				notImplemented();
				break;
				
			case Kind.FREGISTER:
				//TODO
				notImplemented();
				break;
			
			case Kind.STACK:
				//TODO
				notImplemented();
				break;
		}
		return res;
	}

	/**
	 * @see org.jnode.vm.x86.compiler.l1a.Item#push(EmitterContext)
	 */
	final void push(EmitterContext ec) {
		final AbstractX86Stream os = ec.getStream();
		os.log("LongItem.push "+Integer.toString(getKind()));	
		switch (getKind()) {
			case Kind.REGISTER:
			    os.writePUSH(msb);
			    os.writePUSH(lsb);
				break;
				
			case Kind.LOCAL:
				os.writePUSH(FP, offsetToFP+4);
				os.writePUSH(FP, offsetToFP);
				break;
				
			case Kind.CONSTANT:
			    final int lsbv = (int) (value & 0xFFFFFFFFL);
			    final int msbv = (int) ((value >>> 32) & 0xFFFFFFFFL);

			    os.writePUSH(msbv);
			    os.writePUSH(lsbv);
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

	/**
	 * @see org.jnode.vm.x86.compiler.l1a.Item#release(EmitterContext)
	 */
	final void release(EmitterContext ec) {
		final X86RegisterPool pool = ec.getPool();
		
		switch (getKind()) {
			case Kind.REGISTER:
				pool.release(lsb);
				pool.release(msb);
				break;
				
			case Kind.LOCAL:
				// nothing to do
				break;
				
			case Kind.CONSTANT:
				// nothing to do
				break;
				
			case Kind.FREGISTER:
				//TODO
				notImplemented();
				break;
				
			case Kind.STACK:
				// nothing to do
				break;
		}
	}

	/**
	 * @see org.jnode.vm.x86.compiler.l1a.Item#spill(EmitterContext, Register)
	 */
	final void spill(EmitterContext ec, Register reg) {
		notImplemented();
	}

	/**
	 * @see org.jnode.vm.x86.compiler.l1a.Item#uses(org.jnode.assembler.x86.Register)
	 */
	final boolean uses(Register reg) {
		return ((kind == Kind.REGISTER) && (msb.equals(reg) || lsb.equals(reg)));
	}

	static LongItem createStack() {
		return new LongItem(Kind.STACK, 0, null, null, 0);
	}
	
	static LongItem createConst(long value) {
		return new LongItem(Kind.CONSTANT, 0, null, null, value);
	}
	
	static LongItem createReg(Register lsb, Register msb) {
		return new LongItem(Kind.REGISTER, 0, lsb, msb, 0);
	}
	
	static LongItem createLocal(int offsetToFP) {
		return new LongItem(Kind.LOCAL, offsetToFP, null, null, 0);
	}
}
