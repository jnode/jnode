/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.Label;
import org.jnode.assembler.NativeStream;
import org.jnode.assembler.x86.AbstractX86Stream;
import org.jnode.assembler.x86.Register;
import org.jnode.assembler.x86.X86Constants;
import org.jnode.assembler.x86.X86Operation;
import org.jnode.system.BootLog;
import org.jnode.vm.JvmType;
import org.jnode.vm.SoftByteCodes;
import org.jnode.vm.Vm;
import org.jnode.vm.bytecode.BasicBlock;
import org.jnode.vm.bytecode.BytecodeParser;
import org.jnode.vm.bytecode.TypeStack;
import org.jnode.vm.classmgr.ObjectLayout;
import org.jnode.vm.classmgr.Signature;
import org.jnode.vm.classmgr.TIBLayout;
import org.jnode.vm.classmgr.VmArray;
import org.jnode.vm.classmgr.VmClassLoader;
import org.jnode.vm.classmgr.VmClassType;
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
import org.jnode.vm.compiler.CompiledMethod;
import org.jnode.vm.compiler.InlineBytecodeVisitor;
import org.jnode.vm.x86.VmX86Architecture;
import org.jnode.vm.x86.compiler.X86CompilerConstants;
import org.jnode.vm.x86.compiler.X86CompilerContext;
import org.jnode.vm.x86.compiler.X86CompilerHelper;
import org.jnode.vm.x86.compiler.X86IMTCompiler;
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

    /** If true, do additional verifications. Helps to develop this compiler */
    private static final boolean paranoia = false;

    /** Offset in bytes of the first data entry within an array-object */
    private final int arrayDataOffset;

    /** Offset in bytes of the array-length value within an array-object */
    private final int arrayLengthOffset;

    /** The destination compiled method */
    private final CompiledMethod cm;

    /** Current context */
    private final X86CompilerContext context;

    /** Bytecode Address of current instruction */
    private int curAddress;

    /** Label of current instruction */
    private Label curInstrLabel;

    /** The method currently being compiled */
    private VmMethod currentMethod;

    /** The emitter context */
    private final EmitterContext eContext;

    /** Helper class */
    private final X86CompilerHelper helper;

    /** The method currently being inline (or null for none) */
    private InlinedMethodInfo inlinedMethod;

    /** Class loader */
    private VmClassLoader loader;

    /** Emit logging info */
    private final boolean log;

    /** Maximum number of local variable slots */
    private int maxLocals;

    /** The output stream */
    private final AbstractX86Stream os;

    /** Should we set the current instruction label on startInstruction? */
    private boolean setCurInstrLabel;

    /** Size of an object reference */
    private final int slotSize;

    /** Stackframe utility */
    private X86StackFrame stackFrame;

    /** Is this instruction the start of a basic block */
    private boolean startOfBB;

    /** Length of os at start of method */
    private int startOffset;

    /** Offset in bytes of the TIB reference within an object */
    private final int tibOffset;
    
    /** Item factory */
    private final ItemFactory ifac;
    
    private final MagicHelper magicHelper;

    /**
     * Virtual Stack: this stack contains values that have been computed but not
     * emitted yet; emission is delayed to allow for optimizations, in
     * particular using registers instead of stack operations.
     * 
     * The vstack is valid only inside a basic block; items in the stack are
     * flushed at the end of the basic block.
     * 
     * Aliasing: modifying a value that is still on the stack is forbidden. Each
     * time a local is assigned, the stack is checked for aliases. For the same
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
            boolean isBootstrap, X86CompilerContext context, MagicHelper magicHelper) {
        this.os = (AbstractX86Stream) outputStream;
        this.context = context;
        this.magicHelper = magicHelper;
        this.vstack = new VirtualStack(os);
        final X86RegisterPool pool = new X86RegisterPool();
        this.ifac = ItemFactory.getFactory();
        this.helper = new X86CompilerHelper(os, vstack.createStackMgr(pool, ifac),
                context, isBootstrap);
        this.cm = cm;
        this.slotSize = VmX86Architecture.SLOT_SIZE;
        this.arrayLengthOffset = VmArray.LENGTH_OFFSET * slotSize;
        this.arrayDataOffset = VmArray.DATA_OFFSET * slotSize;
        this.tibOffset = ObjectLayout.TIB_SLOT * slotSize;
        this.log = os.isLogEnabled();
        this.eContext = new EmitterContext(os, helper, vstack, pool, ifac);
    }

    private final void assertCondition(boolean cond, String message) {
        if (!cond)
            throw new Error("assert failed at addresss " + curAddress + ": "
                    + message);
    }

    private final void assertCondition(boolean cond, String message,
            Object param) {
        if (!cond)
            throw new Error("assert failed at addresss " + curAddress + ": "
                    + message + param);
    }

    /**
     * Emit code to validate an index of a given array
     * 
     * @param ref
     * @param index
     */
    private final void checkBounds(RefItem ref, IntItem index) {
        final Label test = new Label(curInstrLabel + "$$cbtest");
        final Label failed = new Label(curInstrLabel + "$$cbfailed");

        assertCondition(ref.isRegister(), "ref must be in a register");
        final Register refr = ref.getRegister();
        
        os.writeJMP(test);
        os.setObjectRef(failed);
    	// Call SoftByteCodes.throwArrayOutOfBounds
    	os.writePUSH(refr);
    	if (index.isConstant()) {
    		os.writePUSH(index.getValue());
    	} else {
    		os.writePUSH(index.getRegister());
    	}
    	invokeJavaMethod(context.getThrowArrayOutOfBounds());
        
        // CMP length, index
        os.setObjectRef(test);
        if (index.isConstant()) {
            os.writeCMP_Const(refr, arrayLengthOffset, index.getValue());
        } else {
            os.writeCMP(refr, arrayLengthOffset, index.getRegister());
        }
        os.writeJCC(failed, X86Constants.JNA);
    }

    /**
     * Remove all method arguments of the vstack.
     * 
     * @param method
     * @param hasSelf
     */
    private final void dropParameters(VmMethod method, boolean hasSelf) {
        final int[] argTypes = JvmType.getArgumentTypes(method.getSignature());
        final int count = argTypes.length;
        for (int i = count - 1; i >= 0; i--) {
            final int type = argTypes[i];
            final Item v = vstack.pop(JvmType.TypeToContainingType(type));
            v.release1(eContext);
        }
        if (hasSelf) {
            RefItem v = vstack.popRef();
            v.release1(eContext);
        }
    }

    /**
     * Store a double word item into an array.
     * 
     * @see #visit_dastore()
     * @see #visit_lastore()
     */
    private final void dwastore(int jvmType) {
        final DoubleWordItem val = (DoubleWordItem) vstack.pop(jvmType);
        final IntItem idx = vstack.popInt();
        final RefItem ref = vstack.popRef();

        val.load(eContext);
        idx.loadIf(eContext, ~Item.Kind.CONSTANT);
        ref.load(eContext);
        final Register refr = ref.getRegister();

        // Check bound
        checkBounds(ref, idx);

        // Store
        loadArrayEntryOffset(refr, ref, idx, 8);
        os.writeMOV(INTSIZE, refr, 0, val.getLsbRegister());
        os.writeMOV(INTSIZE, refr, 4, val.getMsbRegister());

        // Release
        ref.release(eContext);
        idx.release(eContext);
        val.release(eContext);
    }

    /**
     * Pop a word item of the stack and return it to the caller
     * 
     * @param JvmType
     */
    private final void dwreturn(int jvmType) {
        final DoubleWordItem val = (DoubleWordItem) vstack.pop(jvmType);

        // Return value must be in EAX:EDX
        if (!(val.uses(EAX) && val.uses(EDX))) {
            if (val.uses(EAX) || val.uses(EDX)) {
                val.push(eContext);
            }
            L1AHelper.requestRegister(eContext, EAX, val);
            L1AHelper.requestRegister(eContext, EDX, val);
            val.loadTo(eContext, EAX, EDX);
        }

        // Release
        val.release(eContext);

        // Do actual return
        visit_return();
    }

    /**
     * Store a double word item into a local variable
     * 
     * @param jvmType
     * @param index
     */
    private final void dwstore(int jvmType, int index) {
        final int disp = stackFrame.getWideEbpOffset(index);

        // Pin down (load) other references to this local
        vstack.loadLocal(eContext, disp);

        // Load
        final DoubleWordItem val = (DoubleWordItem) vstack.pop(jvmType);
        final boolean vconst = val.isConstant();
        if (vconst && (jvmType == JvmType.LONG)) {
            // Store constant long
            final long lval = ((LongItem) val).getValue();
            os.writeMOV_Const(FP, disp + LSB, (int) (lval & 0xFFFFFFFFL));
            os.writeMOV_Const(FP, disp + MSB,
                    (int) ((lval >>> 32) & 0xFFFFFFFFL));
        } else if (vconst && (jvmType == JvmType.DOUBLE)) {
            // Store constant double
            final long lval = Double.doubleToRawLongBits(((DoubleItem) val)
                    .getValue());
            os.writeMOV_Const(FP, disp + LSB, (int) (lval & 0xFFFFFFFFL));
            os.writeMOV_Const(FP, disp + MSB,
                    (int) ((lval >>> 32) & 0xFFFFFFFFL));
        } else if (val.isFPUStack()) {
            // Ensure item is on top of fpu stack
            FPUHelper.fxch(os, vstack.fpuStack, val);
            if (jvmType == JvmType.DOUBLE) {
                os.writeFSTP64(FP, disp);
            } else {
                os.writeFISTP64(FP, disp);
            }
            vstack.fpuStack.pop(val);
        } else if (val.isStack()) {
            // Must be top of stack
            if (VirtualStack.checkOperandStack) {
                vstack.operandStack.pop(val);
            }
            os.writePOP(FP, disp + LSB);
            os.writePOP(FP, disp + MSB);
        } else {
            // Load into register
            val.load(eContext);
            final Register lsb = val.getLsbRegister();
            final Register msb = val.getMsbRegister();
            // Store
            os.writeMOV(INTSIZE, FP, disp + LSB, lsb);
            os.writeMOV(INTSIZE, FP, disp + MSB, msb);
        }

        // Release
        val.release(eContext);
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
        os.setObjectRef(inlinedMethod.getEndOfInlineLabel());
        this.currentMethod = previousMethod;
        inlinedMethod.pushExitStack(ifac, vstack);
        this.inlinedMethod = null;
        if (debug) {
            BootLog.debug("endInlinedMethod");
        }
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#endInstruction()
     */
    public void endInstruction() {
        // In debug mode, do a lot of verifications
        if (paranoia) {

            // Verify the register usage
            // No registers can be in use, unless they are on the virtual stack.
            final X86RegisterPool pool = eContext.getPool();
            pool.visitUsedRegisters(new RegisterVisitor() {

                public void visit(Register reg) {
                    if (!vstack.uses(reg)) {
                        throw new InternalError("Register " + reg
                                + " is in use outsite of the vstack in method "
                                + currentMethod + " at bytecode address "
                                + curAddress);
                    }
                }
            });

            // All items on the FPU stack must also be on the vstack.
            vstack.fpuStack.visitItems(new ItemVisitor() {
                public void visit(Item item) {
                    if (!vstack.contains(item)) {
                        throw new InternalError(
                                "Item "
                                        + item
                                        + " is not on the vstack, but still of the fpu stack in method "
                                        + currentMethod + " at address "
                                        + curAddress);
                    }
                }
            });

            // No item on the vstack may have been released (kind==0)
            vstack.visitItems(new ItemVisitor() {
                public void visit(Item item) {
                    if (item.getKind() == 0) {
                        throw new InternalError("Item " + item
                                + " is kind 0 in method " + currentMethod
                                + " at address " + curAddress);
                    }
                    if (item.isRegister()) {
                        if (item instanceof WordItem) {
                            if (pool.getOwner(((WordItem) item).getRegister()) != item) {
                                throw new InternalError(
                                        "Item "
                                                + item
                                                + " uses a register which is not registered in the register ppol in method "
                                                + currentMethod
                                                + " at address " + curAddress);
                            }
                        } else {
                            if (pool.getOwner(((DoubleWordItem) item)
                                    .getLsbRegister()) != item) {
                                throw new InternalError(
                                        "Item "
                                                + item
                                                + " uses an LSB register which is not registered in the register ppol in method "
                                                + currentMethod
                                                + " at address " + curAddress);
                            }
                            if (pool.getOwner(((DoubleWordItem) item)
                                    .getMsbRegister()) != item) {
                                throw new InternalError(
                                        "Item "
                                                + item
                                                + " uses an MSB register which is not registered in the register ppol in method "
                                                + currentMethod
                                                + " at address " + curAddress);
                            }
                        }
                    }
                }
            });
        }
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
     *            directly after this method Register ECX must be free and it
     *            destroyed.
     */
    private final void instanceOfClass(Register objectr, VmClassType type,
            Register tmpr, Register resultr, Label trueLabel, boolean skipNullTest) {

    	final int depth = type.getSuperClassDepth();
    	final int staticsOfs = arrayDataOffset + (type.getStaticsIndex() << 2);
        final Label notInstanceOfLabel = new Label(this.curInstrLabel
                + "notInstanceOf");

    	// Clear result (means !instanceof)
        if (resultr != null) {
        	os.writeXOR(resultr, resultr);
        }
        // Test objectr == null
        if (!skipNullTest) {
        	// Is objectr null?
        	os.writeTEST(objectr, objectr);
            os.writeJCC(notInstanceOfLabel, X86Constants.JZ);
        }
    	// TIB -> tmp 
        os.writeMOV(INTSIZE, tmpr, objectr, tibOffset);
        // SuperClassesArray -> tmp 
        os.writeMOV(INTSIZE, tmpr, tmpr, arrayDataOffset
                + (TIBLayout.SUPERCLASSES_INDEX * slotSize));
        // Length of superclassarray must be >= depth
        os.writeCMP_Const(tmpr, arrayLengthOffset, depth);
        os.writeJCC(notInstanceOfLabel, X86Constants.JNA);
        // Get superClassesArray[depth] -> objectr
        os.writeMOV(INTSIZE, tmpr, tmpr, arrayDataOffset + (depth << 2));
        // Compare objectr with classtype
        os.writeCMP(STATICS, staticsOfs, tmpr);
        if (resultr != null) {
        	os.writeSETCC(resultr, X86Constants.JE);
        } else {
        	// Conditional forward jump is assumed not to be taken.
        	// Therefor will the JCC followed by a JMP be faster.
        	os.writeJCC(notInstanceOfLabel, X86Constants.JNE);
        	os.writeJMP(trueLabel);
        }
        os.setObjectRef(notInstanceOfLabel);
    }

    /**
     * Emit the core of the instanceof code.
     * 
     * @param objectr
     *            Register containing the object reference
     * @param typer
     *            Register containing the type reference
     * @param trueLabel
     *            Where to jump for a true result. A false result will continue
     *            directly after this method Register ECX must be free and it
     *            destroyed.
     */
    private final void instanceOf(Register objectr, Register typer,
            Register tmpr, Register cntr, Label trueLabel, boolean skipNullTest) {
        final Label loopLabel = new Label(this.curInstrLabel + "loop");
        final Label notInstanceOfLabel = new Label(this.curInstrLabel
                + "notInstanceOf");

        if (!skipNullTest) {
            /* Is objectref null? */
            os.writeTEST(objectr, objectr);
            os.writeJCC(notInstanceOfLabel, X86Constants.JZ);
        }
        /* TIB -> tmp */
        os.writeMOV(INTSIZE, tmpr, objectr, tibOffset);
        /* SuperClassesArray -> tmp */
        os.writeMOV(INTSIZE, tmpr, tmpr, arrayDataOffset
                + (TIBLayout.SUPERCLASSES_INDEX * slotSize));
        /* SuperClassesArray.length -> cntr */
        os.writeMOV(INTSIZE, cntr, tmpr, arrayLengthOffset);
        /* &superClassesArray[cnt-1] -> tmpr */
        os.writeLEA(tmpr, tmpr, cntr, 4, arrayDataOffset - 4);

        os.setObjectRef(loopLabel);
        // cmp superClassesArray[index],type
        os.writeCMP(tmpr, 0, typer);
        // Is equal? 
        os.writeJCC(trueLabel, X86Constants.JE);
        // index--
        os.writeLEA(tmpr, tmpr, -slotSize);
        // cnt--
        os.writeDEC(cntr);
        // if (cnt == 0)
        os.writeJCC(notInstanceOfLabel, X86Constants.JZ);
        // Goto loop
        os.writeJMP(loopLabel);

        // Not instanceof
        os.setObjectRef(notInstanceOfLabel);
    }

    /**
     * Generate code to invoke the method with the given signature.
     * 
     * @param method
     */
    private final void invokeJavaMethod(String signature) {
        if (log) {
            os.log("VStack: " + vstack + ", method: " + signature);
        }
        helper.invokeJavaMethod(signature);
    }

    /**
     * Generate code to invoke the given method.
     * 
     * @param method
     */
    private final void invokeJavaMethod(VmMethod method) {
        if (log) {
            os.log("VStack: " + vstack + ", method: " + method);
        }
        helper.invokeJavaMethod(method);
    }

    /**
     * Write an integer operation.
     * 
     * @see org.jnode.assembler.x86.X86Operation
     * @param operation
     * @param commutative
     */
    private final void ioperation(int operation, boolean commutative) {
        IntItem v2 = vstack.popInt();
        IntItem v1 = vstack.popInt();
        if (prepareForOperation(v1, v2, commutative)) {
            // Swap
            final IntItem tmp = v2;
            v2 = v1;
            v1 = tmp;
        }

        final Register r1 = v1.getRegister();
        switch (v2.getKind()) {
        case Item.Kind.REGISTER:
            os.writeArithOp(operation, r1, v2.getRegister());
            break;
        case Item.Kind.LOCAL:
            os.writeArithOp(operation, r1, FP, v2.getOffsetToFP());
            break;
        case Item.Kind.CONSTANT:
            os.writeArithOp(operation, r1, v2.getValue());
            break;
        }
        v2.release(eContext);
        vstack.push(v1);
    }

    /**
     * Write a shift operation.
     * 
     * @param operation
     */
    private final void ishift(int operation) {
        final IntItem shift = vstack.popInt();
        final boolean isconst = shift.isConstant();

        if (!isconst && !shift.uses(ECX)) {
            L1AHelper.requestRegister(eContext, ECX, shift);
            shift.loadTo(eContext, ECX);
        }

        // Pop & load
        final IntItem val = vstack.popInt();
        val.load(eContext);

        final Register valr = val.getRegister();
        if (isconst) {
            final int imm8 = shift.getValue();
            os.writeShift(operation, valr, imm8);
        } else {
            os.writeShift_CL(operation, valr);
        }

        // Release
        shift.release(eContext);

        // Push result
        vstack.push(val);
    }

    /**
     * Emit code to load the effective address of an array entry into the
     * destination register.
     * 
     * @param dst
     * @param ref
     * @param index
     * @param scale
     */
    private final void loadArrayEntryOffset(Register dst, RefItem ref,
            IntItem index, int scale) {
        assertCondition(ref.isRegister(), "ref must be in a register");
        final Register refr = ref.getRegister();
        if (index.isConstant()) {
            final int offset = index.getValue() * scale;
            os.writeLEA(dst, refr, arrayDataOffset + offset);
        } else {
            os.writeLEA(dst, refr, index.getRegister(), scale, arrayDataOffset);
        }
    }

    /**
     * Write an long operation.
     * 
     * @see org.jnode.assembler.x86.X86Operation
     * @param operation
     * @param commutative
     */
    private final void loperation(int operationLsb, int operationMsb,
            boolean commutative) {
        LongItem v2 = vstack.popLong();
        LongItem v1 = vstack.popLong();
        if (prepareForOperation(v1, v2, commutative)) {
            // Swap
            final LongItem tmp = v2;
            v2 = v1;
            v1 = tmp;
        }

        final Register r1_lsb = v1.getLsbRegister();
        final Register r1_msb = v1.getMsbRegister();
        switch (v2.getKind()) {
        case Item.Kind.REGISTER:
            os.writeArithOp(operationLsb, r1_lsb, v2.getLsbRegister());
            os.writeArithOp(operationMsb, r1_msb, v2.getMsbRegister());
            break;
        case Item.Kind.LOCAL:
            os.writeArithOp(operationLsb, r1_lsb, FP, v2.getLsbOffsetToFP());
            os.writeArithOp(operationMsb, r1_msb, FP, v2.getMsbOffsetToFP());
            break;
        case Item.Kind.CONSTANT:
            os.writeArithOp(operationLsb, r1_lsb, v2.getLsbValue());
            os.writeArithOp(operationMsb, r1_msb, v2.getMsbValue());
            break;
        }

        // Release
        v2.release(eContext);

        // Push result
        vstack.push(v1);
    }

    /**
     * Prepare both operand for operand. At least one operand is loaded into a
     * register. The other operand is constant, local or register.
     * 
     * @param destAndSource
     * @param source
     * @param commutative
     * @return True if the operand must be swapped. when not commutative, false
     *         is always returned.
     */
    private final boolean prepareForOperation(Item destAndSource, Item source,
            boolean commutative) {
        // WARNING: source was on top of the virtual stack (thus higher than
        // destAndSource)
        // x86 can only deal with one complex argument
        // destAndSource must be a register
        source.loadIf(eContext, (Item.Kind.STACK | Item.Kind.FPUSTACK));
        destAndSource.load(eContext);
        return false;
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
            BootLog.debug("-- Start of BB " + bb);
        }
        startOfBB = true;
        this.vstack.reset();
        eContext.getPool().reset(os);
        // Push the result from the outer method stack on the vstack
        if (inlinedMethod != null) {
            inlinedMethod.pushOuterMethodStack(ifac, vstack);
        }
        // Push the items on the vstack the result from a previous basic block.
        final TypeStack tstack = bb.getStartStack();
        vstack.pushAll(ifac, tstack);

        if (debug) {
            BootLog.debug("-- VStack: " + vstack.toString());
        }
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
        this.inlinedMethod.setOuterMethodStack(vstack.asTypeStack());
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
        this.inlinedMethod = new InlinedMethodInfo(inlinedMethod, new Label(
                curInstrLabel + "_end_of_inline"));
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
        if (log) {
            os.log("#" + address);
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
        waload(JvmType.REFERENCE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_aastore()
     */
    public final void visit_aastore() {
        wastore(JvmType.REFERENCE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_aconst_null()
     */
    public final void visit_aconst_null() {
        vstack.push(ifac.createAConst(null));
    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_aload(int)
     */
    public final void visit_aload(int index) {
        vstack.push(ifac.createLocal(JvmType.REFERENCE, stackFrame.getEbpOffset(index)));
    }

    /**
     * @param classRef
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_anewarray(org.jnode.vm.classmgr.VmConstClass)
     */
    public final void visit_anewarray(VmConstClass classRef) {
        // Push all, since we're going to call other methods
        vstack.push(eContext);
        
        // Claim EAX, we're going to use it later
        L1AHelper.requestRegister(eContext, EAX);
        // Request tmp register
        final Register classr = L1AHelper.requestRegister(eContext,
                JvmType.INT, false);

        // Pop
        final IntItem cnt = vstack.popInt();

        // Load the count value
        cnt.load(eContext);
        final Register cntr = cnt.getRegister();

        // Resolve the class
        writeResolveAndLoadClassToReg(classRef, classr);

        // Release EAX so it can be used by invokeJavaMethod
        L1AHelper.releaseRegister(eContext, EAX);

        stackFrame.writePushMethodRef();
        os.writePUSH(classr); /* Class */
        os.writePUSH(cntr); /* Count */
        invokeJavaMethod(context.getAnewarrayMethod());
        /* Result is already push on the stack */

        // Release
        cnt.release(eContext);
        L1AHelper.releaseRegister(eContext, classr);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_areturn()
     */
    public final void visit_areturn() {
        wreturn(JvmType.REFERENCE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_arraylength()
     */
    public final void visit_arraylength() {
        final RefItem ref = vstack.popRef();
        final IntItem result = (IntItem) L1AHelper.requestWordRegister(
                eContext, JvmType.INT, false);

        // Load
        ref.load(eContext);
        final Register refr = ref.getRegister();
        final Register resultr = result.getRegister();

        // Get length
        os.writeMOV(INTSIZE, resultr, refr, arrayLengthOffset);

        // Release
        ref.release(eContext);

        // Push result
        vstack.push(result);
    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_astore(int)
     */
    public final void visit_astore(int index) {
        wstore(JvmType.REFERENCE, index);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_athrow()
     */
    public final void visit_athrow() {
        final RefItem ref = vstack.popRef();

        // Exception must be in EAX
        if (!ref.uses(EAX)) {
            L1AHelper.requestRegister(eContext, EAX, ref);
            ref.loadTo(eContext, EAX);
        }

        // Jump
        helper.writeJumpTableCALL(X86JumpTable.VM_ATHROW_OFS);

        // Release
        ref.release(eContext);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_baload()
     */
    public final void visit_baload() {
        waload(JvmType.BYTE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_bastore()
     */
    public final void visit_bastore() {
        wastore(JvmType.BYTE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_caload()
     */
    public final void visit_caload() {
        waload(JvmType.CHAR);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_castore()
     */
    public final void visit_castore() {
        wastore(JvmType.CHAR);
    }

    /**
     * @param classRef
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_checkcast(org.jnode.vm.classmgr.VmConstClass)
     */
    public final void visit_checkcast(VmConstClass classRef) {
    	// Resolve classRef
    	classRef.resolve(loader);
    	final VmType resolvedType = classRef.getResolvedVmClass();
    	
    	if (resolvedType.isInterface() || resolvedType.isArray()) {    
    		// ClassRef is an interface or array, do the slow test
    		
    		// Pre-claim ECX
    		L1AHelper.requestRegister(eContext, EAX);
    		
    		// check that top item is a reference
    		final RefItem ref = vstack.popRef();
    		
    		// Load the ref
    		ref.load(eContext);
    		final Register refr = ref.getRegister();
    		final Register classr = EAX;
    		final Register cntr = L1AHelper.requestRegister(eContext, JvmType.INT,
    				false);
    		final Register tmpr = L1AHelper.requestRegister(eContext, JvmType.INT,
    				false);
    		
    		// Resolve the class
    		writeResolveAndLoadClassToReg(classRef, classr);
    		
    		final Label okLabel = new Label(this.curInstrLabel + "cc-ok");
    		
    		/* Is objectref null? */
    		os.writeTEST(refr, refr);
    		os.writeJCC(okLabel, X86Constants.JZ);
    		/* Is instanceof? */
    		instanceOf(refr, classr, tmpr, cntr, okLabel, true);
    		/* Not instanceof */
    		
    		// Release temp registers here, so invokeJavaMethod can use it
    		L1AHelper.releaseRegister(eContext, cntr);
    		L1AHelper.releaseRegister(eContext, classr);
    		L1AHelper.releaseRegister(eContext, tmpr);
    		
    		// Call SoftByteCodes.systemException
    		os.writePUSH(SoftByteCodes.EX_CLASSCAST);
    		os.writePUSH(0);
    		invokeJavaMethod(context.getSystemExceptionMethod());
    		final RefItem exi = vstack.popRef();
    		assertCondition(exi.uses(EAX), "item must be in eax");
    		exi.release(eContext);
    		
    		/* Exception in EAX, throw it */
    		helper.writeJumpTableCALL(X86JumpTable.VM_ATHROW_OFS);
    		
    		/* Normal exit */
    		os.setObjectRef(okLabel);
    		
    		// Leave ref on stack
    		vstack.push(ref);
    	} else {
			// classRef is a class, do the fast test
    		
    		// Pre-claim EAX
    		L1AHelper.requestRegister(eContext, EAX);
    		
    		// check that top item is a reference
    		final RefItem ref = vstack.popRef();
    		
    		// Load the ref
    		ref.load(eContext);
    		final Register refr = ref.getRegister();
    		final Register tmpr = L1AHelper.requestRegister(eContext, JvmType.INT,
    				false);
    		
    		final Label okLabel = new Label(this.curInstrLabel + "cc-ok");
    		
    		// Is objectref null?
    		os.writeTEST(refr, refr);
    		os.writeJCC(okLabel, X86Constants.JZ);
    		// Is instanceof?
    		instanceOfClass(refr, (VmClassType)resolvedType, tmpr, null, okLabel, true);
    		// Not instanceof 
    		
    		// Release temp registers here, so invokeJavaMethod can use it
    		L1AHelper.releaseRegister(eContext, EAX);
    		L1AHelper.releaseRegister(eContext, tmpr);
    		
    		// Call SoftByteCodes.systemException
    		os.writePUSH(SoftByteCodes.EX_CLASSCAST);
    		os.writePUSH(0);
    		invokeJavaMethod(context.getSystemExceptionMethod());
    		final RefItem exi = vstack.popRef();
    		assertCondition(exi.uses(EAX), "item must be in eax");
    		exi.release(eContext);
    		
    		/* Exception in EAX, throw it */
    		helper.writeJumpTableCALL(X86JumpTable.VM_ATHROW_OFS);
    		
    		/* Normal exit */
    		os.setObjectRef(okLabel);
    		
    		// Leave ref on stack
    		vstack.push(ref);
			
		}
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
        final IntItem idx = vstack.popInt();
        final RefItem ref = vstack.popRef();

        idx.loadIf(eContext, ~Item.Kind.CONSTANT);
        ref.load(eContext);
        final Register refr = ref.getRegister();

        checkBounds(ref, idx);
        FPUHelper.ensureStackCapacity(os, eContext, vstack, 1);

        if (idx.isConstant()) {
            final int offset = idx.getValue() * 8;
            os.writeFLD64(refr, offset + arrayDataOffset);
        } else {
            final Register idxr = idx.getRegister();
            os.writeFLD64(refr, idxr, 8, arrayDataOffset);
        }

        // Release
        ref.release(eContext);
        idx.release(eContext);

        // Push result
        final Item result = ifac.createFPUStack(JvmType.DOUBLE);
        vstack.fpuStack.push(result);
        vstack.push(result);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dastore()
     */
    public final void visit_dastore() {
        dwastore(JvmType.DOUBLE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dcmpg()
     */
    public final void visit_dcmpg() {
        FPUHelper.compare(os, eContext, vstack, true, JvmType.DOUBLE,
                curInstrLabel);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dcmpl()
     */
    public final void visit_dcmpl() {
        FPUHelper.compare(os, eContext, vstack, false, JvmType.DOUBLE,
                curInstrLabel);
    }

    /**
     * @param value
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dconst(double)
     */
    public final void visit_dconst(double value) {
        vstack.push(ifac.createDConst(value));
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
        vstack.push(ifac.createLocal(JvmType.DOUBLE, stackFrame.getWideEbpOffset(index)));
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
        dwreturn(JvmType.DOUBLE);
    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dstore(int)
     */
    public final void visit_dstore(int index) {
        dwstore(JvmType.DOUBLE, index);
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
            vstack.push(v1.clone(eContext));
            vstack.push(v2);
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
        waload(JvmType.FLOAT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fastore()
     */
    public final void visit_fastore() {
        wastore(JvmType.FLOAT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fcmpg()
     */
    public final void visit_fcmpg() {
        FPUHelper.compare(os, eContext, vstack, true, JvmType.FLOAT,
                curInstrLabel);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fcmpl()
     */
    public final void visit_fcmpl() {
        FPUHelper.compare(os, eContext, vstack, false, JvmType.FLOAT,
                curInstrLabel);
    }

    /**
     * @param value
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fconst(float)
     */
    public final void visit_fconst(float value) {
        vstack.push(ifac.createFConst(value));
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
        vstack.push(ifac.createLocal(JvmType.FLOAT, stackFrame.getEbpOffset(index)));
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
        wreturn(JvmType.FLOAT);
    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fstore(int)
     */
    public final void visit_fstore(int index) {
        wstore(JvmType.FLOAT, index);
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
        fieldRef.resolve(loader);
        final VmField field = fieldRef.getResolvedVmField();
        if (field.isStatic()) {
            throw new IncompatibleClassChangeError(
                    "getfield called on static field " + fieldRef.getName());
        }
        final VmInstanceField inf = (VmInstanceField) field;
        final int fieldOffset = inf.getOffset();
        final int type = JvmType.SignatureToType(fieldRef.getSignature());
        final boolean isfloat = JvmType.isFloat(type);

        // Pop & load
        final RefItem ref = vstack.popRef();
        ref.load(eContext);
        final Register refr = ref.getRegister();

        // get field
        final Item result;
        if (!fieldRef.isWide()) {
            if (isfloat) {
                result = ifac.createFPUStack(JvmType.FLOAT);
                os.writeFLD32(refr, fieldOffset);
                vstack.fpuStack.push(result);
            } else {
                final WordItem iw = L1AHelper.requestWordRegister(eContext,
                        type, false);
                os.writeMOV(INTSIZE, iw.getRegister(), refr, fieldOffset);
                result = iw;
            }
        } else {
            if (isfloat) {
                result = ifac.createFPUStack(JvmType.DOUBLE);
                os.writeFLD64(refr, fieldOffset);
                vstack.fpuStack.push(result);
            } else {
                final DoubleWordItem idw = L1AHelper
                        .requestDoubleWordRegisters(eContext, type);
                final Register lsb = idw.getLsbRegister();
                final Register msb = idw.getMsbRegister();
                os.writeMOV(INTSIZE, lsb, refr, fieldOffset + LSB);
                os.writeMOV(INTSIZE, msb, refr, fieldOffset + MSB);
                result = idw;
            }
        }

        // Release
        ref.release(eContext);

        // Push result
        vstack.push(result);
    }

    /**
     * @param fieldRef
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_getstatic(org.jnode.vm.classmgr.VmConstFieldRef)
     */
    public final void visit_getstatic(VmConstFieldRef fieldRef) {
        fieldRef.resolve(loader);
        final int type = JvmType.SignatureToType(fieldRef.getSignature());
        final VmStaticField sf = (VmStaticField) fieldRef.getResolvedVmField();

        // Initialize if needed
        if (!sf.getDeclaringClass().isInitialized()) {
            final X86RegisterPool pool = eContext.getPool();
            final Register tmp = L1AHelper.requestRegister(eContext,
                    JvmType.INT, false);
            writeInitializeClass(fieldRef, tmp);
            pool.release(tmp);
        }

        // Get static field object
        if (JvmType.isFloat(type)) {
            final boolean is32bit = !fieldRef.isWide();
            helper.writeGetStaticsEntryToFPU(curInstrLabel, sf, is32bit);
            final Item result = ifac.createFPUStack(type);
            vstack.fpuStack.push(result);
            vstack.push(result);
        } else if (!fieldRef.isWide()) {
            final WordItem result = L1AHelper.requestWordRegister(eContext,
                    type, false);
            final Register resultr = result.getRegister();
            helper.writeGetStaticsEntry(curInstrLabel, resultr, sf);
            vstack.push(result);
        } else {
            final DoubleWordItem result = L1AHelper.requestDoubleWordRegisters(
                    eContext, type);
            final Register lsb = result.getLsbRegister();
            final Register msb = result.getMsbRegister();
            helper.writeGetStaticsEntry64(curInstrLabel, lsb, msb, sf);
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
        if (v.isConstant()) {
            vstack.push(ifac.createIConst((byte) v.getValue()));
        } else {
            v.loadToBITS8GPR(eContext);
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
        if (v.isConstant()) {
            vstack.push(ifac.createIConst((char) v.getValue()));
        } else {
            v.load(eContext);
            final Register r = v.getRegister();
            os.writeMOVZX(r, r, WORDSIZE);
            vstack.push(v);
        }
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2d()
     */
    public final void visit_i2d() {
        FPUHelper.convert(eContext, vstack, JvmType.INT, JvmType.DOUBLE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2f()
     */
    public final void visit_i2f() {
        FPUHelper.convert(eContext, vstack, JvmType.INT, JvmType.FLOAT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2l()
     */
    public final void visit_i2l() {
        final IntItem v = vstack.popInt();
        if (v.isConstant()) {
            vstack.push(ifac.createLConst(v.getValue()));
        } else {
            final X86RegisterPool pool = eContext.getPool();
            L1AHelper.requestRegister(eContext, EAX);
            L1AHelper.requestRegister(eContext, EDX);
            v.loadTo(eContext, EAX);

            final LongItem result = (LongItem)ifac.createReg(JvmType.LONG, EAX, EDX);

            os.writeCDQ(); /* Sign extend EAX -> EDX:EAX */
            pool.transferOwnerTo(EAX, result);
            pool.transferOwnerTo(EDX, result);

            // We do not release v, because its register (EAX) is re-used in
            // result

            // Push result
            vstack.push(result);
        }
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2s()
     */
    public final void visit_i2s() {
        final IntItem v = vstack.popInt();
        if (v.isConstant()) {
            vstack.push(ifac.createIConst((short) v.getValue()));
        } else {
            v.load(eContext);
            final Register r = v.getRegister();
            os.writeMOVSX(r, r, WORDSIZE);
            vstack.push(v);
        }
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iadd()
     */
    public final void visit_iadd() {
        ioperation(X86Operation.ADD, true);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iaload()
     */
    public final void visit_iaload() {
        waload(JvmType.INT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iand()
     */
    public final void visit_iand() {
        ioperation(X86Operation.AND, true);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iastore()
     */
    public final void visit_iastore() {
        wastore(JvmType.INT);
    }

    /**
     * @param value
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iconst(int)
     */
    public final void visit_iconst(int value) {
        vstack.push(ifac.createIConst(value));
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
        L1AHelper.requestRegister(eContext, EAX, v1);

        // We need to use EDX, so spill those items using it.
        v1.spillIfUsing(eContext, EDX);
        v2.spillIfUsing(eContext, EDX);
        L1AHelper.requestRegister(eContext, EDX);

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
        if (prepareForOperation(v1, v2, true)) {
            // Swap
            final RefItem tmp = v2;
            v2 = v1;
            v1 = tmp;
        }

        Register r1 = v1.getRegister();

        switch (v2.getKind()) {
        case Item.Kind.REGISTER:
            os.writeCMP(r1, v2.getRegister());
            break;
        case Item.Kind.LOCAL:
            os.writeCMP(r1, FP, v2.getOffsetToFP());
            break;
        case Item.Kind.CONSTANT:
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
        if (prepareForOperation(v1, v2, (jccOpcode == X86Constants.JE)
                || (jccOpcode == X86Constants.JNE))) {
            // Swap
            final IntItem tmp = v2;
            v2 = v1;
            v1 = tmp;
        }

        Register r1 = v1.getRegister();

        switch (v2.getKind()) {
        case Item.Kind.REGISTER:
            os.writeCMP(r1, v2.getRegister());
            break;
        case Item.Kind.LOCAL:
            os.writeCMP(r1, FP, v2.getOffsetToFP());
            break;
        case Item.Kind.CONSTANT:
            final int c2 = v2.getValue();
            os.writeCMP_Const(r1, c2);
            break;
        }
        v1.release(eContext);
        v2.release(eContext);
        os.writeJCC(helper.getInstrLabel(address), jccOpcode);
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

    private void visit_ifxx(int type, int address, int jccOpcode) {
        //IMPROVE: Local case
        final WordItem val = (WordItem) vstack.pop(type);
        if ((type == JvmType.INT) && val.isConstant()) {
            final int ival = ((IntItem) val).getValue();
            final boolean jump;
            switch (jccOpcode) {
            case X86Constants.JE:
                jump = (ival == 0);
                break;
            case X86Constants.JNE:
                jump = (ival != 0);
                break;
            case X86Constants.JL:
                jump = (ival < 0);
                break;
            case X86Constants.JGE:
                jump = (ival >= 0);
                break;
            case X86Constants.JG:
                jump = (ival > 0);
                break;
            case X86Constants.JLE:
                jump = (ival <= 0);
                break;
            default:
                throw new IllegalArgumentException("Unknown jccOpcode "
                        + jccOpcode);
            }
            if (jump) {
                // flush vstack before jumping
                vstack.push(eContext);
                // Actual jump
                os.writeJMP(helper.getInstrLabel(address));
            }
        } else {
            val.load(eContext);
            final Register valr = val.getRegister();
            // flush vstack before jumping
            vstack.push(eContext);

            os.writeTEST(valr, valr);
            os.writeJCC(helper.getInstrLabel(address), jccOpcode);
        }

        // Release
        val.release(eContext);
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
        vstack.push(ifac.createLocal(JvmType.INT, stackFrame.getEbpOffset(index)));
    }

    /**
     * Convert the given multiplier to a shift number.
     * @param val
     * @return -1 if not shiftable.
     */
    private final int getShiftForMultiplier(int val) {
    	int mul = 2;
    	for (int i = 1; i <= 31; i++) {
    		if (val == mul) {
    			return i;
    		}
    		mul <<= 1;
    	}
    	return -1;
    }
    
    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_imul()
     */
    public final void visit_imul() {
        IntItem v2 = vstack.popInt();
        IntItem v1 = vstack.popInt();
        if (prepareForOperation(v1, v2, true)) {
            // Swap
            final IntItem tmp = v2;
            v2 = v1;
            v1 = tmp;
        }

        final Register r1 = v1.getRegister();
        switch (v2.getKind()) {
        case Item.Kind.REGISTER:
            os.writeIMUL(r1, v2.getRegister());
            break;
        case Item.Kind.CONSTANT:
        	final int val = v2.getValue();
        	if (val == 0) {
        		os.writeXOR(r1, r1); // * 0
        	} else if (val == 1) {
        		// Do nothing 
        	} else if (val == -1) {
    			os.writeNEG(r1); // * -1
        	} else {
        		final int shift = getShiftForMultiplier(Math.abs(val));
        		if (shift > 0) {
        			// abs(val) is multiple of 2 && val=2^shift where shift <= 31
        			os.writeSAL(r1, shift);
        			if (val < 0) {
        				os.writeNEG(r1);
        			}
        		} else {
        			os.writeIMUL_3(r1, r1, val);
        		}
        	}
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
        final IntItem val = vstack.popInt();
        val.loadIf(eContext, ~Item.Kind.CONSTANT);
        if (val.isConstant()) {
            vstack.push(ifac.createIConst(-val.getValue()));
        } else {
            os.writeNEG(val.getRegister());
            vstack.push(val);
        }
    }

    /**
     * @see org.jnode.vm.compiler.InlineBytecodeVisitor#visit_inlinedReturn()
     */
    public void visit_inlinedReturn() {
        if (debug) {
            BootLog.debug("inlinedReturn");
        }
        vstack.push(eContext);
        inlinedMethod.setExitStack(vstack);
        os.writeJMP(inlinedMethod.getEndOfInlineLabel());
    }

    /**
     * @param classRef
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_instanceof(org.jnode.vm.classmgr.VmConstClass)
     */
    public final void visit_instanceof(VmConstClass classRef) {
    	// Resolve the classRef
    	classRef.resolve(loader);
    	
		// Prepare
		final X86RegisterPool pool = eContext.getPool();
		final VmType resolvedType = classRef.getResolvedVmClass();
		
		if (resolvedType.isInterface() || resolvedType.isArray()) {    
    		// It is an interface, do it the hard way
    		
    		// Load reference
    		final RefItem ref = vstack.popRef();
    		ref.load(eContext);
    		final Register refr = ref.getRegister();
    		
    		// Allocate tmp registers
    		final Register classr = L1AHelper.requestRegister(eContext,
    				JvmType.INT, false);
    		final Register cntr = L1AHelper.requestRegister(eContext, JvmType.INT,
    				false);
    		final Register tmpr = L1AHelper.requestRegister(eContext, JvmType.INT,
    				false);
    		
    		/* Objectref is already on the stack */
    		writeResolveAndLoadClassToReg(classRef, classr);
    		
    		final Label trueLabel = new Label(this.curInstrLabel + "io-true");
    		final Label endLabel = new Label(this.curInstrLabel + "io-end");
    		
    		/* Is instanceof? */
    		instanceOf(refr, classr, tmpr, cntr, trueLabel, false);
    		/* Not instanceof */
    		//TODO: use setcc instead of jumps
    		os.writeXOR(refr, refr);
    		os.writeJMP(endLabel);
    		
    		os.setObjectRef(trueLabel);
    		os.writeMOV_Const(refr, 1);
    		
    		// Push result
    		os.setObjectRef(endLabel);
    		ref.release(eContext);
    		L1AHelper.requestRegister(eContext, refr);
    		final IntItem result = (IntItem)ifac.createReg(JvmType.INT, refr);
    		pool.transferOwnerTo(refr, result);
    		vstack.push(result);
    		
    		// Release
    		pool.release(classr);
    		pool.release(tmpr);
    		pool.release(cntr);
    	} else {
    		// It is a class, do the fast way
    		
    		// Load reference
    		final RefItem ref = vstack.popRef();
    		ref.load(eContext);
    		final Register refr = ref.getRegister();
    		
    		// Allocate tmp registers
    		final Register tmpr = L1AHelper.requestRegister(eContext, JvmType.INT,
    				false);
    		final IntItem result = (IntItem)L1AHelper.requestWordRegister(eContext, JvmType.INT, false);
    		
    		// Is instanceof
    		instanceOfClass(refr, (VmClassType)classRef.getResolvedVmClass(), tmpr, result.getRegister(), null, false);

    		// Push result
    		vstack.push(result);
    		
    		// Release
    		ref.release(eContext);
    		pool.release(tmpr);
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
        vstack.push(eContext);

        // Resolve the method
        methodRef.resolve(loader);
        final VmMethod method = methodRef.getResolvedVmMethod();
        final int argSlotCount = count - 1;
        
        // remove parameters from vstack
        dropParameters(method, true);
        // Get objectref -> EAX
        os.writeMOV(INTSIZE, EAX, SP, argSlotCount * slotSize);
        // Write the actual invokeinterface
        X86IMTCompiler.emitInvokeInterface(os, method);
        // Write the push result
        helper.pushReturnValue(method.getSignature());
    }

    /**
     * @param methodRef
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_invokespecial(org.jnode.vm.classmgr.VmConstMethodRef)
     */
    public final void visit_invokespecial(VmConstMethodRef methodRef) {
        // Flush the stack before an invoke
        vstack.push(eContext);

        methodRef.resolve(loader);
        try {
            final VmMethod sm = methodRef.getResolvedVmMethod();

            dropParameters(sm, true);

            // Get method from statics table
            helper.writeGetStaticsEntry(curInstrLabel, EAX, sm);
            invokeJavaMethod(methodRef.getSignature());
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
        methodRef.resolve(loader);
        final VmStaticMethod method = (VmStaticMethod) methodRef
                .getResolvedVmMethod();

        if (method.getDeclaringClass().isMagicType()) {
            magicHelper.emitMagic(eContext, method, true);
        } else {
            // Flush the stack before an invoke
            vstack.push(eContext);

            dropParameters(method, false);
            
            // Get static field object
            helper.writeGetStaticsEntry(curInstrLabel, EAX, method);
            invokeJavaMethod(methodRef.getSignature());
            // Result is already on the stack.
        }
    }

    /**
     * @param methodRef
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_invokevirtual(org.jnode.vm.classmgr.VmConstMethodRef)
     */
    public final void visit_invokevirtual(VmConstMethodRef methodRef) {
        methodRef.resolve(loader);
        final VmMethod mts = methodRef.getResolvedVmMethod();

        if (mts.isStatic()) {
            throw new IncompatibleClassChangeError(
                    "Static method in invokevirtual");
        }
        final VmInstanceMethod method = (VmInstanceMethod) mts;
        if (method.getDeclaringClass().isMagicType()) {
            magicHelper.emitMagic(eContext, method, false);
        } else {
            //TODO: port to orp-style
            vstack.push(eContext);

            dropParameters(mts, true);

            final int tibIndex = method.getTibOffset();
            final int argSlotCount = Signature.getArgSlotCount(methodRef
                    .getSignature());
            
            /* Get objectref -> EAX */
            os.writeMOV(INTSIZE, EAX, SP, argSlotCount * slotSize);
            /* Get VMT of objectef -> EAX */
            os.writeMOV(INTSIZE, EAX, EAX, tibOffset);
            /* Get entry in VMT -> EAX */
            os.writeMOV(INTSIZE, EAX, EAX, arrayDataOffset + (tibIndex * slotSize));
            /* Now invoke the method */
            invokeJavaMethod(methodRef.getSignature());
            // Result is already on the stack.
        }
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ior()
     */
    public final void visit_ior() {
        ioperation(X86Operation.OR, true);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_irem()
     */
    public final void visit_irem() {
        // Pre-claim result in EDX
        L1AHelper.requestRegister(eContext, EDX);
        final IntItem result = (IntItem)ifac.createReg(JvmType.INT, EDX);
        eContext.getPool().transferOwnerTo(EDX, result);

        final IntItem v2 = vstack.popInt();
        final IntItem v1 = vstack.popInt();

        // v1 must be in EAX
        L1AHelper.requestRegister(eContext, EAX, v1);

        // Load
        v2.loadIf(eContext, ~Item.Kind.LOCAL);
        v1.loadTo(eContext, EAX);

        // Calculate
        os.writeCDQ(); // EAX -> EDX:EAX
        if (v2.isLocal()) {
            os.writeIDIV_EAX(FP, v2.getOffsetToFP());
        } else {
            os.writeIDIV_EAX(v2.getRegister());
        }

        // Result
        vstack.push(result);

        // Release
        v1.release(eContext);
        v2.release(eContext);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ireturn()
     */
    public final void visit_ireturn() {
        wreturn(JvmType.INT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ishl()
     */
    public final void visit_ishl() {
        ishift(X86Operation.SAL);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ishr()
     */
    public final void visit_ishr() {
        ishift(X86Operation.SAR);
    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_istore(int)
     */
    public final void visit_istore(int index) {
        wstore(JvmType.INT, index);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_isub()
     */
    public final void visit_isub() {
        ioperation(X86Operation.SUB, false);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iushr()
     */
    public final void visit_iushr() {
        ishift(X86Operation.SHR);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ixor()
     */
    public final void visit_ixor() {
        ioperation(X86Operation.XOR, true);
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
        FPUHelper.convert(eContext, vstack, JvmType.LONG, JvmType.DOUBLE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_l2f()
     */
    public final void visit_l2f() {
        FPUHelper.convert(eContext, vstack, JvmType.LONG, JvmType.FLOAT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_l2i()
     */
    public final void visit_l2i() {
        final LongItem v = vstack.popLong();
        if (v.isConstant()) {
            vstack.push(ifac.createIConst((int) v.getValue()));
        } else {
            final X86RegisterPool pool = eContext.getPool();
            v.load(eContext);
            final Register lsb = v.getLsbRegister();
            v.release(eContext);
            pool.request(lsb);
            final IntItem result = (IntItem)ifac.createReg(JvmType.INT, lsb);
            pool.transferOwnerTo(lsb, result);
            vstack.push(result);
        }
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ladd()
     */
    public final void visit_ladd() {
        loperation(X86Operation.ADD, X86Operation.ADC, true);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_laload()
     */
    public final void visit_laload() {
        final IntItem idx = vstack.popInt();
        final RefItem ref = vstack.popRef();

        // Load
        idx.load(eContext);
        ref.load(eContext);
        final Register idxr = idx.getRegister();
        final Register refr = ref.getRegister();

        // Verify
        checkBounds(ref, idx);

        // Get data
        final DoubleWordItem result = L1AHelper.requestDoubleWordRegisters(
                eContext, JvmType.LONG);
        os.writeLEA(refr, refr, idxr, 8, arrayDataOffset);
        os.writeMOV(INTSIZE, result.getLsbRegister(), refr, LSB);
        os.writeMOV(INTSIZE, result.getMsbRegister(), refr, MSB);

        // Result
        vstack.push(result);

        // Release
        idx.release(eContext);
        ref.release(eContext);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_land()
     */
    public final void visit_land() {
        loperation(X86Operation.AND, X86Operation.AND, true);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lastore()
     */
    public final void visit_lastore() {
        dwastore(JvmType.LONG);
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
        final IntItem result = (IntItem) L1AHelper.requestWordRegister(
                eContext, JvmType.INT, false);
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
        vstack.push(ifac.createLConst(v));
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
        vstack.push(ifac.createAConst(value));
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

        invokeJavaMethod(context.getLdivMethod());
    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lload(int)
     */
    public final void visit_lload(int index) {
        vstack.push(ifac.createLocal(JvmType.LONG, stackFrame.getWideEbpOffset(index)));
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
        final LongItem result = (LongItem) L1AHelper
                .requestDoubleWordRegisters(eContext, JvmType.LONG, EAX, EDX);
        vstack.push(result);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lneg()
     */
    public final void visit_lneg() {
        final LongItem v = vstack.popLong();

        if (v.isConstant()) {
            vstack.push(ifac.createLConst(-v.getValue()));
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

        final IntItem key = vstack.popInt();
        key.load(eContext);
        final Register keyr = key.getRegister();
        // Conservative assumption, flush stack
        vstack.push(eContext);
        key.release(eContext);

        for (int i = 0; i < n; i++) {
            os.writeCMP_Const(keyr, matchValues[i]);
            os.writeJCC(helper.getInstrLabel(addresses[i]), X86Constants.JE); // JE
        }
        os.writeJMP(helper.getInstrLabel(defAddress));

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lor()
     */
    public final void visit_lor() {
        loperation(X86Operation.OR, X86Operation.OR, true);
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

        invokeJavaMethod(context.getLremMethod());
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lreturn()
     */
    public final void visit_lreturn() {
        dwreturn(JvmType.LONG);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lshl()
     */
    public final void visit_lshl() {
        final IntItem v2 = vstack.popInt();
        final LongItem v1 = vstack.popLong();

        L1AHelper.requestRegister(eContext, ECX, v2);
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
            L1AHelper.requestRegister(eContext, ECX, cnt);
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
        final LongItem result = (LongItem)ifac.createReg(JvmType.LONG, lsb, msb);
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
        dwstore(JvmType.LONG, index);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lsub()
     */
    public final void visit_lsub() {
        loperation(X86Operation.SUB, X86Operation.SBB, false);
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
            L1AHelper.requestRegister(eContext, ECX, cnt);
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
        final LongItem result = (LongItem)ifac.createReg(JvmType.LONG, lsb, msb);
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
        loperation(X86Operation.XOR, X86Operation.XOR, true);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_monitorenter()
     */
    public final void visit_monitorenter() {
        vstack.push(eContext);
        final RefItem v = vstack.popRef();
        v.release1(eContext);

        // Objectref is already on the stack
        invokeJavaMethod(context.getMonitorEnterMethod());
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_monitorexit()
     */
    public final void visit_monitorexit() {
        vstack.push(eContext);
        final RefItem v = vstack.popRef();
        v.release1(eContext);

        // Objectref is already on the stack
        invokeJavaMethod(context.getMonitorExitMethod());
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
        invokeJavaMethod(context.getAllocPrimitiveArrayMethod());
        final RefItem dims = vstack.popRef();
        final Register dimsr = dims.getRegister();
        // Dimension array is now in dimsr

        // Pop all dimensions (note the reverse order that allocMultiArray
        // expects)
        for (int i = 0; i < dimensions; i++) {
            final int ofs = arrayDataOffset + (i * 4);
            final IntItem v = vstack.popInt();
            v.release1(eContext);
            os.writePOP(dimsr, ofs);
        }

        // Allocate tmp register
        final Register classr = L1AHelper.requestRegister(eContext,
                JvmType.REFERENCE, false);

        // Resolve the array class
        writeResolveAndLoadClassToReg(clazz, classr);

        // Release dims, because invokeJavaMethod needs EAX
        dims.release(eContext);

        // Now call the multianewarrayhelper
        os.writePUSH(classr); // array-class
        os.writePUSH(dimsr); // dimensions[]
        invokeJavaMethod(context.getAllocMultiArrayMethod());
        // Result is now on the vstack

        // Release
        L1AHelper.releaseRegister(eContext, classr);
    }

    /**
     * @param classRef
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_new(org.jnode.vm.classmgr.VmConstClass)
     */
    public final void visit_new(VmConstClass classRef) {
        // Push all
        vstack.push(eContext);

        // Allocate tmp register
        final Register classr = L1AHelper.requestRegister(eContext,
                JvmType.REFERENCE, false);

        writeResolveAndLoadClassToReg(classRef, classr);
        /* Setup a call to SoftByteCodes.allocObject */
        os.writePUSH(classr); /* vmClass */
        os.writePUSH(-1); /* Size */
        invokeJavaMethod(context.getAllocObjectMethod());
        // Result is already on the vstack

        // Release
        L1AHelper.releaseRegister(eContext, classr);
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

        invokeJavaMethod(context.getAllocPrimitiveArrayMethod());
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
        if (field.isStatic()) {
            throw new IncompatibleClassChangeError(
                    "getfield called on static field " + fieldRef.getName());
        }
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
            if (!inf.isPrimitive() && helper.needsWriteBarrier()) {
                final Register tmp = L1AHelper.requestRegister(eContext,
                        JvmType.INT, false);
                helper.writePutfieldWriteBarrier(inf, refr, valr, tmp);
                L1AHelper.releaseRegister(eContext, tmp);
            }
        } else {
            final DoubleWordItem dval = (DoubleWordItem) val;
            os.writeMOV(INTSIZE, refr, offset + MSB, dval.getMsbRegister());
            os.writeMOV(INTSIZE, refr, offset + LSB, dval.getLsbRegister());
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
            final Register tmp = L1AHelper.requestRegister(eContext,
                    JvmType.INT, false);
            writeInitializeClass(fieldRef, tmp);
            L1AHelper.releaseRegister(eContext, tmp);
        }

        // Get value
        final Item val = vstack.pop();
        val.load(eContext);

        // Put static field
        if (!fieldRef.isWide()) {
            final WordItem wval = (WordItem) val;
            final Register valr = wval.getRegister();

            helper.writePutStaticsEntry(curInstrLabel, valr, sf);
            if (!sf.isPrimitive() && helper.needsWriteBarrier()) {
                final Register tmp = L1AHelper.requestRegister(eContext,
                        JvmType.INT, false);
                helper.writePutstaticWriteBarrier(sf, valr, tmp);
                L1AHelper.releaseRegister(eContext, tmp);
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

        // Load ret & jmp
        os.writeJMP(FP, ebpOfs);
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
        waload(JvmType.SHORT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_sastore()
     */
    public final void visit_sastore() {
        wastore(JvmType.SHORT);
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
            os.writeJCC(helper.getInstrLabel(addresses[i]), X86Constants.JE); // JE
        }
        os.writeJMP(helper.getInstrLabel(defAddress));

        val.release(eContext);
    }

    /**
     * Load a WordItem out of an array.
     * 
     * @param jvmType
     *            Type of the array elements
     */
    private final void waload(int jvmType) {
        final IntItem idx = vstack.popInt();
        final RefItem ref = vstack.popRef();

        final int valSize;
        final int scale;
        final int resultType;
        final boolean isfloat = (jvmType == JvmType.FLOAT);
        switch (jvmType) {
        case JvmType.BYTE:
            valSize = BYTESIZE;
            scale = 1;
            resultType = JvmType.INT;
            break;
        case JvmType.CHAR:
        case JvmType.SHORT:
            valSize = WORDSIZE;
            scale = 2;
            resultType = JvmType.INT;
            break;
        case JvmType.INT:
            valSize = INTSIZE;
            scale = 4;
            resultType = JvmType.INT;
            break;
        case JvmType.FLOAT:
            valSize = INTSIZE;
            scale = 4;
            resultType = JvmType.FLOAT;
            break;
        case JvmType.REFERENCE:
            valSize = INTSIZE;
            scale = 4;
            resultType = JvmType.REFERENCE;
            break;
        default:
            throw new IllegalArgumentException("Invalid type " + jvmType);
        }

        // Create result
        final WordItem result;
        final Register resultr;
        if (isfloat) {
            result = (WordItem)ifac.createFPUStack(JvmType.FLOAT);
            resultr = null;
        } else {
            result = L1AHelper.requestWordRegister(eContext, resultType,
                    (valSize == BYTESIZE));
            resultr = result.getRegister();
        }

        // Load
        idx.loadIf(eContext, ~Item.Kind.CONSTANT);
        ref.load(eContext);
        final Register refr = ref.getRegister();

        // Verify
        checkBounds(ref, idx);
        if (isfloat) {
            FPUHelper.ensureStackCapacity(os, eContext, vstack, 1);
        }
        
        if (jvmType == JvmType.CHAR) {
        	// Clear resultr, so we avoid a MOVZX afterwards.
        	os.writeXOR(resultr, resultr);
        }

        // Load data
        if (idx.isConstant()) {
            final int offset = idx.getValue() * scale;
            if (isfloat) {
                os.writeFLD32(refr, offset + arrayDataOffset);
            } else {
                os.writeMOV(valSize, resultr, refr, offset + arrayDataOffset);
            }
        } else {
            final Register idxr = idx.getRegister();
            if (isfloat) {
                os.writeFLD32(refr, idxr, scale, arrayDataOffset);
            } else {
                os.writeMOV(valSize, resultr, refr, idxr, scale,
                        arrayDataOffset);
            }
        }

        // Post process
        switch (jvmType) {
        case JvmType.BYTE:
            os.writeMOVSX(resultr, resultr, BYTESIZE);
            break;
//        case JvmType.CHAR:
//            os.writeMOVZX(resultr, resultr, WORDSIZE);
//            break;
        case JvmType.SHORT:
            os.writeMOVSX(resultr, resultr, WORDSIZE);
            break;
        }

        // Release
        ref.release(eContext);
        idx.release(eContext);

        // Push result
        vstack.push(result);
        if (isfloat) {
            vstack.fpuStack.push(result);
        }
    }

    /**
     * Store a WordItem into an array.
     * 
     * @param jvmType
     *            Type of the array elements
     */
    private final void wastore(int jvmType) {
        final boolean useBarrier = (context.getWriteBarrier() != null);
        final int valSize;
        final int scale;
        final int valType;
        int extraLoadIdxMask = 0;
        switch (jvmType) {
        case JvmType.BYTE:
            valSize = BYTESIZE;
            scale = 1;
            valType = JvmType.INT;
            break;
        case JvmType.CHAR:
        case JvmType.SHORT:
            valSize = WORDSIZE;
            scale = 2;
            valType = JvmType.INT;
            break;
        case JvmType.INT:
            valSize = INTSIZE;
            scale = 4;
            valType = JvmType.INT;
            break;
        case JvmType.FLOAT:
            valSize = INTSIZE;
            scale = 4;
            valType = JvmType.FLOAT;
            break;
        case JvmType.REFERENCE:
            valSize = INTSIZE;
            scale = 4;
            valType = JvmType.REFERENCE;
            extraLoadIdxMask = useBarrier ? ~Item.Kind.REGISTER : 0;
            break;
        default:
            throw new IllegalArgumentException("Invalid type " + jvmType);
        }

        final WordItem val = (WordItem) vstack.pop(valType);
        final IntItem idx = vstack.popInt();
        final RefItem ref = vstack.popRef();

        //IMPROVE: optimize case with const value
        // Load
        if (valSize == BYTESIZE) {
            val.loadToBITS8GPR(eContext);
        } else {
            val.load(eContext);
        }
        idx.loadIf(eContext, ~Item.Kind.CONSTANT | extraLoadIdxMask);
        ref.load(eContext);
        final Register refr = ref.getRegister();
        final Register valr = val.getRegister();

        // Verify
        checkBounds(ref, idx);

        // Store
        if (idx.isConstant()) {
            final int offset = idx.getValue() * scale;
            os.writeMOV(valSize, refr, offset + arrayDataOffset, valr);
        } else {
            final Register idxr = idx.getRegister();
            os.writeMOV(valSize, refr, idxr, scale, arrayDataOffset, valr);
        }

        // Call write barrier (reference only)
        if ((jvmType == JvmType.REFERENCE) && useBarrier) {
            // the write barrier could easily be modified to avoid using a
            // scratch register
            final X86RegisterPool pool = eContext.getPool();
            final Register idxr = idx.getRegister();
            final Register scratch = pool.request(JvmType.INT);
            helper.writeArrayStoreWriteBarrier(refr, idxr, valr, scratch);
            pool.release(scratch);
        }

        // Release
        val.release(eContext);
        idx.release(eContext);
        ref.release(eContext);
    }

    /**
     * Pop a word item of the stack and return it to the caller
     * 
     * @param JvmType
     */
    private final void wreturn(int jvmType) {
        final WordItem val = (WordItem) vstack.pop(jvmType);

        // Return value must be in EAX
        if (!val.uses(EAX)) {
            L1AHelper.requestRegister(eContext, EAX, val);
            val.loadTo(eContext, EAX);
        }

        // Release
        val.release(eContext);

        // Do actual return
        visit_return();
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

            // Push all
            vstack.push(eContext);

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
            invokeJavaMethod(context.getVmTypeInitialize());
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

    /**
     * Store a word item into a local variable
     * 
     * @param jvmType
     * @param index
     */
    private final void wstore(int jvmType, int index) {
        final int disp = stackFrame.getEbpOffset(index);

        // Pin down (load) other references to this local
        vstack.loadLocal(eContext, disp);

        // Load
        final WordItem val = (WordItem) vstack.pop(jvmType);
        final boolean vconst = val.isConstant();
        if (vconst && (jvmType == JvmType.INT)) {
            // Store constant int
            final int ival = ((IntItem) val).getValue();
            os.writeMOV_Const(FP, disp, ival);
        } else if (vconst && (jvmType == JvmType.FLOAT)) {
            // Store constant float
            final int ival = Float.floatToRawIntBits(((FloatItem) val)
                    .getValue());
            os.writeMOV_Const(FP, disp, ival);
        } else if (val.isFPUStack()) {
            // Ensure item is on top of fpu stack
            FPUHelper.fxch(os, vstack.fpuStack, val);
            if (jvmType == JvmType.FLOAT) {
                os.writeFSTP32(FP, disp);
            } else {
                os.writeFISTP32(FP, disp);
            }
            vstack.fpuStack.pop(val);
        } else if (val.isStack()) {
            // Must be top of stack
            if (VirtualStack.checkOperandStack) {
                vstack.operandStack.pop(val);
            }
            os.writePOP(FP, disp);
        } else {
            // Load into register
            val.load(eContext);
            final Register valr = val.getRegister();
            // Store
            os.writeMOV(INTSIZE, FP, disp, valr);
        }

        // Release
        val.release(eContext);
    }

    /**
     * Insert a yieldpoint into the code
     */
    public final void yieldPoint() {
        helper.writeYieldPoint(curInstrLabel);
    }
}