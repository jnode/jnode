/*
 * $Id$
 */
package org.jnode.vm.x86.compiler;

import org.jnode.assembler.Label;
import org.jnode.assembler.x86.AbstractX86Stream;
import org.jnode.assembler.x86.Register;
import org.jnode.assembler.x86.X86Constants;
import org.jnode.vm.Address;
import org.jnode.vm.Unsafe;
import org.jnode.vm.Vm;
import org.jnode.vm.VmProcessor;
import org.jnode.vm.classmgr.VmArray;
import org.jnode.vm.classmgr.VmInstanceField;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmStaticField;
import org.jnode.vm.classmgr.VmStaticsEntry;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.classmgr.VmTypeState;
import org.jnode.vm.memmgr.VmWriteBarrier;
import org.jnode.vm.x86.X86CpuID;

/**
 * Helpers class used by the X86 compilers.
 * 
 * @author epr
 * @author patrik_reali
 */
public class X86CompilerHelper extends X86StackManager implements
        X86CompilerConstants {

    private final X86CompilerContext context;

    private VmMethod method;

    private String labelPrefix;

    private String instrLabelPrefix;

    private final boolean isBootstrap;

    private final Label jumpTableLabel;

    private final Address jumpTableAddress;

    private final boolean haveCMOV;

    private Label[] addressLabels;

    private final boolean debug = Vm.getVm().isDebugMode();

    /**
     * Create a new instance
     * 
     * @param context
     */
    public X86CompilerHelper(AbstractX86Stream os, X86CompilerContext context,
            boolean isBootstrap) {
        super(os);
        this.context = context;
        this.isBootstrap = isBootstrap;
        if (isBootstrap) {
            jumpTableLabel = new Label(X86JumpTable.JUMPTABLE_NAME);
            jumpTableAddress = null;
        } else {
            jumpTableLabel = null;
            jumpTableAddress = Unsafe.getJumpTable();
        }
        final X86CpuID cpuId = (X86CpuID) os.getCPUID();
        haveCMOV = cpuId.hasFeature(X86CpuID.FEAT_CMOV);
    }

    /**
     * Gets the method that is currently being compiled.
     * 
     * @return method
     */
    public final VmMethod getMethod() {
        return method;
    }

    /**
     * Sets the method that is currently being compiled.
     * 
     * @param method
     */
    public final void setMethod(VmMethod method) {
        this.method = method;
        this.labelPrefix = method.toString() + "_";
        this.instrLabelPrefix = labelPrefix + "_bci_";
        this.addressLabels = new Label[ method.getBytecodeSize()];
    }

    /**
     */
    public void startInlinedMethod(VmMethod inlinedMethod, Label curInstrLabel) {
        this.labelPrefix = curInstrLabel + "_" + inlinedMethod + "_";
        this.instrLabelPrefix = labelPrefix + "_bci_";
        this.addressLabels = new Label[ inlinedMethod.getBytecodeSize()];
    }

    /**
     * Create a method relative label to a given bytecode address.
     * 
     * @param address
     * @return The created label
     */
    public final Label getInstrLabel(int address) {
        Label l = addressLabels[ address];
        if (l == null) {
            l = new Label(instrLabelPrefix + address);
            addressLabels[ address] = l;
        }
        return l;
    }

    /**
     * Create a method relative label
     * 
     * @param postFix
     * @return The created label
     */
    public final Label genLabel(String postFix) {
        return new Label(labelPrefix + postFix);
    }

    /**
     * Write code to call the address found at the given offset in the system
     * jumptable.
     * 
     * @param offset
     * @see X86JumpTable
     */
    public final void writeJumpTableCALL(int offset) {
        if (isBootstrap) {
            os.writeCALL(jumpTableLabel, offset, false);
        } else {
            os.writeCALL(jumpTableAddress, offset, true);
        }
    }

    /**
     * Write code to jump to the address found at the given offset in the
     * system jumptable.
     * 
     * @param offset
     * @see X86JumpTable
     */
    public final void writeJumpTableJMP(int offset) {
        if (isBootstrap) {
            os.writeJMP(jumpTableLabel, offset, false);
        } else {
            os.writeJMP(jumpTableAddress, offset, true);
        }
    }

    /**
     * Emit code to invoke a method, where the reference to the VmMethod
     * instance is in register EAX.
     * 
     * @param signature
     */
    public final void invokeJavaMethod(String signature) {
        os.writeCALL(Register.EAX, context.getVmMethodNativeCodeField()
                .getOffset());
        //writeJumpTableCALL(X86JumpTable.VM_INVOKE_OFS);
        final char ch = signature.charAt(signature.length() - 1);
        if (ch == 'V') {
            /** No return value */
        } else if ((ch == 'J') || (ch == 'D')) {
            /** Wide return value */
            writePUSH64(Register.EAX, Register.EDX);
        } else {
            /** Normal return value */
            writePUSH(Register.EAX);
        }
    }

    /**
     * Emit code to invoke a java method
     * 
     * @param method
     */
    public final void invokeJavaMethod(VmMethod method) {
        os.writeMOV_Const(Register.EAX, method);
        invokeJavaMethod(method.getSignature());
    }

    /**
     * Insert a yieldpoint into the code
     */
    public final void writeYieldPoint(Object curInstrLabel) {
        if (method.getThreadSwitchIndicatorMask() != 0) {
            final Label doneLabel = new Label(curInstrLabel + "noYP");
            os.writePrefix(X86Constants.FS_PREFIX);
            os.writeCMP_MEM(context.getVmThreadSwitchIndicatorOffset(),
                    VmProcessor.TSI_SWITCH_REQUESTED);
            os.writeJCC(doneLabel, X86Constants.JNE);
            os.writeINT(X86CompilerConstants.YIELDPOINT_INTNO);
            os.setObjectRef(doneLabel);
        }
    }

    /**
     * Write class initialization code
     * 
     * @param method
     * @param methodReg
     *            Register that holds the method reference before this method
     *            is called.
     * @return true if code was written, false otherwise
     */
    public final boolean writeClassInitialize(VmMethod method,
            Register methodReg, Register scratch) {
        // Only for static methods (non <clinit>)
        if (method.isStatic() && !method.isInitializer()) {
            // Only when class is not initialize
            final VmType cls = method.getDeclaringClass();
            if (!cls.isInitialized()) {
                // Save eax
                writePUSH(EAX);
                // Do the is initialized test
                // Move method.declaringClass -> scratch
                os.writeMOV(INTSIZE, scratch, methodReg, context
                        .getVmMemberDeclaringClassField().getOffset());
                // Move declaringClass.modifiers -> EAX
                os.writeMOV(INTSIZE, EAX, scratch, context.getVmTypeState()
                        .getOffset());
                os.writeTEST_EAX(VmTypeState.ST_INITIALIZED);
                final Label afterInit = new Label(method.getMangledName()
                        + "$$after-classinit");
                os.writeJCC(afterInit, X86Constants.JNZ);
                // Call cls.initialize
                os.writeMOV(INTSIZE, EAX, scratch);
                writePUSH(EAX);
                invokeJavaMethod(context.getVmTypeInitialize());
                os.setObjectRef(afterInit);
                // Restore eax
                writePOP(EAX);
                return true;
            }
        }
        return false;
    }

    /**
     * Write method counter increment code.
     * 
     * @param methodReg
     *            Register that holds the method reference before this method
     *            is called.
     */
    public final void writeIncInvocationCount(Register methodReg) {
        final int offset = context.getVmMethodInvocationCountField()
                .getOffset();
        os.writeADD(methodReg, offset, 1);
    }

    /**
     * Write stack overflow test code.
     * 
     * @param method
     */
    public final void writeStackOverflowTest(VmMethod method) {
        //cmp esp,STACKEND
        //jg vm_invoke_testStackOverflowDone
        //vm_invoke_testStackOverflow:
        //int 0x31
        //vm_invoke_testStackOverflowDone:
        final int offset = context.getVmProcessorStackEnd().getOffset();
        final Label doneLabel = new Label(labelPrefix + "$$stackof-done");
        os.writePrefix(X86Constants.FS_PREFIX);
        os.writeCMP_MEM(Register.ESP, offset);
        os.writeJCC(doneLabel, X86Constants.JG);
        os.writeINT(0x31);
        os.setObjectRef(doneLabel);
    }

    /**
     * Write staticTable load code. After the code (generated by this method)
     * is executed, the STATICS register contains the reference to the statics
     * table.
     */
    public final void writeLoadSTATICS(Label curInstrLabel, String labelPrefix, boolean isTestOnly) {
        final int offset = context.getVmProcessorStaticsTable().getOffset();
        if (isTestOnly) {
            if (debug) {
                final Label ok = new Label(curInstrLabel + labelPrefix + "$$ediok");
                os.writePrefix(X86Constants.FS_PREFIX);
                os.writeCMP_MEM(STATICS, offset);
                os.writeJCC(ok, X86Constants.JE);
                os.writeINT(0x88);
                os.setObjectRef(ok);
            }
        } else {
            os.writeXOR(STATICS, STATICS);
            os.writePrefix(X86Constants.FS_PREFIX);
            os.writeMOV(INTSIZE, STATICS, STATICS, offset);
        }
    }

    /**
     * Is class initialization code needed for the given method.
     * 
     * @param method
     * @return true if class init code is needed, false otherwise.
     */
    public static boolean isClassInitializeNeeded(VmMethod method) {
        // Only for static methods (non <clinit>)
        if (method.isStatic() && !method.isInitializer()) {
            // Only when class is not initialize
            final VmType cls = method.getDeclaringClass();
            if (!cls.isInitialized()) { return true; }
        }
        return false;
    }

    /**
     * Write code to call the arrayStoreWriteBarrier.
     * 
     * @param refReg
     * @param indexReg
     * @param valueReg
     */
    public final void writeArrayStoreWriteBarrier(Register refReg,
            Register indexReg, Register valueReg, Register scratchReg) {
        final VmWriteBarrier wb = context.getWriteBarrier();
        if (wb != null) {
            os.writeMOV_Const(scratchReg, wb);
            writePUSH(scratchReg);
            writePUSH(refReg);
            writePUSH(indexReg);
            writePUSH(valueReg);
            invokeJavaMethod(context.getArrayStoreWriteBarrier());
        }
    }

    /**
     * Write code to call the putfieldWriteBarrier.
     * 
     * @param field
     * @param refReg
     * @param valueReg
     */
    public final void writePutfieldWriteBarrier(VmInstanceField field,
            Register refReg, Register valueReg, Register scratchReg) {
        if (field.isObjectRef()) {
            final VmWriteBarrier wb = context.getWriteBarrier();
            if (wb != null) {
                os.writeMOV_Const(scratchReg, wb);
                writePUSH(scratchReg);
                writePUSH(refReg);
                writePUSH(field.getOffset());
                writePUSH(valueReg);
                invokeJavaMethod(context.getPutfieldWriteBarrier());
            }
        }
    }

    /**
     * Write code to call the putstaticWriteBarrier.
     * 
     * @param field
     * @param valueReg
     */
    public final void writePutstaticWriteBarrier(VmStaticField field,
            Register valueReg, Register scratchReg) {
        if (field.isObjectRef()) {
            final VmWriteBarrier wb = context.getWriteBarrier();
            if (wb != null) {
                os.writeMOV_Const(scratchReg, wb);
                writePUSH(scratchReg);
                writePUSH(field.getStaticsIndex());
                writePUSH(valueReg);
                invokeJavaMethod(context.getPutstaticWriteBarrier());
            }
        }
    }

    /**
     * Is CMOVxx support bu the current cpu.
     * 
     * @return Returns the haveCMOV.
     */
    public final boolean haveCMOV() {
        return this.haveCMOV;
    }

    /**
     * Write code to load the given statics table entry into the given
     * register.
     * 
     * @param curInstrLabel
     * @param dst
     * @param entry
     */
    public final void writeGetStaticsEntry(Label curInstrLabel, Register dst,
            VmStaticsEntry entry) {
        writeLoadSTATICS(curInstrLabel, "gs", true);
        final int staticsIdx = (VmArray.DATA_OFFSET + entry.getStaticsIndex()) << 2;
        os.writeMOV(INTSIZE, dst, STATICS, staticsIdx);
    }

    /**
     * Write code to push the given statics table entry to the stack
     * 
     * @param curInstrLabel
     * @param entry
     */
    /* Patrik, added to push without requiring allocation of a register */
    public final void writePushStaticsEntry(Label curInstrLabel, 
		    VmStaticsEntry entry) {
        writeLoadSTATICS(curInstrLabel, "gs", true);
        final int staticsIdx = (VmArray.DATA_OFFSET + entry.getStaticsIndex()) << 2;
        os.writePUSH(STATICS, staticsIdx);
    }

    /**
     * Write code to load the given 64-bit statics table entry into the given
     * registers.
     * 
     * @param curInstrLabel
     * @param lsbDst
     * @param msbReg
     * @param entry
     */
    public final void writeGetStaticsEntry64(Label curInstrLabel,
            Register lsbDst, Register msbReg, VmStaticsEntry entry) {
        writeLoadSTATICS(curInstrLabel, "gs64", true);
        final int staticsIdx = (VmArray.DATA_OFFSET + entry.getStaticsIndex()) << 2;
        os.writeMOV(INTSIZE, msbReg, STATICS, staticsIdx + 4); // MSB
        os.writeMOV(INTSIZE, lsbDst, STATICS, staticsIdx + 0); // LSB
    }

    /**
     * Write code to store the given statics table entry into the given
     * register.
     * 
     * @param curInstrLabel
     * @param src
     * @param entry
     */
    public final void writePutStaticsEntry(Label curInstrLabel, Register src,
            VmStaticsEntry entry) {
        writeLoadSTATICS(curInstrLabel, "ps", true);
        final int staticsIdx = (VmArray.DATA_OFFSET + entry.getStaticsIndex()) << 2;
        os.writeMOV(INTSIZE, STATICS, staticsIdx, src);
    }

    /**
     * Write code to store the given 64-bit statics table entry into the given
     * registers.
     * 
     * @param curInstrLabel
     * @param lsbSrc
     * @param msbSrc
     * @param entry
     */
    public final void writePutStaticsEntry64(Label curInstrLabel,
            Register lsbSrc, Register msbSrc, VmStaticsEntry entry) {
        writeLoadSTATICS(curInstrLabel, "ps64", true);
        final int staticsIdx = (VmArray.DATA_OFFSET + entry.getStaticsIndex()) << 2;
        os.writeMOV(INTSIZE, STATICS, staticsIdx + 4, msbSrc); // MSB
        os.writeMOV(INTSIZE, STATICS, staticsIdx + 0, lsbSrc); // LSB
    }
}
