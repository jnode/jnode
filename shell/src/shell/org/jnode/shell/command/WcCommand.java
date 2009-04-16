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

package org.jnode.shell.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.command.posix.TrueCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.FlagArgument;

/**
 * Word, line and byte or character count command.
 * 
 * @see http://www.opengroup.org/onlinepubs/7990989775/xcu/wc.html
 * @author cy6erGn0m
 * @author Yves Galante
 */
public class WcCommand extends AbstractCommand {

    private static final String str_super = "Print newline, word, and byte counts for each file.";
    /** The Constant HELP_bytes. */
    private static final String HELP_BYTES = "Write to the standard output the number of bytes.";

    /** The Constant HELP_LINES. */
    private static final String HELP_LINES = "Write to the standard output the number of newline characters.";

    /** The Constant HELP_CHARS. */
    private static final String HELP_CHARS = "Write to the standard output the number of characters.";

    /** The Constant HELP_WORDS. */
    private static final String HELP_WORDS = "Write to the standard output the number of words.";

    /** The Constant HELP_maxCharsInLine. */
    private static final String HELP_MAX_CHARS_IN_LINE = "Write to the standard output the number of characters of " +
                                                         "the longest line.";

    /** Print bytes. */
    private boolean printBytes = false;

    /** Print lines. */
    private boolean printLines = false;

    /** Print chars. */
    private boolean printChars = false;

    /** Print words count. */
    private boolean printWordsCount = false;

    /** Print max chars in line. */
    private boolean printMaxCharsInLine = false;

    /** The Files flags. */
    private final FileArgument Files;

    /** The Bytes flags. */
    private final FlagArgument Bytes;

    /** The Lines flags. */
    private final FlagArgument Lines;

    /** The Chars flags. */
    private final FlagArgument Chars;

    /** The Worlds flags. */
    private final FlagArgument Words;

    /** The Max chars in line flags. */
    private final FlagArgument MaxCharsInLine;

    /**
     * Instantiates a new word count command.
     */
    public WcCommand() {
        super(str_super);
        this.Files = new FileArgument("files", Argument.OPTIONAL | Argument.EXISTING | Argument.MULTIPLE);
        this.Bytes = new FlagArgument("bytes", Argument.OPTIONAL, HELP_BYTES);
        this.Lines = new FlagArgument("lines", Argument.OPTIONAL, HELP_LINES);
        this.Chars = new FlagArgument("chars", Argument.OPTIONAL, HELP_CHARS);
        this.Words = new FlagArgument("worlds", Argument.OPTIONAL, HELP_WORDS);
        this.MaxCharsInLine = new FlagArgument("maxCharLine", Argument.OPTIONAL, HELP_MAX_CHARS_IN_LINE);
        registerArguments(this.Files, this.Bytes, this.Lines, this.Chars, this.Words, this.MaxCharsInLine);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jnode.shell.AbstractCommand#execute()
     */
    @Override
    public void execute() {

        try {
            final List<WcStream> results = new ArrayList<WcStream>(1);
            // Initialize arguments
            initArgs();
            // Read from files
            if (this.Files.isSet()) {
                for (final File file : this.Files.getValues()) {
                    final WcStream wcStream = new WcStream();
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(file);
                        wcStream.processStream(file.getName(), fis);
                    } finally {
                        fis.close();
                    }
                    results.add(wcStream);
                }
                // Or read from input stream
            } else {
                final WcStream wcStream = new WcStream();
                wcStream.processStream(null, getInput().getInputStream());
                results.add(wcStream);
            }
            // Print results
            printResults(getOutput().getPrintWriter(), results);
        } catch (final Exception e) {
            // TODO best error message
            getError().getPrintWriter().append(e.getMessage());
            exit(1);
        }
        exit(0);
    }

    /**
     * Initialize arguments.
     */
    private void initArgs() {
        if (this.Bytes.isSet() || this.Lines.isSet() || this.Chars.isSet() || this.Words.isSet()
                || this.MaxCharsInLine.isSet()) {
            this.printBytes = this.Bytes.isSet();
            this.printChars = this.Chars.isSet();
            this.printLines = this.Lines.isSet();
            this.printWordsCount = this.Words.isSet();
            this.printMaxCharsInLine = this.MaxCharsInLine.isSet();
        } else {
            // Default args
            this.printLines = true;
            this.printBytes = true;
            this.printChars = false;
            this.printWordsCount = true;
            this.printMaxCharsInLine = false;
        }
    }

    /**
     * Prints results.
     * 
     * @param printWriter
     *            the print writer
     * @param listWc
     *            the world count stream list
     */
    private void printResults(final PrintWriter printWriter, final List<WcStream> listWc) {

        long totalBytesRead = 0;
        long totalCharsCount = 0;
        long totalLinesCount = 0;
        long totalWordsCount = 0;
        long maxCharsInLine = 0;

        int paddingSize = 0;

        for (final WcStream wc : listWc) {
            totalBytesRead += wc.getBytesRead();
            totalCharsCount += wc.getCharsCount();
            totalLinesCount += wc.getLinesCount();
            totalWordsCount += wc.getWordsCount();
            if (maxCharsInLine < wc.getMaxCharsInLine()) {
                maxCharsInLine = wc.getMaxCharsInLine();
            }
        }
        // Compute the padding size for printing result on a table
        paddingSize = Long.toString(
                Math.max(totalBytesRead, Math.max(totalCharsCount, Math.max(totalLinesCount, totalWordsCount))))
                .length();

        for (final WcStream wc : listWc) {
            printLine(printWriter, paddingSize, wc.getLinesCount(), wc.getWordsCount(), wc.getCharsCount(), wc
                    .getBytesRead(), wc.getMaxCharsInLine());
            if (wc.getFileName() != null) {
                printWriter.print(" " + wc.getFileName());
            }
            printWriter.println();
        }
        // need print total line ?
        if (listWc.size() > 1) {
            printLine(printWriter, paddingSize, totalLinesCount, totalWordsCount, totalCharsCount, totalBytesRead,
                    maxCharsInLine);
            printWriter.print(" total");
            printWriter.println();
        }
        printWriter.flush();
    }

    /**
     * Prints the line.
     * 
     * @param printWriter
     *            the print writer
     * @param paddingSize
     *            the padding size
     * @param linesCount
     *            the lines count
     * @param wordsCount
     *            the words count
     * @param charsCount
     *            the chars count
     * @param bytesRead
     *            the bytes read
     * @param charsInLine
     *            the chars in line
     */
    private void printLine(final PrintWriter printWriter, final int paddingSize, final long linesCount,
            final long wordsCount, final long charsCount, final long bytesRead, final long charsInLine) {
        boolean first = true;
        if (this.printLines) {
            print(printWriter, first, paddingSize, linesCount);
            first = false;
        }
        if (this.printWordsCount) {
            print(printWriter, first, paddingSize, wordsCount);
            first = false;
        }
        if (this.printChars) {
            print(printWriter, first, paddingSize, charsCount);
            first = false;
        }
        if (this.printBytes) {
            print(printWriter, first, paddingSize, bytesRead);
            first = false;
        }
        if (this.printMaxCharsInLine) {
            print(printWriter, first, paddingSize, charsInLine);
            first = false;
        }
    }

    /**
     * Print a number.
     * 
     * @param printWriter
     *            the print writer
     * @param first
     *            the first
     * @param value
     *            the value
     * @param paddingSize
     *            the padding size
     */
    private void print(final PrintWriter printWriter, final boolean first, final int paddingSize, final long value) {
        final StringBuffer sValue = new StringBuffer(paddingSize + 1);

        sValue.append(value);
        while (sValue.length() < paddingSize) {
            sValue.insert(0, " ");
        }
        if (!first) {
            sValue.insert(0, " ");
        }
        printWriter.print(sValue);
    }

    /**
     * The Class WcStream.
     */
    private static class WcStream {

        /**
         * The Class ByteCountInputStream.
         */
        private static class ByteCountInputStream extends InputStream {

            /** The is. */
            private final InputStream inputStream;

            /** The bytes read. */
            private long bytesRead = 0;

            /**
             * Instantiates a new byte count input stream.
             * 
             * @param inputStream
             *            the input stream
             */
            private ByteCountInputStream(final InputStream inputStream) {
                this.inputStream = inputStream;
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.io.InputStream#read()
             */
            @Override
            public int read() throws IOException {
                final int rchar = this.inputStream.read();
                if (rchar != -1) {
                    this.bytesRead++;
                }
                return rchar;
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.io.InputStream#read(byte[], int, int)
             */
            @Override
            public int read(final byte[] b, final int off, final int len) throws IOException {
                final int wasRead = this.inputStream.read(b, off, len);
                if (wasRead > 0) {
                    this.bytesRead += wasRead;
                }
                return wasRead;
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.io.InputStream#read(byte[])
             */
            @Override
            public int read(final byte[] b) throws IOException {
                final int wasRead = this.inputStream.read(b);
                if (wasRead > 0) {
                    this.bytesRead += wasRead;
                }
                return wasRead;
            }

            /**
             * Gets the bytes read.
             * 
             * @return the bytes read
             */
            public long getBytesRead() {
                return this.bytesRead;
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.io.InputStream#available()
             */
            @Override
            public int available() throws IOException {
                return this.inputStream.available();
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.io.InputStream#markSupported()
             */
            @Override
            public boolean markSupported() {
                return false;
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.io.InputStream#skip(long)
             */
            @Override
            public long skip(final long n) throws IOException {
                final long rs = this.inputStream.skip(n);
                if (rs > 0) {
                    this.bytesRead += rs;
                }
                return rs;
            }
        }

        /** The file name. */
        private String fileName = null;

        /** The bytes read. */
        private long bytesRead = 0;

        /** The chars count. */
        private long charsCount = 0;

        /** The lines count. */
        private long linesCount = 0;

        /** The words count. */
        private long wordsCount = 0;

        /** The max chars in line. */
        private int maxCharsInLine = 0;

        /** The chars in line. */
        private int charsInLine = 0;

        /**
         * Instantiates a new wc stream.
         */
        private WcStream() {

        }

        /**
         * Gets the file name.
         * 
         * @return the file name
         */
        public String getFileName() {
            return this.fileName;
        }

        /**
         * Gets the bytes read.
         * 
         * @return the bytes read
         */
        public long getBytesRead() {
            return this.bytesRead;
        }

        /**
         * Gets the chars count.
         * 
         * @return the chars count
         */
        public long getCharsCount() {
            return this.charsCount;
        }

        /**
         * Gets the lines count.
         * 
         * @return the lines count
         */
        public long getLinesCount() {
            return this.linesCount;
        }

        /**
         * Gets the words count.
         * 
         * @return the words count
         */
        public long getWordsCount() {
            return this.wordsCount;
        }

        /**
         * Gets the max chars in line.
         * 
         * @return the max chars in line
         */
        public int getMaxCharsInLine() {
            return this.maxCharsInLine;
        }

        /**
         * Process stream.
         * 
         * @param fileName
         *            the file name
         * @param inputStream
         *            the input stream
         * 
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        private void processStream(final String fileName, final InputStream inputStream) throws IOException {
            final ByteCountInputStream bic = new ByteCountInputStream(inputStream);
            InputStreamReader reader = null;
            this.fileName = fileName;

            reader = new InputStreamReader(bic);
            boolean wasR = false;
            boolean wasWord = false;
            do {
                final int r = reader.read();
                if (r == -1) {
                    break;
                }
                final char ch = (char) r;
                if (ch == '\r') {
                    wasWord = false;
                    wasR = true;
                    this.linesCount++;
                    this.charsCount++;
                    if (this.charsInLine > this.maxCharsInLine) {
                        this.maxCharsInLine = this.charsInLine;
                        this.charsInLine = 0;
                    }
                } else if (ch == '\n') {
                    wasWord = false;
                    if (!wasR) {
                        this.linesCount++;
                        if (this.charsInLine > this.maxCharsInLine) {
                            this.maxCharsInLine = this.charsInLine;
                            this.charsInLine = 0;
                        }
                    }
                    wasR = false;
                    this.charsCount++;
                } else if (ch == ' ' || ch == '\t') {
                    wasR = false;
                    wasWord = false;
                    this.charsCount++;
                    this.charsInLine++;
                } else {
                    if (!wasWord) {
                        wasWord = true;
                        this.wordsCount++;
                    }
                    this.charsCount++;
                    this.charsInLine++;
                }
            } while (true);
            // if( charsCount > 0 )
            // linesCount++;
            this.bytesRead = bic.getBytesRead();
        }
    }

    /**
     * The main method.
     * 
     * @param args
     *            the arguments
     * 
     * @throws Exception
     *             the exception
     */
    public static void main(final String[] args) throws Exception {
        new TrueCommand().execute(args);
    }
}
