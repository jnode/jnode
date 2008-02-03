package org.jnode.fs.hfsplus;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jnode.fs.FSEntry;
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
	
	public HFSPlusDirectory(HFSPlusEntry e){
		super((HfsPlusFileSystem)e.getFileSystem());
		this.record = e.getRecord();
		this.folder = new CatalogFolder(record.getRecordData());
		log.debug("Folder : " + folder.toString());
	}
	
	@Override
	protected FSEntry createDirectoryEntry(String name) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected FSEntry createFileEntry(String name) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected FSEntryTable readEntries() throws IOException {
		List<FSEntry> pathList = new LinkedList<FSEntry>();
		CatalogNodeId parentID = ((CatalogKey)record.getKey()).getParentId();
		while(parentID.getId() != 1) {
			LeafRecord parent = ((HfsPlusFileSystem)getFileSystem()).getCatalog().getRecord(parentID); // Look for the thread record associated with the parent dir
			if(parent == null)
				throw new RuntimeException("No folder thread found!");
			if(parent.getType() == HfsPlusConstants.RECORD_TYPE_FOLDER_THREAD) {
				CatalogThread threadData = new CatalogThread(parent.getRecordData());
				LeafRecord lf =((HfsPlusFileSystem)getFileSystem()).getCatalog().getRecord(threadData.getParentId(), threadData.getNodeName());
				log.debug("Leaf Record : " + lf.toString());
				HFSPlusEntry entry = new HFSPlusEntry((HfsPlusFileSystem)getFileSystem(),null,this, ((CatalogKey)lf.getKey()).getNodeName().getUnicodeString(), lf);
				pathList.add(entry);
				parentID = threadData.getParentId();
			}
			else if(parent.getType() == HfsPlusConstants.RECORD_TYPE_FILE_THREAD)
				throw new RuntimeException("Tried to get folder thread (" + parentID + ",\"\") but found a file thread!");
			else
				throw new RuntimeException("Tried to get folder thread (" + parentID + ",\"\") but found " + parent.getType() +"!");		
		}
		//return pathList;
		return new FSEntryTable(((HfsPlusFileSystem)getFileSystem()),pathList);
	}

	@Override
	protected void writeEntries(FSEntryTable entries) throws IOException {
		// TODO Auto-generated method stub

	}

}
