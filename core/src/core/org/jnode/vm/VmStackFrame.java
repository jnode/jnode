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

import org.jnode.vm.classmgr.VmByteCode;
import org.jnode.vm.classmgr.VmCompiledCode;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;

/**
 * A VmFrame is the execution frame (locals & stack) for a method during
 * execution. Note that this class is not meant to be a normal java class,
 * instead it is a record structure that maps directly on how a method frame is
 * push on the stack by the compiler. Don't add any methods, since during
 * runtime instances of this class will have no header and thus no virtual
 * method table.
 */
public final class VmStackFrame extends VmSystemObject {

	public static final int MAGIC_MASK = 0xFFFFFF00;
	public static final int MAGIC_INTERPRETED = 0xF25A1200;
	public static final int MAGIC_COMPILED = 0x21A52F00;

	/** The method executing in this frame */
	private final VmMethod sfMethod;
	/** The program counter, only valid for interpreted methods */
	private final int sfPc;
	/** A magic value used to detect stack errors */
	private final int sfMagic;
	/** A reference to the return address */
	private final Address sfReturnAddress;
	/** A reference to instruction pointer of this frame */
	private final Address sfInstructionPointer;
	
	/**
	 * Initialize this instance.
	 * @param src
	 * @param reader
	 */
	VmStackFrame(Address src, VmStackReader reader, Address ip) {
		this.sfMethod = reader.getMethod(src);
		this.sfPc = reader.getPC(src);
		this.sfMagic = reader.getMagic(src);
		this.sfReturnAddress = reader.getReturnAddress(src);
		this.sfInstructionPointer = ip;
	}

	/**
	 * Is a given stackframe of a interpreted method?
	 * @return boolean
	 */
	protected final boolean isInterpreted() {
		return ((sfMagic & MAGIC_MASK) == MAGIC_INTERPRETED);
	}

	/**
	 * @return Returns the magic.
	 */
	public final int getMagic() {
		return this.sfMagic;
	}

	/**
	 * @return Returns the method.
	 */
	public final VmMethod getMethod() {
		return this.sfMethod;
	}

	/**
	 * @return Returns the pc.
	 */
	public final int getPc() {
		return this.sfPc;
	}

	/**
	 * @return Returns the returnAddress.
	 */
	public final Address getReturnAddress() {
		return this.sfReturnAddress;
	}
	
	/**
	 * Gets the line number of the current instruction of this frame.
	 * @return The line number, or -1 if not found.
	 */
	public final String getLocationInfo() {
		if (isInterpreted()) {
			final VmByteCode bc = sfMethod.getBytecode();
			if (bc != null) {
				return String.valueOf(bc.getLineNr(sfPc - 1));
			} else {
				return "?";
			}
		} else {
			final VmCompiledCode cc = sfMethod.getCompiledCode(getMagic());
			if ((cc != null) && (sfInstructionPointer != null)) {
				return cc.getLocationInfo(sfMethod, sfInstructionPointer);
			} else {
				return "?";
			}
		}
	}

	/**
	 * Convert to a String representation.
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		final VmMethod method = sfMethod;
		final VmType vmClass = (method == null) ? null : method.getDeclaringClass();
		final String cname = (vmClass == null) ? "<unknown class>" : vmClass.getName();
		final String mname = (method == null) ? "<unknown method>" : method.getName();
		final String location = getLocationInfo();

		return cname + "!" + mname + " (" + location + ")";
	}
}
