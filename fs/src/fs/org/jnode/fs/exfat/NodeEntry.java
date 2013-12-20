package org.jnode.fs.exfat;

import java.io.IOException;
import org.jnode.fs.FSAccessRights;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSEntryCreated;
import org.jnode.fs.FSEntryLastAccessed;
import org.jnode.fs.FSFile;
import org.jnode.fs.spi.AbstractFSObject;

/**
 * @author Matthias Treydte &lt;waldheinz at gmail.com&gt;
 */
public final class NodeEntry extends AbstractFSObject implements FSEntry, FSEntryCreated, FSEntryLastAccessed {

    private final Node node;
    private final NodeDirectory parent;

    /**
     * The index of this entry in the parent.
     */
    private int index;

    public NodeEntry(ExFatFileSystem fs, Node node, NodeDirectory parent, int index) {
        super(fs);

        this.node = node;
        this.parent = parent;
        this.index = index;
    }

    @Override
    public String getId() {
        return Integer.toString(index);
    }

    @Override
    public String getName() {
        return node.getName();
    }

    @Override
    public FSDirectory getParent() {
        return parent;
    }

    @Override
    public long getLastModified() throws IOException {
        return node.getTimes().getModified().getTime();
    }

    @Override
    public long getCreated() throws IOException {
        return node.getTimes().getCreated().getTime();
    }

    @Override
    public long getLastAccessed() throws IOException {
        return node.getTimes().getAccessed().getTime();
    }

    @Override
    public boolean isFile() {
        return (!this.node.isDirectory());
    }

    @Override
    public boolean isDirectory() {
        return this.node.isDirectory();
    }

    @Override
    public void setName(String newName) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setLastModified(long lastModified) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public FSFile getFile() throws IOException {
        if (!isFile()) {
            throw new UnsupportedOperationException("not a file");
        }

        return new NodeFile((ExFatFileSystem) getFileSystem(), this.node);
    }

    @Override
    public FSDirectory getDirectory() throws IOException {
        if (!isDirectory()) {
            throw new UnsupportedOperationException("not a directory");
        }

        return new NodeDirectory((ExFatFileSystem) getFileSystem(), node);
    }

    @Override
    public FSAccessRights getAccessRights() throws IOException {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    /**
     * Gets the node for this entry.
     *
     * @return the node.
     */
    public Node getNode() {
        return node;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append(NodeEntry.class.getName());
        sb.append(" [node=");
        sb.append(this.node);
        sb.append(", parent=");
        sb.append(this.parent);
        sb.append("]");

        return sb.toString();
    }

}
