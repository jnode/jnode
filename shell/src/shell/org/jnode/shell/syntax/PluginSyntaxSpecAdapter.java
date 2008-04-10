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

import org.jnode.plugin.ConfigurationElement;

/**
 * This class adapts plugin ConfigurationElements for Syntax loaders / readers.
 * 
 * @author crawley@jnode.org
 */
public class PluginSyntaxSpecAdapter implements SyntaxSpecAdapter {
    private final ConfigurationElement element;
    
    public PluginSyntaxSpecAdapter(ConfigurationElement element) {
        this.element = element;
    }

    @Override
    public String getName() {
        return element.getName();
    }

    @Override
    public String getAttribute(String name) {
        String tmp = element.getAttribute(name);
        return (tmp != null && tmp.length() == 0) ? null : tmp;
    }

    @Override
    public SyntaxSpecAdapter getChild(int childNo) {
        return new PluginSyntaxSpecAdapter(element.getElements()[childNo]);
    }

    @Override
    public int getNosChildren() {
        return element.getElements().length;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PluginSyntaxSpecAdapter)) {
            return false;
        }
        return element == ((PluginSyntaxSpecAdapter) obj).element;
    }

    @Override
    public int hashCode() {
        return element.hashCode();
    }
}
