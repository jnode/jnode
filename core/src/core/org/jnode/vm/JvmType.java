/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.vm;

/**
 * JVM types
 */
public final class JvmType {

    public static final int UNKNOWN = 0;

    public static final int BOOLEAN = 1;

    public static final int BYTE = 2;

    public static final int SHORT = 3;

    public static final int CHAR = 4;

    public static final int INT = 5;

    public static final int LONG = 6;

    public static final int FLOAT = 7;

    public static final int DOUBLE = 8;

    public static final int REFERENCE = 9;

    public static final int RETURN_ADDRESS = REFERENCE;

    public static final int VOID = 10;

    private static final String[] names = {
        "UNKONWN", "BOOLEAN", "BYTE", "SHORT", "CHAR", "INT", "LONG", "FLOAT", "DOUBLE", "REF", "VOID"
    };

    public static int getCategory(int type) {
        if ((type == LONG) || (type == DOUBLE)) {
            return 2;
        } else if (type == VOID) {
            return 0;
        } else {
            return 1;
        }
    }

    /**
     * Converts the given type to the smallest type that can contain it. E.g.
     * BYTE to INT FLOAT to FLOAT
     *
     * @param type
     * @return
     */
    public static int TypeToContainingType(int type) {
        switch (type) {
            case BOOLEAN:
            case BYTE:
            case CHAR:
            case SHORT:
            case INT:
                return INT;
            default:
                return type;
        }
    }

    /**
     * @param type
     * @return the internal type value
     */
    public static int SignatureToType(char type) {
        int res;
        switch (type) {
            case 'Z':
                // Boolean
            case 'B':
                // Byte
            case 'C':
                // Character
            case 'S':
                // Short
            case 'I':
                // Integer
                res = JvmType.INT;
                break;
            case 'F':
                // Float
                res = JvmType.FLOAT;
                break;
            case 'L':
                // Object
            case ';':
                // Object
            case '[':
                // Array
                res = JvmType.REFERENCE;
                break;
            case 'J':
                // Long
                res = JvmType.LONG;
                break;
            case 'D':
                // Double
                res = JvmType.DOUBLE;
                break;
            default:
                throw new IllegalArgumentException("Unknown type" + type);
        }
        return res;
    }

    /**
     * @param signature
     * @return the internal type value
     */
    public static int SignatureToType(String signature) {
        return SignatureToType(signature.charAt(0));
    }

    /**
     * Gets the number of arguments of a method signature.
     *
     * @param signature
     * @return The number of arguments.
     */
    public static int getArgumentCount(String signature) {
        final int len = signature.length();
        int cnt = 0;
        for (int i = 1; i < len; i++) {
            switch (signature.charAt(i)) {
                case 'Z':
                case 'B':
                case 'C':
                case 'S':
                case 'I':
                case 'F':
                case 'J':
                case 'D':
                    break;
                case 'L':
                    while (signature.charAt(i) != ';') {
                        i++;
                    }
                    break;
                    // Object
                case '[':
                    while (signature.charAt(i) == '[') {
                        i++;
                    }
                    if (signature.charAt(i) == 'L') {
                        while (signature.charAt(i) != ';') {
                            i++;
                        }
                    }
                    break;
                case ')':
                    // the end
                    i = len;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown type"
                        + signature.substring(i));
            }
            if (i != len) {
                cnt++;
            }
        }
        return cnt;
    }

    /**
     * Gets the argument type of a method signature.
     *
     * @param signature
     * @return
     */
    public static int[] getArgumentTypes(String signature) {
        final int len = signature.length();
        final int[] types = new int[getArgumentCount(signature)];
        int cnt = 0;
        for (int i = 1; i < len; i++) {
            final int t;
            switch (signature.charAt(i)) {
                case 'Z':
                case 'B':
                case 'C':
                case 'S':
                case 'I':
                    t = JvmType.INT;
                    break;
                case 'F':
                    t = JvmType.FLOAT;
                    break;
                case 'L':
                    while (signature.charAt(i) != ';') {
                        i++;
                    }
                    t = JvmType.REFERENCE;
                    break;
                    // Object
                case '[':
                    while (signature.charAt(i) == '[') {
                        i++;
                    }
                    if (signature.charAt(i) == 'L') {
                        while (signature.charAt(i) != ';') {
                            i++;
                        }
                    }
                    t = JvmType.REFERENCE;
                    break;
                case 'J':
                    t = JvmType.LONG;
                    break;
                case 'D':
                    t = JvmType.DOUBLE;
                    break;
                case ')':
                    // the end
                    i = len;
                    t = JvmType.VOID;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown type"
                        + signature.substring(i));
            }
            if (t != VOID) {
                types[cnt++] = t;
            }
        }
        return types;
    }

    /**
     * Gets the return type of a method signature.
     *
     * @param signature
     * @return
     */
    public static int getReturnType(String signature) {
        final int endIdx = signature.indexOf(')');
        final char ch = signature.charAt(endIdx + 1);
        if (ch == 'V') {
            return VOID;
        } else {
            return SignatureToType(ch);
        }
    }

    /**
     * Is the given type a floating point type.
     *
     * @param type
     * @return True if type is FLOAT or DOUBLE, false otherwise.
     */
    public static final boolean isFloat(int type) {
        return ((type == FLOAT) || (type == DOUBLE));
    }

    /**
     * Gets a human readable name of the given type.
     *
     * @param type
     * @return
     */
    public static final String toString(int type) {
        return names[type];
    }
}
