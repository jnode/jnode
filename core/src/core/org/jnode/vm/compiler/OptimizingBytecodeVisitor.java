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
 
package org.jnode.vm.compiler;

import org.jnode.util.Counter;
import org.jnode.vm.JvmType;
import org.jnode.vm.Vm;
import org.jnode.vm.bytecode.BasicBlock;
import org.jnode.vm.bytecode.ControlFlowGraph;
import org.jnode.vm.classmgr.VmByteCode;
import org.jnode.vm.classmgr.VmClassLoader;
import org.jnode.vm.classmgr.VmConstClass;
import org.jnode.vm.classmgr.VmConstMethodRef;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmPrimitiveClass;
import org.jnode.vm.classmgr.VmType;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class OptimizingBytecodeVisitor extends
    VerifyingCompilerBytecodeVisitor<InlineBytecodeVisitor> {

    /**
     * Maximum length of methods that will be inlined
     */
    private static final int SIZE_LIMIT = 32;

    /**
     * Maximum depth of recursive inlining
     */
    private static final int MAX_INLINE_DEPTH = -1; //5;

    /**
     * Common method entrypoints
     */
    private final EntryPoints entryPoints;

    /**
     * The classloader
     */
    private final VmClassLoader loader;

    /**
     * The method that is currently being visited
     */
    private VmMethod method;

    /**
     * The current max locals of method (adjusted for inlined methods)
     */
    private char maxLocals;

    /**
     * Diff to add to local indexes
     */
    private char localDelta;

    /**
     * Has a return been visited during an inline
     */
    private boolean visitedReturn = false;

    /**
     * How many nested inlines we're currently in (0 == no inline)
     */
    private byte inlineDepth = 0;

    // Inter instruction optimization flags

    /**
     * Optimization flag constants.
     */
    private static interface OptFlags {
        int ASTORE = 0x0001;

        int ISTORE = 0x0002;

        int LSTORE = 0x0004;

        int FSTORE = 0x0008;

        int DSTORE = 0x0010;

        int ALOAD = 0x0020;

        int ILOAD = 0x0040;

        int LLOAD = 0x0080;

        int FLOAD = 0x0100;

        int DLOAD = 0x0200;
    }

    /**
     * Optimize flags set by the current instruction
     */
    private int optimizeFlags;

    /**
     * Optimize flags set by the previous instruction
     */
    private int previousOptimizeFlags;

    /**
     * Index of the last xstore instruction
     */
    private int storeIndex;

    /**
     * Index of the last xload instruction
     */
    private int loadIndex;

    /**
     * Statistic counter for #inlined invokespecial's
     */
    private static Counter inlineSpecialCounter = Vm.getVm().getCounter(
        "inlined-invokespecial");

    /**
     * Statistic counter for #inlined invokespecial's
     */
    private static Counter inlineStaticCounter = Vm.getVm().getCounter(
        "inlined-invokestatic");

    /**
     * Statistic counter for #inlined invokespecial's
     */
    private static Counter inlineVirtualCounter = Vm.getVm().getCounter(
        "inlined-invokevirtual");

    /**
     * Statistic counter for astore/aload sequence
     */
    private static Counter storeLoadCounter = Vm.getVm().getCounter(
        "store-load");

    /**
     * Initialize this instance.
     *
     * @param delegate
     * @param loader
     */
    public OptimizingBytecodeVisitor(EntryPoints entryPoints,
                                     InlineBytecodeVisitor delegate, VmClassLoader loader) {
        super(delegate);
        this.entryPoints = entryPoints;
        this.loader = loader;
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#startMethod(org.jnode.vm.classmgr.VmMethod)
     */
    public void startMethod(VmMethod method) {
        this.method = method;
        this.maxLocals = (char) method.getBytecode().getNoLocals();
        // Reset optimization flags
        this.optimizeFlags = 0;
        this.previousOptimizeFlags = 0;
        // Delegate call
        super.startMethod(method);
    }

    /**
     * @see org.jnode.vm.compiler.DelegatingCompilerBytecodeVisitor#startBasicBlock(org.jnode.vm.bytecode.BasicBlock)
     */
    public void startBasicBlock(BasicBlock bb) {
        // Reset optimize flags
        this.previousOptimizeFlags = 0;
        this.optimizeFlags = 0;
        // Delegate call
        super.startBasicBlock(bb);
    }

    /**
     * @see org.jnode.vm.compiler.DelegatingCompilerBytecodeVisitor#startInstruction(int)
     */
    public void startInstruction(int address) {
        // Set previos optimize flags & clear optimize flags
        this.previousOptimizeFlags = this.optimizeFlags;
        this.optimizeFlags = 0;
        // Delegate call
        super.startInstruction(address);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_invokespecial(org.jnode.vm.classmgr.VmConstMethodRef)
     */
    public void visit_invokespecial(VmConstMethodRef methodRef) {
        methodRef.resolve(loader);
        final VmMethod im = methodRef.getResolvedVmMethod();
        if (!canInline(im)) {
            // Do not inline this call
            super.visit_invokespecial(methodRef);
        } else {
            verifyInvoke(methodRef);
            inlineSpecialCounter.inc();
            inline(im);
        }
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_invokestatic(org.jnode.vm.classmgr.VmConstMethodRef)
     */
    public void visit_invokestatic(VmConstMethodRef methodRef) {
        methodRef.resolve(loader);
        final VmMethod im = methodRef.getResolvedVmMethod();
        if (!canInline(im)) {
            // Do not inline this call
            super.visit_invokestatic(methodRef);
        } else {
            verifyInvoke(methodRef);
            inlineStaticCounter.inc();
            inline(im);
        }
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_invokevirtual(org.jnode.vm.classmgr.VmConstMethodRef)
     */
    public void visit_invokevirtual(VmConstMethodRef methodRef) {
        methodRef.resolve(loader);
        final VmMethod im = methodRef.getResolvedVmMethod();
        if (!canInline(im)) {
            // Do not inline this call
            super.visit_invokevirtual(methodRef);
        } else {
            verifyInvoke(methodRef);
            inlineVirtualCounter.inc();
            inline(im);
        }
    }

    /**
     * Inline the given method into the current method
     *
     * @param im
     */
    private void inline(VmMethod im) {
        // Save some variables
        final char oldLocalDelta = this.localDelta;
        final boolean oldVisitedReturn = this.visitedReturn;
        final VmMethod oldMethod = this.method;
        final InlineBytecodeVisitor ibv = getDelegate();
        final VmByteCode bc = im.getBytecode();

        // Calculate the new maxLocals
        final int imLocals = bc.getNoLocals(); // #Locals of the inlined method
        final int curLocals = oldMethod.getBytecode().getNoLocals(); // #Locals
        // of
        // the
        // current
        // method
        maxLocals = (char) Math.max(maxLocals, oldLocalDelta + curLocals
            + imLocals);

        // Set new variables
        this.localDelta += curLocals;
        this.visitedReturn = false;
        this.inlineDepth++;
        this.method = im;

        // Reset optimization flags
        this.optimizeFlags = 0;
        this.previousOptimizeFlags = 0;

        // Start the inlining
        ibv.startInlinedMethodHeader(im, maxLocals);

        // Store the arguments in the locals of the inlined method
        storeArgumentsToLocals(im, ibv, localDelta);

        // Start the inlining
        ibv.startInlinedMethodCode(im, maxLocals);

        // Emit a NOP so we can differentiate when a method is virtually empty
        if (inlineDepth > 1) {
            ibv.visit_nop();
        }

        // Create the control flow graph
        ControlFlowGraph cfg = (ControlFlowGraph) bc.getCompilerData();
        if (cfg == null) {
            cfg = new ControlFlowGraph(bc);
            bc.setCompilerData(cfg);
        }
        // Compile the code 1 basic block at a time
        final CompilerBytecodeParser parser = new CompilerBytecodeParser(bc,
            cfg, this);
        for (BasicBlock bb : cfg) {
            this.startBasicBlock(bb);
            parser.parse(bb.getStartPC(), bb.getEndPC(), false);
            this.endBasicBlock();
        }

        if (!this.isReturnVisited()) {
            // Generate a dummy return to avoid breaking the compilers
            createDummyReturn(im, this);
        }

        // End the inlining
        ibv.endInlinedMethod(oldMethod);

        // Restore variables
        this.localDelta = oldLocalDelta;
        this.visitedReturn = oldVisitedReturn;
        this.inlineDepth--;
        this.method = oldMethod;

        // Reset optimization flags
        this.optimizeFlags = 0;
        this.previousOptimizeFlags = 0;
    }

    private void createDummyReturn(VmMethod im, CompilerBytecodeVisitor bcv) {
        if (im.isReturnVoid()) {
            bcv.visit_return();
        } else if (im.isReturnObject()) {
            bcv.visit_aconst_null();
            bcv.visit_areturn();
        } else if (im.isReturnWide()) {
            if (im.getReturnType().getJvmType() == JvmType.DOUBLE) {
                // double
                bcv.visit_dconst(0.0);
                bcv.visit_dreturn();
            } else {
                // long
                bcv.visit_lconst(0);
                bcv.visit_lreturn();
            }
        } else {
            if (im.getReturnType().getJvmType() == JvmType.FLOAT) {
                // float
                bcv.visit_fconst(0.0f);
                bcv.visit_freturn();
            } else {
                // int
                bcv.visit_iconst(0);
                bcv.visit_ireturn();
            }
        }
    }

    /**
     * Pop the method arguments of the stack and store them in locals.
     *
     * @param im
     * @param ibv
     * @param localDelta
     */
    private void storeArgumentsToLocals(VmMethod im, InlineBytecodeVisitor ibv,
                                        int localDelta) {
        final int cnt = im.getNoArguments();
        int local = localDelta + im.getArgSlotCount() - 1;
        /*
         * if (im.isStatic()) { local--; }
         */

        for (int i = cnt - 1; i >= 0; i--) {
            final VmType<?> argType = im.getArgumentType(i);
            // System.out.println("arg" + i + ": " + argType);

            if (argType.isPrimitive()) {
                final VmPrimitiveClass<?> pc = (VmPrimitiveClass<?>) argType;
                if (pc.isWide()) {
                    local--;
                    if (pc.isFloatingPoint()) {
                        // double
                        ibv.visit_dstore(local);
                    } else {
                        // long
                        ibv.visit_lstore(local);
                    }
                } else {
                    if (pc.isFloatingPoint()) {
                        // float
                        ibv.visit_fstore(local);
                    } else {
                        // int
                        ibv.visit_istore(local);
                    }
                }
            } else {
                ibv.visit_astore(local);
            }
            local--;
        }

        if (!im.isStatic()) {
            // TODO add nullpointer check

            // Store this pointer.
            ibv.visit_astore(local);
        }
    }

    /**
     * Can the given method be inlined?
     *
     * @param method
     * @return
     */
    private boolean canInline(VmMethod method) {

        // First determine if we CAN inline
        if (method.isNative() || method.isAbstract() || method.isSynchronized()) {
            return false;
        }
        if (!(method.isFinal() || method.isPrivate() || method.isStatic() || method
            .getDeclaringClass().isFinal())) {
            return false;
        }
        final VmType<?> declClass = method.getDeclaringClass();
        if (declClass.isMagicType()) {
            return false;
        }
        if (!declClass.isAlwaysInitialized()) {
            return false;
        }
        final VmByteCode bc = method.getBytecode();
        if (bc == null) {
            return false;
        }
        if (method.hasNoInlinePragma()) {
            return false;
        }
        if (bc.getNoExceptionHandlers() > 0) {
            return false;
        }

        // Now determine if we SHOULD inline
        if (!method.hasInlinePragma()) {
            if (inlineDepth >= MAX_INLINE_DEPTH) {
                return false;
            }
            if (bc.getLength() > SIZE_LIMIT) {
                return false;
            }
        }
        return true;
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_aload(int)
     */
    public void visit_aload(int index) {
        index += localDelta;
        if (((previousOptimizeFlags & OptFlags.ASTORE) != 0)
            && (storeIndex == index)) {
            storeLoadCounter.inc();
            super.visit_aloadStored(index);
        } else if (((previousOptimizeFlags & OptFlags.ALOAD) != 0)
            && (loadIndex == index)) {
            super.visit_dup();
        } else {
            super.visit_aload(index);
        }
        loadIndex = index;
        optimizeFlags |= OptFlags.ALOAD;
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_areturn()
     */
    public void visit_areturn() {
        if (inlineDepth == 0) {
            super.visit_areturn();
        } else {
            visitedReturn = true;
            getDelegate().visit_inlinedReturn(JvmType.REFERENCE);
        }
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_astore(int)
     */
    public void visit_astore(int index) {
        index += localDelta;
        this.optimizeFlags |= OptFlags.ASTORE;
        this.storeIndex = index;
        super.visit_astore(index);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dload(int)
     */
    public void visit_dload(int index) {
        index += localDelta;
        if (((previousOptimizeFlags & OptFlags.DSTORE) != 0)
            && (storeIndex == index)) {
            storeLoadCounter.inc();
            super.visit_dloadStored(index);
        } else if (((previousOptimizeFlags & OptFlags.DLOAD) != 0)
            && (loadIndex == index)) {
            super.visit_dup2();
        } else {
            super.visit_dload(index);
        }
        loadIndex = index;
        optimizeFlags |= OptFlags.DLOAD;
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dreturn()
     */
    public void visit_dreturn() {
        if (inlineDepth == 0) {
            super.visit_dreturn();
        } else {
            visitedReturn = true;
            getDelegate().visit_inlinedReturn(JvmType.DOUBLE);
        }
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dstore(int)
     */
    public void visit_dstore(int index) {
        index += localDelta;
        this.optimizeFlags |= OptFlags.DSTORE;
        this.storeIndex = index;
        super.visit_dstore(index);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fload(int)
     */
    public void visit_fload(int index) {
        index += localDelta;
        if (((previousOptimizeFlags & OptFlags.FSTORE) != 0)
            && (storeIndex == index)) {
            storeLoadCounter.inc();
            super.visit_floadStored(index);
        } else if (((previousOptimizeFlags & OptFlags.FLOAD) != 0)
            && (loadIndex == index)) {
            super.visit_dup();
        } else {
            super.visit_fload(index);
        }
        loadIndex = index;
        optimizeFlags |= OptFlags.FLOAD;
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_freturn()
     */
    public void visit_freturn() {
        if (inlineDepth == 0) {
            super.visit_freturn();
        } else {
            visitedReturn = true;
            getDelegate().visit_inlinedReturn(JvmType.FLOAT);
        }
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fstore(int)
     */
    public void visit_fstore(int index) {
        index += localDelta;
        this.optimizeFlags |= OptFlags.FSTORE;
        this.storeIndex = index;
        super.visit_fstore(index);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iinc(int, int)
     */
    public void visit_iinc(int index, int incValue) {
        super.visit_iinc(index + localDelta, incValue);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iload(int)
     */
    public void visit_iload(int index) {
        index += localDelta;
        if (((previousOptimizeFlags & OptFlags.ISTORE) != 0)
            && (storeIndex == index)) {
            storeLoadCounter.inc();
            super.visit_iloadStored(index);
        } else if (((previousOptimizeFlags & OptFlags.ILOAD) != 0)
            && (loadIndex == index)) {
            super.visit_dup();
        } else {
            super.visit_iload(index);
        }
        loadIndex = index;
        optimizeFlags |= OptFlags.ILOAD;
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ireturn()
     */
    public void visit_ireturn() {
        if (inlineDepth == 0) {
            super.visit_ireturn();
        } else {
            visitedReturn = true;
            getDelegate().visit_inlinedReturn(JvmType.INT);
        }
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_istore(int)
     */
    public void visit_istore(int index) {
        index += localDelta;
        this.optimizeFlags |= OptFlags.ISTORE;
        this.storeIndex = index;
        super.visit_istore(index);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lload(int)
     */
    public void visit_lload(int index) {
        index += localDelta;
        if (((previousOptimizeFlags & OptFlags.LSTORE) != 0)
            && (storeIndex == index)) {
            storeLoadCounter.inc();
            super.visit_lloadStored(index);
        } else if (((previousOptimizeFlags & OptFlags.LLOAD) != 0)
            && (loadIndex == index)) {
            super.visit_dup2();
        } else {
            super.visit_lload(index);
        }
        loadIndex = index;
        optimizeFlags |= OptFlags.LLOAD;
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lreturn()
     */
    public void visit_lreturn() {
        if (inlineDepth == 0) {
            super.visit_lreturn();
        } else {
            visitedReturn = true;
            getDelegate().visit_inlinedReturn(JvmType.LONG);
        }
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lstore(int)
     */
    public void visit_lstore(int index) {
        index += localDelta;
        this.optimizeFlags |= OptFlags.LSTORE;
        this.storeIndex = index;
        super.visit_lstore(index);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ret(int)
     */
    public void visit_ret(int index) {
        super.visit_ret(index + localDelta);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_return()
     */
    public void visit_return() {
        if (inlineDepth == 0) {
            super.visit_return();
        } else {
            visitedReturn = true;
            getDelegate().visit_inlinedReturn(JvmType.VOID);
        }
    }

    /**
     * Have we visited a return statement?
     *
     * @return
     */
    public boolean isReturnVisited() {
        return visitedReturn;
    }

    /**
     * @see org.jnode.vm.compiler.DelegatingCompilerBytecodeVisitor#visit_monitorenter()
     */
    public void visit_monitorenter() {
        verifyMonitor();
        inline(entryPoints.getMonitorEnterMethod());
    }

    /**
     * @see org.jnode.vm.compiler.DelegatingCompilerBytecodeVisitor#visit_monitorexit()
     */
    public void visit_monitorexit() {
        verifyMonitor();
        inline(entryPoints.getMonitorExitMethod());
    }

    /**
     * @see org.jnode.vm.compiler.DelegatingCompilerBytecodeVisitor#visit_new(org.jnode.vm.classmgr.VmConstClass)
     */
    public void visit_new(VmConstClass clazz) {
        if (false) {
            // Inline call to {@link SoftByteCodes#allocObject}
            clazz.resolve(loader);

            /* Setup a call to SoftByteCodes.allocObject */
            visit_ldc(clazz.getResolvedVmClass()); // vmClass
            visit_iconst(-1); // size
            inline(entryPoints.getAllocObjectMethod());
        } else {
            super.visit_new(clazz);
        }
    }
}
