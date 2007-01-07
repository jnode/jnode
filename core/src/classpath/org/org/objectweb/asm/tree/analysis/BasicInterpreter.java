/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2005 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.objectweb.asm.tree.analysis;

import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

/**
 * An {@link Interpreter} for {@link BasicValue} values.
 * 
 * @author Eric Bruneton
 * @author Bing Ran
 */
public class BasicInterpreter implements Opcodes, Interpreter {

    public Value newValue(final Type type) {
        if (type == null) {
            return BasicValue.UNINITIALIZED_VALUE;
        }
        switch (type.getSort()) {
            case Type.VOID:
                return null;
            case Type.BOOLEAN:
            case Type.CHAR:
            case Type.BYTE:
            case Type.SHORT:
            case Type.INT:
                return BasicValue.INT_VALUE;
            case Type.FLOAT:
                return BasicValue.FLOAT_VALUE;
            case Type.LONG:
                return BasicValue.LONG_VALUE;
            case Type.DOUBLE:
                return BasicValue.DOUBLE_VALUE;
            case Type.ARRAY:
            case Type.OBJECT:
                return BasicValue.REFERENCE_VALUE;
            default:
                throw new RuntimeException("Internal error.");
        }
    }

    public Value newOperation(final AbstractInsnNode insn) {
        switch (insn.getOpcode()) {
            case ACONST_NULL:
                return newValue(Type.getType("Lnull;"));
            case ICONST_M1:
            case ICONST_0:
            case ICONST_1:
            case ICONST_2:
            case ICONST_3:
            case ICONST_4:
            case ICONST_5:
                return BasicValue.INT_VALUE;
            case LCONST_0:
            case LCONST_1:
                return BasicValue.LONG_VALUE;
            case FCONST_0:
            case FCONST_1:
            case FCONST_2:
                return BasicValue.FLOAT_VALUE;
            case DCONST_0:
            case DCONST_1:
                return BasicValue.DOUBLE_VALUE;
            case BIPUSH:
            case SIPUSH:
                return BasicValue.INT_VALUE;
            case LDC:
                Object cst = ((LdcInsnNode) insn).cst;
                if (cst instanceof Integer) {
                    return BasicValue.INT_VALUE;
                } else if (cst instanceof Float) {
                    return BasicValue.FLOAT_VALUE;
                } else if (cst instanceof Long) {
                    return BasicValue.LONG_VALUE;
                } else if (cst instanceof Double) {
                    return BasicValue.DOUBLE_VALUE;
                } else if (cst instanceof Type) {
                    return newValue(Type.getType("Ljava/lang/Class;"));
                } else {
                    return newValue(Type.getType(cst.getClass()));
                }
            case JSR:
                return BasicValue.RETURNADDRESS_VALUE;
            case GETSTATIC:
                return newValue(Type.getType(((FieldInsnNode) insn).desc));
            case NEW:
                return newValue(Type.getType("L" + ((TypeInsnNode) insn).desc
                        + ";"));
            default:
                throw new RuntimeException("Internal error.");
        }
    }

    public Value copyOperation(final AbstractInsnNode insn, final Value value)
            throws AnalyzerException
    {
        return value;
    }

    public Value unaryOperation(final AbstractInsnNode insn, final Value value)
            throws AnalyzerException
    {
        switch (insn.getOpcode()) {
            case INEG:
            case IINC:
            case L2I:
            case F2I:
            case D2I:
            case I2B:
            case I2C:
            case I2S:
                return BasicValue.INT_VALUE;
            case FNEG:
            case I2F:
            case L2F:
            case D2F:
                return BasicValue.FLOAT_VALUE;
            case LNEG:
            case I2L:
            case F2L:
            case D2L:
                return BasicValue.LONG_VALUE;
            case DNEG:
            case I2D:
            case L2D:
            case F2D:
                return BasicValue.DOUBLE_VALUE;
            case IFEQ:
            case IFNE:
            case IFLT:
            case IFGE:
            case IFGT:
            case IFLE:
            case TABLESWITCH:
            case LOOKUPSWITCH:
            case IRETURN:
            case LRETURN:
            case FRETURN:
            case DRETURN:
            case ARETURN:
            case PUTSTATIC:
                return null;
            case GETFIELD:
                return newValue(Type.getType(((FieldInsnNode) insn).desc));
            case NEWARRAY:
                switch (((IntInsnNode) insn).operand) {
                    case T_BOOLEAN:
                        return newValue(Type.getType("[Z"));
                    case T_CHAR:
                        return newValue(Type.getType("[C"));
                    case T_BYTE:
                        return newValue(Type.getType("[B"));
                    case T_SHORT:
                        return newValue(Type.getType("[S"));
                    case T_INT:
                        return newValue(Type.getType("[I"));
                    case T_FLOAT:
                        return newValue(Type.getType("[F"));
                    case T_DOUBLE:
                        return newValue(Type.getType("[D"));
                    case T_LONG:
                        return newValue(Type.getType("[J"));
                    default:
                        throw new AnalyzerException("Invalid array type");
                }
            case ANEWARRAY:
                String desc = ((TypeInsnNode) insn).desc;
                if (desc.charAt(0) == '[') {
                    return newValue(Type.getType("[" + desc));
                } else {
                    return newValue(Type.getType("[L" + desc + ";"));
                }
            case ARRAYLENGTH:
                return BasicValue.INT_VALUE;
            case ATHROW:
                return null;
            case CHECKCAST:
                desc = ((TypeInsnNode) insn).desc;
                if (desc.charAt(0) == '[') {
                    return newValue(Type.getType(desc));
                } else {
                    return newValue(Type.getType("L" + desc + ";"));
                }
            case INSTANCEOF:
                return BasicValue.INT_VALUE;
            case MONITORENTER:
            case MONITOREXIT:
            case IFNULL:
            case IFNONNULL:
                return null;
            default:
                throw new RuntimeException("Internal error.");
        }
    }

    public Value binaryOperation(
        final AbstractInsnNode insn,
        final Value value1,
        final Value value2) throws AnalyzerException
    {
        switch (insn.getOpcode()) {
            case IALOAD:
            case BALOAD:
            case CALOAD:
            case SALOAD:
            case IADD:
            case ISUB:
            case IMUL:
            case IDIV:
            case IREM:
            case ISHL:
            case ISHR:
            case IUSHR:
            case IAND:
            case IOR:
            case IXOR:
                return BasicValue.INT_VALUE;
            case FALOAD:
            case FADD:
            case FSUB:
            case FMUL:
            case FDIV:
            case FREM:
                return BasicValue.FLOAT_VALUE;
            case LALOAD:
            case LADD:
            case LSUB:
            case LMUL:
            case LDIV:
            case LREM:
            case LSHL:
            case LSHR:
            case LUSHR:
            case LAND:
            case LOR:
            case LXOR:
                return BasicValue.LONG_VALUE;
            case DALOAD:
            case DADD:
            case DSUB:
            case DMUL:
            case DDIV:
            case DREM:
                return BasicValue.DOUBLE_VALUE;
            case AALOAD:
                Type t = ((BasicValue) value1).getType();
                if (t != null && t.getSort() == Type.ARRAY) {
                    return newValue(t.getElementType());
                } else {
                    return BasicValue.REFERENCE_VALUE;
                }
            case LCMP:
            case FCMPL:
            case FCMPG:
            case DCMPL:
            case DCMPG:
                return BasicValue.INT_VALUE;
            case IF_ICMPEQ:
            case IF_ICMPNE:
            case IF_ICMPLT:
            case IF_ICMPGE:
            case IF_ICMPGT:
            case IF_ICMPLE:
            case IF_ACMPEQ:
            case IF_ACMPNE:
            case PUTFIELD:
                return null;
            default:
                throw new RuntimeException("Internal error.");
        }
    }

    public Value ternaryOperation(
        final AbstractInsnNode insn,
        final Value value1,
        final Value value2,
        final Value value3) throws AnalyzerException
    {
        return null;
    }

    public Value naryOperation(final AbstractInsnNode insn, final List values)
            throws AnalyzerException
    {
        if (insn.getOpcode() == MULTIANEWARRAY) {
            return newValue(Type.getType(((MultiANewArrayInsnNode) insn).desc));
        } else {
            return newValue(Type.getReturnType(((MethodInsnNode) insn).desc));
        }
    }

    public Value merge(final Value v, final Value w) {
        if (!v.equals(w)) {
            return BasicValue.UNINITIALIZED_VALUE;
        }
        return v;
    }
}
