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
 
package org.jnode.jnasm.assembler.x86;

import org.jnode.jnasm.assembler.HardwareSupport;
import org.jnode.jnasm.assembler.AssemblerModule;
import org.jnode.jnasm.assembler.Instruction;
import org.jnode.jnasm.assembler.Assembler;
import org.jnode.assembler.x86.X86Assembler;
import org.jnode.assembler.x86.X86BinaryAssembler;
import org.jnode.assembler.x86.X86Constants;
import org.jnode.assembler.x86.X86Register;
import org.jnode.assembler.Label;
import org.jnode.assembler.NativeStream;
import org.jnode.vm.x86.X86CpuID;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.io.OutputStream;
import java.io.IOException;

/**
 * @author Levente S\u00e1ntha (lsantha@users.sourceforge.net)
 */
public class X86Support extends HardwareSupport {
    private final List modules;
    private final List instructions;
    private final Map labels;
    private int pass;
    private final Assembler assembler;
    private X86Assembler nativeStream;

    public X86Support(Assembler assembler, List instructions, Map labels, Map constants) {
        this.modules = new ArrayList();
        this.assembler = assembler;
        this.instructions = instructions;
        this.labels = labels;
        modules.add(new X86Core(labels, constants));
        modules.add(assembler.getPseudo());
    }

    public void assemble(int baseAddress) {
        X86CpuID cpuId = X86CpuID.createID("pentium");
        nativeStream = new X86BinaryAssembler(cpuId, X86Constants.Mode.CODE32, baseAddress);
        doAssembly();
    }

    public void assemble(NativeStream asm) {
        nativeStream = (X86Assembler) asm;
        doAssembly();
    }

    private void doAssembly() {
        for (int i = 0; i < modules.size(); i++) {
            ((AssemblerModule) modules.get(i)).setNativeStream(nativeStream);
        }

        for (Iterator it = instructions.iterator(); it.hasNext();) {
            Instruction ins = (Instruction) it.next();
            //handle prefixes
            int prefix = ins.getPrefix();
            if((prefix & Instruction.LOCK_PREFIX) != 0){
                nativeStream.write8(X86Constants.LOCK_PREFIX);
            } else if ((prefix & Instruction.REP_PREFIX) != 0){
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
                try{
                    int times = ins.getTimes();
                    if(times > 0){
                        for(;times-- > 0;){
                            emit(ins.getMnemonic(), ins.getOperands(), getOperandSize(ins));
                        }
                    } else {
                        emit(ins.getMnemonic(), ins.getOperands(), getOperandSize(ins));
                    }
                }catch(IllegalArgumentException x){
                    if(Assembler.THROW){
                        throw x;
                    } else {
                        System.out.println(x.getMessage() + " at line " + ins.getLineNumber());
                    }
                }
            }
        }
    }

    public void writeTo(OutputStream out) throws IOException{
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
            assembler.putConstant(label, ref.getOffset());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void emit(String mnemonic, List operands, int operandSize) {
        for (int i = 0; i < modules.size(); i++) {
            if (((AssemblerModule) modules.get(i)).emit(mnemonic, operands, operandSize)) {
                return;
            }
        }
        throw new IllegalArgumentException("Unknown instruction: " + mnemonic);
    }

    public static int getOperandSize(Instruction ins){
        String size = ins.getSizeInfo();
        if(size == null){
            return X86Constants.BITS32;
        } else if("byte".equals(size)) {
            return X86Constants.BITS8;
        } else if("word".equals(size)) {
            return X86Constants.BITS16;
        } else if("dword".equals(size)){
            return X86Constants.BITS32;
        } else if("qword".equals(size)) {
            return X86Constants.BITS64;
        } else {
            throw new IllegalArgumentException("Unknown operand size: " + size);
        }
    }

    public boolean isRegister(String str) {
        return X86Register.isRegister(str);
    }
}
