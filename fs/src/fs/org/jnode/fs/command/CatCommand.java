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

import org.apache.log4j.Logger;
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
public class CatCommand implements Command{
	//private static final Logger log = Logger.getLogger(CatCommand.class);

    static final Argument ARG_FILE = new FileArgument("file",
            "the file (or URL) to print out");

    public static Help.Info HELP_INFO = new Help.Info("cat",
            "Print the contents of the given file (or URL).  " +
            "If the file is omitted, standard input is read until EOF is reached; " +
            "e.g. ^D when reading keyboard input.",
            new Parameter[] { new Parameter(ARG_FILE, Parameter.OPTIONAL)});

    public static void main(String[] args) throws Exception {
    	new CatCommand().execute(new CommandLine(args), System.in, System.out, System.err);
    }
    
    public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) throws Exception {
    	ParsedArguments cmdLine = HELP_INFO.parse(commandLine.toStringArray());
    	String fileName = ARG_FILE.getValue(cmdLine);
    	InputStream is = null;
    	boolean isNewFile = false;
    	try {
    		if (fileName == null) {
    			isNewFile = true;
    			is = in;
    		}
    		else {
    			URL url = openURL(fileName);
    			if(url != null)
    			{
        			try {
        				is = url.openStream();	
        			}
        			catch (IOException ex) {
        				//log.error("can't read "+fileName, ex);
        				err.println("Can't access file from url " + fileName);        				
        			}
    			}
    			else
    			{
    		    	// it's not really an error since we can expect a file
    		    	// instead of an url -> write to out and not to err 
    		    	//out.println("Not an url -> assuming it's a file.");
    				
    				is = openFile(fileName, err);
    			}
    			
    			if (is == null) {
    				// Here, we already have printed an appropriate 
    				// error message. Simply return an errorcode.
    				
    				// FIXME ... System.exit(1);
    				return;
    			}
    		}
    		
    		boolean isEmpty = true;
    		int len;
    		final byte[] buf = new byte[ 1024];
    		while ((len = is.read(buf)) > 0) {
    			isEmpty = false;
    			
    			out.write(buf, 0, len);
    		}
    		
    		if(isEmpty && !isNewFile)
    		{
    			out.println("<empty file>");
    		}
    		
    		out.flush();
    	}
    	finally {
    		if (is != null && !isNewFile) {
    			is.close();
    		}
    	}
    }

    private URL openURL(String fname) {
    	URL url = null;
    	
        try {
            url = new URL(fname);
        } catch (MalformedURLException ex) {
        	//log.error(ex);
        }
        
        return url;
    }

    private InputStream openFile(String fname, PrintStream err) {
    	InputStream is = null;
    	
        try {
        	File file = new File(fname);
        	if(!file.exists())
        	{
        		err.println("File doesn't exist");
        	}
        	else if(file.isDirectory())
        	{
        		err.println("Can't print content of a directory");
        	}
        	else
        	{
        		is = new FileInputStream(file);
        	}
        } catch (FileNotFoundException ex) {
        	// should never happen since we check for existence before
        }
        
        // here, if is is null we should have already printed 
        // an appropriate error message
        return is;
    }

}
