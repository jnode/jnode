/*
 * $Id$
 */
package org.jnode.test;

import java.io.File;

import org.jnode.vm.VmSystemClassLoader;
import org.jnode.vm.bytecode.BytecodeParser;
import org.jnode.vm.bytecode.BytecodeViewer;
import org.jnode.vm.bytecode.ControlFlowGraph;
import org.jnode.vm.classmgr.VmByteCode;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.x86.VmX86Architecture;

/**
 * @author epr
 */
public class TestControlFlowGraph {

	public static void main(String[] args) 
	throws Exception {

		final String className = (args.length > 0) ? args[0] : "java.lang.Object";
		final VmSystemClassLoader loader = new VmSystemClassLoader(new File(".").toURL(), new VmX86Architecture());
		final VmType vmClass = loader.loadClass(className, true);
		
		for (int i = 0; i < vmClass.getNoDeclaredMethods(); i++) {
			TestCFG(vmClass.getDeclaredMethod(i));
		}
	}
	
	private static void TestCFG(VmMethod method) {
		final VmByteCode bc = method.getBytecode();
		System.out.println("Method " + method);
		System.out.println("MaxStack " + bc.getMaxStack());
		System.out.println("#Locals  " + bc.getNoLocals());
		System.out.println("#Args    " + method.getNoArgs());
		final ControlFlowGraph cfg = new ControlFlowGraph(method.getBytecode());
		BytecodeParser.parse(method.getBytecode(), new BytecodeViewer(cfg));

		System.out.println();
	}
	
}
