/*
 * $Id$
 */
package org.jnode.plugin;

/**
 * Listener to events of an ExtensionPoint.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface ExtensionPointListener {
	
	/**
	 * An extension has been added to an extension point
	 * @param point
	 * @param extension
	 */
	public void extensionAdded(ExtensionPoint point, Extension extension);

	/**
	 * An extension has been removed from an extension point
	 * @param point
	 * @param extension
	 */
	public void extensionRemoved(ExtensionPoint point, Extension extension);

}
