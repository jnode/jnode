package org.jnode.plugin.model;

import org.jnode.plugin.Extension;
import org.jnode.plugin.ExtensionPoint;
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginDescriptorListener;
import org.jnode.plugin.PluginException;
import org.jnode.plugin.PluginPrerequisite;
import org.jnode.plugin.Runtime;

public class DummyPluginDescriptor implements PluginDescriptor {

    private boolean systemPlugin;

    public DummyPluginDescriptor(boolean systemPlugin) {
        this.systemPlugin = systemPlugin;
    }

    public void addListener(PluginDescriptorListener listener) {
        // TODO Auto-generated method stub

    }

    public boolean depends(String id) {
        // TODO Auto-generated method stub
        return false;
    }

    public String getCustomPluginClassName() {
        // TODO Auto-generated method stub
        return null;
    }

    public ExtensionPoint getExtensionPoint(String extensionPointId) {
        // TODO Auto-generated method stub
        return null;
    }

    public ExtensionPoint[] getExtensionPoints() {
        // TODO Auto-generated method stub
        return null;
    }

    public Extension[] getExtensions() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getId() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getLicenseName() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getLicenseUrl() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    public Plugin getPlugin() throws PluginException {
        // TODO Auto-generated method stub
        return null;
    }

    public ClassLoader getPluginClassLoader() {
        // TODO Auto-generated method stub
        return null;
    }

    public PluginPrerequisite[] getPrerequisites() {
        // TODO Auto-generated method stub
        return null;
    }

    public int getPriority() {
        // TODO Auto-generated method stub
        return 0;
    }

    public String getProviderName() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getProviderUrl() {
        // TODO Auto-generated method stub
        return null;
    }

    public Runtime getRuntime() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getVersion() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean hasCustomPluginClass() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isAutoStart() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isFragment() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isSystemPlugin() {
        return systemPlugin;
    }

    public void removeListener(PluginDescriptorListener listener) {
        // TODO Auto-generated method stub

    }
}
