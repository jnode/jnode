package org.jnode.fs.jifs.files;

import org.jnode.fs.FSDirectory;
import org.jnode.plugin.PluginManager;
import org.jnode.plugin.model.PluginDescriptorModel;
import org.jnode.naming.InitialNaming;

import javax.naming.NameNotFoundException;

/**
 * File, which contains information about the plugin with the same name.
 * 
 * @author Levente S\u00e1ntha
 */
public class JIFSFfragmentJar extends JIFSFpluginJar {
    private String pluginId;
    private String fragmentId;

    public JIFSFfragmentJar() {
        return;
    }

    public JIFSFfragmentJar(String pluginId, String fragmentId, FSDirectory parent) {
        super(fragmentId, parent);
        this.pluginId = pluginId;
        this.fragmentId = fragmentId;
        refresh();
    }

    public void refresh() {
        try {
            final PluginManager mgr = InitialNaming.lookup(PluginManager.NAME);
            PluginDescriptorModel pdm =
                    (PluginDescriptorModel) mgr.getRegistry().getPluginDescriptor(pluginId);
            if (pdm != null) {
                isvalid = false;
                for (PluginDescriptorModel fdm : pdm.fragments()) {
                    if (fdm.getId().equals(fragmentId)) {
                        buffer = fdm.getJarFile().getBuffer();
                        isvalid = buffer != null;
                    }
                }
            } else {
                isvalid = false;
            }
        } catch (NameNotFoundException e) {
            System.err.println(e);
        }
    }
}
