/*
 * $Id$
 */
package org.jnode.plugin.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import nanoxml.XMLElement;

import org.jnode.plugin.Extension;
import org.jnode.plugin.ExtensionPoint;
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.plugin.PluginPrerequisite;
import org.jnode.plugin.PluginRegistry;
import org.jnode.plugin.Runtime;

/**
 * @author epr
 */
public class PluginDescriptorModel extends AbstractModelObject implements PluginDescriptor {

	private final String id;
	private final String providerName;
	private final String name;
	private final String version;
	private final String className;
	private final PluginPrerequisiteModel[] requires;
	private final ExtensionModel[] extensions;
	private final ExtensionPointModel[] extensionPoints;
	private final RuntimeModel runtime;
	private final PluginRegistryModel registry;
	private Plugin plugin;

	/**
	 * Load a plugin-descriptor without a registry.
	 * 
	 * @param e
	 */
	public PluginDescriptorModel(XMLElement e) throws PluginException {
		this(null, e);
	}

	/**
	 * Create a new instance
	 * 
	 * @param e
	 */
	public PluginDescriptorModel(PluginRegistryModel registry, XMLElement e) throws PluginException {
		this.registry = registry;
		id = getAttribute(e, "id", true);
		name = getAttribute(e, "name", true);
		providerName = getAttribute(e, "provider-name", false);
		version = getAttribute(e, "version", true);
		className = getAttribute(e, "class", false);

		final ArrayList epList = new ArrayList();
		final ArrayList exList = new ArrayList();
		final ArrayList reqList = new ArrayList();
		RuntimeModel runtime = null;

		for (Iterator ci = e.getChildren().iterator(); ci.hasNext();) {
			final XMLElement childE = (XMLElement) ci.next();
			final String tag = childE.getName();
			if (tag.equals("extension-point")) {
				final ExtensionPoint ep = new ExtensionPointModel(this, childE);
				epList.add(ep);
				if (registry != null) {
					registry.registerExtensionPoint(ep);
				}
			} else if (tag.equals("requires")) {
				for (Iterator i = childE.getChildren().iterator(); i.hasNext();) {
					final XMLElement impE = (XMLElement) i.next();
					if (impE.getName().equals("import")) {
						reqList.add(new PluginPrerequisiteModel(this, impE));
					} else {
						throw new PluginException("Unknown element " + impE.getName());
					}
				}
			} else if (tag.equals("extension")) {
				exList.add(new ExtensionModel(this, childE));
			} else if (tag.equals("runtime")) {
				if (runtime == null) {
					runtime = new RuntimeModel(this, childE);
				} else {
					throw new PluginException("duplicate runtime element");
				}
			} else {
				throw new PluginException("Unknown element " + tag);
			}
		}
		if (!epList.isEmpty()) {
			extensionPoints = (ExtensionPointModel[]) epList.toArray(new ExtensionPointModel[epList.size()]);
		} else {
			extensionPoints = new ExtensionPointModel[0];
		}

		if (!reqList.isEmpty()) {
			requires = (PluginPrerequisiteModel[]) reqList.toArray(new PluginPrerequisiteModel[reqList.size()]);
		} else {
			requires = new PluginPrerequisiteModel[0];
		}

		if (!exList.isEmpty()) {
			extensions = (ExtensionModel[]) exList.toArray(new ExtensionModel[exList.size()]);
		} else {
			extensions = new ExtensionModel[0];
		}

		this.runtime = runtime;
	}

	/**
	 * Resolve all references to (elements of) other plugin descriptors
	 * 
	 * @throws DocumentException
	 */
	protected void resolve() throws PluginException {
		for (int i = 0; i < extensions.length; i++) {
			extensions[i].resolve();
		}
		for (int i = 0; i < extensionPoints.length; i++) {
			extensionPoints[i].resolve();
		}
		for (int i = 0; i < requires.length; i++) {
			requires[i].resolve();
		}
		if (runtime != null) {
			runtime.resolve();
		}
	}

	/**
	 * Gets the unique identifier of this plugin
	 */
	public String getId() {
		return id;
	}

	/**
	 * Gets the human readable name of this plugin
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the name of the provider of this plugin
	 */
	public String getProviderName() {
		return providerName;
	}

	/**
	 * Gets the version of this plugin
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Gets the required imports
	 * 
	 * @return List&lt;ImportConfig&gt;
	 */
	public PluginPrerequisite[] getPrerequisites() {
		return requires;
	}

	/**
	 * Gets all extension-points provided by this plugin
	 * 
	 * @return List&lt;ExtensionPointConfig&gt;
	 */
	public ExtensionPoint[] getExtensionPoints() {
		return extensionPoints;
	}

	/**
	 * Returns the extension point with the given simple identifier declared in this plug-in, or
	 * null if there is no such extension point.
	 * 
	 * @param extensionPointId
	 *            the simple identifier of the extension point (e.g. "wizard").
	 * @return the extension point, or null
	 */
	public ExtensionPoint getExtensionPoint(String extensionPointId) {
		final int max = extensionPoints.length;
		for (int i = 0; i < max; i++) {
			final ExtensionPoint ep = extensionPoints[i];
			if (ep.getSimpleIdentifier().equals(extensionPointId)) {
				return ep;
			}
		}
		return null;
	}

	/**
	 * Gets all extensions provided by this plugin
	 * 
	 * @return List&lt;ExtensionConfig&gt;
	 */
	public Extension[] getExtensions() {
		return extensions;
	}

	/**
	 * Gets the runtime information of this descriptor.
	 * 
	 * @return The runtime, or null if no runtime information is provided.
	 */
	public Runtime getRuntime() {
		return runtime;
	}

	/**
	 * Gets the registry this plugin is declared in.
	 */
	public PluginRegistry getPluginRegistry() {
		return registry;
	}

	/**
	 * Gets the plugin that is described by this descriptor. If no plugin class is given in the
	 * descriptor, an empty plugin is returned. This method will always returns the same plugin
	 * instance for a given descriptor.
	 */
	public Plugin getPlugin() throws PluginException {
		if (plugin == null) {
			plugin = createPlugin();
		}
		return plugin;
	}

	public String toString() {
		return getId();
	}

	/**
	 * Create the plugin describe by this descriptor
	 */
	private Plugin createPlugin() throws PluginException {
		if (className == null) {
			return new EmptyPlugin(this);
		} else {
			try {
				final Class cls = Thread.currentThread().getContextClassLoader().loadClass(className);
				final Constructor cons = cls.getConstructor(new Class[] { PluginDescriptor.class });
				return (Plugin) cons.newInstance(new Object[] { this });
			} catch (ClassNotFoundException ex) {
				throw new PluginException(ex);
			} catch (IllegalAccessException ex) {
				throw new PluginException(ex);
			} catch (InstantiationException ex) {
				throw new PluginException(ex);
			} catch (InvocationTargetException ex) {
				throw new PluginException(ex.getTargetException());
			} catch (NoSuchMethodException ex) {
				throw new PluginException(ex);
			}
		}
	}
}
