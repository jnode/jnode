/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2008 JNode.org
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

/**
 * A SyntaxBundle represents the alternative syntaxes for a command / alias.
 * 
 * @author crawley@jnode.org
 */
public class SyntaxBundle {
    private final String alias;
    private final String description;
    private final Syntax[] syntaxes;
    
    public SyntaxBundle(String alias, String description, Syntax[] syntaxes) {
        super();
        this.alias = alias;
        this.description = description;
        this.syntaxes = syntaxes == null ? new Syntax[0] : syntaxes;
    }
    
    public SyntaxBundle(String alias, Syntax ... syntaxes) {
        this(alias, null, syntaxes);
    }
    
    public String getAlias() {
        return alias;
    }

    public String getDescription() {
        return description;
    }

    public Syntax[] getSyntaxes() {
        return syntaxes;
    }
    
    public MuSyntax prepare(ArgumentBundle bundle) {
        if (syntaxes.length == 0) {
            return null;
        }
        else if (syntaxes.length == 1) {
            return (syntaxes[0] == null) ? null : 
                    syntaxes[0].prepare(bundle);
        }
        else {
            return new AlternativesSyntax(syntaxes).prepare(bundle);
        }
    }
}
