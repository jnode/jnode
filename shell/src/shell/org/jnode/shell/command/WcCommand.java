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
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.CommandSyntaxException;
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

    private static final String STR_SUPER = "Print newline, word, and byte counts for each file.";
    private static final String STR_TOTAL = "total";
    private static final String STR_ERROR_DIR = "File is a directoy : ";
    private static final String STR_ERROR_NOT_EXIST = "File not exist : ";
    private static final String STR_ERROR_CANT_READ = "File can't be read : ";
    private static final String STR_ERROR_IO_EX = "IO error";
    private static final String HELP_BYTES = "Write to the standard output the number of bytes.";
    private static final String HELP_LINES = "Write to the standard output the number of newline characters.";
    private static final String HELP_CHARS = "Write to the standard output the number of characters.";
    private static final String HELP_WORDS = "Write to the standard output the number of words.";
    private static final String HELP_MAX_CHARS = "Write to the standard output the number of characters of "
            + "the longest line.";

    private boolean printBytes = false;
    private boolean printLines = false;
    private boolean printChars = false;
    private boolean printWordsCount = false;
    private boolean printMaxCharsInLine = false;

    private final FileArgument filesArgs;
    private final FlagArgument bytesArgs;
    private final FlagArgument linesArgs;
    private final FlagArgument charsArgs;
    private final FlagArgument wordsArgs;
    private final FlagArgument maxChars;

    /**
     * Instantiates a new word count command.
     */
    public WcCommand() {
        super(STR_SUPER);
        this.filesArgs = new FileArgument("files", Argument.OPTIONAL | Argument.EXISTING | Argument.MULTIPLE);
        this.bytesArgs = new FlagArgument("bytes", Argument.OPTIONAL, HELP_BYTES);
        this.linesArgs = new FlagArgument("lines", Argument.OPTIONAL, HELP_LINES);
        this.charsArgs = new FlagArgument("chars", Argument.OPTIONAL, HELP_CHARS);
        this.wordsArgs = new FlagArgument("worlds", Argument.OPTIONAL, HELP_WORDS);
        this.maxChars = new FlagArgument("maxCharLine", Argument.OPTIONAL, HELP_MAX_CHARS);
        registerArguments(this.filesArgs, this.bytesArgs, this.linesArgs, this.charsArgs, this.wordsArgs, this.maxChars);
    }
    
    @Override
    public void execute() {
        final List<WcStream> results = new ArrayList<WcStream>(1);
        FileInputStream fis = null;
        // Initialize arguments
        initArgs();
        // Read from files
        if (this.filesArgs.isSet()) {
            for (final File file : this.filesArgs.getValues()) {
                checkFile(file);
                try {
                    fis = new FileInputStream(file);
                    results.add(new WcStream().processStream(file.getName(), fis));
                } catch (IOException io) {
                    getError().getPrintWriter().println(STR_ERROR_IO_EX + " : " + file.getAbsolutePath());
                    exit(1);
                } finally {
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            //
                        }
                        fis = null;
                    }
                }
            }
        } else {
            try {
                // Or read from input stream
                results.add(new WcStream().processStream(null, getInput().getInputStream()));
            } catch (IOException io) {
                getError().getPrintWriter().println(STR_ERROR_IO_EX);
                exit(1);
            }
        }
        // Print results
        printResults(getOutput().getPrintWriter(), results);
        exit(0);
    }

    private void checkFile(final File file) {
        if (!file.exists()) {
            getError().getPrintWriter().println(STR_ERROR_NOT_EXIST + " " + file.getAbsolutePath());
            exit(1);
        } else if (file.isDirectory()) {
            getError().getPrintWriter().println(STR_ERROR_DIR + " " + file.getAbsolutePath());
            exit(1);
        } else if (!file.canRead()) {
            getError().getPrintWriter().println(STR_ERROR_CANT_READ + " " + file.getAbsolutePath());
            exit(1);
        }
    }

    /**
     * Initialize arguments.
     */
    private void initArgs() {
        if (this.bytesArgs.isSet() || this.linesArgs.isSet() || this.charsArgs.isSet() || this.wordsArgs.isSet()
                || this.maxChars.isSet()) {
            this.printBytes = this.bytesArgs.isSet();
            this.printChars = this.charsArgs.isSet();
            this.printLines = this.linesArgs.isSet();
            this.printWordsCount = this.wordsArgs.isSet();
            this.printMaxCharsInLine = this.maxChars.isSet();
        } else {
            // Default arguments
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
            printWriter.println(" " + STR_TOTAL);
        }
        printWriter.flush();
    }

    /**
     * Print a line result
     * 
     * @param printWriter
     * @param paddingSize
     * @param linesCount
     * @param wordsCount
     * @param charsCount
     * @param bytesRead
     * @param charsInLine
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
     * Print a result
     * 
     * @param printWriter
     * @param first
     * @param paddingSize
     * @param value
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

            private final InputStream inputStream;

            private long bytesRead = 0;

            /**
             * Instantiates a new byte count input stream.
             * 
             * @param inputStream
             *            the input stream
             */
            private ByteCountInputStream(final InputStream inputStream) {
                super();
                this.inputStream = inputStream;
            }

            /**
             * Gets the bytes read.
             * 
             * @return the bytes read
             */
            public long getBytesRead() {
                return this.bytesRead;
            }

            @Override
            public int read() throws IOException {
                final int rchar = this.inputStream.read();
                if (rchar != -1) {
                    this.bytesRead++;
                }
                return rchar;
            }

            @Override
            public int read(final byte[] b, final int off, final int len) throws IOException {
                final int wasRead = this.inputStream.read(b, off, len);
                if (wasRead > 0) {
                    this.bytesRead += wasRead;
                }
                return wasRead;
            }

            @Override
            public int read(final byte[] b) throws IOException {
                final int wasRead = this.inputStream.read(b);
                if (wasRead > 0) {
                    this.bytesRead += wasRead;
                }
                return wasRead;
            }

            @Override
            public int available() throws IOException {
                return this.inputStream.available();
            }

            @Override
            public boolean markSupported() {
                return false;
            }

            @Override
            public long skip(final long n) throws IOException {
                final long rChar = this.inputStream.skip(n);
                if (rChar > 0) {
                    this.bytesRead += rChar;
                }
                return rChar;
            }
        }

        private String fileName = null;

        private long bytesRead = 0;

        private long charsCount = 0;

        private long linesCount = 0;

        private long wordsCount = 0;

        private int maxCharsInLine = 0;

        private int charsInLine = 0;

        private WcStream() {
            // default constructor
        }

        public String getFileName() {
            return this.fileName;
        }

        public long getBytesRead() {
            return this.bytesRead;
        }

        public long getCharsCount() {
            return this.charsCount;
        }

        public long getLinesCount() {
            return this.linesCount;
        }

        public long getWordsCount() {
            return this.wordsCount;
        }

        public int getMaxCharsInLine() {
            return this.maxCharsInLine;
        }

        /**
         * Process the stream.
         * 
         * @param fileName
         *            the file name, can be null
         * @param inputStream
         *            the input stream
         * 
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        private WcStream processStream(final String fileName, final InputStream inputStream) throws IOException {
            final ByteCountInputStream bic = new ByteCountInputStream(inputStream);
            InputStreamReader reader = null;
            this.fileName = fileName;

            reader = new InputStreamReader(bic);
            boolean wasR = false;
            boolean wasWord = false;
            int iChar = reader.read();
            for (; iChar >= 0; iChar = reader.read()) {
                final char cChar = (char) iChar;
                if (cChar == '\r') {
                    wasWord = false;
                    wasR = true;
                    this.linesCount++;
                    this.charsCount++;
                    if (this.charsInLine > this.maxCharsInLine) {
                        this.maxCharsInLine = this.charsInLine;
                        this.charsInLine = 0;
                    }
                } else if (cChar == '\n') {
                    if (!wasR) {
                        this.linesCount++;
                        if (this.charsInLine > this.maxCharsInLine) {
                            this.maxCharsInLine = this.charsInLine;
                            this.charsInLine = 0;
                        }
                    }
                    wasWord = false;
                    wasR = false;
                    this.charsCount++;
                } else if (cChar == ' ' || cChar == '\t') {
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
            }
            this.bytesRead = bic.getBytesRead();
            return this;
        }
    }

    /**
     * The main method.
     * 
     * @param args
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception {
        new WcCommand().execute(args);
    }
}
