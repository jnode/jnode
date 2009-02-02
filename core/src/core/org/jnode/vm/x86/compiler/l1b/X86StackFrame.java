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
 
package org.jnode.vm.x86.compiler.l1b;

import org.jnode.assembler.Label;
import org.jnode.assembler.NativeStream.ObjectRef;
import org.jnode.assembler.x86.X86Assembler;
import org.jnode.assembler.x86.X86BinaryAssembler;
import org.jnode.assembler.x86.X86Constants;
import org.jnode.assembler.x86.X86Register;
import org.jnode.assembler.x86.X86Register.GPR;
import org.jnode.vm.classmgr.TypeSizeInfo;
import org.jnode.vm.classmgr.VmByteCode;
import org.jnode.vm.classmgr.VmInterpretedExceptionHandler;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmMethodCode;
import org.jnode.vm.compiler.CompiledExceptionHandler;
import org.jnode.vm.compiler.CompiledMethod;
import org.jnode.vm.compiler.EntryPoints;
import org.jnode.vm.x86.compiler.X86CompilerConstants;
import org.jnode.vm.x86.compiler.X86CompilerHelper;
import org.jnode.vm.x86.compiler.X86JumpTable;

/**
 * Utility class for generating the X86 method stack frame
 *
 * @author epr
 */
final class X86StackFrame implements X86CompilerConstants {

    private final VmMethod method;

    private final X86Assembler os;

    private final X86CompilerHelper helper;

    private final EntryPoints entryPoints;

    private final CompiledMethod cm;

    private final VmByteCode bc;

    /**
     * Label of the footer
     */
    private final Label footerLabel;

    /**
     * Label at start of method init code
     */
    private final Label initLabel;

    /**
     * Label at start of actual method code
     */
    private final Label startCodeLabel;

    private X86BinaryAssembler.ObjectInfo codeObject;

    /**
     * Offset from EBP/RBP to the save EBP/RBP
     */
    private final int EbpFrameRefOffset;

    /**
     * Size of an address
     */
    private final int slotSize;

    /**
     * Number of byte on the stack occupied by saved registers. See
     * {@link #saveRegisters()}
     */
    private static final int SAVED_REGISTERSPACE = 0 * 4;

    /**
     * Create a new instance
     *
     * @param os
     * @param method
     * @param context
     * @param cm
     */
    public X86StackFrame(X86Assembler os, X86CompilerHelper helper,
                         VmMethod method, EntryPoints context, CompiledMethod cm) {
        this.os = os;
        this.helper = helper;
        this.method = method;
        this.entryPoints = context;
        this.cm = cm;
        this.bc = method.getBytecode();
        this.initLabel = helper.genLabel("$$init");
        this.startCodeLabel = helper.genLabel("$$code");
        this.footerLabel = helper.genLabel("$$footer");
        this.slotSize = os.isCode32() ? 4 : 8;
        this.EbpFrameRefOffset = 1 * slotSize;
    }

    /**
     * Emit code to create the stack frame
     *
     * @return The length of os at the start of the method code.
     */
    public int emitHeader() {

        final VmMethodCode code = new VmMethodCode();
        final Label startLabel = helper.genLabel("$$start");
        codeObject = os.startObject(entryPoints.getVmMethodCodeClass());
        os.setObjectRef(code);
        cm.setCodeStart(os.setObjectRef(startLabel));
        final int rc = os.getLength();

        if (false) {
            // Debug only
            os.writeBreakPoint();
        }

        // Jump to init code
        os.writeJMP(initLabel);
        // Set startCode label
        os.setObjectRef(startCodeLabel);

        return rc;
    }

    /**
     * Write code to test the alignment of the stack pointer.
     */
    public void writeStackAlignmentTest(Label curInstrLabel) {
        if (false && os.isCode64()) {
            final int idx = os.getLength();
            final Label test = new Label(curInstrLabel + "$$stackAlignTest" + idx);
            final Label failed = new Label(curInstrLabel + "$$stackAlign" + idx);
            os.writeJMP(test);
            os.setObjectRef(failed);
            os.writeINT(0x41);
            os.setObjectRef(test);
            os.writeTEST(X86Register.RSP, 7);
            os.writeJCC(failed, X86Constants.JNZ);
        }
    }

    /**
     * Emit code to end the stack frame
     */
    public void emitTrailer(TypeSizeInfo typeSizeInfo, int maxLocals) {
        final int argSlotCount = method.getArgSlotCount();
        final Label stackOverflowLabel = helper.genLabel("$$stack-overflow");
        final GPR asp = helper.SP;
        final GPR abp = helper.BP;
        final GPR aax = helper.AAX;
        final int size = helper.ADDRSIZE;

        // Begin footer
        // Now start the actual footer
        os.setObjectRef(footerLabel);

        /* Go restore the previous current frame */
        emitSynchronizationCode(typeSizeInfo, entryPoints.getMonitorExitMethod());
        os.writeLEA(asp, abp, EbpFrameRefOffset);
        os.writePOP(abp);
        restoreRegisters();
        // Return
        if (argSlotCount > 0) {
            os.writeRET(argSlotCount * slotSize);
        } else {
            os.writeRET();
        }
        // End footer

        // Begin header
        // Init starts here
        os.setObjectRef(initLabel);

        // Test stack overflow        
        final int stackEndOffset = entryPoints.getVmProcessorStackEnd().getOffset();
        if (os.isCode32()) {
            os.writePrefix(X86Constants.FS_PREFIX);
            os.writeCMP_MEM(X86Register.ESP, stackEndOffset);
        } else {
            os.writeCMP(X86Register.RSP, PROCESSOR64, stackEndOffset);
        }
        // This forward jump is not predicted by branch prediction.
        // Which is good, because we do not predict a stack overflow
        os.writeJCC(stackOverflowLabel, X86Constants.JLE);

        // Load the statics table reference
        if (method.hasLoadStaticsPragma()) {
            helper.writeLoadSTATICS(helper.genLabel("$$edi"), "init", false);
        }

        // Test stack alignment
        writeStackAlignmentTest(helper.genLabel("$$stackAlignment"));

        // Create class initialization code (if needed)
        helper.writeClassInitialize(method);

        // Increment the invocation count
        //helper.writeIncInvocationCount(aax); (NOT USED for now, aax is also invalid now)

        // Fixed framelayout
        saveRegisters();
        os.writePUSH(abp);
        os.writePUSH(cm.getCompiledCodeId());
        os.writeMOV(size, abp, asp);

        // Emit the code to create the locals
        final int noLocalVars = maxLocals - argSlotCount;
        // Create and clear all local variables
        if (noLocalVars > 0) {
            os.writeXOR(aax, aax);
            for (int i = 0; i < noLocalVars; i++) {
                os.writePUSH(aax);
            }
        }

        // Create the synchronization enter code
        emitSynchronizationCode(typeSizeInfo, entryPoints.getMonitorEnterMethod());

        // And jump back to the actual code start
        os.writeJMP(startCodeLabel);

        // Write stack overflow code
        os.setObjectRef(stackOverflowLabel);
        os.writeINT(0x31);

        // Write class initializers
        helper.writeClassInitializers();

        // End header       

        // No set the exception start&endPtr's
        //final int noLocals = bc.getNoLocals();
        //final int noLocalVars = noLocals - noArgs;
        final int count = bc.getNoExceptionHandlers();
        CompiledExceptionHandler[] ceh = new CompiledExceptionHandler[count];
        for (int i = 0; i < count; i++) {
            final VmInterpretedExceptionHandler eh = bc.getExceptionHandler(i);
            final Label handlerLabel = helper.genLabel("$$ex-handler" + i);

            final ObjectRef handlerRef = os.setObjectRef(handlerLabel);

            /** Clear the calculation stack (only locals are left) */
            if (noLocalVars < 0) {
                System.out.println("@#@#@#@# noLocalVars = " + noLocalVars);
            }
            final int ofs = Math.max(0, noLocalVars) * slotSize;
            os.writeLEA(asp, abp, -ofs);
            /** Push the exception in EAX */
            os.writePUSH(aax);
            /** Goto the real handler */
            os.writeJMP(helper.getInstrLabel(eh.getHandlerPC()));

            ceh[i] = new CompiledExceptionHandler();
            ceh[i].setStartPc(os.getObjectRef(helper.getInstrLabel(eh
                .getStartPC())));
            ceh[i].setEndPc(os
                .getObjectRef(helper.getInstrLabel(eh.getEndPC())));
            ceh[i].setHandler(handlerRef);

        }
        cm.setExceptionHandlers(ceh);

        // Now create the default exception handler
        Label handlerLabel = helper.genLabel("$$def-ex-handler");
        cm.setDefExceptionHandler(os.setObjectRef(handlerLabel));
        emitSynchronizationCode(typeSizeInfo, entryPoints.getMonitorExitMethod());
        os.writeLEA(asp, abp, EbpFrameRefOffset);
        os.writePOP(abp);
        restoreRegisters();
        /**
         * Do not do a ret here, this way the return address will be used by
         * vm_athrow as its return address
         */
        helper.writeJumpTableJMP(X86JumpTable.VM_ATHROW_NOTRACE_IDX);
        //os.writeJMP(helper.VM_ATHROW_NOTRACE);

        codeObject.markEnd();
        cm.setCodeEnd(os.setObjectRef(helper.genLabel("$$end-code-object")));
    }

    /**
     * Emit a jump to the exit code
     */
    public void emitReturn() {
        os.writeJMP(footerLabel);
    }

    /**
     * Gets the offset to EBP (current stack frame) for the local with the given
     * index.
     *
     * @param index
     * @return int
     */
    public final short getEbpOffset(TypeSizeInfo typeSizeInfo, int index) {
        final int noArgs = method.getArgSlotCount();
        final int stackSlot = index;
//        final int stackSlot = Signature.getStackSlotForJavaArgNumber(typeSizeInfo, method, index);
        if (stackSlot < noArgs) {
            // Index refers to a method argument
            return toShort(((noArgs - stackSlot + 1) * slotSize) + EbpFrameRefOffset
                + SAVED_REGISTERSPACE);
        } else {
            // Index refers to a local variable
            return toShort((stackSlot - noArgs + 1) * -slotSize);
        }
    }

    private final short toShort(int v) {
        if ((v >= Short.MIN_VALUE) && (v <= Short.MAX_VALUE)) {
            return (short) v;
        } else {
            throw new IllegalArgumentException("Given value does not fit in a short " + v);
        }
    }

    /**
     * Gets the offset to EBP (current stack frame) for the wide local with the
     * given index.
     *
     * @param index
     * @return int
     */
    public final short getWideEbpOffset(TypeSizeInfo typeSizeInfo, int index) {
        return getEbpOffset(typeSizeInfo, index + 1);
//        if (os.isCode32()) {
//            return getEbpOffset(typeSizeInfo, index + 1);
//        } else {
//            return getEbpOffset(typeSizeInfo, index);            
//        }
    }

    private void emitSynchronizationCode(TypeSizeInfo typeSizeInfo, VmMethod monitorMethod) {
        if (method.isSynchronized()) {
            final GPR aax = os.isCode32() ? (GPR) X86Register.EAX : X86Register.RAX;
            final GPR adx = os.isCode32() ? (GPR) X86Register.EDX : X86Register.RDX;

            os.writePUSH(aax);
            os.writePUSH(adx);
            //System.out.println("synchr. " + method);
            if (method.isStatic()) {
                // Get declaring class
                final int typeOfs = helper.getSharedStaticsOffset(method.getDeclaringClass());
                os.writePUSH(helper.STATICS, typeOfs);
            } else {
                os.writePUSH(helper.BP, getEbpOffset(typeSizeInfo, 0));
            }
            helper.invokeJavaMethod(monitorMethod);
            os.writePOP(adx);
            os.writePOP(aax);
        }
    }

    /**
     * Write code to save the callee saved registers.
     *
     * @see #SAVED_REGISTERSPACE
     * @see org.jnode.vm.x86.VmX86StackReader
     */
    private final void saveRegisters() {
        //os.writePUSH(Register.EBX);
        //os.writePUSH(Register.EDI);
        //os.writePUSH(Register.ESI);
    }

    /**
     * Write code to restore the callee saved registers.
     *
     * @see #SAVED_REGISTERSPACE
     * @see org.jnode.vm.x86.VmX86StackReader
     */
    private final void restoreRegisters() {
        //os.writePOP(Register.ESI);
        //os.writePOP(Register.EDI);
        //os.writePOP(Register.EBX);
    }
}
