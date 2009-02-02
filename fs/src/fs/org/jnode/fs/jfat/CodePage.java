/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
