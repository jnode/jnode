/*
 * $Id$
 */
package org.jnode.vm;

/**
 * Abstract class used for CPU type identification.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class CpuID {

	/**
	 * Gets the name of this processor.
	 * @return String
	 */
	public abstract String getName();

	/**
	 * Gets a human readable description of the processor and its features.
	 * @return String
	 */
	public abstract String toString();

	/**
	 * Has this processor a given feature.
	 * @param feature A platform specific feature constant.
	 * @return boolean
	 */
	public abstract boolean hasFeature(int feature);
}
