package org.jnode.fs.hfsplus.tree;

public interface NodeRecord {
    
    /** 
     * Key identify the record 
     */
    public Key getKey();
    
    /** 
     * Get record data as byte array. 
     */
    public byte[] getData();
    
    /**  
     * Get node record size
     */
    public int getSize();
    
    /** 
     * Get node record as byte array 
     */
    public byte[] getBytes();
    
}
