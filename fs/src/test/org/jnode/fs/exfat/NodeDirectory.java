
package org.jnode.fs.exfat;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.spi.AbstractFSObject;

/**
 * @author Matthias Treydte &lt;waldheinz at gmail.com&gt;
 */
final class NodeDirectory extends AbstractFSObject implements FSDirectory {

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
    public Iterator<FSEntry> iterator() {
        return Collections.<FSEntry>unmodifiableCollection(
            nameToNode.values()).iterator();
    }

    @Override
    public FSEntry getEntry(String name) throws IOException {
        return this.nameToNode.get(upcase.toUpperCase(name));
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
        public void foundNode(Node node) throws IOException {
            final String upcaseName = upcase.toUpperCase(node.getName());

            nameToNode.put(upcaseName,
                new NodeEntry((ExFatFileSystem) getFileSystem(), node, NodeDirectory.this));
        }

    }

}
