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

package org.jnode.shell.command;

import gnu.java.security.action.GetPropertiesAction;

import java.io.InputStream;
import java.io.PrintStream;
import java.security.AccessController;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;

/**
 * @author epr
 */
public class EnvCommand extends AbstractCommand {

    public EnvCommand() {
        super("Print the system properties");
    }

    public static void main(String[] args) throws Exception {
        new EnvCommand().execute(args);
    }

    /**
     * Execute this command
     */
    public void execute(CommandLine cmdLine, InputStream in, PrintStream out, PrintStream err)
        throws Exception {
        final Properties ps = (Properties) AccessController.doPrivileged(new GetPropertiesAction());
        final TreeMap<Object, Object> sortedPs = new TreeMap<Object, Object>(ps);
        for (Map.Entry<Object, Object> entry : sortedPs.entrySet()) {
            final String key = entry.getKey().toString();
            final String value = entry.getValue().toString();

            out.println(key + '=' + value);
        }
    }
}
