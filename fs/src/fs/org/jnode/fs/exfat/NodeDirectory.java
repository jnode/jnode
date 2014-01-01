/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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
 
package org.jnode.fs.exfat;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSDirectoryId;
import org.jnode.fs.FSEntry;
import org.jnode.fs.spi.AbstractFSObject;

/**
 * @author Matthias Treydte &lt;waldheinz at gmail.com&gt;
 */
final class NodeDirectory extends AbstractFSObject implements FSDirectory, FSDirectoryId {

    private final Node node;
    private final Map<String, NodeEntry> nameToNode;
    private final UpcaseTable upcase;

    public NodeDirectory(ExFatFileSystem fs, Node node)
        throws IOException {

        super(fs);

        this.node = node;
        this.upcase = fs.getUpcase();
        this.nameToNode = new HashMap<String, NodeEntry>();

        DirectoryParser.
            create(node).
            setUpcase(this.upcase).
            parse(new VisitorImpl());

    }

    @Override
    public String getDirectoryId() {
        return Long.toString(node.getStartCluster());
    }

    @Override
    public Iterator<FSEntry> iterator() {
        return Collections.<FSEntry>unmodifiableCollection(
            nameToNode.values()).iterator();
    }

    @Override
    public FSEntry getEntry(String name) throws IOException {
        return this.nameToNode.get(upcase.toUpperCase(name));
    }

    @Override
    public FSEntry getEntryById(String id) throws IOException {
        for (NodeEntry nodeEntry : nameToNode.values()) {
            if (nodeEntry.getId().equals(id)) {
                return nodeEntry;
            }
        }

        throw new IOException("Failed to find entry with ID:" + id);
    }

    @Override
    public FSEntry addFile(String name) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public FSEntry addDirectory(String name) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void remove(String name) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void flush() throws IOException {
        /* nothing to do */
    }

    private class VisitorImpl implements DirectoryParser.Visitor {

        @Override
        public void foundLabel(String label) throws IOException {
            /* ignore */
        }

        @Override
        public void foundBitmap(
            long startCluster, long size) throws IOException {

            /* ignore */
        }

        @Override
        public void foundUpcaseTable(DirectoryParser parser, long checksum,
                                     long startCluster, long size) throws IOException {
            
            /* ignore */
        }

        @Override
        public void foundNode(Node node, int index) throws IOException {
            final String upcaseName = upcase.toUpperCase(node.getName());

            nameToNode.put(upcaseName,
                new NodeEntry((ExFatFileSystem) getFileSystem(), node, NodeDirectory.this, index));

        }

    }

}
