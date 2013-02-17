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
 
package org.jnode.command.file;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.FlagArgument;

/**
 * @author epr
 * @author Andreas H\u00e4nel
 * @author Martin Husted Hartvig (hagar@jnode.org)
 * @author Levente S\u00e1ntha
 * @author crawley@jnode.org
 */
public class DirCommand extends AbstractCommand {
    private static final String SEPARATOR = "  ";
    private static final int SEPARATOR_SIZE = SEPARATOR.length();
    private static final int LEFT_MARGIN = 14;
    private static final String help_path = "the file or directory to list";
    private static final String help_humanReadable = "print sizes in human readable format (e.g. 1K, 234M, 2G)";
    private static final String help_super = "List files or directories";
    private static final String fmt_no_path = "No such path: %s%n";

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");
    private final DecimalFormat decimalFormat = new DecimalFormat("###0.00");

    private final FileArgument argPath;
    private final FlagArgument humanReadableArg;

    public DirCommand() {
        super(help_super);
        humanReadableArg = new FlagArgument("humanReadable", Argument.OPTIONAL, help_humanReadable);
        argPath = new FileArgument("path", Argument.OPTIONAL | Argument.MULTIPLE | Argument.EXISTING, help_path);
        registerArguments(argPath, humanReadableArg);
    }

    public static void main(String[] args) throws Exception {
        new DirCommand().execute(args);
    }

    public void execute() 
        throws IOException {
        File[] paths = argPath.getValues();
        if (paths.length == 0) {
            paths = new File[] {new File(System.getProperty("user.dir"))};
        }
        PrintWriter out = getOutput().getPrintWriter(false);
        PrintWriter err = getError().getPrintWriter();
        for (File path : paths) {
            if (!path.exists()) {
                err.format(fmt_no_path, path);
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
            for (File f : list) {
                if (f.exists()) {
                    sb.setLength(0);
                    lastModified.setTime(f.lastModified());
                    if (f.isFile()) {
                        String ln = formatSize(f.length());
                        int cnt = LEFT_MARGIN - ln.length();
                        for (int j = 0; j < cnt; j++) sb.append(' ');
                        sb.append(ln);
                        sb.append(SEPARATOR);
                        sb.append(dateFormat.format(lastModified));
                        sb.append(SEPARATOR);
                        sb.append(f.getName());
                    } else {
                        for (int j = 0; j < LEFT_MARGIN + SEPARATOR_SIZE; j++) sb.append(' ');
                        sb.append(dateFormat.format(lastModified));
                        sb.append(SEPARATOR);
                        sb.append('[');
                        sb.append(f.getName());
                        sb.append(']');
                    }
                    out.println(sb.toString());
                }
            }
            out.println();
        }
    }

    private static final String[] units = {"B", "K", "M", "G", "T", "P", "E", "Z", "Y"};


    protected String formatSize(long bytes) {
        if (humanReadableArg.isSet()) {
            if (bytes >= 1024) {
                double dbytes = (double) bytes;
                int index;
                for (index = 0; dbytes >= 1024; index++) dbytes = dbytes / 1024;
                return decimalFormat.format(dbytes) + units[index];
            } else {
                return bytes + "B";
            }
        } else {
            return bytes + "B";
        }
    }
}
