/*
 * $Id$
 *
 * JNode.org
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
 
package org.jnode.fs.jifs.command;

import javax.naming.NameNotFoundException;

import org.jnode.naming.InitialNaming;
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginException;
import org.jnode.plugin.PluginManager;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.EnumArgument;

// TODO fix class name
// TODO this class is actually just PluginCommand specialized for JIFS ...
/**
 * Just mounts initial JIFS on /Jifs
 * 
 * @author Andreas H\u00e4nel
 */
public class CreateJIFSCommand extends AbstractCommand {

    private static enum Action {
        start, stop, restart;
    }

    private static class ActionArgument extends EnumArgument<Action> {
        public ActionArgument() {
            super("action", Argument.MANDATORY, Action.class, "action to be performed");
        }

        @Override
        protected String argumentKind() {
            return "{start,stop,restart}";
        }
    }
    
    private final ActionArgument ARG_ACTION = new ActionArgument();
    
    public CreateJIFSCommand() {
        super("Manage the JIFS filesystem plugin");
        registerArguments(ARG_ACTION);
    }

    public void execute() throws NameNotFoundException, PluginException {
        final PluginManager mgr = InitialNaming.lookup(PluginManager.NAME);
        final Plugin p =
            mgr.getRegistry().getPluginDescriptor("org.jnode.fs.jifs.def").getPlugin();
        switch (ARG_ACTION.getValue()) {
            case start:
                p.start();
                break;
            case stop:
                p.stop();
                break;
            case restart:
                p.stop();
                p.start();
                break;
        }
    }
}
