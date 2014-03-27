package org.jnode.plugin.model;

import static org.jnode.plugin.model.XMLConstants.FRAGMENT;
import static org.jnode.plugin.model.XMLConstants.PLUGIN_ID;
import static org.jnode.plugin.model.XMLConstants.PLUGIN_VERSION;

/**
 * Class used to build the XML representation of a {@link org.jnode.plugin.model.FragmentDescriptorBuilder}.
 *
 * @see {@link org.jnode.plugin.model.FragmentDescriptorBuilder#buildXmlElement()}}
 *
 * @author Fabien DUMINY (fduminy at jnode.org)
 */
public class FragmentDescriptorBuilder extends PluginDescriptorBuilder {
    public FragmentDescriptorBuilder(String fragmentId, String name, String licenseName, String fragmentVersion,
                                     String pluginId, String pluginVersion) {
        super(fragmentId, name, licenseName, fragmentVersion);
        setRequiredRootAttribute(PLUGIN_ID, pluginId);
        setRequiredRootAttribute(PLUGIN_VERSION, pluginVersion);
    }

    @Override
    protected final String getRootTagName() {
        return FRAGMENT;
    }
}
