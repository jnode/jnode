/*
 * $Id$
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

    /** The permissions extension point */
    private final ExtensionPoint permissionsEp;
    /** The security policy */
    private final JNodePolicy policy;

    /**
     * @param descriptor
     */
    public SecurityPlugin(PluginDescriptor descriptor) {
        super(descriptor);
        permissionsEp = descriptor.getExtensionPoint("permissions");
        policy = new JNodePolicy(permissionsEp);
        permissionsEp.addListener(this);
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
