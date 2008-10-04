package org.jnode.driver.input;

import javax.naming.NamingException;

import org.jnode.naming.InitialNaming;
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;

public class KeyboardInputPlugin extends Plugin {
    
    private KeyboardLayoutManager mgr;
    
    public KeyboardInputPlugin(PluginDescriptor descriptor) {
        super(descriptor);
    }

    @Override
    protected void startPlugin() throws PluginException {
        try {
            mgr = new KeyboardLayoutManager();
            InitialNaming.bind(KeyboardLayoutManager.NAME, mgr);
            // TODO Load the initial layout mappings from the plugin descriptor.
        } catch (NamingException ex) {
            throw new PluginException(ex);
        }
    }

    @Override
    protected void stopPlugin() throws PluginException {
        // Nothing needs to be done
    }

}
