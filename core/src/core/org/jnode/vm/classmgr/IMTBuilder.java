/*
 * $Id$
 */
package org.jnode.vm.classmgr;

/**
 * Class used to construct interface method tables.
 * 
 * @author epr
 */
public class IMTBuilder {

	private final Object[] imt;
	private final boolean[] imtCollisions;
	private final int length;
	/** Number of elements in the IMT that have a collision */
	private int collectionCount;
	
	/**
	 * Initialize a new instance
	 */
	public IMTBuilder() {
		this.length = ObjectLayout.IMT_LENGTH;
		this.imt = new Object[length];
		this.imtCollisions = new boolean[length];
	}
	
	/**
	 * Add the given method to this IMT.
	 * @param method
	 */
	public void add(VmInstanceMethod method) {
		
		final int selector = method.getSelector();
		final int index = selector % length;
		if (!contains(selector, index)) {
		
			if (imt[index] == null) {
				imt[index] = method;
			} else {
				// We have a collision
				if (imtCollisions[index]) {
					// We must extend the collision list that is already there
					final Object[] oldCollisionList = (Object[])imt[index];
					final int oldLength = oldCollisionList.length;
					final Object[] newCollisionList = new Object[oldLength+1];
					System.arraycopy(oldCollisionList, 0, newCollisionList, 0, oldLength);
					newCollisionList[oldLength] = method;
					imt[index] = newCollisionList;
				} else {
					// We must create a new collision list
					final Object[] collisionList = new Object[2];
					collisionList[0] = imt[index];
					collisionList[1] = method;
					imt[index] = collisionList;
					imtCollisions[index] = true;
					collectionCount++;
				}
			}
		}
	}
	
	/**
	 * Does this IMT contain a method with the given selector?
	 * @param selector
	 * @param index
	 * @return boolean
	 */
	private boolean contains(int selector, int index) {
		if (imt[index] == null) {
			return false;
		} else {
			// We have a collision
			if (imtCollisions[index]) {
				// We must extend the collision list that is already there
				final Object[] collisionList = (Object[])imt[index];
				final int length = collisionList.length;
				for (int i = 0; i < length; i++) {
					final VmInstanceMethod method = (VmInstanceMethod)collisionList[i];
					if (selector == method.getSelector()) {
						return true;
					}
				}
				return false;
			} else {
				final VmInstanceMethod method = (VmInstanceMethod)imt[index];
				return (selector == method.getSelector());
			}
		}
	}
	
	/**
	 * Gets the number of elements in the IMT that have a collision.
	 * @return int
	 */
	public int getCollectionCount() {
		return collectionCount;
	}

	/**
	 * Gets the IMT itself.
	 * @return The imt
	 */
	public Object[] getImt() {
		return imt;
	}

	/**
	 * Gets an array describing which IMT indexes have a collision
	 * @return boolean[]
	 */
	public boolean[] getImtCollisions() {
		return imtCollisions;
	}

	/**
	 * Gets the length (in elements) of the IMT.
	 * @return int
	 */
	public int getLength() {
		return length;
	}
}
