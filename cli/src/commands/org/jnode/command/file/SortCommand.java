/*
 * $Id: header.txt 5714 2010-01-03 13:33:07Z lsantha $
 *
 * Copyright (C) 2003-2012 JNode.org
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.jnode.command.util.IOUtils;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.CommandSyntaxException;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.IntegerArgument;
import org.jnode.shell.syntax.StringArgument;

import static org.jnode.shell.syntax.Argument.EXISTING;
import static org.jnode.shell.syntax.Argument.MULTIPLE;
import static org.jnode.shell.syntax.Argument.NONEXISTENT;
import static org.jnode.shell.syntax.FileArgument.HYPHEN_IS_SPECIAL;

public class SortCommand extends AbstractCommand {
    
    private static final boolean DEBUG = true;
    
    private static final String help_files = "files to sort";
    private static final String help_output = "send output to this file, instead of stdout";
    private static final String help_field = "The field or field range to sort on";
    private static final String help_field_sep = "Use a specific character as the field separator";
    private static final String help_merge = "Assume the input is sorted and merge only";
    private static final String help_check = "Instead of sorting, check that the input is sorted.";
    private static final String help_numeric = "When sorting, sort numerically instead of lexicographically";
    private static final String help_reverse = "Reverse the sense of ordering";
    private static final String help_unique = "If sorting, do not insert lines with keys that already exist. If " +
                                              "checking, make sure all keys are unique";
    private static final String help_cmp_print = "For comparisons, ignore non-printing characters";
    private static final String help_cmp_alpha = "For comparisons, use only blank and alphanumeric characters";
    private static final String help_cmp_icase = "For comparisons, ignore case of keys when comparing";
    private static final String help_no_blanks = "Ignore leading blanks when determining that start and end " +
                                                 "positions of a key";
    private static final String help_super = "sort/merge files, or check that files are sorted";
    
    private static class Field {
        private int field;
        private int offset;
        private boolean ignoreBlanks;
        @SuppressWarnings("unused")
        private boolean sortNumeric;
        @SuppressWarnings("unused")
        private boolean cmpPrint;
        @SuppressWarnings("unused")
        private boolean cmpAlpha;
        @SuppressWarnings("unused")
        private boolean cmpICase;
        @SuppressWarnings("unused")
        private boolean reverse;
    }
    
    private static class FieldRange {
        private Field start;
        private Field end;
    }
    
    private static class Key {
        String[] parts;
        FieldRange[] ranges;
    }
    
    @SuppressWarnings("unused")
    private static class Entry {
        Key key;
        String value;
    }
    
    private static class KeyFieldArgument extends Argument<FieldRange> {
        private KeyFieldArgument(String label, int flags, String desc) {
            super(label, flags, new FieldRange[0], desc);
        }

        @Override
        protected FieldRange doAccept(CommandLine.Token token, int flags) throws CommandSyntaxException {
            String text = token.text;
            FieldRange range = new FieldRange();
            int i = text.indexOf(',');
            if (i == -1) {
                range.start = parseField(text, false);
                range.end   = new Field();
                range.end.field  = -1;
                range.end.offset = -1;
            } else {
                range.start = parseField(text.substring(0, i), false);
                range.end   = parseField(text.substring(i + 1), true);
            }
            return range;
        }

        @Override
        protected String argumentKind() {
            return "sort-field";
        }

        private static Field parseField(String text, boolean end) throws CommandSyntaxException {
            Field field  = new Field();
            int i = text.length() - 1;
        LOOP:
            while (true) {
                switch (text.charAt(i)) {
                    case 'b':
                        field.ignoreBlanks = true;
                        break;
                    case 'd':
                        field.cmpAlpha = true;
                        break;
                    case 'f':
                        field.cmpICase = true;
                        break;
                    case 'i':
                        field.cmpPrint = true;
                        break;
                    case 'n':
                        field.sortNumeric = true;
                        break;
                    case 'r':
                        field.reverse = true;
                        break;
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                    case '0':
                        break LOOP;
                    default:
                        throw new CommandSyntaxException("Invalid field: " + text);
                }
                i--;
            }
            text = text.substring(0, i + 1);
            i = text.indexOf('.');
            if (i == 0) {
                throw new CommandSyntaxException("Field offset cannot be empty");
            }
            if (i == (text.length() - 1)) {
                throw new CommandSyntaxException("Character offset cannot be empty if '.' is given");
            }
            try {
                if (i == -1) {
                    field.field = Integer.parseInt(text) - 1;
                    field.offset = end ? -1 : 0;
                } else {
                    field.field = Integer.parseInt(text.substring(0, i)) - 1;
                    field.offset = Integer.parseInt(text.substring(i + 1)) - 1;
                }
            } catch (NumberFormatException e) {
                throw new CommandSyntaxException("Invalid number: " + text);
            }
            if (field.field < 0) {
                throw new CommandSyntaxException("Field offset cannot be less than one: " + field.field);
            }
            if (field.offset < 0 && !end) {
                throw new CommandSyntaxException("Start character offset cannot be less than one: " + field.offset);
            }
            if (field.offset < -1 && end) {
                throw new CommandSyntaxException("End character offset cannot be less than zero");
            }
            return field;
        }
    }
    
    private class FieldComparator implements Comparator<String> {
        
        private boolean trim = false;

        @Override
        public boolean equals(Object o) {
            return o instanceof FieldComparator;
        }
        
        @Override
        public int compare(String a, String b) {
            for (FieldRange range : ranges) {
                trim = range.start.ignoreBlanks;
                String aKey = getKey(a, range);
                String bKey = getKey(b, range);
                int diff = aKey.compareTo(bKey);
                if (diff != 0) {
                    return diff;
                }
            }
            return 0;
        }
        
        // TODO
        // Refactor this out and calculate the keys before sorting
        private String getKey(String text, FieldRange range) {
            Field start = range.start;
            Field end   = range.end;
            String[] fields = (fieldSep != null) ? splitSep(text, fieldSep) : splitDefault(text);
            if (start.field >= fields.length) {
                return "";
            } else {
                if (end.field == -1) {
                    end.field = fields.length - 1;
                }
                if (start.field == end.field) {
                    int startPos = start.offset;
                    int field = start.field;
                    if ((end.offset == -1) || (end.offset >= fields[field].length())) {
                        return fields[field].substring(startPos);
                    } else {
                        return fields[field].substring(startPos, end.offset + 1);
                    }
                }
                StringBuilder capture = new StringBuilder();
                capture.append(fields[start.field].substring(start.offset));
                for (int i = start.field + 1; i < end.field; i++) {
                    capture.append(fields[i]);
                }
                if ((end.offset == -1) || (end.offset >= fields[end.field].length())) {
                    capture.append(fields[end.field]);
                } else {
                    capture.append(fields[end.field].substring(0, end.offset + 1));
                }
                return capture.toString();
            }
        }
        
        private String[] splitSep(String text, String sep) {
            List<String> fields = new LinkedList<String>();
            int mark = 0;
            int i;
            while ((i = text.indexOf(sep, mark)) != -1) {
                fields.add(text.substring(mark, i));
                mark = i + 1;
            }
            fields.add(text.substring(mark, text.length()));
            return fields.toArray(new String[fields.size()]);
        }
        
        private String[] splitDefault(String text) {
            List<String> fields = new LinkedList<String>();
            boolean haveField = false;
            int mark = 0;
            int i;
            for (i = 0; i < text.length(); i++) {
                if (isBlank(text.charAt(i))) {
                    if (haveField) {
                        fields.add(text.substring(mark, i));
                        mark = i;
                        haveField = false;
                    } else {
                        if (trim) {
                            mark = i;
                        }
                    }
                } else {
                    haveField = true;
                }
            }
            if (i > mark) {
                fields.add(text.substring(mark, i));
            }
            return fields.toArray(new String[fields.size()]);
        }
        
        private boolean isBlank(char c) {
            return c == ' ' || c == '\t';
        }
    }
    
    private final FileArgument argFile = new FileArgument("files", MULTIPLE | EXISTING | HYPHEN_IS_SPECIAL, help_files);
    private final FileArgument argOut        = new FileArgument("output", NONEXISTENT, help_output);
    private final FlagArgument argCheck      = new FlagArgument("check", 0, help_check);
    private final FlagArgument argMerge      = new FlagArgument("merge", 0, help_merge);
    private final FlagArgument argUnique     = new FlagArgument("unique", 0, help_unique);
    private final FlagArgument argNumeric    = new FlagArgument("numeric", 0, help_numeric);
    private final FlagArgument argReverse    = new FlagArgument("reverse", 0, help_reverse);
    private final FlagArgument argCmpPrint   = new FlagArgument("cmp-print", 0, help_cmp_print);
    private final FlagArgument argCmpAlpha   = new FlagArgument("cmp-alpha", 0, help_cmp_alpha);
    private final FlagArgument argCmpICase   = new FlagArgument("cmp-icase", 0, help_cmp_icase);
    private final FlagArgument argNoBlanks   = new FlagArgument("no-blanks", 0, help_no_blanks);
    private final KeyFieldArgument argField  = new KeyFieldArgument("field", MULTIPLE, help_field);
    private final StringArgument argFieldSep = new StringArgument("field-sep", 0, help_field_sep);
    
    private final IntegerArgument argSort = new IntegerArgument("sort", 0, " ");
    @SuppressWarnings("unused")
    private static final int SORT_ONE = 1;
    @SuppressWarnings("unused")
    private static final int SORT_TWO = 2;
    private static final int SORT_LAST = 1;
    
    private List<File> files;
    private File outputFile;
    private PrintWriter out;
    private PrintWriter err;
    private FieldRange[] ranges;
    private String fieldSep;
    private int rc;
    @SuppressWarnings("unused")
    private int sort;
    @SuppressWarnings("unused")
    private boolean check;
    @SuppressWarnings("unused")
    private boolean merge;
    @SuppressWarnings("unused")
    private boolean unique;
    @SuppressWarnings("unused")
    private boolean reverse;
    @SuppressWarnings("unused")
    private boolean numeric;
    @SuppressWarnings("unused")
    private boolean cmpPrint;
    @SuppressWarnings("unused")
    private boolean cmpAlpha;
    @SuppressWarnings("unused")
    private boolean cmpICase;
    @SuppressWarnings("unused")
    private boolean noBlanks;
    
    public SortCommand() {
        super(help_super);
        registerArguments(argFile, argOut, argField, argFieldSep, argMerge, argUnique, argNumeric, argReverse);
        registerArguments(argCmpPrint, argCmpAlpha, argCmpICase, argNoBlanks, argCheck);
        
        registerArguments(argSort);
    }
    
    public void execute() {
        err = getError().getPrintWriter();
        parseOptions();
        
        try {
            if (outputFile != null) {
                out = new PrintWriter(outputFile);
            } else {
                out = getOutput().getPrintWriter();
            }
            sortOne();
        } catch (IOException e) {
            // 
        } finally {
            //IOUtils.close(true, out);
            exit(rc);
        }
    }
    
    private void sortOne() {
        // OPTIMIZE
        // This is probably efficient enough for most use cases, but a lot
        // can be done to make this run faster, and not do so much buffering.
        // But it works for now...
        // Also of note, the -m (merge only) option is basically ignore, as
        // we're blindly sorting and merging all in one shot with Collections
        // merge sort. Again, not efficient, but it works.
        Comparator<String> cmp = new FieldComparator();
        List<String> allLines = new LinkedList<String>();
        
        for (File file : files) {
            List<String> lines;
            if (file.getName().equals("-")) {
                lines = IOUtils.readLines(getInput().getReader());
            } else {
                lines = IOUtils.readLines(file);
            }
            if (lines == null) {
                error("Problem reading file: " + file.getName());
                rc = 1;
                continue;
            }
            allLines.addAll(lines);
        }
        
        if (argField.isSet()) {
            Collections.sort(allLines, cmp);
        } else {
            Collections.sort(allLines);
        }
        for (String line : allLines) {
            out.println(line);
        }
    }
    
    @SuppressWarnings("unused")
    private void sortTwo() {
        
    }
    
    private void parseOptions() {
        if (argFile.isSet()) {
            files = Arrays.asList(argFile.getValues());
        } else {
            files = new ArrayList<File>(1);
            files.add(new File("-"));
        }
        if (argField.isSet()) {
            ranges = argField.getValues();
        }
        if (argFieldSep.isSet()) {
            fieldSep = argFieldSep.getValue();
        }
        if (argOut.isSet()) {
            outputFile = argOut.getValue();
        }
        check    = argCheck.isSet();
        merge    = argMerge.isSet();
        unique   = argUnique.isSet();
        numeric  = argNumeric.isSet();
        reverse  = argReverse.isSet();
        cmpPrint = argCmpPrint.isSet();
        cmpAlpha = argCmpAlpha.isSet();
        cmpICase = argCmpICase.isSet();
        noBlanks = argNoBlanks.isSet();
        
        sort = argSort.isSet() ? argSort.getValue() : SORT_LAST;
    }

    private void error(String s) {
        err.println(s);
    }
    
    @SuppressWarnings("unused")
    private void debug(String s) {
        if (DEBUG) {
            error(s);
        }
    }
}
