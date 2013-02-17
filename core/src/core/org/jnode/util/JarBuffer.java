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

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipException;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class JarBuffer implements JarConstants {

    private final ByteBuffer buffer;

    private final Map<String, ByteBuffer> entries;

    private final Manifest manifest;

    /**
     * Initialize this instance.
     *
     * @param buffer
     * @throws IOException
     * @throws ZipException
     */
    public JarBuffer(ByteBuffer buffer) throws ZipException, IOException {
        this.buffer = buffer;
        this.entries = readEntries();
        this.manifest = readManifest();
    }

    /**
     * Gets a map of jar entries.
     *
     * @return A map between the name and the data of each entry.
     */
    public Map<String, ByteBuffer> entries() {
        return Collections.unmodifiableMap(entries);
    }

    /**
     * Returns the manifest for this JarFile or null when the JarFile does not
     * contain a manifest file.
     */
    public Manifest getManifest() throws IOException {
        return manifest;
    }

    @SuppressWarnings("deprecation")
    private Map<String, ByteBuffer> readEntries() throws ZipException,
        IOException {

        // Start at the beginning
        buffer.rewind();
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        /*
         * Search for the End Of Central Directory. When a zip comment is
         * present the directory may start earlier. FIXME: This searches the
         * whole file in a very slow manner if the file isn't a zip file.
         */
        int pos = buffer.limit() - ENDHDR;
        for (; pos >= 0; pos--) {
            if (buffer.getInt(pos) == ENDSIG) {
                break;
            }
        }
        if (pos < 0) {
            throw new ZipException(
                "central directory not found, probably not a zip file");
        }

        final int count = buffer.getShort(pos + ENDTOT);
//        System.out.println("Count=" + count);
        final int centralOffset = buffer.getInt(pos + ENDOFF);
//        System.out.println("centralOffset=" + centralOffset);

        HashMap<String, ByteBuffer> entries = new HashMap<String, ByteBuffer>(
            count + count / 2);
        buffer.position(centralOffset);

        byte[] strBuf = new byte[16];
        for (int i = 0; i < count; i++) {
            pos = buffer.position();
            buffer.position(buffer.position() + CENHDR);

            if (buffer.getInt(pos + 0) != CENSIG) {
                throw new ZipException("Wrong Central Directory signature "
                    + NumberUtils.hex(buffer.getInt(pos + 0)));
            }

            int method = buffer.getShort(pos + CENHOW);
            int dostime = buffer.getInt(pos + CENTIM);
            int crc = buffer.getInt(pos + CENCRC);
            int csize = buffer.getInt(pos + CENSIZ);
            int size = buffer.getInt(pos + CENLEN);
            int nameLen = buffer.getShort(pos + CENNAM);
            int extraLen = buffer.getShort(pos + CENEXT);
            int commentLen = buffer.getShort(pos + CENCOM);
            int offset = buffer.getInt(pos + CENOFF);

            int needBuffer = Math.max(nameLen, commentLen);
            if (strBuf.length < needBuffer) {
                strBuf = new byte[needBuffer];
            }

            buffer.get(strBuf, 0, nameLen);
            String name = new String(strBuf, 0, 0, nameLen);

            if (extraLen > 0) {
                buffer.position(buffer.position() + extraLen);
            }
            if (commentLen > 0) {
                buffer.get(strBuf, 0, commentLen);
            }

            // Slice of entry data
            final int mark = buffer.position();
            buffer.position(checkLocalHeader(buffer, offset, method));
            final ByteBuffer entry = (ByteBuffer) buffer.slice().limit(size);
            buffer.position(mark);
            entries.put(name, entry);
        }

        return entries;
    }

    private int checkLocalHeader(ByteBuffer buffer, int offset, int method)
        throws IOException {

        if (buffer.getInt(offset + 0) != LOCSIG) {
            throw new ZipException("Wrong Local header signature");
        }

        if (method != buffer.getShort(offset + LOCHOW)) {
            throw new ZipException("Compression method mismatch");
        }

        final int nameLen = buffer.getShort(offset + LOCNAM);
        final int extraLen = buffer.getShort(offset + LOCEXT);
        return offset + LOCHDR + nameLen + extraLen;
    }

    private Manifest readManifest() throws IOException {
        final ByteBuffer buf = entries.get(JarFile.MANIFEST_NAME);
        if (buf == null) {
            return null;
        } else {
            return new Manifest(new ByteBufferInputStream(buf));
        }
    }

    public static void main(String[] args) throws SecurityException,
        IOException {
        FileChannel ch = new FileInputStream(args[0]).getChannel();
        ByteBuffer buf = ch.map(FileChannel.MapMode.READ_ONLY, 0, ch.size());
        JarBuffer jb = new JarBuffer(buf);

        for (Map.Entry<String, ByteBuffer> entry : jb.entries().entrySet()) {
            final ByteBuffer ebuf = entry.getValue();
            if (ebuf.limit() > 0) {
                System.out.println(entry.getKey() + ' ' + ebuf.limit() + " 0x"
                    + NumberUtils.hex(ebuf.getInt(0)));
            }
        }
    }

}
