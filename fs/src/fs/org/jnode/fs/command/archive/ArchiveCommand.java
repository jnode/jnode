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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.IOException;

public class ArchiveCommand extends AbstractCommand {
    
    protected static final int OUT_ERROR = 0x01;
    protected static final int OUT_WARN  = 0x02;
    protected static final int OUT_NOTICE = 0x04;
    protected static final int OUT_DEBUG = 0x80;
    
    protected int outMode = OUT_ERROR | OUT_WARN;
    
    protected PrintWriter stdoutWriter;
    protected PrintWriter stderrWriter;
    protected Reader stdinReader;
    
    private byte[] buffer;
    
    protected ArchiveCommand(String s) {
        super(s);
    }
    
    protected void setup() {
        stdoutWriter = getOutput().getPrintWriter();
        stderrWriter = getError().getPrintWriter();
        stdinReader  = getInput().getReader();
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
            if (file.exists()) {
                if (delete) {
                    if (forced) {
                        file.delete();
                    } else {
                        if (prompt_yn(file + "exists. Overwrite? ")) {
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
                error("Could not create file: " + file);
                return null;
            }
            return new FileOutputStream(file);
        } catch (IOException ioe) {
            error("Could not open stream: " + file + " : " + ioe.getLocalizedMessage());
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
            return new FileInputStream(file);
        } catch (IOException ioe) {
            error("Cannot open stream: " + file + " : " + ioe.getLocalizedMessage());
            return null;
        }
    }
    
    /**
     * Prompt the user with a question asking for a yes or no answer.
     *
     * @param String the question to ask the user
     * @return true if the user said yes, false if the user said no
     */
    protected boolean prompt_yn(String s) {
        int choice;
        for (;;) {
            stdoutWriter.print(s + " [y/n]");
            try {
                choice = stdinReader.read();
            } catch (IOException _) {
                choice = 0;
            }
            stdoutWriter.println();
            if (choice == 'y' || choice == 'n') break;
        }
        
        return choice == 'y';
    }
    
    protected void out(String s) {
        stdoutWriter.println(s);
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
}
