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

package org.jnode.vm.x86.compiler.l1b;

import static org.jnode.vm.compiler.BaseMagicHelper.MagicMethod.ATTEMPTINT;
import static org.jnode.vm.compiler.BaseMagicHelper.MagicMethod.FROMINTZEROEXTEND;
import static org.jnode.vm.compiler.BaseMagicHelper.MagicMethod.INTBITSTOFLOAT;
import static org.jnode.vm.compiler.BaseMagicHelper.MagicMethod.LOADCHAR;
import static org.jnode.vm.compiler.BaseMagicHelper.MagicMethod.LOADCHAR_OFS;
import static org.jnode.vm.compiler.BaseMagicHelper.MagicMethod.LONGBITSTODOUBLE;

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
    public void emitMagic(EmitterContext ec, VmMethod method, boolean isstatic, X86BytecodeVisitor bcv,
                          VmMethod caller) {
        //final int type = getClass(method);
        final MagicMethod mcode = MagicMethod.get(method);
        final VirtualStack vstack = ec.getVStack();
        final X86Assembler os = ec.getStream();
        final ItemFactory ifac = ec.getItemFactory();
        final X86RegisterPool pool = ec.getGPRPool();
//        final EntryPoints context = ec.getContext();  
        final X86CompilerHelper helper = ec.getHelper();
        final int slotSize = os.isCode32() ? 4 : 8;

        // Test magic permission first
        testMagicPermission(mcode, caller);

        switch (mcode) {
            case ADD: {
                // addr + ofs
                if (Vm.VerifyAssertions) Vm._assert(!isstatic);
                final WordItem ofs = (WordItem) vstack.pop();
                final RefItem addr = vstack.popRef();
                ofs.load(ec);
                addr.load(ec);
                GPR ofsr = ofs.getRegister();
                final GPR addrr = addr.getRegister();
                if (ofsr.getSize() != addrr.getSize()) {
                    // Sign-extend offset
                    final GPR64 ofsr64 = (GPR64) pool.getRegisterInSameGroup(ofsr, JvmType.REFERENCE);
                    os.writeMOVSXD(ofsr64, (GPR32) ofsr);
                    ofsr = ofsr64;
                }
                os.writeADD(addrr, ofsr);
                ofs.release(ec);
                vstack.push(addr);
                break;
            }
            case AND: {
                // addr & ofs
                if (Vm.VerifyAssertions) Vm._assert(!isstatic);
                final WordItem ofs = (WordItem) vstack.pop();
                final RefItem addr = vstack.popRef();
                ofs.load(ec);
                addr.load(ec);
                GPR ofsr = ofs.getRegister();
                final GPR addrr = addr.getRegister();
                if (ofsr.getSize() != addrr.getSize()) {
                    // Sign-extend offset
                    final GPR64 ofsr64 = (GPR64) pool.getRegisterInSameGroup(ofsr, JvmType.REFERENCE);
                    os.writeMOVSXD(ofsr64, (GPR32) ofsr);
                    ofsr = ofsr64;
                }
                os.writeAND(addrr, ofsr);
                ofs.release(ec);
                vstack.push(addr);
                break;
            }
            case OR: {
                // addr | ofs
                if (Vm.VerifyAssertions) Vm._assert(!isstatic);
                final WordItem ofs = (WordItem) vstack.pop();
                final RefItem addr = vstack.popRef();
                ofs.load(ec);
                addr.load(ec);
                GPR ofsr = ofs.getRegister();
                final GPR addrr = addr.getRegister();
                if (ofsr.getSize() != addrr.getSize()) {
                    // Sign-extend offset
                    final GPR64 ofsr64 = (GPR64) pool.getRegisterInSameGroup(ofsr, JvmType.REFERENCE);
                    os.writeMOVSXD(ofsr64, (GPR32) ofsr);
                    ofsr = ofsr64;
                }
                os.writeOR(addrr, ofsr);
                ofs.release(ec);
                vstack.push(addr);
                break;
            }
            case SUB:
            case DIFF: {
                // addr - ofs
                if (Vm.VerifyAssertions) Vm._assert(!isstatic);
                final WordItem ofs = (WordItem) vstack.pop();
                final RefItem addr = vstack.popRef();
                ofs.load(ec);
                addr.load(ec);
                GPR ofsr = ofs.getRegister();
                final GPR addrr = addr.getRegister();
                if (ofsr.getSize() != addrr.getSize()) {
                    // Sign-extend offset
                    final GPR64 ofsr64 = (GPR64) pool.getRegisterInSameGroup(ofsr, JvmType.REFERENCE);
                    os.writeMOVSXD(ofsr64, (GPR32) ofsr);
                    ofsr = ofsr64;
                }
                os.writeSUB(addrr, ofsr);
                ofs.release(ec);
                vstack.push(addr);
                break;
            }
            case XOR: {
                // addr ^ ofs
                if (Vm.VerifyAssertions) Vm._assert(!isstatic);
                final WordItem ofs = (WordItem) vstack.pop();
                final RefItem addr = vstack.popRef();
                ofs.load(ec);
                addr.load(ec);
                GPR ofsr = ofs.getRegister();
                final GPR addrr = addr.getRegister();
                if (ofsr.getSize() != addrr.getSize()) {
                    // Sign-extend offset
                    final GPR64 ofsr64 = (GPR64) pool.getRegisterInSameGroup(ofsr, JvmType.REFERENCE);
                    os.writeMOVSXD(ofsr64, (GPR32) ofsr);
                    ofsr = ofsr64;
                }
                os.writeXOR(addrr, ofsr);
                ofs.release(ec);
                vstack.push(addr);
                break;
            }
            case NOT: {
                // !addr
                if (Vm.VerifyAssertions) Vm._assert(!isstatic);
                final RefItem addr = vstack.popRef();
                addr.load(ec);
                os.writeNOT(addr.getRegister());
                vstack.push(addr);
                break;
            }
            case TOINT: {
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
                break;
            }
            case TOWORD:
            case TOADDRESS:
            case TOOFFSET:
            case TOOBJECT:
            case TOOBJECTREFERENCE:
            case TOEXTENT: {
                if (Vm.VerifyAssertions) Vm._assert(!isstatic);
                final WordItem addr = vstack.popRef();
                vstack.push(addr);
                break;
            }
            case TOLONG: {
                if (Vm.VerifyAssertions) Vm._assert(!isstatic);
                final WordItem addr = vstack.popRef();
                addr.load(ec);
                final X86Register.GPR r = addr.getRegister();
                addr.release(ec);
                L1AHelper.requestRegister(ec, r);
                final LongItem result;
                if (os.isCode32()) {
                    final X86Register.GPR msb = (X86Register.GPR) L1AHelper.requestRegister(ec, JvmType.INT,
                        false);
                    result = (LongItem) ifac.createReg(ec, JvmType.LONG, r,
                        msb);
                    os.writeXOR(msb, msb);
                    pool.transferOwnerTo(msb, result);
                } else {
                    result = (LongItem) ifac.createReg(ec, JvmType.LONG, (GPR64) r);
                }
                pool.transferOwnerTo(r, result);
                vstack.push(result);
                break;
            }
            case MAX: {
                if (Vm.VerifyAssertions) Vm._assert(isstatic);
                final RefItem result = (RefItem) L1AHelper.requestWordRegister(ec,
                    JvmType.REFERENCE, false);
                final GPR r = result.getRegister();
                os.writeMOV_Const(r, -1);
                vstack.push(result);
                break;
            }
            case ONE: {
                if (Vm.VerifyAssertions) Vm._assert(isstatic);
                final RefItem result = (RefItem) L1AHelper.requestWordRegister(ec,
                    JvmType.REFERENCE, false);
                final GPR r = result.getRegister();
                os.writeMOV_Const(r, 1);
                vstack.push(result);
                break;
            }
            case ZERO:
            case NULLREFERENCE: {
                if (Vm.VerifyAssertions) Vm._assert(isstatic);
                final RefItem result = ifac.createAConst(ec, null);
                vstack.push(result);
                break;
            }
            case SIZE: {
                if (Vm.VerifyAssertions) Vm._assert(isstatic);
                final IntItem result = ifac.createIConst(ec, slotSize);
                vstack.push(result);
                break;
            }
            case ISMAX: {
                if (Vm.VerifyAssertions) Vm._assert(!isstatic);
                final WordItem addr = vstack.popRef();
                addr.load(ec);
                final IntItem result = (IntItem) L1AHelper.requestWordRegister(ec, JvmType.INT, true);
                final GPR addrr = addr.getRegister();
                final GPR resultr = result.getRegister();
                os.writeXOR(resultr, resultr);
                os.writeCMP_Const(addrr, -1);
                os.writeSETCC(resultr, X86Constants.JE);
                addr.release(ec);
                vstack.push(result);
                break;
            }
            case ISZERO:
            case ISNULL: {
                // Just convert to int
                if (Vm.VerifyAssertions) Vm._assert(!isstatic);
                final WordItem addr = vstack.popRef();
                addr.load(ec);
                final IntItem result = (IntItem) L1AHelper.requestWordRegister(ec, JvmType.INT, true);
                final GPR addrr = addr.getRegister();
                final GPR resultr = result.getRegister();
                os.writeXOR(resultr, resultr);
                os.writeTEST(addrr, addrr);
                os.writeSETCC(resultr, X86Constants.JZ);
                addr.release(ec);
                vstack.push(result);
                break;
            }
            case EQUALS:
            case EQ:
            case NE:
            case LT:
            case LE:
            case GE:
            case GT:
            case SLT:
            case SLE:
            case SGE:
            case SGT: {
                // addr .. other
                if (Vm.VerifyAssertions) Vm._assert(!isstatic);
                final RefItem other = vstack.popRef();
                final RefItem addr = vstack.popRef();
                other.load(ec);
                addr.load(ec);
                final IntItem result = (IntItem) L1AHelper.requestWordRegister(ec, JvmType.INT, true);
                final GPR resultr = result.getRegister();
                os.writeXOR(resultr, resultr);
                os.writeCMP(addr.getRegister(), other.getRegister());
                os.writeSETCC(resultr, methodToCC(mcode));
                other.release(ec);
                addr.release(ec);
                vstack.push(result);
                break;
            }
            case FROMINT:
            case FROMINTSIGNEXTEND:
            case FROMINTZEROEXTEND: {
                if (Vm.VerifyAssertions) Vm._assert(isstatic);
                final WordItem addr = vstack.popInt();
                addr.load(ec);
                GPR r = addr.getRegister();
                if (os.isCode64()) {
                    final GPR64 newR = (GPR64) pool.getRegisterInSameGroup(r, JvmType.REFERENCE);
                    if (mcode == FROMINTZEROEXTEND) {
                        // Moving the register to itself in 32-bit mode, will
                        // zero extend the top 32-bits.
                        os.writeMOV(BITS32, r, r);
                    } else {
                        // Sign extend
                        os.writeMOVSXD(newR, (GPR32) r);
                    }
                    r = newR;
                }
                addr.release(ec);
                vstack.push(L1AHelper.requestWordRegister(ec, JvmType.REFERENCE, r));
                break;
            }
            case FROMADDRESS:
            case FROMOBJECT: {
                if (Vm.VerifyAssertions) Vm._assert(isstatic);
                final RefItem obj = vstack.popRef();
                // Do nothing
                vstack.push(obj);
                break;
            }
            case FROMLONG: {
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
                break;
            }
            case LSH:
            case RSHA:
            case RSHL: {
                // addr shift cnt
                if (Vm.VerifyAssertions) Vm._assert(!isstatic);
                final GPR ECX = X86Register.ECX;
                final IntItem cnt = vstack.popInt();
                final RefItem addr = vstack.popRef();
                if (!cnt.isConstant() && !cnt.uses(ECX)) {
                    addr.spillIfUsing(ec, ECX);
                    L1AHelper.requestRegister(ec, ECX);
                    cnt.loadTo(ec, ECX);
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
                break;
            }
            case LOADBYTE:
            case LOADCHAR:
            case LOADSHORT: {
                if (Vm.VerifyAssertions) Vm._assert(!isstatic);
                final RefItem addr = vstack.popRef();
                addr.loadToBITS8GPR(ec);
                final GPR r = addr.getRegister();
                final WordItem result = L1AHelper.requestWordRegister(ec, methodToType(mcode), true);
                final GPR resultr = result.getRegister();
                if (mcode == LOADCHAR) {
                    os.writeMOVZX(resultr, r, 0, methodToSize(mcode));
                } else {
                    os.writeMOVSX(resultr, r, 0, methodToSize(mcode));
                }
                addr.release(ec);
                vstack.push(result);
                break;
            }
            case LOADINT:
            case LOADFLOAT:
            case LOADADDRESS:
            case LOADOBJECTREFERENCE:
            case LOADWORD:
            case PREPAREINT:
            case PREPAREADDRESS:
            case PREPAREOBJECTREFERENCE:
            case PREPAREWORD: {
                if (Vm.VerifyAssertions) Vm._assert(!isstatic);
                final RefItem addr = vstack.popRef();
                addr.load(ec);
                final GPR r = addr.getRegister();
                final WordItem result = L1AHelper.requestWordRegister(ec, methodToType(mcode), false);
                final GPR resultr = result.getRegister();
                os.writeMOV(resultr.getSize(), resultr, r, 0);
                addr.release(ec);
                vstack.push(result);
                break;
            }
            case LOADLONG:
            case LOADDOUBLE: {
                if (Vm.VerifyAssertions) Vm._assert(!isstatic);
                final RefItem addr = vstack.popRef();
                addr.load(ec);
                final X86Register.GPR r = addr.getRegister();
                if (os.isCode32()) {
                    final X86Register.GPR msb = (X86Register.GPR) L1AHelper.requestRegister(ec, JvmType.INT, false);
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
                break;
            }
            case LOADBYTE_OFS:
            case LOADCHAR_OFS:
            case LOADSHORT_OFS: {
                if (Vm.VerifyAssertions) Vm._assert(!isstatic);
                final RefItem ofs = vstack.popRef();
                final RefItem addr = vstack.popRef();
                ofs.load(ec);
                addr.load(ec);
                final GPR ofsr = ofs.getRegister();
                final GPR r = addr.getRegister();
                os.writeLEA(r, r, ofsr, 1, 0);
                final WordItem result = L1AHelper.requestWordRegister(ec, methodToType(mcode), true);
                final GPR resultr = result.getRegister();
                if (mcode == LOADCHAR_OFS) {
                    os.writeMOVZX(resultr, r, 0, methodToSize(mcode));
                } else {
                    os.writeMOVSX(resultr, r, 0, methodToSize(mcode));
                }
                ofs.release(ec);
                addr.release(ec);
                vstack.push(result);
                break;
            }
            case LOADINT_OFS:
            case LOADFLOAT_OFS:
            case LOADADDRESS_OFS:
            case LOADOBJECTREFERENCE_OFS:
            case LOADWORD_OFS:
            case PREPAREINT_OFS:
            case PREPAREADDRESS_OFS:
            case PREPAREOBJECTREFERENCE_OFS:
            case PREPAREWORD_OFS: {
                if (Vm.VerifyAssertions) Vm._assert(!isstatic);
                final RefItem ofs = vstack.popRef();
                final RefItem addr = vstack.popRef();
                ofs.load(ec);
                addr.load(ec);
                final GPR ofsr = ofs.getRegister();
                final GPR r = addr.getRegister();
                final WordItem result = L1AHelper.requestWordRegister(ec, methodToType(mcode), false);
                final GPR resultr = result.getRegister();
                os.writeMOV(resultr.getSize(), resultr, r, ofsr, 1, 0);
                ofs.release(ec);
                addr.release(ec);
                vstack.push(result);
                break;
            }
            case LOADLONG_OFS:
            case LOADDOUBLE_OFS: {
                if (Vm.VerifyAssertions) Vm._assert(!isstatic);
                final RefItem ofs = vstack.popRef();
                final RefItem addr = vstack.popRef();
                ofs.load(ec);
                addr.load(ec);
                final GPR ofsr = ofs.getRegister();
                final GPR r = addr.getRegister();
                if (os.isCode32()) {
                    final GPR msb = (GPR) L1AHelper.requestRegister(ec, JvmType.INT, false);
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
                break;
            }
            case STOREBYTE:
            case STORECHAR:
            case STORESHORT: {
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
                break;
            }
            case STOREINT:
            case STOREFLOAT:
            case STOREADDRESS:
            case STOREOBJECTREFERENCE:
            case STOREWORD: {
                if (Vm.VerifyAssertions) Vm._assert(!isstatic);
                final WordItem val = (WordItem) vstack.pop();
                final RefItem addr = vstack.popRef();
                val.load(ec);
                addr.load(ec);
                final GPR r = addr.getRegister();
                final GPR valr = val.getRegister();
                os.writeMOV(valr.getSize(), r, 0, valr);
                val.release(ec);
                addr.release(ec);
                break;
            }
            case STORELONG:
            case STOREDOUBLE: {
                if (Vm.VerifyAssertions) Vm._assert(!isstatic);
                final DoubleWordItem val = (DoubleWordItem) vstack.pop();
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
                break;
            }
            case STOREBYTE_OFS:
            case STORECHAR_OFS:
            case STORESHORT_OFS: {
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
                break;
            }
            case STOREINT_OFS:
            case STOREFLOAT_OFS:
            case STOREADDRESS_OFS:
            case STOREOBJECTREFERENCE_OFS:
            case STOREWORD_OFS: {
                if (Vm.VerifyAssertions) Vm._assert(!isstatic);
                final RefItem ofs = vstack.popRef();
                final WordItem val = (WordItem) vstack.pop();
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
                break;
            }
            case STORELONG_OFS:
            case STOREDOUBLE_OFS: {
                if (Vm.VerifyAssertions) Vm._assert(!isstatic);
                final RefItem ofs = vstack.popRef();
                final DoubleWordItem val = (DoubleWordItem) vstack.pop();
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
                break;
            }

            case ATTEMPTINT:
            case ATTEMPTADDRESS:
            case ATTEMPTOBJECTREFERENCE:
            case ATTEMPTWORD: {
                if (Vm.VerifyAssertions) Vm._assert(!isstatic);
                final WordItem val = (WordItem) vstack.pop();
                final WordItem old = (WordItem) vstack.pop();
                final RefItem addr = vstack.popRef();
                final X86Register.GPR aax;
                if ((mcode == ATTEMPTINT) || os.isCode32()) {
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
                final IntItem result = (IntItem) L1AHelper.requestWordRegister(ec, JvmType.INT, true);
                final GPR resultr = result.getRegister();

                final GPR r = addr.getRegister();
                final GPR valr = val.getRegister();
                os.writeCMPXCHG_EAX(r, 0, valr, true);
                os.writeSETCC(resultr, X86Constants.JZ);
                os.writeAND(resultr, 0xFF);

                val.release(ec);
                old.release(ec);
                addr.release(ec);
                vstack.push(result);
                break;
            }

            case ATTEMPTINT_OFS:
            case ATTEMPTADDRESS_OFS:
            case ATTEMPTOBJECTREFERENCE_OFS:
            case ATTEMPTWORD_OFS: {
                if (Vm.VerifyAssertions) Vm._assert(!isstatic);
                final RefItem ofs = vstack.popRef();
                final WordItem val = (WordItem) vstack.pop();
                final WordItem old = (WordItem) vstack.pop();
                final RefItem addr = vstack.popRef();
                final X86Register.GPR aax;
                if ((mcode == ATTEMPTINT) || os.isCode32()) {
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
                final IntItem result = (IntItem) L1AHelper.requestWordRegister(ec, JvmType.INT, true);
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
                break;
            }

            case GETOBJECTTYPE: {
                if (Vm.VerifyAssertions) Vm._assert(isstatic);
                final RefItem obj = vstack.popRef();
                obj.load(ec);
                final GPR r = obj.getRegister();
                final RefItem result = (RefItem) L1AHelper.requestWordRegister(ec, JvmType.REFERENCE, false);
                final GPR resultr = result.getRegister();
                // Get TIB
                os.writeMOV(helper.ADDRSIZE, r, r, ObjectLayout.TIB_SLOT * slotSize);
                // Get VmType
                os.writeMOV(helper.ADDRSIZE, resultr, r, (TIBLayout.VMTYPE_INDEX + VmArray.DATA_OFFSET) * slotSize);
                obj.release(ec);
                vstack.push(result);
                break;
            }
            case GETTIB: {
                if (Vm.VerifyAssertions) Vm._assert(isstatic);
                final RefItem obj = vstack.popRef();
                obj.load(ec);
                final GPR r = obj.getRegister();
                // Get TIB
                os.writeMOV(helper.ADDRSIZE, r, r, ObjectLayout.TIB_SLOT * slotSize);
                vstack.push(obj);
                break;
            }
            case GETOBJECTFLAGS: {
                if (Vm.VerifyAssertions) Vm._assert(isstatic);
                final RefItem obj = vstack.popRef();
                obj.load(ec);
                final GPR r = obj.getRegister();
                // Get flags
                os.writeMOV(helper.ADDRSIZE, r, r, ObjectLayout.FLAGS_SLOT * slotSize);
                obj.release(ec);
                vstack.push(L1AHelper.requestWordRegister(ec, JvmType.REFERENCE, r));
                break;
            }
            case SETOBJECTFLAGS: {
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
                break;
            }
            case GETARRAYDATA: {
                if (Vm.VerifyAssertions) Vm._assert(isstatic);
                final RefItem obj = vstack.popRef();
                obj.load(ec);
                final GPR r = obj.getRegister();
                os.writeADD(r, VmArray.DATA_OFFSET * slotSize);
                vstack.push(obj);
                break;
            }
            case GETOBJECTCOLOR: {
                if (Vm.VerifyAssertions) Vm._assert(isstatic);
                final RefItem obj = vstack.popRef();
                obj.load(ec);
                final GPR r = obj.getRegister();
                final IntItem result = (IntItem) L1AHelper.requestWordRegister(ec, JvmType.INT, false);
                final GPR resultr = result.getRegister();
                // Get flags
                os.writeMOV(BITS32, resultr, r, ObjectLayout.FLAGS_SLOT * slotSize);
                os.writeAND(resultr, ObjectFlags.GC_COLOUR_MASK);
                obj.release(ec);
                vstack.push(result);
                break;
            }
            case ISFINALIZED: {
                if (Vm.VerifyAssertions) Vm._assert(isstatic);
                final RefItem obj = vstack.popRef();
                obj.load(ec);
                final GPR r = obj.getRegister();
                final IntItem result = (IntItem) L1AHelper.requestWordRegister(ec, JvmType.INT, false);
                final GPR resultr = result.getRegister();
                // Get flags
                os.writeMOV(BITS32, resultr, r, ObjectLayout.FLAGS_SLOT * slotSize);
                os.writeAND(resultr, ObjectFlags.STATUS_FINALIZED);
                obj.release(ec);
                vstack.push(result);
                break;
            }
            case ATOMICADD:
            case ATOMICAND:
            case ATOMICOR:
            case ATOMICSUB: {
                if (Vm.VerifyAssertions) Vm._assert(!isstatic);
                final RefItem value = vstack.popRef();
                final RefItem addr = vstack.popRef();
                value.load(ec);
                addr.load(ec);
                final X86Register.GPR valuer = (X86Register.GPR) value.getRegister();
                final X86Register.GPR r = (X86Register.GPR) addr.getRegister();
                os.writePrefix(X86Constants.LOCK_PREFIX);
                os.writeArithOp(methodCodeToOperation(mcode), r, 0, valuer);
                value.release(ec);
                addr.release(ec);
                break;
            }
            case GETCURRENTFRAME: {
                if (Vm.VerifyAssertions) Vm._assert(isstatic);
                final WordItem result = L1AHelper.requestWordRegister(ec, JvmType.REFERENCE, false);
                final GPR r = result.getRegister();
                os.writeMOV(helper.ADDRSIZE, r, helper.BP);
                vstack.push(result);
                break;
            }
            case GETTIMESTAMP: {
                if (Vm.VerifyAssertions) Vm._assert(isstatic);
                if (os.isCode32()) {
                    final DoubleWordItem result =
                        L1AHelper.requestDoubleWordRegisters(ec, JvmType.LONG, X86Register.EAX, X86Register.EDX);
                    os.writeRDTSC();
                    vstack.push(result);
                } else {
                    final DoubleWordItem result =
                        L1AHelper.requestDoubleWordRegister(ec, JvmType.LONG, X86Register.RAX);
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
                break;
            }
            case INTBITSTOFLOAT:
            case FLOATTORAWINTBITS: {
                if (Vm.VerifyAssertions) Vm._assert(isstatic);
                final WordItem v = (WordItem) vstack.pop();
                v.load(ec);
                final X86Register.GPR r = v.getRegister();
                v.release(ec);
                final int resultType = (mcode == INTBITSTOFLOAT) ? JvmType.FLOAT : JvmType.INT;
                vstack.push(L1AHelper.requestWordRegister(ec, resultType, r));
                break;
            }
            case LONGBITSTODOUBLE:
            case DOUBLETORAWLONGBITS: {
                if (Vm.VerifyAssertions) Vm._assert(isstatic);
                final DoubleWordItem v = (DoubleWordItem) vstack.pop();
                v.load(ec);
                final int resultType = (mcode == LONGBITSTODOUBLE) ? JvmType.DOUBLE : JvmType.LONG;
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
                break;
            }
            case BREAKPOINT: {
                if (Vm.VerifyAssertions) Vm._assert(isstatic);
                os.writeINT(3);
                break;
            }
            case CURRENTPROCESSOR: {
                if (Vm.VerifyAssertions) Vm._assert(isstatic);
                final RefItem item = (RefItem) L1AHelper.requestWordRegister(ec, JvmType.REFERENCE, false);
                final int offset = ec.getContext().getVmProcessorMeField().getOffset();
                if (os.isCode32()) {
                    os.writePrefix(X86Constants.FS_PREFIX);
                    os.writeMOV(item.getRegister(), offset);
                } else {
                    os.writeMOV(BITS64, item.getRegister(), X86Register.R12, offset);
                }
                vstack.push(item);
                break;
            }
            case GETSHAREDSTATICSFIELDADDRESS: {
                if (Vm.VerifyAssertions) Vm._assert(isstatic);
                final IntItem index = (IntItem) vstack.pop();
                index.load(ec);
                final RefItem item = (RefItem) L1AHelper.requestWordRegister(ec, JvmType.REFERENCE, false);
                final GPR itemReg = item.getRegister();
                final int offset = ec.getContext().getVmProcessorSharedStaticsTable().getOffset();
                // Load table
                if (os.isCode32()) {
                    os.writePrefix(X86Constants.FS_PREFIX);
                    os.writeMOV(itemReg, offset);
                } else {
                    os.writeMOV(BITS64, itemReg, X86CompilerConstants.PROCESSOR64, offset);
                }
                GPR indexReg = index.getRegister();
                if (os.isCode64()) {
                    GPR64 indexReg64 = L1AHelper.get64BitReg(ec, indexReg);
                    os.writeMOVSXD(indexReg64, (GPR32) indexReg);
                    indexReg = indexReg64;
                }
                os.writeLEA(itemReg, itemReg, indexReg, os.getWordSize(), VmArray.DATA_OFFSET * os.getWordSize());
                index.release(ec);
                vstack.push(item);
                break;
            }
            case GETISOLATEDSTATICSFIELDADDRESS: {
                if (Vm.VerifyAssertions) Vm._assert(isstatic);
                final IntItem index = (IntItem) vstack.pop();
                index.load(ec);
                final RefItem item = (RefItem) L1AHelper.requestWordRegister(ec, JvmType.REFERENCE, false);
                final GPR itemReg = item.getRegister();
                final int offset = ec.getContext().getVmProcessorIsolatedStaticsTable().getOffset();
                // Load table
                if (os.isCode32()) {
                    os.writePrefix(X86Constants.FS_PREFIX);
                    os.writeMOV(itemReg, offset);
                } else {
                    os.writeMOV(BITS64, itemReg, X86CompilerConstants.PROCESSOR64, offset);
                }
                GPR indexReg = index.getRegister();
                if (os.isCode64()) {
                    GPR64 indexReg64 = L1AHelper.get64BitReg(ec, indexReg);
                    os.writeMOVSXD(indexReg64, (GPR32) indexReg);
                    indexReg = indexReg64;
                }
                os.writeLEA(itemReg, itemReg, indexReg, os.getWordSize(), VmArray.DATA_OFFSET * os.getWordSize());
                index.release(ec);
                vstack.push(item);
                break;
            }
            case ISRUNNINGJNODE: {
                if (Vm.VerifyAssertions) Vm._assert(isstatic);
                vstack.push(ifac.createIConst(ec, 1));
                break;
            }

            // xyzArray classes
            case ARR_CREATE: {
                if (os.isCode32()) {
                    bcv.visit_newarray(10); // int[]
                } else {
                    bcv.visit_newarray(11); // long[]
                }
                break;
            }
            case ARR_GET: {
                bcv.waload(JvmType.REFERENCE);
                break;
            }
            case ARR_SET: {
                bcv.wastore(JvmType.REFERENCE);
                break;
            }
            case ARR_LENGTH: {
                bcv.visit_arraylength();
                break;
            }

            default:
                throw new InternalError("Unknown method code for method " + method);
        }
    }
}
