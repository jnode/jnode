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

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;

/**
 * @author Levente S\u00e1ntha
 */
public class EditCommand extends AbstractCommand {
    private final FileArgument ARG_EDIT = new FileArgument("file", Argument.OPTIONAL, "the file to edit");

    EditCommand() {
        super("edit a file");
        registerArguments(ARG_EDIT);
    }

    public static void main(String[] args) throws Exception {
        new EditCommand().execute(args);
    }

    public void execute()
        throws Exception {
        final File file = ARG_EDIT.isSet() ? ARG_EDIT.getValue() : null;
        if (file == null)
            Editor.editFile(null);
        else if (file.isDirectory()) {
            getError().getPrintWriter().println(file + " is a directory");
        } else {
            Editor.editFile(file);
        }
    }
}
