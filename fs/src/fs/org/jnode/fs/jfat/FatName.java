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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.util.Arrays;

public class FatName {

    //
    // special characters
    //
    private static final byte underscore = '_';
    private static final byte space = ' ';
    private static final byte period = '.';
    private static final byte tilde = '~';

    //
    // shortname illegal chars
    //
    // 0x22, 0x2A, 0x2B, 0x2C, 0x2F,
    // 0x3A, 0x3B, 0x3C, 0x3D, 0x3E, 0x3F,
    // 0x5B, 0x5C, 0x5D,
    // 0x7C
    //
    private static final byte[] short_illegal = {
        '"', '*', '+', ',', '/', ':', ';', '<', '=', '>', '?', '[', '\\', ']', '|'};

    private final FatDirectory parent;
    private final String name;
    private final CodePageEncoder encoder;
    private final CodePageDecoder decoder;

    private final int numberOfComponents;
    private final String[] components;

    private boolean lossy;
    private boolean stripped;
    private boolean fit;

    private byte[] basis;

    private final String shortName;
    private final String longName;

    private String shortBase;
    private String shortExt;
    private FatCase shortCase;

    FatName(FatDirectory parent, String name) throws IOException {
        CodePage cp = parent.getFatFileSystem().getCodePage();

        this.parent = parent;
        this.name = name;
        this.encoder = cp.newEncoder();
        this.decoder = cp.newDecoder();

        this.longName = FatUtils.longName(name);

        this.lossy = false;
        this.fit = true;
        this.stripped = false;

        this.basis = new byte[11];

        Arrays.fill(this.basis, 0, this.basis.length, space);

        basisName();

        /*
         * log.debug ( "lossy\t[" + lossy + "]" ); log.debug ( "stripped\t[" +
         * stripped + "]" ); log.debug ( "fit\t\t[" + fit + "]" ); log.debug (
         * "basis\t<" + decoder.decode ( basis ) + ">" );
         */

        if (isMangled()) {
            numericTail();

            // log.debug ( "basisN\t<" + decoder.decode ( basis ) + ">" );

            byte[] primary = new byte[8];
            byte[] extension = new byte[3];

            System.arraycopy(basis, 0, primary, 0, 8);
            System.arraycopy(basis, 8, extension, 0, 3);

            shortBase = decoder.decode(primary).trim();
            shortExt = decoder.decode(extension).trim();
            shortCase = new FatCase();
        } else {
            int p = longName.indexOf(period);
            if (p == -1) {
                shortBase = longName;
                shortExt = "";
            } else {
                shortBase = longName.substring(0, p);
                if (p == (longName.length() - 1))
                    shortExt = "";
                else
                    shortExt = longName.substring(p + 1);
            }

            shortCase = new FatCase(shortBase, shortExt);
        }

        if (shortExt.length() > 0)
            shortName = shortBase + "." + shortExt;
        else
            shortName = shortBase;

        int n = longName.length() / 13;

        if ((longName.length() % 13) != 0)
            n++;

        numberOfComponents = n;
        components = new String[n];

        for (int i = 0; i < n - 1; i++)
            components[i] = longName.substring(i * 13, (i + 1) * 13);

        components[n - 1] = longName.substring((n - 1) * 13);
    }

    /*
     * internal utility routine
     */
    private byte[] stripChar(byte[] name, byte ch, boolean strip) {
        boolean flag = !strip;
        ByteBuffer b = ByteBuffer.allocate(name.length);

        for (int i = (name.length - 1); i >= 0; i--) {
            if (name[i] != ch)
                strip = flag;

            if (strip) {
                if (name[i] != ch)
                    b.put(name[i]);
                else
                    stripped = true;
            } else
                b.put(name[i]);
        }

        b.flip();

        byte[] n = new byte[b.remaining()];

        b.get(n);

        return n;
    }

    /*
     * internal utility routine
     */
    boolean collide(byte[] name) {
        return parent.collide(name);
    }

    /*
     * Basis-Name Phase1: Step1 and Step2
     */
    private byte[] encode() throws CharacterCodingException {
        byte[] n = encoder.encode(FatUtils.toUpperCase(longName), underscore);

        lossy = encoder.isLossy();

        for (int i = 0; i < n.length; i++) {
            if (n[i] < 0x20) {
                n[i] = underscore;
                lossy = true;
                continue;
            }

            for (int j = 0; j < short_illegal.length; j++) {
                if ((n[i] == short_illegal[j])) {
                    n[i] = underscore;
                    lossy = true;
                    break;
                }
            }
        }

        return n;
    }

    /*
     * Basis-Name Phase2: Step3 and Step4
     */
    private byte[] strip(byte[] name) {
        return stripChar(stripChar(name, space, false), period, true);
    }

    /*
     * Basis-Name Phase3: Step5, Step6(omitted) and Step7
     */
    private void primary(byte[] name) {
        int i;

        for (i = 0; i < name.length; i++) {
            if (name[i] == period)
                break;
            if (i >= 8) {
                fit = false;
                break;
            }
            basis[i] = name[i];
        }

        for (i = (name.length - 1); i >= 0; i--)
            if (name[i] == period)
                break;

        if (i != -1) {
            if (i > 8)
                fit = false;

            int j;
            int l = i + 1;

            for (j = l; j < name.length; j++) {
                if ((j - l) >= 3)
                    break;
                basis[8 + (j - l)] = name[j];
            }

            if (j != name.length)
                fit = false;
        }
    }

    private void basisName() throws CharacterCodingException {
        primary(strip(encode()));

    }

    private void numericTail() throws IOException {
        int i, j, l, p;
        byte[] nbasis = new byte[11];
        StringBuilder btail = new StringBuilder(7);

        for (p = 7; p >= 0; p--)
            if (basis[p] != space)
                break;

        for (i = 1; i <= 999999; i++) {
            System.arraycopy(basis, 0, nbasis, 0, 11);

            btail.setLength(0);
            btail.append(i);

            byte[] tail = encoder.encode(btail.toString());

            if (tail.length > 6)
                throw new IOException("tail too long: " + tail.length);

            if ((p + tail.length) < 8)
                l = p + 1;
            else
                l = 7 - tail.length;

            nbasis[l] = tilde;

            for (j = l; j < (l + tail.length); j++)
                nbasis[j + 1] = tail[j - l];

            if (!collide(nbasis)) {
                basis = nbasis;
                return;
            }
        }

        throw new IOException("tail too large");
    }

    public byte[] getName() {
        return basis;
    }

    public String getShortName() {
        return shortName;
    }

    public String getLongName() {
        return longName;
    }

    public boolean isMangled() {
        return (lossy || stripped || !fit);
    }

    public String getShortBase() {
        return shortBase;
    }

    public String getShortExt() {
        return shortExt;
    }

    public FatCase getShortCase() {
        return shortCase;
    }

    public int getNumberOfComponents() {
        return numberOfComponents;
    }

    public String[] getComponents() {
        return components;
    }

    public String getComponent(int i) {
        return components[i];
    }

    public String toString() {
        StrWriter out = new StrWriter();

        out.println("*******************************************");
        out.println("FatName");
        out.println("*******************************************");
        out.println("LongName\t" + getLongName());
        out.println("isMangled\t" + isMangled());
        out.println("N.Components\t" + getNumberOfComponents());
        out.println("ShortBase\t" + getShortBase());
        out.println("ShortExt\t" + getShortExt());
        out.println("ShortCase\t" + getShortCase());
        out.println("ShortName\t" + getShortName());
        out.print("*******************************************");

        return out.toString();
    }
}
