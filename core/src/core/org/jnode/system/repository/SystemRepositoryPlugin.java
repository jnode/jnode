/*
 * $Id$
 */
package org.jnode.system.repository;

import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.naming.NamingException;

import org.jnode.naming.InitialNaming;
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.system.repository.plugins.PluginLoaderPlugin;

public final class SystemRepositoryPlugin extends Plugin {

    // private static final Logger log =
    // Logger.getLogger(SystemRepositoryPlugin.class);
    private Repository repository;

    /**
     * @param descriptor
     */
    public SystemRepositoryPlugin(PluginDescriptor descriptor) {
        super(descriptor);
    }

    /**
     * @see org.jnode.plugin.Plugin#startPlugin()
     */
    @Override
    protected void startPlugin() throws PluginException {
        final Repository r = (Repository) AccessController
                .doPrivileged(new PrivilegedAction() {
                    public Object run() {
                        RepositoryPlugin plugins = new PluginLoaderPlugin(null);

                        return new Repository(plugins);
                    }
                });
        this.repository = r;
        try {
            InitialNaming.bind(SystemRepository.NAME, r);
        } catch (NamingException ex) {
            throw new PluginException(ex);
        }
        r.start();
    }

    /**
     * @see org.jnode.plugin.Plugin#stopPlugin()
     */
    @Override
    protected void stopPlugin() throws PluginException {
        final Repository r = this.repository;
        this.repository = null;
        if (r != null) {
            r.stop();
        }
        InitialNaming.unbind(SystemRepository.class);
    }
}
