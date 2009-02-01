/*
 * $Id$
 *
 * JNode.org
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
 
package org.jnode.shell;

import java.io.InputStream;
import java.io.PrintStream;

import org.jnode.shell.io.CommandIO;
import org.jnode.shell.syntax.ArgumentBundle;

/**
 * This interface is implemented by all JNode native 'command' classes.  It
 * provides the 'execute' method(s) used to invoke the command and other
 * methods used to set the arguments and streams.
 * <p>
 * The normal implementation pattern for a JNode native command is to 'extend'
 * the AbstractCommand class.  This class provides implementations of the
 * getters defined by this API along with a default implementations of the
 * execute methods, and some special infrastructure to support the 'classic'
 * Java command entry point. 
 * 
 * @author Martin Husted Hartvig (hagar@jnode.org)
 * @author crawley@jnode.org
 */
public interface Command {
    public int STD_IN = 0;
    public int STD_OUT = 1;
    public int STD_ERR = 2;
    public int SHELL_ERR = 3;
    
    
    /**
     * This is the old native command entry point method.  The method is
     * (was) called to execute the command, passing the command line arguments
     * and byte streams for the command's standard input, output and errors.
     * 
     * @param commandLine what comes in from the user
     * @param in input stream, most often this is System.in, but it can be a
     *        file or piped.
     * @param out output stream, mostly this is System.out, but it can be a file
     *        or piped.
     * @param err err stream, mostly this is System.err, but it can be a file or
     *        piped.
     * @throws Exception command execution may throw any Exception.  It is considered
     * to be <b>good style</b> to allow unexpected exceptions to propagate to the 
     * JNode shell where they can be handled uniformly based on the user's preferences.
     */
    public void execute(CommandLine commandLine, InputStream in,
            PrintStream out, PrintStream err) throws Exception;
    
    /**
     * This is the new native command entry point method.  The command line
     * and streams are typically accessed via getters defined by the 
     * AbstractCommand class.
     * 
     * @throws Exception command execution may throw any Exception.  It is considered
     * to be <b>good style</b> to allow unexpected exceptions to propagate to the 
     * JNode shell where they can be handled uniformly based on the user's preferences.
     */
    public void execute() throws Exception;

    
    /**
     * This method is used by the shell to attach command line
     * arguments and streams prior to command execution.
     * 
     * @param commandLine the command line arguments.
     * @param ios the command's stream vector,
     */
    public void initialize(CommandLine commandLine, CommandIO[] ios);

    /**
     * This method fetches the ArgumentBundle that holds the bindings
     * between Arguments and values from the command line.  The bundle
     * is created when a Command class calls 'register' to register
     * its Arguments.  If this has not happened, the bundle will be 
     * <code>null</code>.
     * 
     * @return the argument bundle or <code>null</code>
     */
    public ArgumentBundle getArgumentBundle();
}
