/*
 * $Id$
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

    /** All declared fields in the JDK type */
    private final Field[] jdkFields;
    
    /** All declared instance fields that are a one-to-one map */
    private final Field[] jdkInstanceFields;
    
    /** All declared statics fields that are a one-to-one map */
    private final Field[] jdkStaticFields;

    /** All declared instance fields in the jnode type */
    private final List jnodeInstanceFields;

    /** All declared static fields in the jnode type */
    private final List jnodeStaticFields;
    
    private boolean exact = true;
    
    /**
     * Initialize this instance.
     * @param jdkType
     * @param jnodeType
     */
    public FieldInfo(Class jdkType, VmType jnodeType) {
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
     * @return Array of fields, certain offset may contain null.
     */
    public Field[] getJdkInstanceFields() {
        return jdkInstanceFields;
    }
    
    /**
     * Return all declared static fields that have a one-to-one map between JDK type and JNode type.
     * @return Array of fields, certain offset may contain null.
     */
    public Field[] getJdkStaticFields() {
        return jdkStaticFields;
    }
    
    /**
     * Gets a declared jnode instance field at a given index.
     * @param index
     */
    public VmField getJNodeInstanceField(int index) {
        return (VmField)jnodeInstanceFields.get(index);
    }
    
    /**
     * Gets a declared jnode static field at a given index.
     * @param index
     */
    public VmField getJNodeStaticField(int index) {
        return (VmField)jnodeStaticFields.get(index);
    }
    
    /**
     * Convert a list of jnode fields into an array of one-to-one mapped
     * jdk fields.
     * @param jdkType
     * @param jnodeFields
     */
    private final Field[] toJdkFields(Class jdkType, List jnodeFields) {
        final int cnt = jnodeFields.size();
        final Field[] jdkFields = new Field[cnt];
        for (int i = 0; i < cnt; i++) {
            final VmField f = (VmField)jnodeFields.get(i);
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
     * @param jnodeType
     */
    private static List getStaticFields(VmType jnodeType) {
    	if (jnodeType != null) {
    		final int all = jnodeType.getNoDeclaredFields();
    		final ArrayList list = new ArrayList(all);
    		for (int i = 0; i < all; i++) {
    			final VmField f = jnodeType.getDeclaredField(i);
    			if (f.isStatic()) {
    				list.add(f);
    			}
    		}
    		return list;
    	} else {
    		return new ArrayList(0);
    	}
    }

    /**
     * Gets the number of instance fields declared in the given type.
     * @param jnodeType
     */
    private static List getInstanceFields(VmType jnodeType) {
    	if (jnodeType != null) {
    		final int all = jnodeType.getNoDeclaredFields();
    		final ArrayList list = new ArrayList(all);
    		for (int i = 0; i < all; i++) {
    			final VmField f = jnodeType.getDeclaredField(i);
    			if (!f.isStatic()) {
    				list.add(f);
    			}
    		}
    		return list;
    	} else {
    		return new ArrayList(0);
    	}
    }
}
