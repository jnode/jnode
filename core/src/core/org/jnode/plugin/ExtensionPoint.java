/*
 * $Id$
 */
package org.jnode.plugin;

/**
 * Descriptor of a well known point in the system.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface ExtensionPoint {
	
	/**
	 * Returns the simple identifier of this extension point. 
	 * This identifier is a non-empty string containing no period 
	 * characters ('.') and is guaranteed to be unique within the 
	 * defining plug-in. 
	 * @return The simple identifier
	 */
	public abstract String getSimpleIdentifier();
	
	/**
	 * Returns the unique identifier of this extension point. 
	 * This identifier is unique within the plug-in registry, and is
	 * composed of the identifier of the plug-in that declared this 
	 * extension point and this extension point's simple identifier. 
	 * @return The unique identifier
	 */
	public abstract String getUniqueIdentifier();
	
	/**
	 * Gets the human readable name of this extensionpoint
	 * @return The name
	 */
	public abstract String getName();
	
	/**
	 * Gets all extensions configured to this extensionpoint.
	 * @return All extensions configured to this extensionpoint.
	 */
	public Extension[] getExtensions();
	
	/**
	 * Add a listener
	 * @param listener
	 */
	public void addListener(ExtensionPointListener listener);
	
	/**
	 * Remove a listener
	 * @param listener
	 */
	public void removeListener(ExtensionPointListener listener);

	/**
	 * Gets the descriptor of the plugin in which this element was declared.
	 * @return The descriptor
	 */
	public PluginDescriptor getDeclaringPluginDescriptor();
}