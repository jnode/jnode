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

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.IntegerArgument;
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
 * TODO add follow support
 * TODO add support for -c +<int> on stdin
 * 
 * @author chris boertien
 */
public class TailCommand extends AbstractCommand {
    
    private static final String help_files = "Print the last 10 lines of each file to stdout, with multiple files a " +
                                             "header giving the file name is printed. With no file, or if file is - " +
                                             "read standard in.";
    private static final String help_bytes = "output the last <int> bytes, or output from +<int> bytes into the file " +
                                             "to the end.";
    private static final String help_follow = "keep the file open, outputing data as the file grows.";
    private static final String help_follow_retry = "same as -f --retry";
    private static final String help_lines = "Output the last <int> lines instead of, or +<int> to start printing " +
                                             "from that line";
    private static final String help_unchanged = "with -f, reopen the file when the size has not change for <int> " +
                                                 "iterations to see if it has be unlinked or renamed Default is 5";
    private static final String help_pid = "with -f, terminate after process PID does (how?)";
    private static final String help_sleep = "with -f, sleep for <int> seconds between iterations. Default is 1";
    private static final String help_quiet = "never output headers giving file names";
    private static final String help_verbose = "always output headers giving file names";
    private static final String help_retry = "keep trying to open a file even if it is inaccessible at the start, or " +
                                             "if it becomes inaccessible.";
    
    private final FileArgument Files;
    private final StringArgument Bytes;
    private final StringArgument Lines;
    // FIXME this has to be able to handle -f --follow and --folow=<file>
    private final FlagArgument Follow;
    private final FlagArgument FollowR;
    private final IntegerArgument MaxUnchanged;
    private final IntegerArgument Sleep;
    private final FlagArgument Retry;
    private final FlagArgument Quiet;
    private final FlagArgument Verbose;
    // TODO This might work as thread id, since we dont have process ids
    //private final IntegerArgument PID = new IntegerArgument("pid", Argument.OPTIONAL, help_pid);
    
    private PrintWriter err;
    
    private int count;
    private int sleep;
    private int unchanged;
    private boolean headers;
    private boolean useLines;
    private boolean reverse;
    private boolean follow;
    private boolean retry;
    
    public TailCommand() {
        super("Print the tail end of a list of files, or stdin.");
        Files = new FileArgument("files", Argument.OPTIONAL | Argument.MULTIPLE, help_files);
        Bytes = new StringArgument("bytes", Argument.EXISTING | Argument.OPTIONAL, help_bytes);
        Lines = new StringArgument("lines", Argument.EXISTING | Argument.OPTIONAL, help_lines);
        Follow = new FlagArgument("follow", Argument.OPTIONAL, help_follow);
        FollowR = new FlagArgument("followr", Argument.OPTIONAL, help_follow_retry);
        MaxUnchanged = new IntegerArgument("unchanged", Argument.OPTIONAL, help_unchanged);
        Sleep = new IntegerArgument("sleep", Argument.OPTIONAL, help_sleep);
        Retry = new FlagArgument("retry", Argument.OPTIONAL, help_retry);
        Quiet = new FlagArgument("quiet", Argument.OPTIONAL, help_quiet);
        Verbose = new FlagArgument("verbose", Argument.OPTIONAL, help_verbose);
        registerArguments(Files, Bytes, Lines, Follow, Retry, FollowR, MaxUnchanged, Sleep, Quiet, Verbose);
    }
    
    public void execute() {
        File[] files;
        err       = getError().getPrintWriter();
        headers   = getHeaders();
        useLines  = getCountType();
        count     = getCount();
        reverse   = getReversed();
        follow    = Follow.isSet() || FollowR.isSet();
        retry     = Retry.isSet()  || FollowR.isSet();
        sleep     = Sleep.isSet() ? Sleep.getValue() : 1;
        unchanged = MaxUnchanged.isSet() ? MaxUnchanged.getValue() : 5;
        
        if (follow) {
            err.println("Follow not supported yet");
            exit(1);
        }
        
        if (!Files.isSet()) {
            files = new File[1];
            files[0] = new File("-");
        } else {
            files = Files.getValues();
        }
        
        try {
            for (File file : files) {
                printHeader(file.getName());
                if (!file.getName().equals("-")) {
                    if (useLines) {
                        printFileLines(file);
                    } else {
                        printFileBytes(file);
                    }
                } else {
                    if (useLines) {
                        printStdinLines();
                    } else {
                        printStdinBytes();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
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
        if (reverse) {
            int n = count;
            while (--n >= 0 && (line = reader.readLine()) != null) {
                // no-op
            }
            while ((line = reader.readLine()) != null) {
                out.println(line);
            }
        } else {
            List<String> lines = new ArrayList<String>();
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            
            int n = lines.size() - count;
            for (; n < lines.size(); n++) {
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
        
        if (reverse) {
            n = count;
        } else if (size != -1) {
            n = size - count;
        } else {
            throw new UnsupportedOperationException("Cannot do -count byte reads on stdin yet");
        }
        in.skip(n);
        
        buffer = new byte[bufsize];
        while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
    }
    
    private void printHeader(String name) {
        PrintWriter out = getOutput().getPrintWriter();
        if (headers) {
            out.println();
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
            return Bytes.getValue().charAt(0) == '+';
        } else if (Lines.isSet()) {
            return Lines.getValue().charAt(0) == '+';
        }
        return false;
    }

    private int parseInt(String s) {
        if (s.charAt(0) == '+') {
            s = s.substring(1);
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }
}
