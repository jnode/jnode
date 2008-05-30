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

package org.jnode.driver.textscreen.x86;

import javax.naming.NamingException;
import org.jnode.naming.InitialNaming;
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class PcTextScreenPlugin extends Plugin {

    private final PcTextScreenManager mgr;

    /**
     * @param descriptor
     */
    public PcTextScreenPlugin(PluginDescriptor descriptor) {
        super(descriptor);
        this.mgr = new PcTextScreenManager();
    }

    /**
     * @see org.jnode.plugin.Plugin#startPlugin()
     */
    protected void startPlugin() throws PluginException {
        try {
            InitialNaming.bind(PcTextScreenManager.NAME, mgr);
        } catch (NamingException ex) {
            throw new PluginException(ex);
        }
    }

    /**
     * @see org.jnode.plugin.Plugin#stopPlugin()
     */
    protected void stopPlugin() throws PluginException {
        InitialNaming.unbind(PcTextScreenManager.NAME);
    }
}
