package org.jnode.fs.jfat;

import java.nio.CharBuffer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharacterCodingException;

/**
 * @author gvt
 */
public class CodePageDecoder {
    private final Charset cs;
    private final CharsetDecoder decoder;

    protected CodePageDecoder(Charset cs) {
        this.cs = cs;
        this.decoder = cs.newDecoder();
        reset();
    }

    public void reset() {
        decoder.reset();
    }

    public CharBuffer decode(ByteBuffer in) throws CharacterCodingException {
        return decoder.decode(in);
    }

    public String decode(byte[] b, int offset, int length) throws CharacterCodingException {
        CharBuffer out = decode(ByteBuffer.wrap(b, offset, length));
        return out.toString();
    }

    public String decode(byte[] b) throws CharacterCodingException {
        return decode(b, 0, b.length);
    }
}
