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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jtestserver.client.utils.PipeInputStream.Listener;


public class ProcessRunner {
    private static final Logger LOGGER = Logger.getLogger(ProcessRunner.class.getName());
    private static final Logger SERVER_LOGGER = Logger.getLogger("Server");
    
    private static final File DEFAULT_WORK_DIR = new File(System.getProperty("user.home"));
        
    private PipeInputStream outputPipe;
    private PipeInputStream errorPipe;
    private Process process;
    private File workDir = DEFAULT_WORK_DIR;

    public boolean executeAndWait(String... command) throws IOException {
        return executeAndWait(null, command);
    }

    public boolean executeAndWait(Listener listener, String... command) throws IOException {
        return executeAndWait(listener, new int[]{0}, command);
    }
    
    public boolean executeAndWait(Listener listener, int[] successExitValue, String... command) throws IOException {
        boolean success = false;
        
        execute(listener, listener, command);
        try {
            int exitValue  = process.waitFor();
            for (int ec : successExitValue) {
                if (exitValue == ec) {
                    success = true;
                    break;
                }
            }
            
            LOGGER.info("exit value : " + exitValue);
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "error while waiting for process", e);
        }
        
        return success;
    }
    
    public void execute(String... command) throws IOException {
        execute(null, null, command);
    }

    public void execute(Listener outputListener,
            Listener errorListener, String... command) throws IOException {
        LOGGER.finer("command: " + command);
        
        Map<String, String> env = System.getenv();
        String[] envArray = new String[env.size()];
        int i = 0;
        for (String key : env.keySet()) {
            envArray[i++] = key + "=" + env.get(key);
        }
        process = Runtime.getRuntime().exec(command, envArray, workDir);

        outputPipe = new PipeInputStream(process.getInputStream(), SERVER_LOGGER, Level.INFO,
                outputListener);
        outputPipe.start();

        errorPipe = new PipeInputStream(process.getErrorStream(), SERVER_LOGGER, Level.SEVERE,
                errorListener);
        errorPipe.start();
    }
    
    public Process getProcess() {
        return process;
    }

    public void setWorkDir(File workDir) {
        this.workDir = ((workDir == null) || !workDir.isDirectory()) ? DEFAULT_WORK_DIR : workDir;
    }
}
