/*
 * $Id$
 */
package org.jnode.vm.classmgr;

import java.util.ArrayList;

/**
 * @author epr
 */
public final class TIBBuilder implements TIBLayout {

	private static final int DEFAULT_SIZE = MIN_TIB_LENGTH + 16;
	private final ArrayList tibAsList;
	private Object[] tibAsArray;
	
	/**
	 * Create a blank instance
	 * @param vmClass
	 */
	public TIBBuilder(VmClassType vmClass) {
		tibAsList = new ArrayList(DEFAULT_SIZE);
		for (int i = 0; i < MIN_TIB_LENGTH; i++) {
			tibAsList.add(null);
		}
		tibAsList.set(VMTYPE_INDEX, vmClass);
	}

	/**
	 * Create a copied instance
	 * @param vmClass
	 * @param src
	 */
	public TIBBuilder(VmClassType vmClass, Object[] src) {
		this(vmClass);
		final int length = src.length;
		for (int i = FIRST_METHOD_INDEX; i < length; i++) {
			tibAsList.add(src[i]);
		}
	}
	
	/**
	 * Add an instance method to this VMT.
	 * The VMT offset of the method is adjusted.
	 * @param method
	 */
	public void add(VmInstanceMethod method) {
		if (tibAsArray != null) {
			throw new RuntimeException("This VMT is locked");
		}
		final int idx = tibAsList.size();
		tibAsList.add(method);
		method.setTibOffset(idx);
	}
	
	/**
	 * Overwrite a method at a given index
	 * The VMT offset of the method is adjusted.
	 * @param index
	 * @param method
	 */
	public void set(int index, VmInstanceMethod method) {
		if (tibAsArray != null) {
			throw new RuntimeException("This VMT is locked");
		}
		if (index < FIRST_METHOD_INDEX) {
			throw new IndexOutOfBoundsException("Index (" + index + ")must be >= " + FIRST_METHOD_INDEX);
		}
		tibAsList.set(index, method);
		method.setTibOffset(index);
	}
	
	/**
	 * Search through a given VMT for a method with a given name & signature.
	 * Return the index in the VMT (0..length-1) if found, -1 otherwise.
	 * @param name
	 * @param signature
	 * @return int
	 */
	public int indexOf(String name, String signature) {
		// Note that index 0 of the VMT contain the class, so
		// skip index 0
		final int length = tibAsList.size();
		for (int i = FIRST_METHOD_INDEX; i < length; i++) {
			final VmInstanceMethod mts = (VmInstanceMethod)tibAsList.get(i);
			final String mts_name = mts.getName();
			final String mts_signature = mts.getSignature();
			if (name.equals(mts_name) && signature.equals(mts_signature)) {
				// We found it
				return i;
			}
		}
		return -1;
	}

	/**
	 * Convert this TIB to a TIB array.
	 * After a call to this method, the TIB cannot be changed anymore.
	 * @return The TIB
	 */
	public Object[] toArray() {
		if (tibAsArray == null) {
			tibAsArray = new Object[tibAsList.size()];
			tibAsList.toArray(tibAsArray);
		}
		return tibAsArray;
	}
}
