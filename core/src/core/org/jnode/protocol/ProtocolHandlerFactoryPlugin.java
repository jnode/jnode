/*
 * $Id$
 */
package org.jnode.protocol;

import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;

import org.jnode.plugin.ConfigurationElement;
import org.jnode.plugin.Extension;
import org.jnode.plugin.ExtensionPoint;
import org.jnode.plugin.ExtensionPointListener;
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.system.BootLog;

/**
 * Plugin that installs itself as an URL Handler Factory.
 * 
 * This plugin has a "handlers" extension point which should be used
 * to register protocol handlers.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ProtocolHandlerFactoryPlugin extends Plugin implements
        URLStreamHandlerFactory, ExtensionPointListener  {

    private final ExtensionPoint handlersEp;
    private final HashMap handlerClasses = new HashMap();
    
    /**
     * @param descriptor
     */
    public ProtocolHandlerFactoryPlugin(PluginDescriptor descriptor) {
        super(descriptor);
        this.handlersEp = descriptor.getExtensionPoint("protocols");
        reloadHandlers();
    }

    /**
     * @see org.jnode.plugin.Plugin#startPlugin()
     */
    protected void startPlugin() throws PluginException {
        handlersEp.addListener(this);
        try {
            setHandlerFactory(this);
        } catch (SecurityException ex) {
            BootLog.error("Cannot set URL Handler Factory");
        }
    }

    /**
     * @see org.jnode.plugin.Plugin#stopPlugin()
     */
    protected void stopPlugin() throws PluginException {
        handlersEp.removeListener(this);
        try {
            setHandlerFactory(null);
        } catch (SecurityException ex) {
            BootLog.error("Cannot reset URL Handler Factory");
        }
    }

    /**
     * @see java.net.URLStreamHandlerFactory#createURLStreamHandler(java.lang.String)
     */
    public synchronized URLStreamHandler createURLStreamHandler(String protocol) {
        final Class cls = (Class)handlerClasses.get(protocol);
        if (cls != null) {
            try {
                return (URLStreamHandler)cls.newInstance();
            } catch (InstantiationException ex) {
                BootLog.error("Cannot instantiate " + cls.getName());
            } catch (IllegalAccessException ex) {
                BootLog.error("Illegal access to " + cls.getName());
            }
        } 
        return null;
    }

    /**
     * @see org.jnode.plugin.ExtensionPointListener#extensionAdded(org.jnode.plugin.ExtensionPoint, org.jnode.plugin.Extension)
     */
    public void extensionAdded(ExtensionPoint point, Extension extension) {
        reloadHandlers();
    }
    
    /**
     * @see org.jnode.plugin.ExtensionPointListener#extensionRemoved(org.jnode.plugin.ExtensionPoint, org.jnode.plugin.Extension)
     */
    public void extensionRemoved(ExtensionPoint point, Extension extension) {
        reloadHandlers();
    }
    
    private void setHandlerFactory(final URLStreamHandlerFactory factory) {
        AccessController.doPrivileged(new PrivilegedAction() {

            public Object run() {
                URL.setURLStreamHandlerFactory(factory);
                return null;
            }
        });
    }
    
    private synchronized void reloadHandlers() {
        handlerClasses.clear();
        final Extension[] exts = handlersEp.getExtensions();
        final int count = exts.length;
        for (int i = 0; i < count; i++) {
            loadHandlers(exts[i]);
        }
    }
    
    private void loadHandlers(Extension ext) {
        final ConfigurationElement[] elems = ext.getConfigurationElements();
        final int count = elems.length;
        final ClassLoader cl = ext.getDeclaringPluginDescriptor().getPluginClassLoader();
        for (int i = 0; i < count; i++) {
            final ConfigurationElement elem = elems[i];
            if (elem.getName().equals("handler")) {
                final String protocol = elem.getAttribute("protocol");
                final String className = elem.getAttribute("class");
                if ((protocol != null) && (className != null)) {
                    try {
                        final Class cls = cl.loadClass(className);
                        handlerClasses.put(protocol, cls);
                    } catch (ClassNotFoundException ex) {
                        BootLog.error("Cannot load protocol handler class " + className);
                    }
                }
            }
        }
    }
}
