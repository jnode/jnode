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
 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
                for (ConfigurationElement element : elements) {
                    final String name = element.getName();
                    if (!"syntax".equals(name)) {
                        log.log(Priority.WARN, "element name is not 'syntax' ... ignoring");
                        continue;
                    }
                    final String alias = getAttribute(element, "alias");
                    if (alias == null) {
                        log.log(Priority.WARN, name + " element has no 'alias' ... ignoring");
                        continue;
                    }
                    try {
                        Syntax syntax = loadSyntax(element);
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

    public static Syntax loadSyntax(ConfigurationElement element) {
        ConfigurationElement[] children = element.getElements();
        List<Syntax> childSyntaxes = new ArrayList<Syntax>(children.length);
        for (ConfigurationElement child : children) {
            childSyntaxes.add(loadSyntax(child, false));
        }
        int nosSyntaxes = childSyntaxes.size();
        if (nosSyntaxes == 0) {
            return new EmptySyntax(null, null);
        }
        else if (nosSyntaxes == 1) {
            return childSyntaxes.get(0);
        }
        else {
            return new AlternativesSyntax(childSyntaxes.toArray(new Syntax[nosSyntaxes]));
        }
    }
    
    private static Syntax loadSyntax(ConfigurationElement syntaxElement, boolean nullOK) 
    throws SyntaxFailureException, IllegalArgumentException {
        String label = getAttribute(syntaxElement, "label");
        String description = getAttribute(syntaxElement, "description");
        String kind = syntaxElement.getName();
        if (kind.equals("empty")) {
            if (nullOK && description == null && label == null) {
                return null;
            }
            else {
                return new EmptySyntax(label, description);
            }
        }
        else if (kind.equals("alternatives")) {
            ConfigurationElement[] children = syntaxElement.getElements();
            Syntax[] alts = new Syntax[children.length];
            for (int i = 0; i < alts.length; i++) {
                alts[i] = loadSyntax(children[i], true);
            }
            return new AlternativesSyntax(label, description, alts);
        }
        else if (kind.equals("optionSet")) {
            ConfigurationElement[] children = syntaxElement.getElements();
            OptionSyntax[] options = new OptionSyntax[children.length];
            for (int i = 0; i < options.length; i++) {
                try {
                    options[i] = (OptionSyntax) loadSyntax(children[i], false);
                }
                catch (ClassCastException ex) {
                    throw new SyntaxFailureException(
                            "<optionSyntax> element can only contain <option> elements");
                }
            }
            return new OptionSetSyntax(label, description, options);
        }
        else if (kind.equals("option")) {
            String argLabel = getAttribute(syntaxElement, "argLabel");
            if (argLabel == null) {
                throw new SyntaxFailureException("<option> element has no 'argLabel' attribute");
            }
            String shortName = getAttribute(syntaxElement, "shortName");
            String longName = getAttribute(syntaxElement, "longName");
            if (shortName == null) {
                if (longName == null) {
                    throw new SyntaxFailureException(
                    "<option> element has must have a 'shortName' or 'longName' attribute");
                }
                return new OptionSyntax(argLabel, longName, description);
            }
            else {
                if (shortName.length() != 1) {
                    throw new SyntaxFailureException(
                    "<option> elements 'shortName' attribute must be one character long");
                }
                if (longName == null) {
                    return new OptionSyntax(argLabel, shortName.charAt(0), description);
                }
                else {
                    return new OptionSyntax(argLabel, longName, shortName.charAt(0), description);
                }
            }
        }
        else if (kind.equals("powerset")) {
            ConfigurationElement[] children = syntaxElement.getElements();
            Syntax[] members = new Syntax[children.length];
            for (int i = 0; i < members.length; i++) {
                members[i] = loadSyntax(children[i], false);
            }
            return new PowersetSyntax(label, description, members);
        }
        else if (kind.equals("repeat")) {
            ConfigurationElement[] children = syntaxElement.getElements();
            int minCount = getCount(syntaxElement, "minCount", 0);
            int maxCount = getCount(syntaxElement, "maxCount", Integer.MAX_VALUE);
            Syntax[] members = new Syntax[children.length];
            for (int i = 0; i < members.length; i++) {
                members[i] = loadSyntax(children[i], false);
            }
            Syntax childSyntax = (members.length == 1) ?
                members[0] : new SequenceSyntax(members);
            return new RepeatSyntax(label, childSyntax, minCount, maxCount, description);
        }
        else if (kind.equals("sequence")) {
            ConfigurationElement[] children = syntaxElement.getElements();
            Syntax[] seq = new OptionSyntax[children.length];
            for (int i = 0; i < seq.length; i++) {
                seq[i] = loadSyntax(children[i], false);
            }
            return new SequenceSyntax(label, description, seq);
        }
        else if (kind.equals("argument")) {
            String argLabel = getAttribute(syntaxElement, "argLabel");
            if (argLabel == null) {
                System.out.println(syntaxElement);
                throw new SyntaxFailureException("<argument> element has no 'argLabel' attribute");
            }
            return new ArgumentSyntax(label, argLabel, description);
        }
        else if (kind.equals("symbol")) {
            String symbol = getAttribute(syntaxElement, "symbol");
            if (symbol == null) {
                throw new SyntaxFailureException(
                        "<symbol> element has no 'symbol' attribute");
            }
            return new TokenSyntax(label, symbol, description);
        }
        else {
            throw new SyntaxFailureException(
                    "<" + kind + "> element does not represent a known syntax");
        }
    }
    
    private static int getCount(ConfigurationElement element, String  name, int defaultValue) {
        String tmp = element.getAttribute(name);
        if (tmp == null) {
            return defaultValue;
        }
        else {
            try {
                return Integer.parseInt(tmp);
            }
            catch (NumberFormatException ex) {
                throw new SyntaxFailureException(
                        "'" + name + "' attribute is not an integer");
            }
        }
    }

    private static String getAttribute(ConfigurationElement element, String name) {
        String tmp = element.getAttribute(name);
        return (tmp != null && tmp.length() == 0) ? null : tmp;
    }

    public void extensionAdded(ExtensionPoint point, Extension extension) {
        refreshSyntaxes();
    }

    public void extensionRemoved(ExtensionPoint point, Extension extension) {
        refreshSyntaxes();
    }
}
