/*
 * $Id: CommandInfo.java 4193 2008-06-04 14:39:34Z crawley $
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
package org.jnode.emu.plugin.model;

import java.util.ArrayList;
import java.util.List;

import org.jnode.plugin.ConfigurationElement;
import org.jnode.plugin.Extension;
import org.jnode.plugin.PluginDescriptor;


/**
 * Dummy plugin extension for configuring plugins outside of the normal JNode framework.
 * Most methods have dummy implementations.  If you come across a use-case that requires
 * a non-dummy implementation, please implement the method in this class rather than
 * subclassing. 
 * 
 * @author crawley@jnode.org
 */
public class DummyExtension implements Extension {
    
    private List<ConfigurationElement> elements;
    
    @Override
    public ConfigurationElement[] getConfigurationElements() {
        if (elements != null) {
            return elements.toArray(new ConfigurationElement[elements.size()]);
        }
        return new ConfigurationElement[0];
    }
    
    public void addElement(ConfigurationElement element) {
        if (elements == null) {
            elements = new ArrayList<ConfigurationElement>(1);
        }
        elements.add(element);
    }

    @Override
    public PluginDescriptor getDeclaringPluginDescriptor() {
        return null;
    }

    @Override
    public String getExtensionPointPluginId() {
        return null;
    }

    @Override
    public String getExtensionPointUniqueIdentifier() {
        return null;
    }

    @Override
    public String getSimpleIdentifier() {
        return null;
    }

    @Override
    public String getUniqueIdentifier() {
        return null;
    }

}
