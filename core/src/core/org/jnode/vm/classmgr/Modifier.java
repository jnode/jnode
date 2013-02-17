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

public class Modifier {
    public static final int ACC_PUBLIC = 0x00000001;
    public static final int ACC_PRIVATE = 0x00000002;
    public static final int ACC_PROTECTED = 0x00000004;
    public static final int ACC_STATIC = 0x00000008;
    public static final int ACC_FINAL = 0x00000010;
    public static final int ACC_SYNCHRONIZED = 0x00000020;
    public static final int ACC_SUPER = 0x00000020;
    public static final int ACC_VOLATILE = 0x00000040;
    public static final int ACC_BRIDGE = 0x00000040;
    public static final int ACC_TRANSIENT = 0x00000080;
    public static final int ACC_VARARGS = 0x00000080;
    public static final int ACC_NATIVE = 0x00000100;
    public static final int ACC_INTERFACE = 0x00000200;
    public static final int ACC_ABSTRACT = 0x00000400;
    public static final int ACC_STRICT = 0x00000800; // F Declared strictfp; floating-point mode is
    // FP-strict
    public static final int ACC_SYNTHETIC = 0x00001000; // Not present in sourcecode
    public static final int ACC_ANNOTATION = 0x00002000; // Declared as annotation type
    public static final int ACC_ENUM = 0x00004000; // Declared as an enum type

    /**
     * Is a member wide (long, double)
     */
    public static final int ACC_WIDE = 0x00010000;
    /**
     * Is a field an object reference
     */
    public static final int ACC_OBJECTREF = 0x00020000;
    public static final int ACC_INITIALIZER = 0x00040000;
    public static final int ACC_CONSTRUCTOR = 0x00080000;
    /**
     * Class has a finalizer other then java.lang.Object#finalizer
     */
    public static final int ACC_FINALIZER = 0x00100000;

    /**
     * Is this a magic class
     */
    public static final int ACC_MAGIC = 0x10000000; // C

    /**
     * Is this a special method (init, clinit)
     */
    public static final int ACC_SPECIAL = 0x80000000;

    public static boolean isPublic(int modifier) {
        return ((modifier & ACC_PUBLIC) != 0);
    }

    public static boolean isPrivate(int modifier) {
        return ((modifier & ACC_PRIVATE) != 0);
    }

    public static boolean isProtected(int modifier) {
        return ((modifier & ACC_PROTECTED) != 0);
    }

    public static boolean isStatic(int modifier) {
        return ((modifier & ACC_STATIC) != 0);
    }

    public static boolean isFinal(int modifier) {
        return ((modifier & ACC_FINAL) != 0);
    }

    public static boolean isObjectRef(int modifier) {
        return ((modifier & ACC_OBJECTREF) != 0);
    }

    public static boolean isSpecial(int modifier) {
        return ((modifier & ACC_SPECIAL) != 0);
    }

    public static boolean isSynchronized(int modifier) {
        return ((modifier & ACC_SYNCHRONIZED) != 0);
    }

    public static boolean isSuper(int modifier) {
        return ((modifier & ACC_SUPER) != 0);
    }

    public static boolean isVolatile(int modifier) {
        return ((modifier & ACC_VOLATILE) != 0);
    }

    public static boolean isTransient(int modifier) {
        return ((modifier & ACC_TRANSIENT) != 0);
    }

    public static boolean isNative(int modifier) {
        return ((modifier & ACC_NATIVE) != 0);
    }

    public static boolean isInterface(int modifier) {
        return ((modifier & ACC_INTERFACE) != 0);
    }

    public static boolean isEnum(int modifier) {
        return ((modifier & ACC_ENUM) != 0);
    }

    public static boolean isAnnotation(int modifier) {
        return ((modifier & ACC_ANNOTATION) != 0);
    }

    public static boolean isAbstract(int modifier) {
        return ((modifier & ACC_ABSTRACT) != 0);
    }

    public static boolean isStrict(int modifier) {
        return ((modifier & ACC_STRICT) != 0);
    }

    public static boolean isInitializer(int modifier) {
        return ((modifier & ACC_INITIALIZER) != 0);
    }

    public static boolean isConstructor(int modifier) {
        return ((modifier & ACC_CONSTRUCTOR) != 0);
    }

    public static boolean isMagic(int modifier) {
        return ((modifier & ACC_MAGIC) != 0);
    }

    public static boolean isSynthetic(int modifier) {
        return ((modifier & ACC_SYNTHETIC) != 0);
    }

    public static boolean isWide(String signature) {
        final int len = signature.length();
        final boolean arr = (len > 1) && (signature.charAt(len - 2) == '[');
        if (arr) {
            return false;
        } else {
            final char ch = signature.charAt(len - 1);
            return ((ch == 'J') || (ch == 'D'));
        }
    }

    public static boolean isPrimitive(String signature) {
        final char ch = signature.charAt(0);
        return ((ch != 'L') && (ch != '['));
    }

    public static boolean isAddressType(String signature) {
        return "Lorg/jnode/vm/VmAddress;".equals(signature) ||
            "Lorg/vmmagic/unboxed/Address;".equals(signature) ||
            "Lorg/vmmagic/unboxed/Extent;".equals(signature) ||
            "Lorg/vmmagic/unboxed/Offset;".equals(signature) ||
            "Lorg/vmmagic/unboxed/Word;".equals(signature);
    }

    /**
     * Gets the size in bytes of the given type. This will return the following values:
     * <ul>
     * <li><code>boolean</code> 1
     * <li><code>byte</code> 1
     * <li><code>char</code> 2
     * <li><code>short</code> 2
     * <li><code>int</code> 4
     * <li><code>long</code> 8
     * <li><code>float</code> 4
     * <li><code>double</code> 8
     * <li><code>reference</code> SLOT_SIZE
     * </ul>
     *
     * @param signature
     * @param slotSize
     * @return byte
     */
    public static byte getTypeSize(String signature, int slotSize) {
        switch (signature.charAt(0)) {
            case 'Z': // Boolean
            case 'B': // Byte
                return 1;
            case 'C': // Character
            case 'S': // Short
                return 2;
            case 'I': // Integer
            case 'F': // Float
                return 4;
            case 'L': // Object
            case '[': // Array
                return (byte) slotSize;
            case 'J': // Long
            case 'D': // Double
                return 8;
            default:
                throw new IllegalArgumentException("Unknown type");
        }
    }

    /**
     * Convert a modifiers int to a readable string of modifier names
     *
     * @param modifiers
     * @return String
     */
    public static String toString(int modifiers) {
        final StringBuilder b = new StringBuilder();

        if (isPublic(modifiers)) {
            b.append("public ");
        }
        if (isPrivate(modifiers)) {
            b.append("private ");
        }
        if (isProtected(modifiers)) {
            b.append("protected ");
        }
        if (isStatic(modifiers)) {
            b.append("static ");
        }
        if (isFinal(modifiers)) {
            b.append("final ");
        }
        if (isSynchronized(modifiers)) {
            b.append("synchronized ");
        }
        if (isSuper(modifiers)) {
            b.append("super ");
        }
        if (isVolatile(modifiers)) {
            b.append("volatile ");
        }
        if (isTransient(modifiers)) {
            b.append("transient ");
        }
        if (isNative(modifiers)) {
            b.append("native ");
        }
        if (isInterface(modifiers)) {
            b.append("interface ");
        }
        if (isAbstract(modifiers)) {
            b.append("abstract ");
        }
        if (isStrict(modifiers)) {
            b.append("strict ");
        }

        if ((modifiers & ACC_WIDE) != 0) {
            b.append("wide ");
        }
        if ((modifiers & ACC_OBJECTREF) != 0) {
            b.append("objectref ");
        }
        /*if ((modifiers & ACC_COMPILED) != 0) {
              b.append("compiled ");
          }*/
        if ((modifiers & ACC_INITIALIZER) != 0) {
            b.append("initializer ");
        }
        if ((modifiers & ACC_CONSTRUCTOR) != 0) {
            b.append("constructor ");
        }

        /*if ((modifiers & ACC_LOADED) != 0) {
              b.append("loaded ");
          }
          if ((modifiers & ACC_DEFINED) != 0) {
              b.append("defined ");
          }
          if ((modifiers & ACC_VERIFYING) != 0) {
              b.append("verifying ");
          }
          if ((modifiers & ACC_VERIFIED) != 0) {
              b.append("verified ");
          }
          if ((modifiers & ACC_PREPARING) != 0) {
              b.append("preparing ");
          }
          if ((modifiers & ACC_PREPARED) != 0) {
              b.append("prepared ");
          }
          if ((modifiers & ACC_INITIALIZED) != 0) {
              b.append("initialized ");
          }
          if ((modifiers & ACC_INITIALIZING) != 0) {
              b.append("initializing ");
          }*/
        /*if ((modifiers & ACC_INVALID) != 0) {
              b.append("invalid ");
          }*/

        return b.toString().trim();
    }
}
