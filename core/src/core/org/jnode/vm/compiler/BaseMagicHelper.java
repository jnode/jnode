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
    protected static final int mEQUALS = 16;
    protected static final int mISZERO = 17;
    protected static final int mISMAX  = 18;

    protected static final int mEQ = 19;
    protected static final int mNE = 20;
    protected static final int mGT = 21;
    protected static final int mGE = 22;
    protected static final int mLT = 23;
    protected static final int mLE = 24;
    protected static final int mSGT = 25;
    protected static final int mSGE = 26;
    protected static final int mSLT = 27;
    protected static final int mSLE = 28;
    
    protected static final int mFROMINT = 29;
    protected static final int mFROMINTSIGNEXTEND = 30;
    protected static final int mFROMINTZEROEXTEND = 31;
    protected static final int mFROMLONG = 32;

    protected static final int mLSH = 33;
    protected static final int mRSHL = 34;
    protected static final int mRSHA = 35;
    
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
        final Integer mcode = (Integer)methodNames.get(mname);
        if (mcode == null) {
            throw new InternalError("Unknown method " + mname + " in " + method.getDeclaringClass().getName());
        }
        return mcode.intValue();
    }
    
    private final void addMethod(String mname, int code) {
        methodNames.put(mname, new Integer(code));
    }
}
