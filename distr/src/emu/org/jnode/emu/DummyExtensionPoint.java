/*
 * $Id$
 */
package org.jnode.emu;

import org.jnode.plugin.Extension;
import org.jnode.plugin.ExtensionPoint;
import org.jnode.plugin.ExtensionPointListener;
import org.jnode.plugin.PluginDescriptor;

/**
 * @author Levente S\u00e1ntha
*/
class DummyExtensionPoint implements ExtensionPoint {
    public String getSimpleIdentifier() {
        return "A";
    }

    public String getUniqueIdentifier() {
        return "aaa";
    }

    public String getName() {
        return "B";
    }

    public Extension[] getExtensions() {
        return new Extension[0];
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
