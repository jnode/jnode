/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.vm.x86.compiler.l2;

import org.jnode.assembler.x86.AbstractX86Stream;
import org.jnode.assembler.x86.X86Register;
import org.jnode.assembler.x86.X86Constants;
import org.jnode.assembler.Label;
import org.jnode.vm.compiler.ir.CodeGenerator;
import org.jnode.vm.compiler.ir.Constant;
import org.jnode.vm.compiler.ir.IntConstant;
import org.jnode.vm.compiler.ir.Location;
import org.jnode.vm.compiler.ir.Operand;
import org.jnode.vm.compiler.ir.RegisterLocation;
import org.jnode.vm.compiler.ir.RegisterPool;
import org.jnode.vm.compiler.ir.StackLocation;
import org.jnode.vm.compiler.ir.Variable;
import org.jnode.vm.compiler.ir.quad.BinaryQuad;
import org.jnode.vm.compiler.ir.quad.ConditionalBranchQuad;
import org.jnode.vm.compiler.ir.quad.ConstantRefAssignQuad;
import org.jnode.vm.compiler.ir.quad.UnaryQuad;
import org.jnode.vm.compiler.ir.quad.UnconditionalBranchQuad;
import org.jnode.vm.compiler.ir.quad.VarReturnQuad;
import org.jnode.vm.compiler.ir.quad.VariableRefAssignQuad;
import org.jnode.vm.compiler.ir.quad.VoidReturnQuad;

/**
 * @author Madhu Siddalingaiah
 * @author Levente Sántha
 */
public class X86CodeGenerator extends CodeGenerator {
    private static final X86Register SR1 = X86Register.EAX;
//    private static final Register SR2 = Register.EBX;
//    private static final Register SR3 = Register.ECX;
//    private static final Register SR4 = Register.EDX;
    public static final int BYTESIZE = X86Constants.BITS8;
    public static final int WORDSIZE = X86Constants.BITS16;
    private Variable[] spilledVariables;
    private AbstractX86Stream os;
    private int displacement;
    private String labelPrefix;
    private String instrLabelPrefix;
    private Label[] addressLabels;

    private final RegisterPool registerPool;

    /**
     * Initialize this instance
     */
    public X86CodeGenerator(AbstractX86Stream x86Stream, int lenght) {
        CodeGenerator.setCodeGenerator(this);
        this.registerPool = new X86RegisterPool();
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
    public RegisterPool getRegisterPool() {
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
    public void setArgumentVariables(Variable[] vars, int nArgs) {
        displacement = 0;
        for (int i = 0; i < nArgs; i += 1) {
            // TODO this might not be right, check with Ewout
            displacement = vars[i].getIndex() * 4;
            vars[i].setLocation(new StackLocation(displacement));
        }
        // not sure how big the last arg is...
        displacement += 8;
    }

    /**
     * @param variables
     */
    public void setSpilledVariables(Variable[] variables) {
        this.spilledVariables = variables;
        int n = spilledVariables.length;
        for (int i = 0; i < n; i += 1) {
            StackLocation loc = (StackLocation) spilledVariables[i].getLocation();
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
        //os.writePUSH(context.getMagic());
        //os.writePUSH(0); // PC, which is only used in interpreted methods
        /** EAX MUST contain the VmMethod structure upon entry of the method */
        //os.writePUSH(Register.EAX);
        os.writeMOV(X86Constants.BITS32, X86Register.EBP, X86Register.ESP);
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.quad.ConditionalBranchQuad)
     */
    public void generateCodeFor(ConditionalBranchQuad quad) {
        throw new IllegalArgumentException("Unknown operation");
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.quad.ConstantRefAssignQuad)
     */
    public void generateCodeFor(ConstantRefAssignQuad quad) {
        throw new IllegalArgumentException("Unknown operation");

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
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.quad.UnconditionalBranchQuad)
     */
    public void generateCodeFor(UnconditionalBranchQuad quad) {
        checkLabel(quad.getAddress());
        os.writeJMP(getInstrLabel(quad.getTargetAddress()));
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.quad.VariableRefAssignQuad)
     */
    public void generateCodeFor(VariableRefAssignQuad quad) {
        Variable lhs = quad.getLHS();
        if (lhs.getAddressingMode() == Operand.MODE_REGISTER) {
            Object reg1 = ((RegisterLocation)lhs.getLocation()).getRegister();
            Operand rhs = quad.getRHS();
            int mode = rhs.getAddressingMode();
            if (mode == Operand.MODE_CONSTANT) {
                throw new IllegalArgumentException("Unknown operation");
            } else if (mode == Operand.MODE_REGISTER) {
                Object reg2 = ((RegisterLocation) ((Variable) rhs).getLocation()).getRegister();
                os.writeMOV(X86Constants.BITS32, (X86Register)reg1, (X86Register) reg2);
            } else if (mode == Operand.MODE_STACK) {
                int disp2 = ((StackLocation) ((Variable) rhs).getLocation()).getDisplacement();
                os.writeMOV(X86Constants.BITS32, (X86Register)reg1, X86Register.EBP, disp2);
            }
        } else if (lhs.getAddressingMode() == Operand.MODE_STACK) {
            int disp1 = ((StackLocation)lhs.getLocation()).getDisplacement();
            Operand rhs = quad.getRHS();
            int mode = rhs.getAddressingMode();
            if (mode == Operand.MODE_CONSTANT) {
                throw new IllegalArgumentException("Unknown operation");
            } else if (mode == Operand.MODE_REGISTER) {
                Object reg2 = ((RegisterLocation) ((Variable) rhs).getLocation()).getRegister();
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg2);
            } else if (mode == Operand.MODE_STACK) {
                //int disp2 = ((StackLocation) ((Variable) rhs).getLocation()).getDisplacement();
                throw new IllegalArgumentException("Unknown operation");
            }
        }

    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.quad.VarReturnQuad)
     */
    public void generateCodeFor(VarReturnQuad quad) {
        checkLabel(quad.getAddress());
        Operand op = quad.getOperand();
        // TODO must deal with other types, see else case also
        if (op instanceof IntConstant) {
            IntConstant iconst = (IntConstant) op;
            os.writeMOV_Const(X86Register.EAX, iconst.getValue());
        } else if (op instanceof Variable) {
            Variable var = (Variable) op;
            Location loc = var.getLocation();
            if (loc instanceof RegisterLocation) {
                RegisterLocation regLoc = (RegisterLocation) loc;
                X86Register src = (X86Register) regLoc.getRegister();
                if (!src.equals(X86Register.EAX)) {
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, src);
                }
            } else {
                StackLocation stackLoc = (StackLocation) loc;
                os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EBP,
                        stackLoc.getDisplacement());
            }
        }

        //TODO: hack for testing
        os.writeMOV(X86Constants.BITS32, X86Register.ESP,  X86Register.EBP);
        os.writePOP(X86Register.EBP);


        os.writeRET();
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.quad.VoidReturnQuad)
     */
    public void generateCodeFor(VoidReturnQuad quad) {
        checkLabel(quad.getAddress());

        //TODO: hack for testing
        os.writeMOV(X86Constants.BITS32, X86Register.ESP,  X86Register.EBP);
        os.writePOP(X86Register.EBP);

        os.writeRET();
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.quad.UnaryQuad, java.lang.Object, int, org.jnode.vm.compiler.ir.Constant)
     */
    public void generateCodeFor(UnaryQuad quad, Object lhsReg, int operation,
                                Constant con) {
        throw new IllegalArgumentException("Constants should be folded");
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.quad.UnaryQuad, java.lang.Object, int, java.lang.Object)
     */
    public void generateCodeFor(UnaryQuad quad, Object lhsReg, int operation,
                                Object rhsReg) {
        checkLabel(quad.getAddress());
        switch (operation) {
            case UnaryQuad.I2L:
                throw new IllegalArgumentException("Unknown operation");

            case UnaryQuad.I2F:
                os.writePUSH((X86Register) rhsReg);
                os.writeFILD32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((X86Register) lhsReg);
                break;

            case UnaryQuad.I2D:
            case UnaryQuad.L2I:
            case UnaryQuad.L2F:
            case UnaryQuad.L2D:
                throw new IllegalArgumentException("Unknown operation");

            case UnaryQuad.F2I:
                os.writePUSH((X86Register) rhsReg);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeFISTP32(X86Register.ESP, 0);
                os.writePOP((X86Register) lhsReg);
                break;

            case UnaryQuad.F2L:
            case UnaryQuad.F2D:
            case UnaryQuad.D2I:
            case UnaryQuad.D2L:
            case UnaryQuad.D2F:
                throw new IllegalArgumentException("Unknown operation");

            case UnaryQuad.I2B:
                os.writeMOVSX((X86Register)lhsReg, (X86Register)rhsReg, BYTESIZE);
                break;

            case UnaryQuad.I2C:
                os.writeMOVZX((X86Register)lhsReg, (X86Register)rhsReg, WORDSIZE);
                break;

            case UnaryQuad.I2S:
                os.writeMOVSX((X86Register)lhsReg, (X86Register)rhsReg, WORDSIZE);
                break;

            case UnaryQuad.INEG:
                if (lhsReg != rhsReg) {
                    os.writeMOV(X86Constants.BITS32, (X86Register) lhsReg, (X86Register) rhsReg);
                }
                os.writeNEG((X86Register) lhsReg);
                break;

            case UnaryQuad.LNEG:
                throw new IllegalArgumentException("Unknown operation");

            case UnaryQuad.FNEG:
                os.writePUSH((X86Register) rhsReg);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeFCHS();
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((X86Register) lhsReg);
                break;

            case UnaryQuad.DNEG:
            default:
                throw new IllegalArgumentException("Unknown operation");
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.quad.UnaryQuad, java.lang.Object, int, int)
     */
    public void generateCodeFor(UnaryQuad quad, Object lhsReg, int operation,
                                int rhsDisp) {
        checkLabel(quad.getAddress());
        switch (operation) {
            case UnaryQuad.I2L:
                throw new IllegalArgumentException("Unknown operation");

            case UnaryQuad.I2F:
                os.writePUSH(X86Register.EBP, rhsDisp);
                os.writeFILD32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((X86Register) lhsReg);
                break;

            case UnaryQuad.I2D:
            case UnaryQuad.L2I:
            case UnaryQuad.L2F:
            case UnaryQuad.L2D:
                throw new IllegalArgumentException("Unknown operation");

            case UnaryQuad.F2I:
                os.writePUSH(X86Register.EBP, rhsDisp);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeFISTP32(X86Register.ESP, 0);
                os.writePOP((X86Register) lhsReg);
                break;

            case UnaryQuad.F2L:
            case UnaryQuad.F2D:
            case UnaryQuad.D2I:
            case UnaryQuad.D2L:
            case UnaryQuad.D2F:
                throw new IllegalArgumentException("Unknown operation");

            case UnaryQuad.I2B:
                os.writeMOVSX((X86Register)lhsReg, X86Register.EBP, rhsDisp, BYTESIZE);
                break;

            case UnaryQuad.I2C:
                os.writeMOVZX((X86Register)lhsReg, X86Register.EBP, rhsDisp, WORDSIZE);
                break;

            case UnaryQuad.I2S:
                os.writeMOVSX((X86Register)lhsReg, X86Register.EBP, rhsDisp, WORDSIZE);
                break;

            case UnaryQuad.INEG:
                os.writeMOV(X86Constants.BITS32, (X86Register) lhsReg, X86Register.EBP,rhsDisp);
                os.writeNEG((X86Register) lhsReg);
                break;

            case UnaryQuad.LNEG:
                throw new IllegalArgumentException("Unknown operation");

            case UnaryQuad.FNEG:
                os.writePUSH(X86Register.EBP, rhsDisp);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeFCHS();
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((X86Register) lhsReg);
                break;

            case UnaryQuad.DNEG:
            default:
                throw new IllegalArgumentException("Unknown operation");
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.quad.UnaryQuad, int, int, java.lang.Object)
     */
    public void generateCodeFor(UnaryQuad quad, int lhsDisp, int operation,
                                Object rhsReg) {
        checkLabel(quad.getAddress());
        switch (operation) {
            case UnaryQuad.I2L:
                throw new IllegalArgumentException("Unknown operation");

            case UnaryQuad.I2F:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, lhsDisp, (X86Register) rhsReg);
                os.writeFILD32(X86Register.EBP, lhsDisp);
                os.writeFSTP32(X86Register.EBP, lhsDisp);
                break;

            case UnaryQuad.I2D:
            case UnaryQuad.L2I:
            case UnaryQuad.L2F:
            case UnaryQuad.L2D:
            case UnaryQuad.F2I:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, lhsDisp, (X86Register) rhsReg);
                os.writeFLD32(X86Register.EBP, lhsDisp);
                os.writeFISTP32(X86Register.EBP, lhsDisp);
                break;

            case UnaryQuad.F2L:
            case UnaryQuad.F2D:
            case UnaryQuad.D2I:
            case UnaryQuad.D2L:
            case UnaryQuad.D2F:
                throw new IllegalArgumentException("Unknown operation");

            case UnaryQuad.I2B:
                os.writePUSH(SR1);
                os.writeMOVSX(SR1, (X86Register) rhsReg, BYTESIZE);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, lhsDisp, SR1);
                os.writePOP(SR1);
                break;

            case UnaryQuad.I2C:
                os.writePUSH(SR1);
                os.writeMOVZX(SR1, (X86Register) rhsReg, WORDSIZE);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, lhsDisp, SR1);
                os.writePOP(SR1);
                break;

            case UnaryQuad.I2S:
                os.writePUSH(SR1);
                os.writeMOVSX(SR1, (X86Register) rhsReg, WORDSIZE);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, lhsDisp, SR1);
                os.writePOP(SR1);
                break;

            case UnaryQuad.INEG:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, lhsDisp, (X86Register) rhsReg);
                os.writeNEG(X86Register.EBP, lhsDisp);
                break;

            case UnaryQuad.LNEG:
                throw new IllegalArgumentException("Unknown operation");

            case UnaryQuad.FNEG:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, lhsDisp, (X86Register) rhsReg);
                os.writeFLD32(X86Register.EBP, lhsDisp);
                os.writeFCHS();
                os.writeFSTP32(X86Register.EBP, lhsDisp);
                break;

            case UnaryQuad.DNEG:
            default:
                throw new IllegalArgumentException("Unknown operation");
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.quad.UnaryQuad, int, int, int)
     */
    public void generateCodeFor(UnaryQuad quad, int lhsDisp, int operation, int rhsDisp) {
         checkLabel(quad.getAddress());
        switch (operation) {
            case UnaryQuad.I2L:
                throw new IllegalArgumentException("Unknown operation");

            case UnaryQuad.I2F:
                os.writeFILD32(X86Register.EBP, rhsDisp);
                os.writeFSTP32(X86Register.EBP, lhsDisp);
                break;

            case UnaryQuad.I2D:
            case UnaryQuad.L2I:
            case UnaryQuad.L2F:
            case UnaryQuad.L2D:
                throw new IllegalArgumentException("Unknown operation");

            case UnaryQuad.F2I:
                os.writeFLD32(X86Register.EBP, rhsDisp);
                os.writeFISTP32(X86Register.EBP, lhsDisp);
                break;

            case UnaryQuad.F2L:
            case UnaryQuad.F2D:
            case UnaryQuad.D2I:
            case UnaryQuad.D2L:
            case UnaryQuad.D2F:
                throw new IllegalArgumentException("Unknown operation");

            case UnaryQuad.I2B:
                os.writePUSH(SR1);
                os.writeMOVSX(SR1, X86Register.EBP, rhsDisp, BYTESIZE);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, lhsDisp, SR1);
                os.writePOP(SR1);
                break;

            case UnaryQuad.I2C:
                os.writePUSH(SR1);
                os.writeMOVZX(SR1, X86Register.EBP, rhsDisp, WORDSIZE);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, lhsDisp, SR1);
                os.writePOP(SR1);
                break;

            case UnaryQuad.I2S:
                os.writePUSH(SR1);
                os.writeMOVSX(SR1, X86Register.EBP, rhsDisp, WORDSIZE);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, lhsDisp, SR1);
                os.writePOP(SR1);
                break;

            case UnaryQuad.INEG:
                if(rhsDisp != lhsDisp){
                    os.writePUSH(X86Register.EBP, rhsDisp);
                    os.writePOP(X86Register.EBP, lhsDisp);
                }
                os.writeNEG(X86Register.EBP, lhsDisp);
                break;

            case UnaryQuad.LNEG:
                throw new IllegalArgumentException("Unknown operation");

            case UnaryQuad.FNEG:
                os.writeFLD32(X86Register.EBP, rhsDisp);
                os.writeFCHS();
                os.writeFSTP32(X86Register.EBP, lhsDisp);
                break;

            case UnaryQuad.DNEG:
            default:
                throw new IllegalArgumentException("Unknown operation");
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.quad.UnaryQuad, int, int, org.jnode.vm.compiler.ir.Constant)
     */
    public void generateCodeFor(UnaryQuad quad, int lhsDisp, int operation, Constant con) {
        throw new IllegalArgumentException("Constants should be folded");
    }


    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(java.lang.Object, org.jnode.vm.compiler.ir.Constant, int, org.jnode.vm.compiler.ir.Constant)
     */
    public void generateBinaryOP(Object reg1, Constant c2, int operation, Constant c3) {
        throw new IllegalArgumentException("Constants should be folded");
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(java.lang.Object, org.jnode.vm.compiler.ir.Constant, int, java.lang.Object)
     */
    public void generateBinaryOP(Object reg1, Constant c2, int operation, Object reg3) {
        IntConstant iconst2 = (IntConstant) c2;
        switch (operation) {
            case BinaryQuad.IADD:
                os.writeMOV_Const((X86Register) reg1, iconst2.getValue());
                os.writeADD((X86Register) reg1, (X86Register) reg3);
                break;

            case BinaryQuad.IAND:
                os.writeMOV_Const((X86Register) reg1, iconst2.getValue());
                os.writeAND((X86Register) reg1, (X86Register) reg3);
                break;

            case BinaryQuad.IDIV:   //needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writeMOV_Const(X86Register.EAX, iconst2.getValue());
                os.writeCDQ();
                if(reg3 == X86Register.EAX){
                    os.writeIDIV_EAX(X86Register.ESP, 0);
                }else if(reg3 == X86Register.EDX){
                    os.writeIDIV_EAX(X86Register.ESP, 4);
                }else{
                    os.writeIDIV_EAX((X86Register)reg3);
                }
                if(reg1 == X86Register.EAX){
                    os.writePOP(X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                }else if(reg1 == X86Register.EDX){
                    os.writeMOV(X86Constants.BITS32, X86Register.EDX, X86Register.EAX);
                    os.writePOP(X86Register.EAX);
                    os.writeADD(X86Register.ESP, 4);
                }else{
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EAX);
                    os.writePOP(X86Register.EAX);
                    os.writePOP(X86Register.EDX);
                }
                break;

            case BinaryQuad.IMUL:
                os.writeIMUL_3((X86Register) reg1, (X86Register) reg3, iconst2.getValue());
                break;

            case BinaryQuad.IOR:
                os.writeMOV_Const((X86Register) reg1, iconst2.getValue());
                os.writeOR((X86Register) reg1, (X86Register) reg3);
                break;

            case BinaryQuad.IREM:   //needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writeMOV_Const(X86Register.EAX, iconst2.getValue());
                os.writeCDQ();
                if(reg3 == X86Register.EAX){
                    os.writeIDIV_EAX(X86Register.ESP, 0);
                }else if(reg3 == X86Register.EDX){
                    os.writeIDIV_EAX(X86Register.ESP, 4);
                }else{
                    os.writeIDIV_EAX((X86Register)reg3);
                }
                if(reg1 == X86Register.EDX){
                    os.writePOP(X86Register.EAX);
                    os.writeADD(X86Register.ESP, 4);
                }else if(reg1 == X86Register.EAX){
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                }else{
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EDX);
                    os.writePOP(X86Register.EAX);
                    os.writePOP(X86Register.EDX);
                }
                break;

            case BinaryQuad.ISHL:   //needs CL
                os.writeMOV_Const((X86Register) reg1, iconst2.getValue());
                if (reg3 != X86Register.CX){
                    os.writePUSH(X86Register.CX);
                    os.writeMOV(X86Constants.BITS32, X86Register.CX, (X86Register) reg3);
                    os.writeSAL_CL((X86Register) reg1);
                    os.writePOP(X86Register.CX);
                }else{
                    os.writeSAL_CL((X86Register) reg1);
                }
                break;

            case BinaryQuad.ISHR:   //needs CL
                os.writeMOV_Const((X86Register) reg1, iconst2.getValue());
                if (reg3 != X86Register.CX){
                    os.writePUSH(X86Register.CX);
                    os.writeMOV(X86Constants.BITS32, X86Register.CX, (X86Register) reg3);
                    os.writeSAL_CL((X86Register) reg1);
                    os.writePOP(X86Register.CX);
                }else{
                    os.writeSAR_CL((X86Register) reg1);
                }
                break;

            case BinaryQuad.ISUB:
                os.writeMOV_Const((X86Register) reg1, iconst2.getValue());
                os.writeSUB((X86Register) reg1, (X86Register) reg3);
                break;

            case BinaryQuad.IUSHR:
                os.writeMOV_Const((X86Register) reg1, iconst2.getValue());
                if (reg3 != X86Register.CX){
                    os.writePUSH(X86Register.CX);
                    os.writeMOV(X86Constants.BITS32, X86Register.CX, (X86Register) reg3);
                    os.writeSAL_CL((X86Register) reg1);
                    os.writePOP(X86Register.CX);
                }else{
                    os.writeSHR_CL((X86Register) reg1);
                }
                break;

            case BinaryQuad.IXOR:
                os.writeMOV_Const((X86Register) reg1, iconst2.getValue());
                os.writeXOR((X86Register) reg1, (X86Register) reg3);
                break;

            case BinaryQuad.DADD:
            case BinaryQuad.DDIV:
            case BinaryQuad.DMUL:
            case BinaryQuad.DREM:
            case BinaryQuad.DSUB:
                throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.FADD:
                os.writePUSH(iconst2.getValue());
                os.writeFLD32(X86Register.ESP, 0);
                os.writeMOV(X86Constants.BITS32, X86Register.ESP, 0, (X86Register) reg3);
                os.writeFADD32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((X86Register) reg1);
                break;

            case BinaryQuad.FDIV:
                os.writePUSH(iconst2.getValue());
                os.writeFLD32(X86Register.ESP, 0);
                os.writeMOV(X86Constants.BITS32, X86Register.ESP, 0, (X86Register) reg3);
                os.writeFDIV32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((X86Register) reg1);
                break;

            case BinaryQuad.FMUL:
                os.writePUSH(iconst2.getValue());
                os.writeFLD32(X86Register.ESP, 0);
                os.writeMOV(X86Constants.BITS32, X86Register.ESP, 0, (X86Register) reg3);
                os.writeFMUL32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((X86Register) reg1);
                break;

            case BinaryQuad.FREM:
                os.writePUSH((X86Register)reg3);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeMOV_Const(X86Register.ESP, 0, iconst2.getValue());
                os.writeFLD32(X86Register.ESP, 0);
                os.writeFPREM();
                os.writeFSTP32(X86Register.ESP, 0);
                os.writeFFREE(X86Register.ST0);
                os.writePOP((X86Register) reg1);
                break;

            case BinaryQuad.FSUB:
                os.writePUSH(iconst2.getValue());
                os.writeFLD32(X86Register.ESP, 0);
                os.writeMOV(X86Constants.BITS32, X86Register.ESP, 0, (X86Register) reg3);
                os.writeFSUB32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((X86Register) reg1);
                break;

            case BinaryQuad.LADD:
            case BinaryQuad.LAND:
            case BinaryQuad.LDIV:
            case BinaryQuad.LMUL:
            case BinaryQuad.LOR:
            case BinaryQuad.LREM:
            case BinaryQuad.LSHL:
            case BinaryQuad.LSHR:
            case BinaryQuad.LSUB:
            case BinaryQuad.LUSHR:
            case BinaryQuad.LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation");
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(java.lang.Object, org.jnode.vm.compiler.ir.Constant, int, int)
     */
    public void generateBinaryOP(Object reg1, Constant c2, int operation, int disp3) {
        IntConstant iconst2 = (IntConstant) c2;
        switch (operation) {

            case BinaryQuad.IADD:
                os.writeMOV_Const((X86Register) reg1, iconst2.getValue());
                os.writeADD((X86Register) reg1, X86Register.EBP, disp3);
                break;

            case BinaryQuad.IAND:
                os.writeMOV_Const((X86Register) reg1, iconst2.getValue());
                os.writeAND((X86Register) reg1, X86Register.EBP, disp3);
                break;

            case BinaryQuad.IDIV:   //not supported
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writeMOV_Const(X86Register.EAX, iconst2.getValue());
                os.writeCDQ();
                os.writeIDIV_EAX(X86Register.EBP, disp3);
                if(reg1 == X86Register.EAX){
                    os.writePOP(X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                }else if(reg1 == X86Register.EDX){
                    os.writeMOV(X86Constants.BITS32, X86Register.EDX, X86Register.EAX);
                    os.writePOP(X86Register.EAX);
                    os.writeADD(X86Register.ESP, 4);
                }else{
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EAX);
                    os.writePOP(X86Register.EAX);
                    os.writePOP(X86Register.EDX);
                }
                break;


            case BinaryQuad.IMUL:
                os.writeIMUL_3((X86Register) reg1, X86Register.EBP, disp3, iconst2.getValue());
                break;

            case BinaryQuad.IOR:
                os.writeMOV_Const((X86Register) reg1, iconst2.getValue());
                os.writeOR((X86Register) reg1, X86Register.EBP, disp3);
                break;

            case BinaryQuad.IREM:
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writeMOV_Const(X86Register.EAX, iconst2.getValue());
                os.writeCDQ();
                os.writeIDIV_EAX(X86Register.EBP, disp3);
                if(reg1 == X86Register.EDX){
                    os.writePOP(X86Register.EAX);
                    os.writeADD(X86Register.ESP, 4);
                }else if(reg1 == X86Register.EAX){
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                }else{
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EDX);
                    os.writePOP(X86Register.EAX);
                    os.writePOP(X86Register.EDX);
                }
                break;

            case BinaryQuad.ISHL:   //not supported
                os.writeMOV_Const((X86Register) reg1, iconst2.getValue());
                os.writePUSH(X86Register.CX);
                os.writeMOV(X86Constants.BITS32, X86Register.CX, X86Register.EBP, disp3);
                os.writeSAL_CL((X86Register) reg1);
                os.writePOP(X86Register.CX);
                break;

            case BinaryQuad.ISHR:   //not supported
                os.writeMOV_Const((X86Register) reg1, iconst2.getValue());
                os.writePUSH(X86Register.CX);
                os.writeMOV(X86Constants.BITS32, X86Register.CX, X86Register.EBP, disp3);
                os.writeSAR_CL((X86Register) reg1);
                os.writePOP(X86Register.CX);
                break;

            case BinaryQuad.ISUB:
                os.writeMOV_Const((X86Register) reg1, iconst2.getValue());
                os.writeSUB((X86Register) reg1, X86Register.EBP, disp3);
                break;

            case BinaryQuad.IUSHR:  //not supported
                os.writeMOV_Const((X86Register) reg1, iconst2.getValue());
                os.writePUSH(X86Register.CX);
                os.writeMOV(X86Constants.BITS32, X86Register.CX, X86Register.EBP, disp3);
                os.writeSHR_CL((X86Register) reg1);
                os.writePOP(X86Register.CX);
                break;

            case BinaryQuad.IXOR:
                os.writeMOV_Const((X86Register) reg1, iconst2.getValue());
                os.writeXOR((X86Register) reg1, X86Register.EBP, disp3);
                break;

            case BinaryQuad.DADD:
            case BinaryQuad.DDIV:
            case BinaryQuad.DMUL:
            case BinaryQuad.DREM:
            case BinaryQuad.DSUB:
                throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.FADD:
                os.writePUSH(iconst2.getValue());
                os.writeFLD32(X86Register.ESP, 0);
                os.writeFADD32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((X86Register) reg1);
                break;

            case BinaryQuad.FDIV:
                os.writePUSH(iconst2.getValue());
                os.writeFLD32(X86Register.ESP, 0);
                os.writeFDIV32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((X86Register) reg1);
                break;

            case BinaryQuad.FMUL:
                os.writePUSH(iconst2.getValue());
                os.writeFLD32(X86Register.ESP, 0);
                os.writeFMUL32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((X86Register) reg1);
                break;

            case BinaryQuad.FREM:
                os.writePUSH(iconst2.getValue());
                os.writeFLD32(X86Register.EBP, disp3);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeFPREM();
                os.writeFSTP32(X86Register.ESP, 0);
                os.writeFFREE(X86Register.ST0);
                os.writePOP((X86Register) reg1);
                break;

            case BinaryQuad.FSUB:
                os.writePUSH(iconst2.getValue());
                os.writeFLD32(X86Register.ESP, 0);
                os.writeFSUB32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((X86Register) reg1);
                break;

            case BinaryQuad.LADD:
            case BinaryQuad.LAND:
            case BinaryQuad.LDIV:
            case BinaryQuad.LMUL:
            case BinaryQuad.LOR:
            case BinaryQuad.LREM:
            case BinaryQuad.LSHL:
            case BinaryQuad.LSHR:
            case BinaryQuad.LSUB:
            case BinaryQuad.LUSHR:
            case BinaryQuad.LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation");
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(java.lang.Object, java.lang.Object, int, org.jnode.vm.compiler.ir.Constant)
     */
    public void generateBinaryOP(Object reg1, Object reg2, int operation, Constant c3) {
        IntConstant iconst3 = (IntConstant) c3;
        switch (operation) {

            case BinaryQuad.IADD:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, (X86Register) reg2);
                }
                os.writeADD((X86Register) reg1, iconst3.getValue());
                break;

            case BinaryQuad.IAND:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, (X86Register) reg2);
                }
                os.writeAND((X86Register) reg1, iconst3.getValue());
                break;

            case BinaryQuad.IDIV:   //needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writePUSH(iconst3.getValue());
                if(reg2 != X86Register.EAX){
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, (X86Register)reg2);
                }
                os.writeCDQ();
                os.writeIDIV_EAX(X86Register.ESP, 0);
                os.writePOP(X86Register.EDX);
                if(reg1 == X86Register.EAX){
                    os.writePOP(X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                }else if(reg1 == X86Register.EDX){
                    os.writeMOV(X86Constants.BITS32, X86Register.EDX, X86Register.EAX);
                    os.writePOP(X86Register.EAX);
                    os.writeADD(X86Register.ESP, 4);
                }else{
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EAX);
                    os.writePOP(X86Register.EAX);
                    os.writePOP(X86Register.EDX);
                }
                break;

            case BinaryQuad.IMUL:
                os.writeIMUL_3((X86Register) reg1, (X86Register) reg2, iconst3.getValue());
                break;

            case BinaryQuad.IOR:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, (X86Register) reg2);
                }
                os.writeOR((X86Register) reg1, iconst3.getValue());
                break;

            case BinaryQuad.IREM:   //needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writePUSH(iconst3.getValue());
                if(reg2 != X86Register.EAX){
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, (X86Register)reg2);
                }
                os.writeCDQ();
                os.writeIDIV_EAX(X86Register.ESP, 0);
                os.writePOP(X86Register.EAX);
                if(reg1 == X86Register.EDX){
                    os.writePOP(X86Register.EAX);
                    os.writeADD(X86Register.ESP, 4);
                }else if(reg1 == X86Register.EAX){
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                }else{
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EDX);
                    os.writePOP(X86Register.EAX);
                    os.writePOP(X86Register.EDX);
                }
                break;

            case BinaryQuad.ISHL:   //needs CL
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, (X86Register) reg2);
                }
                os.writeSAL((X86Register) reg1, iconst3.getValue());
                break;

            case BinaryQuad.ISHR:   //needs CL
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, (X86Register) reg2);
                }
                os.writeSAR((X86Register) reg1, iconst3.getValue());
                break;

            case BinaryQuad.ISUB:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, (X86Register) reg2);
                }
                os.writeSUB((X86Register) reg1, iconst3.getValue());
                break;

            case BinaryQuad.IUSHR:  //needs CL
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, (X86Register) reg2);
                }
                os.writeSHR((X86Register) reg1, iconst3.getValue());
                break;

            case BinaryQuad.IXOR:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, (X86Register) reg2);
                }
                os.writeXOR((X86Register) reg1, iconst3.getValue());
                break;

            case BinaryQuad.DADD:
            case BinaryQuad.DDIV:
            case BinaryQuad.DMUL:
            case BinaryQuad.DREM:
            case BinaryQuad.DSUB:
                throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.FADD:
                os.writePUSH((X86Register) reg2);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeMOV_Const(X86Register.ESP, 0, iconst3.getValue());
                os.writeFADD32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((X86Register) reg1);
                break;

            case BinaryQuad.FDIV:
                os.writePUSH((X86Register)reg2);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeMOV_Const(X86Register.ESP, 0, iconst3.getValue());
                os.writeFDIV32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((X86Register) reg1);
                break;

            case BinaryQuad.FMUL:
                os.writePUSH((X86Register)reg2);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeMOV_Const(X86Register.ESP, 0, iconst3.getValue());
                os.writeFMUL32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((X86Register) reg1);
                break;

            case BinaryQuad.FREM:
                os.writePUSH(iconst3.getValue());
                os.writeFLD32(X86Register.ESP, 0);
                os.writeMOV(X86Constants.BITS32, X86Register.ESP, 0, (X86Register) reg2);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeFPREM();
                os.writeFSTP32(X86Register.ESP, 0);
                os.writeFFREE(X86Register.ST0);
                os.writePOP((X86Register) reg1);
                break;

            case BinaryQuad.FSUB:
                os.writePUSH((X86Register)reg2);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeMOV_Const(X86Register.ESP, 0, iconst3.getValue());
                os.writeFSUB32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((X86Register) reg1);
                break;

            case BinaryQuad.LADD:
            case BinaryQuad.LAND:
            case BinaryQuad.LDIV:
            case BinaryQuad.LMUL:
            case BinaryQuad.LOR:
            case BinaryQuad.LREM:
            case BinaryQuad.LSHL:
            case BinaryQuad.LSHR:
            case BinaryQuad.LSUB:
            case BinaryQuad.LUSHR:
            case BinaryQuad.LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation");
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(java.lang.Object, java.lang.Object, int, java.lang.Object)
     */
    public void generateBinaryOP(Object reg1, Object reg2, int operation, Object reg3) {

        switch (operation) {

            case BinaryQuad.IADD:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, (X86Register) reg2);
                }
                os.writeADD((X86Register) reg1, (X86Register) reg3);
                break;

            case BinaryQuad.IAND:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, (X86Register) reg2);
                }
                os.writeAND((X86Register) reg1, (X86Register) reg3);
                break;

            case BinaryQuad.IDIV:
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                if(reg2 != X86Register.EAX){
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, (X86Register)reg2);
                }
                os.writeCDQ();
                if(reg3 == X86Register.EAX){
                    os.writeIDIV_EAX(X86Register.ESP, 0);
                }else if(reg3 == X86Register.EDX){
                    os.writeIDIV_EAX(X86Register.ESP, 4);
                }else{
                    os.writeIDIV_EAX((X86Register)reg3);
                }
                if(reg1 == X86Register.EAX){
                    os.writePOP(X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                }else if(reg1 == X86Register.EDX){
                    os.writeMOV(X86Constants.BITS32, X86Register.EDX, X86Register.EAX);
                    os.writePOP(X86Register.EAX);
                    os.writeADD(X86Register.ESP, 4);
                }else{
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EAX);
                    os.writePOP(X86Register.EAX);
                    os.writePOP(X86Register.EDX);
                }
                break;

            case BinaryQuad.IMUL:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, (X86Register) reg2);
                }
                os.writeIMUL((X86Register) reg1, (X86Register) reg3);
                break;

            case BinaryQuad.IOR:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, (X86Register) reg2);
                }
                os.writeOR((X86Register) reg1, (X86Register) reg3);
                break;

            case BinaryQuad.IREM:   //needs EAX, EDX  //TODO verify
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                if(reg2 != X86Register.EAX){
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, (X86Register)reg2);
                }
                os.writeCDQ();
                if(reg3 == X86Register.EAX){
                    os.writeIDIV_EAX(X86Register.ESP, 0);
                }else if(reg3 == X86Register.EDX){
                    os.writeIDIV_EAX(X86Register.ESP, 4);
                }else{
                    os.writeIDIV_EAX((X86Register)reg3);
                }
                if(reg1 == X86Register.EDX){
                    os.writePOP(X86Register.EAX);
                    os.writeADD(X86Register.ESP, 4);
                }else if(reg1 == X86Register.EAX){
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                }else{
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EDX);
                    os.writePOP(X86Register.EAX);
                    os.writePOP(X86Register.EDX);
                }
                break;

            case BinaryQuad.ISHL:   //needs CL
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, (X86Register) reg2);
                }
                if (reg3 != X86Register.CX){
                    os.writePUSH(X86Register.CX);
                    os.writeMOV(X86Constants.BITS32, X86Register.CX, (X86Register) reg3);
                    os.writeSAL_CL((X86Register) reg1);
                    os.writePOP(X86Register.CX);
                }else{
                    os.writeSAL_CL((X86Register) reg1);
                }
                break;

            case BinaryQuad.ISHR:   //needs CL
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, (X86Register) reg2);
                }
                if (reg3 != X86Register.CX){
                    os.writePUSH(X86Register.CX);
                    os.writeMOV(X86Constants.BITS32, X86Register.CX, (X86Register) reg3);
                    os.writeSAR_CL((X86Register) reg1);
                    os.writePOP(X86Register.CX);
                }else{
                    os.writeSAL_CL((X86Register) reg1);
                }
                break;

            case BinaryQuad.ISUB:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, (X86Register) reg2);
                }
                os.writeSUB((X86Register) reg1, (X86Register) reg3);
                break;

            case BinaryQuad.IUSHR:  //needs CL
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, (X86Register) reg2);
                }
                if (reg3 != X86Register.CX){
                    os.writePUSH(X86Register.CX);
                    os.writeMOV(X86Constants.BITS32, X86Register.CX, (X86Register) reg3);
                    os.writeSHR_CL((X86Register) reg1);
                    os.writePOP(X86Register.CX);
                }else{
                    os.writeSAL_CL((X86Register) reg1);
                }
                break;

            case BinaryQuad.IXOR:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, (X86Register) reg2);
                }
                os.writeXOR((X86Register) reg1, (X86Register) reg3);
                break;

            case BinaryQuad.DADD:
            case BinaryQuad.DDIV:
            case BinaryQuad.DMUL:
            case BinaryQuad.DREM:
            case BinaryQuad.DSUB:
                throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.FADD:
                os.writePUSH((X86Register) reg2);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeMOV(X86Constants.BITS32, X86Register.ESP, 0, (X86Register) reg3);
                os.writeFADD32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((X86Register) reg1);
                break;

            case BinaryQuad.FDIV:
                os.writePUSH((X86Register)reg2);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeMOV(X86Constants.BITS32, X86Register.ESP, 0, (X86Register) reg3);
                os.writeFDIV32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((X86Register) reg1);
                break;

            case BinaryQuad.FMUL:
                os.writePUSH((X86Register)reg2);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeMOV(X86Constants.BITS32, X86Register.ESP, 0, (X86Register) reg3);
                os.writeFMUL32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((X86Register) reg1);
                break;

            case BinaryQuad.FREM:
                os.writePUSH((X86Register)reg3);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeMOV(X86Constants.BITS32, X86Register.ESP, 0, (X86Register) reg2);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeFPREM();
                os.writeFSTP32(X86Register.ESP, 0);
                os.writeFFREE(X86Register.ST0);
                os.writePOP((X86Register) reg1);
                break;

            case BinaryQuad.FSUB:
                os.writePUSH((X86Register)reg2);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeMOV(X86Constants.BITS32, X86Register.ESP, 0, (X86Register) reg3);
                os.writeFSUB32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((X86Register) reg1);
                break;

            case BinaryQuad.LADD:
            case BinaryQuad.LAND:
            case BinaryQuad.LDIV:
            case BinaryQuad.LMUL:
            case BinaryQuad.LOR:
            case BinaryQuad.LREM:
            case BinaryQuad.LSHL:
            case BinaryQuad.LSHR:
            case BinaryQuad.LSUB:
            case BinaryQuad.LUSHR:
            case BinaryQuad.LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation");
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(java.lang.Object, java.lang.Object, int, int)
     */
    public void generateBinaryOP(Object reg1, Object reg2, int operation, int disp3) {
        switch (operation) {

            case BinaryQuad.IADD:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, (X86Register) reg2);
                }
                os.writeADD((X86Register) reg1, X86Register.EBP, disp3);
                break;

            case BinaryQuad.IAND:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, (X86Register) reg2);
                }
                os.writeAND((X86Register) reg1, X86Register.EBP, disp3);
                break;

            case BinaryQuad.IDIV:   //needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                if(reg2 != X86Register.EAX){
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, (X86Register)reg2);
                }
                os.writeCDQ();
                os.writeIDIV_EAX(X86Register.EBP, disp3);
                if(reg1 == X86Register.EAX){
                    os.writePOP(X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                }else if(reg1 == X86Register.EDX){
                    os.writeMOV(X86Constants.BITS32, X86Register.EDX, X86Register.EAX);
                    os.writePOP(X86Register.EAX);
                    os.writeADD(X86Register.ESP, 4);
                }else{
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EAX);
                    os.writePOP(X86Register.EAX);
                    os.writePOP(X86Register.EDX);
                }
                break;


            case BinaryQuad.IMUL:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, (X86Register) reg2);
                }
                os.writeIMUL((X86Register) reg1, X86Register.EBP, disp3);
                break;

            case BinaryQuad.IOR:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, (X86Register) reg2);
                }
                os.writeOR((X86Register) reg1, X86Register.EBP, disp3);
                break;

            case BinaryQuad.IREM:   //needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                if(reg2 != X86Register.EAX){
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, (X86Register)reg2);
                }
                os.writeCDQ();
                os.writeIDIV_EAX(X86Register.EBP, disp3);
                if(reg1 == X86Register.EDX){
                    os.writePOP(X86Register.EAX);
                    os.writeADD(X86Register.ESP, 4);
                }else if(reg1 == X86Register.EAX){
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                }else{
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EDX);
                    os.writePOP(X86Register.EAX);
                    os.writePOP(X86Register.EDX);
                }
                break;

            case BinaryQuad.ISHL:   //needs CL
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, (X86Register) reg2);
                }
                os.writePUSH(X86Register.CX);
                os.writeMOV(X86Constants.BITS32, X86Register.CX, X86Register.EBP, disp3);
                os.writeSAL_CL((X86Register) reg1);
                os.writePOP(X86Register.CX);
                break;

            case BinaryQuad.ISHR:   //needs CL
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, (X86Register) reg2);
                }
                os.writePUSH(X86Register.CX);
                os.writeMOV(X86Constants.BITS32, X86Register.CX, X86Register.EBP, disp3);
                os.writeSAR_CL((X86Register) reg1);
                os.writePOP(X86Register.CX);
                break;

            case BinaryQuad.ISUB:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, (X86Register) reg2);
                }
                os.writeSUB((X86Register) reg1, X86Register.EBP, disp3);
                break;

            case BinaryQuad.IUSHR:  //needs CL
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, (X86Register) reg2);
                }
                os.writePUSH(X86Register.CX);
                os.writeMOV(X86Constants.BITS32, X86Register.CX, X86Register.EBP, disp3);
                os.writeSHR_CL((X86Register) reg1);
                os.writePOP(X86Register.CX);
                break;

            case BinaryQuad.IXOR:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, (X86Register) reg2);
                }
                os.writeXOR((X86Register) reg1, X86Register.EBP, disp3);
                break;

            case BinaryQuad.DADD:
            case BinaryQuad.DDIV:
            case BinaryQuad.DMUL:
            case BinaryQuad.DREM:
            case BinaryQuad.DSUB:
                throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.FADD:
                os.writePUSH((X86Register) reg2);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeFADD32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((X86Register) reg1);
                break;

            case BinaryQuad.FDIV:
                os.writePUSH((X86Register)reg2);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeFDIV32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((X86Register) reg1);
                break;

            case BinaryQuad.FMUL:
                os.writePUSH((X86Register)reg2);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeFMUL32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((X86Register) reg1);
                break;

            case BinaryQuad.FREM:
                os.writePUSH((X86Register)reg2);
                os.writeFLD32(X86Register.EBP, disp3);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeFPREM();
                os.writeFSTP32(X86Register.ESP, 0);
                os.writeFFREE(X86Register.ST0);
                os.writePOP((X86Register) reg1);
                break;

            case BinaryQuad.FSUB:
                os.writePUSH((X86Register)reg2);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeFSUB32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((X86Register) reg1);
                break;

            case BinaryQuad.LADD:
            case BinaryQuad.LAND:
            case BinaryQuad.LDIV:
            case BinaryQuad.LMUL:
            case BinaryQuad.LOR:
            case BinaryQuad.LREM:
            case BinaryQuad.LSHL:
            case BinaryQuad.LSHR:
            case BinaryQuad.LSUB:
            case BinaryQuad.LUSHR:
            case BinaryQuad.LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation");
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(java.lang.Object, int, int, org.jnode.vm.compiler.ir.Constant)
     */
    public void generateBinaryOP(Object reg1, int disp2, int operation, Constant c3) {
        IntConstant iconst3 = (IntConstant) c3;
        switch (operation) {

            case BinaryQuad.IADD:
                os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EBP, disp2);
                os.writeADD((X86Register) reg1, iconst3.getValue());
                break;

            case BinaryQuad.IAND:
                os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EBP, disp2);
                os.writeAND((X86Register) reg1, iconst3.getValue());
                break;

            case BinaryQuad.IDIV:   //needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writePUSH(iconst3.getValue());
                os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EBP, disp2);
                os.writeCDQ();
                os.writeIDIV_EAX(X86Register.ESP, 0);
                os.writePOP(X86Register.EDX);
                if(reg1 == X86Register.EAX){
                    os.writePOP(X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                }else if(reg1 == X86Register.EDX){
                    os.writeMOV(X86Constants.BITS32, X86Register.EDX, X86Register.EAX);
                    os.writePOP(X86Register.EAX);
                    os.writeADD(X86Register.ESP, 4);
                }else{
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EAX);
                    os.writePOP(X86Register.EAX);
                    os.writePOP(X86Register.EDX);
                }
                break;

            case BinaryQuad.IMUL:
                os.writeIMUL_3((X86Register) reg1, X86Register.EBP, disp2, iconst3.getValue());
                break;

            case BinaryQuad.IOR:
                os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EBP, disp2);
                os.writeOR((X86Register) reg1, iconst3.getValue());
                break;

            case BinaryQuad.IREM:   //needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writePUSH(iconst3.getValue());
                os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EBP, disp2);
                os.writeCDQ();
                os.writeIDIV_EAX(X86Register.ESP, 0);
                os.writePOP(X86Register.EAX);
                if(reg1 == X86Register.EDX){
                    os.writePOP(X86Register.EAX);
                    os.writeADD(X86Register.ESP, 4);
                }else if(reg1 == X86Register.EAX){
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                }else{
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EDX);
                    os.writePOP(X86Register.EAX);
                    os.writePOP(X86Register.EDX);
                }
                break;

            case BinaryQuad.ISHL:   //needs CL
                os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EBP, disp2);
                os.writeSAL((X86Register) reg1, iconst3.getValue());
                break;

            case BinaryQuad.ISHR:   //needs CL
                os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EBP, disp2);
                os.writeSAR((X86Register) reg1, iconst3.getValue());
                break;

            case BinaryQuad.ISUB:
                os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EBP, disp2);
                os.writeSUB((X86Register) reg1, iconst3.getValue());
                break;

            case BinaryQuad.IUSHR:  //needs CL
                os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EBP, disp2);
                os.writeSHR((X86Register) reg1, iconst3.getValue());
                break;

            case BinaryQuad.IXOR:
                os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EBP, disp2);
                os.writeXOR((X86Register) reg1, iconst3.getValue());
                break;

            case BinaryQuad.DADD:
            case BinaryQuad.DDIV:
            case BinaryQuad.DMUL:
            case BinaryQuad.DREM:
            case BinaryQuad.DSUB:
                            throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.FADD:
                os.writePUSH(iconst3.getValue());
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFADD32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((X86Register) reg1);
                break;

            case BinaryQuad.FDIV:
                os.writePUSH(iconst3.getValue());
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFDIV32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((X86Register) reg1);
                break;

            case BinaryQuad.FMUL:
                os.writePUSH(iconst3.getValue());
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFMUL32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((X86Register) reg1);
                break;

            case BinaryQuad.FREM:
                os.writePUSH(iconst3.getValue());
                os.writeFLD32(X86Register.ESP, 0);
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFPREM();
                os.writeFSTP32(X86Register.ESP, 0);
                os.writeFFREE(X86Register.ST0);
                os.writePOP((X86Register) reg1);
                break;

            case BinaryQuad.FSUB:
                os.writePUSH(iconst3.getValue());
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFSUB32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((X86Register) reg1);
                break;

            case BinaryQuad.LADD:
            case BinaryQuad.LAND:
            case BinaryQuad.LDIV:
            case BinaryQuad.LMUL:
            case BinaryQuad.LOR:
            case BinaryQuad.LREM:
            case BinaryQuad.LSHL:
            case BinaryQuad.LSHR:
            case BinaryQuad.LSUB:
            case BinaryQuad.LUSHR:
            case BinaryQuad.LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation");
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(java.lang.Object, int, int, java.lang.Object)
     */
    public void generateBinaryOP(Object reg1, int disp2, int operation, Object reg3) {
        switch (operation) {
            case BinaryQuad.IADD:
                os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EBP, disp2);
                os.writeADD((X86Register) reg1, (X86Register) reg3);
                break;

            case BinaryQuad.IAND:
                os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EBP, disp2);
                os.writeAND((X86Register) reg1, (X86Register) reg3);
                break;

            case BinaryQuad.IDIV:   //needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EBP, disp2);
                os.writeCDQ();
                if(reg3 == X86Register.EAX){
                    os.writeIDIV_EAX(X86Register.ESP, 0);
                }else if(reg3 == X86Register.EDX){
                    os.writeIDIV_EAX(X86Register.ESP, 4);
                }else{
                    os.writeIDIV_EAX((X86Register)reg3);
                }
                if(reg1 == X86Register.EAX){
                    os.writePOP(X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                }else if(reg1 == X86Register.EDX){
                    os.writeMOV(X86Constants.BITS32, X86Register.EDX, X86Register.EAX);
                    os.writePOP(X86Register.EAX);
                    os.writeADD(X86Register.ESP, 4);
                }else{
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EAX);
                    os.writePOP(X86Register.EAX);
                    os.writePOP(X86Register.EDX);
                }
                break;

            case BinaryQuad.IMUL:
                os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EBP, disp2);
                os.writeIMUL((X86Register) reg1, (X86Register) reg3);
                break;

            case BinaryQuad.IOR:
                os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EBP, disp2);
                os.writeOR((X86Register) reg1, (X86Register) reg3);
                break;

            case BinaryQuad.IREM:   //needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EBP, disp2);
                os.writeCDQ();
                if(reg3 == X86Register.EAX){
                    os.writeIDIV_EAX(X86Register.ESP, 0);
                }else if(reg3 == X86Register.EDX){
                    os.writeIDIV_EAX(X86Register.ESP, 4);
                }else{
                    os.writeIDIV_EAX((X86Register)reg3);
                }
                if(reg1 == X86Register.EDX){
                    os.writePOP(X86Register.EAX);
                    os.writeADD(X86Register.ESP, 4);
                }else if(reg1 == X86Register.EAX){
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                }else{
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EDX);
                    os.writePOP(X86Register.EAX);
                    os.writePOP(X86Register.EDX);
                }
                break;

            case BinaryQuad.ISHL:   //needs CL
                os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EBP, disp2);
                if (reg3 != X86Register.CX){
                    os.writePUSH(X86Register.CX);
                    os.writeMOV(X86Constants.BITS32, X86Register.CX, (X86Register) reg3);
                    os.writeSHR_CL((X86Register) reg1);
                    os.writePOP(X86Register.CX);
                }else{
                    os.writeSAL_CL((X86Register) reg1);
                }
                break;

            case BinaryQuad.ISHR:   //needs CL
                os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EBP, disp2);
                if (reg3 != X86Register.CX){
                    os.writePUSH(X86Register.CX);
                    os.writeMOV(X86Constants.BITS32, X86Register.CX, (X86Register) reg3);
                    os.writeSHR_CL((X86Register) reg1);
                    os.writePOP(X86Register.CX);
                }else{
                    os.writeSAR_CL((X86Register) reg1);
                }
                break;

            case BinaryQuad.ISUB:
                os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EBP, disp2);
                os.writeSUB((X86Register) reg1, (X86Register) reg3);
                break;

            case BinaryQuad.IUSHR:  //needs CL
                os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EBP, disp2);
                if (reg3 != X86Register.CX){
                    os.writePUSH(X86Register.CX);
                    os.writeMOV(X86Constants.BITS32, X86Register.CX, (X86Register) reg3);
                    os.writeSHR_CL((X86Register) reg1);
                    os.writePOP(X86Register.CX);
                }else{
                    os.writeSHR_CL((X86Register) reg1);
                }
                break;

            case BinaryQuad.IXOR:
                os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EBP, disp2);
                os.writeXOR((X86Register) reg1, (X86Register) reg3);
                break;

            case BinaryQuad.DADD:
            case BinaryQuad.DDIV:
            case BinaryQuad.DMUL:
            case BinaryQuad.DREM:
            case BinaryQuad.DSUB:
                throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.FADD:
                os.writePUSH((X86Register)reg3);
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFADD32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((X86Register) reg1);
                break;

            case BinaryQuad.FDIV:
                os.writePUSH((X86Register)reg3);
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFDIV32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((X86Register) reg1);
                break;

            case BinaryQuad.FMUL:
                os.writePUSH((X86Register)reg3);
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFMUL32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((X86Register) reg1);
                break;

            case BinaryQuad.FREM:
                os.writePUSH((X86Register)reg3);
                os.writeFLD32(X86Register.ESP, 0);
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFPREM();
                os.writeFSTP32(X86Register.ESP, 0);
                os.writeFFREE(X86Register.ST0);
                os.writePOP((X86Register) reg1);
                break;

            case BinaryQuad.FSUB:
                os.writePUSH((X86Register)reg3);
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFSUB32(X86Register.ESP, 0);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((X86Register) reg1);
                break;

            case BinaryQuad.LADD:
            case BinaryQuad.LAND:
            case BinaryQuad.LDIV:
            case BinaryQuad.LMUL:
            case BinaryQuad.LOR:
            case BinaryQuad.LREM:
            case BinaryQuad.LSHL:
            case BinaryQuad.LSHR:
            case BinaryQuad.LSUB:
            case BinaryQuad.LUSHR:
            case BinaryQuad.LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation");
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(java.lang.Object, int, int, int)
     */
    public void generateBinaryOP(Object reg1, int disp2, int operation, int disp3) {
        switch (operation) {
            case BinaryQuad.IADD:
                os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EBP, disp2);
                os.writeADD((X86Register) reg1, X86Register.EBP, disp3);
                break;

            case BinaryQuad.IAND:
                os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EBP, disp2);
                os.writeAND((X86Register) reg1, X86Register.EBP, disp3);
                break;

            case BinaryQuad.IDIV:   //needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EBP, disp2);
                os.writeCDQ();
                os.writeIDIV_EAX(X86Register.EBP, disp3);
                if(reg1 == X86Register.EAX){
                    os.writePOP(X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                }else if(reg1 == X86Register.EDX){
                    os.writeMOV(X86Constants.BITS32, X86Register.EDX, X86Register.EAX);
                    os.writePOP(X86Register.EAX);
                    os.writeADD(X86Register.ESP, 4);
                }else{
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EAX);
                    os.writePOP(X86Register.EAX);
                    os.writePOP(X86Register.EDX);
                }
                break;

            case BinaryQuad.IMUL:
                os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EBP, disp2);
                os.writeIMUL((X86Register) reg1, X86Register.EBP, disp3);
                break;

            case BinaryQuad.IOR:    //not supported
                os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EBP, disp2);
                os.writeOR((X86Register) reg1, X86Register.EBP, disp3);
                break;

            case BinaryQuad.IREM:   //needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EBP, disp2);
                os.writeCDQ();
                os.writeIDIV_EAX(X86Register.EBP, disp3);
                if(reg1 == X86Register.EDX){
                    os.writePOP(X86Register.EAX);
                    os.writeADD(X86Register.ESP, 4);
                }else if(reg1 == X86Register.EAX){
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                    os.writePOP(X86Register.EDX);
                }else{
                    os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EDX);
                    os.writePOP(X86Register.EAX);
                    os.writePOP(X86Register.EDX);
                }
                break;

            case BinaryQuad.ISHL:   //needs CL
                os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EBP, disp2);
                os.writePUSH(X86Register.CX);
                os.writeMOV(X86Constants.BITS32, X86Register.CX, X86Register.EBP, disp3);
                os.writeSAL_CL((X86Register) reg1);
                os.writePOP(X86Register.CX);
                break;

            case BinaryQuad.ISHR:   //needs CL
                os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EBP, disp2);
                os.writePUSH(X86Register.CX);
                os.writeMOV(X86Constants.BITS32, X86Register.CX, X86Register.EBP, disp3);
                os.writeSAR_CL((X86Register) reg1);
                os.writePOP(X86Register.CX);
                break;

            case BinaryQuad.ISUB:
                os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EBP, disp2);
                os.writeSUB((X86Register) reg1, X86Register.EBP, disp3);
                break;

            case BinaryQuad.IUSHR:  //needs CL
                os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EBP, disp2);
                os.writePUSH(X86Register.CX);
                os.writeMOV(X86Constants.BITS32, X86Register.CX, X86Register.EBP, disp3);
                os.writeSHR_CL((X86Register) reg1);
                os.writePOP(X86Register.CX);
                break;

            case BinaryQuad.IXOR:
                os.writeMOV(X86Constants.BITS32, (X86Register) reg1, X86Register.EBP, disp2);
                os.writeXOR((X86Register) reg1, X86Register.EBP, disp3);
                break;

            case BinaryQuad.DADD:
            case BinaryQuad.DDIV:
            case BinaryQuad.DMUL:
            case BinaryQuad.DREM:
            case BinaryQuad.DSUB:
                throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.FADD:
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFADD32(X86Register.EBP, disp3);
                os.writePUSH((X86Register) reg1);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((X86Register) reg1);
                break;

            case BinaryQuad.FDIV:
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFDIV32(X86Register.EBP, disp3);
                os.writePUSH((X86Register) reg1);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((X86Register) reg1);
                break;

            case BinaryQuad.FMUL:
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFMUL32(X86Register.EBP, disp3);
                os.writePUSH((X86Register) reg1);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((X86Register) reg1);
                break;

            case BinaryQuad.FREM:
                os.writeFLD32(X86Register.EBP, disp3);
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFPREM();
                os.writePUSH((X86Register) reg1);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((X86Register) reg1);
                os.writeFFREE(X86Register.ST0);
                break;

            case BinaryQuad.FSUB:
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFSUB32(X86Register.EBP, disp3);
                os.writePUSH((X86Register) reg1);
                os.writeFSTP32(X86Register.ESP, 0);
                os.writePOP((X86Register) reg1);
                break;

            case BinaryQuad.LADD:
            case BinaryQuad.LAND:
            case BinaryQuad.LDIV:
            case BinaryQuad.LMUL:
            case BinaryQuad.LOR:
            case BinaryQuad.LREM:
            case BinaryQuad.LSHL:
            case BinaryQuad.LSHR:
            case BinaryQuad.LSUB:
            case BinaryQuad.LUSHR:
            case BinaryQuad.LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation");
        }
    }


    /// WE should not get to this method
    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(int, org.jnode.vm.compiler.ir.Constant, int, org.jnode.vm.compiler.ir.Constant)
     */
    public void generateBinaryOP(int disp1, Constant c2, int operation, Constant c3) {
        throw new IllegalArgumentException("Constants should be folded");
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(int, org.jnode.vm.compiler.ir.Constant, int, java.lang.Object)
     */
    public void generateBinaryOP(int disp1, Constant c2, int operation, Object reg3) {
        IntConstant iconst2 = (IntConstant) c2;
        switch (operation) {
            case BinaryQuad.IADD:
                os.writeMOV_Const(X86Register.EBP, disp1, iconst2.getValue());
                os.writeADD(X86Register.EBP, disp1, (X86Register) reg3);
                break;

            case BinaryQuad.IAND:
                os.writeMOV_Const(X86Register.EBP, disp1, iconst2.getValue());
                os.writeAND(X86Register.EBP, disp1, (X86Register) reg3);
                break;

            case BinaryQuad.IDIV:   //needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writeMOV_Const(X86Register.EAX, iconst2.getValue());
                os.writeCDQ();
                if(reg3 == X86Register.EAX){
                    os.writeIDIV_EAX(X86Register.ESP, 0);
                }else if(reg3 == X86Register.EDX){
                    os.writeIDIV_EAX(X86Register.ESP, 4);
                }else{
                    os.writeIDIV_EAX((X86Register)reg3);
                }
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, X86Register.EAX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case BinaryQuad.IMUL:
                os.writePUSH((X86Register) reg3);
                os.writeIMUL_3((X86Register) reg3, (X86Register)reg3, iconst2.getValue());
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register)reg3);
                os.writePOP((X86Register) reg3);
                break;

            case BinaryQuad.IOR:
                os.writeMOV_Const(X86Register.EBP, disp1, iconst2.getValue());
                os.writeOR(X86Register.EBP, disp1, (X86Register) reg3);
                break;

            case BinaryQuad.IREM:   //needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writeMOV_Const(X86Register.EAX, iconst2.getValue());
                os.writeCDQ();
                if(reg3 == X86Register.EAX){
                    os.writeIDIV_EAX(X86Register.ESP, 0);
                }else if(reg3 == X86Register.EDX){
                    os.writeIDIV_EAX(X86Register.ESP, 4);
                }else{
                    os.writeIDIV_EAX((X86Register)reg3);
                }
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, X86Register.EDX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case BinaryQuad.ISHL:   //needs CL
                os.writeMOV_Const(X86Register.EBP, disp1, iconst2.getValue());
                if(reg3 != X86Register.CX){
                    os.writePUSH(X86Register.CX);
                    os.writeMOV(X86Constants.BITS32, X86Register.CX, (X86Register) reg3);
                    os.writeSAL_CL(X86Register.EBP, disp1);
                    os.writePOP(X86Register.CX);
                }else{
                    os.writeSAL_CL(X86Register.EBP, disp1);
                }
                break;

            case BinaryQuad.ISHR:   //needs CL
                os.writeMOV_Const(X86Register.EBP, disp1, iconst2.getValue());
                if(reg3 != X86Register.CX){
                    os.writePUSH(X86Register.CX);
                    os.writeMOV(X86Constants.BITS32, X86Register.CX, (X86Register) reg3);
                    os.writeSAR_CL(X86Register.EBP, disp1);
                    os.writePOP(X86Register.CX);
                }else{
                    os.writeSAR_CL(X86Register.EBP, disp1);
                }
                break;

            case BinaryQuad.ISUB:
                os.writeMOV_Const(X86Register.EBP, disp1, iconst2.getValue());
                os.writeSUB(X86Register.EBP, disp1, (X86Register) reg3);
                break;

            case BinaryQuad.IUSHR:  //needs CL
                os.writeMOV_Const(X86Register.EBP, disp1, iconst2.getValue());
                if(reg3 != X86Register.CX){
                    os.writePUSH(X86Register.CX);
                    os.writeMOV(X86Constants.BITS32, X86Register.CX, (X86Register) reg3);
                    os.writeSHR_CL(X86Register.EBP, disp1);
                    os.writePOP(X86Register.CX);
                }else{
                    os.writeSHR_CL(X86Register.EBP, disp1);
                }
                break;

            case BinaryQuad.IXOR:   //not supported
                os.writeMOV_Const(X86Register.EBP, disp1, iconst2.getValue());
                os.writeXOR(X86Register.EBP, disp1, (X86Register) reg3);
                break;

            case BinaryQuad.DADD:
            case BinaryQuad.DDIV:
            case BinaryQuad.DMUL:
            case BinaryQuad.DREM:
            case BinaryQuad.DSUB:
                throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.FADD:
                os.writeMOV_Const(X86Register.EBP, disp1, iconst2.getValue());
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg3);
                os.writeFADD32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case BinaryQuad.FDIV:
                os.writeMOV_Const(X86Register.EBP, disp1, iconst2.getValue());
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg3);
                os.writeFDIV32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case BinaryQuad.FMUL:
                os.writeMOV_Const(X86Register.EBP, disp1, iconst2.getValue());
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg3);
                os.writeFMUL32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case BinaryQuad.FREM:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg3);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeMOV_Const(X86Register.EBP, disp1, iconst2.getValue());
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFPREM();
                os.writeFSTP32(X86Register.EBP, disp1);
                os.writeFFREE(X86Register.ST0);
                break;

            case BinaryQuad.FSUB:
                os.writeMOV_Const(X86Register.EBP, disp1, iconst2.getValue());
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg3);
                os.writeFSUB32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case BinaryQuad.LADD:
            case BinaryQuad.LAND:
            case BinaryQuad.LDIV:
            case BinaryQuad.LMUL:
            case BinaryQuad.LOR:
            case BinaryQuad.LREM:
            case BinaryQuad.LSHL:
            case BinaryQuad.LSHR:
            case BinaryQuad.LSUB:
            case BinaryQuad.LUSHR:
            case BinaryQuad.LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation");
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(int, org.jnode.vm.compiler.ir.Constant, int, int)
     */
    public void generateBinaryOP(int disp1, Constant c2, int operation, int disp3) {
        IntConstant iconst2 = (IntConstant) c2;
        switch (operation) {
            case BinaryQuad.IADD:
                os.writePUSH(SR1);
                os.writeMOV_Const(SR1, iconst2.getValue());
                os.writeADD(SR1, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, SR1);
                os.writePOP(SR1);
                break;

            case BinaryQuad.IAND:
                os.writePUSH(SR1);
                os.writeMOV_Const(SR1, iconst2.getValue());
                os.writeAND(SR1, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, SR1);
                os.writePOP(SR1);
                break;

            case BinaryQuad.IDIV:
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writeMOV_Const(X86Register.EAX, iconst2.getValue());
                os.writeCDQ();
                os.writeIDIV_EAX(X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, X86Register.EAX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case BinaryQuad.IMUL:
                os.writePUSH(SR1);
                os.writeIMUL_3(SR1, X86Register.EBP, disp3, iconst2.getValue());
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, SR1);
                os.writePOP(SR1);
                break;

            case BinaryQuad.IOR:
                os.writePUSH(SR1);
                os.writeMOV_Const(SR1, iconst2.getValue());
                os.writeOR(SR1, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, SR1);
                os.writePOP(SR1);
                break;

            case BinaryQuad.IREM:
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writeMOV_Const(X86Register.EAX, iconst2.getValue());
                os.writeCDQ();
                os.writeIDIV_EAX(X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, X86Register.EDX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case BinaryQuad.ISHL:
                os.writeMOV_Const(X86Register.EBP, disp1, iconst2.getValue());
                os.writePUSH(X86Register.CX);
                os.writeMOV(X86Constants.BITS32, X86Register.CX, X86Register.EBP, disp3);
                os.writeSAL_CL(X86Register.EBP, disp1);
                os.writePOP(X86Register.CX);
                break;

            case BinaryQuad.ISHR:
                os.writeMOV_Const(X86Register.EBP, disp1, iconst2.getValue());
                os.writePUSH(X86Register.CX);
                os.writeMOV(X86Constants.BITS32, X86Register.CX, X86Register.EBP, disp3);
                os.writeSAR_CL(X86Register.EBP, disp1);
                os.writePOP(X86Register.CX);
                break;

            case BinaryQuad.ISUB:
                os.writePUSH(SR1);
                os.writeMOV_Const(SR1, iconst2.getValue());
                os.writeSUB(SR1, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, SR1);
                os.writePOP(SR1);
                break;

            case BinaryQuad.IUSHR:
                os.writeMOV_Const(X86Register.EBP, disp1, iconst2.getValue());
                os.writePUSH(X86Register.CX);
                os.writeMOV(X86Constants.BITS32, X86Register.CX, X86Register.EBP, disp3);
                os.writeSHR_CL(X86Register.EBP, disp1);
                os.writePOP(X86Register.CX);
                break;

            case BinaryQuad.IXOR:
                os.writePUSH(SR1);
                os.writeMOV_Const(SR1, iconst2.getValue());
                os.writeXOR(SR1, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, SR1);
                os.writePOP(SR1);
                break;

            case BinaryQuad.DADD:
            case BinaryQuad.DDIV:
            case BinaryQuad.DMUL:
            case BinaryQuad.DREM:
            case BinaryQuad.DSUB:
                throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.FADD:
                os.writeMOV_Const(X86Register.EBP, disp1, iconst2.getValue());
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFADD32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case BinaryQuad.FDIV:
                os.writeMOV_Const(X86Register.EBP, disp1, iconst2.getValue());
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFDIV32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case BinaryQuad.FMUL:
                os.writeMOV_Const(X86Register.EBP, disp1, iconst2.getValue());
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFMUL32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case BinaryQuad.FREM:
                os.writeMOV_Const(X86Register.EBP, disp1, iconst2.getValue());
                os.writeFSUB32(X86Register.EBP, disp3);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFPREM();
                os.writeFSTP32(X86Register.EBP, disp1);
                os.writeFFREE(X86Register.ST0);
                break;

            case BinaryQuad.FSUB:
                os.writeMOV_Const(X86Register.EBP, disp1, iconst2.getValue());
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFSUB32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case BinaryQuad.LADD:
            case BinaryQuad.LAND:
            case BinaryQuad.LDIV:
            case BinaryQuad.LMUL:
            case BinaryQuad.LOR:
            case BinaryQuad.LREM:
            case BinaryQuad.LSHL:
            case BinaryQuad.LSHR:
            case BinaryQuad.LSUB:
            case BinaryQuad.LUSHR:
            case BinaryQuad.LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation");
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(int, java.lang.Object, int, org.jnode.vm.compiler.ir.Constant)
     */
    public void generateBinaryOP(int disp1, Object reg2, int operation, Constant c3) {
        IntConstant iconst3 = (IntConstant) c3;
        switch (operation) {

            case BinaryQuad.IADD:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg2);
                os.writeADD(X86Register.EBP, disp1, iconst3.getValue());
                break;

            case BinaryQuad.IAND:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg2);
                os.writeAND(X86Register.EBP, disp1, iconst3.getValue());
                break;

            case BinaryQuad.IDIV:   //needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writePUSH(iconst3.getValue());
                if(reg2 != X86Register.EAX){
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, (X86Register)reg2);
                }
                os.writeCDQ();
                os.writeIDIV_EAX(X86Register.ESP, 0);
                os.writePOP(X86Register.EDX);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, X86Register.EAX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case BinaryQuad.IMUL:
                os.writePUSH((X86Register) reg2);
                os.writeIMUL_3((X86Register) reg2, (X86Register)reg2, iconst3.getValue());
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register)reg2);
                os.writePOP((X86Register) reg2);
                break;

            case BinaryQuad.IOR:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg2);
                os.writeOR(X86Register.EBP, disp1, iconst3.getValue());
                break;

            case BinaryQuad.IREM:   //needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writePUSH(iconst3.getValue());
                if(reg2 != X86Register.EAX){
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, (X86Register)reg2);
                }
                os.writeCDQ();
                os.writeIDIV_EAX(X86Register.ESP, 0);
                os.writePOP(X86Register.EAX);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, X86Register.EDX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case BinaryQuad.ISHL:   //needs CL
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg2);
                os.writeSAL(X86Register.EBP, disp1, iconst3.getValue());
                break;

            case BinaryQuad.ISHR:   //needs CL
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg2);
                os.writeSAR(X86Register.EBP, disp1, iconst3.getValue());
                break;

            case BinaryQuad.ISUB:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg2);
                os.writeSUB(X86Register.EBP, disp1, iconst3.getValue());
                break;

            case BinaryQuad.IUSHR:  //needs CL
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg2);
                os.writeSHR(X86Register.EBP, disp1, iconst3.getValue());
                break;

            case BinaryQuad.IXOR:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg2);
                os.writeXOR(X86Register.EBP, disp1, iconst3.getValue());
                break;

            case BinaryQuad.DADD:
            case BinaryQuad.DDIV:
            case BinaryQuad.DMUL:
            case BinaryQuad.DREM:
            case BinaryQuad.DSUB:
                throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.FADD:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg2);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeMOV_Const(X86Register.EBP, disp1, iconst3.getValue());
                os.writeFADD32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case BinaryQuad.FDIV:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg2);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeMOV_Const(X86Register.EBP, disp1, iconst3.getValue());
                os.writeFDIV32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case BinaryQuad.FMUL:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg2);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeMOV_Const(X86Register.EBP, disp1, iconst3.getValue());
                os.writeFMUL32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case BinaryQuad.FREM:
                os.writeMOV_Const(X86Register.EBP, disp1, iconst3.getValue());
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg2);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFPREM();
                os.writeFSTP32(X86Register.EBP, disp1);
                os.writeFFREE(X86Register.ST0);
                break;

            case BinaryQuad.FSUB:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg2);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeMOV_Const(X86Register.EBP, disp1, iconst3.getValue());
                os.writeFSUB32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case BinaryQuad.LADD:
            case BinaryQuad.LAND:
            case BinaryQuad.LDIV:
            case BinaryQuad.LMUL:
            case BinaryQuad.LOR:
            case BinaryQuad.LREM:
            case BinaryQuad.LSHL:
            case BinaryQuad.LSHR:
            case BinaryQuad.LSUB:
            case BinaryQuad.LUSHR:
            case BinaryQuad.LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation");
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(int, java.lang.Object, int, java.lang.Object)
     */
    public void generateBinaryOP(int disp1, Object reg2, int operation, Object reg3) {
        switch (operation) {
            case BinaryQuad.IADD:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg2);
                os.writeADD(X86Register.EBP, disp1, (X86Register) reg3);
                break;

            case BinaryQuad.IAND:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg2);
                os.writeAND(X86Register.EBP, disp1, (X86Register) reg3);
                break;

            case BinaryQuad.IDIV:   //needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                if(reg2 != X86Register.EAX){
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, (X86Register)reg2);
                }
                os.writeCDQ();
                if(reg3 == X86Register.EAX){
                    os.writeIDIV_EAX(X86Register.ESP, 0);
                }else if(reg3 == X86Register.EDX){
                    os.writeIDIV_EAX(X86Register.ESP, 4);
                }else{
                    os.writeIDIV_EAX((X86Register)reg3);
                }
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, X86Register.EAX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case BinaryQuad.IMUL:
                os.writePUSH((X86Register) reg2);
                os.writeIMUL((X86Register) reg2, (X86Register)reg3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register)reg2);
                os.writePOP((X86Register) reg2);
                break;

            case BinaryQuad.IOR:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg2);
                os.writeOR(X86Register.EBP, disp1, (X86Register) reg3);
                break;

            case BinaryQuad.IREM:   //needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                if(reg2 != X86Register.EAX){
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, (X86Register)reg2);
                }
                os.writeCDQ();
                if(reg3 == X86Register.EAX){
                    os.writeIDIV_EAX(X86Register.ESP, 0);
                }else if(reg3 == X86Register.EDX){
                    os.writeIDIV_EAX(X86Register.ESP, 4);
                }else{
                    os.writeIDIV_EAX((X86Register)reg3);
                }
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, X86Register.EDX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case BinaryQuad.ISHL:   //needs CL
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg2);
                if(reg3 != X86Register.CX){
                    os.writePUSH(X86Register.CX);
                    os.writeMOV(X86Constants.BITS32, X86Register.CX, (X86Register) reg3);
                    os.writeSAL_CL(X86Register.EBP, disp1);
                    os.writePOP(X86Register.CX);
                }else{
                    os.writeSAL_CL(X86Register.EBP, disp1);
                }
                break;

            case BinaryQuad.ISHR:   //needs CL
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg2);
                if(reg3 != X86Register.CX){
                    os.writePUSH(X86Register.CX);
                    os.writeMOV(X86Constants.BITS32, X86Register.CX, (X86Register) reg3);
                    os.writeSAR_CL(X86Register.EBP, disp1);
                    os.writePOP(X86Register.CX);
                }else{
                    os.writeSAR_CL(X86Register.EBP, disp1);
                }
                break;

            case BinaryQuad.ISUB:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg2);
                os.writeSUB(X86Register.EBP, disp1, (X86Register) reg3);
                break;

            case BinaryQuad.IUSHR:  //needs CL
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg2);
                if(reg3 != X86Register.CX){
                    os.writePUSH(X86Register.CX);
                    os.writeMOV(X86Constants.BITS32, X86Register.CX, (X86Register) reg3);
                    os.writeSHR_CL(X86Register.EBP, disp1);
                    os.writePOP(X86Register.CX);
                }else{
                    os.writeSHR_CL(X86Register.EBP, disp1);
                }
                break;

            case BinaryQuad.IXOR:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg2);
                os.writeXOR(X86Register.EBP, disp1, (X86Register) reg3);
                break;

            case BinaryQuad.DADD:
            case BinaryQuad.DDIV:
            case BinaryQuad.DMUL:
            case BinaryQuad.DREM:
            case BinaryQuad.DSUB:
                throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.FADD:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg2);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg3);
                os.writeFADD32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case BinaryQuad.FDIV:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg2);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg3);
                os.writeFDIV32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case BinaryQuad.FMUL:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg2);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg3);
                os.writeFMUL32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case BinaryQuad.FREM:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg3);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg2);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFPREM();
                os.writeFSTP32(X86Register.EBP, disp1);
                os.writeFFREE(X86Register.ST0);
                break;

            case BinaryQuad.FSUB:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg2);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg3);
                os.writeFSUB32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case BinaryQuad.LADD:
            case BinaryQuad.LAND:
            case BinaryQuad.LDIV:
            case BinaryQuad.LMUL:
            case BinaryQuad.LOR:
            case BinaryQuad.LREM:
            case BinaryQuad.LSHL:
            case BinaryQuad.LSHR:
            case BinaryQuad.LSUB:
            case BinaryQuad.LUSHR:
            case BinaryQuad.LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation");
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(int, java.lang.Object, int, int)
     */
    public void generateBinaryOP(int disp1, Object reg2, int operation, int disp3) {
        switch (operation) {
            case BinaryQuad.IADD:
                os.writePUSH((X86Register) reg2);
                os.writeADD((X86Register) reg2, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg2);
                os.writePOP((X86Register) reg2);
                break;


            case BinaryQuad.IAND:
                os.writePUSH((X86Register) reg2);
                os.writeAND((X86Register) reg2, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg2);
                os.writePOP((X86Register) reg2);
                break;

            case BinaryQuad.IDIV:   //needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                if(reg2 != X86Register.EAX){
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, (X86Register)reg2);
                }
                os.writeCDQ();
                os.writeIDIV_EAX(X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, X86Register.EAX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case BinaryQuad.IMUL:
                os.writePUSH((X86Register) reg2);
                os.writeIMUL((X86Register) reg2, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg2);
                os.writePOP((X86Register) reg2);
                break;


            case BinaryQuad.IOR:
                os.writePUSH((X86Register) reg2);
                os.writeOR((X86Register) reg2, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg2);
                os.writePOP((X86Register) reg2);
                break;

            case BinaryQuad.IREM:   //needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                if(reg2 != X86Register.EAX){
                    os.writeMOV(X86Constants.BITS32, X86Register.EAX, (X86Register)reg2);
                }
                os.writeCDQ();
                os.writeIDIV_EAX(X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, X86Register.EDX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case BinaryQuad.ISHL:   //needs CL
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg2);
                os.writePUSH(X86Register.CX);
                os.writeMOV(X86Constants.BITS32, X86Register.CX, X86Register.EBP, disp3);
                os.writeSAL_CL(X86Register.EBP, disp1);
                os.writePOP(X86Register.CX);
                break;

            case BinaryQuad.ISHR:   //needs CL
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg2);
                os.writePUSH(X86Register.CX);
                os.writeMOV(X86Constants.BITS32, X86Register.CX, X86Register.EBP, disp3);
                os.writeSAR_CL(X86Register.EBP, disp1);
                os.writePOP(X86Register.CX);
                break;

            case BinaryQuad.ISUB:
                os.writePUSH((X86Register) reg2);
                os.writeSUB((X86Register) reg2, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg2);
                os.writePOP((X86Register) reg2);
                break;

            case BinaryQuad.IUSHR:  //needs CL
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg2);
                os.writePUSH(X86Register.CX);
                os.writeMOV(X86Constants.BITS32, X86Register.CX, X86Register.EBP, disp3);
                os.writeSHR_CL(X86Register.EBP, disp1);
                os.writePOP(X86Register.CX);
                break;

            case BinaryQuad.IXOR:
                os.writePUSH((X86Register) reg2);
                os.writeXOR((X86Register) reg2, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg2);
                os.writePOP((X86Register) reg2);
                break;

            case BinaryQuad.DADD:
            case BinaryQuad.DDIV:
            case BinaryQuad.DMUL:
            case BinaryQuad.DREM:
            case BinaryQuad.DSUB:
                throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.FADD:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg2);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFADD32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case BinaryQuad.FDIV:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg2);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFDIV32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case BinaryQuad.FMUL:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg2);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFMUL32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case BinaryQuad.FREM:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg2);
                os.writeFLD32(X86Register.EBP, disp3);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFPREM();
                os.writeFSTP32(X86Register.EBP, disp1);
                os.writeFFREE(X86Register.ST0);
                break;

            case BinaryQuad.FSUB:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg2);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFSUB32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case BinaryQuad.LADD:
            case BinaryQuad.LAND:
            case BinaryQuad.LDIV:
            case BinaryQuad.LMUL:
            case BinaryQuad.LOR:
            case BinaryQuad.LREM:
            case BinaryQuad.LSHL:
            case BinaryQuad.LSHR:
            case BinaryQuad.LSUB:
            case BinaryQuad.LUSHR:
            case BinaryQuad.LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation");
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(int, int, int, org.jnode.vm.compiler.ir.Constant)
     */
    public void generateBinaryOP(int disp1, int disp2, int operation, Constant c3) {
        IntConstant iconst3 = (IntConstant) c3;
        switch (operation) {
            case BinaryQuad.IADD:   //not supported due to the move bellow
                if(disp1 != disp2){
                    os.writePUSH(X86Register.EBP,disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writeADD(X86Register.EBP, disp1, iconst3.getValue());
                break;

            case BinaryQuad.IAND:
                if(disp1 != disp2){
                    os.writePUSH(X86Register.EBP,disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writeAND(X86Register.EBP, disp1, iconst3.getValue());
                break;

            case BinaryQuad.IDIV:   //needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writePUSH(iconst3.getValue());
                os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EBP, disp2);
                os.writeCDQ();
                os.writeIDIV_EAX(X86Register.ESP, 0);
                os.writePOP(X86Register.EDX);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, X86Register.EAX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case BinaryQuad.IMUL:
                os.writePUSH(SR1);
                os.writeIMUL_3(SR1, X86Register.EBP, disp2, iconst3.getValue());
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, SR1);
                os.writePOP(SR1);
                break;

            case BinaryQuad.IOR:
                if(disp1 != disp2){
                    os.writePUSH(X86Register.EBP,disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writeOR(X86Register.EBP, disp1, iconst3.getValue());
                break;

            case BinaryQuad.IREM:   //needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writePUSH(iconst3.getValue());
                os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EBP, disp2);
                os.writeCDQ();
                os.writeIDIV_EAX(X86Register.ESP, 0);
                os.writePOP(X86Register.EAX);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, X86Register.EDX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case BinaryQuad.ISHL:   //needs CL
                if(disp1 != disp2){
                    os.writePUSH(X86Register.EBP,disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writeSAL(X86Register.EBP, disp1, iconst3.getValue());
                break;

            case BinaryQuad.ISHR:   //needs CL
                if(disp1 != disp2){
                    os.writePUSH(X86Register.EBP,disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writeSAR(X86Register.EBP, disp1, iconst3.getValue());
                break;

            case BinaryQuad.ISUB:   //not supported
                if(disp1 != disp2){
                    os.writePUSH(X86Register.EBP,disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writeSUB(X86Register.EBP, disp1, iconst3.getValue());
                break;

            case BinaryQuad.IUSHR:  //needs CL
                if(disp1 != disp2){
                    os.writePUSH(X86Register.EBP,disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writeSHR(X86Register.EBP, disp1, iconst3.getValue());
                break;

            case BinaryQuad.IXOR:   //not supported
                if(disp1 != disp2){
                    os.writePUSH(X86Register.EBP,disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writeXOR(X86Register.EBP, disp1, iconst3.getValue());
                break;

            case BinaryQuad.DADD:
            case BinaryQuad.DDIV:
            case BinaryQuad.DMUL:
            case BinaryQuad.DREM:
            case BinaryQuad.DSUB:
                throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.FADD:
                os.writeMOV_Const(X86Register.EBP, disp1, iconst3.getValue());
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFADD32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case BinaryQuad.FDIV:
                os.writeMOV_Const(X86Register.EBP, disp1, iconst3.getValue());
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFDIV32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case BinaryQuad.FMUL:
                os.writeMOV_Const(X86Register.EBP, disp1, iconst3.getValue());
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFMUL32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case BinaryQuad.FREM:
                os.writeMOV_Const(X86Register.EBP, disp1, iconst3.getValue());
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFPREM();
                os.writeFSTP32(X86Register.EBP, disp1);
                os.writeFFREE(X86Register.ST0);
                break;

            case BinaryQuad.FSUB:
                os.writeMOV_Const(X86Register.EBP, disp1, iconst3.getValue());
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFSUB32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case BinaryQuad.LADD:
            case BinaryQuad.LAND:
            case BinaryQuad.LDIV:
            case BinaryQuad.LMUL:
            case BinaryQuad.LOR:
            case BinaryQuad.LREM:
            case BinaryQuad.LSHL:
            case BinaryQuad.LSHR:
            case BinaryQuad.LSUB:
            case BinaryQuad.LUSHR:
            case BinaryQuad.LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation");
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(int, int, int, java.lang.Object)
     */
    public void generateBinaryOP(int disp1, int disp2, int operation, Object reg3) {
        switch (operation) {
            case BinaryQuad.IADD:
                if(disp1 != disp2){
                    os.writePUSH(X86Register.EBP,disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writeADD(X86Register.EBP, disp1, (X86Register)reg3);
                break;


            case BinaryQuad.IAND:
                if(disp1 != disp2){
                    os.writePUSH(X86Register.EBP,disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writeAND(X86Register.EBP, disp1, (X86Register)reg3);
                break;

            case BinaryQuad.IDIV:   //needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EBP, disp2);
                os.writeCDQ();
                if(reg3 == X86Register.EAX){
                    os.writeIDIV_EAX(X86Register.ESP, 0);
                }else if(reg3 == X86Register.EDX){
                    os.writeIDIV_EAX(X86Register.ESP, 4);
                }else{
                    os.writeIDIV_EAX((X86Register)reg3);
                }
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, X86Register.EAX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case BinaryQuad.IMUL:
                os.writePUSH((X86Register) reg3);
                os.writeIMUL((X86Register) reg3, X86Register.EBP, disp2);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register)reg3);
                os.writePOP((X86Register) reg3);
                break;

            case BinaryQuad.IOR:
                if(disp1 != disp2){
                    os.writePUSH(X86Register.EBP,disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writeOR(X86Register.EBP, disp1, (X86Register)reg3);
                break;

            case BinaryQuad.IREM:   //needs EAX
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EBP, disp2);
                os.writeCDQ();
                if(reg3 == X86Register.EAX){
                    os.writeIDIV_EAX(X86Register.ESP, 0);
                }else if(reg3 == X86Register.EDX){
                    os.writeIDIV_EAX(X86Register.ESP, 4);
                }else{
                    os.writeIDIV_EAX((X86Register)reg3);
                }
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, X86Register.EDX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case BinaryQuad.ISHL:   //needs CL
                if(disp1 != disp2){
                    os.writePUSH(X86Register.EBP,disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                if(reg3 != X86Register.CX){
                    os.writePUSH(X86Register.CX);
                    os.writeMOV(X86Constants.BITS32, X86Register.CX, (X86Register) reg3);
                    os.writeSAL_CL(X86Register.EBP, disp1);
                    os.writePOP(X86Register.CX);
                }else{
                    os.writeSAL_CL(X86Register.EBP, disp1);
                }
                break;

            case BinaryQuad.ISHR:   //needs CL
                if(disp1 != disp2){
                    os.writePUSH(X86Register.EBP,disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                if(reg3 != X86Register.CX){
                    os.writePUSH(X86Register.CX);
                    os.writeMOV(X86Constants.BITS32, X86Register.CX, (X86Register) reg3);
                    os.writeSAR_CL(X86Register.EBP, disp1);
                    os.writePOP(X86Register.CX);
                }else{
                    os.writeSAR_CL(X86Register.EBP, disp1);
                }
                break;

            case BinaryQuad.ISUB:
                if(disp1 != disp2){
                    os.writePUSH(X86Register.EBP,disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writeSUB(X86Register.EBP, disp1, (X86Register)reg3);
                break;

            case BinaryQuad.IUSHR:  //needs CL
                if(disp1 != disp2){
                    os.writePUSH(X86Register.EBP,disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                if(reg3 != X86Register.CX){
                    os.writePUSH(X86Register.CX);
                    os.writeMOV(X86Constants.BITS32, X86Register.CX, (X86Register) reg3);
                    os.writeSHR_CL(X86Register.EBP, disp1);
                    os.writePOP(X86Register.CX);
                }else{
                    os.writeSHR_CL(X86Register.EBP, disp1);
                }
                break;

            case BinaryQuad.IXOR:
                if(disp1 != disp2){
                    os.writePUSH(X86Register.EBP,disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writeXOR(X86Register.EBP, disp1, (X86Register)reg3);
                break;

            case BinaryQuad.DADD:
            case BinaryQuad.DDIV:
            case BinaryQuad.DMUL:
            case BinaryQuad.DREM:
            case BinaryQuad.DSUB:
                throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.FADD:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg3);
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFADD32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case BinaryQuad.FDIV:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg3);
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFDIV32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case BinaryQuad.FMUL:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg3);
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFMUL32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case BinaryQuad.FREM:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg3);
                os.writeFLD32(X86Register.EBP, disp1);
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFPREM();
                os.writeFSTP32(X86Register.EBP, disp1);
                os.writeFFREE(X86Register.ST0);
                break;

            case BinaryQuad.FSUB:
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, (X86Register) reg3);
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFSUB32(X86Register.EBP, disp1);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case BinaryQuad.LADD:
            case BinaryQuad.LAND:
            case BinaryQuad.LDIV:
            case BinaryQuad.LMUL:
            case BinaryQuad.LOR:
            case BinaryQuad.LREM:
            case BinaryQuad.LSHL:
            case BinaryQuad.LSHR:
            case BinaryQuad.LSUB:
            case BinaryQuad.LUSHR:
            case BinaryQuad.LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation");
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(int, int, int, int)
     */
    public void generateBinaryOP(int disp1, int disp2, int operation, int disp3) {
        switch (operation) {
            case BinaryQuad.IADD:
                os.writePUSH(SR1);
                os.writeMOV(X86Constants.BITS32, SR1, X86Register.EBP, disp2);
                os.writeADD(SR1, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, SR1);
                os.writePOP(SR1);
                break;

            case BinaryQuad.IAND:
                os.writePUSH(SR1);
                os.writeMOV(X86Constants.BITS32, SR1, X86Register.EBP, disp2);
                os.writeAND(SR1, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, SR1);
                os.writePOP(SR1);
                break;

            case BinaryQuad.IDIV:
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EBP, disp2);
                os.writeCDQ();
                os.writeIDIV_EAX(X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, X86Register.EAX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case BinaryQuad.IMUL:
                os.writePUSH(SR1);
                os.writeMOV(X86Constants.BITS32, SR1, X86Register.EBP, disp2);
                os.writeIMUL(SR1, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, SR1);
                os.writePOP(SR1);
                break;

            case BinaryQuad.IOR:
                os.writePUSH(SR1);
                os.writeMOV(X86Constants.BITS32, SR1, X86Register.EBP, disp2);
                os.writeOR(SR1, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, SR1);
                os.writePOP(SR1);
                break;

            case BinaryQuad.IREM:
                os.writePUSH(X86Register.EDX);
                os.writePUSH(X86Register.EAX);
                os.writeMOV(X86Constants.BITS32, X86Register.EAX, X86Register.EBP, disp2);
                os.writeCDQ();
                os.writeIDIV_EAX(X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, X86Register.EDX);
                os.writePOP(X86Register.EAX);
                os.writePOP(X86Register.EDX);
                break;

            case BinaryQuad.ISHL:
                if(disp1 != disp2){
                    os.writePUSH(X86Register.EBP,disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writePUSH(X86Register.CX);
                os.writeMOV(X86Constants.BITS32, X86Register.CX, X86Register.EBP, disp3);
                os.writeSAL_CL(X86Register.EBP, disp1);
                os.writePOP(X86Register.CX);
                break;

            case BinaryQuad.ISHR:
                if(disp1 != disp2){
                    os.writePUSH(X86Register.EBP,disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writePUSH(X86Register.CX);
                os.writeMOV(X86Constants.BITS32, X86Register.CX, X86Register.EBP, disp3);
                os.writeSAR_CL(X86Register.EBP, disp1);
                os.writePOP(X86Register.CX);
                break;

            case BinaryQuad.ISUB:
                os.writePUSH(SR1);
                os.writeMOV(X86Constants.BITS32, SR1, X86Register.EBP, disp2);
                os.writeSUB(SR1, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, SR1);
                os.writePOP(SR1);
                break;

            case BinaryQuad.IUSHR:
                if(disp1 != disp2){
                    os.writePUSH(X86Register.EBP,disp2);
                    os.writePOP(X86Register.EBP, disp1);
                }
                os.writePUSH(X86Register.CX);
                os.writeMOV(X86Constants.BITS32, X86Register.CX, X86Register.EBP, disp3);
                os.writeSHR_CL(X86Register.EBP, disp1);
                os.writePOP(X86Register.CX);
                break;

            case BinaryQuad.IXOR:
                os.writePUSH(SR1);
                os.writeMOV(X86Constants.BITS32, SR1, X86Register.EBP, disp2);
                os.writeXOR(SR1, X86Register.EBP, disp3);
                os.writeMOV(X86Constants.BITS32, X86Register.EBP, disp1, SR1);
                os.writePOP(SR1);
                break;

            case BinaryQuad.DADD:
                os.writeFLD64(X86Register.EBP, disp2);
                os.writeFADD64(X86Register.EBP, disp3);
                os.writeFSTP64(X86Register.EBP, disp1);
                break;

            case BinaryQuad.DDIV:
                os.writeFLD64(X86Register.EBP, disp2);
                os.writeFDIV64(X86Register.EBP, disp3);
                os.writeFSTP64(X86Register.EBP, disp1);
                break;

            case BinaryQuad.DMUL:
                os.writeFLD64(X86Register.EBP, disp2);
                os.writeFMUL64(X86Register.EBP, disp3);
                os.writeFSTP64(X86Register.EBP, disp1);
                break;

            case BinaryQuad.DREM:
                os.writeFLD64(X86Register.EBP, disp3);
                os.writeFLD64(X86Register.EBP, disp2);
                os.writeFPREM();
                os.writeFSTP64(X86Register.EBP, disp1);
                os.writeFFREE(X86Register.ST0);
                break;

            case BinaryQuad.DSUB:
                os.writeFLD64(X86Register.EBP, disp2);
                os.writeFSUB64(X86Register.EBP, disp3);
                os.writeFSTP64(X86Register.EBP, disp1);
                break;

            case BinaryQuad.FADD:
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFADD32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case BinaryQuad.FDIV:
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFDIV32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case BinaryQuad.FMUL:
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFMUL32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case BinaryQuad.FREM:
                os.writeFLD32(X86Register.EBP, disp3);
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFPREM();
                os.writeFSTP32(X86Register.EBP, disp1);
                os.writeFFREE(X86Register.ST0);
                break;

            case BinaryQuad.FSUB:
                os.writeFLD32(X86Register.EBP, disp2);
                os.writeFSUB32(X86Register.EBP, disp3);
                os.writeFSTP32(X86Register.EBP, disp1);
                break;

            case BinaryQuad.LADD:
            case BinaryQuad.LAND:
            case BinaryQuad.LDIV:
            case BinaryQuad.LMUL:
            case BinaryQuad.LOR:
            case BinaryQuad.LREM:
            case BinaryQuad.LSHL:
            case BinaryQuad.LSHR:
            case BinaryQuad.LSUB:
            case BinaryQuad.LUSHR:
            case BinaryQuad.LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation");
        }
    }


    /********** BRANCHES ***************************************/
    /**
     * @param quad
     * @param condition
     * @param reg
     */
    public void generateCodeFor(ConditionalBranchQuad quad, int condition, Object reg) {
        checkLabel(quad.getAddress());
        os.writeTEST((X86Register) reg, (X86Register) reg);
        generateJumpForUnaryCondition(quad, condition);
    }

    /**
     * @param quad
     * @param condition
     * @param disp
     */
    public void generateCodeFor(ConditionalBranchQuad quad, int condition, int disp) {
        checkLabel(quad.getAddress());
        os.writeCMP_Const(X86Register.EBP, disp, 0);
        generateJumpForUnaryCondition(quad, condition);
    }

    private void generateJumpForUnaryCondition(ConditionalBranchQuad quad, int condition) {
        switch (condition) {
            case ConditionalBranchQuad.IFEQ:
                os.writeJCC(getInstrLabel(quad.getTargetAddress()), X86Constants.JE);
                break;

            case ConditionalBranchQuad.IFNE:
                os.writeJCC(getInstrLabel(quad.getTargetAddress()), X86Constants.JNE);
                break;

            case ConditionalBranchQuad.IFGT:
                os.writeJCC(getInstrLabel(quad.getTargetAddress()), X86Constants.JG);
                break;

            case ConditionalBranchQuad.IFGE:
                os.writeJCC(getInstrLabel(quad.getTargetAddress()), X86Constants.JGE);
                break;

            case ConditionalBranchQuad.IFLT:
                os.writeJCC(getInstrLabel(quad.getTargetAddress()), X86Constants.JL);
                break;

            case ConditionalBranchQuad.IFLE:
                os.writeJCC(getInstrLabel(quad.getTargetAddress()), X86Constants.JLE);
                break;

            case ConditionalBranchQuad.IFNULL:
                os.writeJCC(getInstrLabel(quad.getTargetAddress()), X86Constants.JE);
                break;

            case ConditionalBranchQuad.IFNONNULL:
                os.writeJCC(getInstrLabel(quad.getTargetAddress()), X86Constants.JNE);
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
    public void generateCodeFor(ConditionalBranchQuad quad, int condition, Constant cons) {
        switch (condition) {
            case ConditionalBranchQuad.IFEQ:
            case ConditionalBranchQuad.IFNE:
            case ConditionalBranchQuad.IFGT:
            case ConditionalBranchQuad.IFGE:
            case ConditionalBranchQuad.IFLT:
            case ConditionalBranchQuad.IFLE:
            case ConditionalBranchQuad.IFNULL:
            case ConditionalBranchQuad.IFNONNULL:
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
    public void generateCodeFor(ConditionalBranchQuad quad, Constant c1, int condition, Constant c2) {
        switch (condition) {
            case ConditionalBranchQuad.IF_ICMPEQ:
            case ConditionalBranchQuad.IF_ICMPNE:
            case ConditionalBranchQuad.IF_ICMPGT:
            case ConditionalBranchQuad.IF_ICMPGE:
            case ConditionalBranchQuad.IF_ICMPLT:
            case ConditionalBranchQuad.IF_ICMPLE:
            case ConditionalBranchQuad.IF_ACMPEQ:
            case ConditionalBranchQuad.IF_ACMPNE:
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
    public void generateCodeFor(ConditionalBranchQuad quad, Constant c1, int condition, int disp2) {
        switch (condition) {
            case ConditionalBranchQuad.IF_ICMPEQ:
            case ConditionalBranchQuad.IF_ICMPNE:
            case ConditionalBranchQuad.IF_ICMPGT:
            case ConditionalBranchQuad.IF_ICMPGE:
            case ConditionalBranchQuad.IF_ICMPLT:
            case ConditionalBranchQuad.IF_ICMPLE:
            case ConditionalBranchQuad.IF_ACMPEQ:
            case ConditionalBranchQuad.IF_ACMPNE:
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
    public void generateCodeFor(ConditionalBranchQuad quad, Constant c1, int condition, Object reg2) {
        switch (condition) {
            case ConditionalBranchQuad.IF_ICMPEQ:
            case ConditionalBranchQuad.IF_ICMPNE:
            case ConditionalBranchQuad.IF_ICMPGT:
            case ConditionalBranchQuad.IF_ICMPGE:
            case ConditionalBranchQuad.IF_ICMPLT:
            case ConditionalBranchQuad.IF_ICMPLE:
            case ConditionalBranchQuad.IF_ACMPEQ:
            case ConditionalBranchQuad.IF_ACMPNE:
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
    public void generateCodeFor(ConditionalBranchQuad quad, int disp1, int condition, Constant c2) {
        checkLabel(quad.getAddress());
        os.writeCMP_Const(X86Register.EBP, disp1, ((IntConstant) c2).getValue());
        generateJumpForBinaryCondition(quad, condition);
    }

    /**
     * @param quad
     * @param disp1
     * @param condition
     * @param disp2
     */
    public void generateCodeFor(ConditionalBranchQuad quad, int disp1, int condition, int disp2) {
        switch (condition) {
            case ConditionalBranchQuad.IF_ICMPEQ:
            case ConditionalBranchQuad.IF_ICMPNE:
            case ConditionalBranchQuad.IF_ICMPGT:
            case ConditionalBranchQuad.IF_ICMPGE:
            case ConditionalBranchQuad.IF_ICMPLT:
            case ConditionalBranchQuad.IF_ICMPLE:
            case ConditionalBranchQuad.IF_ACMPEQ:
            case ConditionalBranchQuad.IF_ACMPNE:
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
    public void generateCodeFor(ConditionalBranchQuad quad, int disp1, int condition, Object reg2) {
        checkLabel(quad.getAddress());
        os.writeCMP(X86Register.EBP, disp1, (X86Register) reg2);
        generateJumpForBinaryCondition(quad, condition);
    }

    /**
     * @param quad
     * @param reg1
     * @param condition
     * @param c2
     */
    public void generateCodeFor(ConditionalBranchQuad quad, Object reg1, int condition, Constant c2) {
        checkLabel(quad.getAddress());
        os.writeCMP_Const((X86Register) reg1, ((IntConstant) c2).getValue());
        generateJumpForBinaryCondition(quad, condition);
    }

    /**
     * @param quad
     * @param reg1
     * @param condition
     * @param disp2
     */
    public void generateCodeFor(ConditionalBranchQuad quad, Object reg1, int condition, int disp2) {
        checkLabel(quad.getAddress());
        os.writeCMP((X86Register) reg1, X86Register.EBP, disp2);
        generateJumpForBinaryCondition(quad, condition);
    }

    /**
     * @param quad
     * @param reg1
     * @param condition
     * @param reg2
     */
    public void generateCodeFor(ConditionalBranchQuad quad, Object reg1, int condition, Object reg2) {
        checkLabel(quad.getAddress());
        os.writeCMP((X86Register) reg1, (X86Register) reg2);
        generateJumpForBinaryCondition(quad, condition);
    }

    private void generateJumpForBinaryCondition(ConditionalBranchQuad quad, int condition) {
        switch (condition) {
            case ConditionalBranchQuad.IF_ICMPEQ:
                os.writeJCC(getInstrLabel(quad.getTargetAddress()), X86Constants.JE);
                break;

            case ConditionalBranchQuad.IF_ICMPNE:
                os.writeJCC(getInstrLabel(quad.getTargetAddress()), X86Constants.JNE);
                break;

            case ConditionalBranchQuad.IF_ICMPGT:
                os.writeJCC(getInstrLabel(quad.getTargetAddress()), X86Constants.JG);
                break;

            case ConditionalBranchQuad.IF_ICMPGE:
                os.writeJCC(getInstrLabel(quad.getTargetAddress()), X86Constants.JGE);
                break;

            case ConditionalBranchQuad.IF_ICMPLT:
                os.writeJCC(getInstrLabel(quad.getTargetAddress()), X86Constants.JL);
                break;

            case ConditionalBranchQuad.IF_ICMPLE:
                os.writeJCC(getInstrLabel(quad.getTargetAddress()), X86Constants.JLE);
                break;

            case ConditionalBranchQuad.IF_ACMPEQ:
                os.writeJCC(getInstrLabel(quad.getTargetAddress()), X86Constants.JE);
                break;

            case ConditionalBranchQuad.IF_ACMPNE:
                os.writeJCC(getInstrLabel(quad.getTargetAddress()), X86Constants.JNE);
                break;

            default:
                throw new IllegalArgumentException("Unknown condition " + condition);
        }
    }
}
