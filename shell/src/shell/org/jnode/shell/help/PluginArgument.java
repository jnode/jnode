/*
 * $Id$
 */
package org.jnode.shell.help;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.naming.NameNotFoundException;

import org.jnode.naming.InitialNaming;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginManager;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class PluginArgument extends Argument {

    /**
     * @param name
     * @param description
     */
    public PluginArgument(String name, String description) {
        super(name, description);
    }

    /**
     * @param name
     * @param description
     * @param multi
     */
    public PluginArgument(String name, String description, boolean multi) {
        super(name, description, multi);
    }

    public String complete(String partial) {
        final List ids = new ArrayList();
        try {
            // get the plugin manager
            final PluginManager piMgr = (PluginManager) InitialNaming
                    .lookup(PluginManager.NAME);

            // collect matching plugin id's
            for (Iterator i = piMgr.getRegistry().getDescriptorIterator(); i
                    .hasNext();) {
                final PluginDescriptor descr = (PluginDescriptor) i.next();
                final String id = descr.getId();
                if (id.startsWith(partial)) {
                    ids.add(id);
                }
            }
            return complete(partial, ids);
        } catch (NameNotFoundException ex) {
            // should not happen!
            return partial;
        }
    }
}

