/*
 * $Id$
 */
package org.jnode.vm.memmgr;

import org.jnode.assembler.ObjectResolver;
import org.jnode.vm.Address;
import org.jnode.vm.Monitor;
import org.jnode.vm.VmArchitecture;
import org.jnode.vm.VmThread;
import org.jnode.vm.classmgr.VmClassType;
import org.jnode.vm.classmgr.VmMethod;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class HeapHelper extends ObjectResolver {

	public abstract VmClassType getVmClass(Object object);

	public abstract byte getByte(Object src, int offset);

	public abstract int getInt(Object src, int offset);

	public abstract Object getObject(Object src, int offset);

	public abstract int getObjectFlags(Object src);

	public abstract void setByte(Object dst, int offset, byte value);

	public abstract void setInt(Object dst, int offset, int value);

	public abstract void setObject(Object dst, int offset, Object value);

	public abstract void setObjectFlags(Object dst, int flags);

	public abstract void copy(Address src, Address dst, int size);

	public abstract void clear(Address dst, int size);

	public abstract long addressToLong(Address a);

	public abstract Object getStack(VmThread thread);

	public abstract Address allocateBlock(int size);

	public abstract Address getBootHeapStart();

	public abstract Address getBootHeapEnd();

	public abstract void invokeFinalizer(VmMethod finalizer, Object object);
	
	public abstract void die(String msg);
	
	/**
	 * Gets the inflated monitor of an object (if any).
	 * 
	 * @param object
	 * @param arch
	 * @return The inflated monitor of the given object, or null if the given object has no
	 *         inflated monitor.
	 */
	public abstract Monitor getInflatedMonitor(Object object, VmArchitecture arch);

	/**
	 * Atomic compare and swap. Compares the int value addressed by the given address with the given old value. If they are equal, the value at the given address is replace by the new value and true
	 * is returned, otherwise nothing is changed and false is returned.
	 * 
	 * @param address
	 * @param oldValue
	 * @param newValue
	 * @return boolean true if the value at address is changed, false otherwise.
	 */
	public abstract boolean atomicCompareAndSwap(Address address, int oldValue, int newValue);

}
