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
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jnode.command.util.AbstractDirectoryWalker;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.LongArgument;
import org.jnode.shell.syntax.StringArgument;

/**
 * <code>FindCommand</code> - search for files in a directory hierarchy
 * 
 * @author Alexander Kerner
 * @see AbstractDirectoryWalker
 * 
 */
public class FindCommand extends AbstractCommand {

    private class Walker extends AbstractDirectoryWalker {

        @Override
        protected void handleRestrictedFile(File file) throws IOException {
            err.println("Permission denied for \"" + file + '"');
        }

        @Override
        public void handleDir(File f) {
            out.println(f);
        }

        @Override
        public void handleFile(File f) {
            out.println(f);
        }
    }
    
    private static final String help_name = "filter results to show only files that match a given pattern";
    private static final String help_iname = "same as name, but case insensitive";
    private static final String help_max_depth = "descend at most to the given level of directories";
    private static final String help_min_depth = "ignore files and directories below the given level";
    private static final String help_type = "filter results to show only files of a given type. Valid types are 'd' " +
                                            "for directories and 'f' for files";
    private static final String help_dir = "directory to start searching from";
    private static final String help_super = "Find files and directories";
    
    private final StringArgument nameArg;
    private final StringArgument inameArg;
    private final LongArgument maxdepthArg;
    private final LongArgument mindepthArg;
    private final StringArgument typeArg;
    private final FileArgument dirArg;
    
    private PrintWriter out;
    private PrintWriter err;

    public FindCommand() {
        super(help_super);
        nameArg     = new StringArgument("name", Argument.OPTIONAL, help_name);
        inameArg    = new StringArgument("iname", Argument.OPTIONAL, help_iname);
        maxdepthArg = new LongArgument("maxdepth", Argument.OPTIONAL, help_max_depth);
        mindepthArg = new LongArgument("mindepth", Argument.OPTIONAL, help_min_depth);
        typeArg     = new StringArgument("type", Argument.OPTIONAL, help_type);
        dirArg      = new FileArgument("directory", Argument.OPTIONAL | Argument.MULTIPLE, help_dir);
        registerArguments(dirArg, mindepthArg, maxdepthArg, inameArg, nameArg, typeArg);
    }

    public static void main(String[] args) throws IOException {
        new FindCommand().execute();
    }
    
    @Override
    public void execute() throws IOException {
        out = getOutput().getPrintWriter();
        err = getError().getPrintWriter();
        final Walker walker = new Walker();

        if (maxdepthArg.isSet()) {
            walker.setMaxDepth(maxdepthArg.getValue());
        }

        if (mindepthArg.isSet()) {
            walker.setMinDepth(mindepthArg.getValue());
        }

        if (nameArg.isSet()) {
            final String value = nameArg.getValue();
            walker.addFilter(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    Pattern p = Pattern.compile(value);
                    Matcher m = p.matcher(file.getName());
                    return m.matches();
                }
            });
        }

        if (inameArg.isSet()) {
            final String value = inameArg.getValue();
            walker.addFilter(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    Pattern p = Pattern.compile(value, Pattern.CASE_INSENSITIVE);
                    Matcher m = p.matcher(file.getName());
                    return m.matches();
                }
            });
        }

        if (typeArg.isSet()) {
            final Character value = typeArg.getValue().charAt(0);
            if (value.equals(Character.valueOf('f'))) {
                walker.addFilter(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.isFile();
                    }
                });
            } else if (value.equals(Character.valueOf('d'))) {
                walker.addFilter(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.isDirectory();
                    }
                });
            }
        }
        if (dirArg.isSet()) {
            walker.walk(dirArg.getValues());
        } else {
            walker.walk(new File(System.getProperty("user.dir")));
        }
    }
}
