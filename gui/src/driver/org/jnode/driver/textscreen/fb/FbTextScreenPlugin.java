/*
 * $Id$
 */
package org.jnode.driver.textscreen.fb;

import javax.naming.NamingException;

import org.jnode.naming.InitialNaming;
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;

public final class FbTextScreenPlugin extends Plugin {

    private final FbTextScreenManager mgr;
    
    /**
     * @param descriptor
     */
    public FbTextScreenPlugin(PluginDescriptor descriptor) {
        super(descriptor);
        this.mgr = new FbTextScreenManager();
    }
    
    /**
     * @see org.jnode.plugin.Plugin#startPlugin()
     */
    protected void startPlugin() throws PluginException {
        try {
            InitialNaming.bind(FbTextScreenManager.NAME, mgr);
        } catch (NamingException ex) {
            throw new PluginException(ex);
        }
    }
    
    /**
     * @see org.jnode.plugin.Plugin#stopPlugin()
     */
    protected void stopPlugin() throws PluginException {
        InitialNaming.unbind(FbTextScreenManager.NAME);
    }
}
