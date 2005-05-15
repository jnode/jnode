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
 
package org.jnode.vm.classmgr;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jnode.vm.Vm;

/**
 * @author epr
 */
public final class VmByteCode extends AbstractCode {

	/** The method i'm a part of */
	private final VmMethod method;
	/** The constant pool where indexes in my bytecode refer to */
	private final VmCP cp;
	/** #Local variables of this method */
	private char noLocals;
	/** Max. #slots taken by this method on the stack */
	private char maxStack;
	/** Bytecode of this method. This is a ByteBuffer or byte[] */
	private Object bytecode;
	/** Exception handler table */
	private VmInterpretedExceptionHandler[] eTable;
	/** Line number table */
	private VmLineNumberMap lnTable;
	/** Data used by the native code compilers */
	private transient Object compilerData;
	
	/**
	 * Create a new instance
	 * @param method
	 * @param bytecode
	 * @param noLocals
	 * @param maxStack
	 * @param eTable
	 * @param lnTable
	 */
	public VmByteCode(VmMethod method, ByteBuffer bytecode, int noLocals, int maxStack, VmInterpretedExceptionHandler[] eTable, VmLineNumberMap lnTable) {
		this.method = method;
		this.cp = method.getDeclaringClass().getCP();
        if (Vm.isWritingImage()) {
            final byte[] buildBytecode = new byte[bytecode.limit()];
            bytecode.get(buildBytecode);
            bytecode.rewind();
            this.bytecode = buildBytecode;
        } else {
            this.bytecode = bytecode;
        }
		this.noLocals = (char)noLocals;
		this.maxStack = (char)maxStack;
		this.eTable = eTable;
		this.lnTable = lnTable;
		//this.locked = false;
	}

	/**
	 * Gets the actual bytecode. 
	 * Do not change the contents of the given array!
	 * @return the code
	 */
	public ByteBuffer getBytecode() {
        final Object bytecode = this.bytecode;
        if (bytecode instanceof ByteBuffer) {
            return ((ByteBuffer)bytecode).duplicate();
        } else {
            final ByteBuffer buf = ByteBuffer.wrap((byte[])bytecode);
            if (Vm.isRunningVm()) {
                this.bytecode = buf;
            }
            return buf.duplicate();
        }
	}
	
	/**
	 * Gets the length of the bytecode
	 * @return the length
	 */
	public int getLength() {
        final Object bytecode = this.bytecode;
        if (bytecode instanceof ByteBuffer) {
            return ((ByteBuffer)bytecode).limit();
        } else {
            return ((byte[])bytecode).length;
        }
	}

	/**
	 * Gets the maximum stack size
	 * @return The maximum stack size
	 */
	public int getMaxStack() {
		return maxStack;
	}

	/**
	 * Gets the number of local variables
	 * @return the number of local variables
	 */
	public int getNoLocals() {
		return noLocals;
	}

	/**
	 * Get the number of exception handlers
	 * @return The number of exception handlers
	 */
	public int getNoExceptionHandlers() {
		return (eTable == null) ? 0 : eTable.length;
	}

	/**
	 * Get the handler PC of the exception handler at a given index
	 * @param index
	 * @return The exception handler
	 */
	public VmInterpretedExceptionHandler getExceptionHandler(int index) {
		if (eTable != null) {
			return eTable[index];
		} else {
			throw new IndexOutOfBoundsException("eTable is null; index " + index);
		}
	}
	
	/**
	 * Gets all exception handler as unmodifiable list of VmInterpretedExceptionHandler
	 * instances.
	 * @return The handlers
	 */
	public List<VmInterpretedExceptionHandler> getExceptionHandlers() {
		if (eTable == null) {
			return Collections.emptyList();
		} else {
			return Arrays.asList(eTable);
		}
	}
	
	/**
	 * Gets the line number table, or null if no line number table exists
	 * for this bytecode.
	 * @return
	 */
	public VmLineNumberMap getLineNrs() {
		return lnTable;
	}
	
	/**
	 * Gets the linenumber of a given program counter.
	 * @param pc
	 * @return The linenumber for the given pc, or -1 is not found.
	 */
	public int getLineNr(int pc) {
		final VmLineNumberMap lnTable = this.lnTable;
		if (lnTable != null) {
			return lnTable.findLineNr(pc);
		} else {
			return -1;
		}
	}
	
	/**
	 * Lock this object.
	 * This will make future modifications on this object fail. 
	 */
	final void lock() {
		//this.locked = true;
	}
	
	/**
	 * Gets the constant pool, where indexes in this bytecode refer to
	 * @return The constant pool
	 */
	public VmCP getCP() {
		return cp;
	}
	
	/**
	 * Gets the method where this bytecode is a part of
	 * @return The method
	 */
	public VmMethod getMethod() {
		return method;
	}

	/**
	 * @return Returns the compilerData.
	 */
	public final Object getCompilerData() {
		return this.compilerData;
	}

	/**
	 * @param compilerData The compilerData to set.
	 */
	public final void setCompilerData(Object compilerData) {
		this.compilerData = compilerData;
	}

}
