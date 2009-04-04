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

package org.jnode.fs.command.archive;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FlagArgument;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.IOException;

import java.util.ArrayList;

public class ArchiveCommand extends AbstractCommand {
    
    private static final String help_verbose = "show the compression ratio for each file compressed";
    private static final String help_debug = "internal debug output";
    private static final String help_quiet = "supress non-essential warning messages";
    private static final String help_stdout = "pipe data to stdout";
    private static final String help_force = "force overwrite of output files";
    protected static final String help_decompress = "force decompression";
    
    protected static final String prompt_overwrite = " already exists. Do you wish to overwrite? [Y/n]: ";
    
    protected static final String err_exception_uncaught = "Unhandled Exception thrown";
    protected static final String err_file_create = "Could not create file: ";
    protected static final String err_file_not_exist = "Could not find file: ";
    protected static final String err_stream_create = "Could not create stream: ";
    
    protected static final String fmt_size_diff = "%s:\t%f.2%% -- replaced with %s";
    
    protected final FlagArgument Quiet = new FlagArgument("quiet", Argument.OPTIONAL, help_quiet);
    protected final FlagArgument Verbose = new FlagArgument("verbose", Argument.OPTIONAL, help_verbose);
    protected final FlagArgument Debug = new FlagArgument("debug", Argument.OPTIONAL, help_debug);
    protected final FlagArgument Stdout = new FlagArgument("stdout", Argument.OPTIONAL, help_stdout);
    protected final FlagArgument Force = new FlagArgument("force", Argument.OPTIONAL, help_force);
    
    protected static final int OUT_FATAL = 0x01;
    protected static final int OUT_ERROR = 0x02;
    protected static final int OUT_WARN  = 0x04;
    protected static final int OUT_NOTICE = 0x08;
    protected static final int OUT_DEBUG = 0x80;
    
    protected static final int BUFFER_SIZE = 4096;
    
    protected int outMode = OUT_ERROR | OUT_WARN;
    
    protected PrintWriter stdoutWriter;
    protected PrintWriter stderrWriter;
    protected Reader stdinReader;
    protected InputStream stdin;
    protected OutputStream stdout;
    
    protected String commandName;
    protected boolean use_stdout;
    protected boolean force;
    protected boolean compress;
    
    private byte[] buffer;
    
    private ArchiveCommand() {}
    
    protected ArchiveCommand(String s) {
        super(s);
    }
    
    public void execute(String command) {
        stdoutWriter = getOutput().getPrintWriter();
        stderrWriter = getError().getPrintWriter();
        stdinReader  = getInput().getReader();
        stdin = getInput().getInputStream();
        stdout = getOutput().getOutputStream();
        
        if (command.equals("zcat") || command.equals("bzcat")) return;
        
        if (!command.equals("tar")) {
            if (Quiet.isSet()) outMode = 0;
            force = Force.isSet();
        }
        
        if (!use_stdout) use_stdout = Stdout.isSet();
        
        if (outMode != 0) {
            if (Verbose.isSet()) outMode |= OUT_NOTICE;
            if (Debug.isSet()) outMode |= OUT_DEBUG;
        }
    }
    
    protected void createStreamBuffer(int size) {
        buffer = new byte[size];
    }
    
    protected File[] processFileList(File[] files, boolean recurse) {
        if (files == null || files.length == 0) return null;
        
        if (files.length == 1 && !files[0].isDirectory()) {
            if (files[0].getName().equals("-")) return null;
            return files;
        }
        
        ArrayList<File> _files = new ArrayList<File>();
        
        for (File file : files) {
            debug(file.getName());
            if (file.getName().equals("-")) {
                if (use_stdout) {
                    // A special case where '-' is found amongst a list of files and are
                    // being piped to stdout. The idea is that the content from - should
                    // be concated amongst the list of files. Its more of an error if the
                    // destination is not stdout.
                    _files.add(file);
                } else {
                    fatal("Found stdin in file list.", 1);
                }
            }
            if (!file.exists()) {
                error(err_file_not_exist + file);
                continue;
            }
            if (file.isDirectory() && recurse) {
                File[] dirList = file.listFiles();
                for (File subFile : dirList) {
                    if (subFile.isFile()) {
                        _files.add(subFile);
                    }
                }
            } else {
                if (file.isFile()) {
                    _files.add(file);
                }
            }
        }
        
        if (_files.size() == 0) return null;
        return _files.toArray(files);
    }
    
    /**
     * Pipes the contents of the InputStream into the OutputStream.
     *
     * This is most usefull for applying a stream filter that reads data from a source
     * and pipes the contents to an output stream.
     *
     * @param InputStream stream to read from
     * @param OutputStream stream to write to
     * @param int size of buffer to use.
     */
    protected void processStream(InputStream in , OutputStream out) throws IOException {
        int len;
        if (buffer == null) buffer = new byte[4096];
        while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
    }
    
    /**
     * Opens a FileOutputStream for a file.
     *
     * If there is a problem opening the stream, the exception is caught and an error message
     * is displayed.
     *
     * @param File the file to open the stream on
     * @param boolean if the file exists, delete it first
     * @param boolean if delete is true, this forces the deletion without prompting the user
     * @return an OutputStream on the file, or null if there was a problem. null could also be
     *         returned if the delete option was chosen and the user said no to overwriting.
     */
    protected OutputStream openFileWrite(File file , boolean delete , boolean forced) {
        try {
            boolean createNew = true;
            if (file == null) {
                error(err_file_create + "null");
                return null;
            }
            
            if (file.exists()) {
                if (delete) {
                    if (forced) {
                        file.delete();
                    } else {
                        if (prompt_yn(file + prompt_overwrite, true)) {
                            file.delete();
                        } else {
                            notice("Skipping " + file);
                            return null;
                        }
                    }
                } else {
                    return new FileOutputStream(file);
                }
            }
            if (createNew && !file.createNewFile()) {
                error(err_file_create + file);
                return null;
            }
            return new FileOutputStream(file);
        } catch (IOException ioe) {
            error(err_stream_create + file);
            return null;
        }
    }
    
    /**
     * Opens a FileInputStream on a file.
     *
     * If there is a problem opening the stream, the IOException is caught, and an
     * error message displayed to the console.
     *
     * @param the file to open the stream on
     * @return the InputStream or null if there was a problem.
     */
    protected InputStream openFileRead(File file) {
        try {
            if (file == null || !file.exists()) {
                error(err_file_not_exist + file);
                return null;
            }
            return new FileInputStream(file);
        } catch (IOException ioe) {
            error(err_stream_create + file);
            return null;
        }
    }
    
    /**
     * Prompt the user with a question asking for a yes or no answer.
     *
     * @param String the question to ask the user
     * @return true if the user said yes, false if the user said no
     */
    protected boolean prompt_yn(String s, boolean defaultY) {
        int choice;
        for (;;) {
            stdoutWriter.print(s);
            try {
                choice = stdinReader.read();
            } catch (IOException _) {
                throw new RuntimeException("Problem with stdin");
            }
            stdoutWriter.println();
            if (choice == 'y') return true;
            if (choice == 'n') return false;
            if (choice == '\n') return defaultY;
        }
    }
    
    protected void out(String s) {
        stdoutWriter.println(s);
    }
    
    protected void err(String s) {
        stderrWriter.println(s);
    }
    
    protected void debug(String s) {
        if ((outMode & OUT_DEBUG) == OUT_DEBUG) {
            stderrWriter.print("debug: ");
            stderrWriter.println(s);
        }
    }
    
    protected void notice(String s) {
        if ((outMode & OUT_NOTICE) == OUT_NOTICE) stdoutWriter.println(s);
    }
    
    protected void warn(String s) {
        if ((outMode & OUT_WARN) == OUT_WARN) stdoutWriter.println(s);
    }
    
    protected void error(String s) {
        if ((outMode & OUT_ERROR) == OUT_ERROR) stderrWriter.println(s);
    }
    
    protected void fatal(String s, int exit_code) {
        stderrWriter.println("Fatal error: " + s);
        exit(exit_code);
    }
}
