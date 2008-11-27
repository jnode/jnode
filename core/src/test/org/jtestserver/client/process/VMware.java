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
package org.jtestserver.client.process;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jtestserver.client.utils.PipeInputStream;
import org.jtestserver.client.utils.ProcessRunner;
import org.jtestserver.client.utils.PipeInputStream.Listener;

public class VMware {
    private static final Logger LOGGER = Logger.getLogger(VMware.class.getName());

    private ProcessRunner runner = new ProcessRunner();
    
    private final String[] baseCommand;
    private final File workDirectory = new File(".");
    
    public VMware(String userName, String password) {
        baseCommand = new String[]{"vmrun", "-T", "server", "-h", "http://localhost:8222/sdk", "-u", 
            userName, "-p", password};
    }
    
    public boolean start(String vm) throws IOException {
        return executeCommand("start", vm);
    }
    
    public boolean stop(String vm) throws IOException {        
        return executeCommand("stop", vm);
    }

    public List<String> getRunningVMs() throws IOException {
        final List<String> runningVMs = new ArrayList<String>();
        boolean success  = executeCommand(new PipeInputStream.Listener() {

            @Override
            public void lineReceived(String line) {
                runningVMs.add(line);
            }
            
        }, "list");
        
        if (!success) {
            throw new IOException("failed to get running VMs");
        }
        
        return runningVMs;        
    }

    private boolean executeCommand(String... command) throws IOException {
        return executeCommand(null, command);
    }
    
    private boolean executeCommand(Listener listener, String... command) throws IOException {
        boolean success = false;
        
        String[] fullCommand = new String[baseCommand.length + command.length];
        System.arraycopy(baseCommand, 0, fullCommand, 0, baseCommand.length);
        System.arraycopy(command, 0, fullCommand, baseCommand.length, command.length);
        
        runner.execute(fullCommand, workDirectory, listener, listener);
        try {
            int exitValue  = runner.getProcess().waitFor();
            success = (exitValue == 0);
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "error while waiting for process", e);
        }
        
        return success;
    }
}
