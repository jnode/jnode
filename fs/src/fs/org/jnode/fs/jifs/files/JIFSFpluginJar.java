/*
 * $Id$
 *
 * JNode.org
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

import java.nio.ByteBuffer;

import javax.naming.NameNotFoundException;

import org.jnode.fs.FSDirectory;
import org.jnode.fs.jifs.JIFSFile;
import org.jnode.naming.InitialNaming;
import org.jnode.plugin.PluginManager;
import org.jnode.plugin.model.PluginDescriptorModel;

/**
 * @author Levente S\u00e1ntha
 */
public class JIFSFpluginJar extends JIFSFile {
    private String id;
    protected ByteBuffer buffer;

    public JIFSFpluginJar() {
        return;
    }

    public JIFSFpluginJar(String id, FSDirectory parent) {
        super(id + ".jar", parent);
        this.id = id;
        refresh();
    }

    public void refresh() {
        try {
            final PluginManager mgr = InitialNaming.lookup(PluginManager.NAME);
            PluginDescriptorModel pdm =
                    (PluginDescriptorModel) mgr.getRegistry().getPluginDescriptor(id);
            if (pdm != null) {

                buffer = pdm.getJarFile().getBuffer();
                isvalid = buffer != null;
            } else {
                isvalid = false;
            }
        } catch (NameNotFoundException e) {
            System.err.println(e);
        }
    }

    public void read(long fileOffset, ByteBuffer destBuf) {
        refresh();
        if (buffer == null) {
            return;
        }
        ByteBuffer buf = buffer;
        buf.position((int) fileOffset);
        buf = (ByteBuffer) buf.slice().limit(destBuf.remaining());
        destBuf.put(buf);
    }

    public long getLength() {
        refresh();
        if (buffer == null) {
            return 0;
        }
        ByteBuffer buf = buffer;
        buf.position(0);
        return buf.remaining();
    }
}
