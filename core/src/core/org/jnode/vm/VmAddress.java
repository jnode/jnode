/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2004 JNode.org
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
package org.jnode.vm;

import org.jnode.util.NumberUtils;

/**
 * Address is not a normal Java object. Instead it is used as a reference
 * to a virtual memory address. Variables of this type are not considered to
 * be objects by the garbage collector.
 * @author epr
 */
public abstract class VmAddress extends VmSystemObject {
	
	/**
	 * Convert a 32-bit (int) address into an Address reference.
	 * @param ptr32
	 * @return Address
	 */
	public static VmAddress valueOf(int ptr32) {
		return Unsafe.intToAddress(ptr32); 
	}

	/**
	 * Convert a 64-bit (long) address into an Address reference.
	 * @param ptr64
	 * @return Address
	 */
	public static VmAddress valueOf(long ptr64) {
		return Unsafe.longToAddress(ptr64); 
	}

	/**
	 * Convert an Object reference into an Address reference.
	 * @param object
	 * @return Address
	 */
	public static VmAddress valueOf(Object object) {
		return Unsafe.addressOf(object); 
	}
	
	/**
	 * Convert the given address to a String.
	 * The length of the string depends of the reference size of
	 * the current architecture.
	 * @param addr
	 * @return
	 */
	public static String toString(VmAddress addr) {
	    final int refsize = Unsafe.getCurrentProcessor().getArchitecture().getReferenceSize();
	    if (refsize == 4) {
	        return NumberUtils.hex(Unsafe.addressToInt(addr));
	    } else {
	        return NumberUtils.hex(Unsafe.addressToLong(addr));	        
	    }
	}
	
	/**
	 * Convert an address to a 32-bit integer.
	 * @param address
	 * @return int
	 */
	public static int as32bit(VmAddress address) {
		return Unsafe.addressToInt(address);
	}

	/**
	 * Convert an address to a 64-bit integer.
	 * @param address
	 * @return long
	 */
	public static long as64bit(VmAddress address) {
		return Unsafe.addressToLong(address);
	}
	
	public static long distance(VmAddress a1, VmAddress a2) {
		return Math.abs(Unsafe.addressToLong(a2) - Unsafe.addressToLong(a1));
	}
	
	public static VmAddress add(VmAddress a, int incValue) {
		return Unsafe.add(a, incValue); 
	}
	
	public static int compare(VmAddress a1, VmAddress a2) {
		return Unsafe.compare(a1, a2);
	}
}
