/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.fs.ext2;



import org.jnode.fs.FileSystemException;



/**

 * This class stores nonpersistent information about the inode.

 * It also contains a reference to the part of the inode table

 * which contains this inode.

 * 

 * @author Andras Nagy 

 */

public class INodeDescriptor {

	private int iNodeNr;

	

	/* The number of blocks allocated for the iNode is not neccessary equal to its size in blocks:

	 * 	- if a block has been allocated but not yet been written

	 *  - if the file is in the process of being truncated, i.e. some blocks have already been 

	 * 	  freed, but the new size has not yet been set

	 * lastAllocatedBlockIndex is always the index of the last allocated block 

	 */

	private long lastAllocatedBlockIndex=-1;



	//which block group contains the inode

	private int group;

	//the index of the inode in the block group

	private int index;

	

	//the inode table that contains this inode

	private INodeTable iNodeTable;



	//preallocated blocks are maintained as long as the inode is in memory.

	//When it is flushed(), unused preallocated blocks must be freed in the 

	//block bitmap.

	private long preallocBlock;

	private int preallocCount;

	public synchronized long usePreallocBlock() throws FileSystemException {

		if(preallocCount <= 0)

			throw new FileSystemException("No preallocated blocks");

		--preallocCount;

		

		return preallocBlock++;

	}

	

	public INodeDescriptor(INodeTable iNodeTable, int iNodeNr, int group, int index) {

		this.iNodeTable = iNodeTable;

		this.iNodeNr = iNodeNr;

		this.group = group;

		this.index = index;

		preallocCount = 0;

	}	

	

/*	public long getBlock() {

		return block;

	}



	public long getOffset() {

		return offset;

	}

*/



	/**

	 * @return

	 */

	/*

	public long getPreallocBlock() {

		return preallocBlock;

	}

	*/



	/**

	 * @return

	 */

	public int getPreallocCount() {

		return preallocCount;

	}



	/**

	 * @param l

	 */

	public synchronized void setPreallocBlock(long l) {

		preallocBlock = l;

	}



	/**

	 * @param l

	 */

	public synchronized void setPreallocCount(int l) {

		preallocCount = l;

	}

	/**

	 * Returns in which block group the inode is placed

	 * @return int

	 */

	public int getGroup() {

		return group;

	}



	/**

	 * Returns the index of the inode in its inode table (there is an inode

	 * which block group the inode is placed

	 * @return int

	 */

	public int getIndex() {

		return index;

	}

	

	/**

	 * Returns the inode table that contains this inode

	 * @return the INode table

	 */

	public INodeTable getINodeTable() {

		return iNodeTable;

	}

	/**

	 * @return

	 */

	public long getLastAllocatedBlockIndex() {

		return lastAllocatedBlockIndex;

	}



	/**

	 * @param l

	 */

	public void setLastAllocatedBlockIndex(long l) {

		lastAllocatedBlockIndex = l;

	}



	/**

	 * @return

	 */

	public int getINodeNr() {

		return iNodeNr;

	}



}

