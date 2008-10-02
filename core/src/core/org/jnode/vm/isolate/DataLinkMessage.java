/*
 * $Id: DataLinkMessage.java 4595 2008-10-02 13:24:26Z crawley $
 */
package org.jnode.vm.isolate;


final class DataLinkMessage extends LinkMessageImpl {

    private final byte[] bytes;

    private final int offset;

    private final int length;

    public DataLinkMessage(byte[] bytes, int offset, int length) {
        this.bytes = bytes;
        this.offset = offset;
        this.length = length;
    }

    /**
     * @see org.jnode.vm.isolate.LinkMessageImpl#CloneMessage()
     */
    @Override
    LinkMessageImpl cloneMessage() {
        final byte[] data = new byte[length];
        System.arraycopy(bytes, offset, data, 0, length);
        return new DataLinkMessage(data, 0, length);
    }

    /**
     * @see javax.isolate.LinkMessage#containsData()
     */
    @Override
    public boolean containsData() {
        return true;
    }

    /**
     * @see javax.isolate.LinkMessage#extract()
     */
    @Override
    public Object extract() {
        return extractData();
    }

    /**
     * @see javax.isolate.LinkMessage#extractData()
     */
    @Override
    public byte[] extractData() {
        if ((offset == 0) && (length == bytes.length)) {
            return bytes;
        } else {
            byte[] data = new byte[length];
            System.arraycopy(bytes, offset, data, 0, length);
            return data;
        }
    }
}
