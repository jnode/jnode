/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 
package org.jnode.vm.compiler.ir;

import org.jnode.vm.JvmType;

/**
 * @author Madhu Siddalingaiah
 *
 * An operand of an intermediate operation
 * This could be a constant, local variable, or stack entry
 */
public abstract class Operand {
	/**
	 * NOTE: these values *must* be less than 16!!
	 * @see #getAddressingMode() below
	 */
	public static final int UNKNOWN = 0;
	public static final int BYTE = JvmType.BYTE;
	public static final int SHORT = JvmType.SHORT;
	public static final int CHAR = JvmType.CHAR;
	public static final int INT = JvmType.INT;
	public static final int LONG = JvmType.LONG;
	public static final int FLOAT = JvmType.FLOAT;
	public static final int DOUBLE = JvmType.DOUBLE;
	public static final int REFERENCE = JvmType.REFERENCE;
	
	/*
	 * Addressing mode bits
	 */
	public static final int MODE_CONSTANT = 0x01;
	public static final int MODE_REGISTER = 0x02;
	public static final int MODE_STACK = 0x03;

	private int type;	// One of the above
	
	public Operand(int type) {
		this.type = type;
	}
	
	public int getType() {
		return type;
	}

	/**
	 * @param type
	 */
	public void setType(int type) {
		this.type = type;
	}

	public abstract Operand simplify();
	
	/**
	 * One of MODE_xxx constants defined above
	 * 
	 * @return
	 */
	public abstract int getAddressingMode();
}
