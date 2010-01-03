/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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
import org.jnode.shell.syntax.ArgumentSpecLoader.ArgumentSpec;

/**
 * This syntax manager loads syntaxes specified using plugin extensions.
 * 
 * @author crawley@jnode.org
 */
public class DefaultSyntaxManager implements SyntaxManager, ExtensionPointListener {
    
    private final DefaultSyntaxManager parent;

    private final HashMap<String, SyntaxBundle> syntaxes = 
        new HashMap<String, SyntaxBundle>();
    
    private final HashMap<String, ArgumentSpec<?>[]> arguments =
        new HashMap<String, ArgumentSpec<?>[]>();

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
    
    public void add(String alias, ArgumentSpec<?>[] args) {
        if (parent == null) {
            throw new UnsupportedOperationException(
                    "Cannot modify the system syntax manager");
        } else if (args != null) {
            arguments.put(alias, args);
        }
    }

    public SyntaxBundle remove(String alias) {
        if (parent == null) {
            throw new UnsupportedOperationException(
                    "Cannot modify the system syntax manager");
        } else {
            arguments.remove(alias);
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
    
    public ArgumentBundle getArgumentBundle(String alias) {
        ArgumentSpec<?>[] args = arguments.get(alias);
        if (args != null) {
            return makeArgumentBundle(args);
        } else if (parent != null) {
            return parent.getArgumentBundle(alias);
        } else {
            return null;
        }
    }
    
    private ArgumentBundle makeArgumentBundle(ArgumentSpec<?>[] specs) {
        Argument<?>[] args = new Argument<?>[specs.length];
        for (int i = 0; i < specs.length; i++) {
            try {
                args[i] = specs[i].instantiate();
            } catch (Exception e) {
                return null;
            }
        }
        return new ArgumentBundle(args);
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
            arguments.clear();
            SyntaxSpecLoader syntaxLoader = new SyntaxSpecLoader();
            ArgumentSpecLoader argumentLoader = new ArgumentSpecLoader();
            for (Extension ext : syntaxEP.getExtensions()) {
                for (ConfigurationElement element : ext.getConfigurationElements()) {
                    SyntaxSpecAdapter adaptedElement = new PluginSyntaxSpecAdapter(element);
                    try {
                        if (element.getName().equals("syntax")) {
                            SyntaxBundle bundle = syntaxLoader.loadSyntax(adaptedElement);
                            if (bundle != null) {
                                syntaxes.put(bundle.getAlias(), bundle);
                            }
                        } else if (element.getName().equals("argument-bundle")) {
                            ArgumentSpec<?>[] specs = argumentLoader.loadArguments(adaptedElement);
                            if (specs != null) {
                                arguments.put(element.getAttribute("alias"), specs);
                            }
                        } else {
                            throw new SyntaxFailureException("Element name is not 'syntax' or 'argument-bundle'");
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
