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
 
package org.jnode.vm.compiler;

import gnu.java.lang.VMClassHelper;

import org.jnode.util.BootableHashMap;
import org.jnode.vm.classmgr.VmMethod;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class BaseMagicHelper {

    protected static final int cADDRESS = 1;

    protected static final int cEXTENT = 2;

    protected static final int cOBJECTREFERENCE = 3;

    protected static final int cOFFSET = 4;

    protected static final int cWORD = 5;
    
    protected static final int cVMMAGIC = 6;

    protected static final int mADD = 1;

    protected static final int mAND = 2;

    protected static final int mOR = 3;

    protected static final int mNOT = 4;

    protected static final int mSUB = 5;

    protected static final int mXOR = 6;

    protected static final int mZERO = 7;

    protected static final int mMAX = 8;

    protected static final int mONE = 9;

    protected static final int mTOINT = 10;

    protected static final int mTOLONG = 11;

    protected static final int mTOWORD = 12;

    protected static final int mTOADDRESS = 13;

    protected static final int mTOEXTENT = 14;

    protected static final int mTOOFFSET = 15;

    protected static final int mTOOBJECTREFERENCE = 16;

    protected static final int mEQUALS = 17;

    protected static final int mISZERO = 18;

    protected static final int mISMAX = 19;

    protected static final int mEQ = 20;

    protected static final int mNE = 21;

    protected static final int mGT = 22;

    protected static final int mGE = 23;

    protected static final int mLT = 24;

    protected static final int mLE = 25;

    protected static final int mSGT = 26;

    protected static final int mSGE = 27;

    protected static final int mSLT = 28;

    protected static final int mSLE = 29;

    protected static final int mFROMINT = 30;

    protected static final int mFROMINTSIGNEXTEND = 31;

    protected static final int mFROMINTZEROEXTEND = 32;

    protected static final int mFROMLONG = 33;

    protected static final int mLSH = 34;

    protected static final int mRSHL = 35;

    protected static final int mRSHA = 36;

    // Make sure that the method code for loadXYV(Offset) is always
    // equal to method code of loadXYZ() + 1
    protected static final int mLOADBYTE = 40;

    protected static final int mLOADBYTE_OFS = 41;

    protected static final int mLOADCHAR = 42;

    protected static final int mLOADCHAR_OFS = 43;

    protected static final int mLOADSHORT = 44;

    protected static final int mLOADSHORT_OFS = 45;

    protected static final int mLOADINT = 46;

    protected static final int mLOADINT_OFS = 47;

    protected static final int mLOADFLOAT = 48;

    protected static final int mLOADFLOAT_OFS = 49;

    protected static final int mLOADLONG = 50;

    protected static final int mLOADLONG_OFS = 51;

    protected static final int mLOADDOUBLE = 52;

    protected static final int mLOADDOUBLE_OFS = 53;

    protected static final int mLOADADDRESS = 54;

    protected static final int mLOADADDRESS_OFS = 55;

    protected static final int mLOADWORD = 56;

    protected static final int mLOADWORD_OFS = 57;

    protected static final int mLOADOBJECTREFERENCE = 58;

    protected static final int mLOADOBJECTREFERENCE_OFS = 59;

    protected static final int mSTOREBYTE = 60;

    protected static final int mSTOREBYTE_OFS = 61;

    protected static final int mSTORECHAR = 62;

    protected static final int mSTORECHAR_OFS = 63;

    protected static final int mSTORESHORT = 64;

    protected static final int mSTORESHORT_OFS = 65;

    protected static final int mSTOREINT = 66;

    protected static final int mSTOREINT_OFS = 67;

    protected static final int mSTOREFLOAT = 68;

    protected static final int mSTOREFLOAT_OFS = 69;

    protected static final int mSTORELONG = 70;

    protected static final int mSTORELONG_OFS = 71;

    protected static final int mSTOREDOUBLE = 72;

    protected static final int mSTOREDOUBLE_OFS = 73;

    protected static final int mSTOREADDRESS = 74;

    protected static final int mSTOREADDRESS_OFS = 75;

    protected static final int mSTOREWORD = 76;

    protected static final int mSTOREWORD_OFS = 77;

    protected static final int mSTOREOBJECTREFERENCE = 78;

    protected static final int mSTOREOBJECTREFERENCE_OFS = 79;

    protected static final int mPREPAREINT = 80;

    protected static final int mPREPAREINT_OFS = 81;

    protected static final int mPREPAREADDRESS = 82;

    protected static final int mPREPAREADDRESS_OFS = 83;

    protected static final int mPREPAREWORD = 84;

    protected static final int mPREPAREWORD_OFS = 85;

    protected static final int mPREPAREOBJECTREFERENCE = 86;

    protected static final int mPREPAREOBJECTREFERENCE_OFS = 87;

    protected static final int mATTEMPTINT = 88;

    protected static final int mATTEMPTINT_OFS = 89;

    protected static final int mATTEMPTADDRESS = 90;

    protected static final int mATTEMPTADDRESS_OFS = 91;

    protected static final int mATTEMPTOBJECTREFERENCE = 92;

    protected static final int mATTEMPTOBJECTREFERENCE_OFS = 93;

    protected static final int mATTEMPTWORD = 94;

    protected static final int mATTEMPTWORD_OFS = 95;

    protected static final int mFROMOBJECT = 96;
    protected static final int mFROMADDRESS = 97;
    protected static final int mGETOBJECTTYPE = 98;
    protected static final int mGETTIB = 99;
    protected static final int mGETOBJECTFLAGS = 100;
    protected static final int mSETOBJECTFLAGS = 101;
    protected static final int mTOOBJECT = 102;
    protected static final int mGETARRAYDATA = 103;
    protected static final int mGETOBJECTCOLOR = 104;
    protected static final int mISFINALIZED = 105;
    protected static final int mATOMICADD = 106;
    protected static final int mATOMICAND = 107;
    protected static final int mATOMICOR = 108;
    protected static final int mATOMICSUB = 109;
    protected static final int mGETCURRENTFRAME = 110;
    protected static final int mGETTIMESTAMP = 111;

    private static final int mLOAD_MIN = mLOADBYTE;

    private static final int mLOAD_MAX = mLOADOBJECTREFERENCE_OFS;

    private static final int mPREPARE_MIN = mPREPAREINT;

    private static final int mPREPARE_MAX = mPREPAREOBJECTREFERENCE_OFS;

    private final BootableHashMap methodNames = new BootableHashMap();

    public BaseMagicHelper() {
        addMethod("add", mADD);
        addMethod("and", mAND);
        addMethod("or", mOR);
        addMethod("not", mNOT);
        addMethod("sub", mSUB);
        addMethod("xor", mXOR);
        addMethod("max", mMAX);
        addMethod("zero", mZERO);
        addMethod("one", mONE);
        addMethod("toInt", mTOINT);
        addMethod("toLong", mTOLONG);
        addMethod("toWord", mTOWORD);
        addMethod("toAddress", mTOADDRESS);
        addMethod("toOffset", mTOOFFSET);
        addMethod("toObject", mTOOBJECT);
        addMethod("toObjectReference", mTOOBJECTREFERENCE);
        addMethod("toExtent", mTOEXTENT);
        addMethod("equals", mEQUALS);
        addMethod("isMax", mISMAX);
        addMethod("isZero", mISZERO);
        addMethod("EQ", mEQ);
        addMethod("NE", mNE);
        addMethod("LT", mLT);
        addMethod("LE", mLE);
        addMethod("GT", mGT);
        addMethod("GE", mGE);
        addMethod("sLT", mSLT);
        addMethod("sLE", mSLE);
        addMethod("sGT", mSGT);
        addMethod("sGE", mSGE);
        addMethod("fromInt", mFROMINT);
        addMethod("fromIntSignExtend", mFROMINTSIGNEXTEND);
        addMethod("fromIntZeroExtend", mFROMINTZEROEXTEND);
        addMethod("fromLong", mFROMLONG);
        addMethod("fromAddress", mFROMADDRESS);
        addMethod("fromObject", mFROMOBJECT);
        addMethod("lsh", mLSH);
        addMethod("rsha", mRSHA);
        addMethod("rshl", mRSHL);
        addMethod("loadByte", mLOADBYTE);
        addMethod("loadChar", mLOADCHAR);
        addMethod("loadShort", mLOADSHORT);
        addMethod("loadInt", mLOADINT);
        addMethod("loadFloat", mLOADFLOAT);
        addMethod("loadLong", mLOADLONG);
        addMethod("loadDouble", mLOADDOUBLE);
        addMethod("loadAddress", mLOADADDRESS);
        addMethod("loadObjectReference", mLOADOBJECTREFERENCE);
        addMethod("loadWord", mLOADWORD);
        addMethod("prepareInt", mPREPAREINT);
        addMethod("prepareAddress", mPREPAREADDRESS);
        addMethod("prepareObjectReference", mPREPAREOBJECTREFERENCE);
        addMethod("prepareWord", mPREPAREWORD);
        addMethod("atomicAdd", mATOMICADD);
        addMethod("atomicAnd", mATOMICAND);
        addMethod("atomicOr", mATOMICOR);
        addMethod("atomicSub", mATOMICSUB);
        
        // VmMagic
        addMethod("getObjectType", mGETOBJECTTYPE);
        addMethod("getTIB", mGETTIB);
        addMethod("getObjectFlags", mGETOBJECTFLAGS);
        addMethod("setObjectFlags", mSETOBJECTFLAGS);
        addMethod("getArrayData", mGETARRAYDATA);
        addMethod("getObjectColor", mGETOBJECTCOLOR);
        addMethod("isFinalized", mISFINALIZED);
        addMethod("getCurrentFrame", mGETCURRENTFRAME);
        addMethod("getTimeStamp", mGETTIMESTAMP);
    }

    protected static final int getClass(VmMethod method) {
        final String cname = VMClassHelper.getClassNamePortion(method
                .getDeclaringClass().getName());
        if (cname.equals("Address")) {
            return cADDRESS;
        } else if (cname.equals("Extent")) {
            return cEXTENT;
        } else if (cname.equals("ObjectReference")) {
            return cOBJECTREFERENCE;
        } else if (cname.equals("Offset")) {
            return cOFFSET;
        } else if (cname.equals("VmMagic")) {
            return cVMMAGIC;
        } else if (cname.equals("Word")) {
            return cWORD;
        } else {
            throw new InternalError("Unknown magic type " + cname);
        }
    }

    protected final int getMethodCode(VmMethod method) {
        final String mname = method.getName();
        final Integer mcodeInt = (Integer) methodNames.get(mname);
        if (mcodeInt == null) {
            if (mname.equals("store")) {
                return getStoreMethodCode(method);
            } else if (mname.equals("attempt")) {
                return getAttemptMethodCode(method);
            }
            throw new InternalError("Unknown method " + mname + " in "
                    + method.getDeclaringClass().getName());
        }
        int mcode = mcodeInt.intValue();
        if (((mcode >= mLOAD_MIN) && (mcode <= mLOAD_MAX))
                || ((mcode >= mPREPARE_MIN) && (mcode <= mPREPARE_MAX))) {
            if (method.getNoArguments() == 1) {
                mcode++;
            }
        }
        return mcode;
    }

    private final int getAttemptMethodCode(VmMethod method) {
        int mcode;
        switch (method.getSignature().charAt(1)) {
        case 'I':
            mcode = mATTEMPTINT;
            break;
        case 'L':
            final String argType = VMClassHelper.getClassNamePortion(method
                    .getArgumentType(0).getName());
            if (argType.equals("Address")) {
                mcode = mATTEMPTADDRESS;
            } else if (argType.equals("ObjectReference")) {
                mcode = mATTEMPTOBJECTREFERENCE;
            } else if (argType.equals("Word")) {
                mcode = mATTEMPTWORD;
            } else {
                throw new InternalError("Unknown method " + method.getName()
                        + " in " + method.getDeclaringClass().getName());
            }
            break;
        default:
            throw new InternalError("Unknown method " + method.getName()
                    + " in " + method.getDeclaringClass().getName());
        }
        if (method.getNoArguments() == 3) {
            mcode++;
        }
        return mcode;
    }

    private final int getStoreMethodCode(VmMethod method) {
        int mcode;
        switch (method.getSignature().charAt(1)) {
        case 'B':
            mcode = mSTOREBYTE;
            break;
        case 'C':
            mcode = mSTORECHAR;
            break;
        case 'S':
            mcode = mSTORESHORT;
            break;
        case 'I':
            mcode = mSTOREINT;
            break;
        case 'F':
            mcode = mSTOREFLOAT;
            break;
        case 'J':
            mcode = mSTORELONG;
            break;
        case 'D':
            mcode = mSTOREDOUBLE;
            break;
        case 'L':
            final String argType = VMClassHelper.getClassNamePortion(method
                    .getArgumentType(0).getName());
            if (argType.equals("Address")) {
                mcode = mSTOREADDRESS;
            } else if (argType.equals("ObjectReference")) {
                mcode = mSTOREOBJECTREFERENCE;
            } else if (argType.equals("Word")) {
                mcode = mSTOREWORD;
            } else {
                throw new InternalError("Unknown method " + method.getName()
                        + " in " + method.getDeclaringClass().getName());
            }
            break;
        default:
            throw new InternalError("Unknown method " + method.getName()
                    + " in " + method.getDeclaringClass().getName());
        }
        if (method.getNoArguments() == 2) {
            mcode++;
        }
        return mcode;
    }

    private final void addMethod(String mname, int code) {
        methodNames.put(mname, new Integer(code));
    }
}
