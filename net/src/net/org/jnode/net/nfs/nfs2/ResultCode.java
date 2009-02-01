/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
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
 
package org.jnode.net.nfs.nfs2;

/**
 * @author Andrei Dore
 */
public enum ResultCode {
    NFS_OK(0), NFS_ERROR_NO_PERM(1), NFS_ERROR_NO_ENTRY(2), NFS_ERROR_IO(5), NFS_ERROR_NXIO(6), 
    NFS_ERROR_ACCESS(13), NFS_ERROR_FILE_ALREADY_EXIST(17), NFS_ERROR_NO_DEVICE(19), 
    NFS_ERROR_NOT_DIRECTORY(20), NFS_ERROR_IS_DIRECTORY(21), NFS_ERROR_FILE_TOO_BIG(27), 
    NFS_ERROR_NO_SPACE(28), NFS_ERROR_READ_ONLY_FS(30), NFS_ERROR_NAME_TOO_LONG(63), 
    NFS_ERROR_DIRECTORY_NOT_EMPTY(66), NFS_ERROR_DISK_QUOTA(69), NFS_ERROR_STALE(70),
    NFS_ERROR_WRITE_FLUSH(99);

    private int code;

    ResultCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ResultCode getResultCode(int code) {
        switch (code) {
            case 0:
                return NFS_OK;
            case 1:
                return NFS_ERROR_NO_PERM;
            case 2:
                return NFS_ERROR_NO_ENTRY;
            case 5:
                return NFS_ERROR_IO;
            case 6:
                return NFS_ERROR_NXIO;
            case 13:
                return NFS_ERROR_ACCESS;
            case 17:
                return NFS_ERROR_FILE_ALREADY_EXIST;
            case 19:
                return NFS_ERROR_NO_DEVICE;
            case 20:
                return NFS_ERROR_NOT_DIRECTORY;
            case 21:
                return NFS_ERROR_IS_DIRECTORY;
            case 27:
                return NFS_ERROR_FILE_TOO_BIG;
            case 28:
                return NFS_ERROR_NO_SPACE;
            case 30:
                return NFS_ERROR_READ_ONLY_FS;
            case 63:
                return NFS_ERROR_NAME_TOO_LONG;
            case 66:
                return NFS_ERROR_DIRECTORY_NOT_EMPTY;
            case 69:
                return NFS_ERROR_DISK_QUOTA;
            case 70:
                return NFS_ERROR_STALE;
            case 99:
                return NFS_ERROR_WRITE_FLUSH;
            default:
                throw new IllegalArgumentException("Code  unlnown : " + code);
        }
    }
}
