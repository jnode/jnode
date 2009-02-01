/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
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
 
package java.lang;

import sun.reflect.ConstantPool;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.security.Permissions;
import java.security.AllPermission;
import java.util.ArrayList;
import org.jnode.vm.VmSystem;
import org.jnode.vm.SoftByteCodes;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.classmgr.VmClassLoader;
import org.jnode.vm.classmgr.VmArrayClass;
import org.jnode.vm.classmgr.VmMethod;

/**
 * @see java.lang.Class
 * 
 * @author Levente S\u00e1ntha
 */
class NativeClass {
    /**
     * @see java.lang.Class#registerNatives()
     */
    private static void registerNatives() {

    }
    /**
     * @see java.lang.Class#forName0(java.lang.String, boolean, java.lang.ClassLoader)
     */
    private static Class forName0(String arg1, boolean arg2, ClassLoader arg3) throws ClassNotFoundException {
        return (arg3 == null) ? VmSystem.forName(arg1) : arg3.loadClass(arg1, arg2);
    }
    /**
     * @see java.lang.Class#isInstance(java.lang.Object)
     */
    private static boolean isInstance(Class instance, Object arg1) {
        return SoftByteCodes.isInstanceof(arg1, getLinkedVmClass(instance));
    }
    /**
     * @see java.lang.Class#isAssignableFrom(java.lang.Class)
     */
    private static boolean isAssignableFrom(Class instance, Class arg1) {
        return getLinkedVmClass(instance).isAssignableFrom(getLinkedVmClass(arg1));
    }
    /**
     * @see java.lang.Class#isInterface()
     */
    private static boolean isInterface(Class instance) {
         return VmType.fromClass(instance).isInterface();
    }
    /**
     * @see java.lang.Class#isArray()
     */
    private static boolean isArray(Class instance) {
        return VmType.fromClass(instance).isArray();
    }
    /**
     * @see java.lang.Class#isPrimitive()
     */
    private static boolean isPrimitive(Class instance) {
        return VmType.fromClass(instance).isPrimitive();
    }
    /**
     * @see java.lang.Class#getName0()
     */
    private static String getName0(Class instance) {
        return VmType.fromClass(instance).getName().replace('/', '.');
    }
    /**
     * @see java.lang.Class#getClassLoader0()
     */
    private static ClassLoader getClassLoader0(Class instance) {
        VmClassLoader loader = VmType.fromClass(instance).getLoader();
        return loader.isSystemClassLoader() ? null : loader.asClassLoader();
    }
    /**
     * @see java.lang.Class#getSuperclass()
     */
    private static <T> Class getSuperclass(Class instance) {
        VmType<T> vmType = getLinkedVmClass(instance);

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
     * @see java.lang.Class#getInterfaces()
     */
    private static <T> Class[] getInterfaces(Class instance) {
//        if (interfaces == null) {
            final ArrayList<Class> list = new ArrayList<Class>();
            final VmType<T> vmClass = getLinkedVmClass(instance);
            final int cnt = vmClass.getNoInterfaces();
            for (int i = 0; i < cnt; i++) {
                list.add(vmClass.getInterface(i).asClass());
            }
//            interfaces = list;
//        }
//        return (Class[]) interfaces.toArray(new Class[interfaces.size()]);
        return list.toArray(new Class[list.size()]);
    }
    /**
     * @see java.lang.Class#getComponentType()
     */
    private static <T> Class getComponentType(Class instance) {
        final VmType<T> vmClass = getLinkedVmClass(instance);
        if (vmClass instanceof VmArrayClass) {
            final VmType< ? > vmCompType = ((VmArrayClass<T>) vmClass)
                    .getComponentType();
            if (vmCompType != null) {
                return vmCompType.asClass();
            }
        }
        return null;
    }
    /**
     * @see java.lang.Class#getModifiers()
     */
    private static int getModifiers(Class instance) {
        return VmType.fromClass(instance).getAccessFlags();
    }
    /**
     * @see java.lang.Class#getSigners()
     */
    private static Object[] getSigners(Class instance) {
        //todo implement it
        return null;
    }
    /**
     * @see java.lang.Class#setSigners(java.lang.Object[])
     */
    private static void setSigners(Class instance, Object[] arg1) {
        //todo implement it
    }
    /**
     * @see java.lang.Class#getEnclosingMethod0()
     */
    private static Object[] getEnclosingMethod0(Class instance) {
        //todo implement it
        return null;
    }
    /**
     * @see java.lang.Class#getDeclaringClass()
     */
    private static Class getDeclaringClass(Class instance) {
        //todo implement it
        return null;
    }
    /**
     * @see java.lang.Class#getProtectionDomain0()
     */
    private static ProtectionDomain getProtectionDomain0(Class instance) {
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("getProtectionDomain"));
        }
        final ProtectionDomain pd = getLinkedVmClass(instance).getProtectionDomain();
        if (pd != null) {
            return pd;
        } else {
            return getUnknownProtectionDomain();
        }
    }
    /**
     * @see java.lang.Class#setProtectionDomain0(java.security.ProtectionDomain)
     */
    private static void setProtectionDomain0(Class instance, ProtectionDomain arg1) {
        //todo implement it
    }
    /**
     * @see java.lang.Class#getPrimitiveClass(java.lang.String)
     */
    private static Class getPrimitiveClass(String type) {
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
     * @see java.lang.Class#getGenericSignature()
     */
    private static String getGenericSignature(Class instance) {
        return VmType.fromClass(instance).getSignature();
    }
    /**
     * @see java.lang.Class#getRawAnnotations()
     */
    private static byte[] getRawAnnotations(Class instance) {
        //todo implement it
        return null;
    }
    /**
     * @see java.lang.Class#getConstantPool()
     */
    private static ConstantPool getConstantPool(Class instance) {
        //todo implement it
        return null;
    }
    /**
     * @see java.lang.Class#getDeclaredFields0(boolean)
     */
    private static <T> Field[] getDeclaredFields0(Class instance, boolean publicOnly) {
        //todo optimize , simplify
        Field[] declaredFields;
        {
            final VmType<T> vmClass = getLinkedVmClass(instance);
            final int cnt = vmClass.getNoDeclaredFields();
            final ArrayList<Field> fields = new ArrayList<Field>();
            for (int i = 0; i < cnt; i++) {
                Field field = vmClass.getDeclaredField(i).asField();
                //if (field.getDeclaringClass() == instance) {//todo we need this check?
                    fields.add(field);
                //}
            }

            declaredFields = fields.toArray(new Field[fields.size()]);
        }
        if (publicOnly) {
            final ArrayList<Field> fields = new ArrayList<Field>();
            for (Field field : declaredFields) {
                if ((field.getModifiers() & Modifier.PUBLIC) != 0) {
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
     * @see java.lang.Class#getDeclaredMethods0(boolean)
     */
    private static <T> Method[] getDeclaredMethods0(Class instance, boolean publicOnly) {
        final VmType<T> vmClass = getLinkedVmClass(instance);
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
     * @see java.lang.Class#getDeclaredConstructors0(boolean)
     */
    private static <T> Constructor[] getDeclaredConstructors0(Class instance, boolean arg1) {
        //todo fix public only !!!
            final VmType<T> vmClass = getLinkedVmClass(instance);
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
        return list;
    }
    /**
     * @see java.lang.Class#getDeclaredClasses0()
     */
    private static Class[] getDeclaredClasses0(Class instance) {
        //todo implement it
        return new Class[0];
    }
    /**
     * @see java.lang.Class#desiredAssertionStatus0(java.lang.Class)
     */
    private static boolean desiredAssertionStatus0(Class arg1) {
        //todo implement it
        return false;
    }

    private static <T> VmType<T> getLinkedVmClass(Class clazz) {
        final VmType vmClass = VmType.fromClass(clazz);
        vmClass.link();
        return vmClass;
    }

    private static ProtectionDomain unknownProtectionDomain;
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

    static Class getPrimitiveClass(char type) {
        return VmType.getPrimitiveClass(type).asClass();
    }
}
