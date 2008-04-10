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
 
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.jnode.plugin.ConfigurationElement;
import org.jnode.plugin.Extension;
import org.jnode.plugin.ExtensionPoint;
import org.jnode.plugin.ExtensionPointListener;

/**
 * This syntax manager loads syntaxes specified using plugin extensions.
 * 
 * @author crawley@jnode.org
 */
public class DefaultSyntaxManager implements SyntaxManager,
        ExtensionPointListener {

    private final DefaultSyntaxManager parent;

    private final HashMap<String, Syntax> syntaxes = 
        new HashMap<String, Syntax>();

    private final ExtensionPoint syntaxEP;

    /**
     * Create a new instance
     */
    public DefaultSyntaxManager(ExtensionPoint syntaxEP) {
        this.parent = null;
        this.syntaxEP = syntaxEP;
        syntaxEP.addListener(this);
        refreshSyntaxes();
    }

    private DefaultSyntaxManager(DefaultSyntaxManager parent) {
        this.parent = parent;
        this.syntaxEP = null;
        if (parent == null) {
            throw new IllegalArgumentException("parent cannot be null");
        }
    }

    public void add(String alias, Syntax syntax) {
        if (parent == null) {
            throw new UnsupportedOperationException(
                    "Cannot modify the system syntax manager");
        } 
        else if (syntax != null) {
            syntaxes.put(alias, syntax);
        }
    }

    public Syntax remove(String alias) {
        if (parent == null) {
            throw new UnsupportedOperationException(
                    "Cannot modify the system syntax manager");
        } else {
            return syntaxes.remove(alias);
        }
    }

    public Syntax getSyntax(String alias) {
        Syntax syntax = syntaxes.get(alias);
        if (syntax != null) {
            return syntax;
        }
        else if (parent != null) {
            return parent.getSyntax(alias);
        }
        else {
            return null;
        }
    }
    
    public Collection<String> getKeys() {
        Set<String> res = new HashSet<String>(syntaxes.size());
        if (parent != null) {
            res.addAll(parent.getKeys());
        }
        res.addAll(syntaxes.keySet());
        return res;
    }

    public SyntaxManager createSyntaxManager() {
        return new DefaultSyntaxManager(this);
    }

    /**
     * Reload the syntax list from the extension-point
     */
    protected void refreshSyntaxes() {
        final Logger log = Logger.getLogger(getClass());
        System.out.println("Refreshing syntax list");
        if (syntaxEP != null) {
            syntaxes.clear();
            final Extension[] extensions = syntaxEP.getExtensions();
            for (int i = 0; i < extensions.length; i++) {
                final Extension ext = extensions[i];
                final ConfigurationElement[] elements = 
                    ext.getConfigurationElements();
                SyntaxSpecLoader loader = new SyntaxSpecLoader();
                
                for (int j = 0; j < elements.length; j++) {
                    SyntaxSpecAdapter element = new PluginSyntaxSpecAdapter(elements[j]);
                    final String name = element.getName();
                    if (!"syntax".equals(name)) {
                        log.log(Priority.WARN, "element name is not 'syntax' ... ignoring");
                        continue;
                    }
                    final String alias = element.getAttribute("alias");
                    if (alias == null) {
                        log.log(Priority.WARN, name + " element has no 'alias' ... ignoring");
                        continue;
                    }
                    try {
                        Syntax syntax = loader.loadSyntax(element);
                        if (syntax != null) {
                            syntaxes.put(alias, syntax);
                        }
                    }
                    catch (Exception ex) {
                        log.log(Priority.WARN, 
                                "problem in " + name + " element for alias '" + alias + "'", 
                                ex);
                    }
                }
            }
        }
    }
    
    public void extensionAdded(ExtensionPoint point, Extension extension) {
        refreshSyntaxes();
    }

    public void extensionRemoved(ExtensionPoint point, Extension extension) {
        refreshSyntaxes();
    }
}
