/*
 * $Id$
 */
package org.jnode.vm.memmgr;

import org.jnode.vm.PragmaUninterruptible;
import org.jnode.vm.VmSystemObject;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class VmWriteBarrier extends VmSystemObject {

	/**
	 * This method is inlined to implement the write barrier for aastores
	 *
	 * @param ref The base pointer of the array
	 * @param index The array index being stored into.  NOTE: This is the "natural" index; a[3] will pass 3.
	 * @param value The value being stored
	 */
	public abstract void arrayStoreWriteBarrier(Object ref, int index, Object value) 
	throws PragmaUninterruptible;

	/**
	 * This method implements the write barrier for putfields of references
	 *
	 * @param ref    The base pointer of the array
	 * @param offset The offset being stored into.  NOTE: This is in bytes.
	 * @param value  The value being stored
	 */
	public abstract void putfieldWriteBarrier(Object ref, int offset, Object value) 
	throws PragmaUninterruptible;

	/**
	 * This method is inlined to implement the write barrier for putstatics of references
	 *
	 * @param staticsIndex The offset of static field ( from VmStatics)
	 * @param value        The value being stored
	 */
	public abstract void putstaticWriteBarrier(int staticsIndex, Object value) 
	throws PragmaUninterruptible;

	/**
	 * This method generates write barrier entries needed as a consequence of
	 * an explicit user array copies.
	 *
	 * @param array The referring (source) array.
	 * @param start The first "natural" index into the array (e.g. for
	 * <code>a[1]</code>, index = 1).
	 * @param end The last "natural" index into the array
	 */
	public abstract void arrayCopyWriteBarrier(Object array, int start, int end) 
	throws PragmaUninterruptible;
}
