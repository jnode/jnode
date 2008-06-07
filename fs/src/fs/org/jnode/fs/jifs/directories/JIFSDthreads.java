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
 
package org.jnode.fs.jifs.directories;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.jifs.JIFSDirectory;
import org.jnode.fs.jifs.JIFSFile;
import org.jnode.fs.jifs.files.JIFSFthread;


/**
 * Directory containing one file for each java.lang.thread. Based on the thread
 * command.
 * 
 * @author Andreas H\u00e4nel
 */
public class JIFSDthreads extends JIFSDirectory {

    public JIFSDthreads() throws IOException {
        super("threads");
        refresh();
    }

    public JIFSDthreads(FSDirectory parent) throws IOException {
        this();
        setParent(parent);
    }

    public void refresh() {
        super.clear();
        ThreadGroup grp = Thread.currentThread().getThreadGroup();
        while (grp.getParent() != null) {
            grp = grp.getParent();
        }
        addGroup(grp);
    }

    private void addGroup(final ThreadGroup grp) {

        final int max = grp.activeCount() * 2;
        final Thread[] ts = new Thread[max];
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                grp.enumerate(ts, false);
                return null;
            }
        });
        for (int i = 0; i < max; i++) {
            final Thread t = ts[i];
            if (t != null) {
                JIFSFile F = new JIFSFthread(t.getName(), t, this);
                addFSE(F);
            }
        }

        final int gmax = grp.activeGroupCount() * 2;
        final ThreadGroup[] tgs = new ThreadGroup[gmax];

        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                grp.enumerate(tgs, false);
                return null;
            }
        });
        for (int i = 0; i < gmax; i++) {
            final ThreadGroup tg = tgs[i];
            if (tg != null) {
                addGroup(tg);
            }
        }
    }

    public FSEntry getEntry(String name) {
        return super.getEntry(name);
    }
}
