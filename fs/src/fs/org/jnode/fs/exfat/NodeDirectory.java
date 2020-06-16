/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSDirectoryId;
import org.jnode.fs.FSEntry;
import org.jnode.fs.spi.AbstractFSObject;

/**
 * @author Matthias Treydte &lt;waldheinz at gmail.com&gt;
 */
public class NodeDirectory extends AbstractFSObject implements FSDirectory, FSDirectoryId {

    private final NodeEntry nodeEntry;
    private final Map<String, NodeEntry> nameToNode;
    private final Map<String, NodeEntry> idToNode;
    private final UpcaseTable upcase;

    public NodeDirectory(ExFatFileSystem fs, NodeEntry nodeEntry)
        throws IOException {

        this(fs, nodeEntry, false, false);
    }

    public NodeDirectory(ExFatFileSystem fs, NodeEntry nodeEntry, boolean showDeleted, boolean performChecks)
        throws IOException {

        super(fs);

        this.nodeEntry = nodeEntry;
        this.upcase = fs.getUpcase();
        this.nameToNode = new LinkedHashMap<String, NodeEntry>();
        this.idToNode = new LinkedHashMap<String, NodeEntry>();

        DirectoryParser.
            create(nodeEntry.getNode(), showDeleted, performChecks).
            setUpcase(this.upcase).
            parse(new VisitorImpl());

    }

    @Override
    public String getDirectoryId() {
        return Long.toString(nodeEntry.getNode().getStartCluster());
    }

    @Override
    public Iterator<FSEntry> iterator() {
        return Collections.<FSEntry>unmodifiableCollection(
            idToNode.values()).iterator();
    }

    @Override
    public FSEntry getEntry(String name) throws IOException {
        return this.nameToNode.get(upcase.toUpperCase(name));
    }

    @Override
    public FSEntry getEntryById(String id) throws IOException {
        NodeEntry nodeEntry = idToNode.get(id);

        if (nodeEntry != null) {
            return nodeEntry;
        }

        throw new IOException("Failed to find entry with ID:" + id);
    }

    @Override
    public FSEntry addFile(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public FSEntry addDirectory(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void remove(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void flush() throws IOException {
        /* nothing to do */
    }

    /**
     * Gets the node associated with this directory.
     *
     * @return the node.
     */
    public Node getNode() {
        return nodeEntry.getNode();
    }

    /**
     * Gets the parent directory.
     *
     * @return the parent directory, or {@code null} if this is the root directory.
     */
    public FSDirectory getParent() {
        return nodeEntry.getParent();
    }

    private class VisitorImpl implements DirectoryParser.Visitor {

        @Override
        public void foundLabel(String label) {
            /* ignore */
        }

        @Override
        public void foundBitmap(
            long startCluster, long size) {

            /* ignore */
        }

        @Override
        public void foundUpcaseTable(DirectoryParser parser, long checksum,
                                     long startCluster, long size) {

            /* ignore */
        }

        @Override
        public void foundNode(Node node, int index) throws IOException {
            final String upcaseName = upcase.toUpperCase(node.getName());

            NodeEntry nodeEntry = new NodeEntry((ExFatFileSystem) getFileSystem(), node, NodeDirectory.this, index);
            nameToNode.put(upcaseName, nodeEntry);
            idToNode.put(nodeEntry.getId(), nodeEntry);
        }

    }

}
