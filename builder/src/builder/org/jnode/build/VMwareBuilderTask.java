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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class VMwareBuilderTask extends Task {

	private String logFile; // log file use for kernel debugger messages
    private String isoFile;
    private int memorySize;

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
     * @see org.apache.tools.ant.Task#execute()
     */
    @Override
    public void execute() throws BuildException {
        try {
            FileWriter out = new FileWriter(new File(isoFile + ".vmx"));
            try {
                PrintWriter w = new PrintWriter(out);
                
                put(w, "config.version", "8");
                put(w, "virtualHW.version", "4");
                put(w, "memsize", String.valueOf(memorySize));
                put(w, "MemAllowAutoScaleDown", "FALSE");
                put(w, "ide0:0.present", "TRUE");
                put(w, "ide0:0.fileName", new File(isoFile).getName());
                put(w, "ide0:0.deviceType", "cdrom-image");
                put(w, "ide1:0.present", "FALSE");
                put(w, "floppy0.present", "FALSE");
                put(w, "usb.present", "TRUE");
                put(w, "sound.present", "FALSE");
                put(w, "sound.virtualDev", "es1371");
                put(w, "displayName", "JNode");
                put(w, "guestOS", "dos");

                put(w, "nvram", "JNode.nvram");  
                put(w, "MemTrimRate", "-1");  
                put(w, "ide0:0.redo", "");  
                
                final String osName = System.getProperty("os.name").toLowerCase(); 
                if(osName.contains("linux") || osName.contains("unix") || 
                   osName.contains("bsd"))
                {
                	put(w, "ethernet0.connectionType", "bridged");
                	put(w, "ethernet0.vnet", "/dev/vmnet1");
            	}
                put(w, "ethernet0.addressType", "generated");
                put(w, "ethernet0.generatedAddress", "00:0c:29:2a:96:30");
                put(w, "ethernet0.generatedAddressOffset", "0");
                put(w, "ethernet0.present", "TRUE");
                put(w, "ethernet0.startConnected", "TRUE");

                put(w, "uuid.location", "56 4d 94 59 c9 96 80 88-6c 3a 37 80 04 68 c9 b2");
                put(w, "uuid.bios", "56 4d 94 59 c9 96 80 88-6c 3a 37 80 04 68 c9 b2");

                		
                if((logFile != null) && (logFile.trim().length() != 0))
                {
	                put(w, "serial0.present", "TRUE");
	                put(w, "serial0.fileType", "file");
	                put(w, "serial0.fileName", logFile);
                }
                
                put(w, "tools.syncTime", "TRUE");  
                put(w, "ide1:0.startConnected", "TRUE");  
                put(w, "uuid.action", "create");  
                put(w, "checkpoint.vmState", "");  

            } finally {
                out.close();
            }
        } catch (IOException ex) {
            throw new BuildException(ex);
        }
    }
    
    private void put(PrintWriter w, String key, String value) {
        w.println(key + " = \"" + value + "\"");
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
