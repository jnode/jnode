/*
 * Copyright 2000-2007 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */
package java.beans;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.sun.beans.finder.ClassFinder;
import sun.reflect.misc.MethodUtil;

/**
 * A <code>Statement</code> object represents a primitive statement
 * in which a single method is applied to a target and
 * a set of arguments - as in <code>"a.setFoo(b)"</code>.
 * Note that where this example uses names
 * to denote the target and its argument, a statement
 * object does not require a name space and is constructed with
 * the values themselves.
 * The statement object associates the named method
 * with its environment as a simple set of values:
 * the target and an array of argument values.
 *
 * @since 1.4
 *
 * @author Philip Milne
 */
public class Statement {

    private static Object[] emptyArray = new Object[]{};

    static ExceptionListener defaultExceptionListener = new ExceptionListener() {
        public void exceptionThrown(Exception e) {
            System.err.println(e);
            // e.printStackTrace();
            System.err.println("Continuing ...");
        }
    };

    Object target;
    String methodName;
    Object[] arguments;

    /**
     * Creates a new <code>Statement</code> object with a <code>target</code>,
     * <code>methodName</code> and <code>arguments</code> as per the parameters.
     *
     * @param target The target of this statement.
     * @param methodName The methodName of this statement.
     * @param arguments The arguments of this statement. If <code>null</code> then an empty array will be used.
     *
     */
    public Statement(Object target, String methodName, Object[] arguments) {
	this.target = target;
        this.methodName = methodName;
        this.arguments = (arguments == null) ? emptyArray : arguments;
    }

    /**
     * Returns the target of this statement.
     *
     * @return The target of this statement.
     */
    public Object getTarget() {
        return target;
    }

    /**
     * Returns the name of the method.
     *
     * @return The name of the method.
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Returns the arguments of this statement.
     *
     * @return the arguments of this statement.
     */
    public Object[] getArguments() {
        return arguments;
    }

    /**
     * The execute method finds a method whose name is the same
     * as the methodName property, and invokes the method on
     * the target.
     *
     * When the target's class defines many methods with the given name
     * the implementation should choose the most specific method using
     * the algorithm specified in the Java Language Specification
     * (15.11). The dynamic class of the target and arguments are used
     * in place of the compile-time type information and, like the
     * <code>java.lang.reflect.Method</code> class itself, conversion between
     * primitive values and their associated wrapper classes is handled
     * internally.
     * <p>
     * The following method types are handled as special cases:
     * <ul>
     * <li>
     * Static methods may be called by using a class object as the target.
     * <li>
     * The reserved method name "new" may be used to call a class's constructor
     * as if all classes defined static "new" methods. Constructor invocations
     * are typically considered <code>Expression</code>s rather than <code>Statement</code>s
     * as they return a value.
     * <li>
     * The method names "get" and "set" defined in the <code>java.util.List</code>
     * interface may also be applied to array instances, mapping to
     * the static methods of the same name in the <code>Array</code> class.
     * </ul>
     */
    public void execute() throws Exception {
        invoke();
    }

    Object invoke() throws Exception {
        Object target = getTarget();
        String methodName = getMethodName();

	if (target == null || methodName == null) {
	    throw new NullPointerException((target == null ? "target" : 
					    "methodName") + " should not be null");
	}

        Object[] arguments = getArguments();
        // Class.forName() won't load classes outside
        // of core from a class inside core. Special
        // case this method.
        if (target == Class.class && methodName.equals("forName")) {
            return ClassFinder.resolveClass((String)arguments[0]);
        }
        Class[] argClasses = new Class[arguments.length];
        for(int i = 0; i < arguments.length; i++) {
            argClasses[i] = (arguments[i] == null) ? null : arguments[i].getClass();
        }

        AccessibleObject m = null;
        if (target instanceof Class) {
            /*
            For class methods, simluate the effect of a meta class
            by taking the union of the static methods of the
            actual class, with the instance methods of "Class.class"
            and the overloaded "newInstance" methods defined by the
            constructors.
            This way "System.class", for example, will perform both
            the static method getProperties() and the instance method
            getSuperclass() defined in "Class.class".
            */
            if (methodName.equals("new")) {
                methodName = "newInstance";
            }
            // Provide a short form for array instantiation by faking an nary-constructor. 
            if (methodName.equals("newInstance") && ((Class)target).isArray()) {
                Object result = Array.newInstance(((Class)target).getComponentType(), arguments.length); 
                for(int i = 0; i < arguments.length; i++) { 
                    Array.set(result, i, arguments[i]); 
                }
                return result; 
            }
            if (methodName.equals("newInstance") && arguments.length != 0) {
                // The Character class, as of 1.4, does not have a constructor
                // which takes a String. All of the other "wrapper" classes
                // for Java's primitive types have a String constructor so we
                // fake such a constructor here so that this special case can be
                // ignored elsewhere.
                if (target == Character.class && arguments.length == 1 && 
		    argClasses[0] == String.class) {
                    return new Character(((String)arguments[0]).charAt(0));
                }
		m = ReflectionUtils.getConstructor((Class)target, argClasses);
            }
            if (m == null && target != Class.class) {
                m = ReflectionUtils.getMethod((Class)target, methodName, argClasses);
            }
            if (m == null) {
		m = ReflectionUtils.getMethod(Class.class, methodName, argClasses);
            }
        }
        else {
            /*
            This special casing of arrays is not necessary, but makes files
            involving arrays much shorter and simplifies the archiving infrastrcure.
            The Array.set() method introduces an unusual idea - that of a static method
            changing the state of an instance. Normally statements with side
            effects on objects are instance methods of the objects themselves
            and we reinstate this rule (perhaps temporarily) by special-casing arrays.
            */
            if (target.getClass().isArray() && 
		(methodName.equals("set") || methodName.equals("get"))) {
                int index = ((Integer)arguments[0]).intValue();
                if (methodName.equals("get")) {
                    return Array.get(target, index);
                }
                else {
                    Array.set(target, index, arguments[1]);
                    return null;
                }
            }
            m = ReflectionUtils.getMethod(target.getClass(), methodName, argClasses);
        }
        if (m != null) {
            try {
                if (m instanceof Method) {
                    return MethodUtil.invoke((Method)m, target, arguments);
		}
                else {
                    return ((Constructor)m).newInstance(arguments);
                }
            }
            catch (IllegalAccessException iae) {
                throw new Exception("Statement cannot invoke: " + 
				    methodName + " on " + target.getClass(),
				    iae);
            }
            catch (InvocationTargetException ite) {
                Throwable te = ite.getTargetException();
                if (te instanceof Exception) {
                    throw (Exception)te;
                }
                else {
                    throw ite;
                }
            }
        }
        throw new NoSuchMethodException(toString());
    }

    String instanceName(Object instance) { 
	if (instance == null) {
	    return "null";
	} else if (instance.getClass() == String.class) {
	    return "\""+(String)instance + "\"";
	} else {
	    // Note: there is a minor problem with using the non-caching
	    // NameGenerator method. The return value will not have 
	    // specific information about the inner class name. For example,
	    // In 1.4.2 an inner class would be represented as JList$1 now
	    // would be named Class.

	    return NameGenerator.unqualifiedClassName(instance.getClass());
	}
    }

    /**
     * Prints the value of this statement using a Java-style syntax.
     */
    public String toString() {
        // Respect a subclass's implementation here.
        Object target = getTarget();
        String methodName = getMethodName();
        Object[] arguments = getArguments();

        StringBuffer result = new StringBuffer(instanceName(target) + "." + methodName + "(");
        int n = arguments.length;
        for(int i = 0; i < n; i++) {
            result.append(instanceName(arguments[i]));
            if (i != n -1) {
                result.append(", ");
            }
        }
        result.append(");");
        return result.toString();
    }
}
