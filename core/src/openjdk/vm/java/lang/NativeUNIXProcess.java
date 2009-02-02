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
 
package java.lang;

import java.io.FileDescriptor;

/**
 * @see java.lang.UNIXProcess
 * @author Levente S\u00e1ntha
 */
class NativeUNIXProcess {
    /**
     * @see java.lang.UNIXProcess#waitForProcessExit(int)
     */
    private static int waitForProcessExit(UNIXProcess instance, int arg1) {
        //todo implement it
        //throw new UnsupportedOperationException();
        return -1;
    }
    /**
     * @see java.lang.UNIXProcess#forkAndExec(byte[], byte[], int, byte[], int, byte[], boolean, java.io.FileDescriptor, java.io.FileDescriptor, java.io.FileDescriptor)
     */
    private static int forkAndExec(UNIXProcess instance, byte[] arg1, byte[] arg2, int arg3, byte[] arg4, int arg5, byte[] arg6, boolean arg7, FileDescriptor arg8, FileDescriptor arg9, FileDescriptor arg10) {
        //todo implement it
        //throw new UnsupportedOperationException();
        return -1;
    }
    /**
     * @see java.lang.UNIXProcess#destroyProcess(int)
     */
    private static void destroyProcess(int arg1) {
        //todo implement it
        //throw new UnsupportedOperationException();
    }
    /**
     * @see java.lang.UNIXProcess#initIDs()
     */
    private static void initIDs() {
        
    }
}
