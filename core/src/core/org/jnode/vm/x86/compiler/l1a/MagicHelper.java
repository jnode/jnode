/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.x86.AbstractX86Stream;
import org.jnode.assembler.x86.Register;
import org.jnode.assembler.x86.X86Constants;
import org.jnode.vm.JvmType;
import org.jnode.vm.Vm;
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
        case mNE: {
            // addr !/== other
            if (Vm.VerifyAssertions) Vm._assert(!isstatic);
            final RefItem other = vstack.popRef();
            final RefItem addr = vstack.popRef();
            other.load(ec);
            addr.load(ec);
            final Register addrr = addr.getRegister();
            os.writeXOR(addrr, other.getRegister());
            if (mcode != mNE) {
                os.writeNOT(addrr);
            }
            other.release(ec);
            addr.release(ec);
            vstack.push(L1AHelper.requestWordRegister(ec, JvmType.INT, addrr));
        }
            break;
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
            os.writeCMP(addr.getRegister(), other.getRegister());
            os.writeSETCC(result.getRegister(), methodToCC(mcode));
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
            L1AHelper.requestRegister(ec, Register.ECX);
            if (!cnt.isConstant()) {
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
            addr.load(ec);
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
        case mLOADWORD: {
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
        case mLOADWORD_OFS: {
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
            

        default:
            throw new InternalError("Unknown method code for method " + method);
        }
    }
}