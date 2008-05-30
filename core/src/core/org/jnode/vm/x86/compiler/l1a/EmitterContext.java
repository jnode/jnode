/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.x86.X86Assembler;
import org.jnode.vm.compiler.EntryPoints;
import org.jnode.vm.x86.compiler.X86CompilerHelper;

/**
 * @author Patrik Reali
 *         <p/>
 *         Store information needed by each emitter class
 */
final class EmitterContext {

    /**
     * The output stream
     */
    private final X86Assembler os;

    /**
     * Helper class
     */
    private final X86CompilerHelper helper;

    /**
     * GPR Register Pool
     */
    private final X86RegisterPool gprPool;

    /**
     * XMM Register Pool
     */
    private final X86RegisterPool xmmPool;

    /**
     * Virtual Stack
     */
    private final VirtualStack vstack;

    /**
     * Item factory
     */
    private final ItemFactory itemfac;

    /**
     * The compiler context
     */
    private final EntryPoints context;

    /**
     * Create a new context
     */
    EmitterContext(X86Assembler os, X86CompilerHelper helper,
                   VirtualStack vstack, X86RegisterPool gprPool,
                   X86RegisterPool xmmPool, ItemFactory ifac,
                   EntryPoints context) {
        this.os = os;
        this.helper = helper;
        this.vstack = vstack;
        this.gprPool = gprPool;
        this.xmmPool = xmmPool;
        this.itemfac = ifac;
        this.context = context;
    }

    /**
     * Return the current emitter's stream
     *
     * @return the current emitter's stream
     */
    final X86Assembler getStream() {
        return os;
    }

    /**
     * return the current emitter's helper
     *
     * @return the current compiler helper object
     */
    final X86CompilerHelper getHelper() {
        return helper;
    }

    /**
     * Gets the current emitter's GPR register pool
     *
     * @return the current emitter's GPR register pool
     */
    final X86RegisterPool getGPRPool() {
        return gprPool;
    }

    /**
     * Gets the current emitter's XMM register pool
     *
     * @return the current emitter's XMM register pool
     */
    final X86RegisterPool getXMMPool() {
        return xmmPool;
    }

    /**
     * return the current emitter's virtual stack
     *
     * @return the current emitter's virtual stack
     */
    final VirtualStack getVStack() {
        return vstack;
    }

    /**
     * @return Returns the itemfac.
     */
    final ItemFactory getItemFactory() {
        return this.itemfac;
    }

    /**
     * Gets the compiler context.
     *
     * @return
     */
    final EntryPoints getContext() {
        return context;
    }
}
