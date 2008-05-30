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

package org.jnode.test;

import java.io.File;
import org.jnode.vm.VmSystemClassLoader;
import org.jnode.vm.bytecode.BytecodeParser;
import org.jnode.vm.bytecode.BytecodeViewer;
import org.jnode.vm.bytecode.ControlFlowGraph;
import org.jnode.vm.classmgr.VmByteCode;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.x86.VmX86Architecture32;

/**
 * @author epr
 */
public class TestControlFlowGraph {

    public static void main(String[] args)
        throws Exception {

        final String className = (args.length > 0) ? args[0] : "java.lang.Object";
        final VmSystemClassLoader loader = new VmSystemClassLoader(new File(".").toURL(), new VmX86Architecture32());
        final VmType vmClass = loader.loadClass(className, true);

        for (int i = 0; i < vmClass.getNoDeclaredMethods(); i++) {
            TestCFG(vmClass.getDeclaredMethod(i));
        }
    }

    private static void TestCFG(VmMethod method) {
        final VmByteCode bc = method.getBytecode();
        System.out.println("Method     " + method);
        System.out.println("MaxStack   " + bc.getMaxStack());
        System.out.println("#Locals    " + bc.getNoLocals());
        System.out.println("#Arg slots " + method.getArgSlotCount());
        final ControlFlowGraph cfg = new ControlFlowGraph(method.getBytecode());
        BytecodeParser.parse(method.getBytecode(), new BytecodeViewer(cfg));

        System.out.println();
    }

}
