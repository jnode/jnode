/*
 * $Id$
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

    /**
     * Create a new context
     */
    EmitterContext(AbstractX86Stream os, X86CompilerHelper helper,
            VirtualStack vstack) {
        this.os = os;
        this.helper = helper;
        this.vstack = vstack;
        pool = new X86RegisterPool();
    }

    /**
     * Return the current emitter's stream
     * 
     * @return the current emitter's stream
     */
    AbstractX86Stream getStream() {
        return os;
    }

    /**
     * return the current emitter's helper
     * 
     * @return the current compiler helper object
     */
    X86CompilerHelper getHelper() {
        return helper;
    }

    /**
     * return the current emitter's register pool
     * 
     * @return the current emitter's register pool
     */
    X86RegisterPool getPool() {
        return pool;
    }

    /**
     * return the current emitter's virtual stack
     * 
     * @return the current emitter's virtual stack
     */
    VirtualStack getVStack() {
        return vstack;
    }
}