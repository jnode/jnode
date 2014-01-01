/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jnode.command.util.IOUtils;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.StringArgument;

/**
 * Unix 'paste' command
 *
 * @author chris boertien
 */
public class PasteCommand extends AbstractCommand {
    
    private static final String help_files = "list of files to be operated on";
    private static final String help_serial = "if set, paste files one at a time, instead of in parallel";
    private static final String help_delims = "use the supplied characters as delimiters instead of <TAB>";
    private static final String help_super = "merge lines of files";
    
    private final FileArgument argFiles;
    private final FlagArgument argSerial;
    private final StringArgument argDelims;
    private PrintWriter out;
    
    private List<File> files;
    private char[] delims;
    private int delimPos;
    private int rc = 0;
    private boolean serial;
    
    public PasteCommand() {
        super(help_super);
        int filesFlags = Argument.MULTIPLE | Argument.EXISTING | FileArgument.HYPHEN_IS_SPECIAL;
        argFiles = new FileArgument("files", filesFlags, help_files);
        argSerial = new FlagArgument("serial", 0, help_serial);
        argDelims = new StringArgument("delims", 0, help_delims);
        registerArguments(argFiles, argSerial, argDelims);
    }
    
    public void execute() {
        out = getOutput().getPrintWriter();
        parseOptions();
        try {
            if (serial) {
                pasteSerial();
            } else {
                pasteParallel();
            }
        } finally {
            exit(rc);
        }
    }
    
    private void pasteParallel() {
        List<BufferedReader> readers = new ArrayList<BufferedReader>(files.size());
        List<String> names = new ArrayList<String>(files.size());
        List<String> lines = new ArrayList<String>(files.size());
        try {
            for (File file : files) {
                String name = file.getName();
                int i = names.indexOf(name);
                if (i != -1) {
                    readers.add(readers.get(i));
                } else {
                    if (name.equals("-")) {
                        readers.add(IOUtils.openBufferedReader(getInput().getReader()));
                    } else {
                        readers.add(IOUtils.openBufferedReader(file));
                    }
                }
                names.add(name);
            }
            while (true) {
                int num = readLines(lines, readers);
                if (num == 0) break;
                writeLines(lines, num);
                lines.clear();
            }
        } finally {
            for (BufferedReader reader : readers) {
                IOUtils.close(reader);
            }
        }
    }
    
    private void pasteSerial() {
        BufferedReader reader = null;
        for (File file : files) {
            try {
                List<String> lines = null;
                if (file.getName().equals("-")) {
                    reader = IOUtils.openBufferedReader(getInput().getReader());
                } else {
                    reader = IOUtils.openBufferedReader(file);
                }
                if (reader != null) {
                    lines = IOUtils.readLines(reader);
                    if (lines != null) {
                        writeLines(lines, lines.size());
                    }
                }
            } finally {
                IOUtils.close(reader);
            }
        }
    }
    
    private int readLines(List<String> lines, List<BufferedReader> readers) {
        int count = 0;
        String line = null;
        for (BufferedReader reader : readers) {
            try {
                line = reader.readLine();
            } catch (IOException e) {
                rc = 1;
                return 0;
            }
            if (line != null) {
                lines.add(line);
                count++;
            }
        }
        return count;
    }
    
    private void writeLines(List<String> lines, int max) {
        boolean first = true;
        for (int i = 0; i < max; i++) {
            if (!first) {
                out.write(nextDelim());
            }
            first = false;
            out.write(lines.get(i));
        }
        out.println();
        delimPos = 0;
    }
    
    private char nextDelim() {
        char c = delims[delimPos++];
        if (delimPos == delims.length) delimPos = 0;
        return c;
    }
    
    private void parseOptions() {
        if (argFiles.isSet()) {
            files = Arrays.asList(argFiles.getValues());
        } else {
            files = new ArrayList<File>(1);
            files.add(new File("-"));
        }
        
        if (argDelims.isSet()) {
            delims = argDelims.getValue().toCharArray();
        } else {
            delims = new char[]{'\t'};
        }
        serial = argSerial.isSet();
    }
}
