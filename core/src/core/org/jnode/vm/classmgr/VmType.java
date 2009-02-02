/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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

import gnu.java.lang.VMClassHelper;

import java.io.Serializable;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;

import org.jnode.assembler.NativeStream;
import org.jnode.vm.JvmType;
import org.jnode.vm.LoadCompileService;
import org.jnode.vm.Unsafe;
import org.jnode.vm.Vm;
import org.jnode.vm.VmMagic;
import org.jnode.vm.VmReflection;
import org.jnode.vm.VmSystemClassLoader;
import org.jnode.vm.annotation.Inline;
import org.jnode.vm.annotation.KernelSpace;
import org.jnode.vm.annotation.LoadStatics;
import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.annotation.NoInline;
import org.jnode.vm.annotation.SharedStatics;
import org.jnode.vm.annotation.Uninterruptible;
import org.jnode.vm.compiler.CompileError;
import org.jnode.vm.compiler.CompiledIMT;
import org.jnode.vm.compiler.NativeCodeCompiler;
import org.jnode.vm.isolate.VmIsolateLocal;
import org.jnode.security.JNodePermission;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.ObjectReference;

@SharedStatics
@Uninterruptible
@MagicPermission
public abstract class VmType<T> extends VmAnnotatedElement implements
    VmSharedStaticsEntry, VmIsolatedStaticsEntry {

    /**
     * The parent of this class. Normally VmClass instance, during loading
     * String instance
     */
    private VmNormalClass<? super T> superClass;

    /**
     * The classname of the parent of this class.
     */
    private final String superClassName;

    /**
     * The number of super classes until (and including) Object
     */
    private int superClassDepth;

    /**
     * The name of this class
     */
    private String name;

    /**
     * The the source file name of this class
     */
    private String sourceFile;

    /**
     * The the source file name of this class
     */
    private String signature;

    /**
     * All methods and constructors declared in this class
     */
    private VmMethod[] methodTable;

    /**
     * All fields declared in this class
     */
    private VmField[] fieldTable;

    /**
     * My modifiers
     */
    private int modifiers = -1;

    /**
     * Pragma flags, see {@link TypePragmaFlags}
     */
    private char pragmaFlags;

    /**
     * State of this type see {@link VmTypeState}.
     */
    private char state;

    /**
     * The constant pool
     */
    private VmCP cp;

    /**
     * Interface implemented by this class
     */
    private VmImplementedInterface[] interfaceTable;

    /**
     * All interface implemented by this class and its superclasses
     */
    private VmInterfaceClass[] allInterfaceTable;

    /**
     * Loaded of this class
     */
    private final VmClassLoader loader;

    /**
     * Holder for isolate specific Class instance
     */
    private VmIsolateLocal<Class<T>> javaClassHolder;

    /**
     * Have the references in the constant pool been resolved?
     */
    private boolean resolvedCpRefs;

    /**
     * Size of instances of this class in bytes (1..8)
     */
    private final byte typeSize;

    /**
     * Name of the array class with this class as component type
     */
    private String arrayClassName;

    /**
     * The array class with this class as component type
     */
    private VmArrayClass<T[]> arrayClass;

    /**
     * Array containing all super classes and all implemented interfaces
     */
    private VmType[] superClassesArray;

    /**
     * The finalize method of this class
     */
    private VmMethod finalizeMethod;

    /**
     * The mangled name (cache)
     */
    private String mangledName;

    /**
     * Error message during linkage
     */
    private String errorMsg;

    /**
     * Index of this type in the statics index
     */
    private final int staticsIndex;

    /**
     * Index of this type in the isolated statics index
     */
    private final int isolatedStaticsIndex;

    /**
     * The protection domain of this class
     */
    private final ProtectionDomain protectionDomain;

    /**
     * Type information managed and required by the memory manager
     */
    private Object mmType;

    private static VmNormalClass<Object> ObjectClass;

    private static VmInterfaceClass<Cloneable> CloneableClass;

    private static VmInterfaceClass<Serializable> SerializableClass;

    private static VmNormalClass BooleanClass;

    private static VmNormalClass ByteClass;

    private static VmNormalClass CharClass;

    private static VmNormalClass ShortClass;

    private static VmNormalClass IntClass;

    private static VmNormalClass FloatClass;

    private static VmNormalClass LongClass;

    private static VmNormalClass DoubleClass;

    private static VmNormalClass VoidClass;

    private static VmNormalClass ClassClass;

    private static VmArrayClass<boolean[]> BooleanArrayClass;

    private static VmArrayClass<byte[]> ByteArrayClass;

    private static VmArrayClass<char[]> CharArrayClass;

    private static VmArrayClass<short[]> ShortArrayClass;

    private static VmArrayClass<int[]> IntArrayClass;

    private static VmArrayClass<float[]> FloatArrayClass;

    private static VmArrayClass<long[]> LongArrayClass;

    private static VmArrayClass<double[]> DoubleArrayClass;

    private static VmArrayClass<Object[]> ObjectArrayClass;

    /**
     * Construct a new VmClass
     *
     * @param name
     * @param superClassName
     * @param loader
     * @param accessFlags
     */
    protected VmType(String name, String superClassName, VmClassLoader loader,
                     int accessFlags, ProtectionDomain protectionDomain) {
        this(name, null, superClassName, loader, accessFlags, -1,
            protectionDomain);
    }

    /**
     * Construct a new VmClass with a given name and superclass
     *
     * @param name
     * @param superClass
     * @param loader
     * @param typeSize
     */
    VmType(String name, VmNormalClass<? super T> superClass,
           VmClassLoader loader, int typeSize,
           ProtectionDomain protectionDomain) {
        this(name, superClass, superClass.getName(), loader,
            Modifier.ACC_PUBLIC, typeSize, protectionDomain);
    }

    /**
     * Construct a new VmClass with a given name and superclass
     *
     * @param name
     * @param superClass
     * @param superClassName
     * @param loader
     * @param modifiers
     * @param typeSize
     * @param protectionDomain the protection domain of this type.
     */
    private VmType(String name, VmNormalClass<? super T> superClass,
                   String superClassName, VmClassLoader loader, int modifiers,
                   int typeSize, ProtectionDomain protectionDomain) {
        if (superClassName == null) {
            if (!name.equals("java.lang.Object")) {
                throw new IllegalArgumentException(
                    "superClassName cannot be null in class " + name);
            }
        } else if (superClassName.indexOf('/') >= 0) {
            throw new IllegalArgumentException("superClassName contains '/'");
        }
        if (name.indexOf('/') >= 0) {
            throw new IllegalArgumentException("name contains '/'");
        }

        final String pkg = VMClassHelper.getPackagePortion(name);
        if (pkg.equals("org.vmmagic.unboxed") || pkg.equals("org.jnode.vm")) {
            final String cname = VMClassHelper.getClassNamePortion(name);
            if (cname.equals("Address") || cname.equals("AddressArray")
                || cname.equals("Extent") || cname.equals("ExtentArray")
                || cname.equals("ObjectReference")
                || cname.equals("ObjectReferenceArray")
                || cname.equals("Offset") || cname.equals("OffsetArray")
                || cname.equals("Word") || cname.equals("WordArray")
                | cname.equals("VmMagic")) {
                modifiers |= Modifier.ACC_MAGIC;
            }
        }

        this.name = name.intern();
        this.superClass = superClass;
        this.superClassName = (superClassName == null) ? null : superClassName
            .intern();
        this.modifiers = modifiers;
        this.state = VmTypeState.ST_LOADED;
        this.loader = loader;
        this.protectionDomain = protectionDomain;
        this.staticsIndex = loader.getSharedStatics().allocClass(this);
        this.isolatedStaticsIndex = loader.getIsolatedStatics().allocIntField();
        if (name.charAt(0) == '[') {
            this.interfaceTable = new VmImplementedInterface[]{
                new VmImplementedInterface(CloneableClass),
                new VmImplementedInterface(SerializableClass)};
            this.typeSize = (byte) loader.getArchitecture().getReferenceSize();
        } else if (typeSize >= 0) {
            this.typeSize = (byte) typeSize;
        } else {
            this.typeSize = (byte) loader.getArchitecture().getReferenceSize();
        }

    }

    /**
     * Load the system classes during our bootstrap process.
     *
     * @param clc
     * @return VmClass[]
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    public static VmType[] initializeForBootImage(VmSystemClassLoader clc)
        throws ClassNotFoundException {
        ObjectClass = (VmNormalClass) clc.loadClass("java.lang.Object", false);
        ClassClass = (VmNormalClass) clc.loadClass("java.lang.Class", false);
        CloneableClass = (VmInterfaceClass) clc.loadClass(
            "java.lang.Cloneable", false);
        SerializableClass = (VmInterfaceClass) clc.loadClass(
            "java.io.Serializable", false);

        ObjectClass.link();
        ClassClass.link();
        CloneableClass.link();
        SerializableClass.link();

        final ProtectionDomain protectionDomain = null;
        BooleanClass = new VmPrimitiveClass("boolean", ObjectClass, clc,
            JvmType.BOOLEAN, 1, false, protectionDomain);
        ByteClass = new VmPrimitiveClass("byte", ObjectClass, clc,
            JvmType.BYTE, 1, false, protectionDomain);
        CharClass = new VmPrimitiveClass("char", ObjectClass, clc,
            JvmType.CHAR, 2, false, protectionDomain);
        ShortClass = new VmPrimitiveClass("short", ObjectClass, clc,
            JvmType.SHORT, 2, false, protectionDomain);
        IntClass = new VmPrimitiveClass("int", ObjectClass, clc, JvmType.INT,
            4, false, protectionDomain);
        FloatClass = new VmPrimitiveClass("float", ObjectClass, clc,
            JvmType.FLOAT, 4, true, protectionDomain);
        LongClass = new VmPrimitiveClass("long", ObjectClass, clc,
            JvmType.LONG, 8, false, protectionDomain);
        DoubleClass = new VmPrimitiveClass("double", ObjectClass, clc,
            JvmType.DOUBLE, 8, true, protectionDomain);
        VoidClass = new VmPrimitiveClass("void", ObjectClass, clc,
            JvmType.VOID, 0, false, protectionDomain);

        BooleanClass.link();
        ByteClass.link();
        CharClass.link();
        ShortClass.link();
        IntClass.link();
        FloatClass.link();
        LongClass.link();
        DoubleClass.link();
        VoidClass.link();

        BooleanArrayClass = BooleanClass.getArrayClass("[Z");
        ByteArrayClass = ByteClass.getArrayClass("[B");
        CharArrayClass = CharClass.getArrayClass("[C");
        ShortArrayClass = ShortClass.getArrayClass("[S");
        IntArrayClass = IntClass.getArrayClass("[I");
        FloatArrayClass = FloatClass.getArrayClass("[F");
        LongArrayClass = LongClass.getArrayClass("[J");
        DoubleArrayClass = DoubleClass.getArrayClass("[D");
        ObjectArrayClass = ObjectClass.getArrayClass();

        BooleanArrayClass.link();
        ByteArrayClass.link();
        CharArrayClass.link();
        ShortArrayClass.link();
        IntArrayClass.link();
        FloatArrayClass.link();
        LongArrayClass.link();
        DoubleArrayClass.link();
        ObjectArrayClass.link();

        return new VmType[]{ObjectClass, ClassClass, CloneableClass, SerializableClass,
            BooleanClass, ByteClass, CharClass, ShortClass, IntClass,
            FloatClass, LongClass, DoubleClass, VoidClass,
            BooleanArrayClass, ByteArrayClass, CharArrayClass,
            ShortArrayClass, IntArrayClass, FloatArrayClass,
            LongArrayClass, DoubleArrayClass, ObjectArrayClass};
    }

    /**
     * Load the system classes from an array of system classes. This method is
     * called during the boot of the system.
     *
     * @param bootClasses
     */
    @SuppressWarnings("unchecked")
    @LoadStatics
    protected static void loadFromBootClassArray(VmType[] bootClasses) {
        Unsafe.debug("[loadFromBootClassArray:");
        final int count = bootClasses.length;
        for (int i = 0; i < count; i++) {
            final VmType<?> vmClass = bootClasses[i];
            final String name = vmClass.name;
            if (vmClass.isPrimitive()) {
                if (name.equals("boolean")) {
                    BooleanClass = (VmNormalClass) vmClass;
                } else if (name.equals("byte")) {
                    ByteClass = (VmNormalClass) vmClass;
                } else if (name.equals("char")) {
                    CharClass = (VmNormalClass) vmClass;
                } else if (name.equals("short")) {
                    ShortClass = (VmNormalClass) vmClass;
                } else if (name.equals("int")) {
                    IntClass = (VmNormalClass) vmClass;
                } else if (name.equals("float")) {
                    FloatClass = (VmNormalClass) vmClass;
                } else if (name.equals("long")) {
                    LongClass = (VmNormalClass) vmClass;
                } else if (name.equals("double")) {
                    DoubleClass = (VmNormalClass) vmClass;
                } else if (name.equals("void")) {
                    VoidClass = (VmNormalClass) vmClass;
                }
            } else if (vmClass.isArray()) {
                if (name.equals("[Z")) {
                    BooleanArrayClass = (VmArrayClass) vmClass;
                } else if (name.equals("[B")) {
                    ByteArrayClass = (VmArrayClass) vmClass;
                } else if (name.equals("[C")) {
                    CharArrayClass = (VmArrayClass) vmClass;
                } else if (name.equals("[S")) {
                    ShortArrayClass = (VmArrayClass) vmClass;
                } else if (name.equals("[I")) {
                    IntArrayClass = (VmArrayClass) vmClass;
                } else if (name.equals("[F")) {
                    FloatArrayClass = (VmArrayClass) vmClass;
                } else if (name.equals("[J")) {
                    LongArrayClass = (VmArrayClass) vmClass;
                } else if (name.equals("[D")) {
                    DoubleArrayClass = (VmArrayClass) vmClass;
                }
            } else {
                if (name.equals("java.lang.Object")) {
                    ObjectClass = (VmNormalClass) vmClass;
                } else if (name.equals("java.lang.Class")) {
                    ClassClass = (VmNormalClass) vmClass;
                } else if (name.equals("java.lang.Cloneable")) {
                    CloneableClass = (VmInterfaceClass) vmClass;
                } else if (name.equals("java.io.Serializable")) {
                    SerializableClass = (VmInterfaceClass) vmClass;
                }
            }
        }
        Unsafe.debug("]\n");
    }

    /**
     * Create an array classname with a this class as component type.
     *
     * @return char[]
     */
    public final String getArrayClassName() {
        if (arrayClassName == null) {
            char[] result;
            int count = name.length();
            if (isPrimitive() || isArray()) {
                result = new char[count + 1];
                result[0] = '[';
                for (int i = 0; i < count; i++) {
                    result[i + 1] = name.charAt(i);
                }
            } else {
                result = new char[count + 3];
                result[0] = '[';
                result[1] = 'L';
                result[count + 2] = ';';
                for (int i = 0; i < count; i++) {
                    result[i + 2] = name.charAt(i);
                }
            }
            arrayClassName = new String(result).intern();
        }
        return arrayClassName;
    }

    /**
     * Gets the array class with this class as its component type
     *
     * @param arrayClassName
     * @return The array class
     */
    final VmArrayClass<T[]> getArrayClass(String arrayClassName) {
        if (arrayClass == null) {
            arrayClass = createArrayClass(true, arrayClassName);
        } else {
            arrayClass.link();
        }
        return arrayClass;
    }

    /**
     * Gets the array class with this class as its component type
     *
     * @return The array class
     */
    public final VmArrayClass getArrayClass() {
        return getArrayClass(null);
    }

    /**
     * Gets the array class with this class as its component type
     *
     * @param link
     * @return The array class
     */
    final VmArrayClass getArrayClass(boolean link) {
        if (arrayClass == null) {
            arrayClass = createArrayClass(link, null);
        }
        return arrayClass;
    }

    /**
     * Create an array class with a given component type.
     *
     * @param link           True if the new class should be linked, false otherwise
     * @param arrayClassName
     * @return VmClass
     */
    private final VmArrayClass<T[]> createArrayClass(boolean link,
                                                     String arrayClassName) {
        final String name;
        if (arrayClassName != null) {
            name = arrayClassName;
        } else {
            name = getArrayClassName();
        }
        final VmArrayClass<T[]> arrayClass = new VmArrayClass<T[]>(name, this
            .getLoader(), this, -1, protectionDomain);
        if (link) {
            arrayClass.link();
        }
        return arrayClass;
    }

    /**
     * Gets the VmClass of java.lang.Object.
     *
     * @return The class
     */
    public static VmNormalClass<Object> getObjectClass() {
        if (ObjectClass == null) {
            Unsafe.die("ObjectClass == null");
        }
        return ObjectClass;
    }

    /**
     * Gets the primitive class corresponding to the given signature type.
     *
     * @param type
     * @return VmClass
     */
    public static VmNormalClass<?> getPrimitiveClass(char type) {
        switch (type) {
            case 'Z':
                return BooleanClass;
            case 'B':
                return ByteClass;
            case 'C':
                return CharClass;
            case 'S':
                return ShortClass;
            case 'I':
                return IntClass;
            case 'F':
                return FloatClass;
            case 'J':
                return LongClass;
            case 'D':
                return DoubleClass;
            case 'V':
                return VoidClass;
            default:
                throw new IllegalArgumentException("Unknown type " + type);
        }
    }

    /**
     * Gets the primitive arrayclass corresponding to the given signature type.
     *
     * @param type
     * @return VmClass
     */
    public static VmType getPrimitiveArrayClass(char type) {
        switch (type) {
            case 'Z':
                return BooleanArrayClass;
            case 'B':
                return ByteArrayClass;
            case 'C':
                return CharArrayClass;
            case 'S':
                return ShortArrayClass;
            case 'I':
                return IntArrayClass;
            case 'F':
                return FloatArrayClass;
            case 'J':
                return LongArrayClass;
            case 'D':
                return DoubleArrayClass;
            default:
                throw new IllegalArgumentException("Unknown type " + type);
        }
    }

    /**
     * Gets the primitive array class corresponding to the given (newarray)
     * type.
     *
     * @param type
     * @return VmClass
     */
    public static VmArrayClass getPrimitiveArrayClass(int type) {
        switch (type) {
            case 4:
                return BooleanArrayClass;
            case 8:
                return ByteArrayClass;
            case 5:
                return CharArrayClass;
            case 9:
                return ShortArrayClass;
            case 10:
                return IntArrayClass;
            case 6:
                return FloatArrayClass;
            case 11:
                return LongArrayClass;
            case 7:
                return DoubleArrayClass;
            default:
                throw new IllegalArgumentException("Unknown type " + type);
        }
    }

    /**
     * Gets the size in bytes of instances of this object on the stack.
     *
     * @return 0..8
     */
    public final int getTypeSize() {
        return typeSize;
    }

    /**
     * Return the corresponding java.lang.Class for this VmClass. During build
     * environment the Class will be loaded by Class.forName
     *
     * @return The class
     */
    public final Class<T> asClass() {
        return asClass(false);
    }

    /**
     * Return the corresponding java.lang.Class for this VmClass. During build
     * environment the Class will be loaded by Class.forName
     *
     * @return The class
     */
    public final Class<T> asClassDuringBootstrap() {
        return asClass(true);
    }

    /**
     * Return the corresponding java.lang.Class for this VmClass. During build
     * environment the Class will be loaded by Class.forName
     *
     * @param isBuildEnv
     * @return The class
     */
    @SuppressWarnings("unchecked")
    private final Class<T> asClass(boolean isBuildEnv) {
        if (javaClassHolder == null) {
            javaClassHolder = new VmIsolateLocal<Class<T>>();
        }

        final Class<T> javaClass = javaClassHolder.get();
        if (javaClass == null) {
            if (isBuildEnv) {
                try {
                    javaClassHolder.set((Class<T>) Class.forName(getName()));
                } catch (ClassNotFoundException ex) { /* ignore */
                    throw new NoClassDefFoundError(getName());
                }
            } else {
//                javaClassHolder.set(new Class(this));
                javaClassHolder.set(newClass());
            }
            return javaClassHolder.get();
        } else {
            return javaClass;
        }
    }

    /**
     * Return the super class of this class or return null for java.lang.Object
     *
     * @return The class
     */
    public final VmNormalClass<? super T> getSuperClass() {
        if (superClass != null) {
            return superClass;
        } else if (superClassName == null) {
            // java.lang.Object
            return null;
        } else {
            throw new RuntimeException(
                "getSuperClass called too early in class " + name);
        }
    }

    /**
     * Return the name of this class
     *
     * @return The name of this class
     */
    @KernelSpace
    public final String getName() {
        return name;
    }

    /**
     * Convert myself into a String representation
     *
     * @return The mangled name
     */
    public final String getMangledName() {
        if (mangledName == null) {
            mangledName = mangleClassName(name);
        }
        return mangledName;
    }

    /**
     * Returns the second character of the name of this class
     *
     * @return char
     */
    public char getSecondNameChar() {
        return name.charAt(1);
    }

    /**
     * Is my name equal to the given array of characters?
     *
     * @param otherName
     * @return boolean
     */
    public boolean nameEquals(char[] otherName) {
        return name.equals(otherName);
    }

    /**
     * Is my name equal to the given String?
     *
     * @param otherName
     * @return boolean
     */
    public boolean nameEquals(String otherName) {
        return name.equals(otherName);
    }

    /**
     * Is my name equal to the given array of characters?
     *
     * @param classRef
     * @return boolean
     */
    public boolean nameEquals(VmConstClass classRef) {
        return name.equals(classRef.getClassName());
    }

    public final String toString() {
        return "_CL_" + mangleClassName(getName());
    }

    /**
     * Return the number of fields declared in this class
     *
     * @return int
     */
    public final int getNoDeclaredFields() {
        return (fieldTable == null) ? 0 : fieldTable.length;
    }

    /**
     * Return the declared field with a given index (0..getNoFields()-1)
     *
     * @param index
     * @return The field
     */
    public final VmField getDeclaredField(int index) {
        return fieldTable[index];
    }

    /**
     * Determines if the class or interface represented by this Class object is
     * either the same as, or is a superclass or superinterface of, the class or
     * interface represented by the specified Class parameter. It returns true
     * if so; otherwise it returns false. If this Class object represents a
     * primitive type, this method returns true if the specified Class parameter
     * is exactly this Class object; otherwise it returns false. Specifically,
     * this method tests whether the type represented by the specified Class
     * parameter can be converted to the type represented by this Class object
     * via an identity conversion or via a widening reference conversion. See
     * The Java Language Specification, sections 5.1.1 and 5.1.4, for details.
     *
     * @param S
     * @return boolean
     */
    public boolean isAssignableFrom(VmType S) {

        final VmType[] superClassesArray = S.superClassesArray;
        final int length = superClassesArray.length;
        for (int i = 0; i < length; i++) {
            if (superClassesArray[i] == this) {
                return true;
            }
        }
        return false;
    }

    /**
     * Is this type public.
     *
     * @return boolean
     */
    public final boolean isPublic() {
        return Modifier.isPublic(modifiers);
    }

    /**
     * Is this type public.
     *
     * @return boolean
     */
    public final boolean isProtected() {
        return Modifier.isProtected(modifiers);
    }

    /**
     * Is this type public.
     *
     * @return boolean
     */
    public final boolean isPrivate() {
        return Modifier.isPrivate(modifiers);
    }

    /**
     * Is this type public.
     *
     * @return boolean
     */
    public final boolean isStatic() {
        return Modifier.isStatic(modifiers);
    }

    /**
     * Does this type have shared statics.
     *
     * @return boolean
     */
    public final boolean isSharedStatics() {
        return ((pragmaFlags & TypePragmaFlags.SHAREDSTATICS) != 0);
    }

    /**
     * Does this type have magic permission.
     *
     * @return boolean
     */
    public final boolean isMagicPermissionGranted() {
        return ((pragmaFlags & TypePragmaFlags.MAGIC_PERMISSION) != 0);
    }

    /**
     * Is this type public.
     *
     * @return boolean
     */
    public final boolean isFinal() {
        return Modifier.isFinal(modifiers);
    }

    /**
     * Is this type an enum class.
     *
     * @return boolean
     */
    public final boolean isEnum() {
        return Modifier.isEnum(modifiers);
    }

    /**
     * Is this type an annotation class.
     *
     * @return boolean
     */
    public final boolean isAnnotation() {
        return Modifier.isAnnotation(modifiers);
    }

    /**
     * Is this type a synthetic class.
     *
     * @return boolean
     */
    public final boolean isSynthetic() {
        return Modifier.isSynthetic(modifiers);
    }

    /**
     * Is this type an interface.
     *
     * @return boolean
     */
    public final boolean isInterface() {
        return Modifier.isInterface(modifiers);
    }

    /**
     * Is this type abstract.
     *
     * @return boolean
     */
    public final boolean isAbstract() {
        return Modifier.isAbstract(modifiers);
    }

    /**
     * Is this type a magic type.
     *
     * @return boolean
     */
    public final boolean isMagicType() {
        return Modifier.isMagic(modifiers);
    }

    /**
     * Is this type loaded.
     *
     * @return boolean
     */
    @Inline
    final boolean isLoaded() {
        return ((state & VmTypeState.ST_LOADED) != 0);
    }

    /**
     * Is this type invalid.
     *
     * @return boolean
     */
    @Inline
    public final boolean isInvalid() {
        return ((state & VmTypeState.ST_INVALID) != 0);
    }

    /**
     * Is this type verifying.
     *
     * @return boolean
     */
    public final boolean isVerifying() {
        return ((state & VmTypeState.ST_VERIFYING) != 0);
    }

    /**
     * Is this type verified.
     *
     * @return boolean
     */
    @Inline
    public final boolean isVerified() {
        return ((state & VmTypeState.ST_VERIFIED) != 0);
    }

    /**
     * Is this type preparing.
     *
     * @return boolean
     */
    private final boolean isPreparing() {
        return ((state & VmTypeState.ST_PREPARING) != 0);
    }

    /**
     * Is this type prepared.
     *
     * @return boolean
     */
    @Inline
    final boolean isPrepared() {
        return ((state & VmTypeState.ST_PREPARED) != 0);
    }

    /**
     * Is this type compiling.
     *
     * @return boolean
     */
    final boolean isCompiling() {
        return ((state & VmTypeState.ST_COMPILING) != 0);
    }

    /**
     * Is this type compiled.
     *
     * @return boolean
     */
    @Inline
    public final boolean isCompiled() {
        return ((state & VmTypeState.ST_COMPILED) != 0);
    }

    /**
     * Gets the isolated state of this type.
     */
    @Inline
    private final int getIsolatedState() {
        if (VmMagic.isRunningJNode()) {
            return VmMagic.getIsolatedStaticFieldAddress(isolatedStaticsIndex)
                .loadInt();
        } else {
            return loader.getIsolatedStatics().getInt(isolatedStaticsIndex);
        }
    }

    /**
     * Add a bit to the isolated state of this type.
     */
    @Inline
    private final void addIsolatedState(int value) {
        if (VmMagic.isRunningJNode()) {
            final Address ptr = VmMagic
                .getIsolatedStaticFieldAddress(isolatedStaticsIndex);
            ptr.store(ptr.loadInt() | value);
        } else {
            final VmIsolatedStatics statics;
            final int index = isolatedStaticsIndex;
            statics = loader.getIsolatedStatics();
            statics.setInt(index, statics.getInt(index) | value);
        }
    }

    /**
     * Is this type initializing.
     *
     * @return boolean
     */
    @Inline
    final boolean isInitializing() {
        if (isSharedStatics()) {
            return ((state & VmTypeState.SST_INITIALIZING) != 0);
        } else {
            return ((getIsolatedState() & VmTypeState.IST_INITIALIZING) != 0);
        }
    }

    /**
     * Is this type initialized.
     *
     * @return boolean
     */
    @Inline
    public final boolean isInitialized() {
        return ((state & (VmTypeState.ST_ALWAYS_INITIALIZED | VmTypeState.SST_INITIALIZED)) != 0)
            || ((getIsolatedState() & VmTypeState.IST_INITIALIZED) != 0);
    }

    /**
     * Is this type always initialized in every isolate.
     *
     * @return boolean
     */
    @Inline
    public final boolean isAlwaysInitialized() {
        return ((state & (VmTypeState.ST_ALWAYS_INITIALIZED)) != 0);
    }

    /**
     * Mark this type always initialized.
     * Can only be called during bootstrapping.
     */
    public final void setAlwaysInitialized() {
        Vm._assert(!Vm.isRunningVm());
        state |= VmTypeState.ST_ALWAYS_INITIALIZED;
    }

    /**
     * Is this type linked.
     *
     * @return boolean
     */
    @Inline
    final boolean isLinked() {
        return ((state & VmTypeState.ST_LINKED) != 0);
    }

    /**
     * Is this type an array.
     *
     * @return boolean
     */
    public boolean isArray() {
        return false;
    }

    /**
     * Is this class an array of primitive types
     *
     * @return boolean
     */
    public boolean isPrimitiveArray() {
        return false;
    }

    /**
     * Does this class have a finalize method other then in java.lang.Object.
     *
     * @return boolean
     */
    public final boolean hasFinalizer() {
        return ((modifiers & Modifier.ACC_FINALIZER) != 0);
    }

    /**
     * Gets the finalize method of this class.
     */
    public final VmMethod getFinalizeMethod() {
        return finalizeMethod;
    }

    /**
     * Get the number of interfaces implemented by this class, or its
     * super-classes.
     *
     * @return int
     */
    public final int getNoInterfaces() {
        return (allInterfaceTable == null) ? 0 : allInterfaceTable.length;
    }

    /**
     * Get on of the list of interfaces implemented by this class, or its
     * super-classes.
     *
     * @param index
     * @return class
     */
    public final VmInterfaceClass<?> getInterface(int index) {
        return allInterfaceTable[index];
    }

    /**
     * Get the number of implementing interfaces declared in this class
     *
     * @return int
     */
    public final int getNoDeclaredInterfaces() {
        return (interfaceTable == null) ? 0 : interfaceTable.length;
    }

    /**
     * Get the number of methods declared in this class
     *
     * @return int
     */
    public final int getNoDeclaredMethods() {
        return (methodTable == null) ? 0 : methodTable.length;
    }

    /**
     * Return the declared method with a given index (0..getNoMethods()-1)
     *
     * @param index
     * @return The method
     */
    public final VmMethod getDeclaredMethod(int index) {
        return methodTable[index];
    }

    /**
     * Gets the index of the given method within this class.
     *
     * @param method
     * @return The index of the given method within this class or -1 if not
     *         found.
     */
    public final int indexOf(VmMethod method) {
        final int max = getNoDeclaredMethods();
        for (int i = 0; i < max; i++) {
            if (methodTable[i] == method) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Return the constants pool of this class
     *
     * @return The constant pool
     */
    public final VmCP getCP() {
        return cp;
    }

    /**
     * Return the loader of this class
     *
     * @return The loader
     */
    public final VmClassLoader getLoader() {
        return loader;
    }

    /**
     * Return the accessflags of this class
     *
     * @return The modifiers
     */
    public final int getAccessFlags() {
        return modifiers;
    }

    /**
     * Find the field within the given class that has the given name and
     * signature.
     *
     * @param name
     * @param signature
     * @return The field
     */
    public final VmField getDeclaredField(String name, String signature) {
        if (fieldTable != null) {
            int count = fieldTable.length;
            for (int i = 0; i < count; i++) {
                VmField fs = fieldTable[i];
                if (fs.nameEquals(name)) {
                    if ((signature == null) || fs.signatureEquals(signature)) {
                        return fs;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Find the field within the given class that has the given name and
     * signature.
     *
     * @param name
     * @return The field
     */
    public final VmField getDeclaredField(String name) {
        return getDeclaredField(name, null);
    }

    /**
     * Find the field within the given class (or super-classes) that has the
     * given name and signature.
     *
     * @param name
     * @param signature
     * @return The field
     */
    public final VmField getField(String name, String signature) {
        VmField f = getDeclaredField(name, signature);
        if (f != null) {
            return f;
        }
        if (superClass != null) {
            f = superClass.getField(name, signature);
            if (f != null) {
                return f;
            }
        }
        final int cnt = getNoInterfaces();
        for (int i = 0; i < cnt; i++) {
            f = allInterfaceTable[i].getField(name, signature);
            if (f != null) {
                return f;
            }
        }

        return null;
    }

    /**
     * Find the field within the given class (or super-classes) that has the
     * given name.
     *
     * @param name
     * @return The field
     */
    public final VmField getField(String name) {
        return getField(name, null);
    }

    /**
     * Find the field within the given class (or super-classes) that matches the
     * given fieldRef.
     *
     * @param fieldRef
     * @return The field
     */
    public final VmField getField(VmConstFieldRef fieldRef) {
        return getField(fieldRef.getName(), fieldRef.getSignature());
    }

    /**
     * Find the method within the given class (or super-classes) that has the
     * given name and signature.
     *
     * @param name
     * @param signature
     * @param onlyThisClass
     * @param searchInterfaces
     * @param hashCode
     * @return The method
     */
    final VmMethod getMethod(String name, String signature,
                             boolean onlyThisClass, boolean searchInterfaces, int hashCode) {
        /* Search in my own method table */
        final VmMethod[] mt = this.methodTable;
        if (mt != null) {
            final int count = mt.length;
            for (int i = 0; i < count; i++) {
                final VmMethod mts = mt[i];
                /**
                 * Use the hascode as first match, to make this as fast as
                 * possible
                 */
                int mtsHashCode = mts.getMemberHashCode();
                if (mtsHashCode == hashCode) {
                    if (mts.nameEquals(name) && mts.signatureEquals(signature)) {
                        return mts;
                    }
                } else if (mtsHashCode > hashCode) {
                    /**
                     * The methods are sorted on hashcode, so we can stop
                     * searching here
                     */
                    break;
                }
            }
        }

        // Is it a synthetic abstract method?
        if (isAbstract()) {
            final VmMethod method = getSyntheticAbstractMethod(name, signature,
                hashCode);
            if (method != null) {
                return method;
            }
        }

        // Look in the superclass
        if ((superClass != null) && (!onlyThisClass)) {
            final VmMethod method = superClass.getMethod(name, signature,
                false, false, hashCode);
            if (method != null) {
                return method;
            }
        }

        // Look in the super interfaces
        if (isInterface() || searchInterfaces) {
            final VmInterfaceClass[] ait = allInterfaceTable;
            if (ait != null) {
                final int count = ait.length;
                for (int i = 0; i < count; i++) {
                    final VmInterfaceClass intf = ait[i];
                    final VmMethod method = intf.getMethod(name, signature,
                        true, false, hashCode);
                    if (method != null) {
                        return method;
                    }
                }
            }
        }

        // Not found
        return null;
    }

    /**
     * Search for an synthetic abstract class, that is not in this class, but is
     * a method of one of the implemented interfaces. Synthetic abstract methods
     * are added when the VMT is created.
     *
     * @param name
     * @param signature
     * @param hashCode
     * @return The method
     */
    protected abstract VmMethod getSyntheticAbstractMethod(String name,
                                                           String signature, int hashCode);

    final VmMethod getNativeMethodReplacement(String name, String signature) {
        signature = signature.substring(0, signature.indexOf(')'));
        /* Search only in my own method table */
        final VmMethod[] mt = this.methodTable;
        if (mt != null) {
            final int count = mt.length;
            for (int i = 0; i < count; i++) {
                final VmMethod mts = mt[i];
                if (mts.nameEquals(name)) {
                    String sig2 = mts.getSignature();
                    if (signature.equals(sig2.substring(0, sig2.indexOf(')')))) {
                        return mts;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Find the method within the given class (or super-classes) that has the
     * given name and list of argument types.
     *
     * @param name
     * @param argTypes
     * @param declaredOnly
     * @return The method
     */
    final VmMethod getMethod(String name, VmType[] argTypes,
                             boolean declaredOnly) {

        /* Search in my own method table */
        final VmMethod[] mt = this.methodTable;
        if (mt != null) {
            final int count = mt.length;
            for (int i = 0; i < count; i++) {
                final VmMethod mts = mt[i];
                if (mts.nameEquals(name)) {
                    if (mts.matchArgumentTypes(argTypes)) {
                        return mts;
                    }
                }
            }
        }

        if (isInterface() && (!declaredOnly)) {
            // Look in the super interfaces
            final VmImplementedInterface[] it = interfaceTable;
            if (it != null) {
                int count = it.length;
                for (int i = 0; i < count; i++) {
                    VmImplementedInterface intf = it[i];
                    VmMethod method = intf.getResolvedVmClass().getMethod(name,
                        argTypes, false);
                    if (method != null) {
                        return method;
                    }
                }
            }
        }

        if ((superClass != null) && (!declaredOnly)) {
            // Look in the superclass
            return superClass.getMethod(name, argTypes, false);
        }

        // Not found
        return null;
    }

    /**
     * Find the method within the given class that has the given name and list
     * of argument types.
     *
     * @param name
     * @param argTypes
     * @return The method
     */
    public final VmMethod getDeclaredMethod(String name, VmType[] argTypes) {
        return getMethod(name, argTypes, true);
    }

    /**
     * Find the method within the given class and its super-classes that has the
     * given name and list of argument types.
     *
     * @param name
     * @param argTypes
     * @return The method
     */
    public final VmMethod getMethod(String name, VmType[] argTypes) {
        return getMethod(name, argTypes, false);
    }

    /**
     * Find the method within the given class (or super-classes) that has the
     * given name and signature.
     *
     * @param name
     * @param signature
     * @param onlyThisClass
     * @param searchInterfaces
     * @return The method
     */
    private final VmMethod getMethod(String name, String signature,
                                     boolean onlyThisClass, boolean searchInterfaces) {
        return getMethod(name, signature, onlyThisClass, searchInterfaces,
            VmMember.calcHashCode(name, signature));
    }

    /**
     * Find the method within this class that has the given name and signature.
     *
     * @param name
     * @param signature
     * @return The method
     */
    public final VmMethod getDeclaredMethod(String name, String signature) {
        return getMethod(name, signature, true, false, VmMember.calcHashCode(
            name, signature));
    }

    /**
     * Find the method within the given class (or super-classes) that has the
     * given name and signature.
     *
     * @param name
     * @param signature
     * @return The method
     */
    public final VmMethod getMethod(String name, String signature) {
        return getMethod(name, signature, false, true, VmMember.calcHashCode(
            name, signature));
    }

    /**
     * Find the method within the given class (or super-classes) that matches
     * the given methodRef.
     *
     * @param methodRef
     * @return The method
     */
    public final VmMethod getMethod(VmConstMethodRef methodRef) {
        return getMethod(methodRef.getName(), methodRef.getSignature(), false,
            true, methodRef.getMemberHashCode());
    }

    /**
     * Do in the following order: Verification, Preparation, Resolution
     *
     * @return VmClass This class
     */
    public final VmType link() {
        if (!isLinked()) {
            prepare();
            verify();
            compile();
            if (isInvalid()) {
                if (errorMsg != null) {
                    throw new LinkageError(errorMsg);
                } else {
                    throw new LinkageError("Class invalid");
                }
            }
            if (arrayClass != null) {
                // arrayClass.link();
            }
            if (mmType == null) {
                Vm.notifyClassResolved(this);
            }
            this.state |= VmTypeState.ST_LINKED;
        }
        return this;
    }

    /**
     * Prepare this class. This method is not synchronized since it is called
     * frequently. A simple test is done to see if the class has already been
     * prepared, if not a synchronized helper method is called to do the actual
     * prepare.
     */
    void prepare() {
        if (!isPrepared()) {
            doPrepare();
        }
    }

    /**
     * Compile this class. This method is not synchronized since it is called
     * frequently. A simple test is done to see if the class has already been
     * compiled, if not a synchronized helper method is called to do the actual
     * compile.
     */
    void compile() {
        if (!isCompiled()) {
            doCompile();
        }
    }

    /**
     * Verify this class. This method is not synchronized since it is called
     * frequently. A simple test is done to see if the class has already been
     * verfied, if not a synchronized helper method is called to do the actual
     * verify.
     */
    void verify() {
        if (!isVerified()) {
            doVerify();
        }
    }

    /**
     * Prepare this class. The following steps will be taken.
     * <ul>
     * <li>Load the super class (if any)
     * <li>Load the implemented interface classes (if any)
     * <li>Calculate the object size
     * <li>Fix the offset for all declared non-static fields
     * <li>Create the VMT
     * </ul>
     */
    private synchronized void doPrepare() {
        if (isPrepared()) {
            return;
        }
        if (isPreparing()) {
            throw new Error("Recursive prepare in " + getName());
        }

        state |= VmTypeState.ST_PREPARING;

        try {
            // Step 1a: Load the super class
            if ((superClass == null) && (superClassName != null)) {
                setSuperClass((VmNormalClass<? super T>) loader.loadClass(
                    superClassName, false));
            }

            // Step 1b: Resolve the super class
            if (superClass != null) {
                superClass.prepare();
                superClassDepth = superClass.getSuperClassDepth() + 1;
                addPragmaFlags(superClass.getPragmaFlags()
                    & TypePragmaFlags.INHERITABLE_FLAGS_MASK);
            }

            /**
             * Step 2a: Load the implemented interface classes (if any)
             */
            final int cnt = getNoDeclaredInterfaces();
            for (int i = 0; i < cnt; i++) {
                final VmImplementedInterface intf = interfaceTable[i];
                intf.resolve(loader);
            }

            // Now we're at the DEFINED state
            state |= VmTypeState.ST_DEFINED;
        } catch (ClassNotFoundException ex) {
            state |= VmTypeState.ST_INVALID;
            state &= ~VmTypeState.ST_PREPARING;
            errorMsg = ex.toString();
            throw new NoClassDefFoundError(ex.getMessage() + " in " + getName());
        }

        // Step 3: Calculate the object size
        // Step 4a: Fix the offset for all declared non-static fields
        // Step 4b: Create the referenceOffsets field
        prepareForInstantiation();

        /* Build the allInterfaceTable */
        final HashSet<VmInterfaceClass<?>> all = new HashSet<VmInterfaceClass<?>>();
        getAllInterfaces(all, this);
        this.allInterfaceTable = new VmInterfaceClass[all.size()];
        all.toArray(allInterfaceTable);

        // Step 5: Create the TIB
        final Object[] tib = prepareTIB(all);

        /* Build the interface method table */
        if (all.size() > 0) {
            final IMTBuilder imtBuilder = prepareIMT(all);
            if (imtBuilder != null) {
                tib[TIBLayout.IMT_INDEX] = imtBuilder.getImt();
                tib[TIBLayout.IMTCOLLISIONS_INDEX] = imtBuilder
                    .getImtCollisions();

                final CompiledIMT cimt = loader.compileIMT(imtBuilder);
                tib[TIBLayout.COMPILED_IMT_INDEX] = cimt.getIMTAddress();
            }
        }

        // Process uninterruptible
        if ((pragmaFlags & TypePragmaFlags.UNINTERRUPTIBLE) != 0) {
            final int mCount = getNoDeclaredMethods();
            for (int m = 0; m < mCount; m++) {
                getDeclaredMethod(m).setUninterruptible();
            }
        }

        /* Build the super classes array */
        this.superClassesArray = createSuperClassesArray(all);
        if (tib != null) {
            tib[TIBLayout.SUPERCLASSES_INDEX] = superClassesArray;
        }

        /* Is there a finalizer method other then in java.lang.Object? */
        if (superClass != null) {
            // superClass != null, so we're not in java.lang.Object
            finalizeMethod = getMethod("finalize", "()V", true, false);
            if (finalizeMethod == null) {
                finalizeMethod = superClass.getFinalizeMethod();
            }
            if (finalizeMethod != null) {
                modifiers |= Modifier.ACC_FINALIZER;
            }
        }

        /* If there is no static initializer, this class is initialized */
        if (getMethod("<clinit>", "()V", true, false) == null) {
            state |= VmTypeState.ST_ALWAYS_INITIALIZED;
        }

        // Now we're in the PREPARED state
        state |= VmTypeState.ST_PREPARED;
        state &= ~VmTypeState.ST_PREPARING;

        // Notify all threads that are waiting for me
        notifyAll();
    }

    /**
     * Compile this type.
     */
    private synchronized void doCompile() {
        if (isCompiled()) {
            return;
        }
        if (!isCompiling()) {
            if (loader.isCompileRequired()) {
                state |= VmTypeState.ST_COMPILING;
                // BootLog.debug("Compiling " + getName());

                // Compile the superclass (if any)
                if (superClass != null) {
                    superClass.compile();
                }

                // Compile the methods with the least optimizing compiler
                final int count;
                try {
                    count = doCompileRuntime(0, false);
                } catch (Throwable ex) {
                    state |= VmTypeState.ST_INVALID;
                    state &= ~VmTypeState.ST_COMPILING;
                    errorMsg = ex.toString();
                    final LinkageError le = new LinkageError(
                        "Failed to compile " + name);
                    le.initCause(ex);
                    throw le;
                }
                final int declared = getNoDeclaredMethods();
                if (count != declared) {
                    errorMsg = "Compiled skipped some methods ("
                        + (declared - count);
                    throw new LinkageError(errorMsg);
                }

                state &= ~VmTypeState.ST_COMPILING;

                notifyAll();
            }
        }
    }

    /**
     * Verify this type.
     */
    private synchronized void doVerify() {
        if (isVerified()) {
            return;
        }
        if (isVerifying()) {
            throw new Error("Recursive verify in " + getName());
        }

        state |= VmTypeState.ST_VERIFYING;

        // Verify the superclass (if any)
        if (superClass != null) {
            superClass.verify();
        }

        // TODO implement verification

        state |= VmTypeState.ST_VERIFIED;
        state &= ~VmTypeState.ST_VERIFYING;

        notifyAll();
    }

    /**
     * Do the prepare action required to instantiate this object
     */
    protected abstract void prepareForInstantiation();

    /**
     * Prepare the virtual method table
     *
     * @param allInterfaces
     * @return The tib
     */
    protected abstract Object[] prepareTIB(
        HashSet<VmInterfaceClass<?>> allInterfaces);

    /**
     * Prepare the interface method table
     *
     * @param allInterfaces
     * @return The imt builder
     */
    protected abstract IMTBuilder prepareIMT(
        HashSet<VmInterfaceClass<?>> allInterfaces);

    /**
     * Create the super classes array for this type.
     *
     * @param allInterfaces All interfaces directly or indirectly implemented by this
     *                      class
     * @return The super classes array
     */
    protected VmType<?>[] createSuperClassesArray(
        HashSet<VmInterfaceClass<?>> allInterfaces) {

        final int length = superClassDepth + 1 + allInterfaces.size();
        final VmType[] array = new VmType[length];
        // array[0] = this;
        VmType<? super T> superPtr = superClass;
        for (int i = 0; i < superClassDepth; i++) {
            array[superClassDepth - i - 1] = superPtr;
            superPtr = superPtr.getSuperClass();
        }
        array[superClassDepth] = this;

        int index = superClassDepth + 1;
        for (VmInterfaceClass intfClass : allInterfaces) {
            array[index++] = intfClass;
        }

        if (false) {
            System.out.println("SuperClassesArray for " + getName());
            for (int i = 0; i < length; i++) {
                System.out.println("[" + i + "]\t" + array[i].getName());
            }
        }

        return array;
    }

    /**
     * Fill the given hashset with all interface implemented by the given type
     * C.
     *
     * @param all A HashSet of VmInterfaceClass instances.
     * @param C
     */
    private void getAllInterfaces(HashSet<VmInterfaceClass<?>> all,
                                  VmType<?> C) {
        while (C != null) {
            final VmImplementedInterface[] it = C.interfaceTable;
            if (it != null) {
                int count = it.length;
                for (int i = 0; i < count; i++) {
                    final VmInterfaceClass ic = it[i].getResolvedVmClass();
                    if (!all.contains(ic)) {
                        all.add(ic);
                        getAllInterfaces(all, ic);
                    }
                }
            }
            C = C.getSuperClass();
        }
    }

    /**
     * Resolve all constant references in the constants pool
     */
    public final void resolveCpRefs() {
        if (!resolvedCpRefs) {
            prepare();
            if (superClass != null) {
                superClass.resolveCpRefs();
            }

            /**
             * Step 2b: Load the classes of my fields
             */
            final int fcnt = getNoDeclaredFields();
            for (int i = 0; i < fcnt; i++) {
                final VmField fs = fieldTable[i];
                fs.resolve();
            }

            /**
             * Step 2c: Load the classes of my methods
             */
            final int mcnt = getNoDeclaredMethods();
            for (int i = 0; i < mcnt; i++) {
                final VmMethod mts = methodTable[i];
                mts.resolve();
            }

            VmCP cp = this.cp;
            if (cp != null) {
                for (int i = 0; i < cp.getLength(); i++) {
                    final Object obj = cp.getAny(i);
                    if (obj instanceof VmResolvableConstObject) {
                        ((VmResolvableConstObject) obj).resolve(loader);
                    }
                }
            }
            resolvedCpRefs = true;
        }
    }

    public boolean isCpRefsResolved() {
        return resolvedCpRefs;
    }

    /**
     * Compile all the methods in this class during bootstrapping.
     *
     * @param compiler
     * @param os
     * @param optLevel The optimization level
     * @return The number of compiled methods
     */
    public final int compileBootstrap(NativeCodeCompiler compiler,
                                      NativeStream os, int optLevel) {
        if (!isPrepared()) {
            throw new IllegalStateException("VmType must have been prepared");
        }
        int rc = 0;
        if (!isCompiled()) {
            final VmMethod[] mt = methodTable;
            if (mt != null) {
                final int count = mt.length;
                for (int i = 0; i < count; i++) {
                    final VmMethod method = mt[i];
                    try {
                        // if (optLevel > method.getNativeCodeOptLevel()) {
                        compiler.compileBootstrap(method, os, optLevel);
                        rc++;
                        // method.setModifier(true, Modifier.ACC_COMPILED);
                        // }
                    } catch (Throwable ex) {
                        throw new CompileError("Compile of " + method
                            + " failed", ex);
                    }
                }
            }
            state |= VmTypeState.ST_COMPILED;
        }
        if (arrayClass != null) {
            arrayClass.link();
            rc += arrayClass.compileBootstrap(compiler, os, optLevel);
        }
        return rc;
    }

    /**
     * Compile all the methods in this class during runtime.
     *
     * @param optLevel The optimization level
     * @return The number of compiled methods
     */
    public final int compileRuntime(int optLevel, boolean enableTestCompilers) {
        if (!isPrepared()) {
            throw new IllegalStateException("VmType must have been prepared");
        }
        return doCompileRuntime(optLevel, enableTestCompilers);
    }

    /**
     * Compile all the methods in this class during runtime.
     *
     * @param optLevel The optimization level
     * @return The number of compiled methods
     */
    private final int doCompileRuntime(int optLevel, boolean enableTestCompilers) {
        final VmMethod[] mt = this.methodTable;
        int compileCount = 0;
        if (mt != null) {
            final int count = mt.length;
            for (int i = 0; i < count; i++) {
                final VmMethod method = mt[i];
                if (optLevel > method.getNativeCodeOptLevel()) {
                    LoadCompileService.compile(method, optLevel,
                        enableTestCompilers);
                    // method.setModifier(true, Modifier.ACC_COMPILED);
                    compileCount++;
                }
            }
        }
        this.state |= VmTypeState.ST_COMPILED;
        return compileCount;
    }

    public final int disassemble(String methodName, int optLevel,
                                 boolean enableTestCompilers, Writer writer) {
        if (!isPrepared()) {
            throw new IllegalStateException("VmType must have been prepared");
        }
        return doDisassemble(methodName, optLevel, enableTestCompilers, writer);
    }

    private final int doDisassemble(String methodName, int optLevel,
                                    boolean enableTestCompilers, Writer writer) {
        final VmMethod[] mt = this.methodTable;
        int disasmCount = 0;
        if (mt != null) {
            final int count = mt.length;
            for (int i = 0; i < count; i++) {
                final VmMethod method = mt[i];
                if (methodName == null || "".equals(methodName.trim())) {
                    loader.disassemble(method, optLevel, enableTestCompilers,
                        writer);
                    disasmCount++;
                } else if (method.getName().equals(methodName)) {
                    loader.disassemble(method, optLevel, enableTestCompilers,
                        writer);
                    disasmCount++;
                }
            }
        }
        return disasmCount;
    }

    /**
     * Sets the superClass.
     *
     * @param superClass The superClass to set
     */
    protected void setSuperClass(VmNormalClass<? super T> superClass) {
        if (superClass == null) {
            throw new IllegalArgumentException("superClass cannot be null");
        }
        if (this.superClass == null) {
            this.superClass = superClass;
        } else {
            throw new IllegalArgumentException("Cannot overwrite superClass");
        }
    }

    /**
     * Sets the mTable.
     *
     * @param methodTable The method table to set
     */
    protected final void setMethodTable(VmMethod[] methodTable) {
        if (this.methodTable == null) {
            this.methodTable = methodTable;
            Arrays.sort(methodTable, new MethodComparator());
        } else {
            throw new IllegalArgumentException("Cannot overwrite method table");
        }
    }

    /**
     * Sets the fieldTable.
     *
     * @param fieldTable The fieldTable to set
     */
    protected void setFieldTable(VmField[] fieldTable) {
        if (this.fieldTable == null) {
            this.fieldTable = fieldTable;
        } else {
            throw new IllegalArgumentException("Cannot overwrite field table");
        }
    }

    /**
     * Sets the interfaceTable.
     *
     * @param interfaceTable The interfaceTable to set
     */
    protected final void setInterfaceTable(
        VmImplementedInterface[] interfaceTable) {
        if (this.interfaceTable == null) {
            this.interfaceTable = interfaceTable;
        } else {
            throw new IllegalArgumentException(
                "Cannot overwrite interface table");
        }
    }

    /**
     * Sets the cp.
     *
     * @param cp The cp to set
     */
    protected void setCp(VmCP cp) {
        if (this.cp == null) {
            this.cp = cp;
        } else {
            throw new IllegalArgumentException("Cannot overwrite constant pool");
        }
    }

    /**
     * Is this class a primitive type?
     *
     * @return boolean
     */
    public boolean isPrimitive() {
        return false;
    }

    /**
     * Is this class a reference type. A reference type is an array of a
     * non-primitive class.
     *
     * @return boolean
     */
    public final boolean isReferenceType() {
        return (!isPrimitive() || isArray());
    }

    /**
     * Verify this object before it is written into the bootimage by the
     * bootimage builder.
     *
     * @see org.jnode.vm.VmSystemObject#verifyBeforeEmit()
     */
    public void verifyBeforeEmit() {
        super.verifyBeforeEmit();
        VmMethod clinit = getInitializerMethod();
        if (clinit == null) {
            state |= VmTypeState.ST_ALWAYS_INITIALIZED;
        }
    }

    /**
     * Invoke the static initializer of this class. This method is not
     * synchronized since it is called frequently. A simple test is done to see
     * if the class has already been initialized, if not a synchronized helper
     * method is called to do the actual initialization.
     */
    @Inline
    public final void initialize() {
        if (!isInitialized() && !isInitializing()) {
            linkAndInitialize();
        }
    }

    /**
     * Link and initialize this type. This is a seperate method in order to
     * control the inlining path of the native code compiler.
     */
    @NoInline
    private final void linkAndInitialize() {
        link();
        if ((superClass != null) && !isArray()) {
            /* The direct super-class must be initialized first $2.17.4 */
            superClass.initialize();
        }
        doInitialize();
    }

    /**
     * Invoke the static initializer of this class.
     */
    private synchronized void doInitialize() {
        if (!isInitialized()) {
            if (!isInitializing()) {
                final boolean sharedStatics = isSharedStatics();
                if (sharedStatics) {
                    state |= VmTypeState.SST_INITIALIZING;
                } else {
                    addIsolatedState(VmTypeState.IST_INITIALIZING);
                }
                /*
                 * Screen.debug("initialize("); Screen.debug(name);
                 */
                final VmMethod initMethod = getInitializerMethod();
                if (initMethod != null) {
                    try {
                        VmReflection.invokeStatic(initMethod);
                    } catch (InvocationTargetException ex) {
                        final Throwable targetEx = ex.getTargetException();
                        if (targetEx != null) {
                            ex.getTargetException().printStackTrace();
                            // Unsafe.die("VmType.doInitialize");
                            throw new ExceptionInInitializerError(ex
                                .getTargetException());
                        } else {
                            throw new ExceptionInInitializerError(
                                "targetEx == null");
                        }
                    }
                }
                if (sharedStatics) {
                    state |= VmTypeState.SST_INITIALIZED;
                } else {
                    addIsolatedState(VmTypeState.IST_INITIALIZED);
                }
            }
        }
    }

    /**
     * Gets the <clinit>method or null if no <clinit>method was found in this
     * class.
     *
     * @return VmMethod
     */
    private VmMethod getInitializerMethod() {
        final VmMethod[] mt = this.methodTable;
        if (mt != null) {
            int count = mt.length;
            for (int i = 0; i < count; i++) {
                VmMethod m = mt[i];
                if (m.isInitializer()) {
                    return m;
                }
            }
        }
        return null;
    }

    /**
     * @return String
     * @see org.jnode.vm.VmSystemObject#getExtraInfo()
     */
    public String getExtraInfo() {
        return "Modifiers: " + Modifier.toString(modifiers);
    }

    public static class MethodComparator implements Comparator<VmMethod> {

        /**
         * @param o1
         * @param o2
         * @return int
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(VmMethod o1, VmMethod o2) {
            final int m1 = o1.getMemberHashCode();
            final int m2 = o2.getMemberHashCode();

            if (m1 < m2) {
                return -1;
            } else if (m1 > m2) {
                return 1;
            } else {
                return 0;
            }
        }

        /**
         * @param obj
         * @return boolean
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object obj) {
            return (obj instanceof MethodComparator);
        }

    }

    /**
     * Gets the number of super classes until (and including) Object. E.g. this
     * is 0 for Object and 1 for an interface.
     *
     * @return int
     */
    public final int getSuperClassDepth() {
        return superClassDepth;
    }

    /**
     * Gets the super classes array of this type
     *
     * @return The super classes array
     */
    protected final VmType[] getSuperClassesArray() {
        return superClassesArray;
    }

    /**
     * Gets the index of this type in the shared statics table.
     *
     * @return Returns the staticsIndex.
     */
    public final int getSharedStaticsIndex() {
        return this.staticsIndex;
    }

    /**
     * Gets the protection domain of this type.
     *
     * @return the protection domain of this type.
     */
    public final ProtectionDomain getProtectionDomain() {
        return protectionDomain;
    }

    /**
     * Gets the JvmType of this type.
     *
     * @return
     * @see org.jnode.vm.JvmType
     */
    public int getJvmType() {
        return JvmType.REFERENCE;
    }

    /**
     * Gets the type information required and managed by the memory manager.
     *
     * @return Returns the mmType.
     */
    public final Object getMmType() {
        return mmType;
    }

    /**
     * Sets the type information required and managed by the memory manager.
     *
     * @param mmType The mmType to set.
     */
    public final void setMmType(Object mmType) {
        if (this.mmType != null) {
            Unsafe.debug("Cannot override mmType\n");
        } else {
            this.mmType = mmType;
        }
    }

    /**
     * @see org.jnode.vm.classmgr.VmAnnotatedElement#getSuperElement()
     */
    @Override
    protected final VmAnnotatedElement getSuperElement() {
        return superClass;
    }

    /**
     * Add the given pragma flags to my flags.
     */
    final void addPragmaFlags(int flags) {
        this.pragmaFlags |= flags;
    }

    /**
     * Gets the pragma flags of this type.
     */
    final char getPragmaFlags() {
        return this.pragmaFlags;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    /**
     * Index of the isolated type state. This refers to an int entry.
     *
     * @see org.jnode.vm.classmgr.VmIsolatedStaticsEntry#getIsolatedStaticsIndex()
     */
    public final int getIsolatedStaticsIndex() {
        return isolatedStaticsIndex;
    }

    /**
     * Permission used in {@link #fromClass(Class)}
     */
    private static final JNodePermission GETVMCLASS = new JNodePermission("getVmClass");
    private static int FIELD_OFFSET = -1;
    public static <V> VmType<V> fromClass(Class<V> clazz) {
        if (FIELD_OFFSET == -1) {
            FIELD_OFFSET = ((VmInstanceField) ClassClass.getDeclaredField("vmClass")).getOffset();
        }

//        final SecurityManager sm = System.getSecurityManager();
//        if (sm != null) { //todo: misplaced securty check -> stack overflow in gnu.testlet.
//                          //todo:                            TestSecurityManager.checkPermission
                            //todo secure this method
//            sm.checkPermission(GETVMCLASS);
//        }

        return (VmType<V>) ObjectReference.fromObject(clazz).toAddress().add(FIELD_OFFSET).
            loadObjectReference().toObject();
    }

    public Class newClass() {
        return new Class(this);
    }
}
