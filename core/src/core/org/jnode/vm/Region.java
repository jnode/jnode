/*
 * $Id$
 */
package org.jnode.vm;

import org.jnode.system.ResourceOwner;


/**
 * A Region is a single part of an addressspace.
 * @author epr
 */
public abstract class Region {
	
	/** The owner of this region */
	private final ResourceOwner owner;
	/** Reference to next region */
	private Region next;
	
	/**
	 * Create a new instance
	 * @param owner
	 */
	protected Region(ResourceOwner owner) {
		this.owner = owner;
	}
	
	/**
	 * Gets the owner of this region.
	 * @return The owner
	 */
	public ResourceOwner getOwner() {
		return owner;
	}
	
	/**
	 * Add the given region to the given list of region, such that
	 * the list is sorted from low to high starting addresses.
	 * @param first
	 * @param addition
	 * @return The new list
	 */
	protected static Region add(Region first, Region addition) {
		if (first == null) {
			return addition;
		}
		if (addition.compareTo(first) < 0) {
			// addition < first
			addition.next  = first;
			return addition;
		} 
		Region r = first;
		while ((r.next != null) && (r.next.compareTo(addition) <= 0)) {
			r = r.next;
		}
		addition.next = r.next;
		r.next = addition;
		return first;
	}
	
	/**
	 * Remove the given region from the given list and return the new list.
	 * @param list
	 * @param removal
	 * @return The new list
	 */
	protected static Region remove(Region list, Region removal) {
		if (list == removal) {
			list = list.next;
			removal.next = null;
			return list;
		} else if (list == null) {
			removal.next = null;
			return null;
		} else {
			Region r = list;
			while ((r.next != null) && (r.next != removal)) {
				r = r.next;
			}
			if (r.next == null) {
				removal.next = null;
				return list;
			} else {
				r.next = removal.next;
				removal.next = null;
				return list;
			}
		}
	}
	
	/**
	 * Is the given testRegion still a free region?
	 * @param list
	 * @param testRegion
	 * @return True if there is no region in the given list that has any overlap
	 *   with the given region.
	 */
	protected static boolean isFree(Region list, Region testRegion) {
		Region r = list;
		while (r != null) {
			if (r.compareTo(testRegion) == 0) {
				return false;
			} 
			r = r.next;
		}
		return true;
	}

	/**
	 * Is the given testRegion still a free region?
	 * @param list
	 * @param testRegion
	 * @return True if there is no region in the given list that has any overlap
	 *   with the given region.
	 */
	protected static Region get(Region list, Region testRegion) {
		Region r = list;
		while (r != null) {
			if (r.compareTo(testRegion) == 0) {
				return r;
			} 
			r = r.next;
		}
		return null;
	}

	/**
	 * Compare to regions.
	 * @param other
	 * @return a negative integer, zero, or a positive integer as 
	 *   this object is less than, equal to, or greater than the 
	 *   specified region. If the regions overlap, 0 is returned.
	 */
	public abstract int compareTo(Region other);

}
