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
 
package org.jnode.fs.jifs.command;

import java.io.InputStream;
import java.io.PrintStream;

import javax.naming.NameNotFoundException;

import org.apache.log4j.Logger;
import org.jnode.naming.InitialNaming;
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginManager;
import org.jnode.shell.Command;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.Syntax;
import org.jnode.shell.help.argument.OptionArgument;

/**
 * Just mounts initial JIFS on /Jifs
 * 
 * @author Andreas H\u00e4nel
 */
public class createJIFS implements Command{

    private static final Logger log = Logger.getLogger(createJIFS.class);
    
    static final OptionArgument ACTION = new OptionArgument("action",
            "Action to perform", new OptionArgument.Option[] {
                    new OptionArgument.Option("start", "start the jifs"),
                    new OptionArgument.Option("stop", "stop the jifs"),
                    new OptionArgument.Option("restart",  "restart the jifs"),
					});
    
    static final Parameter PARAM_ACTION = new Parameter(ACTION, Parameter.MANDATORY);
    
    public static Help.Info HELP_INFO = new Help.Info("jifs",
            new Syntax[] { new Syntax(
                    "JIFS - Jnode Information FileSystem",
                    new Parameter[] { PARAM_ACTION })});

    public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) throws Exception {
		ParsedArguments cmdLine = HELP_INFO.parse(commandLine.toStringArray());
		String Act = ACTION.getValue(cmdLine);
		
		try{
			final PluginManager mgr = InitialNaming.lookup(PluginManager.NAME);
			final Plugin p = mgr.getRegistry().getPluginDescriptor("org.jnode.fs.jifs.def").getPlugin();
			if (new String("start").equals(Act)){
				p.start();
				}
			if (new String("stop").equals(Act)){
				p.stop();
				}
			if (new String("restart").equals(Act)){
				p.stop();
				p.start();
				}
		} catch (NameNotFoundException N){
			System.err.println(N);			
		}

	}
     
	
}
