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
 
package org.jnode.fs.jifs.directories;

import java.io.IOException;
import java.util.Iterator;

import org.jnode.fs.FSEntry;
import org.jnode.fs.jifs.JIFSDirectory;
import org.jnode.fs.jifs.files.JIFSFdevices;
import org.jnode.fs.jifs.files.JIFSFmemory;
import org.jnode.fs.jifs.files.JIFSFuptime;
import org.jnode.fs.jifs.files.JIFSFversion;

public class JIFSDrootDir extends JIFSDirectory {

    private boolean inited;

    public JIFSDrootDir(String name) {
        super(name);
        inited = false;
    }

    private void setup() throws IOException {
        // file
        addFSE(new JIFSFuptime(this));
        addFSE(new JIFSFmemory(this));
        addFSE(new JIFSFversion(this));
        addFSE(new JIFSFdevices(this));
        // directory
        addFSE(new JIFSDthreads(this));
        addFSE(new JIFSDplugins(this));
        addFSE(new JIFSDpluginJars(this));
        addFSE(new JIFSDirectory("extended", this));
    }

    private void checkInit() {
        if (!inited) {
            try {
                setup();
            } catch (IOException e) {
                e.printStackTrace();
            }
            inited = true;
        }
    }

    public Iterator<FSEntry> iterator() {
        checkInit();
        return super.iterator();
    }

    public FSEntry getEntry(String name) {
        checkInit();
        return super.getEntry(name);
    }
}
