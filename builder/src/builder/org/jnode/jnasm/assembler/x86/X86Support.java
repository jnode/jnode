/**
 * $Id$  
 */
package org.jnode.jnasm.assembler.x86;

import org.jnode.jnasm.assembler.HardwareSupport;
import org.jnode.jnasm.assembler.AssemblerModule;
import org.jnode.jnasm.assembler.Instruction;
import org.jnode.jnasm.assembler.Assembler;
import org.jnode.assembler.x86.X86Assembler;
import org.jnode.assembler.x86.X86BinaryAssembler;
import org.jnode.assembler.x86.X86Constants;
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
 * @author Levente S\u00e1ntha
 */
public class X86Support extends HardwareSupport {
    private final List modules;
    private final List instructions;
    private final Map labels;
    private int pass;
    private final Assembler assembler;
    private X86Assembler nativeStream;

    public X86Support(Assembler assembler, List instructions, Map labels) {
        this.modules = new ArrayList();
        this.assembler = assembler;
        this.instructions = instructions;
        this.labels = labels;
        modules.add(new X86Core(labels));
        modules.add(assembler.getPseudo());
    }

    public void assemble() {
        X86CpuID cpuId = X86CpuID.createID("pentium");
        nativeStream = new X86BinaryAssembler(cpuId, X86Constants.Mode.CODE32, 0);

        for (int i = 0; i < modules.size(); i++) {
            ((AssemblerModule) modules.get(i)).setNativeStream(nativeStream);
        }

        for (Iterator it = instructions.iterator(); it.hasNext();) {
            Instruction ins = (Instruction) it.next();
            String label = ins.getLabel();
            if (label != null) {
                if (labels.get(label) != null && pass == 1)
                    throw new IllegalArgumentException("Label already defined: " + label);
                defineLabel(label, nativeStream);
            }
            String mnemo = ins.getMnemonic();
            if (mnemo != null) {
                try{
                    emmit(ins.getMnemonic(), ins.getOperands());
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

    private void emmit(String mnemonic, List operands) {
        for (int i = 0; i < modules.size(); i++) {
            if (((AssemblerModule) modules.get(i)).emmit(mnemonic, operands)) {
                return;
            }
        }
        throw new IllegalArgumentException("Unknown instruction: " + mnemonic);
    }
}
