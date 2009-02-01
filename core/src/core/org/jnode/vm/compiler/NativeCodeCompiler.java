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
 
package org.jnode.vm.compiler;

import java.io.Writer;

import org.jnode.assembler.Label;
import org.jnode.assembler.NativeStream;
import org.jnode.assembler.ObjectResolver;
import org.jnode.assembler.UnresolvedObjectRefException;
import org.jnode.vm.Vm;
import org.jnode.vm.VmAddress;
import org.jnode.vm.VmMagic;
import org.jnode.vm.VmSystemObject;
import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.bytecode.BasicBlock;
import org.jnode.vm.bytecode.ControlFlowGraph;
import org.jnode.vm.classmgr.VmAddressMap;
import org.jnode.vm.classmgr.VmByteCode;
import org.jnode.vm.classmgr.VmClassLoader;
import org.jnode.vm.classmgr.VmCompiledExceptionHandler;
import org.jnode.vm.classmgr.VmConstClass;
import org.jnode.vm.classmgr.VmMethod;
import org.vmmagic.unboxed.Address;

/**
 * Abstract native code compiler.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@MagicPermission
public abstract class NativeCodeCompiler extends VmSystemObject {

    /**
     * Compile the given method during bootstrapping
     *
     * @param method
     * @param os
     * @param level  Optimization level
     */
    public final void compileBootstrap(VmMethod method, NativeStream os,
                                       int level) {
        int start = os.getLength();
        final CompiledMethod cm;
        final boolean abstractM = method.isAbstract();
        if (abstractM) {
            if (method.isStatic()) {
                throw new Error("Abstract & static");
            }
            if (method.isNative()) {
                throw new Error("Abstract & native");
            }
            cm = doCompileAbstract(method, os, level, true);
            if (cm == null) {
                return;
            }
        } else {
            cm = doCompile(method, os, level, true);
        }
        int end = os.getLength();

        final VmAddress nativeCode = (VmAddress) cm.getCodeStart().getObject();
        final VmCompiledExceptionHandler[] eTable;
        final VmAddress defExHandler;
        final VmByteCode bc;
        final VmAddressMap aTable = cm.getAddressTable();

        if (!(method.isNative() || abstractM)) {
            final NativeStream.ObjectRef defExHRef = cm
                .getDefExceptionHandler();
            if (defExHRef != null) {
                defExHandler = (VmAddress) defExHRef.getObject();
            } else {
                defExHandler = null;
            }
            bc = method.getBytecode();
            final CompiledExceptionHandler[] ceh = cm.getExceptionHandlers();
            if (ceh != null) {
                eTable = new VmCompiledExceptionHandler[ceh.length];
                for (int i = 0; i < ceh.length; i++) {

                    final VmConstClass catchType = bc.getExceptionHandler(i)
                        .getCatchType();
                    final VmAddress startPtr = (VmAddress) ceh[i].getStartPc()
                        .getObject();
                    final VmAddress endPtr = (VmAddress) ceh[i].getEndPc()
                        .getObject();
                    final VmAddress handler = (VmAddress) ceh[i].getHandler()
                        .getObject();
                    eTable[i] = new VmCompiledExceptionHandler(catchType,
                        startPtr, endPtr, handler);
                }
            } else {
                eTable = null;
            }
        } else {
            eTable = null;
            defExHandler = null;
            bc = null;
        }

        method.addCompiledCode(Vm.getCompiledMethods().createCompiledCode(cm,
            method, this, bc, nativeCode, null, end - start, eTable,
            defExHandler, aTable), level);
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

        // long start = System.currentTimeMillis();

        // System.out.println("Compiling " + method);

        if (os == null) {
            os = createNativeStream(resolver);
        }
        final CompiledMethod cm;
        if (method.isAbstract()) {
            cm = doCompileAbstract(method, os, level, false);
            if (cm == null) {
                return;
            }
        } else {
            cm = doCompile(method, os, level, false);
        }

        try {
            final int startOffset = cm.getCodeStart().getOffset();
            final int size = cm.getCodeEnd().getOffset() - startOffset;
            final byte[] code = new byte[size];
            System.arraycopy(os.getBytes(), startOffset, code, 0, size);
            final Address codePtr = VmMagic.getArrayData(code);

            final NativeStream.ObjectRef defExHRef = cm
                .getDefExceptionHandler();
            final Address defExHandler;
            if (defExHRef != null) {
                defExHandler = codePtr.add(cm.getDefExceptionHandler()
                    .getOffset()
                    - startOffset);
            } else {
                defExHandler = Address.zero();
            }
            final VmCompiledExceptionHandler[] eTable;
            final VmAddressMap aTable = cm.getAddressTable();

            final VmByteCode bc = method.getBytecode();
            final CompiledExceptionHandler[] ceh = cm.getExceptionHandlers();
            if (ceh != null) {
                eTable = new VmCompiledExceptionHandler[ceh.length];
                for (int i = 0; i < ceh.length; i++) {

                    final VmConstClass catchType = bc.getExceptionHandler(i)
                        .getCatchType();
                    final Address startPtr = codePtr.add(ceh[i].getStartPc()
                        .getOffset()
                        - startOffset);
                    final Address endPtr = codePtr.add(ceh[i].getEndPc()
                        .getOffset()
                        - startOffset);
                    final Address handler = codePtr.add(ceh[i].getHandler()
                        .getOffset()
                        - startOffset);

                    eTable[i] = new VmCompiledExceptionHandler(catchType,
                        startPtr.toAddress(), endPtr.toAddress(), handler
                        .toAddress());
                }
            } else {
                eTable = null;
            }

            method.addCompiledCode(Vm.getCompiledMethods().createCompiledCode(
                cm, method, this, bc, codePtr.toAddress(), code, size,
                eTable, defExHandler.toAddress(), aTable), level);

            // For debugging only
            // System.out.println("Code: " + NumberUtils.hex(code));
            // System.out.println();
            // End of debugging only
        } catch (UnresolvedObjectRefException ex) {
            throw new CompileError(ex);
        }

        if (os.hasUnresolvedObjectRefs()) {
            throw new CompileError("Unresolved labels after compile!");
        }
    }

    public abstract void disassemble(VmMethod method, ObjectResolver resolver,
                                     int level, Writer writer);

    /**
     * Create a native stream for the current architecture.
     *
     * @param resolver
     * @return NativeStream
     */
    public abstract NativeStream createNativeStream(ObjectResolver resolver);

    /**
     * Compile the given method into the given stream.
     *
     * @param method
     * @param os
     * @param level       Optimization level
     * @param isBootstrap
     * @return The compiled method
     */
    protected CompiledMethod doCompile(VmMethod method, NativeStream os,
                                       int level, boolean isBootstrap) {
        final CompiledMethod cm = new CompiledMethod(level);
        if (method.isNative()) {
            Object label = new Label(method.getMangledName());
            cm.setCodeStart(os.getObjectRef(label));
        } else {
            // Create the visitor
            CompilerBytecodeVisitor bcv = createBytecodeVisitor(method,
                cm, os, level, isBootstrap);
            // Wrap in verifier if needed
            if (!(bcv instanceof VerifyingCompilerBytecodeVisitor)) {
                bcv = new VerifyingCompilerBytecodeVisitor<CompilerBytecodeVisitor>(bcv);
            }
            // Get the bytecode
            final VmByteCode bc = method.getBytecode();
            // Create the control flow graph
            ControlFlowGraph cfg = (ControlFlowGraph) bc.getCompilerData();
            if (cfg == null) {
                cfg = new ControlFlowGraph(bc);
                bc.setCompilerData(cfg);
            }
            // Compile the code 1 basic block at a time
            final CompilerBytecodeParser parser = new CompilerBytecodeParser(
                bc, cfg, bcv);
            bcv.startMethod(method);
            for (BasicBlock bb : cfg) {
                bcv.startBasicBlock(bb);
                parser.parse(bb.getStartPC(), bb.getEndPC(), false);
                bcv.endBasicBlock();
            }
            bcv.endMethod();

            //remove the compiler data to save memory, will be regenerated if needed
            bc.setCompilerData(null);
        }

        return cm;
    }

    /**
     * Compile the given abstract method into the given stream.
     *
     * @param method
     * @param os
     * @param level       Optimization level
     * @param isBootstrap
     * @return The compiled method
     */
    protected abstract CompiledMethod doCompileAbstract(VmMethod method,
                                                        NativeStream os, int level, boolean isBootstrap);

    /**
     * Create the visitor that converts bytecodes into native code.
     *
     * @param method
     * @param cm
     * @param os
     * @param level
     * @param isBootstrap
     * @return The new bytecode visitor.
     */
    protected abstract CompilerBytecodeVisitor createBytecodeVisitor(
        VmMethod method, CompiledMethod cm, NativeStream os, int level,
        boolean isBootstrap);

    /**
     * Initialize this compiler
     *
     * @param loader
     */
    public abstract void initialize(VmClassLoader loader);

    /**
     * Dump compiler statistics to System.out
     */
    public abstract void dumpStatistics();

    /**
     * Gets the magic value of this compiler.
     *
     * @return
     * @see org.jnode.vm.VmStackFrame#MAGIC_COMPILED
     * @see org.jnode.vm.VmStackFrame#MAGIC_INTERPRETED
     * @see org.jnode.vm.VmStackFrame#MAGIC_MASK
     */
    public abstract int getMagic();

    /**
     * Gets the name of this compiler.
     *
     * @return
     */
    public abstract String getName();

    /**
     * Create an iterator that can iterator of GCMaps generated by this compiler.
     *
     * @return
     */
    public abstract GCMapIterator createGCMapIterator();

    /**
     * Gets the names of the packages that are required by this compiler.
     *
     * @return
     */
    public abstract String[] getCompilerPackages();
}
