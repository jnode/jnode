package org.jnode.fs.ext2;

/**
 * @author Andras Nagy
 */
public class INodeReservation {
	/**
	 * The block group that contains the inode
	 */
	private int group;
	/**
	 * The index of the inode within the block group (begins with 0)
	 */
	private int index;
	private boolean successful;
	
	/**
	 * Results of an attempt to reserve an inode in a block group.
	 * @param successful
	 * @param index: begins at index 0 (shows the index in the inode bitmap and inode table).
	 * 				 The actual inode number is <code>INODEX_PER_GROUP*groupNr + index + 1</code>, 
	 * 				 as inodes begin at 1 (this is what getInodeNr(groupNr) returns)
	 */
	public INodeReservation(boolean successful, int index) {
		this.successful = successful;
		this.index = index;
	}

	public int getINodeNr(int iNodesPerGroup) {
		//iNodes start with 1
		return iNodesPerGroup*group + index + 1;
	}

	/**
	 * Returns the successful.
	 * @return boolean
	 */
	protected boolean isSuccessful() {
		return successful;
	}
	/**
	 * @return
	 */
	public int getGroup() {
		return group;
	}

	/**
	 * @param l
	 */
	public void setGroup(int l) {
		group = l;
	}

	/**
	 * @return
	 */
	public int getIndex() {
		return index;
	}

}
