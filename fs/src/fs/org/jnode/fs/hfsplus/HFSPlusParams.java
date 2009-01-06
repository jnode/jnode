package org.jnode.fs.hfsplus;

public class HFSPlusParams {
    private String volumeName;
    private int blockSize;
    private boolean journaled;
    private int journalSize;
    
    public String getVolumeName() {
        return volumeName;
    }
    public void setVolumeName(String volumeName) {
        this.volumeName = volumeName;
    }
    public int getBlockSize() {
        return blockSize;
    }
    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }
    public boolean isJournaled() {
        return journaled;
    }
    public void setJournaled(boolean journaled) {
        this.journaled = journaled;
    }
    public int getJournalSize() {
        return journalSize;
    }
    public void setJournalSize(int journalSize) {
        this.journalSize = journalSize;
    }
}
