/*
 * $Id: header.txt 5714 2010-01-03 13:33:07Z lsantha $
 *
 * Copyright (C) 2003-2012 JNode.org
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
