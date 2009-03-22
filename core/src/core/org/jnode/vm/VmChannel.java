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
 
package org.jnode.vm;

import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;

/**
 * Native interface to support configuring of channel to run in a non-blocking
 * manner and support scatter/gather io operations.
 * <p/>
 * <p/>
 * This has to give the functionality of the classpath/native/jni/java-nio/gnu_java_nio_VMChannel.c
 *
 */
public final class VmChannel {

    public static int stdin_fd() throws IOException {
        // shouldn't throw IOException
        throw new IOException("Not implemented");
    }

    public static int stdout_fd() throws IOException {
        // shouldn't throw IOException
        throw new IOException("Not implemented");
    }

    public static int stderr_fd() throws IOException {
        // shouldn't throw IOException
        throw new IOException("Not implemented");
    }

    public static void setBlocking(int fd, boolean blocking) throws IOException {
        throw new IOException("Not implemented");
    }

    public static int available(int native_fd) throws IOException {
        throw new IOException("Not implemented");
    }

    public static int read(int fd, ByteBuffer dst) throws IOException {
        throw new IOException("Not implemented");
    }

    public static int read(int fd) throws IOException {
        throw new IOException("Not implemented");
    }

    public static long readScattering(int fd, ByteBuffer[] dsts, int offset, int length) throws IOException {
        throw new IOException("Not implemented");
    }


    public static int receive(int fd, ByteBuffer dst, ByteBuffer address) throws IOException {
        throw new IOException("Not implemented");
    }

    public static int write(int fd, ByteBuffer src) throws IOException {
        throw new IOException("Not implemented");
    }

    public static long writeGathering(int fd, ByteBuffer[] srcs, int offset, int length) throws IOException {
        throw new IOException("Not implemented");
    }

    // Send to an IPv4 address.
    public static int send(int fd, ByteBuffer src, byte[] addr, int port) throws IOException {
        throw new IOException("Not implemented");
    }

    // Send to an IPv6 address.
    public static int send6(int fd, ByteBuffer src, byte[] addr, int port) throws IOException {
        throw new IOException("Not implemented");
    }

    public static void write(int fd, int b) throws IOException {
        throw new IOException("Not implemented");
    }

    public static void initIDs() throws IOException {
        // shouldn't throw IOException
        throw new IOException("Not implemented");
    }

    // Network (socket) specific methods.

    /**
     * Create a new socket, returning the native file descriptor.
     *
     * @param stream Set to true for streaming sockets{} false for datagrams.
     * @return The native file descriptor.
     * @throws java.io.IOException If creating the socket fails.
     */
    public static int socket(boolean stream) throws IOException {
        throw new IOException("Not implemented");
    }


    public static boolean connect(int fd, byte[] addr, int port, int timeout) throws SocketException {
        throw new SocketException("Not implemented");
    }

    public static boolean connect6(int fd, byte[] addr, int port, int timeout) throws SocketException {
        throw new SocketException("Not implemented");
    }

    public static void disconnect(int fd) throws IOException {
        throw new IOException("Not implemented");
    }

    public static int getsockname(int fd, ByteBuffer name) throws IOException {
        throw new IOException("Not implemented");
    }

    /*
    * The format here is the peer address, followed by the port number.
    * The returned value is the length of the peer address{} thus, there
    * will be LEN + 2 valid bytes put into NAME.
    */
    public static int getpeername(int fd, ByteBuffer name) throws IOException {
        throw new IOException("Not implemented");
    }

    public static int accept(int native_fd) throws IOException {
        throw new IOException("Not implemented");
    }


    public static int open(String path, int mode) throws IOException {
        throw new IOException("Not implemented");
    }

    public static long position(int fd) throws IOException {
        throw new IOException("Not implemented");
    }

    public static void seek(int fd, long pos) throws IOException {
        throw new IOException("Not implemented");
    }

    public static void truncate(int fd, long len) throws IOException {
        throw new IOException("Not implemented");
    }

    public static boolean lock(int fd, long pos, long len, boolean shared, boolean wait) throws IOException {
        throw new IOException("Not implemented");
    }

    public static void unlock(int fd, long pos, long len) throws IOException {
        throw new IOException("Not implemented");
    }

    public static long size(int fd) throws IOException {
        throw new IOException("Not implemented");
    }

    public static MappedByteBuffer map(int fd, char mode, long position, int size) throws IOException {
        throw new IOException("Not implemented");
    }

    public static boolean flush(int fd, boolean metadata) throws IOException {
        throw new IOException("Not implemented");
    }

    public static void close(int native_fd) throws IOException {
        throw new IOException("Not implemented");
    }
}
