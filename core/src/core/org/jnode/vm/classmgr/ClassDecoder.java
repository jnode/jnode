/**
 * $Id$
 */

package org.jnode.vm.classmgr;

import org.jnode.system.BootLog;

public final class ClassDecoder {
	// ------------------------------------------
	// VM ClassLoader Code
	// ------------------------------------------

	private static char[] ConstantValueAttrName;
	private static char[] CodeAttrName;
	private static char[] ExceptionsAttrName;
	private static char[] LineNrTableAttrName;
	//private static final Logger log = Logger.getLogger(ClassDecoder.class);

	private static final void cl_init() {
		if (ConstantValueAttrName == null) {
			ConstantValueAttrName = "ConstantValue".toCharArray();
		}
		if (CodeAttrName == null) {
			CodeAttrName = "Code".toCharArray();
		}
		if (ExceptionsAttrName == null) {
			ExceptionsAttrName = "Exceptions".toCharArray();
		}
		if (LineNrTableAttrName == null) {
			LineNrTableAttrName = "LineNumberTable".toCharArray();
		}
	}

	/**
	 * Convert a class-file image into a Class object. Steps taken in this phase: 1. Decode the
	 * class-file image (CLS_LS_DECODED) 2. Load the super-class of the loaded class
	 * (CLS_LS_DEFINED) 3. Link the class so that the VMT is set and the offset of the non-static
	 * fields are set correctly.
	 * 
	 * @param className
	 * @param data
	 * @param offset
	 * @param class_image_length
	 * @param rejectNatives
	 * @param clc
	 * @param selectorMap
	 * @return The defined class
	 */
	public static final VmType defineClass(
		String className,
		byte[] data,
		int offset,
		int class_image_length,
		boolean rejectNatives,
		AbstractVmClassLoader clc,
		SelectorMap selectorMap) {
		cl_init();
		VmType cls = decodeClass(data, offset, class_image_length, rejectNatives, clc, selectorMap);
		return cls;
	}

	/**
	 * Decode a given class.
	 * @param data
	 * @param offset
	 * @param class_image_length
	 * @param rejectNatives
	 * @param clc
	 * @param selectorMap
	 * @return The decoded class
	 * @throws ClassFormatError
	 */
	private static final VmType decodeClass(byte[] data, int offset, int class_image_length, boolean rejectNatives, AbstractVmClassLoader clc, SelectorMap selectorMap)
		throws ClassFormatError {
		if (data == null) {
			throw new ClassFormatError("ClassDecoder.decodeClass: data==null");
		}
		final int[] pos = new int[1];
		final int slotSize = clc.getArchitecture().getReferenceSize();
		pos[0] = offset;

		final int magic = cl_readu4(data, pos);
		if (magic != 0xCAFEBABE) {
			throw new ClassFormatError("invalid magic");
		}
		final int min_version = cl_readu2(data, pos);
		final int maj_version = cl_readu2(data, pos);

		if (false) {
			BootLog.debug("Class file version " + maj_version + ";" + min_version);
		}

		final int cpcount = cl_readu2(data, pos);
		// allocate enough space for the CP
		final byte[] tags = new byte[cpcount];
		final VmCP cp = new VmCP(cpcount, tags);
		for (int i = 1; i < cpcount; i++) {
			final int tag = cl_readu1(data, pos);
			tags[i] = (byte) tag;
			switch (tag) {
				case 1 : // Utf8
					cp.setUTF8(i, new String(readUTF(data, pos)));
					break;
				case 3 : // int
					cp.setInt(i, cl_readu4(data, pos));
					break;
				case 4 : // float
					cp.setInt(i, cl_readu4(data, pos));
					break;
				case 5 : // long
					cp.setLong(i, cl_readu8(data, pos));
					i++;
					break;
				case 6 : // double
					cp.setLong(i, cl_readu8(data, pos));
					i++;
					break;
				case 7 : // class
					cp.setConstClass(i, new VmConstClass(cp, i, cl_readu2(data, pos)));
					break;
				case 8 : // String
					cp.setInt(i, cl_readu2(data, pos));
					break;
				case 9 : // Fieldref
					{
						final int clsIdx = cl_readu2(data, pos);
						final int ntIdx = cl_readu2(data, pos);
						cp.setConstFieldRef(i, new VmConstFieldRef(cp, i, clsIdx, ntIdx));
					}
					break;
				case 10 : // Methodref
					{
						final int clsIdx = cl_readu2(data, pos);
						final int ntIdx = cl_readu2(data, pos);
						cp.setConstMethodRef(i, new VmConstMethodRef(cp, i, clsIdx, ntIdx));
					}
					break;
				case 11 : // IMethodref
					{
						final int clsIdx = cl_readu2(data, pos);
						final int ntIdx = cl_readu2(data, pos);
						cp.setConstIMethodRef(i, new VmConstIMethodRef(cp, i, clsIdx, ntIdx));
					}
					break;
				case 12 : // Name and Type
					{
						final int nIdx = cl_readu2(data, pos);
						final int dIdx = cl_readu2(data, pos);
						cp.setConstNameAndType(i, new VmConstNameAndType(cp, nIdx, dIdx));
					}
					break;
				default :
					throw new ClassFormatError("Invalid constantpool tag: " + tags[i]);
			}
		}

		// Now patch the required entries
		for (int i = 1; i < cpcount; i++) {
			switch (tags[i]) {
				case 8 : // String
					final int idx = cp.getInt(i);
					cp.setString(i, cp.getUTF8(idx));
					break;
			}
		}

		final int classModifiers = cl_readu2(data, pos);

		final VmConstClass this_class = cp.getConstClass(cl_readu2(data, pos));
		final String clsName = this_class.getClassName();

		final VmConstClass super_class = cp.getConstClass(cl_readu2(data, pos));
		final String superClassName;
		if (super_class != null) {
			superClassName = super_class.getClassName();
		} else {
			superClassName = null;
		}

		// Allocate the class object
		final VmType cls;
		if (Modifier.isInterface(classModifiers)) {
			cls = new VmInterfaceClass(clsName, superClassName, clc, classModifiers);
		} else {
			cls = new VmNormalClass(clsName, superClassName, clc, classModifiers);
		}
		cls.setCp(cp);

		// Interface table
		readInterfaces(data, pos, cls, cp);

		// Field table
		readFields(data, pos, cls, cp, slotSize);

		// Method Table
		readMethods(data, rejectNatives, pos, cls, cp, selectorMap);

		return cls;
	}

	/**
	 * Read the interfaces table
	 * 
	 * @param data
	 * @param pos
	 * @param cls
	 * @param cp
	 */
	private static void readInterfaces(byte[] data, int[] pos, VmType cls, VmCP cp) {
		final int icount = cl_readu2(data, pos);
		if (icount > 0) {
			final VmImplementedInterface[] itable = new VmImplementedInterface[icount];
			for (int i = 0; i < icount; i++) {
				final VmConstClass icls = cp.getConstClass(cl_readu2(data, pos));
				itable[i] = new VmImplementedInterface(icls.getClassName());
			}
			cls.setInterfaceTable(itable);
		}
	}

	/**
	 * Read the fields table
	 * 
	 * @param data
	 * @param pos
	 * @param cls
	 * @param cp
	 * @param slotSize
	 */
	private static void readFields(byte[] data, int[] pos, VmType cls, VmCP cp, int slotSize) {
		final int fcount = cl_readu2(data, pos);
		if (fcount > 0) {
			final VmField[] ftable = new VmField[fcount];

			int objectSize = 0;
			for (int i = 0; i < fcount; i++) {
				final boolean wide;
				int modifiers = cl_readu2(data, pos);
				final String name = cp.getUTF8(cl_readu2(data, pos));
				final String signature = cp.getUTF8(cl_readu2(data, pos));
				switch (signature.charAt(0)) {
					case 'J' :
					case 'D' :
						modifiers = modifiers | Modifier.ACC_WIDE;
						wide = true;
						break;
					default :
						wide = false;
				}
				final boolean isstatic = (modifiers & Modifier.ACC_STATIC) != 0;
				Object staticData = null;
				final VmField fs;
				if (isstatic) {
					// If static allocate space for it.
					switch (signature.charAt(0)) {
						case 'B' :
							staticData = new VmStaticByte();
							break;
						case 'C' :
							staticData = new VmStaticChar();
							break;
						case 'D' :
							staticData = new VmStaticDouble();
							break;
						case 'F' :
							staticData = new VmStaticFloat();
							break;
						case 'I' :
							staticData = new VmStaticInt();
							break;
						case 'J' :
							staticData = new VmStaticLong();
							break;
						case 'S' :
							staticData = new VmStaticShort();
							break;
						case 'Z' :
							staticData = new VmStaticBoolean();
							break;
						default :
							{
								if (Modifier.isAddressType(signature)) {
									staticData = new VmStaticAddress();
								} else {
									staticData = new VmStaticObject();
								}
							}
							break;
					}
					fs = new VmStaticField(name, signature, modifiers, staticData, cls, slotSize);
				} else {
					final int fieldOffset;
					// Set the offset (keep in mind that this will be fixed
					// by ClassResolver with respect to the objectsize of the super-class.
					fieldOffset = objectSize;
					// Increment the objectSize
					if (wide)
						objectSize += 8;
					else
						objectSize += 4;
					fs = new VmInstanceField(name, signature, modifiers, fieldOffset, cls, slotSize);
				}
				ftable[i] = fs;

				// Read field attributes
				final int acount = cl_readu2(data, pos);
				for (int a = 0; a < acount; a++) {
					final String attrName = cp.getUTF8(cl_readu2(data, pos));
					final int length = cl_readu4(data, pos);
					if (isstatic && VmArray.equals(ConstantValueAttrName, attrName)) {
						final int idx = cl_readu2(data, pos);
						switch (signature.charAt(0)) {
							case 'B' :
								 ((VmStaticByte) staticData).setValue((byte) cp.getInt(idx));
								break;
							case 'C' :
								 ((VmStaticChar) staticData).setValue((char) cp.getInt(idx));
								break;
							case 'D' :
								 ((VmStaticDouble) staticData).setValue(Double.longBitsToDouble(cp.getLong(idx)));
								break;
							case 'F' :
								 ((VmStaticFloat) staticData).setValue(Float.intBitsToFloat(cp.getInt(idx)));
								break;
							case 'I' :
								 ((VmStaticInt) staticData).setValue(cp.getInt(idx));
								break;
							case 'J' :
								 ((VmStaticLong) staticData).setValue(cp.getLong(idx));
								break;
							case 'S' :
								 ((VmStaticShort) staticData).setValue((short) cp.getInt(idx));
								break;
							case 'Z' :
								 ((VmStaticBoolean) staticData).setValue(cp.getInt(idx) != 0);
								break;
							default :
								 ((VmStaticObject) staticData).setValue(cp.getString(idx));
								break;
						}
					} else {
						cl_skip(pos, length);
					}
				}
			}
			cls.setFieldTable(ftable);
			if (objectSize > 0) {
				((VmNormalClass) cls).setObjectSize(objectSize);
			}
		}
	}

	/**
	 * Read the method table
	 * 
	 * @param data
	 * @param rejectNatives
	 * @param pos
	 * @param cls
	 * @param cp
	 * @param selectorMap
	 */
	private static void readMethods(byte[] data, boolean rejectNatives, int[] pos, VmType cls, VmCP cp, SelectorMap selectorMap) {
		final int mcount = cl_readu2(data, pos);
		if (mcount > 0) {
			final VmMethod[] mtable = new VmMethod[mcount];

			for (int i = 0; i < mcount; i++) {
				final int modifiers = cl_readu2(data, pos);
				final String name = cp.getUTF8(cl_readu2(data, pos));
				final String signature = cp.getUTF8(cl_readu2(data, pos));
				int argSlotCount = Signature.getArgSlotCount(signature);
				final boolean isStatic = ((modifiers & Modifier.ACC_STATIC) != 0);

				if ((modifiers & Modifier.ACC_STATIC) == 0) {
					argSlotCount++; // add the "this" argument
				}

				final VmMethod mts;
				if (isStatic) {
					mts = new VmStaticMethod(name, signature, modifiers, cls, argSlotCount);
				} else {
					mts = new VmInstanceMethod(name, signature, modifiers, cls, argSlotCount, selectorMap);
				}
				mtable[i] = mts;

				// Read methods attributes
				final int acount = cl_readu2(data, pos);
				for (int a = 0; a < acount; a++) {
					String attrName = cp.getUTF8(cl_readu2(data, pos));
					int length = cl_readu4(data, pos);
					if (VmArray.equals(CodeAttrName, attrName)) {
						mts.setBytecode(readCode(data, pos, cls, cp, mts));
					} else if (VmArray.equals(ExceptionsAttrName, attrName)) {
						mts.setExceptions(readExceptions(data, pos, cls, cp));
					} else {
						cl_skip(pos, length);
					}
				}
				if ((modifiers & Modifier.ACC_NATIVE) != 0) {
					if (rejectNatives) {
						throw new ClassFormatError("Native method " + mts);
					}
				}
			}
			cls.setMethodTable(mtable);
		}
	}

	/**
	 * Decode the data of a code-attribute
	 * @param data
	 * @param pos
	 * @param cls
	 * @param cp
	 * @param method
	 * @return The read code
	 */
	private static final VmByteCode readCode(byte[] data, int[] pos, VmType cls, VmCP cp, VmMethod method) {

		final int maxStack = cl_readu2(data, pos);
		final int noLocals = cl_readu2(data, pos);
		final int codelength = cl_readu4(data, pos);
		final byte[] code = readBytes(data, pos, codelength);

		// Read the exception Table
		final int ecount = cl_readu2(data, pos);
		final VmInterpretedExceptionHandler[] etable = new VmInterpretedExceptionHandler[ecount];
		for (int i = 0; i < ecount; i++) {
			final int startPC = cl_readu2(data, pos);
			final int endPC = cl_readu2(data, pos);
			final int handlerPC = cl_readu2(data, pos);
			final int catchType = cl_readu2(data, pos);
			etable[i] = new VmInterpretedExceptionHandler(cp, startPC, endPC, handlerPC, catchType);
		}

		// Read the attributes
		VmLineNumberMap lnTable = null;
		final int acount = cl_readu2(data, pos);
		for (int i = 0; i < acount; i++) {
			final String attrName = cp.getUTF8(cl_readu2(data, pos));
			final int len = cl_readu4(data, pos);
			if (VmArray.equals(LineNrTableAttrName, attrName)) {
				lnTable = readLineNrTable(data, pos);
			} else {
				cl_skip(pos, len);
			}
		}

		return new VmByteCode(method, code, noLocals, maxStack, etable, lnTable);
	}

	/**
	 * Decode the data of a Exceptions attribute
	 * @param data
	 * @param pos
	 * @param cls
	 * @param cp
	 * @return The read exceptions
	 */
	private static final VmExceptions readExceptions(byte[] data, int[] pos, VmType cls, VmCP cp) {

		// Read the exceptions
		final int ecount = cl_readu2(data, pos);
		final VmConstClass[] list = new VmConstClass[ecount];
		for (int i = 0; i < ecount; i++) {
			final int idx = cl_readu2(data, pos);
			list[i] = cp.getConstClass(idx);
		}

		return new VmExceptions(list);
	}

	/**
	 * Decode the data of a LineNumberTable-attribute
	 * @param data
	 * @param pos
	 * @return The line number map
	 */
	private static final VmLineNumberMap readLineNrTable(byte[] data, int[] pos) {
		final int len = cl_readu2(data, pos);
		final char[] lnTable = new char[len * VmLineNumberMap.LNT_ELEMSIZE];

		for (int i = 0; i < len; i++) {
			final int ofs = i * VmLineNumberMap.LNT_ELEMSIZE;
			lnTable[ofs + VmLineNumberMap.LNT_STARTPC_OFS] = (char) cl_readu2(data, pos);
			lnTable[ofs + VmLineNumberMap.LNT_LINENR_OFS] = (char) cl_readu2(data, pos);
		}

		return new VmLineNumberMap(lnTable);
	}

	private static final void cl_skip(int[] pos, int delta) {
		pos[0] += delta;
	}

	private static final int cl_readu1(byte[] data, int[] pos) {
		int v = data[pos[0]] & 0xff;
		pos[0]++;
		return v;
	}

	private static final int cl_readu2(byte[] data, int[] pos) {
		int v = (cl_readu1(data, pos) << 8) + cl_readu1(data, pos);
		return v;
	}

	private static final int cl_readu4(byte[] data, int[] pos) {
		int v = (cl_readu1(data, pos) << 24) + (cl_readu1(data, pos) << 16) + (cl_readu1(data, pos) << 8) + cl_readu1(data, pos);
		return v;
	}

	private static final long cl_readu8(byte[] data, int[] pos) {
		long l0 = cl_readu4(data, pos);
		long l1 = cl_readu4(data, pos);
		return ((l0 & 0xFFFFFFFFL) << 32) | (l1 & 0xFFFFFFFFL);
	}

	private static final byte[] readBytes(byte[] data, int[] pos, int length) {
		byte[] res = new byte[length];
		byteArrayCopy(data, pos[0], res, 0, length);
		pos[0] += length;
		return res;
	}

	private static final char[] readUTF(byte[] data, int[] pos) {
		int utflen = cl_readu2(data, pos);
		char str[] = new char[utflen];
		int count = 0;
		int strlen = 0;
		while (count < utflen) {
			int c = cl_readu1(data, pos);
			int char2, char3;
			switch (c >> 4) {
				case 0 :
				case 1 :
				case 2 :
				case 3 :
				case 4 :
				case 5 :
				case 6 :
				case 7 :
					// 0xxxxxxx
					count++;
					str[strlen++] = (char) c;
					break;
				case 12 :
				case 13 :
					// 110x xxxx 10xx xxxx
					count += 2;
					if (count > utflen)
						throw new RuntimeException("Class Format Error (utf)");
					char2 = cl_readu1(data, pos);
					if ((char2 & 0xC0) != 0x80)
						throw new RuntimeException("Class Format Error (utf)");
					str[strlen++] = (char) (((c & 0x1F) << 6) | (char2 & 0x3F));
					break;
				case 14 :
					// 1110 xxxx 10xx xxxx 10xx xxxx
					count += 3;
					if (count > utflen)
						throw new RuntimeException("Class Format Error (utf)");
					char2 = cl_readu1(data, pos);
					char3 = cl_readu1(data, pos);
					if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
						throw new RuntimeException("Class Format Error (utf)");
					str[strlen++] = (char) (((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
					break;
				default :
					// 10xx xxxx, 1111 xxxx
					throw new RuntimeException("Class Format Error (utf)");
			}
		}
		return new String(str, 0, strlen).intern().toCharArray();
	}

	private static void byteArrayCopy(byte[] src, int src_ofs, byte[] dst, int dst_ofs, int length) {
		for (int i = 0; i < length; i++)
			dst[dst_ofs + i] = src[src_ofs + i];
	}
}
