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
 
package org.jnode.assembler.x86;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface X86Operation {

	static final int ADD = 1;
	static final int ADC = 2;
	static final int SUB = 3;
	static final int SBB = 4;
	//static final int IMUL = 5;
	static final int AND = 6;
	static final int OR = 7;
	static final int XOR = 8;
	
	static final int SAL = 10;
	static final int SAR = 11;
	static final int SHL = 12;
	static final int SHR = 13;
}
