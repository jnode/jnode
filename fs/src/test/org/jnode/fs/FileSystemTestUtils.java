package org.jnode.fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import org.jnode.emu.plugin.model.DummyConfigurationElement;
import org.jnode.emu.plugin.model.DummyExtension;
import org.jnode.emu.plugin.model.DummyExtensionPoint;
import org.jnode.emu.plugin.model.DummyPluginDescriptor;
import org.jnode.fs.service.FileSystemService;
import org.jnode.fs.service.def.FileSystemPlugin;
import org.jnode.util.FileUtils;
import org.junit.Assert;

import static org.junit.Assert.assertTrue;

/**
 * File system test utilities.
 */
public class FileSystemTestUtils {

    /**
     * Gets a file system test file.
     *
     * @param testFile the test file to get. E.g. "ntfs/test.ntfs".
     * @return the file.
     * @throws IOException if an error occurs.
     */
    public static File getTestFile(String testFile) throws IOException {
        File file = new File("fs/src/test/org/jnode/fs/", testFile);

        // Look for the gzip file.
        File gzipFile = new File(file.getParent(), file.getName() + ".gz");
        if (gzipFile.exists()) {
            explodeGzip(gzipFile, file);
            return file;
        }

        Assert.fail("Expected a gzipped file: " + gzipFile.getAbsolutePath());
        return null;
    }

    /**
     * Explodes a GZIP file to a file.
     *
     * @param gzipFile the source GZIP file.
     * @param outputFile the destination file.
     * @throws java.io.IOException if there was an error exploding the GZIP file.
     */
    private static synchronized void explodeGzip(File gzipFile, File outputFile) throws IOException {
        File tempFile = new File(outputFile.getParentFile(), outputFile.getName() + ".tmp");

        if (!outputFile.exists() || gzipFile.lastModified() > outputFile.lastModified()) {
            if (outputFile.exists()) {
                // Force deletion if it's out of date or the renameTo further down will fail.
                Assert.assertTrue(outputFile.delete());
            }

            InputStream in = new GZIPInputStream(new FileInputStream(gzipFile));
            try {
                OutputStream out = new FileOutputStream(tempFile);
                try {
                    FileUtils.copy(in, out, new byte[0x10000], false);
                }
                finally {
                    out.close();
                }
            }
            finally {
                in.close();
            }

            assertTrue(
                String.format("Temp data file couldn't be renamed.\nOld name: %s\nNew name: %s", tempFile, outputFile),
                tempFile.renameTo(outputFile));
        }
    }

    /**
     * Creates a file system service for testing.
     *
     * @param fileSystemTypeClassName the class name of the file system type.
     * @return the file system service.
     */
    public static FileSystemService createFSService(String fileSystemTypeClassName) {
        DummyPluginDescriptor desc = new DummyPluginDescriptor(true);
        DummyExtensionPoint ep = new DummyExtensionPoint("types", "org.jnode.fs.types", "types");
        desc.addExtensionPoint(ep);
        DummyExtension extension = new DummyExtension();
        DummyConfigurationElement element = new DummyConfigurationElement();
        element.addAttribute("class", fileSystemTypeClassName);
        extension.addElement(element);
        ep.addExtension(extension);
        return new FileSystemPlugin(desc);
    }
}
