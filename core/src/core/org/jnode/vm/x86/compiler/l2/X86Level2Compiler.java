/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
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
 
package org.jnode.vm.x86.compiler.l2;

import java.util.Collection;
import java.util.List;
import org.jnode.assembler.Label;
import org.jnode.assembler.NativeStream;
import org.jnode.assembler.ObjectResolver;
import org.jnode.assembler.x86.X86Assembler;
import org.jnode.assembler.x86.X86BinaryAssembler;
import org.jnode.vm.bytecode.BytecodeParser;
import org.jnode.vm.classmgr.VmByteCode;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.compiler.CompiledMethod;
import org.jnode.vm.compiler.CompilerBytecodeVisitor;
import org.jnode.vm.compiler.EntryPoints;
import org.jnode.vm.compiler.GCMapIterator;
import org.jnode.vm.compiler.ir.CodeGenerator;
import org.jnode.vm.compiler.ir.IRBasicBlock;
import org.jnode.vm.compiler.ir.IRControlFlowGraph;
import org.jnode.vm.compiler.ir.IRGenerator;
import org.jnode.vm.compiler.ir.LinearScanAllocator;
import org.jnode.vm.compiler.ir.LiveRange;
import org.jnode.vm.compiler.ir.StackLocation;
import org.jnode.vm.compiler.ir.Variable;
import org.jnode.vm.compiler.ir.quad.Quad;
import org.jnode.vm.facade.TypeSizeInfo;
import org.jnode.vm.scheduler.VmProcessor;
import org.jnode.vm.x86.X86CpuID;
import org.jnode.vm.x86.compiler.AbstractX86Compiler;
import org.jnode.vm.x86.compiler.X86CompilerHelper;

import static org.jnode.vm.x86.compiler.X86CompilerConstants.L2_COMPILER_MAGIC;

/**
 * User: lsantha
 * Date: 8/31/14 8:33 PM
 */
public class X86Level2Compiler extends AbstractX86Compiler {

    public static boolean canCompile(VmMethod method) {
        try {
            BytecodeParser.parse(method.getBytecode(), new L2ByteCodeSupportChecker());
            return true;
        } catch (Exception x) {
            //ignore
        }
        return false;
    }

    /**
     * Compile the given method during runtime.
     *
     * @param method
     * @param resolver
     * @param level    Optimization level
     * @param os       The native stream, can be null
     */
    public void compileRuntime(VmMethod method, ObjectResolver resolver,
                               int level, NativeStream os) {
        if (method.isNative()) {
            throw new IllegalArgumentException("Cannot compile native methods");
        }
        if (method.isAbstract() || canCompile(method)) {
            super.compileRuntime(method, resolver, level, os);
        }
    }

    /**
     * Create a native stream for the current architecture.
     *
     * @param resolver
     * @return NativeStream
     */
    public NativeStream createNativeStream(ObjectResolver resolver) {
        X86CpuID cpuid = (X86CpuID) VmProcessor.current().getCPUID();
        X86BinaryAssembler os = new X86BinaryAssembler(cpuid, getMode(), 0);
        os.setResolver(resolver);
        return os;
    }

    public static LiveRange<?>[] getLiveRanges(List<Variable<?>> liveVariables) {
        Collection<Variable<?>> lv = liveVariables;
        LiveRange<?>[] liveRanges = new LiveRange[lv.size()];
        int i = 0;
        for (Variable<?> var : lv) {
            LiveRange<?> range = new LiveRange(var);
            liveRanges[i++] = range;
        }
        return liveRanges;
    }

    public static LinearScanAllocator allocate(LiveRange[] liveRanges) {
        LinearScanAllocator lsa = new LinearScanAllocator(liveRanges);
        lsa.allocate();
        return lsa;
    }

    public static void generateCode(CodeGenerator cg, IRControlFlowGraph cfg,
                                                            IRGenerator irg, LinearScanAllocator lsa) {
        X86CodeGenerator x86cg = (X86CodeGenerator) cg;
//        x86cg.setArgumentVariables(irg.getVariables(), irg.getNoArgs());
        x86cg.setSpilledVariables(lsa.getSpilledVariables());
        x86cg.emitHeader();
        for (IRBasicBlock b : ((Iterable<? extends IRBasicBlock>) cfg)) {
//            System.out.println();
//            System.out.println(b);
            for (Quad q :  (List<Quad>) b.getQuads()) {
                if (!q.isDeadCode()) {
                    q.generateCode(cg);
                }
            }
        }
        x86cg.endMethod();
    }

    @Override
    protected CompilerBytecodeVisitor createBytecodeVisitor(VmMethod method, CompiledMethod cm, NativeStream os,
                                                            int level, boolean isBootstrap) {
        return null;
    }

    @Override
    public int getMagic() {
        return L2_COMPILER_MAGIC;
    }

    @Override
    public String getName() {
        return "X86-L2";
    }

    @Override
    public GCMapIterator createGCMapIterator() {
        return null;
    }

    @Override
    public String[] getCompilerPackages() {
        return new String[]{
            "org.jnode.vm.compiler.ir",
            "org.jnode.vm.compiler.ir.quad",
            "org.jnode.vm.x86.compiler",
            "org.jnode.vm.x86.compiler.l2"
        };
    }

    @Override
    protected CompiledMethod doCompile(VmMethod method, NativeStream os, int level, boolean isBootstrap) {
        final CompiledMethod cm = new CompiledMethod(level);
        try {
            if (method.isNative()) {
                Object label = new Label(method.getMangledName());
                cm.setCodeStart(os.getObjectRef(label));
            } else {
                EntryPoints entryPoints = getEntryPoints();
                X86CompilerHelper helper = new X86CompilerHelper((X86Assembler) os, null, entryPoints, isBootstrap);
                helper.setMethod(method);
                X86StackFrame stackFrame = new X86StackFrame((X86Assembler) os, helper, method, entryPoints, cm);
                TypeSizeInfo typeSizeInfo = getTypeSizeInfo();

                VmByteCode bytecode = method.getBytecode();
                IRControlFlowGraph cfg = new IRControlFlowGraph(bytecode);
                IRGenerator irg = new IRGenerator(cfg, typeSizeInfo, method.getDeclaringClass().getLoader());
                BytecodeParser.parse(bytecode, irg);

                initMethodArguments(method, stackFrame, typeSizeInfo, irg);

                cfg.constructSSA();
                cfg.optimize();
                cfg.removeUnusedVars();
                cfg.deconstrucSSA();
                cfg.removeDefUseChains();
                cfg.fixupAddresses();

                X86CodeGenerator x86cg = new X86CodeGenerator(method, (X86Assembler) os, bytecode.getLength(),
                    typeSizeInfo, stackFrame);
                List<Variable<?>> liveVariables = cfg.computeLiveVariables();
                LiveRange<?>[] liveRanges = getLiveRanges(liveVariables);
                LinearScanAllocator<?> lsa = allocate(liveRanges);
                generateCode(x86cg, cfg, irg, lsa);

//                Unsafe.debug("L2 compiled method: " + method.getFullName() + "\n");
            }
        } catch (RuntimeException x) {
            System.err.println("ERROR in compilation of " + method.getFullName());
            throw x;
        } catch (Error x) {
            System.err.println("ERROR in compilation of " + method.getFullName());
            throw x;
        }

        return cm;
    }

    public static void initMethodArguments(VmMethod method, X86StackFrame stackFrame, TypeSizeInfo typeSizeInfo,
                                     IRGenerator irg) {
        int nArgs = method.getArgSlotCount();
        Variable[] variables = irg.getVariables();
        for (int i = 0; i < nArgs; i += 1) {
            variables[i].setLocation(new StackLocation(stackFrame.getEbpOffset(typeSizeInfo, i)));
        }
    }
}
