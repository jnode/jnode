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
 
package org.jnode.build;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.jnode.vm.classmgr.VmField;
import org.jnode.vm.classmgr.VmType;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class FieldInfo {

    /**
     * All declared fields in the JDK type.
     */
    private final Field[] jdkFields;

    /**
     * All declared instance fields that are a one-to-one map.
     */
    private final Field[] jdkInstanceFields;

    /**
     * All declared statics fields that are a one-to-one map.
     */
    private final Field[] jdkStaticFields;

    /**
     * All declared instance fields in the jnode type.
     */
    private final List<VmField> jnodeInstanceFields;

    /**
     * All declared static fields in the jnode type.
     */
    private final List<VmField> jnodeStaticFields;

    private boolean exact = true;

    /**
     * Initialize this instance.
     *
     * @param jdkType
     * @param jnodeType
     */
    public FieldInfo(Class<?> jdkType, VmType jnodeType) {
        this.jdkFields = jdkType.getDeclaredFields();

        this.jnodeInstanceFields = getInstanceFields(jnodeType);
        this.jnodeStaticFields = getStaticFields(jnodeType);

        this.jdkInstanceFields = toJdkFields(jdkType, jnodeInstanceFields);
        this.jdkStaticFields = toJdkFields(jdkType, jnodeStaticFields);
    }

    /**
     * Is this class an exact match between JDK & JNode type.
     */
    public boolean isExact() {
        return exact;
    }

    /**
     * Return all declared instance fields that have a one-to-one map between JDK type and JNode type.
     *
     * @return Array of fields, certain offset may contain null.
     */
    public Field[] getJdkInstanceFields() {
        return jdkInstanceFields;
    }

    /**
     * Return all declared static fields that have a one-to-one map between JDK type and JNode type.
     *
     * @return Array of fields, certain offset may contain null.
     */
    public Field[] getJdkStaticFields() {
        return jdkStaticFields;
    }

    /**
     * Gets a declared jnode instance field at a given index.
     *
     * @param index
     */
    public VmField getJNodeInstanceField(int index) {
        return jnodeInstanceFields.get(index);
    }

    /**
     * Gets a declared jnode static field at a given index.
     *
     * @param index
     */
    public VmField getJNodeStaticField(int index) {
        return jnodeStaticFields.get(index);
    }

    /**
     * Convert a list of jnode fields into an array of one-to-one mapped
     * jdk fields.
     *
     * @param jdkType
     * @param jnodeFields
     */
    private final Field[] toJdkFields(Class jdkType, List<VmField> jnodeFields) {
        final int cnt = jnodeFields.size();
        final Field[] jdkFields = new Field[cnt];
        for (int i = 0; i < cnt; i++) {
            final VmField f = jnodeFields.get(i);
            try {
                jdkFields[i] = jdkType.getDeclaredField(f.getName());
                jdkFields[i].setAccessible(true);
                final boolean jdkStatic = ((jdkFields[i].getModifiers() & Modifier.STATIC) != 0);
                if (f.isStatic() != jdkStatic) {
                    jdkFields[i] = null;
                    exact = false;
                }
            } catch (SecurityException ex) {
                if (!f.isTransient()) {
                    exact = false;
                }
                // Ignore
            } catch (NoSuchFieldException ex) {
                if (!f.isTransient()) {
                    exact = false;
                }
                // Ignore
            }
        }
        return jdkFields;
    }

    /**
     * Gets the number of static fields declared in the given type.
     *
     * @param jnodeType
     */
    private static List<VmField> getStaticFields(VmType jnodeType) {
        if (jnodeType != null) {
            final int all = jnodeType.getNoDeclaredFields();
            final ArrayList<VmField> list = new ArrayList<VmField>(all);
            for (int i = 0; i < all; i++) {
                final VmField f = jnodeType.getDeclaredField(i);
                if (f.isStatic()) {
                    list.add(f);
                }
            }
            return list;
        } else {
            return new ArrayList<VmField>(0);
        }
    }

    /**
     * Gets the number of instance fields declared in the given type.
     *
     * @param jnodeType
     */
    private static List<VmField> getInstanceFields(VmType jnodeType) {
        if (jnodeType != null) {
            final int all = jnodeType.getNoDeclaredFields();
            final ArrayList<VmField> list = new ArrayList<VmField>(all);
            for (int i = 0; i < all; i++) {
                final VmField f = jnodeType.getDeclaredField(i);
                if (!f.isStatic()) {
                    list.add(f);
                }
            }
            return list;
        } else {
            return new ArrayList<VmField>(0);
        }
    }
}
