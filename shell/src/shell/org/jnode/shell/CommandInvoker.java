/*
 * $Id$
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
 
package org.jnode.shell;

import java.util.Map;
import java.util.Properties;

/**
 * This 'advanced' invoker API adds methods for invoking commands with
 * different system properties or environment variables to the parent.
 *
 * @author crawley@jnode.org
 */
public interface CommandInvoker extends SimpleCommandInvoker {

    /**
     * Run a command synchronously, passing back the resulting return code.
     * 
     * @param commandLine this provides the command name (alias), the command
     *        arguments and (where relevant) the command's i/o stream context.
     * @param sysProps a Properties object containing the command's system
     *        properties.  If this parameter is {@code null}, a copy of the 
     *        system properties for the calling context should be used.
     * @param env a Map object containing the command's environment variables.  
     *        If this parameter is {@code null}, the environment variables for 
     *        the calling context should be used.
     * @return an integer return code, with zero indicating command success,
     *         non-zero indicating command failure.
     * @throws ShellException if there was some problem launching the command.
     */
    int invoke(CommandLine commandLine, Properties sysProps, Map<String, String> env) 
        throws ShellException;

    /**
     * Create a thread for running a command asynchronously. This can be used
     * for running and for assembling command pipelines.
     * 
     * @param commandLine this provides the command name (alias), the command
     *        arguments and (where relevant) the command's i/o stream context.
     * @param sysProps a Properties object containing the command's system
     *        properties.  If this parameter is {@code null}, a copy of the 
     *        system properties for the calling context should be used.
     * @param env a Map object containing the command's environment variables.  
     *        If this parameter is {@code null}, the environment variables for 
     *        the calling context should be used.
     * @return the thread for the command. Calling
     *         {@link java.lang.Thread#start()} will cause the command to
     *         execute.
     * @throws ShellException if there was some problem launching the command.
     */
    CommandThread invokeAsynchronous(CommandLine commandLine, 
            Properties sysProps, Map<String, String> env) 
        throws ShellException;
    
}
