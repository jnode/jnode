/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 
package org.jnode.fs.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.jnode.shell.Command;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.Argument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.argument.FileArgument;

/**
 * @author epr
 * @author Andreas H\u00e4nel
 * @author Stephen Crawley
 * @author Fabien DUMINY (fduminy@jnode.org)
 */
public class CatCommand implements Command { 

    static final Argument ARG_FILE = new FileArgument("file",
            "the files (or URLs) to be concatenated", true);

    public static Help.Info HELP_INFO = new Help.Info("cat",
            "Concatenate the contents of the files, writing them to standatd output.  " +
            "If there are no arguments, standard input is read until EOF is reached; " +
            "e.g. ^D when reading keyboard input.",
            new Parameter[] { new Parameter(ARG_FILE, Parameter.OPTIONAL)});
    
    private static final int BUFFER_SIZE = 1024;
    

    public static void main(String[] args) throws Exception {
    	new CatCommand().execute(new CommandLine(args), System.in, System.out, System.err);
    }
    
    public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) throws Exception {
    	ParsedArguments cmdLine = HELP_INFO.parse(commandLine.toStringArray());
    	String[] fileNames = ARG_FILE.getValues(cmdLine);
    	boolean ok = true;
    	try {
    		if (fileNames.length == 0) {
    			process(in, out);
    		}
    		else {
    			for (String fileName : fileNames) {
    				InputStream is = null;
    				try {
    					try {
    						// Try to parse the argument as a URL
    						URL url = new URL(fileName);
    						try {
    							// Open stream connection for URL
    							is = url.openStream();	
    						} catch (IOException ex) {
    							err.println("Can't access file from url " + 
    									fileName + ": " + ex.getMessage());        				
    						}
    					} catch (MalformedURLException ex) {
    						// If the argument didn't parse as a URL, treat it as a filename
    						// and open a FileInputStream
	    					is = openFile(fileName, err);
    					}

    					if (is == null) {
    						ok = false;
    					}
    					else {
    						process(is, out);
    					}
    				} finally {
    					if (is != null) {
    						try { 
    							is.close();
    						}
    						catch (IOException ex) {
    							// ignore.
    						}
    					}
    				}
    			}
    		}
        	out.flush();
    	} catch (IOException ex) {
    		// Deal with i/o errors reading from in/is or writing to out.
    		err.println("Problem concatenating file(s): " + ex.getMessage());
    		ok = false;
    	}
    	// TODO need to set a 'return code'; e.g. 
    	// if (!ok) { System.exit(1); }
    }
    
    /**
     * Copy all of stream 'in' to stream 'out'
     * @param in
     * @param out
     * @throws IOException
     */
    private void process(InputStream in, PrintStream out) throws IOException {
    	int len;
		final byte[] buf = new byte[BUFFER_SIZE];
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
    }

    /**
     * Attempt to open a file, writing an error message on failure.
     * @param fname the filename of the file to be opened
     * @param err where we write error messages
     * @return An open stream, or <code>null</code>.
     */
    private InputStream openFile(String fname, PrintStream err) {
    	InputStream is = null;
    	
        try {
        	File file = new File(fname);
        	// FIXME we shouldn't be doing these tests.  Rather, we should be
        	// just trying to create the FileInputStream and printing the 
        	// exception message on failure.  (That assumes that the exception
        	// message is accurate!)
        	if (!file.exists()) {
        		err.println("File doesn't exist");
        	}
        	else if (file.isDirectory()) {
        		err.println("Can't concatenate a directory");
        	}
        	else {
        		is = new FileInputStream(file);
        	}
        } catch (FileNotFoundException ex) {
        	// should never happen since we check for existence before
        }
        
        return is;
    }

}
