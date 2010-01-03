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
 
package org.jnode.shell.bjorne;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.CommandLine.Token;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.CommandSyntaxException;

/**
 * The AssignmentArgument understands 'arguments' of the form &lt;name&gt;['='[&lt;value&gt;]].
 * It will perform completion of the &lt;name&gt; component against the variable names
 * defined in a supplied BjorneContext.
 * 
 * @author crawley@jnode.org
 */
public class AssignmentArgument extends Argument<String> {
    private final BjorneContext context;
    
    public AssignmentArgument(String label, BjorneContext context, int flags, String description) {
        super(label, flags, new String[0], description);
        this.context = context;
    }

    @Override
    protected String argumentKind() {
        return "bjorne assignment";
    }

    @Override
    protected String doAccept(Token value, int flags) throws CommandSyntaxException {
        String tok = value.text;
        int pos = tok.indexOf('=');
        String name = (pos == -1) ? tok : tok.substring(0, pos);
        if (!BjorneToken.isName(name)) {
            throw new CommandSyntaxException("invalid name ('" + name + "')");
        }
        return tok;
    }

    @Override
    public void doComplete(CompletionInfo completions, String partial, int flags) {
        int pos = partial.indexOf('=');
        if (pos != -1) {
            return;
        }
        for (String varName : context.getVariableNames()) {
            if (varName.startsWith(partial)) {
                completions.addCompletion(varName, true);
            }
        }
    }
}
