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
 
package org.jnode.vm.x86.compiler;

import org.jnode.assembler.x86.X86Constants;
import org.jnode.assembler.x86.X86Operation;
import org.jnode.vm.JvmType;
import org.jnode.vm.compiler.BaseMagicHelper;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class BaseX86MagicHelper extends BaseMagicHelper implements X86Constants {

    /**
     * Convert a method code into an X86 condition code.
     * @param mcode
     */
    protected final int methodToCC(int mcode) {
        switch (mcode) {
        case mEQUALS:
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
    
    protected final int methodToSize(int mcode) {
        switch (mcode) {
        case mLOADBYTE:
        case mLOADBYTE_OFS:
        case mSTOREBYTE:
        case mSTOREBYTE_OFS:
            return X86CompilerConstants.BYTESIZE;
        case mLOADCHAR:
        case mLOADCHAR_OFS:
        case mSTORECHAR:
        case mSTORECHAR_OFS:
        case mLOADSHORT:
        case mLOADSHORT_OFS:
        case mSTORESHORT:
        case mSTORESHORT_OFS:
            return X86CompilerConstants.WORDSIZE;
        default: throw new InternalError("Unknown mcode " + mcode);                    
        }
    }

    protected final int methodToType(int mcode) {
        switch (mcode) {
        case mLOADBYTE:
        case mLOADBYTE_OFS:
        case mLOADCHAR:
        case mLOADCHAR_OFS:
        case mLOADSHORT:
        case mLOADSHORT_OFS:
        case mLOADINT:
        case mLOADINT_OFS:
        case mPREPAREINT:
        case mPREPAREINT_OFS:
            return JvmType.INT;
        case mLOADFLOAT:
        case mLOADFLOAT_OFS:
            return JvmType.FLOAT;
        case mLOADLONG:
        case mLOADLONG_OFS:
            return JvmType.LONG;
        case mLOADDOUBLE:
        case mLOADDOUBLE_OFS:
            return JvmType.DOUBLE;
        case mLOADADDRESS:
        case mLOADADDRESS_OFS:
        case mLOADWORD:
        case mLOADWORD_OFS:
        case mLOADOBJECTREFERENCE:
        case mLOADOBJECTREFERENCE_OFS:
        case mPREPAREADDRESS:
        case mPREPAREADDRESS_OFS:
        case mPREPAREOBJECTREFERENCE:
        case mPREPAREOBJECTREFERENCE_OFS:
        case mPREPAREWORD:
        case mPREPAREWORD_OFS:
            return JvmType.REFERENCE;
        default: throw new InternalError("Unknown mcode " + mcode);                    
        }
    }
    
    protected static final int methodCodeToOperation(int mcode) {
        switch (mcode) {
        case mATOMICADD:
        	return X86Operation.ADD;
        case mATOMICAND:
        	return X86Operation.AND;
        case mATOMICOR:
        	return X86Operation.OR;
        case mATOMICSUB:
        	return X86Operation.SUB;
        default: throw new InternalError("Unknown mcode " + mcode);                    
        }    	
    }
}
