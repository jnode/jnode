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
import java.util.Properties;

import org.jtestserver.client.process.ServerProcess;
import org.jtestserver.client.process.VMConfig;
import org.jtestserver.common.ConfigUtils;
import org.jtestserver.server.TestServer;

/**
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class JVMConfig implements VMConfig {

    private final File javaHome;
    private final String classpath;
    private final String bootClasspath;
    
    private String vmName;
    
    public JVMConfig(Properties properties) {
        javaHome = ConfigUtils.getDirectory(properties, "jvm.java.home", true);
        classpath = ConfigUtils.getClasspath(properties, "jvm.classpath", true);
        bootClasspath = ConfigUtils.getClasspath(properties, "jvm.bootclasspath", false);
    }
    
    @Override
    public ServerProcess createServerProcess() {
        return new JVMServerProcess(this);
    }

    /* (non-Javadoc)
     * @see org.jtestserver.client.process.VMConfig#getVmName()
     */
    @Override
    public String getVmName() {
        return vmName;
    }

    /**
     * @return
     */
    public File getJavaHome() {
        return javaHome;
    }

    /**
     * @return
     */
    public String getClasspath() {
        return classpath;
    }

    /**
     * @return
     */
    public String getBootClasspath() {
        return bootClasspath;
    }

    /**
     * @return
     */
    public String getMainClass() {
        return TestServer.class.getName();
    }

    /**
     * @param vmName
     */
    void setVmName(String vmName) {
        this.vmName = vmName;
    }
}
