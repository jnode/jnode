/*
 * $Id$
 */
package org.jnode.vm.compiler;

import gnu.java.lang.VMClassHelper;

import org.jnode.util.BootableHashMap;
import org.jnode.vm.classmgr.VmMethod;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class BaseMagicHelper {

    protected static final int ADDRESS = 1;
    protected static final int EXTENT = 2;
    protected static final int OFFSET  = 3;
    protected static final int WORD = 4;
    
    protected static final int mADD = 1;
    protected static final int mAND = 2;
    protected static final int mOR = 3;
    protected static final int mNOT = 4;
    protected static final int mSUB = 5;
    protected static final int mXOR = 6;
    protected static final int mZERO = 7;
    protected static final int mMAX  = 8;
    protected static final int mONE  = 9;
    protected static final int mTOINT = 10;
    protected static final int mTOLONG = 11;
    protected static final int mTOWORD = 12;
    protected static final int mTOADDRESS = 13;
    protected static final int mTOEXTENT = 14;
    protected static final int mTOOFFSET = 15;
    protected static final int mTOOBJECTREFERENCE = 16;
    protected static final int mEQUALS = 17;
    protected static final int mISZERO = 18;
    protected static final int mISMAX  = 19;

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
    
    private static final int mLOAD_MIN = mLOADBYTE;
    private static final int mLOAD_MAX = mLOADOBJECTREFERENCE_OFS;
    
    
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
    }
    
    protected static final int getClass(VmMethod method) {
        final String cname = VMClassHelper.getClassNamePortion(method.getDeclaringClass().getName());
        if (cname.equals("Address")) {
            return ADDRESS;
        } else if (cname.equals("Extent")) {
            return EXTENT;
        } else if (cname.equals("Offset")) {
            return OFFSET;
        } else if (cname.equals("Word")) {
            return WORD;
        } else {
            throw new InternalError("Unknown magic type " + cname);
        }        
    }

    protected final int getMethodCode(VmMethod method) {
        final String mname = method.getName();
        final Integer mcodeInt = (Integer)methodNames.get(mname);
        if (mcodeInt == null) {
            throw new InternalError("Unknown method " + mname + " in " + method.getDeclaringClass().getName());
        }
        int mcode = mcodeInt.intValue();
        if ((mcode >= mLOAD_MIN) && (mcode <= mLOAD_MAX)) {
            if (method.getNoArguments() == 1) {
                mcode++; 
            }
        }
        return mcode;
    }
    
    private final void addMethod(String mname, int code) {
        methodNames.put(mname, new Integer(code));
    }
}
