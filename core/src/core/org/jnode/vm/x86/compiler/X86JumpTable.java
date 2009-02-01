/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.vm.x86.compiler;

/**
 * Jumptable constants for the X86 architecture.
 * <p/>
 * The constants is this class must match the structure of the jumptable in
 * vm-jumptable.asm.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class X86JumpTable {

    /**
     * Jumptable index of vm_athrow
     */
    public static final int VM_ATHROW_IDX = 0;

    /**
     * Jumptable index of vm_athrow_notrace
     */
    public static final int VM_ATHROW_NOTRACE_IDX = 1;

    /**
     * Jumptable index of vm_invoke_abstract
     */
    public static final int VM_INVOKE_ABSTRACT_IDX = 2;

    /**
     * Number of entries in the table
     */
    public static final int TABLE_LENGTH = 3;

    /**
     * Number of entries in the table
     */
    public static final String TABLE_ENTRY_LABELS[] = {
        "vm_athrow", "vm_athrow_notrace", "vm_invoke_abstract"
    };

    /**
     * Label name of the jumptable
     */
    public static final String JUMPTABLE_NAME = "vm_jumpTable";
}
