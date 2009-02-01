/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.security;

import java.security.Policy;

import org.jnode.plugin.Extension;
import org.jnode.plugin.ExtensionPoint;
import org.jnode.plugin.ExtensionPointListener;
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class SecurityPlugin extends Plugin implements ExtensionPointListener {

    /**
     * The permissions extension point
     */
    private final ExtensionPoint permissionsEp;
    /**
     * The security policy
     */
    private final JNodePolicy policy;

    /**
     * @param descriptor
     */
    public SecurityPlugin(PluginDescriptor descriptor) {
        super(descriptor);
        permissionsEp = descriptor.getExtensionPoint("permissions");
        policy = new JNodePolicy(permissionsEp);
        permissionsEp.addPriorityListener(this);
    }

    /**
     * @see org.jnode.plugin.Plugin#startPlugin()
     */
    protected void startPlugin() throws PluginException {
        // Setup the default policy
        try {
            Policy.setPolicy(policy);
        } catch (Throwable ex) {
            ex.printStackTrace();
            throw new PluginException("setPolicy failed");
        }

        // Setup the securitymanager
        System.setSecurityManager(new JNodeSecurityManager());
    }

    /**
     * @see org.jnode.plugin.Plugin#stopPlugin()
     */
    protected void stopPlugin() throws PluginException {
        // Do nothing
    }

    /**
     * @see org.jnode.plugin.ExtensionPointListener#extensionAdded(org.jnode.plugin.ExtensionPoint,
     *      org.jnode.plugin.Extension)
     */
    public void extensionAdded(ExtensionPoint point, Extension extension) {
        policy.refresh();
    }

    /**
     * @see org.jnode.plugin.ExtensionPointListener#extensionRemoved(org.jnode.plugin.ExtensionPoint,
     *      org.jnode.plugin.Extension)
     */
    public void extensionRemoved(ExtensionPoint point, Extension extension) {
        policy.refresh();
    }
}
