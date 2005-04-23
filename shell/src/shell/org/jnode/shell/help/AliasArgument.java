/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */

package org.jnode.shell.help;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NameNotFoundException;

import org.jnode.shell.ShellUtils;
import org.jnode.shell.alias.AliasManager;

/**
 * @author qades
 */
public class AliasArgument extends Argument {

    public AliasArgument(String name, String description, boolean multi) {
        super(name, description, multi);
    }

    public AliasArgument(String name, String description) {
        super(name, description);
    }

    public String complete(String partial) {
        final List<String> aliases = new ArrayList<String>();
        try {
            // get the alias manager
            final AliasManager aliasMgr = ShellUtils.getShellManager()
                    .getCurrentShell().getAliasManager();

            // collect matching aliases
            for (String alias : aliasMgr.aliases()) {
                if (alias.startsWith(partial)) {
                    aliases.add(alias);
                }
            }

            return complete(partial, aliases);
        } catch (NameNotFoundException ex) {
            // should not happen!
            return partial;
        }
    }
}
