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
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.FlagArgument;

/**
 * Count the words, lines and bytes or characters in a file or stream.
 * 
 * @see <a href="http://www.opengroup.org/onlinepubs/7990989775/xcu/wc.html">POSIX "wc" command</a>
 * @author cy6erGn0m
 * @author Yves Galante
 */
public class WcCommand extends AbstractCommand {

    private static final String STR_SUPER = "Print newline, word, and byte counts for each file.";
    private static final String STR_TOTAL = "total";
    private static final String STR_ERROR_DIR = "File is a directory : ";
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
        filesArgs = new FileArgument("files", Argument.OPTIONAL | Argument.EXISTING | Argument.MULTIPLE);
        bytesArgs = new FlagArgument("bytes", Argument.OPTIONAL, HELP_BYTES);
        linesArgs = new FlagArgument("lines", Argument.OPTIONAL, HELP_LINES);
        charsArgs = new FlagArgument("chars", Argument.OPTIONAL, HELP_CHARS);
        wordsArgs = new FlagArgument("worlds", Argument.OPTIONAL, HELP_WORDS);
        maxChars  = new FlagArgument("maxCharLine", Argument.OPTIONAL, HELP_MAX_CHARS);
        registerArguments(filesArgs, bytesArgs, linesArgs, charsArgs, wordsArgs, maxChars);
    }
    
    @Override
    public void execute() {
        List<WcStream> results = new ArrayList<WcStream>(1);
        FileInputStream fis = null;
        // Initialize arguments
        initArgs();
        // Read from files
        if (filesArgs.isSet()) {
            for (File file : filesArgs.getValues()) {
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

    private void checkFile(File file) {
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
        if (bytesArgs.isSet() || linesArgs.isSet() || charsArgs.isSet() || wordsArgs.isSet()
                || maxChars.isSet()) {
            printBytes = bytesArgs.isSet();
            printChars = charsArgs.isSet();
            printLines = linesArgs.isSet();
            printWordsCount = wordsArgs.isSet();
            printMaxCharsInLine = maxChars.isSet();
        } else {
            // Default arguments
            printLines = true;
            printBytes = true;
            printChars = false;
            printWordsCount = true;
            printMaxCharsInLine = false;
        }
    }

    /**
     * Prints results.
     * 
     * @param printWriter the print writer
     * @param listWc the world count stream list
     */
    private void printResults(PrintWriter printWriter, List<WcStream> listWc) {

        long totalBytesRead = 0;
        long totalCharsCount = 0;
        long totalLinesCount = 0;
        long totalWordsCount = 0;
        long maxCharsInLine = 0;
        int paddingSize = 0;

        for (WcStream wc : listWc) {
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

        for (WcStream wc : listWc) {
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
    private void printLine(PrintWriter printWriter, int paddingSize, long linesCount,
            long wordsCount, long charsCount, long bytesRead, long charsInLine) {
        boolean first = true;
        if (printLines) {
            print(printWriter, first, paddingSize, linesCount);
            first = false;
        }
        if (printWordsCount) {
            print(printWriter, first, paddingSize, wordsCount);
            first = false;
        }
        if (printChars) {
            print(printWriter, first, paddingSize, charsCount);
            first = false;
        }
        if (printBytes) {
            print(printWriter, first, paddingSize, bytesRead);
            first = false;
        }
        if (printMaxCharsInLine) {
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
    private void print(PrintWriter printWriter, boolean first, int paddingSize, long value) {
        StringBuffer sValue = new StringBuffer(paddingSize + 1);

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

            private InputStream inputStream;

            private long bytesRead = 0;

            /**
             * Instantiates a new byte count input stream.
             * 
             * @param in
             *            the input stream
             */
            private ByteCountInputStream(InputStream in) {
                super();
                inputStream = in;
            }

            /**
             * Gets the bytes read.
             * 
             * @return the bytes read
             */
            public long getBytesRead() {
                return bytesRead;
            }

            @Override
            public int read() throws IOException {
                int rchar = inputStream.read();
                if (rchar != -1) {
                    bytesRead++;
                }
                return rchar;
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                int wasRead = inputStream.read(b, off, len);
                if (wasRead > 0) {
                    bytesRead += wasRead;
                }
                return wasRead;
            }

            @Override
            public int read(byte[] b) throws IOException {
                int wasRead = inputStream.read(b);
                if (wasRead > 0) {
                    bytesRead += wasRead;
                }
                return wasRead;
            }

            @Override
            public int available() throws IOException {
                return inputStream.available();
            }

            @Override
            public boolean markSupported() {
                return false;
            }

            @Override
            public long skip(long n) throws IOException {
                long rChar = inputStream.skip(n);
                if (rChar > 0) {
                    bytesRead += rChar;
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
            return fileName;
        }

        public long getBytesRead() {
            return bytesRead;
        }

        public long getCharsCount() {
            return charsCount;
        }

        public long getLinesCount() {
            return linesCount;
        }

        public long getWordsCount() {
            return wordsCount;
        }

        public int getMaxCharsInLine() {
            return maxCharsInLine;
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
        private WcStream processStream(String name, InputStream inputStream) throws IOException {
            ByteCountInputStream bic = new ByteCountInputStream(inputStream);
            InputStreamReader reader = null;
            fileName = name;

            reader = new InputStreamReader(bic);
            boolean wasR = false;
            boolean wasWord = false;
            int iChar = reader.read();
            for (; iChar >= 0; iChar = reader.read()) {
                char cChar = (char) iChar;
                if (cChar == '\r') {
                    wasWord = false;
                    wasR = true;
                    linesCount++;
                    charsCount++;
                    if (charsInLine > maxCharsInLine) {
                        maxCharsInLine = charsInLine;
                        charsInLine = 0;
                    }
                } else if (cChar == '\n') {
                    if (!wasR) {
                        linesCount++;
                        if (charsInLine > maxCharsInLine) {
                            maxCharsInLine = charsInLine;
                            charsInLine = 0;
                        }
                    }
                    wasWord = false;
                    wasR = false;
                    charsCount++;
                } else if (cChar == ' ' || cChar == '\t') {
                    wasR = false;
                    wasWord = false;
                    charsCount++;
                    charsInLine++;
                } else {
                    if (!wasWord) {
                        wasWord = true;
                        wordsCount++;
                    }
                    charsCount++;
                    charsInLine++;
                }
            }
            bytesRead = bic.getBytesRead();
            return this;
        }
    }

    /**
     * The main method.
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        new WcCommand().execute(args);
    }
}
