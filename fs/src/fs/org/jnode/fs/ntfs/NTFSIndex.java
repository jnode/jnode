/*
 * $Id$
 */
package org.jnode.fs.ntfs;

import java.io.IOException;
import java.util.*;

import org.jnode.fs.ntfs.attributes.*;

/**
 * @author Chira
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class NTFSIndex
{
	
	public static int NTFS_INDXMAGIC = 0x58444E49;
	
	NTFSFileRecord fileRecord = null;
	private NTFSResidentAttribute indexRootAttribute = null;
	private NTFSNonResidentAttribute indexAllocationAttribute = null;
	
	public NTFSIndex(NTFSFileRecord fileRecord)
	{
		this.fileRecord = fileRecord;
	}

	public NTFSResidentAttribute getIndexRootAttribute()
	{
		if(indexRootAttribute == null)
			indexRootAttribute = (NTFSResidentAttribute) fileRecord.getAttribute(NTFSFileRecord.$INDEX_ROOT);
		return indexRootAttribute;
	}
	public  NTFSNonResidentAttribute getIndexAllocationAttribute()
	{
		if(indexAllocationAttribute == null)
			indexAllocationAttribute = (NTFSNonResidentAttribute) fileRecord.getAttribute(NTFSFileRecord.$INDEX_ALLOCATION);
		//System.out.println(indexAllocationAttribute.getFlags());
		return indexAllocationAttribute;
	}
	
	public int getAttributeType()
	{
		return NTFSUTIL.LE_READ_U32_INT( 
							this.getIndexRootAttribute().getBuffer(),
							this.getIndexRootAttribute().getAttributeOffset() + 0x00
					);
	}
	
	public int getCollationRule()
	{
		return NTFSUTIL.LE_READ_U32_INT( 
				this.getIndexRootAttribute().getBuffer(),
				this.getIndexRootAttribute().getAttributeOffset() + 0x04
		);
	}
	
	// this is the size of a NODE in the tree
	public int getSizeOfIndexAllocationEntry()
	{
		return NTFSUTIL.LE_READ_U32_INT( 
				this.getIndexRootAttribute().getBuffer(),
				this.getIndexRootAttribute().getAttributeOffset() + 0x08
		);
	}
	// this returns the numer of clusters that we need to read ro a indexrecord
	public byte getClusterPerIndex()
	{
		byte clusterPerindex = this.getIndexRootAttribute().getBuffer()[this.getIndexRootAttribute().getAttributeOffset() + 0x0C];
		if(clusterPerindex < 0 )
			return 1;
		else
			return clusterPerindex;
	}
	
	public int getIndexEntrySizeInBytes()
	{
		int clusterPerindex = this.getIndexRootAttribute().getBuffer()[this.getIndexRootAttribute().getAttributeOffset() + 0x0C];
		if(clusterPerindex > 0)
			return clusterPerindex * this.fileRecord.getVolume().getClusterSize();
		else
			if(clusterPerindex < 0)
				return 1 << -(clusterPerindex); 
		return 0;
	}
	
	public Iterator iterator()
	{
		return new Iterator()
		{
			// start with root
			byte[] node = NTFSIndex.this.getIndexRootAttribute().getBuffer();

			ArrayList subnodesList = new ArrayList();
			
			// offset inside root attribute
			int offset = NTFSIndex.this.getIndexRootAttribute().getAttributeOffset() + 0x10 + 
			NTFSUTIL.LE_READ_32_INT(
					node,
					NTFSIndex.this.getIndexRootAttribute().getAttributeOffset() + 0x10);
			
			NTFSIndexEntry indexEntry = null;
			
			public boolean hasNext()
			{
				// if it is the first time but it is the last entry without subnodes
				if(indexEntry == null && (node[offset + 0x0C] & 0x02) != 0)
					return (node[offset + 0x0C] & 0x01) != 0;
				return (node[offset + 0x0C] & 0x02) == 0 ? true : !subnodesList.isEmpty();
				
			}
			private void endOfNodeReached()
			{
				// take the first subnode from the list
				if(!subnodesList.isEmpty())
				{
					Integer intSubNodeVCN = (Integer) subnodesList.iterator().next();
					int subnodeVCN = intSubNodeVCN.intValue(); 
					subnodesList.remove(intSubNodeVCN);
					if(subnodeVCN > NTFSIndex.this.getIndexAllocationAttribute().getLastVCN() )
					{
						System.out.println("Something is wrong. You try to read VCN="  + subnodeVCN + " but my last VCN=" + NTFSIndex.this.getIndexAllocationAttribute().getLastVCN());
						
					}
					try
					{
						// ok now set the node buffer to an indexrecord
						node = NTFSIndex.this.getIndexAllocationAttribute().readVCN(
												subnodeVCN,
												NTFSIndex.this.getClusterPerIndex()
												);
						if(NTFSUTIL.LE_READ_U32_INT(node,0) != NTFSIndex.NTFS_INDXMAGIC)
							throw new RuntimeException("ERROR: The Index record is not signed!! panic! I have more subnodes to process!");
						
						// reset the offset
						offset = NTFSUTIL.LE_READ_U32_INT(node,0x18) + 0x18;
						
					} catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}
			public Object next()
			{
				//setup the offset and the junmp to subnodes
				int entrysize = NTFSUTIL.LE_READ_U16_INT(
														node[offset + 0x08],
														node[offset + 0x09]
												);
				indexEntry =  
						new NTFSIndexEntry(
									NTFSIndex.this.fileRecord,
									NTFSUTIL.extractSubBuffer(
											node,
											offset,
											entrysize
									)
						);
				if(indexEntry.hasSubNodes())
				{
					// add the vcn to the subnodes list
					subnodesList.add(new Integer(NTFSUTIL.LE_READ_U32_INT(
											node,
											offset + (entrysize - 8))));
				}
				// move the offset to next IndexEntry
				offset += entrysize;
				// if it is the last one than go to next one
				if(indexEntry.isLastIndexEntryInSubnode() && !subnodesList.isEmpty())
				{	
					this.endOfNodeReached();
					if(this.hasNext())
						this.next();
				}
				if(
						indexEntry.getFileName().startsWith("$") || 
						(indexEntry.getNameSpace() & 0x01) == 0)
				{
					if(this.hasNext())
						this.next();
				}
				return indexEntry;
			}

			public void remove()
			{
				throw new UnsupportedOperationException("Not yet implemented: this is the read only version");
			}
		};
	}
	
}
