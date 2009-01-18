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
package org.jtestserver.client.process.jvm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jtestserver.client.process.VmManager;
import org.jtestserver.client.process.kvm.CommandLineBuilder;
import org.jtestserver.client.utils.ProcessRunner;
import org.jtestserver.client.utils.SystemUtils;
import org.jtestserver.client.utils.SystemUtils.ProcessStatus;

/**
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class JVM implements VmManager {
    private static final String JAVA_COMMAND = "java";
    
    private final ProcessRunner runner = new ProcessRunner();
    
    private final JVMConfig config;
    
    private final String startCommandLine;
    
    public JVM(JVMConfig config) {
        this.config = config;
        this.startCommandLine = createStartCommandLine().toString();
        this.config.setVmName(startCommandLine);
    }
    
    /* (non-Javadoc)
     * @see org.jtestserver.client.process.VmManager#getRunningVMs()
     */
    @Override
    public List<String> getRunningVMs() throws IOException {
        List<ProcessStatus> processes = SystemUtils.getInstance().getProcessStatus(JAVA_COMMAND);
        List<String> runningVMs = new ArrayList<String>(processes.size());
        for (ProcessStatus ps : processes) {
            if (ps.getArguments().equals(startCommandLine)) {
                runningVMs.add(ps.getArguments());
            }
        }
        return runningVMs;
    }

    /* (non-Javadoc)
     * @see org.jtestserver.client.process.VmManager#start(java.lang.String)
     */
    @Override
    public boolean start(String vm) throws IOException {
        
        //runner.setWorkDir(new File(classesDir));
        return runner.execute(createStartCommandLine());
    }

    /* (non-Javadoc)
     * @see org.jtestserver.client.process.VmManager#stop(java.lang.String)
     */
    @Override
    public boolean stop(String vm) throws IOException {
        boolean success = true;
        for (ProcessStatus ps : SystemUtils.getInstance().getProcessStatus(JAVA_COMMAND)) {
            if (ps.getArguments().equals(vm)) {
                success &= SystemUtils.getInstance().killProcess(ps.getIdentifier());
            }
        }
        
        return success;
    }
    
    private CommandLineBuilder createStartCommandLine() {
        String java = new File(config.getJavaHome(), "bin/" + JAVA_COMMAND).getAbsolutePath();
        CommandLineBuilder cmdLine = new CommandLineBuilder(java);
        
        if (config.getBootClasspath() != null) {
            cmdLine.append("-Xbootclasspath").append(config.getBootClasspath());
        }
        
        cmdLine.append("-cp").append(config.getClasspath());
        cmdLine.append(config.getMainClass());

        return cmdLine;
    }
}
