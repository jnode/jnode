/*
 * $Id: CdCommand.java 4975 2009-02-02 08:30:52Z lsantha $
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
package org.jnode.command.file;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.jnode.command.util.AbstractDirectoryWalker;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.util.NumberUtils;

/*
 * @author Alexander Kerner
 */
public class DuCommand extends AbstractCommand {
    
    private static final String err_perm = "Permission denied for '%s'%n";
    
    private abstract class Walker extends AbstractDirectoryWalker {

        protected final TreeMap<File, Long> map = new TreeMap<File, Long>();
        protected final boolean humanReadable;

        Walker(boolean humanReadable) {
            this.humanReadable = humanReadable;
        }

        @Override
        public void handleDir(File file) {
            handleAll(file);
        }

        @Override
        public void handleFile(File file) {
            handleAll(file);
        }
        
        @Override
        protected void handleRestrictedFile(File file) throws IOException {
            err.format(err_perm, file);
        }

        private void handleAll(File file) {
            map.put(file, file.length());
        }

        protected TreeMap<File, Long> summariseIt(TreeMap<File, Long> map) {
            TreeMap<File, Long> result = new TreeMap<File, Long>();
            NavigableMap<File, Long> navMap = map.descendingMap();
            Long tmpSize = 0L;
            while (navMap.size() != 0) {
                Entry<File, Long> e = navMap.pollFirstEntry();
                File key = e.getKey();
                Long value = e.getValue();
                tmpSize += key.length();

                if (key.isFile()) {
                    result.put(key, value);
                } else if (key.isDirectory()) {
                    result.put(key, tmpSize);
                } else {
                    // ignore unknown file type
                }
            }
            return result;
        }
    }

    private class AllWalker extends Walker {

        AllWalker(boolean humanReadable) {
            super(humanReadable);
        }

        @Override
        protected void lastAction(boolean wasCancelled) {
            Map<File, Long> summarisedMap = summariseIt(map);
            for (Entry<File, Long> e : summarisedMap.entrySet()) {
                if (humanReadable)
                    out.println(NumberUtils.toBinaryByte(e.getValue()) + "\t" + e.getKey());
                else
                    out.println(e.getValue() + "\t" + e.getKey());
            }
        }
    }

    private class OnlyDirsWalker extends Walker {

        OnlyDirsWalker(boolean humanReadable) {
            super(humanReadable);
        }

        @Override
        protected void lastAction(boolean wasCancelled) {
            Map<File, Long> summarisedMap = summariseIt(map);
            for (Entry<File, Long> e : summarisedMap.entrySet()) {
                if (e.getKey().isDirectory()) {
                    if (humanReadable)
                        out.println(NumberUtils.toBinaryByte(e.getValue()) + "\t" + e.getKey());
                    else
                        out.println(e.getValue() + "\t" + e.getKey());
                }
            }
        }
    }

    private class TotalWalker extends Walker {

        TotalWalker(boolean humanReadable) {
            super(humanReadable);
        }

        @Override
        protected void lastAction(boolean wasCancelled) {
            TreeMap<File, Long> summarisedMap = summariseIt(map);
            Entry<File, Long> e = summarisedMap.firstEntry();
            if (humanReadable)
                out.println(NumberUtils.toBinaryByte(e.getValue()) + "\t" + e.getKey());
            else
                out.println(e.getValue() + "\t" + e.getKey());

        }
    }

    private PrintWriter out;
    private PrintWriter err;

    private static final String HELP_TOTAL = "display only a total for each argument";
    private static final String HELP_ALL = "write counts for all files, not just directories";
    private static final String HELP_SUPER = "print file sizes";
    private static final String HELP_DIR = "directory to start printing sizes";
    private static final String HELP_HUMAN_READABLE = "print sizes in human readable format (e.g., 1K 234M 2G)";

    private final FlagArgument totalArg;
    private final FlagArgument allArg;
    private final FileArgument dirArg;
    private final FlagArgument humanReadableArg;

    public DuCommand() {
        super(HELP_SUPER);
        totalArg         = new FlagArgument("sum", Argument.OPTIONAL, HELP_TOTAL);
        allArg           = new FlagArgument("all", Argument.OPTIONAL, HELP_ALL);
        dirArg           = new FileArgument("directory", Argument.OPTIONAL | Argument.MULTIPLE, HELP_DIR);
        humanReadableArg = new FlagArgument("human-readable", Argument.OPTIONAL, HELP_HUMAN_READABLE);
        registerArguments(totalArg, allArg, humanReadableArg, dirArg);
    }

    public static void main(String[] args) throws IOException {
        new DuCommand().execute();
    }

    public void execute() throws IOException {
        out = getOutput().getPrintWriter();
        err = getError().getPrintWriter();
        Walker walker = null;
        if (totalArg.isSet()) {
            walker = new TotalWalker(humanReadableArg.isSet());
        } else if (allArg.isSet()) {
            walker = new AllWalker(humanReadableArg.isSet());
        } else {
            walker = new OnlyDirsWalker(humanReadableArg.isSet());
        }
        
        if (dirArg.isSet()) {
            walker.walk(dirArg.getValues());
        } else {
            walker.walk(new File(System.getProperty("user.dir")));
        }
    }
}

