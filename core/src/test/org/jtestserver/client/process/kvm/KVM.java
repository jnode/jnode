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
package org.jtestserver.client.process.kvm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.jtestserver.client.process.VmManager;
import org.jtestserver.client.utils.PipeInputStream;
import org.jtestserver.client.utils.ProcessRunner;

public class KVM implements VmManager {
    private static final String OPTION_CDROM = "-cdrom";
    
    private final ProcessRunner runner = new ProcessRunner();
    private final KVMConfig config;
    
    public KVM(KVMConfig config) {
        this.config = config;
    }
    
    @Override
    public List<String> getRunningVMs() throws IOException {
        return getRunningVMs(false);                
    }

    @Override
    public boolean start(String vm) throws IOException {
        CommandLineBuilder cmdLine = new CommandLineBuilder("kvm"); 
        cmdLine.append("-m").append(config.getMemory());
        cmdLine.append(OPTION_CDROM).append(config.getCdrom().getAbsolutePath());
        cmdLine.append(config.getOptions()); 
        cmdLine.append("-serial").append(config.getSerial());
        cmdLine.append("-k").append(config.getKeyboard());
        
        return execute(cmdLine);
    }

    @Override
    public boolean stop(String vm) throws IOException {
        boolean success = true;
        for (String pid : getRunningVMs(true)) {
            CommandLineBuilder cmdLine = new CommandLineBuilder("kill"); 
            cmdLine.append("-9").append(pid);
            
            success &= execute(cmdLine);
        }
        
        return success;
    }

    private List<String> getRunningVMs(final boolean pid) throws IOException {
        final List<String> runningVMs = new ArrayList<String>();
        boolean success  = runner.executeAndWait(new PipeInputStream.Listener() {
            private boolean firstLine = true;
            
            @Override
            public void lineReceived(String line) {
                if (!firstLine) {
                    if (pid) {
                        runningVMs.add(line);
                    } else {
                        int idx = line.indexOf(OPTION_CDROM);
                        if (idx >= 0) {
                            idx += OPTION_CDROM.length() + 1;
                            int idx2 = line.indexOf(' ', idx);
                            if (idx2 >= 0) {
                                runningVMs.add(line.substring(idx, idx2));
                            }
                        }
                    }
                }
                
                firstLine = false;
            }            
        }, new int[]{0, 1}, "ps", "-o", (pid ? "pid" : "args"), "-C", "kvm"); 
        // "lsmod|grep kvm" can also be used but it give less details (no PID for example)
        
        if (!success) {
            throw new IOException("failed to get running VMs");
        }
        
        return runningVMs;                
    }
    
    private boolean execute(CommandLineBuilder cmdLine) throws IOException {
        final List<Boolean> errors = new Vector<Boolean>();
        runner.execute(null, new PipeInputStream.Listener() {

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
