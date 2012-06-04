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
 * @see java.net.PlainSocketImpl
 */
class NativePlainSocketImpl {
    /**
     * @see java.net.PlainSocketImpl#socketCreate(boolean)
     */
    private static void socketCreate(PlainSocketImpl instance, boolean arg1) {
        //todo implement it
        throw new UnsupportedOperationException();
    }
    /**
     * @see java.net.PlainSocketImpl#socketConnect(java.net.InetAddress, int, int)
     */
    private static void socketConnect(PlainSocketImpl instance, InetAddress arg1, int arg2, int arg3) {
        //todo implement it
        throw new UnsupportedOperationException();
    }
    /**
     * @see java.net.PlainSocketImpl#socketBind(java.net.InetAddress, int)
     */
    private static void socketBind(PlainSocketImpl instance, InetAddress arg1, int arg2) {
        //todo implement it
        throw new UnsupportedOperationException();
    }
    /**
     * @see java.net.PlainSocketImpl#socketListen(int)
     */
    private static void socketListen(PlainSocketImpl instance, int arg1) {
        //todo implement it
        throw new UnsupportedOperationException();
    }
    /**
     * @see java.net.PlainSocketImpl#socketAccept(java.net.SocketImpl)
     */
    private static void socketAccept(PlainSocketImpl instance, SocketImpl arg1) {
        //todo implement it
        throw new UnsupportedOperationException();
    }
    /**
     * @see java.net.PlainSocketImpl#socketAvailable()
     */
    private static int socketAvailable(PlainSocketImpl instance) {
        //todo implement it
        //return 0;
        throw new UnsupportedOperationException();
    }
    /**
     * @see java.net.PlainSocketImpl#socketClose0(boolean)
     */
    private static void socketClose0(PlainSocketImpl instance, boolean arg1) {
        //todo implement it
        throw new UnsupportedOperationException();
    }
    /**
     * @see java.net.PlainSocketImpl#socketShutdown(int)
     */
    private static void socketShutdown(PlainSocketImpl instance, int arg1) {
        //todo implement it
        throw new UnsupportedOperationException();
    }
    /**
     * @see java.net.PlainSocketImpl#initProto()
     */
    private static void initProto() {
        //todo implement it
        throw new UnsupportedOperationException();
    }
    /**
     * @see java.net.PlainSocketImpl#socketSetOption(int, boolean, java.lang.Object)
     */
    private static void socketSetOption(PlainSocketImpl instance, int arg1, boolean arg2, Object arg3) {
        //todo implement it
        throw new UnsupportedOperationException();
    }
    /**
     * @see java.net.PlainSocketImpl#socketGetOption(int, java.lang.Object)
     */
    private static int socketGetOption(PlainSocketImpl instance, int arg1, Object arg2) {
        //todo implement it
        //return 0;
        throw new UnsupportedOperationException();
    }
    /**
     * @see java.net.PlainSocketImpl#socketGetOption1(int, java.lang.Object, java.io.FileDescriptor)
     */
    private static int socketGetOption1(PlainSocketImpl instance, int arg1, Object arg2, FileDescriptor arg3) {
        //todo implement it
        //return 0;
        throw new UnsupportedOperationException();
    }
    /**
     * @see java.net.PlainSocketImpl#socketSendUrgentData(int)
     */
    private static void socketSendUrgentData(PlainSocketImpl instance, int arg1) {
        //todo implement it
        throw new UnsupportedOperationException();
    }
}
