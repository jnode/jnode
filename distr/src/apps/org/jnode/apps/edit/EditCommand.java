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

package org.jnode.apps.edit;

import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.argument.FileArgument;

import java.io.File;

/**
 * @author Levente S\u00e1ntha
 */
public class EditCommand {
    static final FileArgument ARG_EDIT = new FileArgument("file", "the file to edit");
    public static Help.Info HELP_INFO = new Help.Info(
            "edit", "edit a file",
            new Parameter[]{new Parameter(ARG_EDIT, Parameter.OPTIONAL)}
    );

    public static void main(String[] args) throws Exception {
        final ParsedArguments cmdLine = HELP_INFO.parse(args);
        final File file = ARG_EDIT.getFile(cmdLine);
        if (file == null)
            Editor.editFile(null);
        else if (file.isDirectory()) {
            System.err.println(file + " is a directory");
        } else {
            Editor.editFile(file);
        }
    }
}
