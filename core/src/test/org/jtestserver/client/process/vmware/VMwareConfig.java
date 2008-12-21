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

public class VMwareConfig implements VMConfig {
    public static final String USERNAME = "vmware.server.username";
    public static final String PASSWORD = "vmware.server.password";
    
    private final String host;
    private final int port;
    private final String userName;
    private final String password;
    private final String vmName;
    
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
    
    @Override
    public String getVmName() {
        return vmName;
    }
    
    @Override
    public VMwareServerProcess createServerProcess() {
        return new VMwareServerProcess(this);
    }
}
