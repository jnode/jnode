/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.plugin;

import org.jnode.permission.JNodePermission;

/**
 * @author epr
 */
public interface PluginSecurityConstants {

    /**
     * Permission required to start a plugin
     */
    static final JNodePermission START_PERM = new JNodePermission("startPlugin");
    /**
     * Permission required to stop a plugin
     */
    static final JNodePermission STOP_PERM = new JNodePermission("stopPlugin");

    /**
     * Permission required to load a plugin
     */
    static final JNodePermission LOAD_PERM = new JNodePermission("loadPlugin");
    /**
     * Permission required to unload a plugin
     */
    static final JNodePermission UNLOAD_PERM = new JNodePermission("unloadPlugin");
}
