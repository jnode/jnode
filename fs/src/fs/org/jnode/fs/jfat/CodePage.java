/*
 *
 */
 
package org.jnode.fs.jfat;

import java.nio.charset.Charset;


/**
 * @author gvt
 */
public class CodePage {
    private final Charset charset;

    protected CodePage(Charset charset) {
        this.charset = charset;
    }

    public static CodePage forName(String codePageName) {
        return new CodePage(Charset.forName(codePageName));
    }

    public CodePageEncoder newEncoder() {
        return new Encoder();
    }

    public CodePageDecoder newDecoder() {
        return new Decoder();
    }

    private class Encoder extends CodePageEncoder {
        protected Encoder() {
            super(charset);
        }
    }

    private class Decoder extends CodePageDecoder {
        protected Decoder() {
            super(charset);
        }
    }

    public String toString() {
        StrWriter out = new StrWriter();

        out.println("*******************************************");
        out.println("CodePage");
        out.println("*******************************************");
        out.print("Charset\t" + charset);

        return out.toString();
    }
}
