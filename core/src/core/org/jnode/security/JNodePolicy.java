/*
 * $Id$
 */
package org.jnode.security;

import gnu.java.security.PolicyFile;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jnode.plugin.ConfigurationElement;
import org.jnode.plugin.Extension;
import org.jnode.plugin.ExtensionPoint;
import org.jnode.system.BootLog;

/**
 * Default policy implementation for JNode.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class JNodePolicy extends PolicyFile {

    /** The permissions extension point */
    private final ExtensionPoint permissionsEp;

    /**
     * Mapping between a codesource (derived from plugin id) and a set of
     * permissions
     */
    private final HashMap codeSource2Permissions = new HashMap();

    /**
     * Initialize this instance.
     */
    public JNodePolicy(ExtensionPoint permissionsEp) {
        super(ClassLoader.getSystemResource("/org/jnode/security/jnode.policy"));
        this.permissionsEp = permissionsEp;
        loadExtensions();
    }

    /**
     * Allow extended classes to add permissions before the permissions
     * collection is set to read-only.
     * 
     * @param codeSource
     * @param perms
     */
    protected void addPermissions(CodeSource codeSource, Permissions perms) {
        for (Iterator it = codeSource2Permissions.entrySet().iterator(); it
                .hasNext();) {
            final Map.Entry e = (Map.Entry) it.next();
            final CodeSource cs = (CodeSource) e.getKey();
            if (cs.implies(codeSource)) {
                // BootLog.info(cs + " -> " + codeSource);
                final PermissionCollection pc = (PermissionCollection) e
                        .getValue();
                for (Enumeration ee = pc.elements(); ee.hasMoreElements();) {
                    perms.add((Permission) ee.nextElement());
                }
            }
        }
    }

    /**
     * @see java.security.Policy#refresh()
     */
    public synchronized void refresh() {
        super.refresh();
        loadExtensions();
    }

    /**
     * Fill the plugin2Permissions map from the extensions connected to the
     * extensionpoint.
     */
    private final void loadExtensions() {
        if (permissionsEp != null) {
            codeSource2Permissions.clear();
            final Extension[] exts = permissionsEp.getExtensions();
            final int count = exts.length;
            for (int i = 0; i < count; i++) {
                loadExtension(exts[ i]);
            }
        }
    }

    private static final Class[] NAME_ACTIONS_ARGS = new Class[] {
            String.class, String.class};

    private static final Class[] NAME_ARGS = new Class[] { String.class};

    private final void loadExtension(Extension ext) {
        final String id = ext.getDeclaringPluginDescriptor().getId();
        final URL url;
        try {
            url = new URL("plugin:" + id + "!/");
            final ClassLoader cl = ext.getDeclaringPluginDescriptor()
                    .getPluginClassLoader();
            final CodeSource cs = new CodeSource(url, null);
            final Permissions perms = new Permissions();
            codeSource2Permissions.put(cs, perms);
            //BootLog.debug("Adding permissions for " + cs);
            final ConfigurationElement[] elems = ext.getConfigurationElements();
            final int count = elems.length;
            for (int i = 0; i < count; i++) {
                final ConfigurationElement elem = elems[ i];
                final String type = elem.getAttribute("class");
                final String name = elem.getAttribute("name");
                final String actions = elem.getAttribute("actions");

                if (type != null) {
                    final Object perm;
                    try {
                        final Class permClass = cl.loadClass(type);
                        if ((name != null) && (actions != null)) {
                            final Constructor c = permClass
                                    .getConstructor(NAME_ACTIONS_ARGS);
                            perm = c.newInstance(new Object[] { name, actions});
                        } else if (name != null) {
                            final Constructor c = permClass
                                    .getConstructor(NAME_ARGS);
                            perm = c.newInstance(new Object[] { name});
                        } else {
                            perm = permClass.newInstance();
                        }
                        final Permission p = (Permission) perm;
                        perms.add(p);
                    } catch (ClassNotFoundException ex) {
                        BootLog
                                .error("Permission class " + type
                                        + " not found");
                    } catch (InstantiationException ex) {
                        BootLog.error("Cannot instantiate permission class "
                                + type);
                    } catch (IllegalAccessException ex) {
                        BootLog.error("Illegal access to permission class "
                                + type);
                    } catch (NoSuchMethodException ex) {
                        BootLog
                                .error("Constructor not found on permission class "
                                        + type);
                    } catch (InvocationTargetException ex) {
                        BootLog.error("Error constructing permission class "
                                + type, ex);
                    } catch (ClassCastException ex) {
                        BootLog.error("Permission class " + type
                                + " not instance of Permission");
                    }
                }
            }
        } catch (MalformedURLException ex) {
            BootLog.error("Cannot create plugin codesource", ex);
        }
    }
}
