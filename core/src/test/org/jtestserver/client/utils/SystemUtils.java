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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jtestserver.client.process.kvm.CommandLineBuilder;

/**
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public abstract class SystemUtils {
    private static final SystemUtils INSTANCE;
    
    static {
        INSTANCE = new UnixSystemUtils();
    }
    
    public static SystemUtils getInstance() {
        return INSTANCE;
    }
    
    public abstract List<ProcessStatus> getProcessStatus(String command) throws IOException;
    public abstract boolean killProcess(String pid) throws IOException;
    
    public static class ProcessStatus {
        private final String identifier;
        private final String arguments;
        
        public ProcessStatus(String identifier, String arguments) {
            super();
            this.identifier = identifier;
            this.arguments = arguments;
        }

        public String getIdentifier() {
            return identifier;
        }

        public String getArguments() {
            return arguments;
        }
        
        @Override
        public String toString() {
            return identifier + '\t' + arguments;
        }
    }
    
    private static class UnixSystemUtils extends SystemUtils {
        private final ProcessRunner runner = new ProcessRunner();
        
        @Override
        public List<ProcessStatus> getProcessStatus(String command) throws IOException {
            final List<ProcessStatus> runningVMs = new ArrayList<ProcessStatus>();
            boolean success  = runner.executeAndWait(new PipeInputStream.Listener() {
                private boolean firstLine = true;
                
                @Override
                public void lineReceived(String line) {
                    if (!firstLine) {
                        int index = line.indexOf(' ');
                        runningVMs.add(new ProcessStatus(line.substring(0, index), line.substring(index + 1)));
                    }
                    
                    firstLine = false;
                }
            }, new int[]{0, 1}, "ps", "-o", "pid,args", "-C", command); 
            
            if (!success) {
                throw new IOException("failed to get process status");
            }
            
            return runningVMs;
        }
        
        /**
         * @param pid
         * @return
         * @throws IOException 
         */
        @Override
        public boolean killProcess(String pid) throws IOException {
            CommandLineBuilder cmdLine = new CommandLineBuilder("kill"); 
            cmdLine.append("-9").append(pid);
            
            return runner.execute(cmdLine);
        }
    }
    
    private SystemUtils() {        
    }
}
