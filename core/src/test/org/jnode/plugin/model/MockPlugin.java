package org.jnode.plugin.model;

import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;

/**
 * @author Fabien DUMINY (fduminy at jnode.org)
 */
public class MockPlugin extends Plugin {
    public MockPlugin(PluginDescriptor descriptor) {
        super(descriptor);
    }

    @Override
    protected void startPlugin() throws PluginException {
    }

    @Override
    protected void stopPlugin() throws PluginException {
    }
}
