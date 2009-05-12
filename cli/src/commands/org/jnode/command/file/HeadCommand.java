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

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.StringArgument;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * TODO add support for -c -<int> on stdin
 * @author chris boertien
 */
public class HeadCommand extends AbstractCommand {

    private static final String help_files = "Print the first 10 lines of each file to stdout, with multiple files a " +
                                             "header giving the file name is printed. With no file, or if file is - " +
                                             "read standard in.";
    private static final String help_bytes = "output the first <int> bytes, or output all but the last -<int> bytes.";
    private static final String help_lines = "output the first <int> lines, or output all but the last -<int> lines.";
    private static final String help_quiet = "never output headers giving file names";
    private static final String help_verbose = "always output headers giving file names";
    
    private final FileArgument Files;
    private final StringArgument Lines;
    private final StringArgument Bytes;
    private final FlagArgument Quiet;
    private final FlagArgument Verbose;
    
    private PrintWriter err;
    
    private int count;
    private boolean headers;
    private boolean useLines;
    private boolean reverse;
    private boolean first = true;
    
    public HeadCommand() {
        super("Print the head of a list of files, or stdin");
        Files   = new FileArgument("files", Argument.MULTIPLE | Argument.OPTIONAL, help_files);
        Lines   = new StringArgument("lines", Argument.OPTIONAL | Argument.EXISTING, help_lines);
        Bytes   = new StringArgument("bytes", Argument.OPTIONAL | Argument.EXISTING, help_bytes);
        Quiet   = new FlagArgument("quiet", Argument.OPTIONAL, help_quiet);
        Verbose = new FlagArgument("verbose", Argument.OPTIONAL, help_verbose);
        registerArguments(Files, Lines, Bytes, Quiet, Verbose);
    }
    
    public void execute() {
        File[] files;
        err      = getError().getPrintWriter();
        headers  = getHeaders();
        useLines = getCountType();
        count    = getCount();
        reverse  = getReversed();
        
        if (!Files.isSet()) {
            files = new File[1];
            files[0] = new File("-");
        } else {
            files = Files.getValues();
        }
        
        try {
            for (File file : files) {
                printHeader(file.getPath());
                if (file.getName().equals("-")) {
                    if (useLines) {
                        printStdinLines();
                    } else {
                        printStdinBytes();
                    }
                } else {
                    if (useLines) {
                        printFileLines(file);
                    } else {
                        printFileBytes(file);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            exit(1);
        }
    }
    
    private void printStdinLines() {
        try {
            printLines(new LineNumberReader(new InputStreamReader(getInput().getInputStream())));
        } catch (IOException ex) {
            err.println(ex);
        }
    }
    
    private void printStdinBytes() {
        try {
            printBytes(getInput().getInputStream(), -1);
        } catch (IOException ex) {
            err.println(ex);
        }
    }
    
    private void printFileLines(File file) {
        LineNumberReader reader = null;
        try {
            reader = new LineNumberReader(new FileReader(file));
            printLines(reader);
        } catch (IOException ex) {
            err.println(ex);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException _) {
                    // ignore
                }
            }
        }
    }
    
    private void printFileBytes(File file) {
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            printBytes(in, (int) file.length());
        } catch (IOException ex) {
            err.println(ex);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException _) {
                    // ignore
                }
            }
        }
    }
    
    private void printLines(LineNumberReader reader) throws IOException {
        PrintWriter out = getOutput().getPrintWriter();
        String line;
        if (!reverse) {
            int n = count;
            while (--n >= 0 && (line = reader.readLine()) != null) {
                out.println(line);
            }
        } else {
            List<String> lines = new ArrayList<String>();
            
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            
            int numLines = lines.size() - count;
            for (int n = 0; n < numLines; n++) {
                out.println(lines.get(n));
            }
        }
    }
    
    private void printBytes(InputStream in, int size) throws IOException {
        OutputStream out = getOutput().getOutputStream();
        byte[] buffer;
        int len;
        int n;
        int bufsize = 8 * 1024;
        
        if (!reverse) {
            n = count;
        } else if (size != -1) {
            n = size - count;
        } else {
            // FIXME
            throw new UnsupportedOperationException("Cannot do -count byte reads on stdin yet");
        }
        
        buffer = new byte[Math.min(n, bufsize)];
        while (n > 0 && (len = in.read(buffer)) > 0) {
            len = Math.min(n, len);
            out.write(buffer, 0, len);
            n -= len;
        }
    }
    
    private void printHeader(String name) {
        PrintWriter out = getOutput().getPrintWriter();
        if (headers) {
            if (!first) {
                out.println();
            }
            first = false;
            if (name.equals("-")) {
                out.println("==> standard input <==");
            } else {
                out.println("==> " + name + " <==");
            }
        }
    }
    
    private boolean getHeaders() {
        if (Quiet.isSet()) {
            return false;
        } else if (Verbose.isSet()) {
            return true;
        } else {
            return headers = Files.isSet() && Files.getValues().length > 1;
        }
    }
    
    private boolean getCountType() {
        return !Bytes.isSet();
    }
    
    private int getCount() {
        int count = 10;
        if (Bytes.isSet()) {
            count = parseInt(Bytes.getValue());
        }
        if (Lines.isSet()) {
            count = parseInt(Lines.getValue());
        }
        return count < 0 ? 10 : count;
    }
    
    private boolean getReversed() {
        if (Bytes.isSet()) {
            return Bytes.getValue().charAt(0) == '-';
        } else if (Lines.isSet()) {
            return Lines.getValue().charAt(0) == '-';
        }
        return false;
    }
    
    private int parseInt(String s) {
        if (s.charAt(0) == '-') {
            s = s.substring(1);
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }
}
