/*
 * $Id$
 *
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
 
package org.jnode.driver.system.acpi.vm;

import java.util.HashMap;
import java.util.Map;
import org.jnode.driver.system.pnp.PnP;

/**
 * Device.
 * <p/>
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Licence: GNU LGPL
 * </p>
 * <p>
 * </p>
 *
 * @author Francois-Frederic Ozog
 * @version 1.0
 */

public class Device extends NameSpace {

    private final Map<String, String> addresses = new HashMap<String, String>();

    public Device(String name) {
        super(name);
    }

    public Device(NameSpace root, String name) {
        super(root, name);
    }

    public void addAddress(String addressType, String address) {
        addresses.put(addressType, address);
    }

    public String toString(String prefix) {
        String className = this.getClass().getName();
        String n = className.substring(className.lastIndexOf(".") + 1);
        StringBuffer buffer = new StringBuffer();
        buffer.append(prefix);
        buffer.append(n);
        buffer.append(": ");
        buffer.append(getName());
        buffer.append(" (");
        for (Map.Entry<String, String> entry : addresses.entrySet()) {
            final String addressType = entry.getKey();
            String address = entry.getValue();
            if (addressType.equals("_HID")) {
                address = PnP.getDescription(address);
            }
            buffer.append(address);
        }
        buffer.append(")");
        return buffer.toString();
    }

}
