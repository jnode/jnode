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

import gnu.java.security.action.GetEnvAction;
import gnu.java.security.action.GetPropertiesAction;

import java.io.PrintWriter;
import java.security.AccessController;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FlagArgument;

/**
 * @author epr
 */
public class EnvCommand extends AbstractCommand {
    // FIXME ... this class and the corresponding alias are incorrectly named
	
	private final FlagArgument envArg = new FlagArgument(
			"env", Argument.OPTIONAL + Argument.SINGLE,
			"If set, print the System 'env' variables rather that the System properties.");

    public EnvCommand() {
        super("Print the System properties");
        registerArguments(envArg);
    }

    public static void main(String[] args) throws Exception {
        new EnvCommand().execute(args);
    }

    /**
     * Execute this command
     */
    public void execute() throws Exception {
    	final TreeMap<Object, Object> sortedPs;
        if (envArg.isSet()) {
        	Map<String, String> ps = 
        		(Map<String, String>) AccessController.doPrivileged(new GetEnvAction());
        	sortedPs = new TreeMap<Object, Object>(ps);
        } else {
        	Properties ps = AccessController.doPrivileged(new GetPropertiesAction());
        	sortedPs = new TreeMap<Object, Object>(ps);
        }
         
        final PrintWriter out = getOutput().getPrintWriter();
        for (Map.Entry<Object, Object> entry : sortedPs.entrySet()) {
            final String key = entry.getKey().toString();
            final String value = entry.getValue().toString();
            out.println(key + '=' + value);
        }
    }
}
