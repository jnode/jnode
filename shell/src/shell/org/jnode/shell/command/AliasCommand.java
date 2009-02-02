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
 
package org.jnode.shell.command;

import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.ShellUtils;
import org.jnode.shell.alias.AliasManager;
import org.jnode.shell.alias.NoSuchAliasException;
import org.jnode.shell.syntax.AliasArgument;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.ClassNameArgument;

/**
 * @author epr
 * @author qades
 * @author Martin Husted Hartvig (hagar@jnode.org)
 * @author crawley@jnode.org
 */
public class AliasCommand extends AbstractCommand {

    private static final String slash_t = ":\t\t";

    private final AliasArgument ARG_ALIAS = 
        new AliasArgument("alias", Argument.OPTIONAL, "the alias to be added");
    
    private final ClassNameArgument ARG_CLASS =
        new ClassNameArgument("className", Argument.OPTIONAL, "the classname");
    
    private final AliasArgument ARG_REMOVE =
        new AliasArgument("remove", Argument.OPTIONAL, "the alias to be removed");
    
    public AliasCommand() {
        super("list, add or remove JNode command aliases");
        registerArguments(ARG_ALIAS, ARG_CLASS, ARG_REMOVE);
    }

    public static void main(String[] args) throws Exception {
        new AliasCommand().execute(args);
    }

    public void execute() throws Exception {
        final AliasManager aliasMgr = ShellUtils.getCurrentAliasManager();

        if (ARG_REMOVE.isSet()) {
            // remove an alias
            aliasMgr.remove(ARG_REMOVE.getValue());
        } else if (ARG_ALIAS.isSet()) {
            // add an alias
            String className = ARG_CLASS.getValue();
            try {
                // If the className argument is actually an existing alias, use
                // the existing alias's class name as the new alias's class name.
                String tmp = aliasMgr.getAliasClassName(className);
                if (tmp != null) {
                    className = tmp;
                }
            } catch (NoSuchAliasException e) {
                // ignore
            }
            aliasMgr.add(ARG_ALIAS.getValue(), className);
        } else {
            // list the aliases
            showAliases(aliasMgr, getOutput().getPrintWriter());
        } 
    }
    
    private void showAliases(AliasManager aliasMgr, PrintWriter out) throws NoSuchAliasException {
        final TreeMap<String, String> map = new TreeMap<String, String>();

        for (String alias : aliasMgr.aliases()) {
            map.put(alias, aliasMgr.getAliasClassName(alias));
        }

        for (Map.Entry<String, String> entry : map.entrySet()) {
            out.println(entry.getKey() + slash_t + entry.getValue());
        }
    }

    
}
