package org.jnode.fs.hfsplus;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jnode.fs.FSEntry;
import org.jnode.fs.ReadOnlyFileSystemException;
import org.jnode.fs.hfsplus.catalog.CatalogFolder;
import org.jnode.fs.hfsplus.catalog.CatalogKey;
import org.jnode.fs.hfsplus.catalog.CatalogNodeId;
import org.jnode.fs.hfsplus.catalog.CatalogThread;
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
    protected final FSEntry createDirectoryEntry(final String name)
        throws IOException {
        if (!canWrite()) {
            throw new ReadOnlyFileSystemException();
        }
        Superblock volumeHeader = ((HfsPlusFileSystem) getFileSystem())
                .getVolumeHeader();

        Calendar now = Calendar.getInstance();
        now.setTime(new Date());
        int macDate = (int) HFSUtils
                .getDate(now.getTimeInMillis() / 1000, true);

        HFSUnicodeString dirName = new HFSUnicodeString(name);
        CatalogThread thread = new CatalogThread(
                HfsPlusConstants.RECORD_TYPE_FOLDER_THREAD, this.folder
                        .getFolderId(), dirName);

        CatalogFolder newFolder = new CatalogFolder();
        newFolder
                .setFolderId(new CatalogNodeId(volumeHeader.getNextCatalogId()));
        newFolder.setCreateDate(macDate);
        newFolder.setContentModDate(macDate);
        newFolder.setAttrModDate(macDate);
        log.debug("New catalog folder :\n" + newFolder.toString());

        CatalogKey key = new CatalogKey(this.folder.getFolderId(), dirName);
        log.debug("New catalog key :\n" + key.toString());

        LeafRecord folderRecord = new LeafRecord(key, newFolder.getBytes());
        log.debug("New record folder :\n" + folderRecord.toString());

        HFSPlusEntry newEntry = new HFSPlusEntry(
                (HfsPlusFileSystem) getFileSystem(), null, this, name,
                folderRecord);
        volumeHeader.setFolderCount(volumeHeader.getFolderCount() + 1);
        log.debug("New volume header :\n" + volumeHeader.toString());

        return newEntry;
    }

    @Override
    protected final FSEntry createFileEntry(final String name)
        throws IOException {
        throw new ReadOnlyFileSystemException();
    }

    public synchronized void remove(String name) throws IOException {
        if (!canWrite()) {
            throw new ReadOnlyFileSystemException();
        }
    }

    @Override
    protected final FSEntryTable readEntries() throws IOException {
        List<FSEntry> pathList = new LinkedList<FSEntry>();
        LeafRecord[] records = ((HfsPlusFileSystem) getFileSystem())
                .getCatalog().getRecords(folder.getFolderId());
        for (LeafRecord rec : records) {
            if (rec.getType() == HfsPlusConstants.RECORD_TYPE_FOLDER
                    || rec.getType() == HfsPlusConstants.RECORD_TYPE_FILE) {
                String name = ((CatalogKey) rec.getKey()).getNodeName()
                        .getUnicodeString();
                HFSPlusEntry e = new HFSPlusEntry(
                        (HfsPlusFileSystem) getFileSystem(), null, this, name,
                        rec);
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
