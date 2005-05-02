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

import gnu.java.lang.VMClassHelper;

import java.io.UTFDataFormatException;
import java.nio.ByteBuffer;
import java.security.ProtectionDomain;

import org.jnode.system.BootLog;
import org.jnode.vm.NoFieldAlignments;

/**
 * Decoder of .class files into VmType instances.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class ClassDecoder {

    // ------------------------------------------
    // VM ClassLoader Code
    // ------------------------------------------

    private static char[] CodeAttrName;

    private static char[] ConstantValueAttrName;

    private static char[] ExceptionsAttrName;

    private static char[] LineNrTableAttrName;

    private static final int NO_FIELD_ALIGNMENT = 0x0001;

    // private static final Logger log = Logger.getLogger(ClassDecoder.class);

    private static final byte[] TYPE_SIZES = { 1, 2, 4, 8 };

    private static final Class< ? >[] BOOT_TYPES = new Class[] { Class.class,
            String.class, Integer.class, Long.class };

    /**
     * Align the given value on the given alignment.
     * 
     * @param value
     * @param alignment
     * @return
     */
    private static final int align(int value, int alignment) {
        while ((value % alignment) != 0) {
            value++;
        }
        return value;
    }

    /**
     * Is the given type of the BOOT_TYPES classes.
     * 
     * @param type
     * @return
     */
    private static final boolean isBootType(VmType< ? > type) {
        final String typeName = type.getName();
        for (Class< ? > c : BOOT_TYPES) {
            if (c.getName().equals(typeName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Align the offsets of the fields in the class optimized for minimal object
     * size.
     * 
     * @param fields
     * @return The objectsize taken by all the fields
     */
    private static final int alignInstanceFields(VmField[] fields, int slotSize) {
        int objectSize = 0;
        for (byte currentTypeSize : TYPE_SIZES) {
            boolean aligned = false;
            for (VmField f : fields) {
                if (!f.isStatic() && (f.getTypeSize() == currentTypeSize)) {
                    if (!aligned) {
                        // Align on the current type size
                        objectSize = align(objectSize, Math.min(
                                currentTypeSize, slotSize));
                        aligned = true;
                    }
                    final VmInstanceField fld = (VmInstanceField) f;
                    fld.setOffset(objectSize);
                    objectSize += currentTypeSize;
                }
            }
        }
        // Make sure the object size is 32-bit aligned
        return align(objectSize, 4);
    }

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
     * Decode a given class.
     * 
     * @param data
     * @param offset
     * @param class_image_length
     * @param rejectNatives
     * @param clc
     * @return The decoded class
     * @throws ClassFormatError
     */
    private static final VmType decodeClass(ByteBuffer data,
            boolean rejectNatives, VmClassLoader clc,
            ProtectionDomain protectionDomain) throws ClassFormatError {
        final VmSharedStatics sharedStatics = clc.getSharedStatics();
        final VmIsolatedStatics isolatedStatics = clc.getIsolatedStatics();
        final int slotSize = clc.getArchitecture().getReferenceSize();

        final int magic = data.getInt();
        if (magic != 0xCAFEBABE) {
            throw new ClassFormatError("invalid magic");
        }
        final int min_version = data.getChar();
        final int maj_version = data.getChar();

        if (false) {
            BootLog.debug("Class file version " + maj_version + ";"
                    + min_version);
        }

        final int cpcount = data.getChar();
        // allocate enough space for the CP
        final byte[] tags = new byte[cpcount];
        final VmCP cp = new VmCP(cpcount);
        for (int i = 1; i < cpcount; i++) {
            final int tag = data.get() & 0xFF;
            tags[i] = (byte) tag;
            switch (tag) {
            case 1:
                // Utf8
                cp.setUTF8(i, readUTF(data));
                break;
            case 3:
                // int
                cp.setInt(i, data.getInt());
                break;
            case 4:
                // float
                // cp.setInt(i, data.getInt());
                final int ival = data.getInt();
                final float fval = Float.intBitsToFloat(ival);
                cp.setFloat(i, fval);
                break;
            case 5:
                // long
                cp.setLong(i, data.getLong());
                i++;
                break;
            case 6:
                // double
                // cp.setLong(i, data.getLong());
                final long lval = data.getLong();
                final double dval = Double.longBitsToDouble(lval);
                cp.setDouble(i, dval);
                i++;
                break;
            case 7:
                // class
                cp.setInt(i, data.getChar());
                break;
            case 8:
                // String
                cp.setInt(i, data.getChar());
                break;
            case 9: // Fieldref
            case 10: // Methodref
            case 11: // IMethodref
            {
                final int clsIdx = data.getChar();
                final int ntIdx = data.getChar();
                cp.setInt(i, clsIdx << 16 | ntIdx);
            }
                break;
            case 12:
            // Name and Type
            {
                final int nIdx = data.getChar();
                final int dIdx = data.getChar();
                cp.setInt(i, nIdx << 16 | dIdx);
            }
                break;
            default:
                throw new ClassFormatError("Invalid constantpool tag: "
                        + tags[i]);
            }
        }

        // Now patch the required entries (level 1)
        for (int i = 1; i < cpcount; i++) {
            switch (tags[i]) {
            case 7: {
                // Class
                final int idx = cp.getInt(i);
                final VmConstClass constClass = new VmConstClass(cp
                        .getUTF8(idx));
                cp.setConstClass(i, constClass);
                break;
            }
            case 8: {
                // String
                final int idx = cp.getInt(i);
                final int staticsIdx = sharedStatics
                        .allocConstantStringField(cp.getUTF8(idx));
                final VmConstString constStr = new VmConstString(staticsIdx);
                cp.setString(i, constStr);
                break;
            }
            }
        }

        // Now patch the required entries (level 2)
        for (int i = 1; i < cpcount; i++) {
            final int tag = tags[i];
            if ((tag >= 9) && (tag <= 11)) {
                final int v = cp.getInt(i);
                final VmConstClass constClass = cp.getConstClass(v >>> 16);
                final int nat = cp.getInt(v & 0xFFFF);
                final String name = cp.getUTF8(nat >>> 16);
                final String descriptor = cp.getUTF8(nat & 0xFFFF);
                switch (tag) {
                case 9:
                    // FieldRef
                    cp.setConstFieldRef(i, new VmConstFieldRef(constClass,
                            name, descriptor));
                    break;
                case 10:
                    // MethodRef
                    cp.setConstMethodRef(i, new VmConstMethodRef(constClass,
                            name, descriptor));
                    break;
                case 11:
                    // IMethodRef
                    cp.setConstIMethodRef(i, new VmConstIMethodRef(constClass,
                            name, descriptor));
                    break;
                }
            }
        }

        // Cleanup the unwantend entries
        for (int i = 1; i < cpcount; i++) {
            switch (tags[i]) {
            case 12:
                // Name and Type
                cp.reset(i);
                break;
            }
        }

        final int classModifiers = data.getChar();

        final VmConstClass this_class = cp.getConstClass(data.getChar());
        final String clsName = this_class.getClassName();

        final VmConstClass super_class = cp.getConstClass(data.getChar());
        final String superClassName;
        if (super_class != null) {
            superClassName = super_class.getClassName();
        } else {
            superClassName = null;
        }

        // Allocate the class object
        final VmType cls;
        if (Modifier.isInterface(classModifiers)) {
            cls = new VmInterfaceClass(clsName, superClassName, clc,
                    classModifiers, protectionDomain);
        } else {
            cls = new VmNormalClass(clsName, superClassName, clc,
                    classModifiers, protectionDomain);
        }
        cls.setCp(cp);

        // Determine if we can safely align the fields
        int flags = 0;
        if (isBootType(cls)) {
            flags |= NO_FIELD_ALIGNMENT;
        }

        // Interface table
        flags |= readInterfaces(data, cls, cp);

        // Field table
        readFields(data, cls, cp, sharedStatics, isolatedStatics, slotSize,
                flags);

        // Method Table
        readMethods(data, rejectNatives, cls, cp, sharedStatics, clc);

        return cls;
    }

    /**
     * Convert a class-file image into a Class object. Steps taken in this
     * phase: 1. Decode the class-file image (CLS_LS_DECODED) 2. Load the
     * super-class of the loaded class (CLS_LS_DEFINED) 3. Link the class so
     * that the VMT is set and the offset of the non-static fields are set
     * correctly.
     * 
     * @param className
     * @param data
     * @param offset
     * @param class_image_length
     * @param rejectNatives
     * @param clc
     * @return The defined class
     */
    public static final VmType defineClass(String className, ByteBuffer data,
            boolean rejectNatives, VmClassLoader clc,
            ProtectionDomain protectionDomain) {
        cl_init();
        final VmType cls = decodeClass(data, rejectNatives, clc,
                protectionDomain);
        return cls;
    }

    /**
     * Gets the bytecode of a native code replacement method.
     * 
     * @param method
     * @param cl
     * @return
     */
    private static VmByteCode getNativeCodeReplacement(VmMethod method,
            VmClassLoader cl, boolean verbose) {
        final String className = method.getDeclaringClass().getName();
        final String pkg = VMClassHelper.getPackagePortion(className);
        final String nativeClassName = pkg + ((pkg.length() > 0) ? "." : "")
                + "Native" + VMClassHelper.getClassNamePortion(className);
        final VmType nativeType;
        try {
            nativeType = cl.loadClass(nativeClassName, true);
        } catch (ClassNotFoundException ex) {
            if (verbose) {
                BootLog.error("Native class replacement (" + nativeClassName
                        + ") not found");
            }
            return null;
        }
        final VmType[] argTypes;
        if (method.isStatic()) {
            final int argCount = method.getNoArguments();
            argTypes = new VmType[argCount];
            for (int i = 0; i < argCount; i++) {
                argTypes[i] = method.getArgumentType(i);
            }
        } else {
            final int argCount = method.getNoArguments();
            argTypes = new VmType[argCount + 1];
            argTypes[0] = method.getDeclaringClass();
            for (int i = 0; i < argCount; i++) {
                argTypes[i + 1] = method.getArgumentType(i);
            }
        }

        final VmMethod nativeMethod = nativeType.getMethod(method.getName(),
                argTypes);
        if (nativeMethod == null) {
            if (verbose) {
                BootLog.error("Native method replacement (" + method
                        + ") not found");
            }
            return null;
        }
        if (!nativeMethod.isStatic()) {
            throw new ClassFormatError(
                    "Native method replacement must be static");
        }
        return nativeMethod.getBytecode();
    }

    /**
     * Decode the data of a code-attribute
     * 
     * @param reader
     * @param cls
     * @param cp
     * @param method
     * @return The read code
     */
    private static final VmByteCode readCode(ByteBuffer data, VmType cls,
            VmCP cp, VmMethod method) {

        final int maxStack = data.getChar();
        final int noLocals = data.getChar();
        final int codelength = data.getInt();
        final ByteBuffer code = readBytes(data, codelength);

        // Read the exception Table
        final int ecount = data.getChar();
        final VmInterpretedExceptionHandler[] etable = new VmInterpretedExceptionHandler[ecount];
        for (int i = 0; i < ecount; i++) {
            final int startPC = data.getChar();
            final int endPC = data.getChar();
            final int handlerPC = data.getChar();
            final int catchType = data.getChar();
            etable[i] = new VmInterpretedExceptionHandler(cp, startPC, endPC,
                    handlerPC, catchType);
        }

        // Read the attributes
        VmLineNumberMap lnTable = null;
        final int acount = data.getChar();
        for (int i = 0; i < acount; i++) {
            final String attrName = cp.getUTF8(data.getChar());
            final int len = data.getInt();
            if (VmArray.equals(LineNrTableAttrName, attrName)) {
                lnTable = readLineNrTable(data);
            } else {
                skip(data, len);
            }
        }

        return new VmByteCode(method, code, noLocals, maxStack, etable, lnTable);
    }

    /**
     * Decode the data of a Exceptions attribute
     * 
     * @param reader
     * @param cls
     * @param cp
     * @return The read exceptions
     */
    private static final VmExceptions readExceptions(ByteBuffer data,
            VmType cls, VmCP cp) {

        // Read the exceptions
        final int ecount = data.getChar();
        final VmConstClass[] list = new VmConstClass[ecount];
        for (int i = 0; i < ecount; i++) {
            final int idx = data.getChar();
            list[i] = cp.getConstClass(idx);
        }

        return new VmExceptions(list);
    }

    /**
     * Read the fields table
     * 
     * @param reader
     * @param cls
     * @param cp
     * @param slotSize
     */
    private static void readFields(ByteBuffer data, VmType< ? > cls, VmCP cp,
            VmSharedStatics sharedStatics, VmIsolatedStatics isolatedStatics,
            int slotSize, int flags) {
        final int fcount = data.getChar();
        if (fcount > 0) {
            final VmField[] ftable = new VmField[fcount];

            int objectSize = 0;
            for (int i = 0; i < fcount; i++) {
                final boolean wide;
                int modifiers = data.getChar();
                final String name = cp.getUTF8(data.getChar());
                final String signature = cp.getUTF8(data.getChar());
                switch (signature.charAt(0)) {
                case 'J':
                case 'D':
                    modifiers = modifiers | Modifier.ACC_WIDE;
                    wide = true;
                    break;
                default:
                    wide = false;
                }
                final boolean isstatic = (modifiers & Modifier.ACC_STATIC) != 0;
                final int staticsIdx;
                final VmField fs;
                final VmStatics statics;
                if (isstatic) {
                    // Determine if the static field should be shared.
                    final boolean shared = cls.isSharedStatics();
                    if (shared) {
                        statics = sharedStatics;
                    } else {
                        statics = isolatedStatics;
                    }

                    // If static allocate space for it.
                    switch (signature.charAt(0)) {
                    case 'B':
                        staticsIdx = statics.allocIntField();
                        break;
                    case 'C':
                        staticsIdx = statics.allocIntField();
                        break;
                    case 'D':
                        staticsIdx = statics.allocLongField();
                        break;
                    case 'F':
                        staticsIdx = statics.allocIntField();
                        break;
                    case 'I':
                        staticsIdx = statics.allocIntField();
                        break;
                    case 'J':
                        staticsIdx = statics.allocLongField();
                        break;
                    case 'S':
                        staticsIdx = statics.allocIntField();
                        break;
                    case 'Z':
                        staticsIdx = statics.allocIntField();
                        break;
                    default: {
                        if (Modifier.isAddressType(signature)) {
                            staticsIdx = statics.allocAddressField();
                        } else {
                            staticsIdx = statics.allocObjectField();
                            // System.out.println(NumberUtils.hex(staticsIdx)
                            // + "\t" + cls.getName() + "." + name);
                        }
                    }
                        break;
                    }
                    fs = new VmStaticField(name, signature, modifiers,
                            staticsIdx, cls, slotSize, shared);
                } else {
                    staticsIdx = -1;
                    statics = null;
                    final int fieldOffset;
                    // Set the offset (keep in mind that this will be fixed
                    // by ClassResolver with respect to the objectsize of the
                    // super-class.
                    fieldOffset = objectSize;
                    // Increment the objectSize
                    if (wide)
                        objectSize += 8;
                    else if (Modifier.isPrimitive(signature)) {
                        objectSize += 4;
                    } else {
                        objectSize += slotSize;
                    }
                    fs = new VmInstanceField(name, signature, modifiers,
                            fieldOffset, cls, slotSize);
                }
                ftable[i] = fs;

                // Read field attributes
                final int acount = data.getChar();
                for (int a = 0; a < acount; a++) {
                    final String attrName = cp.getUTF8(data.getChar());
                    final int length = data.getInt();
                    if (isstatic
                            && VmArray.equals(ConstantValueAttrName, attrName)) {
                        final int idx = data.getChar();
                        switch (signature.charAt(0)) {
                        case 'B':
                            statics.setInt(staticsIdx, cp.getInt(idx));
                            break;
                        case 'C':
                            statics.setInt(staticsIdx, cp.getInt(idx));
                            break;
                        case 'D':
                            final long lval = Double.doubleToRawLongBits(cp
                                    .getDouble(idx));
                            statics.setLong(staticsIdx, lval);
                            break;
                        case 'F':
                            final int ival = Float.floatToRawIntBits(cp
                                    .getFloat(idx));
                            statics.setInt(staticsIdx, ival);
                            break;
                        case 'I':
                            statics.setInt(staticsIdx, cp.getInt(idx));
                            break;
                        case 'J':
                            statics.setLong(staticsIdx, cp.getLong(idx));
                            break;
                        case 'S':
                            statics.setInt(staticsIdx, cp.getInt(idx));
                            break;
                        case 'Z':
                            statics.setInt(staticsIdx, cp.getInt(idx));
                            break;
                        default:
                            // throw new IllegalArgumentException("signature "
                            // + signature);
                            statics.setObject(staticsIdx, cp.getString(idx));
                            break;
                        }
                    } else {
                        skip(data, length);
                    }
                }
            }

            // Align the instance fields for minimal object size.
            if ((flags & NO_FIELD_ALIGNMENT) == 0) {
                final int alignedObjectSize = alignInstanceFields(ftable,
                        slotSize);
                objectSize = alignedObjectSize;
            }

            cls.setFieldTable(ftable);
            if (objectSize > 0) {
                ((VmNormalClass) cls).setObjectSize(objectSize);
            }
        }
    }

    /**
     * Read the interfaces table
     * 
     * @param reader
     * @param cls
     * @param cp
     * @return Some flags
     */
    private static int readInterfaces(ByteBuffer data, VmType cls, VmCP cp) {
        int flags = 0;
        final int icount = data.getChar();
        if (icount > 0) {
            final String noFieldAlignmentsName = NoFieldAlignments.class
                    .getName();
            final VmImplementedInterface[] itable = new VmImplementedInterface[icount];
            for (int i = 0; i < icount; i++) {
                final VmConstClass icls = cp.getConstClass(data.getChar());
                final String iclsName = icls.getClassName();
                itable[i] = new VmImplementedInterface(iclsName);
                if (iclsName.equals(noFieldAlignmentsName)) {
                    flags |= NO_FIELD_ALIGNMENT;
                }
            }
            cls.setInterfaceTable(itable);
        }
        return flags;
    }

    /**
     * Decode the data of a LineNumberTable-attribute
     * 
     * @param reader
     * @return The line number map
     */
    private static final VmLineNumberMap readLineNrTable(ByteBuffer data) {
        final int len = data.getChar();
        final char[] lnTable = new char[len * VmLineNumberMap.LNT_ELEMSIZE];

        for (int i = 0; i < len; i++) {
            final int ofs = i * VmLineNumberMap.LNT_ELEMSIZE;
            lnTable[ofs + VmLineNumberMap.LNT_STARTPC_OFS] = data.getChar();
            lnTable[ofs + VmLineNumberMap.LNT_LINENR_OFS] = data.getChar();
        }

        return new VmLineNumberMap(lnTable);
    }

    /**
     * Read the method table
     * 
     * @param reader
     * @param rejectNatives
     * @param cls
     * @param cp
     */
    private static void readMethods(ByteBuffer data, boolean rejectNatives,
            VmType cls, VmCP cp, VmStatics statics, VmClassLoader cl) {
        final int mcount = data.getChar();
        if (mcount > 0) {
            final VmMethod[] mtable = new VmMethod[mcount];

            for (int i = 0; i < mcount; i++) {
                final int modifiers = data.getChar();
                final String name = cp.getUTF8(data.getChar());
                final String signature = cp.getUTF8(data.getChar());
                final boolean isStatic = ((modifiers & Modifier.ACC_STATIC) != 0);

                final VmMethod mts;
                final boolean isSpecial = name.equals("<init>");
                // final int staticsIdx = statics.allocMethod();
                if (isStatic || isSpecial) {
                    if (isSpecial) {
                        mts = new VmSpecialMethod(name, signature, modifiers,
                                cls);
                    } else {
                        mts = new VmStaticMethod(name, signature, modifiers,
                                cls);
                    }
                } else {
                    mts = new VmInstanceMethod(name, signature, modifiers, cls);
                }
                // statics.setMethod(staticsIdx, mts);
                mtable[i] = mts;

                // Read methods attributes
                final int acount = data.getChar();
                for (int a = 0; a < acount; a++) {
                    String attrName = cp.getUTF8(data.getChar());
                    int length = data.getInt();
                    if (VmArray.equals(CodeAttrName, attrName)) {
                        mts.setBytecode(readCode(data, cls, cp, mts));
                    } else if (VmArray.equals(ExceptionsAttrName, attrName)) {
                        mts.setExceptions(readExceptions(data, cls, cp));
                    } else {
                        skip(data, length);
                    }
                }
                if ((modifiers & Modifier.ACC_NATIVE) != 0) {
                    final VmByteCode bc = getNativeCodeReplacement(mts, cl,
                            rejectNatives);
                    if (bc != null) {
                        mts.setModifier(false, Modifier.ACC_NATIVE);
                        mts.setBytecode(bc);
                    } else {
                        if (rejectNatives) {
                            throw new ClassFormatError("Native method " + mts);
                        }
                    }
                }
            }
            cls.setMethodTable(mtable);
        }
    }
    
    private static final void skip(ByteBuffer data, int delta) {
        data.position(data.position() + delta);
    }

    private static final ByteBuffer readBytes(ByteBuffer data, int length) {
        final ByteBuffer result = (ByteBuffer)data.slice().limit(length);
        data.position(data.position() + length);
        return result;
    }

    private static final String readUTF(ByteBuffer data) {
        final int utflen = data.getChar();
        final String result;
        try {
            result = VmUTF8Convert.fromUTF8(data, utflen);
        } catch (UTFDataFormatException ex) {
            throw (ClassFormatError) new ClassFormatError(
                    "Invalid UTF sequence").initCause(ex);
        }
        return result;
    }
}
