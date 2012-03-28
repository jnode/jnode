/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import org.jnode.command.argument.NumberListArgument;
import org.jnode.command.util.IOUtils;
import org.jnode.command.util.NumberRange;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.StringArgument;

/**
 * Unix `cut` command
 *
 * TODO add --complement to select byte/chars/fields that are *not* within the given ranges
 * TODO make byte-ranges multi-byte character friendly
 * @author chris boertien
 */
public class CutCommand extends AbstractCommand {
    
    private static final String help_byte = "Select only the listed bytes";
    private static final String help_char = "Select only the listed chars";
    private static final String help_field = "Select only the listed fields";
    private static final String help_in_delim = "Use this for the delimeter instead of TAB";
    private static final String help_out_delim = "Replace the input delimeter with this in the output";
    private static final String help_files = "The files to operate on, or stdin if none are given";
    private static final String help_suppress = "Do not output lines that do not contain a delimeter character";
    private static final String help_complement = "Complement the set of selected bytes, chars or fields";
    private static final String help_super = "Remove ranges of bytes, chars, or fields from input lines";
    private static final String err_delim = "An delimeter may only be present when operating on fields.";
    private static final String err_multi_mode = "Only one type of list may be specified";
    private static final String err_no_mode = "Must select either a byte, char or field range";
    private static final String err_suppress = "Suppression only makes sense when using fields";
    private static final String fmt_err = "cut: %s%n";
    
    private static enum Mode {
        BYTE, CHAR, FIELD;
    }
    
    private final NumberListArgument argByteRange;
    private final NumberListArgument argCharRange;
    private final NumberListArgument argFieldRange;
    private final StringArgument argInDelim;
    private final StringArgument argOutDelim;
    private final FileArgument argFiles;
    private final FlagArgument argSuppress;
    private final FlagArgument argComplement;
    
    private PrintWriter err;
    private BufferedWriter out;
    private File[] files;
    private Mode mode;
    private NumberRange[] list;
    private String inDelim;
    private String outDelim;
    private boolean suppress;
    @SuppressWarnings("unused")
    private boolean complement;
    
    public CutCommand() {
        super(help_super);
        argByteRange  = new NumberListArgument("byte-range", 0, 1, Integer.MAX_VALUE - 1, help_byte);
        argCharRange  = new NumberListArgument("char-range", 0, 1, Integer.MAX_VALUE - 1, help_char);
        argFieldRange = new NumberListArgument("field-range", 0, 1, Integer.MAX_VALUE - 1, help_field);
        argInDelim    = new StringArgument("in-delim", 0, help_in_delim);
        argOutDelim   = new StringArgument("out-delim", 0, help_out_delim);
        argFiles      = new FileArgument("files", 0, help_files);
        argSuppress   = new FlagArgument("suppress", 0, help_suppress);
        argComplement = new FlagArgument("complement", 0, help_complement);
        registerArguments(argByteRange, argCharRange, argFieldRange, argInDelim, argOutDelim, argFiles);
        registerArguments(argSuppress, argComplement);
    }
    
    public void execute() throws IOException {
        err = getError().getPrintWriter();
        out = new BufferedWriter(getOutput().getPrintWriter());
        parseOptions();
        
        BufferedReader reader;
        List<String> lines;
        
        for (File file : files) {
            if (file.getName().equals("-")) {
                reader = new BufferedReader(getInput().getReader());
            } else {
                reader = IOUtils.openBufferedReader(file);
            }
            try {
                lines = IOUtils.readLines(reader);
            } finally {
                IOUtils.close(reader);
            }
            try {
                if (mode == Mode.BYTE) {
                    cutBytes(lines);
                } else if (mode == Mode.CHAR) {
                    cutChars(lines);
                } else if (mode == Mode.FIELD) {
                    cutFields(lines);
                }
            } finally {
                IOUtils.flush(out);
            }
        }
    }
    
    private void cutBytes(List<String> lines) throws IOException {
        // FIXME
        // In the case of single-byte characters, this is the right
        // path to take, but if characters are multi-byte, then there
        // is supposed to be aligning done to make sure a byte-range
        // does not fall in the middle of a character.
        cutChars(lines);
    }
    
    private void cutChars(List<String> lines) throws IOException {
        int limit, start, end;
        for (String line : lines) {
            limit = line.length();
            for (NumberRange range : list) {
                start = Math.min(limit, range.start());
                end   = Math.min(limit, range.end());
                if (start == limit) break;
                out.write(line.substring(start - 1, end));
            }
            out.newLine();
        }
    }
    
    private void cutFields(List<String> lines) throws IOException {
        boolean first;
        int limit, start, end;
        for (String line : lines) {
            if (!line.contains(inDelim)) {
                if (!suppress) {
                    out.write(line);
                    out.newLine();
                }
                continue;
            }
            String[] fields = line.split(inDelim);
            if (fields == null || fields.length == 0) {
                out.newLine();
                continue;
            }
            
            first = true;
            limit = fields.length;
            for (NumberRange range : list) {
                start = Math.min(limit, range.start());
                end = Math.min(limit, range.end());
                if (start == limit) break;
                for (int i = start - 1; i < end; i++) {
                    if (!first) {
                        out.write(outDelim);
                    }
                    first = false;
                    out.write(fields[i]);
                }
            }
            out.newLine();
        }
    }
    
    private void parseOptions() {
        if (argByteRange.isSet()) {
            mode = Mode.BYTE;
            list = argByteRange.getValues();
        }
        if (argCharRange.isSet()) {
            if (mode != null) {
                error(err_multi_mode);
            }
            mode = Mode.CHAR;
            list = argCharRange.getValues();
        }
        if (argFieldRange.isSet()) {
            if (mode != null) {
                error(err_multi_mode);
            }
            mode = Mode.FIELD;
            list = argFieldRange.getValues();
        }
        if (mode == null) {
            error(err_no_mode);
        }
        if (argInDelim.isSet()) {
            if (mode != Mode.FIELD) {
                error(err_delim);
            }
            inDelim = argInDelim.getValue();
        } else {
            inDelim = "\t";
        }
        if (argOutDelim.isSet()) {
            if (mode != Mode.FIELD) {
                error(err_delim);
            }
            outDelim = argOutDelim.getValue();
        } else {
            outDelim = inDelim;
        }
        if (argSuppress.isSet()) {
            if (mode != Mode.FIELD) {
                error(err_suppress);
            }
            suppress = true;
        }
        complement = argComplement.isSet();
        if (argFiles.isSet()) {
            files = argFiles.getValues();
        } else {
            files = new File[] {new File("-")};
        }
    }
    
    private void error(String s) {
        err.format(fmt_err, s);
        exit(1);
    }
}
