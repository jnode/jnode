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
		super(kind, LONG, offsetToFP);

		this.lsb = lsb;
		this.msb = msb;
		this.value = val;
	}

	void loadTo(EmitterContext ec, Register lsb, Register msb) {
		AbstractX86Stream os = ec.getStream();
		X86RegisterPool pool = ec.getPool();
		
		myAssert(lsb != msb);
		switch (kind) {
			case REGISTER:
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
					Register reg = (Register)pool.request(INT);
					os.writeMOV(INTSIZE, reg, this.lsb);
					os.writeMOV(INTSIZE, this.lsb, this.msb);
					os.writeMOV(INTSIZE, this.msb, reg);
					pool.release(reg);
				}
				break;
				
			case LOCAL:
				os.writeMOV(INTSIZE, lsb, FP, getOffsetToFP());
				os.writeMOV(INTSIZE, msb, FP, getOffsetToFP()+4);
				break;
				
			case CONSTANT:
			    final int lsbv = (int) (value & 0xFFFFFFFFL);
			    final int msbv = (int) ((value >>> 32) & 0xFFFFFFFFL);

				os.writeMOV_Const(lsb, lsbv);
				os.writeMOV_Const(msb, msbv);
				break;
				
			case FREGISTER:
				//TODO
				notImplemented();
				break;
				
			case STACK:
				os.writePOP(lsb);
				os.writePOP(msb);
				break;

		}
		kind = REGISTER;
		this.lsb = lsb;
		this.msb = msb;
	}
	
	/* (non-Javadoc)
	 * @see org.jnode.vm.x86.compiler.l1a.Item#load()
	 */
	void load(EmitterContext ec) {
		if (kind != REGISTER) {
			X86RegisterPool pool = ec.getPool();
			
			final Register l = (Register)pool.request(INT, this);
			final Register r = (Register)pool.request(INT, this);
	
			loadTo(ec, l, r);
		}
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.x86.compiler.l1a.Item#loadToFPU()
	 */
	void loadToFPU(EmitterContext ec) {
		// TODO Auto-generated method stub
		notImplemented();

	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.x86.compiler.l1a.Item#clone()
	 */
	Item clone(EmitterContext ec) {
		Item res = null;
		switch (getKind()) {
			case REGISTER:
				//TODO
				notImplemented();
				break;
				
			case LOCAL:
				//TODO
				notImplemented();
				break;
				
			case CONSTANT:
				//TODO
				notImplemented();
				break;
				
			case FREGISTER:
				//TODO
				notImplemented();
				break;
			
			case STACK:
				//TODO
				notImplemented();
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
			case REGISTER:
			    os.writePUSH(lsb);
			    os.writePUSH(msb);
				break;
				
			case LOCAL:
				os.writePUSH(FP, offsetToFP+4);
				os.writePUSH(FP, offsetToFP);
				break;
				
			case CONSTANT:
			    final int lsbv = (int) (value & 0xFFFFFFFFL);
			    final int msbv = (int) ((value >>> 32) & 0xFFFFFFFFL);

			    os.writePUSH(lsbv);
			    os.writePUSH(msbv);
				break;
				
			case FREGISTER:
				//TODO
				notImplemented();
				break;
				
			case STACK:
				//nothing to do
				break;
		}
		release(ec);
		kind = STACK;
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.x86.compiler.l1a.Item#release()
	 */
	void release(EmitterContext ec) {
		final X86RegisterPool pool = ec.getPool();
		
		switch (getKind()) {
			case REGISTER:
				pool.release(lsb);
				pool.release(msb);
				break;
				
			case LOCAL:
				// nothing to do
				break;
				
			case CONSTANT:
				// nothing to do
				break;
				
			case FREGISTER:
				//TODO
				notImplemented();
				break;
				
			case STACK:
				// nothing to do
				break;
		}
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.x86.compiler.l1a.Item#uses(org.jnode.assembler.x86.Register)
	 */
	boolean uses(Register reg) {
		return ((kind == REGISTER) && (msb.equals(reg) || lsb.equals(reg)));
	}

	static LongItem createStack() {
		return new LongItem(STACK, 0, null, null, 0);
	}
	
	static LongItem createConst(long value) {
		return new LongItem(CONSTANT, 0, null, null, value);
	}
	
	static LongItem createReg(Register lsb, Register msb) {
		return new LongItem(REGISTER, 0, lsb, msb, 0);
	}
	
	static LongItem createLocal(int offsetToFP) {
		return new LongItem(LOCAL, offsetToFP, null, null, 0);
	}
}
