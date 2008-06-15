package org.jnode.apps.vmware.disk;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Reader;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 * 
 */
public class RandomAccessFileReader extends Reader {
    private final RandomAccessFile raf;
    private final boolean mustClose;

    public RandomAccessFileReader(RandomAccessFile raf, boolean mustClose) {
        this.raf = raf;
        this.mustClose = mustClose;
    }

    @Override
    public void close() throws IOException {
        if (mustClose) {
            raf.close();
        }
    }

    @Override
    public int read(char[] buf, int offset, int count) throws IOException {
        int nbRead = 0;
        for (int i = offset; i < (offset + count); i++) {
            buf[i] = raf.readChar();
        }
        return nbRead;
    }
}
