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

package org.jnode.vm.classmgr;

import java.io.UTFDataFormatException;
import java.lang.annotation.Annotation;
import java.nio.ByteBuffer;
import java.security.ProtectionDomain;
import org.jnode.system.BootLog;
import org.jnode.vm.VmUtils;
import org.jnode.vm.annotation.AllowedPackages;
import org.jnode.vm.annotation.CheckPermission;
import org.jnode.vm.annotation.DoPrivileged;
import org.jnode.vm.annotation.Inline;
import org.jnode.vm.annotation.KernelSpace;
import org.jnode.vm.annotation.LoadStatics;
import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.annotation.NoFieldAlignments;
import org.jnode.vm.annotation.NoInline;
import org.jnode.vm.annotation.NoReadBarrier;
import org.jnode.vm.annotation.NoWriteBarrier;
import org.jnode.vm.annotation.PrivilegedActionPragma;
import org.jnode.vm.annotation.SharedStatics;
import org.jnode.vm.annotation.Uninterruptible;
import org.vmmagic.pragma.PragmaException;
import org.vmmagic.pragma.UninterruptiblePragma;

/**
 * Decoder of .class files into VmType instances.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class ClassDecoder {

    // ------------------------------------------
    // VM ClassLoader Code
    // ------------------------------------------

    private static char[] SourceFileAttrName;

    private static char[] SignatureAttrName;

    private static char[] CodeAttrName;

    private static char[] ConstantValueAttrName;

    private static char[] ExceptionsAttrName;

    private static char[] LocalVariableTableAttrName;

    private static char[] LineNrTableAttrName;

    private static char[] RuntimeVisibleAnnotationsAttrName;

    private static char[] RuntimeInvisibleAnnotationsAttrName;

    private static char[] RuntimeVisibleParameterAnnotationsAttrName;

    private static char[] RuntimeInvisibleParameterAnnotationsAttrName;

    @SuppressWarnings("deprecation")
    private static final MethodPragmaException[] METHOD_PRAGMA_EXCEPTIONS = new MethodPragmaException[]{
        new MethodPragmaException(UninterruptiblePragma.class,
            MethodPragmaFlags.UNINTERRUPTIBLE),
        new MethodPragmaException(org.vmmagic.pragma.InlinePragma.class,
            MethodPragmaFlags.INLINE),
        new MethodPragmaException(org.vmmagic.pragma.NoInlinePragma.class,
            MethodPragmaFlags.NOINLINE)};

    private static final PragmaInterface[] INTERFACE_PRAGMAS = new PragmaInterface[]{new PragmaInterface(
        org.vmmagic.pragma.Uninterruptible.class,
        TypePragmaFlags.UNINTERRUPTIBLE)};

    private static final PragmaAnnotation[] CLASS_ANNOTATIONS = new PragmaAnnotation[]{
        new PragmaAnnotation(MagicPermission.class,
            TypePragmaFlags.MAGIC_PERMISSION),
        new PragmaAnnotation(NoFieldAlignments.class,
            TypePragmaFlags.NO_FIELD_ALIGNMENT),
        new PragmaAnnotation(SharedStatics.class,
            TypePragmaFlags.SHAREDSTATICS),
        new PragmaAnnotation(Uninterruptible.class,
            TypePragmaFlags.UNINTERRUPTIBLE)};

    private static final PragmaAnnotation[] METHOD_ANNOTATIONS = new PragmaAnnotation[]{
        new PragmaAnnotation(CheckPermission.class,
            MethodPragmaFlags.CHECKPERMISSION),
        new PragmaAnnotation(DoPrivileged.class,
            MethodPragmaFlags.DOPRIVILEGED),
        new PragmaAnnotation(Inline.class, MethodPragmaFlags.INLINE),
        new PragmaAnnotation(LoadStatics.class,
            MethodPragmaFlags.LOADSTATICS),
        new PragmaAnnotation(NoInline.class, MethodPragmaFlags.NOINLINE),
        new PragmaAnnotation(NoReadBarrier.class,
            MethodPragmaFlags.NOREADBARRIER),
        new PragmaAnnotation(NoWriteBarrier.class,
            MethodPragmaFlags.NOWRITEBARRIER),
        new PragmaAnnotation(PrivilegedActionPragma.class,
            MethodPragmaFlags.PRIVILEGEDACTION),
        new PragmaAnnotation(Uninterruptible.class,
            MethodPragmaFlags.UNINTERRUPTIBLE),
        new PragmaAnnotation(KernelSpace.class,
            MethodPragmaFlags.KERNELSPACE)};

    /**
     * Names of classes that you use shared statics, but cannot be modified.
     */
    private static final String[] SHARED_STATICS_CLASSNAMES = {
        "java.util.TreeMap",
        "org.apache.log4j.LogManager",
    };

    private static final byte[] TYPE_SIZES = {1, 2, 4, 8};

    private static final Class<?>[] BOOT_TYPES = new Class[]{Class.class,
        String.class, Integer.class, Long.class};

    /**
     * Align the given value on the given alignment.
     *
     * @param value
     * @param alignment
     * @return the new value
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
     */
    private static final boolean isBootType(VmType<?> type) {
        final String typeName = type.getName();
        for (Class<?> c : BOOT_TYPES) {
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
            CodeAttrName = "Code".toCharArray();
            SourceFileAttrName = "SourceFile".toCharArray();
            SignatureAttrName = "Signature".toCharArray();
            ExceptionsAttrName = "Exceptions".toCharArray();
            LineNrTableAttrName = "LineNumberTable".toCharArray();
            LocalVariableTableAttrName = "LocalVariableTable".toCharArray();
            RuntimeVisibleAnnotationsAttrName = "RuntimeVisibleAnnotations"
                .toCharArray();
            RuntimeInvisibleAnnotationsAttrName = "RuntimeInvisibleAnnotations"
                .toCharArray();
            RuntimeVisibleParameterAnnotationsAttrName = "RuntimeVisibleParameterAnnotations"
                .toCharArray();
            RuntimeInvisibleParameterAnnotationsAttrName = "RuntimeInvisibleParameterAnnotations"
                .toCharArray();
        }
    }

    /**
     * Decode a given class.
     *
     * @param data
     * @param rejectNatives
     * @param clc
     * @param protectionDomain
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
                    break;
                }
                case 12:
                    // Name and Type
                {
                    final int nIdx = data.getChar();
                    final int dIdx = data.getChar();
                    cp.setInt(i, nIdx << 16 | dIdx);
                    break;
                }
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
        //int pragmaFlags = 0;
        if (isBootType(cls)) {
            cls.addPragmaFlags(TypePragmaFlags.NO_FIELD_ALIGNMENT);
        }
        cls.addPragmaFlags(getClassNamePragmaFlags(clsName));

        // Interface table
        cls.addPragmaFlags(readInterfaces(data, cls, cp));

        // Field table
        final FieldData[] fieldData = readFields(data, cp, slotSize);

        // Method Table
        readMethods(data, rejectNatives, cls, cp, sharedStatics, clc);

        // Read class attributes
        final int acount = data.getChar();
        VmAnnotation[] rVisAnn = null;
        VmAnnotation[] rInvisAnn = null;
        String sourceFile = null;
        String signature = null;
        for (int a = 0; a < acount; a++) {
            final String attrName = cp.getUTF8(data.getChar());
            final int length = data.getInt();
            if (VmArray.equals(RuntimeVisibleAnnotationsAttrName, attrName)) {
                rVisAnn = readRuntimeAnnotations(data, cp, true);
            } else if (VmArray.equals(RuntimeInvisibleAnnotationsAttrName,
                attrName)) {
                rInvisAnn = readRuntimeAnnotations(data, cp, false);
            } else if (VmArray.equals(SourceFileAttrName, attrName)) {
                sourceFile = cp.getUTF8(data.getChar());
            } else if (VmArray.equals(SignatureAttrName, attrName)) {
                signature = cp.getUTF8(data.getChar());
            } else {
                skip(data, length);
            }
        }
        cls.setRuntimeAnnotations(rVisAnn);
        cls.setSourceFile(sourceFile);
        cls.setSignature(signature);
        if (rInvisAnn != null) {
            cls.addPragmaFlags(getClassPragmaFlags(rInvisAnn, clsName));
        }
        if (rVisAnn != null) {
            cls.addPragmaFlags(getClassPragmaFlags(rVisAnn, clsName));
        }

        // Create the fields
        if (fieldData != null) {
            createFields(cls, fieldData, sharedStatics, isolatedStatics,
                slotSize, cls.getPragmaFlags());
        }

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
     * @param rejectNatives
     * @param clc
     * @param protectionDomain
     * @return The defined class
     */
    public static final VmType defineClass(String className, ByteBuffer data,
                                           boolean rejectNatives, VmClassLoader clc,
                                           ProtectionDomain protectionDomain) {
        cl_init();
        return decodeClass(data, rejectNatives, clc, protectionDomain);
    }

    /**
     * Gets the bytecode of a native code replacement method.
     *
     * @param method
     * @param cl
     * @return the bytecode
     */
    private static VmByteCode getNativeCodeReplacement(VmMethod method,
                                                       VmClassLoader cl, boolean verbose) {
        final String className = method.getDeclaringClass().getName();
        final String nativeClassName = VmUtils.getNativeClassName(className);
        final VmType nativeType;
        try {
            nativeType = cl.loadClass(nativeClassName, false);
        } catch (ClassNotFoundException ex) {
            if (verbose) {
                BootLog.error("Native class replacement (" + nativeClassName
                    + ") not found");
            }
            return null;
        }

        String signature = method.getSignature();
        if (!method.isStatic()) {
            signature = "(" + Signature.toSignature(method.getDeclaringClass()) + signature.substring(1);
        }

        final VmMethod nativeMethod = nativeType.getNativeMethodReplacement(method.getName(), signature);

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
     * @param data
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
        VmLocalVariableTable lvTable = VmLocalVariableTable.EMPTY;
        final int acount = data.getChar();
        for (int i = 0; i < acount; i++) {
            final String attrName = cp.getUTF8(data.getChar());
            final int len = data.getInt();
            if (VmArray.equals(LineNrTableAttrName, attrName)) {
                lnTable = readLineNrTable(data);
            } else if (VmArray.equals(LocalVariableTableAttrName, attrName)) {
                lvTable = readLocalVariableTable(data, cp);
            } else {
                skip(data, len);
            }
        }

        return new VmByteCode(method, code, noLocals, maxStack, etable,
            lnTable, lvTable);
    }

    /**
     * Decode the data of a Exceptions attribute
     *
     * @param data
     * @param cls
     * @param cp
     * @return The read exceptions
     */
    private static final VmExceptions readExceptions(ByteBuffer data,
                                                     VmType cls, VmCP cp) {

        // Read the exceptions
        char pragmaFlags = 0;
        int pragmas = 0;
        final int ecount = data.getChar();
        final VmConstClass[] list = new VmConstClass[ecount];
        for (int i = 0; i < ecount; i++) {
            final int idx = data.getChar();
            final VmConstClass ccls = cp.getConstClass(idx);
            list[i] = ccls;
            for (MethodPragmaException mp : METHOD_PRAGMA_EXCEPTIONS) {
                if (ccls.getClassName().equals(mp.className)) {
                    pragmaFlags |= mp.flags;
                    pragmas++;
                    list[i] = null;
                    break;
                }
            }
        }
        if (pragmas > 0) {
            final int newCnt = ecount - pragmas;
            if (newCnt == 0) {
                return new VmExceptions(null, pragmaFlags);
            } else {
                final VmConstClass[] newList = new VmConstClass[newCnt];
                int k = 0;
                for (int i = 0; i < ecount; i++) {
                    final VmConstClass ccls = list[i];
                    if (ccls != null) {
                        newList[k++] = ccls;
                    }
                }
                return new VmExceptions(newList, pragmaFlags);
            }
        } else {
            return new VmExceptions(list, pragmaFlags);
        }
    }

    /**
     * Read the fields table
     *
     * @param data
     * @param cp
     * @param slotSize
     * @param pragmaFlags
     */
    private static FieldData[] readFields(ByteBuffer data, VmCP cp,
                                          int slotSize) {
        final int fcount = data.getChar();
        if (fcount > 0) {
            final FieldData[] ftable = new FieldData[fcount];

            for (int i = 0; i < fcount; i++) {
                int modifiers = data.getChar();
                final String name = cp.getUTF8(data.getChar());
                final String signature = cp.getUTF8(data.getChar());
                final boolean isstatic = ((modifiers & Modifier.ACC_STATIC) != 0);

                // Read field attributes
                final int acount = data.getChar();
                VmAnnotation[] rVisAnn = null;
                Object constantValue = null;
                for (int a = 0; a < acount; a++) {
                    final String attrName = cp.getUTF8(data.getChar());
                    final int length = data.getInt();
                    if (isstatic
                        && VmArray.equals(ConstantValueAttrName, attrName)) {
                        constantValue = cp.getAny(data.getChar());
                    } else if (VmArray.equals(
                        RuntimeVisibleAnnotationsAttrName, attrName)) {
                        rVisAnn = readRuntimeAnnotations(data, cp, true);
                    } else if (VmArray.equals(
                        RuntimeInvisibleAnnotationsAttrName, attrName)) {
                        readRuntimeAnnotations(data, cp, false);
                    } else {
                        skip(data, length);
                    }
                }

                ftable[i] = new FieldData(name, signature, modifiers,
                    constantValue, rVisAnn);
            }
            return ftable;
        } else {
            return null;
        }
    }

    /**
     * Read the fields table
     *
     * @param cls
     * @param fieldDatas
     * @param sharedStatics
     * @param isolatedStatics
     * @param slotSize
     * @param pragmaFlags
     */
    private static void createFields(VmType<?> cls, FieldData[] fieldDatas,
                                     VmSharedStatics sharedStatics, VmIsolatedStatics isolatedStatics,
                                     int slotSize, int pragmaFlags) {
        final int fcount = fieldDatas.length;
        final VmField[] ftable = new VmField[fcount];

        int objectSize = 0;
        for (int i = 0; i < fcount; i++) {
            final FieldData fd = fieldDatas[i];
            final boolean wide;
            int modifiers = fd.modifiers;
            final String name = fd.name;
            final String signature = fd.signature;
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
                        break;
                    }
                }
                fs = new VmStaticField(name, signature, modifiers, staticsIdx,
                    cls, slotSize, shared);
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
            final VmAnnotation[] rVisAnn = fd.rVisAnn;
            if (isstatic && (fd.constantValue != null)) {
                switch (signature.charAt(0)) {
                    case 'B':
                    case 'C':
                    case 'I':
                    case 'S':
                    case 'Z':
                        statics.setInt(staticsIdx, ((VmConstInt) fd.constantValue)
                            .intValue());
                        break;
                    case 'D':
                        final long lval = Double
                            .doubleToRawLongBits(((VmConstDouble) fd.constantValue)
                                .doubleValue());
                        statics.setLong(staticsIdx, lval);
                        break;
                    case 'F':
                        final int ival = Float
                            .floatToRawIntBits(((VmConstFloat) fd.constantValue)
                                .floatValue());
                        statics.setInt(staticsIdx, ival);
                        break;
                    case 'J':
                        statics.setLong(staticsIdx,
                            ((VmConstLong) fd.constantValue).longValue());
                        break;
                    default:
                        // throw new IllegalArgumentException("signature "
                        // + signature);
                        statics.setObject(staticsIdx,
                            (VmConstString) fd.constantValue);
                        break;
                }
            }
            fs.setRuntimeAnnotations(rVisAnn);
        }

        // Align the instance fields for minimal object size.
        if ((pragmaFlags & TypePragmaFlags.NO_FIELD_ALIGNMENT) == 0) {
            objectSize = alignInstanceFields(ftable, slotSize);
        }

        cls.setFieldTable(ftable);
        if (objectSize > 0) {
            ((VmNormalClass<?>) cls).setObjectSize(objectSize);
        }
    }

    /**
     * Read the interfaces table
     *
     * @param data
     * @param cls
     * @param cp
     * @return Some flags
     */
    private static int readInterfaces(ByteBuffer data, VmType cls, VmCP cp) {
        int flags = 0;
        final int icount = data.getChar();
        if (icount > 0) {
            final VmImplementedInterface[] itable = new VmImplementedInterface[icount];
            for (int i = 0; i < icount; i++) {
                final VmConstClass icls = cp.getConstClass(data.getChar());
                final String iclsName = icls.getClassName();
                itable[i] = new VmImplementedInterface(iclsName);

                for (PragmaInterface pi : INTERFACE_PRAGMAS) {
                    if (iclsName.equals(pi.className)) {
                        flags |= pi.flags;
                    }
                }
            }
            cls.setInterfaceTable(itable);
        }
        return flags;
    }

    /**
     * Decode the data of a LineNumberTable-attribute
     *
     * @param data
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
     * Decode the data of a LocalVariable-attribute
     *
     * @param data
     * @param cp
     * @return The line number map
     */
    private static final VmLocalVariableTable readLocalVariableTable(
        ByteBuffer data, VmCP cp) {
        final int len = data.getChar();
        if (len == 0) {
            return VmLocalVariableTable.EMPTY;
        } else {
            final VmLocalVariable[] table = new VmLocalVariable[len];

            for (int i = 0; i < len; i++) {
                final char startPc = data.getChar();
                final char length = data.getChar();
                final char nameIdx = data.getChar();
                final char descrIdx = data.getChar();
                final char index = data.getChar();

                table[i] = new VmLocalVariable(startPc, length, nameIdx,
                    descrIdx, index);
            }

            return new VmLocalVariableTable(table);
        }
    }

    /**
     * Read the method table
     *
     * @param data
     * @param rejectNatives
     * @param cls
     * @param cp
     * @param statics
     * @param cl
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
                VmAnnotation[] rVisAnn = null;
                VmAnnotation[] rInvisAnn = null;
                for (int a = 0; a < acount; a++) {
                    String attrName = cp.getUTF8(data.getChar());
                    int length = data.getInt();
                    if (VmArray.equals(CodeAttrName, attrName)) {
                        mts.setBytecode(readCode(data, cls, cp, mts));
                    } else if (VmArray.equals(ExceptionsAttrName, attrName)) {
                        mts.setExceptions(readExceptions(data, cls, cp));
                    } else if (VmArray.equals(
                        RuntimeVisibleAnnotationsAttrName, attrName)) {
                        rVisAnn = readRuntimeAnnotations(data, cp, true);
                    } else if (VmArray.equals(
                        RuntimeInvisibleAnnotationsAttrName, attrName)) {
                        rInvisAnn = readRuntimeAnnotations(data, cp, false);
                    } else if (VmArray.equals(
                        RuntimeVisibleParameterAnnotationsAttrName,
                        attrName)) {
                        readRuntimeParameterAnnotations(data, cp, true);
                    } else if (VmArray.equals(
                        RuntimeInvisibleParameterAnnotationsAttrName,
                        attrName)) {
                        readRuntimeParameterAnnotations(data, cp, false);
                    } else {
                        skip(data, length);
                    }
                }
                mts.setRuntimeAnnotations(rVisAnn);
                if (rVisAnn != null) {
                    mts.addPragmaFlags(getMethodPragmaFlags(rVisAnn, cls
                        .getName()));
                }
                if (rInvisAnn != null) {
                    mts.addPragmaFlags(getMethodPragmaFlags(rInvisAnn, cls
                        .getName()));
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

    /**
     * Read a runtime parameter annotations attributes.
     *
     * @param data
     * @param cp
     */
    private static VmAnnotation[][] readRuntimeParameterAnnotations(
        ByteBuffer data, VmCP cp, boolean visible) {
        final int numParams = data.get();
        final VmAnnotation[][] arr = new VmAnnotation[numParams][];
        for (int i = 0; i < numParams; i++) {
            arr[i] = readRuntimeAnnotations(data, cp, visible);
        }
        return arr;
    }

    /**
     * Read a runtime annotations attributes.
     *
     * @param data
     * @param cp
     */
    private static VmAnnotation[] readRuntimeAnnotations(ByteBuffer data,
                                                         VmCP cp, boolean visible) {
        final int numAnn = data.getChar();
        final VmAnnotation[] arr = new VmAnnotation[numAnn];
        for (int i = 0; i < numAnn; i++) {
            arr[i] = readAnnotation(data, cp, visible);
        }
        return arr;
    }

    /**
     * Combine the pragma flags for a given list of annotations.
     *
     * @param annotations
     * @param className
     */
    private static int getMethodPragmaFlags(VmAnnotation[] annotations,
                                            String className) {
        int flags = 0;
        for (VmAnnotation a : annotations) {
            final String typeDescr = a.getTypeDescriptor();
            for (PragmaAnnotation ma : METHOD_ANNOTATIONS) {
                if (ma.typeDescr.equals(typeDescr)) {
                    ma.checkPragmaAllowed(className);
                    flags |= ma.flags;
                }
            }
        }
        return flags;
    }

    /**
     * Combine the pragma flags for a given list of annotations.
     *
     * @param annotations
     * @param className
     */
    private static int getClassPragmaFlags(VmAnnotation[] annotations,
                                           String className) {
        int flags = 0;
        for (VmAnnotation a : annotations) {
            final String typeDescr = a.getTypeDescriptor();
            for (PragmaAnnotation ma : CLASS_ANNOTATIONS) {
                if (ma.typeDescr.equals(typeDescr)) {
                    ma.checkPragmaAllowed(className);
                    flags |= ma.flags;
                }
            }
        }
        for (String name : SHARED_STATICS_CLASSNAMES) {
            if (className.equals(name)) {
                System.out.println("FOUND IT: " + className);
                flags |= TypePragmaFlags.SHAREDSTATICS;
                break;
            }
        }
        return flags;
    }

    /**
     * Combine the pragma flags for a given classname.
     *
     * @param className
     */
    private static int getClassNamePragmaFlags(String className) {
        int flags = 0;
        for (String name : SHARED_STATICS_CLASSNAMES) {
            if (className.equals(name)) {
                flags |= TypePragmaFlags.SHAREDSTATICS;
                break;
            }
        }
        return flags;
    }

    /**
     * Read a single annotation structure.
     *
     * @param data
     * @param cp
     */
    private static VmAnnotation readAnnotation(ByteBuffer data, VmCP cp,
                                               boolean visible) {
        final String typeDescr = cp.getUTF8(data.getChar());
        final int numElemValuePairs = data.getChar();
        final VmAnnotation.ElementValue[] values;
        if (numElemValuePairs == 0) {
            values = VmAnnotation.ElementValue.EMPTY_ARR;
        } else if (visible) {
            values = new VmAnnotation.ElementValue[numElemValuePairs];
            for (int i = 0; i < numElemValuePairs; i++) {
                final String elemName = cp.getUTF8(data.getChar());
                final Object value = readElementValue(data, cp);
                values[i] = new VmAnnotation.ElementValue(elemName, value);
            }
        } else {
            values = VmAnnotation.ElementValue.EMPTY_ARR;
            for (int i = 0; i < numElemValuePairs; i++) {
                data.getChar(); // Skip name ref
                skipElementValue(data, cp);
            }
        }
        return new VmAnnotation(typeDescr, values);
    }

    /**
     * Read a single element_value structure.
     *
     * @param data
     * @param cp
     */
    private static Object readElementValue(ByteBuffer data, VmCP cp) {
        final int tag = data.get() & 0xFF;
        switch (tag) {
            case 'B':
                return Byte.valueOf((byte) cp.getInt(data.getChar()));
            case 'C':
                return Character.valueOf((char) cp.getInt(data.getChar()));
            case 'D':
                return cp.getDouble(data.getChar());
            case 'F':
                return cp.getFloat(data.getChar());
            case 'I':
                return cp.getInt(data.getChar());
            case 'J':
                return cp.getLong(data.getChar());
            case 'S':
                return Short.valueOf((short) cp.getInt(data.getChar()));
            case 'Z':
                return Boolean.valueOf(cp.getInt(data.getChar()) != 0);
            case 's':
                return cp.getAny(data.getChar());
            case 'e': // enum
            {
                final String typeDescr = cp.getUTF8(data.getChar());
                final String constName = cp.getUTF8(data.getChar());
                return new VmAnnotation.EnumValue(typeDescr, constName);
            }
            case 'c': // class
            {
                final String classDescr = cp.getUTF8(data.getChar());
                return new VmAnnotation.ClassInfo(classDescr);
            }
            case '@': // annotation
                return readAnnotation(data, cp, true);
            case '[': // array
            {
                final int numValues = data.getChar();
                final Object[] arr = new Object[numValues];
                for (int i = 0; i < numValues; i++) {
                    arr[i] = readElementValue(data, cp);
                }
                return arr;
            }
            default:
                throw new ClassFormatError("Unknown element_value tag '"
                    + (char) tag + "'");
        }
    }

    /**
     * Skip over a single element_value structure.
     *
     * @param data
     * @param cp
     */
    private static void skipElementValue(ByteBuffer data, VmCP cp) {
        final int tag = data.get() & 0xFF;
        switch (tag) {
            case 'B':
            case 'C':
            case 'D':
            case 'F':
            case 'I':
            case 'J':
            case 'S':
            case 'Z':
            case 's':
                data.getChar();
                break;
            case 'e': // enum
                data.getChar(); // typedescr
                data.getChar(); // constname
                break;
            case 'c': // class
                data.getChar(); // classdescr
                break;
            case '@': // annotation
                readAnnotation(data, cp, false);
                break;
            case '[': // array
            {
                final int numValues = data.getChar();
                for (int i = 0; i < numValues; i++) {
                    skipElementValue(data, cp);
                }
                break;
            }
            default:
                throw new ClassFormatError("Unknown element_value tag '"
                    + (char) tag + "'");
        }
    }

    private static final void skip(ByteBuffer data, int delta) {
        data.position(data.position() + delta);
    }

    private static final ByteBuffer readBytes(ByteBuffer data, int length) {
        final ByteBuffer result = (ByteBuffer) data.slice().limit(length);
        data.position(data.position() + length);
        return result;
    }

    private static final String readUTF(ByteBuffer data) {
        final int utflen = data.getChar();
        final String result;
        try {
            result = VmUTF8Convert.fromUTF8(data,
                getUtfConversionBuffer(utflen), utflen);
        } catch (UTFDataFormatException ex) {
            throw (ClassFormatError) new ClassFormatError(
                "Invalid UTF sequence").initCause(ex);
        }
        return result;
    }

    private static transient ThreadLocal utfConversionBuffer;

    private static final char[] getUtfConversionBuffer(int utfLength) {
        if (utfConversionBuffer == null) {
            synchronized (ClassDecoder.class) {
                if (utfConversionBuffer == null) {
                    utfConversionBuffer = new ThreadLocal();
                }
            }
        }
        char[] buffer = (char[]) utfConversionBuffer.get();
        if ((buffer == null) || (buffer.length < utfLength)) {
            buffer = new char[Math.max(64, utfLength)];
            utfConversionBuffer.set(buffer);
        }

        return buffer;
    }

    private static final class MethodPragmaException {
        public final char flags;

        public final String className;

        public MethodPragmaException(Class<? extends PragmaException> cls,
                                     char flags) {
            this.className = cls.getName();
            this.flags = flags;
        }
    }

    private static final class PragmaInterface {
        public final char flags;

        public final String className;

        public PragmaInterface(Class<?> cls, char flags) {
            this.className = cls.getName();
            this.flags = flags;
        }
    }

    private static final class PragmaAnnotation {
        public final char flags;

        public final String typeDescr;

        private final String[] allowedPackages;

        public PragmaAnnotation(Class<? extends Annotation> cls, char flags) {
            this.typeDescr = "L" + cls.getName().replace('.', '/') + ";";
            this.flags = flags;
            final AllowedPackages ann = cls
                .getAnnotation(AllowedPackages.class);
            if (ann != null) {
                allowedPackages = ann.value();
            } else {
                allowedPackages = null;
            }
        }

        /**
         * Is this annotation allowed for the given classname.
         */
        public final void checkPragmaAllowed(String className) {
            if (allowedPackages != null) {
                final String pkg = className.substring(0, className
                    .lastIndexOf('.'));
                for (String allowedPkg : allowedPackages) {
                    if (pkg.equals(allowedPkg)) {
                        return;
                    }
                }
                throw new SecurityException("Pragma " + typeDescr
                    + " is not allowed in class " + className);
            }
        }
    }

    private static final class FieldData {
        public final String name;

        public final String signature;

        public final int modifiers;

        public final Object constantValue;

        public final VmAnnotation[] rVisAnn;

        /**
         * @param name
         * @param signature
         * @param modifiers
         * @param value
         * @param rVisAnn
         */
        public FieldData(String name, String signature, int modifiers,
                         Object value, VmAnnotation[] rVisAnn) {
            this.name = name;
            this.signature = signature;
            this.modifiers = modifiers;
            this.constantValue = value;
            this.rVisAnn = rVisAnn;
        }

    }
}
