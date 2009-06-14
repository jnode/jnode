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
import org.jtestserver.client.utils.CommandLineBuilder;
import org.jtestserver.client.utils.ConfigurationUtils;
import org.jtestserver.client.utils.ProcessRunner;
import org.jtestserver.client.utils.SystemUtils;
import org.jtestserver.client.utils.SystemUtils.ProcessStatus;

/**
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class JVM implements VmManager<JVMConfig> {
    private static final String JAVA_COMMAND = "java";
    
    private final ProcessRunner runner = new ProcessRunner();
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getRunningVMs(JVMConfig config) throws IOException {
        String startCommandLine = createStartCommandLine(config).toString();        
        List<ProcessStatus> processes = SystemUtils.getInstance().getProcessStatus(JAVA_COMMAND);
        List<String> runningVMs = new ArrayList<String>(processes.size());
        for (ProcessStatus ps : processes) {
            if (ps.getArguments().equals(startCommandLine)) {
                runningVMs.add(ps.getArguments());
            }
        }
        return runningVMs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean start(JVMConfig config) throws IOException {        
        //runner.setWorkDir(new File(classesDir));
        
        CommandLineBuilder cmdLine = createStartCommandLine(config);
        config.setVmName(cmdLine.toString());
        return runner.execute(cmdLine);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean stop(JVMConfig config) throws IOException {
        boolean success = true;
        for (ProcessStatus ps : SystemUtils.getInstance().getProcessStatus(JAVA_COMMAND)) {
            if (ps.getArguments().equals(createStartCommandLine(config).toString())) {
                success &= SystemUtils.getInstance().killProcess(ps.getIdentifier());
            }
        }
        
        return success;
    }
    
    private CommandLineBuilder createStartCommandLine(JVMConfig config) {
        String java = new File(config.getJavaHome(), "bin/" + JAVA_COMMAND).getAbsolutePath();
        CommandLineBuilder cmdLine = new CommandLineBuilder(java);
        
        if (config.getBootClasspath() != null) {
            cmdLine.append("-Xbootclasspath").append(config.getBootClasspath());
        }

        if (config.getOptions() != null) {
            cmdLine.append(config.getOptions());
        }
        
        cmdLine.append("-D" + ConfigurationUtils.LOGGING_CONFIG_FILE + '=' 
                + ConfigurationUtils.getLoggingConfigFile());

        cmdLine.append("-cp").append(config.getClasspath());
        cmdLine.append(config.getMainClass());

        return cmdLine;
    }
}
