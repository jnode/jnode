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
 
package org.jnode.apps.editor;

import java.io.File;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;

/**
 * @author Levente S\u00e1ntha
 */
public class LeedCommand extends AbstractCommand {
    private final FileArgument ARG_EDIT = new FileArgument(
        "file", Argument.MANDATORY, "the file to edit");

    public LeedCommand() {
        super("LEvi's EDitor\n   Ctrl-S - save\n   Ctrl-Q - quit");
        registerArguments(ARG_EDIT);
    }

    public static void main(String[] args) throws Exception {
        new LeedCommand().execute(args);
    }

    public void execute()
        throws Exception {
        final File file = ARG_EDIT.getValue();
        if (file.isDirectory()) {
            getError().getPrintWriter().println(file + " is a directory");
        } else {
            TextEditor.main(new String[]{file.toString()});
        }
    }
}
