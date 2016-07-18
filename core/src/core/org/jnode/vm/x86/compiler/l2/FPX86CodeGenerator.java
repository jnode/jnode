package org.jnode.vm.x86.compiler.l2;

import org.jnode.assembler.Label;
import org.jnode.assembler.x86.X86Assembler;
import org.jnode.assembler.x86.X86Constants;
import org.jnode.assembler.x86.X86Register;
import org.jnode.assembler.x86.X86Register.GPR;
import org.jnode.vm.compiler.ir.AddressingMode;
import org.jnode.vm.compiler.ir.DoubleConstant;
import org.jnode.vm.compiler.ir.FloatConstant;
import org.jnode.vm.compiler.ir.Operand;
import org.jnode.vm.compiler.ir.RegisterLocation;
import org.jnode.vm.compiler.ir.StackLocation;
import org.jnode.vm.compiler.ir.Variable;
import org.jnode.vm.compiler.ir.quad.BinaryOperation;
import org.jnode.vm.compiler.ir.quad.BinaryQuad;

/**
 * User: lsantha
 * Date: 3/13/15 10:51 PM
 */
public class FPX86CodeGenerator<T> {
    protected X86Assembler os;
    GenericX86CodeGenerator context;

    public FPX86CodeGenerator(X86Assembler x86Stream, GenericX86CodeGenerator context) {
        os = x86Stream;
        this.context = context;
    }

    public void generateBinaryOP(BinaryQuad<T> quad) {
        context.checkLabel(quad.getAddress());
        Variable lsh = quad.getLHS();
        Operand op1 = quad.getOperand1();
        Operand op2 = quad.getOperand2();
        BinaryOperation operation = quad.getOperation();
        switch (operation) {
            case FCMPG:
                loadToFPUStack32(op1, os, context);
                loadToFPUStack32(op2, os, context);
                floatCompare(true, lsh, context.getInstrLabel(quad.getAddress()));
                break;
            case FCMPL:
                loadToFPUStack32(op2, os, context);
                loadToFPUStack32(op1, os, context);
                floatCompare(false, lsh, context.getInstrLabel(quad.getAddress()));
                break;
            case DCMPG:
                loadToFPUStack64(op1, os, context);
                loadToFPUStack64(op2, os, context);
                floatCompare(true, lsh, context.getInstrLabel(quad.getAddress()));
                break;
            case DCMPL:
                loadToFPUStack64(op2, os, context);
                loadToFPUStack64(op1, os, context);
                floatCompare(false, lsh, context.getInstrLabel(quad.getAddress()));
                break;
            default:
                throw new IllegalArgumentException("Unsupported operation: " + operation);
        }
    }

    //fpu stack: ... op1, op2 if gt = true
    //fpu stack: ... op2, op1 if gt = false
    //result: result 1 if op1 > op2, -1 is op1 < op2, 0 otherwise
    private void floatCompare(boolean gt, final Variable<T> result, final Label curInstrLabel) {
        // Clear resultReg
        GPR resultReg = null;
        int resultDisp = 0;
        boolean reg;
        AddressingMode addressingMode = result.getAddressingMode();
        switch (addressingMode) {
            case REGISTER:
                reg = true;
                resultReg = (GPR) ((RegisterLocation) ((Variable) result).getLocation()).getRegister();
                break;
            case STACK:
                reg = false;
                resultDisp = ((StackLocation) ((Variable) result).getLocation()).getDisplacement();
                break;
            default:
                throw new IllegalArgumentException("Illegal addressing mode: " + addressingMode);
        }
        if (reg) {
            os.writeXOR(resultReg, resultReg);
        } else {
            os.writeMOV_Const(X86Constants.BITS32, X86Register.EBP, resultDisp, 0);
        }
        os.writeFUCOMPP();
        os.writeFNSTSW_AX();
        os.writeSAHF();
        final Label gtLabel = new Label(curInstrLabel + "gt");
        final Label ltLabel = new Label(curInstrLabel + "lt");
        final Label endLabel = new Label(curInstrLabel + "end");
        os.writeJCC(gtLabel, X86Constants.JA);
        os.writeJCC(ltLabel, X86Constants.JB);
        os.writeJMP(endLabel); // equal
        // Greater
        os.setObjectRef(gtLabel);
        if (gt) {
            if (reg) {
                os.writeDEC(resultReg);
            } else {
                os.writeDEC(X86Constants.BITS32, X86Register.EBP, resultDisp);
            }
        } else {
            if (reg) {
                os.writeINC(resultReg);
            } else {
                os.writeINC(X86Constants.BITS32, X86Register.EBP, resultDisp);
            }
        }
        os.writeJMP(endLabel);
        // Less
        os.setObjectRef(ltLabel);
        if (gt) {
            if (reg) {
                os.writeINC(resultReg);
            } else {
                os.writeINC(X86Constants.BITS32, X86Register.EBP, resultDisp);
            }
        } else {
            if (reg) {
                os.writeDEC(resultReg);
            } else {
                os.writeDEC(X86Constants.BITS32, X86Register.EBP, resultDisp);
            }
        }
        // End
        os.setObjectRef(endLabel);
    }

    //fpu stack: ... op1, op2 if gt = true
    //fpu stack: ... op2, op1 if gt = false
    //result: resultReg or resultDisp 1 if op1 > op2, -1 is op1 < op2, 0 otherwise
    private void floatCompare(boolean gt, final GPR resultReg, int resultDisp, final Label curInstrLabel) {
        // Clear resultReg
        if (resultReg == null) {
            os.writeMOV_Const(X86Constants.BITS32, X86Register.EBP, resultDisp, 0);
        } else {
            os.writeXOR(resultReg, resultReg);
        }
        os.writeFUCOMPP();
        os.writeFNSTSW_AX();
        os.writeSAHF();
        final Label gtLabel = new Label(curInstrLabel + "gt");
        final Label ltLabel = new Label(curInstrLabel + "lt");
        final Label endLabel = new Label(curInstrLabel + "end");
        os.writeJCC(gtLabel, X86Constants.JA);
        os.writeJCC(ltLabel, X86Constants.JB);
        os.writeJMP(endLabel); // equal
        // Greater
        os.setObjectRef(gtLabel);
        if (gt) {
            if (resultReg == null) {
                os.writeDEC(X86Constants.BITS32, X86Register.EBP, resultDisp);
            } else {
                os.writeDEC(resultReg);
            }
        } else {
            if (resultReg == null) {
                os.writeINC(X86Constants.BITS32, X86Register.EBP, resultDisp);
            } else {
                os.writeINC(resultReg);
            }
        }
        os.writeJMP(endLabel);
        // Less
        os.setObjectRef(ltLabel);
        if (gt) {
            if (resultReg == null) {
                os.writeINC(X86Constants.BITS32, X86Register.EBP, resultDisp);
            } else {
                os.writeINC(resultReg);
            }
        } else {
            if (resultReg == null) {
                os.writeDEC(X86Constants.BITS32, X86Register.EBP, resultDisp);
            } else {
                os.writeDEC(resultReg);
            }
        }
        // End
        os.setObjectRef(endLabel);
    }

    static <T> void loadToFPUStack32(Operand<T> operand, X86Assembler os, GenericX86CodeGenerator context) {
        AddressingMode addressingMode = operand.getAddressingMode();
        switch (addressingMode) {
            case CONSTANT:
                os.writePUSH(((FloatConstant) operand).getIntBits());
                os.writeFLD32(X86Register.ESP, 0);
                os.writeLEA(X86Register.ESP, X86Register.ESP, context.stackFrame.getHelper().SLOTSIZE);
                break;
            case REGISTER:
                os.writePUSH((GPR) ((RegisterLocation) ((Variable) operand).getLocation()).getRegister());
                os.writeFLD32(X86Register.ESP, 0);
                os.writeLEA(X86Register.ESP, X86Register.ESP, context.stackFrame.getHelper().SLOTSIZE);
                break;
            case STACK:
                os.writeFLD32(X86Register.EBP, ((StackLocation) ((Variable) operand).getLocation()).getDisplacement());
                break;
            default:
                throw new IllegalArgumentException("Illegal addressing mode: " + addressingMode);
        }
    }

    static <T> void loadToFPUStack64(Operand<T> operand, X86Assembler os, GenericX86CodeGenerator context) {
        AddressingMode addressingMode = operand.getAddressingMode();
        switch (addressingMode) {
            case CONSTANT:
                DoubleConstant dconst = (DoubleConstant) operand;
                long value = Double.doubleToRawLongBits(dconst.getValue());
                final int v_lsb = (int) (value & 0xFFFFFFFFL);
                final int v_msb = (int) ((value >>> 32) & 0xFFFFFFFFL);
                os.writePUSH(v_msb);
                os.writePUSH(v_lsb);
                os.writeFLD64(X86Register.ESP, 0);
                os.writeLEA(X86Register.ESP, X86Register.ESP, 2 * context.stackFrame.getHelper().SLOTSIZE);
                break;
//            case REGISTER:
//                os.writePUSH((GPR) ((RegisterLocation) ((Variable) operand).getLocation()).getRegister());
//                os.writeFLD64(X86Register.ESP, 0);
//                os.writeLEA(X86Register.ESP, X86Register.ESP, context.stackFrame.getHelper().SLOTSIZE);
//                break;
            case STACK:
                int displacement = ((StackLocation) ((Variable) operand).getLocation()).getDisplacement();
                displacement -= context.stackFrame.getHelper().SLOTSIZE;      //todo OK - follow this
                os.writeFLD64(X86Register.EBP, displacement);
                break;
            default:
                throw new IllegalArgumentException("Illegal addressing mode: " + addressingMode);
        }
    }
}
