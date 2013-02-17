/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 * The SetFlagArgument understands '-x' and '+x' forms of a flag.  The first
 * form results in Boolean.FALSE, the second form results in Boolean.TRUE.
 * 
 * @author crawley@jnode.org
 */
public class SetFlagArgument extends Argument<Boolean> {
    private final char flagCh;
    
    public SetFlagArgument(String label, char flagCh, int flags, String description) {
        super(label, flags, new Boolean[0], description);
        this.flagCh = flagCh;
    }

    @Override
    protected String argumentKind() {
        return "bjorne 'set' flag";
    }

    @Override
    protected Boolean doAccept(Token value, int flags) throws CommandSyntaxException {
        String tok = value.text;
        if (tok.length() != 2 || tok.charAt(1) != flagCh ||
                (tok.charAt(0) != '-' && tok.charAt(0) != '+')) {
            throw new CommandSyntaxException("this does not match the '-'" + flagCh + " flag");
        }
        return Boolean.valueOf(tok.charAt(0) == '+');
    }

    @Override
    public void doComplete(CompletionInfo completions, String partial, int flags) {
        if (("-" + flagCh).startsWith(partial)) {
            completions.addCompletion("-" + flagCh);
        }
        if (("+" + flagCh).startsWith(partial)) {
            completions.addCompletion("+" + flagCh);
        }
    }
}
