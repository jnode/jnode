/*
 * $Id$
 *
 * JNode.org
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
 
package org.jnode.net.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocketImpl;
import java.net.ExSocketOptions;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceNotFoundException;
import org.jnode.driver.DeviceUtils;
import org.jnode.net.SocketBuffer;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.system.BootLog;
import org.jnode.util.Queue;

/**
 * @author epr
 */
public abstract class AbstractDatagramSocketImpl extends DatagramSocketImpl implements
        ExSocketOptions {

    /** The receive queue of SocketBuffer instances */
    private final Queue<SocketBuffer> receiveQueue = new Queue<SocketBuffer>();
    
    /** Have I been closed? */
    private boolean closed;
    
    /** Time to live */
    private int ttl = 0xFF;
    
    /** Type of service */
    private int tos = 0;
    
    /** Timeout of network operations */
    private int timeout = 0;
    
    /** Local address */
    private InetAddress laddr;
    
    /** Send using broadcast addresses? */
    private boolean broadcast = true;
    
    /** Device used for transmission (can be null) */
    private Device device;

    /**
     * Create a new instance
     */
    public AbstractDatagramSocketImpl() {
        this.closed = false;
    }

    /**
     * @see java.net.DatagramSocketImpl#bind(int, java.net.InetAddress)
     */
    protected final synchronized void bind(int lport, InetAddress laddr) throws SocketException {
        this.localPort = lport;
        this.laddr = laddr;
        doBind(lport, laddr);
    }

    protected abstract void doBind(int lport, InetAddress laddr) throws SocketException;

    /**
     * @see java.net.DatagramSocketImpl#close()
     */
    protected final synchronized void close() {
        if (!closed) {
            this.closed = true;
            doClose();
            receiveQueue.close();
        }
    }

    protected abstract void doClose();

    /**
     * @see java.net.DatagramSocketImpl#create()
     */
    protected void create() throws SocketException {
        // Nothing todo here
    }

    /**
     * @see java.net.SocketOptions#getOption(int)
     */
    public final synchronized Object getOption(int option_id) throws SocketException {
        if (closed) {
            throw new SocketException("DatagramSocket closed");
        }
        switch (option_id) {
            case IP_TOS:
                return new Integer(tos);
            case SO_BINDADDR:
                return laddr;
            case SO_BROADCAST:
                return new Boolean(broadcast);
            case SO_RCVBUF:
                return new Integer(EthernetConstants.ETH_FRAME_LEN);
            case SO_SNDBUF:
                return new Integer(EthernetConstants.ETH_FRAME_LEN);
            case SO_TRANSMIT_IF:
                return (device == null) ? null : NetworkInterface.getByName(device.getId());
            case SO_TIMEOUT:
                return new Integer(timeout);
            default:
                return doGetOption(option_id);
        }
    }

    protected Object doGetOption(int option_id) throws SocketException {
        throw new SocketException("Unknown option " + option_id);
    }

    /**
     * @see java.net.SocketOptions#setOption(int, java.lang.Object)
     */
    public final synchronized void setOption(int option_id, Object val) throws SocketException {
        if (closed) {
            throw new SocketException("DatagramSocket closed");
        }
        try {
            switch (option_id) {
                case IP_TOS:
                    tos = ((Integer) val).intValue();
                    break;
                case SO_BINDADDR:
                    throw new SocketException("Get only option: SO_BINDADDR");
                case SO_BROADCAST:
                    broadcast = ((Boolean) val).booleanValue();
                    break;
                case SO_RCVBUF: /* ignore */
                    break;
                case SO_SNDBUF: /* ignore */
                    break;
                case SO_TRANSMIT_IF: 
                    if (val == null) {
                        device = null;
                    } else {
                        final NetworkInterface netIf = (NetworkInterface) val;
                        try {
                            device = DeviceUtils.getDevice(netIf.getName());
                        } catch (DeviceNotFoundException ex) {
                            throw new SocketException("Unknown networkinterface " + netIf.getName());
                        }
                    }
                    break;
                case SO_TIMEOUT:
                    timeout = ((Integer) val).intValue();
                    break;
                case SO_REUSEADDR:
                    // Ignored for now
                    break;
                default:
                    doSetOption(option_id, val);
            }
        } catch (ClassCastException ex) {
            throw (SocketException) new SocketException("Invalid option type").initCause(ex);
        }
    }

    protected void doSetOption(int option_id, Object val) throws SocketException {
        BootLog.error("Unknown option " + option_id);
    }

    /**
     * @see java.net.DatagramSocketImpl#getTimeToLive()
     */
    protected final int getTimeToLive() throws IOException {
        return ttl;
    }

    /**
     * @see java.net.DatagramSocketImpl#join(java.net.InetAddress)
     */
    protected void join(InetAddress inetaddr) throws IOException {
        // TODO Auto-generated method stub
    }

    /**
     * @see java.net.DatagramSocketImpl#joinGroup(java.net.SocketAddress,
     *      java.net.NetworkInterface)
     */
    protected void joinGroup(SocketAddress mcastaddr, NetworkInterface netIf) throws IOException {
        // TODO Auto-generated method stub
    }

    /**
     * @see java.net.DatagramSocketImpl#leave(java.net.InetAddress)
     */
    protected void leave(InetAddress inetaddr) throws IOException {
        // TODO Auto-generated method stub
    }

    /**
     * @see java.net.DatagramSocketImpl#leaveGroup(java.net.SocketAddress,
     *      java.net.NetworkInterface)
     */
    protected void leaveGroup(SocketAddress mcastaddr, NetworkInterface netIf) throws IOException {
        // TODO Auto-generated method stub
    }

    /**
     * @see java.net.DatagramSocketImpl#peek(java.net.InetAddress)
     */
    protected int peek(InetAddress i) throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @see java.net.DatagramSocketImpl#peekData(java.net.DatagramPacket)
     */
    protected int peekData(DatagramPacket p) throws IOException {
        throw new IOException("Not implemented");
    }

    /**
     * @see java.net.DatagramSocketImpl#receive(java.net.DatagramPacket)
     */
    protected final void receive(DatagramPacket p) throws IOException {
        if (closed) {
            throw new SocketException("DatagramSocket has been closed");
        }
        final SocketBuffer skbuf = (SocketBuffer) receiveQueue.get(timeout);
        if (skbuf == null) {
            if (closed) {
                throw new SocketException("DatagramSocket has been closed");
            } else {
                throw new SocketTimeoutException("Timeout in receive");
            }
        } else {
            onReceive(p, skbuf);
        }
    }

    protected abstract void onReceive(DatagramPacket p, SocketBuffer skbuf) throws IOException;

    /**
     * Deliver a packet to this socket. This will put the packet in the
     * receive queue if this socket has not been closed.
     * @param skbuf
     */
    public final boolean deliverReceived(SocketBuffer skbuf) {
        if (!closed) {
            receiveQueue.add(skbuf);
            return true;
        } else {
            return false;
        }
    }

    /**
     * @see java.net.DatagramSocketImpl#setTimeToLive(int)
     */
    protected final void setTimeToLive(int ttl) {
        this.ttl = ttl;
    }

    /**
     * Gets the local port of this socket 
     * @see java.net.DatagramSocketImpl#getLocalPort()
     */
    public final int getLocalPort() {
        return super.getLocalPort();
    }

    /**
     * Gets the local port of this socket 
     */
    public final InetAddress getLocalAddress() {
        return laddr;
    }

    /**
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    /**
     * Gets the device used to send/receive packets.
     */
    protected Device getDevice() {
        return device;
    }

    /**
     * Gets the timeout used in receive
     */
    protected int getTimeout() {
        return timeout;
    }

    /**
     * Gets the Type of Service, used in send
     */
    protected int getTos() {
        return tos;
    }
}
