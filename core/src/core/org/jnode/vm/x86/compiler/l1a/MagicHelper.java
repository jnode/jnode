/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.x86.X86Assembler;
import org.jnode.assembler.x86.X86Constants;
import org.jnode.assembler.x86.X86Register;
import org.jnode.assembler.x86.X86Register.GPR;
import org.jnode.assembler.x86.X86Register.GPR32;
import org.jnode.assembler.x86.X86Register.GPR64;
import org.jnode.vm.JvmType;
import org.jnode.vm.Vm;
import org.jnode.vm.classmgr.ObjectFlags;
import org.jnode.vm.classmgr.ObjectLayout;
import org.jnode.vm.classmgr.TIBLayout;
import org.jnode.vm.classmgr.VmArray;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.x86.compiler.BaseX86MagicHelper;
import org.jnode.vm.x86.compiler.X86CompilerConstants;
import org.jnode.vm.x86.compiler.X86CompilerContext;
import org.jnode.vm.x86.compiler.X86CompilerHelper;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class MagicHelper extends BaseX86MagicHelper {

    /**
     * Generate code for a magic method.
     * 
     * @param ec
     * @param method
     * @param isstatic
     */
    public void emitMagic(EmitterContext ec, VmMethod method, boolean isstatic) {
        //final int type = getClass(method);
        final int mcode = getMethodCode(method);
        final VirtualStack vstack = ec.getVStack();
        final X86Assembler os = ec.getStream();
        final ItemFactory ifac = ec.getItemFactory();
        final X86RegisterPool pool = ec.getGPRPool();
        final X86CompilerContext context = ec.getContext();  
        final X86CompilerHelper helper = ec.getHelper();
        final int slotSize = os.isCode32() ? 4 : 8;

        switch (mcode) {
        case mADD: {
            // addr + ofs
            if (Vm.VerifyAssertions) Vm._assert(!isstatic);
            final WordItem ofs = (WordItem) vstack.pop();
            final RefItem addr = vstack.popRef();
            ofs.load(ec);
            addr.load(ec);
            os.writeADD(addr.getRegister(), ofs.getRegister());
            ofs.release(ec);
            vstack.push(addr);
        }
            break;
        case mAND: {
            // addr & ofs
            if (Vm.VerifyAssertions) Vm._assert(!isstatic);
            final WordItem ofs = (WordItem) vstack.pop();
            final RefItem addr = vstack.popRef();
            ofs.load(ec);
            addr.load(ec);
            os.writeAND(addr.getRegister(), ofs.getRegister());
            ofs.release(ec);
            vstack.push(addr);
        }
            break;
        case mOR: {
            // addr | ofs
            if (Vm.VerifyAssertions) Vm._assert(!isstatic);
            final WordItem ofs = (WordItem) vstack.pop();
            final RefItem addr = vstack.popRef();
            ofs.load(ec);
            addr.load(ec);
            os.writeOR(addr.getRegister(), ofs.getRegister());
            ofs.release(ec);
            vstack.push(addr);
        }
            break;
        case mSUB: {
            // addr - ofs
            if (Vm.VerifyAssertions) Vm._assert(!isstatic);
            final WordItem ofs = (WordItem) vstack.pop();
            final RefItem addr = vstack.popRef();
            ofs.load(ec);
            addr.load(ec);
            os.writeSUB(addr.getRegister(), ofs.getRegister());
            ofs.release(ec);
            vstack.push(addr);
        }
            break;
        case mXOR: {
            // addr ^ ofs
            if (Vm.VerifyAssertions) Vm._assert(!isstatic);
            final WordItem ofs = (WordItem) vstack.pop();
            final RefItem addr = vstack.popRef();
            ofs.load(ec);
            addr.load(ec);
            os.writeXOR(addr.getRegister(), ofs.getRegister());
            ofs.release(ec);
            vstack.push(addr);
        }
            break;
        case mNOT: {
            // !addr
            if (Vm.VerifyAssertions) Vm._assert(!isstatic);
            final RefItem addr = vstack.popRef();
            addr.load(ec);
            os.writeNOT(addr.getRegister());
            vstack.push(addr);
        }
            break;
        case mTOINT: {
            if (Vm.VerifyAssertions) Vm._assert(!isstatic);
            final WordItem addr = vstack.popRef();
            addr.load(ec);
            X86Register r = addr.getRegister();
            if (os.isCode64()) {
            	r = pool.getRegisterInSameGroup(r, JvmType.INT);
            	// We just take the lower 32-bit, so no actual mov's needed.
            }
            addr.release(ec);
            L1AHelper.requestRegister(ec, r);
            final IntItem result = (IntItem) ifac.createReg(ec, JvmType.INT, r);
            pool.transferOwnerTo(r, result);
            vstack.push(result);
        }
            break;
        case mTOWORD: 
        case mTOADDRESS:
        case mTOOFFSET: 
        case mTOOBJECT: 
        case mTOOBJECTREFERENCE: 
        case mTOEXTENT: {
            if (Vm.VerifyAssertions) Vm._assert(!isstatic);
            final WordItem addr = vstack.popRef();
            vstack.push(addr);
        }
            break;
        case mTOLONG: {
            if (Vm.VerifyAssertions) Vm._assert(!isstatic);
            final WordItem addr = vstack.popRef();
            addr.load(ec);
            final X86Register.GPR r = addr.getRegister();
            addr.release(ec);
            L1AHelper.requestRegister(ec, r);
            final LongItem result;
            if (os.isCode32()) {
            	final X86Register.GPR msb = (X86Register.GPR)L1AHelper.requestRegister(ec, JvmType.INT,
            			false);
            	result = (LongItem) ifac.createReg(ec, JvmType.LONG, r,
            			msb);
            	os.writeXOR(msb, msb);
            	pool.transferOwnerTo(msb, result);
            } else {
            	result = (LongItem) ifac.createReg(ec, JvmType.LONG, (GPR64)r);
            }
            pool.transferOwnerTo(r, result);
            vstack.push(result);
        }
            break;
        case mMAX: {
            if (Vm.VerifyAssertions) Vm._assert(isstatic);
            final RefItem result = (RefItem) L1AHelper.requestWordRegister(ec,
                    JvmType.REFERENCE, false);
            final GPR r = result.getRegister();
            os.writeMOV_Const(r, -1);
            vstack.push(result);
        }
            break;
        case mONE: {
            if (Vm.VerifyAssertions) Vm._assert(isstatic);
            final RefItem result = (RefItem) L1AHelper.requestWordRegister(ec,
                    JvmType.REFERENCE, false);
            final GPR r = result.getRegister();
            os.writeMOV_Const(r, 1);
            vstack.push(result);
        }
            break;
        case mZERO: {
            if (Vm.VerifyAssertions) Vm._assert(isstatic);
            final RefItem result = ifac.createAConst(ec, null);
            vstack.push(result);
        }
            break;
        case mISMAX: {
            if (Vm.VerifyAssertions) Vm._assert(!isstatic);
            final WordItem addr = vstack.popRef();
            addr.load(ec);
            final IntItem result = (IntItem)L1AHelper.requestWordRegister(ec, JvmType.INT, true);
            final GPR addrr = addr.getRegister();
            final GPR resultr = result.getRegister();
            os.writeXOR(resultr, resultr);
            os.writeCMP_Const(addrr, -1);
            os.writeSETCC(resultr, X86Constants.JE);
            addr.release(ec);
            vstack.push(result);
        }
            break;
        case mISZERO: {
            // Just convert to int
            if (Vm.VerifyAssertions) Vm._assert(!isstatic);
            final WordItem addr = vstack.popRef();
            addr.load(ec);
            final IntItem result = (IntItem)L1AHelper.requestWordRegister(ec, JvmType.INT, true);
            final GPR addrr = addr.getRegister();
            final GPR resultr = result.getRegister();
            os.writeXOR(resultr, resultr);
            os.writeTEST(addrr, addrr);
            os.writeSETCC(resultr, X86Constants.JZ);
            addr.release(ec);
            vstack.push(result);
        }
            break;
        case mEQUALS: 
        case mEQ: 
        case mNE: 
        case mLT:
        case mLE:
        case mGE:
        case mGT:
        case mSLT:
        case mSLE:
        case mSGE:
        case mSGT:
        {
            // addr .. other
            if (Vm.VerifyAssertions) Vm._assert(!isstatic);
            final RefItem other = vstack.popRef();
            final RefItem addr = vstack.popRef();
            other.load(ec);
            addr.load(ec);
            final IntItem result = (IntItem)L1AHelper.requestWordRegister(ec, JvmType.INT, true);
            final GPR resultr = result.getRegister();
            os.writeXOR(resultr, resultr);
            os.writeCMP(addr.getRegister(), other.getRegister());
            os.writeSETCC(resultr, methodToCC(mcode));
            other.release(ec);
            addr.release(ec);
            vstack.push(result);
        }
            break;
        case mFROMINT: 
        case mFROMINTSIGNEXTEND: 
        case mFROMINTZEROEXTEND: {
            if (Vm.VerifyAssertions) Vm._assert(isstatic);
            final WordItem addr = vstack.popInt();
            addr.load(ec);
            GPR r = addr.getRegister();
            if (os.isCode64()) {
            	final GPR64 newR = (GPR64)pool.getRegisterInSameGroup(r, JvmType.REFERENCE);
            	if (mcode == mFROMINTZEROEXTEND) {
            		// Moving the register to itself in 32-bit mode, will
            		// zero extend the top 32-bits.
            		os.writeMOV(BITS32, r, r);
            	} else {
            		// Sign extend
            		os.writeMOVSXD(newR, (GPR32)r);            		
            	}
            	r = newR;
            }
            addr.release(ec);
            vstack.push(L1AHelper.requestWordRegister(ec, JvmType.REFERENCE, r));
        }
            break;
        case mFROMADDRESS:
        case mFROMOBJECT: {
            if (Vm.VerifyAssertions) Vm._assert(isstatic);
            final RefItem obj = vstack.popRef();
            // Do nothing
            vstack.push(obj);
        }
            break;
        case mFROMLONG: {
            if (Vm.VerifyAssertions) Vm._assert(isstatic);
            final LongItem addr = vstack.popLong();
            addr.load(ec);
            final X86Register r;
            if (os.isCode32()) {
            	r = addr.getLsbRegister(ec);
            } else {
            	r = addr.getRegister(ec);
            }
            addr.release(ec);
            vstack.push(L1AHelper.requestWordRegister(ec, JvmType.REFERENCE, r));
        }
            break;
        case mLSH:
        case mRSHA:
        case mRSHL:
        {
            // addr shift cnt
            if (Vm.VerifyAssertions) Vm._assert(!isstatic);
            final IntItem cnt = vstack.popInt();
            final RefItem addr = vstack.popRef();
            if (!cnt.isConstant()) {
                L1AHelper.requestRegister(ec, X86Register.ECX);
                cnt.loadTo(ec, X86Register.ECX);
            }
            addr.load(ec);
            final int shift = methodToShift(mcode);
            if (cnt.isConstant()) {
                os.writeShift(shift, addr.getRegister(), cnt.getValue());
            } else {
                os.writeShift_CL(shift, addr.getRegister());
            }
            cnt.release(ec);
            vstack.push(addr);
        }
            break;
        case mLOADBYTE: 
        case mLOADCHAR:
        case mLOADSHORT: {
            if (Vm.VerifyAssertions) Vm._assert(!isstatic);
            final RefItem addr = vstack.popRef();
            addr.loadToBITS8GPR(ec);
            final GPR r = addr.getRegister();
            addr.release(ec);
            final WordItem result = L1AHelper.requestWordRegister(ec, methodToType(mcode), true);
            final GPR resultr = result.getRegister();
            if (mcode == mLOADCHAR) {
                os.writeMOVZX(resultr, r, 0, methodToSize(mcode));                
            } else {
                os.writeMOVSX(resultr, r, 0, methodToSize(mcode));
            }
            vstack.push(result);
        } break;
        case mLOADINT:
        case mLOADFLOAT: 
        case mLOADADDRESS: 
        case mLOADOBJECTREFERENCE:
        case mLOADWORD:
        case mPREPAREINT:
        case mPREPAREADDRESS:
        case mPREPAREOBJECTREFERENCE:
        case mPREPAREWORD: {
            if (Vm.VerifyAssertions) Vm._assert(!isstatic);
            final RefItem addr = vstack.popRef();
            addr.load(ec);
            final GPR r = addr.getRegister();
            addr.release(ec);
            final WordItem result = L1AHelper.requestWordRegister(ec, methodToType(mcode), false);
            final GPR resultr = result.getRegister();
            os.writeMOV(resultr.getSize(), resultr, r, 0);
            vstack.push(result);
        } break;
        case mLOADLONG:
        case mLOADDOUBLE: {
            if (Vm.VerifyAssertions) Vm._assert(!isstatic);
            final RefItem addr = vstack.popRef();
            addr.load(ec);
            final X86Register.GPR r = addr.getRegister();
            if (os.isCode32()) {
            	final X86Register.GPR msb = (X86Register.GPR)L1AHelper.requestRegister(ec, JvmType.INT, false);
            	addr.release(ec);
            	L1AHelper.releaseRegister(ec, msb);
            	os.writeMOV(X86CompilerConstants.INTSIZE, msb, r, X86CompilerConstants.MSB);
            	os.writeMOV(X86CompilerConstants.INTSIZE, r, r, X86CompilerConstants.LSB);
            	vstack.push(L1AHelper.requestDoubleWordRegisters(ec, methodToType(mcode), r, msb));
            } else {
            	final DoubleWordItem result = L1AHelper.requestDoubleWordRegisters(ec, methodToType(mcode));
            	os.writeMOV(BITS64, result.getRegister(ec), r, 0);
            	addr.release(ec);
            	vstack.push(result);
            }            	
        } break;
        case mLOADBYTE_OFS: 
        case mLOADCHAR_OFS:
        case mLOADSHORT_OFS: {
            if (Vm.VerifyAssertions) Vm._assert(!isstatic);
            final RefItem ofs = vstack.popRef();
            final RefItem addr = vstack.popRef();
            ofs.load(ec);
            addr.load(ec);
            final GPR ofsr = ofs.getRegister();
            final GPR r = addr.getRegister();
            ofs.release(ec);
            addr.release(ec);
            os.writeLEA(r, r, ofsr, 1, 0);
            final WordItem result = L1AHelper.requestWordRegister(ec, methodToType(mcode), true);
            final GPR resultr = result.getRegister();
            if (mcode == mLOADCHAR_OFS) {
                os.writeMOVZX(resultr, r, 0, methodToSize(mcode));
            } else {
                os.writeMOVSX(resultr, r, 0, methodToSize(mcode));
            }
            vstack.push(result);
        } break;
        case mLOADINT_OFS: 
        case mLOADFLOAT_OFS: 
        case mLOADADDRESS_OFS: 
        case mLOADOBJECTREFERENCE_OFS:
        case mLOADWORD_OFS:
        case mPREPAREINT_OFS:
        case mPREPAREADDRESS_OFS:
        case mPREPAREOBJECTREFERENCE_OFS:
        case mPREPAREWORD_OFS: {
            if (Vm.VerifyAssertions) Vm._assert(!isstatic);
            final RefItem ofs = vstack.popRef();
            final RefItem addr = vstack.popRef();
            ofs.load(ec);
            addr.load(ec);
            final GPR ofsr = ofs.getRegister();
            final GPR r = addr.getRegister();
            ofs.release(ec);
            addr.release(ec);
            final WordItem result = L1AHelper.requestWordRegister(ec, methodToType(mcode), false);
            final GPR resultr = result.getRegister();
            os.writeMOV(resultr.getSize(), resultr, r, ofsr, 1, 0);
            vstack.push(result);
        } break;
        case mLOADLONG_OFS: 
        case mLOADDOUBLE_OFS: {
            if (Vm.VerifyAssertions) Vm._assert(!isstatic);
            final RefItem ofs = vstack.popRef();
            final RefItem addr = vstack.popRef();
            ofs.load(ec);
            addr.load(ec);
            final GPR ofsr = ofs.getRegister();
            final GPR r = addr.getRegister();
            if (os.isCode32()) {
            	final GPR msb = (GPR)L1AHelper.requestRegister(ec, JvmType.INT, false);
            	os.writeMOV(X86CompilerConstants.INTSIZE, msb, r, ofsr, 1, X86CompilerConstants.MSB);
            	os.writeMOV(X86CompilerConstants.INTSIZE, r, r, ofsr, 1, X86CompilerConstants.LSB);
            	ofs.release(ec);
            	addr.release(ec);
            	L1AHelper.releaseRegister(ec, msb);
            	vstack.push(L1AHelper.requestDoubleWordRegisters(ec, methodToType(mcode), r, msb));
            } else {
            	final DoubleWordItem result = L1AHelper.requestDoubleWordRegisters(ec, methodToType(mcode));
            	os.writeMOV(BITS64, result.getRegister(ec), r, ofsr, 1, 0);
            	addr.release(ec);
            	ofs.release(ec);
            	vstack.push(result);
            }
        } break;
        case mSTOREBYTE: 
        case mSTORECHAR:
        case mSTORESHORT: {
            if (Vm.VerifyAssertions) Vm._assert(!isstatic);
            final IntItem val = vstack.popInt();
            final RefItem addr = vstack.popRef();
            val.loadToBITS8GPR(ec);
            addr.load(ec);
            final GPR r = addr.getRegister();
            final GPR valr = val.getRegister();
            os.writeMOV(methodToSize(mcode), r, 0, valr);
            val.release(ec);
            addr.release(ec);
        } break;
        case mSTOREINT: 
        case mSTOREFLOAT:
        case mSTOREADDRESS:
        case mSTOREOBJECTREFERENCE: 
        case mSTOREWORD: {
            if (Vm.VerifyAssertions) Vm._assert(!isstatic);
            final WordItem val = (WordItem)vstack.pop();
            final RefItem addr = vstack.popRef();
            val.load(ec);
            addr.load(ec);
            final GPR r = addr.getRegister();
            final GPR valr = val.getRegister();
            os.writeMOV(valr.getSize(), r, 0, valr);
            val.release(ec);
            addr.release(ec);
        } break;
        case mSTORELONG: 
        case mSTOREDOUBLE: {
            if (Vm.VerifyAssertions) Vm._assert(!isstatic);
            final DoubleWordItem val = (DoubleWordItem)vstack.pop();
            final RefItem addr = vstack.popRef();
            val.load(ec);
            addr.load(ec);
            final GPR r = addr.getRegister();
            if (os.isCode32()) {
            	final GPR lsb = val.getLsbRegister(ec);
            	final GPR msb = val.getMsbRegister(ec);
            	os.writeMOV(X86CompilerConstants.INTSIZE, r, X86CompilerConstants.LSB, lsb);
            	os.writeMOV(X86CompilerConstants.INTSIZE, r, X86CompilerConstants.MSB, msb);
            } else {
            	final GPR64 valr = val.getRegister(ec);
            	os.writeMOV(BITS64, r, 0, valr);            	
            }
            val.release(ec);
            addr.release(ec);
        } break;
        case mSTOREBYTE_OFS: 
        case mSTORECHAR_OFS:
        case mSTORESHORT_OFS: {
            if (Vm.VerifyAssertions) Vm._assert(!isstatic);
            final RefItem ofs = vstack.popRef();
            final IntItem val = vstack.popInt();
            final RefItem addr = vstack.popRef();
            ofs.load(ec);
            val.loadToBITS8GPR(ec);
            addr.load(ec);
            final GPR r = addr.getRegister();
            final GPR ofsr = ofs.getRegister();            
            final GPR valr = val.getRegister();
            os.writeMOV(methodToSize(mcode), r, ofsr, 1, 0, valr);
            ofs.release(ec);
            val.release(ec);
            addr.release(ec);
        } break;
        case mSTOREINT_OFS: 
        case mSTOREFLOAT_OFS:
        case mSTOREADDRESS_OFS:
        case mSTOREOBJECTREFERENCE_OFS: 
        case mSTOREWORD_OFS: {
            if (Vm.VerifyAssertions) Vm._assert(!isstatic);
            final RefItem ofs = vstack.popRef();
            final WordItem val = (WordItem)vstack.pop();
            final RefItem addr = vstack.popRef();
            ofs.load(ec);
            val.load(ec);
            addr.load(ec);
            final GPR r = addr.getRegister();
            final GPR ofsr = ofs.getRegister();            
            final GPR valr = val.getRegister();
            os.writeMOV(valr.getSize(), r, ofsr, 1, 0, valr);
            ofs.release(ec);
            val.release(ec);
            addr.release(ec);
        } break;
        case mSTORELONG_OFS: 
        case mSTOREDOUBLE_OFS: {
            if (Vm.VerifyAssertions) Vm._assert(!isstatic);
            final RefItem ofs = vstack.popRef();
            final DoubleWordItem val = (DoubleWordItem)vstack.pop();
            final RefItem addr = vstack.popRef();
            ofs.load(ec);
            val.load(ec);
            addr.load(ec);
            final GPR r = addr.getRegister();
            final GPR ofsr = ofs.getRegister();    
            if (os.isCode32()) {
            	final GPR lsb = val.getLsbRegister(ec);
            	final GPR msb = val.getMsbRegister(ec);
            	os.writeMOV(X86CompilerConstants.INTSIZE, r, ofsr, 1, X86CompilerConstants.LSB, lsb);
            	os.writeMOV(X86CompilerConstants.INTSIZE, r, ofsr, 1, X86CompilerConstants.MSB, msb);
            } else {
            	final GPR64 valr = val.getRegister(ec);
            	os.writeMOV(BITS64, r, ofsr, 1, 0, valr);            	
            }
            ofs.release(ec);
            val.release(ec);
            addr.release(ec);
        } break;
        
        case mATTEMPTINT:
        case mATTEMPTADDRESS:
        case mATTEMPTOBJECTREFERENCE:
        case mATTEMPTWORD: {
            if (Vm.VerifyAssertions) Vm._assert(!isstatic);
            final WordItem val = (WordItem)vstack.pop();
            final WordItem old = (WordItem)vstack.pop();
            final RefItem addr = vstack.popRef();
            final X86Register.GPR aax;
            if ((mcode == mATTEMPTINT) || os.isCode32()) {
            	aax = X86Register.EAX;
            } else {
            	aax = X86Register.RAX;
            }
            if (!old.uses(aax)) {
                L1AHelper.requestRegister(ec, aax, old);
                val.load(ec);
                old.loadTo(ec, aax);
            } else {
                val.load(ec);
            }
            addr.load(ec);
            final IntItem result = (IntItem)L1AHelper.requestWordRegister(ec, JvmType.INT, true);
            final GPR resultr = result.getRegister();

            final GPR r = addr.getRegister();
            final GPR valr = val.getRegister();
            os.writeCMPXCHG_EAX(r, 0, valr, true);
            os.writeSETCC(resultr, X86Constants.JZ);
            os.writeAND(resultr, 0xFF);

            val.release(ec);
            old.release(ec);
            addr.release(ec);
            vstack.push(L1AHelper.requestWordRegister(ec, JvmType.INT, resultr));
        } break;

        case mATTEMPTINT_OFS:
        case mATTEMPTADDRESS_OFS:
        case mATTEMPTOBJECTREFERENCE_OFS:
        case mATTEMPTWORD_OFS: {
            if (Vm.VerifyAssertions) Vm._assert(!isstatic);
            final RefItem ofs = vstack.popRef();
            final WordItem val = (WordItem)vstack.pop();
            final WordItem old = (WordItem)vstack.pop();
            final RefItem addr = vstack.popRef();
            final X86Register.GPR aax;
            if ((mcode == mATTEMPTINT) || os.isCode32()) {
            	aax = X86Register.EAX;
            } else {
            	aax = X86Register.RAX;
            }
            if (!old.uses(aax)) {
                L1AHelper.requestRegister(ec, aax, old);
                ofs.load(ec);
                val.load(ec);
                old.loadTo(ec, aax);
            } else {
                ofs.load(ec);
                val.load(ec);
            }
            addr.load(ec);
            final IntItem result = (IntItem)L1AHelper.requestWordRegister(ec, JvmType.INT, true);
            final GPR resultr = result.getRegister();

            final GPR r = addr.getRegister();
            final GPR valr = val.getRegister();
            final GPR ofsr = ofs.getRegister();
            os.writeLEA(r, r, ofsr, 1, 0);
            os.writeCMPXCHG_EAX(r, 0, valr, true);
            os.writeSETCC(resultr, X86Constants.JZ);
            os.writeAND(resultr, 0xFF);

            ofs.release(ec);
            val.release(ec);
            old.release(ec);
            addr.release(ec);
            vstack.push(L1AHelper.requestWordRegister(ec, JvmType.INT, resultr));
        } break;

        case mGETOBJECTTYPE: {
            if (Vm.VerifyAssertions) Vm._assert(isstatic);
            final RefItem obj = vstack.popRef();
            obj.load(ec);
            final GPR r = obj.getRegister();
            // Get TIB
            os.writeMOV(helper.ADDRSIZE, r, r, ObjectLayout.TIB_SLOT * slotSize);
            // Get VmType
            os.writeMOV(helper.ADDRSIZE, r, r, (TIBLayout.VMTYPE_INDEX + VmArray.DATA_OFFSET) * slotSize);
            vstack.push(obj);
        } break;
        case mGETTIB: {
            if (Vm.VerifyAssertions) Vm._assert(isstatic);
            final RefItem obj = vstack.popRef();
            obj.load(ec);
            final GPR r = obj.getRegister();
            // Get TIB
            os.writeMOV(helper.ADDRSIZE, r, r, ObjectLayout.TIB_SLOT * slotSize);
            vstack.push(obj);
        } break;
        case mGETOBJECTFLAGS: {
            if (Vm.VerifyAssertions) Vm._assert(isstatic);
            final RefItem obj = vstack.popRef();
            obj.load(ec);
            final GPR r = obj.getRegister();
            // Get flags
            os.writeMOV(helper.ADDRSIZE, r, r, ObjectLayout.FLAGS_SLOT * slotSize);
            obj.release(ec);
            vstack.push(L1AHelper.requestWordRegister(ec, JvmType.REFERENCE, r));
        } break;
        case mSETOBJECTFLAGS: {
            if (Vm.VerifyAssertions) Vm._assert(isstatic);
            final RefItem flags = vstack.popRef();
            final RefItem obj = vstack.popRef();
            flags.load(ec);
            obj.load(ec);
            final GPR flagsr = flags.getRegister();
            final GPR r = obj.getRegister();
            // Set flags
            os.writeMOV(helper.ADDRSIZE, r, ObjectLayout.FLAGS_SLOT * slotSize, flagsr);
            flags.release(ec);
            obj.release(ec);
        } break;
        case mGETARRAYDATA: {
            if (Vm.VerifyAssertions) Vm._assert(isstatic);
            final RefItem obj = vstack.popRef();
            obj.load(ec);
            final GPR r = obj.getRegister();
            os.writeADD(r, VmArray.DATA_OFFSET * slotSize);
            vstack.push(obj);
        } break;
        case mGETOBJECTCOLOR: {
            if (Vm.VerifyAssertions) Vm._assert(isstatic);
            final RefItem obj = vstack.popRef();
            obj.load(ec);
            final GPR r = obj.getRegister();
            final IntItem result = (IntItem)L1AHelper.requestWordRegister(ec, JvmType.INT, false);
            final GPR resultr = result.getRegister();
            // Get flags
            os.writeMOV(BITS32, resultr, r, ObjectLayout.FLAGS_SLOT * slotSize);
            os.writeAND(resultr, ObjectFlags.GC_COLOUR_MASK);
            obj.release(ec);
            vstack.push(result);
        } break;
        case mISFINALIZED: {
            if (Vm.VerifyAssertions) Vm._assert(isstatic);
            final RefItem obj = vstack.popRef();
            obj.load(ec);
            final GPR r = obj.getRegister();
            final IntItem result = (IntItem)L1AHelper.requestWordRegister(ec, JvmType.INT, false);
            final GPR resultr = result.getRegister();
            // Get flags
            os.writeMOV(BITS32, resultr, r, ObjectLayout.FLAGS_SLOT * slotSize);
            os.writeAND(resultr, ObjectFlags.STATUS_FINALIZED);
            obj.release(ec);
            vstack.push(result);
        } break;
        case mATOMICADD: 
        case mATOMICAND: 
        case mATOMICOR:
        case mATOMICSUB: {
            if (Vm.VerifyAssertions) Vm._assert(!isstatic);
            final RefItem value = vstack.popRef();
            final RefItem addr = vstack.popRef();
            value.load(ec);
            addr.load(ec);
            final X86Register.GPR valuer = (X86Register.GPR)value.getRegister();
            final X86Register.GPR r = (X86Register.GPR)addr.getRegister();
            os.writePrefix(X86Constants.LOCK_PREFIX);
            os.writeArithOp(methodCodeToOperation(mcode), r, 0, valuer);
            value.release(ec);
            addr.release(ec);
        } break;
        case mGETCURRENTFRAME: {
            if (Vm.VerifyAssertions) Vm._assert(isstatic);
            final WordItem result = L1AHelper.requestWordRegister(ec, JvmType.REFERENCE, false);
            final GPR r = result.getRegister();
            os.writeMOV(helper.ADDRSIZE, r, helper.BP);
            vstack.push(result);
        } break;
        case mGETTIMESTAMP: {
            if (Vm.VerifyAssertions) Vm._assert(isstatic);
            if (os.isCode32()) {
            	final DoubleWordItem result = L1AHelper.requestDoubleWordRegisters(ec, JvmType.LONG, X86Register.EAX, X86Register.EDX);
            	os.writeRDTSC();
            	vstack.push(result);
            } else {
            	final DoubleWordItem result = L1AHelper.requestDoubleWordRegister(ec, JvmType.LONG, X86Register.RAX);
            	L1AHelper.requestRegister(ec, X86Register.RDX);
            	os.writeRDTSC();
            	// Move MSB to upper 32-bit of RDX
            	os.writeSHL(X86Register.RDX, 32);
            	// RAX is zero extended by RDTSC, so an OR of RAX,RDX will combine
            	// the upper 32-bits of RDX and the lower 32-bits of RAX.
            	os.writeOR(X86Register.RAX, X86Register.RDX);
            	// Now free RDX
            	L1AHelper.releaseRegister(ec, X86Register.RDX);
            	vstack.push(result);            	
            }
        } break;
        case mINTBITSTOFLOAT:
        case mFLOATTORAWINTBITS: {
            if (Vm.VerifyAssertions) Vm._assert(isstatic);
            final WordItem v = (WordItem)vstack.pop();
            v.load(ec);
            final X86Register.GPR r = v.getRegister();
            v.release(ec);
            final int resultType = (mcode == mINTBITSTOFLOAT) ? JvmType.FLOAT : JvmType.INT;
            vstack.push(L1AHelper.requestWordRegister(ec, resultType, r));            
        } break;
        case mLONGBITSTODOUBLE: 
        case mDOUBLETORAWLONGBITS: {
            if (Vm.VerifyAssertions) Vm._assert(isstatic);
            final DoubleWordItem v = (DoubleWordItem)vstack.pop();
            v.load(ec);
        	final int resultType = (mcode == mLONGBITSTODOUBLE) ? JvmType.DOUBLE : JvmType.LONG;
            if (os.isCode32()) {
            	final X86Register.GPR lsb = v.getLsbRegister(ec);
            	final X86Register.GPR msb = v.getMsbRegister(ec);
            	v.release(ec);
            	vstack.push(L1AHelper.requestDoubleWordRegisters(ec, resultType, lsb, msb));
            } else {
            	final GPR64 vreg = v.getRegister(ec);
            	v.release(ec);
            	vstack.push(L1AHelper.requestDoubleWordRegister(ec, resultType, vreg));            	
            }
        } break;
        case mBREAKPOINT: {
            if (Vm.VerifyAssertions) Vm._assert(isstatic);
            os.writeINT(3);
        } break;
        	
        
        default:
            throw new InternalError("Unknown method code for method " + method);
        }
    }
}
