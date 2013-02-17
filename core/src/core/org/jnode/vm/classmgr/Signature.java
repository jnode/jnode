/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.vm.classmgr;

import java.util.ArrayList;
import org.jnode.vm.InternString;
import org.jnode.vm.JvmType;
import org.jnode.vm.facade.TypeSizeInfo;

/**
 * <description>
 *
 * @author epr
 */
public class Signature {

    private String signature;

    private VmType[] parts;

    /**
     * Create a new instance
     *
     * @param signature
     * @param loader
     * @throws ClassNotFoundException
     */
    public Signature(String signature, VmClassLoader loader)
        throws ClassNotFoundException {
        this.signature = signature;
        this.parts = split(signature.toCharArray(), loader);
    }

    /**
     * Is this the signature of a field?
     *
     * @return boolean
     */
    public boolean isField() {
        return (signature.charAt(0) != '(');
    }

    /**
     * Is this the signature of a method?
     *
     * @return boolean
     */
    public boolean isMethod() {
        return (signature.charAt(0) == '(');
    }

    /**
     * Gets the type of the signature (for field signatures)
     *
     * @return the type as a VmType
     */
    public VmType getType() {
        return parts[0];
    }

    /**
     * Gets the return type of the signature (for method signatures)
     *
     * @return the type as a VmType
     */
    public VmType getReturnType() {
        return parts[parts.length - 1];
    }

    /**
     * Gets the type of the parameter with the given index in this signature
     * (for method signatures)
     *
     * @param index
     * @return the type as a VmType
     */
    public VmType getParamType(int index) {
        return parts[index];
    }

    /**
     * Gets the number of parameters (for method signatures)
     *
     * @return the parameter count
     */
    public int getParamCount() {
        return parts.length - 1;
    }

    /**
     * Calculate the number of argument slots that a method required, based of 
     * the method signature supplied as a character array.
     *
     * @param typeSizeInfo gives the type sizes
     * @param signature the method signature as a character array
     * @return the slot count
     */
    public static final int getArgSlotCount(TypeSizeInfo typeSizeInfo,
                                            char[] signature) {
        int ofs = 0;
        final int len = signature.length;
        if (signature[ofs++] != '(') {
            return 0;
        }
        int count = 0;
        while (ofs < len) {
            char ch = signature[ofs++];
            switch (ch) {
                case ')':
                    return count;
                case 'B': // Byte
                case 'Z': // Boolean
                case 'C': // Char
                case 'S': // Short
                case 'I': // Int
                    count += typeSizeInfo.getStackSlots(JvmType.INT);
                    break;
                case 'F': // Float
                    count += typeSizeInfo.getStackSlots(JvmType.FLOAT);
                    break;
                case 'D': // Double
                    count += typeSizeInfo.getStackSlots(JvmType.DOUBLE);
                    break;
                case 'J': // Long
                    count += typeSizeInfo.getStackSlots(JvmType.LONG);
                    break;
                case '[': // Array
                {
                    count += typeSizeInfo.getStackSlots(JvmType.REFERENCE);
                    while (signature[ofs] == '[')
                        ofs++;
                    if (signature[ofs] == 'L') {
                        ofs++;
                        while (signature[ofs] != ';') {
                            ofs++;
                        }
                    }
                    ofs++;
                    break;
                }
                case 'L': // Object
                {
                    count += typeSizeInfo.getStackSlots(JvmType.REFERENCE);
                    while (signature[ofs] != ';')
                        ofs++;
                    ofs++;
                    break;
                }
            }
        }
        throw new RuntimeException("Invalid signature in getArgSlotCount: "
            + String.valueOf(signature));
    }

    /**
     * Calculate the number of argument slots that a method required, based of 
     * the method signature supplied as a String.
     *
     * @param typeSizeInfo gives the type sizes
     * @param signature the method signature as a String
     * @return the slot count
     */
    public static final int getArgSlotCount(TypeSizeInfo typeSizeInfo,
                                            String signature) {
        return getArgSlotCount(typeSizeInfo, signature.toCharArray());
    }

    /**
     * Gets the stack slot number (0..) for a given java argument number. This
     * method takes into account the (per architecture) difference between the
     * size of longs, double and references and the number of slots they use.
     *
     * @param typeSizeInfo
     * @param method
     * @param javaArgIndex
     * @return the slot number
     */
    public static final int getStackSlotForJavaArgNumber(
        TypeSizeInfo typeSizeInfo, VmMethod method, int javaArgIndex) {
        final int[] argTypes = JvmType.getArgumentTypes(method.getSignature());
        final int argCount = argTypes.length;
        int stackSlot = 0;
        for (int i = 0; i < argCount; i++) {
            if (javaArgIndex == 0) {
                return stackSlot;
            }
            final int argJvmType = argTypes[i];
            stackSlot += typeSizeInfo.getStackSlots(argJvmType);
            if ((argJvmType == JvmType.LONG) || (argJvmType == JvmType.DOUBLE)) {
                javaArgIndex -= 2;
            } else {
                javaArgIndex -= 1;
            }
        }
        return stackSlot + javaArgIndex;
    }

    private VmType[] split(char[] signature, VmClassLoader loader)
        throws ClassNotFoundException {
        ArrayList<VmType> list = new ArrayList<VmType>();
        int ofs = 0;
        final int len = signature.length;
        if (signature[ofs] == '(') {
            ofs++;
        }
        while (ofs < len) {
            int start = ofs;
            char ch = signature[ofs++];
            VmType vmClass;
            switch (ch) {
                case ')':
                    continue;
                case 'B': // Byte
                case 'Z': // Boolean
                case 'C': // Char
                case 'S': // Short
                case 'I': // Int
                case 'F': // Float
                case 'D': // Double
                case 'J': // Long
                case 'V': // Void
                    vmClass = VmType.getPrimitiveClass(ch);
                    break;
                case '[': // Array
                {
                    while (signature[ofs] == '[') {
                        ofs++;
                    }
                    if (signature[ofs] == 'L') {
                        ofs++;
                        while (signature[ofs] != ';') {
                            ofs++;
                        }
                    }
                    ofs++;
                    String sig = new String(signature, start, ofs - start).replace('/', '.');
                    sig = InternString.internString(sig);
                    vmClass = loader.loadClass(sig, true);
                    break;
                }
                case 'L': // Object
                {
                    start++;
                    while (signature[ofs] != ';') {
                        ofs++;
                    }
                    String sig = new String(signature, start, ofs - start).replace('/', '.');
                    sig = InternString.internString(sig);
                    ofs++;
                    vmClass = loader.loadClass(sig, true);
                    break;
                }
                default:
                    throw new ClassFormatError("Unknown signature character " + ch);
            }
            if (vmClass == null) {
                throw new RuntimeException(
                    "vmClass is null for signature character " + ch);
            }
            list.add(vmClass);
        }
        return (VmType[]) list.toArray(new VmType[list.size()]);
    }

    /**
     * Convert the given class to a signature
     *
     * @param cls
     * @return String
     */
    public static String toSignature(Class<?> cls) {
        if (cls == null) {
            throw new NullPointerException("cls==null");
        }

        if (cls.isArray()) {
            return '[' + toSignature(cls.getComponentType());
        } else if (cls.isPrimitive()) {
            if (cls == Boolean.TYPE) {
                return "Z";
            } else if (cls == Byte.TYPE) {
                return "B";
            } else if (cls == Character.TYPE) {
                return "C";
            } else if (cls == Short.TYPE) {
                return "S";
            } else if (cls == Integer.TYPE) {
                return "I";
            } else if (cls == Long.TYPE) {
                return "J";
            } else if (cls == Float.TYPE) {
                return "F";
            } else if (cls == Double.TYPE) {
                return "D";
            } else if (cls == Void.TYPE) {
                return "V";
            }
            return cls.getName();
        } else {
            return 'L' + cls.getName().replace('.', '/') + ';';
        }
    }

    /**
     * Convert the given class array to a signature
     *
     * @param returnType
     * @param argTypes
     * @return String
     */
    public static String toSignature(Class returnType, Class[] argTypes) {
        StringBuilder b = new StringBuilder();
        b.append('(');
        if (argTypes != null) {
            for (Class argType : argTypes) {
                b.append(toSignature(argType));
            }
        }
        b.append(')');
        if (returnType == null) {
            b.append('V');
        } else {
            b.append(toSignature(returnType));
        }
        return b.toString();
    }

    /**
     * Convert the given VmType to a signature.
     *
     * @param cls a VmType instance
     * @return String the signature
     */
    public static String toSignature(VmType cls) {
        if (cls == null) {
            throw new NullPointerException("cls==null");
        }

        if (cls.isArray()) {
            return '[' + toSignature(((VmArrayClass) cls).getComponentType());
        } else if (cls.isPrimitive()) {
            if (cls == VmType.getPrimitiveClass('Z')) {
                return "Z";
            } else if (cls == VmType.getPrimitiveClass('B')) {
                return "B";
            } else if (cls == VmType.getPrimitiveClass('C')) {
                return "C";
            } else if (cls == VmType.getPrimitiveClass('S')) {
                return "S";
            } else if (cls == VmType.getPrimitiveClass('I')) {
                return "I";
            } else if (cls == VmType.getPrimitiveClass('J')) {
                return "J";
            } else if (cls == VmType.getPrimitiveClass('F')) {
                return "F";
            } else if (cls == VmType.getPrimitiveClass('D')) {
                return "D";
            } else if (cls == VmType.getPrimitiveClass('V')) {
                return "V";
            }
            return cls.getName();
        } else {
            return 'L' + cls.getName().replace('.', '/') + ';';
        }
    }

    /**
     * Convert the given VmType array to a signature.
     *
     * @param returnType
     * @param argTypes
     * @return String
     */
    public static String toSignature(VmType returnType, VmType[] argTypes) {
        StringBuilder b = new StringBuilder();
        b.append('(');
        if (argTypes != null) {
            for (VmType argType : argTypes) {
                b.append(toSignature(argType));
            }
        }
        b.append(')');
        if (returnType == null) {
            b.append('V');
        } else {
            b.append(toSignature(returnType));
        }
        return b.toString();
    }
}
