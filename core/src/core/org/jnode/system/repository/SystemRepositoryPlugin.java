/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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
