package org.jnode.fs.hfsplus.catalog;

import static org.junit.Assert.*;

import org.junit.Test;

public class CatalogFolderTest {

	@Test
	public void testCatalogFolderIntCatalogNodeId() {
		CatalogFolder folder = new CatalogFolder(0,CatalogNodeId.HFSPLUS_ROOT_CNID);
		assertNotNull(folder);
		assertEquals(0, folder.getValence());
		assertEquals(2,folder.getFolderId().getId());
		folder = new CatalogFolder(folder.getBytes());
		assertNotNull(folder);
		assertEquals(0, folder.getValence());
		assertEquals(2,folder.getFolderId().getId());
		
	}
	
	@Test
	public void testCatalogFolder(){
		CatalogFolder folder = new CatalogFolder(new byte[]{0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, -53, 96, 7, 78, -53, 96, 7, 78, -53, 96, 7, 78, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 26, 0, 0, 0, 2, 0, 9, 0, 116, 0, 101, 0, 115, 0, 116, 0, 100, 0, 114, 0, 105, 0, 118, 0, 101, 0, 3});
		assertNotNull(folder);
		assertEquals(0, folder.getValence());
		assertEquals(2,folder.getFolderId().getId());
	}

}
