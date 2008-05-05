/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2007-2008 JNode.org
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

package org.jnode.shell.syntax;

import javax.naming.NameNotFoundException;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.naming.InitialNaming;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginManager;

/**
 * This class captures plugin id argument values.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author crawley@jnode.org
 */
public class PluginArgument extends StringArgument {
    
    public PluginArgument(String label, int flags, String description) {
        super(label, flags, description);
    }
  
    @Override
    public void complete(CompletionInfo completion, String partial) {
        try {
            // get the plugin manager
            final PluginManager piMgr = (PluginManager) InitialNaming
                    .lookup(PluginManager.NAME);

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
    
    @Override
    public String toString() {
        return "PluginArgument{" + super.toString() + "}";
    }
    
    @Override
    protected String argumentKind() {
        return "plugin id";
    }
}
