/*
 * $Id$
 */
package org.jnode.vm.classmgr;

import org.jnode.vm.VmSystemObject;

/**
 * @author epr
 */
public class VmLocalVariableTable extends VmSystemObject {
	
	private final VmLocalVariable[] table;
	
	/**
	 * Create a new instance
	 * @param table
	 */
	public VmLocalVariableTable(VmLocalVariable[] table) {
		this.table = table;
	}
	
	/**
	 * Gets the number of local variables in this table.
	 * @return The length
	 */
	public int getLength() {
		return table.length;
	}
}
