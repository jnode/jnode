/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.vm.classmgr;

import org.jnode.vm.objects.VmSystemObject;

/**
 * @author epr
 */
public final class VmLocalVariableTable extends VmSystemObject {

    /**
     * Empty table
     */
    static final VmLocalVariableTable EMPTY = new VmLocalVariableTable(VmLocalVariable.EMPTY);

    /**
     * Local variables
     */
    private final VmLocalVariable[] table;

    /**
     * Create a new instance
     *
     * @param table
     */
    public VmLocalVariableTable(VmLocalVariable[] table) {
        this.table = table;
    }

    /**
     * Gets the number of local variables in this table.
     *
     * @return The length
     */
    public int getLength() {
        return table.length;
    }

    /**
     * Find the local variable at the given program counter (index
     * in bytecode) and the given index.
     *
     * @param pc
     * @param index
     * @return The variable or null if not found.
     */
    public final VmLocalVariable getVariable(int pc, int index) {
        for (VmLocalVariable var : table) {
            if (var.matches(pc, index)) {
                return var;
            }
        }
        return null;
    }

    public final String toString() {
        StringBuilder sb = new StringBuilder();
        for (VmLocalVariable var : table) {
            sb.append(var);
            sb.append(' ');
        }
        return sb.toString();
    }
}
