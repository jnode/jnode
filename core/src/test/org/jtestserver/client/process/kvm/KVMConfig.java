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
package org.jtestserver.client.process.kvm;

import java.io.File;
import java.util.Properties;

import org.jtestserver.client.process.ServerProcess;
import org.jtestserver.client.process.VMConfig;
import org.jtestserver.common.ConfigUtils;

public class KVMConfig implements VMConfig {
    private final int memory;
    private final File cdrom;
    private final String options;
    private final String serial;
    private final String keyboard;
    
    public KVMConfig(Properties properties) {
        memory = ConfigUtils.getInt(properties, "kvm.memory", 256);
        cdrom = ConfigUtils.getFile(properties, "kvm.cdrom", true);
        options = ConfigUtils.getString(properties, "kvm.options");
        serial = ConfigUtils.getString(properties, "kvm.serial");
        keyboard = ConfigUtils.getString(properties, "kvm.keyboard");
    }

    public int getMemory() {
        return memory;
    }

    public File getCdrom() {
        return cdrom;
    }

    public String getOptions() {
        return options;
    }

    public String getSerial() {
        return serial;
    }

    public String getKeyboard() {
        return keyboard;
    }
    
    @Override
    public String getVmName() {
        return getCdrom().getAbsolutePath();
    }
    
    @Override
    public final ServerProcess createServerProcess() {
        return new KVMServerProcess(this);
    }
}
