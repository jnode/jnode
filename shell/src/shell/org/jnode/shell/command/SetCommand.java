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
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.PropertyNameArgument;
import org.jnode.shell.syntax.StringArgument;


/**
 * Shell command to set property values.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Martin Husted Hartvig (hagar@jnode.org)
 * @author Levente S\u00e1ntha
 */

public class SetCommand extends AbstractCommand {
    private PropertyNameArgument keyArg = 
        new PropertyNameArgument("key", Argument.MANDATORY, "The name of the property to be set (or cleared)");
    private StringArgument valueArg = 
        new StringArgument("value", Argument.OPTIONAL, "The new property value");
    
    public SetCommand() {
        super("Set or clear the value of a property");
        registerArguments(keyArg, valueArg);
    }

    public static void main(String[] args) throws Exception {
        new SetCommand().execute(args);
    }

    public void execute() throws Exception {
        PrintWriter out = getOutput().getPrintWriter();
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
