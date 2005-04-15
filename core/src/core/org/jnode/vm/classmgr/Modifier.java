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
	public static final int ACC_STRICT      = 0x00000800; // F Declared strictfp; floating-point mode is
													 // FP-strict
	public static final int ACC_SYNTHETIC   = 0x00001000; // Not present in sourcecode
	public static final int ACC_ANNOTATION  = 0x00002000; // Declared as annotation type
	public static final int ACC_ENUM        = 0x00004000; // Declared as an enum type

	/** Is a member wide (long, double) */
	public static final int ACC_WIDE        = 0x00010000;
	/** Is a field an object reference */
	public static final int ACC_OBJECTREF   = 0x00020000;
	public static final int ACC_INITIALIZER = 0x00040000;
	public static final int ACC_CONSTRUCTOR = 0x00080000;
	/** Class has a finalizer other then java.lang.Object#finalizer */
	public static final int ACC_FINALIZER   = 0x00100000;

	/** Is this a magic class */
	public static final int ACC_MAGIC = 0x20000000; // C

	/** gather profile information for this method */
	public static final int ACC_PROFILE     = 0x40000000; // M
	public static final int ACC_SPECIAL     = 0x80000000;
	
	public static boolean isPublic(int modifier) {
		int mask = ACC_PUBLIC;
		return ((modifier & mask) == mask);
	}

	public static boolean isPrivate(int modifier) {
		int mask = ACC_PRIVATE;
		return ((modifier & mask) == mask);
	}

	public static boolean isProtected(int modifier) {
		int mask = ACC_PROTECTED;
		return ((modifier & mask) == mask);
	}

	public static boolean isStatic(int modifier) {
		int mask = ACC_STATIC;
		return ((modifier & mask) == mask);
	}

	public static boolean isFinal(int modifier) {
		int mask = ACC_FINAL;
		return ((modifier & mask) == mask);
	}

	public static boolean isObjectRef(int modifier) {
		int mask = ACC_OBJECTREF;
		return ((modifier & mask) == mask);
	}

	public static boolean isSpecial(int modifier) {
		int mask = ACC_SPECIAL;
		return ((modifier & mask) == mask);
	}

	public static boolean isSynchronized(int modifier) {
		int mask = ACC_SYNCHRONIZED;
		return ((modifier & mask) == mask);
	}

	public static boolean isSuper(int modifier) {
		int mask = ACC_SUPER;
		return ((modifier & mask) == mask);
	}

	public static boolean isVolatile(int modifier) {
		int mask = ACC_VOLATILE;
		return ((modifier & mask) == mask);
	}

	public static boolean isTransient(int modifier) {
		int mask = ACC_TRANSIENT;
		return ((modifier & mask) == mask);
	}

	public static boolean isNative(int modifier) {
		int mask = ACC_NATIVE;
		return ((modifier & mask) == mask);
	}

	public static boolean isInterface(int modifier) {
		int mask = ACC_INTERFACE;
		return ((modifier & mask) == mask);
	}

    public static boolean isEnum(int modifier) {
        int mask = ACC_ENUM;
        return ((modifier & mask) == mask);
    }

	public static boolean isAbstract(int modifier) {
		int mask = ACC_ABSTRACT;
		return ((modifier & mask) == mask);
	}

	public static boolean isStrict(int modifier) {
		int mask = ACC_STRICT;
		return ((modifier & mask) == mask);
	}

	/*public static boolean isCompiled(int modifier) {
		int mask = ACC_COMPILED;
		return ((modifier & mask) == mask);
	}*/

	public static boolean isInitializer(int modifier) {
		int mask = ACC_INITIALIZER;
		return ((modifier & mask) == mask);
	}

	public static boolean isConstructor(int modifier) {
		int mask = ACC_CONSTRUCTOR;
		return ((modifier & mask) == mask);
	}

	public static boolean isMagic(int modifier) {
		int mask = ACC_MAGIC;
		return ((modifier & mask) == mask);
	}

/*	public static boolean isLoaded(int modifier) {
		int mask = ACC_LOADED;
		return ((modifier & mask) == mask);
	}

	public static boolean isDefined(int modifier) {
		int mask = ACC_DEFINED;
		return ((modifier & mask) == mask);
	}

	public static boolean isVerifying(int modifier) {
		int mask = ACC_VERIFYING;
		return ((modifier & mask) == mask);
	}

	public static boolean isVerified(int modifier) {
		int mask = ACC_VERIFIED;
		return ((modifier & mask) == mask);
	}

	public static boolean isPreparing(int modifier) {
		int mask = ACC_PREPARING;
		return ((modifier & mask) == mask);
	}

	public static boolean isPrepared(int modifier) {
		int mask = ACC_PREPARED;
		return ((modifier & mask) == mask);
	}

	public static boolean isInitialized(int modifier) {
		int mask = ACC_INITIALIZED;
		return ((modifier & mask) == mask);
	}

	public static boolean isInitializing(int modifier) {
		int mask = ACC_INITIALIZING;
		return ((modifier & mask) == mask);
	}*/

	/**
	 * Should profile information be gathered?
	 * 
	 * @param modifier
	 * @return boolean
	 */
	public static boolean isProfile(int modifier) {
		int mask = ACC_PROFILE;
		return ((modifier & mask) == mask);
	}

	/*public static boolean isInvalid(int modifier) {
		int mask = ACC_INVALID;
		return ((modifier & mask) == mask);
	}*/

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
		char ch = signature.charAt(0);
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
			case 'Z' : // Boolean
			case 'B' : // Byte
				return 1;
			case 'C' : // Character
			case 'S' : // Short
				return 2;
			case 'I' : // Integer
			case 'F' : // Float
				return 4;
			case 'L' : // Object
			case '[' : // Array
				return (byte) slotSize;
			case 'J' : // Long
			case 'D' : // Double
				return 8;
			default :
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
		final StringBuffer b = new StringBuffer();

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
		if ((modifiers & ACC_PROFILE) != 0) {
			b.append("profile ");
		}

		return b.toString().trim();
	}
}
