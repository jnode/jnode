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
 
package org.jnode.vm.compiler.ir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Iterator;

import org.jnode.assembler.x86.X86Assembler;
import org.jnode.assembler.x86.X86BinaryAssembler;
import org.jnode.assembler.x86.X86TextAssembler;
import org.jnode.util.BootableHashMap;
import org.jnode.vm.VmSystemClassLoader;
import org.jnode.vm.bytecode.BytecodeParser;
import org.jnode.vm.classmgr.VmByteCode;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.compiler.ir.quad.Quad;
import org.jnode.vm.x86.VmX86Architecture32;
import org.jnode.vm.x86.X86CpuID;
import org.jnode.vm.x86.compiler.l2.X86CodeGenerator;

/**
 * @author Madhu Siddalingaiah
 * @author Levente S\u00e1ntha
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
            X86BinaryAssembler os = new X86BinaryAssembler(cpuId, 0);
            generateCode(os, className);
            FileOutputStream fos = new FileOutputStream("test.bin");
            os.writeTo(fos);
            fos.close();
        }else{
            X86TextAssembler tos = new X86TextAssembler(new OutputStreamWriter(System.out), cpuId);
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

    private static void generateCode(X86Assembler os, String className) throws MalformedURLException, ClassNotFoundException {
		//VmByteCode code = loadByteCode(className, "discriminant");
		//VmByteCode code = loadByteCode(className, "arithOptIntx");
		//VmByteCode code = loadByteCode(className, "simpleWhile");
		//VmByteCode code = loadByteCode(className, "terniary2");
		VmByteCode code = loadByteCode(className, "trivial");
		//VmByteCode code = loadByteCode(className, "appel");

        IRControlFlowGraph cfg = new IRControlFlowGraph(code);
		X86CodeGenerator x86cg = new X86CodeGenerator(os, code.getLength());

		//BytecodeViewer bv = new BytecodeViewer();
		//BytecodeParser.parse(code, bv);

		//System.out.println(cfg.toString());
		//System.out.println();

        //System.out.println(cfg);
        IRGenerator irg = new IRGenerator(cfg);
        BytecodeParser.parse(code, irg);

		cfg.constructSSA();
        cfg.optimize();

		cfg.deconstrucSSA();
		cfg.fixupAddresses();
		
		BootableHashMap liveVariables = new BootableHashMap();

        Iterator it = cfg.basicBlockIterator();
        while (it.hasNext()) {
        	IRBasicBlock b = (IRBasicBlock) it.next();
        	System.out.println();
        	System.out.println(b + ", stackOffset = " + b.getStackOffset());
        	Iterator qi = b.getQuads().iterator();
        	while (qi.hasNext()) {
				Quad q = (Quad) qi.next();
				if (!q.isDeadCode()) {
					q.computeLiveness(liveVariables);
					System.out.println(q);
				}
        	}
        }
		System.out.println();

		System.out.println("Live ranges:");
		Collection lv = liveVariables.values();
		LiveRange[] liveRanges = new LiveRange[lv.size()];
		it = lv.iterator();
		for (int i=0; i<liveRanges.length; i+=1) {
			Variable var = (Variable) it.next();
			LiveRange range = new LiveRange(var);
			liveRanges[i] = range;
		}

		LinearScanAllocator lsa = new LinearScanAllocator(liveRanges);
		lsa.allocate();

		for (int i=0; i<liveRanges.length; i+=1) {
			LiveRange range = liveRanges[i];
			System.out.println(range);
		}

		x86cg.setArgumentVariables(irg.getVariables(), irg.getNoArgs());
		x86cg.setSpilledVariables(lsa.getSpilledVariables());
		x86cg.emitHeader();
		it = cfg.basicBlockIterator();
		while (it.hasNext()) {
			IRBasicBlock b = (IRBasicBlock) it.next();
			System.out.println();
			System.out.println(b);
			Iterator qi = b.getQuads().iterator();
			while (qi.hasNext()) {
				Quad q = (Quad) qi.next();
				if (!q.isDeadCode()) {
					q.generateCode(x86cg);
				}
			}
		}

		// TODO
		// 1. Fix method argument location, allocator leaves it null and breaks
		// 2. Many necessary operations are not implemented in the code generator
		// 3. Do something about unused phi nodes, they just waste space right now


//        BootableArrayList quads = irg.getQuadList();
//        int n = quads.size();
//        BootableHashMap liveVariables = new BootableHashMap();
//        for (int i=0; i<n; i+=1) {
//            Quad quad = (Quad) quads.get(i);
////            System.out.println(quad);
//            quad.doPass2(liveVariables);
//            System.out.println(quad);
//        }

//        Collection lv = liveVariables.values();
//        n = lv.size();
//        LiveRange[] liveRanges = new LiveRange[n];
//        Iterator it = lv.iterator();
//        for (int i=0; i<n; i+=1) {
//            Variable v = (Variable) it.next();
//            liveRanges[i] = new LiveRange(v);
//            // System.out.println("Live range: " + liveRanges[i]);
//        }
//        Arrays.sort(liveRanges);
//        System.out.println(Arrays.asList(liveRanges));
//        LinearScanAllocator lsa = new LinearScanAllocator(liveRanges);
//        lsa.allocate();
//
//        x86cg.setArgumentVariables(irg.getVariables(), irg.getNoArgs());
//        x86cg.setSpilledVariables(lsa.getSpilledVariables());
//        x86cg.emitHeader();
//
//        n = quads.size();
//        for (int i=0; i<n; i+=1) {
//            Quad quad = (Quad) quads.get(i);
//            if (!quad.isDeadCode()) {
//                quad.generateCode(x86cg);
//            }
//        }
    }

    private static VmByteCode loadByteCode(String className, String methodName)
		throws MalformedURLException, ClassNotFoundException {
		VmSystemClassLoader vmc = new VmSystemClassLoader(new File(".").toURL(), new VmX86Architecture32());
		VmType type = vmc.loadClass(className, true);
		VmMethod arithMethod = null;
		int nMethods = type.getNoDeclaredMethods();
		for (int i=0; i<nMethods; i+=1) {
			VmMethod method = type.getDeclaredMethod(i);
			if (methodName.equals(method.getName())) {
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
