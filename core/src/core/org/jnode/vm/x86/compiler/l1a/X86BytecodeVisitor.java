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
import org.jnode.vm.SoftByteCodes;
import org.jnode.vm.bytecode.BasicBlock;
import org.jnode.vm.bytecode.BytecodeParser;
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

    /** The output stream */
    private final AbstractX86Stream os;

    /** Helper class */
    private final X86CompilerHelper helper;

    /** The destination compiled method */
    private final CompiledMethod cm;

    /** Label of current instruction */
    private Label curInstrLabel;

    /** Length of os at start of method */
    private int startOffset;

    /** Stackframe utility */
    private X86StackFrame stackFrame;

    /** Size of an object reference */
    private final int slotSize;

    /** Current context */
    private final X86CompilerContext context;

    /** Emit logging info */
    private final boolean log;

    /** Class loader */
    private VmClassLoader loader;

    private boolean startOfBB;

    private Label endOfInlineLabel;

    private int maxLocals;
    
    private VmMethod currentMethod;

    private EmitterContext eContext;

    /*
     * Virtual Stack: this stack contains values that have been computed but
     * not emitted yet; emission is delayed to allow for optimizations, in
     * particular using registers instead of stack operations.
     * 
     * The vstack is valid only inside a basic block; items in the stack are
     * flushed at the end of the basic block.
     * 
     * Aliasing: modifying a value that is still on the stack is forbidden.
     * Each time a local is assigned, the stack is checked for aliases. For teh
     * same reason, array and field operations are not delayed.
     */
    private VirtualStack vstack;

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
        this.helper = new X86CompilerHelper(os, context, isBootstrap);
        this.cm = cm;
        this.slotSize = VmX86Architecture.SLOT_SIZE;
        this.log = os.isLogEnabled();
        this.eContext = new EmitterContext(this.os, this.helper);
	this.vstack = eContext.getVStack();
    }

    /**
     * @param method
     * @see org.jnode.vm.bytecode.BytecodeVisitor#startMethod(org.jnode.vm.classmgr.VmMethod)
     */
    public void startMethod(VmMethod method) {
        this.currentMethod = method;
        this.maxLocals = method.getBytecode().getNoLocals();
        this.loader = method.getDeclaringClass().getLoader();
        helper.setMethod(method);
        // this.startOffset = os.getLength();
        this.stackFrame = new X86StackFrame(os, helper, method, context, cm);
        this.startOffset = stackFrame.emitHeader();
    }

    /**
     * The given basic block is about to start.
     */
    public void startBasicBlock(BasicBlock bb) {
        if (true || log) {
            os.log("Start of basic block " + bb);
        }
        helper.reset();
        startOfBB = true;
        this.vstack.reset();
        eContext.getPool().reset(os);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#startInstruction(int)
     */
    public void startInstruction(int address) {
        this.curInstrLabel = helper.getInstrLabel(address);
        if (startOfBB) {
            os.setObjectRef(curInstrLabel);
            startOfBB = false;
        }
        final int offset = os.getLength() - startOffset;
        cm.add(currentMethod, address, offset);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#endInstruction()
     */
    public void endInstruction() {
        // Nothing to do here
    }

    /**
     * The started basic block has finished.
     */
    public void endBasicBlock() {
        // flush vstack: at end/begin of basic block are all items on the stack
        vstack.push(eContext);
        if (true || log) {
            os.log("End of basic block");
        }
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#endMethod()
     */
    public void endMethod() {
        stackFrame.emitTrailer(maxLocals);
    }

    /**
     * @param parser
     * @see org.jnode.vm.bytecode.BytecodeVisitor#setParser(org.jnode.vm.bytecode.BytecodeParser)
     */
    public void setParser(BytecodeParser parser) {
        // Nothing to do here
    }

    private final void assertCondition(boolean cond) {
        if (!cond) throw new Error("assert failed");
    }

    /*private final void notImplemented() {
        throw new Error("NotImplemented");
    }*/

    /*private void OpcodeNotImplemented(String name) {
        System.out.println(name + " not implemented");
    }*/

    /**
     * reserve a register for an item. The item is not loaded with the register.
     * The register is spilled if another item holds it.
     * 
     * @param reg the register to reserve
     * @param it the item requiring the register
     */
    private final void requestRegister(Register reg, Item it) {
        X86RegisterPool pool = eContext.getPool();
        
        // check item doesn't already use register
        if (!it.uses(reg)) {
            if (!pool.isFree(reg)) {
                //TODO: spill register; make sure that the stack items 
                // and floating items are handled correctly
                final Item i = (Item)pool.getOwner(reg);
		i.spill(eContext, reg);
		assertCondition(pool.isFree(reg));
            }
            pool.request(reg, it);
        }
    }
    
    private final void prepareForOperation(Item destAndSource, Item source) {
        // WARNING: source was on top of the virtual stack (thus higher than
        // destAndSource)
        // x86 can only deal with one complex argument
        // destAndSource must be a register
        source.loadIf(eContext, (Item.Kind.STACK | Item.Kind.FREGISTER));
        destAndSource.load(eContext);
    }

    /**
     * @see org.jnode.vm.compiler.InlineBytecodeVisitor#endInlinedMethod(org.jnode.vm.classmgr.VmMethod)
     */
    public void endInlinedMethod(VmMethod previousMethod) {
        helper.setMethod(previousMethod);
        os.setObjectRef(endOfInlineLabel);
        this.currentMethod = previousMethod;
    }

    /**
     * @see org.jnode.vm.compiler.InlineBytecodeVisitor#startInlinedMethod(org.jnode.vm.classmgr.VmMethod,
     *      int)
     */
    public void startInlinedMethod(VmMethod inlinedMethod, int newMaxLocals) {
        //TODO: check whether this is really needed
        vstack.push(eContext);
        maxLocals = newMaxLocals;
        endOfInlineLabel = new Label(curInstrLabel + "_end_of_inline");
        helper.startInlinedMethod(inlinedMethod, curInstrLabel);
        this.currentMethod = inlinedMethod;
    }

    /**
     * @see org.jnode.vm.compiler.InlineBytecodeVisitor#visit_inlinedReturn()
     */
    public void visit_inlinedReturn() {
        vstack.push(eContext);
        os.writeJMP(endOfInlineLabel);
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
        idx.loadIf(eContext, (useBarrier) ? ~Item.Kind.REGISTER : ~(Item.Kind.CONSTANT|Item.Kind.REGISTER));
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
            // the write barrier could easily be modified to avoid using a scratch register
            final X86RegisterPool pool = eContext.getPool();
            final Register scratch = (Register)pool.request(Item.JvmType.INT);
            helper.writeArrayStoreWriteBarrier(r, idx.getRegister(), v, scratch);
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
        vstack.push(eContext);
	Item count = vstack.popInt();
	count.release1(eContext);

        writeResolveAndLoadClassToEAX(classRef, S0);
        helper.writePOP(S0); /* Count */

        stackFrame.writePushMethodRef();
        helper.writePUSH(EAX); /* Class */
        helper.writePUSH(S0); /* Count */
        helper.invokeJavaMethod(context.getAnewarrayMethod());
        /* Result is already push on the stack */

        vstack.push1(RefItem.createStack());
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_areturn()
     */
    public final void visit_areturn() {
        RefItem i = vstack.popRef();
        requestRegister(EAX, i);
        i.loadTo(eContext, EAX);
        i.release(eContext);
        
        visit_return();
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_arraylength()
     */
    public final void visit_arraylength() {
        RefItem ref = vstack.popRef();
        ref.load(eContext);
        final Register r = ref.getRegister();
        
        os.writeMOV(INTSIZE, r, r, VmArray.LENGTH_OFFSET * slotSize);

        final IntItem i = IntItem.createRegister(r);
        eContext.getPool().transferOwnerTo(r, i);
        vstack.push(i);
    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_astore(int)
     */
    public final void visit_astore(int index) {
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
        RefItem ref = vstack.popRef();

	//TODO: should I really bother to allocate EAX in this case?
        requestRegister(EAX, ref);
        ref.loadTo(eContext, EAX);

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
       
        final IntItem result = IntItem.createRegister(r);
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
            os.writeMOV(WORDSIZE, r, r,offset, 2,VmArray.DATA_OFFSET * 4);
            idx.release(eContext);
        }
        os.writeMOVZX(r, r, WORDSIZE);
        // do not release ref, it is recycled into the result
        final IntItem result = IntItem.createRegister(r);
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
        // flush stack because of method call, also frees registers
        vstack.push(eContext);
        
        // check that top item is a reference
        RefItem ref = vstack.popRef();
        vstack.push(ref);

	if (VirtualStack.checkOperandStack) {
		// sanity check
		vstack.popFromOperandStack(ref);
		vstack.pushOnOperandStack(ref);
	}

        writeResolveAndLoadClassToEAX(classRef, S0);

        final Label okLabel = new Label(this.curInstrLabel + "cc-ok");

        /* objectref -> ECX (also leave in on the stack */
        os.writeMOV(INTSIZE, ECX, SP, 0);
        /* Is objectref null? */
        os.writeTEST(ECX, ECX);
        os.writeJCC(okLabel, X86Constants.JZ);
        /* Is instanceof? */
        instanceOf(okLabel);
        /* Not instanceof */

        // Call SoftByteCodes.systemException
        helper.writePUSH(SoftByteCodes.EX_CLASSCAST);
        helper.writePUSH(0);
        helper.invokeJavaMethod(context.getSystemExceptionMethod());

        /* Exception in EAX, throw it */
        helper.writeJumpTableCALL(X86JumpTable.VM_ATHROW_OFS);

        /* Normal exit */
        os.setObjectRef(okLabel);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_d2f()
     */
    public final void visit_d2f() {
        //TODO: port to orp-style
        vstack.push(eContext);
        Item v = vstack.pop(Item.JvmType.DOUBLE);
        v.release1(eContext);
        vstack.push1(FloatItem.createStack());

        os.writeFLD64(SP, 0);
        os.writeLEA(SP, SP, 4);
        os.writeFSTP32(SP, 0);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_d2i()
     */
    public final void visit_d2i() {
        //TODO: port to orp-style
        vstack.push(eContext);
        Item v = vstack.pop(Item.JvmType.DOUBLE);
        v.release1(eContext);
        vstack.push1(IntItem.createStack());

        os.writeFLD64(SP, 0);
        os.writeLEA(SP, SP, 4);
        os.writeFISTP32(SP, 0);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_d2l()
     */
    public final void visit_d2l() {
        //TODO: port to orp-style
        vstack.push(eContext);
        Item v = vstack.pop(Item.JvmType.DOUBLE);
        v.release1(eContext);
        vstack.push1(LongItem.createStack());

        os.writeFLD64(SP, 0);
        os.writeFISTP64(SP, 0);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dadd()
     */
    public final void visit_dadd() {
        //TODO: port to orp-style
        vstack.push(eContext);
        Item v1 = vstack.pop(Item.JvmType.DOUBLE);
        Item v2 = vstack.pop(Item.JvmType.DOUBLE);
        v1.release1(eContext);
        v2.release1(eContext);
        vstack.push1(DoubleItem.createStack());

        os.writeFLD64(SP, 8);
        os.writeFADD64(SP, 0);
        os.writeLEA(SP, SP, 8);
        os.writeFSTP64(SP, 0);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_daload()
     */
    public final void visit_daload() {
        //TODO: port to orp-style
        vstack.push(eContext);
        IntItem idx = vstack.popInt();
        RefItem ref = vstack.popRef();
        idx.release1(eContext);
        ref.release1(eContext);
        vstack.push1(DoubleItem.createStack());

        helper.writePOP(T0); // Index
        helper.writePOP(S0); // Arrayref
        checkBounds(S0, T0);
        os.writeLEA(S0, S0, T0, 8, VmArray.DATA_OFFSET * 4);
        os.writeMOV(INTSIZE, T0, S0, 0);
        os.writeMOV(INTSIZE, T1, S0, 4);
        helper.writePUSH64(T0, T1);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dastore()
     */
    public final void visit_dastore() {
        //TODO: port to orp-style
        vstack.push(eContext);
        Item val = vstack.pop(Item.JvmType.DOUBLE);
        IntItem idx = vstack.popInt();
        RefItem ref = vstack.popRef();
        val.release1(eContext);
        idx.release1(eContext);
        ref.release1(eContext);

        os.writeMOV(INTSIZE, T0, SP, 8); // Index
        os.writeMOV(INTSIZE, S0, SP, 12); // Arrayref
        checkBounds(S0, T0);
        os.writeLEA(S0, S0, T0, 8, VmArray.DATA_OFFSET * 4);
        helper.writePOP64(T0, T1); // Value
        os.writeMOV(INTSIZE, S0, 0, T0);
        os.writeMOV(INTSIZE, S0, 4, T1);
        os.writeLEA(SP, SP, 8); // Remove index, arrayref
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dcmpg()
     */
    public final void visit_dcmpg() {
        //TODO: port to orp-style
        visit_dfcmp(true, false);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dcmpl()
     */
    public final void visit_dcmpl() {
        //TODO: port to orp-style
        visit_dfcmp(false, false);
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
        //TODO: port to orp-style
        vstack.push(eContext);
        Item v1 = vstack.pop(Item.JvmType.DOUBLE);
        Item v2 = vstack.pop(Item.JvmType.DOUBLE);
        v1.release1(eContext);
        v2.release1(eContext);
        vstack.push1(DoubleItem.createStack());

        os.writeFLD64(SP, 8);
        os.writeFDIV64(SP, 0);
        os.writeLEA(SP, SP, 8);
        os.writeFSTP64(SP, 0);
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
        //TODO: port to orp-style
        vstack.push(eContext);
        Item v1 = vstack.pop(Item.JvmType.DOUBLE);
        Item v2 = vstack.pop(Item.JvmType.DOUBLE);
        v1.release1(eContext);
        v2.release1(eContext);
        vstack.push1(DoubleItem.createStack());

        os.writeFLD64(SP, 8);
        os.writeFMUL64(SP, 0);
        os.writeLEA(SP, SP, 8);
        os.writeFSTP64(SP, 0);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dneg()
     */
    public final void visit_dneg() {
        //TODO: port to orp-style
        vstack.push(eContext);
        Item v1 = vstack.pop(Item.JvmType.DOUBLE);
        v1.release1(eContext);
        vstack.push1(DoubleItem.createStack());

        os.writeFLD64(SP, 0);
        os.writeFCHS();
        os.writeFSTP64(SP, 0);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_drem()
     */
    public final void visit_drem() {
        //TODO: port to orp-style
        vstack.push(eContext);
        Item v1 = vstack.pop(Item.JvmType.DOUBLE);
        Item v2 = vstack.pop(Item.JvmType.DOUBLE);
        v1.release1(eContext);
        v2.release1(eContext);
        vstack.push1(DoubleItem.createStack());

        os.writeFLD64(SP, 8);
        os.writeFLD64(SP, 0);
        os.writeFPREM();
        os.writeLEA(SP, SP, 8);
        os.writeFSTP64(SP, 0);
        //        |os.writeFFREE(Register.ST0);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dreturn()
     */
    public final void visit_dreturn() {
        //TODO: port to orp-style
        Item v1 = vstack.pop(Item.JvmType.DOUBLE);
        v1.push(eContext);
        v1.release1(eContext);

        helper.writePOP64(Register.EAX, Register.EDX);
        visit_return();
    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dstore(int)
     */
    public final void visit_dstore(int index) {
        //TODO: port to orp-style
        Item val = vstack.pop(Item.JvmType.DOUBLE);

	// This break the invariant (if an item is pushed,
	// all items below should as be pushed), but it
	// is popped later in this same instruction
        val.push(eContext);
        val.release1(eContext);

        int ebpOfs = stackFrame.getWideEbpOffset(index);

	// pin down other references to this load
        vstack.loadLocal(eContext, ebpOfs);

        helper.writePOP64(Register.EAX, Register.EDX);
        os.writeMOV(INTSIZE, FP, ebpOfs, Register.EAX);
        os.writeMOV(INTSIZE, FP, ebpOfs + 4, Register.EDX);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dsub()
     */
    public final void visit_dsub() {
        //TODO: port to orp-style
        vstack.push(eContext);
        Item v1 = vstack.pop(Item.JvmType.DOUBLE);
        Item v2 = vstack.pop(Item.JvmType.DOUBLE);
        v1.release1(eContext);
        v2.release1(eContext);
        vstack.push1(DoubleItem.createStack());

        os.writeFLD64(SP, 8);
        os.writeFSUB64(SP, 0);
        os.writeLEA(SP, SP, 8);
        os.writeFSTP64(SP, 0);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup_x1()
     */
    public final void visit_dup_x1() {
        //TODO: port to orp-style
        os.log("dup_x1");
//        vstack.push(eContext);
//        Item v1 = vstack.pop();
//        Item v2 = vstack.pop();
//        vstack.push(v1);
//        vstack.push(v2);
//        vstack.push(v1);

        // trash stack
        vstack.push(eContext);
        vstack.reset();
        
        helper.writePOP(T0); // Value1
        helper.writePOP(S0); // Value2
        helper.writePUSH(T0); // Value1
        helper.writePUSH(S0); // Value2
        helper.writePUSH(T0); // Value1
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup_x2()
     */
    public final void visit_dup_x2() {
        //TODO: port to orp-style
        os.log("dup_x2");
//        vstack.push(eContext);
//        Item v1 = vstack.pop();
//        Item v2 = vstack.pop();
//        if (v2.getCategory() == 2) {
//            // form2
//            vstack.push(v1);
//            vstack.push(v2);
//            vstack.push(v1);
//        } else {
//            // form1
//            Item v3 = vstack.pop();
//            vstack.push(v1);
//            vstack.push(v3);
//            vstack.push(v2);
//            vstack.push(v1);
//        }

        // trash vstack
        vstack.push(eContext);
        vstack.reset();

        helper.writePOP(T0); // Value1
        helper.writePOP(T1); // Value2
        helper.writePOP(S0); // Value3
        helper.writePUSH(T0); // Value1
        helper.writePUSH(S0); // Value3
        helper.writePUSH(T1); // Value2
        helper.writePUSH(T0); // Value1
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup()
     */
    public final void visit_dup() {
        if (vstack.isEmpty()) {
	    // do not use vstack here because no item type is available
            os.writeMOV(INTSIZE, T0, SP, 0); // Value1, leave on stack
            helper.writePUSH(T0);
	} else {
            Item v1 = vstack.pop();
            vstack.push(v1);
            vstack.push(v1.clone(eContext));
	}
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup2_x1()
     */
    public final void visit_dup2_x1() {
        //TODO: port to orp-style
        os.log("dup2_x1");
//        vstack.push(eContext);
//        Item v1 = vstack.pop();
//        Item v2 = vstack.pop();
//        assertCondition(v2.getCategory() == 1);
//        if (v1.getCategory() == 2) { // form2
//            vstack.push(v1);
//            vstack.push(v2);
//            vstack.push(v1);
//        } else {
//            Item v3 = vstack.pop();
//            vstack.push(v2);
//            vstack.push(v1);
//            vstack.push(v3);
//            vstack.push(v2);
//            vstack.push(v1);
//        }

        // trash vstack
        vstack.push(eContext);
        vstack.reset();

        helper.writePOP(T0); // Value1
        helper.writePOP(T1); // Value2
        helper.writePOP(S0); // Value3
        helper.writePUSH(T1); // Value2
        helper.writePUSH(T0); // Value1
        helper.writePUSH(S0); // Value3
        helper.writePUSH(T1); // Value2
        helper.writePUSH(T0); // Value1
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup2_x2()
     */
    public final void visit_dup2_x2() {
        //TODO: port to orp-style
        os.log("dup2_x2");

//        vstack.push(eContext);
//        Item v1 = vstack.pop();
//        Item v2 = vstack.pop();
//        int c1 = v1.getCategory();
//        int c2 = v2.getCategory();
//        // cope with brain-dead definition from Sun (look-like somebody there
//        // was to eager to optimize this and it landed in the compiler...
//        if (c2 == 2) {
//            // form 4
//            assertCondition(c1 == 2);
//            vstack.push(v1);
//            vstack.push(v2);
//            vstack.push(v1);
//        } else {
//            Item v3 = vstack.pop();
//            int c3 = v3.getCategory();
//            if (c1 == 2) {
//                // form 2
//                assertCondition(c3 == 1);
//                vstack.push(v1);
//                vstack.push(v3);
//                vstack.push(v2);
//                vstack.push(v1);
//            } else if (c3 == 2) {
//                // form 3
//                vstack.push(v2);
//                vstack.push(v1);
//                vstack.push(v3);
//                vstack.push(v2);
//                vstack.push(v1);
//            } else {
//                // form 1
//                Item v4 = vstack.pop();
//                vstack.push(v2);
//                vstack.push(v1);
//                vstack.push(v4);
//                vstack.push(v3);
//                vstack.push(v2);
//                vstack.push(v1);
//            }
//        }

        // trash vstack
        vstack.push(eContext);
        vstack.reset();

        helper.writePOP(Register.EAX); // Value1
        helper.writePOP(Register.EBX); // Value2
        helper.writePOP(Register.ECX); // Value3
        helper.writePOP(Register.EDX); // Value4
        helper.writePUSH(Register.EBX); // Value2
        helper.writePUSH(Register.EAX); // Value1
        helper.writePUSH(Register.EDX); // Value4
        helper.writePUSH(Register.ECX); // Value3
        helper.writePUSH(Register.EBX); // Value2
        helper.writePUSH(Register.EAX); // Value1
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup2()
     */
    public final void visit_dup2() {
        //TODO: port to orp-style
        //REFACTORING: merge all duplication opcodes
        os.log("dup2");
//        vstack.push(eContext);
//        Item v1 = vstack.pop();
//        if (v1.getCategory() == 1) {
//            // form1
//            Item v2 = vstack.pop();
//            assertCondition(v2.getCategory() == 1);
//            vstack.push(v2);
//            vstack.push(v1);
//            vstack.push(v2);
//            vstack.push(v1);
//        } else {
//            vstack.push(v1);
//            vstack.push(v1);
//        }

        // trash vstack
        vstack.push(eContext);
        vstack.reset();

        os.writeMOV(INTSIZE, T0, SP, 0); // value1
        os.writeMOV(INTSIZE, S0, SP, 4); // value2
        helper.writePUSH(S0);
        helper.writePUSH(T0);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_f2d()
     */
    public final void visit_f2d() {
        //TODO: port to orp-style
        vstack.push(eContext);
        Item v1 = vstack.pop(Item.JvmType.FLOAT);
        v1.release1(eContext);
        vstack.push1(DoubleItem.createStack());

        os.writeFLD32(SP, 0);
        os.writeLEA(SP, SP, -4);
        os.writeFSTP64(SP, 0);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_f2i()
     */
    public final void visit_f2i() {
        //TODO: port to orp-style
        vstack.push(eContext);
        Item v1 = vstack.pop(Item.JvmType.FLOAT);
        v1.release1(eContext);
        vstack.push1(IntItem.createStack());

        os.writeFLD32(SP, 0);
        os.writeFISTP32(SP, 0);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_f2l()
     */
    public final void visit_f2l() {
        //TODO: port to orp-style
        vstack.push(eContext);
        Item v1 = vstack.pop(Item.JvmType.FLOAT);
        v1.release1(eContext);
        vstack.push1(LongItem.createStack());

        os.writeFLD32(SP, 0);
        os.writeLEA(SP, SP, -4);
        os.writeFISTP64(SP, 0);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fadd()
     */
    public final void visit_fadd() {
        //TODO: port to orp-style
        vstack.push(eContext);
        Item v1 = vstack.pop(Item.JvmType.FLOAT);
        Item v2 = vstack.pop(Item.JvmType.FLOAT);
        v1.release1(eContext);
        v2.release1(eContext);
        vstack.push1(FloatItem.createStack());
        os.writeFLD32(SP, 4);
        os.writeFADD32(SP, 0);
        os.writeLEA(SP, SP, 4);
        os.writeFSTP32(SP, 0);
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
        //TODO: port to orp-style
        vstack.push(eContext);
        Item val = vstack.pop(Item.JvmType.FLOAT);
        IntItem idx = vstack.popInt();
        RefItem ref = vstack.popRef();
        val.release1(eContext);
        idx.release1(eContext);
        ref.release1(eContext);

        helper.writePOP(T1); // Value
        helper.writePOP(T0); // Index
        helper.writePOP(S0); // Arrayref
        checkBounds(S0, T0);
        os.writeMOV(INTSIZE, S0, T0, 4, VmArray.DATA_OFFSET * 4, T1);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fcmpg()
     */
    public final void visit_fcmpg() {
        visit_dfcmp(true, true);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fcmpl()
     */
    public final void visit_fcmpl() {
        visit_dfcmp(false, true);
    }

    private void visit_dfcmp(boolean gt, boolean isfloat) {
        //TODO: port to orp-style
        vstack.push(eContext);
        Item v1 = vstack.pop(isfloat ? Item.JvmType.FLOAT : Item.JvmType.DOUBLE);
        Item v2 = vstack.pop(isfloat ? Item.JvmType.FLOAT : Item.JvmType.DOUBLE);

        v1.release1(eContext);
        v2.release1(eContext);

        if (isfloat) {
            if (gt) {
                os.writeFLD32(SP, 4); // reverse order
                os.writeFLD32(SP, 0);
            } else {
                os.writeFLD32(SP, 0);
                os.writeFLD32(SP, 4);
            }
            os.writeLEA(SP, SP, 8);
        } else {
            if (gt) {
                os.writeFLD64(SP, 8); // reverse order
                os.writeFLD64(SP, 0);
            } else {
                os.writeFLD64(SP, 0);
                os.writeFLD64(SP, 8);
            }
            os.writeLEA(SP, SP, 16);
        }
        os.writeFUCOMPP(); // Compare, Pop twice
        os.writeFNSTSW_AX(); // Store fp status word in AX
        os.writeSAHF(); // Store AH to Flags
        Label eqLabel = new Label(this.curInstrLabel + "eq");
        Label ltLabel = new Label(this.curInstrLabel + "lt");
        Label endLabel = new Label(this.curInstrLabel + "end");
        os.writeJCC(eqLabel, X86Constants.JE);
        os.writeJCC(ltLabel, X86Constants.JB);
        // Greater
        if (gt) {
            os.writeMOV_Const(Register.ECX, -1);
        } else {
            os.writeMOV_Const(Register.ECX, 1);
        }
        os.writeJMP(endLabel);
        // Equal
        os.setObjectRef(eqLabel);
        os.writeXOR(Register.ECX, Register.ECX);
        os.writeJMP(endLabel);
        // Less
        os.setObjectRef(ltLabel);
        if (gt) {
            os.writeMOV_Const(Register.ECX, 1);
        } else {
            os.writeMOV_Const(Register.ECX, -1);
        }
        // End
        os.setObjectRef(endLabel);
        //helper.writePUSH(Register.ECX);

	final X86RegisterPool pool = eContext.getPool();
	pool.request(Register.ECX);  // no check, this won't fail
	IntItem res = IntItem.createRegister(Register.ECX);
	pool.transferOwnerTo(Register.ECX, res);
	vstack.push(res);
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
        //TODO: port to orp-style
        vstack.push(eContext);
        Item v1 = vstack.pop(Item.JvmType.FLOAT);
        Item v2 = vstack.pop(Item.JvmType.FLOAT);
        v1.release1(eContext);
        v2.release1(eContext);
        vstack.push1(FloatItem.createStack());

        os.writeFLD32(SP, 4);
        os.writeFDIV32(SP, 0);
        os.writeLEA(SP, SP, 4);
        os.writeFSTP32(SP, 0);
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
        //TODO: port to orp-style
        vstack.push(eContext);
        Item v1 = vstack.pop(Item.JvmType.FLOAT);
        Item v2 = vstack.pop(Item.JvmType.FLOAT);
        v1.release1(eContext);
        v2.release1(eContext);
        vstack.push1(FloatItem.createStack());

        os.writeFLD32(SP, 4);
        os.writeFMUL32(SP, 0);
        os.writeLEA(SP, SP, 4);
        os.writeFSTP32(SP, 0);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fneg()
     */
    public final void visit_fneg() {
        //TODO: port to orp-style
        vstack.push(eContext);
        Item v1 = vstack.pop(Item.JvmType.FLOAT);
        v1.release1(eContext);
        vstack.push1(FloatItem.createStack());

        os.writeFLD32(SP, 0);
        os.writeFCHS();
        os.writeFSTP32(SP, 0);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_frem()
     */
    public final void visit_frem() {
        //TODO: port to orp-style
        // reverse because pushing on fp stack
        vstack.push(eContext);
        Item v1 = vstack.pop(Item.JvmType.FLOAT);
        Item v2 = vstack.pop(Item.JvmType.FLOAT);
        v1.release1(eContext);
        v2.release1(eContext);
        vstack.push1(FloatItem.createStack());

        os.writeFLD32(SP, 0);
        os.writeFLD32(SP, 4);
        os.writeFPREM();
        os.writeLEA(SP, SP, 4);
        os.writeFSTP32(SP, 0);
        os.writeFFREE(Register.ST0);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_freturn()
     */
    public final void visit_freturn() {
        //TODO: port to orp-style
        vstack.push(eContext);
        Item v1 = vstack.pop(Item.JvmType.FLOAT);
        v1.release1(eContext);

        helper.writePOP(Register.EAX);
        visit_return();
    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fstore(int)
     */
    public final void visit_fstore(int index) {
        //TODO: port to orp-style
        vstack.push(eContext);
        Item v1 = vstack.pop(Item.JvmType.FLOAT);

        final int ebpOfs = stackFrame.getEbpOffset(index);

	// pin down other references to this local
        vstack.loadLocal(eContext, ebpOfs);

        v1.release1(eContext);
        helper.writePOP(FP, ebpOfs);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fsub()
     */
    public final void visit_fsub() {
        //TODO: port to orp-style
        vstack.push(eContext);
        Item v1 = vstack.pop(Item.JvmType.FLOAT);
        Item v2 = vstack.pop(Item.JvmType.FLOAT);
        v1.release1(eContext);
        v2.release1(eContext);
        vstack.push1(FloatItem.createStack());

        os.writeFLD32(SP, 4);
        os.writeFSUB32(SP, 0);
        os.writeLEA(SP, SP, 4);
        os.writeFSTP32(SP, 0);
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

        final int type = Item.SignatureToType(fieldRef.getSignature().charAt(0));

        RefItem ref = vstack.popRef();
	ref.load(eContext);
	final Register r = ref.getRegister();
	final X86RegisterPool pool = eContext.getPool();

	switch (type) {
		case Item.JvmType.INT:
			os.writeMOV(INTSIZE, r, r, offset);

			Item i = IntItem.createRegister(r);
			pool.transferOwnerTo(r, i);
			vstack.push(i);
			break;
		case Item.JvmType.REFERENCE:
			os.writeMOV(INTSIZE, r, r, offset);

			Item ir = RefItem.createRegister(r);
			pool.transferOwnerTo(r, ir);
			vstack.push(ir);
			break;
		case Item.JvmType.LONG:
		case Item.JvmType.DOUBLE:
                        ref.release(eContext);
			Item res = vstack.createStack(type);
                        vstack.push(eContext);
			vstack.push1(res);

			if (r != T1) {
				os.writeMOV(INTSIZE, T1, r, offset + 4); // MSB
				os.writeMOV(INTSIZE, T0, r, offset); // LSB
				helper.writePUSH64(T0, T1);
			} else {
				os.writeMOV(INTSIZE, T0, r, offset + 4); // MSB
				os.writeMOV(INTSIZE, T1, r, offset); // LSB
				helper.writePUSH64(T1, T0);
			}
			break;
		case Item.JvmType.FLOAT:
			//TODO: improve
                        os.writeMOV(INTSIZE, T0, r, offset);
                        os.writePUSH(T0);
                        ref.release(eContext);

                        vstack.push(eContext);
			vstack.push1(FloatItem.createStack());
			break;
		default:
			Item.myAssert(false);
	}
    }

    /**
     * @param fieldRef
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_getstatic(org.jnode.vm.classmgr.VmConstFieldRef)
     */
    public final void visit_getstatic(VmConstFieldRef fieldRef) {
        //TODO: port to orp-style
        vstack.push(eContext);

        fieldRef.resolve(loader);
        final VmStaticField sf = (VmStaticField) fieldRef.getResolvedVmField();
        if (!sf.getDeclaringClass().isInitialized()) {
            writeInitializeClass(fieldRef, S0);
        }

        // Get static field object
        if (!fieldRef.isWide()) {
            helper.writeGetStaticsEntry(curInstrLabel, T0, sf);
            helper.writePUSH(T0);
        } else {
            helper.writeGetStaticsEntry64(curInstrLabel, T0, T1, sf);
            helper.writePUSH64(T0, T1);
        }

        int type = Item.SignatureToType(fieldRef.getSignature().charAt(0));
	Item res = vstack.createStack(type);
        vstack.push1(res);
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
        //TODO: improve const case
        IntItem v = vstack.popInt();
        v.load(eContext);
        final Register r = v.getRegister();
        os.writeMOVSX(r, r, BYTESIZE);
        vstack.push(v);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2c()
     */
    public final void visit_i2c() {
        IntItem v = vstack.popInt();
        v.load(eContext);
        final Register r = v.getRegister();
        os.writeMOVZX(r, r, BYTESIZE);
        vstack.push(v);
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
        //TODO: port to orp-style
        vstack.push(eContext);
        IntItem v = vstack.popInt();
        v.release1(eContext);
        vstack.push1(LongItem.createStack());

        helper.writePOP(Register.EAX);
        os.writeCDQ(); /* Sign extend EAX -> EDX:EAX */
        helper.writePUSH64(Register.EAX, Register.EDX);
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
        IntItem v2 = vstack.popInt();
        IntItem v1 = vstack.popInt();
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

        final IntItem result = IntItem.createRegister(r);
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
        //TODO: port to orp-style
        vstack.push(eContext);
        IntItem v2 = vstack.popInt();
        IntItem v1 = vstack.popInt();
        v2.release1(eContext);
        v1.release1(eContext);
//        vstack.push(IntItem.createStack());

        helper.writePOP(Register.ECX); // Value2
        helper.writePOP(Register.EAX); // Value1
        os.writeCDQ();
        os.writeIDIV_EAX(Register.ECX); // EAX = EDX:EAX / ECX
//        helper.writePUSH(Register.EAX);
        // avoid push, set result to register item
        X86RegisterPool pool = eContext.getPool();
       
	// stack was flushed, thus EAX is free
        pool.request(Register.EAX);
        IntItem result = IntItem.createRegister(Register.EAX);
        pool.transferOwnerTo(Register.EAX, result);
        vstack.push(result);
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

    private void visit_ifxx(int type, int address, int jccOpcode) {
        //IMPROVE: Constant case / Local case
        Item v;
        Register r;
        if (type == Item.JvmType.REFERENCE) {
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
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifeq(int)
     */
    public final void visit_ifeq(int address) {
        visit_ifxx(Item.JvmType.INT, address, X86Constants.JE); // JE
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifge(int)
     */
    public final void visit_ifge(int address) {
        visit_ifxx(Item.JvmType.INT, address, X86Constants.JGE); // JGE
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifgt(int)
     */
    public final void visit_ifgt(int address) {
        visit_ifxx(Item.JvmType.INT, address, X86Constants.JG); // JG
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifle(int)
     */
    public final void visit_ifle(int address) {
        visit_ifxx(Item.JvmType.INT, address, X86Constants.JLE); // JLE
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iflt(int)
     */
    public final void visit_iflt(int address) {
        visit_ifxx(Item.JvmType.INT, address, X86Constants.JL); // JL
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifne(int)
     */
    public final void visit_ifne(int address) {
        visit_ifxx(Item.JvmType.INT, address, X86Constants.JNE); // JNE
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifnonnull(int)
     */
    public final void visit_ifnonnull(int address) {
        visit_ifxx(Item.JvmType.REFERENCE, address, X86Constants.JNE);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifnull(int)
     */
    public final void visit_ifnull(int address) {
        visit_ifxx(Item.JvmType.REFERENCE, address, X86Constants.JE);
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
        switch(v2.getKind()) {
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
     * @param classRef
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_instanceof(org.jnode.vm.classmgr.VmConstClass)
     */
    public final void visit_instanceof(VmConstClass classRef) {
        //TODO: port to orp-style
	X86RegisterPool pool = eContext.getPool();
        vstack.push(eContext);
        RefItem v = vstack.popRef();
	v.release1(eContext);

        /* Objectref is already on the stack */
        writeResolveAndLoadClassToEAX(classRef, S0);

        final Label trueLabel = new Label(this.curInstrLabel + "io-true");
        final Label endLabel = new Label(this.curInstrLabel + "io-end");

        /* Pop objectref */
        helper.writePOP(Register.ECX);
        /* Is instanceof? */
        instanceOf(trueLabel);
        /* Not instanceof */
	//TODO: use setcc instead of jumps
        os.writeXOR(T0, T0);
        os.writeJMP(endLabel);

        os.setObjectRef(trueLabel);
        os.writeMOV_Const(T0, 1);

        os.setObjectRef(endLabel);
        //os.writePUSH(T0);

	// WARNING: when porting this code, result of request must be checked
	pool.request(T0);
	IntItem res = IntItem.createRegister(T0);
	pool.transferOwnerTo(T0, res);
	vstack.push(res);
    }

    private final void pushReturnValue(String signature) {
        char t = signature.charAt(signature.indexOf(')') + 1);
        if (t != 'V') {
            int type = Item.SignatureToType(t);
	    Item res = vstack.createStack(type);
            vstack.push1(res);
        }
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

        pushReturnValue(methodRef.getSignature());
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
        } catch (ClassCastException ex) {
            System.out.println(methodRef.getResolvedVmMethod().getClass()
                    .getName()
                    + "#" + methodRef.getName());
            throw ex;
        }

        pushReturnValue(methodRef.getSignature());

        /*
         * methodRef.resolve(loader);
         * writeResolveAndLoadClassToEAX(methodRef.getConstClass(), S0);
         * writeResolveAndLoadMethodToEAX(methodRef, S0);
         * helper.invokeJavaMethod(methodRef.getSignature(), context);
         */
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

        pushReturnValue(methodRef.getSignature());
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

        pushReturnValue(methodRef.getSignature());
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

        helper.writePOP(S0); // Value2
        helper.writePOP(EAX); // Value1
        os.writeCDQ();
        os.writeIDIV_EAX(S0); // EAX = EDX:EAX / S0
        helper.writePUSH(EDX); // Remainder
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ireturn()
     */
    public final void visit_ireturn() {
        IntItem v = vstack.popInt();
        requestRegister(EAX, v);
        v.loadTo(eContext, EAX);
        v.release(eContext);

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
        Item v = vstack.pop(Item.JvmType.LONG);
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
        Item v = vstack.pop(Item.JvmType.LONG);
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
        //TODO: port to orp-style
        vstack.push(eContext);
        Item v = vstack.pop(Item.JvmType.LONG);
        v.release1(eContext);
        vstack.push1(IntItem.createStack());

        helper.writePOP64(T0, T1);
        helper.writePUSH(T0);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ladd()
     */
    public final void visit_ladd() {
        //TODO: port to orp-style
        vstack.push(eContext);
        Item v2 = vstack.pop(Item.JvmType.LONG);
        Item v1 = vstack.pop(Item.JvmType.LONG);
        v2.release1(eContext);
        v1.release1(eContext);
        vstack.push1(LongItem.createStack());

        helper.writePOP64(T0, T1); // Value 2
        helper.writePOP64(S0, S1); // Value 1
        os.writeADD(S0, T0);
        os.writeADC(S1, T1);
        helper.writePUSH64(S0, S1);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_laload()
     */
    public final void visit_laload() {
        //TODO: port to orp-style
        vstack.push(eContext);
        IntItem idx = vstack.popInt();
        RefItem ref = vstack.popRef();
        idx.release1(eContext);
        ref.release1(eContext);
        vstack.push1(LongItem.createStack());

        helper.writePOP(T0); // Index
        helper.writePOP(S0); // Arrayref
        checkBounds(S0, T0);
        os.writeLEA(S0, S0, T0, 8, VmArray.DATA_OFFSET * 4);
        os.writeMOV(INTSIZE, T0, S0, 0);
        os.writeMOV(INTSIZE, T1, S0, 4);
        helper.writePUSH64(T0, T1);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_land()
     */
    public final void visit_land() {
        //TODO: port to orp-style
        vstack.push(eContext);
        Item v2 = vstack.pop(Item.JvmType.LONG);
        Item v1 = vstack.pop(Item.JvmType.LONG);
        v2.release1(eContext);
        v1.release1(eContext);
        vstack.push1(LongItem.createStack());

        helper.writePOP64(T0, T1); // Value 2
        helper.writePOP64(S0, S1); // Value 1
        os.writeAND(S0, T0);
        os.writeAND(S1, T1);
        helper.writePUSH64(S0, S1);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lastore()
     */
    public final void visit_lastore() {
        //TODO: port to orp-style
        vstack.push(eContext);
	Item val = vstack.pop(Item.JvmType.LONG);
        IntItem idx = vstack.popInt();
        RefItem ref = vstack.popRef();
	val.release1(eContext);
        idx.release1(eContext);
        ref.release1(eContext);

        os.writeMOV(INTSIZE, T0, SP, 8); // Index
        os.writeMOV(INTSIZE, S0, SP, 12); // Arrayref
        checkBounds(S0, T0);
        os.writeLEA(S0, S0, T0, 8, VmArray.DATA_OFFSET * 4);
        helper.writePOP64(T0, T1); // Value
        os.writeMOV(INTSIZE, S0, 0, T0);
        os.writeMOV(INTSIZE, S0, 4, T1);
        os.writeLEA(SP, SP, 8); // Remove index, arrayref
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lcmp()
     */
    public final void visit_lcmp() {
        //TODO: port to orp-style
        vstack.push(eContext);
        Item v2 = vstack.pop(Item.JvmType.LONG);
        Item v1 = vstack.pop(Item.JvmType.LONG);
        v2.release1(eContext);
        v1.release1(eContext);
        vstack.push1(IntItem.createStack());

        helper.writePOP64(Register.EBX, Register.ECX); // Value 2
        helper.writePOP64(Register.EAX, Register.EDX); // Value 1

        Label ltLabel = new Label(curInstrLabel + "lt");
        Label endLabel = new Label(curInstrLabel + "end");

        os.writeXOR(Register.ESI, Register.ESI);
        os.writeSUB(Register.EAX, Register.EBX);
        os.writeSBB(Register.EDX, Register.ECX);
        os.writeJCC(ltLabel, X86Constants.JL); // JL
        os.writeOR(Register.EAX, Register.EDX);
        os.writeJCC(endLabel, X86Constants.JZ); // value1 == value2
        /** GT */
        os.writeINC(Register.ESI);
        os.writeJMP(endLabel);
        /** LT */
        os.setObjectRef(ltLabel);
        os.writeDEC(Register.ESI);
        os.setObjectRef(endLabel);
        helper.writePUSH(Register.ESI);
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
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ldc(VmConstString)
     */
    public final void visit_ldc(VmConstString value) {
        vstack.push(RefItem.createConst(value));
        //        //REFACTOR: use Constant instead of Object!
        //        if (value instanceof Integer) {
        //            visit_iconst(((Integer) value).intValue());
        //// os.writeMOV_Const(Register.EAX, ((Integer) value).intValue());
        //        } else if (value instanceof Float) {
        //            visit_fconst(((Float) value).floatValue());
        //// os.writeMOV_Const(Register.EAX, Float.floatToRawIntBits(((Float)
        // value).floatValue()));
        //        } else if (value instanceof String) {
        //            //REFACTOR: useCreateStackstant
        //            vstack.push(RefItem.createStack());
        //            helper.writeGetStaticsEntry(curInstrLabel, T0, value);
        //            helper.writePUSH(T0);
        //        } else {
        //            throw new ClassFormatError("ldc with unknown type " +
        // value.getClass().getName());
        //        }
        //// helper.writePUSH(Register.EAX);
    }

    /**
     * @param value
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ldc(VmConstClass)
     */
    public final void visit_ldc(VmConstClass value) {
        throw new Error("Not implemented yet");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ldiv()
     */
    public final void visit_ldiv() {
        //TODO: port to orp-style
        vstack.push(eContext);
        Item v2 = vstack.pop(Item.JvmType.LONG);
        Item v1 = vstack.pop(Item.JvmType.LONG);
        v2.release1(eContext);
        v1.release1(eContext);
        vstack.push1(LongItem.createStack());

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
        Item v2 = vstack.pop(Item.JvmType.LONG);
        Item v1 = vstack.pop(Item.JvmType.LONG);
        v2.release1(eContext);
        v1.release1(eContext);
        vstack.push1(LongItem.createStack());

        helper.writePOP64(Register.EBX, Register.ECX); // Value 2
        helper.writePOP64(Register.ESI, Register.EDI); // Value 1

        Label tmp1 = new Label(curInstrLabel + "$tmp1");
        Label tmp2 = new Label(curInstrLabel + "$tmp2");

        os.writeMOV(INTSIZE, Register.EAX, Register.EDI); // hi2
        os.writeOR(Register.EAX, Register.ECX); // hi1 | hi2
        os.writeJCC(tmp1, X86Constants.JNZ);
        os.writeMOV(INTSIZE, Register.EAX, Register.ESI); // lo2
        os.writeMUL_EAX(Register.EBX); // lo1*lo2
        os.writeJMP(tmp2);
        os.setObjectRef(tmp1);
        os.writeMOV(INTSIZE, Register.EAX, Register.ESI); // lo2
        os.writeMUL_EAX(Register.ECX); // hi1*lo2
        os.writeMOV(INTSIZE, Register.ECX, Register.EAX);
        os.writeMOV(INTSIZE, Register.EAX, Register.EDI); // hi2
        os.writeMUL_EAX(Register.EBX); // hi2*lo1
        os.writeADD(Register.ECX, Register.EAX); // hi2*lo1 + hi1*lo2
        os.writeMOV(INTSIZE, Register.EAX, Register.ESI); // lo2
        os.writeMUL_EAX(Register.EBX); // lo1*lo2
        os.writeADD(Register.EDX, Register.ECX); // hi2*lo1 + hi1*lo2 +
                                                 // hi(lo1*lo2)
        os.setObjectRef(tmp2);
        // Reload the statics table, since it was destroyed here
        helper.writeLoadSTATICS(curInstrLabel, "lmul", false);
        helper.writePUSH64(Register.EAX, Register.EDX);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lneg()
     */
    public final void visit_lneg() {
        //TODO: port to orp-style
        vstack.push(eContext);
        Item v = vstack.pop(Item.JvmType.LONG);
	
	// force check of operand stack top item
	// release must be removed when orp-style ready
	v.release1(eContext);
        vstack.push1(v);

        helper.writePOP64(T0, T1);
        os.writeNEG(T1); // msb := -msb
        os.writeNEG(T0); // lsb := -lsb
        os.writeSBB(T1, 0); // high += borrow
        helper.writePUSH64(T0, T1);
        /*
         * os.writeNEG(SP, 0); os.writeNEG(SP, 4);
         */
    }

    /**
     * @param defAddress
     * @param matchValues
     * @param addresses
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lookupswitch(int,
     *      int[], int[])
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
        //TODO: port to orp-style
        vstack.push(eContext);
        Item v2 = vstack.pop(Item.JvmType.LONG);
        Item v1 = vstack.pop(Item.JvmType.LONG);
        v2.release1(eContext);
        v1.release1(eContext);
        vstack.push1(LongItem.createStack());

        helper.writePOP64(T0, T1); // Value 2
        helper.writePOP64(S0, S1); // Value 1
        os.writeOR(S0, T0);
        os.writeOR(S1, T1);
        helper.writePUSH64(S0, S1);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lrem()
     */
    public final void visit_lrem() {
        //TODO: port to orp-style
        vstack.push(eContext);
        Item v2 = vstack.pop(Item.JvmType.LONG);
        Item v1 = vstack.pop(Item.JvmType.LONG);
        v2.release1(eContext);
        v1.release1(eContext);
        vstack.push1(LongItem.createStack());

        helper.invokeJavaMethod(context.getLremMethod());
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lreturn()
     */
    public final void visit_lreturn() {
        //TODO: port to orp-style
        vstack.push(eContext);
        Item v = vstack.pop(Item.JvmType.LONG);
        v.release1(eContext);

        helper.writePOP64(T0, T1);
        visit_return();
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lshl()
     */
    public final void visit_lshl() {
        //TODO: port to orp-style
        vstack.push(eContext);
        Item v2 = vstack.pop(Item.JvmType.INT);
        Item v1 = vstack.pop(Item.JvmType.LONG);
        v2.release1(eContext);
        v1.release1(eContext);
        vstack.push1(LongItem.createStack());

        helper.writePOP(Register.ECX); // Value 2
        helper.writePOP64(Register.EAX, Register.EDX); // Value 1
        os.writeAND(Register.ECX, 63);
        os.writeCMP_Const(Register.ECX, 32);
        Label gt32Label = new Label(curInstrLabel + "gt32");
        Label endLabel = new Label(curInstrLabel + "end");
        os.writeJCC(gt32Label, X86Constants.JAE); // JAE
        /** ECX < 32 */
        os.writeSHLD_CL(Register.EDX, Register.EAX);
        os.writeSHL_CL(Register.EAX);
        os.writeJMP(endLabel);
        /** ECX >= 32 */
        os.setObjectRef(gt32Label);
        os.writeMOV(INTSIZE, Register.EDX, Register.EAX);
        os.writeXOR(Register.EAX, Register.EAX);
        os.writeSHL_CL(Register.EDX);
        os.setObjectRef(endLabel);
        helper.writePUSH64(Register.EAX, Register.EDX);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lshr()
     */
    public final void visit_lshr() {
        //TODO: port to orp-style
        vstack.push(eContext);
        Item v2 = vstack.pop(Item.JvmType.INT);
        Item v1 = vstack.pop(Item.JvmType.LONG);
        v2.release1(eContext);
        v1.release1(eContext);
        vstack.push1(LongItem.createStack());

        helper.writePOP(Register.ECX); // Value 2
        helper.writePOP64(Register.EAX, Register.EDX); // Value 1
        os.writeAND(Register.ECX, 63);
        os.writeCMP_Const(Register.ECX, 32);
        Label gt32Label = new Label(curInstrLabel + "gt32");
        Label endLabel = new Label(curInstrLabel + "end");
        os.writeJCC(gt32Label, X86Constants.JAE); // JAE
        /** ECX < 32 */
        os.writeSHRD_CL(Register.EAX, Register.EDX);
        os.writeSAR_CL(Register.EDX);
        os.writeJMP(endLabel);
        /** ECX >= 32 */
        os.setObjectRef(gt32Label);
        os.writeMOV(INTSIZE, Register.EAX, Register.EDX);
        os.writeSAR(Register.EDX, 31);
        os.writeSAR_CL(Register.EAX);
        os.setObjectRef(endLabel);
        helper.writePUSH64(Register.EAX, Register.EDX);
    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lstore(int)
     */
    public final void visit_lstore(int index) {
        //TODO: port to orp-style
        vstack.push(eContext);
        Item v = vstack.pop(Item.JvmType.LONG);
        v.release1(eContext);

        int ebpOfs = stackFrame.getWideEbpOffset(index);

	// pin down other references to this local
        vstack.loadLocal(eContext, ebpOfs);

        helper.writePOP64(T0, T1);
        os.writeMOV(INTSIZE, FP, ebpOfs, T0);
        os.writeMOV(INTSIZE, FP, ebpOfs + 4, T1);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lsub()
     */
    public final void visit_lsub() {
        //TODO: port to orp-style
        vstack.push(eContext);
        Item v2 = vstack.pop(Item.JvmType.LONG);
        Item v1 = vstack.pop(Item.JvmType.LONG);
        v2.release1(eContext);
        v1.release1(eContext);
        vstack.push1(LongItem.createStack());

        helper.writePOP64(T0, T1); // Value 2
        helper.writePOP64(S0, S1); // Value 1
        os.writeSUB(S0, T0);
        os.writeSBB(S1, T1);
        helper.writePUSH64(S0, S1);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lushr()
     */
    public final void visit_lushr() {
        //TODO: port to orp-style
        vstack.push(eContext);
        Item v2 = vstack.pop(Item.JvmType.INT);    // shift
        Item v1 = vstack.pop(Item.JvmType.LONG);   // value
        v2.release1(eContext);
        v1.release1(eContext);
        vstack.push1(LongItem.createStack());

        helper.writePOP(Register.ECX); // Value 2
        helper.writePOP64(Register.EAX, Register.EDX); // Value 1
        os.writeAND(Register.ECX, 63);
        os.writeCMP_Const(Register.ECX, 32);
        Label gt32Label = new Label(curInstrLabel + "gt32");
        Label endLabel = new Label(curInstrLabel + "end");
        os.writeJCC(gt32Label, X86Constants.JAE); // JAE
        /** ECX < 32 */
        os.writeSHRD_CL(Register.EAX, Register.EDX);
        os.writeSHR_CL(Register.EDX);
        os.writeJMP(endLabel);
        /** ECX >= 32 */
        os.setObjectRef(gt32Label);
        os.writeMOV(INTSIZE, Register.EAX, Register.EDX);
        os.writeXOR(Register.EDX, Register.EDX);
        os.writeSHR_CL(Register.EAX);
        os.setObjectRef(endLabel);
        helper.writePUSH64(Register.EAX, Register.EDX);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lxor()
     */
    public final void visit_lxor() {
        //TODO: port to orp-style
        vstack.push(eContext);
        Item v2 = vstack.pop(Item.JvmType.LONG);
        Item v1 = vstack.pop(Item.JvmType.LONG);
        v2.release1(eContext);
        v1.release1(eContext);
        vstack.push1(LongItem.createStack());

        helper.writePOP64(T0, T1); // Value 2
        helper.writePOP64(S0, S1); // Value 1
        os.writeXOR(S0, T0);
        os.writeXOR(S1, T1);
        helper.writePUSH64(S0, S1);
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
        helper.writePUSH(10); /* type=int */
        helper.writePUSH(dimensions); /* elements */
        helper.invokeJavaMethod(context.getAllocPrimitiveArrayMethod());
        helper.writePOP(S1);
        // Dimension array is now in S1
        
        // Pop all dimensions (note the reverse order that allocMultiArray expects)
        for (int i = 0; i < dimensions; i++) {
            final int ofs = (VmArray.DATA_OFFSET + i) * slotSize;
            IntItem v = vstack.popInt();
            v.release1(eContext);
            helper.writePOP(S1, ofs);
        }
        
        // Resolve the array class
        writeResolveAndLoadClassToEAX(clazz, S0);
        
        // Now call the multianewarrayhelper
        helper.writePUSH(EAX); // array-class
        helper.writePUSH(S1); // dimensions[]
        helper.invokeJavaMethod(context.getAllocMultiArrayMethod());
        // Result is now on the stack
        vstack.push1(RefItem.createStack());
    }

    /**
     * @param classRef
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_new(org.jnode.vm.classmgr.VmConstClass)
     */
    public final void visit_new(VmConstClass classRef) {
        vstack.push(eContext);

        writeResolveAndLoadClassToEAX(classRef, S0);
        /* Setup a call to SoftByteCodes.allocObject */
        helper.writePUSH(Register.EAX); /* vmClass */
        helper.writePUSH(-1); /* Size */
        helper.invokeJavaMethod(context.getAllocObjectMethod());
        /* Result is already on the stack */
        vstack.push1(RefItem.createStack());
    }

    /**
     * @param type
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_newarray(int)
     */
    public final void visit_newarray(int type) {
        IntItem count = vstack.popInt();
        count.loadIf(eContext, Item.Kind.STACK);
	vstack.push(eContext);  /* flush stack, result also on stack */

        /* Setup a call to SoftByteCodes.allocArray */
        helper.writePUSH(type); /* type */
        count.push(eContext);   /* count */
	count.release1(eContext);  // release and remove parameter from stack

        helper.invokeJavaMethod(context.getAllocPrimitiveArrayMethod());
        /* Result is already on the stack */

        vstack.push1(RefItem.createStack());
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_nop()
     */
    public final void visit_nop() {
        os.writeNOP();
    }

    private final void generic_pop(int size) {
        if (vstack.isEmpty()) {
            os.writeLEA(SP, SP, size);
	} else {
            Item v = vstack.pop();
            assertCondition(v.getCategory() == (size >> 2));
            if (v.getKind() == Item.Kind.STACK) {
		// sanity check
		if (VirtualStack.checkOperandStack) {
		    vstack.popFromOperandStack(v);
		}
                os.writeLEA(SP, SP, size);
            } 
            v.release(eContext);
	}
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
        //TODO: port to orp-style
        fieldRef.resolve(loader);
        final VmField field = fieldRef.getResolvedVmField();
        if (field.isStatic()) { throw new IncompatibleClassChangeError(
                "getfield called on static field " + fieldRef.getName()); }
        final VmInstanceField inf = (VmInstanceField) field;
        final int offset = inf.getOffset();
        final boolean wide = fieldRef.isWide();

        vstack.push(eContext);
	if (!vstack.isEmpty()) {
            Item val = vstack.pop();
	    if (VirtualStack.checkOperandStack) {
		    vstack.popFromOperandStack(val);
	    }
	    assertCondition(val.getCategory() == ((wide) ? 2 : 1));
	    if (!vstack.isEmpty()) {
                RefItem ref = vstack.popRef();
		    if (VirtualStack.checkOperandStack) {
			    vstack.popFromOperandStack(ref);
		    }
		// in fact, should release val first, in case they are on
		// stack, but the invariant allows this (if ref is on stack,
		// then so is val)
                ref.release(eContext);
	    }
            val.release(eContext);
	}

        if (!wide) {
            /* Value -> T0 */
            helper.writePOP(T0);
            /* Objectref -> S0 */
            helper.writePOP(S0); // Objectref
            os.writeMOV(INTSIZE, S0, offset, T0);
            helper.writePutfieldWriteBarrier(inf, S0, T0, S1);

        } else {
            /* Value LSB -> T0 */
            helper.writePOP(T0);
            /* Value MSB -> T1 */
            helper.writePOP(T1);
            /* Objectref -> S0 */
            helper.writePOP(S0); // Objectref
            /** Msb */
            os.writeMOV(INTSIZE, S0, offset + 4, T1);
            /** Lsb */
            os.writeMOV(INTSIZE, S0, offset, T0);
        }
    }

    /**
     * @param fieldRef
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_putstatic(org.jnode.vm.classmgr.VmConstFieldRef)
     */
    public final void visit_putstatic(VmConstFieldRef fieldRef) {
        //TODO: port to orp-style
	if (!vstack.isEmpty()) {
            vstack.push(eContext);
            Item val = vstack.pop();
            val.release1(eContext);
	}

        fieldRef.resolve(loader);
        final VmStaticField sf = (VmStaticField) fieldRef.getResolvedVmField();

        if (!sf.getDeclaringClass().isInitialized()) {
            writeInitializeClass(fieldRef, S0);
        }
        // Put static field
        if (!fieldRef.isWide()) {
            helper.writePOP(T0);
            helper.writePutStaticsEntry(curInstrLabel, T0, sf);
            helper.writePutstaticWriteBarrier(sf, T0, S1);
        } else {
            helper.writePOP64(T0, T1);
            helper.writePutStaticsEntry64(curInstrLabel, T0, T1, sf);
        }
    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ret(int)
     */
    public final void visit_ret(int index) {
        final int ebpOfs = stackFrame.getEbpOffset(index);
        os.writeMOV(INTSIZE, T0, FP, ebpOfs);
        os.writeJMP(T0);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_return()
     */
    public final void visit_return() {
        stackFrame.emitReturn();
        assertCondition(vstack.isEmpty());
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
        if (idx.getKind() == Item.Kind.CONSTANT) {
            final int i = idx.getValue();
            os.writeMOV(WORDSIZE, r, r, i + VmArray.DATA_OFFSET * 4);
        } else {
            os.writeMOV(WORDSIZE, r, r, idx.getRegister(), 2,
                    VmArray.DATA_OFFSET * 4);
            idx.release(eContext);
        }
        os.writeMOVSX(r, r, WORDSIZE);
        final IntItem result = IntItem.createRegister(r);
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
        Item v1 = vstack.pop();
        Item v2 = vstack.pop();
        assertCondition((v1.getCategory() == 1) && (v2.getCategory() == 1));
        //TODO: handle floats
        assertCondition((v1.getType() != Item.JvmType.FLOAT));
        final boolean v1IsBool = (v1.getKind() == Item.Kind.STACK);
        final boolean v2IsBool = (v2.getKind() == Item.Kind.STACK);
        if (v1IsBool || v2IsBool) {
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
        IntItem v = vstack.popInt();
        v.load(eContext);
        final Register r = v.getRegister();
        vstack.push(eContext);

        final int n = addresses.length;
        //TODO: port optimized version of L1
        // Space wasting, but simple implementation
        for (int i = 0; i < n; i++) {
            os.writeCMP_Const(r, lowValue + i);
            os.writeJCC(helper.getInstrLabel(addresses[ i]), X86Constants.JE); // JE
        }
        os.writeJMP(helper.getInstrLabel(defAddress));

        v.release(eContext);
    }

    /**
     * Emit code to validate an index of a given array
     * 
     * @param ref
     * @param index
     */
    private final void checkBounds(RefItem ref, IntItem index) {
        /*
         * helper.writePUSH(arrayRef, VmArray.LENGTH_OFFSET * slotSize);
         * os.writeDEC(SP, 0); helper.writePUSH(0); os.writeBOUND(index, SP,
         * 0); os.writeLEA(SP, SP, 8);
         */
        final Label ok = new Label(curInstrLabel + "$$cbok");
        // CMP length, index
        assertCondition(ref.getKind() == Item.Kind.REGISTER);
        final Register r = ref.getRegister();
        if (index.getKind() == Item.Kind.CONSTANT) {
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
    //REFACTOR: remove this method
    private final void checkBounds(Register arrayRef, Register index) {
        /*
         * 
         * slotSize); os.writeDEC(SP, 0); helper.writePUSH(0);
         * os.writeBOUND(index, SP, 0);
         */
        final Label ok = new Label(curInstrLabel + "$$cbok");
        // CMP length, index
        os.writeCMP(arrayRef, VmArray.LENGTH_OFFSET * slotSize, index);
        os.writeJCC(ok, X86Constants.JA);
        // Signal ArrayIndexOutOfBounds
        os.writeINT(5);
        os.setObjectRef(ok);
    }

    /**
     * Write code to resolve the given constant field referred to by fieldRef
     * 
     * @param fieldRef
     * @param scratch
     */
    private final void writeInitializeClass(VmConstFieldRef fieldRef,
            Register scratch) {
        //TODO: port to orp-style
        // Get fieldRef via constantpool to avoid direct object references in
        // the native code
        if (scratch == EAX) { throw new IllegalArgumentException(
                "scratch cannot be equal to EAX"); }

        final VmType declClass = fieldRef.getResolvedVmField()
                .getDeclaringClass();
        if (!declClass.isInitialized()) {
            // Now look for class initialization
            // Load classRef into EAX
            // Load the class from the statics table
            helper.writeGetStaticsEntry(new Label(curInstrLabel + "$$ic"), EAX,
                    declClass);

            // Load declaringClass.typeState into scratch
            os.writeMOV(INTSIZE, scratch, EAX, context.getVmTypeState()
                    .getOffset());
            // Test for initialized
            os.writeTEST(scratch, VmTypeState.ST_INITIALIZED);
            final Label afterInit = new Label(curInstrLabel + "$$aci");
            os.writeJCC(afterInit, X86Constants.JNZ);
            // Call cls.initialize
            helper.writePUSH(EAX);
            helper.invokeJavaMethod(context.getVmTypeInitialize());
            os.setObjectRef(afterInit);
        }

    }

    /**
     * Write code to resolve the given constant class (if needed) and load the
     * resolved class (VmType instance) into EAX.
     * 
     * @param classRef
     * @param scratch
     */
    private final void writeResolveAndLoadClassToEAX(VmConstClass classRef,
            Register scratch) {
        //TODO: port to orp-style
        // Check assertConditionions
        if (scratch == EAX) { throw new IllegalArgumentException(
                "scratch cannot be equal to EAX"); }

        // Resolve the class
        classRef.resolve(loader);
        final VmType type = classRef.getResolvedVmClass();

        // Load the class from the statics table
        helper.writeGetStaticsEntry(curInstrLabel, EAX, type);
    }

    /**
     * Emit the core of the instanceof code Input: ECX objectref EAX vmType
     * 
     * @param trueLabel
     *            Where to jump for a true result. A false result will continue
     *            directly after this method
     */
    private final void instanceOf(Label trueLabel) {
        //TODO: port to orp-style
        final Label loopLabel = new Label(this.curInstrLabel + "loop");
        final Label notInstanceOfLabel = new Label(this.curInstrLabel
                + "notInstanceOf");

        /* Is objectref null? */
        os.writeTEST(ECX, ECX);
        os.writeJCC(notInstanceOfLabel, X86Constants.JZ);
        /* vmType -> edx */
        os.writeMOV(INTSIZE, Register.EDX, Register.EAX);
        /* TIB -> ESI */
        os.writeMOV(INTSIZE, Register.ESI, Register.ECX, ObjectLayout.TIB_SLOT
                * slotSize);
        /* SuperClassesArray -> ESI */
        os
                .writeMOV(INTSIZE, Register.ESI, Register.ESI,
                        (VmArray.DATA_OFFSET + TIBLayout.SUPERCLASSES_INDEX)
                                * slotSize);
        /* SuperClassesArray.length -> ECX */
        os.writeMOV(INTSIZE, Register.ECX, Register.ESI, VmArray.LENGTH_OFFSET
                * slotSize);
        /* &superClassesArray[0] -> esi */
        os.writeLEA(Register.ESI, Register.ESI, VmArray.DATA_OFFSET * slotSize);

        os.setObjectRef(loopLabel);
        /* superClassesArray[index++] -> eax */
        os.writeLODSD();
        /* Is equal? */
        os.writeCMP(Register.EAX, Register.EDX);
        os.writeJCC(trueLabel, X86Constants.JE);
        try {
            os.writeLOOP(loopLabel);
        } catch (UnresolvedObjectRefException ex) {
            throw new CompileError(ex);
        }
        os.setObjectRef(notInstanceOfLabel);
    }

    /**
     * Insert a yieldpoint into the code
     */
    public final void yieldPoint() {
        helper.writeYieldPoint(curInstrLabel);
    }
}
