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
 * You should have received a copy of the GNU General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.fs.command;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;

import org.jnode.shell.Command;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.Help;

/**
 * @author Martin Husted Hartvig (hagar@jnode.org)
 */
public class PwdCommand implements Command
{
  public static Help.Info HELP_INFO = new Help.Info("pwd", "show the name of current working directory");

  public static void main(String[] args) throws Exception
  {
    new PwdCommand().execute(new CommandLine(args), System.in, System.out, System.err);
  }

  public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) throws Exception
  {
    File file = new File("");
    out.println(file.getAbsolutePath());
  }
}
