/*
 * $Id: header.txt 5714 2010-01-03 13:33:07Z lsantha $
 *
 * Copyright (C) 2003-2012 JNode.org
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
 
package java.net;

import java.io.FileDescriptor;

/**
 * @see java.net.SocketInputStream
 */
class NativeSocketInputStream {
    /**
     * @see java.net.SocketInputStream#socketRead0(java.io.FileDescriptor, byte[], int, int, int)
     */
    private static int socketRead0(SocketInputStream instance, FileDescriptor arg1, byte[] arg2, int arg3, int arg4, int arg5) {
        //todo implement it
        //return 0;
        throw new UnsupportedOperationException();
    }
    /**
     * @see java.net.SocketInputStream#init()
     */
    private static void init() {
        //todo implement it
        throw new UnsupportedOperationException();
    }
}
