/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.Label;
import org.jnode.assembler.NativeStream;
import org.jnode.assembler.UnresolvedObjectRefException;
import org.jnode.assembler.x86.AbstractX86Stream;
import org.jnode.assembler.x86.Register;
import org.jnode.assembler.x86.X86Constants;
import org.jnode.system.BootLog;
import org.jnode.vm.JvmType;
import org.jnode.vm.SoftByteCodes;
import org.jnode.vm.bytecode.BasicBlock;
import org.jnode.vm.bytecode.BytecodeParser;
import org.jnode.vm.bytecode.TypeStack;
import org.jnode.vm.classmgr.ObjectLayout;
import org.jnode.vm.classmgr.Signature;
import org.jnode.vm.classmgr.TIBLayout;
import org.jnode.vm.classmgr.VmArray;
import org.jnode.vm.classmgr.VmClassLoader;
import org.jnode.vm.classmgr.VmConstClass;
import org.jnode.vm.classmgr.VmConstFieldRef;
import org.jnode.vm.classmgr.VmConstIMethodRef;
import org.jnode.vm.classmgr.VmConstMethodRef;
import org.jnode.vm.classmgr.VmConstString;
import org.jnode.vm.classmgr.VmField;
import org.jnode.vm.classmgr.VmInstanceField;
import org.jnode.vm.classmgr.VmInstanceMethod;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmStaticField;
import org.jnode.vm.classmgr.VmStaticMethod;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.classmgr.VmTypeState;
import org.jnode.vm.compiler.CompileError;
import org.jnode.vm.compiler.CompiledMethod;
import org.jnode.vm.compiler.InlineBytecodeVisitor;
import org.jnode.vm.x86.VmX86Architecture;
import org.jnode.vm.x86.compiler.X86CompilerConstants;
import org.jnode.vm.x86.compiler.X86CompilerContext;
import org.jnode.vm.x86.compiler.X86CompilerHelper;
import org.jnode.vm.x86.compiler.X86JumpTable;

/**
 * Actual converter from bytecodes to X86 native code. Uses a virtual stack to
 * delay item emission, as described in the ORP project
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Patrik Reali
 *  
 */

class X86BytecodeVisitor extends InlineBytecodeVisitor implements
        X86CompilerConstants {

    /** Debug this visitor, logs extra info */
    private static final boolean debug = false;

    /** The destination compiled method */
    private final CompiledMethod cm;

    /** Current context */
    private final X86CompilerContext context;

    /** Bytecode Address of current instruction */
    private int curAddress;

    /** Label of current instruction */
    private Label curInstrLabel;

    private VmMethod currentMethod;

    private final EmitterContext eContext;

    private Label endOfInlineLabel;

    /** Helper class */
    private final X86CompilerHelper helper;

    /** Class loader */
    private VmClassLoader loader;

    /** Emit logging info */
    private final boolean log;

    private int maxLocals;

    /** The output stream */
    private final AbstractX86Stream os;

    private TypeStack outerMethodStack;

    private boolean setCurInstrLabel;

    /** Size of an object reference */
    private final int slotSize;

    /** Stackframe utility */
    private X86StackFrame stackFrame;

    private boolean startOfBB;

    /** Length of os at start of method */
    private int startOffset;

    /*
     * Virtual Stack: this stack contains values that have been computed but not
     * emitted yet; emission is delayed to allow for optimizations, in
     * particular using registers instead of stack operations.
     * 
     * The vstack is valid only inside a basic block; items in the stack are
     * flushed at the end of the basic block.
     * 
     * Aliasing: modifying a value that is still on the stack is forbidden. Each
     * time a local is assigned, the stack is checked for aliases. For teh same
     * reason, array and field operations are not delayed.
     */
    private final VirtualStack vstack;

    /**
     * Create a new instance
     * 
     * @param outputStream
     * @param cm
     * @param isBootstrap
     * @param context
     */
    public X86BytecodeVisitor(NativeStream outputStream, CompiledMethod cm,
            boolean isBootstrap, X86CompilerContext context) {
        this.os = (AbstractX86Stream) outputStream;
        this.context = context;
        this.vstack = new VirtualStack(os);
        this.helper = new X86CompilerHelper(os, vstack.createStackMgr(),
                context, isBootstrap);
        this.cm = cm;
        this.slotSize = VmX86Architecture.SLOT_SIZE;
        this.log = os.isLogEnabled();
        this.eContext = new EmitterContext(os, helper, vstack);
    }

    /**
     * The started basic block has finished.
     */
    public void endBasicBlock() {
        // flush vstack: at end/begin of basic block are all items on the stack
        vstack.push(eContext);
    }

    /**
     * @see org.jnode.vm.compiler.InlineBytecodeVisitor#endInlinedMethod(org.jnode.vm.classmgr.VmMethod)
     */
    public void endInlinedMethod(VmMethod previousMethod) {
        helper.setMethod(previousMethod);
        os.setObjectRef(endOfInlineLabel);
        this.currentMethod = previousMethod;
        this.outerMethodStack = null;
        if (debug) {
            BootLog.debug("endInlinedMethod");
        }
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#endInstruction()
     */
    public void endInstruction() {
        // Verify the register usage
        // No registers can be in use, unless they are on the virtual stack.
        final X86RegisterPool pool = eContext.getPool();
        pool.visitUsedRegisters(new RegisterVisitor() {

            public void visit(Register reg) {
                if (!vstack.uses(reg)) { throw new InternalError(
                        "Register "
                                + reg
                                + " is in use outsite of the vstack at bytecode address "
                                + curAddress); }
            }
        });
        // Nothing to do here
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#endMethod()
     */
    public void endMethod() {
        stackFrame.emitTrailer(maxLocals);
    }

    /**
     * A try block has finished
     */
    public void endTryBlock() {
        setCurInstrLabel = true;
    }

    /**
     * @param parser
     * @see org.jnode.vm.bytecode.BytecodeVisitor#setParser(org.jnode.vm.bytecode.BytecodeParser)
     */
    public void setParser(BytecodeParser parser) {
        // Nothing to do here
    }

    /**
     * The given basic block is about to start.
     */
    public void startBasicBlock(BasicBlock bb) {
        if (log) {
            os.log("Start of basic block " + bb);
        }
        if (debug) {
            BootLog.debug("-- Start of BB");
        }
        startOfBB = true;
        this.vstack.reset();
        eContext.getPool().reset(os);
        // Push the result from the outer method stack on the vstack
        vstack.pushAll(outerMethodStack);
        // Push the items on the vstack the result from a previous basic block.
        final TypeStack tstack = bb.getStartStack();
        vstack.pushAll(tstack);
    }

    /**
     * @see org.jnode.vm.compiler.InlineBytecodeVisitor#startInlinedMethodCode(VmMethod,
     *      int)
     */
    public void startInlinedMethodCode(VmMethod inlinedMethod, int newMaxLocals) {
        if (debug) {
            BootLog.debug("startInlinedMethodCode(" + inlinedMethod + ")");
        }
        //TODO: check whether this is really needed
        // For now yes, because a new basic block resets the registerpool
        // and that fails if not all registers are freed.
        vstack.push(eContext);
        outerMethodStack = vstack.asTypeStack();
    }

    /**
     * @see org.jnode.vm.compiler.InlineBytecodeVisitor#startInlinedMethodHeader(VmMethod,
     *      int)
     */
    public void startInlinedMethodHeader(VmMethod inlinedMethod,
            int newMaxLocals) {
        if (debug) {
            BootLog.debug("startInlinedMethodHeader(" + inlinedMethod + ")");
        }
        maxLocals = newMaxLocals;
        endOfInlineLabel = new Label(curInstrLabel + "_end_of_inline");
        helper.startInlinedMethod(inlinedMethod, curInstrLabel);
        this.currentMethod = inlinedMethod;
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#startInstruction(int)
     */
    public void startInstruction(int address) {
        if (debug) {
            BootLog.debug("#" + address + "\t" + vstack);
        }
        this.curAddress = address;
        this.curInstrLabel = helper.getInstrLabel(address);
        if (startOfBB || setCurInstrLabel) {
            os.setObjectRef(curInstrLabel);
            startOfBB = false;
            setCurInstrLabel = false;
        }
        final int offset = os.getLength() - startOffset;
        cm.add(currentMethod, address, offset);
    }

    /**
     * @param method
     * @see org.jnode.vm.bytecode.BytecodeVisitor#startMethod(org.jnode.vm.classmgr.VmMethod)
     */
    public void startMethod(VmMethod method) {
        if (debug) {
            BootLog.debug("setMethod(" + method + ")");
        }
        this.currentMethod = method;
        this.maxLocals = method.getBytecode().getNoLocals();
        this.loader = method.getDeclaringClass().getLoader();
        helper.setMethod(method);
        // this.startOffset = os.getLength();
        this.stackFrame = new X86StackFrame(os, helper, method, context, cm);
        this.startOffset = stackFrame.emitHeader();
    }

    /**
     * A try block is about to start
     */
    public void startTryBlock() {
        setCurInstrLabel = true;
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_aaload()
     */
    public final void visit_aaload() {
        IntItem idx = vstack.popInt();
        RefItem ref = vstack.popRef();
        idx.loadIf(eContext, ~Item.Kind.CONSTANT);
        ref.load(eContext);
        Register refReg = ref.getRegister();
        checkBounds(ref, idx);
        if (idx.getKind() == Item.Kind.CONSTANT) {
            final int offset = idx.getValue();
            os.writeMOV(INTSIZE, refReg, refReg, offset + VmArray.DATA_OFFSET
                    * 4);
        } else {
            os.writeMOV(INTSIZE, refReg, refReg, idx.getRegister(), 4,
                    VmArray.DATA_OFFSET * 4);
            idx.release(eContext);
        }
        // do not release ref: it contains the result, so push it onto the stack
        vstack.push(ref);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_aastore()
     */
    public final void visit_aastore() {
        final boolean useBarrier = (context.getWriteBarrier() != null);

        RefItem val = vstack.popRef();
        IntItem idx = vstack.popInt();
        RefItem ref = vstack.popRef();

        //IMPROVE: optimize case with const value
        val.load(eContext);
        // if barrier in use, the index must also be in a register
        idx.loadIf(eContext, (useBarrier) ? ~Item.Kind.REGISTER
                : ~(Item.Kind.CONSTANT | Item.Kind.REGISTER));
        ref.load(eContext);
        final Register r = ref.getRegister();
        final Register v = val.getRegister();
        checkBounds(ref, idx);
        if (idx.getKind() == Item.Kind.CONSTANT) {
            final int i = idx.getValue();
            os.writeMOV(INTSIZE, r, i + VmArray.DATA_OFFSET * 4, v);
        } else {
            final Register i = idx.getRegister();
            os.writeMOV(INTSIZE, r, i, 4, VmArray.DATA_OFFSET * 4, v);
        }
        if (useBarrier) {
            // the write barrier could easily be modified to avoid using a
            // scratch register
            final X86RegisterPool pool = eContext.getPool();
            final Register scratch = pool.request(JvmType.INT);
            helper
                    .writeArrayStoreWriteBarrier(r, idx.getRegister(), v,
                            scratch);
            pool.release(scratch);
        }

        val.release(eContext);
        idx.release(eContext);
        ref.release(eContext);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_aconst_null()
     */
    public final void visit_aconst_null() {
        vstack.push(RefItem.createConst(null));
    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_aload(int)
     */
    public final void visit_aload(int index) {
        vstack.push(RefItem.createLocal(stackFrame.getEbpOffset(index)));
    }

    /**
     * @param classRef
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_anewarray(org.jnode.vm.classmgr.VmConstClass)
     */
    public final void visit_anewarray(VmConstClass classRef) {
        final IntItem cnt = vstack.popInt();

        // Load the count value
        cnt.load(eContext);
        final Register cntr = cnt.getRegister();
        // Request tmp register
        final Register classr = requestRegister(JvmType.INT);

        writeResolveAndLoadClassToReg(classRef, classr);

        stackFrame.writePushMethodRef();
        os.writePUSH(classr); /* Class */
        os.writePUSH(cntr); /* Count */
        helper.invokeJavaMethod(context.getAnewarrayMethod());
        /* Result is already push on the stack */

        // Release
        cnt.release(eContext);
        releaseRegister(classr);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_areturn()
     */
    public final void visit_areturn() {
        final RefItem val = vstack.popRef();
        if (!val.uses(EAX)) {
            requestRegister(EAX, val);
            val.loadTo(eContext, EAX);
        }
        val.release(eContext);

        visit_return();
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_arraylength()
     */
    public final void visit_arraylength() {
        final RefItem ref = vstack.popRef();
        ref.load(eContext);
        final Register r = ref.getRegister();

        os.writeMOV(INTSIZE, r, r, VmArray.LENGTH_OFFSET * slotSize);

        final IntItem i = IntItem.createReg(r);
        eContext.getPool().transferOwnerTo(r, i);
        vstack.push(i);
    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_astore(int)
     */
    public final void visit_astore(int index) {
        if (debug) {
            BootLog.debug("astore_" + index + "\t" + vstack);
        }
        int disp = stackFrame.getEbpOffset(index);

        // Pin down (load) other references to this local
        vstack.loadLocal(eContext, disp);

        RefItem i = vstack.popRef();
        i.load(eContext);
        os.writeMOV(INTSIZE, FP, disp, i.getRegister());
        i.release(eContext);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_athrow()
     */
    public final void visit_athrow() {
        final RefItem ref = vstack.popRef();

        // Exception must be in EAX
        if (!ref.uses(EAX)) {
            requestRegister(EAX, ref);
            ref.loadTo(eContext, EAX);
        }

        helper.writeJumpTableCALL(X86JumpTable.VM_ATHROW_OFS);
        ref.release(eContext);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_baload()
     */
    public final void visit_baload() {
        IntItem idx = vstack.popInt();
        RefItem ref = vstack.popRef();

        idx.loadIf(eContext, ~Item.Kind.CONSTANT);
        ref.load(eContext);
        final Register r = ref.getRegister();
        checkBounds(ref, idx);
        if (idx.getKind() == Item.Kind.CONSTANT) {
            final int offset = idx.getValue();
            os.writeMOV(BYTESIZE, r, r, offset + VmArray.DATA_OFFSET * 4);
        } else {
            final Register i = idx.getRegister();
            os.writeMOV(BYTESIZE, r, r, i, 1, VmArray.DATA_OFFSET * 4);
            idx.release(eContext);
        }
        os.writeMOVSX(r, r, BYTESIZE);

        final IntItem result = IntItem.createReg(r);
        eContext.getPool().transferOwnerTo(r, result);
        vstack.push(result);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_bastore()
     */
    public final void visit_bastore() {
        IntItem val = vstack.popInt();
        IntItem idx = vstack.popInt();
        RefItem ref = vstack.popRef();

        //IMPROVE: optimize case with const value
        val.load(eContext);
        idx.loadIf(eContext, ~Item.Kind.CONSTANT);
        ref.load(eContext);
        final Register r = ref.getRegister();
        final Register v = val.getRegister();

        checkBounds(ref, idx);
        if (idx.getKind() == Item.Kind.CONSTANT) {
            final int i = idx.getValue();
            os.writeMOV(BYTESIZE, r, i + VmArray.DATA_OFFSET * 4, v);
        } else {
            final Register i = idx.getRegister();
            os.writeMOV(BYTESIZE, r, i, 4, VmArray.DATA_OFFSET * 4, v);
        }
        val.release(eContext);
        idx.release(eContext);
        ref.release(eContext);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_caload()
     */
    public final void visit_caload() {
        IntItem idx = vstack.popInt();
        RefItem ref = vstack.popRef();

        idx.loadIf(eContext, ~Item.Kind.CONSTANT);
        ref.load(eContext);

        final Register r = ref.getRegister();
        checkBounds(ref, idx);
        if (idx.getKind() == Item.Kind.CONSTANT) {
            final int offset = idx.getValue();
            os.writeMOV(WORDSIZE, r, r, offset + VmArray.DATA_OFFSET * 4);
        } else {
            final Register offset = idx.getRegister();
            os.writeMOV(WORDSIZE, r, r, offset, 2, VmArray.DATA_OFFSET * 4);
            idx.release(eContext);
        }
        os.writeMOVZX(r, r, WORDSIZE);
        // do not release ref, it is recycled into the result
        final IntItem result = IntItem.createReg(r);
        eContext.getPool().transferOwnerTo(r, result);
        vstack.push(result);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_castore()
     */
    public final void visit_castore() {
        IntItem val = vstack.popInt();
        IntItem idx = vstack.popInt();
        RefItem ref = vstack.popRef();

        //IMPROVE: optimize case with const value
        val.load(eContext);
        idx.loadIf(eContext, ~Item.Kind.CONSTANT);
        ref.load(eContext);
        final Register r = ref.getRegister();
        final Register v = val.getRegister();

        checkBounds(ref, idx);
        if (idx.getKind() == Item.Kind.CONSTANT) {
            final int i = idx.getValue();
            os.writeMOV(WORDSIZE, r, i + VmArray.DATA_OFFSET * 4, v);
        } else {
            final Register i = idx.getRegister();
            os.writeMOV(WORDSIZE, r, i, 2, VmArray.DATA_OFFSET * 4, v);
        }
        val.release(eContext);
        idx.release(eContext);
        ref.release(eContext);
    }

    /**
     * @param classRef
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_checkcast(org.jnode.vm.classmgr.VmConstClass)
     */
    public final void visit_checkcast(VmConstClass classRef) {
        // check that top item is a reference
        final RefItem ref = vstack.popRef();

        // Load the ref
        ref.load(eContext);
        final Register refr = ref.getRegister();
        final Register classr = requestRegister(JvmType.INT);

        // Resolve the class
        writeResolveAndLoadClassToReg(classRef, classr);

        final Label okLabel = new Label(this.curInstrLabel + "cc-ok");

        /* Is objectref null? */
        os.writeTEST(refr, refr);
        os.writeJCC(okLabel, X86Constants.JZ);
        /* Is instanceof? */
        instanceOf(refr, okLabel);
        /* Not instanceof */

        // Call SoftByteCodes.systemException
        os.writePUSH(SoftByteCodes.EX_CLASSCAST);
        os.writePUSH(0);
        helper.invokeJavaMethod(context.getSystemExceptionMethod());
        final RefItem exi = vstack.popRef();
        assertCondition(exi.uses(EAX), "item must be in eax");

        /* Exception in EAX, throw it */
        helper.writeJumpTableCALL(X86JumpTable.VM_ATHROW_OFS);

        /* Normal exit */
        os.setObjectRef(okLabel);

        // Leave ref on stack
        vstack.push(ref);

        // Release
        releaseRegister(classr);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_d2f()
     */
    public final void visit_d2f() {
        FPUHelper.convert(eContext, vstack, JvmType.DOUBLE, JvmType.FLOAT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_d2i()
     */
    public final void visit_d2i() {
        FPUHelper.convert(eContext, vstack, JvmType.DOUBLE, JvmType.INT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_d2l()
     */
    public final void visit_d2l() {
        FPUHelper.convert(eContext, vstack, JvmType.DOUBLE, JvmType.LONG);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dadd()
     */
    public final void visit_dadd() {
        FPUHelper.add(os, eContext, vstack, JvmType.DOUBLE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_daload()
     */
    public final void visit_daload() {
        visit_dwaload(JvmType.DOUBLE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dastore()
     */
    public final void visit_dastore() {
        visit_dwastore(JvmType.DOUBLE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dcmpg()
     */
    public final void visit_dcmpg() {
        FPUHelper.compare(os, eContext, vstack, true, JvmType.DOUBLE, curInstrLabel);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dcmpl()
     */
    public final void visit_dcmpl() {
        FPUHelper.compare(os, eContext, vstack, false, JvmType.DOUBLE, curInstrLabel);
    }

    /**
     * @param value
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dconst(double)
     */
    public final void visit_dconst(double value) {
        vstack.push(DoubleItem.createConst(value));
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ddiv()
     */
    public final void visit_ddiv() {
        FPUHelper.div(os, eContext, vstack, JvmType.DOUBLE);
    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dload(int)
     */
    public final void visit_dload(int index) {
        vstack.push(DoubleItem.createLocal(stackFrame.getWideEbpOffset(index)));
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dmul()
     */
    public final void visit_dmul() {
        FPUHelper.mul(os, eContext, vstack, JvmType.DOUBLE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dneg()
     */
    public final void visit_dneg() {
        FPUHelper.neg(os, eContext, vstack, JvmType.DOUBLE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_drem()
     */
    public final void visit_drem() {
        FPUHelper.rem(os, eContext, vstack, JvmType.DOUBLE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dreturn()
     */
    public final void visit_dreturn() {
        final DoubleItem val = vstack.popDouble();
        if (!(val.uses(EAX) && val.uses(EDX))) {
            if (val.uses(EAX) || val.uses(EDX)) {
                val.push(eContext);
            }
            requestRegister(EAX, val);
            requestRegister(EDX, val);
            val.loadTo(eContext, EAX, EDX);
        }
        val.release(eContext);
        visit_return();
    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dstore(int)
     */
    public final void visit_dstore(int index) {
        final DoubleItem val = vstack.popDouble();

        final int ebpOfs = stackFrame.getWideEbpOffset(index);

        // pin down other references to this load
        vstack.loadLocal(eContext, ebpOfs);

        // Load value
        val.loadToGPR(eContext);

        // Store
        final Register lsb = val.getLsbRegister();
        final Register msb = val.getMsbRegister();
        os.writeMOV(INTSIZE, FP, ebpOfs, lsb);
        os.writeMOV(INTSIZE, FP, ebpOfs + 4, msb);

        // Release
        val.release(eContext);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dsub()
     */
    public final void visit_dsub() {
        FPUHelper.sub(os, eContext, vstack, JvmType.DOUBLE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup()
     */
    public final void visit_dup() {
        final Item v1 = vstack.pop();
        v1.loadIf(eContext, Item.Kind.STACK | Item.Kind.FPUSTACK);
        vstack.push(v1);
        vstack.push(v1.clone(eContext));
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup_x1()
     */
    public final void visit_dup_x1() {
        final Item v1 = vstack.pop();
        final Item v2 = vstack.pop();
        v1.loadIf(eContext, Item.Kind.STACK | Item.Kind.FPUSTACK);
        v2.loadIf(eContext, Item.Kind.STACK | Item.Kind.FPUSTACK);
        vstack.push(v1.clone(eContext));
        vstack.push(v2);
        vstack.push(v1);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup_x2()
     */
    public final void visit_dup_x2() {
        final Item v1 = vstack.pop();
        final Item v2 = vstack.pop();
        v1.loadIf(eContext, Item.Kind.STACK | Item.Kind.FPUSTACK);
        v2.loadIf(eContext, Item.Kind.STACK | Item.Kind.FPUSTACK);
        if (v2.getCategory() == 2) {
            // form2
            vstack.push(v1.clone(eContext));
            vstack.push(v2);
            vstack.push(v1);
        } else {
            // form1
            final Item v3 = vstack.pop();
            v3.loadIf(eContext, Item.Kind.STACK | Item.Kind.FPUSTACK);
            vstack.push(v1.clone(eContext));
            vstack.push(v3);
            vstack.push(v2);
            vstack.push(v1);
        }
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup2()
     */
    public final void visit_dup2() {
        final Item v1 = vstack.pop();
        v1.loadIf(eContext, Item.Kind.STACK | Item.Kind.FPUSTACK);
        if (v1.getCategory() == 1) {
            // form1
            final Item v2 = vstack.pop();
            v2.loadIf(eContext, Item.Kind.STACK | Item.Kind.FPUSTACK);
            assertCondition(v2.getCategory() == 1, "category mismatch");
            vstack.push(v2.clone(eContext));
            vstack.push(v1);
            vstack.push(v2.clone(eContext));
            vstack.push(v1);
        } else {
            vstack.push(v1.clone(eContext));
            vstack.push(v1);
        }
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup2_x1()
     */
    public final void visit_dup2_x1() {
        final Item v1 = vstack.pop();
        final Item v2 = vstack.pop();
        v1.loadIf(eContext, Item.Kind.STACK | Item.Kind.FPUSTACK);
        v2.loadIf(eContext, Item.Kind.STACK | Item.Kind.FPUSTACK);
        assertCondition(v2.getCategory() == 1, "category mismatch");
        if (v1.getCategory() == 2) { // form2
            vstack.push(v1.clone(eContext));
            vstack.push(v2);
            vstack.push(v1);
        } else {
            final Item v3 = vstack.pop();
            v3.loadIf(eContext, Item.Kind.STACK | Item.Kind.FPUSTACK);
            vstack.push(v2.clone(eContext));
            vstack.push(v1.clone(eContext));
            vstack.push(v3);
            vstack.push(v2);
            vstack.push(v1);
        }
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup2_x2()
     */
    public final void visit_dup2_x2() {
        final Item v1 = vstack.pop();
        final Item v2 = vstack.pop();
        final int c1 = v1.getCategory();
        final int c2 = v2.getCategory();
        v1.loadIf(eContext, Item.Kind.STACK | Item.Kind.FPUSTACK);
        v2.loadIf(eContext, Item.Kind.STACK | Item.Kind.FPUSTACK);
        // cope with brain-dead definition from Sun (look-like somebody there
        // was to eager to optimize this and it landed in the compiler...
        if (c2 == 2) {
            // form 4
            assertCondition(c1 == 2, "category mismatch");
            vstack.push(v1.clone(eContext));
            vstack.push(v2);
            vstack.push(v1);
        } else {
            final Item v3 = vstack.pop();
            v3.loadIf(eContext, Item.Kind.STACK | Item.Kind.FPUSTACK);
            int c3 = v3.getCategory();
            if (c1 == 2) {
                // form 2
                assertCondition(c3 == 1, "category mismatch");
                vstack.push(v1.clone(eContext));
                vstack.push(v3);
                vstack.push(v2);
                vstack.push(v1);
            } else if (c3 == 2) {
                // form 3
                vstack.push(v2.clone(eContext));
                vstack.push(v1.clone(eContext));
                vstack.push(v3);
                vstack.push(v2);
                vstack.push(v1);
            } else {
                // form 1
                final Item v4 = vstack.pop();
                v4.loadIf(eContext, Item.Kind.STACK | Item.Kind.FPUSTACK);
                vstack.push(v2.clone(eContext));
                vstack.push(v1.clone(eContext));
                vstack.push(v4);
                vstack.push(v3);
                vstack.push(v2);
                vstack.push(v1);
            }
        }
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_f2d()
     */
    public final void visit_f2d() {
        FPUHelper.convert(eContext, vstack, JvmType.FLOAT, JvmType.DOUBLE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_f2i()
     */
    public final void visit_f2i() {
        FPUHelper.convert(eContext, vstack, JvmType.FLOAT, JvmType.INT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_f2l()
     */
    public final void visit_f2l() {
        FPUHelper.convert(eContext, vstack, JvmType.FLOAT, JvmType.LONG);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fadd()
     */
    public final void visit_fadd() {
        FPUHelper.add(os, eContext, vstack, JvmType.FLOAT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_faload()
     */
    public final void visit_faload() {
        IntItem idx = vstack.popInt();
        RefItem ref = vstack.popRef();

        // flush vstack onto operand stack because result
        // is also on stack
        vstack.push(eContext);

        idx.loadIf(eContext, ~Item.Kind.CONSTANT);
        ref.load(eContext);
        final Register refReg = ref.getRegister();

        checkBounds(ref, idx);
        //IMPROVE: load to ST
        if (idx.getKind() == Item.Kind.CONSTANT) {
            final int offset = idx.getValue();
            //os.writeMOV(INTSIZE, refReg, refReg, offset+VmArray.DATA_OFFSET
            // * 4);
            os.writePUSH(refReg, offset + VmArray.DATA_OFFSET * 4);
            idx.release(eContext);
        } else {
            //os.writeMOV(INTSIZE, refReg, refReg, idx.getRegister(), 2,
            // VmArray.DATA_OFFSET * 4);
            os.writePUSH(refReg, idx.getRegister(), 2, VmArray.DATA_OFFSET * 4);
            ref.release(eContext);
            idx.release(eContext);
        }

        // do not release ref, it is recycled into the result
        vstack.push1(FloatItem.createStack());
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fastore()
     */
    public final void visit_fastore() {
        final FloatItem val = vstack.popFloat();
        final IntItem idx = vstack.popInt();
        final RefItem ref = vstack.popRef();

        val.loadToGPR(eContext);
        idx.load(eContext);
        ref.load(eContext);
        final Register valr = val.getRegister();
        final Register idxr = idx.getRegister();
        final Register refr = ref.getRegister();

        checkBounds(refr, idxr);
        os.writeMOV(INTSIZE, refr, idxr, 4, VmArray.DATA_OFFSET * 4, valr);

        val.release(eContext);
        idx.release(eContext);
        ref.release(eContext);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fcmpg()
     */
    public final void visit_fcmpg() {
        FPUHelper.compare(os, eContext, vstack, true, JvmType.FLOAT, curInstrLabel);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fcmpl()
     */
    public final void visit_fcmpl() {
        FPUHelper.compare(os, eContext, vstack, false, JvmType.FLOAT, curInstrLabel);
    }

    /**
     * @param value
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fconst(float)
     */
    public final void visit_fconst(float value) {
        vstack.push(FloatItem.createConst(value));
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fdiv()
     */
    public final void visit_fdiv() {
        FPUHelper.div(os, eContext, vstack, JvmType.FLOAT);
    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fload(int)
     */
    public final void visit_fload(int index) {
        vstack.push(FloatItem.createLocal(stackFrame.getEbpOffset(index)));
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fmul()
     */
    public final void visit_fmul() {
        FPUHelper.mul(os, eContext, vstack, JvmType.FLOAT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fneg()
     */
    public final void visit_fneg() {
        FPUHelper.neg(os, eContext, vstack, JvmType.FLOAT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_frem()
     */
    public final void visit_frem() {
        FPUHelper.rem(os, eContext, vstack, JvmType.FLOAT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_freturn()
     */
    public final void visit_freturn() {
        final FloatItem val = vstack.popFloat();
        if (!val.uses(EAX)) {
            requestRegister(EAX, val);
            val.loadTo(eContext, EAX);
        }
        val.release(eContext);
        visit_return();
    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fstore(int)
     */
    public final void visit_fstore(int index) {
        final FloatItem val = vstack.popFloat();
        final int ebpOfs = stackFrame.getEbpOffset(index);

        // pin down other references to this local
        vstack.loadLocal(eContext, ebpOfs);

        // Load
        val.loadToGPR(eContext);
        final Register valr = val.getRegister();

        // Save
        os.writeMOV(INTSIZE, FP, ebpOfs, valr);

        // Release
        val.release(eContext);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fsub()
     */
    public final void visit_fsub() {
        FPUHelper.sub(os, eContext, vstack, JvmType.FLOAT);
    }

    /**
     * @param fieldRef
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_getfield(org.jnode.vm.classmgr.VmConstFieldRef)
     */
    public final void visit_getfield(VmConstFieldRef fieldRef) {
        //TODO: port to orp-style; must probably implement a getfield method
        // in each Item!
        fieldRef.resolve(loader);
        final VmField field = fieldRef.getResolvedVmField();
        if (field.isStatic()) { throw new IncompatibleClassChangeError(
                "getfield called on static field " + fieldRef.getName()); }
        final VmInstanceField inf = (VmInstanceField) field;
        final int offset = inf.getOffset();

        final int type = JvmType.SignatureToType(fieldRef.getSignature());

        final RefItem ref = vstack.popRef();
        ref.load(eContext);
        final Register refr = ref.getRegister();
        final X86RegisterPool pool = eContext.getPool();

        if (!fieldRef.isWide()) {
            final WordItem iw = WordItem.createReg(type, refr);
            os.writeMOV(INTSIZE, refr, refr, offset);
            pool.transferOwnerTo(refr, iw);
            vstack.push(iw);
        } else {
            final DoubleWordItem idw = requestDoubleWordRegisters(type);
            os.writeMOV(INTSIZE, idw.getMsbRegister(), refr, offset + 4); // MSB
            os.writeMOV(INTSIZE, idw.getLsbRegister(), refr, offset); // LSB
            ref.release(eContext);
            vstack.push(idw);
        }
    }

    /**
     * @param fieldRef
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_getstatic(org.jnode.vm.classmgr.VmConstFieldRef)
     */
    public final void visit_getstatic(VmConstFieldRef fieldRef) {
        final X86RegisterPool pool = eContext.getPool();

        fieldRef.resolve(loader);
        final int type = JvmType.SignatureToType(fieldRef.getSignature());
        final VmStaticField sf = (VmStaticField) fieldRef.getResolvedVmField();

        // Initialize if needed
        if (!sf.getDeclaringClass().isInitialized()) {
            final Register tmp = requestRegister(JvmType.INT);
            writeInitializeClass(fieldRef, tmp);
            pool.release(tmp);
        }

        // Get static field object
        if (!fieldRef.isWide()) {
            final WordItem result = requestWordRegister(type);
            helper
                    .writeGetStaticsEntry(curInstrLabel, result.getRegister(),
                            sf);
            vstack.push(result);
        } else {
            final DoubleWordItem result = requestDoubleWordRegisters(type);
            helper.writeGetStaticsEntry64(curInstrLabel, result
                    .getLsbRegister(), result.getMsbRegister(), sf);
            vstack.push(result);
        }
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_goto(int)
     */
    public final void visit_goto(int address) {
        vstack.push(eContext);
        os.writeJMP(helper.getInstrLabel(address));
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2b()
     */
    public final void visit_i2b() {
        final IntItem v = vstack.popInt();
        if (v.getKind() == Item.Kind.CONSTANT) {
            vstack.push(IntItem.createConst((byte) v.getValue()));
        } else {
            v.load(eContext);
            final Register r = v.getRegister();
            os.writeMOVSX(r, r, BYTESIZE);
            vstack.push(v);
        }
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2c()
     */
    public final void visit_i2c() {
        final IntItem v = vstack.popInt();
        if (v.getKind() == Item.Kind.CONSTANT) {
            vstack.push(IntItem.createConst((char) v.getValue()));
        } else {
            v.load(eContext);
            final Register r = v.getRegister();
            os.writeMOVZX(r, r, BYTESIZE);
            vstack.push(v);
        }
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2d()
     */
    public final void visit_i2d() {
        //TODO: port to orp-style
        vstack.push(eContext);
        IntItem v = vstack.popInt();
        v.release1(eContext);
        vstack.push1(DoubleItem.createStack());

        os.writeFILD32(SP, 0);
        os.writeLEA(SP, SP, -4);
        os.writeFSTP64(SP, 0);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2f()
     */
    public final void visit_i2f() {
        //TODO: port to orp-style
        vstack.push(eContext);
        IntItem v = vstack.popInt();
        v.release1(eContext);
        vstack.push1(FloatItem.createStack());

        os.writeFILD32(SP, 0);
        os.writeFSTP32(SP, 0);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2l()
     */
    public final void visit_i2l() {
        final IntItem v = vstack.popInt();
        if (v.getKind() == Item.Kind.CONSTANT) {
            vstack.push(LongItem.createConst(v.getValue()));
        } else {
            final X86RegisterPool pool = eContext.getPool();
            requestRegister(EAX, v);
            v.loadTo(eContext, EAX);

            final LongItem result = LongItem.createReg(EAX, EDX);
            requestRegister(EDX, result);

            os.writeCDQ(); /* Sign extend EAX -> EDX:EAX */
            pool.transferOwnerTo(EAX, result);

            vstack.push(result);
        }
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2s()
     */
    public final void visit_i2s() {
        IntItem v = vstack.popInt();
        v.load(eContext);
        final Register r = v.getRegister();
        os.writeMOVSX(r, r, WORDSIZE);
        vstack.push(v);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iadd()
     */
    public final void visit_iadd() {
        //REFACTOR: parametrize the os.write operations to avoid code
        // duplication
        //IMPROVE: allow parameter permutation
        final IntItem v2 = vstack.popInt();
        final IntItem v1 = vstack.popInt();
        prepareForOperation(v1, v2);

        final Register r1 = v1.getRegister();
        switch (v2.getKind()) {
        case Item.Kind.REGISTER:
            os.writeADD(r1, v2.getRegister());
            break;
        case Item.Kind.LOCAL:
            os.writeADD(r1, FP, v2.getOffsetToFP());
            break;
        case Item.Kind.CONSTANT:
            os.writeADD(r1, v2.getValue());
            break;
        }
        v2.release(eContext);
        vstack.push(v1);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iaload()
     */
    public final void visit_iaload() {
        IntItem idx = vstack.popInt();
        RefItem ref = vstack.popRef();

        idx.loadIf(eContext, ~Item.Kind.CONSTANT);
        ref.load(eContext);

        final Register r = ref.getRegister();
        checkBounds(ref, idx);
        if (idx.getKind() == Item.Kind.CONSTANT) {
            final int i = idx.getValue();
            os.writeMOV(INTSIZE, r, r, i + VmArray.DATA_OFFSET * 4);
        } else {
            final Register i = idx.getRegister();
            os.writeMOV(INTSIZE, r, r, i, 4, VmArray.DATA_OFFSET * 4);
            idx.release(eContext);
        }

        final IntItem result = IntItem.createReg(r);
        eContext.getPool().transferOwnerTo(r, result);
        vstack.push(result);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iand()
     */
    public final void visit_iand() {
        //REFACTOR: parametrize the os.write operations to avoid code
        // duplication
        //IMPROVE: allow parameter permutation
        IntItem v2 = vstack.popInt();
        IntItem v1 = vstack.popInt();
        prepareForOperation(v1, v2);

        final Register r1 = v1.getRegister();
        switch (v2.getKind()) {
        case Item.Kind.REGISTER:
            os.writeAND(r1, v2.getRegister());
            break;
        case Item.Kind.LOCAL:
            os.writeAND(r1, FP, v2.getOffsetToFP());
            break;
        case Item.Kind.CONSTANT:
            os.writeAND(r1, v2.getValue());
            break;
        }
        v2.release(eContext);
        vstack.push(v1);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iastore()
     */
    public final void visit_iastore() {
        IntItem val = vstack.popInt();
        IntItem idx = vstack.popInt();
        RefItem ref = vstack.popRef();

        val.loadIf(eContext, ~Item.Kind.CONSTANT);
        idx.loadIf(eContext, ~Item.Kind.CONSTANT);
        ref.load(eContext);
        final Register r = ref.getRegister();

        checkBounds(ref, idx);
        if (idx.getKind() == Item.Kind.CONSTANT) {
            final int i = idx.getValue();
            if (val.getKind() == Item.Kind.CONSTANT) {
                final int vc = val.getValue();
                os.writeMOV_Const(r, i + VmArray.DATA_OFFSET * 4, vc);
            } else {
                final Register v = val.getRegister();
                os.writeMOV(INTSIZE, r, i + VmArray.DATA_OFFSET * 4, v);
            }
        } else {
            final Register i = idx.getRegister();
            val.load(eContext); //tmp
            if (val.getKind() == Item.Kind.CONSTANT) {
                // int vc = val.getValue();
                //TODO: implement writeMOV_Const disp[reg][idx], imm32
                //os.writeMOV_Const(r, idx.getRegister(), 4,
                // VmArray.DATA_OFFSET * 4, vc);
            } else {
                final Register v = val.getRegister();
                os.writeMOV(INTSIZE, r, i, 4, VmArray.DATA_OFFSET * 4, v);
            }
        }
        val.release(eContext);
        idx.release(eContext);
        ref.release(eContext);
    }

    /**
     * @param value
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iconst(int)
     */
    public final void visit_iconst(int value) {
        vstack.push(IntItem.createConst(value));
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_idiv()
     */
    public final void visit_idiv() {
        final X86RegisterPool pool = eContext.getPool();

        // Pop the arguments of the vstack
        final IntItem v2 = vstack.popInt();
        final IntItem v1 = vstack.popInt();

        // We need v1 in EAX, so if that is not the case,
        // spill those item using EAX
        requestRegister(EAX, v1);

        // We need to use EDX, so spill those items using it.
        v1.spillIfUsing(eContext, EDX);
        v2.spillIfUsing(eContext, EDX);
        requestRegister(EDX);

        // Load v2, v1 into a register
        v2.load(eContext);
        v1.loadTo(eContext, Register.EAX);

        // EAX -> sign extend EDX:EAX
        os.writeCDQ();

        // EAX = EDX:EAX / v2.reg
        os.writeIDIV_EAX(v2.getRegister());

        // Free unused registers
        pool.release(Register.EDX);
        v2.release(eContext);

        // And push the result on the vstack.
        vstack.push(v1);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_acmpeq(int)
     */
    public final void visit_if_acmpeq(int address) {
        visit_if_acmp(address, X86Constants.JE); // JE
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_acmpne(int)
     */
    public final void visit_if_acmpne(int address) {
        visit_if_acmp(address, X86Constants.JNE); // JNE
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmpeq(int)
     */
    public final void visit_if_icmpeq(int address) {
        visit_if_icmp(address, X86Constants.JE); // JE
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmpge(int)
     */
    public final void visit_if_icmpge(int address) {
        visit_if_icmp(address, X86Constants.JGE); // JGE
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmpgt(int)
     */
    public final void visit_if_icmpgt(int address) {
        visit_if_icmp(address, X86Constants.JG); // JG
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmple(int)
     */
    public final void visit_if_icmple(int address) {
        visit_if_icmp(address, X86Constants.JLE); // JLE
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmplt(int)
     */
    public final void visit_if_icmplt(int address) {
        visit_if_icmp(address, X86Constants.JL); // JL
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmpne(int)
     */
    public final void visit_if_icmpne(int address) {
        visit_if_icmp(address, X86Constants.JNE); // JNE
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifeq(int)
     */
    public final void visit_ifeq(int address) {
        visit_ifxx(JvmType.INT, address, X86Constants.JE); // JE
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifge(int)
     */
    public final void visit_ifge(int address) {
        visit_ifxx(JvmType.INT, address, X86Constants.JGE); // JGE
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifgt(int)
     */
    public final void visit_ifgt(int address) {
        visit_ifxx(JvmType.INT, address, X86Constants.JG); // JG
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifle(int)
     */
    public final void visit_ifle(int address) {
        visit_ifxx(JvmType.INT, address, X86Constants.JLE); // JLE
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iflt(int)
     */
    public final void visit_iflt(int address) {
        visit_ifxx(JvmType.INT, address, X86Constants.JL); // JL
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifne(int)
     */
    public final void visit_ifne(int address) {
        visit_ifxx(JvmType.INT, address, X86Constants.JNE); // JNE
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifnonnull(int)
     */
    public final void visit_ifnonnull(int address) {
        visit_ifxx(JvmType.REFERENCE, address, X86Constants.JNE);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifnull(int)
     */
    public final void visit_ifnull(int address) {
        visit_ifxx(JvmType.REFERENCE, address, X86Constants.JE);
    }

    /**
     * @param index
     * @param incValue
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iinc(int, int)
     */
    public final void visit_iinc(int index, int incValue) {
        final int ebpOfs = stackFrame.getEbpOffset(index);

        // pin down other references to this local
        vstack.loadLocal(eContext, ebpOfs);

        if (incValue == 1) {
            os.writeINC(FP, ebpOfs);
        } else {
            os.writeADD(FP, ebpOfs, incValue);
        }
    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iload(int)
     */
    public final void visit_iload(int index) {
        vstack.push(IntItem.createLocal(stackFrame.getEbpOffset(index)));
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_imul()
     */
    public final void visit_imul() {
        IntItem v2 = vstack.popInt();
        IntItem v1 = vstack.popInt();
        prepareForOperation(v1, v2);
        final Register r1 = v1.getRegister();
        switch (v2.getKind()) {
        case Item.Kind.REGISTER:
            os.writeIMUL(r1, v2.getRegister());
            break;
        case Item.Kind.CONSTANT:
            os.writeIMUL_3(r1, r1, v2.getValue());
            break;
        case Item.Kind.LOCAL:
            os.writeIMUL(r1, FP, v2.getOffsetToFP());
            break;
        }
        v2.release(eContext);
        vstack.push(v1);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ineg()
     */
    public final void visit_ineg() {
        IntItem v = vstack.popInt();
        v.load(eContext);
        os.writeNEG(v.getRegister());
        vstack.push(v);
    }

    /**
     * @see org.jnode.vm.compiler.InlineBytecodeVisitor#visit_inlinedReturn()
     */
    public void visit_inlinedReturn() {
        if (debug) {
            BootLog.debug("inlinedReturn");
        }
        vstack.push(eContext);
        os.writeJMP(endOfInlineLabel);
    }

    /**
     * @param classRef
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_instanceof(org.jnode.vm.classmgr.VmConstClass)
     */
    public final void visit_instanceof(VmConstClass classRef) {
        // Load reference
        final RefItem ref = vstack.popRef();
        ref.load(eContext);
        final Register refr = ref.getRegister();

        // Allocate tmp register
        final Register classr = requestRegister(JvmType.INT);

        /* Objectref is already on the stack */
        writeResolveAndLoadClassToReg(classRef, classr);

        final Label trueLabel = new Label(this.curInstrLabel + "io-true");
        final Label endLabel = new Label(this.curInstrLabel + "io-end");

        /* Is instanceof? */
        instanceOf(refr, trueLabel);
        /* Not instanceof */
        //TODO: use setcc instead of jumps
        os.writeXOR(refr, refr);
        os.writeJMP(endLabel);

        os.setObjectRef(trueLabel);
        os.writeMOV_Const(refr, 1);

        os.setObjectRef(endLabel);
        final IntItem result = IntItem.createReg(refr);
        eContext.getPool().transferOwnerTo(refr, result);
        vstack.push(result);
    }

    /**
     * @param methodRef
     * @param count
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_invokeinterface(VmConstIMethodRef,
     *      int)
     */
    public final void visit_invokeinterface(VmConstIMethodRef methodRef,
            int count) {
        //TODO: port to orp-style
        vstack.push(eContext);

        methodRef.resolve(loader);
        /*
         * if (!methodRef.getConstClass().isResolved()) { Label startClassLabel =
         * new Label(this.curInstrLabel + "startClass");
         * os.setObjectRef(startClassLabel);
         * resolveClass(methodRef.getConstClass()); patch_NOP(startClassLabel); }
         * 
         * if (!methodRef.isResolved()) { Label startLabel = new
         * Label(this.curInstrLabel + "start"); os.setObjectRef(startLabel);
         * resolveMethod(methodRef); patch_NOP(startLabel);
         */

        final VmMethod method = methodRef.getResolvedVmMethod();
        final int selector = method.getSelector();
        final int imtIndex = selector % ObjectLayout.IMT_LENGTH;
        final int argSlotCount = count - 1;
        final Label noCollLabel = new Label(this.curInstrLabel + "NoCollision");
        final Label findSelectorLabel = new Label(this.curInstrLabel
                + "FindSelector");
        final Label endLabel = new Label(this.curInstrLabel + "End");

        // remove parameters from vstack
        dropParameters(method, true);

        // Get objectref -> EBX
        os.writeMOV(INTSIZE, Register.EBX, SP, argSlotCount * slotSize);

        /*
         * // methodRef -> EDX os.writeMOV_Const(Register.EDX, methodRef); //
         * methodRef.selector -> ecx os.writeMOV(INTSIZE, Register.ECX,
         * Register.EDX,
         * context.getVmConstIMethodRefSelectorField().getOffset()); //
         * methodRef.selector -> eax os.writeMOV(INTSIZE, Register.EAX,
         * Register.ECX); // Clear edx os.writeXOR(Register.EDX, Register.EDX); //
         * IMT_LENGTH -> ESI os.writeMOV_Const(Register.ESI,
         * ObjectLayout.IMT_LENGTH); // selector % IMT_LENGTH -> edx
         */
        os.writeMOV_Const(ECX, selector);
        os.writeMOV_Const(EDX, imtIndex);
        // Output: EBX=objectref, ECX=selector, EDX=imtIndex

        /* objectref.TIB -> ebx */
        os.writeMOV(INTSIZE, Register.EBX, Register.EBX, ObjectLayout.TIB_SLOT
                * slotSize);
        /* boolean[] imtCollisions -> esi */
        os.writeMOV(INTSIZE, Register.ESI, Register.EBX,
                (VmArray.DATA_OFFSET + TIBLayout.IMTCOLLISIONS_INDEX)
                        * slotSize);
        /* Has collision at imt[index] ? */
        os.writeMOV(INTSIZE, Register.EAX, Register.ESI, Register.EDX, 1,
                VmArray.DATA_OFFSET * slotSize);
        os.writeTEST_AL(0xFF);
        /* Object[] imt -> esi */
        os.writeMOV(INTSIZE, Register.ESI, Register.EBX,
                (VmArray.DATA_OFFSET + TIBLayout.IMT_INDEX) * slotSize);
        /* selector -> ebx */
        os.writeMOV(INTSIZE, Register.EBX, Register.ECX);

        os.writeJCC(noCollLabel, X86Constants.JZ);

        // We have a collision
        /* imt[index] (=collisionList) -> esi */
        os.writeMOV(INTSIZE, Register.ESI, Register.ESI, Register.EDX, 4,
                VmArray.DATA_OFFSET * slotSize);
        /* collisionList.length -> ecx */
        os.writeMOV(INTSIZE, Register.ECX, Register.ESI, VmArray.LENGTH_OFFSET
                * slotSize);
        /* &collisionList[0] -> esi */
        os.writeLEA(Register.ESI, Register.ESI, VmArray.DATA_OFFSET * slotSize);

        os.setObjectRef(findSelectorLabel);

        /* collisionList[index] -> eax */
        os.writeLODSD();
        /* collisionList[index].selector == selector? */
        os.writeMOV(INTSIZE, Register.EDX, Register.EAX, context
                .getVmMethodSelectorField().getOffset());
        os.writeCMP(Register.EBX, Register.EDX);
        os.writeJCC(endLabel, X86Constants.JE);
        try {
            os.writeLOOP(findSelectorLabel);
        } catch (UnresolvedObjectRefException ex) {
            throw new CompileError(ex);
        }
        /* Force a NPE further on */
        os.writeXOR(Register.EAX, Register.EAX);
        os.writeJMP(endLabel);

        os.setObjectRef(noCollLabel);
        /* imt[index] -> eax */
        os.writeMOV(INTSIZE, Register.EAX, Register.ESI, Register.EDX, 4,
                VmArray.DATA_OFFSET * slotSize);

        os.setObjectRef(endLabel);

        /** Now invoke the method */
        helper.invokeJavaMethod(methodRef.getSignature());
        // Result is already on the stack.
    }

    /**
     * @param methodRef
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_invokespecial(org.jnode.vm.classmgr.VmConstMethodRef)
     */
    public final void visit_invokespecial(VmConstMethodRef methodRef) {
        //TODO: port to orp-style
        vstack.push(eContext);

        methodRef.resolve(loader);
        try {
            final VmMethod sm = methodRef.getResolvedVmMethod();

            dropParameters(sm, true);

            // Get method from statics table
            helper.writeGetStaticsEntry(curInstrLabel, EAX, sm);
            helper.invokeJavaMethod(methodRef.getSignature());
            // Result is already on the stack.
        } catch (ClassCastException ex) {
            BootLog.error(methodRef.getResolvedVmMethod().getClass().getName()
                    + "#" + methodRef.getName());
            throw ex;
        }
    }

    /**
     * @param methodRef
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_invokestatic(org.jnode.vm.classmgr.VmConstMethodRef)
     */
    public final void visit_invokestatic(VmConstMethodRef methodRef) {
        //TODO: port to orp-style
        vstack.push(eContext);

        methodRef.resolve(loader);
        final VmStaticMethod sm = (VmStaticMethod) methodRef
                .getResolvedVmMethod();

        dropParameters(sm, false);

        // Get static field object
        helper.writeGetStaticsEntry(curInstrLabel, EAX, sm);
        helper.invokeJavaMethod(methodRef.getSignature());
        // Result is already on the stack.
    }

    /**
     * @param methodRef
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_invokevirtual(org.jnode.vm.classmgr.VmConstMethodRef)
     */
    public final void visit_invokevirtual(VmConstMethodRef methodRef) {
        //TODO: port to orp-style
        vstack.push(eContext);

        methodRef.resolve(loader);
        final VmMethod mts = methodRef.getResolvedVmMethod();

        dropParameters(mts, true);

        if (mts.isStatic()) { throw new IncompatibleClassChangeError(
                "Static method in invokevirtual"); }
        final VmInstanceMethod method = (VmInstanceMethod) mts;
        final int tibOffset = method.getTibOffset();
        final int argSlotCount = Signature.getArgSlotCount(methodRef
                .getSignature());

        /* Get objectref -> S0 */
        os.writeMOV(INTSIZE, S0, SP, argSlotCount * slotSize);
        /* Get VMT of objectef -> S0 */
        os.writeMOV(INTSIZE, S0, S0, ObjectLayout.TIB_SLOT * slotSize);
        /* Get entry in VMT -> EAX */
        os.writeMOV(INTSIZE, EAX, S0, (VmArray.DATA_OFFSET + tibOffset)
                * slotSize);
        /* Now invoke the method */
        helper.invokeJavaMethod(methodRef.getSignature());
        // Result is already on the stack.
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ior()
     */
    public final void visit_ior() {
        //REFACTOR: parametrize the os.write operations to avoid code
        // duplication
        //IMPROVE: allow parameter permutation
        IntItem v2 = vstack.popInt();
        IntItem v1 = vstack.popInt();
        prepareForOperation(v1, v2);

        final Register r1 = v1.getRegister();
        switch (v2.getKind()) {
        case Item.Kind.REGISTER:
            os.writeOR(r1, v2.getRegister());
            break;
        case Item.Kind.LOCAL:
            os.writeOR(r1, FP, v2.getOffsetToFP());
            break;
        case Item.Kind.CONSTANT:
            os.writeOR(r1, v2.getValue());
            break;
        }
        v2.release(eContext);
        vstack.push(v1);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_irem()
     */
    public final void visit_irem() {
        //TODO: port to orp-style
        vstack.push(eContext);
        IntItem v2 = vstack.popInt();
        IntItem v1 = vstack.popInt();
        v2.release1(eContext);
        v1.release1(eContext);
        vstack.push1(IntItem.createStack());

        os.writePOP(S0); // Value2
        os.writePOP(EAX); // Value1
        os.writeCDQ();
        os.writeIDIV_EAX(S0); // EAX = EDX:EAX / S0
        os.writePUSH(EDX); // Remainder
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ireturn()
     */
    public final void visit_ireturn() {
        final IntItem val = vstack.popInt();
        if (!val.uses(EAX)) {
            requestRegister(EAX, val);
            val.loadTo(eContext, EAX);
        }
        val.release(eContext);

        visit_return();
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ishl()
     */
    public final void visit_ishl() {
        IntItem shift = vstack.popInt();
        if (shift.getKind() != Item.Kind.CONSTANT) {
            requestRegister(ECX, shift);
            shift.loadTo(eContext, ECX);
        }

        IntItem value = vstack.popInt();

        value.load(eContext);

        Register v = value.getRegister();

        if (shift.getKind() == Item.Kind.CONSTANT) {
            int offset = shift.getValue();
            os.writeSAL(v, offset);
        } else {
            os.writeSAL_CL(v);
        }
        shift.release(eContext);
        vstack.push(value);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ishr()
     */
    public final void visit_ishr() {
        IntItem shift = vstack.popInt();
        if (shift.getKind() != Item.Kind.CONSTANT) {
            requestRegister(ECX, shift);
            shift.loadTo(eContext, ECX);
        }
        IntItem value = vstack.popInt();

        value.load(eContext);

        Register v = value.getRegister();

        if (shift.getKind() == Item.Kind.CONSTANT) {
            int offset = shift.getValue();
            os.writeSAR(v, offset);
        } else {
            os.writeSAR_CL(v);
        }
        shift.release(eContext);
        vstack.push(value);
    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_istore(int)
     */
    public final void visit_istore(int index) {
        final int disp = stackFrame.getEbpOffset(index);
        vstack.loadLocal(eContext, disp); // avoid aliasing throubles
        IntItem i = vstack.popInt();
        i.loadIf(eContext, ~Item.Kind.CONSTANT);

        if (i.getKind() == Item.Kind.CONSTANT) {
            os.writeMOV_Const(FP, disp, i.getValue());
        } else {
            os.writeMOV(INTSIZE, FP, disp, i.getRegister());
            i.release(eContext);
        }
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_isub()
     */
    public final void visit_isub() {
        //REFACTOR: parametrize the os.write operations to avoid code
        // duplication
        IntItem v2 = vstack.popInt();
        IntItem v1 = vstack.popInt();
        prepareForOperation(v1, v2);

        final Register r1 = v1.getRegister();
        switch (v2.getKind()) {
        case Item.Kind.REGISTER:
            os.writeSUB(r1, v2.getRegister());
            break;
        case Item.Kind.LOCAL:
            os.writeSUB(r1, FP, v2.getOffsetToFP());
            break;
        case Item.Kind.CONSTANT:
            final int val = v2.getValue();
            os.writeSUB(r1, val);
            break;
        }
        v2.release(eContext);
        vstack.push(v1);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iushr()
     */
    public final void visit_iushr() {
        IntItem shift = vstack.popInt();

        if (shift.getKind() != Item.Kind.CONSTANT) {
            requestRegister(ECX, shift);
            shift.loadTo(eContext, ECX);
        }

        IntItem value = vstack.popInt();

        value.load(eContext);

        Register v = value.getRegister();

        if (shift.getKind() == Item.Kind.CONSTANT) {
            int offset = shift.getValue();
            os.writeSHR(v, offset);
        } else {
            os.writeSHR_CL(v);
        }
        shift.release(eContext);
        vstack.push(value);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ixor()
     */
    public final void visit_ixor() {
        //REFACTOR: parametrize the os.write operations to avoid code
        // duplication
        //IMPROVE: allow parameter permutation
        IntItem v2 = vstack.popInt();
        IntItem v1 = vstack.popInt();
        prepareForOperation(v1, v2);

        final Register r1 = v1.getRegister();
        switch (v2.getKind()) {
        case Item.Kind.REGISTER:
            os.writeXOR(r1, v2.getRegister());
            break;
        case Item.Kind.LOCAL:
            os.writeXOR(r1, FP, v2.getOffsetToFP());
            break;
        case Item.Kind.CONSTANT:
            os.writeXOR(r1, v2.getValue());
            break;
        }
        v2.release(eContext);
        vstack.push(v1);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_jsr(int)
     */
    public final void visit_jsr(int address) {
        os.writeCALL(helper.getInstrLabel(address));
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_l2d()
     */
    public final void visit_l2d() {
        //TODO: port to orp-style
        vstack.push(eContext);
        Item v = vstack.pop(JvmType.LONG);
        v.release1(eContext);
        vstack.push1(DoubleItem.createStack());

        os.writeFILD64(SP, 0);
        os.writeFSTP64(SP, 0);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_l2f()
     */
    public final void visit_l2f() {
        //TODO: port to orp-style
        vstack.push(eContext);
        Item v = vstack.pop(JvmType.LONG);
        v.release1(eContext);
        vstack.push1(FloatItem.createStack());

        os.writeFILD64(SP, 0);
        os.writeLEA(SP, SP, 4);
        os.writeFSTP32(SP, 0);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_l2i()
     */
    public final void visit_l2i() {
        final LongItem v = vstack.popLong();
        if (v.isConstant()) {
            vstack.push(IntItem.createConst((int) v.getValue()));
        } else {
            final X86RegisterPool pool = eContext.getPool();
            v.load(eContext);
            final Register lsb = v.getLsbRegister();
            v.release(eContext);
            pool.request(lsb);
            final IntItem result = IntItem.createReg(lsb);
            pool.transferOwnerTo(lsb, result);
            vstack.push(result);
        }
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ladd()
     */
    public final void visit_ladd() {
        final LongItem v2 = vstack.popLong();
        final LongItem v1 = vstack.popLong();
        prepareForOperation(v1, v2);

        final Register r1_lsb = v1.getLsbRegister();
        final Register r1_msb = v1.getMsbRegister();
        switch (v2.getKind()) {
        case Item.Kind.REGISTER:
            os.writeADD(r1_lsb, v2.getLsbRegister());
            os.writeADC(r1_msb, v2.getMsbRegister());
            break;
        case Item.Kind.LOCAL:
            os.writeADD(r1_lsb, FP, v2.getLsbOffsetToFP());
            os.writeADC(r1_msb, FP, v2.getMsbOffsetToFP());
            break;
        case Item.Kind.CONSTANT:
            os.writeADD(r1_lsb, v2.getLsbValue());
            os.writeADC(r1_msb, v2.getMsbValue());
            break;
        }
        v2.release(eContext);
        vstack.push(v1);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_laload()
     */
    public final void visit_laload() {
        visit_dwaload(JvmType.LONG);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_land()
     */
    public final void visit_land() {
        final LongItem v2 = vstack.popLong();
        final LongItem v1 = vstack.popLong();
        prepareForOperation(v1, v2);

        final Register r1_lsb = v1.getLsbRegister();
        final Register r1_msb = v1.getMsbRegister();
        switch (v2.getKind()) {
        case Item.Kind.REGISTER:
            os.writeAND(r1_lsb, v2.getLsbRegister());
            os.writeAND(r1_msb, v2.getMsbRegister());
            break;
        case Item.Kind.LOCAL:
            os.writeAND(r1_lsb, FP, v2.getLsbOffsetToFP());
            os.writeAND(r1_msb, FP, v2.getMsbOffsetToFP());
            break;
        case Item.Kind.CONSTANT:
            os.writeAND(r1_lsb, v2.getLsbValue());
            os.writeAND(r1_msb, v2.getMsbValue());
            break;
        }
        v2.release(eContext);
        vstack.push(v1);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lastore()
     */
    public final void visit_lastore() {
        visit_dwastore(JvmType.LONG);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lcmp()
     */
    public final void visit_lcmp() {
        final LongItem v2 = vstack.popLong();
        final LongItem v1 = vstack.popLong();

        // Load
        v2.load(eContext);
        final Register v2_lsb = v2.getLsbRegister();
        final Register v2_msb = v2.getMsbRegister();
        v1.load(eContext);
        final Register v1_lsb = v1.getLsbRegister();
        final Register v1_msb = v1.getMsbRegister();

        // Claim result reg
        final IntItem result = (IntItem) requestWordRegister(JvmType.INT);
        final Register resr = result.getRegister();

        final Label ltLabel = new Label(curInstrLabel + "lt");
        final Label endLabel = new Label(curInstrLabel + "end");

        // Calculate
        os.writeXOR(resr, resr);
        os.writeSUB(v1_lsb, v2_lsb);
        os.writeSBB(v1_msb, v2_msb);
        os.writeJCC(ltLabel, X86Constants.JL); // JL
        os.writeOR(v1_lsb, v1_msb);
        os.writeJCC(endLabel, X86Constants.JZ); // value1 == value2
        /** GT */
        os.writeINC(resr);
        os.writeJMP(endLabel);
        /** LT */
        os.setObjectRef(ltLabel);
        os.writeDEC(resr);
        os.setObjectRef(endLabel);

        // Push
        vstack.push(result);

        // Release
        v1.release(eContext);
        v2.release(eContext);
    }

    /**
     * @param v
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lconst(long)
     */
    public final void visit_lconst(long v) {
        vstack.push(LongItem.createConst(v));
    }

    /**
     * @param value
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ldc(VmConstClass)
     */
    public final void visit_ldc(VmConstClass value) {
        throw new Error("Not implemented yet");
    }

    /**
     * @param value
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ldc(VmConstString)
     */
    public final void visit_ldc(VmConstString value) {
        vstack.push(RefItem.createConst(value));
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ldiv()
     */
    public final void visit_ldiv() {
        //TODO: port to orp-style
        vstack.push(eContext);
        final LongItem v2 = vstack.popLong();
        final LongItem v1 = vstack.popLong();
        v2.release1(eContext);
        v1.release1(eContext);

        helper.invokeJavaMethod(context.getLdivMethod());
    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lload(int)
     */
    public final void visit_lload(int index) {
        vstack.push(LongItem.createLocal(stackFrame.getWideEbpOffset(index)));
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lmul()
     */
    public final void visit_lmul() {
        //TODO: port to orp-style
        vstack.push(eContext);
        final LongItem v2 = vstack.popLong();
        final LongItem v1 = vstack.popLong();
        v2.release1(eContext);
        v1.release1(eContext);

        writePOP64(Register.EBX, Register.ECX); // Value 2
        final Register v2_lsb = Register.EBX;
        final Register v2_msb = Register.ECX;
        writePOP64(Register.ESI, Register.EDI); // Value 1
        final Register v1_lsb = Register.ESI;
        final Register v1_msb = Register.EDI;

        final Label tmp1 = new Label(curInstrLabel + "$tmp1");
        final Label tmp2 = new Label(curInstrLabel + "$tmp2");

        os.writeMOV(INTSIZE, EAX, v1_msb); // hi2
        os.writeOR(EAX, v2_msb); // hi1 | hi2
        os.writeJCC(tmp1, X86Constants.JNZ);
        os.writeMOV(INTSIZE, EAX, v1_lsb); // lo2
        os.writeMUL_EAX(v2_lsb); // lo1*lo2
        os.writeJMP(tmp2);
        os.setObjectRef(tmp1);
        os.writeMOV(INTSIZE, EAX, v1_lsb); // lo2
        os.writeMUL_EAX(v2_msb); // hi1*lo2
        os.writeMOV(INTSIZE, v2_msb, EAX);
        os.writeMOV(INTSIZE, EAX, v1_msb); // hi2
        os.writeMUL_EAX(v2_lsb); // hi2*lo1
        os.writeADD(v2_msb, EAX); // hi2*lo1 + hi1*lo2
        os.writeMOV(INTSIZE, EAX, v1_lsb); // lo2
        os.writeMUL_EAX(v2_lsb); // lo1*lo2
        os.writeADD(EDX, v2_msb); // hi2*lo1 + hi1*lo2 +
        // hi(lo1*lo2)
        os.setObjectRef(tmp2);
        // Reload the statics table, since it was destroyed here
        helper.writeLoadSTATICS(curInstrLabel, "lmul", false);

        // Push
        final LongItem result = (LongItem) requestDoubleWordRegisters(
                JvmType.LONG, EAX, EDX);
        vstack.push(result);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lneg()
     */
    public final void visit_lneg() {
        final LongItem v = vstack.popLong();

        if (v.isConstant()) {
            vstack.push(LongItem.createConst(-v.getValue()));
        } else {
            // Load val
            v.load(eContext);
            final Register lsb = v.getLsbRegister();
            final Register msb = v.getMsbRegister();

            // Calculate
            os.writeNEG(msb); // msb := -msb
            os.writeNEG(lsb); // lsb := -lsb
            os.writeSBB(msb, 0); // high += borrow

            // Push
            vstack.push(v);
        }
    }

    /**
     * @param defAddress
     * @param matchValues
     * @param addresses
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lookupswitch(int, int[],
     *      int[])
     */
    public final void visit_lookupswitch(int defAddress, int[] matchValues,
            int[] addresses) {
        final int n = matchValues.length;
        //BootLog.debug("lookupswitch length=" + n);

        IntItem key = vstack.popInt();
        key.load(eContext);
        Register r = key.getRegister();
        // Conservative assumption, flush stack
        vstack.push(eContext);
        key.release(eContext);

        for (int i = 0; i < n; i++) {
            os.writeCMP_Const(r, matchValues[ i]);
            os.writeJCC(helper.getInstrLabel(addresses[ i]), X86Constants.JE); // JE
        }
        os.writeJMP(helper.getInstrLabel(defAddress));

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lor()
     */
    public final void visit_lor() {
        final LongItem v2 = vstack.popLong();
        final LongItem v1 = vstack.popLong();
        prepareForOperation(v1, v2);

        final Register r1_lsb = v1.getLsbRegister();
        final Register r1_msb = v1.getMsbRegister();
        switch (v2.getKind()) {
        case Item.Kind.REGISTER:
            os.writeOR(r1_lsb, v2.getLsbRegister());
            os.writeOR(r1_msb, v2.getMsbRegister());
            break;
        case Item.Kind.LOCAL:
            os.writeOR(r1_lsb, FP, v2.getLsbOffsetToFP());
            os.writeOR(r1_msb, FP, v2.getMsbOffsetToFP());
            break;
        case Item.Kind.CONSTANT:
            os.writeOR(r1_lsb, v2.getLsbValue());
            os.writeOR(r1_msb, v2.getMsbValue());
            break;
        }
        v2.release(eContext);
        vstack.push(v1);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lrem()
     */
    public final void visit_lrem() {
        //TODO: port to orp-style
        vstack.push(eContext);
        Item v2 = vstack.pop(JvmType.LONG);
        Item v1 = vstack.pop(JvmType.LONG);
        v2.release1(eContext);
        v1.release1(eContext);

        helper.invokeJavaMethod(context.getLremMethod());
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lreturn()
     */
    public final void visit_lreturn() {
        final LongItem val = vstack.popLong();
        if (!(val.uses(EAX) && val.uses(EDX))) {
            if (val.uses(EAX) || val.uses(EDX)) {
                val.push(eContext);
            }
            requestRegister(EAX, val);
            requestRegister(EDX, val);
            val.loadTo(eContext, EAX, EDX);
        }
        val.release(eContext);

        visit_return();
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lshl()
     */
    public final void visit_lshl() {
        final IntItem v2 = vstack.popInt();
        final LongItem v1 = vstack.popLong();

        requestRegister(ECX, v2);
        v2.loadTo(eContext, ECX);
        v1.load(eContext);
        final Register v1_lsb = v1.getLsbRegister();
        final Register v1_msb = v1.getMsbRegister();

        os.writeAND(ECX, 63);
        os.writeCMP_Const(ECX, 32);
        final Label gt32Label = new Label(curInstrLabel + "gt32");
        final Label endLabel = new Label(curInstrLabel + "end");
        os.writeJCC(gt32Label, X86Constants.JAE); // JAE
        /** ECX < 32 */
        os.writeSHLD_CL(v1_msb, v1_lsb);
        os.writeSHL_CL(v1_lsb);
        os.writeJMP(endLabel);
        /** ECX >= 32 */
        os.setObjectRef(gt32Label);
        os.writeMOV(INTSIZE, v1_msb, v1_lsb);
        os.writeXOR(v1_lsb, v1_lsb);
        os.writeSHL_CL(v1_msb);
        os.setObjectRef(endLabel);

        // Release
        v2.release(eContext);
        vstack.push(v1);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lshr()
     */
    public final void visit_lshr() {
        final IntItem cnt = vstack.popInt();
        final LongItem val = vstack.popLong();
        final X86RegisterPool pool = eContext.getPool();

        // Get cnt into ECX
        if (!cnt.uses(ECX)) {
            val.spillIfUsing(eContext, ECX);
            requestRegister(ECX, cnt);
            cnt.loadTo(eContext, ECX);
        }

        // Load val
        val.load(eContext);
        final Register lsb = val.getLsbRegister();
        final Register msb = val.getMsbRegister();

        // Calculate
        os.writeAND(ECX, 63);
        os.writeCMP_Const(ECX, 32);
        final Label gt32Label = new Label(curInstrLabel + "gt32");
        final Label endLabel = new Label(curInstrLabel + "end");
        os.writeJCC(gt32Label, X86Constants.JAE); // JAE
        /** ECX < 32 */
        os.writeSHRD_CL(lsb, msb);
        os.writeSAR_CL(msb);
        os.writeJMP(endLabel);
        /** ECX >= 32 */
        os.setObjectRef(gt32Label);
        os.writeMOV(INTSIZE, lsb, msb);
        os.writeSAR(msb, 31);
        os.writeSAR_CL(lsb);
        os.setObjectRef(endLabel);

        // Push
        final LongItem result = LongItem.createReg(lsb, msb);
        pool.transferOwnerTo(lsb, result);
        pool.transferOwnerTo(msb, result);
        vstack.push(result);

        // Release
        cnt.release(eContext);
    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lstore(int)
     */
    public final void visit_lstore(int index) {
        final LongItem v = vstack.popLong();
        final int ebpOfs = stackFrame.getWideEbpOffset(index);

        // pin down other references to this local
        vstack.loadLocal(eContext, ebpOfs);

        // Load the value
        v.load(eContext);

        // Copy to local
        os.writeMOV(INTSIZE, FP, ebpOfs, v.getLsbRegister());
        os.writeMOV(INTSIZE, FP, ebpOfs + 4, v.getMsbRegister());

        // Release
        v.release(eContext);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lsub()
     */
    public final void visit_lsub() {
        final LongItem v2 = vstack.popLong();
        final LongItem v1 = vstack.popLong();
        prepareForOperation(v1, v2);

        final Register r1_lsb = v1.getLsbRegister();
        final Register r1_msb = v1.getMsbRegister();
        switch (v2.getKind()) {
        case Item.Kind.REGISTER:
            os.writeSUB(r1_lsb, v2.getLsbRegister());
            os.writeSBB(r1_msb, v2.getMsbRegister());
            break;
        case Item.Kind.LOCAL:
            os.writeSUB(r1_lsb, FP, v2.getLsbOffsetToFP());
            os.writeSBB(r1_msb, FP, v2.getMsbOffsetToFP());
            break;
        case Item.Kind.CONSTANT:
            os.writeSUB(r1_lsb, v2.getLsbValue());
            os.writeSBB(r1_msb, v2.getMsbValue());
            break;
        }
        v2.release(eContext);
        vstack.push(v1);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lushr()
     */
    public final void visit_lushr() {
        final IntItem cnt = vstack.popInt();
        final LongItem val = vstack.popLong();
        final X86RegisterPool pool = eContext.getPool();

        // Get cnt into ECX
        if (!cnt.uses(ECX)) {
            val.spillIfUsing(eContext, ECX);
            requestRegister(ECX, cnt);
            cnt.loadTo(eContext, ECX);
        }

        // Load val
        val.load(eContext);
        final Register lsb = val.getLsbRegister();
        final Register msb = val.getMsbRegister();

        // Calculate
        os.writeAND(ECX, 63);
        os.writeCMP_Const(ECX, 32);
        final Label gt32Label = new Label(curInstrLabel + "gt32");
        final Label endLabel = new Label(curInstrLabel + "end");
        os.writeJCC(gt32Label, X86Constants.JAE); // JAE
        /** ECX < 32 */
        os.writeSHRD_CL(lsb, msb);
        os.writeSHR_CL(msb);
        os.writeJMP(endLabel);
        /** ECX >= 32 */
        os.setObjectRef(gt32Label);
        os.writeMOV(INTSIZE, lsb, msb);
        os.writeXOR(msb, msb);
        os.writeSHR_CL(lsb);
        os.setObjectRef(endLabel);

        // Push
        final LongItem result = LongItem.createReg(lsb, msb);
        pool.transferOwnerTo(lsb, result);
        pool.transferOwnerTo(msb, result);
        vstack.push(result);

        // Release
        cnt.release(eContext);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lxor()
     */
    public final void visit_lxor() {
        final LongItem v2 = vstack.popLong();
        final LongItem v1 = vstack.popLong();
        prepareForOperation(v1, v2);

        final Register r1_lsb = v1.getLsbRegister();
        final Register r1_msb = v1.getMsbRegister();
        switch (v2.getKind()) {
        case Item.Kind.REGISTER:
            os.writeXOR(r1_lsb, v2.getLsbRegister());
            os.writeXOR(r1_msb, v2.getMsbRegister());
            break;
        case Item.Kind.LOCAL:
            os.writeXOR(r1_lsb, FP, v2.getLsbOffsetToFP());
            os.writeXOR(r1_msb, FP, v2.getMsbOffsetToFP());
            break;
        case Item.Kind.CONSTANT:
            os.writeXOR(r1_lsb, v2.getLsbValue());
            os.writeXOR(r1_msb, v2.getMsbValue());
            break;
        }
        v2.release(eContext);
        vstack.push(v1);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_monitorenter()
     */
    public final void visit_monitorenter() {
        vstack.push(eContext);
        RefItem v = vstack.popRef();
        v.release1(eContext);

        // Objectref is already on the stack
        helper.invokeJavaMethod(context.getMonitorEnterMethod());
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_monitorexit()
     */
    public final void visit_monitorexit() {
        vstack.push(eContext);
        RefItem v = vstack.popRef();
        v.release1(eContext);

        // Objectref is already on the stack
        helper.invokeJavaMethod(context.getMonitorExitMethod());
    }

    /**
     * @param clazz
     * @param dimensions
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_multianewarray(VmConstClass,
     *      int)
     */
    public final void visit_multianewarray(VmConstClass clazz, int dimensions) {
        // flush all vstack items to the stack
        // all registers are freed
        vstack.push(eContext);

        // Create the dimensions array
        os.writePUSH(10); /* type=int */
        os.writePUSH(dimensions); /* elements */
        helper.invokeJavaMethod(context.getAllocPrimitiveArrayMethod());
        final RefItem dims = vstack.popRef();
        final Register dimsr = dims.getRegister();
        // Dimension array is now in S1

        // Pop all dimensions (note the reverse order that allocMultiArray
        // expects)
        for (int i = 0; i < dimensions; i++) {
            final int ofs = (VmArray.DATA_OFFSET + i) * slotSize;
            final IntItem v = vstack.popInt();
            v.release1(eContext);
            os.writePOP(dimsr, ofs);
        }

        // Allocate tmp register
        final Register classr = requestRegister(JvmType.REFERENCE);

        // Resolve the array class
        writeResolveAndLoadClassToReg(clazz, classr);

        // Now call the multianewarrayhelper
        os.writePUSH(classr); // array-class
        os.writePUSH(dimsr); // dimensions[]
        helper.invokeJavaMethod(context.getAllocMultiArrayMethod());
        // Result is now on the vstack

        // Release
        dims.release(eContext);
        releaseRegister(classr);
    }

    /**
     * @param classRef
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_new(org.jnode.vm.classmgr.VmConstClass)
     */
    public final void visit_new(VmConstClass classRef) {
        // Push all
        vstack.push(eContext);

        // Allocate tmp register
        final Register classr = requestRegister(JvmType.REFERENCE);

        writeResolveAndLoadClassToReg(classRef, classr);
        /* Setup a call to SoftByteCodes.allocObject */
        os.writePUSH(classr); /* vmClass */
        os.writePUSH(-1); /* Size */
        helper.invokeJavaMethod(context.getAllocObjectMethod());
        // Result is already on the vstack

        // Release
        releaseRegister(classr);
    }

    /**
     * @param type
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_newarray(int)
     */
    public final void visit_newarray(int type) {
        // Load count
        final IntItem count = vstack.popInt();
        count.loadIf(eContext, Item.Kind.STACK);

        // flush stack, result also on stack
        vstack.push(eContext);

        // Setup a call to SoftByteCodes.allocArray
        os.writePUSH(type); /* type */
        count.push(eContext); /* count */
        count.release1(eContext); // release and remove parameter from stack

        helper.invokeJavaMethod(context.getAllocPrimitiveArrayMethod());
        // Result is already on the vstack
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_nop()
     */
    public final void visit_nop() {
        // Nothing
        os.writeNOP();
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_pop()
     */
    public final void visit_pop() {
        generic_pop(4);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_pop2()
     */
    public final void visit_pop2() {
        generic_pop(8);
    }

    /**
     * @param fieldRef
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_putfield(org.jnode.vm.classmgr.VmConstFieldRef)
     */
    public final void visit_putfield(VmConstFieldRef fieldRef) {
        fieldRef.resolve(loader);
        final VmField field = fieldRef.getResolvedVmField();
        if (field.isStatic()) { throw new IncompatibleClassChangeError(
                "getfield called on static field " + fieldRef.getName()); }
        final VmInstanceField inf = (VmInstanceField) field;
        final int offset = inf.getOffset();
        final boolean wide = fieldRef.isWide();

        // Get operands
        final Item val = vstack.pop();
        assertCondition(val.getCategory() == ((wide) ? 2 : 1),
                "category mismatch");
        final RefItem ref = vstack.popRef();

        // Load value & ref
        val.load(eContext);
        ref.load(eContext);
        final Register refr = ref.getRegister();

        if (!wide) {
            final WordItem wval = (WordItem) val;
            final Register valr = wval.getRegister();

            // Store field
            os.writeMOV(INTSIZE, refr, offset, valr);
            // Writebarrier
            if (helper.needsWriteBarrier()) {
                final Register tmp = requestRegister(JvmType.INT);
                helper.writePutfieldWriteBarrier(inf, refr, valr, tmp);
                releaseRegister(tmp);
            }
        } else {
            final DoubleWordItem dval = (DoubleWordItem) val;
            /** Msb */
            os.writeMOV(INTSIZE, refr, offset + 4, dval.getMsbRegister());
            /** Lsb */
            os.writeMOV(INTSIZE, refr, offset, dval.getLsbRegister());
        }

        // Release
        val.release(eContext);
        ref.release(eContext);
    }

    /**
     * @param fieldRef
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_putstatic(org.jnode.vm.classmgr.VmConstFieldRef)
     */
    public final void visit_putstatic(VmConstFieldRef fieldRef) {
        fieldRef.resolve(loader);
        final VmStaticField sf = (VmStaticField) fieldRef.getResolvedVmField();

        // Initialize class if needed
        if (!sf.getDeclaringClass().isInitialized()) {
            final Register tmp = requestRegister(JvmType.INT);
            writeInitializeClass(fieldRef, tmp);
            releaseRegister(tmp);
        }

        // Get value
        final Item val = vstack.pop();
        val.load(eContext);

        // Put static field
        if (!fieldRef.isWide()) {
            final WordItem wval = (WordItem) val;
            final Register valr = wval.getRegister();

            helper.writePutStaticsEntry(curInstrLabel, valr, sf);
            if (helper.needsWriteBarrier()) {
                final Register tmp = requestRegister(JvmType.INT);
                helper.writePutstaticWriteBarrier(sf, valr, tmp);
                releaseRegister(tmp);
            }
        } else {
            final DoubleWordItem dval = (DoubleWordItem) val;
            helper.writePutStaticsEntry64(curInstrLabel, dval.getLsbRegister(),
                    dval.getMsbRegister(), sf);
        }

        // Release
        val.release(eContext);
    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ret(int)
     */
    public final void visit_ret(int index) {
        // Calc EBP offset
        final int ebpOfs = stackFrame.getEbpOffset(index);

        // Claim tmp register
        final Register tmp = requestRegister(JvmType.INT);

        // Load ret & jmp
        os.writeMOV(INTSIZE, tmp, FP, ebpOfs);
        os.writeJMP(tmp);

        // Release
        releaseRegister(tmp);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_return()
     */
    public final void visit_return() {
        stackFrame.emitReturn();
        assertCondition(vstack.isEmpty(), "vstack should be empty; it is ",
                vstack);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_saload()
     */
    public final void visit_saload() {
        IntItem idx = vstack.popInt();
        RefItem ref = vstack.popRef();
        idx.loadIf(eContext, ~Item.Kind.CONSTANT);
        ref.load(eContext);
        Register r = ref.getRegister();
        checkBounds(ref, idx);
        if (idx.isConstant()) {
            final int i = idx.getValue();
            os.writeMOV(WORDSIZE, r, r, i + VmArray.DATA_OFFSET * 4);
        } else {
            os.writeMOV(WORDSIZE, r, r, idx.getRegister(), 2,
                    VmArray.DATA_OFFSET * 4);
            idx.release(eContext);
        }
        os.writeMOVSX(r, r, WORDSIZE);
        final IntItem result = IntItem.createReg(r);
        eContext.getPool().transferOwnerTo(r, result);
        vstack.push(result);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_sastore()
     */
    public final void visit_sastore() {
        IntItem val = vstack.popInt();
        IntItem idx = vstack.popInt();
        RefItem ref = vstack.popRef();

        //IMPROVE: optimize case with const value
        val.load(eContext);
        idx.loadIf(eContext, ~Item.Kind.CONSTANT);
        ref.load(eContext);
        final Register r = ref.getRegister();
        final Register v = val.getRegister();

        checkBounds(ref, idx);
        if (idx.getKind() == Item.Kind.CONSTANT) {
            final int i = idx.getValue();
            os.writeMOV(WORDSIZE, r, i + VmArray.DATA_OFFSET * 4, v);
        } else {
            final Register i = idx.getRegister();
            os.writeMOV(WORDSIZE, r, i, 2, VmArray.DATA_OFFSET * 4, v);
        }
        val.release(eContext);
        idx.release(eContext);
        ref.release(eContext);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_swap()
     */
    public final void visit_swap() {
        final Item v1 = vstack.pop();
        final Item v2 = vstack.pop();
        assertCondition((v1.getCategory() == 1) && (v2.getCategory() == 1),
                "category mismatch");

        final boolean v1_load = (v1.isStack() || v1.isFPUStack());
        final boolean v2_load = (v2.isStack() || v2.isFPUStack());
        if (v1_load || v2_load) {
            // at least one element the stack: must be popped to be inverted
            // (inverting only on vstack not enough)
            v1.load(eContext);
            v2.load(eContext);
        }
        vstack.push(v1);
        vstack.push(v2);
    }

    /**
     * @param defAddress
     * @param lowValue
     * @param highValue
     * @param addresses
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_tableswitch(int, int,
     *      int, int[])
     */
    public final void visit_tableswitch(int defAddress, int lowValue,
            int highValue, int[] addresses) {
        //IMPROVE: check Jaos implementation
        final IntItem val = vstack.popInt();
        val.load(eContext);
        final Register valr = val.getRegister();
        vstack.push(eContext);

        final int n = addresses.length;
        //TODO: port optimized version of L1
        // Space wasting, but simple implementation
        for (int i = 0; i < n; i++) {
            os.writeCMP_Const(valr, lowValue + i);
            os.writeJCC(helper.getInstrLabel(addresses[ i]), X86Constants.JE); // JE
        }
        os.writeJMP(helper.getInstrLabel(defAddress));

        val.release(eContext);
    }

    /**
     * Insert a yieldpoint into the code
     */
    public final void yieldPoint() {
        helper.writeYieldPoint(curInstrLabel);
    }

    private final void assertCondition(boolean cond, String message) {
        if (!cond)
                throw new Error("assert failed at addresss " + curAddress
                        + ": " + message);
    }

    private final void assertCondition(boolean cond, String message,
            Object param) {
        if (!cond)
                throw new Error("assert failed at addresss " + curAddress
                        + ": " + message + param);
    }

    /**
     * Emit code to validate an index of a given array
     * 
     * @param ref
     * @param index
     */
    private final void checkBounds(RefItem ref, IntItem index) {
        final Label ok = new Label(curInstrLabel + "$$cbok");
        // CMP length, index
        assertCondition(ref.isRegister(), "ref must be in a register");
        final Register r = ref.getRegister();
        if (index.isConstant()) {
            //TODO: implement CMP dist[reg], imm32
            // final int val = ((IntConstant)index.getConstant()).getValue();
            index.load(eContext);
            os.writeCMP(r, VmArray.LENGTH_OFFSET * slotSize, index
                    .getRegister());
        } else {
            os.writeCMP(r, VmArray.LENGTH_OFFSET * slotSize, index
                    .getRegister());
        }
        os.writeJCC(ok, X86Constants.JA);
        // Signal ArrayIndexOutOfBounds
        os.writeINT(5);
        os.setObjectRef(ok);
    }

    /**
     * Emit code to validate an index of a given array
     * 
     * @param arrayRef
     * @param index
     */
    // TODO REFACTOR: remove this method
    private final void checkBounds(Register arrayRef, Register index) {
        final Label ok = new Label(curInstrLabel + "$$cbok");
        // CMP length, index
        os.writeCMP(arrayRef, VmArray.LENGTH_OFFSET * slotSize, index);
        os.writeJCC(ok, X86Constants.JA);
        // Signal ArrayIndexOutOfBounds
        os.writeINT(5);
        os.setObjectRef(ok);
    }

    private final void dropParameters(VmMethod method, boolean hasSelf) {
        //TODO: check parameter types
        final int count = method.getNoArguments() + ((hasSelf) ? 1 : 0);
        for (int i = 0; (!vstack.isEmpty() && (i < count)); i++) {
            Item v = vstack.pop();
            v.release1(eContext);
        }
    }

    /**
     * Helper for various pop bytecodes.
     * 
     * @param size
     */
    private final void generic_pop(int size) {
        final Item v = vstack.pop();
        assertCondition(v.getCategory() == (size >> 2), "category mismatch");
        if (v.getKind() == Item.Kind.STACK) {
            // sanity check
            if (VirtualStack.checkOperandStack) {
                vstack.operandStack.pop(v);
            }
            os.writeLEA(SP, SP, size);
        }
        v.release(eContext);
    }

    /**
     * Emit the core of the instanceof code.
     * 
     * @param objectr
     *            Register containing the object reference
     * @param trueLabel
     *            Where to jump for a true result. A false result will continue
     *            directly after this method
     */
    private final void instanceOf(Register objectr, Label trueLabel) {
        //TODO: port to orp-style
        final Label loopLabel = new Label(this.curInstrLabel + "loop");
        final Label notInstanceOfLabel = new Label(this.curInstrLabel
                + "notInstanceOf");

        /* Is objectref null? */
        os.writeTEST(objectr, objectr);
        os.writeJCC(notInstanceOfLabel, X86Constants.JZ);
        /* vmType -> edx */
        os.writeMOV(INTSIZE, EDX, EAX);
        /* TIB -> ESI */
        os.writeMOV(INTSIZE, Register.ESI, objectr, ObjectLayout.TIB_SLOT
                * slotSize);
        /* SuperClassesArray -> ESI */
        os
                .writeMOV(INTSIZE, Register.ESI, Register.ESI,
                        (VmArray.DATA_OFFSET + TIBLayout.SUPERCLASSES_INDEX)
                                * slotSize);
        /* SuperClassesArray.length -> ECX */
        os.writeMOV(INTSIZE, ECX, Register.ESI, VmArray.LENGTH_OFFSET
                * slotSize);
        /* &superClassesArray[0] -> esi */
        os.writeLEA(Register.ESI, Register.ESI, VmArray.DATA_OFFSET * slotSize);

        os.setObjectRef(loopLabel);
        /* superClassesArray[index++] -> eax */
        os.writeLODSD();
        /* Is equal? */
        os.writeCMP(EAX, EDX);
        os.writeJCC(trueLabel, X86Constants.JE);
        try {
            os.writeLOOP(loopLabel);
        } catch (UnresolvedObjectRefException ex) {
            throw new CompileError(ex);
        }
        os.setObjectRef(notInstanceOfLabel);
    }

    private final void prepareForOperation(Item destAndSource, Item source) {
        // WARNING: source was on top of the virtual stack (thus higher than
        // destAndSource)
        // x86 can only deal with one complex argument
        // destAndSource must be a register
        source.loadIf(eContext, (Item.Kind.STACK | Item.Kind.FPUSTACK));
        destAndSource.load(eContext);
    }

    /**
     * Release a register.
     * 
     * @param reg
     */
    private final void releaseRegister(Register reg) {
        final X86RegisterPool pool = eContext.getPool();
        pool.release(reg);
    }

    /**
     * Request two register for a 8-byte item.
     */
    private final DoubleWordItem requestDoubleWordRegisters(int jvmType) {
        final X86RegisterPool pool = eContext.getPool();
        final Register lsb = requestRegister(JvmType.INT);
        final Register msb = requestRegister(JvmType.INT);
        final DoubleWordItem result = DoubleWordItem.createReg(jvmType, lsb,
                msb);
        pool.transferOwnerTo(lsb, result);
        pool.transferOwnerTo(msb, result);
        return result;
    }

    /**
     * Request two register for a 8-byte item.
     */
    private final DoubleWordItem requestDoubleWordRegisters(int jvmType,
            Register lsb, Register msb) {
        final X86RegisterPool pool = eContext.getPool();
        requestRegister(lsb);
        requestRegister(msb);
        final DoubleWordItem result = DoubleWordItem.createReg(jvmType, lsb,
                msb);
        pool.transferOwnerTo(lsb, result);
        pool.transferOwnerTo(msb, result);
        return result;
    }

    /**
     * Request a register of a given type, not tied to an item. Make sure to
     * release the register afterwards.
     */
    private final Register requestRegister(int type) {
        final X86RegisterPool pool = eContext.getPool();
        Register r = pool.request(type);
        if (r == null) {
            vstack.push(eContext);
            r = pool.request(type);
        }
        assertCondition(r != null, "failed to request register");
        return r;
    }

    /**
     * Request a register for calcuation, not tied to an item. Make sure to
     * release the register afterwards.
     * 
     * @param reg
     */
    private final void requestRegister(Register reg) {
        final X86RegisterPool pool = eContext.getPool();
        if (!pool.isFree(reg)) {
            final Item i = (Item) pool.getOwner(reg);
            i.spill(eContext, reg);
            assertCondition(pool.isFree(reg),
                    "register is not free after spill");
        }
        pool.request(reg);
    }

    /**
     * reserve a register for an item. The item is not loaded with the register.
     * The register is spilled if another item holds it.
     * 
     * @param reg
     *            the register to reserve
     * @param it
     *            the item requiring the register
     */
    private final void requestRegister(Register reg, Item it) {
        final X86RegisterPool pool = eContext.getPool();

        // check item doesn't already use register
        if (!it.uses(reg)) {
            if (!pool.isFree(reg)) {
                //TODO: spill register; make sure that the stack items
                // and floating items are handled correctly
                final Item i = (Item) pool.getOwner(reg);
                i.spill(eContext, reg);
                assertCondition(pool.isFree(reg),
                        "register is not free after spill");
            }
            pool.request(reg, it);
        }
    }

    /**
     * Request one register for a 4-byte item.
     */
    private final WordItem requestWordRegister(int jvmType) {
        final X86RegisterPool pool = eContext.getPool();
        final Register reg = requestRegister(JvmType.INT);
        final WordItem result = WordItem.createReg(jvmType, reg);
        pool.transferOwnerTo(reg, result);
        return result;
    }

    /**
     * @see #visit_daload()
     * @see #visit_laload()
     */
    private final void visit_dwaload(int type) {
        final IntItem idx = vstack.popInt();
        final RefItem ref = vstack.popRef();

        idx.load(eContext);
        ref.load(eContext);
        final Register idxr = idx.getRegister();
        final Register refr = ref.getRegister();
        checkBounds(refr, idxr);

        final DoubleWordItem result = requestDoubleWordRegisters(type);
        os.writeLEA(refr, refr, idxr, 8, VmArray.DATA_OFFSET * 4);
        os.writeMOV(INTSIZE, result.getLsbRegister(), refr, 0);
        os.writeMOV(INTSIZE, result.getMsbRegister(), refr, 4);

        idx.release(eContext);
        ref.release(eContext);

        vstack.push(result);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lastore()
     */
    private final void visit_dwastore(int type) {
        final DoubleWordItem val = (DoubleWordItem) vstack.pop(type);
        final IntItem idx = vstack.popInt();
        final RefItem ref = vstack.popRef();

        val.load(eContext);
        idx.load(eContext);
        ref.load(eContext);
        final Register idxr = idx.getRegister();
        final Register refr = ref.getRegister();
        checkBounds(refr, idxr);
        os.writeLEA(refr, refr, idxr, 8, VmArray.DATA_OFFSET * 4);
        os.writeMOV(INTSIZE, refr, 0, val.getLsbRegister());
        os.writeMOV(INTSIZE, refr, 4, val.getMsbRegister());

        ref.release(eContext);
        idx.release(eContext);
        val.release(eContext);
    }

    /**
     * Helper method for visit_if_acmpxx
     * 
     * @param address
     * @param jccOpcode
     */
    private final void visit_if_acmp(int address, int jccOpcode) {
        RefItem v2 = vstack.popRef();
        RefItem v1 = vstack.popRef();

        // flush vstack before jumping
        vstack.push(eContext);

        //TODO: can be less restrictive: v1 must not be register
        prepareForOperation(v1, v2);

        Register r1 = v1.getRegister();

        switch (v2.getKind()) {
        case Item.Kind.REGISTER:
            os.writeCMP(r1, v2.getRegister());
            break;
        case Item.Kind.LOCAL:
            os.writeCMP(r1, FP, v2.getOffsetToFP());
            break;
        case Item.Kind.CONSTANT:
            //TODO: implement writeCMP_Const(reg, object)
            //Object c2 = v2.getValue();
            //os.writeCMP_Const(r1, c2);
            v2.load(eContext);
            os.writeCMP(r1, v2.getRegister());
            break;
        }
        v1.release(eContext);
        v2.release(eContext);
        os.writeJCC(helper.getInstrLabel(address), jccOpcode);
    }

    /**
     * Helper method for visit_if_icmpxx
     * 
     * @param address
     * @param jccOpcode
     */
    private final void visit_if_icmp(int address, int jccOpcode) {
        IntItem v2 = vstack.popInt();
        IntItem v1 = vstack.popInt();

        // flush vstack before jumping
        vstack.push(eContext);

        //TODO: can be less restrictive: v1 must not be register
        prepareForOperation(v1, v2);

        Register r1 = v1.getRegister();

        switch (v2.getKind()) {
        case Item.Kind.REGISTER:
            os.writeCMP(r1, v2.getRegister());
            break;
        case Item.Kind.LOCAL:
            os.writeCMP(r1, FP, v2.getOffsetToFP());
            break;
        case Item.Kind.CONSTANT:
            int c2 = v2.getValue();
            os.writeCMP_Const(r1, c2);
            break;
        }
        v1.release(eContext);
        v2.release(eContext);
        os.writeJCC(helper.getInstrLabel(address), jccOpcode);
    }

    private void visit_ifxx(int type, int address, int jccOpcode) {
        //IMPROVE: Constant case / Local case
        Item v;
        Register r;
        if (type == JvmType.REFERENCE) {
            RefItem vr = vstack.popRef();
            vr.load(eContext);
            r = vr.getRegister();
            v = vr;
        } else {
            IntItem vi = vstack.popInt();
            vi.load(eContext);
            r = vi.getRegister();
            v = vi;
        }
        // flush vstack before jumping
        vstack.push(eContext);

        v.release(eContext);
        os.writeTEST(r, r);
        os.writeJCC(helper.getInstrLabel(address), jccOpcode);
    }

    /**
     * Write code to resolve the given constant field referred to by fieldRef
     * 
     * @param fieldRef
     * @param scratch
     */
    private final void writeInitializeClass(VmConstFieldRef fieldRef,
            Register scratch) {
        // Get fieldRef via constantpool to avoid direct object references in
        // the native code

        final VmType declClass = fieldRef.getResolvedVmField()
                .getDeclaringClass();
        if (!declClass.isInitialized()) {
            // Now look for class initialization
            // Load classRef into EAX
            // Load the class from the statics table
            helper.writeGetStaticsEntry(new Label(curInstrLabel + "$$ic"),
                    scratch, declClass);

            // Load declaringClass.typeState into scratch
            // Test for initialized
            final int offset = context.getVmTypeState().getOffset();
            os.writeTEST(scratch, offset, VmTypeState.ST_INITIALIZED);
            final Label afterInit = new Label(curInstrLabel + "$$aci");
            os.writeJCC(afterInit, X86Constants.JNZ);
            // Call cls.initialize
            os.writePUSH(scratch);
            helper.invokeJavaMethod(context.getVmTypeInitialize());
            os.setObjectRef(afterInit);
        }

    }

    /**
     * Write code to pop a 64-bit word from the stack
     * 
     * @param lsbReg
     * @param msbReg
     */
    private final void writePOP64(Register lsbReg, Register msbReg) {
        os.writePOP(lsbReg);
        os.writePOP(msbReg);
    }

    /**
     * Write code to resolve the given constant class (if needed) and load the
     * resolved class (VmType instance) into the given register.
     * 
     * @param classRef
     */
    private final void writeResolveAndLoadClassToReg(VmConstClass classRef,
            Register dst) {
        // Resolve the class
        classRef.resolve(loader);
        final VmType type = classRef.getResolvedVmClass();

        // Load the class from the statics table
        helper.writeGetStaticsEntry(curInstrLabel, dst, type);
    }
}