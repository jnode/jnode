/*
 * $Id$
 */
package org.jnode.vm.compiler.ir;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.jnode.util.BootableArrayList;
import org.jnode.util.BootableHashMap;
import org.jnode.vm.VmSystemClassLoader;
import org.jnode.vm.bytecode.BytecodeParser;
import org.jnode.vm.bytecode.BytecodeViewer;
import org.jnode.vm.classmgr.VmByteCode;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.compiler.ir.quad.*;
import org.jnode.vm.x86.VmX86Architecture;
import org.jnode.vm.x86.compiler.l2.X86CodeGenerator;

/**
 * @author Madhu Siddalingaiah
 * 
 */
public class IRTest {
	public static void main(String args[]) throws SecurityException, IOException, ClassNotFoundException {
		X86CodeGenerator x86cg = new X86CodeGenerator();
		VmByteCode code = loadByteCode(args);

		IRControlFlowGraph cfg = new IRControlFlowGraph(code);

		IRGenerator irg = new IRGenerator(cfg);
		BytecodeParser.parse(code, irg);
		BootableArrayList quads = irg.getQuadList();
		int n = quads.size();
		BootableHashMap liveVariables = new BootableHashMap();
		for (int i=0; i<n; i+=1) {
			Quad quad = (Quad) quads.get(i);
			quad.doPass2(liveVariables);
		}

		Collection lv = liveVariables.values();
		n = lv.size();
		LiveRange[] liveRanges = new LiveRange[n];
		Iterator it = lv.iterator();
		for (int i=0; i<n; i+=1) {
			Variable v = (Variable) it.next();
			liveRanges[i] = new LiveRange(v);
		}
		Arrays.sort(liveRanges);
		LinearScanAllocator lsa = new LinearScanAllocator(liveRanges);
		lsa.allocate();

		x86cg.setVariableMap(lsa.getVariableMap());
		n = quads.size();
		for (int i=0; i<n; i+=1) {
			Quad quad = (Quad) quads.get(i);
			if (!quad.isDeadCode()) {
				quad.generateCode(x86cg);
			}
		}

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
	}

	private static VmByteCode loadByteCode(String[] args)
		throws MalformedURLException, ClassNotFoundException {
		String className = "org.jnode.vm.compiler.ir.IRTest";
		if (args.length > 0) {
			className = args[0];
		}
		VmSystemClassLoader vmc = new VmSystemClassLoader(new File(".").toURL(), new VmX86Architecture());
		VmType type = vmc.loadClass(className, true);
		VmMethod arithMethod = null;
		int nMethods = type.getNoDeclaredMethods();
		for (int i=0; i<nMethods; i+=1) {
			VmMethod method = type.getDeclaredMethod(i);
			if ("arithOptLoop".equals(method.getName())) {
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

	public static int arithOptLoop(int a0, int a1, int a2) {
		int l3 = 1;
		int l4 = 3*a1;
		for (int l5=10; l5 > 0; l5-=1) {
			l3 += 2*a0 + l4;
			l4 += 1;
		}
		return l3;
	}

	public static int arithOptIntx(int a0, int a1, int a2) {
		return a0 + a1;
	}
}
