/*
 * $Id: NameSpace.java 4564 2008-09-18 22:01:10Z fduminy $
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
package org.jnode.test.shell.harness;

/**
 * This class holds a simple specification for a plugin required by a test or test set.
 *
 * @author crawley@jnode
 */
public class PluginSpecification {
    private final String pluginId;
    private final String pluginVersion;
    private final String pseudoPluginClassName;

    public PluginSpecification(String pluginId, String pluginVersion,
                               String pseudoPluginClassName) {
        super();
        this.pluginId = pluginId;
        this.pluginVersion = pluginVersion;
        this.pseudoPluginClassName = pseudoPluginClassName;
    }

    public String getPluginId() {
        return pluginId;
    }

    public String getPluginVersion() {
        return pluginVersion;
    }

    /**
     * This method returns the classname of a 'pseudo plugin' class that can perform
     * minimal plugin specific initialization when a plugin is 'required' in
     * a context where Plugin loading is not possible.
     *
     * @return the class name.
     */
    public String getClassName() {
        return pseudoPluginClassName;
    }
}
