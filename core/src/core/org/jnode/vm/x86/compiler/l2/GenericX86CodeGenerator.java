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
 
package org.jnode.vm.x86.compiler.l2;

import static org.jnode.vm.compiler.ir.AddressingMode.CONSTANT;
import static org.jnode.vm.compiler.ir.AddressingMode.REGISTER;
import static org.jnode.vm.compiler.ir.AddressingMode.STACK;

import org.jnode.assembler.Label;
import org.jnode.assembler.x86.X86Assembler;
import org.jnode.assembler.x86.X86Constants;
import org.jnode.assembler.x86.X86Register;
import org.jnode.assembler.x86.X86Register.GPR;
import org.jnode.vm.compiler.ir.AddressingMode;
import org.jnode.vm.compiler.ir.CodeGenerator;
import org.jnode.vm.compiler.ir.Constant;
import org.jnode.vm.compiler.ir.IntConstant;
import org.jnode.vm.compiler.ir.Location;
import org.jnode.vm.compiler.ir.Operand;
import org.jnode.vm.compiler.ir.RegisterLocation;
import org.jnode.vm.compiler.ir.RegisterPool;
import org.jnode.vm.compiler.ir.StackLocation;
import org.jnode.vm.compiler.ir.Variable;
import org.jnode.vm.compiler.ir.quad.BinaryOperation;
import org.jnode.vm.compiler.ir.quad.BranchCondition;
import org.jnode.vm.compiler.ir.quad.ConditionalBranchQuad;
import org.jnode.vm.compiler.ir.quad.ConstantRefAssignQuad;
import org.jnode.vm.compiler.ir.quad.UnaryOperation;
import org.jnode.vm.compiler.ir.quad.UnaryQuad;
import org.jnode.vm.compiler.ir.quad.UnconditionalBranchQuad;
import org.jnode.vm.compiler.ir.quad.VarReturnQuad;
import org.jnode.vm.compiler.ir.quad.VariableRefAssignQuad;
import org.jnode.vm.compiler.ir.quad.VoidReturnQuad;

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

    private Variable<T>[] spilledVariables;

    private X86Assembler os;

    private int displacement;

    private String labelPrefix;

    private String instrLabelPrefix;

    private Label[] addressLabels;

    private final RegisterPool<T> registerPool;

    /**
     * Initialize this instance
     */
    public GenericX86CodeGenerator(X86Assembler x86Stream, RegisterPool<T> pool, int lenght) {
        CodeGenerator.setCodeGenerator(this);
        this.registerPool = pool;
        this.os = x86Stream;

        labelPrefix = "label";
        instrLabelPrefix = labelPrefix + "_bci_";
        addressLabels = new Label[lenght];
    }

    public final Label getInstrLabel(int address) {
        Label l = addressLabels[address];
        if (l == null) {
            l = new Label(instrLabelPrefix + address);
            addressLabels[address] = l;
        }
        return l;
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#getRegisterPool()
     */
    public RegisterPool<T> getRegisterPool() {
        return registerPool;
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#supports3AddrOps()
     */
    public boolean supports3AddrOps() {
        return false;
    }

    /**
     * @param vars
     * @param nArgs
     */
    public void setArgumentVariables(Variable<T>[] vars, int nArgs) {
        displacement = 0;
        for (int i = 0; i < nArgs; i += 1) {
            // TODO this might not be right, check with Ewout
            displacement = vars[i].getIndex() * 4;
            vars[i].setLocation(new StackLocation<T>(displacement));
        }
        // not sure how big the last arg is...
        displacement += 8;
    }

    /**
     * @param variables
     */
    public void setSpilledVariables(Variable<T>[] variables) {
        this.spilledVariables = variables;
        int n = spilledVariables.length;
        for (int i = 0; i < n; i += 1) {
            StackLocation<T> loc = (StackLocation<T>) spilledVariables[i]
                .getLocation();
            loc.setDisplacement(displacement);
            switch (spilledVariables[i].getType()) {
                case Operand.BYTE:
                case Operand.CHAR:
                case Operand.SHORT:
                case Operand.INT:
                case Operand.FLOAT:
                case Operand.REFERENCE:
                    displacement += 4;
                    break;
                case Operand.LONG:
                case Operand.DOUBLE:
                    displacement += 8;
                    break;
            }
        }
    }

    /**
     *
     */
    public void emitHeader() {
        os.writePUSH(X86Register.EBP);
        // os.writePUSH(context.getMagic());
        // os.writePUSH(0); // PC, which is only used in interpreted methods
        /** EAX MUST contain the VmMethod structure upon entry of the method */
        // os.writePUSH(Register.EAX);
        os.writeMOV(X86Constants.BITS32, X86Register.EBP, X86Register.ESP);
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.quad.ConditionalBranchQuad)
     */
    public void generateCodeFor(ConditionalBranchQuad<T> quad) {
        throw new IllegalArgumentException("Unknown operation");
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.quad.ConstantRefAssignQuad)
     */
    public void generateCodeFor(ConstantRefAssignQuad<T> quad) {
        Variable<T> lhs = quad.getLHS();
        if (lhs.getAddressingMode() == REGISTER) {
            T reg1 = ((RegisterLocation<T>) lhs
                .getLocation()).getRegister();
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

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator
     * #generateCodeFor(org.jnode.vm.compiler.ir.quad.UnconditionalBranchQuad)
     */
    public void generateCodeFor(UnconditionalBranchQuad<T> quad) {
        checkLabel(quad.getAddress());
        os.writeJMP(getInstrLabel(quad.getTargetAddress()));
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.quad.VariableRefAssignQuad)
     */
    public void generateCodeFor(VariableRefAssignQuad<T> quad) {
        Variable<T> lhs = quad.getLHS();
        if (lhs.getAddressingMode() == REGISTER) {
            T reg1 = ((RegisterLocation<T>) lhs
                .getLocation()).getRegister();
            Operand<T> rhs = quad.getRHS();
            AddressingMode mode = rhs.getAddressingMode();
            if (mode == CONSTANT) {
                // TODO throw new IllegalArgumentException("Unknown operation");
                os.writeMOV_Const((GPR) reg1, ((IntConstant<T>) rhs).getValue());
            } else if (mode == REGISTER) {
                T reg2 = ((RegisterLocation<T>) ((Variable<T>) rhs)
                    .getLocation()).getRegister();
                os.writeMOV(X86Constants.BITS32, (GPR) reg1, (GPR) reg2);
            } else if (mode == STACK) {
                int disp2 = ((StackLocation<T>) ((Variable<T>) rhs).getLocation())
                    .getDisplacement();
                os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP,
                    disp2);
            }
        } else if (lhs.getAddressingMode() == STACK) {
            int disp1 = ((StackLocation<T>) lhs.getLocation()).getDisplacement();
            Operand<T> rhs = quad.getRHS();
            AddressingMode mode = rhs.getAddressingMode();
            if (mode == CONSTANT) {
                // TODO throw new IllegalArgumentException("Unknown operation");
                // todo os.writeMOV_Const(X86Register.EBP, disp1,
                // ((IntConstant)rhs).getValue());
            } else if (mode == REGISTER) {
                T reg2 = ((RegisterLocation<T>) ((Variable<T>) rhs)
                    .getLocation()).getRegister();
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                    (GPR) reg2);
            } else if (mode == STACK) {
                // int disp2 = ((StackLocation) ((Variable)
                // rhs).getLocation()).getDisplacement();
                throw new IllegalArgumentException("Unknown operation");
            }
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.quad.VarReturnQuad)
     */
    public void generateCodeFor(VarReturnQuad<T> quad) {
        checkLabel(quad.getAddress());
        Operand<T> op = quad.getOperand();
        // TODO must deal with other types, see else case also
        if (op instanceof IntConstant) {
            IntConstant<T> iconst = (IntConstant<T>) op;
            os.writeMOV_Const(X86Register.EAX, iconst.getValue());
        } else if (op instanceof Variable) {
            Variable<T> var = (Variable<T>) op;
            Location<T> loc = var.getLocation();
            if (loc instanceof RegisterLocation) {
                RegisterLocation<T> regLoc = (RegisterLocation<T>) loc;
                GPR src = (GPR) regLoc.getRegister();
                if (!src.equals(X86Register.EAX)) {
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, src);
                }
            } else {
                StackLocation<T> stackLoc = (StackLocation<T>) loc;
                os.writeMOV(X86Constants.BITS32, X86Register.EAX,
                    X86Register.EBP, stackLoc.getDisplacement());
            }
        }

        // TODO: hack for testing
        os.writeMOV(X86Constants.BITS32, X86Register.ESP, X86Register.EBP);
        os.writePOP(X86Register.EBP);

        os.writeRET();
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.quad.VoidReturnQuad)
     */
    public void generateCodeFor(VoidReturnQuad<T> quad) {
        checkLabel(quad.getAddress());

        // TODO: hack for testing
        os.writeMOV(X86Constants.BITS32, X86Register.ESP, X86Register.EBP);
        os.writePOP(X86Register.EBP);

        os.writeRET();
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.quad.UnaryQuad,
     *      java.lang.Object, int, org.jnode.vm.compiler.ir.Constant)
     */
    public void generateCodeFor(UnaryQuad<T> quad, Object lhsReg, UnaryOperation operation,
                                Constant<T> con) {
        throw new IllegalArgumentException("Constants should be folded");
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.quad.UnaryQuad,
     *      java.lang.Object, int, java.lang.Object)
     */
    public void generateCodeFor(UnaryQuad<T> quad, Object lhsReg, UnaryOperation operation,
                                Object rhsReg) {
        checkLabel(quad.getAddress());
        switch (operation) {
            case I2L:
                throw new IllegalArgumentException("Unknown operation");

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
                throw new IllegalArgumentException("Unknown operation");

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
                throw new IllegalArgumentException("Unknown operation");

            case I2B:
                os.writeMOVSX((GPR) lhsReg, (GPR) rhsReg, BYTESIZE);
                break;

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
                throw new IllegalArgumentException("Unknown operation");

            case FNEG:
                os.writePUSH((GPR) rhsReg);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeFCHS();
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) lhsReg);
                break;

            case DNEG:
            default:
                throw new IllegalArgumentException("Unknown operation");
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.quad.UnaryQuad,
     *      java.lang.Object, int, int)
     */
    public void generateCodeFor(UnaryQuad<T> quad, Object lhsReg, UnaryOperation operation,
                                int rhsDisp) {
        checkLabel(quad.getAddress());
        switch (operation) {
            case I2L:
                throw new IllegalArgumentException("Unknown operation");

            case I2F:
                os.writePUSH(X86Register.EBP, rhsDisp);
                os.writeFILD32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) lhsReg);
                break;

            case I2D:
            case L2I:
            case L2F:
            case L2D:
                throw new IllegalArgumentException("Unknown operation");

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
                throw new IllegalArgumentException("Unknown operation");

            case I2B:
                os.writeMOVSX((GPR) lhsReg, X86Register.EBP, rhsDisp, BYTESIZE);
                break;

            case I2C:
                os.writeMOVZX((GPR) lhsReg, X86Register.EBP, rhsDisp, WORDSIZE);
                break;

            case I2S:
                os.writeMOVSX((GPR) lhsReg, X86Register.EBP, rhsDisp, WORDSIZE);
                break;

            case INEG:
                os.writeMOV(X86Constants.BITS32, (GPR) lhsReg, X86Register.EBP,
                    rhsDisp);
                os.writeNEG((GPR) lhsReg);
                break;

            case LNEG:
                throw new IllegalArgumentException("Unknown operation");

            case FNEG:
                os.writePUSH(X86Register.EBP, rhsDisp);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeFCHS();
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) lhsReg);
                break;

            case DNEG:
            default:
                throw new IllegalArgumentException("Unknown operation");
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.quad.UnaryQuad,
     *      int, int, java.lang.Object)
     */
    public void generateCodeFor(UnaryQuad<T> quad, int lhsDisp, UnaryOperation operation,
                                Object rhsReg) {
        checkLabel(quad.getAddress());
        switch (operation) {
            case I2L:
                throw new IllegalArgumentException("Unknown operation");

            case I2F:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, lhsDisp,
                    (GPR) rhsReg);
                os.writeFILD32(X86Register.EBP, lhsDisp);
                os.writeFSTP32(X86Register.EBP, lhsDisp);
                break;

            case I2D:
            case L2I:
            case L2F:
            case L2D:
            case F2I:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, lhsDisp,
                    (GPR) rhsReg);
                os.writeFLD32(X86Register.EBP, lhsDisp);
                os.writeFISTP32(X86Register.EBP, lhsDisp);
                break;

            case F2L:
            case F2D:
            case D2I:
            case D2L:
            case D2F:
                throw new IllegalArgumentException("Unknown operation");

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
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, lhsDisp,
                    (GPR) rhsReg);
                os.writeNEG(BITS32, X86Register.EBP, lhsDisp);
                break;

            case LNEG:
                throw new IllegalArgumentException("Unknown operation");

            case FNEG:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, lhsDisp,
                    (GPR) rhsReg);
                os.writeFLD32(X86Register.EBP, lhsDisp);
                os.writeFCHS();
                os.writeFSTP32(X86Register.EBP, lhsDisp);
                break;

            case DNEG:
            default:
                throw new IllegalArgumentException("Unknown operation");
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.quad.UnaryQuad,
     *      int, int, int)
     */
    public void generateCodeFor(UnaryQuad<T> quad, int lhsDisp, UnaryOperation operation,
                                int rhsDisp) {
        checkLabel(quad.getAddress());
        switch (operation) {
            case I2L:
                throw new IllegalArgumentException("Unknown operation");

            case I2F:
                os.writeFILD32(X86Register.EBP, rhsDisp);
                os.writeFSTP32(X86Register.EBP, lhsDisp);
                break;

            case I2D:
            case L2I:
            case L2F:
            case L2D:
                throw new IllegalArgumentException("Unknown operation");

            case F2I:
                os.writeFLD32(X86Register.EBP, rhsDisp);
                os.writeFISTP32(X86Register.EBP, lhsDisp);
                break;

            case F2L:
            case F2D:
            case D2I:
            case D2L:
            case D2F:
                throw new IllegalArgumentException("Unknown operation");

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
                throw new IllegalArgumentException("Unknown operation");

            case FNEG:
                os.writeFLD32(X86Register.EBP, rhsDisp);
                os.writeFCHS();
                os.writeFSTP32(X86Register.EBP, lhsDisp);
                break;

            case DNEG:
            default:
                throw new IllegalArgumentException("Unknown operation");
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.quad.UnaryQuad,
     *      int, int, org.jnode.vm.compiler.ir.Constant)
     */
    public void generateCodeFor(UnaryQuad<T> quad, int lhsDisp, UnaryOperation operation,
                                Constant<T> con) {
        throw new IllegalArgumentException("Constants should be folded");
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(java.lang.Object,
     *      org.jnode.vm.compiler.ir.Constant, int,
     *      org.jnode.vm.compiler.ir.Constant)
     */
    public void generateBinaryOP(T reg1, Constant<T> c2,
                                 BinaryOperation operation, Constant<T> c3) {
        throw new IllegalArgumentException("Constants should be folded");
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(java.lang.Object,
     *      org.jnode.vm.compiler.ir.Constant, int, java.lang.Object)
     */
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
                    os.writeMOV(X86Constants.BITS32, X86Register.EDX,
                        X86Register.EAX);
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
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX,
                        X86Register.EDX);
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
                throw new IllegalArgumentException("Unknown operation");

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
                throw new IllegalArgumentException("Unknown operation");
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(java.lang.Object,
     *      org.jnode.vm.compiler.ir.Constant<T>, int, int)
     */
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
                    os.writeMOV(X86Constants.BITS32, X86Register.EDX,
                        X86Register.EAX);
                    os.writePOP(X86Register.EAX);
                    os.writeADD(X86Register.ESP, 4);
                } else {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EAX);
                    os.writePOP(X86Register.EAX);
                    os.writePOP(X86Register.EDX);
                }
                break;

            case IMUL:
                os.writeIMUL_3((GPR) reg1, X86Register.EBP, disp3, iconst2
                    .getValue());
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
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX,
                        X86Register.EDX);
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
                os.writeMOV(X86Constants.BITS32, X86Register.ECX, X86Register.EBP,
                    disp3);
                os.writeSAL_CL((GPR) reg1);
                os.writePOP(X86Register.ECX);
                break;

            case ISHR: // not supported
                os.writeMOV_Const((GPR) reg1, iconst2.getValue());
                os.writePUSH(X86Register.ECX);
                os.writeMOV(X86Constants.BITS32, X86Register.ECX, X86Register.EBP,
                    disp3);
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
                os.writeMOV(X86Constants.BITS32, X86Register.ECX, X86Register.EBP,
                    disp3);
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
                throw new IllegalArgumentException("Unknown operation");

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
                throw new IllegalArgumentException("Unknown operation");
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(java.lang.Object,
     *      java.lang.Object, int, org.jnode.vm.compiler.ir.Constant<T>)
     */
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
                    os.writeMOV(X86Constants.BITS32, X86Register.EDX,
                        X86Register.EAX);
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
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX,
                        X86Register.EDX);
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
                throw new IllegalArgumentException("Unknown operation");

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
                throw new IllegalArgumentException("Unknown operation");
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(java.lang.Object,
     *      java.lang.Object, int, java.lang.Object)
     */
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
                    os.writeMOV(X86Constants.BITS32, X86Register.EDX,
                        X86Register.EAX);
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
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX,
                        X86Register.EDX);
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
                throw new IllegalArgumentException("Unknown operation");

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
                throw new IllegalArgumentException("Unknown operation");
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(java.lang.Object,
     *      java.lang.Object, int, int)
     */
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
                    os.writeMOV(X86Constants.BITS32, X86Register.EDX,
                        X86Register.EAX);
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
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX,
                        X86Register.EDX);
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
                os.writeMOV(X86Constants.BITS32, X86Register.ECX, X86Register.EBP,
                    disp3);
                os.writeSAL_CL((GPR) reg1);
                os.writePOP(X86Register.ECX);
                break;

            case ISHR: // needs CL
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, (GPR) reg2);
                }
                os.writePUSH(X86Register.ECX);
                os.writeMOV(X86Constants.BITS32, X86Register.ECX, X86Register.EBP,
                    disp3);
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
                os.writeMOV(X86Constants.BITS32, X86Register.ECX, X86Register.EBP,
                    disp3);
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
                throw new IllegalArgumentException("Unknown operation");

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
                throw new IllegalArgumentException("Unknown operation");
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(java.lang.Object,
     *      int, int, org.jnode.vm.compiler.ir.Constant<T>)
     */
    public void generateBinaryOP(T reg1, int disp2,
                                 BinaryOperation operation, Constant<T> c3) {
        IntConstant<T> iconst3 = (IntConstant<T>) c3;
        switch (operation) {

            case IADD:
                os
                    .writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP,
                        disp2);
                os.writeADD((GPR) reg1, iconst3.getValue());
                break;

            case IAND:
                os
                    .writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP,
                        disp2);
                os.writeAND((GPR) reg1, iconst3.getValue());
                break;

            case IDIV: // needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writePUSH(iconst3.getValue());
                os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EBP,
                    disp2);
                os.writeCDQ(BITS32);
                os.writeIDIV_EAX(BITS32, X86Register.ESP, 0);
                os.writePOP(X86Register.EDX);
                if (reg1 == X86Register.EAX) {
                    os.writePOP(X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                } else if (reg1 == X86Register.EDX) {
                    os.writeMOV(X86Constants.BITS32, X86Register.EDX,
                        X86Register.EAX);
                    os.writePOP(X86Register.EAX);
                    os.writeADD(X86Register.ESP, 4);
                } else {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EAX);
                    os.writePOP(X86Register.EAX);
                    os.writePOP(X86Register.EDX);
                }
                break;

            case IMUL:
                os.writeIMUL_3((GPR) reg1, X86Register.EBP, disp2, iconst3
                    .getValue());
                break;

            case IOR:
                os
                    .writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP,
                        disp2);
                os.writeOR((GPR) reg1, iconst3.getValue());
                break;

            case IREM: // needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writePUSH(iconst3.getValue());
                os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EBP,
                    disp2);
                os.writeCDQ(BITS32);
                os.writeIDIV_EAX(BITS32, X86Register.ESP, 0);
                os.writePOP(X86Register.EAX);
                if (reg1 == X86Register.EDX) {
                    os.writePOP(X86Register.EAX);
                    os.writeADD(X86Register.ESP, 4);
                } else if (reg1 == X86Register.EAX) {
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX,
                        X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                } else {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EDX);
                    os.writePOP(X86Register.EAX);
                    os.writePOP(X86Register.EDX);
                }
                break;

            case ISHL: // needs CL
                os
                    .writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP,
                        disp2);
                os.writeSAL((GPR) reg1, iconst3.getValue());
                break;

            case ISHR: // needs CL
                os
                    .writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP,
                        disp2);
                os.writeSAR((GPR) reg1, iconst3.getValue());
                break;

            case ISUB:
                os
                    .writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP,
                        disp2);
                os.writeSUB((GPR) reg1, iconst3.getValue());
                break;

            case IUSHR: // needs CL
                os
                    .writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP,
                        disp2);
                os.writeSHR((GPR) reg1, iconst3.getValue());
                break;

            case IXOR:
                os
                    .writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP,
                        disp2);
                os.writeXOR((GPR) reg1, iconst3.getValue());
                break;

            case DADD:
            case DDIV:
            case DMUL:
            case DREM:
            case DSUB:
                throw new IllegalArgumentException("Unknown operation");

            case FADD:
                os.writePUSH(iconst3.getValue());
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFADD32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) reg1);
                break;

            case FDIV:
                os.writePUSH(iconst3.getValue());
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFDIV32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) reg1);
                break;

            case FMUL:
                os.writePUSH(iconst3.getValue());
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFMUL32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((GPR) reg1);
                break;

            case FREM:
                os.writePUSH(iconst3.getValue());
                os.writeFLD32(X86Register.ESP, 0);
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFPREM();
                os.writeFSTP32(X86Register.ESP, 0);
                os.writeFFREE(X86Register.ST0);
                os.writePOP((GPR) reg1);
                break;

            case FSUB:
                os.writePUSH(iconst3.getValue());
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
                throw new IllegalArgumentException("Unknown operation");
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(java.lang.Object,
     *      int, int, java.lang.Object)
     */
    public void generateBinaryOP(T reg1, int disp2,
                                 BinaryOperation operation, T reg3) {
        switch (operation) {
            case IADD:
                os
                    .writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP,
                        disp2);
                os.writeADD((GPR) reg1, (GPR) reg3);
                break;

            case IAND:
                os
                    .writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP,
                        disp2);
                os.writeAND((GPR) reg1, (GPR) reg3);
                break;

            case IDIV: // needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EBP,
                    disp2);
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
                    os.writeMOV(X86Constants.BITS32, X86Register.EDX,
                        X86Register.EAX);
                    os.writePOP(X86Register.EAX);
                    os.writeADD(X86Register.ESP, 4);
                } else {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EAX);
                    os.writePOP(X86Register.EAX);
                    os.writePOP(X86Register.EDX);
                }
                break;

            case IMUL:
                os
                    .writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP,
                        disp2);
                os.writeIMUL((GPR) reg1, (GPR) reg3);
                break;

            case IOR:
                os
                    .writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP,
                        disp2);
                os.writeOR((GPR) reg1, (GPR) reg3);
                break;

            case IREM: // needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EBP,
                    disp2);
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
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX,
                        X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                } else {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EDX);
                    os.writePOP(X86Register.EAX);
                    os.writePOP(X86Register.EDX);
                }
                break;

            case ISHL: // needs CL
                os
                    .writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP,
                        disp2);
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
                os
                    .writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP,
                        disp2);
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
                os
                    .writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP,
                        disp2);
                os.writeSUB((GPR) reg1, (GPR) reg3);
                break;

            case IUSHR: // needs CL
                os
                    .writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP,
                        disp2);
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
                os
                    .writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP,
                        disp2);
                os.writeXOR((GPR) reg1, (GPR) reg3);
                break;

            case DADD:
            case DDIV:
            case DMUL:
            case DREM:
            case DSUB:
                throw new IllegalArgumentException("Unknown operation");

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
                throw new IllegalArgumentException("Unknown operation");
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(java.lang.Object,
     *      int, int, int)
     */
    public void generateBinaryOP(T reg1, int disp2,
                                 BinaryOperation operation, int disp3) {
        switch (operation) {
            case IADD:
                os
                    .writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP,
                        disp2);
                os.writeADD((GPR) reg1, X86Register.EBP, disp3);
                break;

            case IAND:
                os
                    .writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP,
                        disp2);
                os.writeAND((GPR) reg1, X86Register.EBP, disp3);
                break;

            case IDIV: // needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EBP,
                    disp2);
                os.writeCDQ(BITS32);
                os.writeIDIV_EAX(BITS32, X86Register.EBP, disp3);
                if (reg1 == X86Register.EAX) {
                    os.writePOP(X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                } else if (reg1 == X86Register.EDX) {
                    os.writeMOV(X86Constants.BITS32, X86Register.EDX,
                        X86Register.EAX);
                    os.writePOP(X86Register.EAX);
                    os.writeADD(X86Register.ESP, 4);
                } else {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EAX);
                    os.writePOP(X86Register.EAX);
                    os.writePOP(X86Register.EDX);
                }
                break;

            case IMUL:
                os
                    .writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP,
                        disp2);
                os.writeIMUL((GPR) reg1, X86Register.EBP, disp3);
                break;

            case IOR: // not supported
                os
                    .writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP,
                        disp2);
                os.writeOR((GPR) reg1, X86Register.EBP, disp3);
                break;

            case IREM: // needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EBP,
                    disp2);
                os.writeCDQ(BITS32);
                os.writeIDIV_EAX(BITS32, X86Register.EBP, disp3);
                if (reg1 == X86Register.EDX) {
                    os.writePOP(X86Register.EAX);
                    os.writeADD(X86Register.ESP, 4);
                } else if (reg1 == X86Register.EAX) {
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX,
                        X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                } else {
                    os.writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EDX);
                    os.writePOP(X86Register.EAX);
                    os.writePOP(X86Register.EDX);
                }
                break;

            case ISHL: // needs CL
                os
                    .writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP,
                        disp2);
                os.writePUSH(X86Register.ECX);
                os.writeMOV(X86Constants.BITS32, X86Register.ECX, X86Register.EBP,
                    disp3);
                os.writeSAL_CL((GPR) reg1);
                os.writePOP(X86Register.ECX);
                break;

            case ISHR: // needs CL
                os
                    .writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP,
                        disp2);
                os.writePUSH(X86Register.ECX);
                os.writeMOV(X86Constants.BITS32, X86Register.ECX, X86Register.EBP,
                    disp3);
                os.writeSAR_CL((GPR) reg1);
                os.writePOP(X86Register.ECX);
                break;

            case ISUB:
                os
                    .writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP,
                        disp2);
                os.writeSUB((GPR) reg1, X86Register.EBP, disp3);
                break;

            case IUSHR: // needs CL
                os
                    .writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP,
                        disp2);
                os.writePUSH(X86Register.ECX);
                os.writeMOV(X86Constants.BITS32, X86Register.ECX, X86Register.EBP,
                    disp3);
                os.writeSHR_CL((GPR) reg1);
                os.writePOP(X86Register.ECX);
                break;

            case IXOR:
                os
                    .writeMOV(X86Constants.BITS32, (GPR) reg1, X86Register.EBP,
                        disp2);
                os.writeXOR((GPR) reg1, X86Register.EBP, disp3);
                break;

            case DADD:
            case DDIV:
            case DMUL:
            case DREM:
            case DSUB:
                throw new IllegalArgumentException("Unknown operation");

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
                throw new IllegalArgumentException("Unknown operation");
        }
    }

    // / WE should not get to this method
    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(int,
     *      org.jnode.vm.compiler.ir.Constant<T>, int,
     *      org.jnode.vm.compiler.ir.Constant<T>)
     */
    public void generateBinaryOP(int disp1, Constant<T> c2,
                                 BinaryOperation operation, Constant<T> c3) {
        throw new IllegalArgumentException("Constants should be folded");
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(int,
     *      org.jnode.vm.compiler.ir.Constant<T>, int, java.lang.Object)
     */
    public void generateBinaryOP(int disp1, Constant<T> c2,
                                 BinaryOperation operation, T reg3) {
        IntConstant<T> iconst2 = (IntConstant<T>) c2;
        switch (operation) {
            case IADD:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2
                    .getValue());
                os.writeADD(X86Register.EBP, disp1, (GPR) reg3);
                break;

            case IAND:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2
                    .getValue());
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
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                    X86Register.EAX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case IMUL:
                os.writePUSH((GPR) reg3);
                os.writeIMUL_3((GPR) reg3, (GPR) reg3, iconst2.getValue());
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg3);
                os.writePOP((GPR) reg3);
                break;

            case IOR:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2
                    .getValue());
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
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                    X86Register.EDX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case ISHL: // needs CL
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2
                    .getValue());
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
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2
                    .getValue());
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
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2
                    .getValue());
                os.writeSUB(X86Register.EBP, disp1, (GPR) reg3);
                break;

            case IUSHR: // needs CL
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2
                    .getValue());
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
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2
                    .getValue());
                os.writeXOR(X86Register.EBP, disp1, (GPR) reg3);
                break;

            case DADD:
            case DDIV:
            case DMUL:
            case DREM:
            case DSUB:
                throw new IllegalArgumentException("Unknown operation");

            case FADD:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2
                    .getValue());
                os.writeFLD32(X86Register.EBP, disp1);
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg3);
                os.writeFADD32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FDIV:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2
                    .getValue());
                os.writeFLD32(X86Register.EBP, disp1);
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg3);
                os.writeFDIV32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FMUL:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2
                    .getValue());
                os.writeFLD32(X86Register.EBP, disp1);
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg3);
                os.writeFMUL32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FREM:
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg3);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2
                    .getValue());
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFPREM();
                os.writeFSTP32(X86Register.EBP, disp1);
                os.writeFFREE(X86Register.ST0);
                break;

            case FSUB:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2
                    .getValue());
                os.writeFLD32(X86Register.EBP, disp1);
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg3);
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
                throw new IllegalArgumentException("Unknown operation");
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(int,
     *      org.jnode.vm.compiler.ir.Constant<T>, int, int)
     */
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
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                    X86Register.EAX);
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
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                    X86Register.EDX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case ISHL:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2
                    .getValue());
                os.writePUSH(X86Register.ECX);
                os.writeMOV(X86Constants.BITS32, X86Register.ECX, X86Register.EBP,
                    disp3);
                os.writeSAL_CL(BITS32, X86Register.EBP, disp1);
                os.writePOP(X86Register.ECX);
                break;

            case ISHR:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2
                    .getValue());
                os.writePUSH(X86Register.ECX);
                os.writeMOV(X86Constants.BITS32, X86Register.ECX, X86Register.EBP,
                    disp3);
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
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2
                    .getValue());
                os.writePUSH(X86Register.ECX);
                os.writeMOV(X86Constants.BITS32, X86Register.ECX, X86Register.EBP,
                    disp3);
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
                throw new IllegalArgumentException("Unknown operation");

            case FADD:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2
                    .getValue());
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFADD32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FDIV:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2
                    .getValue());
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFDIV32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FMUL:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2
                    .getValue());
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFMUL32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FREM:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2
                    .getValue());
                os.writeFSUB32(X86Register.EBP, disp3);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFPREM();
                os.writeFSTP32(X86Register.EBP, disp1);
                os.writeFFREE(X86Register.ST0);
                break;

            case FSUB:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst2
                    .getValue());
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
                throw new IllegalArgumentException("Unknown operation");
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(int,
     *      java.lang.Object, int, org.jnode.vm.compiler.ir.Constant<T>)
     */
    public void generateBinaryOP(int disp1, T reg2,
                                 BinaryOperation operation, Constant<T> c3) {
        IntConstant<T> iconst3 = (IntConstant<T>) c3;
        switch (operation) {

            case IADD:
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
                os.writeADD(BITS32, X86Register.EBP, disp1, iconst3.getValue());
                break;

            case IAND:
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
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
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                    X86Register.EAX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case IMUL:
                os.writePUSH((GPR) reg2);
                os.writeIMUL_3((GPR) reg2, (GPR) reg2, iconst3.getValue());
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
                os.writePOP((GPR) reg2);
                break;

            case IOR:
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
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
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                    X86Register.EDX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case ISHL: // needs CL
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
                os.writeSAL(BITS32, X86Register.EBP, disp1, iconst3.getValue());
                break;

            case ISHR: // needs CL
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
                os.writeSAR(BITS32, X86Register.EBP, disp1, iconst3.getValue());
                break;

            case ISUB:
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
                os.writeSUB(BITS32, X86Register.EBP, disp1, iconst3.getValue());
                break;

            case IUSHR: // needs CL
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
                os.writeSHR(BITS32, X86Register.EBP, disp1, iconst3.getValue());
                break;

            case IXOR:
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
                os.writeXOR(BITS32, X86Register.EBP, disp1, iconst3.getValue());
                break;

            case DADD:
            case DDIV:
            case DMUL:
            case DREM:
            case DSUB:
                throw new IllegalArgumentException("Unknown operation");

            case FADD:
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst3
                    .getValue());
                os.writeFADD32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FDIV:
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst3
                    .getValue());
                os.writeFDIV32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FMUL:
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst3
                    .getValue());
                os.writeFMUL32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FREM:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst3
                    .getValue());
                os.writeFLD32(X86Register.EBP, disp1);
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFPREM();
                os.writeFSTP32(X86Register.EBP, disp1);
                os.writeFFREE(X86Register.ST0);
                break;

            case FSUB:
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst3
                    .getValue());
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
                throw new IllegalArgumentException("Unknown operation");
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(int,
     *      java.lang.Object, int, java.lang.Object)
     */
    public void generateBinaryOP(int disp1, T reg2,
                                 BinaryOperation operation, T reg3) {
        switch (operation) {
            case IADD:
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
                os.writeADD(X86Register.EBP, disp1, (GPR) reg3);
                break;

            case IAND:
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
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
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                    X86Register.EAX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case IMUL:
                os.writePUSH((GPR) reg2);
                os.writeIMUL((GPR) reg2, (GPR) reg3);
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
                os.writePOP((GPR) reg2);
                break;

            case IOR:
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
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
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                    X86Register.EDX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case ISHL: // needs CL
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
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
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
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
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
                os.writeSUB(X86Register.EBP, disp1, (GPR) reg3);
                break;

            case IUSHR: // needs CL
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
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
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
                os.writeXOR(X86Register.EBP, disp1, (GPR) reg3);
                break;

            case DADD:
            case DDIV:
            case DMUL:
            case DREM:
            case DSUB:
                throw new IllegalArgumentException("Unknown operation");

            case FADD:
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
                os.writeFLD32(X86Register.EBP, disp1);
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg3);
                os.writeFADD32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FDIV:
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
                os.writeFLD32(X86Register.EBP, disp1);
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg3);
                os.writeFDIV32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FMUL:
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
                os.writeFLD32(X86Register.EBP, disp1);
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg3);
                os.writeFMUL32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FREM:
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg3);
                os.writeFLD32(X86Register.EBP, disp1);
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFPREM();
                os.writeFSTP32(X86Register.EBP, disp1);
                os.writeFFREE(X86Register.ST0);
                break;

            case FSUB:
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
                os.writeFLD32(X86Register.EBP, disp1);
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg3);
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
                throw new IllegalArgumentException("Unknown operation");
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(int,
     *      java.lang.Object, int, int)
     */
    public void generateBinaryOP(int disp1, T reg2,
                                 BinaryOperation operation, int disp3) {
        switch (operation) {
            case IADD:
                os.writePUSH((GPR) reg2);
                os.writeADD((GPR) reg2, X86Register.EBP, disp3);
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
                os.writePOP((GPR) reg2);
                break;

            case IAND:
                os.writePUSH((GPR) reg2);
                os.writeAND((GPR) reg2, X86Register.EBP, disp3);
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
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
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                    X86Register.EAX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case IMUL:
                os.writePUSH((GPR) reg2);
                os.writeIMUL((GPR) reg2, X86Register.EBP, disp3);
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
                os.writePOP((GPR) reg2);
                break;

            case IOR:
                os.writePUSH((GPR) reg2);
                os.writeOR((GPR) reg2, X86Register.EBP, disp3);
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
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
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                    X86Register.EDX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case ISHL: // needs CL
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
                os.writePUSH(X86Register.ECX);
                os.writeMOV(X86Constants.BITS32, X86Register.ECX, X86Register.EBP,
                    disp3);
                os.writeSAL_CL(BITS32, X86Register.EBP, disp1);
                os.writePOP(X86Register.ECX);
                break;

            case ISHR: // needs CL
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
                os.writePUSH(X86Register.ECX);
                os.writeMOV(X86Constants.BITS32, X86Register.ECX, X86Register.EBP,
                    disp3);
                os.writeSAR_CL(BITS32, X86Register.EBP, disp1);
                os.writePOP(X86Register.ECX);
                break;

            case ISUB:
                os.writePUSH((GPR) reg2);
                os.writeSUB((GPR) reg2, X86Register.EBP, disp3);
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
                os.writePOP((GPR) reg2);
                break;

            case IUSHR: // needs CL
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
                os.writePUSH(X86Register.ECX);
                os.writeMOV(X86Constants.BITS32, X86Register.ECX, X86Register.EBP,
                    disp3);
                os.writeSHR_CL(BITS32, X86Register.EBP, disp1);
                os.writePOP(X86Register.ECX);
                break;

            case IXOR:
                os.writePUSH((GPR) reg2);
                os.writeXOR((GPR) reg2, X86Register.EBP, disp3);
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
                os.writePOP((GPR) reg2);
                break;

            case DADD:
            case DDIV:
            case DMUL:
            case DREM:
            case DSUB:
                throw new IllegalArgumentException("Unknown operation");

            case FADD:
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFADD32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FDIV:
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFDIV32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FMUL:
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFMUL32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FREM:
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
                os.writeFLD32(X86Register.EBP, disp3);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFPREM();
                os.writeFSTP32(X86Register.EBP, disp1);
                os.writeFFREE(X86Register.ST0);
                break;

            case FSUB:
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg2);
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
                throw new IllegalArgumentException("Unknown operation");
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(int, int,
     *      int, org.jnode.vm.compiler.ir.Constant<T>)
     */
    public void generateBinaryOP(int disp1, int disp2,
                                 BinaryOperation operation, Constant<T> c3) {
        IntConstant<T> iconst3 = (IntConstant<T>) c3;
        switch (operation) {
            case IADD: // not supported due to the move bellow
                if (disp1 != disp2) {
                    os.writePUSH(X86Register.EBP, disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writeADD(BITS32, X86Register.EBP, disp1, iconst3.getValue());
                break;

            case IAND:
                if (disp1 != disp2) {
                    os.writePUSH(X86Register.EBP, disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writeAND(BITS32, X86Register.EBP, disp1, iconst3.getValue());
                break;

            case IDIV: // needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writePUSH(iconst3.getValue());
                os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EBP,
                    disp2);
                os.writeCDQ(BITS32);
                os.writeIDIV_EAX(BITS32, X86Register.ESP, 0);
                os.writePOP(X86Register.EDX);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                    X86Register.EAX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case IMUL:
                os.writePUSH(SR1);
                os.writeIMUL_3(SR1, X86Register.EBP, disp2, iconst3.getValue());
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, SR1);
                os.writePOP(SR1);
                break;

            case IOR:
                if (disp1 != disp2) {
                    os.writePUSH(X86Register.EBP, disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writeOR(BITS32, X86Register.EBP, disp1, iconst3.getValue());
                break;

            case IREM: // needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writePUSH(iconst3.getValue());
                os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EBP,
                    disp2);
                os.writeCDQ(BITS32);
                os.writeIDIV_EAX(BITS32, X86Register.ESP, 0);
                os.writePOP(X86Register.EAX);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                    X86Register.EDX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case ISHL: // needs CL
                if (disp1 != disp2) {
                    os.writePUSH(X86Register.EBP, disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writeSAL(BITS32, X86Register.EBP, disp1, iconst3.getValue());
                break;

            case ISHR: // needs CL
                if (disp1 != disp2) {
                    os.writePUSH(X86Register.EBP, disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writeSAR(BITS32, X86Register.EBP, disp1, iconst3.getValue());
                break;

            case ISUB: // not supported
                if (disp1 != disp2) {
                    os.writePUSH(X86Register.EBP, disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writeSUB(BITS32, X86Register.EBP, disp1, iconst3.getValue());
                break;

            case IUSHR: // needs CL
                if (disp1 != disp2) {
                    os.writePUSH(X86Register.EBP, disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writeSHR(BITS32, X86Register.EBP, disp1, iconst3.getValue());
                break;

            case IXOR: // not supported
                if (disp1 != disp2) {
                    os.writePUSH(X86Register.EBP, disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writeXOR(BITS32, X86Register.EBP, disp1, iconst3.getValue());
                break;

            case DADD:
            case DDIV:
            case DMUL:
            case DREM:
            case DSUB:
                throw new IllegalArgumentException("Unknown operation");

            case FADD:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst3
                    .getValue());
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFADD32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FDIV:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst3
                    .getValue());
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFDIV32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FMUL:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst3
                    .getValue());
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFMUL32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FREM:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst3
                    .getValue());
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFPREM();
                os.writeFSTP32(X86Register.EBP, disp1);
                os.writeFFREE(X86Register.ST0);
                break;

            case FSUB:
                os.writeMOV_Const(BITS32, X86Register.EBP, disp1, iconst3
                    .getValue());
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
                throw new IllegalArgumentException("Unknown operation");
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(int, int,
     *      int, java.lang.Object)
     */
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
                os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EBP,
                    disp2);
                os.writeCDQ(BITS32);
                if (reg3 == X86Register.EAX) {
                    os.writeIDIV_EAX(BITS32, X86Register.ESP, 0);
                } else if (reg3 == X86Register.EDX) {
                    os.writeIDIV_EAX(BITS32, X86Register.ESP, 4);
                } else {
                    os.writeIDIV_EAX((GPR) reg3);
                }
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                    X86Register.EAX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case IMUL:
                os.writePUSH((GPR) reg3);
                os.writeIMUL((GPR) reg3, X86Register.EBP, disp2);
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg3);
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
                os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EBP,
                    disp2);
                os.writeCDQ(BITS32);
                if (reg3 == X86Register.EAX) {
                    os.writeIDIV_EAX(BITS32, X86Register.ESP, 0);
                } else if (reg3 == X86Register.EDX) {
                    os.writeIDIV_EAX(BITS32, X86Register.ESP, 4);
                } else {
                    os.writeIDIV_EAX((GPR) reg3);
                }
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                    X86Register.EDX);
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
                throw new IllegalArgumentException("Unknown operation");

            case FADD:
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg3);
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFADD32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FDIV:
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg3);
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFDIV32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FMUL:
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg3);
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFMUL32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case FREM:
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg3);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFPREM();
                os.writeFSTP32(X86Register.EBP, disp1);
                os.writeFFREE(X86Register.ST0);
                break;

            case FSUB:
                os
                    .writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                        (GPR) reg3);
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
                throw new IllegalArgumentException("Unknown operation");
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(int, int,
     *      int, int)
     */
    public void generateBinaryOP(int disp1, int disp2,
                                 BinaryOperation operation, int disp3) {
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
                os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EBP,
                    disp2);
                os.writeCDQ(BITS32);
                os.writeIDIV_EAX(BITS32, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                    X86Register.EAX);
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
                os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EBP,
                    disp2);
                os.writeCDQ(BITS32);
                os.writeIDIV_EAX(BITS32, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1,
                    X86Register.EDX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case ISHL:
                if (disp1 != disp2) {
                    os.writePUSH(X86Register.EBP, disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writePUSH(X86Register.ECX);
                os.writeMOV(X86Constants.BITS32, X86Register.ECX, X86Register.EBP,
                    disp3);
                os.writeSAL_CL(BITS32, X86Register.EBP, disp1);
                os.writePOP(X86Register.ECX);
                break;

            case ISHR:
                if (disp1 != disp2) {
                    os.writePUSH(X86Register.EBP, disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writePUSH(X86Register.ECX);
                os.writeMOV(X86Constants.BITS32, X86Register.ECX, X86Register.EBP,
                    disp3);
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
                os.writeMOV(X86Constants.BITS32, X86Register.ECX, X86Register.EBP,
                    disp3);
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
                throw new IllegalArgumentException("Unknown operation");
        }
    }

    /** ******** BRANCHES ************************************** */
    /**
     * @param quad
     * @param condition
     * @param reg
     */
    public void generateCodeFor(ConditionalBranchQuad<T> quad, BranchCondition condition,
                                Object reg) {
        checkLabel(quad.getAddress());
        os.writeTEST((GPR) reg, (GPR) reg);
        generateJumpForUnaryCondition(quad, condition);
    }

    /**
     * @param quad
     * @param condition
     * @param disp
     */
    public void generateCodeFor(ConditionalBranchQuad<T> quad, BranchCondition condition,
                                int disp) {
        checkLabel(quad.getAddress());
        os.writeCMP_Const(BITS32, X86Register.EBP, disp, 0);
        generateJumpForUnaryCondition(quad, condition);
    }

    private void generateJumpForUnaryCondition(ConditionalBranchQuad<T> quad,
                                               BranchCondition condition) {
        switch (condition) {
            case IFEQ:
                os
                    .writeJCC(getInstrLabel(quad.getTargetAddress()),
                        X86Constants.JE);
                break;

            case IFNE:
                os.writeJCC(getInstrLabel(quad.getTargetAddress()),
                    X86Constants.JNE);
                break;

            case IFGT:
                os
                    .writeJCC(getInstrLabel(quad.getTargetAddress()),
                        X86Constants.JG);
                break;

            case IFGE:
                os.writeJCC(getInstrLabel(quad.getTargetAddress()),
                    X86Constants.JGE);
                break;

            case IFLT:
                os
                    .writeJCC(getInstrLabel(quad.getTargetAddress()),
                        X86Constants.JL);
                break;

            case IFLE:
                os.writeJCC(getInstrLabel(quad.getTargetAddress()),
                    X86Constants.JLE);
                break;

            case IFNULL:
                os
                    .writeJCC(getInstrLabel(quad.getTargetAddress()),
                        X86Constants.JE);
                break;

            case IFNONNULL:
                os.writeJCC(getInstrLabel(quad.getTargetAddress()),
                    X86Constants.JNE);
                break;

            default:
                throw new IllegalArgumentException("Unknown condition " + condition);
        }
    }

    /**
     * @param quad
     * @param condition
     * @param cons
     */
    public void generateCodeFor(ConditionalBranchQuad<T> quad, BranchCondition condition,
                                Constant<T> cons) {
        switch (condition) {
            case IFEQ:
            case IFNE:
            case IFGT:
            case IFGE:
            case IFLT:
            case IFLE:
            case IFNULL:
            case IFNONNULL:
            default:
                throw new IllegalArgumentException("Unknown condition " + condition);
        }
    }

    /**
     * @param quad
     * @param c1
     * @param condition
     * @param c2
     */
    public void generateCodeFor(ConditionalBranchQuad<T> quad, Constant<T> c1,
                                BranchCondition condition, Constant<T> c2) {
        switch (condition) {
            case IF_ICMPEQ:
            case IF_ICMPNE:
            case IF_ICMPGT:
            case IF_ICMPGE:
            case IF_ICMPLT:
            case IF_ICMPLE:
            case IF_ACMPEQ:
            case IF_ACMPNE:
            default:
                throw new IllegalArgumentException("Unknown condition " + condition);
        }
    }

    /**
     * @param quad
     * @param c1
     * @param condition
     * @param disp2
     */
    public void generateCodeFor(ConditionalBranchQuad<T> quad, Constant<T> c1,
                                BranchCondition condition, int disp2) {
        switch (condition) {
            case IF_ICMPEQ:
            case IF_ICMPNE:
            case IF_ICMPGT:
            case IF_ICMPGE:
            case IF_ICMPLT:
            case IF_ICMPLE:
            case IF_ACMPEQ:
            case IF_ACMPNE:
            default:
                throw new IllegalArgumentException("Unknown condition " + condition);
        }
    }

    /**
     * @param quad
     * @param c1
     * @param condition
     * @param reg2
     */
    public void generateCodeFor(ConditionalBranchQuad<T> quad, Constant<T> c1,
                                BranchCondition condition, Object reg2) {
        switch (condition) {
            case IF_ICMPEQ:
            case IF_ICMPNE:
            case IF_ICMPGT:
            case IF_ICMPGE:
            case IF_ICMPLT:
            case IF_ICMPLE:
            case IF_ACMPEQ:
            case IF_ACMPNE:
            default:
                throw new IllegalArgumentException("Unknown condition " + condition);
        }
    }

    /**
     * @param quad
     * @param disp1
     * @param condition
     * @param c2
     */
    public void generateCodeFor(ConditionalBranchQuad<T> quad, int disp1,
                                BranchCondition condition, Constant<T> c2) {
        checkLabel(quad.getAddress());
        os.writeCMP_Const(BITS32, X86Register.EBP, disp1, ((IntConstant<T>) c2)
            .getValue());
        generateJumpForBinaryCondition(quad, condition);
    }

    /**
     * @param quad
     * @param disp1
     * @param condition
     * @param disp2
     */
    public void generateCodeFor(ConditionalBranchQuad<T> quad, int disp1,
                                BranchCondition condition, int disp2) {
        switch (condition) {
            case IF_ICMPEQ:
            case IF_ICMPNE:
            case IF_ICMPGT:
            case IF_ICMPGE:
            case IF_ICMPLT:
            case IF_ICMPLE:
            case IF_ACMPEQ:
            case IF_ACMPNE:
            default:
                throw new IllegalArgumentException("Unknown condition " + condition);
        }
    }

    /**
     * @param quad
     * @param disp1
     * @param condition
     * @param reg2
     */
    public void generateCodeFor(ConditionalBranchQuad<T> quad, int disp1,
                                BranchCondition condition, Object reg2) {
        checkLabel(quad.getAddress());
        os.writeCMP(X86Register.EBP, disp1, (GPR) reg2);
        generateJumpForBinaryCondition(quad, condition);
    }

    /**
     * @param quad
     * @param reg1
     * @param condition
     * @param c2
     */
    public void generateCodeFor(ConditionalBranchQuad<T> quad, Object reg1,
                                BranchCondition condition, Constant<T> c2) {
        checkLabel(quad.getAddress());
        os.writeCMP_Const((GPR) reg1, ((IntConstant<T>) c2).getValue());
        generateJumpForBinaryCondition(quad, condition);
    }

    /**
     * @param quad
     * @param reg1
     * @param condition
     * @param disp2
     */
    public void generateCodeFor(ConditionalBranchQuad<T> quad, Object reg1,
                                BranchCondition condition, int disp2) {
        checkLabel(quad.getAddress());
        os.writeCMP((GPR) reg1, X86Register.EBP, disp2);
        generateJumpForBinaryCondition(quad, condition);
    }

    /**
     * @param quad
     * @param reg1
     * @param condition
     * @param reg2
     */
    public void generateCodeFor(ConditionalBranchQuad<T> quad, Object reg1,
                                BranchCondition condition, Object reg2) {
        checkLabel(quad.getAddress());
        os.writeCMP((GPR) reg1, (GPR) reg2);
        generateJumpForBinaryCondition(quad, condition);
    }

    private void generateJumpForBinaryCondition(ConditionalBranchQuad<T> quad,
                                                BranchCondition condition) {
        switch (condition) {
            case IF_ICMPEQ:
                os
                    .writeJCC(getInstrLabel(quad.getTargetAddress()),
                        X86Constants.JE);
                break;

            case IF_ICMPNE:
                os.writeJCC(getInstrLabel(quad.getTargetAddress()),
                    X86Constants.JNE);
                break;

            case IF_ICMPGT:
                os
                    .writeJCC(getInstrLabel(quad.getTargetAddress()),
                        X86Constants.JG);
                break;

            case IF_ICMPGE:
                os.writeJCC(getInstrLabel(quad.getTargetAddress()),
                    X86Constants.JGE);
                break;

            case IF_ICMPLT:
                os
                    .writeJCC(getInstrLabel(quad.getTargetAddress()),
                        X86Constants.JL);
                break;

            case IF_ICMPLE:
                os.writeJCC(getInstrLabel(quad.getTargetAddress()),
                    X86Constants.JLE);
                break;

            case IF_ACMPEQ:
                os
                    .writeJCC(getInstrLabel(quad.getTargetAddress()),
                        X86Constants.JE);
                break;

            case IF_ACMPNE:
                os.writeJCC(getInstrLabel(quad.getTargetAddress()),
                    X86Constants.JNE);
                break;

            default:
                throw new IllegalArgumentException("Unknown condition " + condition);
        }
    }
}
