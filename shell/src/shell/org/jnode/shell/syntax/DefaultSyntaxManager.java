/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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
public class DefaultSyntaxManager implements SyntaxManager, ExtensionPointListener {

    private final DefaultSyntaxManager parent;

    private final HashMap<String, SyntaxBundle> syntaxes = 
        new HashMap<String, SyntaxBundle>();

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

    public void add(SyntaxBundle bundle) {
        if (parent == null) {
            throw new UnsupportedOperationException(
                    "Cannot modify the system syntax manager");
        } else if (bundle != null) {
            syntaxes.put(bundle.getAlias(), bundle);
        }
    }

    public SyntaxBundle remove(String alias) {
        if (parent == null) {
            throw new UnsupportedOperationException(
                    "Cannot modify the system syntax manager");
        } else {
            return syntaxes.remove(alias);
        }
    }

    public SyntaxBundle getSyntaxBundle(String alias) {
        SyntaxBundle bundle = syntaxes.get(alias);
        if (bundle != null) {
            return bundle;
        } else if (parent != null) {
            return parent.getSyntaxBundle(alias);
        } else {
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
            for (Extension ext : syntaxEP.getExtensions()) {
                SyntaxSpecLoader loader = new SyntaxSpecLoader();
                for (ConfigurationElement element : ext.getConfigurationElements()) {
                    SyntaxSpecAdapter adaptedElement = new PluginSyntaxSpecAdapter(element);
                    try {
                        SyntaxBundle bundle = loader.loadSyntax(adaptedElement);
                        if (bundle != null) {
                            syntaxes.put(bundle.getAlias(), bundle);
                        }
                    } catch (Exception ex) {
                        log.log(Priority.WARN, "problem in syntax", ex);
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
