/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.command.archive;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.IOException;
import org.apache.tools.zip.ZipFile;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FlagArgument;

/**
 * This is a base class that holds some convenience methods for implementing archive commands.
 *
 * @author chris boertien
 */
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
    protected int rc = 1;
    protected boolean use_stdout;
    protected boolean force;
    protected boolean compress;
    
    private byte[] buffer;
    
    protected ArchiveCommand(String s) {
        super(s);
    }
    
    public void execute(String command) {
        stdoutWriter = getOutput().getPrintWriter();
        stderrWriter = getError().getPrintWriter();
        stdinReader  = getInput().getReader();
        stdin = getInput().getInputStream();
        stdout = getOutput().getOutputStream();
        
        // FIXME get rid of this {
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
        // }
    }
    
    protected void createStreamBuffer(int size) {
        buffer = new byte[size];
    }
    
    /**
     * Pipes the contents of the InputStream into the OutputStream.
     *
     * This is most usefull for applying a stream filter that reads data from a source
     * and pipes the contents to an output stream.
     *
     * @param in stream to read from
     * @param out stream to write to
     */
    protected void processStream(InputStream in, OutputStream out) throws IOException {
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
     * @param file the file to open the stream on
     * @param delete if the file exists, delete it first
     * @param forced if true, this forces the deletion without prompting the user
     * @return an OutputStream on the file, or null if there was a problem. null could also be
     *         returned if the delete option was chosen and the user said no to overwriting.
     */
    protected OutputStream openFileWrite(File file, boolean delete, boolean forced) {
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
                        if (askUser(file + prompt_overwrite, true)) {
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
     * @param file the file to open the stream on
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
     * Convenience method for closing streams and writers.
     */
    protected void close(Closeable obj) {
        if (obj != null) {
            try {
                obj.close();
            } catch (IOException ex) {
                //ignore
            }
        }
    }
    
    /**
     * Convenience method for closing org.apache.tools.zip.ZipFile
     */
    protected void close(ZipFile zfile) {
        if (zfile != null) {
            try {
                zfile.close();
            } catch (IOException ex) {
                // ignore
            }
        }
    }
    
    /**
     * Prompt the user with a question asking for a yes or no answer.
     *
     * FIXME This is unsafe as it will trigger an endless loop if stdin
     *       is not the terminal.
     *
     * @param s the question to ask the user
     * @param defaultY if {#code true}, the default answer is yes, otherwise no.
     * @return true if the user said yes, false if the user said no
     */
    protected boolean askUser(String s, boolean defaultY) {
        int choice;
        // put a cap on the looping to prevent non-terminal stdin 
        // from an infinite loop
        for (int i = 0; i < 10; i++) {
            stdoutWriter.print(s);
            try {
                choice = stdinReader.read();
            } catch (IOException ex) {
                throw new RuntimeException("Problem with stdin");
            }
            stdoutWriter.println();
            if (choice == 'y') return true;
            if (choice == 'n') return false;
            if (choice == '\n') return defaultY;
        }
        
        return false;
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
    
    protected void fatal(String s, int exitCode) {
        stderrWriter.println("Fatal error: " + s);
        exit(exitCode);
    }
}
