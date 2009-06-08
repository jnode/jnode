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
package org.jnode.shell.bjorne;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.CommandLine.Token;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.CommandSyntaxException;

/**
 * The BjorneAliasNameArgument understands 'arguments' of the form &lt;name&gt;.
 * It will perform completion of the &lt;name&gt; component against the alias names
 * defined in a supplied BjorneContext.
 * 
 * @author crawley@jnode.org
 */
public class BjorneAliasNameArgument extends Argument<String> {
    private final BjorneContext context;
    
    public BjorneAliasNameArgument(String label, BjorneContext context, int flags, String description) {
        super(label, flags, new String[0], description);
        this.context = context;
    }

    @Override
    protected String argumentKind() {
        return "bjorne alias name";
    }

    @Override
    protected String doAccept(Token value, int flags) throws CommandSyntaxException {
        String tok = value.text;
        if (!BjorneToken.isName(tok)) {
            throw new CommandSyntaxException("invalid name ('" + tok + "')");
        }
        return tok;
    }

    @Override
    public void doComplete(CompletionInfo completions, String partial, int flags) {
        for (String aliasName : context.getAliases().keySet()) {
            if (aliasName.startsWith(partial)) {
                completions.addCompletion(aliasName, true);
            }
        }
    }
}
