/*
 * $Id: DataLinkImpl.java 4552 2008-09-11 11:38:42Z crawley $
 */
package org.jnode.vm.isolate.link;

import java.io.IOException;
import java.io.InterruptedIOException;

import javax.isolate.ClosedLinkException;
import javax.isolate.Isolate;
import javax.isolate.Link;
import javax.isolate.LinkMessage;

final class LinkImpl extends Link {

    private final VmLink vmLink;

    /**
     * Constructor
     *
     * @param vmLink
     */
    LinkImpl(VmLink vmLink) {
        this.vmLink = vmLink;
    }

    final VmLink getImpl() {
        return vmLink;
    }

    /**
     * @see javax.isolate.Link#close()
     */
    @Override
    public void close() {
        vmLink.close();
    }

    /**
     * @see javax.isolate.Link#Equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof LinkImpl) {
            return (((LinkImpl) other).vmLink == this.vmLink);
        }
        return false;
    }

    /**
     * @see javax.isolate.Link#getReceiver()
     */
    @Override
    public Isolate getReceiver() {
        return vmLink.getReceiver().getIsolate();
    }

    /**
     * @see javax.isolate.Link#getSender()
     */
    @Override
    public Isolate getSender() {
        return vmLink.getSender().getIsolate();
    }

    /**
     * @see javax.isolate.Link#isOpen()
     */
    @Override
    public boolean isOpen() {
        return vmLink.isOpen();
    }

    /**
     * @see javax.isolate.Link#receive()
     */
    @Override
    public LinkMessage receive()
        throws ClosedLinkException, IllegalStateException, InterruptedIOException, IOException {
        return vmLink.receive();
    }

    /**
     * @see javax.isolate.Link#send(javax.isolate.LinkMessage)
     */
    @Override
    public void send(LinkMessage message) throws ClosedLinkException, InterruptedIOException, IOException {
        vmLink.send(message);
    }

    /**
     * @see javax.isolate.Link#toString()
     */
    @Override
    public String toString() {
        return vmLink.toString();
    }
}
