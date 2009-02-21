/*
 * Copyright 2005-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package sun.tools.jconsole;

import java.util.*;
import java.io.IOException;
import java.io.File;

// Sun specific
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;

// Sun private
import sun.management.ConnectorAddressLink;
import sun.jvmstat.monitor.HostIdentifier;
import sun.jvmstat.monitor.Monitor;
import sun.jvmstat.monitor.MonitoredHost;
import sun.jvmstat.monitor.MonitoredVm;
import sun.jvmstat.monitor.MonitoredVmUtil;
import sun.jvmstat.monitor.MonitorException;
import sun.jvmstat.monitor.VmIdentifier;

public class LocalVirtualMachine {
    private String address;
    private String commandLine;
    private String displayName;
    private int vmid;
    private boolean isAttachSupported;

    public LocalVirtualMachine(int vmid, String commandLine, boolean canAttach, String connectorAddress) {
        this.vmid = vmid;
        this.commandLine = commandLine;
        this.address = connectorAddress;
        this.isAttachSupported = canAttach;
        this.displayName = getDisplayName(commandLine);
    }

    private static String getDisplayName(String commandLine) {
        // trim the pathname of jar file if it's a jar
        String[] res = commandLine.split(" ", 2);
        if (res[0].endsWith(".jar")) {
           File jarfile = new File(res[0]);
           String displayName = jarfile.getName();
           if (res.length == 2) {
               displayName += " " + res[1];
           }
           return displayName;
        }
        return commandLine;
    }

    public int vmid() {
        return vmid;
    }

    public boolean isManageable() {
        return (address != null);
    }

    public boolean isAttachable() {
        return isAttachSupported;
    }

    public void startManagementAgent() throws IOException {
        if (address != null) {
            // already started
            return;
        }

        if (!isAttachable()) {
            throw new IOException("This virtual machine \"" + vmid +
                "\" does not support dynamic attach.");
        }

        loadManagementAgent();
        // fails to load or start the management agent
        if (address == null) {
            // should never reach here
            throw new IOException("Fails to find connector address");
        }
    }

    public String connectorAddress() {
        // return null if not available or no JMX agent
        return address;
    }

    public String displayName() {
        return displayName;
    }

    public String toString() {
        return commandLine;
    }

    // This method returns the list of all virtual machines currently
    // running on the machine
    public static Map<Integer, LocalVirtualMachine> getAllVirtualMachines() {
        Map<Integer, LocalVirtualMachine> map =
            new HashMap<Integer, LocalVirtualMachine>();
        getMonitoredVMs(map);
        getAttachableVMs(map);
        return map;
    }

    private static void getMonitoredVMs(Map<Integer, LocalVirtualMachine> map) {
        MonitoredHost host;
        Set vms;
        try {
            host = MonitoredHost.getMonitoredHost(new HostIdentifier((String)null));
            vms = host.activeVms();
        } catch (java.net.URISyntaxException sx) {
            throw new InternalError(sx.getMessage());
        } catch (MonitorException mx) {
            throw new InternalError(mx.getMessage());
        }
        for (Object vmid: vms) {
            if (vmid instanceof Integer) {
                int pid = ((Integer) vmid).intValue();
                String name = vmid.toString(); // default to pid if name not available
                boolean attachable = false;
                String address = null;
                try {
                     MonitoredVm mvm = host.getMonitoredVm(new VmIdentifier(name));
                     // use the command line as the display name
                     name =  MonitoredVmUtil.commandLine(mvm);
                     attachable = MonitoredVmUtil.isAttachable(mvm);
                     address = ConnectorAddressLink.importFrom(pid);
                     mvm.detach();
                } catch (Exception x) {
                     // ignore
                }
                map.put((Integer) vmid,
                        new LocalVirtualMachine(pid, name, attachable, address));
            }
        }
    }

    private static final String LOCAL_CONNECTOR_ADDRESS_PROP =
        "com.sun.management.jmxremote.localConnectorAddress";

    private static void getAttachableVMs(Map<Integer, LocalVirtualMachine> map) {
        List<VirtualMachineDescriptor> vms = VirtualMachine.list();
        for (VirtualMachineDescriptor vmd : vms) {
            try {
                Integer vmid = Integer.valueOf(vmd.id());
                if (!map.containsKey(vmid)) {
                    boolean attachable = false;
                    String address = null;
                    try {
                        VirtualMachine vm = VirtualMachine.attach(vmd);
                        attachable = true;
                        Properties agentProps = vm.getAgentProperties();
                        address = (String) agentProps.get(LOCAL_CONNECTOR_ADDRESS_PROP);
                        vm.detach();
                    } catch (AttachNotSupportedException x) {
                        // not attachable
                    } catch (IOException x) {
                        // ignore
                    }
                    map.put(vmid, new LocalVirtualMachine(vmid.intValue(),
                                                          vmd.displayName(),
                                                          attachable,
                                                          address));
                }
            } catch (NumberFormatException e) {
                // do not support vmid different than pid
            }
        }
    }

    public static LocalVirtualMachine getLocalVirtualMachine(int vmid) {
        Map<Integer, LocalVirtualMachine> map = getAllVirtualMachines();
        LocalVirtualMachine lvm = map.get(vmid);
        if (lvm == null) {
            // Check if the VM is attachable but not included in the list
            // if it's running with a different security context.
            // For example, Windows services running
            // local SYSTEM account are attachable if you have Adminstrator
            // privileges.
            boolean attachable = false;
            String address = null;
            String name = String.valueOf(vmid); // default display name to pid
            try {
                VirtualMachine vm = VirtualMachine.attach(name);
                attachable = true;
                Properties agentProps = vm.getAgentProperties();
                address = (String) agentProps.get(LOCAL_CONNECTOR_ADDRESS_PROP);
                vm.detach();
                lvm = new LocalVirtualMachine(vmid, name, attachable, address);
            } catch (AttachNotSupportedException x) {
                // not attachable
                if (JConsole.isDebug()) {
                    x.printStackTrace();
                }
            } catch (IOException x) {
                // ignore
                if (JConsole.isDebug()) {
                    x.printStackTrace();
                }
            }
        }
        return lvm;
    }

    // load the management agent into the target VM
    private void loadManagementAgent() throws IOException {
        VirtualMachine vm = null;
        String name = String.valueOf(vmid);
        try {
            vm = VirtualMachine.attach(name);
        } catch (AttachNotSupportedException x) {
            IOException ioe = new IOException(x.getMessage());
            ioe.initCause(x);
            throw ioe;
        }

        String home = vm.getSystemProperties().getProperty("java.home");

        // Normally in ${java.home}/jre/lib/management-agent.jar but might
        // be in ${java.home}/lib in build environments.

        String agent = home + File.separator + "jre" + File.separator +
                           "lib" + File.separator + "management-agent.jar";
        File f = new File(agent);
        if (!f.exists()) {
            agent = home + File.separator +  "lib" + File.separator +
                        "management-agent.jar";
            f = new File(agent);
            if (!f.exists()) {
                throw new IOException("Management agent not found");
            }
        }

        agent = f.getCanonicalPath();
        try {
            vm.loadAgent(agent, "com.sun.management.jmxremote");
        } catch (AgentLoadException x) {
            IOException ioe = new IOException(x.getMessage());
            ioe.initCause(x);
            throw ioe;
        } catch (AgentInitializationException x) {
            IOException ioe = new IOException(x.getMessage());
            ioe.initCause(x);
            throw ioe;
        }

        // get the connector address
        Properties agentProps = vm.getAgentProperties();
        address = (String) agentProps.get(LOCAL_CONNECTOR_ADDRESS_PROP);

        vm.detach();
    }
}
