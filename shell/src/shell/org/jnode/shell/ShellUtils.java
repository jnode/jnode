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
 
package org.jnode.shell;

import javax.naming.NameNotFoundException;

import org.jnode.naming.InitialNaming;
import org.jnode.shell.alias.AliasManager;
import org.jnode.shell.syntax.SyntaxManager;

/**
 * @author epr
 * @author crawley@jnode.org
 */
public class ShellUtils {

    /**
     * Get the root shell manager.
     * 
     * @return The root shell manager
     * @throws NameNotFoundException
     */
    public static ShellManager getShellManager() throws NameNotFoundException {
        return InitialNaming.lookup(ShellManager.NAME);
    }

    /**
     * Get the current shell.
     * 
     * @return The current shell
     * @throws NameNotFoundException
     */
    public static Shell getCurrentShell() throws NameNotFoundException {
        return getShellManager().getCurrentShell();
    }

    /**
     * Get the root alias manager.
     * 
     * @return The root alias manager
     * @throws NameNotFoundException
     */
    public static AliasManager getAliasManager() throws NameNotFoundException {
        return InitialNaming.lookup(AliasManager.NAME);
    }

    /**
     * Get the current shell's alias manager.
     * 
     * @return The current alias manager
     * @throws NameNotFoundException
     */
    public static AliasManager getCurrentAliasManager() throws NameNotFoundException {
        return getShellManager().getCurrentShell().getAliasManager();
    }

    /**
     * Get the root syntax manager.
     * 
     * @return The root syntax manager
     * @throws NameNotFoundException
     */
    public static SyntaxManager getSyntaxManager() throws NameNotFoundException {
        return InitialNaming.lookup(SyntaxManager.NAME);
    }

    /**
     * Get the current shell's syntax manager.
     * 
     * @return The current syntax manager
     * @throws NameNotFoundException
     */
    public static SyntaxManager getCurrentSyntaxManager() throws NameNotFoundException {
        return getShellManager().getCurrentShell().getSyntaxManager();
    }

    public static void registerCommandInvoker(SimpleCommandInvoker.Factory factory)
        throws NameNotFoundException {
        getShellManager().registerInvokerFactory(factory);
    }

    public static void registerCommandInterpreter(CommandInterpreter.Factory factory) 
        throws NameNotFoundException {
        getShellManager().registerInterpreterFactory(factory);
    }

    public static SimpleCommandInvoker createInvoker(String name, CommandShell shell)
        throws IllegalArgumentException {
        try {
            return getShellManager().createInvoker(name, shell);
        } catch (NameNotFoundException ex) {
            throw new ShellFailureException("no shell manager", ex);
        }
    }

    public static CommandInterpreter createInterpreter(String name)
        throws IllegalArgumentException, ShellFailureException {
        try {
            return getShellManager().createInterpreter(name);
        } catch (NameNotFoundException ex) {
            throw new ShellFailureException("no shell manager", ex);
        }
    }
}
