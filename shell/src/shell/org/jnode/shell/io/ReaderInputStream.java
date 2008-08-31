package org.jnode.shell.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

public class ReaderInputStream extends InputStream {
    private final Reader reader;
    
    private CharBuffer chars = CharBuffer.allocate(1024);
    private ByteBuffer bytes = ByteBuffer.allocate(2048);
    
    private CharsetEncoder encoder;

    public ReaderInputStream(Reader reader, String encoding) {
        this.reader = reader;
        this.encoder = Charset.forName(encoding).newEncoder();
        this.bytes.position(bytes.limit());
        this.chars.position(chars.limit());
    }

    @Override
    public synchronized int read() throws IOException {
        if (bytes.remaining() == 0) {
            if (!fillBuffer(true)) {
                return -1;
            }
        }
        return bytes.get();
    }
    
    @Override
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        if (off < 0 || off > b.length || len < 0 || off + len > b.length || off + len < 0) {
            throw new IndexOutOfBoundsException();
        }
        // This implementation is simple-minded.  I'm sure we could recode it to avoid
        // the 'bytes.get' copying step if we thought about it.
        int count = 0;
        while (count < len) {
            if (bytes.remaining() == 0) {
                if (!fillBuffer(count == 0)) {
                    return count == 0 ? -1 : count;
                }
            }
            int copied = Math.min(bytes.remaining(), len);
            bytes.get(b, off, copied);
            System.err.println("Copied " + copied);
            count += copied;
            len -= copied;
            off += copied;
        }
        return count;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }
    
    /**
     * This method puts bytes into the (empty) 'bytes' buffer.  It returns
     * <code>false</code> if no bytes were copied either because the reader
     * would have blocked or because it returned <code>-1</code>.  
     * 
     * @param wait if <code>true</code> allow the reader to block.
     * @return <code>true</code> if we've added some data to 'bytes'.
     * @throws IOException
     */
    private boolean fillBuffer(boolean wait) throws IOException {
        bytes.clear();
        // The loop is necessary because the way that the encoder has to deal
        // with UTF-16 surrogate pairs.  If the one and only character returned
        // by the reader is the first char of a surrogate pair, the encoder won't
        // (can't) put anything into the 'bytes' buffer.  So if 'wait' is true, 
        // we must go around a second time to get the second character of the
        // surrogate pair.
        do {
            CoderResult cr;
            if (chars.remaining() == 0) {
                chars.clear();
                if (!reader.ready() && !wait) {
                    bytes.flip();
                    return false;
                }
                if (reader.read(chars) == -1) {
                    System.err.println("Reached EOF");
                    bytes.flip();
                    cr = encoder.encode(chars, bytes, true);
                    if (cr.isError()) {
                        cr.throwException();
                    }
                    return bytes.remaining() > 0;
                }
                chars.flip();
            }
            cr = encoder.encode(chars, bytes, false);
            if (cr.isError()) {
                cr.throwException();
            }
        } while (wait && bytes.position() == 0);
        bytes.flip();
        return true;
    }
}
