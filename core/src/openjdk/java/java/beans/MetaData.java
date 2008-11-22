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

import java.awt.AWTKeyStroke;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.font.TextAttribute;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.security.AccessController;
import java.security.PrivilegedAction;

import java.sql.Timestamp;

import java.util.*;

import javax.swing.Box;
import javax.swing.JLayeredPane;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.ColorUIResource;

import sun.swing.PrintColorUIResource;

/*
 * Like the <code>Intropector</code>, the <code>MetaData</code> class
 * contains <em>meta</em> objects that describe the way
 * classes should express their state in terms of their
 * own public APIs.
 *
 * @see java.beans.Intropector
 *
 * @author Philip Milne
 * @author Steve Langley
 */

class NullPersistenceDelegate extends PersistenceDelegate {
    // Note this will be called by all classes when they reach the
    // top of their superclass chain.
    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
    }
    protected Expression instantiate(Object oldInstance, Encoder out) { return null; }

    public void writeObject(Object oldInstance, Encoder out) {
    // System.out.println("NullPersistenceDelegate:writeObject " + oldInstance);
    }
}

/**
 * The persistence delegate for <CODE>enum</CODE> classes.
 *
 * @author Sergey A. Malenkov
 */
class EnumPersistenceDelegate extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return oldInstance == newInstance;
    }

    protected Expression instantiate(Object oldInstance, Encoder out) {
        Enum e = (Enum) oldInstance;
        return new Expression(e, Enum.class, "valueOf", new Object[]{e.getClass(), e.name()});
    }
}

class PrimitivePersistenceDelegate extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return oldInstance.equals(newInstance);
    }

    protected Expression instantiate(Object oldInstance, Encoder out) {
        return new Expression(oldInstance, oldInstance.getClass(),
                  "new", new Object[]{oldInstance.toString()});
    }
}

class ArrayPersistenceDelegate extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return (newInstance != null &&
                oldInstance.getClass() == newInstance.getClass() && // Also ensures the subtype is correct.
                Array.getLength(oldInstance) == Array.getLength(newInstance));
        }

    protected Expression instantiate(Object oldInstance, Encoder out) {
        // System.out.println("instantiate: " + type + " " + oldInstance);
        Class oldClass = oldInstance.getClass();
        return new Expression(oldInstance, Array.class, "newInstance",
                   new Object[]{oldClass.getComponentType(),
                                new Integer(Array.getLength(oldInstance))});
        }

    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        int n = Array.getLength(oldInstance);
        for (int i = 0; i < n; i++) {
            Object index = new Integer(i);
            // Expression oldGetExp = new Expression(Array.class, "get", new Object[]{oldInstance, index});
            // Expression newGetExp = new Expression(Array.class, "get", new Object[]{newInstance, index});
            Expression oldGetExp = new Expression(oldInstance, "get", new Object[]{index});
            Expression newGetExp = new Expression(newInstance, "get", new Object[]{index});
            try {
                Object oldValue = oldGetExp.getValue();
                Object newValue = newGetExp.getValue();
                out.writeExpression(oldGetExp);
                if (!MetaData.equals(newValue, out.get(oldValue))) {
                    // System.out.println("Not equal: " + newGetExp + " != " + actualGetExp);
                    // invokeStatement(Array.class, "set", new Object[]{oldInstance, index, oldValue}, out);
                    DefaultPersistenceDelegate.invokeStatement(oldInstance, "set", new Object[]{index, oldValue}, out);
                }
            }
            catch (Exception e) {
                // System.err.println("Warning:: failed to write: " + oldGetExp);
                out.getExceptionListener().exceptionThrown(e);
            }
        }
    }
}

class ProxyPersistenceDelegate extends PersistenceDelegate {
    protected Expression instantiate(Object oldInstance, Encoder out) {
        Class type = oldInstance.getClass();
        java.lang.reflect.Proxy p = (java.lang.reflect.Proxy)oldInstance;
        // This unappealing hack is not required but makes the
        // representation of EventHandlers much more concise.
        java.lang.reflect.InvocationHandler ih = java.lang.reflect.Proxy.getInvocationHandler(p);
        if (ih instanceof EventHandler) {
            EventHandler eh = (EventHandler)ih;
            Vector args = new Vector();
            args.add(type.getInterfaces()[0]);
            args.add(eh.getTarget());
            args.add(eh.getAction());
            if (eh.getEventPropertyName() != null) {
                args.add(eh.getEventPropertyName());
            }
            if (eh.getListenerMethodName() != null) {
                args.setSize(4);
                args.add(eh.getListenerMethodName());
            }
            return new Expression(oldInstance,
                                  EventHandler.class,
                                  "create",
                                  args.toArray());
        }
        return new Expression(oldInstance,
                              java.lang.reflect.Proxy.class,
                              "newProxyInstance",
                              new Object[]{type.getClassLoader(),
                                           type.getInterfaces(),
                                           ih});
    }
}

// Strings
class java_lang_String_PersistenceDelegate extends PersistenceDelegate {
    protected Expression instantiate(Object oldInstance, Encoder out) { return null; }

    public void writeObject(Object oldInstance, Encoder out) {
        // System.out.println("NullPersistenceDelegate:writeObject " + oldInstance);
    }
}

// Classes
class java_lang_Class_PersistenceDelegate extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return oldInstance.equals(newInstance);
    }

    protected Expression instantiate(Object oldInstance, Encoder out) {
        Class c = (Class)oldInstance;
        // As of 1.3 it is not possible to call Class.forName("int"),
        // so we have to generate different code for primitive types.
        // This is needed for arrays whose subtype may be primitive.
        if (c.isPrimitive()) {
            Field field = null;
	    try {
		field = ReflectionUtils.typeToClass(c).getDeclaredField("TYPE");
	    } catch (NoSuchFieldException ex) {
                System.err.println("Unknown primitive type: " + c);
            }
            return new Expression(oldInstance, field, "get", new Object[]{null});
        }
        else if (oldInstance == String.class) {
            return new Expression(oldInstance, "", "getClass", new Object[]{});
        }
        else if (oldInstance == Class.class) {
            return new Expression(oldInstance, String.class, "getClass", new Object[]{});
        }
        else {
            return new Expression(oldInstance, Class.class, "forName", new Object[]{c.getName()});
        }
    }
}

// Fields
class java_lang_reflect_Field_PersistenceDelegate extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return oldInstance.equals(newInstance);
    }

    protected Expression instantiate(Object oldInstance, Encoder out) {
        Field f = (Field)oldInstance;
        return new Expression(oldInstance,
                f.getDeclaringClass(),
                "getField",
                new Object[]{f.getName()});
    }
}

// Methods
class java_lang_reflect_Method_PersistenceDelegate extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return oldInstance.equals(newInstance);
    }

    protected Expression instantiate(Object oldInstance, Encoder out) {
        Method m = (Method)oldInstance;
        return new Expression(oldInstance,
                m.getDeclaringClass(),
                "getMethod",
                new Object[]{m.getName(), m.getParameterTypes()});
    }
}

// Dates

/**
 * The persistence delegate for <CODE>java.util.Date</CODE> classes.
 * Do not extend DefaultPersistenceDelegate to improve performance and
 * to avoid problems with <CODE>java.sql.Date</CODE>,
 * <CODE>java.sql.Time</CODE> and <CODE>java.sql.Timestamp</CODE>.
 *
 * @author Sergey A. Malenkov
 */
class java_util_Date_PersistenceDelegate extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        if (!super.mutatesTo(oldInstance, newInstance)) {
            return false;
        }
        Date oldDate = (Date)oldInstance;
        Date newDate = (Date)newInstance;

        return oldDate.getTime() == newDate.getTime();
    }

    protected Expression instantiate(Object oldInstance, Encoder out) {
        Date date = (Date)oldInstance;
        return new Expression(date, date.getClass(), "new", new Object[] {date.getTime()});
    }
}

/**
 * The persistence delegate for <CODE>java.sql.Timestamp</CODE> classes.
 * It supports nanoseconds.
 *
 * @author Sergey A. Malenkov
 */
final class java_sql_Timestamp_PersistenceDelegate extends java_util_Date_PersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        Timestamp oldTime = (Timestamp)oldInstance;
        Timestamp newTime = (Timestamp)newInstance;

        int nanos = oldTime.getNanos();
        if (nanos != newTime.getNanos()) {
            out.writeStatement(new Statement(oldTime, "setNanos", new Object[] {nanos}));
        }
    }
}

// Collections

/*
The Hashtable and AbstractMap classes have no common ancestor yet may
be handled with a single persistence delegate: one which uses the methods
of the Map insterface exclusively. Attatching the persistence delegates
to the interfaces themselves is fraught however since, in the case of
the Map, both the AbstractMap and HashMap classes are declared to
implement the Map interface, leaving the obvious implementation prone
to repeating their initialization. These issues and questions around
the ordering of delegates attached to interfaces have lead us to
ignore any delegates attached to interfaces and force all persistence
delegates to be registered with concrete classes.
*/

/**
 * The base class for persistence delegates for inner classes
 * that can be created using {@link Collections}.
 *
 * @author Sergey A. Malenkov
 */
abstract class java_util_Collections extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        if (!super.mutatesTo(oldInstance, newInstance)) {
            return false;
        }
        if ((oldInstance instanceof List) || (oldInstance instanceof Set) || (oldInstance instanceof Map)) {
            return oldInstance.equals(newInstance);
        }
        Collection oldC = (Collection) oldInstance;
        Collection newC = (Collection) newInstance;
        return (oldC.size() == newC.size()) && oldC.containsAll(newC);
    }

    static Object getPrivateField(final Object instance, final String name) {
        return AccessController.doPrivileged(
                new PrivilegedAction() {
                    public Object run() {
                        Class type = instance.getClass();
                        while ( true ) {
                            try {
                                Field field = type.getDeclaredField(name);
                                field.setAccessible(true);
                                return field.get( instance );
                            }
                            catch (NoSuchFieldException exception) {
                                type = type.getSuperclass();
                                if (type == null) {
                                    throw new IllegalStateException("Could not find field " + name, exception);
                                }
                            }
                            catch (Exception exception) {
                                throw new IllegalStateException("Could not get value " + type.getName() + '.' + name, exception);
                            }
                        }
                    }
                } );
    }

    static final class EmptyList_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            return new Expression(oldInstance, Collections.class, "emptyList", null);
        }
    }

    static final class EmptySet_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            return new Expression(oldInstance, Collections.class, "emptySet", null);
        }
    }

    static final class EmptyMap_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            return new Expression(oldInstance, Collections.class, "emptyMap", null);
        }
    }

    static final class SingletonList_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            List list = (List) oldInstance;
            return new Expression(oldInstance, Collections.class, "singletonList", new Object[]{list.get(0)});
        }
    }

    static final class SingletonSet_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            Set set = (Set) oldInstance;
            return new Expression(oldInstance, Collections.class, "singleton", new Object[]{set.iterator().next()});
        }
    }

    static final class SingletonMap_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            Map map = (Map) oldInstance;
            Object key = map.keySet().iterator().next();
            return new Expression(oldInstance, Collections.class, "singletonMap", new Object[]{key, map.get(key)});
        }
    }

    static final class UnmodifiableCollection_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            List list = new ArrayList((Collection) oldInstance);
            return new Expression(oldInstance, Collections.class, "unmodifiableCollection", new Object[]{list});
        }
    }

    static final class UnmodifiableList_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            List list = new LinkedList((Collection) oldInstance);
            return new Expression(oldInstance, Collections.class, "unmodifiableList", new Object[]{list});
        }
    }

    static final class UnmodifiableRandomAccessList_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            List list = new ArrayList((Collection) oldInstance);
            return new Expression(oldInstance, Collections.class, "unmodifiableList", new Object[]{list});
        }
    }

    static final class UnmodifiableSet_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            Set set = new HashSet((Set) oldInstance);
            return new Expression(oldInstance, Collections.class, "unmodifiableSet", new Object[]{set});
        }
    }

    static final class UnmodifiableSortedSet_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            SortedSet set = new TreeSet((SortedSet) oldInstance);
            return new Expression(oldInstance, Collections.class, "unmodifiableSortedSet", new Object[]{set});
        }
    }

    static final class UnmodifiableMap_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            Map map = new HashMap((Map) oldInstance);
            return new Expression(oldInstance, Collections.class, "unmodifiableMap", new Object[]{map});
        }
    }

    static final class UnmodifiableSortedMap_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            SortedMap map = new TreeMap((SortedMap) oldInstance);
            return new Expression(oldInstance, Collections.class, "unmodifiableSortedMap", new Object[]{map});
        }
    }

    static final class SynchronizedCollection_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            List list = new ArrayList((Collection) oldInstance);
            return new Expression(oldInstance, Collections.class, "synchronizedCollection", new Object[]{list});
        }
    }

    static final class SynchronizedList_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            List list = new LinkedList((Collection) oldInstance);
            return new Expression(oldInstance, Collections.class, "synchronizedList", new Object[]{list});
        }
    }

    static final class SynchronizedRandomAccessList_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            List list = new ArrayList((Collection) oldInstance);
            return new Expression(oldInstance, Collections.class, "synchronizedList", new Object[]{list});
        }
    }

    static final class SynchronizedSet_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            Set set = new HashSet((Set) oldInstance);
            return new Expression(oldInstance, Collections.class, "synchronizedSet", new Object[]{set});
        }
    }

    static final class SynchronizedSortedSet_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            SortedSet set = new TreeSet((SortedSet) oldInstance);
            return new Expression(oldInstance, Collections.class, "synchronizedSortedSet", new Object[]{set});
        }
    }

    static final class SynchronizedMap_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            Map map = new HashMap((Map) oldInstance);
            return new Expression(oldInstance, Collections.class, "synchronizedMap", new Object[]{map});
        }
    }

    static final class SynchronizedSortedMap_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            SortedMap map = new TreeMap((SortedMap) oldInstance);
            return new Expression(oldInstance, Collections.class, "synchronizedSortedMap", new Object[]{map});
        }
    }

    static final class CheckedCollection_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            Object type = getPrivateField(oldInstance, "type");
            List list = new ArrayList((Collection) oldInstance);
            return new Expression(oldInstance, Collections.class, "checkedCollection", new Object[]{list, type});
        }
    }

    static final class CheckedList_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            Object type = getPrivateField(oldInstance, "type");
            List list = new LinkedList((Collection) oldInstance);
            return new Expression(oldInstance, Collections.class, "checkedList", new Object[]{list, type});
        }
    }

    static final class CheckedRandomAccessList_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            Object type = getPrivateField(oldInstance, "type");
            List list = new ArrayList((Collection) oldInstance);
            return new Expression(oldInstance, Collections.class, "checkedList", new Object[]{list, type});
        }
    }

    static final class CheckedSet_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            Object type = getPrivateField(oldInstance, "type");
            Set set = new HashSet((Set) oldInstance);
            return new Expression(oldInstance, Collections.class, "checkedSet", new Object[]{set, type});
        }
    }

    static final class CheckedSortedSet_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            Object type = getPrivateField(oldInstance, "type");
            SortedSet set = new TreeSet((SortedSet) oldInstance);
            return new Expression(oldInstance, Collections.class, "checkedSortedSet", new Object[]{set, type});
        }
    }

    static final class CheckedMap_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            Object keyType = getPrivateField(oldInstance, "keyType");
            Object valueType = getPrivateField(oldInstance, "valueType");
            Map map = new HashMap((Map) oldInstance);
            return new Expression(oldInstance, Collections.class, "checkedMap", new Object[]{map, keyType, valueType});
        }
    }

    static final class CheckedSortedMap_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            Object keyType = getPrivateField(oldInstance, "keyType");
            Object valueType = getPrivateField(oldInstance, "valueType");
            SortedMap map = new TreeMap((SortedMap) oldInstance);
            return new Expression(oldInstance, Collections.class, "checkedSortedMap", new Object[]{map, keyType, valueType});
        }
    }
}

/**
 * The persistence delegate for <CODE>java.util.EnumMap</CODE> classes.
 *
 * @author Sergey A. Malenkov
 */
class java_util_EnumMap_PersistenceDelegate extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return super.mutatesTo(oldInstance, newInstance) && (getType(oldInstance) == getType(newInstance));
    }

    protected Expression instantiate(Object oldInstance, Encoder out) {
        return new Expression(oldInstance, EnumMap.class, "new", new Object[] {getType(oldInstance)});
    }

    private static Object getType(Object instance) {
        return java_util_Collections.getPrivateField(instance, "keyType");
    }
}

/**
 * The persistence delegate for <CODE>java.util.EnumSet</CODE> classes.
 *
 * @author Sergey A. Malenkov
 */
class java_util_EnumSet_PersistenceDelegate extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return super.mutatesTo(oldInstance, newInstance) && (getType(oldInstance) == getType(newInstance));
    }

    protected Expression instantiate(Object oldInstance, Encoder out) {
        return new Expression(oldInstance, EnumSet.class, "noneOf", new Object[] {getType(oldInstance)});
    }

    private static Object getType(Object instance) {
        return java_util_Collections.getPrivateField(instance, "elementType");
    }
}

// Collection
class java_util_Collection_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
	java.util.Collection oldO = (java.util.Collection)oldInstance;
	java.util.Collection newO = (java.util.Collection)newInstance;

        if (newO.size() != 0) {
            invokeStatement(oldInstance, "clear", new Object[]{}, out);
        }
        for (Iterator i = oldO.iterator(); i.hasNext();) {
            invokeStatement(oldInstance, "add", new Object[]{i.next()}, out);
        }
    }
}

// List
class java_util_List_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        java.util.List oldO = (java.util.List)oldInstance;
        java.util.List newO = (java.util.List)newInstance;
        int oldSize = oldO.size();
        int newSize = (newO == null) ? 0 : newO.size();
        if (oldSize < newSize) {
            invokeStatement(oldInstance, "clear", new Object[]{}, out);
            newSize = 0;
        }
        for (int i = 0; i < newSize; i++) {
            Object index = new Integer(i);

            Expression oldGetExp = new Expression(oldInstance, "get", new Object[]{index});
            Expression newGetExp = new Expression(newInstance, "get", new Object[]{index});
            try {
                Object oldValue = oldGetExp.getValue();
                Object newValue = newGetExp.getValue();
                out.writeExpression(oldGetExp);
                if (!MetaData.equals(newValue, out.get(oldValue))) {
                    invokeStatement(oldInstance, "set", new Object[]{index, oldValue}, out);
                }
            }
            catch (Exception e) {
                out.getExceptionListener().exceptionThrown(e);
            }
        }
        for (int i = newSize; i < oldSize; i++) {
            invokeStatement(oldInstance, "add", new Object[]{oldO.get(i)}, out);
        }
    }
}


// Map
class java_util_Map_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        // System.out.println("Initializing: " + newInstance);
        java.util.Map oldMap = (java.util.Map)oldInstance;
        java.util.Map newMap = (java.util.Map)newInstance;
        // Remove the new elements.
        // Do this first otherwise we undo the adding work.
        if (newMap != null) {
            for ( Object newKey : newMap.keySet() ) {
               // PENDING: This "key" is not in the right environment.
                if (!oldMap.containsKey(newKey)) {
                    invokeStatement(oldInstance, "remove", new Object[]{newKey}, out);
                }
            }
        }
        // Add the new elements.
        for ( Object oldKey : oldMap.keySet() ) {
            Expression oldGetExp = new Expression(oldInstance, "get", new Object[]{oldKey});
            // Pending: should use newKey.
            Expression newGetExp = new Expression(newInstance, "get", new Object[]{oldKey});
            try {
                Object oldValue = oldGetExp.getValue();
                Object newValue = newGetExp.getValue();
                out.writeExpression(oldGetExp);
                if (!MetaData.equals(newValue, out.get(oldValue))) {
                    invokeStatement(oldInstance, "put", new Object[]{oldKey, oldValue}, out);
                } else if ((newValue == null) && !newMap.containsKey(oldKey)) {
                    // put oldValue(=null?) if oldKey is absent in newMap
                    invokeStatement(oldInstance, "put", new Object[]{oldKey, oldValue}, out);
                }
            }
            catch (Exception e) {
                out.getExceptionListener().exceptionThrown(e);
            }
        }
    }
}

class java_util_AbstractCollection_PersistenceDelegate extends java_util_Collection_PersistenceDelegate {}
class java_util_AbstractList_PersistenceDelegate extends java_util_List_PersistenceDelegate {}
class java_util_AbstractMap_PersistenceDelegate extends java_util_Map_PersistenceDelegate {}
class java_util_Hashtable_PersistenceDelegate extends java_util_Map_PersistenceDelegate {}


// Beans
class java_beans_beancontext_BeanContextSupport_PersistenceDelegate extends java_util_Collection_PersistenceDelegate {}

// AWT

/**
 * The persistence delegate for {@link Dimension}.
 * It is impossible to use {@link DefaultPersistenceDelegate}
 * because all getters have return types that differ from parameter types
 * of the constructor {@link Dimension#Dimension(int, int)}.
 *
 * @author Sergey A. Malenkov
 */
final class java_awt_Dimension_PersistenceDelegate extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return oldInstance.equals(newInstance);
    }

    protected Expression instantiate(Object oldInstance, Encoder out) {
        Dimension dimension = (Dimension) oldInstance;
        Object[] args = new Object[] {
                dimension.width,
                dimension.height,
        };
        return new Expression(dimension, dimension.getClass(), "new", args);
    }
}

/**
 * The persistence delegate for {@link GridBagConstraints}.
 * It is impossible to use {@link DefaultPersistenceDelegate}
 * because this class does not have any properties.
 *
 * @author Sergey A. Malenkov
 */
final class java_awt_GridBagConstraints_PersistenceDelegate extends PersistenceDelegate {
    protected Expression instantiate(Object oldInstance, Encoder out) {
        GridBagConstraints gbc = (GridBagConstraints) oldInstance;
        Object[] args = new Object[] {
                gbc.gridx,
                gbc.gridy,
                gbc.gridwidth,
                gbc.gridheight,
                gbc.weightx,
                gbc.weighty,
                gbc.anchor,
                gbc.fill,
                gbc.insets,
                gbc.ipadx,
                gbc.ipady,
        };
        return new Expression(gbc, gbc.getClass(), "new", args);
    }
}

/**
 * The persistence delegate for {@link Insets}.
 * It is impossible to use {@link DefaultPersistenceDelegate}
 * because this class does not have any properties.
 *
 * @author Sergey A. Malenkov
 */
final class java_awt_Insets_PersistenceDelegate extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return oldInstance.equals(newInstance);
    }

    protected Expression instantiate(Object oldInstance, Encoder out) {
        Insets insets = (Insets) oldInstance;
        Object[] args = new Object[] {
                insets.top,
                insets.left,
                insets.bottom,
                insets.right,
        };
        return new Expression(insets, insets.getClass(), "new", args);
    }
}

/**
 * The persistence delegate for {@link Point}.
 * It is impossible to use {@link DefaultPersistenceDelegate}
 * because all getters have return types that differ from parameter types
 * of the constructor {@link Point#Point(int, int)}.
 *
 * @author Sergey A. Malenkov
 */
final class java_awt_Point_PersistenceDelegate extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return oldInstance.equals(newInstance);
    }

    protected Expression instantiate(Object oldInstance, Encoder out) {
        Point point = (Point) oldInstance;
        Object[] args = new Object[] {
                point.x,
                point.y,
        };
        return new Expression(point, point.getClass(), "new", args);
    }
}

/**
 * The persistence delegate for {@link Rectangle}.
 * It is impossible to use {@link DefaultPersistenceDelegate}
 * because all getters have return types that differ from parameter types
 * of the constructor {@link Rectangle#Rectangle(int, int, int, int)}.
 *
 * @author Sergey A. Malenkov
 */
final class java_awt_Rectangle_PersistenceDelegate extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return oldInstance.equals(newInstance);
    }

    protected Expression instantiate(Object oldInstance, Encoder out) {
        Rectangle rectangle = (Rectangle) oldInstance;
        Object[] args = new Object[] {
                rectangle.x,
                rectangle.y,
                rectangle.width,
                rectangle.height,
        };
        return new Expression(rectangle, rectangle.getClass(), "new", args);
    }
}

/**
 * The persistence delegate for {@link Font}.
 * It is impossible to use {@link DefaultPersistenceDelegate}
 * because size of the font can be float value.
 *
 * @author Sergey A. Malenkov
 */
final class java_awt_Font_PersistenceDelegate extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return oldInstance.equals(newInstance);
    }

    protected Expression instantiate(Object oldInstance, Encoder out) {
        Font font = (Font) oldInstance;

        int count = 0;
        String family = null;
        int style = Font.PLAIN;
        int size = 12;

        Map basic = font.getAttributes();
        Map clone = new HashMap(basic.size());
        for (Object key : basic.keySet()) {
            Object value = basic.get(key);
            if (value != null) {
                clone.put(key, value);
            }
            if (key == TextAttribute.FAMILY) {
                if (value instanceof String) {
                    count++;
                    family = (String) value;
                }
            }
            else if (key == TextAttribute.WEIGHT) {
                if (TextAttribute.WEIGHT_REGULAR.equals(value)) {
                    count++;
                } else if (TextAttribute.WEIGHT_BOLD.equals(value)) {
                    count++;
                    style |= Font.BOLD;
                }
            }
            else if (key == TextAttribute.POSTURE) {
                if (TextAttribute.POSTURE_REGULAR.equals(value)) {
                    count++;
                } else if (TextAttribute.POSTURE_OBLIQUE.equals(value)) {
                    count++;
                    style |= Font.ITALIC;
                }
            } else if (key == TextAttribute.SIZE) {
                if (value instanceof Number) {
                    Number number = (Number) value;
                    size = number.intValue();
                    if (size == number.floatValue()) {
                        count++;
                    }
                }
            }
        }
        Class type = font.getClass();
        if (count == clone.size()) {
            return new Expression(font, type, "new", new Object[]{family, style, size});
        }
        if (type == Font.class) {
            return new Expression(font, type, "getFont", new Object[]{clone});
        }
        return new Expression(font, type, "new", new Object[]{Font.getFont(clone)});
    }
}

/**
 * The persistence delegate for {@link AWTKeyStroke}.
 * It is impossible to use {@link DefaultPersistenceDelegate}
 * because this class have no public constructor.
 *
 * @author Sergey A. Malenkov
 */
final class java_awt_AWTKeyStroke_PersistenceDelegate extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return oldInstance.equals(newInstance);
    }
 
    protected Expression instantiate(Object oldInstance, Encoder out) {
        AWTKeyStroke key = (AWTKeyStroke) oldInstance;
 
        char ch = key.getKeyChar();
        int code = key.getKeyCode();
        int mask = key.getModifiers();
        boolean onKeyRelease = key.isOnKeyRelease();
 
        Object[] args = null;
        if (ch == KeyEvent.CHAR_UNDEFINED) {
            args = !onKeyRelease
                    ? new Object[]{code, mask}
                    : new Object[]{code, mask, onKeyRelease};
        } else if (code == KeyEvent.VK_UNDEFINED) {
            if (!onKeyRelease) {
                args = (mask == 0)
                        ? new Object[]{ch}
                        : new Object[]{ch, mask};
            } else if (mask == 0) {
                args = new Object[]{ch, onKeyRelease};
            }
        }
        if (args == null) {
            throw new IllegalStateException("Unsupported KeyStroke: " + key);
        }
        Class type = key.getClass();
        String name = type.getName();
        // get short name of the class
        int index = name.lastIndexOf('.') + 1;
        if (index > 0) {
            name = name.substring(index);
        }
        return new Expression( key, type, "get" + name, args );
    }
}

class StaticFieldsPersistenceDelegate extends PersistenceDelegate {
    protected void installFields(Encoder out, Class<?> cls) {
        Field fields[] = cls.getFields();
        for(int i = 0; i < fields.length; i++) { 
            Field field = fields[i]; 
            // Don't install primitives, their identity will not be preserved 
            // by wrapping. 
            if (Object.class.isAssignableFrom(field.getType())) { 
                out.writeExpression(new Expression(field, "get", new Object[]{null})); 
            }
        }
    }

    protected Expression instantiate(Object oldInstance, Encoder out) { 
        throw new RuntimeException("Unrecognized instance: " + oldInstance); 
    }
    
    public void writeObject(Object oldInstance, Encoder out) {
        if (out.getAttribute(this) == null) {
            out.setAttribute(this, Boolean.TRUE);
            installFields(out, oldInstance.getClass()); 
	}
        super.writeObject(oldInstance, out);
    }
} 

// SystemColor
class java_awt_SystemColor_PersistenceDelegate extends StaticFieldsPersistenceDelegate {}

// TextAttribute
class java_awt_font_TextAttribute_PersistenceDelegate extends StaticFieldsPersistenceDelegate {}

// MenuShortcut
class java_awt_MenuShortcut_PersistenceDelegate extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return oldInstance.equals(newInstance);
    }

    protected Expression instantiate(Object oldInstance, Encoder out) { 
        java.awt.MenuShortcut m = (java.awt.MenuShortcut)oldInstance; 
        return new Expression(oldInstance, m.getClass(), "new", 
                   new Object[]{new Integer(m.getKey()), Boolean.valueOf(m.usesShiftModifier())});
    }
}

// Component
class java_awt_Component_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        super.initialize(type, oldInstance, newInstance, out);
        java.awt.Component c = (java.awt.Component)oldInstance;
        java.awt.Component c2 = (java.awt.Component)newInstance;
        // The "background", "foreground" and "font" properties.
        // The foreground and font properties of Windows change from
        // null to defined values after the Windows are made visible -
        // special case them for now.
        if (!(oldInstance instanceof java.awt.Window)) {
            String[] fieldNames = new String[]{"background", "foreground", "font"};
            for(int i = 0; i < fieldNames.length; i++) {
                String name = fieldNames[i];
                Object oldValue = ReflectionUtils.getPrivateField(oldInstance, java.awt.Component.class, name, out.getExceptionListener());
                Object newValue = (newInstance == null) ? null : ReflectionUtils.getPrivateField(newInstance, java.awt.Component.class, name, out.getExceptionListener());
                if (oldValue != null && !oldValue.equals(newValue)) {
                    invokeStatement(oldInstance, "set" + NameGenerator.capitalize(name), new Object[]{oldValue}, out);
                }
            }
        }

        // Bounds
        java.awt.Container p = c.getParent();
        if (p == null || p.getLayout() == null) {
            // Use the most concise construct.
            boolean locationCorrect = c.getLocation().equals(c2.getLocation()); 
            boolean sizeCorrect = c.getSize().equals(c2.getSize()); 
            if (!locationCorrect && !sizeCorrect) { 
                invokeStatement(oldInstance, "setBounds", new Object[]{c.getBounds()}, out);
            } 
            else if (!locationCorrect) { 
                invokeStatement(oldInstance, "setLocation", new Object[]{c.getLocation()}, out);
            } 
            else if (!sizeCorrect) { 
                invokeStatement(oldInstance, "setSize", new Object[]{c.getSize()}, out);
            }             
        }
    }
}

// Container
class java_awt_Container_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        super.initialize(type, oldInstance, newInstance, out);
        // Ignore the children of a JScrollPane.
        // Pending(milne) find a better way to do this.
        if (oldInstance instanceof javax.swing.JScrollPane) {
            return;
        }
        java.awt.Container oldC = (java.awt.Container)oldInstance;
        java.awt.Component[] oldChildren = oldC.getComponents();
        java.awt.Container newC = (java.awt.Container)newInstance;
        java.awt.Component[] newChildren = (newC == null) ? new java.awt.Component[0] : newC.getComponents();

        BorderLayout layout = ( oldC.getLayout() instanceof BorderLayout )
                ? ( BorderLayout )oldC.getLayout()
                : null;

        JLayeredPane oldLayeredPane = (oldInstance instanceof JLayeredPane)
                ? (JLayeredPane) oldInstance
                : null;

        // Pending. Assume all the new children are unaltered.
        for(int i = newChildren.length; i < oldChildren.length; i++) {
            Object[] args = ( layout != null )
                    ? new Object[] {oldChildren[i], layout.getConstraints( oldChildren[i] )}
                    : (oldLayeredPane != null)
                            ? new Object[] {oldChildren[i], oldLayeredPane.getLayer(oldChildren[i]), Integer.valueOf(-1)}
                    : new Object[] {oldChildren[i]};

            invokeStatement(oldInstance, "add", args, out);
        }
    }
}

// Choice
class java_awt_Choice_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        super.initialize(type, oldInstance, newInstance, out);
        java.awt.Choice m = (java.awt.Choice)oldInstance;
        java.awt.Choice n = (java.awt.Choice)newInstance;
        for (int i = n.getItemCount(); i < m.getItemCount(); i++) {
            invokeStatement(oldInstance, "add", new Object[]{m.getItem(i)}, out);
        }
    }
}

// Menu
class java_awt_Menu_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        super.initialize(type, oldInstance, newInstance, out);
        java.awt.Menu m = (java.awt.Menu)oldInstance;
        java.awt.Menu n = (java.awt.Menu)newInstance;
        for (int i = n.getItemCount(); i < m.getItemCount(); i++) {
            invokeStatement(oldInstance, "add", new Object[]{m.getItem(i)}, out);
        }
    }
}

// MenuBar
class java_awt_MenuBar_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        super.initialize(type, oldInstance, newInstance, out);
        java.awt.MenuBar m = (java.awt.MenuBar)oldInstance;
        java.awt.MenuBar n = (java.awt.MenuBar)newInstance;
        for (int i = n.getMenuCount(); i < m.getMenuCount(); i++) {
            invokeStatement(oldInstance, "add", new Object[]{m.getMenu(i)}, out);
        }
    }
}

// List
class java_awt_List_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        super.initialize(type, oldInstance, newInstance, out);
        java.awt.List m = (java.awt.List)oldInstance;
        java.awt.List n = (java.awt.List)newInstance;
        for (int i = n.getItemCount(); i < m.getItemCount(); i++) {
            invokeStatement(oldInstance, "add", new Object[]{m.getItem(i)}, out);
        }
    }
}
    

// LayoutManagers

// BorderLayout
class java_awt_BorderLayout_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance, 
			      Object newInstance, Encoder out) {
        super.initialize(type, oldInstance, newInstance, out);
        String[] locations = {"north", "south", "east", "west", "center"};
        String[] names = {java.awt.BorderLayout.NORTH, java.awt.BorderLayout.SOUTH, 
			  java.awt.BorderLayout.EAST, java.awt.BorderLayout.WEST, 
			  java.awt.BorderLayout.CENTER};
        for(int i = 0; i < locations.length; i++) {
            Object oldC = ReflectionUtils.getPrivateField(oldInstance, 
							  java.awt.BorderLayout.class, 
							  locations[i], 
							  out.getExceptionListener());
            Object newC = ReflectionUtils.getPrivateField(newInstance, 
							  java.awt.BorderLayout.class, 
							  locations[i], 
							  out.getExceptionListener());
            // Pending, assume any existing elements are OK.
            if (oldC != null && newC == null) {
                invokeStatement(oldInstance, "addLayoutComponent", 
				new Object[]{oldC, names[i]}, out);
            }
        }
    }
}

// CardLayout
class java_awt_CardLayout_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance, 
			      Object newInstance, Encoder out) {
        super.initialize(type, oldInstance, newInstance, out);
        Hashtable tab = (Hashtable)ReflectionUtils.getPrivateField(oldInstance, 
								   java.awt.CardLayout.class, 
								   "tab", 
								   out.getExceptionListener());
        if (tab != null) {
            for(Enumeration e = tab.keys(); e.hasMoreElements();) {
                Object child = e.nextElement();
                invokeStatement(oldInstance, "addLayoutComponent",
                                new Object[]{child, (String)tab.get(child)}, out);
            }
        }
    }
}

// GridBagLayout
class java_awt_GridBagLayout_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance, 
			      Object newInstance, Encoder out) {
        super.initialize(type, oldInstance, newInstance, out);
        Hashtable comptable = (Hashtable)ReflectionUtils.getPrivateField(oldInstance, 
						 java.awt.GridBagLayout.class, 
						 "comptable", 
						 out.getExceptionListener());
        if (comptable != null) {
            for(Enumeration e = comptable.keys(); e.hasMoreElements();) {
                Object child = e.nextElement();
                invokeStatement(oldInstance, "addLayoutComponent",
                                new Object[]{child, comptable.get(child)}, out);
            }
        }
    }
}

// Swing

// JFrame (If we do this for Window instead of JFrame, the setVisible call
// will be issued before we have added all the children to the JFrame and
// will appear blank).
class javax_swing_JFrame_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        super.initialize(type, oldInstance, newInstance, out);
        java.awt.Window oldC = (java.awt.Window)oldInstance;
        java.awt.Window newC = (java.awt.Window)newInstance;
        boolean oldV = oldC.isVisible();
        boolean newV = newC.isVisible();
        if (newV != oldV) {
            // false means: don't execute this statement at write time.
            boolean executeStatements = out.executeStatements;
            out.executeStatements = false;
            invokeStatement(oldInstance, "setVisible", new Object[]{Boolean.valueOf(oldV)}, out);
            out.executeStatements = executeStatements;
        }
    }
}

// Models

// DefaultListModel
class javax_swing_DefaultListModel_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        // Note, the "size" property will be set here.
        super.initialize(type, oldInstance, newInstance, out);
        javax.swing.DefaultListModel m = (javax.swing.DefaultListModel)oldInstance;
        javax.swing.DefaultListModel n = (javax.swing.DefaultListModel)newInstance;
        for (int i = n.getSize(); i < m.getSize(); i++) {
            invokeStatement(oldInstance, "add", // Can also use "addElement".
                    new Object[]{m.getElementAt(i)}, out);
        }
    }
}

// DefaultComboBoxModel
class javax_swing_DefaultComboBoxModel_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        super.initialize(type, oldInstance, newInstance, out);
        javax.swing.DefaultComboBoxModel m = (javax.swing.DefaultComboBoxModel)oldInstance;
        for (int i = 0; i < m.getSize(); i++) {
            invokeStatement(oldInstance, "addElement", new Object[]{m.getElementAt(i)}, out);
        }
    }
}


// DefaultMutableTreeNode
class javax_swing_tree_DefaultMutableTreeNode_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance, Object
			      newInstance, Encoder out) {
        super.initialize(type, oldInstance, newInstance, out);
        javax.swing.tree.DefaultMutableTreeNode m =
	    (javax.swing.tree.DefaultMutableTreeNode)oldInstance;
        javax.swing.tree.DefaultMutableTreeNode n =
	    (javax.swing.tree.DefaultMutableTreeNode)newInstance;
        for (int i = n.getChildCount(); i < m.getChildCount(); i++) {
            invokeStatement(oldInstance, "add", new
		Object[]{m.getChildAt(i)}, out);
        }
    }
}

// ToolTipManager
class javax_swing_ToolTipManager_PersistenceDelegate extends PersistenceDelegate {
    protected Expression instantiate(Object oldInstance, Encoder out) {
        return new Expression(oldInstance, javax.swing.ToolTipManager.class,
                              "sharedInstance", new Object[]{});
    }
}

// JTabbedPane
class javax_swing_JTabbedPane_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        super.initialize(type, oldInstance, newInstance, out);
        javax.swing.JTabbedPane p = (javax.swing.JTabbedPane)oldInstance;
        for (int i = 0; i < p.getTabCount(); i++) {
            invokeStatement(oldInstance, "addTab",
                                          new Object[]{
                                              p.getTitleAt(i),
                                              p.getIconAt(i),
                                              p.getComponentAt(i)}, out);
        }
    }
}

// Box
class javax_swing_Box_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return super.mutatesTo(oldInstance, newInstance) && getAxis(oldInstance).equals(getAxis(newInstance));
    }

    protected Expression instantiate(Object oldInstance, Encoder out) {
        return new Expression(oldInstance, oldInstance.getClass(), "new", new Object[] {getAxis(oldInstance)});
    }
	
    private Integer getAxis(Object object) {
        Box box = (Box) object;
        return (Integer) java_util_Collections.getPrivateField(box.getLayout(), "axis");
    }
}

// JMenu
// Note that we do not need to state the initialiser for
// JMenuItems since the getComponents() method defined in
// Container will return all of the sub menu items that
// need to be added to the menu item.
// Not so for JMenu apparently.
class javax_swing_JMenu_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        super.initialize(type, oldInstance, newInstance, out);
        javax.swing.JMenu m = (javax.swing.JMenu)oldInstance;
        java.awt.Component[] c = m.getMenuComponents();
        for (int i = 0; i < c.length; i++) {
            invokeStatement(oldInstance, "add", new Object[]{c[i]}, out);
        }
    }
}

/**
 * The persistence delegate for {@link MatteBorder}.
 * It is impossible to use {@link DefaultPersistenceDelegate}
 * because this class does not have writable properties.
 *
 * @author Sergey A. Malenkov
 */
final class javax_swing_border_MatteBorder_PersistenceDelegate extends PersistenceDelegate {
    protected Expression instantiate(Object oldInstance, Encoder out) {
        MatteBorder border = (MatteBorder) oldInstance;
        Insets insets = border.getBorderInsets();
        Object object = border.getTileIcon();
        if (object == null) {
            object = border.getMatteColor();
        }
        Object[] args = new Object[] {
                insets.top,
                insets.left,
                insets.bottom,
                insets.right,
                object,
        };
        return new Expression(border, border.getClass(), "new", args);
    }
}

/* XXX - doens't seem to work. Debug later.
class javax_swing_JMenu_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        super.initialize(type, oldInstance, newInstance, out);
        javax.swing.JMenu m = (javax.swing.JMenu)oldInstance;
        javax.swing.JMenu n = (javax.swing.JMenu)newInstance;
        for (int i = n.getItemCount(); i < m.getItemCount(); i++) {
            invokeStatement(oldInstance, "add", new Object[]{m.getItem(i)}, out);
        }
    }
}
*/

/**
 * The persistence delegate for {@link PrintColorUIResource}.
 * It is impossible to use {@link DefaultPersistenceDelegate}
 * because this class has special rule for serialization:
 * it should be converted to {@link ColorUIResource}.
 *
 * @see PrintColorUIResource#writeReplace
 *
 * @author Sergey A. Malenkov
 */
final class sun_swing_PrintColorUIResource_PersistenceDelegate extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return oldInstance.equals(newInstance);
    }

    protected Expression instantiate(Object oldInstance, Encoder out) {
        Color color = (Color) oldInstance;
        Object[] args = new Object[] {color.getRGB()};
        return new Expression(color, ColorUIResource.class, "new", args);
    }
}

class MetaData {
    private static Hashtable internalPersistenceDelegates = new Hashtable();
    private static Hashtable transientProperties = new Hashtable();

    private static PersistenceDelegate nullPersistenceDelegate = new NullPersistenceDelegate();
    private static PersistenceDelegate enumPersistenceDelegate = new EnumPersistenceDelegate();
    private static PersistenceDelegate primitivePersistenceDelegate = new PrimitivePersistenceDelegate();
    private static PersistenceDelegate defaultPersistenceDelegate = new DefaultPersistenceDelegate();
    private static PersistenceDelegate arrayPersistenceDelegate;
    private static PersistenceDelegate proxyPersistenceDelegate;

    static {

// Constructors.

  // beans

        registerConstructor("java.beans.Statement", new String[]{"target", "methodName", "arguments"});
        registerConstructor("java.beans.Expression", new String[]{"target", "methodName", "arguments"});
        registerConstructor("java.beans.EventHandler", new String[]{"target", "action", "eventPropertyName", "listenerMethodName"});

  // awt

        registerConstructor("java.awt.Color", new String[]{"red", "green", "blue", "alpha"});
        registerConstructor("java.awt.Cursor", new String[]{"type"});
        registerConstructor("java.awt.ScrollPane", new String[]{"scrollbarDisplayPolicy"});

  // swing

        registerConstructor("javax.swing.plaf.ColorUIResource", new String[]{"red", "green", "blue"});

        registerConstructor("javax.swing.tree.DefaultTreeModel", new String[]{"root"});
        registerConstructor("javax.swing.JTree", new String[]{"model"});
        registerConstructor("javax.swing.tree.TreePath", new String[]{"path"});

        registerConstructor("javax.swing.OverlayLayout", new String[]{"target"});
        registerConstructor("javax.swing.BoxLayout", new String[]{"target", "axis"});
        registerConstructor("javax.swing.Box$Filler", new String[]{"minimumSize", "preferredSize",
                                                                   "maximumSize"});
        registerConstructor("javax.swing.DefaultCellEditor", new String[]{"component"});

        /*
        This is required because the JSplitPane reveals a private layout class
        called BasicSplitPaneUI$BasicVerticalLayoutManager which changes with
        the orientation. To avoid the necessity for instantiating it we cause
        the orientation attribute to get set before the layout manager - that
        way the layout manager will be changed as a side effect. Unfortunately,
        the layout property belongs to the superclass and therefore precedes
        the orientation property. PENDING - we need to allow this kind of
        modification. For now, put the property in the constructor.
        */
        registerConstructor("javax.swing.JSplitPane", new String[]{"orientation"});
        // Try to synthesize the ImageIcon from its description.
        registerConstructor("javax.swing.ImageIcon", new String[]{"description"});
        // JButton's "text" and "actionCommand" properties are related,
        // use the text as a constructor argument to ensure that it is set first.
        // This remove the benign, but unnecessary, manipulation of actionCommand
        // property in the common case.
        registerConstructor("javax.swing.JButton", new String[]{"text"});

        // borders

        registerConstructor("javax.swing.border.BevelBorder", new String[]{"bevelType", "highlightOuterColor", "highlightInnerColor", "shadowOuterColor", "shadowInnerColor"});
        registerConstructor("javax.swing.plaf.BorderUIResource$BevelBorderUIResource", new String[]{"bevelType", "highlightOuterColor", "highlightInnerColor", "shadowOuterColor", "shadowInnerColor"});
        registerConstructor("javax.swing.border.CompoundBorder", new String[]{"outsideBorder", "insideBorder"});
        registerConstructor("javax.swing.plaf.BorderUIResource$CompoundBorderUIResource", new String[]{"outsideBorder", "insideBorder"});
        registerConstructor("javax.swing.border.EmptyBorder", new String[]{"borderInsets"});
        registerConstructor("javax.swing.plaf.BorderUIResource$EmptyBorderUIResource", new String[]{"borderInsets"});
        registerConstructor("javax.swing.border.EtchedBorder", new String[]{"etchType", "highlightColor", "shadowColor"});
        registerConstructor("javax.swing.plaf.BorderUIResource$EtchedBorderUIResource", new String[]{"etchType", "highlightColor", "shadowColor"});
        registerConstructor("javax.swing.border.LineBorder", new String[]{"lineColor", "thickness", "roundedCorners"});
        registerConstructor("javax.swing.plaf.BorderUIResource$LineBorderUIResource", new String[]{"lineColor", "thickness"});
        registerConstructor("javax.swing.border.SoftBevelBorder", new String[]{"bevelType", "highlightOuterColor", "highlightInnerColor", "shadowOuterColor", "shadowInnerColor"});
        // registerConstructorWithBadEqual("javax.swing.plaf.BorderUIResource$SoftBevelBorderUIResource", new String[]{"bevelType", "highlightOuter", "highlightInner", "shadowOuter", "shadowInner"});
        registerConstructor("javax.swing.border.TitledBorder", new String[]{"border", "title", "titleJustification", "titlePosition", "titleFont", "titleColor"});
        registerConstructor("javax.swing.plaf.BorderUIResource$TitledBorderUIResource", new String[]{"border", "title", "titleJustification", "titlePosition", "titleFont", "titleColor"});

        internalPersistenceDelegates.put("java.net.URI",
                                         new PrimitivePersistenceDelegate());

        // it is possible because MatteBorder is assignable from MatteBorderUIResource
        internalPersistenceDelegates.put("javax.swing.plaf.BorderUIResource$MatteBorderUIResource",
                                         new javax_swing_border_MatteBorder_PersistenceDelegate());

        // it is possible because FontUIResource is supported by java_awt_Font_PersistenceDelegate
        internalPersistenceDelegates.put("javax.swing.plaf.FontUIResource",
                                         new java_awt_Font_PersistenceDelegate());

        // it is possible because KeyStroke is supported by java_awt_AWTKeyStroke_PersistenceDelegate
        internalPersistenceDelegates.put("javax.swing.KeyStroke",
                                         new java_awt_AWTKeyStroke_PersistenceDelegate());

        internalPersistenceDelegates.put("java.sql.Date", new java_util_Date_PersistenceDelegate());
        internalPersistenceDelegates.put("java.sql.Time", new java_util_Date_PersistenceDelegate());

        internalPersistenceDelegates.put("java.util.JumboEnumSet", new java_util_EnumSet_PersistenceDelegate());
        internalPersistenceDelegates.put("java.util.RegularEnumSet", new java_util_EnumSet_PersistenceDelegate());

// Transient properties

  // awt

    // Infinite graphs.
        removeProperty("java.awt.geom.RectangularShape", "frame");
        // removeProperty("java.awt.Rectangle2D", "frame");
        // removeProperty("java.awt.Rectangle", "frame");

        removeProperty("java.awt.Rectangle", "bounds");
        removeProperty("java.awt.Dimension", "size");
        removeProperty("java.awt.Point", "location");

        // The color and font properties in Component need special treatment, see above.
        removeProperty("java.awt.Component", "foreground");
        removeProperty("java.awt.Component", "background");
        removeProperty("java.awt.Component", "font");

        // The visible property of Component needs special treatment because of Windows.
        removeProperty("java.awt.Component", "visible");

        // This property throws an exception if accessed when there is no child. 
        removeProperty("java.awt.ScrollPane", "scrollPosition"); 
        
	// 4917458 this should be removed for XAWT since it may throw
	// an unsupported exception if there isn't any input methods.
	// This shouldn't be a problem since these are added behind
	// the scenes automatically.
        removeProperty("java.awt.im.InputContext", "compositionEnabled"); 

  // swing

        // The size properties in JComponent need special treatment, see above.
        removeProperty("javax.swing.JComponent", "minimumSize");
        removeProperty("javax.swing.JComponent", "preferredSize");
        removeProperty("javax.swing.JComponent", "maximumSize");

        // These properties have platform specific implementations
        // and should not appear in archives.
        removeProperty("javax.swing.ImageIcon", "image");
        removeProperty("javax.swing.ImageIcon", "imageObserver");

        // This property unconditionally throws a "not implemented" exception.
        removeProperty("javax.swing.JMenuBar", "helpMenu");

        // The scrollBars in a JScrollPane are dynamic and should not
        // be archived. The row and columns headers are changed by
        // components like JTable on "addNotify".
        removeProperty("javax.swing.JScrollPane", "verticalScrollBar");
        removeProperty("javax.swing.JScrollPane", "horizontalScrollBar");
        removeProperty("javax.swing.JScrollPane", "rowHeader");
        removeProperty("javax.swing.JScrollPane", "columnHeader");
        
        removeProperty("javax.swing.JViewport", "extentSize");

        // Renderers need special treatment, since their properties
        // change during rendering.
        removeProperty("javax.swing.table.JTableHeader", "defaultRenderer");
        removeProperty("javax.swing.JList", "cellRenderer");

        removeProperty("javax.swing.JList", "selectedIndices");

        // The lead and anchor selection indexes are best ignored.
        // Selection is rarely something that should persist from
        // development to deployment.
        removeProperty("javax.swing.DefaultListSelectionModel", "leadSelectionIndex");
        removeProperty("javax.swing.DefaultListSelectionModel", "anchorSelectionIndex");

        // The selection must come after the text itself.
        removeProperty("javax.swing.JComboBox", "selectedIndex");

        // All selection information should come after the JTabbedPane is built
        removeProperty("javax.swing.JTabbedPane", "selectedIndex");
        removeProperty("javax.swing.JTabbedPane", "selectedComponent");

        // PENDING: The "disabledIcon" property is often computed from the icon property.
        removeProperty("javax.swing.AbstractButton", "disabledIcon");
	removeProperty("javax.swing.JLabel", "disabledIcon");

        // The caret property throws errors when it it set beyond
        // the extent of the text. We could just set it after the
        // text, but this is probably not something we want to archive anyway.
        removeProperty("javax.swing.text.JTextComponent", "caret");
        removeProperty("javax.swing.text.JTextComponent", "caretPosition");
        // The selectionStart must come after the text itself.
        removeProperty("javax.swing.text.JTextComponent", "selectionStart");
        removeProperty("javax.swing.text.JTextComponent", "selectionEnd");
    }

    /*pp*/ static boolean equals(Object o1, Object o2) {
        return (o1 == null) ? (o2 == null) : o1.equals(o2);
    }

    public synchronized static PersistenceDelegate getPersistenceDelegate(Class type) {
        if (type == null) {
            return nullPersistenceDelegate;
        }
        if (Enum.class.isAssignableFrom(type)) {
            return enumPersistenceDelegate;
        }
        if (ReflectionUtils.isPrimitive(type)) {
            return primitivePersistenceDelegate;
        }
        // The persistence delegate for arrays is non-trivial; instantiate it lazily.
        if (type.isArray()) {
            if (arrayPersistenceDelegate == null) {
                arrayPersistenceDelegate = new ArrayPersistenceDelegate();
            }
            return arrayPersistenceDelegate;
        }
        // Handle proxies lazily for backward compatibility with 1.2.
        try {
            if (java.lang.reflect.Proxy.isProxyClass(type)) {
                if (proxyPersistenceDelegate == null) {
                    proxyPersistenceDelegate = new ProxyPersistenceDelegate();
                }
                return proxyPersistenceDelegate;
            }
        }
        catch(Exception e) {}
        // else if (type.getDeclaringClass() != null) {
        //     return new DefaultPersistenceDelegate(new String[]{"this$0"});
        // }

	String typeName = type.getName();

	// Check to see if there are properties that have been lazily registered for removal.
	if (getBeanAttribute(type, "transient_init") == null) {
	    Vector tp = (Vector)transientProperties.get(typeName);
	    if (tp != null) {
		for(int i = 0; i < tp.size(); i++) {
		    setPropertyAttribute(type, (String)tp.get(i), "transient", Boolean.TRUE);
		}
	    }
	    setBeanAttribute(type, "transient_init", Boolean.TRUE);
	}

        PersistenceDelegate pd = (PersistenceDelegate)getBeanAttribute(type, "persistenceDelegate");
        if (pd == null) {
            pd = (PersistenceDelegate)internalPersistenceDelegates.get(typeName);
            if (pd != null) {
                return pd;
            }
            internalPersistenceDelegates.put(typeName, defaultPersistenceDelegate);
            try {
                String name =  type.getName();
                Class c = Class.forName("java.beans." + name.replace('.', '_') 
					+ "_PersistenceDelegate");
                pd = (PersistenceDelegate)c.newInstance();
                internalPersistenceDelegates.put(typeName, pd);
            }
            catch (ClassNotFoundException e) {
                String[] properties = getConstructorProperties(type);
                if (properties != null) {
                    pd = new DefaultPersistenceDelegate(properties);
                    internalPersistenceDelegates.put(typeName, pd);
                }
            }
            catch (Exception e) {
                System.err.println("Internal error: " + e);
            }
        }

        return (pd != null) ? pd : defaultPersistenceDelegate;
    }

    private static String[] getConstructorProperties(Class type) {
        String[] names = null;
        int length = 0;
        for (Constructor<?> constructor : type.getConstructors()) {
            String[] value = getAnnotationValue(constructor);
            if ((value != null) && (length < value.length) && isValid(constructor, value)) {
                names = value;
                length = value.length;
            }
        }
        return names;
    }

    private static String[] getAnnotationValue(Constructor<?> constructor) {
        ConstructorProperties annotation = constructor.getAnnotation(ConstructorProperties.class);
        return (annotation != null)
                ? annotation.value()
                : null;
    }

    private static boolean isValid(Constructor<?> constructor, String[] names) {
        Class[] parameters = constructor.getParameterTypes();
        if (names.length != parameters.length) {
            return false;
        }
        for (String name : names) {
            if (name == null) {
                return false;
            }
        }
        return true;
    }

    // Wrapper for Introspector.getBeanInfo to handle exception handling.
    // Note: this relys on new 1.4 Introspector semantics which cache the BeanInfos
    public static BeanInfo getBeanInfo(Class type) {
	BeanInfo info = null;
	try {
	    info = Introspector.getBeanInfo(type);
	} catch (Throwable e) {
	    e.printStackTrace();
	} 

	return info;
    }

    private static PropertyDescriptor getPropertyDescriptor(Class type, String propertyName) {
        BeanInfo info = getBeanInfo(type);
        PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
        // System.out.println("Searching for: " + propertyName + " in " + type);
        for(int i = 0; i < propertyDescriptors.length; i++) {
            PropertyDescriptor pd  = propertyDescriptors[i];
            if (propertyName.equals(pd.getName())) {
                return pd;
            }
        }
        return null;
    }

    private static void setPropertyAttribute(Class type, String property, String attribute, Object value) {
        PropertyDescriptor pd = getPropertyDescriptor(type, property);
        if (pd == null) {
            System.err.println("Warning: property " + property + " is not defined on " + type);
            return;
        }
        pd.setValue(attribute, value);
    }

    private static void setBeanAttribute(Class type, String attribute, Object value) {
        getBeanInfo(type).getBeanDescriptor().setValue(attribute, value);
    }

    private static Object getBeanAttribute(Class type, String attribute) {
	return getBeanInfo(type).getBeanDescriptor().getValue(attribute);
    }

    // MetaData registration

    private synchronized static void registerConstructor(String typeName,
                                                         String[] constructor) {
        internalPersistenceDelegates.put(typeName,
                                         new DefaultPersistenceDelegate(constructor));
    }

    private static void removeProperty(String typeName, String property) {
        Vector tp = (Vector)transientProperties.get(typeName);
        if (tp == null) {
            tp = new Vector();
            transientProperties.put(typeName, tp);
        }
        tp.add(property);
    }
}
