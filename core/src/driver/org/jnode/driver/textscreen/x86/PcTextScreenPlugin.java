/*
 * $Id$
 */
package org.jnode.driver.textscreen.x86;

import javax.naming.NamingException;

import org.jnode.naming.InitialNaming;
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class PcTextScreenPlugin extends Plugin {

    private final PcTextScreenManager mgr;
    
    /**
     * @param descriptor
     */
    public PcTextScreenPlugin(PluginDescriptor descriptor) {
        super(descriptor);
        this.mgr = new PcTextScreenManager();
    }
    
    /**
     * @see org.jnode.plugin.Plugin#startPlugin()
     */
    protected void startPlugin() throws PluginException {
        try {
            InitialNaming.bind(PcTextScreenManager.NAME, mgr);
        } catch (NamingException ex) {
            throw new PluginException(ex);
        }
    }
    
    /**
     * @see org.jnode.plugin.Plugin#stopPlugin()
     */
    protected void stopPlugin() throws PluginException {
        InitialNaming.unbind(PcTextScreenManager.NAME);
    }
}
