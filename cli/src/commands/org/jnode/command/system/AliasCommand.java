/*
 * $Id$
 *
 * Copyright (C) 2003-2012 JNode.org
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
 
package org.jnode.command.system;

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

    private static final String help_alias = "the alias to be added";
    private static final String help_class = "the class name";
    private static final String help_remove = "the alias to be removed";
    private static final String help_super = "List, add or remove JNOde command aliases";
    
    private static final String slash_t = ":\t\t";
    
    private final AliasArgument argAlias;
    private final ClassNameArgument argClass;
    private final AliasArgument argRemove;
    
    public AliasCommand() {
        super(help_super);
        argAlias  = new AliasArgument("alias", Argument.OPTIONAL, help_alias);
        argClass  = new ClassNameArgument("className", Argument.OPTIONAL, help_class);
        argRemove = new AliasArgument("remove", Argument.OPTIONAL, help_remove);
        registerArguments(argAlias, argClass, argRemove);
    }

    public static void main(String[] args) throws Exception {
        new AliasCommand().execute(args);
    }

    public void execute() throws Exception {
        final AliasManager aliasMgr = ShellUtils.getCurrentAliasManager();

        if (argRemove.isSet()) {
            // remove an alias
            aliasMgr.remove(argRemove.getValue());
        } else if (argAlias.isSet()) {
            // add an alias
            String className = argClass.getValue();
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
            aliasMgr.add(argAlias.getValue(), className);
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
