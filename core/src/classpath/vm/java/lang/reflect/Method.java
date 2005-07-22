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
 
package java.lang.reflect;

import java.lang.annotation.Annotation;
import java.util.ArrayList;

import org.jnode.vm.VmReflection;
import org.jnode.vm.classmgr.VmExceptions;
import org.jnode.vm.classmgr.VmMethod;

/**
 * The Method class represents a member method of a class. It also allows
 * dynamic invocation, via reflection. This works for both static and
 * instance methods. Invocation on Method objects knows how to do
 * widening conversions, but throws {@link IllegalArgumentException} if
 * a narrowing conversion would be necessary. You can query for information
 * on this Method regardless of location, but invocation access may be limited
 * by Java language access controls. If you can't do it in the compiler, you
 * can't normally do it here either.<p>
 *
 * <B>Note:</B> This class returns and accepts types as Classes, even
 * primitive types; there are Class types defined that represent each
 * different primitive type.  They are <code>java.lang.Boolean.TYPE,
 * java.lang.Byte.TYPE,</code>, also available as <code>boolean.class,
 * byte.class</code>, etc.  These are not to be confused with the
 * classes <code>java.lang.Boolean, java.lang.Byte</code>, etc., which are
 * real classes.<p>
 *
 * Also note that this is not a serializable class.  It is entirely feasible
 * to make it serializable using the Externalizable interface, but this is
 * on Sun, not me.
 *
 * @author John Keiser
 * @author Eric Blake <ebb9@email.byu.edu>
 * @see Member
 * @see Class
 * @see java.lang.Class#getMethods()
 * @see java.lang.Class#getDeclaredMethods()
 * @since 1.1
 * @status updated to 1.4
 */
public final class Method extends AccessibleObject implements Member, AnnotatedElement {
	
	private VmMethod vmMethod;
	private ArrayList<Class> parameterTypes;
	private ArrayList<Class> exceptionTypes;	

	/**
	 * This class is uninstantiable.
	 */
	public Method(VmMethod vmMethod) {
		this.vmMethod = vmMethod;
	}

	/**
	 * Gets the class that declared this method, or the class where this method
	 * is a non-inherited member.
	 * @return the class that declared this member
	 */
	public Class getDeclaringClass() {
		return vmMethod.getDeclaringClass().asClass();
	}

	/**
	 * Gets the name of this method.
	 * @return the name of this method
	 */
	public String getName() {
		return vmMethod.getName();
	}

	/**
	 * Gets the modifiers this method uses.  Use the <code>Modifier</code>
	 * class to interpret the values.  A method can only have a subset of the
	 * following modifiers: public, private, protected, abstract, static,
	 * final, synchronized, native, and strictfp.
	 *
	 * @return an integer representing the modifiers to this Member
	 * @see Modifier
	 */
	public int getModifiers() {
		return vmMethod.getModifiers();
	}

	/**
	 * Gets the return type of this method.
	 * @return the type of this method
	 */
	public Class getReturnType() {
		return vmMethod.getReturnType().asClass();
	}

	/**
	 * Get the parameter list for this method, in declaration order. If the
	 * method takes no parameters, returns a 0-length array (not null).
	 *
	 * @return a list of the types of the method's parameters
	 */
	public Class[] getParameterTypes() {
		if (parameterTypes == null) {
			int cnt = vmMethod.getNoArguments();
			ArrayList<Class> list = new ArrayList<Class>(cnt);
			for (int i = 0; i < cnt; i++) {
				list.add(vmMethod.getArgumentType(i).asClass());
			}
			parameterTypes = list;
		}
		return (Class[])parameterTypes.toArray(new Class[parameterTypes.size()]);
	}

	/**
	 * Get the exception types this method says it throws, in no particular
	 * order. If the method has no throws clause, returns a 0-length array
	 * (not null).
	 *
	 * @return a list of the types in the method's throws clause
	 */
	public Class[] getExceptionTypes() {
		if (exceptionTypes == null) {
			final VmExceptions exceptions = vmMethod.getExceptions();
			final int cnt = exceptions.getLength();
			final ArrayList<Class> list = new ArrayList<Class>(cnt);
			for (int i = 0; i < cnt; i++) {
        try
        {
          list.add(exceptions.getException(i).getResolvedVmClass().asClass());
        }
        catch (Exception e)
        {
          // there is some missing getException(i).getResolvedVmClass()
          // if one makes a system.out on the methods it fails, looks "java.lang.Exception" is missing
          // I think I'll look into it, if I can.
          // 10/03/2005 Martin Husted Hartvig
        }
      }
			exceptionTypes = list;
		}
		return (Class[])exceptionTypes.toArray(new Class[exceptionTypes.size()]);
	}

	/**
	 * Compare two objects to see if they are semantically equivalent.
	 * Two Methods are semantically equivalent if they have the same declaring
	 * class, name, and parameter list.  This ignores different exception
	 * clauses or return types.
	 *
	 * @param o the object to compare to
	 * @return <code>true</code> if they are equal; <code>false</code> if not
	 */
	public boolean equals(Object o) {
		return (this == o);
	}

	/**
	 * Get the hash code for the Method. The Method hash code is the hash code
	 * of its name XOR'd with the hash code of its class name.
	 *
	 * @return the hash code for the object
	 */
	public int hashCode() {
        final Class<?> declClass = getDeclaringClass();
		return declClass.getName().hashCode() ^ getName().hashCode();
	}

	/**
	 * Get a String representation of the Method. A Method's String
	 * representation is "&lt;modifiers&gt; &lt;returntype&gt;
	 * &lt;methodname&gt;(&lt;paramtypes&gt;) throws &lt;exceptions&gt;", where
	 * everything after ')' is omitted if there are no exceptions.<br> Example:
	 * <code>public static int run(java.lang.Runnable,int)</code>
	 *
	 * @return the String representation of the Method
	 */
	public String toString() {
		// 128 is a reasonable buffer initial size for constructor
		StringBuffer sb = new StringBuffer(128);
		Modifier.toString(getModifiers(), sb).append(' ');
        final Class<?> retType = getReturnType();
        final Class<?> declClass = getDeclaringClass();
		sb.append(retType.getName()).append(' ');
		sb.append(declClass.getName()).append('.');
		sb.append(getName()).append('(');
		Class<?>[] c = getParameterTypes();
		if (c.length > 0) {
			sb.append(c[0].getName());
			for (int i = 1; i < c.length; i++)
				sb.append(',').append(c[i].getName());
		}
		sb.append(')');
		c = getExceptionTypes();
		if (c.length > 0) {
			sb.append(" throws ").append(c[0].getName());
			for (int i = 1; i < c.length; i++)
				sb.append(',').append(c[i].getName());
		}
		return sb.toString();
	}

	/**
	 * Invoke the method. Arguments are automatically unwrapped and widened,
	 * and the result is automatically wrapped, if needed.<p>
	 *
	 * If the method is static, <code>o</code> will be ignored. Otherwise,
	 * the method uses dynamic lookup as described in JLS 15.12.4.4. You cannot
	 * mimic the behavior of nonvirtual lookup (as in super.foo()). This means
	 * you will get a <code>NullPointerException</code> if <code>o</code> is
	 * null, and an <code>IllegalArgumentException</code> if it is incompatible
	 * with the declaring class of the method. If the method takes 0 arguments,
	 * you may use null or a 0-length array for <code>args</code>.<p>
	 *
	 * Next, if this Method enforces access control, your runtime context is
	 * evaluated, and you may have an <code>IllegalAccessException</code> if
	 * you could not acces this method in similar compiled code. If the method
	 * is static, and its class is uninitialized, you trigger class
	 * initialization, which may end in a
	 * <code>ExceptionInInitializerError</code>.<p>
	 *
	 * Finally, the method is invoked. If it completes normally, the return value
	 * will be null for a void method, a wrapped object for a primitive return
	 * method, or the actual return of an Object method. If it completes
	 * abruptly, the exception is wrapped in an
	 * <code>InvocationTargetException</code>.
	 *
	 * @param o the object to invoke the method on
	 * @param args the arguments to the method
	 * @return the return value of the method, wrapped in the appropriate
	 *         wrapper if it is primitive
	 * @throws IllegalAccessException if the method could not normally be called
	 *         by the Java code (i.e. it is not public)
	 * @throws IllegalArgumentException if the number of arguments is incorrect;
	 *         if the arguments types are wrong even with a widening conversion;
	 *         or if <code>o</code> is not an instance of the class or interface
	 *         declaring this method
	 * @throws InvocationTargetException if the method throws an exception
	 * @throws NullPointerException if <code>o</code> is null and this field
	 *         requires an instance
	 * @throws ExceptionInInitializerError if accessing a static method triggered
	 *         class initialization, which then failed
	 */
	public Object invoke(Object o, Object[] args)
		throws IllegalAccessException, InvocationTargetException {
		return VmReflection.invoke(vmMethod, o, args);
	}

    /**
     * @see java.lang.reflect.AnnotatedElement#getAnnotation(java.lang.Class)
     */
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return vmMethod.getAnnotation(annotationClass);
    }

    /**
     * @see java.lang.reflect.AnnotatedElement#getAnnotations()
     */
    public Annotation[] getAnnotations() {
        return vmMethod.getAnnotations();
    }

    /**
     * @see java.lang.reflect.AnnotatedElement#getDeclaredAnnotations()
     */
    public Annotation[] getDeclaredAnnotations() {
        return vmMethod.getDeclaredAnnotations();
    }

    /**
     * @see java.lang.reflect.AnnotatedElement#isAnnotationPresent(java.lang.Class)
     */
    public boolean isAnnotationPresent(Class< ? extends Annotation> annotationClass) {
        return vmMethod.isAnnotationPresent(annotationClass);
    }       
}
