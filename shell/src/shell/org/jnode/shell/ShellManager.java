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

/**
 * A ShellManager records the current shell.  It also manages factories for instantiating invokers,
 * and interpreters and provides methods for instantiating them.
 * 
 * @author epr
 * @author crawley@jnode.org
 */
public interface ShellManager {

    /**
     * Name used to bind this manager into the InitialNaming namespace.
     * 
     * @see org.jnode.naming.InitialNaming
     */
    public static final Class<ShellManager> NAME = ShellManager.class;

    /**
     * Gets the current shell
     */
    public Shell getCurrentShell();

    /**
     * Register the new current shell
     * 
     * @param currentShell
     */
    public void registerShell(Shell currentShell);

    public void registerInvokerFactory(CommandInvoker.Factory factory);

    public void registerInterpreterFactory(CommandInterpreter.Factory factory);

    public void unregisterInvokerFactory(CommandInvoker.Factory factory);

    public void unregisterInterpreterFactory(CommandInterpreter.Factory factory);

    public CommandInvoker createInvoker(String name, CommandShell shell)
        throws IllegalArgumentException;

    public CommandInterpreter createInterpreter(String name)
        throws IllegalArgumentException;

}
