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

/**
 * Jumptable constants for the X86 architecture.
 * 
 * The constants is this class must match the structure of the jumptable in
 * vm-jumptable.asm.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class X86JumpTable {

    /** Jumptable offset of vm_athrow */
    public static final int VM_ATHROW_OFS = 0;

    /** Jumptable offset of vm_athrow_notrace */
    public static final int VM_ATHROW_NOTRACE_OFS = 4;

    /** Jumptable offset of vm_invoke_abstract */
    public static final int VM_INVOKE_ABSTRACT_OFS = 8;

    /** Jumptable offset of vm_invoke_method_after_recompile */
    public static final int VM_INVOKE_METHOD_AFTER_RECOMPILE_OFS = 12;

    /** Label name of the jumptable */
    public static final String JUMPTABLE_NAME = "vm_jumpTable";
}
