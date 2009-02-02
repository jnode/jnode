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
 
package org.jnode.fs.command;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;

/**
 * @author epr
 * @author Andreas H\u00e4nel
 * @author Martin Husted Hartvig (hagar@jnode.org)
 * @author Levente S\u00e1ntha
 * @author crawley@jnode.org
 */
public class DirCommand extends AbstractCommand {
    private static final int LEFT_MARGIN = 14;
    private static final SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd HH:mm");

    private final FileArgument ARG_PATH = new FileArgument(
            "path", Argument.OPTIONAL | Argument.MULTIPLE | Argument.EXISTING, 
            "the file or directory to list");
    
    public DirCommand() {
        super("List files or directories");
        registerArguments(ARG_PATH);
    }

    public static void main(String[] args) throws Exception {
        new DirCommand().execute(args);
    }

    public void execute() 
        throws IOException {
        File[] paths = ARG_PATH.getValues();
        if (paths.length == 0) {
            paths = new File[] {new File(System.getProperty("user.dir"))};
        }
        PrintWriter out = getOutput().getPrintWriter(false);
        PrintWriter err = getError().getPrintWriter();
        for (File path : paths) {
            if (!path.exists()) {
                err.println("No such path: " + path);
            } else {
                if (paths.length > 1) {
                    out.println(path + ":");
                }
                if (path.isDirectory()) {
                    final File[] list = path.listFiles();
                    printList(list, out);
                } else if (path.isFile()) {
                    printList(new File[]{path}, out);
                }
            }
        }
    }

    private void printList(File[] list, PrintWriter out) {
        if (list != null) {
            Arrays.sort(list, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    boolean b1 = f1.isDirectory();
                    boolean b2 = f2.isDirectory();
                    return b1 == b2 ? f1.getName().compareTo(f2.getName()) :
                            b1 & !b2 ? -1 : 1;
                }
            });
            StringBuilder sb = new StringBuilder();
            Date lastModified = new Date();
            for (int i = 0; i < list.length; i++) {
                File f = list[i];
                if (f.exists()) {
                    sb.setLength(0);
                    lastModified.setTime(f.lastModified());
                    if (f.isFile()) {
                        String ln = String.valueOf(f.length()).concat("B");
                        int cnt = LEFT_MARGIN - ln.length();
                        for (int j = 0; j < cnt; j++, sb.append(' '))
                            ;
                        sb.append(ln);
                        sb.append("   ");
                        sb.append(df.format(lastModified));
                        sb.append("   ");
                        sb.append(f.getName());
                    } else {
                        for (int j = 0; j < LEFT_MARGIN + 3; j++, sb.append(' '))
                            ;
                        sb.append(df.format(lastModified));
                        sb.append("   [");
                        sb.append(f.getName());
                        sb.append(']');
                    }
                    out.println(sb.toString());
                }
            }
            out.println();
        }
    }
}
