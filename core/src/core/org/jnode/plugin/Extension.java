/*
 * $Id$
 */
package org.jnode.plugin;

/**
 * Descriptor of an "implementation" of an ExtensionPoint.
 * 
 * @see org.jnode.plugin.ExtensionPoint
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface Extension {

	/**
	 * Returns the simple identifier of this extension, or null if this 
	 * extension does not have an identifier. 
	 * This identifier is specified in the plug-in manifest (plugin.xml) 
	 * file as a non-empty string containing no period characters ('.') 
	 * and must be unique within the defining plug-in. 
	 * @return The simple identifier
	 */
	public abstract String getSimpleIdentifier();
	
	/**
	 * Returns the unique identifier of this extension, or null if this 
	 * extension does not have an identifier. 
	 * If available, this identifier is unique within the plug-in registry, 
	 * and is composed of the identifier of the plug-in that declared this 
	 * extension and this extension's simple identifier. 
	 * @return The unique identifier
	 */
	public abstract String getUniqueIdentifier();
	
	/**
	 * Gets all child elements
	 * @return List&lt;Element&gt;
	 */
	public abstract ConfigurationElement[] getConfigurationElements();

	/**
	 * Gets the name of the extension-point this extension connects to.
	 * @return The unique id of the extension-point
	 */
	public String getExtensionPointUniqueIdentifier();

	/**
	 * Gets the descriptor of the plugin in which this element was declared.
	 * @return The descriptor
	 */
	public PluginDescriptor getDeclaringPluginDescriptor();
}