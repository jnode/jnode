/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.plugin.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.List;
import org.jnode.bootlog.BootLogInstance;
import org.jnode.nanoxml.XMLElement;
import org.jnode.plugin.Extension;
import org.jnode.plugin.ExtensionPoint;
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginDescriptorListener;
import org.jnode.plugin.PluginException;
import org.jnode.plugin.PluginPrerequisite;
import org.jnode.plugin.PluginReference;
import org.jnode.plugin.Runtime;
import org.jnode.vm.VmSystem;
import org.jnode.vm.classmgr.VmClassLoader;
import org.jnode.vm.isolate.VmIsolateLocal;
import org.jnode.vm.objects.BootableArrayList;

/**
 * Implementation of {@link org.jnode.plugin.PluginDescriptor}.
 *
 * @author epr
 */
public class PluginDescriptorModel extends AbstractModelObject implements
    PluginDescriptor {

    private final boolean autoStart;

    private transient VmIsolateLocal<ClassLoader> classLoaderHolder;

    private transient VmClassLoader vmClassLoader;

    private final String className;

    private final ExtensionPointModel[] extensionPoints;

    private final ExtensionModel[] extensions;

    private final List<FragmentDescriptorModel> fragments;

    private final String id;

    private final PluginJar jarFile;

    private final String licenseName;

    private final String licenseUrl;

    private List<PluginDescriptorListener> listeners;

    private final Object listenerLock = new Object();

    private final String name;

    private Plugin plugin;

    private final String providerName;

    private final String providerUrl;

    private PluginRegistryModel registry;

    private final PluginPrerequisiteModel[] requires;

    private boolean resolved;

    private final RuntimeModel runtime;

    private boolean started = false;

    private boolean starting = false;

    private final Object startLock = new Object();

    private final boolean system;

    private final String version;

    private final int priority;
    
    private PluginReference reference;

    /**
     * Create a new instance
     *
     * @param rootElement the root XMLElement for the XML plugin descriptor
     * @param jarFile the PluginJar object to associate with the descriptor.
     */
    PluginDescriptorModel(PluginJar jarFile, XMLElement rootElement)
        throws PluginException {
        this.jarFile = jarFile;
        this.fragments = new BootableArrayList<FragmentDescriptorModel>();
        id = getAttribute(rootElement, "id", true);
        name = getAttribute(rootElement, "name", true);
        providerName = getAttribute(rootElement, "provider-name", false);
        providerUrl = getAttribute(rootElement, "provider-url", false);
        licenseName = getAttribute(rootElement, "license-name", true);
        licenseUrl = getAttribute(rootElement, "license-url", false);
        version = getAttribute(rootElement, "version", true);
        className = getAttribute(rootElement, "class", false);
        system = getBooleanAttribute(rootElement, "system", false);
        autoStart = getBooleanAttribute(rootElement, "auto-start", false);
        priority = Math.min(MAX_PRIORITY, Math.max(MIN_PRIORITY,
            getIntAttribute(rootElement, "priority", DEFAULT_PRIORITY)));

        // if (registry != null) {
        // registry.registerPlugin(this);
        // }

        final ArrayList<ExtensionPointModel> epList = new ArrayList<ExtensionPointModel>();
        final ArrayList<ExtensionModel> exList = new ArrayList<ExtensionModel>();
        final ArrayList<PluginPrerequisiteModel> reqList = new ArrayList<PluginPrerequisiteModel>();
        RuntimeModel runtime = null;

        initializeRequiresList(reqList, rootElement);

        for (final XMLElement childE : rootElement.getChildren()) {
            final String tag = childE.getName();
            if (tag.equals("extension-point")) {
                final ExtensionPointModel ep = new ExtensionPointModel(this,
                    childE);
                epList.add(ep);
                // if (registry != null) {
                // registry.registerExtensionPoint(ep);
                // }
            } else if (tag.equals("requires")) {
                for (final XMLElement impE : childE.getChildren()) {
                    if (impE.getName().equals("import")) {
                        reqList.add(new PluginPrerequisiteModel(this, impE));
                    } else {
                        throw new PluginException("Unknown element "
                            + impE.getName());
                    }
                }
            } else if (tag.equals("extension")) {
                exList.add(new ExtensionModel(this, childE));
            } else if (tag.equals("runtime")) {
                if (runtime == null) {
                    runtime = new RuntimeModel(this, childE);
                } else {
                    throw new PluginException("duplicate runtime element");
                }
            } else {
                throw new PluginException("Unknown element " + tag);
            }
        }
        if (!epList.isEmpty()) {
            extensionPoints = (ExtensionPointModel[]) epList
                .toArray(new ExtensionPointModel[epList.size()]);
        } else {
            extensionPoints = new ExtensionPointModel[0];
        }

        if (!reqList.isEmpty()) {
            requires = (PluginPrerequisiteModel[]) reqList
                .toArray(new PluginPrerequisiteModel[reqList.size()]);
        } else {
            requires = new PluginPrerequisiteModel[0];
        }

        if (!exList.isEmpty()) {
            extensions = (ExtensionModel[]) exList
                .toArray(new ExtensionModel[exList.size()]);
        } else {
            extensions = new ExtensionModel[0];
        }

        this.runtime = runtime;
    }

    /**
     * Create a plugin descriptor without a PluginJar object
     *
     * @param e the root XMLElement for the XML plugin descriptor
     */
    PluginDescriptorModel(XMLElement e) throws PluginException {
        this(null, e);
    }

    /**
     * Add a fragment to this plugin. This method is called only by
     * {@link FragmentDescriptorModel#resolve(PluginRegistryModel) }.
     *
     * @param fragment
     */
    final void add(FragmentDescriptorModel fragment) {
        fragments.add(fragment);
        if (isSystemPlugin()) {
            VmSystem.getSystemClassLoader().add(fragment);
        }
    }

    /**
     * Add a listener to this descriptor.
     *
     * @param listener
     */
    public final void addListener(PluginDescriptorListener listener) {
        synchronized (listenerLock) {
            if (listeners == null) {
                listeners = new ArrayList<PluginDescriptorListener>();
            }
            listeners.add(listener);
        }
    }

    /**
     * Create the plugin described by this descriptor
     * @return the Plugin so created.
     */
    private Plugin createPlugin() throws PluginException {
        if (className == null) {
            return new EmptyPlugin(this);
        } else {
            try {
                // final Class cls =
                // Thread.currentThread().getContextClassLoader().loadClass(className);
                final ClassLoader cl = getPluginClassLoader();
                // System.out.println("cl=" + cl.getClass().getName());
                final Class<?> cls = cl.loadClass(className);
                // Loading the class may have triggered the creation of the
                // plugin already.
                if (plugin != null) {
                    return plugin;
                } else {
                    final Constructor<?> cons = cls
                        .getConstructor(new Class[]{PluginDescriptor.class});
                    return (Plugin) cons.newInstance(new Object[]{this});
                }
            } catch (ClassNotFoundException ex) {
                throw new PluginException(ex);
            } catch (IllegalAccessException ex) {
                throw new PluginException(ex);
            } catch (InstantiationException ex) {
                throw new PluginException(ex);
            } catch (InvocationTargetException ex) {
                throw new PluginException(ex.getTargetException());
            } catch (NoSuchMethodException ex) {
                throw new PluginException(ex);
            }
        }
    }

    /**
     * Does the plugin described by this descriptor directly depends on the
     * given plugin id.
     *
     * @param id
     * @return True if id is in the list of required plugins of this descriptor,
     *         false otherwise.
     */
    public boolean depends(String id) {
        final PluginPrerequisite[] req = this.requires;
        final int max = req.length;
        for (int i = 0; i < max; i++) {
            if (req[i].getPluginId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public final void firePluginStarted() {
        final List<PluginDescriptorListener> listeners;
        synchronized (listenerLock) {
            if (this.listeners != null) {
                listeners = new ArrayList<PluginDescriptorListener>(
                    this.listeners);
            } else {
                return;
            }
        }
        for (PluginDescriptorListener l : listeners) {
            l.pluginStarted(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    public final void firePluginStopped() {
        final List<PluginDescriptorListener> listeners;
        synchronized (listenerLock) {
            if (this.listeners != null) {
                listeners = new ArrayList<PluginDescriptorListener>(
                    this.listeners);
            } else {
                return;
            }
        }
        for (PluginDescriptorListener l : listeners) {
            l.pluginStop(this);
        }
    }

    /**
     * Gets all fragments attached to this plugin.
     *
     * @return the fragments.
     */
    public final List<FragmentDescriptorModel> fragments() {
        return fragments;
    }

    /**
     * Gets the name of the custom plugin class of this plugin.
     *
     * @return the custom class name or {@code null}
     */
    public String getCustomPluginClassName() {
        return className;
    }

    /**
     * Returns the extension point with the given simple identifier declared in
     * this plug-in, or null if there is no such extension point.
     *
     * @param extensionPointId the simple identifier of the extension point (e.g. "wizard").
     * @return the extension point, or null
     */
    public ExtensionPoint getExtensionPoint(String extensionPointId) {
        final int max = extensionPoints.length;
        for (int i = 0; i < max; i++) {
            final ExtensionPoint ep = extensionPoints[i];
            if (ep.getSimpleIdentifier().equals(extensionPointId)) {
                return ep;
            }
        }
        return null;
    }

    /**
     * Gets all extension-points provided by this plugin
     *
     * @return the extension points as an array.
     */
    public ExtensionPoint[] getExtensionPoints() {
        return extensionPoints;
    }

    /**
     * Gets all extensions provided by this plugin
     *
     * @return the estensions as an array.
     */
    public Extension[] getExtensions() {
        return extensions;
    }

    /**
     * Gets the registry this plugin is declared in.
     */
    /*
     * public PluginRegistry getPluginRegistry() { return registry; }
     */

    /**
     * Gets the unique identifier of this plugin.
     * @return the plugin identifier.
     */
    public String getId() {
        return id;
    }

    /**
     * Get the plugin's JAR file as a PluginJar object.
     * @return the plugin's JAR file or {@code null}
     */
    public final PluginJar getJarFile() {
        return this.jarFile;
    }

    public String getLicenseName() {
        return licenseName;
    }

    public String getLicenseUrl() {
        return licenseUrl;
    }

    /**
     * Gets the human readable name for this plugin
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the plugin that is described by this descriptor. If no plugin class
     * is given in the descriptor, an empty plugin is returned. This method will
     * always returns the same plugin instance for a given descriptor.
     */
    public Plugin getPlugin() throws PluginException {
        if (plugin == null) {
            plugin = createPlugin();
        }
        return plugin;
    }

    /**
     * Gets the classloader of this plugin descriptor.
     *
     * @return ClassLoader
     */
    public ClassLoader getPluginClassLoader() {
        if (classLoaderHolder == null) {
            classLoaderHolder = new VmIsolateLocal<ClassLoader>();
        }
        if (classLoaderHolder.get() == null) {
            if (system) {
                classLoaderHolder.set(ClassLoader.getSystemClassLoader());
            } else {
                classLoaderHolder.set(createClassLoader());
            }
        }
        return classLoaderHolder.get();
    }

    private final PluginClassLoaderImpl createClassLoader() {
        if (registry == null) {
            throw new RuntimeException("Plugin is not resolved yet");
        }
        if (jarFile == null) {
            throw new RuntimeException(
                "Cannot create classloader without a jarfile");
        }
        final int reqMax = requires.length;
        final PluginClassLoaderImpl[] preLoaders = new PluginClassLoaderImpl[reqMax];
        for (int i = 0; i < reqMax; i++) {
            final String reqId = requires[i].getPluginId();
            final PluginDescriptor reqDescr = registry
                .getPluginDescriptor(reqId);
            final ClassLoader cl = reqDescr.getPluginClassLoader();
            if (cl instanceof PluginClassLoaderImpl) {
                preLoaders[i] = (PluginClassLoaderImpl) cl;
            }
        }
        final VmClassLoader currentVmClassLoader = this.vmClassLoader;
        final PrivilegedAction<Object[]> a = new PrivilegedAction<Object[]>() {
            public Object[] run() {
                if (currentVmClassLoader != null) {
                    PluginClassLoaderImpl cl = new PluginClassLoaderImpl(
                        currentVmClassLoader, registry,
                        PluginDescriptorModel.this, jarFile, preLoaders);
                    return new Object[]{cl, currentVmClassLoader};
                } else {
                    PluginClassLoaderImpl cl = new PluginClassLoaderImpl(
                        registry, PluginDescriptorModel.this, jarFile,
                        preLoaders);
                    return new Object[]{cl, cl.getVmClassLoader()};
                }
            }
        };
        final Object[] result = AccessController.doPrivileged(a);
        this.vmClassLoader = (VmClassLoader) result[1];
        return (PluginClassLoaderImpl) result[0];
    }

    /**
     * Gets the plugin's prerequisites.
     *
     * @return the prerequisites as an array.
     */
    public PluginPrerequisite[] getPrerequisites() {
        return requires;
    }

    /**
     * Gets the name of the provider of this plugin
     */
    public String getProviderName() {
        return providerName;
    }

    public String getProviderUrl() {
        return providerUrl;
    }

    /**
     * Gets the runtime information of this descriptor.
     *
     * @return The runtime, or null if no runtime information is provided.
     */
    public Runtime getRuntime() {
        return runtime;
    }

    /**
     * Gets the version of this plugin
     */
    public String getVersion() {
        return version;
    }
    
    public PluginReference getPluginReference() {
        if (reference == null) {
            // lazy creation
            reference = new PluginReference(id, version);
        }
        return reference;
    }

    /**
     * Does this plugin have a custom plugin class specified?
     *
     * @return {@code true} if this plugin has a custom plugin class, {@code false} otherwise.
     */
    public boolean hasCustomPluginClass() {
        return (className != null);
    }

    /**
     * Initialize the list of plugin requirements.
     */
    protected void initializeRequiresList(List<PluginPrerequisiteModel> list,
                                          XMLElement e) throws PluginException {
        // Nothing here
    }

    /**
     * Has this plugin the auto-start flag set. If true, the plugin will be
     * started automatically at boot/load time.
     *
     * @return the plugin's autostart flag.
     */
    public boolean isAutoStart() {
        return autoStart;
    }

    /**
     * Gets the priority of this plugin. Plugins are loaded by increasing
     * priority.
     *
     * @return the plugin's priority.
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Is this a descriptor of a fragment.
     *
     * @return {@code true} for a fragment, {@code false} for a plugin.
     */
    public boolean isFragment() {
        return false;
    }

    /**
     * Is this a descriptor of a system plugin. System plugins are not
     * reloadable.
     *
     * @return {@code true} if the plugin is reloadable.
     */
    public boolean isSystemPlugin() {
        return system;
    }

    /**
     * Remove a fragment from this plugin. This method is called only by
     * {@link FragmentDescriptorModel#unresolve(PluginRegistryModel)}.
     *
     * @param fragment the fagment to be removed
     */
    final void remove(FragmentDescriptorModel fragment) {
        if (isSystemPlugin()) {
            VmSystem.getSystemClassLoader().remove(fragment);
        }
        fragments.remove(fragment);
    }

    /**
     * Remove a listener from this descriptor.
     *
     * @param listener the listener to be removed.
     */
    public final void removeListener(PluginDescriptorListener listener) {
        synchronized (listenerLock) {
            if (listeners != null) {
                listeners.remove(listener);
            }
        }
    }

    /**
     * Resolve all references to (elements of) other plugins
     *
     * @param registry the registry that will be used to resolve references. 
     * @throws PluginException
     */
    public void resolve(PluginRegistryModel registry) throws PluginException {
        if ((this.registry != null) && (this.registry != registry)) {
            throw new SecurityException("Cannot overwrite the registry");
        }
        if (!resolved) {
            // BootLogInstance.get().info("Resolve " + id);
            this.registry = registry;
            registry.registerPlugin(this);
            for (ExtensionPointModel extensionPoint : extensionPoints) {
                extensionPoint.resolve(registry);
            }
            for (PluginPrerequisiteModel require : requires) {
                require.resolve(registry);
            }
            if (runtime != null) {
                runtime.resolve(registry);
            }
            resolved = true;
            for (ExtensionModel extension : extensions) {
                extension.resolve(registry);
            }
        }
    }

    /**
     * Start this plugin. This descriptor is resolved. All plugins that this
     * plugin depends on, are started first.
     * @param registry the registry that will be used to resolve references. 
     */
    final void startPlugin(final PluginRegistryModel registry)
        throws PluginException {
        if (started || starting) {
            return;
        }
        synchronized (startLock) {
            if (started || starting) {
                return;
            }
            starting = true;
        }
        // BootLogInstance.get().info("Resolve on plugin " + getId());
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                public Object run() throws PluginException {
                    resolve(registry);
                    final int reqMax = requires.length;
                    for (int i = 0; i < reqMax; i++) {
                        final String reqId = requires[i].getPluginId();
                        // BootLogInstance.get().info("Start dependency " + reqId);
                        final PluginDescriptorModel reqDescr = (PluginDescriptorModel) registry
                            .getPluginDescriptor(reqId);
                        reqDescr.startPlugin(registry);
                        // Make sure that it is really started
                        reqDescr.waitUntilStarted();
                    }
                    // BootLogInstance.get().info("Start myself " + getId());
                    getPlugin().start();
                    return null;
                }
            });
        } catch (PrivilegedActionException ex) {
            BootLogInstance.get().error("Error starting plugin", ex);
            /*try {
                Thread.sleep(10000);
            } catch (InterruptedException ex1) {
                // Ignore
            }*/
        } finally {
            synchronized (startLock) {
                started = true;
                startLock.notifyAll();
            }
        }
    }

    /**
     * Block the current thread until this plugin is started.
     * If the current thread is starting this plugin, then the thread
     * will not be blocked.
     *
     * @throws PluginException if the blocked thread was interrupted.
     */
    private void waitUntilStarted() throws PluginException {
        if (!started) {
            synchronized (startLock) {
                if (starting) {
                    // I'm the thread doing the start, otherwise
                    // we would have been blocked here.
                    return;
                }
                while (!started) {
                    try {
                        startLock.wait();
                    } catch (InterruptedException ex) {
                        throw new PluginException(ex);
                    }
                }
            }
        }
    }

    public String toString() {
        return getId();
    }

    /**
     * Remove all references to (elements of) other plugin descriptors
     * 
     * @param registry the registry that will be used to unresolve references. 
     * @throws PluginException
     */
    protected void unresolve(PluginRegistryModel registry)
        throws PluginException {
        if (plugin != null) {
            plugin.stop();
        }
        if (runtime != null) {
            runtime.unresolve(registry);
        }
        for (PluginPrerequisiteModel require : requires) {
            require.unresolve(registry);
        }
        for (ExtensionPointModel extensionPoint : extensionPoints) {
            extensionPoint.unresolve(registry);
        }
        for (ExtensionModel extension : extensions) {
            extension.unresolve(registry);
        }
        registry.unregisterPlugin(this);
        resolved = false;
    }
}
