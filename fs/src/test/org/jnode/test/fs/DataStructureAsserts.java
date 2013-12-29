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
        while (position < file.getLength()) {
            int chunkLength = (int) Math.min(file.getLength() - position, buffer.length);
            file.read(position, ByteBuffer.wrap(buffer, 0, chunkLength));
            md5.update(buffer, 0, chunkLength);
            position += chunkLength;
        }

        byte[] digest = md5.digest();
        return Hexdump.toHexString(digest, 0, digest.length * 2).toLowerCase();
    }
}
