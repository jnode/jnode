/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 
package org.vmmagic.unboxed;

import org.vmmagic.pragma.Uninterruptible;

/**
 * The object reference type is used by the runtime system and collector to
 * represent a type that holds a reference to a single object. We use a separate
 * type instead of the Java Object type for coding clarity, to make a clear
 * distinction between objects the VM is written in, and objects that the VM is
 * managing. No operations that can not be completed in pure Java should be
 * allowed on Object.
 * 
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
