/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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
 
package org.jnode.test.core;

import gnu.java.lang.reflect.TypeSignature;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ProxyBuilder {

    /**
     * Returns the proxy {@link Class} for the given ClassLoader and array of
     * interfaces, dynamically generating it if necessary.
     * <p/>
     * <p>
     * There are several restrictions on this method, the violation of which
     * will result in an IllegalArgumentException or NullPointerException:
     * </p>
     * <p/>
     * <ul>
     * <li>All objects in `interfaces' must represent distinct interfaces.
     * Classes, primitive types, null, and duplicates are forbidden.</li>
     * <li>The interfaces must be visible in the specified ClassLoader. In
     * other words, for each interface i:
     * <code>Class.forName(i.getName(), false, loader) == i</code> must be
     * true.</li>
     * <li>All non-public interfaces (if any) must reside in the same package,
     * or the proxy class would be non-instantiable. If there are no non-public
     * interfaces, the package of the proxy class is unspecified.</li>
     * <li>All interfaces must be compatible - if two declare a method with the
     * same name and parameters, the return type must be the same and the throws
     * clause of the proxy class will be the maximal subset of subclasses of the
     * throws clauses for each method that is overridden.</li>
     * <li>VM constraints limit the number of interfaces a proxy class may
     * directly implement (however, the indirect inheritance of
     * {@link java.io.Serializable} does not count against this limit). Even though most
     * VMs can theoretically have 65535 superinterfaces for a class, the actual
     * limit is smaller because a class's constant pool is limited to 65535
     * entries, and not all entries can be interfaces.</li>
     * </ul>
     * <p/>
     * <p>
     * Note that different orders of interfaces produce distinct classes.
     * </p>
     *
     * @param loader     the class loader to define the proxy class in; null implies
     *                   the bootstrap class loader
     * @param interfaces the array of interfaces the proxy class implements, may be
     *                   empty, but not null
     * @return the Class object of the proxy class
     * @throws IllegalArgumentException if the constraints above were violated, except for problems
     *                                  with null
     * @throws NullPointerException     if `interfaces' is null or contains a null entry
     */
    // synchronized so that we aren't trying to build the same class
    // simultaneously in two threads
    public static byte[] getProxyClass(ClassLoader loader, Class[] interfaces) {
        ProxyType pt = new ProxyType(loader, interfaces);
        ProxyData data = ProxyData.getProxyData(pt);

        return new ClassFactory(data).generate(loader);
    }

    /**
     * Helper class for mapping unique ClassLoader and interface combinations to
     * proxy classes.
     *
     * @author Eric Blake (ebb9@email.byu.edu)
     */
    private static final class ProxyType {
        /**
         * Store the class loader (may be null)
         */
        final ClassLoader loader;

        /**
         * Store the interfaces (never null, all elements are interfaces)
         */
        final Class[] interfaces;

        /**
         * Construct the helper object.
         *
         * @param loader     the class loader to define the proxy class in; null
         *                   implies the bootstrap class loader
         * @param interfaces an array of interfaces
         */
        ProxyType(ClassLoader loader, Class[] interfaces) {
            this.loader = loader;
            this.interfaces = interfaces;
        }

        /**
         * Calculates the hash code.
         *
         * @return a combination of the classloader and interfaces hashcodes.
         */
        public int hashCode() {
            int hash = loader == null ? 0 : loader.hashCode();
            for (int i = 0; i < interfaces.length; i++)
                hash = hash * 31 + interfaces[i].hashCode();
            return hash;
        }

        /**
         * Calculates equality.
         *
         * @param other object to compare to
         * @return true if it is a ProxyType with same data
         */
        public boolean equals(Object other) {
            ProxyType pt = (ProxyType) other;
            if (loader != pt.loader
                || interfaces.length != pt.interfaces.length)
                return false;
            for (int i = 0; i < interfaces.length; i++)
                if (interfaces[i] != pt.interfaces[i])
                    return false;
            return true;
        }
    } // class ProxyType

    /**
     * Helper class which allows hashing of a method name and signature without
     * worrying about return type, declaring class, or throws clause, and which
     * reduces the maximally common throws clause between two methods
     *
     * @author Eric Blake (ebb9@email.byu.edu)
     */
    private static final class ProxySignature {
        /**
         * The core signatures which all Proxy instances handle.
         */
        static final HashMap<ProxySignature, ProxySignature> coreMethods =
            new HashMap<ProxySignature, ProxySignature>();

        static {
            try {
                ProxySignature sig = new ProxySignature(Object.class.getMethod(
                    "equals", new Class[]{Object.class}));
                coreMethods.put(sig, sig);
                sig = new ProxySignature(Object.class.getMethod("hashCode",
                    null));
                coreMethods.put(sig, sig);
                sig = new ProxySignature(Object.class.getMethod("toString",
                    null));
                coreMethods.put(sig, sig);
            } catch (Exception e) {
                // assert false;
                throw (Error) new InternalError("Unexpected: " + e)
                    .initCause(e);
            }
        }

        /**
         * The underlying Method object, never null
         */
        final Method method;

        /**
         * The set of compatible thrown exceptions, may be empty
         */
        final Set<Class> exceptions = new HashSet<Class>();

        /**
         * Construct a signature
         *
         * @param method the Method this signature is based on, never null
         */
        ProxySignature(Method method) {
            this.method = method;
            Class[] exc = method.getExceptionTypes();
            int i = exc.length;
            while (--i >= 0) {
                // discard unchecked exceptions
                if (Error.class.isAssignableFrom(exc[i])
                    || RuntimeException.class.isAssignableFrom(exc[i]))
                    continue;
                exceptions.add(exc[i]);
            }
        }

        /**
         * Given a method, make sure it's return type is identical to this, and
         * adjust this signature's throws clause appropriately
         *
         * @param other the signature to merge in
         * @throws IllegalArgumentException if the return types conflict
         */
        void checkCompatibility(ProxySignature other) {
            if (method.getReturnType() != other.method.getReturnType())
                throw new IllegalArgumentException(
                    "incompatible return types: " + method + ", "
                        + other.method);

            // if you can think of a more efficient way than this O(n^2) search,
            // implement it!
            int size1 = exceptions.size();
            int size2 = other.exceptions.size();
            boolean[] valid1 = new boolean[size1];
            boolean[] valid2 = new boolean[size2];
            Iterator itr = exceptions.iterator();
            int pos = size1;
            while (--pos >= 0) {
                Class<?> c1 = (Class) itr.next();
                Iterator itr2 = other.exceptions.iterator();
                int pos2 = size2;
                while (--pos2 >= 0) {
                    Class<?> c2 = (Class) itr2.next();
                    if (c2.isAssignableFrom(c1))
                        valid1[pos] = true;
                    if (c1.isAssignableFrom(c2))
                        valid2[pos2] = true;
                }
            }
            pos = size1;
            itr = exceptions.iterator();
            while (--pos >= 0) {
                itr.next();
                if (!valid1[pos])
                    itr.remove();
            }
            pos = size2;
            itr = other.exceptions.iterator();
            while (--pos >= 0) {
                itr.next();
                if (!valid2[pos])
                    itr.remove();
            }
            exceptions.addAll(other.exceptions);
        }

        /**
         * Calculates the hash code.
         *
         * @return a combination of name and parameter types
         */
        public int hashCode() {
            int hash = method.getName().hashCode();
            Class[] types = method.getParameterTypes();
            for (int i = 0; i < types.length; i++)
                hash = hash * 31 + types[i].hashCode();
            return hash;
        }

        /**
         * Calculates equality.
         *
         * @param other object to compare to
         * @return true if it is a ProxySignature with same data
         */
        public boolean equals(Object other) {
            ProxySignature ps = (ProxySignature) other;
            Class[] types1 = method.getParameterTypes();
            Class[] types2 = ps.method.getParameterTypes();
            if (!method.getName().equals(ps.method.getName())
                || types1.length != types2.length)
                return false;
            int i = types1.length;
            while (--i >= 0)
                if (types1[i] != types2[i])
                    return false;
            return true;
        }
    } // class ProxySignature

    /**
     * A flat representation of all data needed to generate bytecode/instantiate
     * a proxy class. This is basically a struct.
     *
     * @author Eric Blake (ebb9@email.byu.edu)
     */
    static final class ProxyData {
        /**
         * The package this class is in <b>including the trailing dot</b> or an
         * empty string for the unnamed (aka default) package.
         */
        String pack = "";

        /**
         * The interfaces this class implements. Non-null, but possibly empty.
         */
        Class[] interfaces;

        /**
         * The Method objects this class must pass as the second argument to
         * invoke (also useful for determining what methods this class has).
         * Non-null, non-empty (includes at least Object.hashCode,
         * Object.equals, and Object.toString).
         */
        Method[] methods;

        /**
         * The exceptions that do not need to be wrapped in
         * UndeclaredThrowableException. exceptions[i] is the same as, or a
         * subset of subclasses, of methods[i].getExceptionTypes(), depending on
         * compatible throws clauses with multiple inheritance. It is
         * unspecified if these lists include or exclude subclasses of Error and
         * RuntimeException, but excluding them is harmless and generates a
         * smaller class.
         */
        Class[][] exceptions;

        /**
         * For unique id's
         */
        private static int count;

        /**
         * The id of this proxy class
         */
        final int id = count++;

        /**
         * Construct a ProxyData with uninitialized data members.
         */
        ProxyData() {
        }

        /**
         * Return the name of a package (including the trailing dot) given the
         * name of a class. Returns an empty string if no package. We use this
         * in preference to using Class.getPackage() to avoid problems with
         * ClassLoaders that don't set the package.
         */
        private static String getPackage(Class k) {
            String name = k.getName();
            int idx = name.lastIndexOf('.');
            return name.substring(0, idx + 1);
        }

        /**
         * Verifies that the arguments are legal, and sets up remaining data
         * This should only be called when a class must be generated, as it is
         * expensive.
         *
         * @param pt the ProxyType to convert to ProxyData
         * @return the flattened, verified ProxyData structure for use in class
         *         generation
         * @throws IllegalArgumentException if `interfaces' contains non-interfaces or incompatible
         *                                  combinations, and verify is true
         * @throws NullPointerException     if interfaces is null or contains null
         */
        static ProxyData getProxyData(ProxyType pt) {
            Map<ProxySignature, ProxySignature> method_set =
                (Map<ProxySignature, ProxySignature>) ProxySignature.coreMethods
                    .clone();
            boolean in_package = false; // true if we encounter non-public
            // interface

            ProxyData data = new ProxyData();
            data.interfaces = pt.interfaces;

            // if interfaces is too large, we croak later on when the constant
            // pool overflows
            int i = data.interfaces.length;
            while (--i >= 0) {
                Class inter = data.interfaces[i];
                if (!inter.isInterface())
                    throw new IllegalArgumentException("not an interface: "
                        + inter);
                try {
                    if (Class.forName(inter.getName(), false, pt.loader) != inter)
                        throw new IllegalArgumentException("not accessible in "
                            + "classloader: " + inter);
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("not accessible in "
                        + "classloader: " + inter);
                }
                if (!Modifier.isPublic(inter.getModifiers()))
                    if (in_package) {
                        String p = getPackage(inter);
                        if (!data.pack.equals(p))
                            throw new IllegalArgumentException(
                                "non-public interfaces "
                                    + "from different " + "packages");
                    } else {
                        in_package = true;
                        data.pack = getPackage(inter);
                    }
                for (int j = i - 1; j >= 0; j--)
                    if (data.interfaces[j] == inter)
                        throw new IllegalArgumentException(
                            "duplicate interface: " + inter);
                Method[] methods = inter.getMethods();
                int j = methods.length;
                while (--j >= 0) {
                    if (isCoreObjectMethod(methods[j])) {
                        // In the case of an attempt to redefine a public
                        // non-final
                        // method of Object, we must skip it
                        continue;
                    }
                    ProxySignature sig = new ProxySignature(methods[j]);
                    ProxySignature old = (ProxySignature) method_set.put(sig,
                        sig);
                    if (old != null)
                        sig.checkCompatibility(old);
                }
            }

            i = method_set.size();
            data.methods = new Method[i];
            data.exceptions = new Class[i][];
            Iterator itr = method_set.values().iterator();
            while (--i >= 0) {
                ProxySignature sig = (ProxySignature) itr.next();
                data.methods[i] = sig.method;
                data.exceptions[i] = (Class[]) sig.exceptions
                    .toArray(new Class[sig.exceptions.size()]);
            }
            return data;
        }

        /**
         * Checks whether the method is similar to a public non-final method of
         * Object or not (i.e. with the same name and parameter types). Note
         * that we can't rely, directly or indirectly (via Collection.contains)
         * on Method.equals as it would also check the declaring class, what we
         * do not want. We only want to check that the given method have the
         * same signature as a core method (same name and parameter types)
         *
         * @param method the method to check
         * @return whether the method has the same name and parameter types as
         *         Object.equals, Object.hashCode or Object.toString
         * @see java.lang.Object#equals(Object)
         * @see java.lang.Object#hashCode()
         * @see java.lang.Object#toString()
         */
        private static boolean isCoreObjectMethod(Method method) {
            String methodName = method.getName();
            if (methodName.equals("equals")) {
                return Arrays.equals(method.getParameterTypes(),
                    new Class[]{Object.class});
            }
            if (methodName.equals("hashCode")) {
                return method.getParameterTypes().length == 0;
            }
            if (methodName.equals("toString")) {
                return method.getParameterTypes().length == 0;
            }
            return false;
        }

    } // class ProxyData

    /**
     * Does all the work of building a class. By making this a nested class,
     * this code is not loaded in memory if the VM has a native implementation
     * instead.
     *
     * @author Eric Blake (ebb9@email.byu.edu)
     */
    private static final class ClassFactory {
        /**
         * Constants for assisting the compilation
         */
        private static final byte FIELD = 1;

        private static final byte METHOD = 2;

        private static final byte INTERFACE = 3;

        private static final String CTOR_SIG = "(Ljava/lang/reflect/InvocationHandler;)V";

        private static final String INVOKE_SIG = "(Ljava/lang/Object;"
            + "Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;";

        /**
         * Bytecodes for insertion in the class definition byte[]
         */
        private static final char ACONST_NULL = 1;

        private static final char ICONST_0 = 3;

        private static final char BIPUSH = 16;

        private static final char SIPUSH = 17;

        private static final char ILOAD = 21;

        private static final char ILOAD_0 = 26;

        private static final char ALOAD_0 = 42;

        private static final char ALOAD_1 = 43;

        private static final char AALOAD = 50;

        private static final char AASTORE = 83;

        private static final char DUP = 89;

        private static final char DUP_X1 = 90;

        private static final char SWAP = 95;

        private static final char IRETURN = 172;

        private static final char LRETURN = 173;

        private static final char FRETURN = 174;

        private static final char DRETURN = 175;

        private static final char ARETURN = 176;

        private static final char RETURN = 177;

        private static final char GETSTATIC = 178;

        private static final char GETFIELD = 180;

        private static final char INVOKEVIRTUAL = 182;

        private static final char INVOKESPECIAL = 183;

        private static final char INVOKEINTERFACE = 185;

        private static final char NEW = 187;

        private static final char ANEWARRAY = 189;

        private static final char ATHROW = 191;

        private static final char CHECKCAST = 192;

        private static final char POP = 87;

        // Implementation note: we use StringBuffers to hold the byte data,
        // since
        // they automatically grow. However, we only use the low 8 bits of
        // every char in the array, so we are using twice the necessary memory
        // for the ease StringBuffer provides.

        /**
         * The constant pool.
         */
        private final StringBuffer pool = new StringBuffer();

        /**
         * The rest of the class data.
         */
        private final StringBuffer stream = new StringBuffer();

        /**
         * Map of strings to byte sequences, to minimize size of pool.
         */
        private final Map poolEntries = new HashMap();

        /**
         * The VM name of this proxy class.
         */
        private final String qualName;

        /**
         * The Method objects the proxy class refers to when calling the
         * invocation handler.
         */
        private final Method[] methods;

        /**
         * Initializes the buffers with the bytecode contents for a proxy class.
         *
         * @param data the remainder of the class data
         * @throws IllegalArgumentException if anything else goes wrong this late in the game; as far
         *                                  as I can tell, this will only happen if the constant pool
         *                                  overflows, which is possible even when the user doesn't
         *                                  exceed the 65535 interface limit
         */
        ClassFactory(ProxyData data) {
            methods = data.methods;

            // magic = 0xcafebabe
            // minor_version = 0
            // major_version = 46
            // constant_pool_count: place-holder for now
            pool.append("\u00ca\u00fe\u00ba\u00be\0\0\0\56\0\0");
            // constant_pool[], filled in as we go

            // access_flags
            putU2(0x0020/* Modifier.SUPER */ | Modifier.FINAL | Modifier.PUBLIC);
            // this_class
            qualName = (data.pack + "$Proxy" + data.id);
            putU2(classInfo(TypeSignature.getEncodingOfClass(qualName, false)));
            // super_class
            putU2(classInfo("java/lang/reflect/Proxy"));

            // interfaces_count
            putU2(data.interfaces.length);
            // interfaces[]
            for (int i = 0; i < data.interfaces.length; i++)
                putU2(classInfo(data.interfaces[i]));

            // Recall that Proxy classes serialize specially, so we do not need
            // to worry about a <clinit> method for this field. Instead, we
            // just assign it by reflection after the class is successfully
            // loaded.
            // fields_count - private static Method[] m;
            putU2(1);
            // fields[]
            // m.access_flags
            putU2(Modifier.PRIVATE | Modifier.STATIC);
            // m.name_index
            putU2(utf8Info("m"));
            // m.descriptor_index
            putU2(utf8Info("[Ljava/lang/reflect/Method;"));
            // m.attributes_count
            putU2(0);
            // m.attributes[]

            // methods_count - # handler methods, plus <init>
            putU2(methods.length + 1);
            // methods[]
            // <init>.access_flags
            putU2(Modifier.PUBLIC);
            // <init>.name_index
            putU2(utf8Info("<init>"));
            // <init>.descriptor_index
            putU2(utf8Info(CTOR_SIG));
            // <init>.attributes_count - only Code is needed
            putU2(1);
            // <init>.Code.attribute_name_index
            putU2(utf8Info("Code"));
            // <init>.Code.attribute_length = 18
            // <init>.Code.info:
            // $Proxynn(InvocationHandler h) { super(h); }
            // <init>.Code.max_stack = 2
            // <init>.Code.max_locals = 2
            // <init>.Code.code_length = 6
            // <init>.Code.code[]
            stream.append("\0\0\0\22\0\2\0\2\0\0\0\6" + ALOAD_0 + ALOAD_1
                + INVOKESPECIAL);
            putU2(refInfo(METHOD, "java/lang/reflect/Proxy", "<init>", CTOR_SIG));
            // <init>.Code.exception_table_length = 0
            // <init>.Code.exception_table[]
            // <init>.Code.attributes_count = 0
            // <init>.Code.attributes[]
            stream.append(RETURN + "\0\0\0\0");

            for (int i = methods.length - 1; i >= 0; i--)
                emitMethod(i, data.exceptions[i]);

            // attributes_count
            putU2(0);
            // attributes[] - empty; omit SourceFile attribute
            // XXX should we mark this with a Synthetic attribute?
        }

        /**
         * Produce the bytecode for a single method.
         *
         * @param i the index of the method we are building
         * @param e the exceptions possible for the method
         */
        private void emitMethod(int i, Class[] e) {
            // First, we precalculate the method length and other information.

            Method m = methods[i];
            Class[] paramtypes = m.getParameterTypes();
            int wrap_overhead = 0; // max words taken by wrapped primitive
            int param_count = 1; // 1 for this
            int code_length = 16; // aload_0, getfield, aload_0, getstatic,
            // const,
            // aaload, const/aconst_null, invokeinterface
            if (i > 5) {
                if (i > Byte.MAX_VALUE)
                    code_length += 2; // sipush
                else
                    code_length++; // bipush
            }
            if (paramtypes.length > 0) {
                code_length += 3; // anewarray
                if (paramtypes.length > Byte.MAX_VALUE)
                    code_length += 2; // sipush
                else if (paramtypes.length > 5)
                    code_length++; // bipush
                for (int j = 0; j < paramtypes.length; j++) {
                    code_length += 4; // dup, const, load, store
                    Class type = paramtypes[j];
                    if (j > 5) {
                        if (j > Byte.MAX_VALUE)
                            code_length += 2; // sipush
                        else
                            code_length++; // bipush
                    }
                    if (param_count >= 4)
                        code_length++; // 2-byte load
                    param_count++;
                    if (type.isPrimitive()) {
                        code_length += 7; // new, dup, invokespecial
                        if (type == long.class || type == double.class) {
                            wrap_overhead = 3;
                            param_count++;
                        } else if (wrap_overhead < 2)
                            wrap_overhead = 2;
                    }
                }
            }
            int end_pc = code_length;
            Class ret_type = m.getReturnType();
            if (ret_type == void.class) {
                System.out.println("ret_type==void");
                code_length++; // pop
                code_length++; // return
            } else if (ret_type.isPrimitive())
                code_length += 7; // cast, invokevirtual, return
            else
                code_length += 4; // cast, return
            int exception_count = 0;
            boolean throws_throwable = false;
            for (int j = 0; j < e.length; j++)
                if (e[j] == Throwable.class) {
                    throws_throwable = true;
                    break;
                }
            if (!throws_throwable) {
                exception_count = e.length + 3; // Throwable, Error,
                // RuntimeException
                code_length += 9; // new, dup_x1, swap, invokespecial, athrow
            }
            int handler_pc = code_length - 1;
            StringBuffer signature = new StringBuffer("(");
            for (int j = 0; j < paramtypes.length; j++)
                signature.append(TypeSignature
                    .getEncodingOfClass(paramtypes[j]));
            signature.append(")").append(
                TypeSignature.getEncodingOfClass(ret_type));

            // Now we have enough information to emit the method.

            // handler.access_flags
            putU2(Modifier.PUBLIC | Modifier.FINAL);
            // handler.name_index
            putU2(utf8Info(m.getName()));
            // handler.descriptor_index
            putU2(utf8Info(signature.toString()));
            // handler.attributes_count - Code is necessary, Exceptions possible
            putU2(e.length > 0 ? 2 : 1);

            // handler.Code.info:
            // type name(args) {
            // try {
            // return (type) h.invoke(this, methods[i], new Object[] {args});
            // } catch (<declared Exceptions> e) {
            // throw e;
            // } catch (Throwable t) {
            // throw new UndeclaredThrowableException(t);
            // }
            // }
            // Special cases:
            // if arg_n is primitive, wrap it
            // if method throws Throwable, try-catch is not needed
            // if method returns void, return statement not needed
            // if method returns primitive, unwrap it
            // save space by sharing code for all the declared handlers

            // handler.Code.attribute_name_index
            putU2(utf8Info("Code"));
            // handler.Code.attribute_length
            putU4(12 + code_length + 8 * exception_count);
            // handler.Code.max_stack
            putU2(param_count == 1 ? 4 : 7 + wrap_overhead);
            // handler.Code.max_locals
            putU2(param_count);
            // handler.Code.code_length
            putU4(code_length);
            // handler.Code.code[]
            putU1(ALOAD_0);
            putU1(GETFIELD);
            putU2(refInfo(FIELD, "java/lang/reflect/Proxy", "h",
                "Ljava/lang/reflect/InvocationHandler;"));
            putU1(ALOAD_0);
            putU1(GETSTATIC);
            putU2(refInfo(FIELD, TypeSignature.getEncodingOfClass(qualName,
                false), "m", "[Ljava/lang/reflect/Method;"));
            putConst(i);
            putU1(AALOAD);
            if (paramtypes.length > 0) {
                putConst(paramtypes.length);
                putU1(ANEWARRAY);
                putU2(classInfo("java/lang/Object"));
                param_count = 1;
                for (int j = 0; j < paramtypes.length; j++, param_count++) {
                    putU1(DUP);
                    putConst(j);
                    if (paramtypes[j].isPrimitive()) {
                        putU1(NEW);
                        putU2(classInfo(wrapper(paramtypes[j])));
                        putU1(DUP);
                    }
                    putLoad(param_count, paramtypes[j]);
                    if (paramtypes[j].isPrimitive()) {
                        putU1(INVOKESPECIAL);
                        putU2(refInfo(
                            METHOD,
                            wrapper(paramtypes[j]),
                            "<init>",
                            '(' + (TypeSignature
                                .getEncodingOfClass(paramtypes[j]) + ")V")));
                        if (paramtypes[j] == long.class
                            || paramtypes[j] == double.class)
                            param_count++;
                    }
                    putU1(AASTORE);
                }
            } else
                putU1(ACONST_NULL);
            putU1(INVOKEINTERFACE);
            putU2(refInfo(INTERFACE, "java/lang/reflect/InvocationHandler",
                "invoke", INVOKE_SIG));
            putU1(4); // InvocationHandler, this, Method, Object[]
            putU1(0);
            System.out.println(" return type: " + ret_type.getName());
            if (ret_type == void.class) {
                System.out.println(" return type == void");
                putU1(POP);
                putU1(RETURN);
            } else if (ret_type.isPrimitive()) {
                putU1(CHECKCAST);
                putU2(classInfo(wrapper(ret_type)));
                putU1(INVOKEVIRTUAL);
                putU2(refInfo(METHOD, wrapper(ret_type), ret_type.getName()
                    + "Value", "()"
                    + TypeSignature.getEncodingOfClass(ret_type)));
                if (ret_type == long.class)
                    putU1(LRETURN);
                else if (ret_type == float.class)
                    putU1(FRETURN);
                else if (ret_type == double.class)
                    putU1(DRETURN);
                else
                    putU1(IRETURN);
            } else {
                putU1(CHECKCAST);
                putU2(classInfo(ret_type));
                putU1(ARETURN);
            }
            if (!throws_throwable) {
                putU1(NEW);
                putU2(classInfo("java/lang/reflect/UndeclaredThrowableException"));
                putU1(DUP_X1);
                putU1(SWAP);
                putU1(INVOKESPECIAL);
                putU2(refInfo(METHOD,
                    "java/lang/reflect/UndeclaredThrowableException",
                    "<init>", "(Ljava/lang/Throwable;)V"));
                putU1(ATHROW);
            }

            // handler.Code.exception_table_length
            putU2(exception_count);
            // handler.Code.exception_table[]
            if (!throws_throwable) {
                // handler.Code.exception_table.start_pc
                putU2(0);
                // handler.Code.exception_table.end_pc
                putU2(end_pc);
                // handler.Code.exception_table.handler_pc
                putU2(handler_pc);
                // handler.Code.exception_table.catch_type
                putU2(classInfo("java/lang/Error"));
                // handler.Code.exception_table.start_pc
                putU2(0);
                // handler.Code.exception_table.end_pc
                putU2(end_pc);
                // handler.Code.exception_table.handler_pc
                putU2(handler_pc);
                // handler.Code.exception_table.catch_type
                putU2(classInfo("java/lang/RuntimeException"));
                for (int j = 0; j < e.length; j++) {
                    // handler.Code.exception_table.start_pc
                    putU2(0);
                    // handler.Code.exception_table.end_pc
                    putU2(end_pc);
                    // handler.Code.exception_table.handler_pc
                    putU2(handler_pc);
                    // handler.Code.exception_table.catch_type
                    putU2(classInfo(e[j]));
                }
                // handler.Code.exception_table.start_pc
                putU2(0);
                // handler.Code.exception_table.end_pc
                putU2(end_pc);
                // handler.Code.exception_table.handler_pc -
                // -8 for undeclared handler, which falls thru to normal one
                putU2(handler_pc - 8);
                // handler.Code.exception_table.catch_type
                putU2(0);
            }
            // handler.Code.attributes_count
            putU2(0);
            // handler.Code.attributes[]

            if (e.length > 0) {
                // handler.Exceptions.attribute_name_index
                putU2(utf8Info("Exceptions"));
                // handler.Exceptions.attribute_length
                putU4(2 * e.length + 2);
                // handler.Exceptions.number_of_exceptions
                putU2(e.length);
                // handler.Exceptions.exception_index_table[]
                for (int j = 0; j < e.length; j++)
                    putU2(classInfo(e[j]));
            }
        }

        /**
         * Creates the Class object that corresponds to the bytecode buffers
         * built when this object was constructed.
         *
         * @param loader the class loader to define the proxy class in; null
         *               implies the bootstrap class loader
         * @return the proxy class Class object
         */
        byte[] generate(ClassLoader loader) {
            byte[] bytecode = new byte[pool.length() + stream.length()];
            // More efficient to bypass calling charAt() repetitively.
            char[] c = pool.toString().toCharArray();
            int i = c.length;
            while (--i >= 0)
                bytecode[i] = (byte) c[i];
            c = stream.toString().toCharArray();
            i = c.length;
            int j = bytecode.length;
            while (i > 0)
                bytecode[--j] = (byte) c[--i];

            // Patch the constant pool size, which we left at 0 earlier.
            int count = poolEntries.size() + 1;
            bytecode[8] = (byte) (count >> 8);
            bytecode[9] = (byte) count;

            return bytecode;
        }

        /**
         * Put a single byte on the stream.
         *
         * @param i the information to add (only lowest 8 bits are used)
         */
        private void putU1(int i) {
            stream.append((char) i);
        }

        /**
         * Put two bytes on the stream.
         *
         * @param i the information to add (only lowest 16 bits are used)
         */
        private void putU2(int i) {
            stream.append((char) (i >> 8)).append((char) i);
        }

        /**
         * Put four bytes on the stream.
         *
         * @param i the information to add (treated as unsigned)
         */
        private void putU4(int i) {
            stream.append((char) (i >> 24)).append((char) (i >> 16));
            stream.append((char) (i >> 8)).append((char) i);
        }

        /**
         * Put bytecode to load a constant integer on the stream. This only
         * needs to work for values less than Short.MAX_VALUE.
         *
         * @param i the int to add
         */
        private void putConst(int i) {
            if (i >= -1 && i <= 5)
                putU1(ICONST_0 + i);
            else if (i >= Byte.MIN_VALUE && i <= Byte.MAX_VALUE) {
                putU1(BIPUSH);
                putU1(i);
            } else {
                putU1(SIPUSH);
                putU2(i);
            }
        }

        /**
         * Put bytecode to load a given local variable on the stream.
         *
         * @param i    the slot to load
         * @param type the base type of the load
         */
        private void putLoad(int i, Class type) {
            int offset = 0;
            if (type == long.class)
                offset = 1;
            else if (type == float.class)
                offset = 2;
            else if (type == double.class)
                offset = 3;
            else if (!type.isPrimitive())
                offset = 4;
            if (i < 4)
                putU1(ILOAD_0 + 4 * offset + i);
            else {
                putU1(ILOAD + offset);
                putU1(i);
            }
        }

        /**
         * Given a primitive type, return its wrapper class name.
         *
         * @param clazz the primitive type (but not void.class)
         * @return the internal form of the wrapper class name
         */
        private String wrapper(Class clazz) {
            if (clazz == boolean.class)
                return "java/lang/Boolean";
            if (clazz == byte.class)
                return "java/lang/Byte";
            if (clazz == short.class)
                return "java/lang/Short";
            if (clazz == char.class)
                return "java/lang/Character";
            if (clazz == int.class)
                return "java/lang/Integer";
            if (clazz == long.class)
                return "java/lang/Long";
            if (clazz == float.class)
                return "java/lang/Float";
            if (clazz == double.class)
                return "java/lang/Double";
            // assert false;
            return null;
        }

        /**
         * Returns the entry of this String in the Constant pool, adding it if
         * necessary.
         *
         * @param str the String to resolve
         * @return the index of the String in the constant pool
         */
        private char utf8Info(String str) {
            String utf8 = toUtf8(str);
            int len = utf8.length();
            return poolIndex("\1" + (char) (len >> 8) + (char) (len & 0xff)
                + utf8);
        }

        /**
         * Returns the entry of the appropriate class info structure in the
         * Constant pool, adding it if necessary.
         *
         * @param name the class name, in internal form
         * @return the index of the ClassInfo in the constant pool
         */
        private char classInfo(String name) {
            char index = utf8Info(name);
            char[] c = {7, (char) (index >> 8), (char) (index & 0xff)};
            return poolIndex(new String(c));
        }

        /**
         * Returns the entry of the appropriate class info structure in the
         * Constant pool, adding it if necessary.
         *
         * @param clazz the class type
         * @return the index of the ClassInfo in the constant pool
         */
        private char classInfo(Class clazz) {
            return classInfo(TypeSignature.getEncodingOfClass(clazz.getName(),
                false));
        }

        /**
         * Returns the entry of the appropriate fieldref, methodref, or
         * interfacemethodref info structure in the Constant pool, adding it if
         * necessary.
         *
         * @param structure FIELD, METHOD, or INTERFACE
         * @param clazz     the class name, in internal form
         * @param name      the simple reference name
         * @param type      the type of the reference
         * @return the index of the appropriate Info structure in the constant
         *         pool
         */
        private char refInfo(byte structure, String clazz, String name,
                             String type) {
            char cindex = classInfo(clazz);
            char ntindex = nameAndTypeInfo(name, type);
            // relies on FIELD == 1, METHOD == 2, INTERFACE == 3
            char[] c = {(char) (structure + 8), (char) (cindex >> 8),
                (char) (cindex & 0xff), (char) (ntindex >> 8),
                (char) (ntindex & 0xff)};
            return poolIndex(new String(c));
        }

        /**
         * Returns the entry of the appropriate nameAndTyperef info structure in
         * the Constant pool, adding it if necessary.
         *
         * @param name the simple name
         * @param type the reference type
         * @return the index of the NameAndTypeInfo structure in the constant
         *         pool
         */
        private char nameAndTypeInfo(String name, String type) {
            char nindex = utf8Info(name);
            char tindex = utf8Info(type);
            char[] c = {12, (char) (nindex >> 8), (char) (nindex & 0xff),
                (char) (tindex >> 8), (char) (tindex & 0xff)};
            return poolIndex(new String(c));
        }

        /**
         * Converts a regular string to a UTF8 string, where the upper byte of
         * every char is 0, and '\\u0000' is not in the string. This is
         * basically to use a String as a fancy byte[], and while it is less
         * efficient in memory use, it is easier for hashing.
         *
         * @param str the original, in straight unicode
         * @return a modified string, in UTF8 format in the low bytes
         */
        private String toUtf8(String str) {
            final char[] ca = str.toCharArray();
            final int len = ca.length;

            // Avoid object creation, if str is already fits UTF8.
            int i;
            for (i = 0; i < len; i++)
                if (ca[i] == 0 || ca[i] > '\u007f')
                    break;
            if (i == len)
                return str;

            final StringBuffer sb = new StringBuffer(str);
            sb.setLength(i);
            for (; i < len; i++) {
                final char c = ca[i];
                if (c > 0 && c <= '\u007f')
                    sb.append(c);
                else if (c <= '\u07ff') {
                    // includes '\0'
                    
                    sb.append((char) (0xc0 | (c >> 6)));
                    sb.append((char) (0x80 | (c & 0x6f)));
                } else {
                    sb.append((char) (0xe0 | (c >> 12)));
                    sb.append((char) (0x80 | ((c >> 6) & 0x6f)));
                    sb.append((char) (0x80 | (c & 0x6f)));
                }
            }
            return sb.toString();
        }

        /**
         * Returns the location of a byte sequence (conveniently wrapped in a
         * String with all characters between \u0001 and \u00ff inclusive) in
         * the constant pool, adding it if necessary.
         *
         * @param sequence the byte sequence to look for
         * @return the index of the sequence
         * @throws IllegalArgumentException if this would make the constant pool overflow
         */
        private char poolIndex(String sequence) {
            Integer i = (Integer) poolEntries.get(sequence);
            if (i == null) {
                // pool starts at index 1
                int size = poolEntries.size() + 1;
                if (size >= 65535)
                    throw new IllegalArgumentException("exceeds VM limitations");
                i = new Integer(size);
                poolEntries.put(sequence, i);
                pool.append(sequence);
            }
            return (char) i.intValue();
        }
    } // class ClassFactory
}
