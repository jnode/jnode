/*
 * $Id$
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
 
package org.jnode.shell.def;

import java.util.HashMap;

import org.jnode.shell.CommandInterpreter;
import org.jnode.shell.CommandInvoker;
import org.jnode.shell.CommandShell;
import org.jnode.shell.Shell;
import org.jnode.shell.ShellManager;
import org.jnode.util.SystemInputStream;

/**
 * This is the default implementation of the ShellManager API.
 * 
 * @author epr
 * @author crawley@jnode.org
 */
public class DefaultShellManager implements ShellManager {

    private final InheritableThreadLocal<Shell> currentShell = 
        new InheritableThreadLocal<Shell>();

    private final HashMap<String, CommandInvoker.Factory> invokerFactories =
        new HashMap<String, CommandInvoker.Factory>();

    private final HashMap<String, CommandInterpreter.Factory> interpreterFactories =
        new HashMap<String, CommandInterpreter.Factory>();

    /**
     * @see org.jnode.shell.ShellManager#getCurrentShell()
     */
    public Shell getCurrentShell() {
        return (Shell) currentShell.get();
    }

    /**
     * Register the new current shell
     * @param currentShell
     */
    public void registerShell(Shell currentShell) {
        SystemInputStream.getInstance().claimSystemIn();
        this.currentShell.set(currentShell);
    }

    public CommandInterpreter createInterpreter(String name) throws IllegalArgumentException {
        CommandInterpreter.Factory factory = interpreterFactories.get(name);
        if (factory == null) {
            throw new IllegalArgumentException("Unknown interpreter '" + name + "'");
        }
        return factory.create();
    }

    public CommandInvoker createInvoker(String name, CommandShell shell) throws IllegalArgumentException {
        CommandInvoker.Factory factory = invokerFactories.get(name);
        if (factory == null) {
            throw new IllegalArgumentException("Unknown invoker '" + name + "'");
        }
        return factory.create(shell);
    }

    public void registerInterpreterFactory(CommandInterpreter.Factory factory) {
        interpreterFactories.put(factory.getName(), factory);
    }

    public void registerInvokerFactory(CommandInvoker.Factory factory) {
        invokerFactories.put(factory.getName(), factory);
    }

    public void unregisterInterpreterFactory(CommandInterpreter.Factory factory) {
        interpreterFactories.put(factory.getName(), null);
    }

    public void unregisterInvokerFactory(CommandInvoker.Factory factory) {
        invokerFactories.put(factory.getName(), null);
    }
}
