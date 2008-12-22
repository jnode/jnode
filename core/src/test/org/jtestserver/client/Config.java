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

import java.io.File;
import java.util.Properties;

import org.jtestserver.client.process.VMConfig;
import org.jtestserver.common.ConfigUtils;

/**
 * Class containing various parameters used to configure JTestServer.
 *  
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class Config {

    /**
     * Client side timeout for connection with the server.
     */
    private final int clientTimeout;
    
    /**
     * Name of the server. <br>Examples : server.mydomain.org, 123.456.789.1
     */
    private final String serverName;
    
    /**
     * Port of the server.
     */
    private final int serverPort;
    
    /**
     * Work directory.
     */
    private final File workDir;
    
    /**
     * Excluding filters, used to remove some tests from a list.
     */
    private final String[] excludingFilters;
    
    /**
     * Should we force reading the mauve list on startup instead of getting it 
     * from previous run (that save it in the work directory) ?
     */
    private final boolean forceUseMauveList;
    
    /**
     * Time between 2 checks of the WatchDog.
     */
    private final int watchDogPollInterval;
    
    /**
     * Configuration of the VM in which tests will actually be run.
     */
    private final VMConfig vmConfig;
    
    /**
     * 
     * @param properties configuration parameters of JTestServer
     * @param vmConfig configuration of the VM in which tests will actually be run.
     */
    Config(Properties properties, VMConfig vmConfig) {
        clientTimeout = ConfigUtils.getInt(properties, "client.timeout", 30000);
        serverName = properties.getProperty("server.name", "localhost");
        serverPort = ConfigUtils.getInt(properties, "client.timeout", 10000);
        workDir = ConfigUtils.getDirectory(properties, "work.dir", new File("."));
        excludingFilters = ConfigUtils.getStringArray(properties, "excluding.filters");
        forceUseMauveList = ConfigUtils.getBoolean(properties, "force.use.mauve.list", false);
        watchDogPollInterval = ConfigUtils.getInt(properties, "watchdog.poll.interval", 10000);
        this.vmConfig = vmConfig;
    }

    public int getClientTimeout() {
        return clientTimeout;
    }

    public String getServerName() {
        return serverName;
    }

    public int getServerPort() {
        return serverPort;
    }
    
    public File getWorkDir() {
        return workDir;
    }
    
    public boolean isForceUseMauveList() {
        return forceUseMauveList;
    }

    public String[] getExcludingFilters() {
        return excludingFilters;
    }

    public int getWatchDogPollInterval() {
        return watchDogPollInterval;
    }

    public VMConfig getVMConfig() {
        return vmConfig;
    }
}
