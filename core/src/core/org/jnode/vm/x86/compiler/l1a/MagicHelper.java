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

import org.jnode.assembler.x86.AbstractX86Stream;
import org.jnode.assembler.x86.Register;
import org.jnode.assembler.x86.X86Constants;
import org.jnode.vm.JvmType;
import org.jnode.vm.Vm;
import org.jnode.vm.classmgr.ObjectFlags;
import org.jnode.vm.classmgr.ObjectLayout;
import org.jnode.vm.classmgr.TIBLayout;
import org.jnode.vm.classmgr.VmArray;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.x86.compiler.BaseX86MagicHelper;
import org.jnode.vm.x86.compiler.X86CompilerConstants;

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
        final AbstractX86Stream os = ec.getStream();
        final ItemFactory ifac = ec.getItemFactory();
        final X86RegisterPool pool = ec.getPool();

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
            final Register r = addr.getRegister();
            addr.release(ec);
            L1AHelper.requestRegister(ec, r);
            final IntItem result = (IntItem) ifac.createReg(JvmType.INT, r);
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
            final Register r = addr.getRegister();
            addr.release(ec);
            L1AHelper.requestRegister(ec, r);
            final Register msb = L1AHelper.requestRegister(ec, JvmType.INT,
                    false);
            final LongItem result = (LongItem) ifac.createReg(JvmType.LONG, r,
                    msb);
            os.writeXOR(msb, msb);
            pool.transferOwnerTo(r, result);
            pool.transferOwnerTo(msb, result);
            vstack.push(result);
        }
            break;
        case mMAX: {
            if (Vm.VerifyAssertions) Vm._assert(isstatic);
            final RefItem result = (RefItem) L1AHelper.requestWordRegister(ec,
                    JvmType.REFERENCE, false);
            final Register r = result.getRegister();
            os.writeMOV_Const(r, 0xFFFFFFFF);
            vstack.push(result);
        }
            break;
        case mONE: {
            if (Vm.VerifyAssertions) Vm._assert(isstatic);
            final RefItem result = (RefItem) L1AHelper.requestWordRegister(ec,
                    JvmType.REFERENCE, false);
            final Register r = result.getRegister();
            os.writeMOV_Const(r, 1);
            vstack.push(result);
        }
            break;
        case mZERO: {
            if (Vm.VerifyAssertions) Vm._assert(isstatic);
            final RefItem result = ifac.createAConst(null);
            vstack.push(result);
        }
            break;
        case mISMAX: {
            if (Vm.VerifyAssertions) Vm._assert(!isstatic);
            final WordItem addr = vstack.popRef();
            addr.load(ec);
            final IntItem result = (IntItem)L1AHelper.requestWordRegister(ec, JvmType.INT, true);
            final Register addrr = addr.getRegister();
            final Register resultr = result.getRegister();
            os.writeXOR(resultr, resultr);
            os.writeCMP_Const(addrr, 0xFFFFFFFF);
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
            final Register addrr = addr.getRegister();
            final Register resultr = result.getRegister();
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
            final Register resultr = result.getRegister();
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
            final Register r = addr.getRegister();
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
            final Register r = addr.getLsbRegister();
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
                L1AHelper.requestRegister(ec, Register.ECX);
                cnt.loadTo(ec, Register.ECX);
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
            final Register r = addr.getRegister();
            addr.release(ec);
            if (mcode == mLOADCHAR) {
                os.writeMOVZX(r, r, 0, methodToSize(mcode));                
            } else {
                os.writeMOVSX(r, r, 0, methodToSize(mcode));
            }
            vstack.push(L1AHelper.requestWordRegister(ec, JvmType.INT, r));
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
            final Register r = addr.getRegister();
            addr.release(ec);
            os.writeMOV(X86CompilerConstants.INTSIZE, r, r, 0);
            vstack.push(L1AHelper.requestWordRegister(ec, methodToType(mcode), r));
        } break;
        case mLOADLONG:
        case mLOADDOUBLE: {
            if (Vm.VerifyAssertions) Vm._assert(!isstatic);
            final RefItem addr = vstack.popRef();
            addr.load(ec);
            final Register r = addr.getRegister();
            final Register msb = L1AHelper.requestRegister(ec, JvmType.INT, false);
            addr.release(ec);
            L1AHelper.releaseRegister(ec, msb);
            os.writeMOV(X86CompilerConstants.INTSIZE, msb, r, X86CompilerConstants.MSB);
            os.writeMOV(X86CompilerConstants.INTSIZE, r, r, X86CompilerConstants.LSB);
            vstack.push(L1AHelper.requestDoubleWordRegisters(ec, methodToType(mcode), r, msb));
        } break;
        case mLOADBYTE_OFS: 
        case mLOADCHAR_OFS:
        case mLOADSHORT_OFS: {
            if (Vm.VerifyAssertions) Vm._assert(!isstatic);
            final RefItem ofs = vstack.popRef();
            final RefItem addr = vstack.popRef();
            ofs.load(ec);
            addr.load(ec);
            final Register ofsr = ofs.getRegister();
            final Register r = addr.getRegister();
            ofs.release(ec);
            addr.release(ec);
            os.writeLEA(r, r, ofsr, 1, 0);
            if (mcode == mLOADCHAR_OFS) {
                os.writeMOVZX(r, r, 0, methodToSize(mcode));
            } else {
                os.writeMOVSX(r, r, 0, methodToSize(mcode));
            }
            vstack.push(L1AHelper.requestWordRegister(ec, JvmType.INT, r));
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
            final Register ofsr = ofs.getRegister();
            final Register r = addr.getRegister();
            ofs.release(ec);
            addr.release(ec);
            os.writeMOV(X86CompilerConstants.INTSIZE, r, r, ofsr, 1, 0);
            vstack.push(L1AHelper.requestWordRegister(ec, methodToType(mcode), r));
        } break;
        case mLOADLONG_OFS: 
        case mLOADDOUBLE_OFS: {
            if (Vm.VerifyAssertions) Vm._assert(!isstatic);
            final RefItem ofs = vstack.popRef();
            final RefItem addr = vstack.popRef();
            ofs.load(ec);
            addr.load(ec);
            final Register ofsr = ofs.getRegister();
            final Register r = addr.getRegister();
            final Register msb = L1AHelper.requestRegister(ec, JvmType.INT, false);
            os.writeMOV(X86CompilerConstants.INTSIZE, msb, r, ofsr, 1, X86CompilerConstants.MSB);
            os.writeMOV(X86CompilerConstants.INTSIZE, r, r, ofsr, 1, X86CompilerConstants.LSB);
            ofs.release(ec);
            addr.release(ec);
            L1AHelper.releaseRegister(ec, msb);
            vstack.push(L1AHelper.requestDoubleWordRegisters(ec, methodToType(mcode), r, msb));
        } break;
        case mSTOREBYTE: 
        case mSTORECHAR:
        case mSTORESHORT: {
            if (Vm.VerifyAssertions) Vm._assert(!isstatic);
            final IntItem val = vstack.popInt();
            final RefItem addr = vstack.popRef();
            val.loadToBITS8GPR(ec);
            addr.load(ec);
            final Register r = addr.getRegister();
            final Register valr = val.getRegister();
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
            final Register r = addr.getRegister();
            final Register valr = val.getRegister();
            os.writeMOV(X86CompilerConstants.INTSIZE, r, 0, valr);
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
            final Register r = addr.getRegister();
            final Register lsb = val.getLsbRegister();
            final Register msb = val.getMsbRegister();
            os.writeMOV(X86CompilerConstants.INTSIZE, r, X86CompilerConstants.LSB, lsb);
            os.writeMOV(X86CompilerConstants.INTSIZE, r, X86CompilerConstants.MSB, msb);
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
            final Register r = addr.getRegister();
            final Register ofsr = ofs.getRegister();            
            final Register valr = val.getRegister();
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
            final Register r = addr.getRegister();
            final Register ofsr = ofs.getRegister();            
            final Register valr = val.getRegister();
            os.writeMOV(X86CompilerConstants.INTSIZE, r, ofsr, 1, 0, valr);
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
            final Register r = addr.getRegister();
            final Register ofsr = ofs.getRegister();            
            final Register lsb = val.getLsbRegister();
            final Register msb = val.getMsbRegister();
            os.writeMOV(X86CompilerConstants.INTSIZE, r, ofsr, 1, X86CompilerConstants.LSB, lsb);
            os.writeMOV(X86CompilerConstants.INTSIZE, r, ofsr, 1, X86CompilerConstants.MSB, msb);
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
            final Register eax = Register.EAX;
            if (!old.uses(eax)) {
                L1AHelper.requestRegister(ec, eax, old);
                val.load(ec);
                old.loadTo(ec, eax);
            } else {
                val.load(ec);
            }
            addr.load(ec);

            final Register r = addr.getRegister();
            final Register valr = val.getRegister();
            os.writeCMPXCHG_EAX(r, 0, valr, true);
            os.writeSETCC(eax, X86Constants.JZ);
            os.writeAND(eax, 0xFF);

            val.release(ec);
            old.release(ec);
            addr.release(ec);
            vstack.push(L1AHelper.requestWordRegister(ec, JvmType.INT, eax));
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
            final Register eax = Register.EAX;
            if (!old.uses(eax)) {
                L1AHelper.requestRegister(ec, eax, old);
                ofs.load(ec);
                val.load(ec);
                old.loadTo(ec, eax);
            } else {
                ofs.load(ec);
                val.load(ec);
            }
            addr.load(ec);

            final Register r = addr.getRegister();
            final Register valr = val.getRegister();
            final Register ofsr = ofs.getRegister();
            os.writeLEA(r, r, ofsr, 1, 0);
            os.writeCMPXCHG_EAX(r, 0, valr, true);
            os.writeSETCC(eax, X86Constants.JZ);
            os.writeAND(eax, 0xFF);

            ofs.release(ec);
            val.release(ec);
            old.release(ec);
            addr.release(ec);
            vstack.push(L1AHelper.requestWordRegister(ec, JvmType.INT, eax));
        } break;

        case mGETOBJECTTYPE: {
            if (Vm.VerifyAssertions) Vm._assert(isstatic);
            final RefItem obj = vstack.popRef();
            obj.load(ec);
            final Register r = obj.getRegister();
            // Get TIB
            os.writeMOV(X86CompilerConstants.INTSIZE, r, r, ObjectLayout.TIB_SLOT * 4);
            // Get VmType
            os.writeMOV(X86CompilerConstants.INTSIZE, r, r, (TIBLayout.VMTYPE_INDEX + VmArray.DATA_OFFSET) * 4);
            vstack.push(obj);
        } break;
        case mGETTIB: {
            if (Vm.VerifyAssertions) Vm._assert(isstatic);
            final RefItem obj = vstack.popRef();
            obj.load(ec);
            final Register r = obj.getRegister();
            // Get TIB
            os.writeMOV(X86CompilerConstants.INTSIZE, r, r, ObjectLayout.TIB_SLOT * 4);
            vstack.push(obj);
        } break;
        case mGETOBJECTFLAGS: {
            if (Vm.VerifyAssertions) Vm._assert(isstatic);
            final RefItem obj = vstack.popRef();
            obj.load(ec);
            final Register r = obj.getRegister();
            // Get flags
            os.writeMOV(X86CompilerConstants.INTSIZE, r, r, ObjectLayout.FLAGS_SLOT * 4);
            obj.release(ec);
            vstack.push(L1AHelper.requestWordRegister(ec, JvmType.INT, r));
        } break;
        case mSETOBJECTFLAGS: {
            if (Vm.VerifyAssertions) Vm._assert(isstatic);
            final IntItem flags = vstack.popInt();
            final RefItem obj = vstack.popRef();
            flags.load(ec);
            obj.load(ec);
            final Register flagsr = flags.getRegister();
            final Register r = obj.getRegister();
            // Set flags
            os.writeMOV(X86CompilerConstants.INTSIZE, r, ObjectLayout.FLAGS_SLOT * 4, flagsr);
            flags.release(ec);
            obj.release(ec);
        } break;
        case mGETARRAYDATA: {
            if (Vm.VerifyAssertions) Vm._assert(isstatic);
            final RefItem obj = vstack.popRef();
            obj.load(ec);
            final Register r = obj.getRegister();
            os.writeADD(r, VmArray.DATA_OFFSET * 4);
            vstack.push(obj);
        } break;
        case mGETOBJECTCOLOR: {
            if (Vm.VerifyAssertions) Vm._assert(isstatic);
            final RefItem obj = vstack.popRef();
            obj.load(ec);
            final Register r = obj.getRegister();
            // Get flags
            os.writeMOV(X86CompilerConstants.INTSIZE, r, r, ObjectLayout.FLAGS_SLOT * 4);
            os.writeAND(r, ObjectFlags.GC_COLOUR_MASK);
            obj.release(ec);
            vstack.push(L1AHelper.requestWordRegister(ec, JvmType.INT, r));
        } break;
        case mISFINALIZED: {
            if (Vm.VerifyAssertions) Vm._assert(isstatic);
            final RefItem obj = vstack.popRef();
            obj.load(ec);
            final Register r = obj.getRegister();
            // Get flags
            os.writeMOV(X86CompilerConstants.INTSIZE, r, r, ObjectLayout.FLAGS_SLOT * 4);
            os.writeAND(r, ObjectFlags.STATUS_FINALIZED);
            obj.release(ec);
            vstack.push(L1AHelper.requestWordRegister(ec, JvmType.INT, r));
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
            final Register valuer = value.getRegister();
            final Register r = addr.getRegister();
            os.writePrefix(X86Constants.LOCK_PREFIX);
            os.writeArithOp(methodCodeToOperation(mcode), r, 0, valuer);
            value.release(ec);
            addr.release(ec);
        } break;
        case mGETCURRENTFRAME: {
            if (Vm.VerifyAssertions) Vm._assert(isstatic);
            final WordItem result = L1AHelper.requestWordRegister(ec, JvmType.REFERENCE, false);
            final Register r = result.getRegister();
            os.writeMOV(X86CompilerConstants.INTSIZE, r, Register.EBP);
            vstack.push(result);
        } break;
            
        default:
            throw new InternalError("Unknown method code for method " + method);
        }
    }
}
