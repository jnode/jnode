/*
 * $Id$
 *
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

import java.util.ArrayList;
import java.util.List;

/**
 * The SyntaxSpecLoader traverses a syntax specification (e.g. in XML) wrapped
 * in an adapter, and creates a Syntax tree.
 * 
 * @author crawley@jnode.org
 */
public class SyntaxSpecLoader {

    public SyntaxBundle loadSyntax(SyntaxSpecAdapter element) {
        final String name = element.getName();
        if (!"syntax".equals(name)) {
            throw new SyntaxFailureException("element name is not 'syntax'");
        }
        final String alias = element.getAttribute("alias");
        if (alias == null) {
            throw new SyntaxFailureException("syntax element has no 'alias' attribute");
        }
        final String description = element.getAttribute("description");

        int nos = element.getNosChildren();
        List<Syntax> childSyntaxes = new ArrayList<Syntax>(nos);
        for (int i = 0; i < nos; i++) {
            childSyntaxes.add(doLoad(element.getChild(i)));
        }
        return new SyntaxBundle(alias, description, 
                childSyntaxes.toArray(new Syntax[childSyntaxes.size()]));
    }

    private Syntax doLoad(SyntaxSpecAdapter syntaxElement) 
        throws SyntaxFailureException, IllegalArgumentException {
        String label = syntaxElement.getAttribute("label");
        String description = syntaxElement.getAttribute("description");
        String kind = syntaxElement.getName();
        if (kind.equals("empty")) {
            return new EmptySyntax(label, description);
        } else if (kind.equals("alternatives")) {
            int nos = syntaxElement.getNosChildren();
            Syntax[] alts = new Syntax[nos];
            for (int i = 0; i < nos; i++) {
                alts[i] = doLoad(syntaxElement.getChild(i));
            }
            return new AlternativesSyntax(label, description, alts);
        } else if (kind.equals("optionSet")) {
            int nos = syntaxElement.getNosChildren();
            OptionSyntax[] options = new OptionSyntax[nos];
            for (int i = 0; i < nos; i++) {
                try {
                    options[i] = (OptionSyntax) doLoad(syntaxElement.getChild(i));
                } catch (ClassCastException ex) {
                    throw new SyntaxFailureException(
                            "<optionSyntax> element can only contain <option> elements");
                }
            }
            return new OptionSetSyntax(label, description, options);
        } else if (kind.equals("option")) {
            String argLabel = syntaxElement.getAttribute("argLabel");
            if (argLabel == null) {
                throw new SyntaxFailureException("<option> element has no 'argLabel' attribute");
            }
            String shortName = syntaxElement.getAttribute("shortName");
            String longName = syntaxElement.getAttribute("longName");
            if (shortName == null) {
                if (longName == null) {
                    throw new SyntaxFailureException(
                            "<option> element has must have a 'shortName' or 'longName' attribute");
                }
                return new OptionSyntax(argLabel, longName, description);
            } else {
                if (shortName.length() != 1) {
                    throw new SyntaxFailureException(
                            "<option> elements 'shortName' attribute must be one character long");
                }
                if (longName == null) {
                    return new OptionSyntax(argLabel, shortName.charAt(0), description);
                } else {
                    return new OptionSyntax(argLabel, longName, shortName.charAt(0), description);
                }
            }
        } else if (kind.equals("powerset")) {
            int nos = syntaxElement.getNosChildren();
            Syntax[] members = new Syntax[nos];
            for (int i = 0; i < nos; i++) {
                members[i] = doLoad(syntaxElement.getChild(i));
            }
            return new PowersetSyntax(label, description, members);
        } else if (kind.equals("repeat")) {
            int nos = syntaxElement.getNosChildren();
            int minCount = getCount(syntaxElement, "minCount", 0);
            int maxCount = getCount(syntaxElement, "maxCount", Integer.MAX_VALUE);
            Syntax[] members = new Syntax[nos];
            for (int i = 0; i < nos; i++) {
                members[i] = doLoad(syntaxElement.getChild(i));
            }
            Syntax childSyntax = (members.length == 1) ? members[0] : new SequenceSyntax(members);
            return new RepeatSyntax(label, childSyntax, minCount, maxCount, description);
        } else if (kind.equals("sequence")) {
            int nos = syntaxElement.getNosChildren();
            Syntax[] seq = new Syntax[nos];
            for (int i = 0; i < nos; i++) {
                seq[i] = doLoad(syntaxElement.getChild(i));
            }
            return new SequenceSyntax(label, description, seq);
        } else if (kind.equals("optional")) {
            boolean eager = getFlag(syntaxElement, "eager", false);
            int nos = syntaxElement.getNosChildren();
            Syntax[] seq = new Syntax[nos];
            for (int i = 0; i < nos; i++) {
                seq[i] = doLoad(syntaxElement.getChild(i));
            }
            return new OptionalSyntax(label, description, eager, seq);
        } else if (kind.equals("argument")) {
            String argLabel = syntaxElement.getAttribute("argLabel");
            if (argLabel == null) {
                System.out.println(syntaxElement);
                throw new SyntaxFailureException("<argument> element has no 'argLabel' attribute");
            }
            return new ArgumentSyntax(label, argLabel, description);
        } else if (kind.equals("verb")) {
            String symbol = syntaxElement.getAttribute("symbol");
            if (symbol == null) {
                throw new SyntaxFailureException("<verb> element has no 'symbol' attribute");
            }
            String argLabel = syntaxElement.getAttribute("argLabel");
            if (argLabel == null) {
                System.out.println(syntaxElement);
                throw new SyntaxFailureException("<argument> element has no 'argLabel' attribute");
            }
            return new VerbSyntax(label, symbol, argLabel, description);
        } else if (kind.equals("symbol")) {
            String symbol = syntaxElement.getAttribute("symbol");
            if (symbol == null) {
                throw new SyntaxFailureException("<symbol> element has no 'symbol' attribute");
            }
            return new SymbolSyntax(label, symbol, description);
        } else {
            throw new SyntaxFailureException("<" + kind + "> element does not represent a known syntax");
        }
    }

    private int getCount(SyntaxSpecAdapter element, String  name, int defaultValue) {
        String tmp = element.getAttribute(name);
        if (tmp == null) {
            return defaultValue;
        } else {
            try {
                return Integer.parseInt(tmp);
            } catch (NumberFormatException ex) {
                throw new SyntaxFailureException("'" + name + "' attribute is not an integer");
            }
        }
    }

    private boolean getFlag(SyntaxSpecAdapter element, String  name, boolean defaultValue) {
        String tmp = element.getAttribute(name);
        if (tmp == null) {
            return defaultValue;
        } else if (tmp.equalsIgnoreCase("true")) {
            return true;
        } else if (tmp.equalsIgnoreCase("false")) {
            return true;
        } else {
            throw new SyntaxFailureException("'" + name + "' attribute is not 'true' or 'false'");
        }
    }
}
