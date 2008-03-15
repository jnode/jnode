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
 
package org.jnode.apps.httpd;

import fi.iki.elonen.NanoHTTPD;
import java.io.*;
import java.util.Properties;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.Command;
import org.jnode.shell.CommandLine;

/**
 * @author Martin Husted Hartvig (hagar@jnode.org)
 */

public class NanoHTTPDCommand extends AbstractCommand
{
  public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) throws Exception
  {
    File file = new File("/jnode/index.htm");  // ram disk is fat, so no long extension, I guess

    if (!file.exists())
    {
      PrintWriter printWriter = new PrintWriter(new FileOutputStream(file));
      printWriter.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n<html><body>JNode - Java New Operating System Design Effort</body></html>\n");
      printWriter.close();
    }

    NanoHTTPD nanoHTTPD = new NanoHTTPD(80)
    {
      public Response serve(String uri, String method, Properties header, Properties parms)
      {
        return serveFile(uri, header, new File("/jnode"), true);
      }

    };

    while (true)
    {
      try
      {
        Thread.sleep(250);
      }
      catch (InterruptedException e)
      {
        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
      }
    }

  }


  public static void main(String[] args)
  {
    NanoHTTPDCommand nanoHTTPDCommand = new NanoHTTPDCommand();

    try
    {
      nanoHTTPDCommand.execute(null, System.in, System.out, System.err);
    }
    catch (Exception e)
    {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
  }
}
