/* Class.java -- Representation of a Java class.
   Copyright (C) 1998, 1999, 2000, 2002, 2003, 2004, 2005, 2006
   Free Software Foundation
 
This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

package java.lang;

import gnu.java.lang.VMClassHelper;
import gnu.java.lang.reflect.ClassSignatureParser;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.net.URL;
import java.security.AllPermission;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.HashSet;

import org.jnode.security.JNodePermission;
import org.jnode.vm.SoftByteCodes;
import org.jnode.vm.VmReflection;
import org.jnode.vm.VmSystem;
import org.jnode.vm.classmgr.Signature;
import org.jnode.vm.classmgr.VmArrayClass;
import org.jnode.vm.classmgr.VmField;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.classmgr.VmClassLoader;

/**
 * A Class represents a Java type.  There will never be multiple Class
 * objects with identical names and ClassLoaders. Primitive types, array
 * types, and void also have a Class object.
 * 
 * <p>Arrays with identical type and number of dimensions share the same class.
 * The array class ClassLoader is the same as the ClassLoader of the element
 * type of the array (which can be null to indicate the bootstrap classloader).
 * The name of an array class is <code>[&lt;signature format&gt;;</code>.
 * <p> For example,
 * String[]'s class is <code>[Ljava.lang.String;</code>. boolean, byte,
 * short, char, int, long, float and double have the "type name" of
 * Z,B,S,C,I,J,F,D for the purposes of array classes.  If it's a
 * multidimensioned array, the same principle applies:
 * <code>int[][][]</code> == <code>[[[I</code>.
 *
 * <p>There is no public constructor - Class objects are obtained only through
 * the virtual machine, as defined in ClassLoaders.
 *
 * @serialData Class objects serialize specially:
 * <code>TC_CLASS ClassDescriptor</code>. For more serialization information,
 * see {@link java.io.ObjectStreamClass}.
 *
 * @author John Keiser
 * @author Eric Blake (ebb9@email.byu.edu)
 * @author Tom Tromey (tromey@redhat.com)
 * @author Andrew John Hughes (gnu_andrew@member.fsf.org)
 * @since 1.0
 * @see ClassLoader
 */
public final class Class<T> implements AnnotatedElement, Serializable, Type,
        GenericDeclaration {

    /**
     * Compatible with JDK 1.0+.
     */
    private static final long serialVersionUID = 3206093459760846163L;

    /**
     * Permission used in {@link #getVmClass()}
     */
    private static final JNodePermission GETVMCLASS = new JNodePermission(
            "getVmClass");

    private final VmType<T> vmClass;

    private Constructor[] declaredConstructors;

    private Field[] declaredFields;

    private Method[] declaredMethods;

    private ArrayList<Field> fields;

    private ArrayList<Method> methods;

    private ArrayList<Class> interfaces;

    private ArrayList<Constructor> constructors;

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

    /**
     * Returns the desired assertion status of this class, if it were to be
     * initialized at this moment. The class assertion status, if set, is
     * returned; the backup is the default package status; then if there is a
     * class loader, that default is returned; and finally the system default is
     * returned. This method seldom needs calling in user code, but exists for
     * compilers to implement the assert statement. Note that there is no
     * guarantee that the result of this method matches the class's actual
     * assertion status.
     * 
     * @return the desired assertion status
     * @see ClassLoader#setClassAssertionStatus(String, boolean)
     * @see ClassLoader#setPackageAssertionStatus(String, boolean)
     * @see ClassLoader#setDefaultAssertionStatus(boolean)
     * @since 1.4
     */
    public boolean desiredAssertionStatus() {
        ClassLoader c = getClassLoader();
        Object status;
        if (c == null)
            return VMClassLoader.defaultAssertionStatus();
        if (c.classAssertionStatus != null)
            synchronized (c) {
                status = c.classAssertionStatus.get(getName());
                if (status != null) {
                    return status.equals(Boolean.TRUE);
                }
            }
        else {
            if (ClassLoader.StaticData.systemClassAssertionStatus == null) {
                throw new Error("systClassAssertionStatus == null");
            }
            status = ClassLoader.StaticData.systemClassAssertionStatus
                    .get(getName());
            if (status != null) {
                return status.equals(Boolean.TRUE);
            }
        }
        if (c.packageAssertionStatus != null)
            synchronized (c) {
                String name = getPackagePortion(getName());
                if ("".equals(name))
                    status = c.packageAssertionStatus.get(null);
                else
                    do {
                        status = c.packageAssertionStatus.get(name);
                        name = getPackagePortion(name);
                    } while (!"".equals(name) && status == null);
                if (status != null)
                    return status.equals(Boolean.TRUE);
            }
        else {
            String name = getPackagePortion(getName());
            if ("".equals(name))
                status = ClassLoader.StaticData.systemPackageAssertionStatus
                        .get(null);
            else
                do {
                    status = ClassLoader.StaticData.systemPackageAssertionStatus
                            .get(name);
                    name = getPackagePortion(name);
                } while (!"".equals(name) && status == null);
            if (status != null)
                return status.equals(Boolean.TRUE);
        }
        return c.defaultAssertionStatus;
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
        VmType<T> vmType = getLinkedVmClass();
        
        if(vmType.isPrimitive() || vmType.isInterface())
            return null;

        VmType< ? super T> superCls = vmType.getSuperClass();
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
            final ArrayList<Class> list = new ArrayList<Class>();
            final VmType<T> vmClass = getLinkedVmClass();
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
    public boolean isAssignableFrom(Class< ? > c) {
        return getLinkedVmClass().isAssignableFrom(c.getLinkedVmClass());
    }

    /**
     * Returns the simple name for this class, as used in the source
     * code.  For normal classes, this is the content returned by
     * <code>getName()</code> which follows the last ".".  Anonymous
     * classes have no name, and so the result of calling this method is
     * "".  The simple name of an array consists of the simple name of
     * its component type, followed by "[]".  Thus, an array with the
     * component type of an anonymous class has a simple name of simply
     * "[]".
     *
     * @return the simple name for this class.
     * @since 1.5
     */
    public String getSimpleName()
    {
      return getSimpleName(this);
    }

    static String getSimpleName(Class klass) {
        if (klass.getVmClass().isArray()){
	        return klass.getComponentType().getSimpleName() + "[]";
        }
        String fullName = klass.getName();
        return fullName.substring(fullName.lastIndexOf(".") + 1);
    }
    /**
     * Gets the runtime visible annotations of this class.
     */
    public Annotation[] getAnnotations() {
        return vmClass.getAnnotations();
    }

    /**
     * @see java.lang.reflect.AnnotatedElement#getAnnotation(java.lang.Class)
     */
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return vmClass.getAnnotation(annotationClass);
    }

    /**
     * @see java.lang.reflect.AnnotatedElement#getDeclaredAnnotations()
     */
    public Annotation[] getDeclaredAnnotations() {
        return vmClass.getDeclaredAnnotations();
    }

    /**
     * @see java.lang.reflect.AnnotatedElement#isAnnotationPresent(java.lang.Class)
     */
    public boolean isAnnotationPresent(
            Class< ? extends Annotation> annotationClass) {
        return vmClass.isAnnotationPresent(annotationClass);
    }

    /**
     * Gets the classloader used to load this class.
     * 
     * @return ClassLoader
     */
    public final ClassLoader getClassLoader() {
        VmClassLoader loader = vmClass.getLoader();
        return loader.isSystemClassLoader() ? null : loader.asClassLoader();
    }

    /**
     * Create a new instance of this class, using the default constructor
     * 
     * @return Object
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public final T newInstance() throws InstantiationException,
            IllegalAccessException {
        if (defaultConstructor == null) {
            defaultConstructor = getLinkedVmClass().getDeclaredMethod("<init>",
                    "()V");
        }
        if (defaultConstructor == null) {
            throw new InstantiationException("No default constructor");
        }
        try {
            return (T)VmReflection.newInstance(defaultConstructor);
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
    public Field[] getFields()
  {
    //todo enable this: memberAccessCheck(Member.PUBLIC);
    return internalGetFields();
  }

  /**
   * Like <code>getFields()</code> but without the security checks.
   */
  private Field[] internalGetFields()
  {
    HashSet<Field> set = new HashSet<Field>();
    set.addAll(Arrays.asList(getDeclaredFields(true)));
    Class[] interfaces = getInterfaces();
    for (int i = 0; i < interfaces.length; i++)
      set.addAll(Arrays.asList(interfaces[i].internalGetFields()));
    Class superClass = getSuperclass();
    if (superClass != null)
      set.addAll(Arrays.asList(superClass.internalGetFields()));
    return set.toArray(new Field[set.size()]);
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
    public Field[] getDeclaredFields()
  {
    //todo enable this memberAccessCheck(Member.DECLARED);
    return getDeclaredFields(false);
  }

  Field[] getDeclaredFields (boolean publicOnly)
  {
      if (declaredFields == null) {
        final VmType<T> vmClass = getLinkedVmClass();
        final int cnt = vmClass.getNoDeclaredFields();
          final ArrayList<Field> fields = new ArrayList<Field>();
            for (int i = 0; i < cnt; i++) {
                Field field = vmClass.getDeclaredField(i).asField();
                if(field.getDeclaringClass() == this) {//todo we need this check?
                    fields.add(field);
                }
            }
          declaredFields = fields.toArray(new Field[fields.size()]);
      }
      if(publicOnly){
        final ArrayList<Field> fields = new ArrayList<Field>();
        for (Field field : declaredFields) {
            if((field.getModifiers() & Modifier.PUBLIC) != 0) {
                fields.add(field);
            }
        }
          return fields.toArray(new Field[fields.size()]);
      } else {
        //todo fials! return Arrays.copyOf(declaredFields, declaredFields.length);
          return declaredFields;
      }
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
        final VmType<T> vmClass = getLinkedVmClass();
        if (vmClass instanceof VmArrayClass) {
            final VmType< ? > vmCompType = ((VmArrayClass<T>) vmClass)
                    .getComponentType();
            if (vmCompType != null) {
                return vmCompType.asClass();
            }
        }
        return null;
    }

    private static final class MethodKey
  {
    private String name;
    private Class[] params;
    private Class returnType;
    private int hash;

    MethodKey(Method m)
    {
      name = m.getName();
      params = m.getParameterTypes();
      returnType = m.getReturnType();
      hash = name.hashCode() ^ returnType.hashCode();
      for(int i = 0; i < params.length; i++)
	{
	  hash ^= params[i].hashCode();
	}
    }

    public boolean equals(Object o)
    {
      if (o instanceof MethodKey)
	{
	  MethodKey m = (MethodKey) o;
	  if (m.name.equals(name) && m.params.length == params.length
              && m.returnType == returnType)
	    {
	      for (int i = 0; i < params.length; i++)
		{
		  if (m.params[i] != params[i])
		    return false;
		}
	      return true;
	    }
	}
      return false;
    }

    public int hashCode()
    {
      return hash;
    }
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
    public Method getMethod(String name, Class<?>... argTypes)
            throws NoSuchMethodException, SecurityException {
        VmType< ? >[] vmArgTypes;
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
            Method[] a = internalGetMethods();
            ArrayList<Method> list = new ArrayList<Method>();            
            for(int i = 0; i < a.length; i++){
                list.add(a[i]);
            }
            methods = list;
        }
        return methods.toArray(new Method[methods.size()]);
    }
    /*
    public Method[] getMethods() {
        if (methods == null) {
            final ArrayList<Method> list = new ArrayList<Method>();
            Class< ? > cls = this;
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
    }*/

    /**
       * Like <code>getMethods()</code> but without the security checks.
       */
      private Method[] internalGetMethods()
      {
        HashMap map = new HashMap();
        Method[] methods;
        Class[] interfaces = getInterfaces();
        for(int i = 0; i < interfaces.length; i++)
          {
        methods = interfaces[i].internalGetMethods();
        for(int j = 0; j < methods.length; j++)
          {
            map.put(new MethodKey(methods[j]), methods[j]);
          }
          }
        Class superClass = getSuperclass();
        if(superClass != null)
          {
        methods = superClass.internalGetMethods();
        for(int i = 0; i < methods.length; i++)
          {
            map.put(new MethodKey(methods[i]), methods[i]);
          }
          }
        methods = getDeclaredMethods(true);
        for(int i = 0; i < methods.length; i++)
          {
        map.put(new MethodKey(methods[i]), methods[i]);
          }
        return (Method[])map.values().toArray(new Method[map.size()]);
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
    public Method getDeclaredMethod(String name, Class< ? >... argTypes)
            throws NoSuchMethodException, SecurityException {
        VmType< ? >[] vmArgTypes;
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
            declaredMethods = getDeclaredMethods(false);
        }
        return declaredMethods;
    }

    /**
     * Gets all methods declared in this class.
     *
     * @return Method[]
     */
    private Method[] getDeclaredMethods(boolean publicOnly) {
        final VmType<T> vmClass = getLinkedVmClass();
        final int cnt = vmClass.getNoDeclaredMethods();
        int max = 0;
        for (int i = 0; i < cnt; i++) {
            VmMethod method = vmClass.getDeclaredMethod(i);
            if (!method.isConstructor() &&
                    (!publicOnly || method.isPublic())) {
                max++;
            }
        }
        final Method[] list = new Method[max];
        max = 0;
        for (int i = 0; i < cnt; i++) {
            VmMethod vmMethod = vmClass.getDeclaredMethod(i);
            if (!vmMethod.isConstructor() &&
                    (!publicOnly || vmMethod.isPublic())) {
                list[max++] = (Method) vmMethod.asMember();
            }
        }
        return list;
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

    public Constructor<T> getDeclaredConstructor(Class<?>... argTypes)
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
            final VmType<T> vmClass = getLinkedVmClass();
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
     * Returns the <code>Package</code> in which this class is defined Returns
     * null when this information is not available from the classloader of this
     * class or when the classloader of this class is null.
     * 
     * @return the package for this class, if it is available
     * @since 1.2
     */
    public Package getPackage() {
        ClassLoader cl = getClassLoader();
        if (cl != null) {
            return cl.getPackage(getPackagePortion(getName()));
        }
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
    public Constructor<T> getConstructor(Class<?>... argTypes)
            throws NoSuchMethodException {
        // Check security
        memberAccessCheck(Member.PUBLIC);

        // Create signature
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
            ArrayList<Constructor> list = new ArrayList<Constructor>();
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
            sm.checkPermission(GETVMCLASS);
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
     * Gets a primitive class of a given type.
     *
     * @param type
     * @return
     * @see VmType#getPrimitiveClass(char)
     */
    static Class getPrimitiveClass(String type) {
        if(type.equals("double"))
            return getPrimitiveClass('D');
        else if(type.equals("float"))
            return getPrimitiveClass('F');
        else if(type.equals("boolean"))
            return getPrimitiveClass('Z');
        else if(type.equals("byte"))
            return getPrimitiveClass('B');
        else if(type.equals("char"))
            return getPrimitiveClass('C');
        else if(type.equals("short"))
            return getPrimitiveClass('S');
        else if(type.equals("int"))
            return getPrimitiveClass('I');
        else if(type.equals("long"))
            return getPrimitiveClass('J');
        else if(type.equals("void"))
            return getPrimitiveClass('V');
        else
            throw new IllegalArgumentException("Unknown type " + type);                         
    }

    /**
     * Returns the enumeration constants of this class, or null if this class is
     * not an <code>Enum</code>.
     * 
     * @return an array of <code>Enum</code> constants associated with this
     *         class, or null if this class is not an <code>enum</code>.
     */
    @SuppressWarnings("unchecked")
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
     * Return object, cast to this Class' type.
     * 
     * @param obj
     *            the object to cast
     * @throws ClassCastException
     *             if obj is not an instance of this class
     * @since 1.5
     */
    @SuppressWarnings("unchecked")
    public T cast(Object obj) {
        if ((obj != null) && !isInstance(obj)) {
            throw new ClassCastException();
        }
        return (T) obj;
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

    /**
     * @see java.lang.reflect.GenericDeclaration#getTypeParameters()
     */
    public TypeVariable< ? >[] getTypeParameters() {
        String sig = vmClass.getSignature();
        if (sig == null)
          return new TypeVariable[0];

        ClassSignatureParser p = new ClassSignatureParser(this, sig);
        return p.getTypeParameters();
    }

    /**
     * <p>
     * Casts this class to represent a subclass of the specified class. This
     * method is useful for `narrowing' the type of a class so that the class
     * object, and instances of that class, can match the contract of a more
     * restrictive method. For example, if this class has the static type of
     * <code>Class&lt;Object&gt;</code>, and a dynamic type of
     * <code>Class&lt;Rectangle&gt;</code>, then, assuming <code>Shape</code>
     * is a superclass of <code>Rectangle</code>, this method can be used on
     * this class with the parameter, <code>Class&lt;Shape&gt;</code>, to
     * retain the same instance but with the type
     * <code>Class&lt;? extends Shape&gt;</code>.
     * </p>
     * <p>
     * If this class can be converted to an instance which is parameterised over
     * a subtype of the supplied type, <code>U</code>, then this method
     * returns an appropriately cast reference to this object. Otherwise, a
     * <code>ClassCastException</code> is thrown.
     * </p>
     * 
     * @param klass
     *            the class object, the parameterized type (<code>U</code>)
     *            of which should be a superclass of the parameterized type of
     *            this instance.
     * @return a reference to this object, appropriately cast.
     * @throws ClassCastException
     *             if this class can not be converted to one which represents a
     *             subclass of the specified type, <code>U</code>.
     * @since 1.5
     */
    @SuppressWarnings("unchecked")
    public <U> Class< ? extends U> asSubclass(Class<U> klass) {
        if (!klass.isAssignableFrom(this))
            throw new ClassCastException();
        return (Class< ? extends U>) this;
    }

    /**
   * Returns the class which immediately encloses this class.  If this class
   * is a top-level class, this method returns <code>null</code>.
   *
   * @return the immediate enclosing class, or <code>null</code> if this is
   *         a top-level class.
   * @since 1.5
   */
  public Class<?> getEnclosingClass()
  {
    //return VMClass.getEnclosingClass(this);
      //todo implement it
      return null;
  }

  /**
   * Returns the constructor which immediately encloses this class.  If
   * this class is a top-level class, or a local or anonymous class
   * immediately enclosed by a type definition, instance initializer
   * or static initializer, then <code>null</code> is returned.
   *
   * @return the immediate enclosing constructor if this class is
   *         declared within a constructor.  Otherwise, <code>null</code>
   *         is returned.
   * @since 1.5
   */
  public Constructor getEnclosingConstructor()
  {
    //return VMClass.getEnclosingConstructor(this);
      //todo implement it
      return null;
  }

  /**
   * Returns the method which immediately encloses this class.  If
   * this class is a top-level class, or a local or anonymous class
   * immediately enclosed by a type definition, instance initializer
   * or static initializer, then <code>null</code> is returned.
   *
   * @return the immediate enclosing method if this class is
   *         declared within a method.  Otherwise, <code>null</code>
   *         is returned.
   * @since 1.5
   */
  public Method getEnclosingMethod()
  {
    //return VMClass.getEnclosingMethod(this);
      //todo implement it
      return null;
  }

    /**
     * Perform security checks common to all of the methods that get members of
     * this Class.
     */
    private void memberAccessCheck(int which) {
        SecurityManager sm = SecurityManager.current;
        if (sm != null) {
            sm.checkMemberAccess(this, which);
            Package pkg = getPackage();
            if (pkg != null) {
                sm.checkPackageAccess(pkg.getName());
            }
        }
    }

    /**
     * Strip the last portion of the name (after the last dot).
     * 
     * @param name
     *            the name to get package of
     * @return the package name, or "" if no package
     */
    private static String getPackagePortion(String name) {
        int lastInd = name.lastIndexOf('.');
        if (lastInd == -1)
            return "";
        return name.substring(0, lastInd);
    }

    /**
     * Returns true if this class is an <code>Annotation</code>.
     *
     * @return true if this is an annotation class.
     * @since 1.5
     */
    public boolean isAnnotation()
    {
        //todo implement it
        throw new UnsupportedOperationException();
    }

    public String getCanonicalName() {
        //todo implement it
        throw new UnsupportedOperationException();
        /*
      if (vmClass.isArray())
        {
      String componentName = vmClass.getComponentType().getCanonicalName();
      if (componentName != null)
        return componentName + "[]";
        }
      if (vmClass.isMemberClass(klass))
        {
      String memberName = getDeclaringClass(klass).getCanonicalName();
      if (memberName != null)
        return memberName + "." + getSimpleName(klass);
        }
      if (isLocalClass(klass) || vmClass.isAnonymousClass(klass))
        return null;
      return getName(klass);
      */
    }

    /**
   * <p>
   * Returns a <code>Type</code> object representing the direct superclass,
   * whether class, interface, primitive type or void, of this class.
   * If this class is an array class, then a class instance representing
   * the <code>Object</code> class is returned.  If this class is primitive,
   * an interface, or a representation of either the <code>Object</code>
   * class or void, then <code>null</code> is returned.
   * </p>
   * <p>
   * If the superclass is a parameterized type, then the
   * object returned for this interface reflects the actual type
   * parameters used in the source code.  Type parameters are created
   * using the semantics specified by the <code>ParameterizedType</code>
   * interface, and only if an instance has not already been created.
   * </p>
   *
   * @return the superclass of this class.
   * @throws GenericSignatureFormatError if the generic signature of the
   *         class does not comply with that specified by the Java
   *         Virtual Machine specification, 3rd edition.
   * @throws TypeNotPresentException if the superclass refers
   *         to a non-existant type.
   * @throws MalformedParameterizedTypeException if the superclass
   *         refers to a parameterized type that can not be instantiated for
   *         some reason.
   * @since 1.5
   * @see java.lang.reflect.ParameterizedType
   */
  public Type getGenericSuperclass()
  {
    if (isArray())
      return Object.class;

    if (isPrimitive() || isInterface() || this == Object.class)
      return null;

    String sig = vmClass.getSignature();
    if (sig == null)
      return getSuperclass();

    ClassSignatureParser p = new ClassSignatureParser(this, sig);
    return p.getSuperclassType();
  }

    /**
     * <p>
     * Returns an array of <code>Type</code> objects which represent the
     * interfaces directly implemented by this class or extended by this
     * interface.
     * </p>
     * <p>
     * If one of the superinterfaces is a parameterized type, then the
     * object returned for this interface reflects the actual type
     * parameters used in the source code.  Type parameters are created
     * using the semantics specified by the <code>ParameterizedType</code>
     * interface, and only if an instance has not already been created.
     * </p>
     * <p>
     * The order of the interfaces in the array matches the order in which
     * the interfaces are declared.  For classes which represent an array,
     * an array of two interfaces, <code>Cloneable</code> and
     * <code>Serializable</code>, is always returned, with the objects in
     * that order.  A class representing a primitive type or void always
     * returns an array of zero size.
     * </p>
     *
     * @return an array of interfaces implemented or extended by this class.
     * @throws GenericSignatureFormatError if the generic signature of one
     *         of the interfaces does not comply with that specified by the Java
     *         Virtual Machine specification, 3rd edition.
     * @throws TypeNotPresentException if any of the superinterfaces refers
     *         to a non-existant type.
     * @throws MalformedParameterizedTypeException if any of the interfaces
     *         refer to a parameterized type that can not be instantiated for
     *         some reason.
     * @since 1.5
     * @see java.lang.reflect.ParameterizedType
     */
    public Type[] getGenericInterfaces()
    {
      if (isPrimitive())
        return new Type[0];

      String sig = vmClass.getSignature();
      if (sig == null)
        return getInterfaces();

      ClassSignatureParser p = new ClassSignatureParser(this, sig);
      return p.getInterfaceTypes();
    }

    //jnode openjdk
    /**
     * Returns the elements of this enum class or null if this
     * Class object does not represent an enum type;
     * identical to getEnumConstantsShared except that
     * the result is uncloned, cached, and shared by all callers.
     */
    T[] getEnumConstantsShared() {
        if (enumConstants == null) {
            if (!isEnum()) return null;
            try {
                final Method values = getMethod("values");
                java.security.AccessController.doPrivileged
                        (new java.security.PrivilegedAction() {
                            public Object run() {
                                values.setAccessible(true);
                                return null;
                            }
                        });
                enumConstants = (T[]) values.invoke(null);
            }
            // These can happen when users concoct enum-like classes
            // that don't comply with the enum spec.
            catch (InvocationTargetException ex) {
                return null;
            }
            catch (NoSuchMethodException ex) {
                return null;
            }
            catch (IllegalAccessException ex) {
                return null;
            }
        }
        return enumConstants;
    }
    private volatile transient T[] enumConstants;


    /**
     * Returns a map from simple name to enum constant.  This package-private
     * method is used internally by Enum to implement
     * public static <T extends Enum<T>> T valueOf(Class<T>, String)
     * efficiently.  Note that the map is returned by this method is
     * created lazily on first use.  Typically it won't ever get created.
     */
    HashMap<String, T> enumConstantDirectory() {
        if (enumConstantDirectory == null) {
            T[] universe = getEnumConstantsShared();
            if (universe == null)
                throw new IllegalArgumentException(
                        getName() + " is not an enum type");
            HashMap<String, T> m = new HashMap<String, T>(2 * universe.length);
            for (T constant : universe)
                m.put(((Enum) constant).name(), constant);
            enumConstantDirectory = m;
        }
        return enumConstantDirectory;
    }
    private volatile transient HashMap enumConstantDirectory;

    ClassLoader getClassLoader0(){
        return getClassLoader();
    }

    /**
     * Returns <tt>true</tt> if and only if the underlying class
     * is an anonymous class.
     *
     * @return <tt>true</tt> if and only if this class is an anonymous class.
     * @since 1.5
     */
    public boolean isAnonymousClass() {
	    return "".equals(getSimpleName());
    }

    /**
     * Returns <tt>true</tt> if and only if the underlying class
     * is a local class.
     *
     * @return <tt>true</tt> if and only if this class is a local class.
     * @since 1.5
     */
    public boolean isLocalClass() {
	return isLocalOrAnonymousClass() && !isAnonymousClass();
    }

    /**
     * Returns <tt>true</tt> if this is a local class or an anonymous
     * class.  Returns <tt>false</tt> otherwise.
     */
    private boolean isLocalOrAnonymousClass() {
	// JVM Spec 4.8.6: A class must have an EnclosingMethod
	// attribute if and only if it is a local class or an
	// anonymous class.
	return getEnclosingMethodInfo() != null;
    }

    /*
    private EnclosingMethodInfo getEnclosingMethodInfo() {
	Object[] enclosingInfo = getEnclosingMethod0();
	if (enclosingInfo == null)
	    return null;
	else {
	    return new EnclosingMethodInfo(enclosingInfo);
	}
    } */

    private EnclosingMethodInfo getEnclosingMethodInfo() {
        //todo implement it
        return null;
    }

    public boolean isMemberClass() {
	return getSimpleBinaryName() != null && !isLocalOrAnonymousClass();
    }

    /**
     * Returns the "simple binary name" of the underlying class, i.e.,
     * the binary name without the leading enclosing class name.
     * Returns <tt>null</tt> if the underlying class is a top level
     * class.
     */
    private String getSimpleBinaryName() {
	Class<?> enclosingClass = getEnclosingClass();
	if (enclosingClass == null) // top level class
	    return null;
	// Otherwise, strip the enclosing class' name
	try {
	    return getName().substring(enclosingClass.getName().length());
	} catch (IndexOutOfBoundsException ex) {
	    throw new InternalError("Malformed class name");
	}
    }

    private final static class EnclosingMethodInfo {
	private Class<?> enclosingClass;
	private String name;
	private String descriptor;

	private EnclosingMethodInfo(Object[] enclosingInfo) {
	    if (enclosingInfo.length != 3)
		throw new InternalError("Malformed enclosing method information");
	    try {
		// The array is expected to have three elements:

		// the immediately enclosing class
		enclosingClass = (Class<?>) enclosingInfo[0];
		assert(enclosingClass != null);

		// the immediately enclosing method or constructor's
		// name (can be null).
		name		= (String)   enclosingInfo[1];

		// the immediately enclosing method or constructor's
		// descriptor (null iff name is).
		descriptor	= (String)   enclosingInfo[2];
		assert((name != null && descriptor != null) || name == descriptor);
	    } catch (ClassCastException cce) {
		throw new InternalError("Invalid type in enclosing method information");
	    }
	}

	boolean isPartial() {
	    return enclosingClass == null || name == null || descriptor == null;
	}

	boolean isConstructor() { return !isPartial() && "<init>".equals(name); }

	boolean isMethod() { return !isPartial() && !isConstructor() && !"<clinit>".equals(name); }

	Class<?> getEnclosingClass() { return enclosingClass; }

	String getName() { return name; }

	String getDescriptor() { return descriptor; }

    }
}
