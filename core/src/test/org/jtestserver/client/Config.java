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
import java.io.IOException;
import java.util.Properties;

import org.jtestserver.common.ConfigUtils;

public class Config {

    public static Config read() throws IOException {
        Properties properties = new Properties();
        properties.load(Config.class.getResourceAsStream("config.properties"));
        return new Config(properties);
    }
    
    private final int clientTimeout;
    private final String serverName;
    private final int serverPort;
    private final File workDir;
    private final String[] excludingFilters;
    private final boolean forceUseMauveList;
    private final File vmwareVmxFile;
    private final int watchDogPollInterval;
    
    private final String userName;
    private final String password;
    private final String vmName;
    
    private Config(Properties properties) {
        clientTimeout = ConfigUtils.getInt(properties, "client.timeout", 30000);
        serverName = properties.getProperty("server.name", "localhost");
        serverPort = ConfigUtils.getInt(properties, "client.timeout", 10000);
        workDir = ConfigUtils.getDirectory(properties, "work.dir", new File("."));
        excludingFilters = ConfigUtils.getStringArray(properties, "excluding.filters");
        forceUseMauveList = ConfigUtils.getBoolean(properties, "force.use.mauve.list", false);
        watchDogPollInterval = ConfigUtils.getInt(properties, "watchdog.poll.interval", 10000);
        userName = ConfigUtils.getString(properties, "vmware.server.username");
        password = ConfigUtils.getString(properties, "vmware.server.password");
        vmName = ConfigUtils.getString(properties, "vmware.server.vmName");
        
        //FIXME
        //this.vmwareVmxFile = ConfigUtils.getFile(properties, "vmware.vmx.file", true);
        this.vmwareVmxFile = null;
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
    
    public File getVmwareVmxFile() {
        return vmwareVmxFile;
    }
    
    public boolean isForceUseMauveList() {
        return forceUseMauveList;
    }

    public String[] getExcludingFilters() {
        return excludingFilters;
    }

    public String getVMwareServerUser() {
        return userName;
    }

    public String getVMwareServerPassword() {
        return password;
    }

    public int getWatchDogPollInterval() {
        return watchDogPollInterval;
    }

    public String getVmName() {
        return vmName;
    }
}
