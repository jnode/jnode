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

import static org.jnode.vm.compiler.ir.AddressingMode.CONSTANT;
import static org.jnode.vm.compiler.ir.AddressingMode.REGISTER;
import static org.jnode.vm.compiler.ir.AddressingMode.STACK;
import static org.jnode.vm.compiler.ir.AddressingMode.TOPS;
import static org.jnode.vm.x86.compiler.X86CompilerConstants.INTSIZE;

import org.jnode.assembler.Label;
import org.jnode.assembler.x86.X86Assembler;
import org.jnode.assembler.x86.X86Constants;
import org.jnode.assembler.x86.X86Register;
import org.jnode.assembler.x86.X86Register.GPR;
import org.jnode.bootlog.BootLogInstance;
import org.jnode.vm.JvmType;
import org.jnode.vm.classmgr.ObjectLayout;
import org.jnode.vm.classmgr.Signature;
import org.jnode.vm.classmgr.TIBLayout;
import org.jnode.vm.classmgr.VmArray;
import org.jnode.vm.classmgr.VmClassType;
import org.jnode.vm.classmgr.VmConstClass;
import org.jnode.vm.classmgr.VmConstFieldRef;
import org.jnode.vm.classmgr.VmConstMethodRef;
import org.jnode.vm.classmgr.VmField;
import org.jnode.vm.classmgr.VmInstanceField;
import org.jnode.vm.classmgr.VmInstanceMethod;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmSharedStaticsEntry;
import org.jnode.vm.classmgr.VmStaticField;
import org.jnode.vm.classmgr.VmStaticMethod;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.compiler.CompileError;
import org.jnode.vm.compiler.ir.AddressingMode;
import org.jnode.vm.compiler.ir.CodeGenerator;
import org.jnode.vm.compiler.ir.Constant;
import org.jnode.vm.compiler.ir.IRBasicBlock;
import org.jnode.vm.compiler.ir.IntConstant;
import org.jnode.vm.compiler.ir.LongConstant;
import org.jnode.vm.compiler.ir.Operand;
import org.jnode.vm.compiler.ir.RegisterLocation;
import org.jnode.vm.compiler.ir.RegisterPool;
import org.jnode.vm.compiler.ir.StackLocation;
import org.jnode.vm.compiler.ir.Variable;
import org.jnode.vm.compiler.ir.quad.ArrayAssignQuad;
import org.jnode.vm.compiler.ir.quad.ArrayLengthAssignQuad;
import org.jnode.vm.compiler.ir.quad.ArrayStoreQuad;
import org.jnode.vm.compiler.ir.quad.BinaryOperation;
import org.jnode.vm.compiler.ir.quad.BinaryQuad;
import org.jnode.vm.compiler.ir.quad.BranchCondition;
import org.jnode.vm.compiler.ir.quad.CheckcastQuad;
import org.jnode.vm.compiler.ir.quad.ConditionalBranchQuad;
import org.jnode.vm.compiler.ir.quad.ConstantClassAssignQuad;
import org.jnode.vm.compiler.ir.quad.ConstantRefAssignQuad;
import org.jnode.vm.compiler.ir.quad.ConstantStringAssignQuad;
import org.jnode.vm.compiler.ir.quad.InstanceofAssignQuad;
import org.jnode.vm.compiler.ir.quad.InterfaceCallAssignQuad;
import org.jnode.vm.compiler.ir.quad.InterfaceCallQuad;
import org.jnode.vm.compiler.ir.quad.LookupswitchQuad;
import org.jnode.vm.compiler.ir.quad.MonitorenterQuad;
import org.jnode.vm.compiler.ir.quad.MonitorexitQuad;
import org.jnode.vm.compiler.ir.quad.NewAssignQuad;
import org.jnode.vm.compiler.ir.quad.NewMultiArrayAssignQuad;
import org.jnode.vm.compiler.ir.quad.NewObjectArrayAssignQuad;
import org.jnode.vm.compiler.ir.quad.NewPrimitiveArrayAssignQuad;
import org.jnode.vm.compiler.ir.quad.Quad;
import org.jnode.vm.compiler.ir.quad.RefAssignQuad;
import org.jnode.vm.compiler.ir.quad.RefStoreQuad;
import org.jnode.vm.compiler.ir.quad.SpecialCallAssignQuad;
import org.jnode.vm.compiler.ir.quad.SpecialCallQuad;
import org.jnode.vm.compiler.ir.quad.StaticCallAssignQuad;
import org.jnode.vm.compiler.ir.quad.StaticCallQuad;
import org.jnode.vm.compiler.ir.quad.StaticRefAssignQuad;
import org.jnode.vm.compiler.ir.quad.StaticRefStoreQuad;
import org.jnode.vm.compiler.ir.quad.TableswitchQuad;
import org.jnode.vm.compiler.ir.quad.ThrowQuad;
import org.jnode.vm.compiler.ir.quad.UnaryOperation;
import org.jnode.vm.compiler.ir.quad.UnaryQuad;
import org.jnode.vm.compiler.ir.quad.UnconditionalBranchQuad;
import org.jnode.vm.compiler.ir.quad.VarReturnQuad;
import org.jnode.vm.compiler.ir.quad.VariableRefAssignQuad;
import org.jnode.vm.compiler.ir.quad.VirtualCallAssignQuad;
import org.jnode.vm.compiler.ir.quad.VirtualCallQuad;
import org.jnode.vm.compiler.ir.quad.VoidReturnQuad;
import org.jnode.vm.facade.TypeSizeInfo;
import org.jnode.vm.facade.VmUtils;
import org.jnode.vm.x86.compiler.X86CompilerHelper;
import org.jnode.vm.x86.compiler.X86IMTCompiler32;
import org.jnode.vm.x86.compiler.X86JumpTable;

/**
 * @author Madhu Siddalingaiah
 * @author Levente S\u00e1ntha
 */
public class GenericX86CodeGenerator<T extends X86Register> extends CodeGenerator<T> implements
    X86Constants {
    private static final GPR SR1 = X86Register.EAX;

    // private static final Register SR2 = Register.EBX;
    // private static final Register SR3 = Register.ECX;
    // private static final Register SR4 = Register.EDX;
    public static final int BYTESIZE = X86Constants.BITS8;

    public static final int WORDSIZE = X86Constants.BITS16;
    protected final VmMethod currentMethod;
    protected final X86StackFrame stackFrame;
    protected final TypeSizeInfo typeSizeInfo;

    protected Variable<T>[] spilledVariables;

    protected X86Assembler os;
    protected int startOffset;

    private int displacement = -4;

    private String labelPrefix;

    private String instrLabelPrefix;

    private Label[] addressLabels;

    private final RegisterPool<T> registerPool;

    /**
     * Initialize this instance
     */
    public GenericX86CodeGenerator(X86Assembler x86Stream, RegisterPool<T> pool, int lenght, TypeSizeInfo typeSizeInfo,
                                   X86StackFrame stackFrame, VmMethod method) {
        CodeGenerator.setCodeGenerator(this);
        this.registerPool = pool;
        this.os = x86Stream;

        labelPrefix = stackFrame.getHelper().genLabel("").toString();
        instrLabelPrefix = labelPrefix + "_bci_";
        addressLabels = new Label[lenght];
        this.typeSizeInfo = typeSizeInfo;
        this.stackFrame = stackFrame;
        this.currentMethod = method;
    }

    public final Label getInstrLabel(int address) {
        Label l = addressLabels[address];
        if (l == null) {
            l = new Label(instrLabelPrefix + address);
            addressLabels[address] = l;
        }
        return l;
    }

    public RegisterPool<T> getRegisterPool() {
        return registerPool;
    }

    public boolean supports3AddrOps() {
        return false;
    }

//    public void setArgumentVariables(Variable<T>[] vars, int nArgs) {
//        displacement = 0;
//        for (int i = 0; i < nArgs; i += 1) {
//            // TODO this might not be right, check with Ewout
//            displacement = vars[i].getIndex() * 4;
//            vars[i].setLocation(new StackLocation<T>(displacement));
//        }
//        // not sure how big the last arg is...
//        displacement += 8;
//    }

//    public void setSpilledVariables(Variable<T>[] variables) {
//        this.spilledVariables = variables;
//        int n = spilledVariables.length;
//        for (int i = 0; i < n; i += 1) {
//            StackLocation<T> loc = (StackLocation<T>) spilledVariables[i]
//                .getLocation();
//            loc.setDisplacement(displacement);
//            switch (spilledVariables[i].getType()) {
//                case Operand.BYTE:
//                case Operand.CHAR:
//                case Operand.SHORT:
//                case Operand.INT:
//                case Operand.FLOAT:
//                case Operand.REFERENCE:
//                    displacement -= 4;
//                    break;
//                case Operand.LONG:
//                case Operand.DOUBLE:
//                    displacement -= 8;
//                    break;
//            }
//        }
//    }

    public void setSpilledVariables(Variable[] variables) {
        this.spilledVariables = variables;
        int n = spilledVariables.length;
        int noArgs = currentMethod.getNoArguments();
        for (int i = 0; i < n; i += 1) {
            Variable<X86Register> var = (Variable<X86Register>) spilledVariables[i];
            StackLocation loc = (StackLocation) var.getLocation();
            loc.setDisplacement(stackFrame.getEbpOffset(typeSizeInfo, noArgs + i));
        }
    }

//    public void emitHeader() {
//        os.writePUSH(X86Register.EBP);
//        // os.writePUSH(context.getMagic());
//        // os.writePUSH(0); // PC, which is only used in interpreted methods
//        /** EAX MUST contain the VmMethod structure upon entry of the method */
//        // os.writePUSH(Register.EAX);
//        os.writeMOV(X86Constants.BITS32, X86Register.EBP, X86Register.ESP);
//    }

    @Override
    public void emitHeader() {
        this.startOffset = stackFrame.emitHeader();
    }

    public void generateCodeFor(ConditionalBranchQuad<T> quad) {
        throw new IllegalArgumentException("Unknown operation");
    }

    public void generateCodeFor(ConstantRefAssignQuad<T> quad) {
        checkLabel(quad.getAddress());
        Variable<T> lhs = quad.getLHS();
        if (lhs.getAddressingMode() == REGISTER) {
            T reg1 = ((RegisterLocation<T>) lhs.getLocation()).getRegister();
            IntConstant<T> rhs = (IntConstant<T>) quad.getRHS();
            os.writeMOV_Const((GPR) reg1, rhs.getValue());
        } else if (lhs.getAddressingMode() == STACK) {
            IntConstant<T> rhs = (IntConstant<T>) quad.getRHS();
            int disp1 = ((StackLocation<T>) lhs.getLocation()).getDisplacement();
            // TODO os.writeMOV_Const(X86Register.EBP, disp1, rhs.getValue());
        } else {
            throw new IllegalArgumentException("Unknown operation");
        }
    }

    private int prev_addr = 0;

    public void checkLabel(int address) {
        for (int i = prev_addr + 1; i <= address; i++) {
            Label l = addressLabels[i];
            if (l == null) {
                l = getInstrLabel(i);
            }
            os.setObjectRef(l);
        }
        prev_addr = address;
    }

    public void generateCodeFor(UnconditionalBranchQuad<T> quad) {
        checkLabel(quad.getAddress());
        if (quad.getTargetAddress() < quad.getAddress()) {
            stackFrame.getHelper().writeYieldPoint(getInstrLabel(quad.getAddress()));
        }
        os.writeJMP(getInstrLabel(quad.getTargetAddress()));
    }

    public void generateCodeFor(VariableRefAssignQuad<T> quad) {
        checkLabel(quad.getAddress());

        Variable<T> lhs = quad.getLHS();
        if (lhs.getAddressingMode() == REGISTER) {
            T reg1 = ((RegisterLocation<T>) lhs.getLocation()).getRegister();
            Operand<T> rhs = quad.getRHS();
            AddressingMode mode = rhs.getAddressingMode();
            if (mode == CONSTANT) {
                os.writeMOV_Const((GPR) reg1, ((IntConstant<T>) rhs).getValue());
            } else if (mode == REGISTER) {
                T reg2 = ((RegisterLocation<T>) ((Variable<T>) rhs).getLocation()).getRegister();
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, (GPR) reg2);
                }
            } else if (mode == STACK) {
                int disp2 = ((StackLocation<T>) ((Variable<T>) rhs).getLocation()).getDisplacement();
                os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP, disp2);
            } else if (mode == TOPS) {
                os.writePOP((GPR) reg1);
            } else {
                throw new IllegalArgumentException();
            }
        } else if (lhs.getAddressingMode() == STACK) {
            int disp1 = ((StackLocation<T>) lhs.getLocation()).getDisplacement();
            Operand<T> rhs = quad.getRHS();
            AddressingMode mode = rhs.getAddressingMode();
            if (mode == CONSTANT) {
                //todo investigate lhs.getType() based checks
//                if (lhs.getType() == Operand.INT) {
//                    os.writeMOV_Const(X86Constants.BITS32,  X86Register.EBP, disp1, ((IntConstant<T>) rhs).getValue());
//                } else if (lhs.getType() == Operand.LONG) {
//                    long value = ((LongConstant<T>) rhs).getValue();
//                    final int v_lsb = (int) (value & 0xFFFFFFFFL);
//                    final int v_msb = (int) ((value >>> 32) & 0xFFFFFFFFL);
//                    os.writeMOV_Const(BITS32,  X86Register.EBP, disp1 - stackFrame.getHelper().SLOTSIZE, v_lsb);
//                    os.writeMOV_Const(BITS32,  X86Register.EBP, disp1, v_msb);
//                } else {
//                    throw new IllegalArgumentException("Type: " + lhs.getType());
//                }
                if (rhs instanceof IntConstant) {
                    os.writeMOV_Const(X86Constants.BITS32,  X86Register.EBP, disp1, ((IntConstant<T>) rhs).getValue());
                } else if (rhs instanceof LongConstant) {
                    long value = ((LongConstant<T>) rhs).getValue();
                    final int v_lsb = (int) (value & 0xFFFFFFFFL);
                    final int v_msb = (int) ((value >>> 32) & 0xFFFFFFFFL);
                    os.writeMOV_Const(BITS32,  X86Register.EBP, disp1 - stackFrame.getHelper().SLOTSIZE, v_lsb);
                    os.writeMOV_Const(BITS32,  X86Register.EBP, disp1, v_msb);
                } else {
                    throw new IllegalArgumentException("Type: " + lhs.getType());
                }
            } else if (mode == REGISTER) {
                T reg2 = ((RegisterLocation<T>) ((Variable<T>) rhs).getLocation()).getRegister();
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
            } else if (mode == STACK) {
                //todo optimize it
                int disp2 = ((StackLocation) ((Variable) rhs).getLocation()).getDisplacement();
                if (disp1 != disp2) {
                    os.writePUSH(X86Register.EBP, disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
            } else if (mode == TOPS) {
                os.writePOP(X86Register.EBP, disp1);
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

    public void generateCodeFor(VarReturnQuad<T> quad) {
        checkLabel(quad.getAddress());
        Operand<T> op = quad.getOperand();
        if (op.getAddressingMode() == CONSTANT) {
            if (op instanceof IntConstant) {
                IntConstant<T> iconst = (IntConstant<T>) op;
                os.writeMOV_Const(X86Register.EAX, iconst.getValue());
            } else if (op instanceof LongConstant) {
                LongConstant<T> lconst = (LongConstant<T>) op;
                long value = lconst.getValue();
                if (value != 0) {
                    final int lsbv = (int) (value & 0xFFFFFFFFL);
                    final int msbv = (int) ((value >>> 32) & 0xFFFFFFFFL);

                    os.writeMOV_Const(X86Register.EAX, lsbv);
                    os.writeMOV_Const(X86Register.EDX, msbv);
                } else {
                    os.writeXOR(X86Register.EAX, X86Register.EAX);
                    os.writeXOR(X86Register.EDX, X86Register.EDX);
                }
            } else {
                throw new IllegalArgumentException();
            }
        } else if (op.getAddressingMode() == REGISTER) {
            GPR src = (GPR) ((RegisterLocation<T>) ((Variable<T>) op).getLocation()).getRegister();
            if (!src.equals(X86Register.EAX)) {
                os.writeMOV(X86Constants.BITS32, X86Register.EAX, src);
            }
        } else if (op.getAddressingMode() == STACK) {
            if (op.getType() != Operand.LONG && op.getType() != Operand.DOUBLE) {
                StackLocation<T> stackLoc = (StackLocation<T>) ((Variable<T>) op).getLocation();
                os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EBP, stackLoc.getDisplacement());
            } else if (op.getType() == Operand.LONG) {
                int disp1 = ((StackLocation<T>) ((Variable<T>) op).getLocation()).getDisplacement();
                int disp2 = disp1 - stackFrame.getHelper().SLOTSIZE;
                os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EBP, disp2);
                os.writeMOV(X86Constants.BITS32, X86Register.EDX, X86Register.EBP, disp1);
            } else {
                throw new IllegalArgumentException();
            }
        } else {
            throw new IllegalArgumentException();
        }

        stackFrame.emitReturn();
    }

    public void generateCodeFor(VoidReturnQuad<T> quad) {
        checkLabel(quad.getAddress());

        // TODO: hack for testing
//        os.writeMOV(X86Constants.BITS32, X86Register.ESP, X86Register.EBP);
//        os.writePOP(X86Register.EBP);

//        os.writeRET();

        stackFrame.emitReturn();
    }

    public void generateCodeFor(UnaryQuad<T> quad, Object lhsReg, UnaryOperation operation,
                                Constant<T> con) {
        throw new IllegalArgumentException("Constants should be folded");
    }

    public void generateCodeFor(UnaryQuad<T> quad, Object lhsReg, UnaryOperation operation, Object rhsReg) {
        checkLabel(quad.getAddress());
        switch (operation) {
            case I2L:
                throw new IllegalArgumentException("Unknown operation: " + operation);

            case I2F:
                os.writePUSH((GPR) rhsReg);
                os.writeFILD32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) lhsReg);
                break;

            case I2D:
            case L2I:
            case L2F:
            case L2D:
                throw new IllegalArgumentException("Unknown operation: " + operation);

            case F2I:
                os.writePUSH((GPR) rhsReg);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeFISTP32(X86Register.ESP, 0);
                os.writePOP((GPR) lhsReg);
                break;

            case F2L:
            case F2D:
            case D2I:
            case D2L:
            case D2F:
                throw new IllegalArgumentException("Unknown operation: " + operation);

            case I2B: {
                GPR lhsGpr = (GPR) lhsReg;
                if (lhsGpr.isSuitableForBits8()) {
                    os.writeMOVSX(lhsGpr, (GPR) rhsReg, BYTESIZE);
                } else {
                    os.writeMOVSX(SR1, lhsGpr, BYTESIZE);
                    os.writeMOV(X86Constants.BITS32, lhsGpr, SR1);
                }
                break;
            }

            case I2C:
                os.writeMOVZX((GPR) lhsReg, (GPR) rhsReg, WORDSIZE);
                break;

            case I2S:
                os.writeMOVSX((GPR) lhsReg, (GPR) rhsReg, WORDSIZE);
                break;

            case INEG:
                if (lhsReg != rhsReg) {
                    os.writeMOV(X86Constants.BITS32, (GPR) lhsReg, (GPR) rhsReg);
                }
                os.writeNEG((GPR) lhsReg);
                break;

            case LNEG:
                throw new IllegalArgumentException("Unknown operation: " + operation);

            case FNEG:
                os.writePUSH((GPR) rhsReg);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeFCHS();
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) lhsReg);
                break;

            case DNEG:
            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
    }

    public void generateCodeFor(UnaryQuad<T> quad, Object lhsReg, UnaryOperation operation,
                                int rhsDisp) {
        checkLabel(quad.getAddress());
        switch (operation) {
            case I2L:
                throw new IllegalArgumentException("Unknown operation: " + operation);

            case I2F:
                os.writePUSH(X86Register.EBP, rhsDisp);
                os.writeFILD32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) lhsReg);
                break;

            case L2I:
                os.writeMOV(BITS32, (GPR) lhsReg, X86Register.EBP, rhsDisp - stackFrame.getHelper().SLOTSIZE);
                break;
            case I2D:
            case L2F:
            case L2D:
                throw new IllegalArgumentException("Unknown operation: " + operation);

            case F2I:
                os.writePUSH(X86Register.EBP, rhsDisp);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeFISTP32(X86Register.ESP, 0);
                os.writePOP((GPR) lhsReg);
                break;

            case F2L:
            case F2D:
            case D2I:
            case D2L:
            case D2F:
                throw new IllegalArgumentException("Unknown operation: " + operation);

            case I2B: {
                GPR lhsGpr = (GPR) lhsReg;
                if (lhsGpr.isSuitableForBits8()) {
                    os.writeMOVSX(lhsGpr, SR1, rhsDisp, BYTESIZE);
                } else {
                    os.writeMOVSX(SR1, X86Register.EBP, rhsDisp, BYTESIZE);
                    os.writeMOV(X86Constants.BITS32, lhsGpr, SR1);
                }
                break;
            }

            case I2C:
                os.writeMOVZX((GPR) lhsReg, X86Register.EBP, rhsDisp, WORDSIZE);
                break;

            case I2S:
                os.writeMOVSX((GPR) lhsReg, X86Register.EBP, rhsDisp, WORDSIZE);
                break;

            case INEG:
                os.writeMOV(X86Constants.BITS32, (GPR) lhsReg, X86Register.EBP, rhsDisp);
                os.writeNEG((GPR) lhsReg);
                break;

            case LNEG:
                throw new IllegalArgumentException("Unknown operation: " + operation);

            case FNEG:
                os.writePUSH(X86Register.EBP, rhsDisp);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeFCHS();
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) lhsReg);
                break;

            case DNEG:
            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
    }

    public void generateCodeFor(UnaryQuad<T> quad, int lhsDisp, UnaryOperation operation,
                                Object rhsReg) {
        checkLabel(quad.getAddress());
        switch (operation) {
            case I2L:
                os.writePUSH(X86Register.EAX);
                os.writePUSH(X86Register.EDX);
                os.writeMOV(BITS32, X86Register.EAX, (GPR) rhsReg);
                os.writeCDQ(BITS32);
                os.writeMOV(BITS32, X86Register.EBP, lhsDisp, X86Register.EDX);
                os.writeMOV(BITS32, X86Register.EBP, lhsDisp - stackFrame.getHelper().SLOTSIZE, X86Register.EAX);
                os.writePOP(X86Register.EDX);
                os.writePOP(X86Register.EAX);
                break;

            case I2F:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, lhsDisp, (GPR) rhsReg);
                os.writeFILD32(X86Register.EBP, lhsDisp);
                os.writeFSTP32(X86Register.EBP, lhsDisp);
                break;

            case I2D:
            case L2I:
            case L2F:
            case L2D:
            case F2I:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, lhsDisp, (GPR) rhsReg);
                os.writeFLD32(X86Register.EBP, lhsDisp);
                os.writeFISTP32(X86Register.EBP, lhsDisp);
                break;

            case F2L:
            case F2D:
            case D2I:
            case D2L:
            case D2F:
                throw new IllegalArgumentException("Unknown operation: " + operation);

            case I2B:
                os.writePUSH(SR1);
                os.writeMOVSX(SR1, (GPR) rhsReg, BYTESIZE);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, lhsDisp, SR1);
                os.writePOP(SR1);
                break;

            case I2C:
                os.writePUSH(SR1);
                os.writeMOVZX(SR1, (GPR) rhsReg, WORDSIZE);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, lhsDisp, SR1);
                os.writePOP(SR1);
                break;

            case I2S:
                os.writePUSH(SR1);
                os.writeMOVSX(SR1, (GPR) rhsReg, WORDSIZE);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, lhsDisp, SR1);
                os.writePOP(SR1);
                break;

            case INEG:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, lhsDisp, (GPR) rhsReg);
                os.writeNEG(BITS32, X86Register.EBP, lhsDisp);
                break;

            case LNEG:
                throw new IllegalArgumentException("Unknown operation: " + operation);

            case FNEG:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, lhsDisp, (GPR) rhsReg);
                os.writeFLD32(X86Register.EBP, lhsDisp);
                os.writeFCHS();
                os.writeFSTP32(X86Register.EBP, lhsDisp);
                break;

            case DNEG:
            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
    }

    public void generateCodeFor(UnaryQuad<T> quad, int lhsDisp, UnaryOperation operation,
                                int rhsDisp) {
        checkLabel(quad.getAddress());
        switch (operation) {
            case I2L:
                os.writePUSH(X86Register.EAX);
                os.writePUSH(X86Register.EDX);
                os.writeMOV(BITS32, X86Register.EAX, X86Register.EBP, rhsDisp);
                os.writeCDQ(BITS32);
                os.writeMOV(BITS32, X86Register.EBP, lhsDisp, X86Register.EDX);
                os.writeMOV(BITS32, X86Register.EBP, lhsDisp - stackFrame.getHelper().SLOTSIZE, X86Register.EAX);
                os.writePOP(X86Register.EDX);
                os.writePOP(X86Register.EAX);
                break;

            case I2F:
                os.writeFILD32(X86Register.EBP, rhsDisp);
                os.writeFSTP32(X86Register.EBP, lhsDisp);
                break;

            case I2D:
                throw new IllegalArgumentException("Unknown operation: " + operation);
            case L2I:
                os.writeMOV(BITS32, SR1, X86Register.EBP, rhsDisp - stackFrame.getHelper().SLOTSIZE);
                os.writeMOV(BITS32, X86Register.EBP, lhsDisp, SR1);
                break;
            case L2F:
            case L2D:
                throw new IllegalArgumentException("Unknown operation: " + operation);

            case F2I:
                os.writeFLD32(X86Register.EBP, rhsDisp);
                os.writeFISTP32(X86Register.EBP, lhsDisp);
                break;

            case F2L:
            case F2D:
            case D2I:
            case D2L:
            case D2F:
                throw new IllegalArgumentException("Unknown operation: " + operation);

            case I2B:
                os.writePUSH(SR1);
                os.writeMOVSX(SR1, X86Register.EBP, rhsDisp, BYTESIZE);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, lhsDisp, SR1);
                os.writePOP(SR1);
                break;

            case I2C:
                os.writePUSH(SR1);
                os.writeMOVZX(SR1, X86Register.EBP, rhsDisp, WORDSIZE);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, lhsDisp, SR1);
                os.writePOP(SR1);
                break;

            case I2S:
                os.writePUSH(SR1);
                os.writeMOVSX(SR1, X86Register.EBP, rhsDisp, WORDSIZE);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, lhsDisp, SR1);
                os.writePOP(SR1);
                break;

            case INEG:
                if (rhsDisp != lhsDisp) {
                    os.writePUSH(X86Register.EBP, rhsDisp);
                    os.writePOP(X86Register.EBP, lhsDisp);
                }
                os.writeNEG(BITS32, X86Register.EBP, lhsDisp);
                break;

            case LNEG:
                throw new IllegalArgumentException("Unknown operation: " + operation);

            case FNEG:
                os.writeFLD32(X86Register.EBP, rhsDisp);
                os.writeFCHS();
                os.writeFSTP32(X86Register.EBP, lhsDisp);
                break;

            case DNEG:
            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
    }

    public void generateCodeFor(UnaryQuad<T> quad, int lhsDisp, UnaryOperation operation,
                                Constant<T> con) {
        throw new IllegalArgumentException("Constants should be folded");
    }

    public void generateBinaryOP(T reg1, Constant<T> c2,
                                 BinaryOperation operation, Constant<T> c3) {
        throw new IllegalArgumentException("Constants should be folded");
    }

    public void generateBinaryOP(T reg1, Constant<T> c2,
                                 BinaryOperation operation, T reg3) {
        IntConstant<T> iconst2 = (IntConstant<T>) c2;
        switch (operation) {
            case IADD:
                os.writeMOV_Const((GPR) reg1, iconst2.getValue());
                os.writeADD((GPR) reg1, (GPR) reg3);
                break;

            case IAND:
                os.writeMOV_Const((GPR) reg1, iconst2.getValue());
                os.writeAND((GPR) reg1, (GPR) reg3);
                break;

            case IDIV: // needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writeMOV_Const(X86Register.EAX, iconst2.getValue());
                os.writeCDQ(BITS32);
                if (reg3 == X86Register.EAX) {
                    os.writeIDIV_EAX(BITS32, X86Register.ESP, 0);
                } else if (reg3 == X86Register.EDX) {
                    os.writeIDIV_EAX(BITS32, X86Register.ESP, 4);
                } else {
                    os.writeIDIV_EAX((GPR) reg3);
                }
                if (reg1 == X86Register.EAX) {
                    os.writePOP(X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                } else if (reg1 == X86Register.EDX) {
                    os.writeMOV(X86Constants.BITS32, X86Register.EDX, X86Register.EAX);
                    os.writePOP(X86Register.EAX);
                    os.writeADD(X86Register.ESP, 4);
                } else {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EAX);
                    os.writePOP(X86Register.EAX);
                    os.writePOP(X86Register.EDX);
                }
                break;

            case IMUL:
                os.writeIMUL_3((GPR) reg1, (GPR) reg3, iconst2.getValue());
                break;

            case IOR:
                os.writeMOV_Const((GPR) reg1, iconst2.getValue());
                os.writeOR((GPR) reg1, (GPR) reg3);
                break;

            case IREM: // needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writeMOV_Const(X86Register.EAX, iconst2.getValue());
                os.writeCDQ(BITS32);
                if (reg3 == X86Register.EAX) {
                    os.writeIDIV_EAX(BITS32, X86Register.ESP, 0);
                } else if (reg3 == X86Register.EDX) {
                    os.writeIDIV_EAX(BITS32, X86Register.ESP, 4);
                } else {
                    os.writeIDIV_EAX((GPR) reg3);
                }
                if (reg1 == X86Register.EDX) {
                    os.writePOP(X86Register.EAX);
                    os.writeADD(X86Register.ESP, 4);
                } else if (reg1 == X86Register.EAX) {
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                } else {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EDX);
                    os.writePOP(X86Register.EAX);
                    os.writePOP(X86Register.EDX);
                }
                break;

            case ISHL: // needs CL
                os.writeMOV_Const((GPR) reg1, iconst2.getValue());
                if (reg3 != X86Register.ECX) {
                    os.writePUSH(X86Register.ECX);
                    os.writeMOV(X86Constants.BITS32, X86Register.ECX, (GPR) reg3);
                    os.writeSAL_CL((GPR) reg1);
                    os.writePOP(X86Register.ECX);
                } else {
                    os.writeSAL_CL((GPR) reg1);
                }
                break;

            case ISHR: // needs CL
                os.writeMOV_Const((GPR) reg1, iconst2.getValue());
                if (reg3 != X86Register.ECX) {
                    os.writePUSH(X86Register.ECX);
                    os.writeMOV(X86Constants.BITS32, X86Register.ECX, (GPR) reg3);
                    os.writeSAL_CL((GPR) reg1);
                    os.writePOP(X86Register.ECX);
                } else {
                    os.writeSAR_CL((GPR) reg1);
                }
                break;

            case ISUB:
                os.writeMOV_Const((GPR) reg1, iconst2.getValue());
                os.writeSUB((GPR) reg1, (GPR) reg3);
                break;

            case IUSHR:
                os.writeMOV_Const((GPR) reg1, iconst2.getValue());
                if (reg3 != X86Register.ECX) {
                    os.writePUSH(X86Register.ECX);
                    os.writeMOV(X86Constants.BITS32, X86Register.ECX, (GPR) reg3);
                    os.writeSAL_CL((GPR) reg1);
                    os.writePOP(X86Register.ECX);
                } else {
                    os.writeSHR_CL((GPR) reg1);
                }
                break;

            case IXOR:
                os.writeMOV_Const((GPR) reg1, iconst2.getValue());
                os.writeXOR((GPR) reg1, (GPR) reg3);
                break;

            case DADD:
            case DDIV:
            case DMUL:
            case DREM:
            case DSUB:
                throw new IllegalArgumentException("Unknown operation: " + operation);

            case FADD:
                os.writePUSH(iconst2.getValue());
                os.writeFLD32(X86Register.ESP, 0);
                os.writeMOV(X86Constants.BITS32, X86Register.ESP, 0, (GPR) reg3);
                os.writeFADD32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) reg1);
                break;

            case FDIV:
                os.writePUSH(iconst2.getValue());
                os.writeFLD32(X86Register.ESP, 0);
                os.writeMOV(X86Constants.BITS32, X86Register.ESP, 0, (GPR) reg3);
                os.writeFDIV32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) reg1);
                break;

            case FMUL:
                os.writePUSH(iconst2.getValue());
                os.writeFLD32(X86Register.ESP, 0);
                os.writeMOV(X86Constants.BITS32, X86Register.ESP, 0, (GPR) reg3);
                os.writeFMUL32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) reg1);
                break;

            case FREM:
                os.writePUSH((GPR) reg3);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeMOV_Const(BITS32, X86Register.ESP, 0, iconst2.getValue());
                os.writeFLD32(X86Register.ESP, 0);
                os.writeFPREM();
                os.writeFSTP32(X86Register.ESP, 0);
                os.writeFFREE(X86Register.ST0);
                os.writePOP((GPR) reg1);
                break;

            case FSUB:
                os.writePUSH(iconst2.getValue());
                os.writeFLD32(X86Register.ESP, 0);
                os.writeMOV(X86Constants.BITS32, X86Register.ESP, 0, (GPR) reg3);
                os.writeFSUB32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) reg1);
                break;

            case LADD:
            case LAND:
            case LDIV:
            case LMUL:
            case LOR:
            case LREM:
            case LSHL:
            case LSHR:
            case LSUB:
            case LUSHR:
            case LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
    }

    public void generateBinaryOP(T reg1, Constant<T> c2,
                                 BinaryOperation operation, int disp3) {
        IntConstant<T> iconst2 = (IntConstant<T>) c2;
        switch (operation) {

            case IADD:
                os.writeMOV_Const((GPR) reg1, iconst2.getValue());
                os.writeADD((GPR) reg1, X86Register.EBP, disp3);
                break;

            case IAND:
                os.writeMOV_Const((GPR) reg1, iconst2.getValue());
                os.writeAND((GPR) reg1, X86Register.EBP, disp3);
                break;

            case IDIV: // not supported
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writeMOV_Const(X86Register.EAX, iconst2.getValue());
                os.writeCDQ(BITS32);
                os.writeIDIV_EAX(BITS32, X86Register.EBP, disp3);
                if (reg1 == X86Register.EAX) {
                    os.writePOP(X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                } else if (reg1 == X86Register.EDX) {
                    os.writeMOV(X86Constants.BITS32, X86Register.EDX, X86Register.EAX);
                    os.writePOP(X86Register.EAX);
                    os.writeADD(X86Register.ESP, 4);
                } else {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EAX);
                    os.writePOP(X86Register.EAX);
                    os.writePOP(X86Register.EDX);
                }
                break;

            case IMUL:
                os.writeIMUL_3((GPR) reg1, X86Register.EBP, disp3, iconst2.getValue());
                break;

            case IOR:
                os.writeMOV_Const((GPR) reg1, iconst2.getValue());
                os.writeOR((GPR) reg1, X86Register.EBP, disp3);
                break;

            case IREM:
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writeMOV_Const(X86Register.EAX, iconst2.getValue());
                os.writeCDQ(BITS32);
                os.writeIDIV_EAX(BITS32, X86Register.EBP, disp3);
                if (reg1 == X86Register.EDX) {
                    os.writePOP(X86Register.EAX);
                    os.writeADD(X86Register.ESP, 4);
                } else if (reg1 == X86Register.EAX) {
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                } else {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EDX);
                    os.writePOP(X86Register.EAX);
                    os.writePOP(X86Register.EDX);
                }
                break;

            case ISHL: // not supported
                os.writeMOV_Const((GPR) reg1, iconst2.getValue());
                os.writePUSH(X86Register.ECX);
                os.writeMOV(X86Constants.BITS32, X86Register.ECX, X86Register.EBP, disp3);
                os.writeSAL_CL((GPR) reg1);
                os.writePOP(X86Register.ECX);
                break;

            case ISHR: // not supported
                os.writeMOV_Const((GPR) reg1, iconst2.getValue());
                os.writePUSH(X86Register.ECX);
                os.writeMOV(X86Constants.BITS32, X86Register.ECX, X86Register.EBP, disp3);
                os.writeSAR_CL((GPR) reg1);
                os.writePOP(X86Register.ECX);
                break;

            case ISUB:
                os.writeMOV_Const((GPR) reg1, iconst2.getValue());
                os.writeSUB((GPR) reg1, X86Register.EBP, disp3);
                break;

            case IUSHR: // not supported
                os.writeMOV_Const((GPR) reg1, iconst2.getValue());
                os.writePUSH(X86Register.ECX);
                os.writeMOV(X86Constants.BITS32, X86Register.ECX, X86Register.EBP, disp3);
                os.writeSHR_CL((GPR) reg1);
                os.writePOP(X86Register.ECX);
                break;

            case IXOR:
                os.writeMOV_Const((GPR) reg1, iconst2.getValue());
                os.writeXOR((GPR) reg1, X86Register.EBP, disp3);
                break;

            case DADD:
            case DDIV:
            case DMUL:
            case DREM:
            case DSUB:
                throw new IllegalArgumentException("Unknown operation: " + operation);

            case FADD:
                os.writePUSH(iconst2.getValue());
                os.writeFLD32(X86Register.ESP, 0);
                os.writeFADD32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) reg1);
                break;

            case FDIV:
                os.writePUSH(iconst2.getValue());
                os.writeFLD32(X86Register.ESP, 0);
                os.writeFDIV32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) reg1);
                break;

            case FMUL:
                os.writePUSH(iconst2.getValue());
                os.writeFLD32(X86Register.ESP, 0);
                os.writeFMUL32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) reg1);
                break;

            case FREM:
                os.writePUSH(iconst2.getValue());
                os.writeFLD32(X86Register.EBP, disp3);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeFPREM();
                os.writeFSTP32(X86Register.ESP, 0);
                os.writeFFREE(X86Register.ST0);
                os.writePOP((GPR) reg1);
                break;

            case FSUB:
                os.writePUSH(iconst2.getValue());
                os.writeFLD32(X86Register.ESP, 0);
                os.writeFSUB32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) reg1);
                break;

            case LADD:
            case LAND:
            case LDIV:
            case LMUL:
            case LOR:
            case LREM:
            case LSHL:
            case LSHR:
            case LSUB:
            case LUSHR:
            case LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
    }

    public void generateBinaryOP(T reg1, T reg2,
                                 BinaryOperation operation, Constant<T> c3) {
        IntConstant<T> iconst3 = (IntConstant<T>) c3;
        switch (operation) {

            case IADD:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, (GPR) reg2);
                }
                os.writeADD((GPR) reg1, iconst3.getValue());
                break;

            case IAND:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, (GPR) reg2);
                }
                os.writeAND((GPR) reg1, iconst3.getValue());
                break;

            case IDIV: // needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writePUSH(iconst3.getValue());
                if (reg2 != X86Register.EAX) {
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, (GPR) reg2);
                }
                os.writeCDQ(BITS32);
                os.writeIDIV_EAX(BITS32, X86Register.ESP, 0);
                os.writePOP(X86Register.EDX);
                if (reg1 == X86Register.EAX) {
                    os.writePOP(X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                } else if (reg1 == X86Register.EDX) {
                    os.writeMOV(X86Constants.BITS32, X86Register.EDX, X86Register.EAX);
                    os.writePOP(X86Register.EAX);
                    os.writeADD(X86Register.ESP, 4);
                } else {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EAX);
                    os.writePOP(X86Register.EAX);
                    os.writePOP(X86Register.EDX);
                }
                break;

            case IMUL:
                os.writeIMUL_3((GPR) reg1, (GPR) reg2, iconst3.getValue());
                break;

            case IOR:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, (GPR) reg2);
                }
                os.writeOR((GPR) reg1, iconst3.getValue());
                break;

            case IREM: // needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writePUSH(iconst3.getValue());
                if (reg2 != X86Register.EAX) {
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, (GPR) reg2);
                }
                os.writeCDQ(BITS32);
                os.writeIDIV_EAX(BITS32, X86Register.ESP, 0);
                os.writePOP(X86Register.EAX);
                if (reg1 == X86Register.EDX) {
                    os.writePOP(X86Register.EAX);
                    os.writeADD(X86Register.ESP, 4);
                } else if (reg1 == X86Register.EAX) {
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                } else {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EDX);
                    os.writePOP(X86Register.EAX);
                    os.writePOP(X86Register.EDX);
                }
                break;

            case ISHL: // needs CL
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, (GPR) reg2);
                }
                os.writeSAL((GPR) reg1, iconst3.getValue());
                break;

            case ISHR: // needs CL
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, (GPR) reg2);
                }
                os.writeSAR((GPR) reg1, iconst3.getValue());
                break;

            case ISUB:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, (GPR) reg2);
                }
                os.writeSUB((GPR) reg1, iconst3.getValue());
                break;

            case IUSHR: // needs CL
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, (GPR) reg2);
                }
                os.writeSHR((GPR) reg1, iconst3.getValue());
                break;

            case IXOR:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, (GPR) reg2);
                }
                os.writeXOR((GPR) reg1, iconst3.getValue());
                break;

            case DADD:
            case DDIV:
            case DMUL:
            case DREM:
            case DSUB:
                throw new IllegalArgumentException("Unknown operation: " + operation);

            case FADD:
                os.writePUSH((GPR) reg2);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeMOV_Const(BITS32, X86Register.ESP, 0, iconst3.getValue());
                os.writeFADD32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) reg1);
                break;

            case FDIV:
                os.writePUSH((GPR) reg2);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeMOV_Const(BITS32, X86Register.ESP, 0, iconst3.getValue());
                os.writeFDIV32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) reg1);
                break;

            case FMUL:
                os.writePUSH((GPR) reg2);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeMOV_Const(BITS32, X86Register.ESP, 0, iconst3.getValue());
                os.writeFMUL32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) reg1);
                break;

            case FREM:
                os.writePUSH(iconst3.getValue());
                os.writeFLD32(X86Register.ESP, 0);
                os.writeMOV(X86Constants.BITS32, X86Register.ESP, 0, (GPR) reg2);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeFPREM();
                os.writeFSTP32(X86Register.ESP, 0);
                os.writeFFREE(X86Register.ST0);
                os.writePOP((GPR) reg1);
                break;

            case FSUB:
                os.writePUSH((GPR) reg2);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeMOV_Const(BITS32, X86Register.ESP, 0, iconst3.getValue());
                os.writeFSUB32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) reg1);
                break;

            case LADD:
            case LAND:
            case LDIV:
            case LMUL:
            case LOR:
            case LREM:
            case LSHL:
            case LSHR:
            case LSUB:
            case LUSHR:
            case LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
    }

    public void generateBinaryOP(T reg1, T reg2,
                                 BinaryOperation operation, T reg3) {

        switch (operation) {

            case IADD:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, (GPR) reg2);
                }
                os.writeADD((GPR) reg1, (GPR) reg3);
                break;

            case IAND:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, (GPR) reg2);
                }
                os.writeAND((GPR) reg1, (GPR) reg3);
                break;

            case IDIV:
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                if (reg2 != X86Register.EAX) {
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, (GPR) reg2);
                }
                os.writeCDQ(BITS32);
                if (reg3 == X86Register.EAX) {
                    os.writeIDIV_EAX(BITS32, X86Register.ESP, 0);
                } else if (reg3 == X86Register.EDX) {
                    os.writeIDIV_EAX(BITS32, X86Register.ESP, 4);
                } else {
                    os.writeIDIV_EAX((GPR) reg3);
                }
                if (reg1 == X86Register.EAX) {
                    os.writePOP(X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                } else if (reg1 == X86Register.EDX) {
                    os.writeMOV(X86Constants.BITS32, X86Register.EDX, X86Register.EAX);
                    os.writePOP(X86Register.EAX);
                    os.writeADD(X86Register.ESP, 4);
                } else {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EAX);
                    os.writePOP(X86Register.EAX);
                    os.writePOP(X86Register.EDX);
                }
                break;

            case IMUL:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, (GPR) reg2);
                }
                os.writeIMUL((GPR) reg1, (GPR) reg3);
                break;

            case IOR:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, (GPR) reg2);
                }
                os.writeOR((GPR) reg1, (GPR) reg3);
                break;

            case IREM: // needs EAX, EDX //TODO verify
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                if (reg2 != X86Register.EAX) {
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, (GPR) reg2);
                }
                os.writeCDQ(BITS32);
                if (reg3 == X86Register.EAX) {
                    os.writeIDIV_EAX(BITS32, X86Register.ESP, 0);
                } else if (reg3 == X86Register.EDX) {
                    os.writeIDIV_EAX(BITS32, X86Register.ESP, 4);
                } else {
                    os.writeIDIV_EAX((GPR) reg3);
                }
                if (reg1 == X86Register.EDX) {
                    os.writePOP(X86Register.EAX);
                    os.writeADD(X86Register.ESP, 4);
                } else if (reg1 == X86Register.EAX) {
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                } else {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EDX);
                    os.writePOP(X86Register.EAX);
                    os.writePOP(X86Register.EDX);
                }
                break;

            case ISHL: // needs CL
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, (GPR) reg2);
                }
                if (reg3 != X86Register.ECX) {
                    os.writePUSH(X86Register.ECX);
                    os.writeMOV(X86Constants.BITS32, X86Register.ECX, (GPR) reg3);
                    os.writeSAL_CL((GPR) reg1);
                    os.writePOP(X86Register.ECX);
                } else {
                    os.writeSAL_CL((GPR) reg1);
                }
                break;

            case ISHR: // needs CL
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, (GPR) reg2);
                }
                if (reg3 != X86Register.ECX) {
                    os.writePUSH(X86Register.ECX);
                    os.writeMOV(X86Constants.BITS32, X86Register.ECX, (GPR) reg3);
                    os.writeSAR_CL((GPR) reg1);
                    os.writePOP(X86Register.ECX);
                } else {
                    os.writeSAL_CL((GPR) reg1);
                }
                break;

            case ISUB:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, (GPR) reg2);
                }
                os.writeSUB((GPR) reg1, (GPR) reg3);
                break;

            case IUSHR: // needs CL
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, (GPR) reg2);
                }
                if (reg3 != X86Register.ECX) {
                    os.writePUSH(X86Register.ECX);
                    os.writeMOV(X86Constants.BITS32, X86Register.ECX, (GPR) reg3);
                    os.writeSHR_CL((GPR) reg1);
                    os.writePOP(X86Register.ECX);
                } else {
                    os.writeSAL_CL((GPR) reg1);
                }
                break;

            case IXOR:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, (GPR) reg2);
                }
                os.writeXOR((GPR) reg1, (GPR) reg3);
                break;

            case DADD:
            case DDIV:
            case DMUL:
            case DREM:
            case DSUB:
                throw new IllegalArgumentException("Unknown operation: " + operation);

            case FADD:
                os.writePUSH((GPR) reg2);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeMOV(X86Constants.BITS32, X86Register.ESP, 0, (GPR) reg3);
                os.writeFADD32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) reg1);
                break;

            case FDIV:
                os.writePUSH((GPR) reg2);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeMOV(X86Constants.BITS32, X86Register.ESP, 0, (GPR) reg3);
                os.writeFDIV32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) reg1);
                break;

            case FMUL:
                os.writePUSH((GPR) reg2);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeMOV(X86Constants.BITS32, X86Register.ESP, 0, (GPR) reg3);
                os.writeFMUL32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) reg1);
                break;

            case FREM:
                os.writePUSH((GPR) reg3);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeMOV(X86Constants.BITS32, X86Register.ESP, 0, (GPR) reg2);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeFPREM();
                os.writeFSTP32(X86Register.ESP, 0);
                os.writeFFREE(X86Register.ST0);
                os.writePOP((GPR) reg1);
                break;

            case FSUB:
                os.writePUSH((GPR) reg2);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeMOV(X86Constants.BITS32, X86Register.ESP, 0, (GPR) reg3);
                os.writeFSUB32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) reg1);
                break;

            case LADD:
            case LAND:
            case LDIV:
            case LMUL:
            case LOR:
            case LREM:
            case LSHL:
            case LSHR:
            case LSUB:
            case LUSHR:
            case LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
    }

    public void generateBinaryOP(T reg1, T reg2,
                                 BinaryOperation operation, int disp3) {
        switch (operation) {

            case IADD:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, (GPR) reg2);
                }
                os.writeADD((GPR) reg1, X86Register.EBP, disp3);
                break;

            case IAND:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, (GPR) reg2);
                }
                os.writeAND((GPR) reg1, X86Register.EBP, disp3);
                break;

            case IDIV: // needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                if (reg2 != X86Register.EAX) {
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, (GPR) reg2);
                }
                os.writeCDQ(BITS32);
                os.writeIDIV_EAX(BITS32, X86Register.EBP, disp3);
                if (reg1 == X86Register.EAX) {
                    os.writePOP(X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                } else if (reg1 == X86Register.EDX) {
                    os.writeMOV(X86Constants.BITS32, X86Register.EDX, X86Register.EAX);
                    os.writePOP(X86Register.EAX);
                    os.writeADD(X86Register.ESP, 4);
                } else {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EAX);
                    os.writePOP(X86Register.EAX);
                    os.writePOP(X86Register.EDX);
                }
                break;

            case IMUL:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, (GPR) reg2);
                }
                os.writeIMUL((GPR) reg1, X86Register.EBP, disp3);
                break;

            case IOR:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, (GPR) reg2);
                }
                os.writeOR((GPR) reg1, X86Register.EBP, disp3);
                break;

            case IREM: // needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                if (reg2 != X86Register.EAX) {
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, (GPR) reg2);
                }
                os.writeCDQ(BITS32);
                os.writeIDIV_EAX(BITS32, X86Register.EBP, disp3);
                if (reg1 == X86Register.EDX) {
                    os.writePOP(X86Register.EAX);
                    os.writeADD(X86Register.ESP, 4);
                } else if (reg1 == X86Register.EAX) {
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                } else {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EDX);
                    os.writePOP(X86Register.EAX);
                    os.writePOP(X86Register.EDX);
                }
                break;

            case ISHL: // needs CL
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, (GPR) reg2);
                }
                os.writePUSH(X86Register.ECX);
                os.writeMOV(X86Constants.BITS32, X86Register.ECX, X86Register.EBP, disp3);
                os.writeSAL_CL((GPR) reg1);
                os.writePOP(X86Register.ECX);
                break;

            case ISHR: // needs CL
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, (GPR) reg2);
                }
                os.writePUSH(X86Register.ECX);
                os.writeMOV(X86Constants.BITS32, X86Register.ECX, X86Register.EBP, disp3);
                os.writeSAR_CL((GPR) reg1);
                os.writePOP(X86Register.ECX);
                break;

            case ISUB:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, (GPR) reg2);
                }
                os.writeSUB((GPR) reg1, X86Register.EBP, disp3);
                break;

            case IUSHR: // needs CL
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, (GPR) reg2);
                }
                os.writePUSH(X86Register.ECX);
                os.writeMOV(X86Constants.BITS32, X86Register.ECX, X86Register.EBP, disp3);
                os.writeSHR_CL((GPR) reg1);
                os.writePOP(X86Register.ECX);
                break;

            case IXOR:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, (GPR) reg2);
                }
                os.writeXOR((GPR) reg1, X86Register.EBP, disp3);
                break;

            case DADD:
            case DDIV:
            case DMUL:
            case DREM:
            case DSUB:
                throw new IllegalArgumentException("Unknown operation: " + operation);

            case FADD:
                os.writePUSH((GPR) reg2);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeFADD32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) reg1);
                break;

            case FDIV:
                os.writePUSH((GPR) reg2);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeFDIV32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) reg1);
                break;

            case FMUL:
                os.writePUSH((GPR) reg2);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeFMUL32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) reg1);
                break;

            case FREM:
                os.writePUSH((GPR) reg2);
                os.writeFLD32(X86Register.EBP, disp3);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeFPREM();
                os.writeFSTP32(X86Register.ESP, 0);
                os.writeFFREE(X86Register.ST0);
                os.writePOP((GPR) reg1);
                break;

            case FSUB:
                os.writePUSH((GPR) reg2);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeFSUB32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) reg1);
                break;

            case LADD:
            case LAND:
            case LDIV:
            case LMUL:
            case LOR:
            case LREM:
            case LSHL:
            case LSHR:
            case LSUB:
            case LUSHR:
            case LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
    }

    public void generateBinaryOP(BinaryQuad<T> quad, T reg1, int disp2,
                                 BinaryOperation operation, Constant<T> c3) {
        switch (operation) {
            case IADD:
                os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP, disp2);
                os.writeADD((GPR) reg1, ((IntConstant<T>) c3).getValue());
                break;

            case IAND:
                os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP, disp2);
                os.writeAND((GPR) reg1, ((IntConstant<T>) c3).getValue());
                break;

            case IDIV: // needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writePUSH(((IntConstant<T>) c3).getValue());
                os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EBP, disp2);
                os.writeCDQ(BITS32);
                os.writeIDIV_EAX(BITS32, X86Register.ESP, 0);
                os.writePOP(X86Register.EDX);
                if (reg1 == X86Register.EAX) {
                    os.writePOP(X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                } else if (reg1 == X86Register.EDX) {
                    os.writeMOV(X86Constants.BITS32, X86Register.EDX, X86Register.EAX);
                    os.writePOP(X86Register.EAX);
                    os.writeADD(X86Register.ESP, 4);
                } else {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EAX);
                    os.writePOP(X86Register.EAX);
                    os.writePOP(X86Register.EDX);
                }
                break;

            case IMUL:
                os.writeIMUL_3((GPR) reg1, X86Register.EBP, disp2, ((IntConstant<T>) c3).getValue());
                break;

            case IOR:
                os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP, disp2);
                os.writeOR((GPR) reg1, ((IntConstant<T>) c3).getValue());
                break;

            case IREM: // needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writePUSH(((IntConstant<T>) c3).getValue());
                os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EBP, disp2);
                os.writeCDQ(BITS32);
                os.writeIDIV_EAX(BITS32, X86Register.ESP, 0);
                os.writePOP(X86Register.EAX);
                if (reg1 == X86Register.EDX) {
                    os.writePOP(X86Register.EAX);
                    os.writeADD(X86Register.ESP, 4);
                } else if (reg1 == X86Register.EAX) {
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                } else {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EDX);
                    os.writePOP(X86Register.EAX);
                    os.writePOP(X86Register.EDX);
                }
                break;

            case ISHL: // needs CL
                os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP, disp2);
                os.writeSAL((GPR) reg1, ((IntConstant<T>) c3).getValue());
                break;

            case ISHR: // needs CL
                os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP, disp2);
                os.writeSAR((GPR) reg1, ((IntConstant<T>) c3).getValue());
                break;

            case ISUB:
                os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP, disp2);
                os.writeSUB((GPR) reg1, ((IntConstant<T>) c3).getValue());
                break;

            case IUSHR: // needs CL
                os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP, disp2);
                os.writeSHR((GPR) reg1, ((IntConstant<T>) c3).getValue());
                break;

            case IXOR:
                os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP, disp2);
                os.writeXOR((GPR) reg1, ((IntConstant<T>) c3).getValue());
                break;

            case DADD:
            case DDIV:
            case DMUL:
            case DREM:
            case DSUB:
                throw new IllegalArgumentException("Unknown operation: " + operation);

            case FADD:
                os.writePUSH(((IntConstant<T>) c3).getValue());
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFADD32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) reg1);
                break;

            case FDIV:
                os.writePUSH(((IntConstant<T>) c3).getValue());
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFDIV32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) reg1);
                break;

            case FMUL:
                os.writePUSH(((IntConstant<T>) c3).getValue());
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFMUL32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) reg1);
                break;

            case FREM:
                os.writePUSH(((IntConstant<T>) c3).getValue());
                os.writeFLD32(X86Register.ESP, 0);
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFPREM();
                os.writeFSTP32(X86Register.ESP, 0);
                os.writeFFREE(X86Register.ST0);
                os.writePOP((GPR) reg1);
                break;

            case FSUB:
                os.writePUSH(((IntConstant<T>) c3).getValue());
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFSUB32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) reg1);
                break;

            case LCMP: {
                final Label curInstrLabel = getInstrLabel(quad.getAddress());
                final Label ltLabel = new Label(curInstrLabel + "lt");
                final Label endLabel = new Label(curInstrLabel + "end");
                GPR gpr1 = (GPR) reg1;

                // Calculate
                if (os.isCode32()) {
                    long value = ((LongConstant<T>) c3).getValue();
                    final int v_lsb = (int) (value & 0xFFFFFFFFL);
                    final int v_msb = (int) ((value >>> 32) & 0xFFFFFFFFL);
                    int disp2lsb = disp2 - stackFrame.getHelper().SLOTSIZE;
                    int disp2msb = disp2;
                    os.writeXOR(gpr1, gpr1);
                    os.writeSUB(BITS32, X86Register.EBP, disp2lsb, v_lsb);
                    os.writeSBB(BITS32, X86Register.EBP, disp2msb, v_msb);
                    os.writeJCC(ltLabel, X86Constants.JL); // JL
                    os.writeMOV(BITS32, SR1, X86Register.EBP, disp2lsb);
                    os.writeOR(SR1, X86Register.EBP, disp2msb);
                }
//                else {
//                    final GPR64 v2r = v2.getRegister(eContext);
//                    final GPR64 v1r = v1.getRegister(eContext);
//                    os.writeCMP(v1r, v2r);
//                    os.writeJCC(ltLabel, X86Constants.JL); // JL
//                }

                os.writeJCC(endLabel, X86Constants.JZ); // value1 == value2
                /** GT */
                os.writeINC(gpr1);
                os.writeJMP(endLabel);
                /** LT */
                os.setObjectRef(ltLabel);
                os.writeDEC(gpr1);
                os.setObjectRef(endLabel);
                break;
            }
            case LADD:
            case LAND:
            case LDIV:
            case LMUL:
            case LOR:
            case LREM:
            case LSHL:
            case LSHR:
            case LSUB:
            case LUSHR:
            case LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
    }

    public void generateBinaryOP(T reg1, int disp2,
                                 BinaryOperation operation, T reg3) {
        switch (operation) {
            case IADD:
                os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP, disp2);
                os.writeADD((GPR) reg1, (GPR) reg3);
                break;

            case IAND:
                os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP, disp2);
                os.writeAND((GPR) reg1, (GPR) reg3);
                break;

            case IDIV: // needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EBP, disp2);
                os.writeCDQ(BITS32);
                if (reg3 == X86Register.EAX) {
                    os.writeIDIV_EAX(BITS32, X86Register.ESP, 0);
                } else if (reg3 == X86Register.EDX) {
                    os.writeIDIV_EAX(BITS32, X86Register.ESP, 4);
                } else {
                    os.writeIDIV_EAX((GPR) reg3);
                }
                if (reg1 == X86Register.EAX) {
                    os.writePOP(X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                } else if (reg1 == X86Register.EDX) {
                    os.writeMOV(X86Constants.BITS32, X86Register.EDX, X86Register.EAX);
                    os.writePOP(X86Register.EAX);
                    os.writeADD(X86Register.ESP, 4);
                } else {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EAX);
                    os.writePOP(X86Register.EAX);
                    os.writePOP(X86Register.EDX);
                }
                break;

            case IMUL:
                os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP, disp2);
                os.writeIMUL((GPR) reg1, (GPR) reg3);
                break;

            case IOR:
                os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP, disp2);
                os.writeOR((GPR) reg1, (GPR) reg3);
                break;

            case IREM: // needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EBP, disp2);
                os.writeCDQ(BITS32);
                if (reg3 == X86Register.EAX) {
                    os.writeIDIV_EAX(BITS32, X86Register.ESP, 0);
                } else if (reg3 == X86Register.EDX) {
                    os.writeIDIV_EAX(BITS32, X86Register.ESP, 4);
                } else {
                    os.writeIDIV_EAX((GPR) reg3);
                }
                if (reg1 == X86Register.EDX) {
                    os.writePOP(X86Register.EAX);
                    os.writeADD(X86Register.ESP, 4);
                } else if (reg1 == X86Register.EAX) {
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                } else {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EDX);
                    os.writePOP(X86Register.EAX);
                    os.writePOP(X86Register.EDX);
                }
                break;

            case ISHL: // needs CL
                os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP, disp2);
                if (reg3 != X86Register.ECX) {
                    os.writePUSH(X86Register.ECX);
                    os.writeMOV(X86Constants.BITS32, X86Register.ECX, (GPR) reg3);
                    os.writeSHR_CL((GPR) reg1);
                    os.writePOP(X86Register.ECX);
                } else {
                    os.writeSAL_CL((GPR) reg1);
                }
                break;

            case ISHR: // needs CL
                os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP, disp2);
                if (reg3 != X86Register.ECX) {
                    os.writePUSH(X86Register.ECX);
                    os.writeMOV(X86Constants.BITS32, X86Register.ECX, (GPR) reg3);
                    os.writeSHR_CL((GPR) reg1);
                    os.writePOP(X86Register.ECX);
                } else {
                    os.writeSAR_CL((GPR) reg1);
                }
                break;

            case ISUB:
                os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP, disp2);
                os.writeSUB((GPR) reg1, (GPR) reg3);
                break;

            case IUSHR: // needs CL
                os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP, disp2);
                if (reg3 != X86Register.ECX) {
                    os.writePUSH(X86Register.ECX);
                    os.writeMOV(X86Constants.BITS32, X86Register.ECX, (GPR) reg3);
                    os.writeSHR_CL((GPR) reg1);
                    os.writePOP(X86Register.ECX);
                } else {
                    os.writeSHR_CL((GPR) reg1);
                }
                break;

            case IXOR:
                os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP, disp2);
                os.writeXOR((GPR) reg1, (GPR) reg3);
                break;

            case DADD:
            case DDIV:
            case DMUL:
            case DREM:
            case DSUB:
                throw new IllegalArgumentException("Unknown operation: " + operation);

            case FADD:
                os.writePUSH((GPR) reg3);
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFADD32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) reg1);
                break;

            case FDIV:
                os.writePUSH((GPR) reg3);
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFDIV32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) reg1);
                break;

            case FMUL:
                os.writePUSH((GPR) reg3);
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFMUL32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) reg1);
                break;

            case FREM:
                os.writePUSH((GPR) reg3);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFPREM();
                os.writeFSTP32(X86Register.ESP, 0);
                os.writeFFREE(X86Register.ST0);
                os.writePOP((GPR) reg1);
                break;

            case FSUB:
                os.writePUSH((GPR) reg3);
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFSUB32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) reg1);
                break;

            case LADD:
            case LAND:
            case LDIV:
            case LMUL:
            case LOR:
            case LREM:
            case LSHL:
            case LSHR:
            case LSUB:
            case LUSHR:
            case LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
    }

    public void generateBinaryOP(T reg1, int disp2,
                                 BinaryOperation operation, int disp3) {
        switch (operation) {
            case IADD:
                os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP, disp2);
                os.writeADD((GPR) reg1, X86Register.EBP, disp3);
                break;

            case IAND:
                os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP, disp2);
                os.writeAND((GPR) reg1, X86Register.EBP, disp3);
                break;

            case IDIV: // needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EBP, disp2);
                os.writeCDQ(BITS32);
                os.writeIDIV_EAX(BITS32, X86Register.EBP, disp3);
                if (reg1 == X86Register.EAX) {
                    os.writePOP(X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                } else if (reg1 == X86Register.EDX) {
                    os.writeMOV(X86Constants.BITS32, X86Register.EDX, X86Register.EAX);
                    os.writePOP(X86Register.EAX);
                    os.writeADD(X86Register.ESP, 4);
                } else {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EAX);
                    os.writePOP(X86Register.EAX);
                    os.writePOP(X86Register.EDX);
                }
                break;

            case IMUL:
                os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP, disp2);
                os.writeIMUL((GPR) reg1, X86Register.EBP, disp3);
                break;

            case IOR: // not supported
                os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP, disp2);
                os.writeOR((GPR) reg1, X86Register.EBP, disp3);
                break;

            case IREM: // needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EBP, disp2);
                os.writeCDQ(BITS32);
                os.writeIDIV_EAX(BITS32, X86Register.EBP, disp3);
                if (reg1 == X86Register.EDX) {
                    os.writePOP(X86Register.EAX);
                    os.writeADD(X86Register.ESP, 4);
                } else if (reg1 == X86Register.EAX) {
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                } else {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EDX);
                    os.writePOP(X86Register.EAX);
                    os.writePOP(X86Register.EDX);
                }
                break;

            case ISHL: // needs CL
                os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP, disp2);
                os.writePUSH(X86Register.ECX);
                os.writeMOV(X86Constants.BITS32, X86Register.ECX, X86Register.EBP, disp3);
                os.writeSAL_CL((GPR) reg1);
                os.writePOP(X86Register.ECX);
                break;

            case ISHR: // needs CL
                os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP, disp2);
                os.writePUSH(X86Register.ECX);
                os.writeMOV(X86Constants.BITS32, X86Register.ECX, X86Register.EBP, disp3);
                os.writeSAR_CL((GPR) reg1);
                os.writePOP(X86Register.ECX);
                break;

            case ISUB:
                os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP, disp2);
                os.writeSUB((GPR) reg1, X86Register.EBP, disp3);
                break;

            case IUSHR: // needs CL
                os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP, disp2);
                os.writePUSH(X86Register.ECX);
                os.writeMOV(X86Constants.BITS32, X86Register.ECX, X86Register.EBP, disp3);
                os.writeSHR_CL((GPR) reg1);
                os.writePOP(X86Register.ECX);
                break;

            case IXOR:
                os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP, disp2);
                os.writeXOR((GPR) reg1, X86Register.EBP, disp3);
                break;

            case DADD:
            case DDIV:
            case DMUL:
            case DREM:
            case DSUB:
                throw new IllegalArgumentException("Unknown operation: " + operation);

            case FADD:
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFADD32(X86Register.EBP, disp3);
                os.writePUSH((GPR) reg1);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) reg1);
                break;

            case FDIV:
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFDIV32(X86Register.EBP, disp3);
                os.writePUSH((GPR) reg1);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) reg1);
                break;

            case FMUL:
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFMUL32(X86Register.EBP, disp3);
                os.writePUSH((GPR) reg1);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) reg1);
                break;

            case FREM:
                os.writeFLD32(X86Register.EBP, disp3);
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFPREM();
                os.writePUSH((GPR) reg1);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) reg1);
                os.writeFFREE(X86Register.ST0);
                break;

            case FSUB:
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFSUB32(X86Register.EBP, disp3);
                os.writePUSH((GPR) reg1);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) reg1);
                break;

            case LADD:
            case LAND:
            case LDIV:
            case LMUL:
            case LOR:
            case LREM:
            case LSHL:
            case LSHR:
            case LSUB:
            case LUSHR:
            case LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
    }

    // / WE should not get to this method
    public void generateBinaryOP(int disp1, Constant<T> c2,
                                 BinaryOperation operation, Constant<T> c3) {
        throw new IllegalArgumentException("Constants should be folded");
    }

    public void generateBinaryOP(int disp1, Constant<T> c2,
                                 BinaryOperation operation, T reg3) {
        IntConstant<T> iconst2 = (IntConstant<T>) c2;
        switch (operation) {
            case IADD:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2.getValue());
                os.writeADD(X86Register.EBP, disp1, (GPR) reg3);
                break;

            case IAND:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2.getValue());
                os.writeAND(X86Register.EBP, disp1, (GPR) reg3);
                break;

            case IDIV: // needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writeMOV_Const(X86Register.EAX, iconst2.getValue());
                os.writeCDQ(BITS32);
                if (reg3 == X86Register.EAX) {
                    os.writeIDIV_EAX(BITS32, X86Register.ESP, 0);
                } else if (reg3 == X86Register.EDX) {
                    os.writeIDIV_EAX(BITS32, X86Register.ESP, 4);
                } else {
                    os.writeIDIV_EAX((GPR) reg3);
                }
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, X86Register.EAX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case IMUL:
                os.writePUSH((GPR) reg3);
                os.writeIMUL_3((GPR) reg3, (GPR) reg3, iconst2.getValue());
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg3);
                os.writePOP((GPR) reg3);
                break;

            case IOR:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2.getValue());
                os.writeOR(X86Register.EBP, disp1, (GPR) reg3);
                break;

            case IREM: // needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writeMOV_Const(X86Register.EAX, iconst2.getValue());
                os.writeCDQ(BITS32);
                if (reg3 == X86Register.EAX) {
                    os.writeIDIV_EAX(BITS32, X86Register.ESP, 0);
                } else if (reg3 == X86Register.EDX) {
                    os.writeIDIV_EAX(BITS32, X86Register.ESP, 4);
                } else {
                    os.writeIDIV_EAX((GPR) reg3);
                }
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, X86Register.EDX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case ISHL: // needs CL
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2.getValue());
                if (reg3 != X86Register.ECX) {
                    os.writePUSH(X86Register.ECX);
                    os.writeMOV(X86Constants.BITS32, X86Register.ECX, (GPR) reg3);
                    os.writeSAL_CL(BITS32, X86Register.EBP, disp1);
                    os.writePOP(X86Register.ECX);
                } else {
                    os.writeSAL_CL(BITS32, X86Register.EBP, disp1);
                }
                break;

            case ISHR: // needs CL
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2.getValue());
                if (reg3 != X86Register.ECX) {
                    os.writePUSH(X86Register.ECX);
                    os.writeMOV(X86Constants.BITS32, X86Register.ECX, (GPR) reg3);
                    os.writeSAR_CL(BITS32, X86Register.EBP, disp1);
                    os.writePOP(X86Register.ECX);
                } else {
                    os.writeSAR_CL(BITS32, X86Register.EBP, disp1);
                }
                break;

            case ISUB:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2.getValue());
                os.writeSUB(X86Register.EBP, disp1, (GPR) reg3);
                break;

            case IUSHR: // needs CL
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2.getValue());
                if (reg3 != X86Register.ECX) {
                    os.writePUSH(X86Register.ECX);
                    os.writeMOV(X86Constants.BITS32, X86Register.ECX, (GPR) reg3);
                    os.writeSHR_CL(BITS32, X86Register.EBP, disp1);
                    os.writePOP(X86Register.ECX);
                } else {
                    os.writeSHR_CL(BITS32, X86Register.EBP, disp1);
                }
                break;

            case IXOR: // not supported
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2.getValue());
                os.writeXOR(X86Register.EBP, disp1, (GPR) reg3);
                break;

            case DADD:
            case DDIV:
            case DMUL:
            case DREM:
            case DSUB:
                throw new IllegalArgumentException("Unknown operation: " + operation);

            case FADD:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2.getValue());
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg3);
                os.writeFADD32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FDIV:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2.getValue());
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg3);
                os.writeFDIV32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FMUL:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2.getValue());
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg3);
                os.writeFMUL32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FREM:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg3);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2.getValue());
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFPREM();
                os.writeFSTP32(X86Register.EBP, disp1);
                os.writeFFREE(X86Register.ST0);
                break;

            case FSUB:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2.getValue());
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg3);
                os.writeFSUB32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case LADD:
            case LAND:
            case LDIV:
            case LMUL:
            case LOR:
            case LREM:
            case LSHL:
            case LSHR:
            case LSUB:
            case LUSHR:
            case LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
    }

    public void generateBinaryOP(int disp1, Constant<T> c2,
                                 BinaryOperation operation, int disp3) {
        IntConstant<T> iconst2 = (IntConstant<T>) c2;
        switch (operation) {
            case IADD:
                os.writePUSH(SR1);
                os.writeMOV_Const(SR1, iconst2.getValue());
                os.writeADD(SR1, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, SR1);
                os.writePOP(SR1);
                break;

            case IAND:
                os.writePUSH(SR1);
                os.writeMOV_Const(SR1, iconst2.getValue());
                os.writeAND(SR1, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, SR1);
                os.writePOP(SR1);
                break;

            case IDIV:
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writeMOV_Const(X86Register.EAX, iconst2.getValue());
                os.writeCDQ(BITS32);
                os.writeIDIV_EAX(BITS32, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, X86Register.EAX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case IMUL:
                os.writePUSH(SR1);
                os.writeIMUL_3(SR1, X86Register.EBP, disp3, iconst2.getValue());
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, SR1);
                os.writePOP(SR1);
                break;

            case IOR:
                os.writePUSH(SR1);
                os.writeMOV_Const(SR1, iconst2.getValue());
                os.writeOR(SR1, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, SR1);
                os.writePOP(SR1);
                break;

            case IREM:
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writeMOV_Const(X86Register.EAX, iconst2.getValue());
                os.writeCDQ(BITS32);
                os.writeIDIV_EAX(BITS32, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, X86Register.EDX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case ISHL:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2.getValue());
                os.writePUSH(X86Register.ECX);
                os.writeMOV(X86Constants.BITS32, X86Register.ECX, X86Register.EBP, disp3);
                os.writeSAL_CL(BITS32, X86Register.EBP, disp1);
                os.writePOP(X86Register.ECX);
                break;

            case ISHR:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2.getValue());
                os.writePUSH(X86Register.ECX);
                os.writeMOV(X86Constants.BITS32, X86Register.ECX, X86Register.EBP, disp3);
                os.writeSAR_CL(BITS32, X86Register.EBP, disp1);
                os.writePOP(X86Register.ECX);
                break;

            case ISUB:
                os.writePUSH(SR1);
                os.writeMOV_Const(SR1, iconst2.getValue());
                os.writeSUB(SR1, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, SR1);
                os.writePOP(SR1);
                break;

            case IUSHR:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2.getValue());
                os.writePUSH(X86Register.ECX);
                os.writeMOV(X86Constants.BITS32, X86Register.ECX, X86Register.EBP, disp3);
                os.writeSHR_CL(BITS32, X86Register.EBP, disp1);
                os.writePOP(X86Register.ECX);
                break;

            case IXOR:
                os.writePUSH(SR1);
                os.writeMOV_Const(SR1, iconst2.getValue());
                os.writeXOR(SR1, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, SR1);
                os.writePOP(SR1);
                break;

            case DADD:
            case DDIV:
            case DMUL:
            case DREM:
            case DSUB:
                throw new IllegalArgumentException("Unknown operation: " + operation);

            case FADD:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2.getValue());
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFADD32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FDIV:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2.getValue());
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFDIV32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FMUL:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2.getValue());
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFMUL32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FREM:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2.getValue());
                os.writeFSUB32(X86Register.EBP, disp3);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFPREM();
                os.writeFSTP32(X86Register.EBP, disp1);
                os.writeFFREE(X86Register.ST0);
                break;

            case FSUB:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2.getValue());
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFSUB32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case LADD:
            case LAND:
            case LDIV:
            case LMUL:
            case LOR:
            case LREM:
            case LSHL:
            case LSHR:
            case LSUB:
            case LUSHR:
            case LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
    }

    public void generateBinaryOP(int disp1, T reg2,
                                 BinaryOperation operation, Constant<T> c3) {
        IntConstant<T> iconst3 = (IntConstant<T>) c3;
        switch (operation) {

            case IADD:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                os.writeADD(BITS32, X86Register.EBP, disp1, iconst3.getValue());
                break;

            case IAND:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                os.writeAND(BITS32, X86Register.EBP, disp1, iconst3.getValue());
                break;

            case IDIV: // needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writePUSH(iconst3.getValue());
                if (reg2 != X86Register.EAX) {
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, (GPR) reg2);
                }
                os.writeCDQ(BITS32);
                os.writeIDIV_EAX(BITS32, X86Register.ESP, 0);
                os.writePOP(X86Register.EDX);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, X86Register.EAX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case IMUL:
                os.writePUSH((GPR) reg2);
                os.writeIMUL_3((GPR) reg2, (GPR) reg2, iconst3.getValue());
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                os.writePOP((GPR) reg2);
                break;

            case IOR:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                os.writeOR(BITS32, X86Register.EBP, disp1, iconst3.getValue());
                break;

            case IREM: // needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writePUSH(iconst3.getValue());
                if (reg2 != X86Register.EAX) {
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, (GPR) reg2);
                }
                os.writeCDQ(BITS32);
                os.writeIDIV_EAX(BITS32, X86Register.ESP, 0);
                os.writePOP(X86Register.EAX);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, X86Register.EDX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case ISHL: // needs CL
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                os.writeSAL(BITS32, X86Register.EBP, disp1, iconst3.getValue());
                break;

            case ISHR: // needs CL
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                os.writeSAR(BITS32, X86Register.EBP, disp1, iconst3.getValue());
                break;

            case ISUB:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                os.writeSUB(BITS32, X86Register.EBP, disp1, iconst3.getValue());
                break;

            case IUSHR: // needs CL
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                os.writeSHR(BITS32, X86Register.EBP, disp1, iconst3.getValue());
                break;

            case IXOR:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                os.writeXOR(BITS32, X86Register.EBP, disp1, iconst3.getValue());
                break;

            case DADD:
            case DDIV:
            case DMUL:
            case DREM:
            case DSUB:
                throw new IllegalArgumentException("Unknown operation: " + operation);

            case FADD:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst3.getValue());
                os.writeFADD32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FDIV:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst3.getValue());
                os.writeFDIV32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FMUL:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst3.getValue());
                os.writeFMUL32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FREM:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst3.getValue());
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFPREM();
                os.writeFSTP32(X86Register.EBP, disp1);
                os.writeFFREE(X86Register.ST0);
                break;

            case FSUB:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst3.getValue());
                os.writeFSUB32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case LADD:
            case LAND:
            case LDIV:
            case LMUL:
            case LOR:
            case LREM:
            case LSHL:
            case LSHR:
            case LSUB:
            case LUSHR:
            case LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
    }

    public void generateBinaryOP(int disp1, T reg2,
                                 BinaryOperation operation, T reg3) {
        switch (operation) {
            case IADD:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                os.writeADD(X86Register.EBP, disp1, (GPR) reg3);
                break;

            case IAND:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                os.writeAND(X86Register.EBP, disp1, (GPR) reg3);
                break;

            case IDIV: // needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                if (reg2 != X86Register.EAX) {
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, (GPR) reg2);
                }
                os.writeCDQ(BITS32);
                if (reg3 == X86Register.EAX) {
                    os.writeIDIV_EAX(BITS32, X86Register.ESP, 0);
                } else if (reg3 == X86Register.EDX) {
                    os.writeIDIV_EAX(BITS32, X86Register.ESP, 4);
                } else {
                    os.writeIDIV_EAX((GPR) reg3);
                }
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, X86Register.EAX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case IMUL:
                os.writePUSH((GPR) reg2);
                os.writeIMUL((GPR) reg2, (GPR) reg3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                os.writePOP((GPR) reg2);
                break;

            case IOR:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                os.writeOR(X86Register.EBP, disp1, (GPR) reg3);
                break;

            case IREM: // needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                if (reg2 != X86Register.EAX) {
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, (GPR) reg2);
                }
                os.writeCDQ(BITS32);
                if (reg3 == X86Register.EAX) {
                    os.writeIDIV_EAX(BITS32, X86Register.ESP, 0);
                } else if (reg3 == X86Register.EDX) {
                    os.writeIDIV_EAX(BITS32, X86Register.ESP, 4);
                } else {
                    os.writeIDIV_EAX((GPR) reg3);
                }
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, X86Register.EDX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case ISHL: // needs CL
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                if (reg3 != X86Register.ECX) {
                    os.writePUSH(X86Register.ECX);
                    os.writeMOV(X86Constants.BITS32, X86Register.ECX, (GPR) reg3);
                    os.writeSAL_CL(BITS32, X86Register.EBP, disp1);
                    os.writePOP(X86Register.ECX);
                } else {
                    os.writeSAL_CL(BITS32, X86Register.EBP, disp1);
                }
                break;

            case ISHR: // needs CL
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                if (reg3 != X86Register.ECX) {
                    os.writePUSH(X86Register.ECX);
                    os.writeMOV(X86Constants.BITS32, X86Register.ECX, (GPR) reg3);
                    os.writeSAR_CL(BITS32, X86Register.EBP, disp1);
                    os.writePOP(X86Register.ECX);
                } else {
                    os.writeSAR_CL(BITS32, X86Register.EBP, disp1);
                }
                break;

            case ISUB:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                os.writeSUB(X86Register.EBP, disp1, (GPR) reg3);
                break;

            case IUSHR: // needs CL
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                if (reg3 != X86Register.ECX) {
                    os.writePUSH(X86Register.ECX);
                    os.writeMOV(X86Constants.BITS32, X86Register.ECX, (GPR) reg3);
                    os.writeSHR_CL(BITS32, X86Register.EBP, disp1);
                    os.writePOP(X86Register.ECX);
                } else {
                    os.writeSHR_CL(BITS32, X86Register.EBP, disp1);
                }
                break;

            case IXOR:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                os.writeXOR(X86Register.EBP, disp1, (GPR) reg3);
                break;

            case DADD:
            case DDIV:
            case DMUL:
            case DREM:
            case DSUB:
                throw new IllegalArgumentException("Unknown operation: " + operation);

            case FADD:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg3);
                os.writeFADD32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FDIV:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg3);
                os.writeFDIV32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FMUL:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg3);
                os.writeFMUL32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FREM:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg3);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFPREM();
                os.writeFSTP32(X86Register.EBP, disp1);
                os.writeFFREE(X86Register.ST0);
                break;

            case FSUB:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg3);
                os.writeFSUB32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case LADD:
            case LAND:
            case LDIV:
            case LMUL:
            case LOR:
            case LREM:
            case LSHL:
            case LSHR:
            case LSUB:
            case LUSHR:
            case LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
    }

    public void generateBinaryOP(int disp1, T reg2, BinaryOperation operation, int disp3) {
        switch (operation) {
            case IADD:
                os.writePUSH((GPR) reg2);
                os.writeADD((GPR) reg2, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                os.writePOP((GPR) reg2);
                break;

            case IAND:
                os.writePUSH((GPR) reg2);
                os.writeAND((GPR) reg2, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                os.writePOP((GPR) reg2);
                break;

            case IDIV: // needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                if (reg2 != X86Register.EAX) {
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, (GPR) reg2);
                }
                os.writeCDQ(BITS32);
                os.writeIDIV_EAX(BITS32, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, X86Register.EAX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case IMUL:
                os.writePUSH((GPR) reg2);
                os.writeIMUL((GPR) reg2, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                os.writePOP((GPR) reg2);
                break;

            case IOR:
                os.writePUSH((GPR) reg2);
                os.writeOR((GPR) reg2, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                os.writePOP((GPR) reg2);
                break;

            case IREM: // needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                if (reg2 != X86Register.EAX) {
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, (GPR) reg2);
                }
                os.writeCDQ(BITS32);
                os.writeIDIV_EAX(BITS32, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, X86Register.EDX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case ISHL: // needs CL
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                os.writePUSH(X86Register.ECX);
                os.writeMOV(X86Constants.BITS32, X86Register.ECX, X86Register.EBP, disp3);
                os.writeSAL_CL(BITS32, X86Register.EBP, disp1);
                os.writePOP(X86Register.ECX);
                break;

            case ISHR: // needs CL
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                os.writePUSH(X86Register.ECX);
                os.writeMOV(X86Constants.BITS32, X86Register.ECX, X86Register.EBP, disp3);
                os.writeSAR_CL(BITS32, X86Register.EBP, disp1);
                os.writePOP(X86Register.ECX);
                break;

            case ISUB:
                os.writePUSH((GPR) reg2);
                os.writeSUB((GPR) reg2, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                os.writePOP((GPR) reg2);
                break;

            case IUSHR: // needs CL
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                os.writePUSH(X86Register.ECX);
                os.writeMOV(X86Constants.BITS32, X86Register.ECX, X86Register.EBP, disp3);
                os.writeSHR_CL(BITS32, X86Register.EBP, disp1);
                os.writePOP(X86Register.ECX);
                break;

            case IXOR:
                os.writePUSH((GPR) reg2);
                os.writeXOR((GPR) reg2, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                os.writePOP((GPR) reg2);
                break;

            case DADD:
            case DDIV:
            case DMUL:
            case DREM:
            case DSUB:
                throw new IllegalArgumentException("Unknown operation: " + operation);

            case FADD:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFADD32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FDIV:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFDIV32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FMUL:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFMUL32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FREM:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                os.writeFLD32(X86Register.EBP, disp3);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFPREM();
                os.writeFSTP32(X86Register.EBP, disp1);
                os.writeFFREE(X86Register.ST0);
                break;

            case FSUB:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg2);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFSUB32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case LADD:
            case LAND:
            case LDIV:
            case LMUL:
            case LOR:
            case LREM:
            case LSHL:
            case LSHR:
            case LSUB:
            case LUSHR:
            case LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
    }

    public void generateBinaryOP(BinaryQuad<T> quad, int disp1, int disp2, BinaryOperation operation, Constant<T> c3) {
        switch (operation) {
            case IADD: // not supported due to the move bellow
                if (disp1 != disp2) {
                    os.writePUSH(X86Register.EBP, disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writeADD(BITS32, X86Register.EBP, disp1, ((IntConstant<T>) c3).getValue());
                break;

            case IAND:
                if (disp1 != disp2) {
                    os.writePUSH(X86Register.EBP, disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writeAND(BITS32, X86Register.EBP, disp1, ((IntConstant<T>) c3).getValue());
                break;

            case IDIV: // needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writePUSH(((IntConstant<T>) c3).getValue());
                os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EBP, disp2);
                os.writeCDQ(BITS32);
                os.writeIDIV_EAX(BITS32, X86Register.ESP, 0);
                os.writePOP(X86Register.EDX);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, X86Register.EAX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case IMUL:
                os.writePUSH(SR1);
                os.writeIMUL_3(SR1, X86Register.EBP, disp2, ((IntConstant<T>) c3).getValue());
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, SR1);
                os.writePOP(SR1);
                break;

            case IOR:
                if (disp1 != disp2) {
                    os.writePUSH(X86Register.EBP, disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writeOR(BITS32, X86Register.EBP, disp1, ((IntConstant<T>) c3).getValue());
                break;

            case IREM: // needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writePUSH(((IntConstant<T>) c3).getValue());
                os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EBP, disp2);
                os.writeCDQ(BITS32);
                os.writeIDIV_EAX(BITS32, X86Register.ESP, 0);
                os.writePOP(X86Register.EAX);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, X86Register.EDX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case ISHL: // needs CL
                if (disp1 != disp2) {
                    os.writePUSH(X86Register.EBP, disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writeSAL(BITS32, X86Register.EBP, disp1, ((IntConstant<T>) c3).getValue());
                break;

            case ISHR: // needs CL
                if (disp1 != disp2) {
                    os.writePUSH(X86Register.EBP, disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writeSAR(BITS32, X86Register.EBP, disp1, ((IntConstant<T>) c3).getValue());
                break;

            case ISUB: // not supported
                if (disp1 != disp2) {
                    os.writePUSH(X86Register.EBP, disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writeSUB(BITS32, X86Register.EBP, disp1, ((IntConstant<T>) c3).getValue());
                break;

            case IUSHR: // needs CL
                if (disp1 != disp2) {
                    os.writePUSH(X86Register.EBP, disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writeSHR(BITS32, X86Register.EBP, disp1, ((IntConstant<T>) c3).getValue());
                break;

            case IXOR: // not supported
                if (disp1 != disp2) {
                    os.writePUSH(X86Register.EBP, disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writeXOR(BITS32, X86Register.EBP, disp1, ((IntConstant<T>) c3).getValue());
                break;

            case DADD:
            case DDIV:
            case DMUL:
            case DREM:
            case DSUB:
                throw new IllegalArgumentException("Unknown operation: " + operation);

            case FADD:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, ((IntConstant<T>) c3).getValue());
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFADD32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FDIV:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, ((IntConstant<T>) c3).getValue());
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFDIV32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FMUL:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, ((IntConstant<T>) c3).getValue());
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFMUL32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FREM:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, ((IntConstant<T>) c3).getValue());
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFPREM();
                os.writeFSTP32(X86Register.EBP, disp1);
                os.writeFFREE(X86Register.ST0);
                break;

            case FSUB:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, ((IntConstant<T>) c3).getValue());
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFSUB32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case LCMP: {
                final Label curInstrLabel = getInstrLabel(quad.getAddress());
                final Label ltLabel = new Label(curInstrLabel + "lt");
                final Label endLabel = new Label(curInstrLabel + "end");

                // Calculate
                if (os.isCode32()) {
                    long value = ((LongConstant<T>) c3).getValue();
                    final int v_lsb = (int) (value & 0xFFFFFFFFL);
                    final int v_msb = (int) ((value >>> 32) & 0xFFFFFFFFL);
                    int disp2lsb = disp2 - stackFrame.getHelper().SLOTSIZE;
                    int disp2msb = disp2;
                    os.writeSUB(BITS32, X86Register.EBP, disp2lsb, v_lsb);
                    os.writeSBB(BITS32, X86Register.EBP, disp2msb, v_msb);
                    os.writeJCC(ltLabel, X86Constants.JL); // JL
                    os.writeMOV(BITS32, SR1, X86Register.EBP, disp2lsb);
                    os.writeOR(SR1, X86Register.EBP, disp2msb);
                }
//                else {
//                    final GPR64 v2r = v2.getRegister(eContext);
//                    final GPR64 v1r = v1.getRegister(eContext);
//                    os.writeCMP(v1r, v2r);
//                    os.writeJCC(ltLabel, X86Constants.JL); // JL
//                }
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, 0);
                os.writeJCC(endLabel, X86Constants.JZ); // value1 == value2
                /** GT */
                os.writeINC(BITS32, X86Register.EBP, disp1);
                os.writeJMP(endLabel);
                /** LT */
                os.setObjectRef(ltLabel);
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, -1);
                os.setObjectRef(endLabel);
                break;
            }
            case LADD:
            case LAND:
            case LDIV:
            case LMUL:
            case LOR:
            case LREM:
            case LSHL:
            case LSHR:
            case LSUB:
            case LUSHR:
            case LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
    }

    public void generateBinaryOP(int disp1, int disp2,
                                 BinaryOperation operation, T reg3) {
        switch (operation) {
            case IADD:
                if (disp1 != disp2) {
                    os.writePUSH(X86Register.EBP, disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writeADD(X86Register.EBP, disp1, (GPR) reg3);
                break;

            case IAND:
                if (disp1 != disp2) {
                    os.writePUSH(X86Register.EBP, disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writeAND(X86Register.EBP, disp1, (GPR) reg3);
                break;

            case IDIV: // needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EBP, disp2);
                os.writeCDQ(BITS32);
                if (reg3 == X86Register.EAX) {
                    os.writeIDIV_EAX(BITS32, X86Register.ESP, 0);
                } else if (reg3 == X86Register.EDX) {
                    os.writeIDIV_EAX(BITS32, X86Register.ESP, 4);
                } else {
                    os.writeIDIV_EAX((GPR) reg3);
                }
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, X86Register.EAX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case IMUL:
                os.writePUSH((GPR) reg3);
                os.writeIMUL((GPR) reg3, X86Register.EBP, disp2);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg3);
                os.writePOP((GPR) reg3);
                break;

            case IOR:
                if (disp1 != disp2) {
                    os.writePUSH(X86Register.EBP, disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writeOR(X86Register.EBP, disp1, (GPR) reg3);
                break;

            case IREM: // needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EBP, disp2);
                os.writeCDQ(BITS32);
                if (reg3 == X86Register.EAX) {
                    os.writeIDIV_EAX(BITS32, X86Register.ESP, 0);
                } else if (reg3 == X86Register.EDX) {
                    os.writeIDIV_EAX(BITS32, X86Register.ESP, 4);
                } else {
                    os.writeIDIV_EAX((GPR) reg3);
                }
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, X86Register.EDX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case ISHL: // needs CL
                if (disp1 != disp2) {
                    os.writePUSH(X86Register.EBP, disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                if (reg3 != X86Register.ECX) {
                    os.writePUSH(X86Register.ECX);
                    os.writeMOV(X86Constants.BITS32, X86Register.ECX, (GPR) reg3);
                    os.writeSAL_CL(BITS32, X86Register.EBP, disp1);
                    os.writePOP(X86Register.ECX);
                } else {
                    os.writeSAL_CL(BITS32, X86Register.EBP, disp1);
                }
                break;

            case ISHR: // needs CL
                if (disp1 != disp2) {
                    os.writePUSH(X86Register.EBP, disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                if (reg3 != X86Register.ECX) {
                    os.writePUSH(X86Register.ECX);
                    os.writeMOV(X86Constants.BITS32, X86Register.ECX, (GPR) reg3);
                    os.writeSAR_CL(BITS32, X86Register.EBP, disp1);
                    os.writePOP(X86Register.ECX);
                } else {
                    os.writeSAR_CL(BITS32, X86Register.EBP, disp1);
                }
                break;

            case ISUB:
                if (disp1 != disp2) {
                    os.writePUSH(X86Register.EBP, disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writeSUB(X86Register.EBP, disp1, (GPR) reg3);
                break;

            case IUSHR: // needs CL
                if (disp1 != disp2) {
                    os.writePUSH(X86Register.EBP, disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                if (reg3 != X86Register.ECX) {
                    os.writePUSH(X86Register.ECX);
                    os.writeMOV(X86Constants.BITS32, X86Register.ECX, (GPR) reg3);
                    os.writeSHR_CL(BITS32, X86Register.EBP, disp1);
                    os.writePOP(X86Register.ECX);
                } else {
                    os.writeSHR_CL(BITS32, X86Register.EBP, disp1);
                }
                break;

            case IXOR:
                if (disp1 != disp2) {
                    os.writePUSH(X86Register.EBP, disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writeXOR(GPR.EBP, disp1, (GPR) reg3);
                break;

            case DADD:
            case DDIV:
            case DMUL:
            case DREM:
            case DSUB:
                throw new IllegalArgumentException("Unknown operation: " + operation);

            case FADD:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg3);
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFADD32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FDIV:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg3);
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFDIV32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FMUL:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg3);
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFMUL32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FREM:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg3);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFPREM();
                os.writeFSTP32(X86Register.EBP, disp1);
                os.writeFFREE(X86Register.ST0);
                break;

            case FSUB:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (GPR) reg3);
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFSUB32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case LADD:
            case LAND:
            case LDIV:
            case LMUL:
            case LOR:
            case LREM:
            case LSHL:
            case LSHR:
            case LSUB:
            case LUSHR:
            case LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
    }

    public void generateBinaryOP(BinaryQuad<T> quad, int disp1, int disp2, BinaryOperation operation, int disp3) {
        switch (operation) {
            case IADD:
                os.writePUSH(SR1);
                os.writeMOV(X86Constants.BITS32, SR1, X86Register.EBP, disp2);
                os.writeADD(SR1, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, SR1);
                os.writePOP(SR1);
                break;

            case IAND:
                os.writePUSH(SR1);
                os.writeMOV(X86Constants.BITS32, SR1, X86Register.EBP, disp2);
                os.writeAND(SR1, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, SR1);
                os.writePOP(SR1);
                break;

            case IDIV:
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EBP, disp2);
                os.writeCDQ(BITS32);
                os.writeIDIV_EAX(BITS32, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, X86Register.EAX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case IMUL:
                os.writePUSH(SR1);
                os.writeMOV(X86Constants.BITS32, SR1, X86Register.EBP, disp2);
                os.writeIMUL(SR1, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, SR1);
                os.writePOP(SR1);
                break;

            case IOR:
                os.writePUSH(SR1);
                os.writeMOV(X86Constants.BITS32, SR1, X86Register.EBP, disp2);
                os.writeOR(SR1, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, SR1);
                os.writePOP(SR1);
                break;

            case IREM:
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EBP, disp2);
                os.writeCDQ(BITS32);
                os.writeIDIV_EAX(BITS32, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, X86Register.EDX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case ISHL:
                if (disp1 != disp2) {
                    os.writePUSH(X86Register.EBP, disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writePUSH(X86Register.ECX);
                os.writeMOV(X86Constants.BITS32, X86Register.ECX, X86Register.EBP, disp3);
                os.writeSAL_CL(BITS32, X86Register.EBP, disp1);
                os.writePOP(X86Register.ECX);
                break;

            case ISHR:
                if (disp1 != disp2) {
                    os.writePUSH(X86Register.EBP, disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writePUSH(X86Register.ECX);
                os.writeMOV(X86Constants.BITS32, X86Register.ECX, X86Register.EBP, disp3);
                os.writeSAR_CL(BITS32, X86Register.EBP, disp1);
                os.writePOP(X86Register.ECX);
                break;

            case ISUB:
                os.writePUSH(SR1);
                os.writeMOV(X86Constants.BITS32, SR1, X86Register.EBP, disp2);
                os.writeSUB(SR1, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, SR1);
                os.writePOP(SR1);
                break;

            case IUSHR:
                if (disp1 != disp2) {
                    os.writePUSH(X86Register.EBP, disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writePUSH(X86Register.ECX);
                os.writeMOV(X86Constants.BITS32, X86Register.ECX, X86Register.EBP, disp3);
                os.writeSHR_CL(BITS32, X86Register.EBP, disp1);
                os.writePOP(X86Register.ECX);
                break;

            case IXOR:
                os.writePUSH(SR1);
                os.writeMOV(X86Constants.BITS32, SR1, X86Register.EBP, disp2);
                os.writeXOR(SR1, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, SR1);
                os.writePOP(SR1);
                break;

            case DADD:
                os.writeFLD64(X86Register.EBP, disp2);
                os.writeFADD64(X86Register.EBP, disp3);
                os.writeFSTP64(X86Register.EBP, disp1);
                break;

            case DDIV:
                os.writeFLD64(X86Register.EBP, disp2);
                os.writeFDIV64(X86Register.EBP, disp3);
                os.writeFSTP64(X86Register.EBP, disp1);
                break;

            case DMUL:
                os.writeFLD64(X86Register.EBP, disp2);
                os.writeFMUL64(X86Register.EBP, disp3);
                os.writeFSTP64(X86Register.EBP, disp1);
                break;

            case DREM:
                os.writeFLD64(X86Register.EBP, disp3);
                os.writeFLD64(X86Register.EBP, disp2);
                os.writeFPREM();
                os.writeFSTP64(X86Register.EBP, disp1);
                os.writeFFREE(X86Register.ST0);
                break;

            case DSUB:
                os.writeFLD64(X86Register.EBP, disp2);
                os.writeFSUB64(X86Register.EBP, disp3);
                os.writeFSTP64(X86Register.EBP, disp1);
                break;

            case FADD:
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFADD32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FDIV:
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFDIV32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FMUL:
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFMUL32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FREM:
                os.writeFLD32(X86Register.EBP, disp3);
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFPREM();
                os.writeFSTP32(X86Register.EBP, disp1);
                os.writeFFREE(X86Register.ST0);
                break;

            case FSUB:
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFSUB32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case LCMP: {
                final Label curInstrLabel = getInstrLabel(quad.getAddress());
                final Label ltLabel = new Label(curInstrLabel + "lt");
                final Label endLabel = new Label(curInstrLabel + "end");

                // Calculate
                if (os.isCode32()) {
                    int disp3lsb = disp3 - stackFrame.getHelper().SLOTSIZE;
                    int disp3msb = disp3;
                    int disp2lsb = disp2 - stackFrame.getHelper().SLOTSIZE;
                    int disp2msb = disp2;
                    os.writeMOV(BITS32, SR1, X86Register.EBP, disp2lsb);
                    os.writeSUB(SR1, X86Register.EBP, disp3lsb);
                    os.writeMOV(BITS32, X86Register.EBP, disp2lsb, SR1);
                    os.writeMOV(BITS32, SR1, X86Register.EBP, disp2msb);
                    os.writeSBB(SR1, X86Register.EBP, disp3msb);
                    os.writeJCC(ltLabel, X86Constants.JL); // JL
                    os.writeOR(X86Register.EBP, disp2lsb, SR1);
                }
//                else {
//                    final GPR64 v2r = v2.getRegister(eContext);
//                    final GPR64 v1r = v1.getRegister(eContext);
//                    os.writeCMP(v1r, v2r);
//                    os.writeJCC(ltLabel, X86Constants.JL); // JL
//                }
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, 0);
                os.writeJCC(endLabel, X86Constants.JZ); // value1 == value2
                /** GT */
                os.writeINC(BITS32, X86Register.EBP, disp1);
                os.writeJMP(endLabel);
                /** LT */
                os.setObjectRef(ltLabel);
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, -1);
                os.setObjectRef(endLabel);
                break;
            }
            case LADD: {
                int disp3lsb = disp3 - stackFrame.getHelper().SLOTSIZE;
                int disp3msb = disp3;
                int disp2lsb = disp2 - stackFrame.getHelper().SLOTSIZE;
                int disp2msb = disp2;
                int disp1lsb = disp2 - stackFrame.getHelper().SLOTSIZE;
                int disp1msb = disp2;
                os.writeMOV(BITS32, SR1, X86Register.EBP, disp2lsb);
                os.writeADD(SR1, X86Register.EBP, disp3lsb);
                os.writeMOV(BITS32, X86Register.EBP, disp1lsb, SR1);
                os.writeMOV(BITS32, SR1, X86Register.EBP, disp2msb);
                os.writeADC(SR1, X86Register.EBP, disp3msb);
                os.writeMOV(BITS32, X86Register.EBP, disp1msb, SR1);
                break;
            }
            case LAND:
            case LDIV:
            case LMUL:
            case LOR:
            case LREM:
            case LSHL:
            case LSHR:
                throw new IllegalArgumentException("Unknown operation: " + operation);
            case LSUB:
                int disp3lsb = disp3 - stackFrame.getHelper().SLOTSIZE;
                int disp3msb = disp3;
                int disp2lsb = disp2 - stackFrame.getHelper().SLOTSIZE;
                int disp2msb = disp2;
                int disp1lsb = disp2 - stackFrame.getHelper().SLOTSIZE;
                int disp1msb = disp2;
                os.writeMOV(BITS32, SR1, X86Register.EBP, disp2lsb);
                os.writeSUB(SR1, X86Register.EBP, disp3lsb);
                os.writeMOV(BITS32, X86Register.EBP, disp1lsb, SR1);
                os.writeMOV(BITS32, SR1, X86Register.EBP, disp2msb);
                os.writeSBB(SR1, X86Register.EBP, disp3msb);
                os.writeMOV(BITS32, X86Register.EBP, disp1msb, SR1);
                break;
            case LUSHR:
            case LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
    }

    /** ******** BRANCHES ************************************** */

    public void generateCodeFor(ConditionalBranchQuad<T> quad, BranchCondition condition, Object reg) {
        checkLabel(quad.getAddress());
        yieldPoint(quad);
        os.writeTEST((GPR) reg, (GPR) reg);
        generateJumpForUnaryCondition(quad, condition);
    }

    public void generateCodeFor(ConditionalBranchQuad<T> quad, BranchCondition condition, int disp) {
        checkLabel(quad.getAddress());
        yieldPoint(quad);
        os.writeCMP_Const(BITS32, X86Register.EBP, disp, 0);
        generateJumpForUnaryCondition(quad, condition);
    }

    private void generateJumpForUnaryCondition(ConditionalBranchQuad<T> quad, BranchCondition condition) {
        switch (condition) {
            case IFEQ:
                os.writeJCC(getInstrLabel(quad.getTargetAddress()), X86Constants.JE);
                break;

            case IFNE:
                os.writeJCC(getInstrLabel(quad.getTargetAddress()), X86Constants.JNE);
                break;

            case IFGT:
                os.writeJCC(getInstrLabel(quad.getTargetAddress()), X86Constants.JG);
                break;

            case IFGE:
                os.writeJCC(getInstrLabel(quad.getTargetAddress()), X86Constants.JGE);
                break;

            case IFLT:
                os.writeJCC(getInstrLabel(quad.getTargetAddress()), X86Constants.JL);
                break;

            case IFLE:
                os.writeJCC(getInstrLabel(quad.getTargetAddress()), X86Constants.JLE);
                break;

            case IFNULL:
                os.writeJCC(getInstrLabel(quad.getTargetAddress()), X86Constants.JE);
                break;

            case IFNONNULL:
                os.writeJCC(getInstrLabel(quad.getTargetAddress()), X86Constants.JNE);
                break;

            default:
                throw new IllegalArgumentException("Unknown condition " + condition);
        }
    }

    public void generateCodeFor(ConditionalBranchQuad<T> quad, BranchCondition condition, Constant<T> cons) {
        checkLabel(quad.getAddress());
        yieldPoint(quad);
        os.writeMOV_Const(SR1, ((IntConstant) cons).getValue());
        os.writeCMP_Const(SR1, 0);
        generateJumpForUnaryCondition(quad, condition);
    }

    public void generateCodeFor(ConditionalBranchQuad<T> quad, Constant<T> c1, BranchCondition condition,
                                Constant<T> c2) {
        checkLabel(quad.getAddress());
        yieldPoint(quad);
        os.writeMOV_Const(SR1, ((IntConstant) c1).getValue());
        os.writeCMP_Const(SR1, ((IntConstant) c2).getValue());
        generateJumpForBinaryCondition(quad, condition);
    }

    public void generateCodeFor(ConditionalBranchQuad<T> quad, Constant<T> c1, BranchCondition condition, int disp2) {
        checkLabel(quad.getAddress());
        yieldPoint(quad);
        os.writeMOV_Const(SR1, ((IntConstant) c1).getValue());
        os.writeCMP(SR1, X86Register.EBP, disp2);
        generateJumpForBinaryCondition(quad, condition);
    }

    public void generateCodeFor(ConditionalBranchQuad<T> quad, Constant<T> c1, BranchCondition condition, Object reg2) {
        checkLabel(quad.getAddress());
        yieldPoint(quad);
        os.writeMOV_Const(SR1, ((IntConstant) c1).getValue());
        os.writeCMP(SR1, (GPR) reg2);
        generateJumpForBinaryCondition(quad, condition);
    }

    public void generateCodeFor(ConditionalBranchQuad<T> quad, int disp1, BranchCondition condition, Constant<T> c2) {
        checkLabel(quad.getAddress());
        os.writeCMP_Const(BITS32, X86Register.EBP, disp1, ((IntConstant<T>) c2).getValue());
        generateJumpForBinaryCondition(quad, condition);
    }

    public void generateCodeFor(ConditionalBranchQuad<T> quad, int disp1, BranchCondition condition, int disp2) {
        checkLabel(quad.getAddress());
        yieldPoint(quad);
        os.writeMOV(X86Constants.BITS32, SR1, X86Register.EBP, disp1);
        os.writeCMP(SR1, X86Register.EBP, disp2);
        generateJumpForBinaryCondition(quad, condition);
    }

    public void generateCodeFor(ConditionalBranchQuad<T> quad, int disp1, BranchCondition condition, Object reg2) {
        checkLabel(quad.getAddress());
        os.writeCMP(X86Register.EBP, disp1, (GPR) reg2);
        generateJumpForBinaryCondition(quad, condition);
    }

    public void generateCodeFor(ConditionalBranchQuad<T> quad, Object reg1, BranchCondition condition, Constant<T> c2) {
        checkLabel(quad.getAddress());
        os.writeCMP_Const((GPR) reg1, ((IntConstant<T>) c2).getValue());
        generateJumpForBinaryCondition(quad, condition);
    }

    public void generateCodeFor(ConditionalBranchQuad<T> quad, Object reg1, BranchCondition condition, int disp2) {
        checkLabel(quad.getAddress());
        yieldPoint(quad);
        os.writeCMP((GPR) reg1, X86Register.EBP, disp2);
        generateJumpForBinaryCondition(quad, condition);
    }

    public void generateCodeFor(ConditionalBranchQuad<T> quad, Object reg1, BranchCondition condition, Object reg2) {
        checkLabel(quad.getAddress());
        yieldPoint(quad);
        os.writeCMP((GPR) reg1, (GPR) reg2);
        generateJumpForBinaryCondition(quad, condition);
    }

    private void generateJumpForBinaryCondition(ConditionalBranchQuad<T> quad, BranchCondition condition) {
        switch (condition) {
            case IF_ICMPEQ:
                os.writeJCC(getInstrLabel(quad.getTargetAddress()), X86Constants.JE);
                break;

            case IF_ICMPNE:
                os.writeJCC(getInstrLabel(quad.getTargetAddress()), X86Constants.JNE);
                break;

            case IF_ICMPGT:
                os.writeJCC(getInstrLabel(quad.getTargetAddress()), X86Constants.JG);
                break;

            case IF_ICMPGE:
                os.writeJCC(getInstrLabel(quad.getTargetAddress()), X86Constants.JGE);
                break;

            case IF_ICMPLT:
                os.writeJCC(getInstrLabel(quad.getTargetAddress()), X86Constants.JL);
                break;

            case IF_ICMPLE:
                os.writeJCC(getInstrLabel(quad.getTargetAddress()), X86Constants.JLE);
                break;

            case IF_ACMPEQ:
                os.writeJCC(getInstrLabel(quad.getTargetAddress()), X86Constants.JE);
                break;

            case IF_ACMPNE:
                os.writeJCC(getInstrLabel(quad.getTargetAddress()), X86Constants.JNE);
                break;

            default:
                throw new IllegalArgumentException("Unknown condition " + condition);
        }
    }

    public void endMethod() {
        stackFrame.emitTrailer(typeSizeInfo, currentMethod.getBytecode().getNoLocals());
    }

    public synchronized void startMethod(VmMethod method) {

//        this.maxLocals = method.getBytecode().getNoLocals();
//        this.loader = method.getDeclaringClass().getLoader();
////        helper.reset();
////        helper.setMethod(method);
//        // this.startOffset = os.getLength();
//
//        this.startOffset = stackFrame.emitHeader();
    }

    private void yieldPoint(ConditionalBranchQuad<T> quad) {
        if (quad.getTargetAddress() < quad.getAddress()) {
            stackFrame.getHelper().writeYieldPoint(getInstrLabel(quad.getAddress()));
        }
    }

    @Override
    public void generateCodeFor(NewPrimitiveArrayAssignQuad<T> quad) {
        // Setup a call to SoftByteCodes.allocArray
        X86CompilerHelper helper = stackFrame.getHelper();
        helper.writePushStaticsEntry(getInstrLabel(quad.getAddress()),
            helper.getMethod().getDeclaringClass()); /* currentClass */
        os.writePUSH(quad.getType()); /* type */
        Operand size = quad.getSize();
        /* count */
        if (size.getAddressingMode() == CONSTANT) {
            os.writePUSH(((IntConstant) size).getValue());
        } else if (size.getAddressingMode() == REGISTER) {
            os.writePUSH((GPR) ((RegisterLocation) ((Variable) size).getLocation()).getRegister());
        } else if (size.getAddressingMode() == STACK) {
            os.writePUSH(X86Register.EBP,
                ((StackLocation) ((Variable) size).getLocation()).getDisplacement());
        } else {
            throw new IllegalArgumentException();
        }

        helper.invokeJavaMethod(stackFrame.getEntryPoints().getAllocPrimitiveArrayMethod());
        Variable lhs = quad.getLHS();
        if (lhs.getAddressingMode() == REGISTER) {
            os.writeMOV(BITS32, (GPR) ((RegisterLocation) lhs.getLocation()).getRegister(), X86Register.EAX);
        } else if (lhs.getAddressingMode() == STACK) {
            os.writeMOV(BITS32, X86Register.EBP,
                ((StackLocation) lhs.getLocation()).getDisplacement(), X86Register.EAX);
        } else {
            throw new IllegalArgumentException();
        }

    }

    @Override
    public void generateCodeFor(NewObjectArrayAssignQuad<T> quad) {
        VmConstClass clazz = quad.getComponentType();
        Label label = getInstrLabel(quad.getAddress());
        writeResolveAndLoadClassToReg(clazz, SR1, label);
        os.writePUSH(SR1);
        Operand sizeOp = quad.getSize();
        if (sizeOp.getAddressingMode() == CONSTANT) {
            os.writePUSH(((IntConstant) sizeOp).getValue());
        } else if (sizeOp.getAddressingMode() == REGISTER) {
            os.writePUSH((GPR) ((RegisterLocation) ((Variable) sizeOp).getLocation()).getRegister());
        } else if (sizeOp.getAddressingMode() == STACK) {
            os.writePUSH(X86Register.EBP, ((StackLocation) ((Variable) sizeOp).getLocation()).getDisplacement());
        } else {
            throw new IllegalArgumentException();
        }
        stackFrame.getHelper().invokeJavaMethod(stackFrame.getEntryPoints().getAnewarrayMethod());
        Variable lhs = quad.getLHS();
        if (lhs.getAddressingMode() == REGISTER) {
            os.writeMOV(BITS32, (GPR) ((RegisterLocation) lhs.getLocation()).getRegister(), X86Register.EAX);
        } else if (lhs.getAddressingMode() == STACK) {
            os.writeMOV(BITS32, X86Register.EBP, ((StackLocation) lhs.getLocation()).getDisplacement(),
                X86Register.EAX);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void generateCodeFor(NewMultiArrayAssignQuad<T> quad) {
        // Create the dimensions array
        Operand[] sizes = quad.getSizes();
        Label label = getInstrLabel(quad.getAddress());
        X86CompilerHelper helper = stackFrame.getHelper();
        helper.writePushStaticsEntry(label, currentMethod.getDeclaringClass()); /* currentClass */
        os.writePUSH(10); /* type=int */
        os.writePUSH(sizes.length); /* elements */
        helper.invokeJavaMethod(stackFrame.getEntryPoints().getAllocPrimitiveArrayMethod());
        final GPR dimsr = SR1;
        if (SR1 != X86Register.EAX) {
            os.writeMOV(BITS32, SR1, X86Register.EAX);
        }
        // Dimension array is now in dimsr
        // Pop all dimensions (note the reverse order that allocMultiArray
        // expects)
        final int slotSize = stackFrame.getHelper().SLOTSIZE;
        int arrayDataOffset = VmArray.DATA_OFFSET * slotSize;
        for (int i = 0; i < sizes.length; i++) {
            final int ofs = arrayDataOffset + (i * 4);
            Operand size = sizes[i];
            if (size.getAddressingMode() == CONSTANT) {
                os.writeMOV_Const(BITS32, dimsr, ofs, ((IntConstant) size).getValue());
            } else if (size.getAddressingMode() == REGISTER) {
                os.writeMOV(BITS32, dimsr, ofs,
                    (GPR) ((RegisterLocation) ((Variable) size).getLocation()).getRegister());
            } else if (size.getAddressingMode() == STACK) {
                GPR sr2 = SR1 == X86Register.EAX ? X86Register.EBX : X86Register.EAX;
                os.writePUSH(sr2);
                os.writeMOV(BITS32, sr2, X86Register.EBP,
                    ((StackLocation) ((Variable) size).getLocation()).getDisplacement());
                os.writeMOV(BITS32, dimsr, ofs, sr2);
                os.writePOP(sr2);
            } else {
                throw new IllegalArgumentException();
            }
        }
        os.writePUSH(dimsr);
        VmConstClass clazz = quad.getComponentType();
        // Resolve the array class
        writeResolveAndLoadClassToReg(clazz, dimsr, label);
        // Now call the multianewarrayhelper
        os.writeXCHG(X86Register.ESP, 0, dimsr);
        os.writePUSH(dimsr); // dimensions[]
        helper.invokeJavaMethod(stackFrame.getEntryPoints().getAllocMultiArrayMethod());
        Variable lhs = quad.getLHS();
        if (lhs.getAddressingMode() == REGISTER) {
            os.writeMOV(BITS32, (GPR) ((RegisterLocation) lhs.getLocation()).getRegister(), X86Register.EAX);
        } else if (lhs.getAddressingMode() == STACK) {
            os.writeMOV(BITS32, X86Register.EBP, ((StackLocation) lhs.getLocation()).getDisplacement(),
                X86Register.EAX);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void generateCodeFor(ArrayLengthAssignQuad quad) {
        Variable lhs = quad.getLHS();
        final int slotSize = stackFrame.getHelper().SLOTSIZE;
        int arrayLengthOffset = VmArray.LENGTH_OFFSET * slotSize;
        if (lhs.getAddressingMode() == REGISTER) {
            GPR dstReg = (GPR) ((RegisterLocation) lhs.getLocation()).getRegister();
            Variable ref = quad.getRef();
            if (ref.getAddressingMode() == REGISTER) {
                os.writeMOV(INTSIZE, dstReg, (GPR) ((RegisterLocation) ref.getLocation()).getRegister(),
                    arrayLengthOffset);
            } else if (ref.getAddressingMode() == STACK) {
                os.writeMOV(BITS32, SR1, X86Register.EBP, ((StackLocation) ref.getLocation()).getDisplacement());
                os.writeMOV(INTSIZE, dstReg, SR1, arrayLengthOffset);
            } else {
                throw new IllegalArgumentException();
            }
        } else if (lhs.getAddressingMode() == STACK) {
            Variable ref = quad.getRef();
            if (ref.getAddressingMode() == REGISTER) {
                os.writeMOV(INTSIZE, SR1, (GPR) ((RegisterLocation) ref.getLocation()).getRegister(),
                    arrayLengthOffset);
            } else if (ref.getAddressingMode() == STACK) {
                GPR sr2 = SR1 == X86Register.EAX ? X86Register.EBX : X86Register.EAX;
                os.writeMOV(BITS32, sr2, X86Register.EBP, ((StackLocation) ref.getLocation()).getDisplacement());
                os.writeMOV(INTSIZE, SR1, sr2, arrayLengthOffset);
            } else {
                throw new IllegalArgumentException();
            }
            os.writeMOV(BITS32, X86Register.EBP, ((StackLocation) lhs.getLocation()).getDisplacement(), SR1);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void generateCodeFor(ArrayAssignQuad quad) {
        checkLabel(quad.getAddress());

        Variable lhs = quad.getLHS();
        Variable ref = quad.getRef();
        Operand ind = quad.getInd();

        checkBounds(ref, ind, quad.getAddress());

        final int slotSize = stackFrame.getHelper().SLOTSIZE;
        int arrayDataOffset = VmArray.DATA_OFFSET * slotSize;

        // Load data
//        if (idx.isConstant()) {
//            final int offset = idx.getValue() * scale;
//            os.writeMOV(valSize, resultr, refr, offset + arrayDataOffset);
//        } else {
        int scale = 4;
        if (quad.getInd().getAddressingMode() == CONSTANT) {
            IntConstant indr = (IntConstant) ind;
            GPR resultr = (GPR) ((RegisterLocation) lhs.getLocation()).getRegister();
//            if (os.isCode64()) {
//                final GPR64 idxr64 = (GPR64) eContext.getGPRPool().getRegisterInSameGroup(idxr, JvmType.LONG);
//                os.writeMOVSXD(idxr64, (GPR32) idxr);
//                idxr = idxr64;
//            }

            if (ref.getAddressingMode() == REGISTER) {
                GPR refr = (GPR) ((RegisterLocation) ref.getLocation()).getRegister();
                os.writeMOV(BITS32, resultr, refr, indr.getValue() * scale + arrayDataOffset);
            } else if (ref.getAddressingMode() == STACK) {
                os.writeMOV(BITS32, SR1, X86Register.EBP, ((StackLocation) ref.getLocation()).getDisplacement());
                os.writeMOV(BITS32, resultr, SR1, indr.getValue() * scale + arrayDataOffset);
            } else {
                throw new IllegalArgumentException();
            }
        } else if (quad.getInd().getAddressingMode() == REGISTER) {
            GPR indr = (GPR) ((RegisterLocation) ((Variable) ind).getLocation()).getRegister();
            if (lhs.getAddressingMode() == REGISTER) {
                GPR resultr = (GPR) ((RegisterLocation) lhs.getLocation()).getRegister();
                if (ref.getAddressingMode() == REGISTER) {
                    GPR refr = (GPR) ((RegisterLocation) ref.getLocation()).getRegister();
                    os.writeMOV(BITS32, resultr, refr, indr, scale,  arrayDataOffset);
                } else if (ref.getAddressingMode() == STACK) {
                    os.writeMOV(BITS32, SR1, X86Register.EBP, ((StackLocation) ref.getLocation()).getDisplacement());
                    os.writeMOV(BITS32, resultr, SR1, indr, scale,  arrayDataOffset);
                } else {
                    throw new IllegalArgumentException();
                }
            } else if (lhs.getAddressingMode() == STACK) {
                int rdisp = ((StackLocation) lhs.getLocation()).getDisplacement();
                if (ref.getAddressingMode() == REGISTER) {
                    GPR refr = (GPR) ((RegisterLocation) ref.getLocation()).getRegister();
                    os.writeMOV(BITS32, SR1, refr, indr, scale,  arrayDataOffset);
                    os.writeMOV(BITS32, X86Register.EBP, rdisp, SR1);
                } else if (ref.getAddressingMode() == STACK) {
                    os.writeMOV(BITS32, SR1, X86Register.EBP, ((StackLocation) ref.getLocation()).getDisplacement());
                    os.writePUSH(SR1, indr, scale, arrayDataOffset);
                    os.writePOP(X86Register.EBP, rdisp);
                } else {
                    throw new IllegalArgumentException();
                }
            } else {
                throw new IllegalArgumentException();
            }
//            if (os.isCode64()) {
//                final GPR64 idxr64 = (GPR64) eContext.getGPRPool().getRegisterInSameGroup(idxr, JvmType.LONG);
//                os.writeMOVSXD(idxr64, (GPR32) idxr);
//                idxr = idxr64;
//            }

            //os.writeMOV(BITS32, resultr, refr, indr, scale, arrayDataOffset);
        } else if (quad.getInd().getAddressingMode() == STACK) {
            int indDisp = ((StackLocation) ((Variable) ind).getLocation()).getDisplacement();
            if (lhs.getAddressingMode() == REGISTER) {
                GPR resultr = (GPR) ((RegisterLocation) lhs.getLocation()).getRegister();
                if (ref.getAddressingMode() == REGISTER) {
                    os.writeMOV(BITS32, SR1, X86Register.EBP, indDisp);
                    GPR refr = (GPR) ((RegisterLocation) ref.getLocation()).getRegister();
                    os.writeMOV(BITS32, resultr, refr, SR1, scale,  arrayDataOffset);
                } else if (ref.getAddressingMode() == STACK) {
                    os.writeMOV(BITS32, SR1, X86Register.EBP, ((StackLocation) ref.getLocation()).getDisplacement());
                    GPR sr2 = (resultr == X86Register.EDX) ? X86Register.EBX : X86Register.EDX;
                    os.writePUSH(sr2);
                    os.writeMOV(BITS32, sr2, X86Register.EBP, indDisp);
                    os.writeMOV(BITS32, resultr, SR1, sr2, scale,  arrayDataOffset);
                    os.writePOP(sr2);
                } else {
                    throw new IllegalArgumentException();
                }
            } else if (lhs.getAddressingMode() == STACK) {
                int rdisp = ((StackLocation) lhs.getLocation()).getDisplacement();
                if (ref.getAddressingMode() == REGISTER) {
                    os.writeMOV(BITS32, SR1, X86Register.EBP, indDisp);
                    GPR refr = (GPR) ((RegisterLocation) ref.getLocation()).getRegister();
                    os.writePUSH(refr, SR1, scale,  arrayDataOffset);
                    os.writePOP(X86Register.EBP, rdisp);
                } else if (ref.getAddressingMode() == STACK) {
                    os.writeMOV(BITS32, SR1, X86Register.EBP, ((StackLocation) ref.getLocation()).getDisplacement());
                    GPR sr2 = (SR1 == X86Register.EDX) ? X86Register.EBX : X86Register.EDX;
                    os.writePUSH(sr2);
                    os.writeMOV(BITS32, sr2, X86Register.EBP, indDisp);
                    os.writePUSH(SR1, sr2, scale,  arrayDataOffset);
                    os.writePOP(X86Register.EBP, rdisp);
                    os.writePOP(sr2);
                } else {
                    throw new IllegalArgumentException();
                }
            } else {
                throw new IllegalArgumentException();
            }

//            if (os.isCode64()) {
//                final GPR64 idxr64 = (GPR64) eContext.getGPRPool().getRegisterInSameGroup(idxr, JvmType.LONG);
//                os.writeMOVSXD(idxr64, (GPR32) idxr);
//                idxr = idxr64;
//            }

            //os.writeMOV(BITS32, resultr, refr, indr, scale, arrayDataOffset);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void generateCodeFor(ArrayStoreQuad quad) {
        checkLabel(quad.getAddress());

        Variable ref = quad.getRef();
        Operand ind = quad.getInd();
        Operand rhs = quad.getRHS();

        checkBounds(ref, ind, quad.getAddress());

        final int slotSize = stackFrame.getHelper().SLOTSIZE;
        int arrayDataOffset = VmArray.DATA_OFFSET * slotSize;

        // Load data
//        if (idx.isConstant()) {
//            final int offset = idx.getValue() * scale;
//            os.writeMOV(valSize, resultr, refr, offset + arrayDataOffset);
//        } else {
        int scale = 4;

        // Verify
        //todo spec issue: add type compatibility check (elemType <- valueType), throw ArrayStoreException


        if (ref.getAddressingMode() == REGISTER) {
            GPR dstReg = (GPR) ((RegisterLocation) ref.getLocation()).getRegister();
            if (ind.getAddressingMode() == CONSTANT) {
                final int offset = ((IntConstant) ind).getValue() * scale;
                if (rhs.getAddressingMode() == CONSTANT) {
                    os.writeMOV_Const(BITS32, dstReg, offset + arrayDataOffset, ((IntConstant) rhs).getValue());
                } else if (rhs.getAddressingMode() == REGISTER) {
                    os.writeMOV(BITS32, dstReg, offset + arrayDataOffset,
                        (GPR) ((RegisterLocation) ((Variable) rhs).getLocation()).getRegister());
                } else if (rhs.getAddressingMode() == STACK) {
                    os.writeMOV(X86Constants.BITS32, SR1, X86Register.EBP,
                        ((StackLocation) ((Variable) rhs).getLocation()).getDisplacement());
                    os.writeMOV(BITS32, dstReg, offset + arrayDataOffset, SR1);
                } else {
                    throw new IllegalArgumentException();
                }
            } else if (ind.getAddressingMode() == REGISTER) {
                GPR idxReg = (GPR) ((RegisterLocation) ((Variable) ind).getLocation()).getRegister();
                if (rhs.getAddressingMode() == CONSTANT) {
                    os.writeMOV_Const(BITS32, dstReg, idxReg, scale, arrayDataOffset, ((IntConstant) rhs).getValue());
                } else if (rhs.getAddressingMode() == REGISTER) {
                    os.writeMOV(BITS32, dstReg, idxReg, scale, arrayDataOffset,
                        (GPR) ((RegisterLocation) ((Variable) rhs).getLocation()).getRegister());
                } else if (rhs.getAddressingMode() == STACK) {
                    os.writeMOV(X86Constants.BITS32, SR1, X86Register.EBP,
                        ((StackLocation) ((Variable) rhs).getLocation()).getDisplacement());
                    os.writeMOV(BITS32, dstReg, idxReg, scale, arrayDataOffset, SR1);
                } else {
                    throw new IllegalArgumentException();
                }
            } else if (ind.getAddressingMode() == STACK) {
                os.writeMOV(X86Constants.BITS32, SR1, X86Register.EBP,
                    ((StackLocation) ((Variable) ind).getLocation()).getDisplacement());
                if (rhs.getAddressingMode() == CONSTANT) {
                    os.writeMOV_Const(BITS32, dstReg, SR1, scale, arrayDataOffset, ((IntConstant) rhs).getValue());
                } else if (rhs.getAddressingMode() == REGISTER) {
                    os.writeMOV(BITS32, dstReg, SR1, scale, arrayDataOffset,
                        (GPR) ((RegisterLocation) ((Variable) rhs).getLocation()).getRegister());
                } else if (rhs.getAddressingMode() == STACK) {
                    GPR sr2 = SR1 == X86Register.EAX ? X86Register.EBX : X86Register.EAX;
                    os.writePUSH(sr2);
                    os.writeMOV(BITS32, sr2, X86Register.EBP,
                        ((StackLocation) ((Variable) rhs).getLocation()).getDisplacement());
                    os.writeMOV(BITS32, dstReg, SR1, scale, arrayDataOffset, sr2);
                    os.writePOP(sr2);
                } else {
                    throw new IllegalArgumentException();
                }
            } else {
                throw new IllegalArgumentException();
            }
        } else if (ref.getAddressingMode() == STACK) {
            os.writeMOV(BITS32, SR1, X86Register.EBP, ((StackLocation) ref.getLocation()).getDisplacement());
            if (ind.getAddressingMode() == CONSTANT) {
                final int offset = ((IntConstant) ind).getValue() * scale;
                if (rhs.getAddressingMode() == CONSTANT) {
                    os.writeMOV_Const(BITS32, SR1, offset + arrayDataOffset, ((IntConstant) rhs).getValue());
                } else if (rhs.getAddressingMode() == REGISTER) {
                    os.writeMOV(BITS32, SR1, offset + arrayDataOffset,
                        (GPR) ((RegisterLocation) ((Variable) rhs).getLocation()).getRegister());
                } else if (rhs.getAddressingMode() == STACK) {
                    GPR sr2 = SR1 == X86Register.EAX ? X86Register.EBX : X86Register.EAX;
                    os.writePUSH(sr2);
                    os.writeMOV(BITS32, sr2, X86Register.EBP,
                        ((StackLocation) ((Variable) rhs).getLocation()).getDisplacement());
                    os.writeMOV(BITS32, SR1, offset + arrayDataOffset, sr2);
                    os.writePOP(sr2);
                } else {
                    throw new IllegalArgumentException();
                }
            } else if (ind.getAddressingMode() == REGISTER) {
                GPR idxReg = (GPR) ((RegisterLocation) ((Variable) ind).getLocation()).getRegister();
                if (rhs.getAddressingMode() == CONSTANT) {
                    os.writeMOV_Const(BITS32, SR1, idxReg, scale, arrayDataOffset, ((IntConstant) rhs).getValue());
                } else if (rhs.getAddressingMode() == REGISTER) {
                    os.writeMOV(BITS32, SR1, idxReg, scale, arrayDataOffset,
                        (GPR) ((RegisterLocation) ((Variable) rhs).getLocation()).getRegister());
                } else if (rhs.getAddressingMode() == STACK) {
                    GPR sr2 = SR1 == X86Register.EAX ? X86Register.EBX : X86Register.EAX;
                    os.writePUSH(sr2);
                    os.writeMOV(BITS32, sr2, X86Register.EBP,
                        ((StackLocation) ((Variable) rhs).getLocation()).getDisplacement());
                    os.writeMOV(BITS32, SR1, idxReg, scale, arrayDataOffset, sr2);
                    os.writePOP(sr2);
                } else {
                    throw new IllegalArgumentException();
                }
            } else if (ind.getAddressingMode() == STACK) {
                GPR sr2 = SR1 == X86Register.EAX ? X86Register.EBX : X86Register.EAX;
                os.writePUSH(sr2);
                os.writeMOV(BITS32, sr2, X86Register.EBP,
                    ((StackLocation) ((Variable) ind).getLocation()).getDisplacement());
                if (rhs.getAddressingMode() == CONSTANT) {
                    os.writeMOV_Const(BITS32, SR1, sr2, scale, arrayDataOffset, ((IntConstant) rhs).getValue());
                } else if (rhs.getAddressingMode() == REGISTER) {
                    os.writeMOV(BITS32, SR1, sr2, scale, arrayDataOffset,
                        (GPR) ((RegisterLocation) ((Variable) rhs).getLocation()).getRegister());
                } else if (rhs.getAddressingMode() == STACK) {
                    GPR sr3;
                    if (SR1 != X86Register.ECX) {
                        if (sr2 != X86Register.ECX) {
                            sr3 = X86Register.ECX;
                        } else if (SR1 != X86Register.EDX) {
                            sr3 = X86Register.EDX;
                        } else {
                            sr3 = X86Register.EAX;
                        }
                    } else {
                        sr3 = sr2 == X86Register.EDX ? X86Register.EBX : X86Register.EDX;
                    }
                    os.writePUSH(sr3);
                    os.writeMOV(BITS32, sr3, X86Register.EBP,
                        ((StackLocation) ((Variable) rhs).getLocation()).getDisplacement());
                    os.writeMOV(BITS32, SR1, sr2, scale, arrayDataOffset, sr3);
                    os.writePOP(sr3);
                } else {
                    throw new IllegalArgumentException();
                }
                os.writePOP(sr2);
            } else {
                throw new IllegalArgumentException();
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    private final void checkBounds(Variable ref, Operand index, int address) {
//        counters.getCounter("checkbounds").inc();
        final Label curInstrLabel = getInstrLabel(address);
        final Label test = new Label(curInstrLabel + "$$cbtest");
        final Label failed = new Label(curInstrLabel + "$$cbfailed");

//        assertCondition(ref.isGPR(), "ref must be in a register");
//        final GPR refr = (GPR) ((RegisterLocation) ref.getLocation()).getRegister();

        os.writeJMP(test);
        os.setObjectRef(failed);
        // Call SoftByteCodes.throwArrayOutOfBounds
        if (ref.getAddressingMode() == REGISTER) {
            os.writePUSH((GPR) ((RegisterLocation) ref.getLocation()).getRegister());
        } else if (ref.getAddressingMode() == STACK) {
            os.writePUSH(X86Register.EBP, ((StackLocation) ref.getLocation()).getDisplacement());
        } else {
            throw new IllegalArgumentException();
        }
//        if (index.isConstant()) {
//            os.writePUSH(index.getValue());
//        } else {
        if (index.getAddressingMode() == CONSTANT) {
            os.writePUSH(((IntConstant) index).getValue());
        } else if (index.getAddressingMode() == REGISTER) {
            os.writePUSH((GPR) ((RegisterLocation) ((Variable) index).getLocation()).getRegister());
        } else if (index.getAddressingMode() == STACK) {
            os.writePUSH(X86Register.EBP, ((StackLocation) ((Variable) index).getLocation()).getDisplacement());
        } else {
            throw new IllegalArgumentException();
        }
//        }
//        invokeJavaMethod(context.getThrowArrayOutOfBounds());
        stackFrame.getHelper().invokeJavaMethod(stackFrame.getEntryPoints().getThrowArrayOutOfBounds());

        final int slotSize = stackFrame.getHelper().SLOTSIZE;
        int arrayLengthOffset = VmArray.LENGTH_OFFSET * slotSize;

        // CMP length, index
        os.setObjectRef(test);
//        if (index.isConstant()) {
//            os
//                .writeCMP_Const(BITS32, refr, arrayLengthOffset, index
//                    .getValue());
//        } else {
        if (index.getAddressingMode() == CONSTANT) {
            if (ref.getAddressingMode() == REGISTER) {
                os.writeCMP_Const(X86Constants.BITS32, (GPR) ((RegisterLocation) ref.getLocation()).getRegister(),
                    arrayLengthOffset, ((IntConstant) index).getValue());
            } else if (ref.getAddressingMode() == STACK) {
                os.writeMOV(X86Constants.BITS32, SR1, X86Register.EBP,
                    ((StackLocation) ref.getLocation()).getDisplacement());
                os.writeCMP_Const(X86Constants.BITS32, SR1, arrayLengthOffset, ((IntConstant) index).getValue());
            } else {
                throw new IllegalArgumentException();
            }
        } else if (index.getAddressingMode() == REGISTER) {
            if (ref.getAddressingMode() == REGISTER) {
                os.writeCMP((GPR) ((RegisterLocation) ref.getLocation()).getRegister(),
                    arrayLengthOffset, (GPR) ((RegisterLocation) ((Variable) index).getLocation()).getRegister());
            } else if (ref.getAddressingMode() == STACK) {
                os.writeMOV(X86Constants.BITS32, SR1, X86Register.EBP,
                    ((StackLocation) ref.getLocation()).getDisplacement());
                os.writeCMP(SR1, arrayLengthOffset,
                    (GPR) ((RegisterLocation) ((Variable) index).getLocation()).getRegister());

            } else {
                throw new IllegalArgumentException();
            }
        } else if (index.getAddressingMode() == STACK) {
            if (ref.getAddressingMode() == REGISTER) {
                os.writeMOV(X86Constants.BITS32, SR1, X86Register.EBP,
                    ((StackLocation) ((Variable) index).getLocation()).getDisplacement());
                os.writeCMP((GPR) ((RegisterLocation) ref.getLocation()).getRegister(), arrayLengthOffset, SR1);
            } else if (ref.getAddressingMode() == STACK) {
                os.writeMOV(X86Constants.BITS32, SR1, X86Register.EBP,
                    ((StackLocation) ref.getLocation()).getDisplacement());
                os.writeADD(SR1, arrayLengthOffset);
                os.writeCMP(SR1, X86Register.EBP, ((StackLocation) ((Variable) index).getLocation()).getDisplacement());
            } else {
                throw new IllegalArgumentException();
            }
        } else {
            throw new IllegalArgumentException();
        }

//        }
        os.writeJCC(failed, X86Constants.JNA);
    }

    @Override
    public void generateCodeFor(ConstantClassAssignQuad<T> quad) {
        VmConstClass clazz = quad.getConstClass();
        // Resolve the class
        Label label = getInstrLabel(quad.getAddress());
        writeResolveAndLoadClassToReg(clazz, SR1, label);
        // Call SoftByteCodes#getClassForVmType
        os.writePUSH(SR1);
        stackFrame.getHelper().invokeJavaMethod(stackFrame.getEntryPoints().getGetClassForVmTypeMethod());
        Variable lhs = quad.getLHS();
        if (lhs.getAddressingMode() == REGISTER) {
            os.writeMOV(BITS32, (GPR) ((RegisterLocation) lhs.getLocation()).getRegister(), X86Register.EAX);
        } else if (lhs.getAddressingMode() == STACK) {
            os.writeMOV(BITS32, X86Register.EBP, ((StackLocation) lhs.getLocation()).getDisplacement(),
                X86Register.EAX);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void generateCodeFor(ConstantStringAssignQuad<T> quad) {
        //todo
//        throw new UnsupportedOperationException();
    }

    @Override
    public void generateCodeFor(CheckcastQuad<T> quad) {
        //todo
//        throw new UnsupportedOperationException();
    }

    @Override
    public void generateCodeFor(InstanceofAssignQuad<T> quad) {
        VmConstClass clazz = quad.getConstClass();
        Operand ref = quad.getRef();
        Variable lhs = quad.getLHS();
        Label currentLabel = getInstrLabel(quad.getAddress());
        // Resolve the classRef
        clazz.resolve(currentMethod.getDeclaringClass().getLoader());

        // Prepare
//        final X86RegisterPool pool = eContext.getGPRPool();
        final VmType<?> resolvedType = clazz.getResolvedVmClass();

        if (resolvedType.isInterface() || resolvedType.isArray()) {
            if (ref.getAddressingMode() == REGISTER) {
                //todo
                throw new IllegalArgumentException();
            } else if (ref.getAddressingMode() == STACK) {
                //todo
                throw new IllegalArgumentException();
            } else {
                throw new IllegalArgumentException();
            }

//            // It is an interface, do it the hard way
//
//            // Load reference
//            final RefItem ref = vstack.popRef();
//            ref.load(eContext);
//            final GPR refr = ref.getRegister();
//
//            // Allocate tmp registers
//            final GPR classr = (GPR) L1AHelper.requestRegister(eContext,
//                JvmType.REFERENCE, false);
//            final GPR cntr = (GPR) L1AHelper.requestRegister(eContext,
//                JvmType.INT, false);
//            final GPR tmpr = (GPR) L1AHelper.requestRegister(eContext,
//                JvmType.REFERENCE, false);
//            final Label curInstrLabel = currentLabel;
//
//            /* Objectref is already on the stack */
//            writeResolveAndLoadClassToReg(classRef, classr);
//            stackFrame.getHelper().writeClassInitialize(curInstrLabel, classr, tmpr, resolvedType);
//
//            final Label trueLabel = new Label(curInstrLabel + "io-true");
//            final Label endLabel = new Label(curInstrLabel + "io-end");
//
//            /* Is instanceof? */
//            instanceOf(refr, classr, tmpr, cntr, trueLabel, false, currentLabel);
//
//            final IntItem result = (IntItem) L1AHelper.requestWordRegister(eContext, JvmType.INT, false);
//            final GPR resultr = result.getRegister();
//
//            /* Not instanceof */
//            // TODO: use setcc instead of jumps
//            os.writeXOR(resultr, resultr);
//            os.writeJMP(endLabel);
//
//            os.setObjectRef(trueLabel);
//            os.writeMOV_Const(resultr, 1);
//
//            // Push result
//            os.setObjectRef(endLabel);
//            ref.release(eContext);
//
//            vstack.push(result);
//
//            // Release
//            pool.release(classr);
//            pool.release(tmpr);
//            pool.release(cntr);
        } else {
            // It is a class, do the fast way
//            if (ref.getAddressingMode() == REGISTER) {
//                GPR refr = (GPR) ((RegisterLocation) ((Variable) ref).getLocation()).getRegister();
//                if (lhs.getAddressingMode() == REGISTER) {
//                    GPR resultr = (GPR) ((RegisterLocation) lhs.getLocation()).getRegister();
//                    instanceOfClass(refr, (VmClassType<?>) clazz.getResolvedVmClass(),
//                        SR1, resultr, null, false, currentLabel);
//                } else if (lhs.getAddressingMode() == STACK) {
//                    //todo
//                    throw new IllegalArgumentException();
//                } else {
//                    throw new IllegalArgumentException();
//                }
//            } else if (ref.getAddressingMode() == STACK) {
//                if (lhs.getAddressingMode() == REGISTER) {
//                    //todo
//                    throw new IllegalArgumentException();
//                } else if (lhs.getAddressingMode() == STACK) {
//                    //todo
//                    throw new IllegalArgumentException();
//                } else {
//                    throw new IllegalArgumentException();
//                }
//            } else {
//                throw new IllegalArgumentException();
//            }
        }
    }

    /**
     * Emit the core of the instanceof code.
     *
     * @param objectr   Register containing the object reference
     * @param trueLabel Where to jump for a true result. A false result will continue
     *                  directly after this method Register ECX must be free and it
     *                  destroyed.
     */
    private void instanceOfClass(GPR objectr, VmClassType<?> type, GPR tmpr,
                                 GPR resultr, Label trueLabel, boolean skipNullTest, Label currentLabel) {

        final int depth = type.getSuperClassDepth();
        X86CompilerHelper helper = stackFrame.getHelper();
        final int staticsOfs = helper.getSharedStaticsOffset(type);
        final Label curInstrLabel = currentLabel;
        final Label notInstanceOfLabel = new Label(curInstrLabel
            + "notInstanceOf");

        if (!type.isAlwaysInitialized()) {
            if (os.isCode32()) {
                helper.writeGetStaticsEntry(curInstrLabel, tmpr, type);
            }
//            else {
//                helper.writeGetStaticsEntry64(curInstrLabel, (GPR64) tmpr, (VmSharedStaticsEntry) type);
//            }
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
        final int tibOffset = ObjectLayout.TIB_SLOT * slotSize;
        final int arrayLengthOffset = VmArray.LENGTH_OFFSET * slotSize;
        final int arrayDataOffset = VmArray.DATA_OFFSET * slotSize;

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
    private void instanceOf(GPR objectr, GPR typer, GPR tmpr, GPR cntr,
                            Label trueLabel, boolean skipNullTest, Label currentLabel) {
        final Label curInstrLabel = currentLabel;
        final Label loopLabel = new Label(curInstrLabel + "loop");
        final Label notInstanceOfLabel = new Label(curInstrLabel
            + "notInstanceOf");

        X86CompilerHelper helper = stackFrame.getHelper();

        if (VmUtils.verifyAssertions()) {
            VmUtils._assert(objectr.getSize() == helper.ADDRSIZE, "objectr size");
            VmUtils._assert(typer.getSize() == helper.ADDRSIZE, "typer size");
            VmUtils._assert(tmpr.getSize() == helper.ADDRSIZE, "tmpr size");
            VmUtils._assert(cntr.getSize() == BITS32, "cntr size");
        }

        if (!skipNullTest) {
            /* Is objectref null? */
            os.writeTEST(objectr, objectr);
            os.writeJCC(notInstanceOfLabel, X86Constants.JZ);
        }

        final int slotSize = helper.SLOTSIZE;
        final int asize = helper.ADDRSIZE;
        final int tibOffset = ObjectLayout.TIB_SLOT * slotSize;
        final int arrayLengthOffset = VmArray.LENGTH_OFFSET * slotSize;
        final int arrayDataOffset = VmArray.DATA_OFFSET * slotSize;

        // TIB -> tmp
        os.writeMOV(asize, tmpr, objectr, tibOffset);
        // SuperClassesArray -> tmp
        os.writeMOV(asize, tmpr, tmpr, arrayDataOffset
            + (TIBLayout.SUPERCLASSES_INDEX * slotSize));
        // SuperClassesArray.length -> cntr
        os.writeMOV(BITS32, cntr, tmpr, arrayLengthOffset);
        // &superClassesArray[cnt-1] -> tmpr
//        if (os.isCode64()) {
//            // the MOV to cntr already zero-extends it, so no extension needed.
//            cntr = L1AHelper.get64BitReg(eContext, cntr);
//        }
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

    @Override
    public void generateCodeFor(LookupswitchQuad<T> quad) {
        checkLabel(quad.getAddress());
        int[] matchValues = quad.getMatchValues();
        final int n = matchValues.length;
        final GPR keyr;
        Operand key = quad.getKey();
        if (key.getAddressingMode() == CONSTANT) {
            //todo optimize it
            os.writeMOV_Const(SR1, ((IntConstant) key).getValue());
            keyr = SR1;
        } else if (key.getAddressingMode() == REGISTER) {
            keyr = (GPR) ((RegisterLocation) ((Variable) key).getLocation()).getRegister();
        } else if (key.getAddressingMode() == STACK) {
            int displacement1 = ((StackLocation) ((Variable) key).getLocation()).getDisplacement();
            os.writeMOV(BITS32, SR1, X86Register.EBP, displacement1);
            keyr = SR1;
        } else {
            throw new IllegalArgumentException();
        }
        IRBasicBlock[] blocks = quad.getTargetBlocks();
        for (int i = 0; i < n; i++) {
            os.writeCMP_Const(keyr, matchValues[i]);
            os.writeJCC(getInstrLabel(blocks[i].getStartPC()), X86Constants.JE); // JE
        }
        os.writeJMP(getInstrLabel(blocks[n].getStartPC()));
    }

    @Override
    public void generateCodeFor(TableswitchQuad<T> quad) {
        // IMPROVE: check Jaos implementation
        Operand val = quad.getValue();
        IRBasicBlock[] blocks = quad.getTargetBlocks();
        int lowValue = quad.getLowValue();
        int highValue = quad.getHighValue();
        int defAddress = quad.getDefaultAddress();
        X86CompilerHelper helper = stackFrame.getHelper();

        final int n = blocks.length;
        if ((n > 4) && os.isCode32()) {
            // Optimized version.  Needs some overhead, so only useful for
            // larger tables.
            //counters.getCounter("tableswitch-opt").inc();
            final Label curInstrLabel = getInstrLabel(quad.getAddress());
            final Label l1 = new Label(curInstrLabel + "$$l1");
            final Label l2 = new Label(curInstrLabel + "$$l2");
            final int l12distance = os.isCode32() ? 12 : 23;
            final int l1Ofs;
            if (val.getAddressingMode() == CONSTANT) {
                //todo optimize it more
                int value = ((IntConstant) val).getValue();
                final GPR tmp = SR1;
                value -= lowValue;
                // If outsite low-high range, jump to default
                if (value >= n) {
                    os.writeJMP(helper.getInstrLabel(defAddress));
                }
                // Get absolute address of l1 into S0. (do not use
                // stackMgr.writePOP!)
                os.writeCALL(l1);
                os.setObjectRef(l1);
                l1Ofs = os.getLength();
                os.writePOP(tmp);
                // Calculate absolute address of jumptable entry into S1
                os.writeLEA(tmp, tmp, value * helper.ADDRSIZE + l12distance);
                // Calculate absolute address of jump target
                os.writeADD(tmp, tmp, 0);
                os.writeLEA(tmp, tmp, 4); // Compensate for writeRelativeObject
                // difference
                // Jump to the calculated address
                os.writeJMP(tmp);
            } else if (val.getAddressingMode() == REGISTER) {
                GPR valr = (GPR) ((RegisterLocation) ((Variable) val).getLocation()).getRegister();
                final GPR tmp = SR1;
                if (lowValue != 0) {
                    os.writeSUB(valr, lowValue);
                }
                // If outsite low-high range, jump to default
                os.writeCMP_Const(valr, n);
                os.writeJCC(helper.getInstrLabel(defAddress), X86Constants.JAE);



                // Get absolute address of l1 into S0. (do not use
                // stackMgr.writePOP!)
                os.writeCALL(l1);
                os.setObjectRef(l1);
                l1Ofs = os.getLength();
                os.writePOP(tmp);
                // Calculate absolute address of jumptable entry into S1
                os.writeLEA(tmp, tmp, valr, helper.ADDRSIZE, l12distance);
                // Calculate absolute address of jump target
                os.writeADD(tmp, tmp, 0);
                os.writeLEA(tmp, tmp, 4); // Compensate for writeRelativeObject
                // difference
                // Jump to the calculated address
                os.writeJMP(tmp);
            } else if (val.getAddressingMode() == STACK) {
                int vald = ((StackLocation) ((Variable) val).getLocation()).getDisplacement();
                final GPR tmp = SR1;
                if (lowValue != 0) {
                    os.writeSUB(BITS32, X86Register.EBP, vald, lowValue);
                }
                // If outsite low-high range, jump to default
                os.writeCMP_Const(BITS32, X86Register.EBP, vald, n);
                os.writeJCC(helper.getInstrLabel(defAddress), X86Constants.JAE);



                // Get absolute address of l1 into S0. (do not use
                // stackMgr.writePOP!)
                os.writeCALL(l1);
                os.setObjectRef(l1);
                l1Ofs = os.getLength();
                os.writePOP(tmp);
                GPR sr2 = SR1 == X86Register.EAX ? X86Register.EBX : X86Register.EAX;
                os.writePUSH(sr2);
                os.writeMOV(BITS32, sr2, X86Register.EBP, vald);
                // Calculate absolute address of jumptable entry into S1
                os.writeLEA(tmp, tmp, sr2, helper.ADDRSIZE, l12distance);
                os.writePOP(sr2);
                // Calculate absolute address of jump target
                os.writeADD(tmp, tmp, 0);
                os.writeLEA(tmp, tmp, 4); // Compensate for writeRelativeObject
                // difference
                // Jump to the calculated address
                os.writeJMP(tmp);
            } else {
                throw new IllegalArgumentException();
            }

            // Emit offsets relative to where they are emitted
            os.setObjectRef(l2);
            final int l2Ofs = os.getLength();
            if ((l2Ofs - l1Ofs) != l12distance) {
                if (!os.isTextStream()) {
                    throw new CompileError("l12distance must be "
                        + (l2Ofs - l1Ofs));
                }
            }

            for (int i = 0; i < n; i++) {
                os.writeRelativeObjectRef(getInstrLabel(blocks[i].getStartPC()));
            }
//            L1AHelper.releaseRegister(eContext, tmp);
        } else {
            // Space wasting, but simple implementation

//            counters.getCounter("tableswitch-nonopt").inc();
            GPR valr;
            if (val.getAddressingMode() == CONSTANT) {
                //todo optimize it
                os.writeMOV_Const(SR1, ((IntConstant) val).getValue());
                valr = SR1;
            } else if (val.getAddressingMode() == REGISTER) {
                valr = (GPR) ((RegisterLocation) ((Variable) val).getLocation()).getRegister();
            } else if (val.getAddressingMode() == STACK) {
                int displacement1 = ((StackLocation) ((Variable) val).getLocation()).getDisplacement();
                os.writeMOV(BITS32, SR1, X86Register.EBP, displacement1);
                valr = SR1;
            } else {
                throw new IllegalArgumentException();
            }
            for (int i = 0; i < n; i++) {
                os.writeCMP_Const(valr, lowValue + i);
                os.writeJCC(getInstrLabel(blocks[i].getStartPC()), X86Constants.JE); // JE
            }
            os.writeJMP(getInstrLabel(defAddress));
        }

//        val.release(eContext);
    }

    @Override
    public void generateCodeFor(MonitorenterQuad<T> quad) {
        Operand op = quad.getOperand();
        if (op.getAddressingMode() == REGISTER) {
            os.writePUSH((GPR) ((RegisterLocation) ((Variable) op).getLocation()).getRegister());
        } else if (op.getAddressingMode() == STACK) {
            os.writePUSH(X86Register.EBP, ((StackLocation) ((Variable) op).getLocation()).getDisplacement());
        } else {
            throw new IllegalArgumentException();
        }
        stackFrame.getHelper().invokeJavaMethod(stackFrame.getEntryPoints().getMonitorEnterMethod());
    }

    @Override
    public void generateCodeFor(MonitorexitQuad<T> quad) {
        Operand op = quad.getOperand();
        if (op.getAddressingMode() == REGISTER) {
            os.writePUSH((GPR) ((RegisterLocation) ((Variable) op).getLocation()).getRegister());
        } else if (op.getAddressingMode() == STACK) {
            os.writePUSH(X86Register.EBP, ((StackLocation) ((Variable) op).getLocation()).getDisplacement());
        } else {
            throw new IllegalArgumentException();
        }
        stackFrame.getHelper().invokeJavaMethod(stackFrame.getEntryPoints().getMonitorExitMethod());
    }

    @Override
    public void generateCodeFor(NewAssignQuad<T> quad) {
        VmConstClass clazz = quad.getType();
        Label label = getInstrLabel(quad.getAddress());
        writeResolveAndLoadClassToReg(clazz, SR1, label);
        /* Setup a call to SoftByteCodes.allocObject */
        os.writePUSH(SR1); /* vmClass */
        os.writePUSH(-1); /* Size */
        stackFrame.getHelper().invokeJavaMethod(stackFrame.getEntryPoints().getAllocObjectMethod());
        Variable lhs = quad.getLHS();
        if (lhs.getAddressingMode() == REGISTER) {
            os.writeMOV(BITS32, (GPR) ((RegisterLocation) lhs.getLocation()).getRegister(), X86Register.EAX);
        } else if (lhs.getAddressingMode() == STACK) {
            os.writeMOV(BITS32, X86Register.EBP, ((StackLocation) lhs.getLocation()).getDisplacement(),
                X86Register.EAX);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void generateCodeFor(ThrowQuad<T> quad) {
        // Exception must be in EAX
        Operand op = quad.getOperand();
        if (op.getAddressingMode() == REGISTER) {
            GPR reg = (GPR) ((RegisterLocation) ((Variable) op).getLocation()).getRegister();
            if (reg != X86Register.EAX) {
                os.writeMOV(BITS32, X86Register.EAX, reg);
            }
        } else if (op.getAddressingMode() == STACK) {
            os.writeMOV(BITS32, X86Register.EAX, X86Register.EBP,
                ((StackLocation) ((Variable) op).getLocation()).getDisplacement());
        } else {
            throw new IllegalArgumentException();
        }

        // Jump
        stackFrame.getHelper().writeJumpTableCALL(X86JumpTable.VM_ATHROW_IDX);
    }

    /**
     * Write code to resolve the given constant class (if needed) and load the
     * resolved class (VmType instance) into the given register.
     *
     * @param classRef
     * @param label
     */
    private void writeResolveAndLoadClassToReg(VmConstClass classRef, GPR dst, Label label) {
        // Resolve the class
        classRef.resolve(currentMethod.getDeclaringClass().getLoader());
        final VmType type = classRef.getResolvedVmClass();

        // Load the class from the statics table
        X86CompilerHelper helper = stackFrame.getHelper();
        if (os.isCode32()) {
            helper.writeGetStaticsEntry(label, dst, type);
        } else {
            helper.writeGetStaticsEntry64(label, (X86Register.GPR64) dst, (VmSharedStaticsEntry) type);
        }
    }

    @Override
    public void generateCodeFor(StaticRefAssignQuad<T> quad) {
        checkLabel(quad.getAddress());
        VmConstFieldRef fieldRef = quad.getRHS().getFiledRef();
        final Label curInstrLabel = getInstrLabel(quad.getAddress());
        fieldRef.resolve(currentMethod.getDeclaringClass().getLoader());
        final int type = JvmType.SignatureToType(fieldRef.getSignature());
        final VmStaticField sf = (VmStaticField) fieldRef.getResolvedVmField();

        // Initialize if needed
//        if (!sf.getDeclaringClass().isAlwaysInitialized()) {
//            writeInitializeClass(fieldRef);
//        }

        // Get static field object
//        if (JvmType.isFloat(type)) {
//            final boolean is32bit = !fieldRef.isWide();
//            if (sf.isShared()) {
//                stackFrame.getHelper().writeGetStaticsEntryToFPU(curInstrLabel, (VmSharedStaticsEntry) sf, is32bit);
//            } else {
//                final GPR tmp = (GPR) L1AHelper.requestRegister(eContext,
//                    JvmType.REFERENCE, false);
//                helper.writeGetStaticsEntryToFPU(curInstrLabel,
//                    (VmIsolatedStaticsEntry) sf, is32bit, tmp);
//                L1AHelper.releaseRegister(eContext, tmp);
//            }
//            final Item result = ifac.createFPUStack(type);
//            pushFloat(result);
//            vstack.push(result);
//        } else
        if (!fieldRef.isWide()) {
            //final WordItem result = L1AHelper.requestWordRegister(eContext, type, false);
            Variable<T> lhs = quad.getLHS();
            //final GPR resultr = lhs result.getRegister();
            if (os.isCode32() || (type != JvmType.REFERENCE)) {
                if (sf.isShared()) {
                    if (lhs.getAddressingMode() == REGISTER) {
                        stackFrame.getHelper().writeGetStaticsEntry(curInstrLabel,
                            (GPR) ((RegisterLocation) lhs.getLocation()).getRegister(), sf);
                    } else {
                        //todo
                    }
                } else {
                    if (lhs.getAddressingMode() == REGISTER) {
                        //todo ESI ??
                        GPR tmp = GPR.ESI;
                        stackFrame.getHelper().writeGetStaticsEntry(curInstrLabel,
                            (GPR) ((RegisterLocation) lhs.getLocation()).getRegister(), sf, tmp);
                    } else {
                        //todo
                    }
                }
            }
//            else {
//                if (sf.isShared()) {
//                    stackFrame.getHelper().writeGetStaticsEntry64(curInstrLabel, (GPR64) resultr,
// (VmSharedStaticsEntry) sf);
//                } else {
//                    stackFrame.getHelper().writeGetStaticsEntry64(curInstrLabel, (GPR64) resultr,
// (VmIsolatedStaticsEntry) sf);
//                }
//            }
        }
//        else {
//            final DoubleWordItem result = L1AHelper.requestDoubleWordRegisters(
//                eContext, type);
//            if (os.isCode32()) {
//                final GPR lsb = result.getLsbRegister(eContext);
//                final GPR msb = result.getMsbRegister(eContext);
//                if (sf.isShared()) {
//                    helper.writeGetStaticsEntry64(curInstrLabel, lsb, msb, (VmSharedStaticsEntry) sf);
//                } else {
//                    helper.writeGetStaticsEntry64(curInstrLabel, lsb, msb, (VmIsolatedStaticsEntry) sf);
//                }
//            } else {
//                final GPR64 reg = result.getRegister(eContext);
//                if (sf.isShared()) {
//                    helper.writeGetStaticsEntry64(curInstrLabel, reg, (VmSharedStaticsEntry) sf);
//                } else {
//                    helper.writeGetStaticsEntry64(curInstrLabel, reg, (VmIsolatedStaticsEntry) sf);
//                }
//            }
//            vstack.push(result);
//        }
    }

    public void generateCodeFor(StaticRefStoreQuad<T> quad) {
        checkLabel(quad.getAddress());
        final Label curInstrLabel = getInstrLabel(quad.getAddress());
        VmConstFieldRef fieldRef = quad.getField().getFiledRef();
        fieldRef.resolve(currentMethod.getDeclaringClass().getLoader());
        final int type = JvmType.SignatureToType(fieldRef.getSignature());
        final VmStaticField sf = (VmStaticField) fieldRef.getResolvedVmField();

        if (!fieldRef.isWide()) {

            if (os.isCode32() || (type != JvmType.REFERENCE)) {
                if (sf.isShared()) {
                    if (quad.getOperand().getAddressingMode() == REGISTER) {
                        stackFrame.getHelper().writePutStaticsEntry(curInstrLabel,
                            (GPR) ((RegisterLocation) ((Variable) quad.getOperand()).getLocation()).getRegister(), sf);
                    } else {
                        //todo
                    }
                } else {
                    if (quad.getOperand().getAddressingMode() == REGISTER) {
                        //todo ESI ??
                        GPR tmp = GPR.ESI;
                        stackFrame.getHelper().writePutStaticsEntry(curInstrLabel,
                            (GPR) ((RegisterLocation) ((Variable) quad.getOperand()).getLocation()).getRegister(),
                            sf, tmp);
                    } else {
                        //todo
                    }
                }
            }
//            else {
//                if (sf.isShared()) {
//                    helper.writePutStaticsEntry64(curInstrLabel, (GPR64) valr, sf);
//                } else {
//                    final GPR tmp = (GPR) L1AHelper.requestRegister(eContext, JvmType.REFERENCE, false);
//                    helper.writePutStaticsEntry64(curInstrLabel, (GPR64) valr, sf, tmp);
//                    L1AHelper.releaseRegister(eContext, tmp);
//                }
//            }
//            if (!sf.isPrimitive() && helper.needsWriteBarrier()) {
//                final GPR tmp = (GPR) L1AHelper.requestRegister(eContext, JvmType.INT, false);
//                helper.writePutstaticWriteBarrier(sf, valr, tmp);
//                L1AHelper.releaseRegister(eContext, tmp);
//            }
        }

//        else {
//            final DoubleWordItem dval = (DoubleWordItem) val;
//            if (os.isCode32()) {
//                if (sf.isShared()) {
//                    helper.writePutStaticsEntry64(curInstrLabel, dval.getLsbRegister(eContext), dval
//                        .getMsbRegister(eContext), sf);
//                } else {
//                    final GPR tmp = (GPR) L1AHelper.requestRegister(eContext, JvmType.REFERENCE, false);
//                    helper.writePutStaticsEntry64(curInstrLabel, dval.getLsbRegister(eContext),
//                        dval.getMsbRegister(eContext), sf, tmp);
//                    L1AHelper.releaseRegister(eContext, tmp);
//                }
//            } else {
//                if (sf.isShared()) {
//                    helper.writePutStaticsEntry64(curInstrLabel, dval.getRegister(eContext), sf);
//                } else {
//                    final GPR tmp = (GPR) L1AHelper.requestRegister(eContext, JvmType.REFERENCE, false);
//                    helper.writePutStaticsEntry64(curInstrLabel, dval.getRegister(eContext), sf, tmp);
//                    L1AHelper.releaseRegister(eContext, tmp);
//                }
//            }
//        }

    }

    @Override
    public void generateCodeFor(RefAssignQuad<T> quad) {
        VmConstFieldRef fieldRef = quad.getFieldRef();
        fieldRef.resolve(currentMethod.getDeclaringClass().getLoader());
        final VmField field = fieldRef.getResolvedVmField();
        if (field.isStatic()) {
            throw new IncompatibleClassChangeError(
                "getfield called on static field " + fieldRef.getName());
        }
        final VmInstanceField inf = (VmInstanceField) field;
        final int fieldOffset = inf.getOffset();
        final int type = JvmType.SignatureToType(fieldRef.getSignature());
        final boolean isfloat = JvmType.isFloat(type);

        Variable dest = quad.getLHS();
        Variable ref = (Variable) quad.getRef();

        // get field
        if (!fieldRef.isWide()) {
            if (isfloat) {
                //todo
                throw new IllegalArgumentException();
//                result = ifac.createFPUStack(JvmType.FLOAT);
//                os.writeFLD32(refr, fieldOffset);
//                pushFloat(result);
            } else {
                final char fieldType = field.getSignature().charAt(0);
                //todo check 8bits support for registers
                switch (fieldType) {
                    case 'Z': { // boolean
                        if (dest.getAddressingMode() == REGISTER) {
                            GPR destr = (GPR) ((RegisterLocation) dest.getLocation()).getRegister();
                            if (ref.getAddressingMode() == REGISTER) {
                                GPR refr = (GPR) ((RegisterLocation) ref.getLocation()).getRegister();
                                os.writeMOVZX(destr, refr, fieldOffset, BITS8);
                            } else if (ref.getAddressingMode() == STACK) {
                                int disp = ((StackLocation) ref.getLocation()).getDisplacement();
                                os.writeMOV(BITS32, SR1, X86Register.EBP, disp);
                                os.writeMOVZX(destr, SR1, fieldOffset, BITS8);
                            } else {
                                throw new IllegalArgumentException();
                            }
                        } else if (dest.getAddressingMode() == STACK) {
                            int destd = ((StackLocation) dest.getLocation()).getDisplacement();
                            if (ref.getAddressingMode() == REGISTER) {
                                GPR refr = (GPR) ((RegisterLocation) ref.getLocation()).getRegister();
                                os.writeMOVZX(SR1, refr, fieldOffset, BITS8);
                                os.writeMOV(BITS32, X86Register.EBP, destd, SR1);
                            } else if (ref.getAddressingMode() == STACK) {
                                GPR sr2 = SR1 == X86Register.EAX ? X86Register.EBX : X86Register.EAX;
                                os.writePUSH(sr2);
                                int disp = ((StackLocation) ref.getLocation()).getDisplacement();
                                os.writeMOV(BITS32, SR1, X86Register.EBP, disp);
                                os.writeMOVZX(sr2, SR1, fieldOffset, BITS8);
                                os.writeMOV(BITS32, X86Register.EBP, destd, sr2);
                                os.writePOP(sr2);
                            } else {
                                throw new IllegalArgumentException();
                            }
                        } else {
                            throw new IllegalArgumentException();
                        }
                        break;
                    }
                    case 'B': { // byte
                        if (dest.getAddressingMode() == REGISTER) {
                            GPR destr = (GPR) ((RegisterLocation) dest.getLocation()).getRegister();
                            if (ref.getAddressingMode() == REGISTER) {
                                GPR refr = (GPR) ((RegisterLocation) ref.getLocation()).getRegister();
                                os.writeMOVSX(destr, refr, fieldOffset, BITS8);
                            } else if (ref.getAddressingMode() == STACK) {
                                int disp = ((StackLocation) ref.getLocation()).getDisplacement();
                                os.writeMOV(BITS32, SR1, X86Register.EBP, disp);
                                os.writeMOVSX(destr, SR1, fieldOffset, BITS8);
                            } else {
                                throw new IllegalArgumentException();
                            }
                        } else if (dest.getAddressingMode() == STACK) {
                            int destd = ((StackLocation) dest.getLocation()).getDisplacement();
                            if (ref.getAddressingMode() == REGISTER) {
                                GPR refr = (GPR) ((RegisterLocation) ref.getLocation()).getRegister();
                                os.writeMOVSX(SR1, refr, fieldOffset, BITS8);
                                os.writeMOV(BITS32, X86Register.EBP, destd, SR1);
                            } else if (ref.getAddressingMode() == STACK) {
                                GPR sr2 = SR1 == X86Register.EAX ? X86Register.EBX : X86Register.EAX;
                                os.writePUSH(sr2);
                                int disp = ((StackLocation) ref.getLocation()).getDisplacement();
                                os.writeMOV(BITS32, SR1, X86Register.EBP, disp);
                                os.writeMOVSX(sr2, SR1, fieldOffset, BITS8);
                                os.writeMOV(BITS32, X86Register.EBP, destd, sr2);
                                os.writePOP(sr2);
                            } else {
                                throw new IllegalArgumentException();
                            }
                        } else {
                            throw new IllegalArgumentException();
                        }
                        break;
                    }
                    case 'C': { // char
                        if (dest.getAddressingMode() == REGISTER) {
                            GPR destr = (GPR) ((RegisterLocation) dest.getLocation()).getRegister();
                            if (ref.getAddressingMode() == REGISTER) {
                                GPR refr = (GPR) ((RegisterLocation) ref.getLocation()).getRegister();
                                os.writeMOVZX(destr, refr, fieldOffset, BITS16);
                            } else if (ref.getAddressingMode() == STACK) {
                                int disp = ((StackLocation) ref.getLocation()).getDisplacement();
                                os.writeMOV(BITS32, SR1, X86Register.EBP, disp);
                                os.writeMOVZX(destr, SR1, fieldOffset, BITS16);
                            } else {
                                throw new IllegalArgumentException();
                            }
                        } else if (dest.getAddressingMode() == STACK) {
                            int destd = ((StackLocation) dest.getLocation()).getDisplacement();
                            if (ref.getAddressingMode() == REGISTER) {
                                GPR refr = (GPR) ((RegisterLocation) ref.getLocation()).getRegister();
                                os.writeMOVZX(SR1, refr, fieldOffset, BITS16);
                                os.writeMOV(BITS32, X86Register.EBP, destd, SR1);
                            } else if (ref.getAddressingMode() == STACK) {
                                GPR sr2 = SR1 == X86Register.EAX ? X86Register.EBX : X86Register.EAX;
                                os.writePUSH(sr2);
                                int disp = ((StackLocation) ref.getLocation()).getDisplacement();
                                os.writeMOV(BITS32, SR1, X86Register.EBP, disp);
                                os.writeMOVZX(sr2, SR1, fieldOffset, BITS16);
                                os.writeMOV(BITS32, X86Register.EBP, destd, sr2);
                                os.writePOP(sr2);
                            } else {
                                throw new IllegalArgumentException();
                            }
                        } else {
                            throw new IllegalArgumentException();
                        }
                        break;
                    }
                    case 'S': { // short
                        if (dest.getAddressingMode() == REGISTER) {
                            GPR destr = (GPR) ((RegisterLocation) dest.getLocation()).getRegister();
                            if (ref.getAddressingMode() == REGISTER) {
                                GPR refr = (GPR) ((RegisterLocation) ref.getLocation()).getRegister();
                                os.writeMOVSX(destr, refr, fieldOffset, BITS16);
                            } else if (ref.getAddressingMode() == STACK) {
                                int disp = ((StackLocation) ref.getLocation()).getDisplacement();
                                os.writeMOV(BITS32, SR1, X86Register.EBP, disp);
                                os.writeMOVSX(destr, SR1, fieldOffset, BITS16);
                            } else {
                                throw new IllegalArgumentException();
                            }
                        } else if (dest.getAddressingMode() == STACK) {
                            int destd = ((StackLocation) dest.getLocation()).getDisplacement();
                            if (ref.getAddressingMode() == REGISTER) {
                                GPR refr = (GPR) ((RegisterLocation) ref.getLocation()).getRegister();
                                os.writeMOVSX(SR1, refr, fieldOffset, BITS16);
                                os.writeMOV(BITS32, X86Register.EBP, destd, SR1);
                            } else if (ref.getAddressingMode() == STACK) {
                                GPR sr2 = SR1 == X86Register.EAX ? X86Register.EBX : X86Register.EAX;
                                os.writePUSH(sr2);
                                int disp = ((StackLocation) ref.getLocation()).getDisplacement();
                                os.writeMOV(BITS32, SR1, X86Register.EBP, disp);
                                os.writeMOVSX(sr2, SR1, fieldOffset, BITS16);
                                os.writeMOV(BITS32, X86Register.EBP, destd, sr2);
                                os.writePOP(sr2);
                            } else {
                                throw new IllegalArgumentException();
                            }
                        } else {
                            throw new IllegalArgumentException();
                        }
                        break;
                    }
                    case 'I': // int
                    case 'L': // Object
                    case '[': { // array
                        if (dest.getAddressingMode() == REGISTER) {
                            GPR destr = (GPR) ((RegisterLocation) dest.getLocation()).getRegister();
                            if (ref.getAddressingMode() == REGISTER) {
                                GPR refr = (GPR) ((RegisterLocation) ref.getLocation()).getRegister();
                                os.writeMOV(BITS32, destr, refr, fieldOffset);
                            } else if (ref.getAddressingMode() == STACK) {
                                int disp = ((StackLocation) ref.getLocation()).getDisplacement();
                                os.writeMOV(BITS32, SR1, X86Register.EBP, disp);
                                os.writeMOV(BITS32, destr, SR1, fieldOffset);
                            } else {
                                throw new IllegalArgumentException();
                            }
                        } else if (dest.getAddressingMode() == STACK) {
                            int destd = ((StackLocation) dest.getLocation()).getDisplacement();
                            if (ref.getAddressingMode() == REGISTER) {
                                GPR refr = (GPR) ((RegisterLocation) ref.getLocation()).getRegister();
                                os.writeMOV(BITS32, SR1, refr, fieldOffset);
                                os.writeMOV(BITS32, X86Register.EBP, destd, SR1);
                            } else if (ref.getAddressingMode() == STACK) {
                                GPR sr2 = SR1 == X86Register.EAX ? X86Register.EBX : X86Register.EAX;
                                os.writePUSH(sr2);
                                int disp = ((StackLocation) ref.getLocation()).getDisplacement();
                                os.writeMOV(BITS32, SR1, X86Register.EBP, disp);
                                os.writeMOV(BITS32, sr2, SR1, fieldOffset);
                                os.writeMOV(BITS32, X86Register.EBP, destd, sr2);
                                os.writePOP(sr2);
                            } else {
                                throw new IllegalArgumentException();
                            }
                        } else {
                            throw new IllegalArgumentException();
                        }
                        break;
                    }
                    default:
                        throw new IllegalArgumentException("Unknown fieldType " + fieldType);
                }
            }
        } else {
//            if (isfloat) {
//                result = ifac.createFPUStack(JvmType.DOUBLE);
//                os.writeFLD64(refr, fieldOffset);
//                pushFloat(result);
//            } else {
//                final DoubleWordItem idw = L1AHelper
//                    .requestDoubleWordRegisters(eContext, type);
//                if (os.isCode32()) {
//                    final GPR lsb = idw.getLsbRegister(eContext);
//                    final GPR msb = idw.getMsbRegister(eContext);
//                    os.writeMOV(BITS32, lsb, refr, fieldOffset + LSB);
//                    os.writeMOV(BITS32, msb, refr, fieldOffset + MSB);
//                } else {
//                    final GPR64 reg = idw.getRegister(eContext);
//                    os.writeMOV(BITS64, reg, refr, fieldOffset);
//                }
//                result = idw;
//            }
            //todo
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void generateCodeFor(RefStoreQuad<T> quad) {
        VmConstFieldRef fieldRef = quad.getFieldRef();
        fieldRef.resolve(currentMethod.getDeclaringClass().getLoader());
        final VmField field = fieldRef.getResolvedVmField();
        if (field.isStatic()) {
            throw new IncompatibleClassChangeError(
                "getfield called on static field " + fieldRef.getName());
        }
        final VmInstanceField inf = (VmInstanceField) field;
        final int offset = inf.getOffset();
        final boolean wide = fieldRef.isWide();

        // Get operands
//        final Item val = vstack.pop();
//        assertCondition(val.getCategory() == ((wide) ? 2 : 1),
//            "category mismatch");

        Operand ref = quad.getRef();
        Operand val = quad.getValue();

        if (!wide) {
            final char fieldType = field.getSignature().charAt(0);
            // Store field
            switch (fieldType) {
                case 'Z': // boolean
                case 'B': // byte
                    //todo 8bits support wval.loadToBITS8GPR(eContext);
                    if (ref.getAddressingMode() == REGISTER) {
                        GPR refr = (GPR) ((RegisterLocation) ((Variable) ref).getLocation()).getRegister();
                        if (val.getAddressingMode() == CONSTANT) {
                            os.writeMOV_Const(BITS8, refr, offset, ((IntConstant) val).getValue());
                        } else if (val.getAddressingMode() == REGISTER) {
                            os.writeMOV(BITS8, refr, offset,
                                (GPR) ((RegisterLocation) ((Variable) val).getLocation()).getRegister());
                        } else if (val.getAddressingMode() == STACK) {
                            os.writeMOV(BITS32, SR1, X86Register.EBP,
                                ((StackLocation) ((Variable) val).getLocation()).getDisplacement());
                            os.writeMOV(BITS8, refr, offset, SR1);
                        } else {
                            throw new IllegalArgumentException();
                        }
                    } else if (ref.getAddressingMode() == STACK) {
                        int disp = ((StackLocation) ((Variable) ref).getLocation()).getDisplacement();
                        if (val.getAddressingMode() == CONSTANT) {
                            os.writeMOV(BITS32, SR1, X86Register.EBP, disp);
                            os.writeMOV_Const(BITS8, SR1, offset, ((IntConstant) val).getValue());
                        } else if (val.getAddressingMode() == REGISTER) {
                            os.writeMOV(BITS32, SR1, X86Register.EBP, disp);
                            os.writeMOV(BITS8, SR1, offset,
                                (GPR) ((RegisterLocation) ((Variable) val).getLocation()).getRegister());
                        } else if (val.getAddressingMode() == STACK) {
                            GPR sr2 = SR1 == X86Register.EAX ? X86Register.EBX : X86Register.EAX;
                            os.writePUSH(sr2);
                            os.writeMOV(BITS32, SR1, X86Register.EBP, disp);
                            os.writeMOV(BITS32, sr2, X86Register.EBP,
                                ((StackLocation) ((Variable) val).getLocation()).getDisplacement());
                            os.writeMOV(BITS8, SR1, offset, sr2);
                            os.writePOP(sr2);
                        } else {
                            throw new IllegalArgumentException();
                        }
                    } else {
                        throw new IllegalArgumentException();
                    }
                    break;
                case 'C': // char
                case 'S': // short
                    if (ref.getAddressingMode() == REGISTER) {
                        GPR refr = (GPR) ((RegisterLocation) ((Variable) ref).getLocation()).getRegister();
                        if (val.getAddressingMode() == CONSTANT) {
                            os.writeMOV_Const(BITS16, refr, offset, ((IntConstant) val).getValue());
                        } else if (val.getAddressingMode() == REGISTER) {
                            os.writeMOV(BITS16, refr, offset,
                                (GPR) ((RegisterLocation) ((Variable) val).getLocation()).getRegister());
                        } else if (val.getAddressingMode() == STACK) {
                            os.writeMOV(BITS32, SR1, X86Register.EBP,
                                ((StackLocation) ((Variable) val).getLocation()).getDisplacement());
                            os.writeMOV(BITS16, refr, offset, SR1);
                        } else {
                            throw new IllegalArgumentException();
                        }
                    } else if (ref.getAddressingMode() == STACK) {
                        int disp = ((StackLocation) ((Variable) ref).getLocation()).getDisplacement();
                        if (val.getAddressingMode() == CONSTANT) {
                            os.writeMOV(BITS32, SR1, X86Register.EBP, disp);
                            os.writeMOV_Const(BITS16, SR1, offset, ((IntConstant) val).getValue());
                        } else if (val.getAddressingMode() == REGISTER) {
                            os.writeMOV(BITS32, SR1, X86Register.EBP, disp);
                            os.writeMOV(BITS16, SR1, offset,
                                (GPR) ((RegisterLocation) ((Variable) val).getLocation()).getRegister());
                        } else if (val.getAddressingMode() == STACK) {
                            GPR sr2 = SR1 == X86Register.EAX ? X86Register.EBX : X86Register.EAX;
                            os.writePUSH(sr2);
                            os.writeMOV(BITS32, SR1, X86Register.EBP, disp);
                            os.writeMOV(BITS32, sr2, X86Register.EBP,
                                ((StackLocation) ((Variable) val).getLocation()).getDisplacement());
                            os.writeMOV(BITS16, SR1, offset, sr2);
                            os.writePOP(sr2);
                        } else {
                            throw new IllegalArgumentException();
                        }
                    } else {
                        throw new IllegalArgumentException();
                    }
                    break;
                case 'F': // float
                case 'I': // int
                case 'L': // Object
                case '[': // array
                    if (ref.getAddressingMode() == REGISTER) {
                        GPR refr = (GPR) ((RegisterLocation) ((Variable) ref).getLocation()).getRegister();
                        if (val.getAddressingMode() == CONSTANT) {
                            os.writeMOV_Const(BITS32, refr, offset, ((IntConstant) val).getValue());
                        } else if (val.getAddressingMode() == REGISTER) {
                            os.writeMOV(BITS32, refr, offset,
                                (GPR) ((RegisterLocation) ((Variable) val).getLocation()).getRegister());
                        } else if (val.getAddressingMode() == STACK) {
                            os.writeMOV(BITS32, SR1, X86Register.EBP,
                                ((StackLocation) ((Variable) val).getLocation()).getDisplacement());
                            os.writeMOV(BITS32, refr, offset, SR1);
                        } else {
                            throw new IllegalArgumentException();
                        }
                    } else if (ref.getAddressingMode() == STACK) {
                        int disp = ((StackLocation) ((Variable) ref).getLocation()).getDisplacement();
                        if (val.getAddressingMode() == CONSTANT) {
                            os.writeMOV(BITS32, SR1, X86Register.EBP, disp);
                            os.writeMOV_Const(BITS32, SR1, offset, ((IntConstant) val).getValue());
                        } else if (val.getAddressingMode() == REGISTER) {
                            os.writeMOV(BITS32, SR1, X86Register.EBP, disp);
                            os.writeMOV(BITS32, SR1, offset,
                                (GPR) ((RegisterLocation) ((Variable) val).getLocation()).getRegister());
                        } else if (val.getAddressingMode() == STACK) {
                            GPR sr2 = SR1 == X86Register.EAX ? X86Register.EBX : X86Register.EAX;
                            os.writePUSH(sr2);
                            os.writeMOV(BITS32, SR1, X86Register.EBP, disp);
                            os.writeMOV(BITS32, sr2, X86Register.EBP,
                                ((StackLocation) ((Variable) val).getLocation()).getDisplacement());
                            os.writeMOV(BITS32, SR1, offset, sr2);
                            os.writePOP(sr2);
                        } else {
                            throw new IllegalArgumentException();
                        }
                    } else {
                        throw new IllegalArgumentException();
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unknown fieldType: " + fieldType);
            }
            // Writebarrier
//            if (!inf.isPrimitive() && helper.needsWriteBarrier()) {
//                final GPR tmp = (GPR) L1AHelper.requestRegister(eContext,
//                    JvmType.REFERENCE, false);
//                helper.writePutfieldWriteBarrier(inf, refr, valr, tmp);
//                L1AHelper.releaseRegister(eContext, tmp);
//            }
        } else {
//            final DoubleWordItem dval = (DoubleWordItem) val;
//            if (os.isCode32()) {
//                os.writeMOV(BITS32, refr, offset + MSB, dval
//                    .getMsbRegister(eContext));
//                os.writeMOV(BITS32, refr, offset + LSB, dval
//                    .getLsbRegister(eContext));
//            } else {
//                os.writeMOV(BITS64, refr, offset, dval.getRegister(eContext));
//            }
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void generateCodeFor(SpecialCallAssignQuad quad) {
        VmConstMethodRef methodRef = quad.getMethodRef();
        methodRef.resolve(currentMethod.getDeclaringClass().getLoader());
        try {
            final VmMethod sm = methodRef.getResolvedVmMethod();

            //dropParameters(sm, true);
            writeParameters(quad);
            // Call the methods code from the statics table
            stackFrame.getHelper().invokeJavaMethod(sm);
            // Result is already on the stack.
        } catch (ClassCastException ex) {
            BootLogInstance.get().error(methodRef.getResolvedVmMethod().getClass().getName() + '#' +
                methodRef.getName());
            throw ex;
        }
        Variable lhs = quad.getLHS();
        if (lhs.getAddressingMode() == REGISTER) {
            GPR reg = (GPR) ((RegisterLocation) lhs.getLocation()).getRegister();
            if (reg != GPR.EAX) {
                os.writeMOV(X86Constants.BITS32, reg, GPR.EAX);
            }
        } else if (lhs.getAddressingMode() == STACK) {
            int disp = ((StackLocation) lhs.getLocation()).getDisplacement();
            os.writeMOV(X86Constants.BITS32, GPR.ESP, disp, GPR.EAX);
        }
    }

    @Override
    public void generateCodeFor(SpecialCallQuad quad) {
        VmConstMethodRef methodRef = quad.getMethodRef();
        methodRef.resolve(currentMethod.getDeclaringClass().getLoader());
        try {
            final VmMethod sm = methodRef.getResolvedVmMethod();

            //dropParameters(sm, true);
            writeParameters(quad);
            // Call the methods code from the statics table
            stackFrame.getHelper().invokeJavaMethod(sm);
            // Result is already on the stack.
        } catch (ClassCastException ex) {
//            BootLogInstance.get().error(methodRef.getResolvedVmMethod().getClass().getName() + '#' +
//                methodRef.getName());
            throw ex;
        }
    }

    @Override
    public void generateCodeFor(VirtualCallAssignQuad quad) {
        checkLabel(quad.getAddress());
        VmConstMethodRef methodRef = quad.getMethodRef();
        methodRef.resolve(currentMethod.getDeclaringClass().getLoader());
        final VmMethod mts = methodRef.getResolvedVmMethod();

        if (mts.isStatic()) {
            throw new IncompatibleClassChangeError(
                "Static method in invokevirtual");
        }

        final VmInstanceMethod method = (VmInstanceMethod) mts;
        final VmType<?> declClass = method.getDeclaringClass();
        if (declClass.isMagicType()) {
//            magicHelper.emitMagic(eContext, method, false, this, currentMethod);
        } else {
            // TODO: port to ORP style (http://orp.sourceforge.net/)
//            vstack.push(eContext);

            writeParameters(quad);
//            dropParameters(mts, true);

            if (method.isFinal() || method.isPrivate() || declClass.isFinal()) {
                // Do a fast invocation
//                counters.getCounter("virtual-final").inc();

                // Call the methods native code from the statics table
                stackFrame.getHelper().invokeJavaMethod(method);
                // Result is already on the stack.
            } else {
                // Do a virtual method table invocation
//                counters.getCounter("virtual-vmt").inc();

                final int tibIndex = method.getTibOffset();
                final int argSlotCount = Signature.getArgSlotCount(typeSizeInfo, methodRef
                    .getSignature());

                final int slotSize = stackFrame.getHelper().SLOTSIZE;
                final int asize = stackFrame.getHelper().ADDRSIZE;
                int arrayDataOffset = VmArray.DATA_OFFSET * slotSize;
                int tibOffset = ObjectLayout.TIB_SLOT * slotSize;

                /* Get objectref -> EAX */
                os.writeMOV(asize, stackFrame.getHelper().AAX, stackFrame.getHelper().SP, argSlotCount * slotSize);
                /* Get VMT of objectref -> EAX */
                os.writeMOV(asize, stackFrame.getHelper().AAX, stackFrame.getHelper().AAX, tibOffset);
                /* Get entry in VMT -> EAX */
                os.writeMOV(asize, stackFrame.getHelper().AAX, stackFrame.getHelper().AAX,
                    arrayDataOffset + (tibIndex * slotSize));

                /* Now invoke the method */
                os.writeCALL(stackFrame.getHelper().AAX,
                    stackFrame.getEntryPoints().getVmMethodNativeCodeField().getOffset());
//                stackFrame.getHelper().pushReturnValue(methodRef.getSignature());
                // Result is already on the stack.
            }
        }


        Variable lhs = quad.getLHS();
        if (lhs.getAddressingMode() == REGISTER) {
            GPR reg = (GPR) ((RegisterLocation) lhs.getLocation()).getRegister();
            if (reg != GPR.EAX) {
                os.writeMOV(X86Constants.BITS32, reg, GPR.EAX);
            }
        } else if (lhs.getAddressingMode() == STACK) {
            int disp = ((StackLocation) lhs.getLocation()).getDisplacement();
            os.writeMOV(X86Constants.BITS32, GPR.ESP, disp, GPR.EAX);
        }
    }

    @Override
    public void generateCodeFor(VirtualCallQuad quad) {
        checkLabel(quad.getAddress());
        VmConstMethodRef methodRef = quad.getMethodRef();
        methodRef.resolve(currentMethod.getDeclaringClass().getLoader());
        final VmMethod mts = methodRef.getResolvedVmMethod();

        if (mts.isStatic()) {
            throw new IncompatibleClassChangeError(
                "Static method in invokevirtual");
        }

        final VmInstanceMethod method = (VmInstanceMethod) mts;
        final VmType<?> declClass = method.getDeclaringClass();
        if (declClass.isMagicType()) {
//            magicHelper.emitMagic(eContext, method, false, this, currentMethod);
        } else {
            // TODO: port to ORP style (http://orp.sourceforge.net/)
//            vstack.push(eContext);

            writeParameters(quad);
//            dropParameters(mts, true);

            if (method.isFinal() || method.isPrivate() || declClass.isFinal()) {
                // Do a fast invocation
//                counters.getCounter("virtual-final").inc();

                // Call the methods native code from the statics table
                stackFrame.getHelper().invokeJavaMethod(method);
                // Result is already on the stack.
            } else {
                // Do a virtual method table invocation
//                counters.getCounter("virtual-vmt").inc();

                final int tibIndex = method.getTibOffset();
                final int argSlotCount = Signature.getArgSlotCount(typeSizeInfo, methodRef
                    .getSignature());

                final int slotSize = stackFrame.getHelper().SLOTSIZE;
                final int asize = stackFrame.getHelper().ADDRSIZE;
                int arrayDataOffset = VmArray.DATA_OFFSET * slotSize;
                int tibOffset = ObjectLayout.TIB_SLOT * slotSize;

                /* Get objectref -> EAX */
                os.writeMOV(asize, stackFrame.getHelper().AAX, stackFrame.getHelper().SP, argSlotCount * slotSize);
                /* Get VMT of objectref -> EAX */
                os.writeMOV(asize, stackFrame.getHelper().AAX, stackFrame.getHelper().AAX, tibOffset);
                /* Get entry in VMT -> EAX */
                os.writeMOV(asize, stackFrame.getHelper().AAX, stackFrame.getHelper().AAX,
                    arrayDataOffset + (tibIndex * slotSize));

                /* Now invoke the method */
                os.writeCALL(stackFrame.getHelper().AAX,
                    stackFrame.getEntryPoints().getVmMethodNativeCodeField().getOffset());
//                stackFrame.getHelper().pushReturnValue(methodRef.getSignature());
                // Result is already on the stack.
            }
        }
    }

    @Override
    public void generateCodeFor(StaticCallAssignQuad<T> quad) {
        checkLabel(quad.getAddress());
        VmConstMethodRef methodRef = quad.getMethodRef();
        methodRef.resolve(currentMethod.getDeclaringClass().getLoader());
        final VmStaticMethod method = (VmStaticMethod) methodRef.getResolvedVmMethod();
        if (method.getDeclaringClass().isMagicType()) {
//todo            magicHelper.emitMagic(eContext, method, true, this, currentMethod);
        } else {
            writeParameters(quad);
            //todo handle return types
            final int offset = stackFrame.getHelper().getSharedStaticsOffset(method);
            os.writeCALL(stackFrame.getHelper().STATICS, offset);
            Variable lhs = quad.getLHS();
            if (lhs.getAddressingMode() == REGISTER) {
                GPR reg = (GPR) ((RegisterLocation) lhs.getLocation()).getRegister();
                if (reg != GPR.EAX) {
                    os.writeMOV(X86Constants.BITS32, reg, GPR.EAX);
                }
            } else if (lhs.getAddressingMode() == STACK) {
                int disp = ((StackLocation) lhs.getLocation()).getDisplacement();
                os.writeMOV(X86Constants.BITS32, GPR.ESP, disp, GPR.EAX);
            }
        }
    }

    @Override
    public void generateCodeFor(StaticCallQuad<T> quad) {
        checkLabel(quad.getAddress());
        VmConstMethodRef methodRef = quad.getMethodRef();
        methodRef.resolve(currentMethod.getDeclaringClass().getLoader());
        final VmStaticMethod method = (VmStaticMethod) methodRef.getResolvedVmMethod();
        if (method.getDeclaringClass().isMagicType()) {
//todo            magicHelper.emitMagic(eContext, method, true, this, currentMethod);
        } else {
            writeParameters(quad);
            final int offset = stackFrame.getHelper().getSharedStaticsOffset(method);
            os.writeCALL(stackFrame.getHelper().STATICS, offset);
        }
    }

    @Override
    public void generateCodeFor(InterfaceCallAssignQuad quad) {
        VmConstMethodRef methodRef = quad.getMethodRef();
        methodRef.resolve(currentMethod.getDeclaringClass().getLoader());
        final VmMethod method = methodRef.getResolvedVmMethod();
        final int argSlotCount = quad.getReferencedOps().length - 1;
        writeParameters(quad);
        // Get objectref -> EAX
        X86CompilerHelper helper = stackFrame.getHelper();
        os.writeMOV(helper.ADDRSIZE, helper.AAX, helper.SP, argSlotCount * helper.SLOTSIZE);
        X86IMTCompiler32.emitInvokeInterface(os, method);

        Variable lhs = quad.getLHS();
        if (lhs.getAddressingMode() == REGISTER) {
            GPR reg = (GPR) ((RegisterLocation) lhs.getLocation()).getRegister();
            if (reg != GPR.EAX) {
                os.writeMOV(X86Constants.BITS32, reg, GPR.EAX);
            }
        } else if (lhs.getAddressingMode() == STACK) {
            int disp = ((StackLocation) lhs.getLocation()).getDisplacement();
            os.writeMOV(X86Constants.BITS32, GPR.ESP, disp, GPR.EAX);
        }
    }

    @Override
    public void generateCodeFor(InterfaceCallQuad quad) {
        VmConstMethodRef methodRef = quad.getMethodRef();
        // Resolve the method
        methodRef.resolve(currentMethod.getDeclaringClass().getLoader());

        final VmMethod method = methodRef.getResolvedVmMethod();
        final int argSlotCount = quad.getReferencedOps().length - 1;

        // remove parameters from vstack
        writeParameters(quad);
        // Get objectref -> EAX
        X86CompilerHelper helper = stackFrame.getHelper();
        os.writeMOV(helper.ADDRSIZE, helper.AAX, helper.SP, argSlotCount * helper.SLOTSIZE);
        // Write the actual invokeinterface
//        if (os.isCode32()) {
        X86IMTCompiler32.emitInvokeInterface(os, method);
//        } else {
//            X86IMTCompiler64.emitInvokeInterface(os, method);
//        }
        // Test the stack alignment
        //stackFrame.writeStackAlignmentTest(getInstrLabel(quad.getAddress()));
    }

    private void writeParameters(Quad quad) {
        Operand<T>[] referencedOps = quad.getReferencedOps();
        for (int i = 0; i < referencedOps.length; i++) {
            Operand operand = referencedOps[i];
            if (operand.getAddressingMode() == CONSTANT) {
                //todo handle other types
                int c = ((IntConstant) operand).getValue();
                os.writePUSH(c);
            } else if (operand.getAddressingMode() == REGISTER) {
                GPR reg = (GPR) ((RegisterLocation) ((Variable) operand).getLocation()).getRegister();
                os.writePUSH(reg);
            } else if (operand.getAddressingMode() == STACK) {
                int disp = ((StackLocation) ((Variable) operand).getLocation()).getDisplacement();
                os.writePUSH(GPR.EBP, disp);
                if (operand.getType() == Operand.LONG) {
                    os.writePUSH(GPR.EBP, disp - stackFrame.getHelper().SLOTSIZE);
                }
            } else {
                throw new IllegalArgumentException();
            }
        }
    }
}
