/*
 * $Id$
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
