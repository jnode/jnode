/*
 * $Id$
 */
package org.jnode.vm.x86.compiler;

import org.jnode.assembler.x86.X86Constants;
import org.jnode.assembler.x86.X86Operation;
import org.jnode.vm.compiler.BaseMagicHelper;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class BaseX86MagicHelper extends BaseMagicHelper {

    /**
     * Convert a method code into an X86 condition code.
     * @param mcode
     */
    protected final int methodToCC(int mcode) {
        switch (mcode) {
        case mEQ: return X86Constants.JE;
        case mNE: return X86Constants.JNE;
        case mLT: return X86Constants.JB;
        case mLE: return X86Constants.JBE;
        case mGT: return X86Constants.JA;
        case mGE: return X86Constants.JAE;
        case mSLT: return X86Constants.JL;
        case mSLE: return X86Constants.JLE;
        case mSGT: return X86Constants.JG;
        case mSGE: return X86Constants.JGE;
        default: throw new InternalError("Unknown mcode " + mcode);
        }
    }
    
    /**
     * Convert a method code into an X86 condition code.
     * @param mcode
     */
    protected final int methodToShift(int mcode) {
        switch (mcode) {
        case mLSH: return X86Operation.SAL;
        case mRSHA: return X86Operation.SAR;
        case mRSHL: return X86Operation.SHR;
        default: throw new InternalError("Unknown mcode " + mcode);
        }
    }
}
