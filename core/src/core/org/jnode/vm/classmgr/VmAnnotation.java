/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.jnode.vm.objects.VmSystemObject;
import sun.reflect.annotation.AnnotationParser;

/**
 * VM representation of a single annotation.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VmAnnotation extends VmSystemObject {

    /**
     * An empty array of annotations
     */
    static final VmAnnotation[] EMPTY_ARR = new VmAnnotation[0];

    /**
     * The descriptor of the annotation type
     */
    private final String typeDescr;

    /**
     * The element values
     */
    private ElementValue[] values;

    /**
     * The type of this annotation
     */
    private transient VmType<? extends Annotation> annType;

    /**
     * The type implementing this annotation
     */
    private transient Annotation value;

    /**
     * @param typeDescr the annotation type descriptor
     * @param values    annotation parameter name-value pairs
     */
    public VmAnnotation(String typeDescr, ElementValue[] values) {
        this.typeDescr = typeDescr;
        this.values = values;
    }

    /**
     * Gets the type of this annotation.
     */
    @SuppressWarnings("unchecked")
    final VmType<? extends Annotation> annotationType(VmClassLoader loader) {
        if (annType == null) {
            try {
                annType = new Signature(typeDescr, loader).getType();
            } catch (ClassNotFoundException ex) {
                throw new NoClassDefFoundError(ex.getMessage());
            }
        }
        return annType;
    }

    /**
     * Gets the annotation value.
     *
     * @param loader
     * @return
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    final Annotation getValue(VmClassLoader loader)
        throws ClassNotFoundException {
        if (value == null) {
            final VmType<? extends Annotation> annType = annotationType(loader);
            //todo will be obsolate when annotation paring is migrated to openjdk
            if (!annType.getName().equals(org.jnode.annotation.AllowedPackages.class.getName())) {
                int dmc = annType.getNoDeclaredMethods();
                Map vmap = new HashMap();
                for (int i = 0; i < dmc; i++) {
                    VmMethod met = annType.getDeclaredMethod(i);
                    Object o = met.getAnnotationDefault();
                    if (o != null) {
                        String name = met.getName();
                        vmap.put(name, o);
                    }
                }


                for (ElementValue value1 : values) {
                    vmap.put(value1.name, value1.value);
                }

                Set<Map.Entry> set = vmap.entrySet();
                values = new ElementValue[set.size()];
                int i = 0;
                for (Map.Entry e : set) {
                    values[i++] = new ElementValue((String) e.getKey(), e.getValue());
                }

                for (Map.Entry e : set) {
                    Object o = e.getValue();
                    if (o != null && o.getClass().isArray()) {
                        Object[] arr = (Object[]) o;
                        if (arr.length > 0) {
                            Object el = arr[0];
                            Class elc = el.getClass();
                            Object[] ar2 = (Object[]) Array.newInstance(elc, arr.length);
                            for (int v = 0; v < arr.length; v++) {
                                ar2[v] = elc.cast(arr[v]);
                            }
                            e.setValue(ar2);
                        }
                    }
                }

                //try {
                /*value =*/
                return AnnotationParser.annotationForMap(loader.asClassLoader().loadClass(annType.getName()), vmap);

            } else {

                final VmType<? extends ImplBase> implType = getImplClass(annType);
                final ImplBase value;
                try {
                    value = implType.asClass().newInstance();
                } catch (InstantiationException ex) {
                    throw new RuntimeException(ex);
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
                value.initialize(annType, values, loader);
                this.value = value;
            }
        }
        return value;
    }

    /**
     * Gets the annotation implementation class.
     */
    @SuppressWarnings("unchecked")
    private VmNormalClass<? extends ImplBase> getImplClass(VmType<?> annType)
        throws ClassNotFoundException {
        final String clsName = annType.getName() + "$Impl";
        try {
            return (VmNormalClass<? extends ImplBase>) annType.getLoader()
                .loadClass(clsName, true);
        } catch (ClassNotFoundException ex) {
            return createImplClass(annType, clsName);
        }
    }

    /**
     * Create a class that implements this annotation.
     *
     * @param loader
     * @return
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    private VmNormalClass<? extends ImplBase> createImplClass(
        VmType<?> annType, String clsName) throws ClassNotFoundException {
        // Create the implementation class
        final VmClassLoader loader = annType.getLoader();
        final String superClassName = ImplBase.class.getName();
        final int accFlags = Modifier.ACC_FINAL | Modifier.ACC_PUBLIC;
        final VmNormalClass<? extends ImplBase> implType = new VmNormalClass(
            clsName, superClassName, loader, accFlags, annType
            .getProtectionDomain());

        // Create the constant pool
        final VmCP cp = new VmCP(2);
        final VmConstClass superClassRef = new VmConstClass(superClassName);
        // Set field ref values[] at index 0
        cp.setConstFieldRef(0, new VmConstFieldRef(superClassRef, "values",
            "[Ljava/lang/Object;"));
        // Set <init> ref at index 1
        cp.setConstMethodRef(1, new VmConstMethodRef(superClassRef, "<init>",
            "()V"));
        implType.setCp(cp);

        // Create method table
        final int mcnt = annType.getNoDeclaredMethods();
        final VmMethod[] methods = new VmMethod[mcnt + 1];

        // Add default constructor
        final int caccFlags = Modifier.ACC_FINAL | Modifier.ACC_PUBLIC;
        final VmSpecialMethod cons = new VmSpecialMethod("<init>", "()V",
            caccFlags, implType);
        methods[0] = cons;
        final byte[] cbytes = new byte[5];
        cbytes[0] = (byte) 0x2a; // aload_0
        cbytes[1] = (byte) 0xB7; // invokespecial <init>
        cbytes[2] = (byte) 0x00;
        cbytes[3] = (byte) 0x01;
        cbytes[4] = (byte) 0xb1; // return
        cons.setBytecode(new VmByteCode(cons, ByteBuffer.wrap(cbytes), 1,
            2, null, null, null));

        // Add the methods
        final int maccFlags = Modifier.ACC_FINAL | Modifier.ACC_PUBLIC;
        for (int i = 0; i < mcnt; i++) {
            final VmMethod imethod = annType.getDeclaredMethod(i);
            final VmInstanceMethod m = new VmInstanceMethod(imethod.getName(),
                imethod.getSignature(), maccFlags, implType);
            final int noLocals = 1; // this == local0
            final int maxStack = 3; // TODO fix me
            // Create bytecode
            final byte[] bytes = new byte[8];
            bytes[0] = (byte) 0x2a; // aload_0
            bytes[1] = (byte) 0xB4; // getfield <values>
            bytes[2] = (byte) 0x00;
            bytes[3] = (byte) 0x00;
            bytes[4] = (byte) 0x10; // bipush <element_value index>
            bytes[5] = (byte) indexOfElementValue(imethod.getName());
            bytes[6] = (byte) 0x32; // aaload
            bytes[7] = (byte) 0xB0; // areturn

            m.setBytecode(new VmByteCode(m, ByteBuffer.wrap(bytes), noLocals,
                maxStack, null, null, null));
            methods[i + 1] = m;
        }
        implType.setMethodTable(methods);

        // Add the implemented interfaces
        final VmImplementedInterface[] itable = new VmImplementedInterface[1];
        itable[0] = new VmImplementedInterface(annType);
        implType.setInterfaceTable(itable);

        return (VmNormalClass<? extends ImplBase>) loader
            .defineClass(implType);
    }

    /**
     * Gets the index in the values array of the element with the given name.
     */
    private final int indexOfElementValue(String name) {
        for (int i = 0;; i++) {
            if (values[i].getName().equals(name)) {
                return i;
            }
        }
    }

    /**
     * Class holding a single element_value structure.
     *
     * @author Ewout Prangsma (epr@users.sourceforge.net)
     */
    static final class ElementValue extends VmSystemObject {

        static final ElementValue[] EMPTY_ARR = new ElementValue[0];

        private final String name;

        private final Object value;

        /**
         * @param name
         * @param value
         */
        public ElementValue(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        /**
         * @return Returns the name.
         */
        final String getName() {
            return name;
        }

        /**
         * @return Returns the value.
         * @throws ClassNotFoundException
         */
        final Object getValue(VmClassLoader loader, VmType<?> valueType)
            throws ClassNotFoundException {
            return resolve(value, loader, valueType);
        }

        private final Object resolve(Object value, VmClassLoader loader, VmType<?> valueType)
            throws ClassNotFoundException {
            if (value instanceof EnumValue) {
                return ((EnumValue) value).getValue(loader);
            } else if (value instanceof ClassInfo) {
                return ((ClassInfo) value).getValue(loader);
            } else if (value instanceof VmAnnotation) {
                return ((VmAnnotation) value).getValue(loader);
            } else if (value instanceof Object[]) {
                final Object[] arr = (Object[]) value;
                final int max = arr.length;
                final Class<?> compType = ((VmArrayClass<?>) valueType).getComponentType().asClass();
                final Object result = Array.newInstance(compType, max);
                for (int i = 0; i < max; i++) {
                    final Object v = resolve(arr[i], loader, VmType.getObjectClass());
                    if (v != null) {
                        Array.set(result, i, v);
                    }
                }
                return result;
            } else {
                return value;
            }
        }
    }

    /**
     * Class representing an enum_const_value structure.
     *
     * @author Ewout Prangsma (epr@users.sourceforge.net)
     */
    static final class EnumValue {
        private final String typeDescr;

        private final String constName;

        private transient Object value;

        /**
         * @param typeDescr
         * @param constName
         */
        public EnumValue(String typeDescr, String constName) {
            this.typeDescr = typeDescr;
            this.constName = constName;
        }

        /**
         * @return Returns the constName.
         */
        final String getConstName() {
            return constName;
        }

        /**
         * @return Returns the typeDescr.
         */
        final String getTypeDescriptor() {
            return typeDescr;
        }

        /**
         * @return Returns the value.
         * @throws ClassNotFoundException
         */
        @SuppressWarnings("unchecked")
        final Object getValue(VmClassLoader loader)
            throws ClassNotFoundException {
            if (value == null) {
                // Load the enum value
                final VmType enumType = new Signature(typeDescr, loader)
                    .getType();
                value = Enum.valueOf(enumType.asClass(), constName);
            }
            return value;
        }

        /**
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return typeDescr + '#' + constName;
        }
    }

    /**
     * Class representing a single class_info structure.
     *
     * @author Ewout Prangsma (epr@users.sourceforge.net)
     */
    static final class ClassInfo extends VmSystemObject {
        private final String classDescr;

        private transient Class value;

        /**
         * @param classDescr
         */
        public ClassInfo(String classDescr) {
            this.classDescr = classDescr;
        }

        /**
         * @return Returns the value.
         * @throws ClassNotFoundException
         */
        @SuppressWarnings("unchecked")
        final Object getValue(VmClassLoader loader)
            throws ClassNotFoundException {
            if (value == null) {
                value = new Signature(classDescr, loader).getType().asClass();
            }
            return value;
        }
    }

    /**
     * Base class used to implement annotations
     *
     * @author Ewout Prangsma (epr@users.sourceforge.net)
     */
    public static final class ImplBase implements Annotation {

        /**
         * The annotation type
         */
        private VmType<? extends Annotation> annotationType;

        /**
         * The actual values
         */
        protected Object[] values;

        /**
         * The loaded name+value pairs
         */
        private ElementValue[] elemValues;

        /**
         * Should this annotation be inherited
         */
        private boolean inheritable;

        /**
         * Initialize this annotation implementation.
         *
         * @param type
         * @param values
         * @param loader
         * @throws ClassNotFoundException
         */
        final void initialize(VmType<? extends Annotation> type,
                              ElementValue[] values, VmClassLoader loader)
            throws ClassNotFoundException {
            this.annotationType = type;
            this.elemValues = values;
            this.values = new Object[values.length];
            int i = 0;
            for (ElementValue v : values) {
                this.values[i++] = v.getValue(loader, getReturnType(type, v.getName()));
            }
            this.inheritable = type.isAnnotationPresent(Inherited.class);
        }

        private final VmType getReturnType(VmType<?> type, String elemName) {
            for (int i = 0;; i++) {
                final VmMethod m = type.getDeclaredMethod(i);
                if (m.getName().equals(elemName)) {
                    return m.getReturnType();
                }
            }
        }

        /**
         * Should this annotation be inherited.
         */
        public final boolean isInheritable() {
            return inheritable;
        }

        /**
         * @see java.lang.annotation.Annotation#annotationType()
         */
        public Class<? extends Annotation> annotationType() {
            return annotationType.asClass();
        }

        /**
         * Converts to a string representation.
         */
        public final String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append('@');
            sb.append(annotationType.getName());
            sb.append('(');
            final int max = values.length;
            for (int i = 0; i < max; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                if (max > 1) {
                    sb.append(elemValues[i].getName());
                    sb.append('=');
                }
                final Object value = values[i];
                if ((value != null) && (value.getClass().isArray())) {
                    sb.append('[');
                    final int arrLen = Array.getLength(value);
                    for (int j = 0; j < arrLen; j++) {
                        if (j > 0) {
                            sb.append(", ");
                        }
                        sb.append(Array.get(value, j));
                    }
                    sb.append(']');
                } else {
                    sb.append(value);
                }
            }
            sb.append(')');
            return sb.toString();
        }
    }

    /**
     * @return Returns the typeDescr.
     */
    final String getTypeDescriptor() {
        return typeDescr;
    }
}
