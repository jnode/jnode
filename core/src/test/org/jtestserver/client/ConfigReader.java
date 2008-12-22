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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.jtestserver.client.process.VMConfig;
import org.jtestserver.client.process.kvm.KVMConfig;
import org.jtestserver.client.process.vmware.VMwareConfig;
import org.jtestserver.common.ConfigUtils;

/**
 * Utility class used to read the JTestServer configuration and the VM configuration.
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class ConfigReader {
    /**
     * Key name in the property file that specify the type of the VM (vmware, kvm, ...)
     */
    protected static final String VM_TYPE = "type";
    
    /**
     * Value assigned to {@link ConfigReader#VM_TYPE} key for a VMware machine.
     */
    public static final String VMWARE_TYPE = "vmware";
    
    /**
     * Value assigned to {@link ConfigReader#VM_TYPE} key for a KVM machine.
     */
    public static final String KVM_TYPE = "kvm";
    
    /**
     * Read the JTestServer configuration and the VM configuration.
     * @param configDir Directory where configuration is stored.
     * @return the configuration.
     * @throws IOException
     */
    public Config read(File configDir) throws IOException {
        Properties properties = readProperties(configDir, "config.properties");
        
        // read the vm configuration
        String vm = ConfigUtils.getString(properties, "use.vm");
        Properties vmProperties = readProperties(configDir, vm + ".properties");
        VMConfig vmConfig = createVMConfig(vmProperties, vm);
            
        return new Config(properties, vmConfig);
    }

    /**
     * Read the configuration of the VM.
     * @param vmProperties properties of the VM
     * @param vm name of the VM
     * @return
     * @throws IOException
     */
    protected VMConfig createVMConfig(Properties vmProperties, String vm) throws IOException {
        String type = ConfigUtils.getString(vmProperties, VM_TYPE);
        final VMConfig vmConfig;
        if (KVM_TYPE.equals(type)) {
            vmConfig = new KVMConfig(vmProperties);
        } else if (VMWARE_TYPE.equals(type)) {
            vmConfig = new VMwareConfig(vmProperties);
        } else {
            throw new IllegalArgumentException("unsupported type for " + vm + " vm : " + type);
        }
        
        return vmConfig;
    }
    
    /**
     * Read a properties file.
     * 
     * @param configDir Directory where configuration is stored.
     * @param name of the file to read.
     * @return the properties file.
     * @throws IOException
     */
    private Properties readProperties(File configDir, String name) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(new File(configDir, name)));
        return properties;
    }
}
