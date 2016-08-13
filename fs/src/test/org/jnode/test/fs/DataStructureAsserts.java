/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
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
 
package org.jnode.test.fs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;

import jcifs.util.Hexdump;

import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.jnode.fs.FSFileStreams;
import org.jnode.fs.FileSystem;
import org.junit.Assert;

/**
 * Data structure asserts for file system tests.
 */
public class DataStructureAsserts {

    private DataStructureAsserts() {
        // Prevent instantiation
    }

    /**
     * Asserts the data structure for a file system.
     *
     * @param fileSystem the file system to assert.
     * @param expected the expected structure.
     * @throws IOException if an error occurs.
     */
    public static void assertStructure(FileSystem fileSystem, String expected) throws IOException {
        StringBuilder actual = new StringBuilder(expected.length());

        actual.append(String.format("type: %s vol:%s total:%d free:%d\n",
            fileSystem.getType().getName(), fileSystem.getVolumeName(),
            fileSystem.getTotalSpace(), fileSystem.getFreeSpace()));

        FSEntry entry = fileSystem.getRootEntry();
        buildStructure(entry, actual, "  ");

        Assert.assertEquals("Wrong structure", expected, actual.toString());
    }

    /**
     * Asserts the data structure for a file entry.
     *
     * @param entry the entry to assert.
     * @param expected the expected structure.
     * @throws IOException if an error occurs.
     */
    public static void assertStructure(FSEntry entry, String expected) throws IOException {
        StringBuilder actual = new StringBuilder(expected.length());
        buildStructure(entry, actual, "");
        Assert.assertEquals("Wrong structure", expected, actual.toString());
    }

    /**
     * Builds up the structure for the given file system entry.
     *
     * @param entry the entry to process.
     * @param actual the string to append to.
     * @param indent the indent level.
     * @throws IOException if an error occurs.
     */
    public static void buildStructure(FSEntry entry, StringBuilder actual, String indent) throws IOException {
        actual.append(indent);
        actual.append(entry.getName());
        actual.append("; ");

        if (entry.isFile()) {
            FSFile file = entry.getFile();
            actual.append(file.getLength());
            actual.append("; ");
            actual.append(getMD5Digest(file));
            actual.append("\n");

            if (file instanceof FSFileStreams) {
                Map<String, FSFile> streams = ((FSFileStreams) file).getStreams();

                for (Map.Entry<String, FSFile> streamEntry : streams.entrySet()) {
                    actual.append(indent);
                    actual.append(entry.getName());
                    actual.append(":");
                    actual.append(streamEntry.getKey());
                    actual.append("; ");
                    actual.append(streamEntry.getValue().getLength());
                    actual.append("; ");
                    actual.append(getMD5Digest(streamEntry.getValue()));
                    actual.append("\n");
                }
            }

        } else {
            actual.append("\n");

            FSDirectory directory = entry.getDirectory();
            Iterator<? extends FSEntry> iterator = directory.iterator();

            while (iterator.hasNext()) {
                FSEntry child = iterator.next();

                if (".".equals(child.getName()) || "..".equals(child.getName())) {
                    continue;
                }

                buildStructure(child, actual, indent + "  ");
            }
        }
    }

    /**
     * Gets the MD5 string for the contents of the given file.
     *
     * @param file the file to compute the MD5 for.
     * @return the MD5 string.
     * @throws IOException if an error occurs reading the file.
     */
    public static String getMD5Digest(FSFile file) throws IOException {
        MessageDigest md5;

        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Couldn't find MD5");
        }

        byte[] buffer = new byte[0x1000];
        long position = 0;
        long length = file.getLength();
        while (position < length) {
            int chunkLength = (int) Math.min(length - position, buffer.length);
            file.read(position, ByteBuffer.wrap(buffer, 0, chunkLength));
            md5.update(buffer, 0, chunkLength);
            position += chunkLength;
        }

        byte[] digest = md5.digest();
        return Hexdump.toHexString(digest, 0, digest.length * 2).toLowerCase();
    }

    /**
     * Gets the MD5 string for the contents of the given buffer.
     *
     * @param buffer the buffer to compute the MD5 for.
     * @return the MD5 string.
     * @throws IOException if an error occurs reading the file.
     */
    public static String getMD5Digest(byte[] buffer) throws IOException {
        MessageDigest md5;

        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Couldn't find MD5");
        }

        md5.update(buffer, 0, buffer.length);

        byte[] digest = md5.digest();
        return Hexdump.toHexString(digest, 0, digest.length * 2).toLowerCase();
    }
}
