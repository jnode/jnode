/*
 * $Id: AliasArgument.java 2945 2006-12-20 08:51:17Z qades $
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

import javax.naming.NameNotFoundException;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.ShellUtils;
import org.jnode.shell.CommandLine.Token;
import org.jnode.shell.alias.AliasManager;

/**
 * This class implements alias-valued command line arguments.  At the moment, it performs
 * minimal syntax checking and performs completion against the shell's alias namespace.
 * 
 * @author qades
 * @author crawley@jnode.org
 */
public class AliasArgument extends Argument<String> {

    public AliasArgument(String label, int flags, String description) {
        super(label, flags, new String[0], description);
    }

    public AliasArgument(String label, int flags) {
        this(label, flags, null);
    }

    public AliasArgument(String label) {
        this(label, 0);
    }

    @Override
    public String doAccept(Token value) throws CommandSyntaxException {
        if (value.text.length() == 0) {
            throw new CommandSyntaxException("empty alias name");
        }
        return value.text;
    }

    public void complete(CompletionInfo completion, String partial) {
        try {
            // get the alias manager
            final AliasManager aliasMgr = 
                ShellUtils.getShellManager().getCurrentShell().getAliasManager();

            // collect matching aliases
            for (String alias : aliasMgr.aliases()) {
                if (alias.startsWith(partial)) {
                    completion.addCompletion(alias);
                }
            }
        } catch (NameNotFoundException ex) {
            // should not happen!
            return;
        }
    }

    @Override
    protected String argumentKind() {
        return "alias";
    }
}
