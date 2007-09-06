// This file was generated AUTOMATICALLY from a template file Fri May 25 16:17:12 EDT 2007
/*
 * Copyright 2003-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package java.lang;

/** The CharacterData class encapsulates the large tables found in
    Java.lang.Character. */

class CharacterData02 extends CharacterData {
    /* The character properties are currently encoded into 32 bits in the following manner:
        1 bit   mirrored property
        4 bits  directionality property
        9 bits  signed offset used for converting case
        1 bit   if 1, adding the signed offset converts the character to lowercase
        1 bit   if 1, subtracting the signed offset converts the character to uppercase
        1 bit   if 1, this character has a titlecase equivalent (possibly itself)
        3 bits  0  may not be part of an identifier
                1  ignorable control; may continue a Unicode identifier or Java identifier
                2  may continue a Java identifier but not a Unicode identifier (unused)
                3  may continue a Unicode identifier or Java identifier
                4  is a Java whitespace character
                5  may start or continue a Java identifier;
                   may continue but not start a Unicode identifier (underscores)
                6  may start or continue a Java identifier but not a Unicode identifier ($)
                7  may start or continue a Unicode identifier or Java identifier
                Thus:
                   5, 6, 7 may start a Java identifier
                   1, 2, 3, 5, 6, 7 may continue a Java identifier
                   7 may start a Unicode identifier
                   1, 3, 5, 7 may continue a Unicode identifier
                   1 is ignorable within an identifier
                   4 is Java whitespace
        2 bits  0  this character has no numeric property
                1  adding the digit offset to the character code and then
                   masking with 0x1F will produce the desired numeric value
                2  this character has a "strange" numeric value
                3  a Java supradecimal digit: adding the digit offset to the
                   character code, then masking with 0x1F, then adding 10
                   will produce the desired numeric value
        5 bits  digit offset
        5 bits  character type

        The encoding of character properties is subject to change at any time.
     */

    int getProperties(int ch) {
		char offset = (char)ch;
        int props = A[Y[X[offset>>5]|((offset>>1)&0xF)]|(offset&0x1)];
        return props;
    }

    int getType(int ch) {
        int props = getProperties(ch);
        return (props & 0x1F);
    }

    boolean isJavaIdentifierStart(int ch) {
        int props = getProperties(ch);
        return ((props & 0x00007000) >= 0x00005000);
    }

    boolean isJavaIdentifierPart(int ch) {
        int props = getProperties(ch);
        return ((props & 0x00003000) != 0);
    }

    boolean isUnicodeIdentifierStart(int ch) {
        int props = getProperties(ch);
        return ((props & 0x00007000) == 0x00007000);
    }

    boolean isUnicodeIdentifierPart(int ch) {
        int props = getProperties(ch);
        return ((props & 0x00001000) != 0);
    }

    boolean isIdentifierIgnorable(int ch) {
        int props = getProperties(ch);
        return ((props & 0x00007000) == 0x00001000);
    }

    int toLowerCase(int ch) {
        int mapChar = ch;
        int val = getProperties(ch);

        if ((val & 0x00020000) != 0) {
            int offset = val << 5 >> (5+18);
            mapChar = ch + offset;
        }
        return mapChar;
    }

    int toUpperCase(int ch) {
        int mapChar = ch;
        int val = getProperties(ch);

        if ((val & 0x00010000) != 0) {
            int offset = val  << 5 >> (5+18);
            mapChar =  ch - offset;
        }
        return mapChar;
    }

    int toTitleCase(int ch) {
        int mapChar = ch;
        int val = getProperties(ch);

        if ((val & 0x00008000) != 0) {
            // There is a titlecase equivalent.  Perform further checks:
            if ((val & 0x00010000) == 0) {
                // The character does not have an uppercase equivalent, so it must
                // already be uppercase; so add 1 to get the titlecase form.
                mapChar = ch + 1;
            }
            else if ((val & 0x00020000) == 0) {
                // The character does not have a lowercase equivalent, so it must
                // already be lowercase; so subtract 1 to get the titlecase form.
                mapChar = ch - 1;
            }
            // else {
            // The character has both an uppercase equivalent and a lowercase
            // equivalent, so it must itself be a titlecase form; return it.
            // return ch;
            //}
        }
        else if ((val & 0x00010000) != 0) {
            // This character has no titlecase equivalent but it does have an
            // uppercase equivalent, so use that (subtract the signed case offset).
            mapChar = toUpperCase(ch);
        }
        return mapChar;
    }

    int digit(int ch, int radix) {
        int value = -1;
        if (radix >= Character.MIN_RADIX && radix <= Character.MAX_RADIX) {
            int val = getProperties(ch);
            int kind = val & 0x1F;
            if (kind == Character.DECIMAL_DIGIT_NUMBER) {
                value = ch + ((val & 0x3E0) >> 5) & 0x1F;
            }
            else if ((val & 0xC00) == 0x00000C00) {
                // Java supradecimal digit
                value = (ch + ((val & 0x3E0) >> 5) & 0x1F) + 10;
            }
        }
        return (value < radix) ? value : -1;
    }

    int getNumericValue(int ch) {
        int val = getProperties(ch);
        int retval = -1;

        switch (val & 0xC00) {
        default: // cannot occur
        case (0x00000000):         // not numeric
            retval = -1;
            break;
        case (0x00000400):              // simple numeric
            retval = ch + ((val & 0x3E0) >> 5) & 0x1F;
            break;
        case (0x00000800)      :       // "strange" numeric
            retval = -2;
            break;
        case (0x00000C00):           // Java supradecimal
            retval = (ch + ((val & 0x3E0) >> 5) & 0x1F) + 10;
            break;
        }
        return retval;
    }

    boolean isWhitespace(int ch) {
        return (getProperties(ch) & 0x00007000) == 0x00004000;
    }

    byte getDirectionality(int ch) {
        int val = getProperties(ch);
        byte directionality = (byte)((val & 0x78000000) >> 27);
        if (directionality == 0xF ) {
	        directionality = Character.DIRECTIONALITY_UNDEFINED;
        }
        return directionality;
    }

    boolean isMirrored(int ch) {
        return (getProperties(ch) & 0x80000000) != 0;
    }

    static final CharacterData instance = new CharacterData02();
    private CharacterData02() {};

    // The following tables and code generated using:
  // java GenerateCharacter -plane 2 -template ../../tools/GenerateCharacter/CharacterData02.java.template -spec ../../tools/UnicodeData/UnicodeData.txt -specialcasing ../../tools/UnicodeData/SpecialCasing.txt -o /notnfs/fkung/workspace/openjdk/control/build/linux-i586/gensrc/java/lang/CharacterData02.java -string -usecharforbyte 11 4 1
  // The X table has 2048 entries for a total of 4096 bytes.

  static final char X[] = (
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\020\040\040\040\040\040\040\040\040\040\040\040\040\040\040"+
    "\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040"+
    "\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040"+
    "\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040"+
    "\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040"+
    "\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040"+
    "\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040"+
    "\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040"+
    "\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040"+
    "\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040"+
    "\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040"+
    "\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040"+
    "\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040"+
    "\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040"+
    "\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040"+
    "\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040"+
    "\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040"+
    "\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040"+
    "\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040"+
    "\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040"+
    "\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040"+
    "\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040"+
    "\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040"+
    "\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040"+
    "\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040"+
    "\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040"+
    "\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040"+
    "\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040"+
    "\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040"+
    "\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040"+
    "\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040"+
    "\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040"+
    "\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040"+
    "\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040"+
    "\040\040\040\040\040\040\040\040\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\060\040\040\040\040\040\040\040\040\040\040\040\040\040"+
    "\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040"+
    "\040\040\040\040\040\040\040\040\040\040\040\040\040\040\040").toCharArray();

  // The Y table has 64 entries for a total of 128 bytes.

  static final char Y[] = (
    "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\000\000\002\004\004\004\004\004\004\004\004\004\004"+
    "\004\004\004\004\004\004\004\004\004\004\000\000\000\000\000\000\000\000\000"+
    "\000\000\000\000\000\000\004").toCharArray();

  // The A table has 6 entries for a total of 24 bytes.

  static final int A[] = new int[6];
  static final String A_DATA =
    "\000\u7005\000\u7005\000\u7005\u7800\000\u7800\000\u7800\000";

  // In all, the character property tables require 4248 bytes.

    static {
                { // THIS CODE WAS AUTOMATICALLY CREATED BY GenerateCharacter:
            char[] data = A_DATA.toCharArray();
            assert (data.length == (6 * 2));
            int i = 0, j = 0;
            while (i < (6 * 2)) {
                int entry = data[i++] << 16;
                A[j++] = entry | data[i++];
            }
        }

    }        
}
