/*
 * $Id$
 */
package org.jnode.vm;

import org.jnode.vm.classmgr.VmClassType;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.memmgr.HeapHelper;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class HeapHelperImpl extends HeapHelper implements Uninterruptible {

	/**
	 * @see org.jnode.vm.memmgr.HeapHelper#addressToLong(org.jnode.vm.Address)
	 */
	public final long addressToLong(Address a) {
		return Unsafe.addressToLong(a);
	}

	/**
	 * @see org.jnode.vm.memmgr.HeapHelper#allocateBlock(int)
	 */
	public final Address allocateBlock(int size) {
		return MemoryBlockManager.allocateBlock(size);
	}

	/**
	 * @see org.jnode.vm.memmgr.HeapHelper#clear(org.jnode.vm.Address, int)
	 */
	public final void clear(Address dst, int size) {
		Unsafe.clear(dst, size);
	}

	/**
	 * @see org.jnode.vm.memmgr.HeapHelper#copy(org.jnode.vm.Address, org.jnode.vm.Address, int)
	 */
	public final void copy(Address src, Address dst, int size) {
		Unsafe.copy(src, dst, size);
	}

	/**
	 * @see org.jnode.vm.memmgr.HeapHelper#getBootHeapEnd()
	 */
	public final Address getBootHeapEnd() {
		return Unsafe.getBootHeapEnd();
	}

	/**
	 * @see org.jnode.vm.memmgr.HeapHelper#getBootHeapStart()
	 */
	public final Address getBootHeapStart() {
		return Unsafe.getBootHeapStart();
	}

	/**
	 * @see org.jnode.vm.memmgr.HeapHelper#getByte(java.lang.Object, int)
	 */
	public final byte getByte(Object src, int offset) {
		return Unsafe.getByte(src, offset);
	}

	/**
	 * @see org.jnode.vm.memmgr.HeapHelper#getInt(java.lang.Object, int)
	 */
	public final int getInt(Object src, int offset) {
		return Unsafe.getInt(src, offset);
	}

	/**
	 * @see org.jnode.vm.memmgr.HeapHelper#getObject(java.lang.Object, int)
	 */
	public final Object getObject(Object src, int offset) {
		return Unsafe.getObject(src, offset);
	}

	/**
	 * @see org.jnode.vm.memmgr.HeapHelper#getObjectFlags(java.lang.Object)
	 */
	public final int getObjectFlags(Object src) {
		return Unsafe.getObjectFlags(src);
	}

	/**
	 * @see org.jnode.vm.memmgr.HeapHelper#getStack(org.jnode.vm.VmThread)
	 */
	public final Object getStack(VmThread thread) {
		return thread.getStack();
	}

	/**
	 * @see org.jnode.vm.memmgr.HeapHelper#getVmClass(java.lang.Object)
	 */
	public final VmClassType getVmClass(Object object) {
		return Unsafe.getVmClass(object);
	}

	/**
	 * @see org.jnode.vm.memmgr.HeapHelper#setByte(java.lang.Object, int, byte)
	 */
	public final void setByte(Object dst, int offset, byte value) {
		Unsafe.setByte(dst, offset, value);
	}

	/**
	 * @see org.jnode.vm.memmgr.HeapHelper#setInt(java.lang.Object, int, int)
	 */
	public final void setInt(Object dst, int offset, int value) {
		Unsafe.setInt(dst, offset, value);
	}

	/**
	 * @see org.jnode.vm.memmgr.HeapHelper#setObject(java.lang.Object, int, java.lang.Object)
	 */
	public final void setObject(Object dst, int offset, Object value) {
		Unsafe.setObject(dst, offset, value);
	}

	/**
	 * @see org.jnode.vm.memmgr.HeapHelper#setObjectFlags(java.lang.Object, int)
	 */
	public final void setObjectFlags(Object dst, int flags) {
		Unsafe.setObjectFlags(dst, flags);
	}

	/**
	 * @see org.jnode.assembler.ObjectResolver#add(org.jnode.vm.Address, int)
	 */
	public final Address add(Address address, int offset) {
		return Unsafe.add(address, offset);
	}

	/**
	 * @see org.jnode.assembler.ObjectResolver#addressOf(java.lang.Object)
	 */
	public final Address addressOf(Object object) {
		return Unsafe.addressOf(object);
	}

	/**
	 * @see org.jnode.assembler.ObjectResolver#addressOf32(java.lang.Object)
	 */
	public final int addressOf32(Object object) {
		return Unsafe.addressToInt(Unsafe.addressOf(object));
	}

	/**
	 * @see org.jnode.assembler.ObjectResolver#addressOf64(java.lang.Object)
	 */
	public final long addressOf64(Object object) {
		return Unsafe.addressToLong(Unsafe.addressOf(object));
	}

	/**
	 * @see org.jnode.assembler.ObjectResolver#addressOfArrayData(java.lang.Object)
	 */
	public final Address addressOfArrayData(Object array) {
		return Address.addressOfArrayData(array);
	}

	/**
	 * @see org.jnode.assembler.ObjectResolver#objectAt(org.jnode.vm.Address)
	 */
	public final Object objectAt(Address ptr) {
		return Unsafe.objectAt(ptr);
	}

	/**
	 * @see org.jnode.assembler.ObjectResolver#objectAt32(int)
	 */
	public final Object objectAt32(int ptr) {
		return Unsafe.objectAt(Unsafe.intToAddress(ptr));
	}

	/**
	 * @see org.jnode.assembler.ObjectResolver#objectAt64(long)
	 */
	public final Object objectAt64(long ptr) {
		return Unsafe.objectAt(Unsafe.longToAddress(ptr));
	}
	/**
	 * @see org.jnode.vm.memmgr.HeapHelper#invokeFinalizer(org.jnode.vm.classmgr.VmMethod, java.lang.Object)
	 */
	public final void invokeFinalizer(VmMethod finalizer, Object object) {
		Unsafe.pushObject(object);
		Unsafe.invokeVoid(finalizer);
	}
	
	/**
	 * @see org.jnode.vm.memmgr.HeapHelper#getInflatedMonitor(java.lang.Object, org.jnode.vm.VmArchitecture)
	 */
	public final Monitor getInflatedMonitor(Object object, VmArchitecture arch) {
		return MonitorManager.getInflatedMonitor(object, arch);
	}

}
