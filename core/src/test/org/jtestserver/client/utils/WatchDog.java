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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jtestserver.client.Config;
import org.jtestserver.client.process.ServerProcess;

/**
 * That abstract utility class is used to watch a {@link ServerProcess} and 
 * check regularly that it is alive. Users have to implement the abstract 
 * method {@link #processDead()}, which is called when the WatchDog has detected
 *  that the process is not alive.
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public abstract class WatchDog extends Thread {
    private static final Logger LOGGER = Logger.getLogger(WatchDog.class.getName());
        
    /**
     * The {@link ServerProcess} to watch.
     */
    private final ServerProcess process;
    
    /**
     * Configuration of the WatchDog.
     */
    private final Config config;
    
    /**
     * Is the WatchDog actually watching the {@link ServerProcess} ? 
     */
    private boolean watch = false;

    /**
     * Create a WatchDog for the given {@link ServerProcess}, 
     * with the provided configuration.
     * @param process to watch.
     * @param config
     */
    public WatchDog(ServerProcess process, Config config) {
        this.process = process;
        this.config = config;
        setDaemon(true);
        start();
    }

    /**
     * Start watching the {@link ServerProcess}
     */
    public void startWatching() {
        watch = true;
    }

    /**
     * Stop watching the {@link ServerProcess}
     */
    public void stopWatching() {
        watch = false;
    }
    
    /**
     * Manage the activity of the WatchDog and notify when the process is dead.
     */
    @Override
    public void run() {
        while (true) {
            if (watch) {
                while (processIsAlive()) {
                    goSleep();
                }
                
                processDead();
            } else {
                goSleep();
            }
        }
    }
    
    /**
     * Callback method used to notify that the watched process is dead.
     */
    protected abstract void processDead();
    
    /**
     * Sleep for the amount of time specified in the configuration.
     */
    private void goSleep() {
        try {
            Thread.sleep(config.getWatchDogPollInterval());
        } catch (InterruptedException e) {
            // ignore
        }
    }
    
    private boolean processIsAlive() {
        try {
            return process.isAlive();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "error while checking if alive", e);
            return true;
        }
    }
}
