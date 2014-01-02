/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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

package org.jnode.driver.input;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.log4j.Logger;
import org.jnode.plugin.ConfigurationElement;
import org.jnode.plugin.Extension;
import org.jnode.plugin.ExtensionPoint;
import org.jnode.plugin.ExtensionPointListener;

public class MouseProtocolHandlerManager implements ExtensionPointListener {

    /**
     * Name of mouse protocol handlers extension point
     */
    public static final String EP_NAME = "org.jnode.driver.input.mouse-protocol-handlers";

    /**
     * The name used to bind this manager in the InitialNaming namespace.
     */
    public static Class<MouseProtocolHandlerManager> NAME = MouseProtocolHandlerManager.class;

    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(MouseInterpreter.class);

    /**
     * List of valid protocol handlers
     */
    private static final List<MouseProtocolHandler> protocolHandlers = new ArrayList<MouseProtocolHandler>();

    private final ExtensionPoint mouseProtocolHandlersEP;

    /**
     * Default ctor
     *
     * @param mouseProtocolHandlersEP
     */
    public MouseProtocolHandlerManager(ExtensionPoint mouseProtocolHandlersEP) {
        this.mouseProtocolHandlersEP = mouseProtocolHandlersEP;
        for (Extension e : mouseProtocolHandlersEP.getExtensions()) {
            addHandlers(e);
        }
        mouseProtocolHandlersEP.addListener(this);
    }

    /**
     * Gets a collection of all registered protocol handlers.
     */
    public synchronized Collection<MouseProtocolHandler> protocolHandlers() {
        return new ArrayList<MouseProtocolHandler>(protocolHandlers);
    }

    /**
     * Add all mouse protocol handlers in the extension data to the map.
     */
    @Override
    public void extensionAdded(ExtensionPoint point, Extension extension) {
        if (point.equals(mouseProtocolHandlersEP)) {
            addHandlers(extension);
        }
    }

    /**
     * Remove from the map any mouse protocol handlers that are identical to the extension data.
     */
    @Override
    public void extensionRemoved(ExtensionPoint point, Extension extension) {
        if (point.equals(mouseProtocolHandlersEP)) {
            removeHandlers(extension);
        }
    }

    /**
     * Add handlers defined in the given extension.
     *
     * @param extension
     */
    private synchronized void addHandlers(Extension extension) {
        for (ConfigurationElement element : extension.getConfigurationElements()) {
            if (element.getName().equals("handler")) {
                String name = element.getAttribute("name");
                String className = element.getAttribute("class");
                if (name != null && className != null) {
                    add(name, className);
                }
            }
        }
    }

    /**
     * Remove handlers found in the given extension
     *
     * @param extension
     */
    private synchronized void removeHandlers(Extension extension) {
        for (ConfigurationElement element : extension.getConfigurationElements()) {
            if (element.getName().equals("handler")) {
                String name = element.getAttribute("name");
                String className = element.getAttribute("class");
                if (name == null || className == null) {
                    continue;
                }
                for (MouseProtocolHandler handler : protocolHandlers) {
                    if ((handler.getName() == name) && (handler.getClass().getName() == className)) {
                        protocolHandlers.remove(handler);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Add a handler with given name and class name.
     *
     * @param name
     * @param className
     */
    private void add(String name, String className) {
        for (MouseProtocolHandler handler : protocolHandlers) {
            if (handler.getName() == name) {
                log.error("Duplicate mouse protocol handler name: " + name);
                return;
            }
        }

        try {
            // FIXME ... think about whether using the current thread's class
            // loader might present a security issue.
            final ClassLoader cl = Thread.currentThread().getContextClassLoader();
            MouseProtocolHandler handler = (MouseProtocolHandler) cl.loadClass(className).newInstance();
            protocolHandlers.add(handler);

        } catch (ClassNotFoundException ex) {
            log.error("Mouse protocol handler class not found: " + className);
        } catch (Exception ex) {
            // Could be an access, and instantiation or a typecast exception ...
            log.error("Error instantiating keyboard interpreter class:" + className, ex);
        }
    }
}
