/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jnode.build;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * This task builds a VMWare '.vmx' file to allow JNode to be run using VMWare player.
 * 
 * @author ...
 * @author crawley@jnode.org
 */
public class VMwareBuilderTask extends Task {

    private String logFile; // log file use for kernel debugger messages
    private String isoFile;
    private int memorySize;
    private String overrideFile;

    /**
     * @return Returns the memorySize.
     */
    public final int getMemSize() {
        return memorySize;
    }

    /**
     * @param memorySize The memorySize to set.
     */
    public final void setMemSize(int memorySize) {
        this.memorySize = memorySize;
    }

    /**
     * @return Returns the memorySize.
     */
    public final String getLogFile() {
        return logFile;
    }

    /**
     * @param memorySize The memorySize to set.
     */
    public final void setLogFile(String logFile) {
        this.logFile = logFile;
    }

    /**
     * The override file is a Java Properties file containing VMX settings
     * to override the default ones hard-wired into this class.
     * 
     * @return Returns the override file or <code>null</code>
     */
    public String getOverrideFile() {
        return overrideFile;
    }

    /**
     * @param overrideFile The override file to set.
     */
    public void setOverrideFile(String overrideFile) {
        this.overrideFile = overrideFile;
    }

    /**
     * @see org.apache.tools.ant.Task#execute()
     */
    @Override
    public void execute() throws BuildException {
        // Build the default properties, based on the supplied memSize and logFile.
        Properties props = new Properties();
        buildDefaultProperties(props);

        if (overrideFile != null && overrideFile.length() > 0) {
            // If VMX overrides are provided, read them and add them to the properties;
            BufferedReader br = null;
            try {
                // Unfortunately, we cannot use java.util.Property#load(...) because
                // VMX properties can have ':' in the property name.
                br = new BufferedReader(new FileReader(overrideFile));
                String line; 
                final Pattern propertyPattern = Pattern.compile("^([a-zA-Z0-9\\.:]+)\\s*=\\s*\"([^\"]*)\"");
                final Pattern commentPattern = Pattern.compile("^#");
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || commentPattern.matcher(line).find()) {
                        continue;
                    }
                    Matcher matcher = propertyPattern.matcher(line);
                    if (!matcher.find()) {
                        throw new BuildException(
                                "Cannot parse this VMX override: '" + line + "'");
                    }
                    props.put(matcher.group(1), matcher.group(2));
                }
            } catch (FileNotFoundException ex) {
                throw new BuildException(
                        "Cannot open the VMX override file: " + overrideFile, ex);
            } catch (IOException ex) {
                throw new BuildException(
                        "Problem reading the VMX override file: " + overrideFile, ex);
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException ex) {
                        /* ignore it */
                    }
                }
            }
        }

        // Now output the VMX file from the properties, sorted in key order for neatness.
        File vmxFile = new File(isoFile + ".vmx");
        try {
            FileWriter out = new FileWriter(vmxFile);
            try {
                PrintWriter w = new PrintWriter(out);
                TreeSet<Object> keys = new TreeSet<Object>();
                keys.addAll(props.keySet());
                for (Object key : keys) {
                    Object value = props.get(key);
                    w.println(key + " = \"" + value + "\"");
                }
            } finally {
                out.close();
            }
        } catch (IOException ex) {
            throw new BuildException("Problem writing the VMX file: " + vmxFile);
        }
    }

    private void buildDefaultProperties(Properties props) {
        props.put("config.version", "8");
        props.put("virtualHW.version", "4");
        props.put("memsize", String.valueOf(memorySize));
        props.put("MemAllowAutoScaleDown", "FALSE");

        props.put("ide0:0.present", "TRUE");
        props.put("ide0:0.startConnected", "TRUE");
        props.put("ide0:0.fileName", new File(isoFile).getName());
        props.put("ide0:0.deviceType", "cdrom-image");
        props.put("ide0:0.redo", "");

        props.put("ide1:0.present", "FALSE");
        props.put("ide1:0.startConnected", "TRUE");

        props.put("floppy0.present", "FALSE");
        props.put("usb.present", "TRUE");
        props.put("sound.present", "FALSE");
        props.put("sound.virtualDev", "es1371");
        props.put("displayName", "JNode");
        props.put("guestOS", "dos");

        props.put("nvram", "JNode.nvram");  
        props.put("MemTrimRate", "-1");  

        final String osName = System.getProperty("os.name").toLowerCase(); 
        if (osName.contains("linux") || osName.contains("unix") || 
                osName.contains("bsd")) {
            props.put("ethernet0.connectionType", "bridged");
            props.put("ethernet0.vnet", "/dev/vmnet1");
        }
        props.put("ethernet0.addressType", "generated");
        props.put("ethernet0.generatedAddress", "00:0c:29:2a:96:30");
        props.put("ethernet0.generatedAddressOffset", "0");
        props.put("ethernet0.present", "TRUE");
        props.put("ethernet0.startConnected", "TRUE");

        props.put("uuid.location", "56 4d 94 59 c9 96 80 88-6c 3a 37 80 04 68 c9 b2");
        props.put("uuid.bios", "56 4d 94 59 c9 96 80 88-6c 3a 37 80 04 68 c9 b2");

        if (logFile != null && logFile.trim().length() != 0) {
            props.put("serial0.present", "TRUE");
            props.put("serial0.fileType", "file");
            props.put("serial0.fileName", logFile);
        }

        props.put("tools.syncTime", "TRUE");  
        props.put("uuid.action", "create");  
        props.put("checkpoint.vmState", "");
    }

    /**
     * @return Returns the isoFile.
     */
    public final String getIsoFile() {
        return isoFile;
    }

    /**
     * @param isoFile
     *            The isoFile to set.
     */
    public final void setIsoFile(String isoFile) {
        this.isoFile = isoFile;
    }

}
