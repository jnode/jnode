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
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jtestserver.client.process.kvm.CommandLineBuilder;
import org.jtestserver.client.utils.PipeInputStream.Listener;

/**
 * Utility class used to provide a higher level API for running commands 
 * on the command line by wrapping the {@link Runtime#exec(String)} method 
 * and its variants.The main added features are the optional ability to wait  
 * for process termination and to redirect output and error streams. 
 * 
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class ProcessRunner {
    /**
     * Logger used for our internal usage.
     */
    private static final Logger LOGGER = Logger.getLogger(ProcessRunner.class.getName());
    
    /**
     * Logger used by our {@link PipeInputStream}s.
     */
    private static final Logger SERVER_LOGGER = Logger.getLogger("Server");
    
    /**
     * Default work directory for the processes we are launching.
     */
    private static final File DEFAULT_WORK_DIR = new File(System.getProperty("user.home"));
    
    /**
     * {@link PipeInputStream} used to redirect the process output stream.
     */
    private PipeInputStream outputPipe;
    
    /**
     * {@link PipeInputStream} used to redirect the process error stream.
     */
    private PipeInputStream errorPipe;
    
    /**
     * The process we have launched.
     */
    private Process process;
    
    /**
     * The actual work directory 
     */
    private File workDir = DEFAULT_WORK_DIR;

    /**
     * Executes a command and waits for its termination.
     * 
     * @param command to execute.
     * @return true on successful termination of the command
     * @throws IOException
     */
    public boolean executeAndWait(String... command) throws IOException {
        return executeAndWait(null, command);
    }

    /**
     * Executes a command and waits for its termination. 
     * Also listen for lines received from the process streams. 
     * 
     * @param listener optional listener for doing additional processing on the lines received 
     * from the process input & error streams.
     * @param command to execute.
     * @return true on successful termination of the command
     * @throws IOException
     */
    public boolean executeAndWait(Listener listener, String... command) throws IOException {
        return executeAndWait(listener, new int[]{0}, command);
    }
    
    /**
     * Executes a command and waits for its termination. 
     * Also listen for lines received from the process streams and give the return codes that    
     * means successful completion of the command.
     * 
     * @param listener optional listener for doing additional processing on the lines received 
     * from the process input & error streams.
     * @param successExitValue list of codes by the command in case of success.
     * @param command to execute.
     * @return true on successful termination of the command
     * @throws IOException
     */
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
        
        outputPipe.waitFor();
        errorPipe.waitFor();
        
        return success;
    }

    /**
     * Executes a command without waiting for its termination. 
     * @param command to execute
     * @throws IOException
     */
    public void execute(String... command) throws IOException {
        execute(null, null, command);
    }

    /**
     * Executes a command without waiting for its termination.
     * @param outputListener listener for additional processing on the process output stream 
     * @param errorListener listener for additional processing on the process error stream
     * @param command to execute
     * @throws IOException
     */
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

    /**
     * Get the launched process
     * @return
     */
    public Process getProcess() {
        return process;
    }

    /**
     * Defines the work directory. It must be called before any of the execute/executeAndWait methods.
     * @param workDir the work directory
     */
    public void setWorkDir(File workDir) {
        this.workDir = ((workDir == null) || !workDir.isDirectory()) ? DEFAULT_WORK_DIR : workDir;
    }

    
    /**
     * Execute the given command line.
     * 
     * @param cmdLine command line to execute
     * @return true if the execution succeed
     * @throws IOException
     */
    public boolean execute(CommandLineBuilder cmdLine) throws IOException {
        final List<Boolean> errors = new Vector<Boolean>();
        execute(null, new PipeInputStream.Listener() {

            @Override
            public void lineReceived(String line) {
                errors.add(Boolean.TRUE);
            }            
        }, cmdLine.toArray());

        // wait a bit to see if an error happen
        // but don't wait the end of the KVM process 
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // ignore
        }
        
        return errors.isEmpty();
    }
}
