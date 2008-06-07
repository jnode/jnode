package org.jnode.fs.hfsplus;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jnode.fs.FSEntry;
import org.jnode.fs.hfsplus.catalog.CatalogFolder;
import org.jnode.fs.hfsplus.catalog.CatalogKey;
import org.jnode.fs.hfsplus.tree.LeafRecord;
import org.jnode.fs.spi.AbstractFSDirectory;
import org.jnode.fs.spi.FSEntryTable;

public class HFSPlusDirectory extends AbstractFSDirectory {
    private final Logger log = Logger.getLogger(getClass());

    private LeafRecord record;

    private CatalogFolder folder;

    public HFSPlusDirectory(final HFSPlusEntry e) {
        super((HfsPlusFileSystem) e.getFileSystem());
        this.record = e.getRecord();
        this.folder = new CatalogFolder(record.getRecordData());
        log.debug("Associated record:" + record.toString());
        if (record.getType() == HfsPlusConstants.RECORD_TYPE_FOLDER) {
            log.debug("Associated folder : " + folder.toString());
        }
    }

    @Override
    protected final FSEntry createDirectoryEntry(final String name) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected final FSEntry createFileEntry(final String name) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected final FSEntryTable readEntries() throws IOException {
        List<FSEntry> pathList = new LinkedList<FSEntry>();
        LeafRecord[] records =
                ((HfsPlusFileSystem) getFileSystem()).getCatalog().getRecords(folder.getFolderId());
        for (LeafRecord rec : records) {
            if (rec.getType() == HfsPlusConstants.RECORD_TYPE_FOLDER ||
                    rec.getType() == HfsPlusConstants.RECORD_TYPE_FILE) {
                String name = ((CatalogKey) rec.getKey()).getNodeName().getUnicodeString();
                HFSPlusEntry e =
                        new HFSPlusEntry((HfsPlusFileSystem) getFileSystem(), null, null, name, rec);
                pathList.add(e);
            }
        }
        return new FSEntryTable(((HfsPlusFileSystem) getFileSystem()), pathList);
    }

    @Override
    protected void writeEntries(final FSEntryTable entries) throws IOException {
        // TODO Auto-generated method stub

    }

}
