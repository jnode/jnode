/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.jnasm.assembler.x86;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jnode.assembler.Label;
import org.jnode.assembler.NativeStream;
import org.jnode.assembler.x86.X86Assembler;
import org.jnode.assembler.x86.X86BinaryAssembler;
import org.jnode.assembler.x86.X86Constants;
import org.jnode.assembler.x86.X86Register;
import org.jnode.jnasm.assembler.Assembler;
import org.jnode.jnasm.assembler.AssemblerModule;
import org.jnode.jnasm.assembler.HardwareSupport;
import org.jnode.jnasm.assembler.Instruction;
import org.jnode.vm.x86.X86CpuID;

/**
 * @author Levente S\u00e1ntha (lsantha@users.sourceforge.net)
 */
public class X86Support extends HardwareSupport {
    private final List<AssemblerModule> modules;
    private final List<Instruction> instructions;
    private final Map<String, Label> labels;
    private int pass;
    private final Assembler assembler;
    private X86Assembler nativeStream;

    public X86Support(Assembler assembler, List<Instruction> instructions,
                      Map<String, Label> labels, Map<String, Integer> constants) {
        this.modules = new ArrayList<AssemblerModule>();
        this.assembler = assembler;
        this.instructions = instructions;
        this.labels = labels;
        modules.add(new X86Core(labels, constants));
        modules.add(new MMX(labels, constants));
        modules.add(assembler.getPseudo());
    }

    public void assemble(int baseAddress) {
        X86CpuID cpuId = X86CpuID.createID("pentium");
        nativeStream = new X86BinaryAssembler(cpuId, X86Constants.Mode.CODE32, baseAddress);
        ((X86BinaryAssembler) nativeStream).setByteValueEnabled(false);
        doAssembly();
    }

    public void assemble(NativeStream asm) {
        nativeStream = (X86Assembler) asm;
        if (nativeStream instanceof X86BinaryAssembler) {
            ((X86BinaryAssembler) nativeStream).setByteValueEnabled(false);
        }
        doAssembly();
        if (nativeStream instanceof X86BinaryAssembler) {
            ((X86BinaryAssembler) nativeStream).setByteValueEnabled(true);
        }
    }

    private void doAssembly() {
        for (AssemblerModule asmMod : modules) {
            asmMod.setNativeStream(nativeStream);
        }

        for (Instruction ins : instructions) {
            //handle prefixes
            int prefix = ins.getPrefix();
            if ((prefix & Instruction.LOCK_PREFIX) != 0) {
                nativeStream.write8(X86Constants.LOCK_PREFIX);
            } else if ((prefix & Instruction.REP_PREFIX) != 0) {
                nativeStream.write8(X86Constants.REP_PREFIX);
            }
            String label = ins.getLabel();
            if (label != null) {
                if (labels.get(label) != null && pass == 1)
                    throw new IllegalArgumentException("Label already defined: " + label);
                defineLabel(label, nativeStream);
            }
            String mnemo = ins.getMnemonic();
            if (mnemo != null) {
                try {
                    int times = ins.getTimes();
                    if (times > 0) {
                        for (; times-- > 0;) {
                            emit(ins.getMnemonic(), ins.getOperands(), getOperandSize(ins));
                        }
                    } else {
                        emit(ins.getMnemonic(), ins.getOperands(), getOperandSize(ins));
                    }
                } catch (IllegalArgumentException x) {
                    if (Assembler.THROW) {
                        throw x;
                    } else {
                        System.out.println(x.getMessage() + " at line " + ins.getLineNumber());
                    }
                }
            }
        }
    }

    public void writeTo(OutputStream out) throws IOException {
        nativeStream.writeTo(out);
    }

    public void setPass(int pass) {
        this.pass = pass;
    }

    private void defineLabel(String label, X86Assembler asm) {
        try {
            Label lab = new Label(label);
            labels.put(label, lab);
            NativeStream.ObjectRef ref = asm.setObjectRef(lab);
            assembler.putConstant(label, ref.getOffset() + (int) asm.getBaseAddr());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void emit(String mnemonic, List<Object> operands, int operandSize) {
        for (AssemblerModule module : modules) {
            if (module.emit(mnemonic, operands, operandSize)) {
                return;
            }
        }
        throw new IllegalArgumentException("Unknown instruction: " + mnemonic);
    }

    public static int getOperandSize(Instruction ins) {
        String size = ins.getSizeInfo();
        if (size == null) {
            return X86Constants.BITS32;
        } else if ("byte".equals(size)) {
            return X86Constants.BITS8;
        } else if ("word".equals(size)) {
            return X86Constants.BITS16;
        } else if ("dword".equals(size)) {
            return X86Constants.BITS32;
        } else if ("qword".equals(size)) {
            return X86Constants.BITS64;
        } else {
            throw new IllegalArgumentException("Unknown operand size: " + size);
        }
    }

    public boolean isRegister(String str) {
        return X86Register.isRegister(str);
    }
}
