/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
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
import org.vmmagic.unboxed.Address;

/**
 * Address is not a normal Java object. Instead it is used as a reference
 * to a virtual memory address. Variables of this type are not considered to
 * be objects by the garbage collector.
 * @author epr
 */
public abstract class VmAddress extends VmSystemObject {
	
	/**
	 * Convert the given address to a String.
	 * The length of the string depends of the reference size of
	 * the current architecture.
	 * @param addr
	 * @return
	 */
	public static String toString(VmAddress addr) {
	    final int refsize = VmProcessor.current().getArchitecture().getReferenceSize();
	    if (refsize == 4) {
	        return NumberUtils.hex(Address.fromAddress(addr).toInt());
	    } else {
	        return NumberUtils.hex(Address.fromAddress(addr).toLong());	        
	    }
	}
}
