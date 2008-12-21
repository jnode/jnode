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
package org.jtestserver.client.process.vmware;

import java.util.Properties;

import org.jtestserver.client.process.VMConfig;
import org.jtestserver.common.ConfigUtils;

/**
 * Implementation of {@link VMConfig} to configure {@link VMware} and specify 
 * the parameters of the machine to run in <a href="http://www.vmware.com/">VMware</a>.
 * 
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class VMwareConfig implements VMConfig {
    /**
     * Key for the username parameter in the config file.
     */
    public static final String USERNAME = "vmware.server.username";
    
    /**
     * Key for the password parameter in the config file.
     */
    public static final String PASSWORD = "vmware.server.password";

    /**
     * Host of VMware server.
     */
    private final String host;
    
    /**
     * Port of VMware server.
     */
    private final int port;
    
    /**
     * Username to connect to VMware server.
     */
    private final String userName;
    
    /**
     * password to connect to VMware server.
     */
    private final String password;
    
    /**
     * Name of the machine to run in the VMware server. 
     */
    private final String vmName;
    
    /**
     * Build an instance from the given {@link Properties}. 
     * Only properties whose key starts with <b>vmware.</b> will be used.
     * @param properties
     */
    public VMwareConfig(Properties properties) {
        host = ConfigUtils.getString(properties, "vmware.server.host");
        port = ConfigUtils.getInt(properties, "vmware.server.port", 8222);
        userName = ConfigUtils.getString(properties, USERNAME);
        password = ConfigUtils.getString(properties, PASSWORD);
        vmName = ConfigUtils.getString(properties, "vmware.vmName");
    }
    public String getHost() {
        return host;
    }
    public int getPort() {
        return port;
    }
    public String getUserName() {
        return userName;
    }
    public String getPassword() {
        return password;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getVmName() {
        return vmName;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public VMwareServerProcess createServerProcess() {
        return new VMwareServerProcess(this);
    }
}
