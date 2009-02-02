/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.fs.jifs.files;

import org.jnode.fs.FSDirectory;
import org.jnode.fs.jifs.JIFSFile;
import org.jnode.vm.VmSystem;

/**
 * File, which contains the uptime of the system.
 * 
 * @author Andreas H\u00e4nel
 */
public class JIFSFuptime extends JIFSFile {

    public JIFSFuptime() {
        super("uptime");
        refresh();
    }

    public JIFSFuptime(FSDirectory parent) {
        this();
        setParent(parent);
    }

    public void refresh() {
        super.refresh();
        long sinceboot = VmSystem.currentKernelMillis();
        int hours = (int) (sinceboot / (1000 * 60 * 60));
        int minutes = (int) ((sinceboot / (1000 * 60)) % 60);
        int seconds = (int) ((sinceboot / 1000) % 60);
        String h = (hours < 10) ? "0" : "";
        String m = (minutes < 10) ? "0" : "";
        String s = (seconds < 10) ? "0" : "";
        addStringln("Time since booting JNode:");
        addStringln("\t" + h + hours + ":" + m + minutes + ":" + s + seconds);
    }

}
