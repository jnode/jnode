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
 
package org.jnode.vm.x86.compiler.l1a;

import java.util.HashMap;
import java.util.Map;
import org.jnode.assembler.Label;
import org.jnode.assembler.NativeStream;
import org.jnode.assembler.x86.X86Assembler;
import org.jnode.assembler.x86.X86Constants;
import org.jnode.assembler.x86.X86Operation;
import org.jnode.assembler.x86.X86Register;
import org.jnode.assembler.x86.X86Register.GPR;
import org.jnode.assembler.x86.X86Register.GPR32;
import org.jnode.assembler.x86.X86Register.GPR64;
import org.jnode.system.BootLog;
import org.jnode.util.CounterGroup;
import org.jnode.vm.JvmType;
import org.jnode.vm.Vm;
import org.jnode.vm.bytecode.BasicBlock;
import org.jnode.vm.bytecode.BytecodeParser;
import org.jnode.vm.bytecode.TypeStack;
import org.jnode.vm.classmgr.ObjectLayout;
import org.jnode.vm.classmgr.Signature;
import org.jnode.vm.classmgr.TIBLayout;
import org.jnode.vm.classmgr.TypeSizeInfo;
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
import org.jnode.vm.classmgr.VmIsolatedStaticsEntry;
import org.jnode.vm.classmgr.VmLocalVariable;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmSharedStaticsEntry;
import org.jnode.vm.classmgr.VmStaticField;
import org.jnode.vm.classmgr.VmStaticMethod;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.compiler.CompileError;
import org.jnode.vm.compiler.CompiledMethod;
import org.jnode.vm.compiler.EntryPoints;
import org.jnode.vm.compiler.InlineBytecodeVisitor;
import org.jnode.vm.x86.compiler.AbstractX86StackManager;
import org.jnode.vm.x86.compiler.X86CompilerConstants;
import org.jnode.vm.x86.compiler.X86CompilerHelper;
import org.jnode.vm.x86.compiler.X86IMTCompiler32;
import org.jnode.vm.x86.compiler.X86IMTCompiler64;
import org.jnode.vm.x86.compiler.X86JumpTable;

/**
 * Actual converter from bytecodes to X86 native code. Uses a virtual stack to
 * delay item emission, as described in the ORP project
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Patrik Reali
 */
final class X86BytecodeVisitor extends InlineBytecodeVisitor implements
    X86CompilerConstants {

    /**
     * Debug this visitor, logs extra info
     */
    private static final boolean debug = false;

    /**
     * If true, do additional verifications. Helps to develop this compiler
     */
    private static final boolean paranoia = false;

    /**
     * Offset in bytes of the first data entry within an array-object
     */
    private final int arrayDataOffset;

    /**
     * Offset in bytes of the array-length value within an array-object
     */
    private final int arrayLengthOffset;

    /**
     * The destination compiled method
     */
    private final CompiledMethod cm;

    /**
     * Current context
     */
    private final EntryPoints context;

    /**
     * Bytecode Address of current instruction
     */
    private int curAddress;

    /**
     * Label of current instruction
     */
    private Label _curInstrLabel;

    /**
     * The method currently being compiled
     */
    private VmMethod currentMethod;

    /**
     * The emitter context
     */
    private final EmitterContext eContext;

    /**
     * Helper class
     */
    private final X86CompilerHelper helper;

    /**
     * The method currently being inline (or null for none)
     */
    private InlinedMethodInfo inlinedMethodInfo;

    /**
     * Class loader
     */
    private VmClassLoader loader;

    /**
     * Emit logging info
     */
    private final boolean log;

    /**
     * Maximum number of local variable slots
     */
    private int maxLocals;

    /**
     * The output stream
     */
    private final X86Assembler os;

    /**
     * Should we set the current instruction label on startInstruction?
     */
    private boolean setCurInstrLabel;

    /**
     * Stackframe utility
     */
    private X86StackFrame stackFrame;

    /**
     * Is this instruction the start of a basic block
     */
    private boolean startOfBB;

    /**
     * Length of os at start of method
     */
    private int startOffset;

    /**
     * Offset in bytes of the TIB reference within an object
     */
    private final int tibOffset;

    /**
     * Item factory
     */
    private final ItemFactory ifac;

    /**
     * Magic method compiler
     */
    private final MagicHelper magicHelper;

    /**
     * FP instruction compiler
     */
    private final FPCompiler fpCompiler;

    /**
     * Type size information
     */
    private final TypeSizeInfo typeSizeInfo;

    /**
     * Current inline depth (starting at 0)
     */
    private byte inlineDepth;

    /**
     * Register used by wstore (see xloadStored methods)
     */
    private GPR wstoreReg;

    /**
     * Constant values that are stored in local variables
     */
    private Map<Integer, Item> constLocals = new HashMap<Integer, Item>();

    /**
     * The current basic block
     */
    private BasicBlock currentBasicBlock;

    /**
     * The parser that is parsing the bytecode we're compiling
     */
    private BytecodeParser parser;

    /**
     * Virtual Stack: this stack contains values that have been computed but not
     * emitted yet; emission is delayed to allow for optimizations, in
     * particular using registers instead of stack operations.
     * <p/>
     * The vstack is valid only inside a basic block; items in the stack are
     * flushed at the end of the basic block.
     * <p/>
     * Aliasing: modifying a value that is still on the stack is forbidden. Each
     * time a local is assigned, the stack is checked for aliases. For the same
     * reason, array and field operations are not delayed.
     */
    private final VirtualStack vstack;

    /**
     * My counters
     */
    private final CounterGroup counters = Vm.getVm().getCounterGroup(getClass().getName());

    /**
     * Create a new instance
     *
     * @param outputStream
     * @param cm
     * @param isBootstrap
     * @param context
     */
    public X86BytecodeVisitor(NativeStream outputStream, CompiledMethod cm,
                              boolean isBootstrap, EntryPoints context,
                              MagicHelper magicHelper, TypeSizeInfo typeSizeInfo) {
        this.os = (X86Assembler) outputStream;
        this.context = context;
        this.typeSizeInfo = typeSizeInfo;
        this.magicHelper = magicHelper;
        this.vstack = new VirtualStack(os);
        final X86RegisterPool gprPool;
        final X86RegisterPool xmmPool;
        if (os.isCode32()) {
            gprPool = new X86RegisterPool.GPRs32();
            xmmPool = new X86RegisterPool.XMMs32();
        } else {
            gprPool = new X86RegisterPool.GPRs64();
            xmmPool = new X86RegisterPool.XMMs64();
        }
        this.ifac = ItemFactory.getFactory();
        final AbstractX86StackManager stackMgr = vstack.createStackMgr(gprPool,
            ifac);
        this.helper = new X86CompilerHelper(os, stackMgr, context, isBootstrap);
        this.cm = cm;
        final int slotSize = helper.SLOTSIZE;
        this.arrayLengthOffset = VmArray.LENGTH_OFFSET * slotSize;
        this.arrayDataOffset = VmArray.DATA_OFFSET * slotSize;
        this.tibOffset = ObjectLayout.TIB_SLOT * slotSize;
        this.log = os.isLogEnabled();
        this.eContext = new EmitterContext(os, helper, vstack, gprPool,
            xmmPool, ifac, context);
        vstack.initializeStackMgr(stackMgr, eContext);
        // TODO check for SSE support and switch to SSE compiler if available
        this.fpCompiler = new FPCompilerFPU(this, os, eContext, vstack,
            arrayDataOffset);
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
    final void checkBounds(RefItem ref, IntItem index) {
        counters.getCounter("checkbounds").inc();
        final Label curInstrLabel = getCurInstrLabel();
        final Label test = new Label(curInstrLabel + "$$cbtest");
        final Label failed = new Label(curInstrLabel + "$$cbfailed");

        assertCondition(ref.isGPR(), "ref must be in a register");
        final GPR refr = ref.getRegister();

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
            os
                .writeCMP_Const(BITS32, refr, arrayLengthOffset, index
                    .getValue());
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
        final GPR refr = ref.getRegister();

        // Check bound
        checkBounds(ref, idx);

        // Store
        loadArrayEntryOffset(refr, ref, idx, 8);
        if (os.isCode32()) {
            os.writeMOV(INTSIZE, refr, 0, val.getLsbRegister(eContext));
            os.writeMOV(INTSIZE, refr, 4, val.getMsbRegister(eContext));
        } else {
            os.writeMOV(BITS64, refr, 0, val.getRegister(eContext));
        }

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
    private final void dwreturn(int jvmType, boolean callVisitReturn) {
        final DoubleWordItem val = (DoubleWordItem) vstack.pop(jvmType);

        if (os.isCode32()) {
            // Return value must be in EAX:EDX
            if (!(val.uses(X86Register.EAX) && val.uses(X86Register.EDX))) {
                if (val.uses(X86Register.EAX) || val.uses(X86Register.EDX)) {
                    val.push(eContext);
                }
                L1AHelper.requestRegister(eContext, X86Register.EAX, val);
                L1AHelper.requestRegister(eContext, X86Register.EDX, val);
                val.loadTo32(eContext, X86Register.EAX, X86Register.EDX);
            }
        } else {
            // Return value must be in RAX
            if (!val.uses(X86Register.RAX)) {
                L1AHelper.requestRegister(eContext, X86Register.RAX, val);
                val.loadTo64(eContext, X86Register.RAX);
            }
        }

        // Release
        val.release(eContext);

        // Do actual return
        if (callVisitReturn) {
            visit_return();
        }
    }

    /**
     * Store a double word item into a local variable
     *
     * @param jvmType
     * @param index
     */
    private final void dwstore(int jvmType, int index) {
        final int disp = stackFrame.getWideEbpOffset(typeSizeInfo, index);

        // Pin down (load) other references to this local
        vstack.loadLocal(eContext, disp);

        // Load
        final DoubleWordItem val = (DoubleWordItem) vstack.pop(jvmType);
        final boolean vconst = val.isConstant();
        if (vconst && (jvmType == JvmType.LONG)) {
            // Store constant long
            final long lval = ((LongItem) val).getValue();
            os.writeMOV_Const(BITS32, helper.BP, disp + LSB,
                (int) (lval & 0xFFFFFFFFL));
            os.writeMOV_Const(BITS32, helper.BP, disp + MSB,
                (int) ((lval >>> 32) & 0xFFFFFFFFL));
        } else if (vconst && (jvmType == JvmType.DOUBLE)) {
            // Store constant double
            final long lval = Double.doubleToRawLongBits(((DoubleItem) val)
                .getValue());
            os.writeMOV_Const(BITS32, helper.BP, disp + LSB,
                (int) (lval & 0xFFFFFFFFL));
            os.writeMOV_Const(BITS32, helper.BP, disp + MSB,
                (int) ((lval >>> 32) & 0xFFFFFFFFL));
        } else if (val.isFPUStack()) {
            // Ensure item is on top of fpu stack
            FPUHelper.fxch(os, vstack.fpuStack, val);
            if (jvmType == JvmType.DOUBLE) {
                os.writeFSTP64(helper.BP, disp);
            } else {
                os.writeFISTP64(helper.BP, disp);
            }
            vstack.fpuStack.pop(val);
        } else if (val.isStack()) {
            // Must be top of stack
            if (VirtualStack.checkOperandStack) {
                vstack.operandStack.pop(val);
            }
            if (os.isCode32()) {
                os.writePOP(helper.BP, disp + LSB);
                os.writePOP(helper.BP, disp + MSB);
            } else {
                os.writePOP(helper.BP, disp);
                os.writeLEA(X86Register.RSP, X86Register.RSP, 8); // garbage
            }
        } else {
            // Load into register
            val.load(eContext);
            if (os.isCode32()) {
                final GPR lsb = val.getLsbRegister(eContext);
                final GPR msb = val.getMsbRegister(eContext);
                // Store
                os.writeMOV(INTSIZE, helper.BP, disp + LSB, lsb);
                os.writeMOV(INTSIZE, helper.BP, disp + MSB, msb);
            } else {
                final GPR64 reg = val.getRegister(eContext);
                // Store
                os.writeMOV(BITS64, helper.BP, disp, reg);
            }
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
        if (log) {
            os.log("End of inlined method</inline>");
        }
        // Do some housekeeping
        helper.setMethod(previousMethod);
        os.setObjectRef(inlinedMethodInfo.getEndOfInlineLabel());
        this.currentMethod = previousMethod;
        this.inlineDepth--;

        // Push the types on the vstack
        inlinedMethodInfo.pushExitStack(ifac, vstack);

        // Push the return value
        inlinedMethodInfo.pushReturnValue(helper);

        // Cleanup
        helper.setLabelPrefix(inlinedMethodInfo.getPreviousLabelPrefix());
        this.inlinedMethodInfo = inlinedMethodInfo.getPrevious();
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
            final X86RegisterPool pool = eContext.getGPRPool();
            pool.visitUsedRegisters(new RegisterVisitor() {

                public void visit(X86Register reg) {
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
                    if (item.isGPR()) {
                        if (item instanceof WordItem) {
                            if (pool.getOwner(((WordItem) item).getRegister()) != item) {
                                throw new InternalError(
                                    "Item "
                                        + item
                                        + " uses a register which is not registered in the register pool in method "
                                        + currentMethod
                                        + " at address " + curAddress);
                            }
                        } else {
                            if (os.isCode32()) {
                                if (pool.getOwner(((DoubleWordItem) item)
                                    .getLsbRegister(eContext)) != item) {
                                    throw new InternalError(
                                        "Item "
                                            + item
                                            +
                                            " uses an LSB register which is not registered in " +
                                            "the register pool in method "
                                            + currentMethod
                                            + " at address "
                                            + curAddress);
                                }
                                if (pool.getOwner(((DoubleWordItem) item)
                                    .getMsbRegister(eContext)) != item) {
                                    throw new InternalError(
                                        "Item "
                                            + item
                                            +
                                            " uses an MSB register which is not registered in the " +
                                            "register pool in method "
                                            + currentMethod
                                            + " at address "
                                            + curAddress);
                                }
                            } else {
                                if (pool.getOwner(((DoubleWordItem) item)
                                    .getRegister(eContext)) != item) {
                                    throw new InternalError(
                                        "Item "
                                            + item
                                            +
                                            " uses an register which is not registered in the register pool in method "
                                            + currentMethod
                                            + " at address "
                                            + curAddress);
                                }
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
        stackFrame.emitTrailer(typeSizeInfo, maxLocals);
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
        assertCondition(v.getCategory() == (size / helper.SLOTSIZE), "category mismatch");
        if (v.isStack()) {
            // sanity check
            if (VirtualStack.checkOperandStack) {
                vstack.operandStack.pop(v);
            }
            os.writeLEA(helper.SP, helper.SP, size);
        }
        v.release(eContext);
    }

    /**
     * Emit the core of the instanceof code.
     *
     * @param objectr   Register containing the object reference
     * @param trueLabel Where to jump for a true result. A false result will continue
     *                  directly after this method Register ECX must be free and it
     *                  destroyed.
     */
    private final void instanceOfClass(GPR objectr, VmClassType<?> type, GPR tmpr,
                                       GPR resultr, Label trueLabel, boolean skipNullTest) {

        final int depth = type.getSuperClassDepth();
        final int staticsOfs = helper.getSharedStaticsOffset(type);
        final Label curInstrLabel = getCurInstrLabel();
        final Label notInstanceOfLabel = new Label(curInstrLabel
            + "notInstanceOf");

        if (!type.isAlwaysInitialized()) {
            if (os.isCode32()) {
                helper.writeGetStaticsEntry(curInstrLabel, tmpr, type);
            } else {
                helper.writeGetStaticsEntry64(curInstrLabel, (GPR64) tmpr, (VmSharedStaticsEntry) type);
            }
            helper.writeClassInitialize(curInstrLabel, tmpr, tmpr, type);
        }

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

        final int slotSize = helper.SLOTSIZE;
        final int asize = helper.ADDRSIZE;

        // TIB -> tmp
        os.writeMOV(asize, tmpr, objectr, tibOffset);
        // SuperClassesArray -> tmp
        os.writeMOV(asize, tmpr, tmpr, arrayDataOffset
            + (TIBLayout.SUPERCLASSES_INDEX * slotSize));
        // Length of superclassarray must be >= depth
        os.writeCMP_Const(BITS32, tmpr, arrayLengthOffset, depth);
        os.writeJCC(notInstanceOfLabel, X86Constants.JNA);
        // Get superClassesArray[depth] -> objectr
        os.writeMOV(asize, tmpr, tmpr, arrayDataOffset + (depth * slotSize));
        // Compare objectr with classtype
        os.writeCMP(helper.STATICS, staticsOfs, tmpr);
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
     * @param objectr   Register containing the object reference
     * @param typer     Register containing the type reference
     * @param trueLabel Where to jump for a true result. A false result will continue
     *                  directly after this method Register ECX must be free and it
     *                  destroyed.
     */
    private final void instanceOf(GPR objectr, GPR typer, GPR tmpr, GPR cntr,
                                  Label trueLabel, boolean skipNullTest) {
        final Label curInstrLabel = getCurInstrLabel();
        final Label loopLabel = new Label(curInstrLabel + "loop");
        final Label notInstanceOfLabel = new Label(curInstrLabel
            + "notInstanceOf");

        if (Vm.VerifyAssertions) {
            Vm._assert(objectr.getSize() == helper.ADDRSIZE, "objectr size");
            Vm._assert(typer.getSize() == helper.ADDRSIZE, "typer size");
            Vm._assert(tmpr.getSize() == helper.ADDRSIZE, "tmpr size");
            Vm._assert(cntr.getSize() == BITS32, "cntr size");
        }

        if (!skipNullTest) {
            /* Is objectref null? */
            os.writeTEST(objectr, objectr);
            os.writeJCC(notInstanceOfLabel, X86Constants.JZ);
        }

        final int slotSize = helper.SLOTSIZE;
        final int asize = helper.ADDRSIZE;

        // TIB -> tmp
        os.writeMOV(asize, tmpr, objectr, tibOffset);
        // SuperClassesArray -> tmp
        os.writeMOV(asize, tmpr, tmpr, arrayDataOffset
            + (TIBLayout.SUPERCLASSES_INDEX * slotSize));
        // SuperClassesArray.length -> cntr
        os.writeMOV(BITS32, cntr, tmpr, arrayLengthOffset);
        // &superClassesArray[cnt-1] -> tmpr
        if (os.isCode64()) {
            // the MOV to cntr already zero-extends it, so no extension needed.
            cntr = L1AHelper.get64BitReg(eContext, cntr);
        }
        os.writeLEA(tmpr, tmpr, cntr, slotSize, arrayDataOffset - slotSize);

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
     * Generate code to invoke the given method.
     *
     * @param method
     */
    private final void invokeJavaMethod(VmMethod method) {
        if (log) {
            os.log("VStack: " + vstack + ", method: " + method);
        }
        helper.invokeJavaMethod(method);
        // Test the stack alignment
        stackFrame.writeStackAlignmentTest(getCurInstrLabel());
    }

    /**
     * Write an integer operation.
     *
     * @param operation
     * @param commutative
     * @see org.jnode.assembler.x86.X86Operation
     */
    private final void ioperation(int operation, boolean commutative) {
        IntItem v2 = vstack.popInt();
        IntItem v1 = vstack.popInt();

        if (v2.isConstant() && v1.isConstant()) {
            counters.getCounter("ioperation-const").inc();

            final int v;
            switch (operation) {
                case X86Operation.ADD:
                    v = v1.getValue() + v2.getValue();
                    break;
                case X86Operation.AND:
                    v = v1.getValue() & v2.getValue();
                    break;
                case X86Operation.OR:
                    v = v1.getValue() | v2.getValue();
                    break;
                case X86Operation.SUB:
                    v = v1.getValue() - v2.getValue();
                    break;
                case X86Operation.XOR:
                    v = v1.getValue() ^ v2.getValue();
                    break;
                default:
                    throw new RuntimeException("Invalid operation " + operation);
            }
            v1.release(eContext);
            v2.release(eContext);
            vstack.push(ifac.createIConst(eContext, v));
        } else {
            counters.getCounter("ioperation-nonconst").inc();

            if (prepareForOperation(v1, v2, commutative)) {
                // Swap
                final IntItem tmp = v2;
                v2 = v1;
                v1 = tmp;
            }

            final X86Register.GPR r1 = (X86Register.GPR) v1.getRegister();
            switch (v2.getKind()) {
                case Item.Kind.GPR:
                    os.writeArithOp(operation, r1, (X86Register.GPR) v2.getRegister());
                    break;
                case Item.Kind.LOCAL:
                    os.writeArithOp(operation, r1, helper.BP, v2
                        .getOffsetToFP(eContext));
                    break;
                case Item.Kind.CONSTANT:
                    os.writeArithOp(operation, r1, v2.getValue());
                    break;
            }
            v2.release(eContext);
            vstack.push(v1);
        }
    }

    /**
     * Write a shift operation.
     *
     * @param operation
     */
    private final void ishift(int operation) {
        final IntItem shift = vstack.popInt();
        final boolean isconst = shift.isConstant();

        if (!isconst && !shift.uses(X86Register.ECX)) {
            L1AHelper.requestRegister(eContext, X86Register.ECX, shift);
            shift.loadTo(eContext, X86Register.ECX);
        }

        // Pop & load
        final IntItem val = vstack.popInt();
        val.load(eContext);

        final GPR valr = val.getRegister();
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
    private final void loadArrayEntryOffset(GPR dst, RefItem ref,
                                            IntItem index, int scale) {
        assertCondition(ref.isGPR(), "ref must be in a register");
        final GPR refr = ref.getRegister();
        if (index.isConstant()) {
            final int offset = index.getValue() * scale;
            os.writeLEA(dst, refr, arrayDataOffset + offset);
        } else {
            final GPR32 idxr = (GPR32) index.getRegister();
            if (os.isCode32()) {
                os.writeLEA(dst, refr, idxr, scale, arrayDataOffset);
            } else {
                final GPR64 idxr64 = (GPR64) eContext.getGPRPool().getRegisterInSameGroup(idxr, JvmType.LONG);
                os.writeMOVSXD(idxr64, (GPR32) idxr);
                os.writeLEA(dst, refr, idxr64, scale, arrayDataOffset);
            }
        }
    }

    /**
     * Write an long operation.
     *
     * @param operation
     * @param commutative
     * @see org.jnode.assembler.x86.X86Operation
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

        if (os.isCode32()) {
            final GPR r1_lsb = v1.getLsbRegister(eContext);
            final GPR r1_msb = v1.getMsbRegister(eContext);
            switch (v2.getKind()) {
                case Item.Kind.GPR:
                    os.writeArithOp(operationLsb, r1_lsb, v2
                        .getLsbRegister(eContext));
                    os.writeArithOp(operationMsb, r1_msb, v2
                        .getMsbRegister(eContext));
                    break;
                case Item.Kind.LOCAL:
                    os.writeArithOp(operationLsb, r1_lsb, helper.BP, v2
                        .getLsbOffsetToFP(eContext));
                    os.writeArithOp(operationMsb, r1_msb, helper.BP, v2
                        .getMsbOffsetToFP(eContext));
                    break;
                case Item.Kind.CONSTANT:
                    os.writeArithOp(operationLsb, r1_lsb, v2.getLsbValue());
                    os.writeArithOp(operationMsb, r1_msb, v2.getMsbValue());
                    break;
            }
        } else {
            final GPR64 r1 = v1.getRegister(eContext);
            switch (v2.getKind()) {
                case Item.Kind.GPR:
                    os.writeArithOp(operationLsb, r1, v2.getRegister(eContext));
                    break;
                case Item.Kind.LOCAL:
                    os.writeArithOp(operationLsb, r1, X86Register.RBP, v2
                        .getOffsetToFP(eContext));
                    break;
                case Item.Kind.CONSTANT:
                    // 64-bit instructions still take 32-bit constants, so load it
                    // first.
                    v2.load(eContext);
                    os.writeArithOp(operationLsb, r1, v2.getRegister(eContext));
                    break;
            }
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

        if (commutative) {
            if (destAndSource.getKindWeight() < source.getKindWeight()) {
                // We should swap
                source.load(eContext);
                destAndSource.loadIf(eContext, (Item.Kind.STACK | Item.Kind.FPUSTACK));
                return true;
            }
        }

        source.loadIf(eContext, (Item.Kind.STACK | Item.Kind.FPUSTACK));
        destAndSource.load(eContext);
        return false;
    }

    /**
     * @param parser
     * @see org.jnode.vm.bytecode.BytecodeVisitor#setParser(org.jnode.vm.bytecode.BytecodeParser)
     */
    public void setParser(BytecodeParser parser) {
        this.parser = parser;
    }

    /**
     * The given basic block is about to start.
     */
    public void startBasicBlock(BasicBlock bb) {
        this.currentBasicBlock = bb;
        if (log) {
            os.log("Start of basic block " + bb);
        }
        if (debug) {
            BootLog.debug("-- Start of BB " + bb);
        }
        startOfBB = true;
        this.vstack.reset();
        eContext.getGPRPool().reset(os);
        // Push the result from the outer method stack on the vstack
        if (inlinedMethodInfo != null) {
            inlinedMethodInfo.pushOuterMethodStack(ifac, vstack);
        }
        // Push the items on the vstack the result from a previous basic block.
        final TypeStack tstack = bb.getStartStack();
        vstack.pushAll(ifac, tstack);
        // Clear all constant locals
        constLocals.clear();

        if (debug) {
            BootLog.debug("-- VStack: " + vstack.toString());
        }
    }

    /**
     * @see org.jnode.vm.compiler.InlineBytecodeVisitor#startInlinedMethodCode(VmMethod,
     *      int)
     */
    public void startInlinedMethodCode(VmMethod inlinedMethod, int newMaxLocals) {
        if (log) {
            os.log("<inline name=\"" + inlinedMethod.getName() + "\">Start of inlined method code");
        }
        if (debug) {
            BootLog.debug("startInlinedMethodCode(" + inlinedMethod + ")");
        }
        // TODO: check whether this is really needed
        // For now yes, because a new basic block resets the registerpool
        // and that fails if not all registers are freed.
        vstack.push(eContext);
        this.inlinedMethodInfo.setOuterMethodStack(vstack.asTypeStack());
        this.inlineDepth++;
    }

    /**
     * @see org.jnode.vm.compiler.InlineBytecodeVisitor#startInlinedMethodHeader(VmMethod,
     *      int)
     */
    public void startInlinedMethodHeader(VmMethod inlinedMethod,
                                         int newMaxLocals) {
        if (log) {
            os.log("Start of inlined method header " + inlinedMethod.getName());
        }
        if (debug) {
            BootLog.debug("startInlinedMethodHeader(" + inlinedMethod + ")");
        }
        maxLocals = newMaxLocals;
        final Label curInstrLabel = getCurInstrLabel();
        final String prefix = curInstrLabel + "_" + inlinedMethod.getName()
            + '_';
        this.inlinedMethodInfo = new InlinedMethodInfo(inlinedMethodInfo,
            inlinedMethod, new Label(curInstrLabel + "_end_of_inline"),
            helper.getLabelPrefix());
        helper.setLabelPrefix(prefix);
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
            if (debug) {
                os.log("#" + address + " VStack: " + vstack.toString());
            } else {
                os.log("#" + address);
            }
        }
        this.curAddress = address;
        this._curInstrLabel = null;
        if (startOfBB || setCurInstrLabel) {
            os.setObjectRef(getCurInstrLabel());
            startOfBB = false;
            setCurInstrLabel = false;
        }
        final int offset = os.getLength() - startOffset;
        cm.add(currentMethod, address, offset, inlineDepth);
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
        helper.reset();
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
        vstack.push(ifac.createAConst(eContext, null));
    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_aload(int)
     */
    public final void visit_aload(int index) {
        wload(JvmType.REFERENCE, index, false);
    }

    /**
     * @see org.jnode.vm.compiler.CompilerBytecodeVisitor#visit_aloadStored(int)
     */
    public void visit_aloadStored(int index) {
        wload(JvmType.REFERENCE, index, true);
    }

    /**
     * @param classRef
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_anewarray(org.jnode.vm.classmgr.VmConstClass)
     */
    public final void visit_anewarray(VmConstClass classRef) {
        counters.getCounter("anewarray").inc();

        // Push all, since we're going to call other methods
        vstack.push(eContext);

        // Claim EAX/RAX, we're going to use it later
        L1AHelper.requestRegister(eContext, helper.AAX);
        // Request tmp register
        final GPR classr = (GPR) L1AHelper.requestRegister(eContext,
            JvmType.REFERENCE, false);

        // Pop
        final IntItem cnt = vstack.popInt();

        // Load the count value
        cnt.load(eContext);
        final GPR cntr = cnt.getRegister();

        // Resolve the class
        writeResolveAndLoadClassToReg(classRef, classr);

        // Release EAX so it can be used by invokeJavaMethod
        L1AHelper.releaseRegister(eContext, helper.AAX);

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
        wreturn(JvmType.REFERENCE, true);
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
        final GPR refr = ref.getRegister();
        final GPR resultr = result.getRegister();

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
        if (!ref.uses(X86Register.EAX)) {
            L1AHelper.requestRegister(eContext, X86Register.EAX, ref);
            ref.loadTo(eContext, X86Register.EAX);
        }

        // Jump
        helper.writeJumpTableCALL(X86JumpTable.VM_ATHROW_IDX);

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
        final VmType<?> resolvedType = classRef.getResolvedVmClass();
        final Label curInstrLabel = getCurInstrLabel();

        if (resolvedType.isInterface() || resolvedType.isArray()) {
            // ClassRef is an interface or array, do the slow test

            // Pre-claim ECX
            L1AHelper.requestRegister(eContext, helper.AAX);

            // check that top item is a reference
            final RefItem ref = vstack.popRef();

            // Load the ref
            ref.load(eContext);
            final GPR refr = ref.getRegister();
            final GPR classr = helper.AAX;
            final GPR cntr = (GPR) L1AHelper.requestRegister(eContext,
                JvmType.INT, false);
            final GPR tmpr = (GPR) L1AHelper.requestRegister(eContext,
                JvmType.REFERENCE, false);

            // Resolve the class
            writeResolveAndLoadClassToReg(classRef, classr);
            helper.writeClassInitialize(curInstrLabel, classr, tmpr, resolvedType);

            final Label okLabel = new Label(curInstrLabel + "cc-ok");

            /* Is objectref null? */
            os.writeTEST(refr, refr);
            os.writeJCC(okLabel, X86Constants.JZ);
            /* Is instanceof? */
            instanceOf(refr, classr, tmpr, cntr, okLabel, true);
            /* Not instanceof */

            // Call classCastFailed
            os.writePUSH(refr);
            os.writePUSH(classr);
            // Release temp registers here, so invokeJavaMethod can use it
            L1AHelper.releaseRegister(eContext, cntr);
            L1AHelper.releaseRegister(eContext, tmpr);
            L1AHelper.releaseRegister(eContext, classr);
            invokeJavaMethod(context.getClassCastFailedMethod());

            /* Normal exit */
            os.setObjectRef(okLabel);

            // Leave ref on stack
            vstack.push(ref);
        } else {
            // classRef is a class, do the fast test

            // Pre-claim EAX/RAX
            L1AHelper.requestRegister(eContext, helper.AAX);

            // check that top item is a reference
            final RefItem ref = vstack.popRef();

            // Load the ref
            ref.load(eContext);
            final GPR refr = ref.getRegister();
            final GPR tmpr = (GPR) L1AHelper.requestRegister(eContext,
                JvmType.REFERENCE, false);

            final Label okLabel = new Label(curInstrLabel + "cc-ok");

            // Is objectref null?
            os.writeTEST(refr, refr);
            os.writeJCC(okLabel, X86Constants.JZ);
            // Is instanceof?
            instanceOfClass(refr, (VmClassType<?>) resolvedType, tmpr, null,
                okLabel, true);
            // Not instanceof

            // Load class into tmpr
            if (os.isCode32()) {
                helper.writeGetStaticsEntry(curInstrLabel, tmpr, resolvedType);
            } else {
                helper.writeGetStaticsEntry64(curInstrLabel, (GPR64) tmpr, (VmSharedStaticsEntry) resolvedType);
            }

            // Call SoftByteCodes.classCastFailed(Object, VmType)
            os.writePUSH(refr);
            os.writePUSH(tmpr);
            // Release temp registers here, so invokeJavaMethod can use it
            L1AHelper.releaseRegister(eContext, helper.AAX);
            L1AHelper.releaseRegister(eContext, tmpr);
            invokeJavaMethod(context.getClassCastFailedMethod());

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
        fpCompiler.convert(JvmType.DOUBLE, JvmType.FLOAT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_d2i()
     */
    public final void visit_d2i() {
        fpCompiler.convert(JvmType.DOUBLE, JvmType.INT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_d2l()
     */
    public final void visit_d2l() {
        fpCompiler.convert(JvmType.DOUBLE, JvmType.LONG);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dadd()
     */
    public final void visit_dadd() {
        fpCompiler.add(JvmType.DOUBLE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_daload()
     */
    public final void visit_daload() {
        fpCompiler.fpaload(JvmType.DOUBLE);
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
        fpCompiler.compare(true, JvmType.DOUBLE, getCurInstrLabel());
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dcmpl()
     */
    public final void visit_dcmpl() {
        fpCompiler.compare(false, JvmType.DOUBLE, getCurInstrLabel());
    }

    /**
     * @param value
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dconst(double)
     */
    public final void visit_dconst(double value) {
        vstack.push(ifac.createDConst(eContext, value));
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ddiv()
     */
    public final void visit_ddiv() {
        fpCompiler.div(JvmType.DOUBLE);
    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dload(int)
     */
    public final void visit_dload(int index) {
        vstack.push(ifac.createLocal(JvmType.DOUBLE, stackFrame
            .getWideEbpOffset(typeSizeInfo, index)));
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dmul()
     */
    public final void visit_dmul() {
        fpCompiler.mul(JvmType.DOUBLE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dneg()
     */
    public final void visit_dneg() {
        fpCompiler.neg(JvmType.DOUBLE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_drem()
     */
    public final void visit_drem() {
        fpCompiler.rem(JvmType.DOUBLE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dreturn()
     */
    public final void visit_dreturn() {
        dwreturn(JvmType.DOUBLE, true);
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
        fpCompiler.sub(JvmType.DOUBLE);
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

	// TODO: port to ORP style

        // Push all on the stack, since this opcode is just too complicated
        vstack.push(eContext);

        final Item v1 = vstack.pop1();
        final Item v2 = vstack.pop1();
        final int c1 = v1.getCategory();
        final int c2 = v2.getCategory();

        // Perform a stack swap independent of the actual form
        os.writePOP(helper.AAX); // Value1
        os.writePOP(helper.ABX); // Value2
        os.writePOP(helper.ACX); // Value3
        os.writePOP(helper.ADX); // Value4
        os.writePUSH(helper.ABX); // Value2
        os.writePUSH(helper.AAX); // Value1
        os.writePUSH(helper.ADX); // Value4
        os.writePUSH(helper.ACX); // Value3
        os.writePUSH(helper.ABX); // Value2
        os.writePUSH(helper.AAX); // Value1

        // Now update the operandstack
        // cope with brain-dead definition from Sun (look-like somebody there
        // was to eager to optimize this and it landed in the compiler...
        if (c2 == 2) {
            // form 4
            assertCondition(c1 == 2, "category mismatch");
            vstack.push1(ifac.createStack(v1.getType()));
            vstack.push1(v2);
            vstack.push1(v1);
        } else {
            final Item v3 = vstack.pop1();
            int c3 = v3.getCategory();
            if (c1 == 2) {
                // form 2
                assertCondition(c3 == 1, "category mismatch");
                vstack.push1(ifac.createStack(v1.getType()));
                vstack.push1(v3);
                vstack.push1(v2);
                vstack.push1(v1);
            } else if (c3 == 2) {
                // form 3
                vstack.push1(ifac.createStack(v2.getType()));
                vstack.push1(ifac.createStack(v1.getType()));
                vstack.push1(v3);
                vstack.push1(v2);
                vstack.push1(v1);
            } else {
                // form 1
                final Item v4 = vstack.pop1();
                vstack.push1(ifac.createStack(v2.getType()));
                vstack.push1(ifac.createStack(v1.getType()));
                vstack.push1(v4);
                vstack.push1(v3);
                vstack.push1(v2);
                vstack.push1(v1);
            }
        }
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_f2d()
     */
    public final void visit_f2d() {
        fpCompiler.convert(JvmType.FLOAT, JvmType.DOUBLE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_f2i()
     */
    public final void visit_f2i() {
        fpCompiler.convert(JvmType.FLOAT, JvmType.INT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_f2l()
     */
    public final void visit_f2l() {
        fpCompiler.convert(JvmType.FLOAT, JvmType.LONG);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fadd()
     */
    public final void visit_fadd() {
        fpCompiler.add(JvmType.FLOAT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_faload()
     */
    public final void visit_faload() {
        fpCompiler.fpaload(JvmType.FLOAT);
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
        fpCompiler.compare(true, JvmType.FLOAT, getCurInstrLabel());
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fcmpl()
     */
    public final void visit_fcmpl() {
        fpCompiler.compare(false, JvmType.FLOAT, getCurInstrLabel());
    }

    /**
     * @param value
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fconst(float)
     */
    public final void visit_fconst(float value) {
        vstack.push(ifac.createFConst(eContext, value));
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fdiv()
     */
    public final void visit_fdiv() {
        fpCompiler.div(JvmType.FLOAT);
    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fload(int)
     */
    public final void visit_fload(int index) {
        wload(JvmType.FLOAT, index, false);
    }

    /**
     * @see org.jnode.vm.compiler.CompilerBytecodeVisitor#visit_floadStored(int)
     */
    public void visit_floadStored(int index) {
        wload(JvmType.FLOAT, index, true);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fmul()
     */
    public final void visit_fmul() {
        fpCompiler.mul(JvmType.FLOAT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fneg()
     */
    public final void visit_fneg() {
        fpCompiler.neg(JvmType.FLOAT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_frem()
     */
    public final void visit_frem() {
        fpCompiler.rem(JvmType.FLOAT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_freturn()
     */
    public final void visit_freturn() {
        wreturn(JvmType.FLOAT, true);
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
        fpCompiler.sub(JvmType.FLOAT);
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
        final GPR refr = ref.getRegister();

        // get field
        final Item result;
        if (!fieldRef.isWide()) {
            if (isfloat) {
                result = ifac.createFPUStack(JvmType.FLOAT);
                os.writeFLD32(refr, fieldOffset);
                pushFloat(result);
            } else {
                final char fieldType = field.getSignature().charAt(0);
                final WordItem iw = L1AHelper.requestWordRegister(eContext,
                    type, (fieldType != 'I') && (type != JvmType.REFERENCE));
                final GPR iwr = iw.getRegister();
                switch (fieldType) {
                    case 'Z': // boolean
                        os.writeMOVZX(iwr, refr, fieldOffset, BITS8);
                        break;
                    case 'B': // byte
                        os.writeMOVSX(iwr, refr, fieldOffset, BITS8);
                        break;
                    case 'C': // char
                        os.writeMOVZX(iwr, refr, fieldOffset, BITS16);
                        break;
                    case 'S': // short
                        os.writeMOVSX(iwr, refr, fieldOffset, BITS16);
                        break;
                    case 'I': // int
                    case 'L': // Object
                    case '[': // array
                        os.writeMOV(iwr.getSize(), iwr, refr, fieldOffset);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown fieldType " + fieldType);
                }
                result = iw;
            }
        } else {
            if (isfloat) {
                result = ifac.createFPUStack(JvmType.DOUBLE);
                os.writeFLD64(refr, fieldOffset);
                pushFloat(result);
            } else {
                final DoubleWordItem idw = L1AHelper
                    .requestDoubleWordRegisters(eContext, type);
                if (os.isCode32()) {
                    final GPR lsb = idw.getLsbRegister(eContext);
                    final GPR msb = idw.getMsbRegister(eContext);
                    os.writeMOV(BITS32, lsb, refr, fieldOffset + LSB);
                    os.writeMOV(BITS32, msb, refr, fieldOffset + MSB);
                } else {
                    final GPR64 reg = idw.getRegister(eContext);
                    os.writeMOV(BITS64, reg, refr, fieldOffset);
                }
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
        final Label curInstrLabel = getCurInstrLabel();
        fieldRef.resolve(loader);
        final int type = JvmType.SignatureToType(fieldRef.getSignature());
        final VmStaticField sf = (VmStaticField) fieldRef.getResolvedVmField();

        // Initialize if needed
        if (!sf.getDeclaringClass().isAlwaysInitialized()) {
            writeInitializeClass(fieldRef);
        }

        // Get static field object
        if (JvmType.isFloat(type)) {
            final boolean is32bit = !fieldRef.isWide();
            if (sf.isShared()) {
                helper.writeGetStaticsEntryToFPU(curInstrLabel, (VmSharedStaticsEntry) sf, is32bit);
            } else {
                final GPR tmp = (GPR) L1AHelper.requestRegister(eContext,
                    JvmType.REFERENCE, false);
                helper.writeGetStaticsEntryToFPU(curInstrLabel,
                    (VmIsolatedStaticsEntry) sf, is32bit, tmp);
                L1AHelper.releaseRegister(eContext, tmp);
            }
            final Item result = ifac.createFPUStack(type);
            pushFloat(result);
            vstack.push(result);
        } else if (!fieldRef.isWide()) {
            final WordItem result = L1AHelper.requestWordRegister(eContext,
                type, false);
            final GPR resultr = result.getRegister();
            if (os.isCode32() || (type != JvmType.REFERENCE)) {
                if (sf.isShared()) {
                    helper.writeGetStaticsEntry(curInstrLabel, resultr, (VmSharedStaticsEntry) sf);
                } else {
                    final GPR tmp = (GPR) L1AHelper.requestRegister(eContext,
                        JvmType.REFERENCE, false);
                    helper.writeGetStaticsEntry(curInstrLabel, resultr,
                        (VmIsolatedStaticsEntry) sf, tmp);
                    L1AHelper.releaseRegister(eContext, tmp);
                }
            } else {
                if (sf.isShared()) {
                    helper.writeGetStaticsEntry64(curInstrLabel, (GPR64) resultr, (VmSharedStaticsEntry) sf);
                } else {
                    helper.writeGetStaticsEntry64(curInstrLabel, (GPR64) resultr, (VmIsolatedStaticsEntry) sf);
                }
            }
            vstack.push(result);
        } else {
            final DoubleWordItem result = L1AHelper.requestDoubleWordRegisters(
                eContext, type);
            if (os.isCode32()) {
                final GPR lsb = result.getLsbRegister(eContext);
                final GPR msb = result.getMsbRegister(eContext);
                if (sf.isShared()) {
                    helper.writeGetStaticsEntry64(curInstrLabel, lsb, msb, (VmSharedStaticsEntry) sf);
                } else {
                    helper.writeGetStaticsEntry64(curInstrLabel, lsb, msb, (VmIsolatedStaticsEntry) sf);
                }
            } else {
                final GPR64 reg = result.getRegister(eContext);
                if (sf.isShared()) {
                    helper.writeGetStaticsEntry64(curInstrLabel, reg, (VmSharedStaticsEntry) sf);
                } else {
                    helper.writeGetStaticsEntry64(curInstrLabel, reg, (VmIsolatedStaticsEntry) sf);
                }
            }
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
            vstack.push(ifac.createIConst(eContext, (byte) v.getValue()));
        } else {
            v.loadToBITS8GPR(eContext);
            final GPR r = v.getRegister();
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
            vstack.push(ifac.createIConst(eContext, (char) v.getValue()));
        } else {
            v.load(eContext);
            final GPR r = v.getRegister();
            os.writeMOVZX(r, r, WORDSIZE);
            vstack.push(v);
        }
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2d()
     */
    public final void visit_i2d() {
        fpCompiler.convert(JvmType.INT, JvmType.DOUBLE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2f()
     */
    public final void visit_i2f() {
        fpCompiler.convert(JvmType.INT, JvmType.FLOAT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2l()
     */
    public final void visit_i2l() {
        final IntItem v = vstack.popInt();
        if (v.isConstant()) {
            vstack.push(ifac.createLConst(eContext, v.getValue()));
        } else {
            final X86RegisterPool pool = eContext.getGPRPool();
            final LongItem result;
            if (!v.uses(X86Register.EAX)) {
                L1AHelper.requestRegister(eContext, X86Register.EAX);
                v.loadTo(eContext, X86Register.EAX);
            }
            if (os.isCode32()) {
                L1AHelper.requestRegister(eContext, X86Register.EDX);
                result = (LongItem) ifac.createReg(eContext, JvmType.LONG,
                    X86Register.EAX, X86Register.EDX);
                os.writeCDQ(BITS32); /* Sign extend EAX -> EDX:EAX */
                pool.transferOwnerTo(X86Register.EAX, result);
                pool.transferOwnerTo(X86Register.EDX, result);
                // We do not release v, because its register (EAX) is re-used in
                // result
            } else {
                v.release(eContext);
                L1AHelper.requestRegister(eContext, X86Register.RAX);
                result = (LongItem) ifac.createReg(eContext, JvmType.LONG,
                    X86Register.RAX);
                os.writeCDQE(); // Sign extend EAX -> RAX
                pool.transferOwnerTo(X86Register.RAX, result);
            }

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
            vstack.push(ifac.createIConst(eContext, (short) v.getValue()));
        } else {
            v.load(eContext);
            final GPR r = v.getRegister();
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
        vstack.push(ifac.createIConst(eContext, value));
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_idiv()
     */
    public final void visit_idiv() {
        final X86RegisterPool pool = eContext.getGPRPool();

        // Pop the arguments of the vstack
        final IntItem v2 = vstack.popInt();
        final IntItem v1 = vstack.popInt();

        final int shift;
        if (v1.isConstant() && v2.isConstant()) {
            // Update counter
            counters.getCounter("idiv-const").inc();

            vstack.push(ifac.createIConst(eContext, v1.getValue() / v2.getValue()));
            v1.release(eContext);
            v2.release(eContext);
        } else if (v2.isConstant() && ((shift = getShiftForMultiplier(v2.getValue())) > 0)) {
            // Update counter
            counters.getCounter("idiv-const-shift").inc();

            // Load v1
            v1.load(eContext);

            // Divide by shifting
            os.writeSAR(v1.getRegister(), shift);

            // Release
            v2.release(eContext);

            // And push the result on the vstack.
            vstack.push(v1);
        } else {
            // Update counter
            counters.getCounter("idiv-nonconst").inc();

            // We need v1 in EAX, so if that is not the case,
            // spill those item using EAX
            L1AHelper.requestRegister(eContext, X86Register.EAX, v1);

            // We need to use EDX, so spill those items using it.
            v1.spillIfUsing(eContext, X86Register.EDX);
            v2.spillIfUsing(eContext, X86Register.EDX);
            L1AHelper.requestRegister(eContext, X86Register.EDX);

            // Load v2, v1 into a register
            v2.load(eContext);
            v1.loadTo(eContext, X86Register.EAX);

            // EAX -> sign extend EDX:EAX
            os.writeCDQ(BITS32);

            // EAX = EDX:EAX / v2.reg
            os.writeIDIV_EAX(v2.getRegister());

            // Free unused registers
            pool.release(X86Register.EDX);
            v2.release(eContext);

            // And push the result on the vstack.
            vstack.push(v1);
        }
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

        // TODO: can be less restrictive: v1 must not be register
        if (prepareForOperation(v1, v2, true)) {
            // Swap
            final RefItem tmp = v2;
            v2 = v1;
            v1 = tmp;
        }

        final GPR r1 = v1.getRegister();

        switch (v2.getKind()) {
            case Item.Kind.GPR:
                os.writeCMP(r1, v2.getRegister());
                break;
            case Item.Kind.LOCAL:
                os.writeCMP(r1, helper.BP, v2.getOffsetToFP(eContext));
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

        // TODO: can be less restrictive: v1 must not be register
        if (prepareForOperation(v1, v2, (jccOpcode == X86Constants.JE)
            || (jccOpcode == X86Constants.JNE))) {
            // Swap
            final IntItem tmp = v2;
            v2 = v1;
            v1 = tmp;
        }

        final GPR r1 = v1.getRegister();

        switch (v2.getKind()) {
            case Item.Kind.GPR:
                os.writeCMP(r1, v2.getRegister());
                break;
            case Item.Kind.LOCAL:
                os.writeCMP(r1, helper.BP, v2.getOffsetToFP(eContext));
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
        // IMPROVE: Local case
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
            final GPR valr = val.getRegister();
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
        final int ebpOfs = stackFrame.getEbpOffset(typeSizeInfo, index);

        // pin down other references to this local
        vstack.loadLocal(eContext, ebpOfs);

        if (incValue == 1) {
            os.writeINC(BITS32, helper.BP, ebpOfs);
        } else {
            os.writeADD(BITS32, helper.BP, ebpOfs, incValue);
        }

        // Local no longer constant
        constLocals.remove(index);
    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iload(int)
     */
    public final void visit_iload(int index) {
        wload(JvmType.INT, index, false);
    }

    /**
     * @see org.jnode.vm.compiler.CompilerBytecodeVisitor#visit_iloadStored(int)
     */
    public void visit_iloadStored(int index) {
        wload(JvmType.INT, index, true);
    }

    /**
     * Convert the given multiplier to a shift number.
     *
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

        if (v2.isConstant() && v1.isConstant()) {
            vstack.push(ifac.createIConst(eContext, v1.getValue() * v2.getValue()));
            v1.release(eContext);
            v2.release(eContext);
        } else {
            if (prepareForOperation(v1, v2, true)) {
                // Swap
                final IntItem tmp = v2;
                v2 = v1;
                v1 = tmp;
            }

            final GPR r1 = v1.getRegister();
            switch (v2.getKind()) {
                case Item.Kind.GPR:
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
                            // abs(val) is multiple of 2 && val=2^shift where shift
                            // <=
                            // 31
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
                    os.writeIMUL(r1, helper.BP, v2.getOffsetToFP(eContext));
                    break;
            }
            v2.release(eContext);
            vstack.push(v1);
        }
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ineg()
     */
    public final void visit_ineg() {
        final IntItem val = vstack.popInt();
        val.loadIf(eContext, ~Item.Kind.CONSTANT);
        if (val.isConstant()) {
            vstack.push(ifac.createIConst(eContext, -val.getValue()));
        } else {
            os.writeNEG(val.getRegister());
            vstack.push(val);
        }
    }

    /**
     * @see org.jnode.vm.compiler.InlineBytecodeVisitor#visit_inlinedReturn()
     */
    public void visit_inlinedReturn(int jvmType) {
        if (debug) {
            BootLog.debug("inlinedReturn [type " + jvmType + "]");
        }

        // Pop the return value
        switch (jvmType) {
            case JvmType.INT:
            case JvmType.FLOAT:
            case JvmType.REFERENCE:
                wreturn(jvmType, false);
                break;
            case JvmType.LONG:
            case JvmType.DOUBLE:
                dwreturn(jvmType, false);
                break;
            case JvmType.VOID:
                break;
            default:
                throw new CompileError("Unknown return type " + jvmType);
        }

        // Push the remaining vstack items to the stack
        vstack.push(eContext);

        inlinedMethodInfo.setExitStack(vstack);
        os.writeJMP(inlinedMethodInfo.getEndOfInlineLabel());
    }

    /**
     * @param classRef
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_instanceof(org.jnode.vm.classmgr.VmConstClass)
     */
    public final void visit_instanceof(VmConstClass classRef) {
        // Resolve the classRef
        classRef.resolve(loader);

        // Prepare
        final X86RegisterPool pool = eContext.getGPRPool();
        final VmType<?> resolvedType = classRef.getResolvedVmClass();

        if (resolvedType.isInterface() || resolvedType.isArray()) {
            // It is an interface, do it the hard way

            // Load reference
            final RefItem ref = vstack.popRef();
            ref.load(eContext);
            final GPR refr = ref.getRegister();

            // Allocate tmp registers
            final GPR classr = (GPR) L1AHelper.requestRegister(eContext,
                JvmType.REFERENCE, false);
            final GPR cntr = (GPR) L1AHelper.requestRegister(eContext,
                JvmType.INT, false);
            final GPR tmpr = (GPR) L1AHelper.requestRegister(eContext,
                JvmType.REFERENCE, false);
            final Label curInstrLabel = getCurInstrLabel();

            /* Objectref is already on the stack */
            writeResolveAndLoadClassToReg(classRef, classr);
            helper.writeClassInitialize(curInstrLabel, classr, tmpr, resolvedType);

            final Label trueLabel = new Label(curInstrLabel + "io-true");
            final Label endLabel = new Label(curInstrLabel + "io-end");

            /* Is instanceof? */
            instanceOf(refr, classr, tmpr, cntr, trueLabel, false);

            final IntItem result = (IntItem) L1AHelper.requestWordRegister(eContext, JvmType.INT, false);
            final GPR resultr = result.getRegister();

            /* Not instanceof */
            // TODO: use setcc instead of jumps
            os.writeXOR(resultr, resultr);
            os.writeJMP(endLabel);

            os.setObjectRef(trueLabel);
            os.writeMOV_Const(resultr, 1);

            // Push result
            os.setObjectRef(endLabel);
            ref.release(eContext);

            vstack.push(result);

            // Release
            pool.release(classr);
            pool.release(tmpr);
            pool.release(cntr);
        } else {
            // It is a class, do the fast way
            //vstack.push(eContext); // just a (slow) test

            // Load reference
            final RefItem ref = vstack.popRef();
            ref.load(eContext);
            final GPR refr = ref.getRegister();

            // Allocate tmp registers
            final GPR tmpr = (GPR) L1AHelper.requestRegister(eContext,
                JvmType.REFERENCE, false);
            final IntItem result = (IntItem) L1AHelper.requestWordRegister(
                eContext, JvmType.INT, true);

            // Is instanceof
            instanceOfClass(refr, (VmClassType<?>) classRef.getResolvedVmClass(),
                tmpr, result.getRegister(), null, false);

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
        os.writeMOV(helper.ADDRSIZE, helper.AAX, helper.SP, argSlotCount
            * helper.SLOTSIZE);
        // Write the actual invokeinterface
        if (os.isCode32()) {
            X86IMTCompiler32.emitInvokeInterface(os, method);
        } else {
            X86IMTCompiler64.emitInvokeInterface(os, method);
        }
        // Test the stack alignment
        stackFrame.writeStackAlignmentTest(getCurInstrLabel());
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

            // Call the methods code from the statics table
            helper.invokeJavaMethod(sm);
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
            magicHelper.emitMagic(eContext, method, true, this, currentMethod);
        } else {
            // Flush the stack before an invoke
            vstack.push(eContext);

            dropParameters(method, false);

            // Call the methods native code from the statics table
            helper.invokeJavaMethod(method);
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
        final VmType<?> declClass = method.getDeclaringClass();
        if (declClass.isMagicType()) {
            magicHelper.emitMagic(eContext, method, false, this, currentMethod);
        } else {
            // TODO: port to orp-style
            vstack.push(eContext);

            dropParameters(mts, true);

            if (method.isFinal() || method.isPrivate() || declClass.isFinal()) {
                // Do a fast invocation
                counters.getCounter("virtual-final").inc();

                // Call the methods native code from the statics table
                helper.invokeJavaMethod(method);
                // Result is already on the stack.
            } else {
                // Do a virtual method table invocation
                counters.getCounter("virtual-vmt").inc();

                final int tibIndex = method.getTibOffset();
                final int argSlotCount = Signature.getArgSlotCount(typeSizeInfo, methodRef
                    .getSignature());

                final int slotSize = helper.SLOTSIZE;
                final int asize = helper.ADDRSIZE;

                /* Get objectref -> EAX */
                os.writeMOV(asize, helper.AAX, helper.SP, argSlotCount
                    * slotSize);
                /* Get VMT of objectef -> EAX */
                os.writeMOV(asize, helper.AAX, helper.AAX, tibOffset);
                /* Get entry in VMT -> EAX */
                os.writeMOV(asize, helper.AAX, helper.AAX,
                    arrayDataOffset + (tibIndex * slotSize));

                /* Now invoke the method */
                os.writeCALL(helper.AAX, context.getVmMethodNativeCodeField().getOffset());
                helper.pushReturnValue(methodRef.getSignature());
                // Result is already on the stack.
            }
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
        L1AHelper.requestRegister(eContext, X86Register.EDX);
        final IntItem result = (IntItem) ifac.createReg(eContext, JvmType.INT,
            X86Register.EDX);
        eContext.getGPRPool().transferOwnerTo(X86Register.EDX, result);

        final IntItem v2 = vstack.popInt();
        final IntItem v1 = vstack.popInt();

        // v1 must be in EAX
        L1AHelper.requestRegister(eContext, X86Register.EAX, v1);

        // Load
        v2.loadIf(eContext, ~Item.Kind.LOCAL);
        v1.loadTo(eContext, X86Register.EAX);

        // Calculate
        os.writeCDQ(BITS32); // EAX -> EDX:EAX
        if (v2.isLocal()) {
            os.writeIDIV_EAX(BITS32, helper.BP, v2.getOffsetToFP(eContext));
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
        wreturn(JvmType.INT, true);
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
        fpCompiler.convert(JvmType.LONG, JvmType.DOUBLE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_l2f()
     */
    public final void visit_l2f() {
        fpCompiler.convert(JvmType.LONG, JvmType.FLOAT);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_l2i()
     */
    public final void visit_l2i() {
        final LongItem v = vstack.popLong();
        if (v.isConstant()) {
            vstack.push(ifac.createIConst(eContext, (int) v.getValue()));
        } else {
            final X86RegisterPool pool = eContext.getGPRPool();
            final IntItem result;
            v.load(eContext);
            if (os.isCode32()) {
                final X86Register lsb = v.getLsbRegister(eContext);
                v.release(eContext);
                pool.request(lsb);
                result = (IntItem) ifac.createReg(eContext, JvmType.INT, lsb);
                pool.transferOwnerTo(lsb, result);
            } else {
                final X86Register reg = v.getRegister(eContext);
                final X86Register intReg = pool.getRegisterInSameGroup(reg,
                    JvmType.INT);
                v.release(eContext);
                pool.request(intReg);
                result = (IntItem) ifac.createReg(eContext, JvmType.INT, intReg);
                pool.transferOwnerTo(intReg, result);
            }
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
        GPR idxr = idx.getRegister();
        final GPR refr = ref.getRegister();

        // Verify
        checkBounds(ref, idx);

        // Get data
        final DoubleWordItem result = L1AHelper.requestDoubleWordRegisters(
            eContext, JvmType.LONG);
        if (os.isCode64()) {
            final GPR64 idxr64 = (GPR64) eContext.getGPRPool().getRegisterInSameGroup(idxr, JvmType.LONG);
            os.writeMOVSXD(idxr64, (GPR32) idxr);
            idxr = idxr64;
        }
        os.writeLEA(refr, refr, idxr, 8, arrayDataOffset);
        if (os.isCode32()) {
            os.writeMOV(INTSIZE, result.getLsbRegister(eContext), refr, LSB);
            os.writeMOV(INTSIZE, result.getMsbRegister(eContext), refr, MSB);
        } else {
            os.writeMOV(BITS64, result.getRegister(eContext), refr, 0);
        }

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
        v1.load(eContext);

        // Claim result reg
        final IntItem result = (IntItem) L1AHelper.requestWordRegister(
            eContext, JvmType.INT, false);
        final GPR resr = result.getRegister();

        final Label curInstrLabel = getCurInstrLabel();
        final Label ltLabel = new Label(curInstrLabel + "lt");
        final Label endLabel = new Label(curInstrLabel + "end");

        // Calculate
        os.writeXOR(resr, resr);
        if (os.isCode32()) {
            final GPR v2_lsb = v2.getLsbRegister(eContext);
            final GPR v2_msb = v2.getMsbRegister(eContext);
            final GPR v1_lsb = v1.getLsbRegister(eContext);
            final GPR v1_msb = v1.getMsbRegister(eContext);
            os.writeSUB(v1_lsb, v2_lsb);
            os.writeSBB(v1_msb, v2_msb);
            os.writeJCC(ltLabel, X86Constants.JL); // JL
            os.writeOR(v1_lsb, v1_msb);
        } else {
            final GPR64 v2r = v2.getRegister(eContext);
            final GPR64 v1r = v1.getRegister(eContext);
            os.writeCMP(v1r, v2r);
            os.writeJCC(ltLabel, X86Constants.JL); // JL
        }
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
        vstack.push(ifac.createLConst(eContext, v));
    }

    /**
     * @param value
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ldc(VmConstClass)
     */
    public final void visit_ldc(VmConstClass classRef) {
        counters.getCounter("ldc-class").inc();
        // Push all, since we're going to call other methods
        vstack.push(eContext);

        L1AHelper.requestRegister(eContext, helper.AAX);
        final GPR classr = (GPR) L1AHelper.requestRegister(eContext,
            JvmType.REFERENCE, false);

        // Resolve the class
        writeResolveAndLoadClassToReg(classRef, classr);

        // Call SoftByteCodes#getClassForVmType
        os.writePUSH(classr);
        L1AHelper.releaseRegister(eContext, helper.AAX); // So it can be used by invoke

        invokeJavaMethod(context.getGetClassForVmTypeMethod());
        // The resulting class is now on the stack

        // Release
        L1AHelper.releaseRegister(eContext, classr);
    }


    /**
     * Push the given VmType on the stack.
     */
    public final void visit_ldc(VmType<?> value) {
        final WordItem item = L1AHelper.requestWordRegister(eContext, JvmType.REFERENCE, false);
        final Label curInstrLabel = getCurInstrLabel();
        final GPR reg = item.getRegister();

        // Load the class from the statics table
        if (os.isCode32()) {
            helper.writeGetStaticsEntry(curInstrLabel, reg, value);
        } else {
            helper.writeGetStaticsEntry64(curInstrLabel, (GPR64) reg, (VmSharedStaticsEntry) value);
        }
        vstack.push(item);
    }

    /**
     * @param value
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ldc(VmConstString)
     */
    public final void visit_ldc(VmConstString value) {
        vstack.push(ifac.createAConst(eContext, value));
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ldiv()
     */
    public final void visit_ldiv() {
        // Maintain counter
        counters.getCounter("ldiv").inc();

        if (os.isCode32()) {
            // TODO: port to orp-style
            vstack.push(eContext);
            final LongItem v2 = vstack.popLong();
            final LongItem v1 = vstack.popLong();
            v2.release1(eContext);
            v1.release1(eContext);

            invokeJavaMethod(context.getLdivMethod());
        } else {
            final X86RegisterPool pool = eContext.getGPRPool();
            final LongItem v2 = vstack.popLong();
            final LongItem v1 = vstack.popLong();
            // We need v1 in RAX, so if that is not the case,
            // spill those item using RAX
            L1AHelper.requestRegister(eContext, X86Register.RAX, v1);

            // We need to use RDX, so spill those items using it.
            v1.spillIfUsing(eContext, X86Register.RDX);
            v2.spillIfUsing(eContext, X86Register.RDX);
            L1AHelper.requestRegister(eContext, X86Register.RDX);

            // Load v2, v1 into a register
            v2.load(eContext);
            v1.loadTo64(eContext, X86Register.RAX);

            // RAX -> sign extend RDX:RAX
            os.writeCDQ(BITS64);

            // RAX = RDX:RAX / v2.reg
            os.writeIDIV_EAX(v2.getRegister(eContext));

            // Free unused registers
            pool.release(X86Register.RDX);
            v2.release(eContext);

            // And push the result on the vstack.
            vstack.push(v1);
        }
    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lload(int)
     */
    public final void visit_lload(int index) {
        vstack.push(ifac.createLocal(JvmType.LONG, stackFrame
            .getWideEbpOffset(typeSizeInfo, index)));
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lmul()
     */
    public final void visit_lmul() {
        // Maintain counter
        counters.getCounter("lmul").inc();

        if (os.isCode32()) {
            final Label curInstrLabel = getCurInstrLabel();

            // TODO: port to orp-style
            vstack.push(eContext);
            final LongItem v2 = vstack.popLong();
            final LongItem v1 = vstack.popLong();
            v2.release1(eContext);
            v1.release1(eContext);

            assertCondition(eContext.getGPRPool().isFree(X86Register.EAX), "EAX not free");
            assertCondition(eContext.getGPRPool().isFree(X86Register.EBX), "EBX not free");
            assertCondition(eContext.getGPRPool().isFree(X86Register.ECX), "ECX not free");
            assertCondition(eContext.getGPRPool().isFree(X86Register.EDX), "EDX not free");
            assertCondition(eContext.getGPRPool().isFree(X86Register.ESI), "ESI not free");

            writePOP64(X86Register.EBX, X86Register.ECX); // Value 2
            final GPR v2_lsb = X86Register.EBX;
            final GPR v2_msb = X86Register.ECX;
            writePOP64(X86Register.ESI, X86Register.EDI); // Value 1
            final GPR v1_lsb = X86Register.ESI;
            final GPR v1_msb = X86Register.EDI;

            final Label tmp1 = new Label(curInstrLabel + "$tmp1");
            final Label tmp2 = new Label(curInstrLabel + "$tmp2");
            final GPR EAX = X86Register.EAX;
            final GPR EDX = X86Register.EDX;

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
                .requestDoubleWordRegisters(eContext, JvmType.LONG, EAX,
                    EDX);
            vstack.push(result);
        } else {
            final LongItem v2 = vstack.popLong();
            final LongItem v1 = vstack.popLong();
            v2.load(eContext);
            v1.load(eContext);
            os.writeIMUL(v1.getRegister(eContext), v2.getRegister(eContext));
            v2.release(eContext);
            vstack.push(v1);
        }
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lneg()
     */
    public final void visit_lneg() {
        final LongItem v = vstack.popLong();

        if (v.isConstant()) {
            vstack.push(ifac.createLConst(eContext, -v.getValue()));
        } else {
            // Load val
            v.load(eContext);
            if (os.isCode32()) {
                final GPR lsb = v.getLsbRegister(eContext);
                final GPR msb = v.getMsbRegister(eContext);

                // Calculate
                os.writeNEG(msb); // msb := -msb
                os.writeNEG(lsb); // lsb := -lsb
                os.writeSBB(msb, 0); // high += borrow
            } else {
                final GPR64 reg = v.getRegister(eContext);
                // Calculate
                os.writeNEG(reg); // reg := -reg
            }

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
        // BootLog.debug("lookupswitch length=" + n);

        final IntItem key = vstack.popInt();
        key.load(eContext);
        final GPR keyr = key.getRegister();
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
        if (os.isCode32()) {
            // TODO: port to orp-style
            vstack.push(eContext);
            Item v2 = vstack.pop(JvmType.LONG);
            Item v1 = vstack.pop(JvmType.LONG);
            v2.release1(eContext);
            v1.release1(eContext);

            invokeJavaMethod(context.getLremMethod());
        } else {
            // Pre-claim result in RDX
            L1AHelper.requestRegister(eContext, X86Register.RDX);
            final LongItem result = (LongItem) ifac.createReg(eContext, JvmType.LONG,
                X86Register.RDX);
            eContext.getGPRPool().transferOwnerTo(X86Register.RDX, result);

            final LongItem v2 = vstack.popLong();
            final LongItem v1 = vstack.popLong();

            // v1 must be in RAX
            L1AHelper.requestRegister(eContext, X86Register.RAX, v1);

            // Load
            v2.loadIf(eContext, ~Item.Kind.LOCAL);
            v1.loadTo64(eContext, X86Register.RAX);

            // Calculate
            os.writeCDQ(BITS64); // RAX -> RDX:RAX
            if (v2.isLocal()) {
                os.writeIDIV_EAX(BITS64, helper.BP, v2.getOffsetToFP(eContext));
            } else {
                os.writeIDIV_EAX(v2.getRegister(eContext));
            }

            // Result
            vstack.push(result);

            // Release
            v1.release(eContext);
            v2.release(eContext);
        }
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lreturn()
     */
    public final void visit_lreturn() {
        dwreturn(JvmType.LONG, true);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lshl()
     */
    public final void visit_lshl() {
        final GPR ECX = X86Register.ECX;

        final IntItem cnt = vstack.popInt();
        final LongItem val = vstack.popLong();

        if (!cnt.uses(ECX)) {
            val.spillIfUsing(eContext, ECX);
            L1AHelper.requestRegister(eContext, ECX, cnt);
            cnt.loadTo(eContext, ECX);
        }
        val.load(eContext);

        if (os.isCode32()) {
            final GPR v1_lsb = val.getLsbRegister(eContext);
            final GPR v1_msb = val.getMsbRegister(eContext);
            final Label curInstrLabel = getCurInstrLabel();

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
        } else {
            final GPR64 v1r = val.getRegister(eContext);
            os.writeSHL_CL(v1r);
        }

        // Release
        cnt.release(eContext);
        vstack.push(val);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lshr()
     */
    public final void visit_lshr() {
        final GPR ECX = X86Register.ECX;

        final IntItem cnt = vstack.popInt();
        final LongItem val = vstack.popLong();

        // Get cnt into ECX
        if (!cnt.uses(ECX)) {
            val.spillIfUsing(eContext, ECX);
            L1AHelper.requestRegister(eContext, ECX, cnt);
            cnt.loadTo(eContext, ECX);
        }

        // Load val
        val.load(eContext);

        if (os.isCode32()) {
            final X86Register.GPR lsb = val.getLsbRegister(eContext);
            final X86Register.GPR msb = val.getMsbRegister(eContext);
            final Label curInstrLabel = getCurInstrLabel();

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
        } else {
            final GPR64 valr = val.getRegister(eContext);
            os.writeSAR_CL(valr);
        }

        vstack.push(val);

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
        final GPR ECX = X86Register.ECX;

        final IntItem cnt = vstack.popInt();
        final LongItem val = vstack.popLong();

        // Get cnt into ECX
        if (!cnt.uses(ECX)) {
            val.spillIfUsing(eContext, ECX);
            L1AHelper.requestRegister(eContext, ECX, cnt);
            cnt.loadTo(eContext, ECX);
        }

        // Load val
        val.load(eContext);

        if (os.isCode32()) {
            final X86Register.GPR lsb = val.getLsbRegister(eContext);
            final X86Register.GPR msb = val.getMsbRegister(eContext);
            final Label curInstrLabel = getCurInstrLabel();

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
        } else {
            final GPR64 valr = val.getRegister(eContext);
            os.writeSHR_CL(valr);
        }

        // Push
        vstack.push(val);

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
        counters.getCounter("monitor-enter").inc();

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
        counters.getCounter("monitor-exit").inc();

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
        counters.getCounter("multianewarray").inc();

        // flush all vstack items to the stack
        // all registers are freed
        vstack.push(eContext);

        // Create the dimensions array
        helper.writePushStaticsEntry(getCurInstrLabel(), helper.getMethod().getDeclaringClass()); /* currentClass */
        os.writePUSH(10); /* type=int */
        os.writePUSH(dimensions); /* elements */
        invokeJavaMethod(context.getAllocPrimitiveArrayMethod());
        final RefItem dims = vstack.popRef();
        final GPR dimsr = dims.getRegister();
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
        final GPR classr = (GPR) L1AHelper.requestRegister(eContext,
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
        counters.getCounter("new").inc();

        // Push all
        vstack.push(eContext);

        // Allocate tmp register
        final GPR classr = (GPR) L1AHelper.requestRegister(eContext,
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
        counters.getCounter("newarray").inc();

        // Load count
        final IntItem count = vstack.popInt();
        count.loadIf(eContext, Item.Kind.STACK);

        // flush stack, result also on stack
        vstack.push(eContext);

        // Setup a call to SoftByteCodes.allocArray
        helper.writePushStaticsEntry(getCurInstrLabel(), helper.getMethod().getDeclaringClass()); /* currentClass */
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
        generic_pop(helper.SLOTSIZE);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_pop2()
     */
    public final void visit_pop2() {
        generic_pop(helper.SLOTSIZE * 2);
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
        final GPR refr = ref.getRegister();

        if (!wide) {
            final WordItem wval = (WordItem) val;
            final GPR valr = wval.getRegister();
            final char fieldType = field.getSignature().charAt(0);

            // Store field
            switch (fieldType) {
                case 'Z': // boolean
                case 'B': // byte
                    wval.loadToBITS8GPR(eContext);
                    os.writeMOV(BITS8, refr, offset, wval.getRegister());
                    break;
                case 'C': // char
                case 'S': // short
                    os.writeMOV(BITS16, refr, offset, valr);
                    break;
                case 'F': // float
                case 'I': // int
                case 'L': // Object
                case '[': // array
                    os.writeMOV(valr.getSize(), refr, offset, valr);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown fieldType: " + fieldType);
            }
            // Writebarrier
            if (!inf.isPrimitive() && helper.needsWriteBarrier()) {
                final GPR tmp = (GPR) L1AHelper.requestRegister(eContext,
                    JvmType.REFERENCE, false);
                helper.writePutfieldWriteBarrier(inf, refr, valr, tmp);
                L1AHelper.releaseRegister(eContext, tmp);
            }
        } else {
            final DoubleWordItem dval = (DoubleWordItem) val;
            if (os.isCode32()) {
                os.writeMOV(BITS32, refr, offset + MSB, dval
                    .getMsbRegister(eContext));
                os.writeMOV(BITS32, refr, offset + LSB, dval
                    .getLsbRegister(eContext));
            } else {
                os.writeMOV(BITS64, refr, offset, dval.getRegister(eContext));
            }
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
        final Label curInstrLabel = getCurInstrLabel();
        fieldRef.resolve(loader);
        final VmStaticField sf = (VmStaticField) fieldRef.getResolvedVmField();

        // Initialize class if needed
        if (!sf.getDeclaringClass().isAlwaysInitialized()) {
            writeInitializeClass(fieldRef);
        }

        // Get value
        final Item val = vstack.pop();
        val.load(eContext);

        // Put static field
        if (!fieldRef.isWide()) {
            final WordItem wval = (WordItem) val;
            final GPR valr = wval.getRegister();

            if (os.isCode32() || (wval.getType() != JvmType.REFERENCE)) {
                if (sf.isShared()) {
                    helper.writePutStaticsEntry(curInstrLabel, valr,
                        (VmSharedStaticsEntry) sf);
                } else {
                    final GPR tmp = (GPR) L1AHelper.requestRegister(eContext,
                        JvmType.REFERENCE, false);
                    helper.writePutStaticsEntry(curInstrLabel, valr,
                        (VmIsolatedStaticsEntry) sf, tmp);
                    L1AHelper.releaseRegister(eContext, tmp);
                }
            } else {
                if (sf.isShared()) {
                    helper.writePutStaticsEntry64(curInstrLabel, (GPR64) valr,
                        (VmSharedStaticsEntry) sf);
                } else {
                    final GPR tmp = (GPR) L1AHelper.requestRegister(eContext,
                        JvmType.REFERENCE, false);
                    helper.writePutStaticsEntry64(curInstrLabel, (GPR64) valr,
                        (VmIsolatedStaticsEntry) sf, tmp);
                    L1AHelper.releaseRegister(eContext, tmp);
                }
            }
            if (!sf.isPrimitive() && helper.needsWriteBarrier()) {
                final GPR tmp = (GPR) L1AHelper.requestRegister(eContext,
                    JvmType.INT, false);
                helper.writePutstaticWriteBarrier(sf, valr, tmp);
                L1AHelper.releaseRegister(eContext, tmp);
            }
        } else {
            final DoubleWordItem dval = (DoubleWordItem) val;
            if (os.isCode32()) {
                if (sf.isShared()) {
                    helper.writePutStaticsEntry64(curInstrLabel, dval
                        .getLsbRegister(eContext), dval
                        .getMsbRegister(eContext),
                        (VmSharedStaticsEntry) sf);
                } else {
                    final GPR tmp = (GPR) L1AHelper.requestRegister(eContext,
                        JvmType.REFERENCE, false);
                    helper.writePutStaticsEntry64(curInstrLabel, dval
                        .getLsbRegister(eContext), dval
                        .getMsbRegister(eContext),
                        (VmIsolatedStaticsEntry) sf, tmp);
                    L1AHelper.releaseRegister(eContext, tmp);
                }
            } else {
                if (sf.isShared()) {
                    helper.writePutStaticsEntry64(curInstrLabel, dval
                        .getRegister(eContext), (VmSharedStaticsEntry) sf);
                } else {
                    final GPR tmp = (GPR) L1AHelper.requestRegister(eContext,
                        JvmType.REFERENCE, false);
                    helper.writePutStaticsEntry64(curInstrLabel, dval
                        .getRegister(eContext),
                        (VmIsolatedStaticsEntry) sf, tmp);
                    L1AHelper.releaseRegister(eContext, tmp);
                }
            }
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
        final int ebpOfs = stackFrame.getEbpOffset(typeSizeInfo, index);

        // Load ret & jmp
        os.writeJMP(helper.BP, ebpOfs);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_return()
     */
    public final void visit_return() {
        // Discard vstack first
        while (!vstack.isEmpty()) {
            Item v = vstack.pop();
            if (v.isStack()) {
                // sanity check
                if (VirtualStack.checkOperandStack) {
                    vstack.operandStack.pop(v);
                }
                os.writeLEA(helper.SP, helper.SP, v.getCategory() * helper.SLOTSIZE);
            }
            v.release(eContext);
        }

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
        // IMPROVE: check Jaos implementation
        final IntItem val = vstack.popInt();
        val.load(eContext);
        GPR valr = val.getRegister();
        vstack.push(eContext);

        final int n = addresses.length;
        if ((n > 4) && os.isCode32()) {
            // Optimized version.  Needs some overhead, so only useful for
            // larger tables.
            counters.getCounter("tableswitch-opt").inc();

            final GPR tmp = (GPR) L1AHelper.requestRegister(eContext, JvmType.REFERENCE, false);
            if (os.isCode64()) {
                GPR64 valr64 = L1AHelper.get64BitReg(eContext, valr);
                os.writeMOVSXD(valr64, (GPR32) valr);
                valr = valr64;
            }
            if (lowValue != 0) {
                os.writeSUB(valr, lowValue);
            }
            // If outsite low-high range, jump to default
            os.writeCMP_Const(valr, n);
            os.writeJCC(helper.getInstrLabel(defAddress), X86Constants.JAE);

            final Label curInstrLabel = getCurInstrLabel();
            final Label l1 = new Label(curInstrLabel + "$$l1");
            final Label l2 = new Label(curInstrLabel + "$$l2");
            final int l12distance = os.isCode32() ? 12 : 23;

            // Get absolute address of l1 into S0. (do not use
            // stackMgr.writePOP!)
            os.writeCALL(l1);
            os.setObjectRef(l1);
            final int l1Ofs = os.getLength();
            os.writePOP(tmp);

            // Calculate absolute address of jumptable entry into S1
            os.writeLEA(tmp, tmp, valr, helper.ADDRSIZE, l12distance);

            // Calculate absolute address of jump target
            if (os.isCode32()) {
                os.writeADD(tmp, tmp, 0);
            } else {
                final GPR32 tmp2 = (GPR32) L1AHelper.requestRegister(eContext, JvmType.INT, false);
                os.writeMOV(BITS32, tmp2, tmp, 0);
                final GPR64 tmp2_64 = L1AHelper.get64BitReg(eContext, tmp2);
                os.writeMOVSXD(tmp2_64, tmp2);
                os.writeADD(tmp, tmp2_64);
                L1AHelper.releaseRegister(eContext, tmp2);
            }
            os.writeLEA(tmp, tmp, 4); // Compensate for writeRelativeObject
            // difference

            // Jump to the calculated address
            os.writeJMP(tmp);

            // Emit offsets relative to where they are emitted
            os.setObjectRef(l2);
            final int l2Ofs = os.getLength();
            for (int i = 0; i < n; i++) {
                os.writeRelativeObjectRef(helper.getInstrLabel(addresses[i]));
            }

            if ((l2Ofs - l1Ofs) != l12distance) {
                if (!os.isTextStream()) {
                    throw new CompileError("l12distance must be "
                        + (l2Ofs - l1Ofs));
                }
            }
            L1AHelper.releaseRegister(eContext, tmp);
        } else {
            // Space wasting, but simple implementation

            counters.getCounter("tableswitch-nonopt").inc();
            for (int i = 0; i < n; i++) {
                os.writeCMP_Const(valr, lowValue + i);
                os.writeJCC(helper.getInstrLabel(addresses[i]), X86Constants.JE); // JE
            }
            os.writeJMP(helper.getInstrLabel(defAddress));
        }

        val.release(eContext);
    }

    /**
     * Load a WordItem out of an array.
     *
     * @param jvmType Type of the array elements
     */
    final void waload(int jvmType) {
        final IntItem idx = vstack.popInt();
        final RefItem ref = vstack.popRef();

        final int valSize;
        final int scale;
        final int resultType;
        switch (jvmType) {
            case JvmType.BYTE:
                valSize = BITS8;
                scale = 1;
                resultType = JvmType.INT;
                break;
            case JvmType.CHAR:
            case JvmType.SHORT:
                valSize = BITS16;
                scale = 2;
                resultType = JvmType.INT;
                break;
            case JvmType.INT:
                valSize = BITS32;
                scale = 4;
                resultType = JvmType.INT;
                break;
            case JvmType.FLOAT:
                valSize = BITS32;
                scale = 4;
                resultType = JvmType.FLOAT;
                break;
            case JvmType.REFERENCE:
                valSize = helper.ADDRSIZE;
                scale = helper.SLOTSIZE;
                resultType = JvmType.REFERENCE;
                break;
            default:
                throw new IllegalArgumentException("Invalid type " + jvmType);
        }

        // Create result
        final WordItem result;
        final GPR resultr;
        result = L1AHelper.requestWordRegister(eContext, resultType,
            (valSize == BYTESIZE));
        resultr = result.getRegister();

        // Load
        idx.loadIf(eContext, ~Item.Kind.CONSTANT);
        ref.load(eContext);
        final GPR refr = ref.getRegister();

        // Verify
        checkBounds(ref, idx);

        if (jvmType == JvmType.CHAR) {
            // Clear resultr, so we avoid a MOVZX afterwards.
            os.writeXOR(resultr, resultr);
        }

        // Load data
        if (idx.isConstant()) {
            final int offset = idx.getValue() * scale;
            os.writeMOV(valSize, resultr, refr, offset + arrayDataOffset);
        } else {
            GPR idxr = idx.getRegister();
            if (os.isCode64()) {
                final GPR64 idxr64 = (GPR64) eContext.getGPRPool().getRegisterInSameGroup(idxr, JvmType.LONG);
                os.writeMOVSXD(idxr64, (GPR32) idxr);
                idxr = idxr64;
            }
            os.writeMOV(valSize, resultr, refr, idxr, scale, arrayDataOffset);
        }

        // Post process
        switch (jvmType) {
            case JvmType.BYTE:
                os.writeMOVSX(resultr, resultr, BYTESIZE);
                break;
                // case JvmType.CHAR:
                // os.writeMOVZX(resultr, resultr, WORDSIZE);
                // break;
            case JvmType.SHORT:
                os.writeMOVSX(resultr, resultr, WORDSIZE);
                break;
        }

        // Release
        ref.release(eContext);
        idx.release(eContext);

        // Push result
        vstack.push(result);
    }

    /**
     * Store a WordItem into an array.
     *
     * @param jvmType Type of the array elements
     */
    final void wastore(int jvmType) {
        final boolean useBarrier = (context.getWriteBarrier() != null);
        final int valSize;
        final int scale;
        final int valType;
        int extraLoadIdxMask = 0;
        switch (jvmType) {
            case JvmType.BYTE:
                valSize = BITS8;
                scale = 1;
                valType = JvmType.INT;
                break;
            case JvmType.CHAR:
            case JvmType.SHORT:
                valSize = BITS16;
                scale = 2;
                valType = JvmType.INT;
                break;
            case JvmType.INT:
                valSize = BITS32;
                scale = 4;
                valType = JvmType.INT;
                break;
            case JvmType.FLOAT:
                valSize = BITS32;
                scale = 4;
                valType = JvmType.FLOAT;
                break;
            case JvmType.REFERENCE:
                valSize = helper.ADDRSIZE;
                scale = helper.SLOTSIZE;
                valType = JvmType.REFERENCE;
                extraLoadIdxMask = useBarrier ? ~Item.Kind.GPR : 0;
                break;
            default:
                throw new IllegalArgumentException("Invalid type " + jvmType);
        }

        final WordItem val = (WordItem) vstack.pop(valType);
        final IntItem idx = vstack.popInt();
        final RefItem ref = vstack.popRef();

        // IMPROVE: optimize case with const value
        // Load
        if (valSize == BYTESIZE) {
            val.loadToBITS8GPR(eContext);
        } else {
            val.load(eContext);
        }
        idx.loadIf(eContext, ~Item.Kind.CONSTANT | extraLoadIdxMask);
        ref.load(eContext);
        final GPR refr = ref.getRegister();
        final GPR valr = val.getRegister();
        final X86RegisterPool pool = eContext.getGPRPool();

        // Verify
        checkBounds(ref, idx);

        //todo spec issue: add type compatibility check (elemType <- valueType), throw ArrayStoreException

        // Store
        if (idx.isConstant()) {
            final int offset = idx.getValue() * scale;
            os.writeMOV(valSize, refr, offset + arrayDataOffset, valr);
        } else {
            GPR idxr = idx.getRegister();
            if (os.isCode64()) {
                final GPR64 idxr64 = (GPR64) pool.getRegisterInSameGroup(idxr, JvmType.LONG);
                os.writeMOVSXD(idxr64, (GPR32) idxr);
                idxr = idxr64;
            }
            os.writeMOV(valSize, refr, idxr, scale, arrayDataOffset, valr);
        }

        // Call write barrier (reference only)
        if ((jvmType == JvmType.REFERENCE) && useBarrier) {
            // the write barrier could easily be modified to avoid using a
            // scratch register
            final GPR idxr;
            if (os.isCode32()) {
                idxr = idx.getRegister();
            } else {
                idxr = (GPR) eContext.getGPRPool().getRegisterInSameGroup(idx.getRegister(), JvmType.LONG);
            }
            final GPR scratch = (GPR) pool.request(JvmType.INT);
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
    private final void wreturn(int jvmType, boolean callVisitReturn) {
        final WordItem val = (WordItem) vstack.pop(jvmType);
        final GPR reg;
        if (os.isCode32() || (jvmType != JvmType.REFERENCE)) {
            reg = X86Register.EAX;
        } else {
            reg = X86Register.RAX;
        }

        // Return value must be in EAX
        if (!val.uses(reg)) {
            L1AHelper.requestRegister(eContext, reg, val);
            val.loadTo(eContext, reg);
        }

        // Release
        val.release(eContext);

        // Do actual return
        if (callVisitReturn) {
            visit_return();
        }
    }

    /**
     * Write code to resolve the given constant field referred to by fieldRef
     *
     * @param fieldRef
     * @param scratch
     */
    private final void writeInitializeClass(VmConstFieldRef fieldRef) {
        // Get fieldRef via constantpool to avoid direct object references in
        // the native code

        final VmType<?> declClass = fieldRef.getResolvedVmField()
            .getDeclaringClass();
        if (!declClass.isAlwaysInitialized()) {
            final Label curInstrLabel = getCurInstrLabel();

            // Allocate a register to hold the class
            final GPR classReg = (GPR) L1AHelper.requestRegister(eContext, JvmType.REFERENCE, false);

            // Load classRef into the register
            // Load the class from the statics table
            if (os.isCode32()) {
                helper.writeGetStaticsEntry(new Label(curInstrLabel + "$$ic"),
                    classReg, declClass);
            } else {
                helper.writeGetStaticsEntry64(new Label(curInstrLabel + "$$ic"),
                    (GPR64) classReg, (VmSharedStaticsEntry) declClass);
            }

            // Write class initialization code
            helper.writeClassInitialize(curInstrLabel, classReg, classReg, declClass);

            // Free class
            L1AHelper.releaseRegister(eContext, classReg);
        }

    }

    /**
     * Write code to pop a 64-bit word from the stack
     *
     * @param lsbReg
     * @param msbReg
     */
    private final void writePOP64(GPR lsbReg, GPR msbReg) {
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
                                                     GPR dst) {
        // Resolve the class
        classRef.resolve(loader);
        final VmType type = classRef.getResolvedVmClass();
        final Label curInstrLabel = getCurInstrLabel();

        // Load the class from the statics table
        if (os.isCode32()) {
            helper.writeGetStaticsEntry(curInstrLabel, dst, type);
        } else {
            helper.writeGetStaticsEntry64(curInstrLabel, (GPR64) dst, (VmSharedStaticsEntry) type);
        }
    }

    /**
     * Generate code for a word load local instruction.
     *
     * @param jvmType
     * @param index
     */
    private final void wload(int jvmType, int index, boolean useStored) {
        Item constValue = constLocals.get(index);
        if (constValue != null) {
            counters.getCounter("const-local").inc();
            vstack.push(constValue.clone(eContext));
        } else if (false && useStored && (wstoreReg != null)) {
            vstack.push(L1AHelper.requestWordRegister(eContext, jvmType, wstoreReg));
        } else {
            vstack.push(ifac.createLocal(jvmType, stackFrame
                .getEbpOffset(typeSizeInfo, index)));
        }
    }

    /**
     * Does the local variable with the given index and current
     * program counter escape the current basic block?
     *
     * @param index
     * @return
     */
    private final boolean localEscapesBasicBlock(int index) {
        if (true) {
            return true;
        }
        VmLocalVariable var;
        var = currentMethod.getBytecode().getVariable(curAddress, index);
        if (var == null) {
            var = currentMethod.getBytecode().getVariable(parser.getNextAddress(), index);
        }
        if (var != null) {
            final int varBegin = var.getStartPC();
            final int varEnd = var.getEndPC();
            return ((varBegin < currentBasicBlock.getStartPC())
                || (varEnd > currentBasicBlock.getEndPC()));
        } else {
            return true;
        }
    }

    /**
     * Store a word item into a local variable
     *
     * @param jvmType
     * @param index
     */
    private final void wstore(int jvmType, int index) {
        final int disp = stackFrame.getEbpOffset(typeSizeInfo, index);
        wstoreReg = null;

        // Pin down (load) other references to this local
        vstack.loadLocal(eContext, disp);

        // Load
        final WordItem val = (WordItem) vstack.pop(jvmType);
        final boolean vconst = val.isConstant();
        if (vconst) {
            // Store constant locals
            constLocals.put(index, val.clone(eContext));
        } else {
            // Not constant anymore, remove it
            constLocals.remove(index);
        }
        if (vconst && (jvmType == JvmType.INT)) {
            if (localEscapesBasicBlock(index)) {
                // Store constant int
                final int ival = ((IntItem) val).getValue();
                os.writeMOV_Const(BITS32, helper.BP, disp, ival);
            }
        } else if (vconst && (jvmType == JvmType.FLOAT)) {
            if (localEscapesBasicBlock(index)) {
                // Store constant float
                final int ival = Float.floatToRawIntBits(((FloatItem) val)
                    .getValue());
                os.writeMOV_Const(BITS32, helper.BP, disp, ival);
            }
        } else if (val.isFPUStack()) {
            // Ensure item is on top of fpu stack
            FPUHelper.fxch(os, vstack.fpuStack, val);
            if (jvmType == JvmType.FLOAT) {
                os.writeFSTP32(helper.BP, disp);
            } else {
                os.writeFISTP32(helper.BP, disp);
            }
            vstack.fpuStack.pop(val);
        } else if (val.isStack()) {
            // Must be top of stack
            if (VirtualStack.checkOperandStack) {
                vstack.operandStack.pop(val);
            }
            os.writePOP(helper.BP, disp);
        } else {
            // Load into register
            val.load(eContext);
            final GPR valr = val.getRegister();
            // Store
            os.writeMOV(valr.getSize(), helper.BP, disp, valr);
            wstoreReg = valr;
        }

        // Release
        val.release(eContext);
    }

    /**
     * Insert a yieldpoint into the code
     */
    public final void yieldPoint() {
        helper.writeYieldPoint(getCurInstrLabel());
    }

    /**
     * @return Returns the curInstrLabel.
     */
    private final Label getCurInstrLabel() {
        if (_curInstrLabel == null) {
            _curInstrLabel = helper.getInstrLabel(this.curAddress);
        }
        return _curInstrLabel;
    }
    
    private void pushFloat(Item floatItem) {
        // TODO should we do the same check for all calls to vstack.fpuStack.push(Item) ? 
        if (!vstack.fpuStack.hasCapacity(1)) {
            vstack.push(eContext);
        }
        
        vstack.fpuStack.push(floatItem);
    }
}
