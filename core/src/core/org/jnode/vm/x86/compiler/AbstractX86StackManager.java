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
 
package org.jnode.vm.x86.compiler;

import org.jnode.assembler.x86.X86Register;
import org.jnode.vm.*;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface AbstractX86StackManager {

	/**
	 * Write code to push the contents of the given register on the stack
	 * 
	 * @param reg
	 * @see JvmType
	 */
	public void writePUSH(int jvmType, X86Register reg);

	/**
	 * Write code to push a 64-bit word on the stack
	 * 
	 * @param lsbReg
	 * @param msbReg
	 * @see JvmType
	 */
	public void writePUSH64(int jvmType, X86Register lsbReg, X86Register msbReg);
}
