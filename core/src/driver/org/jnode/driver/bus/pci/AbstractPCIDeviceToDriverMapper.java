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

package org.jnode.driver.bus.pci;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;
import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;
import org.jnode.plugin.ConfigurationElement;
import org.jnode.util.NumberUtils;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class AbstractPCIDeviceToDriverMapper {

    final Logger log = Logger.getLogger(getClass());
    private final String driverClass;
    private final int[] ids;
    private final int[] masks;
    private final ClassLoader loader;
    private final ConfigurationElement config;

    public AbstractPCIDeviceToDriverMapper(ConfigurationElement config) throws DriverException {
        this.config = config;
        this.loader = config.getDeclaringPluginDescriptor().getPluginClassLoader();
        final String id = config.getAttribute("id");
        if (id == null) {
            throw new DriverException("Specify an id attribute");
        }
        driverClass = config.getAttribute("driver-class");
        if (driverClass == null) {
            throw new DriverException("Specific a driver-class");
        }
        final StringTokenizer tok = new StringTokenizer(id, ":");

        ids = new int[3];
        masks = new int[3];
        for (int i = 0; i < 3; i++) {
            if (tok.hasMoreTokens()) {
                ids[i] = parse(tok.nextToken());
                masks[i] = 0xFFFFFFFF;
            } else {
                ids[i] = 0;
                masks[i] = 0;
            }
        }
    }

    final Driver newDriver(PCIDevice device) {
        try {
            final Class cls = loader.loadClass(driverClass);
            try {
                final Constructor c = cls.getConstructor(new Class[]{ConfigurationElement.class});
                try {
                    return (Driver) c.newInstance(new Object[]{config});
                } catch (InvocationTargetException ex1) {
                    final InstantiationException ie = new InstantiationException();
                    ie.initCause(ex1.getTargetException());
                    throw ie;
                }
            } catch (NoSuchMethodException ex) {
                return (Driver) cls.newInstance();
            }
        } catch (ClassNotFoundException ex) {
            log.error("Cannot find driver class " + driverClass);
        } catch (InstantiationException ex) {
            log.error("Cannot instantiate driver class " + driverClass, ex);
        } catch (IllegalAccessException ex) {
            log.error("Cannot access driver class " + driverClass);
        }

        return null;
    }

    final boolean matches(int major, int sub, int minor) {
        if ((major & masks[0]) != ids[0]) {
            return false;
        }
        if ((sub & masks[1]) != ids[1]) {
            return false;
        }
        if ((minor & masks[2]) != ids[2]) {
            return false;
        }
        return true;
    }

    final boolean hasMinor() {
        return (masks[2] != 0);
    }

    final int parse(String v) {
        return Integer.parseInt(v, 16);
    }

    public String toString() {
        return NumberUtils.hex(ids, 4) + " mask " + NumberUtils.hex(masks, 4);
    }
}
