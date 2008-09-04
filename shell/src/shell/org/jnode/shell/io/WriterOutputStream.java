package org.jnode.shell.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

public class WriterOutputStream extends OutputStream {
    // TODO deal with decoder errors.
    
    private ByteBuffer bytes = ByteBuffer.allocate(2048);
    private CharBuffer chars = CharBuffer.allocate(2048);
    
    private Writer writer;
    private CharsetDecoder decoder;

    public WriterOutputStream(Writer writer, String encoding) {
        this.writer = writer;
        this.decoder = Charset.forName(encoding).newDecoder();
        bytes.clear();
        chars.clear();
    }

    @Override
    public void write(int b) throws IOException {
        bytes.put((byte) b);
        if (bytes.remaining() == 0) {
            flush(false);
        }
    }

    @Override
    public void flush() throws IOException {
        flush(false);
    }
    
    @Override
    public void close() throws IOException {
        flush(true);
        writer.close();
    }

    private int flush(boolean all) throws IOException {
        if (bytes.position() > 0) {
            bytes.flip();
            chars.clear();
            CoderResult cr = decoder.decode(bytes, chars, all);
            int count = chars.position();
            if (count > 0) {
                int pos = chars.arrayOffset();
                writer.write(chars.array(), pos, count);
            }
            if (cr.isError() || (all && cr == CoderResult.UNDERFLOW)) {
                cr.throwException();
            }
            if (bytes.remaining() > 0) {
                byte[] tmp = new byte[bytes.remaining()];
                bytes.get(tmp);
                bytes.clear();
                bytes.put(tmp);
            } else {
                bytes.clear();
            }
            return count;
        } else {
            return 0;
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (off < 0 || off > b.length || len < 0 || off + len > b.length || off + len < 0) {
            throw new IndexOutOfBoundsException();
        }
        while (len > 0) {
            int toWrite = Math.min(len, bytes.remaining());
            bytes.put(b, off, toWrite);
            off += toWrite;
            len -= toWrite;
            if (bytes.remaining() == 0) {
                flush(false);
            }
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }
}
