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

package org.jnode.shell.help.argument;

import javax.naming.NameNotFoundException;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.naming.InitialNaming;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginManager;
import org.jnode.shell.help.Argument;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @deprecated use the org.jnode.shell.syntax.* classes instead.
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

    public void complete(CompletionInfo completion, String partial) {
        try {
            // get the plugin manager
            final PluginManager piMgr = InitialNaming.lookup(PluginManager.NAME);

            // collect matching plugin id's
            for (PluginDescriptor descr : piMgr.getRegistry()) {
                final String id = descr.getId();
                if (id.startsWith(partial)) {
                    completion.addCompletion(id);
                }
            }
        } catch (NameNotFoundException ex) {
            // should not happen!
            return;
        }
    }
}
