/*
 * $Id$
 *
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
 
package org.jnode.test.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import org.jnode.assembler.ObjectResolver;
import org.jnode.assembler.x86.X86TextAssembler;
import org.jnode.vm.Vm;
import org.jnode.vm.VmArchitecture;
import org.jnode.vm.VmSystemClassLoader;
import org.jnode.vm.bytecode.BytecodeParser;
import org.jnode.vm.bytecode.BytecodeViewer;
import org.jnode.vm.bytecode.ControlFlowGraph;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.compiler.NativeCodeCompiler;
import org.jnode.vm.x86.VmX86Architecture;
import org.jnode.vm.x86.VmX86Architecture32;
import org.jnode.vm.x86.VmX86Architecture64;
import org.jnode.vm.x86.X86CpuID;
import org.jnode.vm.x86.compiler.l1a.X86Level1ACompiler;

/**
 * @author epr
 */
public class CompilerTest {

    public static final String[] clsNames = {
//            "java.util.zip.ZipFile$PartialInputStream",
//            "java.security.SecureRandom",
//            "java.lang.Boolean",
//            "java.lang.Integer",
//            "java.lang.String",
        //"java.lang.StrictMath",
//            "java.lang.VMDouble",
//            "java.io.StringBufferInputStream",
//            "java.net.MimeTypeMapper",
//            "gnu.java.io.ObjectIdentityWrapper",
//            "gnu.java.io.encode.EncoderEightBitLookup",
//            "java.io.ObjectInputStream$3",
//            "java.io.PrintWriter",
//            "java.nio.ByteOrder",
//            "org.jnode.assembler.x86.X86BinaryAssembler",
//            "org.jnode.assembler.x86.X86BinaryAssembler$X86ObjectInfo",
//            "org.jnode.fs.ext2.Ext2FileSystem",
//            "org.jnode.vm.Unsafe$UnsafeObjectResolver",
//            "org.jnode.vm.MemoryBlockManager",
//            "org.jnode.vm.SoftByteCodes",
//            "org.jnode.vm.VmSystem",
//      "org.jnode.vm.VmStacReader",
        "org.jnode.vm.classmgr.VmType",
//            "org.jnode.test.ArrayLongTest",
//            "org.jnode.test.Linpack",
//            "org.jnode.test.MultiANewArrayTest",
//            "org.jnode.test.Sieve",
//            "org.jnode.test.PrimitiveIntTest",
//            "org.jnode.test.PrimitiveLongTest",
//            "org.jnode.test.PrimitiveFloatTest",
//            "org.jnode.test.PrimitiveDoubleTest", "org.jnode.test.InvokeTest",
//            "org.jnode.test.InvokeInterfaceTest",
//            "org.jnode.test.InvokeStaticTest",
//            "org.jnode.plugin.model.PluginJar",
//            "org.jnode.test.ArithOpt",
//            "org.jnode.test.InlineTestClass",
//            "org.jnode.test.CastTest",
//            "org.jnode.test.InstanceOfTest",
        //"org.jnode.test.ConvertTest",
//            "org.jnode.vm.MonitorManager",
//            "org.jnode.vm.memmgr.def.VmBootHeap",
//            "org.jnode.vm.classmgr.VmCP",
//            "java.util.zip.ZipInputStream",
//            "org.jnode.test.MagicWordTest",
//        "gnu.java.awt.color.SrgbConverter",
//           "gnu.classpath.SystemProperties",
//            "org.jnode.test.ArrayTest",
//        "org.jnode.test.IfNullTest",
//        "org.jnode.vm.bytecode.BytecodeParser",
    };

    public static void main(String[] args) throws Exception {

        final String processorId = System.getProperty("cpu", "p5");
        final String dir = System.getProperty("classes.dir", ".");
        final String archName = System.getProperty("arch", "x86");

        final VmX86Architecture arch;
        if (archName.equals("x86_64")) {
            arch = new VmX86Architecture64();
        } else {
            arch = new VmX86Architecture32();
        }
        final VmSystemClassLoader cl = new VmSystemClassLoader(new File(dir)
            .toURL(), arch);
        VmType.initializeForBootImage(cl);
        final Vm vm = new Vm("?", arch, cl.getSharedStatics(), false, cl, null);
        vm.toString();
        System.out.println("Architecture: " + arch.getFullName());

        //final ObjectResolver resolver = new DummyResolver();
        final X86CpuID cpuId = X86CpuID.createID(processorId);
        //NativeCodeCompiler c = cs[0];
        final NativeCodeCompiler[] cs = {//new X86Level1Compiler(),
            new X86Level1ACompiler()};
        for (int i = 0; i < cs.length; i++) {
            cs[i].initialize(cl);
        }
        long times[] = new long[cs.length];
        int counts[] = new int[cs.length];

        for (int ci = 0; ci < cs.length; ci++) {
            final long start = System.currentTimeMillis();
            for (int k = 0; k < clsNames.length; k++) {
                final String clsName = clsNames[k];
                System.out.println("Compiling " + clsName);
                final VmType type = cl.loadClass(clsName, true);
                final int cnt = type.getNoDeclaredMethods();
                for (int i = 0; i < cnt; i++) {
                    final VmMethod method = type.getDeclaredMethod(i);
                    counts[ci]++;
                    compile(method, arch, cs[ci], cpuId, ci + 1);
                }
            }
            final long end = System.currentTimeMillis();
            times[ci] += end - start;
        }
        for (int ci = 0; ci < cs.length; ci++) {
            System.out.println("Compiled " + counts[ci] + " methods using "
                + cs[ci].getName() + " in " + times[ci] + "ms");
        }
    }

    static void compile(VmMethod method, VmArchitecture arch, NativeCodeCompiler c, X86CpuID cpuId,
                        int level) throws IOException {
        final String cname = method.getDeclaringClass().getName();
        final String mname = method.getName();
        final String fname = cname + "#"
            + mname.replace('<', '_').replace('>', '_') + "." + c.getName()
            + ".method";
        final FileOutputStream out = new FileOutputStream(fname);

        try {
            if (!method.isAbstract()) {
                final PrintStream ps = new PrintStream(out);
                BytecodeViewer viewer = new BytecodeViewer(
                    new ControlFlowGraph(method.getBytecode()), ps);
                BytecodeParser.parse(method.getBytecode(), viewer);
                ps.flush();
            }

            X86TextAssembler os = new X86TextAssembler(new OutputStreamWriter(out),
                cpuId, ((VmX86Architecture) arch).getMode(), method.getMangledName());
            try {
                c.compileBootstrap(method, os, level);
                //c.compileRuntime(method, resolver, level, os);
            } finally {
                os.flush();
            }
        } catch (Throwable ex) {
            System.out.println("Error in ### Method " + mname + " -> " + fname);
            ex.printStackTrace();
            System.exit(1);
        } finally {
            out.close();
        }
    }

    static class DummyResolver extends ObjectResolver {

        /**
         * @see org.jnode.assembler.ObjectResolver#addressOf32(java.lang.Object)
         */
        public int addressOf32(Object object) {
            return 0;
        }

        /**
         * @see org.jnode.assembler.ObjectResolver#addressOf64(java.lang.Object)
         */
        public long addressOf64(Object object) {
            return 0L;
        }
    }
}
