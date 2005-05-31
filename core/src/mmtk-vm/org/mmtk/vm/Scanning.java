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
 
package org.mmtk.vm;

import org.jnode.vm.Vm;
import org.mmtk.utility.Constants;
import org.mmtk.utility.deque.AddressDeque;
import org.mmtk.utility.deque.AddressPairDeque;
import org.mmtk.utility.scan.Enumerator;
import org.mmtk.utility.scan.PreCopyEnumerator;
import org.mmtk.utility.scan.Scan;
import org.vmmagic.pragma.InlinePragma;
import org.vmmagic.pragma.UninterruptiblePragma;
import org.vmmagic.unboxed.ObjectReference;

/**
 * $Id$
 * 
 * @author <a href="http://cs.anu.edu.au/~Steve.Blackburn">Steve Blackburn</a>
 * @author Perry Cheng
 * @version $Revision$
 * @date $Date$
 */
public final class Scanning implements Constants {

    /** An enumerator used to forward root objects */
    private static PreCopyEnumerator preCopyEnum;

    /**
     * Initialization that occurs at <i>build</i> time. The values of statics
     * at the completion of this routine will be reflected in the boot image.
     * Any objects referenced by those statics will be transitively included in
     * the boot image. This is called from MM_Interface.
     */
    public static final void init() {
        preCopyEnum = new PreCopyEnumerator();
    }

    /**
     * Delegated scanning of a object, processing each pointer field
     * encountered. <b>Jikes RVM never delegates, so this is never executed</b>.
     * 
     * @param object
     *            The object to be scanned.
     */
    public static void scanObject(ObjectReference object) {
        // Should never be reached
        Vm._assert(false);
    }

    /**
     * Delegated enumeration of the pointers in an object, calling back to a
     * given plan for each pointer encountered. <b>Jikes RVM never delegates, so
     * this is never executed</b>.
     * 
     * @param object
     *            The object to be scanned.
     * @param enum
     *            the Enumerate object through which the callback is made
     */
    public static void enumeratePointers(ObjectReference object, Enumerator e) {
        // Should never be reached
        Vm._assert(false);
    }

    /**
     * Prepares for using the <code>computeAllRoots</code> method. The thread
     * counter allows multiple GC threads to co-operatively iterate through the
     * thread data structure (if load balancing parallel GC threads were not
     * important, the thread counter could simply be replaced by a for loop).
     */
    public static void resetThreadCounter() {
    }

    /**
     * Pre-copy all potentially movable instances used in the course of GC. This
     * includes the thread objects representing the GC threads themselves. It is
     * crucial that these instances are forwarded <i>prior</i> to the GC
     * proper. Since these instances <i>are not</i> enqueued for scanning, it
     * is important that when roots are computed the same instances are
     * explicitly scanned and included in the set of roots. The existence of
     * this method allows the actions of calculating roots and forwarding GC
     * instances to be decoupled. The <code>threadCounter</code> must be reset
     * so that load balancing parallel GC can share the work of scanning
     * threads.
     */
    public static void preCopyGCInstances() {
    }

    /**
     * Enumerate the pointers in an object, calling back to a given plan for
     * each pointer encountered. <i>NOTE</i> that only the "real" pointer
     * fields are enumerated, not the TIB.
     * 
     * @param object
     *            The object to be scanned.
     * @param e
     *            the Enumerate object through which the callback is made
     */
    private static void enumeratePointers(Object object, Enumerator e)
            throws UninterruptiblePragma, InlinePragma {
        Scan.enumeratePointers(ObjectReference.fromObject(object), e);
    }

    /**
     * Computes all roots. This method establishes all roots for collection and
     * places them in the root values, root locations and interior root
     * locations queues. This method should not have side effects (such as
     * copying or forwarding of objects). There are a number of important
     * preconditions:
     * <ul>
     * <li> All objects used in the course of GC (such as the GC thread objects)
     * need to be "pre-copied" prior to calling this method.
     * <li> The <code>threadCounter</code> must be reset so that load
     * balancing parallel GC can share the work of scanning threads.
     * </ul>
     * 
     * @param rootLocations
     *            set to store addresses containing roots
     * @param interiorRootLocations
     *            set to store addresses containing return adddresses, or
     *            <code>null</code> if not required
     */
    public static void computeAllRoots(AddressDeque rootLocations,
            AddressPairDeque interiorRootLocations) {
    }

}
