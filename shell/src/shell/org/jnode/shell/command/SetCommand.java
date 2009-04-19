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

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.ShellUtils;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.PropertyNameArgument;
import org.jnode.shell.syntax.ShellPropertyNameArgument;
import org.jnode.shell.syntax.StringArgument;


/**
 * Shell command to set system or shell property values.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Martin Husted Hartvig (hagar@jnode.org)
 * @author Levente S\u00e1ntha
 * @author crawley@jnode.org
 */

public class SetCommand extends AbstractCommand {
    private PropertyNameArgument keyArg = new PropertyNameArgument(
            "key", Argument.OPTIONAL, "The name of the property to be set (or cleared)");
    
    private ShellPropertyNameArgument skeyArg = new ShellPropertyNameArgument(
            "skey", Argument.OPTIONAL, "The name of the shell property to be set (or cleared)");
    
    private StringArgument valueArg = new StringArgument(
            "value", Argument.OPTIONAL, "The new property value");
    
    private final FlagArgument shellArg = new FlagArgument(
            "shell", Argument.OPTIONAL + Argument.SINGLE,
            "If set, print the current shell properties rather that the System properties.");

    
    public SetCommand() {
        super("Set or clear the value of a property");
        registerArguments(keyArg, skeyArg, valueArg, shellArg);
    }

    public static void main(String[] args) throws Exception {
        new SetCommand().execute(args);
    }

    public void execute() throws Exception {
        PrintWriter out = getOutput().getPrintWriter();
        if (shellArg.isSet()) {
            String key = skeyArg.getValue();
            if (!valueArg.isSet()) {
                out.println("Removing " + key);
                ShellUtils.getCurrentShell().removeProperty(key);
            } else {
                String value = valueArg.getValue();
                out.println("Setting " + key + " to " + value);
                ShellUtils.getCurrentShell().setProperty(key, value);
            }
        } else {
            String key = keyArg.getValue();
            if (!valueArg.isSet()) {
                out.println("Removing " + key);
                System.getProperties().remove(key);
            } else {
                String value = valueArg.getValue();
                out.println("Setting " + key + " to " + value);
                System.getProperties().setProperty(key, value);
            }
        }
    }
}
