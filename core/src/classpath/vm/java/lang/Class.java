/*
 * $Id$
 */

package java.lang;

import gnu.java.lang.VMClassHelper;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.security.AllPermission;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.util.ArrayList;

import org.jnode.security.JNodePermission;
import org.jnode.vm.SoftByteCodes;
import org.jnode.vm.VmReflection;
import org.jnode.vm.VmSystem;
import org.jnode.vm.classmgr.Signature;
import org.jnode.vm.classmgr.VmArrayClass;
import org.jnode.vm.classmgr.VmField;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;

/**
 * Class. If you change any fields in this class, also change
 * <code>emitClass</code> in <code>org.jnode.build.ObjectEmitter</code>.
 * 
 * @author epr
 */
public final class Class<T> implements Serializable {

    private final VmType<T> vmClass;

    private Constructor[] declaredConstructors;

    private Field[] declaredFields;

    private Method[] declaredMethods;

    private ArrayList fields;

    private ArrayList methods;

    private ArrayList interfaces;

    private ArrayList constructors;

    private VmMethod defaultConstructor;

    private String name;

    /** The unknown protection domain. */
    private static ProtectionDomain unknownProtectionDomain;

    /**
     * Create a new instance. This constructor can be public, because the
     * creation of VmClass instances of already protected.
     * 
     * @param vmClass
     */
    public Class(VmType<T> vmClass) {
        if (vmClass == null) {
            throw new IllegalArgumentException("vmClass cannot be null");
        }
        this.vmClass = vmClass;
    }

    public static Class forName(String className) throws ClassNotFoundException {
        // System.out.println("Class.forName [" + className + "]");

        return VmSystem.forName(className);
    }

    public static Class forName(String className, boolean initialize,
            ClassLoader loader) throws ClassNotFoundException {
        return VmSystem.forName(className);
    }

    /**
     * Converts this object to its String representation
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return (isInterface() ? "interface " : "class ") + getName();
    }

    public boolean desiredAssertionStatus() {
        return true;
    }

    /**
     * Gets the name of this class
     * 
     * @return String
     */
    public String getName() {
        if (name == null) {
            name = vmClass.getName().replace('/', '.');
        }
        return name;
    }

    /**
     * Is this class an interface?
     * 
     * @return boolean
     */
    public boolean isInterface() {
        return vmClass.isInterface();
    }

    /**
     * Gets the Class this class extends, or null if this class is
     * <code>java.lang. Object</code>
     * 
     * @return Class
     */
    public final Class< ? super T> getSuperclass() {
        VmType superCls = getLinkedVmClass().getSuperClass();
        if (superCls != null) {
            return superCls.asClass();
        } else {
            return null;
        }
    }

    /**
     * Gets the signers of this class.
     * 
     * @return
     */
    public Object[] getSigners() {
        // TODO implement me
        return null;
    }

    /**
     * Determines the interfaces implemented by the class or interface
     * represented by this object.
     * 
     * @return Class[]
     */
    public final Class[] getInterfaces() {
        if (interfaces == null) {
            final ArrayList list = new ArrayList();
            final VmType vmClass = getLinkedVmClass();
            final int cnt = vmClass.getNoInterfaces();
            for (int i = 0; i < cnt; i++) {
                list.add(vmClass.getInterface(i).asClass());
            }
            interfaces = list;
        }
        return (Class[]) interfaces.toArray(new Class[interfaces.size()]);
    }

    /**
     * Is the given object instanceof this class.
     * 
     * @param object
     * @return boolean
     */
    public boolean isInstance(Object object) {
        return SoftByteCodes.isInstanceof(object, getLinkedVmClass());
    }

    /**
     * Discover whether an instance of the Class parameter would be an instance
     * of this Class as well. Think of doing
     * <code>isInstance(c.newInstance())</code> or even
     * <code>c.newInstance() instanceof (this class)</code>. While this
     * checks widening conversions for objects, it must be exact for primitive
     * types.
     * 
     * @param c
     *            the class to check
     * @return whether an instance of c would be an instance of this class as
     *         well
     * @throws NullPointerException
     *             if c is null
     * @since 1.1
     */
    public boolean isAssignableFrom(Class c) {
        return getLinkedVmClass().isAssignableFrom(c.getLinkedVmClass());
    }

    /**
     * Gets the classloader used to load this class.
     * 
     * @return ClassLoader
     */
    public final ClassLoader getClassLoader() {
        return vmClass.getLoader().asClassLoader();
    }

    /**
     * Create a new instance of this class, using the default constructor
     * 
     * @return Object
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public final Object newInstance() throws InstantiationException,
            IllegalAccessException {
        if (defaultConstructor == null) {
            defaultConstructor = getLinkedVmClass().getDeclaredMethod("<init>",
                    "()V");
        }
        if (defaultConstructor == null) {
            throw new InstantiationException("No default constructor");
        }
        try {
            return VmReflection.newInstance(defaultConstructor);
        } catch (InvocationTargetException ex) {
            final InstantiationException ie = new InstantiationException();
            ie.initCause(ex);
            throw ie;
        }
    }

    /**
     * Gets the modifiers of this class
     * 
     * @return int
     */
    public final int getModifiers() {
        return vmClass.getAccessFlags();
    }

    /**
     * Gets the field with the given name that is declared in this class or any
     * of my super-classes.
     * 
     * @param name
     * @return Field
     * @throws NoSuchFieldException
     * @throws SecurityException
     */
    public Field getField(String name) throws NoSuchFieldException,
            SecurityException {
        VmField f = getLinkedVmClass().getField(name);
        if (f != null) {
            return f.asField();
        } else {
            throw new NoSuchFieldException(name);
        }
    }

    /**
     * Gets all fields declared in this class and all of its super-classes.
     * 
     * @return Field[]
     */
    public Field[] getFields() {
        if (fields == null) {
            ArrayList list = new ArrayList();
            Class cls = this;
            while (cls != null) {
                final Field[] dlist = cls.getDeclaredFields();
                for (int i = 0; i < dlist.length; i++) {
                    list.add(dlist[i]);
                }
                cls = cls.getSuperclass();
            }
            fields = list;
        }
        return (Field[]) fields.toArray(new Field[fields.size()]);
    }

    /**
     * Gets the field with the given name that is declared in this class.
     * 
     * @param name
     * @return Field
     * @throws NoSuchFieldException
     * @throws SecurityException
     */
    public Field getDeclaredField(String name) throws NoSuchFieldException,
            SecurityException {
        VmField f = getLinkedVmClass().getDeclaredField(name);
        if (f != null) {
            return f.asField();
        } else {
            throw new NoSuchFieldException(name);
        }
    }

    /**
     * Gets all fields declared in this class
     * 
     * @return Field[]
     */
    public Field[] getDeclaredFields() {
        if (declaredFields == null) {
            final VmType vmClass = getLinkedVmClass();
            final int cnt = vmClass.getNoDeclaredFields();
            final Field[] list = new Field[cnt];
            for (int i = 0; i < cnt; i++) {
                list[i] = vmClass.getDeclaredField(i).asField();
            }
            declaredFields = list;
        }
        return declaredFields;
    }

    /**
     * Is this class a primitive class?
     * 
     * @return boolean
     */
    public boolean isPrimitive() {
        return vmClass.isPrimitive();
    }

    /**
     * Return the class of my components (if this class is an array)
     * 
     * @return Class
     */
    public Class getComponentType() {
        final VmType vmClass = getLinkedVmClass();
        if (vmClass instanceof VmArrayClass) {
            final VmType vmCompType = ((VmArrayClass) vmClass)
                    .getComponentType();
            if (vmCompType != null) {
                return vmCompType.asClass();
            }
        }
        return null;
    }

    /**
     * Gets the method with the given name and argument types declared in this
     * class or any of its super-classes.
     * 
     * @param name
     * @param argTypes
     * @return Method
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public Method getMethod(String name, Class[] argTypes)
            throws NoSuchMethodException, SecurityException {
        VmType[] vmArgTypes;
        if (argTypes == null) {
            vmArgTypes = null;
        } else {
            final int cnt = argTypes.length;
            vmArgTypes = new VmType[cnt];
            for (int i = 0; i < cnt; i++) {
                vmArgTypes[i] = argTypes[i].getLinkedVmClass();
            }
        }
        VmMethod method = getLinkedVmClass().getMethod(name, vmArgTypes);
        if (method != null) {
            return (Method) method.asMember();
        } else {
            throw new NoSuchMethodException(name);
        }
    }

    /**
     * Gets all methods declared in this class and its super-classes
     * 
     * @return Method[]
     */
    public Method[] getMethods() {
        if (methods == null) {
            ArrayList list = new ArrayList();
            Class cls = this;
            while (cls != null) {
                final Method[] dlist = cls.getDeclaredMethods();
                for (int i = 0; i < dlist.length; i++) {
                    list.add(dlist[i]);
                }
                cls = cls.getSuperclass();
            }
            methods = list;
        }
        return (Method[]) methods.toArray(new Method[methods.size()]);
    }

    /**
     * Gets the method with the given name and argument types declared in this
     * class.
     * 
     * @param name
     * @param argTypes
     * @return Method
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public Method getDeclaredMethod(String name, Class[] argTypes)
            throws NoSuchMethodException, SecurityException {
        VmType[] vmArgTypes;
        if (argTypes == null) {
            vmArgTypes = null;
        } else {
            final int cnt = argTypes.length;
            vmArgTypes = new VmType[cnt];
            for (int i = 0; i < cnt; i++) {
                vmArgTypes[i] = argTypes[i].getLinkedVmClass();
            }
        }
        VmMethod method = getLinkedVmClass()
                .getDeclaredMethod(name, vmArgTypes);
        if (method != null) {
            return (Method) method.asMember();
        } else {
            throw new NoSuchMethodException(name);
        }
    }

    /**
     * Gets all methods declared in this class.
     * 
     * @return Method[]
     */
    public Method[] getDeclaredMethods() {
        if (declaredMethods == null) {
            final VmType vmClass = getLinkedVmClass();
            final int cnt = vmClass.getNoDeclaredMethods();
            int max = 0;
            for (int i = 0; i < cnt; i++) {
                if (!vmClass.getDeclaredMethod(i).isConstructor()) {
                    max++;
                }
            }
            final Method[] list = new Method[max];
            max = 0;
            for (int i = 0; i < cnt; i++) {
                VmMethod vmMethod = vmClass.getDeclaredMethod(i);
                if (!vmMethod.isConstructor()) {
                    list[max++] = (Method) vmMethod.asMember();
                }
            }
            declaredMethods = list;
        }
        return declaredMethods;
    }

    /**
     * Returns an array of all public classes and interfaces that are members of
     * this class and declared in this class.
     * 
     * @return
     */
    public Class[] getDeclaredClasses() throws SecurityException {
        // TODO implement me
        return new Class[0];
    }

    /**
     * If the class or interface represented by this Class object is a member of
     * another class, returns the Class object representing the class in which
     * it was declared. This method returns null if this class or interface is
     * not a member of any other class. If this Class object represents an array
     * class, a primitive type, or void,then this method returns null.
     * 
     * @return
     */
    public Class getDeclaringClass() {
        // TODO implement me
        return null;
    }

    public Constructor getDeclaredConstructor(Class[] argTypes)
            throws NoSuchMethodException {
        String signature = Signature.toSignature(null, argTypes);
        final VmMethod vmMethod = getLinkedVmClass().getDeclaredMethod(
                "<init>", signature);
        if (vmMethod != null) {
            return (Constructor) vmMethod.asMember();
        } else {
            throw new NoSuchMethodException("<init> " + signature);
        }
    }

    /**
     * Gets all constructors declared in this class
     * 
     * @return Constructor[]
     */
    public Constructor[] getDeclaredConstructors() {
        if (declaredConstructors == null) {
            final VmType vmClass = getLinkedVmClass();
            int cnt = vmClass.getNoDeclaredMethods();
            int max = 0;
            for (int i = 0; i < cnt; i++) {
                if (vmClass.getDeclaredMethod(i).isConstructor()) {
                    max++;
                }
            }
            Constructor[] list = new Constructor[max];
            max = 0;
            for (int i = 0; i < cnt; i++) {
                VmMethod vmMethod = vmClass.getDeclaredMethod(i);
                if (vmMethod.isConstructor()) {
                    list[max++] = (Constructor) vmMethod.asMember();
                }
            }
            declaredConstructors = list;
        }
        return declaredConstructors;
    }

    /**
     * Gets the package for this class. The class loader of this class is used
     * to find the package. If the class was loaded by the bootstrap class
     * loader the set of packages loaded from CLASSPATH is searched to find the
     * package of the class. Null is returned if no package object was created
     * by the class loader of this class. Packages have attributes for versions
     * and specifications only if the information was defined in the manifests
     * that accompany the classes, and if the class loader created the package
     * instance with the attributes from the manifest.
     * 
     * @return the package of the class, or null if no package information is
     *         available from the archive or codebase.
     */
    public Package getPackage() {
        return null;
    }

    /**
     * Returns the ProtectionDomain of this class. If there is a security
     * manager installed, this method first calls the security manager's
     * checkPermission method with a RuntimePermission("getProtectionDomain")
     * permission to ensure it's ok to get the ProtectionDomain.
     * 
     * @return the ProtectionDomain of this class
     * @throws SecurityException
     *             if a security manager exists and its checkPermission method
     *             doesn't allow getting the ProtectionDomain.
     * @since 1.2
     */
    public ProtectionDomain getProtectionDomain() {
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("getProtectionDomain"));
        }
        final ProtectionDomain pd = getLinkedVmClass().getProtectionDomain();
        if (pd != null) {
            return pd;
        } else {
            return getUnknownProtectionDomain();
        }
    }

    /**
     * Gets the unknown protection domain. Create on demand.
     * 
     * @return
     */
    private static final ProtectionDomain getUnknownProtectionDomain() {
        if (unknownProtectionDomain == null) {
            Permissions permissions = new Permissions();
            permissions.add(new AllPermission());
            unknownProtectionDomain = new ProtectionDomain(null, permissions);
        }
        return unknownProtectionDomain;
    }

    /**
     * Is this class an array class?
     * 
     * @return boolean
     */
    public boolean isArray() {
        return vmClass.isArray();
    }

    /**
     * Returns an array of all public classes and interfaces that are members of
     * this class.
     * 
     * @return
     */
    public Class[] getClasses() throws SecurityException {
        // TODO implement me
        return new Class[0];
    }

    /**
     * Gets the constructor with the given argument types.
     * 
     * @param argTypes
     * @return Constructor
     * @throws NoSuchMethodException
     */
    public Constructor getConstructor(Class[] argTypes)
            throws NoSuchMethodException {
        String signature = Signature.toSignature(null, argTypes);
        final VmMethod vmMethod = getLinkedVmClass().getMethod("<init>",
                signature);
        if (vmMethod != null) {
            return (Constructor) vmMethod.asMember();
        } else {
            throw new NoSuchMethodException("<init> " + signature);
        }
    }

    /**
     * Returns an array containing Constructor objects reflecting all the public
     * constructors of the class represented by this Class object. An array of
     * length 0 is returned if the class has no public constructors, or if the
     * class is an array class, or if the class reflects a primitive type or
     * void. If there is a security manager, this method first calls the
     * security manager's checkMemberAccess method with this and Member.PUBLIC
     * as its arguments. If the class is in a package, then this method also
     * calls the security manager's checkPackageAccess method with the package
     * name as its argument. Either of these calls could result in a
     * SecurityException.
     * 
     * @return Constructor[]
     */
    public Constructor[] getConstructors() {
        if (constructors == null) {
            ArrayList list = new ArrayList();
            final Constructor[] dlist = getDeclaredConstructors();
            for (int i = 0; i < dlist.length; i++) {
                final Constructor c = dlist[i];
                if ((c.getModifiers() & Modifier.PUBLIC) != 0) {
                    list.add(dlist[i]);
                }
            }
            constructors = list;
        }
        return (Constructor[]) constructors
                .toArray(new Constructor[constructors.size()]);
    }

    /**
     * Get a resource URL using this class's package using the
     * getClassLoader().getResource() method. If this class was loaded using the
     * system classloader, ClassLoader.getSystemResource() is used instead.
     * <p>
     * If the name you supply is absolute (it starts with a <code>/</code>),
     * then it is passed on to getResource() as is. If it is relative, the
     * package name is prepended, and <code>.</code>'s are replaced with
     * <code>/</code>.
     * <p>
     * The URL returned is system- and classloader-dependent, and could change
     * across implementations.
     * 
     * @param name
     *            the name of the resource, generally a path
     * @return the URL to the resource
     * @throws NullPointerException
     *             if name is null
     * @since 1.1
     */
    public URL getResource(String name) {
        if (name.length() > 0 && name.charAt(0) != '/') {
            name = VMClassHelper.getPackagePortion(getName()).replace('.', '/')
                    + "/" + name;
        }
        final ClassLoader ld = getClassLoader();
        if (ld != null) {
            return ld.getResource(name);
        } else {
            return ClassLoader.getSystemResource(name);
        }
    }

    /**
     * Get a resource using this class's package using the
     * getClassLoader().getResourceAsStream() method. If this class was loaded
     * using the system classloader, ClassLoader.getSystemResource() is used
     * instead.
     * <p>
     * If the name you supply is absolute (it starts with a <code>/</code>),
     * then it is passed on to getResource() as is. If it is relative, the
     * package name is prepended, and <code>.</code>'s are replaced with
     * <code>/</code>.
     * <p>
     * The URL returned is system- and classloader-dependent, and could change
     * across implementations.
     * 
     * @param name
     *            the name of the resource, generally a path
     * @return an InputStream with the contents of the resource in it, or null
     * @throws NullPointerException
     *             if name is null
     * @since 1.1
     */
    public InputStream getResourceAsStream(String name) {
        if (name.length() > 0 && name.charAt(0) != '/') {
            name = VMClassHelper.getPackagePortion(getName()).replace('.', '/')
                    + "/" + name;
        }
        final ClassLoader ld = getClassLoader();
        if (ld != null) {
            return ld.getResourceAsStream(name);
        } else {
            return ClassLoader.getSystemResourceAsStream(name);
        }
    }

    /**
     * Gets the JNode VmType (internal) representation of this class. If there
     * is a security manager installed, this method first calls the security
     * manager's checkPermission method with a RuntimePermission("getVmClass")
     * permission to ensure it's ok to get the internal representation.
     * 
     * @return the JNode internal representation of this class.
     */
    public final VmType<T> getVmClass() {
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new JNodePermission("getVmClass"));
        }
        return vmClass;
    }

    /**
     * Gets a primitive class of a given type.
     * 
     * @param type
     * @return
     * @see VmType#getPrimitiveClass(char)
     */
    static Class getPrimitiveClass(char type) {
        return VmType.getPrimitiveClass(type).asClass();
    }

    /**
     * Returns the enumeration constants of this class, or null if this class is
     * not an <code>Enum</code>.
     * 
     * @return an array of <code>Enum</code> constants associated with this
     *         class, or null if this class is not an <code>enum</code>.
     */
    public T[] getEnumConstants() {
        if (isEnum()) {
            try {
                return (T[]) getMethod("values", null).invoke(null, null);
            } catch (NoSuchMethodException exception) {
                throw new Error("Enum lacks values() method");
            } catch (IllegalAccessException exception) {
                throw new Error("Unable to access Enum class");
            } catch (InvocationTargetException exception) {
                throw new RuntimeException(
                        "The values method threw an exception", exception);
            }
        } else {
            return null;
        }
    }

    /**
     * Returns true if this class is an <code>Enum</code>.
     * 
     * @return true if this is an enumeration class.
     */
    public boolean isEnum() {
        return vmClass.isEnum();
    }

    /**
     * Gets the VmType and make sure it is linked.
     * 
     * @return
     */
    private final VmType<T> getLinkedVmClass() {
        vmClass.link();
        return vmClass;
    }
}
