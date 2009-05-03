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
 
package org.jnode.command.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;

import java.util.regex.Matcher;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jnode.command.util.AbstractDirectoryWalker;
import org.jnode.command.util.AbstractDirectoryWalker.PathnamePatternFilter;
import org.jnode.command.util.IOUtils;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.IntegerArgument;
import org.jnode.shell.syntax.StringArgument;

import java.io.Closeable;
import java.io.Flushable;
import java.io.Writer;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * TODO check performance of prefixing, probably needs some buffering.
 * TODO implement outputting context lines (requires buffering output lines)
 * TODO implement Fixed/Basic/Ext matchers
 * TODO implement --color (if/when possible)
 * @author peda
 * @author crawley@jnode.org
 * @author chris boertien
 */
public class GrepCommand extends AbstractCommand {

    private static final Logger log = Logger.getLogger(GrepCommand.class);
    private static final boolean DEBUG = true;
    private static final int BUFFER_SIZE = 8192;
    
    private static final String help_matcher_fixed = "Patterns are fixed strings, seperated by new lines. Any of " +
                                                     "which is to be matched.";
    private static final String help_matcher_basic = "Use basic regular expressions (Default).";
    private static final String help_matcher_ext = "Use extended regular expressions.";
    private static final String help_matcher_perl = "Use perl regular expressions.";
    private static final String help_case = "Ignore case in both the <pattern> and the input files.";
    private static final String help_invert = "Invert the matching to select non-matching lines.";
    private static final String help_match_word = "Select only those lines containing matches that form whole words." +
                                                 "The test is that the matching substring must either be at the " +
                                                 "beginning of the line, or preceded by a non-word character. It " +
                                                 "also must be at the end of a line, or followed by a non-word " +
                                                 "character. Word characters are letters, digits and the underscore.";
    private static final String help_match_line = "Select only those matches that match the whole line";
    private static final String help_count = "Suppress normal output, instead printing a count of matching lines for " +
                                            "input file. With --invert-match, count non-matching lines.";
    private static final String help_file_nomatch = "Suppress normal output, instead printing the name of each input " +
                                                    "file from which no output would normally be printed. Scanning " +
                                                    "stops after making a single match.";
    private static final String help_file_match = "Suppress normal output, isntead printing the name of each input " +
                                                 "file from which output would normally be printed. Scanning stops " +
                                                 "after making a single match.";
    private static final String help_max = "Stop reading a file after n matches. If the input is stdin from a " +
                                          "regular file, and n matching lines are output, grep ensures that the " +
                                          "standard input is positioned, to just after the last matching line before " +
                                          "exiting, regardless of trailing context lines. When grep stops after n " +
                                          "matching lines, it will output trailing context lines. When used with " +
                                          "--count, count wil never be more than n. When used with --invert, grep " +
                                          "stops after n non-matching lines.";
    private static final String help_only_matching = "Print only the matched parts of the matching line. with each " +
                                                    "part on a seperate output line.";
    private static final String help_quiet = "Do not write _anything_ to stdout. Exit immediately with zero status if" +
                                            " any match is found, even if an error was detected.";
    private static final String help_suppress = "Suppress error messages about nonexistant or unreadable files.";
    private static final String help_debug = "Output debug information.";
    private static final String help_prefix_byte = "Print the 0-based byte offset within the input file before each " +
                                                  "line of output. If --only-matching is set, print the offset of " +
                                                  "the matching part itself.";
    private static final String help_prefix_file = "Print the file name for each match. This is the default when " +
                                                  "there are multiple files.";
    private static final String help_prefix_nofile = "Suppress the prefixing of file names on output. This is the " +
                                                    "default when there is only one file, or only stdin to search.";
    private static final String help_prefix_label = "Display input coming from stdin as actually coming from the " +
                                                   "given label.";
    private static final String help_prefix_line = "Prefix each line of output with the 1-based line number within " +
                                                   "its input file.";
    private static final String help_prefix_tab = "Make sure that the first character of actual line content lies on " +
                                                  "a tab stop so that the alignment looks normal. This also causes " +
                                                  "the line number and byte offset, if output, to be printed in a " +
                                                  "minimum size field width.";
    private static final String help_prefix_null = "Output a zero byte instead of the character that normally " +
                                                   "a file name, which is normally a newline.";
    private static final String help_context_after = "Print n lines of trailing context lines after matching lines. " +
                                                     "Places a line containing a group seperator '--' between " +
                                                     "contiguous groups of matches. With --only-matching this has no " +
                                                     "effect and a warning is given.";
    private static final String help_context_before = "Print n lines of leading context lines before matching lines. " +
                                                      "Places a line containing a group seperator '--' between " +
                                                      "contiguous groups of matches. With --only-matching this has " +
                                                      "no effect and a waring is given.";
    private static final String help_context_both = "Print n lines of leading and trailing context. Places a line " +
                                                    "containing a group seperator '--' between contiguous groups of " +
                                                    "matches. With --only-matching this has no effect and a warning " +
                                                    "is given.";
    private static final String help_mode_binary = "If the first few bytes of a file indicate that the file contains " +
                                                   "binary data, assume the file is the specified type. By default " +
                                                   "the type is 'binary' and grep outputs a one line message saying " +
                                                   "that the binary file matches or nothing if there is no match. If " +
                                                   "type is 'without-match' grep assumes that binary files do not " +
                                                   "match. If type is 'text' grep processes a binary file as if it " +
                                                   "were text. __Warning__: --binary text may output binary garbage " +
                                                   "which can have side effects if the output is a terminal.";
    private static final String help_mode_binary_text = "Same as --binary text";
    private static final String help_mode_binary_skip = "Same as --binary without-match";
    private static final String help_mode_device = "If an input file is a device, fifo or socket, then set this to " +
                                                   "'read' to read the data like a normal file, or 'skip' to not " +
                                                   "read these types of files. The default is read.";
    private static final String help_mode_dir = "If an input file is a directory, then set this to 'read' to read " +
                                                "directory file itself. Set it to 'skip' to skip listed directories. " +
                                                "Set it to recurse in order to tell grep to search listed " +
                                                "directories for files to match recursively. The default is read.";
    private static final String help_mode_dir_recurse = "Same as --directories recurse.";
    private static final String help_exclude = "Skip processing files whose basename matches the given glob-pattern.";
    private static final String help_exclude_file = "Skip processing files whos basenames matches any of the " +
                                                    "glob patterns in the given file, one per line.";
    private static final String help_exclude_dir = "Exclude directories matching the given glob-pattern.";
    private static final String help_include = "Only process files whose basename matches the given glob-pattern.";
    private static final String help_null_term = "Treat the input as a set of lines terminated by a null byte " +
                                                 "instead of a newline.";
    private static final String help_patterns = "Match the given pattern(s) against the input files.";
    private static final String help_pattern_files = "File with patterns to match, one per line.";
    private static final String help_files = "The files to match against. If there are no files, or if any file is " +
                                             "'-' then match stdandard input.";
    private static final String err_ex_walker = "Exception while walking.";
    
    private class ContextLineWriter implements Closeable, Flushable {
        
        protected LineNumberReader reader;
        
        private PrintWriter writer;
        private Deque<String> contextStack;
        private int linesUntilFlush;
        private int linesForFlush;
        private int contextBefore;
        private int contextAfter;
        private boolean haveLine;
        private boolean firstFlush;
        
        public ContextLineWriter(Writer writer, int before, int after) {
            if (writer instanceof PrintWriter) {
                this.writer = (PrintWriter) writer;
            } else {
                this.writer = new PrintWriter(writer);
            }
            
            firstFlush      = true;
            contextBefore   = before;
            contextAfter    = after;
            linesForFlush   = before + after + 1;
            contextStack    = new ArrayDeque<String>();
        }
        
        public ContextLineWriter(OutputStream out, int before, int after) {
            this(new PrintWriter(out), before, after);
        }
        
        @Override
        public void close() {
            if (reader != null) {
                finish();
            }
            IOUtils.close(true, writer);
            writer = null;
        }
        
        @Override
        public void flush() {
            doFlush();
            while(contextStack.size() > 0) {
                writer.println(contextStack.removeFirst());
            }
            haveLine = false;
            writer.flush();
        }
        
        public void setIn(InputStream in) throws IOException {
            setIn(new InputStreamReader(in));
        }
        
        public LineNumberReader setIn(Reader reader) throws IOException {
            if (isClosed()) {
                throw new IOException("Stream closed");
            }
            if (this.reader != null) {
                finish();
            }
            
            this.reader = new LineNumberReader(reader, BUFFER_SIZE) {
                @Override
                public String readLine() throws IOException {
                    String line = super.readLine();
                    if (line != null) {
                        if (!haveLine) {
                            if (contextStack.size() > 0) {
                                contextStack.addLast(doContextLine(contextStack.removeLast()));
                            }
                            if (contextStack.size() > contextBefore) {
                                if (contextStack.size() > (contextBefore + 1)) {
                                    log.debug("Too many 'before' lines on stack!");
                                }
                                contextStack.removeFirst();
                            }
                        } else {
                            if (linesUntilFlush < (linesForFlush)) {
                                contextStack.addLast(doContextLine(contextStack.removeLast()));
                            }
                            if (linesUntilFlush == 0) {
                                String[] saveLines = new String[contextBefore];
                                for (int i = 0; i < contextBefore; i++) {
                                    saveLines[i] = contextStack.removeLast();
                                }
                                contextStack.removeLast();
                                flush();
                                for (int i = 0; i < contextBefore; i++) {
                                    contextStack.addLast(saveLines[i]);
                                }
                            } else {
                                linesUntilFlush--;
                            }
                        }
                        contextStack.addLast(line);
                    } else {
                        if (!haveLine) {
                            contextStack.clear();
                        } else {
                            int excessLines = contextAfter - (linesForFlush - linesUntilFlush);
                            if (excessLines > 0) {
                                for (int i = 0; i < excessLines; i++) {
                                    contextStack.removeLast();
                                }
                            }
                        }
                        finish();
                    }
                    return line;
                }
            };
            
            return this.reader;
        }
        
        public void addLine(String s) throws IOException {
            if (isClosed()) {
                throw new IOException("Stream closed");
            }
            contextStack.addLast(s);
            writeLast();
        }
        
        public void rewriteLast(String s) throws IOException {
            if (isClosed()) {
                throw new IOException("Stream closed");
            }
            contextStack.removeLast();
            contextStack.addLast(s);
            writeLast();
        }
        
        public void writeLast() throws IOException {
            if (isClosed()) {
                throw new IOException("Stream closed");
            }
            if (!haveLine) {
                haveLine = true;
            }
            linesUntilFlush = linesForFlush;
        }
        
        private void finish() {
            if (reader == null) return;
            if (haveLine) {
                flush();
            }
            doFinish();
            IOUtils.close(reader);
            reader = null;
        }
        
        private boolean isClosed() {
            return writer == null;
        }
        
        private boolean haveLine() {
            return haveLine;
        }
        
        protected void doFlush() {
            if (!firstFlush) {
                contextStack.addFirst("-----");
            }
            firstFlush = false;
        }
        
        protected void doFinish() {
            // no-op
        }
        
        protected String doContextLine(String s) {
            return prefixLine(s, currentFile, currentLine, currentByte, '-');
        }
    }

    private final StringArgument Patterns;
    private final FileArgument PatternFiles;
    private final FileArgument Files;
    private final FlagArgument MatcherFixed;
    private final FlagArgument MatcherBasic;
    private final FlagArgument MatcherExt;
    private final FlagArgument MatcherPerl;
    
    private final FlagArgument IgnoreCase;
    private final FlagArgument Invert;
    private final FlagArgument MatchWord;
    private final FlagArgument MatchLine;
    
    private final FlagArgument ShowCount;
    private final FlagArgument ShowFileNoMatch;
    private final FlagArgument ShowFileMatch;
    private final FlagArgument ShowOnlyMatch;
    private final IntegerArgument MaxCount;
    private final FlagArgument Quiet;
    private final FlagArgument Suppress;
    
    private final FlagArgument PrefixByte;
    private final FlagArgument PrefixFile;
    private final FlagArgument PrefixNoFile;
    private final StringArgument PrefixLabel;
    private final FlagArgument PrefixLine;
    private final FlagArgument PrefixTab;
    private final FlagArgument PrefixNull;
    
    private final IntegerArgument ContextAfter;
    private final IntegerArgument ContextBefore;
    private final IntegerArgument ContextBoth;
    
    private final StringArgument ModeBinary;
    private final StringArgument ModeDevice;
    private final StringArgument ModeDir;
    private final FlagArgument ModeBinaryText;
    private final FlagArgument ModeBinarySkip;
    private final FlagArgument ModeDirRecurse;
    private final StringArgument Exclude;
    private final FileArgument ExcludeFile;
    private final StringArgument ExcludeDir;
    private final StringArgument Include;
    
    private final FlagArgument NullTerm;
    private final FlagArgument Debug;
    
    private static final int MATCHER_FIXED = 1;
    private static final int MATCHER_BASIC = 2;
    private static final int MATCHER_EXT   = 3;
    private static final int MATCHER_PERL  = 4;
    
    private static final int PREFIX_FILE   = 0x01;
    private static final int PREFIX_LINE   = 0x02;
    private static final int PREFIX_BYTE   = 0x04;
    private static final int PREFIX_NOFILE = 0x08;
    private static final int PREFIX_TAB    = 0x10;
    private static final int PREFIX_NULL   = 0x20;
    private static final int PREFIX_ALL    = PREFIX_FILE | PREFIX_LINE | PREFIX_BYTE;
    private static final int PREFIX_FL     = PREFIX_FILE | PREFIX_LINE;
    private static final int PREFIX_FB     = PREFIX_FILE | PREFIX_BYTE;
    private static final int PREFIX_LB     = PREFIX_LINE | PREFIX_BYTE;
    
    private PrintWriter err;
    private PrintWriter out;
    private ContextLineWriter contextOut;
    private Reader in;
    private InputStream stdin;
    private OutputStream stdout;
    private List<File> files;
    private List<Pattern> patterns;
    private String prefixLabel;
    private Matcher match;
    private String currentFile;
    private int matcher;
    private int prefix;
    private int maxCount = Integer.MAX_VALUE;
    private int contextBefore;
    private int contextAfter;
    private int patternFlags;
    private int rc = 1;
    private int currentLine;
    private int currentByte;
    private boolean inverse;
    private boolean matchCase;
    private boolean matchWord;
    private boolean matchLine;
    private boolean showCount;
    private boolean showFileMatch;
    private boolean showFileNoMatch;
    private boolean showOnlyMatch;
    private boolean quiet;
    private boolean suppress;
    private boolean debug;
    private boolean recurse;
    private boolean dirAsFile;
    private boolean binaryAsText;
    private boolean binaryAsBinary;
    private boolean readDevice;
    private boolean exitOnFirstMatch;
    
    public GrepCommand() {
        super("Search for lines that match a string or regex");
        MatcherFixed = new FlagArgument("matcher-fixed", 0, help_matcher_fixed);
        MatcherBasic = new FlagArgument("matcher-basic", 0, help_matcher_basic);
        MatcherExt   = new FlagArgument("matcher-ext", 0, help_matcher_ext);
        MatcherPerl  = new FlagArgument("matcher-perl", 0, help_matcher_perl);
        registerArguments(MatcherFixed, MatcherBasic, MatcherExt, MatcherPerl);
        
        IgnoreCase   = new FlagArgument("ignore-case", 0, help_case);
        Invert       = new FlagArgument("invert", 0, help_invert);
        MatchWord    = new FlagArgument("word-match", 0, help_match_word);
        MatchLine    = new FlagArgument("line-match", 0, help_match_line);
        MaxCount     = new IntegerArgument("max-matches", 0, help_max);
        Quiet        = new FlagArgument("quiet", 0, help_quiet);
        Suppress     = new FlagArgument("suppress", 0, help_suppress);
        Debug        = new FlagArgument("debug", 0, help_debug);
        registerArguments(IgnoreCase, Invert, MatchWord, MatchLine, MaxCount, Quiet, Suppress, Debug);
        
        ShowCount       = new FlagArgument("show-count", 0, help_count);
        ShowFileNoMatch = new FlagArgument("show-files-nomatch", 0, help_file_nomatch);
        ShowFileMatch   = new FlagArgument("show-files-match", 0, help_file_match);
        ShowOnlyMatch   = new FlagArgument("show-only-match", 0, help_only_matching);
        registerArguments(ShowCount, ShowFileNoMatch, ShowFileMatch, ShowOnlyMatch);
        
        PrefixByte   = new FlagArgument("prefix-byte", 0, help_prefix_byte);
        PrefixFile   = new FlagArgument("prefix-file", 0, help_prefix_file);
        PrefixNoFile = new FlagArgument("prefix-nofile", 0, help_prefix_nofile);
        PrefixLabel  = new StringArgument("prefix-label", 0, help_prefix_label);
        PrefixLine   = new FlagArgument("prefix-line", 0, help_prefix_line);
        PrefixTab    = new FlagArgument("prefix-tab", 0, help_prefix_tab);
        PrefixNull   = new FlagArgument("prefix-null", 0, help_prefix_null);
        registerArguments(PrefixByte, PrefixFile, PrefixNoFile, PrefixLabel, PrefixLine, PrefixTab, PrefixNull);
        
        ContextAfter  = new IntegerArgument("show-context-after", 0, help_context_after);
        ContextBefore = new IntegerArgument("show-context-before", 0, help_context_before);
        ContextBoth   = new IntegerArgument("show-context-both", 0, help_context_both);
        registerArguments(ContextAfter, ContextBefore, ContextBoth);
        
        ModeBinary     = new StringArgument("mode-binary", 0, help_mode_binary);
        ModeDevice     = new StringArgument("mode-device", 0, help_mode_device);
        ModeDir        = new StringArgument("mode-dir", 0, help_mode_dir);
        ModeBinaryText = new FlagArgument("mode-binary-text", 0, help_mode_binary_text);
        ModeBinarySkip = new FlagArgument("mode-binary-skip", 0, help_mode_binary_skip);
        ModeDirRecurse = new FlagArgument("mode-dir-recurse", 0, help_mode_dir_recurse);
        Exclude        = new StringArgument("pattern-exclude", 0, help_exclude);
        ExcludeFile    = new FileArgument("pattern-exclude-file", Argument.EXISTING, help_exclude_file);
        ExcludeDir     = new StringArgument("pattern-exclude-dir", 0, help_exclude_dir);
        Include        = new StringArgument("pattern-include", 0, help_include);
        registerArguments(ModeBinary, ModeBinaryText, ModeBinarySkip, ModeDevice, ModeDir, ModeDirRecurse);
        registerArguments(Exclude, ExcludeFile, ExcludeDir, Include);
        
        NullTerm = new FlagArgument("null-term", 0, help_null_term);
        Patterns = new StringArgument("patterns", Argument.MULTIPLE | Argument.MANDATORY, help_patterns);
        PatternFiles = new FileArgument("pattern-files", Argument.MULTIPLE | Argument.EXISTING, help_pattern_files);
        Files = new 
            FileArgument("files", Argument.MULTIPLE | Argument.EXISTING | FileArgument.HYPHEN_IS_SPECIAL, help_files);
        registerArguments(Patterns, PatternFiles, Files, NullTerm);
        
        // Default matcher
        match = Pattern.compile(".*").matcher("");
    }

    /**
     * main method, normally not used, use execute instead!!
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        new GrepCommand().execute(args);
    }

    /**
     * Primary entry point
     */
    public void execute() throws Exception {
        err    = getError().getPrintWriter();
        in     = getInput().getReader();
        out    = getOutput().getPrintWriter();
        stdout = getOutput().getOutputStream();
        stdin  = getInput().getInputStream();
        
        LineNumberReader reader;
        String name;
        
        try {
            parseOptions();
            if ((contextBefore > 0) || (contextAfter > 0)) {
                debug("Using ContextLineWriter");
                debug("Before=" + contextBefore);
                debug("After=" + contextAfter);
                contextOut = new ContextLineWriter(out, contextBefore, contextAfter);
            }
            
            for (File file : files) {
                debug("Processing file: " + file);
                reader = null;
                name   = file.getPath();
                try {
                    if (name.equals("-")) {
                        if (contextOut != null) {
                            reader = contextOut.setIn(in);
                        } else {
                            reader = new LineNumberReader(in, BUFFER_SIZE);
                        }
                        name = prefixLabel;
                    } else {
                        if (contextOut != null) {
                            reader = contextOut.setIn(IOUtils.openReader(file));
                        } else {
                            reader = IOUtils.openLineReader(file, BUFFER_SIZE);
                        }
                    }
                    currentFile = name;
                    if (exitOnFirstMatch) {
                        debug(" exitOnFirstMatch");
                        if (matchUntilOne(reader)) {
                            rc = 0;
                            break;
                        }
                        continue;
                    }
                    if (showFileMatch) {
                        debug(" showFileMatch");
                        if (matchUntilOne(reader)) {
                            printFile(name);
                        }
                        continue;
                    }
                    if (showFileNoMatch) {
                        debug(" showFileNoMatch");
                        if (!matchUntilOne(reader)) {
                            printFile(name);
                        }
                        continue;
                    }
                    if (showCount) {
                        debug(" showCount");
                        printFileCount(matchCount(reader));
                        continue;
                    }
                    debug(" normal");
                    matchNormal(reader);
                } catch (IOException e) {
                    error("IOException greping file : " + file);
                    e.printStackTrace();
                } finally {
                    IOUtils.close(reader);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            exit(rc);
        }
    }
    
    /* Each of the next few methods are inner loops for different conditions. This
     * is mostly to avoid a complex set of branches inside the inner loop. With any
     * luck they will get inlined anyway.
     */
    
    /**
     * Matches lines in a file until a single match is made.
     */
    private boolean matchUntilOne(LineNumberReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (match(line) != null) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Counts the number of matching or non-matching lines in a file.
     */
    private int matchCount(LineNumberReader reader) throws IOException {
        String line;
        int ret = 0;
        while ((ret < maxCount) && ((line = reader.readLine()) != null)) {
            if (((match(line) != null) ^ inverse)) {
                ret++;
            }
        }
        return ret;
    }
    
    /**
     * Uses the MatchResult to only print the substring of the line that matched.
     */
    private void matchSubstring(LineNumberReader reader, String name) throws IOException {
        String line;
        MatchResult result;
        int byteCount = 0;
        int matches = 0;
        while ((matches < maxCount) && (line = reader.readLine()) != null) {
            result = match(line);
            if (result != null) {
                printMatch(line.substring(result.start(), result.end()), 
                           name, reader.getLineNumber(), byteCount + result.start());
                rc = 0;
                matches++;
            }
            byteCount += line.length();
        }
    }
    
    /**
     * Prints matching or non-matching lines to stdout with a possible prefix.
     */
    private void matchNormal(LineNumberReader reader) throws IOException {
        String line;
        MatchResult result;
        int matches = 0;
        
        while ((matches < maxCount) && ((line = reader.readLine()) != null)) {
            result = match(line);
            currentLine = reader.getLineNumber();
            if ((match(line) != null) ^ inverse) {
                printMatch(line, currentFile, currentLine, currentByte);
                rc = 0;
                matches++;
            }
            currentByte += line.length() + 1;
        }
    }
    
    /**
     * Compares the line to a list of patterns, returing the result of the first one to match.
     */
    private MatchResult match(String line) {
        match.reset(line);
        for (Pattern pattern : patterns) {
            if (match.reset().usePattern(pattern).find()) {
                return match.toMatchResult();
            }
        }
        return null;
    }
    
    private String prefixLine(String line, String name, int lineCount, int byteCount, char fieldSep) {
        if (prefix == PREFIX_NOFILE) {
            return line;
        }
        
        StringBuilder sb = new StringBuilder();
        
        if ((prefix & PREFIX_TAB) != 0) {
            if ((prefix & PREFIX_FILE) != 0) {
                sb.append(name);
                if ((prefix & (PREFIX_LINE | PREFIX_BYTE)) == 0) {
                    sb.append("\t");
                }
                sb.append(fieldSep);
            }
            if ((prefix & PREFIX_LINE) != 0) {
                sb.append(padNumber(lineCount, 4));
                if ((prefix & PREFIX_BYTE) == 0) {
                    sb.append("\t");
                }
                sb.append(fieldSep);
            }
            if ((prefix & PREFIX_BYTE) != 0) {
                sb.append(padNumber(byteCount, 9));
                sb.append("\t");
                sb.append(fieldSep);
            }
        } else {
            if ((prefix & PREFIX_FILE) != 0) {
                sb.append(name);
                sb.append(fieldSep);
            }
            if ((prefix & PREFIX_LINE) != 0) {
                sb.append(lineCount);
                sb.append(fieldSep);
            }
            if ((prefix & PREFIX_BYTE) != 0) {
                sb.append(byteCount);
                sb.append(fieldSep);
            }
        }
        return sb.append(line).toString();
    }
    
    /**
     * Outputs a matched string, in the given file, at the given line and the given byte offset. Outputs
     * to stdout the match string, along with any set prefix options.
     */
    private void printMatch(String line, String name, int lineCount, int byteCount) throws IOException {
        if (quiet) return;
        String newLine = prefixLine(line, name, lineCount, byteCount, ':');
        if (contextOut != null) {
            if (newLine == line) {
                contextOut.writeLast();
            } else {
                contextOut.rewriteLast(newLine);
            }
        } else {
            out.println(newLine);
        }
    }
    
    /**
     * Outputs the name and count seperated by a colon or null byte
     */
    private void printFileCount(int count) {
        if (quiet) return;
        out.print(currentFile);
        out.print(":");
        out.println(count);
    }
    
    /**
     * Outputs a file name and appends a null byte if PREFIX_NULL is set, otherwise it
     * appends a newline.
     */
    private void printFile(String name) {
        if (quiet) return;
        out.print(name);
        if ((prefix & PREFIX_NULL) != 0) {
            out.print('\u0000');
        } else {
            out.println();
        }
    }
    
    private String padNumber(int n, int size) {
        return String.format("%" + size + "d", n);
    }
    
    /**
     * grep uses different mnemonics for character classes, need to convert grep-style
     * to java-style.
     * Perl regex is unformatted
     * Ext regex is formatted as follows
     * [:alnum:] = \p{Alnum}
     * [:alpha:] = \p{Alpha}
     * [:cntrl:] = \p{Cntrl}
     * [:digit:] = \p{Digit}
     * [:graph:] = ???
     * [:lower:] = \p{Lower}
     * [:print:] = ???
     * [:punct:] = \p{Punct}
     * [:upper:] = \p{Upper}
     * [:xdigit:] = [0-9A-Fa-f]
     * \< and \> = match word boundary
     * In basic
     */
    private Pattern rewritePattern(String pattern) throws PatternSyntaxException {
        debug("Pattern before: " + pattern);
        int flags = 0;
        
        if (matcher == MATCHER_FIXED) {
            pattern = Pattern.quote(pattern);
        }
        if (!matchCase) {
            flags |= Pattern.CASE_INSENSITIVE;
        }
        
        StringBuilder sb = new StringBuilder(pattern);
        
        switch(matcher) {
            case MATCHER_BASIC :
                // de-sensitize some meta-characters
                // fall through
            case MATCHER_EXT :
                // perform class synax conversion ([:class:] -> \p{Class})
                // fall through :
            case MATCHER_PERL :
                // nothing for now
                // fall through
            default :
                break;
        }
        
        if (matchWord) {
            if (!pattern.startsWith("\\b")) {
                sb.insert(0, "\\b");
            }
            if (!pattern.endsWith("\\b")) {
                sb.append("\\b");
            }
        } else if (matchLine) {
            if (!pattern.startsWith("^")) {
                sb.insert(0, '^');
            }
            if (!pattern.endsWith("$")) {
                sb.append('$');
            }
        }
        
        debug("Pattern after : " + sb);
        return Pattern.compile(sb.toString(), flags);
    }
    
    /*********************************************************/
    /************** Command Line Parsing *********************/
    /*********************************************************/
    
    private void parseOptions() {
        // parse these early so they are enforced
        quiet    = Quiet.isSet();
        suppress = Suppress.isSet();
        debug    = DEBUG || Debug.isSet();
        
        if (PrefixLabel.isSet())  prefixLabel = PrefixLabel.getValue();
        else                      prefixLabel = "stdin";
        if (PrefixByte.isSet())   prefix |= PREFIX_BYTE;
        if (PrefixFile.isSet())   prefix |= PREFIX_FILE;
        if (PrefixNoFile.isSet()) prefix |= PREFIX_NOFILE;
        if (PrefixLine.isSet())   prefix |= PREFIX_LINE;
        if (PrefixTab.isSet())    prefix |= PREFIX_TAB;
        if (PrefixNull.isSet())   prefix |= PREFIX_NULL;
        
        if ((prefix & (PREFIX_FILE | PREFIX_NOFILE)) == (PREFIX_FILE | PREFIX_NOFILE)) {
            prefix ^= PREFIX_NOFILE;
        }
        
        if (MatcherFixed.isSet()) matcher = MATCHER_FIXED;
        if (MatcherBasic.isSet()) matcher = MATCHER_BASIC;
        if (MatcherExt.isSet())   matcher = MATCHER_EXT;
        if (MatcherPerl.isSet())  matcher = MATCHER_PERL;
        if (matcher == 0)         matcher = MATCHER_BASIC;
        matchWord   = MatchWord.isSet();
        matchLine   = MatchLine.isSet();
        matchCase   = !IgnoreCase.isSet();
        inverse = Invert.isSet();
        parsePatterns();  // This requires the above options be parsed already.
        
        if (MaxCount.isSet()) maxCount = MaxCount.getValue();
        
        showCount       = ShowCount.isSet();
        showFileMatch   = ShowFileMatch.isSet();
        showFileNoMatch = ShowFileNoMatch.isSet();
        showOnlyMatch   = ShowOnlyMatch.isSet();
        
        // Setup for fast-path exit(0) on first match
        if (quiet || (inverse && showOnlyMatch)) {
            exitOnFirstMatch = true;
        }
        
        String s = " ";
        if (ModeBinary.isSet()) {
            s = ModeBinary.getValue();
        }
        
        if (!(ModeBinarySkip.isSet() || s.equals("without-match"))) {
            if (ModeBinaryText.isSet() || s.equals("text")) {
                binaryAsText = true;
            } else {
                binaryAsBinary = true;
            }
        }
        
        s = " ";
        if (ModeDir.isSet()) {
            s = ModeDir.getValue();
        }
        
        if (ModeDirRecurse.isSet() || s.equals("recurse")) {
            recurse = true;
        }
        
        if (!(ModeDevice.isSet() && ModeDevice.getValue().equals("skip"))) {
            readDevice = true;
        }
        
        parseFiles();    // This requires the above options be parsed already.
        if (files.size() > 1) {
            if ((prefix & PREFIX_NOFILE) == 0) {
                prefix |= PREFIX_FILE;
            }
        } else {
            if ((prefix & PREFIX_FILE) == 0) {
                prefix |= PREFIX_NOFILE;
            }
        }
        
        if ((prefix & (PREFIX_FILE | PREFIX_NOFILE)) == (PREFIX_FILE | PREFIX_NOFILE)) {
            throw new AssertionError("PREFIX_NOFILE && PREFIX_FILE");
        }
        
        if (ContextBoth.isSet()) {
            contextAfter = contextBefore = ContextBoth.getValue();
        } else if (ContextBefore.isSet()) {
            contextBefore = ContextBefore.getValue();
        } else if (ContextAfter.isSet()) {
            contextAfter = ContextAfter.getValue();
        }
    }
    
    private void parsePatterns() {
        BufferedReader reader;
        String line;
        patterns = new ArrayList<Pattern>();
        
        for (String s : Patterns.getValues()) {
            try {
                patterns.add(rewritePattern(s));
            } catch (PatternSyntaxException e) {
                error("Invalid Pattern : " + s);
                exit(2);
            }
        }
        
        for (File file : PatternFiles.getValues()) {
            reader = null;
            try {
                reader = IOUtils.openBufferedReader(file, BUFFER_SIZE);
                while ((line = reader.readLine()) != null) {
                    try {
                        patterns.add(rewritePattern(line));
                    } catch (PatternSyntaxException e) {
                        error("Invalid Pattern : " + line);
                        exit(2);
                    }
                }
            } catch (IOException e) {
                debug("IOException while parsing pattern file : " + file);
                error("Error reading file: " + file);
                exit(2);
            } finally {
                IOUtils.close(reader);
            }
        }
    }
    
    private class Walker extends AbstractDirectoryWalker {
        @Override
        public void handleFile(File file) {
            files.add(file);
        }
        @Override
        public void handleDir(File dir) {
            // no-op
        }
        @Override
        public void handleRestrictedFile(File file) {
            // no-op
        }
        
        private void doFile(File file) {
            if (notFiltered(file)) {
                files.add(file);
            }
        }
    }
    
    private void parseFiles() {
        String line;
        String name;
        BufferedReader reader;
        
        files = new ArrayList<File>();
        
        if (!Files.isSet()) {
            files.add(new File("-"));
            return;
        }
        
        Walker walker = new Walker();
        
        for (String s : Include.getValues()) {
            walker.addFilter(new PathnamePatternFilter(s, false));
        }
        
        for (String s : Exclude.getValues()) {
            walker.addFilter(new PathnamePatternFilter(s, true));
        }
        
        for (File file : ExcludeFile.getValues()) {
            reader = IOUtils.openBufferedReader(file, BUFFER_SIZE);
            List<String> lines = null;
            try {
                lines = IOUtils.readLines(reader);
            } finally {
                IOUtils.close(reader);
            }
            if (lines != null) {
                for (String s : lines) {
                    walker.addFilter(new PathnamePatternFilter(s, true));
                }
            }
        }
        
        List<String> excludeDirs  = new ArrayList<String>();
        
        for (final String s : ExcludeDir.getValues()) {
            walker.addDirectoryFilter(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return !(file.isDirectory() && file.getName().equals(s));
                }
            });
        }
        
        List<File> dirs = new ArrayList<File>();
        
        for (File file : Files.getValues()) {
            if (file.isDirectory()) {
                if (recurse) {
                    dirs.add(file);
                }
            } else if (file.isFile()) {
                walker.doFile(file);
            } else {
                // skip special files
            }
        }
        
        try {
            if (dirs.size() > 0) {
                walker.walk(dirs);
            }
        } catch (IOException e) {
            // technically, the walker shouldn't let this propogate unless something
            // is really wrong.
            error(err_ex_walker);
            exit(2);
        }
    }
    
    private void error(String s) {
        if (!suppress) err.println(s);
    }
    
    private void debug(String s) {
        if (debug) log.debug(s);
    }
    
    private void debugOptions() {
        debug("Files : " + files.size());
        for (File file : files) {
            debug(" - " + file);
        }
        debug("Patterns : " + patterns.size());
        for (Pattern p : patterns) {
            debug(" - " + p);
        }
    }
}
