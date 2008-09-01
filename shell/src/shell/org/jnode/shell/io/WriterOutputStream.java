package org.jnode.shell.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class WriterOutputStream extends OutputStream {
    // TODO deal with decoder errors.
    
    private ByteBuffer bytes = ByteBuffer.allocate(2048);
    private CharBuffer chars = CharBuffer.allocate(1024);
    
    private Writer writer;
    private CharsetDecoder decoder;

    public WriterOutputStream(Writer writer, String encoding) {
        this.writer = writer;
        this.decoder = Charset.forName(encoding).newDecoder();
    }

    @Override
    public void write(int b) throws IOException {
        bytes.put((byte) b);
        if (bytes.remaining() == 0) {
            decoder.decode(bytes, chars, false);
            flush();
        }
    }

    @Override
    public void flush() throws IOException {
        if (chars.position() > 0) {
            int len = chars.position();
            int pos = chars.arrayOffset();
            writer.write(chars.array(), pos, len);
            chars.clear();
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (off < 0 || off > b.length || len < 0 || off + len > b.length || off + len < 0) {
            throw new IndexOutOfBoundsException();
        }
        while (len > 0) {
            int pos = bytes.position();
            bytes.put(b, off, len);
            int count = bytes.position() - pos;
            off += count;
            len -= count;
            decoder.decode(bytes, chars, false);
            flush();
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }
}
