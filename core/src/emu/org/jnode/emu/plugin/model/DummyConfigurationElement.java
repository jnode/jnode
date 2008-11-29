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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jnode.plugin.ConfigurationElement;
import org.jnode.plugin.PluginDescriptor;

/**
 * Dummy configuration element for configuring plugins outside of the normal JNode framework.
 * Most methods have dummy implementations.  If you come across a use-case that requires
 * a non-dummy implementation, please implement the method in this class rather than
 * subclassing. 
 * 
 * @author crawley@jnode.org
 */
public class DummyConfigurationElement implements ConfigurationElement {
    
    private Map<String, String> map = new HashMap<String, String>();

    @Override
    public Set<String> attributeNames() {
        return map.keySet();
    }

    @Override
    public String getAttribute(String name) {
        return map.get(name);
    }

    @Override
    public PluginDescriptor getDeclaringPluginDescriptor() {
        return null;
    }

    @Override
    public ConfigurationElement[] getElements() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    public void addAttribute(String name, String value) {
        map.put(name, value);
    }

}
