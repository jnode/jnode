/**
 * $Id$
 */
package org.jnode.vm.classmgr;

import java.util.ArrayList;

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
	 * @return boolean
	 */
	public boolean isField() {
		return (signature.charAt(0) != '(');
	}

	/**
	 * Is this the signature of a method?
	 * @return boolean
	 */
	public boolean isMethod() {
		return (signature.charAt(0) == '(');
	}

	/**
	 * Gets the type of the signature (for field signatures)
	 * @return String
	 */
	public VmType getType() {
		return parts[0];
	}

	/**
	 * Gets the return type of the signature (for method signatures)
	 * @return String
	 */
	public VmType getReturnType() {
		return parts[parts.length - 1];
	}

	/**
	 * Gets the type of the parameter with the given index in this signature
	 * (for method signatures)
	 * @param index
	 * @return String
	 */
	public VmType getParamType(int index) {
		return parts[index];
	}

	/**
	 * Gets the type of the parameter with the given index in this signature
	 * (for method signatures)
	 * @return String
	 */
	public int getParamCount() {
		return parts.length - 1;
	}

	/**
	 * Calculate the number of arguments a method has, based of the signature
	 * @param signature
	 * @return int
	 */
	public static final int getArgSlotCount(char[] signature) {
		int ofs = 0;
		final int len = signature.length;
		if (signature[ofs++] != '(')
			return 0;
		int count = 0;
		while (ofs < len) {
			char ch = signature[ofs++];
			switch (ch) {
				case ')' :
					return count;
				case 'B' : // Byte
				case 'Z' : // Boolean
				case 'C' : // Char
				case 'S' : // Short
				case 'I' : // Int
				case 'F' : // Float
					count++;
					break;
				case 'D' : // Double
				case 'J' : // Long
					count += 2;
					break;
				case '[' : // Array
					{
						count++;
						while (signature[ofs] == '[')
							ofs++;
						if (signature[ofs] == 'L') {
							ofs++;
							while (signature[ofs] != ';')
								ofs++;
						}
						ofs++;
					}
					break;
				case 'L' : // Object
					{
						count++;
						while (signature[ofs] != ';')
							ofs++;
						ofs++;
					}
					break;
			}
		}
		throw new RuntimeException(
			"Invalid signature in getArgSlotCount: "
				+ String.valueOf(signature));
	}

	/**
	 * Calculate the number of arguments a method has, based of the signature 
	 * @param signature
	 * @return int
	 */
	public static final int getArgSlotCount(String signature) {
		/*int ofs = 0;
		final int len = signature.length();
		if (signature.charAt(ofs++) != '(')
			return 0;
		int count = 0;
		while (ofs < len) {
			char ch = signature.charAt(ofs++);
			switch (ch) {
				case ')' :
					return count;
				case 'B' : // Byte
				case 'Z' : // Boolean
				case 'C' : // Char
				case 'S' : // Short
				case 'I' : // Int
				case 'F' : // Float
					count++;
					break;
				case 'D' : // Double
				case 'J' : // Long
					count += 2;
					break;
				case '[' : // Array
					{
						count++;
						while (signature.charAt(ofs) == '[')
							ofs++;
						if (signature.charAt(ofs) == 'L') {
							ofs++;
							while (signature.charAt(ofs) != ';')
								ofs++;
						}
						ofs++;
					}
					break;
				case 'L' : // Object
					{
						count++;
						while (signature.charAt(ofs) != ';')
							ofs++;
						ofs++;
					}
					break;
			}
		}
		throw new RuntimeException(
			"Invalid signature in getArgSlotCount: " + signature);*/
	    return getArgSlotCount(signature.toCharArray());
	}

	private VmType[] split(char[] signature, VmClassLoader loader)
		throws ClassNotFoundException {
		ArrayList list = new ArrayList();
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
				case ')' :
					continue;
				case 'B' : // Byte
				case 'Z' : // Boolean
				case 'C' : // Char
				case 'S' : // Short
				case 'I' : // Int
				case 'F' : // Float
				case 'D' : // Double
				case 'J' : // Long
				case 'V' : // Void
					vmClass = VmType.getPrimitiveClass(ch);
					break;
				case '[' : // Array
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
						vmClass = loader.loadClass(sig, true);
					}
					break;
				case 'L' : // Object
					{
						start++;
						while (signature[ofs] != ';') {
							ofs++;
						}
						String sig = new String(signature, start, ofs - start).replace('/', '.');
						ofs++;
						vmClass = loader.loadClass(sig, true);
					}
					break;
				default :
					throw new ClassFormatError(
						"Unknown signature character " + ch);
			}
			if (vmClass == null) {
			    throw new RuntimeException("vmClass is null for signature character " + ch);
			}
			list.add(vmClass);
		}
		return (VmType[])list.toArray(new VmType[list.size()]);
	}

	/**
	 * Convert the given class to a signature
	 * @param cls
	 * @return String
	 */
	public static String toSignature(Class cls) {
		if (cls == null) {
			throw new NullPointerException("cls==null");
		}
		
		if (cls.isArray()) {
			return "[" + toSignature(cls.getComponentType());
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
			return "L" + cls.getName().replace('.', '/') + ";";
		}
	}

	/**
	 * Convert the given class array to a signature
	 * @param returnType
	 * @param argTypes
	 * @return String
	 */
	public static String toSignature(Class returnType, Class[] argTypes) {
		StringBuffer b = new StringBuffer();
		b.append('(');
		if (argTypes != null) {
			for (int i = 0; i < argTypes.length; i++) {
				b.append(toSignature(argTypes[i]));
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
