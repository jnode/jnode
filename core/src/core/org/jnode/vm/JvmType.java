/*
 * $Id$
 */
package org.jnode.vm;

/**
 * JVM types
 */
public final class JvmType {

    public static final int UNKNOWN = 0;

    public static final int BYTE = 1;

    public static final int SHORT = 2;

    public static final int CHAR = 3;

    public static final int INT = 4;

    public static final int LONG = 5;

    public static final int FLOAT = 6;

    public static final int DOUBLE = 7;

    public static final int REFERENCE = 8;

    public static final int RETURN_ADDRESS = REFERENCE;

    public static final int VOID = 9;

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
     * Gets the argument type of a method signature.
     * 
     * @param signature
     * @return
     */
    public static int[] getArgumentTypes(String signature) {
        final int len = signature.length();
        int[] types = new int[ len - 3]; // '(, ')', return are skipped
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
                types[ cnt++] = t;
            }
        }
        final int[] result = new int[ cnt];
        System.arraycopy(types, 0, result, 0, cnt);
        return result;
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
}