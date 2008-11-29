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
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginDescriptorListener;
import org.jnode.plugin.PluginException;
import org.jnode.plugin.PluginPrerequisite;
import org.jnode.plugin.Runtime;

/**
 * Dummy plugin descriptor for configuring plugins outside of the normal JNode framework.
 * Most methods have dummy implementations.  If you come across a use-case that requires
 * a non-dummy implementation, please implement the method in this class rather than
 * subclassing. 
 * 
 * @author scrawley@jnode.org
 */
public class DummyPluginDescriptor implements PluginDescriptor {

    private boolean systemPlugin;
    private List<ExtensionPoint> extensionPoints = null;

    public DummyPluginDescriptor(boolean systemPlugin) {
        this.systemPlugin = systemPlugin;
    }

    public void addListener(PluginDescriptorListener listener) {
    }

    public boolean depends(String id) {
        return false;
    }

    public String getCustomPluginClassName() {
        return null;
    }

    public ExtensionPoint getExtensionPoint(String extensionPointId) {
        if (extensionPoints != null) {
            for (ExtensionPoint ep : extensionPoints) {
                if (ep.getSimpleIdentifier().equals(extensionPointId)) {
                    return ep;
                }
            }
        }
        return null;
    }

    public ExtensionPoint[] getExtensionPoints() {
        if (extensionPoints == null) {
            return null;
        } else {
            return extensionPoints.toArray(new ExtensionPoint[extensionPoints.size()]);
        }
    }
    
    public void addExtensionPoint(ExtensionPoint ep) {
        if (extensionPoints == null) {
            extensionPoints = new ArrayList<ExtensionPoint>(1);
        }
        extensionPoints.add(ep);
    }

    public Extension[] getExtensions() {
        return null;
    }

    public String getId() {
        return null;
    }

    public String getLicenseName() {
        return null;
    }

    public String getLicenseUrl() {
        return null;
    }

    public String getName() {
        return null;
    }

    public Plugin getPlugin() throws PluginException {
        return null;
    }

    public ClassLoader getPluginClassLoader() {
        return null;
    }

    public PluginPrerequisite[] getPrerequisites() {
        return null;
    }

    public int getPriority() {
        return 0;
    }

    public String getProviderName() {
        return null;
    }

    public String getProviderUrl() {
        return null;
    }

    public Runtime getRuntime() {
        return null;
    }

    public String getVersion() {
        return null;
    }

    public boolean hasCustomPluginClass() {
        return false;
    }

    public boolean isAutoStart() {
        return false;
    }

    public boolean isFragment() {
        return false;
    }

    public boolean isSystemPlugin() {
        return systemPlugin;
    }

    public void removeListener(PluginDescriptorListener listener) {
        // TODO Auto-generated method stub

    }
}
