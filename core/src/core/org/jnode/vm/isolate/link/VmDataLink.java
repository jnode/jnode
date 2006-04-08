/*
 * $Id$
 */
package org.jnode.vm.isolate.link;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.LinkedList;
import java.util.Queue;

import javax.isolate.ClosedLinkException;
import javax.isolate.Link;
import javax.isolate.LinkMessage;

import org.jnode.vm.isolate.VmIsolate;
import org.jnode.vm.isolate.VmIsolateLocal;

/**
 * Shared implementation of javax.isolate.Link
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VmDataLink {

    private final VmIsolateLocal<DataLinkImpl> linkHolder = new VmIsolateLocal<DataLinkImpl>();

    private final Queue<LinkMessageImpl> messages = new LinkedList<LinkMessageImpl>();

    private boolean closed = false;

    private VmIsolate sender;

    private VmIsolate receiver;

    /**
     * Create a new data link between the given isolates.
     * 
     * @param sender
     * @param receiver
     * @return
     */
    public static Link newLink(VmIsolate sender, VmIsolate receiver) {
        if (sender == receiver) {
            throw new IllegalArgumentException("sender == receiver");
        }
        VmDataLink vmLink = new VmDataLink(sender, receiver);
        return vmLink.asLink();
    }

    public static VmDataLink fromLink(Link link) {
        return ((DataLinkImpl)link).getImpl();
    }
    
    /**
     * @param sender
     * @param receiver
     */
    VmDataLink(VmIsolate sender, VmIsolate receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }

    /**
     * Gets this shared link as Link instance.
     * 
     * @return
     */
    public final Link asLink() {
        final DataLinkImpl link = linkHolder.get();
        if (link == null) {
            linkHolder.set(new DataLinkImpl(this));
            return linkHolder.get();
        } else {
            return link;
        }
    }

    /**
     * Close this link.
     */
    final void close() {
        if (!this.closed) {
            final VmIsolate current = VmIsolate.currentIsolate();
            if ((current != receiver) && (current != sender)) {
                throw new IllegalStateException(
                        "Only sender or receiver can close this link");
            }
            this.closed = true;
            synchronized (this) {
                notifyAll();
            }
        }
    }

    /**
     * Is this link currently open.
     * 
     * @return
     */
    final boolean isOpen() {
        return !closed;
    }

    /**
     * @return the receiver
     */
    final VmIsolate getReceiver() {
        return receiver;
    }

    /**
     * @return the sender
     */
    final VmIsolate getSender() {
        return sender;
    }

    /**
     * Receives a copy of a message sent on this Link.
     * 
     * The current thread will block in this method until a sender is available.
     * When the sender and receiver rendezvous, the message will then be
     * transferred. If multiple threads invoke this method on the same object,
     * only one thread will receive any message at rendezvous and the other
     * threads will contend for subsequent access to the rendezvous point. A
     * normal return indicates that the message was received successfully. If an
     * exception occured on the sender side, no rendezvous will occur, and no
     * object will be transferred; the receiver will wait for the next
     * successful send. If an exception occurs on the receive, the sender will
     * see a successful transfer.
     * 
     * This method never returns null.
     * 
     * If the sending isolate becomes terminated after this method is invoked
     * but before it returns, the link will be closed, a ClosedLinkException
     * will be thrown, any subsequent attempts to use receive() will result in a
     * ClosedLinkException, and any Isolate objects corresponding to the receive
     * side of the link will reflect a terminated state.
     * 
     * If invoked on a closed Link, this method will throw a
     * ClosedLinkException.
     * 
     * If close() is invoked on the link while a thread is blocked in receive(),
     * receive() will throw a ClosedLinkException. No message will have been
     * transferred in that case.
     * 
     * If Thread.interrupt() is invoked on a thread that has not yet completed
     * an invocation of this method, the results are undefined. An
     * InterruptedIOException may or may not be thrown; if not, control will
     * return from the method with a valid LinkMessage object. However, even if
     * InterruptedIOException is thrown, rendezvous and data transfer from the
     * sending isolate may have occurred. In particular, undetected message loss
     * between sender and receiver may occur.
     * 
     * If a failure occurs due to the object being transferred between isolates
     * an IOException may be thrown in the receiver. For example, if a message
     * containing a large buffer is sent and the receiver has insufficient heap
     * memory for the buffer or if construction of a link in the receiver
     * isolate fails, an IOException will be thrown. The sender will see a
     * successful transfer in these cases.
     * 
     * If the current isolate is not a receiver on this Link an
     * IllegalStateException will be thrown. The receiver will not rendezvous
     * with a sender in this case.
     * 
     * @return
     */
    final LinkMessage receive() throws ClosedLinkException,
            IllegalStateException, InterruptedIOException, IOException {
        if (VmIsolate.currentIsolate() != receiver) {
            // Current isolate is not the receiver
            throw new IllegalStateException();
        }
        if (this.closed) {
            throw new ClosedLinkException();
        }
        final LinkMessageImpl message;
        synchronized (this) {
            while (messages.isEmpty()) {
                if (this.closed) {
                    throw new ClosedLinkException();
                }
                try {
                    wait();
                } catch (InterruptedException ex) {
                    throw new InterruptedIOException();
                }
            }
            message = messages.poll();
            notifyAll();
        }
        return message.CloneMessage();
    }

    /**
     * Sends the given message on this Link.
     * 
     * The current thread will block in this method until a receiver is
     * available. When the sender and receiver rendezvous, the message will then
     * be transferred. A normal return indicates that the message was
     * transferred. If an exception occurs on the sender side, no rendezvous
     * will occur and no object will be transferred. But if an exception occurs
     * on the receive(), the sender will see a successful transfer.
     * 
     * If the receiving isolate becomes terminated after this method is invoked
     * but before it returns, the link will be closed, a ClosedLinkException
     * will be thrown, any subsequent attempts to use send() will result in a
     * ClosedLinkException, and any Isolate objects corresponding to the receive
     * side of the link will reflect a terminated state.
     * 
     * If invoked on a closed link, this method will throw a
     * ClosedLinkException.
     * 
     * If close() is invoked on this Link while a thread is blocked in send(),
     * send() will throw a ClosedLinkException. No message will have been
     * transferred in that case.
     * 
     * If Thread.interrupt() is invoked on a thread that has not yet completed
     * an invocation of this method, the results are undefined. An
     * InterruptedIOException may or may not be thrown; however, even if not,
     * control will return from the method. Rendezvous and data transfer to the
     * receiving isolate may or may not have occurred. In particular, undetected
     * message loss between sender and receiver may occur.
     * 
     * If a failure occurs during the transfer an IOException may be thrown in
     * the sender and the object will not be sent. A transfer may succeed from
     * the sender's point of view, but cause an independent IOException in the
     * receiver. For example, if a message containing a large buffer is sent and
     * the receiver has insufficient heap memory for the buffer or if
     * construction of a link in the receiver isolate fails, an IOException will
     * be thrown in the receiver after the transfer completes.
     * 
     * A ClosedLinkException will be thrown if the given LinkMessage contains a
     * closed Link, Socket, or ServerSocket. No object will be transferred in
     * this case.
     * 
     * If the current isolate is not a sender on this Link, an
     * UnsupportedOperationException will be thrown. No object will be sent in
     * this case.
     * 
     * @param message
     * @throws ClosedLinkException
     * @throws InterruptedIOException
     * @throws IOException
     */
    final void send(LinkMessage message) throws ClosedLinkException,
            InterruptedIOException, IOException {
        if (VmIsolate.currentIsolate() != sender) {
            // Current isolate is not a sender
            throw new UnsupportedOperationException();
        }
        if (this.closed) {
            throw new ClosedLinkException();
        }
        final LinkMessageImpl messageImpl = (LinkMessageImpl)message;
        synchronized (this) {
            if (this.closed) {
                throw new ClosedLinkException();
            }
            // Send message
            messages.add(messageImpl);
            // Wait for the message to be picked up by the receiver
            while (messages.contains(messageImpl) && !closed) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    throw new InterruptedIOException();
                }
            }
        }
    }
}
