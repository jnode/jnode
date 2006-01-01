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
 
package org.jnode.fs.command;

import org.jnode.shell.Command;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.FileArgument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.util.NumberUtils;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

/**
 * @author epr
 * @author Andreas H\u00e4nel
 * @author Martin Husted Hartvig (hagar@jnode.org)
 * @author Levente S\u00e1ntha
 */
public class DirCommand implements Command {
    private static final int LEFT_MARGIN = 14;
    private static final SimpleDateFormat df = new SimpleDateFormat("yyyy MMM dd hh:mm");


    static final FileArgument ARG_DIR = new FileArgument("directory", "the directory to list contents of");
    public static Help.Info HELP_INFO =
            new Help.Info(
                    "dir",
                    "List the entries of the given directory",
                    new Parameter[]{new Parameter(ARG_DIR, Parameter.OPTIONAL)});

    public static void main(String[] args) throws Exception {
        new DirCommand().execute(new CommandLine(args), System.in, System.out, System.err);
    }

    public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) throws Exception {
        ParsedArguments cmdLine = HELP_INFO.parse(commandLine.toStringArray());
        String userDir = System.getProperty("user.dir");
        File dir = ARG_DIR.getFile(cmdLine);
        if (dir == null) dir = new File(userDir);
        if (dir.exists() && dir.isDirectory()) {
            final File[] list = dir.listFiles();
            this.printList(list, out);
        } else if (dir.exists() && dir.isFile()) {
            this.printList(new File[]{dir}, out);
        } else {
            err.println("No such directory " + dir);
        }
    }

    private void printList(File[] list, PrintStream out) {
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
                        String ln = NumberUtils.size(f.length());
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
