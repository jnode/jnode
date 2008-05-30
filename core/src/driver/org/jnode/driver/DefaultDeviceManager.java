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

package org.jnode.driver;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import javax.naming.NamingException;
import org.jnode.naming.InitialNaming;
import org.jnode.plugin.ConfigurationElement;
import org.jnode.plugin.Extension;
import org.jnode.plugin.ExtensionPoint;
import org.jnode.plugin.ExtensionPointListener;
import org.jnode.plugin.PluginException;
import org.jnode.system.BootLog;
import org.jnode.work.Work;
import org.jnode.work.WorkUtils;

/**
 * Default device manager.
 *
 * @author epr
 */
public final class DefaultDeviceManager extends AbstractDeviceManager
    implements ExtensionPointListener {

    /**
     * finder extension-point
     */
    private final ExtensionPoint findersEP;

    /**
     * mappers extension-point
     */
    private final ExtensionPoint mappersEP;

    /**
     * Create a new instance
     *
     * @param findersEP
     * @param mappersEP
     */
    public DefaultDeviceManager(ExtensionPoint findersEP,
                                ExtensionPoint mappersEP) {
        super();
        if (findersEP == null) {
            throw new IllegalArgumentException("finders extension-point cannot be null");
        }
        if (mappersEP == null) {
            throw new IllegalArgumentException("mappers extension-point cannot be null");
        }
        this.findersEP = findersEP;
        this.mappersEP = mappersEP;
        findersEP.addListener(this);
        mappersEP.addListener(this);
    }

    /**
     * Start this manager
     *
     * @throws PluginException
     */
    public void start() throws PluginException {
        try {
            InitialNaming.bind(NAME, this);
            WorkUtils.add(new Work("Start device manager") {
                public void execute() {
                    loadExtensions();
                }
            });
        } catch (NamingException ex) {
            throw new PluginException(ex);
        }
    }

    /**
     * @param point
     * @param extension
     */
    public final void extensionAdded(ExtensionPoint point, Extension extension) {
        loadExtensions();
        findDeviceDrivers();
    }

    /**
     * @param point
     * @param extension
     */
    public final void extensionRemoved(ExtensionPoint point, Extension extension) {
        loadExtensions();
    }

    /**
     * Refresh the list of finders, based on the mappers extension-point.
     *
     * @param finders
     */
    protected final void refreshFinders(List<DeviceFinder> finders) {
        finders.clear();
        final Extension[] extensions = findersEP.getExtensions();
        BootLog.debug("Found " + extensions.length + " device finders");

        for (int i = 0; i < extensions.length; i++) {
            final Extension ext = extensions[i];
            final ConfigurationElement[] elements = ext
                .getConfigurationElements();
            for (int j = 0; j < elements.length; j++) {
                configureFinder(finders, elements[j]);
            }
        }
    }

    /**
     * Refresh the list of mappers, based on the mappers extension-point.
     *
     * @param mappers
     */
    protected final void refreshMappers(List<DeviceToDriverMapper> mappers) {
        mappers.clear();
        final Extension[] extensions = mappersEP.getExtensions();
        BootLog.debug("Found " + extensions.length + " mapper extensions");

        // First load all mappers
        for (int i = 0; i < extensions.length; i++) {
            final Extension ext = extensions[i];
            final ConfigurationElement[] elements = ext
                .getConfigurationElements();
            for (int j = 0; j < elements.length; j++) {
                configureMapper(mappers, elements[j]);
            }
        }

        // Now sort them
        Collections.sort(mappers, MapperComparator.INSTANCE);
    }

    /**
     * Configure a finder for a single finder configuration element and add the
     * new finder to the given list.
     *
     * @param finders
     * @param element
     */
    private void configureFinder(List<DeviceFinder> finders, ConfigurationElement element) {
        BootLog.debug("Configure finder: " + element);

        final String elementName = element.getName();
        if (!elementName.equals("finder")) {
            BootLog.warn("Ignoring unrecognised descriptor element: " + elementName);
            return;
        }

        final String className = element.getAttribute("class");
        if (className != null) {
            try {
                final Class cls = Thread.currentThread()
                    .getContextClassLoader().loadClass(className);
                final DeviceFinder finder = (DeviceFinder) cls.newInstance();
                finders.add(finder);
            } catch (ClassNotFoundException ex) {
                BootLog.error("Cannot find finder class " + className);
            } catch (IllegalAccessException ex) {
                BootLog.error("Cannot access finder class " + className);
            } catch (InstantiationException ex) {
                BootLog.error("Cannot instantiate finder class " + className);
            } catch (ClassCastException ex) {
                BootLog.error("Finder class " + className
                    + " does not implement the DeviceFinder interface");
            }
        }
    }

    /**
     * Configure a mapper for a single mapper configuration element and add the
     * new mapper to the given list.
     *
     * @param mappers
     * @param element
     */
    private void configureMapper(List<DeviceToDriverMapper> mappers, ConfigurationElement element) {
        BootLog.debug("Configure mapper: " + element);

        final String elementName = element.getName();
        if (!elementName.equals("mapper")) {
            BootLog.warn("Ignoring unrecognised descriptor element: " + elementName);
            return;
        }

        final String className = element.getAttribute("class");
        if (className != null) {
            try {
                final Class cls = Thread.currentThread()
                    .getContextClassLoader().loadClass(className);
                final DeviceToDriverMapper mapper = newMapperInstance(cls, element);
                mappers.add(mapper);
            } catch (ClassNotFoundException ex) {
                BootLog.error("Cannot find mapper class " + className, ex);
            } catch (IllegalAccessException ex) {
                BootLog.error("Cannot access mapper class " + className, ex);
            } catch (InstantiationException ex) {
                BootLog.error("Cannot instantiate mapper class " + className, ex);
            } catch (ClassCastException ex) {
                BootLog
                    .error("Mapper class "
                        + className
                        + " does not implement the DeviceToDriverMapper interface");
            }
        } else {
            BootLog.error("class attribute required in mapper");
        }
    }

    /**
     * Instantiate the device to driver mapper.
     * First look for a constructor with a ConfigurationElement parameter,
     * if not found, use the default constructor.
     *
     * @param cls
     * @param element
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private DeviceToDriverMapper newMapperInstance(Class cls, ConfigurationElement element)
        throws InstantiationException, IllegalAccessException {
        try {
            final Constructor c = cls.getConstructor(new Class[]{ConfigurationElement.class});
            try {
                return (DeviceToDriverMapper) c.newInstance(new Object[]{element});
            } catch (InvocationTargetException ex1) {
                final InstantiationException ie = new InstantiationException();
                ie.initCause(ex1.getTargetException());
                throw ie;
            }
        } catch (NoSuchMethodException ex) {
            return (DeviceToDriverMapper) cls.newInstance();
        }
    }
}
