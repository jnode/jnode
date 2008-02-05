package org.jnode.net.nfs.nfs2;

public class FileSystemAttribute {

    private long transferSize;

    private long blockSize;

    private long blockCount;

    private long freeBlockCount;

    private long availableBlockCount;

    public long getTransferSize() {
        return transferSize;
    }

    public void setTransferSize(long transferSize) {
        this.transferSize = transferSize;
    }

    public long getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(long blockSize) {
        this.blockSize = blockSize;
    }

    public long getBlockCount() {
        return blockCount;
    }

    public void setBlockCount(long blockCount) {
        this.blockCount = blockCount;
    }

    public long getFreeBlockCount() {
        return freeBlockCount;
    }

    public void setFreeBlockCount(long freeBlockCount) {
        this.freeBlockCount = freeBlockCount;
    }

    public long getAvailableBlockCount() {
        return availableBlockCount;
    }

    public void setAvailableBlockCount(long availableBlockCount) {
        this.availableBlockCount = availableBlockCount;
    }

    @Override
    public String toString() {

        StringBuffer buffer = new StringBuffer();
        buffer.append("FileSystemAttribute ");
        buffer.append("transferSize:");
        buffer.append(transferSize);
        buffer.append(";blockSize:");
        buffer.append(blockSize);
        buffer.append(";blockCount:");
        buffer.append(blockCount);
        buffer.append(";freeBlockCount:");
        buffer.append(freeBlockCount);
        return buffer.toString();
    }

}
