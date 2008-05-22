/*
 *  This file is part of the Jikes RVM project (http://jikesrvm.org).
 *
 *  This file is licensed to You under the Common Public License (CPL);
 *  You may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/cpl1.0.php
 *
 *  See the COPYRIGHT.txt file distributed with this work for information
 *  regarding copyright ownership.
 */
 
package org.mmtk.vm;

import static org.jnode.vm.VirtualMemoryRegion.AVAILABLE;

import org.jnode.vm.Vm;

/**
 * This file is a <b>stub</b> file representing all VM-specific constants. This
 * file will be shadowed by a <i>concrete</i> instance of itself supplied by
 * the client VM, populated with VM-specific values. <i>The specific values in
 * this stub file are therefore meaningless.</i>
 * <p>
 * Note that these methods look as though they are constants. This is
 * intentional. They would be constants except that we want MMTk to be
 * Java->bytecode compiled separately, ahead of time, in a VM-neutral way. MMTk
 * must be compiled against this stub, but if these were actual constants rather
 * than methods, then the Java compiler would legally constant propagate and
 * constant fold the values in this file, thereby ignoring the real values held
 * in the concrete VM-specific file. The constants are realized correctly at
 * class initialization time, so the performance overhead of this approach is
 * negligible (and has been measured to be insignificant). $Id:
 * VMConstants.java,v 1.1 2005/05/04 08:59:27 epr Exp $
 * 
 * @author <a href="http://cs.anu.edu.au/~Steve.Blackburn">Steve Blackburn</a>
 * @version $Revision$
 */
public final class VMConstants {
    
    /** 
     * @return The log base two of the size of an address 
     */
    public static final byte LOG_BYTES_IN_ADDRESS() {
        final int refSize = Vm.getArch().getReferenceSize();
        if (refSize == 4) {
            return 2;
        } else if (refSize == 8) {
            return 3;
        } else {
            throw new IllegalArgumentException("Unknown reference size " + refSize);
        }
    }

    /** 
     * @return The log base two of the size of a word
     */
    public static final byte LOG_BYTES_IN_WORD() {
        return LOG_BYTES_IN_ADDRESS();
    }

    /** @return The log base two of the size of an OS page */
    public static final byte LOG_BYTES_IN_PAGE() {
        return Vm.getArch().getLogPageSize(AVAILABLE);
    }

    /** @return The log base two of the minimum allocation alignment */
    public static final byte LOG_MIN_ALIGNMENT() {
        return 2; // 32bit
    }

    /** @return The log base two of (MAX_ALIGNMENT/MIN_ALIGNMENT) */
    public static final byte MAX_ALIGNMENT_SHIFT() {
        return 1; // 64bit-32bit
    }

    /** @return The maximum number of bytes of padding to prepend to an object */
    public static final int MAX_BYTES_PADDING() {
        return 3; // 64bit
    }
}
