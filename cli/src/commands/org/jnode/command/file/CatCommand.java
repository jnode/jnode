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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URL;

import org.jnode.command.util.IOUtils;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.URLArgument;

/**
 * Read files or network resources and write the concatenation to standard output.  If
 * no filenames or URIs are provided, copy standard input to standard output.  Data is
 * copied byte-wise unless flags are used that add additional formatting to the lines.
 * <p>
 * If any file or URL cannot be opened, it is skipped and we (eventually) set a non-zero
 * return code.  If we get an IOException reading or writing data, we allow it to propagate.
 * 
 * @author epr
 * @author Andreas H\u00e4nel
 * @author crawley@jnode.org
 * @author Fabien DUMINY (fduminy@jnode.org)
 * @author chris boertien
 */
public class CatCommand extends AbstractCommand {

    private static final String HELP_FILE = "the files to be concatenated";
    private static final String HELP_URL = "the urls to be concatenated";
    private static final String HELP_URLS = "If set, arguments will be urls";
    private static final String HELP_NUM_NB = "If set, number nonempty output lines";
    private static final String HELP_NUM = "If set, number all output lines";
    private static final String HELP_ENDS = "If set, print a $ at the end of each lines";
    private static final String HELP_SQUEEZE = "If set, supress printing of sequential blank lines";
    private static final String HELP_SUPER = "Concatenate the contents of files, urls or standard input";
    private static final String ERR_URL = "Cannot fetch %s: %s%n";
    private static final String ERR_FILE = "Cannot open %sn";
    
    private final FileArgument argFile;
    private final FlagArgument argNumNB;
    private final FlagArgument argNumAll;
    private final FlagArgument argEnds;
    private final FlagArgument argSqueeze;
    
    private final URLArgument argUrl;
    private final FlagArgument argUrls;

    private PrintWriter err;
    private PrintWriter out;
    private Reader in;
    private InputStream stdin;
    private OutputStream stdout;
    private File[] files;
    private String end;
    private int rc = 0;
    private int lineCount;
    private boolean squeeze;
    private boolean numAll;
    private boolean numNB;
    private boolean useStreams;
    
    public CatCommand() {
        super(HELP_SUPER);
        int fileFlags = Argument.MULTIPLE | Argument.EXISTING | FileArgument.HYPHEN_IS_SPECIAL;
        argFile    = new FileArgument("file", fileFlags, HELP_FILE);
        argNumNB   = new FlagArgument("num-nonblank", 0, HELP_NUM_NB);
        argNumAll  = new FlagArgument("num", 0, HELP_NUM);
        argEnds    = new FlagArgument("show-ends", 0, HELP_ENDS);
        argSqueeze = new FlagArgument("squeeze", 0, HELP_SQUEEZE);
        registerArguments(argFile, argNumNB, argNumAll, argEnds, argSqueeze);
        
        argUrl  = new URLArgument("url", Argument.MULTIPLE | Argument.EXISTING, HELP_URL);
        argUrls = new FlagArgument("urls", Argument.OPTIONAL, HELP_URLS);
        registerArguments(argUrl, argUrls);
    }
    
    private static final int BUFFER_SIZE = 8192;
    
    public static void main(String[] args) throws Exception {
        new CatCommand().execute(args);
    }
    
    public void execute() throws IOException {
        in     = getInput().getReader();
        stdin  = getInput().getInputStream();
        out    = getOutput().getPrintWriter();
        stdout = getOutput().getOutputStream();
        err    = getError().getPrintWriter();
        
        parseOptions();
        
        if (files != null && files.length > 0) {
            handleFiles();
            out.flush();
            exit(rc);
        }
        
        // FIXME remove this url code once wget is more complete
        URL[] urls = argUrl.getValues();
        if (urls != null && urls.length > 0) {
            byte[] buffer = new byte[BUFFER_SIZE];
            for (URL url : urls) {
                InputStream is = null;
                try {
                    is = url.openStream();
                } catch (IOException ex) {
                    err.format(ERR_URL, url, ex.getLocalizedMessage());
                    rc = 1;
                }
                if (is != null) {
                    try {
                        IOUtils.copyStream(is, stdout, buffer);
                    } finally {
                        IOUtils.close(is);
                    }
                }
            }
            out.flush();
            exit(rc);
        }
        
        // should not reach this
        throw new IllegalStateException("Nothing to process");
    }
    
    private boolean handleFiles() {
        InputStream in = null;
        BufferedReader reader = null;
        byte[] buffer = new byte[BUFFER_SIZE];
        boolean ok = true;
        
        for (File file : files) {
            try {
                if (useStreams) {
                    if ((in = openFileStream(file)) != null) {
                        IOUtils.copyStream(in, stdout, buffer);
                    } else {
                        rc = 1;
                    }
                } else {
                    if ((reader = openFileReader(file)) != null) {
                        processReader(reader);
                    } else {
                        rc = 1;
                    }
                }
            } catch (IOException e) {
                rc = 1;
            } finally {
                IOUtils.close(in, reader);
            }
        }
        
        return ok;
    }
    
    /**
     * Process the input through a BufferedReader.
     *
     * Instead of doing a straight stream->stream copy, we process line by line
     * in order to do some per-line editing before we send to stdout.
     */
    private void processReader(BufferedReader reader) throws IOException {
        String line;
        boolean haveBlank = false;
        
        while ((line = reader.readLine()) != null) {
            if (line.length() == 0) {
                if (!haveBlank) {
                    haveBlank = true;
                } else if (squeeze) {
                    continue;
                }
            } else {
                haveBlank = false;
            }
            
            if (numAll) {
                println(line, ++lineCount);
            } else if (numNB) {
                println(line, (haveBlank ? -1 : ++lineCount));
            } else {
                println(line, 0);
            }
        }
    }
    
    /**
     * Attempt to open a file, writing an error message on failure. If the file
     * is '-', return stdin.
     *
     * @param file the file to open the stream on
     * @return the reader, or null
     */
    private InputStream openFileStream(File file) {
        InputStream ret = null;
        
        if (file.getName().equals("-")) {
            ret = stdin;
        } else {
            ret = IOUtils.openInputStream(file);
            if (ret == null) {
                err.format(ERR_FILE, file);
            }
        }
        
        return ret;
    }
    
    /**
     * Attempt to open a file reader, writing an error message on failure. If the file
     * is '-', return stdin.
     *
     * @param file the file to open the reader on
     * @return the reader, or null
     */
    private BufferedReader openFileReader(File file) {
        BufferedReader ret = null;
        
        if (file.getName().equals("-")) {
            ret = new BufferedReader(in, BUFFER_SIZE);
        } else {
            ret = IOUtils.openBufferedReader(file, BUFFER_SIZE);
            if (ret == null) {
                err.format(ERR_FILE, file);
            }
        }
        
        return ret;
    }
    
    private void parseOptions() {
        files = argFile.getValues();
        
        if (files == null || files.length == 0) {
            files = new File[] {new File("-")};
        }
        
        useStreams = true;
        
        if (argNumNB.isSet()) {
            numNB = true;
            useStreams = false;
        } else if (argNumAll.isSet()) {
            numAll = true;
            useStreams = false;
        }
        
        if (argEnds.isSet()) {
            end = "$";
            useStreams = false;
        } else {
            end = "";
        }
        
        if (argSqueeze.isSet()) {
            squeeze = true;
            useStreams = false;
        }
    }
    
    private void println(String s, int line) {
        if (numNB || numAll) {
            if (line == -1) {
                out.format("       %s%s%n", s, end);
            } else {
                out.format("%6d %s%s%n", line, s, end);
            }
        } else {
            out.format("%s%s%n", s, end);
        }
    }
}
