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
 
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.x86.AbstractX86Stream;
import org.jnode.vm.x86.compiler.X86CompilerHelper;

/**
 * @author Patrik Reali
 * 
 * Store information needed by each emitter class
 */
final class EmitterContext {

    /** The output stream */
    private final AbstractX86Stream os;

    /** Helper class */
    private final X86CompilerHelper helper;

    /** Register Pool */
    private final X86RegisterPool pool;

    /** Virtual Stack */
    private final VirtualStack vstack;

    /** Item factory */
    private final ItemFactory itemfac;

    /**
     * Create a new context
     */
    EmitterContext(AbstractX86Stream os, X86CompilerHelper helper,
            VirtualStack vstack, X86RegisterPool pool, ItemFactory ifac) {
        this.os = os;
        this.helper = helper;
        this.vstack = vstack;
        this.pool = pool;
        this.itemfac = ifac;
    }

    /**
     * Return the current emitter's stream
     * 
     * @return the current emitter's stream
     */
    final AbstractX86Stream getStream() {
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
     * return the current emitter's register pool
     * 
     * @return the current emitter's register pool
     */
    final X86RegisterPool getPool() {
        return pool;
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
}
