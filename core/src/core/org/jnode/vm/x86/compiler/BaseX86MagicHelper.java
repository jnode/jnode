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
     *
     * @param mcode
     */
    protected final int methodToCC(MagicMethod mcode) {
        switch (mcode) {
            case EQUALS:
            case EQ:
                return X86Constants.JE;
            case NE:
                return X86Constants.JNE;
            case LT:
                return X86Constants.JB;
            case LE:
                return X86Constants.JBE;
            case GT:
                return X86Constants.JA;
            case GE:
                return X86Constants.JAE;
            case SLT:
                return X86Constants.JL;
            case SLE:
                return X86Constants.JLE;
            case SGT:
                return X86Constants.JG;
            case SGE:
                return X86Constants.JGE;
            default:
                throw new InternalError("Unknown mcode " + mcode);
        }
    }

    /**
     * Convert a method code into an X86 condition code.
     *
     * @param mcode
     */
    protected final int methodToShift(MagicMethod mcode) {
        switch (mcode) {
            case LSH:
                return X86Operation.SAL;
            case RSHA:
                return X86Operation.SAR;
            case RSHL:
                return X86Operation.SHR;
            default:
                throw new InternalError("Unknown mcode " + mcode);
        }
    }

    protected final int methodToSize(MagicMethod mcode) {
        switch (mcode) {
            case LOADBYTE:
            case LOADBYTE_OFS:
            case STOREBYTE:
            case STOREBYTE_OFS:
                return X86CompilerConstants.BYTESIZE;
            case LOADCHAR:
            case LOADCHAR_OFS:
            case STORECHAR:
            case STORECHAR_OFS:
            case LOADSHORT:
            case LOADSHORT_OFS:
            case STORESHORT:
            case STORESHORT_OFS:
                return X86CompilerConstants.WORDSIZE;
            default:
                throw new InternalError("Unknown mcode " + mcode);
        }
    }

    protected final int methodToType(MagicMethod mcode) {
        switch (mcode) {
            case LOADBYTE:
            case LOADBYTE_OFS:
            case LOADCHAR:
            case LOADCHAR_OFS:
            case LOADSHORT:
            case LOADSHORT_OFS:
            case LOADINT:
            case LOADINT_OFS:
            case PREPAREINT:
            case PREPAREINT_OFS:
                return JvmType.INT;
            case LOADFLOAT:
            case LOADFLOAT_OFS:
                return JvmType.FLOAT;
            case LOADLONG:
            case LOADLONG_OFS:
                return JvmType.LONG;
            case LOADDOUBLE:
            case LOADDOUBLE_OFS:
                return JvmType.DOUBLE;
            case LOADADDRESS:
            case LOADADDRESS_OFS:
            case LOADWORD:
            case LOADWORD_OFS:
            case LOADOBJECTREFERENCE:
            case LOADOBJECTREFERENCE_OFS:
            case PREPAREADDRESS:
            case PREPAREADDRESS_OFS:
            case PREPAREOBJECTREFERENCE:
            case PREPAREOBJECTREFERENCE_OFS:
            case PREPAREWORD:
            case PREPAREWORD_OFS:
                return JvmType.REFERENCE;
            default:
                throw new InternalError("Unknown mcode " + mcode);
        }
    }

    protected static final int methodCodeToOperation(MagicMethod mcode) {
        switch (mcode) {
            case ATOMICADD:
                return X86Operation.ADD;
            case ATOMICAND:
                return X86Operation.AND;
            case ATOMICOR:
                return X86Operation.OR;
            case ATOMICSUB:
                return X86Operation.SUB;
            default:
                throw new InternalError("Unknown mcode " + mcode);
        }
    }
}
