/*
 * $Id$
 */
package org.jnode.vm.memmgr;

import org.jnode.assembler.ObjectResolver;
import org.jnode.vm.Monitor;
import org.jnode.vm.ObjectVisitor;
import org.jnode.vm.VmAddress;
import org.jnode.vm.VmArchitecture;
import org.jnode.vm.classmgr.VmMethod;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class HeapHelper extends ObjectResolver {

	/**
	 * Mark the given object as finalized.
	 * @param src
	 */
	public abstract void setFinalized(Object src);
	
	/**
	 * Change the color of the given object from oldColor to newColor.
	 * @param dst
	 * @param oldColor
	 * @param newColor
	 * @return True if the color was changed, false if the current color of the object was not equal to oldColor.
	 */
	public abstract boolean atomicChangeObjectColor(Object dst, int oldColor, int newColor);

	public abstract void copy(VmAddress src, VmAddress dst, int size);

	public abstract void clear(VmAddress dst, int size);

	public abstract long addressToLong(VmAddress a);

	public abstract VmAddress allocateBlock(int size);

	public abstract VmAddress getBootHeapStart();

	public abstract VmAddress getBootHeapEnd();

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
	 * Stop and block all threads (on all processors) on a GC safe point.
	 * Only the calling thread (the GC thread) will continue.
	 */
	public abstract void stopThreadsAtSafePoint(); 
	
	/**
	 * Unblock all threads (on all processors).
	 * This method is called after a call a call to {@link #stopThreadsAtSafePoint()}.
	 */
	public abstract void restartThreads();
    
    /**
     * Visit all roots of the object tree.
     * @param visitor
     */
    public abstract void visitAllRoots(ObjectVisitor visitor, VmHeapManager heapManager, ObjectResolver resolver); 
}
