/*
 * $Id$
 */
package org.jnode.plugin.model;

import java.net.URL;

import nanoxml.XMLElement;

import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class Factory {

    /**
     * Create a new PluginRegistry.
     * 
     * @param pluginFiles
     * @return
     * @throws PluginException
     */
    public static PluginRegistryModel createRegistry(URL[] pluginFiles)
            throws PluginException {
        return new PluginRegistryModel(pluginFiles);
    }
    
    /**
     * Parse an xml descriptor into the instantiated PluginDescriptor.
     * @param root
     * @return
     * @throws PluginException
     */
    public static PluginDescriptor parseDescriptor(XMLElement root) throws PluginException {
        return parseDescriptor(null, root);
    }
    
    /**
     * Parse an xml descriptor into the instantiated PluginDescriptor.
     * @param root
     * @return
     * @throws PluginException
     */
    static PluginDescriptorModel parseDescriptor(PluginJar jar, XMLElement root) throws PluginException {
        if (root.getName().equals("plugin")) {
            return new PluginDescriptorModel(jar, root);
        } else if (root.getName().equals("fragment")) {
            return new FragmentDescriptorModel(jar, root);                
        } else {
            throw new PluginException("Unknown root tag " + root.getName());
        }
    }
}
