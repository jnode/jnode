/*
 * $Id: EnvCommand.java 4977 2009-02-02 09:09:41Z lsantha $
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

import gnu.java.security.action.GetEnvAction;

import java.io.PrintWriter;
import java.security.AccessController;
import java.util.Map;
import java.util.TreeMap;

import org.jnode.shell.AbstractCommand;

/**
 * This command prints the current environment variables.  A regular JNode command
 * cannot set environment variables because the Java APIs do not allow this.  Environment
 * variable setting is accomplished by the shell / interpreter and builtin commands, it at all.
 * 
 * @author crawley@jnode.org
 */
public class PrintEnvCommand extends AbstractCommand {

    public PrintEnvCommand() {
        super("Print the current environment variables");
    }

    public static void main(String[] args) throws Exception {
        new PrintEnvCommand().execute(args);
    }

    /**
     * Execute this command
     */
    public void execute() throws Exception {
        final Map<String, String> env = 
            (Map<String, String>) AccessController.doPrivileged(new GetEnvAction());
        final TreeMap<String, String> sortedEnv = new TreeMap<String, String>(env);
        final PrintWriter out = getOutput().getPrintWriter();
        for (Map.Entry<String, String> entry : sortedEnv.entrySet()) {
            out.println(entry.getKey() + '=' + entry.getValue());
        }
    }
}
