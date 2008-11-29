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

import org.jnode.plugin.Extension;
import org.jnode.plugin.ExtensionPoint;
import org.jnode.plugin.ExtensionPointListener;
import org.jnode.plugin.PluginDescriptor;

/**
 * Dummy plugin extension point for configuring plugins outside of the normal JNode framework.
 * Most methods have dummy implementations.  If you come across a use-case that requires
 * a non-dummy implementation, please implement the method in this class rather than
 * subclassing. 
 * 
 * @author Levente S\u00e1ntha
 * @author crawley@jnode.org
 */
public class DummyExtensionPoint implements ExtensionPoint {
    
    private final String id;
    private final String uid;
    private final String name;
    private List<Extension> extensions;

    public DummyExtensionPoint() {
        this("A", "aaa", "B");
    }
    
    public DummyExtensionPoint(String id, String uid, String name) {
        this.id = id;
        this.uid = uid;
        this.name = name;
    }

    public String getSimpleIdentifier() {
        return id;
    }

    public String getUniqueIdentifier() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public Extension[] getExtensions() {
        if (extensions != null) {
            return extensions.toArray(new Extension[extensions.size()]);
        }
        return new Extension[0];
    }
    
    public void addExtension(Extension extension) {
        if (extensions == null) {
            extensions = new ArrayList<Extension>(1);
        }
        extensions.add(extension);
    }

    public void addListener(ExtensionPointListener listener) {
    }

    public void addPriorityListener(ExtensionPointListener listener) {
    }

    public void removeListener(ExtensionPointListener listener) {
    }

    public PluginDescriptor getDeclaringPluginDescriptor() {
        return null;
    }
}
