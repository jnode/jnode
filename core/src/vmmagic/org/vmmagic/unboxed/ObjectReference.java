/*
 *  This file is part of the Jikes RVM project (http://jikesrvm.org).
 *
 *  This file is licensed to You under the Common Public License (CPL);
 *  You may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/cpl1.0.php
 *
 *  See the COPYRIGHT.txt file distributed with this work for information
 *  regarding copyright ownership.
 */

package org.vmmagic.unboxed;

import org.jnode.annotation.KernelSpace;
import org.vmmagic.pragma.Uninterruptible;

/**
 * The object reference type is used by the runtime system and collector to
 * represent a type that holds a reference to a single object. We use a separate
 * type instead of the Java Object type for coding clarity, to make a clear
 * distinction between objects the VM is written in, and objects that the VM is
 * managing. No operations that can not be completed in pure Java should be
 * allowed on Object.
 * <br/><br/>
 * <u>JNode specific notes</u> : This class contains some "magic"
 * methods that are interpreted by the VM itself, instead of being executed
 * as normal java methods.  <b>The actual method bodies are never used</b>.
 * See {@link org.jnode.vm.classmgr.VmType VmType} to get the list of "magic" classes
 * and see {@link org.jnode.vm.compiler.BaseMagicHelper.MagicMethod MagicMethod}
 * to get the list of "magic" methods
 * @author Daniel Frampton
 */
public final class ObjectReference implements Uninterruptible {

	/**
	 * Convert from an object to a reference. Note: this is a JikesRVM specific
	 * extension to vmmagic.
	 * 
	 * @param obj
	 *            The object
	 * @return The corresponding reference
	 */
    @KernelSpace
	public static ObjectReference fromObject(Object obj) {
		return null;
	}

	/**
	 * Convert from an address to an object. Note: this is a JikesRVM specific
	 * extension to vmmagic.
	 * 
	 * @param address
	 *            The object address
	 * @return The corresponding reference
	 */
	public static ObjectReference fromAddress(Address address) {
		return null;
	}

	/**
	 * Return a null reference
	 */
	public static final ObjectReference nullReference() {
		return null;
	}

	/**
	 * Convert from an reference to an object. Note: this is a JikesRVM specific
	 * extension to vmmagic.
	 * 
	 * @return The object
	 */
	public Object toObject() {
		return null;
	}

	/**
	 * Get a heap address for the object.
	 */
    @KernelSpace
	public Address toAddress() {
		return null;
	}

	/**
	 * Is this a null reference?
	 */
	public boolean isNull() {
		return false;
	}
}
