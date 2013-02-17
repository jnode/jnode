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

import gnu.java.lang.VMClassHelper;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author epr
 */
public final class TIBBuilder implements TIBLayout {

    private static final int DEFAULT_SIZE = MIN_TIB_LENGTH + 16;

    private final ArrayList<Object> tibAsList;
    private final HashMap<String, Integer> nameSignature2Index;

    private Object[] tibAsArray;

    /**
     * Create a blank instance
     *
     * @param vmClass
     */
    public TIBBuilder(VmClassType vmClass, int methodCount) {
        tibAsList = new ArrayList<Object>(DEFAULT_SIZE);
        for (int i = 0; i < MIN_TIB_LENGTH; i++) {
            tibAsList.add(null);
        }
        tibAsList.set(VMTYPE_INDEX, vmClass);
        nameSignature2Index = new HashMap<String, Integer>(methodCount);
    }

    /**
     * Create a copied instance
     *
     * @param vmClass
     * @param src
     */
    public TIBBuilder(VmClassType vmClass, Object[] src, int methodCount) {
        this(vmClass, src.length + methodCount);
        final int length = src.length;
        for (int i = FIRST_METHOD_INDEX; i < length; i++) {
            tibAsList.add(src[i]);
            nameSignature2Index.put(getNameSignature((VmInstanceMethod) src[i]), i);
        }
    }

    /**
     * Add an instance method to this VMT. The VMT offset of the method is
     * adjusted.
     *
     * @param method
     */
    public void add(VmInstanceMethod method) {
        if (tibAsArray != null) {
            throw new RuntimeException(
                "This VMT is locked");
        }
        final int idx = tibAsList.size();
        tibAsList.add(method);
        nameSignature2Index.put(getNameSignature(method), idx);
        method.setTibOffset(idx);
    }

    /**
     * Overwrite a method at a given index The VMT offset of the method is
     * adjusted.
     *
     * @param index
     * @param method
     */
    public void set(int index, VmInstanceMethod method) {
        if (tibAsArray != null) {
            throw new RuntimeException(
                "This VMT is locked");
        }
        if (index < FIRST_METHOD_INDEX) {
            throw new IndexOutOfBoundsException(
                "Index (" + index + ")must be >= " + FIRST_METHOD_INDEX);
        }
        tibAsList.set(index, method);
        nameSignature2Index.put(getNameSignature(method), index);
        method.setTibOffset(index);
    }

    //todo review rules for overriding and do more testing
    /**
     * Check if the method has the correct visibility ofr overriding the method at index.
     * It asumed that the signutares were already checked and they match
     * @param index
     * @param method
     * @return
     */
    boolean overrides(int index, VmInstanceMethod method) {
        if (tibAsArray != null) {
            throw new RuntimeException("This VMT is locked");
        }
        if (index < FIRST_METHOD_INDEX) {
            throw new IndexOutOfBoundsException("Index (" + index + ")must be >= " + FIRST_METHOD_INDEX);
        }
        VmInstanceMethod met = (VmInstanceMethod) tibAsList.get(index);
        return (met.isPublic() || met.isProtected() || (!met.isPrivate() &&
            VMClassHelper.getPackagePortion(met.getDeclaringClass().getName()).
                equals(VMClassHelper.getPackagePortion(method.getDeclaringClass().getName()))));
    }
    
    /**
     * Search through a given VMT for a method with a given name & signature.
     * Return the index in the VMT (0..length-1) if found, -1 otherwise.
     *
     * @param name
     * @param signature
     * @return int
     */
    public int indexOf(String name, String signature) {
        // Note that index 0 of the VMT contain the class, so
        // skip index 0
        final Object idx = nameSignature2Index.get(getNameSignature(name, signature));
        if (idx != null) {
            return (Integer) idx;
        } else {
            return -1;
        }
        /*final int hash = VmMember.calcHashCode(name, signature);
        final int length = tibAsList.size();
        for (int i = FIRST_METHOD_INDEX; i < length; i++) {
            final VmInstanceMethod mts = (VmInstanceMethod) tibAsList.get(i);
            if (hash == mts.getMemberHashCode()) {
                final String mts_name = mts.getName();
                final String mts_signature = mts.getSignature();
                if (name.equals(mts_name) && signature.equals(mts_signature)) { 
                // We found it
                return i; }
            }
        }
        return -1;*/
    }

    /**
     * Convert this TIB to a TIB array. After a call to this method, the TIB
     * cannot be changed anymore.
     *
     * @return The TIB
     */
    public Object[] toArray() {
        if (tibAsArray == null) {
            tibAsArray = new Object[tibAsList.size()];
            tibAsList.toArray(tibAsArray);
        }
        return tibAsArray;
    }

    private final String getNameSignature(String name, String signature) {
        return name + '#' + signature;
    }

    private final String getNameSignature(VmInstanceMethod method) {
        return method.getName() + '#' + method.getSignature();
    }
}
