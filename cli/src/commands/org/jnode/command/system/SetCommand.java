/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
    
    private static final String help_key = "The name of the property to be set (or cleared)";
    private static final String help_skey = "The name of the shell property to be set (or cleared)";
    private static final String help_value = "The new property value";
    private static final String help_shell = "If set, print the current shell props rather than the System props";
    private static final String help_super = "Set or clear the value of a property";
    private static final String fmt_remove = "Removing %s%n";
    private static final String fmt_set = "Setting %s to %s%n";
    
    private PropertyNameArgument keyArg;
    private ShellPropertyNameArgument skeyArg;
    private StringArgument valueArg;
    private final FlagArgument shellArg;

    
    public SetCommand() {
        super(help_super);
        keyArg   = new PropertyNameArgument("key", Argument.OPTIONAL, help_key);
        skeyArg  = new ShellPropertyNameArgument("skey", Argument.OPTIONAL, help_skey);
        valueArg = new StringArgument("value", Argument.OPTIONAL, help_value);
        shellArg = new FlagArgument("shell", Argument.OPTIONAL + Argument.SINGLE, help_shell);
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
                out.format(fmt_remove, key);
                ShellUtils.getCurrentShell().removeProperty(key);
            } else {
                String value = valueArg.getValue();
                out.format(fmt_set, key, value);
                ShellUtils.getCurrentShell().setProperty(key, value);
            }
        } else {
            String key = keyArg.getValue();
            if (!valueArg.isSet()) {
                out.format(fmt_remove, key);
                System.getProperties().remove(key);
            } else {
                String value = valueArg.getValue();
                out.format(fmt_set, key, value);
                System.getProperties().setProperty(key, value);
            }
        }
    }
}
