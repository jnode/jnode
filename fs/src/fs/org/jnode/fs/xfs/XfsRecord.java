package org.jnode.fs.xfs;

/**
 * A base class for XFS records.
 *
 * @author Luke Quinane.
 */
public abstract class XfsRecord extends XfsObject {

    /**
     * Creates a new record.
     */
    protected XfsRecord() {
    }

    /**
     * Creates a new record.
     *
     * @param data the data.
     * @param offset the offset to this record.
     */
    protected XfsRecord(byte[] data, int offset) {
        super(data, offset);
    }

    /**
     * Gets the magic number stored in this record.
     *
     * @return the magic.
     */
    public long getMagic() {
        return getUInt32(0x0);
    }
}
