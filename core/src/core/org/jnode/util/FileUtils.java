/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * <description>
 *
 * @author epr
 */
public class FileUtils {

    /**
     * Copy dest.length bytes from the inputstream into the dest bytearray.
     *
     * @param is
     * @param dest
     * @throws IOException
     */
    public static void copy(InputStream is, byte[] dest)
        throws IOException {
        int len = dest.length;
        int ofs = 0;
        while (len > 0) {
            int size = is.read(dest, ofs, len);
            ofs += size;
            len -= size;
        }
    }

    /**
     * Copy the contents of is to os.
     *
     * @param is
     * @param os
     * @param buf   Can be null
     * @param close If true, is is closed after the copy.
     * @throws IOException
     */
    public static final void copy(InputStream is, OutputStream os, byte[] buf, boolean close) throws IOException {
        try {
            int len;
            if (buf == null) {
                buf = new byte[4096];
            }
            while ((len = is.read(buf)) > 0) {
                os.write(buf, 0, len);
            }
            os.flush();
        } finally {
            // in any case, we must close the stream if requested
            if (close) {
                is.close();
            }
        }
    }

    /**
     * Copy the contents of is to the returned byte array.
     *
     * @param is
     * @param close If true, is is closed after the copy.
     * @throws IOException
     */
    public static final byte[] load(InputStream is, boolean close) throws IOException {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        copy(is, os, null, close);
        return os.toByteArray();
    }

    /**
     * Copy the contents of is to the returned byte buffer.
     *
     * @param is
     * @param close If true, is is closed after the copy.
     * @throws IOException
     */
    public static final ByteBuffer loadToBuffer(InputStream is, boolean close) throws IOException {
        return ByteBuffer.wrap(load(is, close));
    }

    /**
     * close quietly a stream (no IOException thrown), which might be null
     *
     * @param closeable the stream to close, might be null
     * @return true if the stream was null or was closed properly
     */
    public static final boolean close(Closeable closeable) {
        boolean ok = false;

        try {
            if (closeable != null) {
                closeable.close();
            }

            ok = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ok;
    }

    public static final void copyFile(File srcFile, File destFile) throws IOException {
        InputStream in = null;
        OutputStream out = null;

        try {
            in = new FileInputStream(srcFile);
            out = new FileOutputStream(destFile);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) >= 0) {
                out.write(buffer, 0, bytesRead);
            }
        } finally {
            FileUtils.close(out);
            FileUtils.close(in);
        }
    }

    public static final void copyFile(String srcFileCopy, String destFileCopy, String destFileName) throws IOException {

        // make sure the source file is indeed a readable file
        File srcFile = new File(srcFileCopy);
        if (!srcFile.isFile() || !srcFile.canRead()) {
            throw new IllegalArgumentException("Not a readable file: " + srcFile.getName());
        }

        // make sure the second argument is a directory
        File destDir = new File(destFileCopy);

        // create File object for destination file
        File destFile = new File(destDir, destFileName);

        // copy file, optionally creating a checksum
        copyFile(srcFile, destFile);
    }
}
