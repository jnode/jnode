/*

JTestServer is a client/server framework for testing any JVM implementation.
 
Copyright (C) 2008  Fabien DUMINY (fduminy@jnode.org)

JTestServer is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

JTestServer is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package org.jtestserver.client.utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jtestserver.client.utils.PipeInputStream.Listener;


public class ProcessRunner {
    private static final Logger LOGGER = Logger.getLogger(ProcessRunner.class.getName());
    private static final Logger SERVER_LOGGER = Logger.getLogger("Server");
        
    private PipeInputStream outputPipe;
    private PipeInputStream errorPipe;
    private Process process;

    public void execute(String command, File workDir) throws IOException {
        execute(command, workDir, null, null);
    }

    public void execute(String command, File workDir, Listener outputListener,
            Listener errorListener) throws IOException {
        LOGGER.finer("command: " + command);
        process = Runtime.getRuntime().exec(command, new String[0], workDir);

        outputPipe = new PipeInputStream(process.getInputStream(), SERVER_LOGGER, Level.INFO,
                outputListener);
        outputPipe.start();

        errorPipe = new PipeInputStream(process.getErrorStream(), SERVER_LOGGER, Level.SEVERE,
                errorListener);
        errorPipe.start();
    }
    
    public void execute(String[] command, File workDir) throws IOException {
        execute(command, workDir, null, null);
    }

    public void execute(String[] command, File workDir, Listener outputListener,
            Listener errorListener) throws IOException {
        LOGGER.finer("command: " + command);
        process = Runtime.getRuntime().exec(command, new String[0], workDir);

        outputPipe =
                new PipeInputStream(process.getInputStream(), SERVER_LOGGER, Level.INFO,
                        outputListener);
        outputPipe.start();

        errorPipe =
                new PipeInputStream(process.getErrorStream(), SERVER_LOGGER, Level.SEVERE,
                        errorListener);
        errorPipe.start();
    }
    
    public Process getProcess() {
        return process;
    }
}
