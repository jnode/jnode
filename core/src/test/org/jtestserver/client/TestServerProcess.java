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
package org.jtestserver.client;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TestServerProcess {
    private static final Logger LOGGER = Logger.getLogger(TestServerProcess.class.getName());
        
    private static final Logger SERVER_LOGGER = Logger.getLogger("Server");
    
    private final TestServerLauncher launcher;
    private PipeInputStream outputPipe;
    private PipeInputStream errorPipe;
    
    private Process process;
    private WatchDog watchDog;
    
    public TestServerProcess(TestServerLauncher launcher) {
        this.launcher = launcher;
        
    }
    
    public void start() throws IOException {
        process = launcher.launch();
        
        outputPipe = new PipeInputStream(process.getInputStream(), SERVER_LOGGER, Level.INFO);
        outputPipe.start();
        
        errorPipe = new PipeInputStream(process.getErrorStream(), SERVER_LOGGER, Level.SEVERE);
        errorPipe.start();
        
        watchDog = new WatchDog();
        watchDog.start();
        
        LOGGER.finer("process = " + process);
    }

    public boolean isAlive() {
        boolean alive = false;
        
        try {
            process.exitValue();
        } catch (IllegalThreadStateException e) {
            alive = true;
        }
        return alive;
    }
    
    private class WatchDog extends Thread {
        public WatchDog() {
            setDaemon(true);
        }
        
        @Override
        public void run() {
            while (true) {
                while (TestServerProcess.this.isAlive()) {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                                        
                }
            }
        }
    }
}
