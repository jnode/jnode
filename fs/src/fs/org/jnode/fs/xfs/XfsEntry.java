package org.jnode.fs.xfs;

import java.io.IOException;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntryCreated;
import org.jnode.fs.FSEntryLastAccessed;
import org.jnode.fs.spi.AbstractFSEntry;
import org.jnode.fs.util.UnixFSConstants;
import org.jnode.fs.xfs.inode.INode;

/**
 * An entry in a XFS file system.
 *
 * @author Luke Quinane
 */
public class XfsEntry extends AbstractFSEntry implements FSEntryCreated, FSEntryLastAccessed {

    /**
     * The inode.
     */
    private final INode inode;

    /**
     * The directory record ID.
     */
    private final long directoryRecordId;

    /**
     * Creates a new entry.
     *
     * @param inode the inode.
     * @param name the name.
     * @param directoryRecordId the directory record ID.
     * @param fileSystem the file system.
     * @param parent the parent.
     */
    public XfsEntry(INode inode, String name, long directoryRecordId, XfsFileSystem fileSystem, FSDirectory parent) {
        super(fileSystem, null, parent, name, getFSEntryType(name, inode));

        this.inode = inode;
        this.directoryRecordId = directoryRecordId;
    }

    @Override
    public String getId() {
        return Long.toString(inode.getINodeNr()) + '-' + Long.toString(directoryRecordId);
    }

    @Override
    public long getCreated() throws IOException {
        return inode.getCreatedTime();
    }

    @Override
    public long getLastAccessed() throws IOException {
        return inode.getAccessTime();
    }

    @Override
    public long getLastModified() throws IOException {
        return inode.getModifiedTime();
    }

    /**
     * Gets the inode.
     *
     * @return the inode.
     */
    public INode getINode() {
        return inode;
    }

    private static int getFSEntryType(String name, INode inode) {
        int mode = inode.getMode() & UnixFSConstants.S_IFMT;

        if ("/".equals(name))
            return AbstractFSEntry.ROOT_ENTRY;
        else if (mode == UnixFSConstants.S_IFDIR)
            return AbstractFSEntry.DIR_ENTRY;
        else if (mode == UnixFSConstants.S_IFREG || mode == UnixFSConstants.S_IFLNK ||
            mode == UnixFSConstants.S_IFIFO || mode == UnixFSConstants.S_IFCHR ||
            mode == UnixFSConstants.S_IFBLK)
            return AbstractFSEntry.FILE_ENTRY;
        else
            return AbstractFSEntry.OTHER_ENTRY;
    }
}
