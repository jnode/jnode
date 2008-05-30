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

import org.jnode.assembler.ObjectResolver;
import org.jnode.assembler.x86.X86Assembler;
import org.jnode.assembler.x86.X86Register;
import org.jnode.util.LittleEndian;
import org.jnode.vm.classmgr.ObjectLayout;
import org.jnode.vm.classmgr.TIBLayout;
import org.jnode.vm.classmgr.VmArray;
import org.jnode.vm.classmgr.VmClassLoader;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.compiler.CompiledIMT;
import org.jnode.vm.compiler.IMTCompiler;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class X86IMTCompiler32 extends IMTCompiler implements
    X86CompilerConstants {

    /**
     * Register that holds the selector
     */
    public static final X86Register SELECTOR_REG = X86Register.EDX;

    /**
     * Size in bytes of an entry in the IMT jump table generated by this method.
     */
    private static final int IMT_ENTRY_SIZE = 6;

    /**
     * Initialize this instance.
     */
    public X86IMTCompiler32() {
    }

    /**
     * Generate the actual invocation code of an interface method. Inputs: EDI
     * statics table (EDI is preserved) EAX object to invoke method on (EAX is
     * destroyed)
     *
     * @param os
     */
    public static void emitInvokeInterface(X86Assembler os, VmMethod method) {
        final int selector = method.getSelector();
        final int index = selector % ObjectLayout.IMT_LENGTH;
        final int offset = (VmArray.DATA_OFFSET * 4) + (index * IMT_ENTRY_SIZE);

        // For debugging only
        // os.writeMOV_Const(Register.ESI, offset);

        // Get tib into EAX
        os.writeMOV(BITS32, X86Register.EAX, X86Register.EAX,
            ObjectLayout.TIB_SLOT * 4);
        // Get selector into EDX
        os.writeMOV_Const(X86Register.EDX, selector);
        // Get compiled IMT into EAX
        os.writeMOV(BITS32, X86Register.EAX, X86Register.EAX,
            (TIBLayout.COMPILED_IMT_INDEX + VmArray.DATA_OFFSET) * 4);
        // Call to offset within compiled IMT
        os.writeLEA(X86Register.EAX, X86Register.EAX, offset);
        os.writeCALL(X86Register.EAX);
    }

    /**
     * Initialize this compiler
     *
     * @param loader
     */
    public void initialize(VmClassLoader loader) {
        // Nothing to do
    }

    /**
     * @see org.jnode.vm.compiler.IMTCompiler#compile(ObjectResolver, Object[],
     *      boolean[])
     */
    public CompiledIMT compile(ObjectResolver resolver__, Object[] imt,
                               boolean[] imtCollisions) {
        final int imtLength = imt.length;

        // Calculate size of code array
        int size = imtLength * IMT_ENTRY_SIZE;
        for (int i = 0; i < imtLength; i++) {
            if (imtCollisions[i]) {
                final Object[] arr = (Object[]) imt[i];
                size += arr.length * 15;
            }
        }

        final byte[] code = new byte[size];
        // Create the jump table
        int extraIndex = imtLength * IMT_ENTRY_SIZE;
        for (int i = 0; i < imtLength; i++) {
            final int ofsStart = i * IMT_ENTRY_SIZE;
            int ofs = ofsStart;

            if (imtCollisions[i]) {
                // Complex route
                final Object[] arr = (Object[]) imt[i];
                final int arrLength = arr.length;
                final int jmpEaxCodeOfs;

                // JMP extraIndex
                code[ofs++] = (byte) 0xE9;
                LittleEndian.setInt32(code, ofs, (extraIndex - ofs) - 4);
                ofs += 4;

                // Create extra field
                for (int k = 0; k < arrLength; k++) {
                    final VmMethod method = (VmMethod) arr[k];

                    if (k + 1 == arrLength) {
                        // Last entry, jump directly
                        // JMP [statics+method_statics_index]
                        extraIndex = genJmpStaticsCodeOfs(code, extraIndex,
                            method.getSharedStaticsIndex());
                    } else {
                        // Non-last entry, compare and jump of select match

                        // CMP selectorReg, imm32_selector
                        code[extraIndex++] = (byte) 0x81;
                        code[extraIndex++] = (byte) 0xFA;
                        LittleEndian.setInt32(code, extraIndex, method
                            .getSelector());
                        extraIndex += 4;

                        // JNE labelAfterJump
                        code[extraIndex++] = (byte) 0x75;
                        code[extraIndex++] = (byte) 0x06;

                        // JMP [statics+method_statics_index]
                        extraIndex = genJmpStaticsCodeOfs(code, extraIndex,
                            method.getSharedStaticsIndex());
                    }
                }
            } else if (imt[i] != null) {
                // Simple route

                // JMP [STATICS+staticsOfs]
                final VmMethod method = (VmMethod) imt[i];
                ofs = genJmpStaticsCodeOfs(code, ofs, method.getSharedStaticsIndex());
            } else {
                // Empty IMT slot
                // INT ABSTRACT_METHOD
                code[ofs++] = (byte) 0xCD;
                code[ofs++] = (byte) X86CompilerConstants.ABSTRACT_METHOD_INTNO;
                // Fill with NOP's
                code[ofs++] = (byte) 0x90;
                code[ofs++] = (byte) 0x90;
                code[ofs++] = (byte) 0x90;
                code[ofs++] = (byte) 0x90;
            }

            if (ofs > ofsStart + IMT_ENTRY_SIZE) {
                throw new InternalError("Adjust IMT_ENTRY_SIZE to "
                    + (ofs - ofsStart));
            }
        }

        return new X86CompiledIMT(code);
    }

    /**
     * Generate: JMP [STATICS+method.staticsOffset]
     *
     * @param code
     * @param ofs
     * @return the new offset
     */
    private final int genJmpStaticsCodeOfs(byte[] code, int ofs,
                                           int staticsIndex) {
        final int offset = (staticsIndex + VmArray.DATA_OFFSET) * 4;
        // JMP [EDI+codeOfs]
        code[ofs++] = (byte) 0xFF;
        code[ofs++] = (byte) 0xA7;
        LittleEndian.setInt32(code, ofs, offset);
        return ofs + 4;
    }
}
