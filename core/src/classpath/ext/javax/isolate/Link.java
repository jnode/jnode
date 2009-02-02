/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package javax.isolate;

import java.io.IOException;
import java.io.InterruptedIOException;

import org.jnode.vm.isolate.VmLink;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class Link {

    /**
     * Returns the isolate that can receive on this link or null if it is not
     * possible to determine a unique receiver.
     * 
     * @return
     */
    public abstract Isolate getReceiver();

    /**
     * Returns the isolate that can send on this link.
     * 
     * @return
     */
    public abstract Isolate getSender();

    /**
     * Closes this Link, disabling both sending and receiving on it. Invocations
     * of send() or receive() on a closed Link will result in a
     * ClosedLinkException. For any send() or receive() invocations that are
     * active in other threads when close() is invoked, those invocations will
     * either complete successfully on both sides, or both sides will see a
     * ClosedLinkException. If the invoking isolate is neither a sender nor a
     * receiver on this Link, an IllegalStateException will be thrown, and the
     * Link will not be closed. If this Link is already closed or is in the
     * process of closing, invoking this method has no effect.
     */
    public void close() {
    }

    /**
     * Tests to see if this Link is open.
     * 
     * A data link is open from the point of its creation until the earliest
     * occurrence of at least one of its associated isolates closing it, or of
     * at least one of its associated Isolates terminating.
     * 
     * A status link is open from the point of its creation until the earliest
     * occurrence of its receiving isolate closing it, of its receiving isolate
     * terminating, or of the receipt of a message containing a
     * IsolateStatus.State object with value EXITED.
     * 
     * This method throws IllegalStateException if the invoking isolate is
     * neither a sender nor a receiver for this link.
     * 
     * @return
     */
    public boolean isOpen() {
        return false;
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
    public LinkMessage receive() throws ClosedLinkException,
            IllegalStateException, InterruptedIOException, IOException {
        return null;
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
    public void send(LinkMessage message) throws ClosedLinkException,
            InterruptedIOException, IOException {

    }

    /**
     * Tests this Link for equality with the given object. Returns true if and
     * only if other is not null and denotes the same link as this, with respect
     * to rendezvous points.
     * 
     * @param other
     * @return
     */
    public boolean equals(Object other) {
        return super.equals(other);
    }

    /**
     * Returns a description of this link. It includes information returned by
     * getSender().toString() and getReceiver().toString().
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return super.toString();
    }

    /**
     * Creates a new data link between the given pair of Isolate instances. The
     * sender and the receiver must not be equal.
     * 
     * The links that this method returns support the optional getSender() and
     * getReceiver() methods.
     * 
     * @param sender
     * @param receiver
     * @return
     * @throws ClosedLinkException
     */
    public static Link newLink(Isolate sender, Isolate receiver)
            throws ClosedLinkException {
        return VmLink.newLink(sender.getImpl(), receiver.getImpl());
    }
}
