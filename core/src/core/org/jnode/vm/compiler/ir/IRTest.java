/*
 * $Id$
 */
package org.jnode.vm.compiler.ir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.jnode.assembler.x86.AbstractX86Stream;
import org.jnode.assembler.x86.TextX86Stream;
import org.jnode.assembler.x86.X86Stream;
import org.jnode.util.BootableArrayList;
import org.jnode.util.BootableHashMap;
import org.jnode.vm.VmSystemClassLoader;
import org.jnode.vm.bytecode.BytecodeParser;
import org.jnode.vm.classmgr.VmByteCode;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.compiler.ir.quad.Quad;
import org.jnode.vm.x86.VmX86Architecture;
import org.jnode.vm.x86.X86CpuID;
import org.jnode.vm.x86.compiler.l2.X86CodeGenerator;

/**
 * @author Madhu Siddalingaiah
 * @author Levente Sántha
 */
public class IRTest {
	public static void main(String args[]) throws SecurityException, IOException, ClassNotFoundException {
//        System.in.read();
        X86CpuID cpuId = X86CpuID.createID("p5");
        boolean binary = false;

        String className = "org.jnode.vm.compiler.ir.PrimitiveTest";
        if (args.length > 0) {
            String arg0 = args[0];
            if("-b".equals(arg0)){
                binary = true;
                if(args.length > 1){
                    className = args[1];
                }
            }else{
                className = arg0;
            }
        }

        if(binary){
            X86Stream os = new X86Stream(cpuId, 0);
            generateCode(os, className);
            FileOutputStream fos = new FileOutputStream("test.bin");
            os.writeTo(fos);
            fos.close();
        }else{
            TextX86Stream tos = new TextX86Stream(new OutputStreamWriter(System.out), cpuId);
            generateCode(tos, className);
            tos.flush();
        }



/*
		BytecodeViewer bv = new BytecodeViewer();
		BytecodeParser.parse(code, bv);

		// System.out.println(cfg.toString());
		// System.out.println();

		boolean printDeadCode = false;
		boolean printDetail = false;
		IRBasicBlock currentBlock = null;
		for (int i=0; i<n; i+=1) {
			Quad quad = (Quad) quads.get(i);
			if (currentBlock != quad.getBasicBlock()) {
				currentBlock = quad.getBasicBlock();
				System.out.println();
				System.out.println(currentBlock);
			}
			if (printDeadCode && quad.isDeadCode()) {
				if (printDetail) {
					printQuadDetail(quad);
				}
				System.out.println(quad);
			}
			if (!quad.isDeadCode()) {
				if (printDetail) {
					printQuadDetail(quad);
				}
				System.out.println(quad);
			}
		}

		System.out.println();
		System.out.println("Live ranges:");
		n = lv.size();
		for (int i=0; i<n; i+=1) {
			System.out.println(liveRanges[i]);
		}
*/
	}

    private static void generateCode(AbstractX86Stream os, String className) throws MalformedURLException, ClassNotFoundException {


        VmByteCode code = loadByteCode(className);

        X86CodeGenerator x86cg = new X86CodeGenerator(os, code.getLength());

        IRControlFlowGraph cfg = new IRControlFlowGraph(code);

		//BytecodeViewer bv = new BytecodeViewer();
		//BytecodeParser.parse(code, bv);

		//System.out.println(cfg.toString());
		//System.out.println();

        System.out.println(cfg);
        IRGenerator irg = new IRGenerator(cfg);
        BytecodeParser.parse(code, irg);

        BootableArrayList quads = irg.getQuadList();
        int n = quads.size();
        BootableHashMap liveVariables = new BootableHashMap();
        for (int i=0; i<n; i+=1) {
            Quad quad = (Quad) quads.get(i);
//            System.out.println(quad);
            quad.doPass2(liveVariables);
            System.out.println(quad);
        }

        Collection lv = liveVariables.values();
        n = lv.size();
        LiveRange[] liveRanges = new LiveRange[n];
        Iterator it = lv.iterator();
        for (int i=0; i<n; i+=1) {
            Variable v = (Variable) it.next();
            liveRanges[i] = new LiveRange(v);
            // System.out.println("Live range: " + liveRanges[i]);
        }
        Arrays.sort(liveRanges);
        System.out.println(Arrays.asList(liveRanges));
        LinearScanAllocator lsa = new LinearScanAllocator(liveRanges);
        lsa.allocate();

        x86cg.setArgumentVariables(irg.getVariables(), irg.getNoArgs());
        x86cg.setSpilledVariables(lsa.getSpilledVariables());
        x86cg.emitHeader();

        n = quads.size();
        for (int i=0; i<n; i+=1) {
            Quad quad = (Quad) quads.get(i);
            if (!quad.isDeadCode()) {
                quad.generateCode(x86cg);
            }
        }
    }

    private static VmByteCode loadByteCode(String className)
		throws MalformedURLException, ClassNotFoundException {
		VmSystemClassLoader vmc = new VmSystemClassLoader(new File(".").toURL(), new VmX86Architecture());
		VmType type = vmc.loadClass(className, true);
		VmMethod arithMethod = null;
		int nMethods = type.getNoDeclaredMethods();
		for (int i=0; i<nMethods; i+=1) {
			VmMethod method = type.getDeclaredMethod(i);
			if ("terniary".equals(method.getName())) {
				arithMethod = method;
				break;
			}
		}
		VmByteCode code = arithMethod.getBytecode();
		return code;
	}

	public static void printQuadDetail(Quad quad) {
		System.out.print(quad.getBasicBlock());
		System.out.print(" ");
		Variable[] vars = quad.getBasicBlock().getVariables();
		System.out.print("[");
		for (int j=0; j<vars.length; j+=1) {
			System.out.print(vars[j]);
			System.out.print(",");
		}
		System.out.print("] ");
		if (quad.isDeadCode()) {
			System.out.print("(dead) ");
		}
	}
}
