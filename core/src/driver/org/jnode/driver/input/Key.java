/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.driver.input;

import java.awt.event.KeyEvent;

/**
 * @author Martin Husted Hartvig
 * @since 0.1.6
 */

public class Key {
    private char lowerChar = KeyEvent.CHAR_UNDEFINED;
    private char upperChar = KeyEvent.CHAR_UNDEFINED;
    private char altGrChar = KeyEvent.CHAR_UNDEFINED;

    private int lowerVirtuelKey = KeyEvent.VK_UNDEFINED;
    private int upperVirtuelKey = KeyEvent.VK_UNDEFINED;
    private int altGrVirtuelKey = KeyEvent.VK_UNDEFINED;

    private char[][] deadChars = null;

    protected static final char[] noDeadChar = new char[]{0};

    public Key() {
    }

    public Key(char lowerChar, int lowerVirtuelKey, char upperChar, int upperVirtuelKey, char altGrChar,
               int altGrVirtuelKey) {
        this.lowerChar = lowerChar;
        this.upperChar = upperChar;
        this.altGrChar = altGrChar;
        this.lowerVirtuelKey = lowerVirtuelKey;
        this.upperVirtuelKey = upperVirtuelKey;
        this.altGrVirtuelKey = altGrVirtuelKey;
    }

    public Key(char lowerChar, int lowerVirtuelKey, char upperChar, int upperVirtuelKey) {
        this.lowerChar = lowerChar;
        this.upperChar = upperChar;
        this.lowerVirtuelKey = lowerVirtuelKey;
        this.upperVirtuelKey = upperVirtuelKey;
    }

    public Key(char lowerChar, char upperChar, char altGrChar, int virtuelKey) {
        this.lowerChar = lowerChar;
        this.upperChar = upperChar;
        this.altGrChar = altGrChar;
        this.lowerVirtuelKey = virtuelKey;
        this.upperVirtuelKey = virtuelKey;
    }

    public Key(char lowerChar, char upperChar, int virtuelKey) {
        this.lowerChar = lowerChar;
        this.upperChar = upperChar;
        this.lowerVirtuelKey = virtuelKey;
        this.upperVirtuelKey = virtuelKey;
    }

    public Key(char lowerUpperChar, int virtuelKey) {
        this.lowerChar = lowerUpperChar;
        this.upperChar = lowerUpperChar;
        this.lowerVirtuelKey = virtuelKey;
        this.upperVirtuelKey = virtuelKey;
    }

    public Key(char lowerChar, char upperChar) {
        this.lowerChar = lowerChar;
        this.upperChar = upperChar;
    }

    public Key(int virtuelKey) {
        this.lowerVirtuelKey = virtuelKey;
        this.upperVirtuelKey = virtuelKey;
        this.altGrVirtuelKey = virtuelKey;
    }

    public char getLowerChar() {
        return lowerChar;
    }

    public void setLowerChar(char lowerChar) {
        this.lowerChar = lowerChar;
    }

    public char getUpperChar() {
        return upperChar;
    }

    public void setUpperChar(char upperChar) {
        this.upperChar = upperChar;
    }

    public char getControlChar() {
        return (char) (upperChar & 0x1f);
    }

    public char getAltGrChar() {
        return altGrChar;
    }

    public void setAltGrChar(char altGrChar) {
        this.altGrChar = altGrChar;
    }

    public int getLowerVirtuelKey() {
        return lowerVirtuelKey;
    }

    public void setLowerVirtuelKey(int lowerVirtuelKey) {
        this.lowerVirtuelKey = lowerVirtuelKey;
    }

    public int getUpperVirtuelKey() {
        return upperVirtuelKey;
    }

    public void setUpperVirtuelKey(int upperVirtuelKey) {
        this.upperVirtuelKey = upperVirtuelKey;
    }

    public int getAltGrVirtuelKey() {
        return altGrVirtuelKey;
    }

    public void setAltGrVirtuelKey(int altGrVirtuelKey) {
        this.altGrVirtuelKey = altGrVirtuelKey;
    }

    public void addDeadKeyChar(int virtuelKey, char[] deadChar) {
        if (deadChars == null)
            deadChars = new char[16][2];

        switch (virtuelKey) {
            case KeyEvent.VK_DEAD_ABOVEDOT:
                deadChars[0] = deadChar;
                break;

            case KeyEvent.VK_DEAD_ABOVERING:
                deadChars[1] = deadChar;
                break;

            case KeyEvent.VK_DEAD_ACUTE:
                deadChars[2] = deadChar;
                break;

            case KeyEvent.VK_DEAD_BREVE:
                deadChars[3] = deadChar;
                break;

            case KeyEvent.VK_DEAD_CARON:
                deadChars[4] = deadChar;
                break;

            case KeyEvent.VK_DEAD_CEDILLA:
                deadChars[5] = deadChar;
                break;

            case KeyEvent.VK_DEAD_CIRCUMFLEX:
                deadChars[6] = deadChar;
                break;

            case KeyEvent.VK_DEAD_DIAERESIS:
                deadChars[7] = deadChar;
                break;

            case KeyEvent.VK_DEAD_DOUBLEACUTE:
                deadChars[8] = deadChar;
                break;

            case KeyEvent.VK_DEAD_GRAVE:
                deadChars[9] = deadChar;
                break;

            case KeyEvent.VK_DEAD_IOTA:
                deadChars[10] = deadChar;
                break;

            case KeyEvent.VK_DEAD_MACRON:
                deadChars[11] = deadChar;
                break;

            case KeyEvent.VK_DEAD_OGONEK:
                deadChars[12] = deadChar;
                break;

            case KeyEvent.VK_DEAD_SEMIVOICED_SOUND:
                deadChars[13] = deadChar;
                break;

            case KeyEvent.VK_DEAD_TILDE:
                deadChars[14] = deadChar;
                break;

            case KeyEvent.VK_DEAD_VOICED_SOUND:
                deadChars[15] = deadChar;
                break;

        }
    }

    public char[] getDeadKeyChar(int virtuelKey) {
        if (deadChars == null)
            return noDeadChar;

        switch (virtuelKey) {
            case KeyEvent.VK_DEAD_ABOVEDOT:
                return deadChars[0];

            case KeyEvent.VK_DEAD_ABOVERING:
                return deadChars[1];

            case KeyEvent.VK_DEAD_ACUTE:
                return deadChars[2];

            case KeyEvent.VK_DEAD_BREVE:
                return deadChars[3];

            case KeyEvent.VK_DEAD_CARON:
                return deadChars[4];

            case KeyEvent.VK_DEAD_CEDILLA:
                return deadChars[5];

            case KeyEvent.VK_DEAD_CIRCUMFLEX:
                return deadChars[6];

            case KeyEvent.VK_DEAD_DIAERESIS:
                return deadChars[7];

            case KeyEvent.VK_DEAD_DOUBLEACUTE:
                return deadChars[8];

            case KeyEvent.VK_DEAD_GRAVE:
                return deadChars[9];

            case KeyEvent.VK_DEAD_IOTA:
                return deadChars[10];

            case KeyEvent.VK_DEAD_MACRON:
                return deadChars[11];

            case KeyEvent.VK_DEAD_OGONEK:
                return deadChars[12];

            case KeyEvent.VK_DEAD_SEMIVOICED_SOUND:
                return deadChars[13];

            case KeyEvent.VK_DEAD_TILDE:
                return deadChars[14];

            case KeyEvent.VK_DEAD_VOICED_SOUND:
                return deadChars[15];

            default:
                return noDeadChar;
        }
    }


    public String toString() {
        return "Key{" +
            "lowerChar=" + lowerChar +
            ", upperChar=" + upperChar +
            ", altGrChar=" + altGrChar +
            ", lowerVirtuelKey=" + lowerVirtuelKey +
            ", upperVirtuelKey=" + upperVirtuelKey +
            ", altGrVirtuelKey=" + altGrVirtuelKey +
            "}";
    }
}
