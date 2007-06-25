/*
 * Copyright 1997 Sun Microsystems, Inc.  All Rights Reserved.
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
package sun.io;

public abstract class CharToByteDBCS_ASCII extends CharToByteConverter
{

    private char highHalfZoneCode;
    private byte[] outputByte = new byte[2];

    protected short index1[];
    protected String index2;
    protected String index2a;
    protected int   mask1;
    protected int   mask2;
    protected int   shift;

    /**
      * flush out any residual data and reset the buffer state
      */
    public int flush(byte [] output, int outStart, int outEnd)
        throws MalformedInputException, ConversionBufferFullException
    {

       if (highHalfZoneCode != 0) {
          reset();
          badInputLength = 0;
          throw new MalformedInputException();
       }

       reset();
       return 0;
    }

    /**
     * Character conversion
     */
    public int convert(char[] input, int inOff, int inEnd,
                       byte[] output, int outOff, int outEnd)
        throws UnknownCharacterException, MalformedInputException,
               ConversionBufferFullException
    {
        char    inputChar;
        int     inputSize;

        byteOff = outOff;
        charOff = inOff;

        while(charOff < inEnd) {

           int   index;
           int   theBytes;
           int   spaceNeeded;

           if (highHalfZoneCode == 0) {
              inputChar = input[charOff];
              inputSize = 1;
           } else {
              inputChar = highHalfZoneCode;
              inputSize = 0;
              highHalfZoneCode = 0;
           }


           // Is this a high surrogate?
           if(inputChar >= '\ud800' && inputChar <= '\udbff') {
              // Is this the last character of the input?
              if (charOff + inputSize >= inEnd) {
                 highHalfZoneCode = inputChar;
                 charOff += inputSize;
                 break;
              }

              // Is there a low surrogate following?
              inputChar = input[charOff + inputSize];
              if (inputChar >= '\udc00' && inputChar <= '\udfff') {

                 // We have a valid surrogate pair.  Too bad we don't do
                 // surrogates.  Is substitution enabled?
                 if (subMode) {
                    if (subBytes.length == 1) {
                       outputByte[0] = 0x00;
                       outputByte[1] = subBytes[0];
                    }
                    else {
                       outputByte[0] = subBytes[0];
                       outputByte[1] = subBytes[1];
                    }

                    inputSize++;
                 } else {
                    badInputLength = 2;
                    throw new UnknownCharacterException();
                 }
              } else {

                 // We have a malformed surrogate pair
                 badInputLength = 1;
                 throw new MalformedInputException();
              }
           }

           // Is this an unaccompanied low surrogate?
           else
              if (inputChar >= '\uDC00' && inputChar <= '\uDFFF') {
                 badInputLength = 1;
                 throw new MalformedInputException();
              } else {

                 // We have a valid character, get the bytes for it
                 index = index1[((inputChar & mask1) >> shift)] + (inputChar & mask2);
                 if (index < 15000)
                   theBytes = (int)(index2.charAt(index));
                 else
                   theBytes = (int)(index2a.charAt(index-15000));
                 outputByte[0] = (byte)((theBytes & 0x0000ff00)>>8);
                 outputByte[1] = (byte)(theBytes & 0x000000ff);
              }

           // if there was no mapping - look for substitution characters
           if (outputByte[0] == 0x00 && outputByte[1] == 0x00
                             && inputChar != '\u0000')
           {
              if (subMode) {
                 if (subBytes.length == 1) {
                    outputByte[0] = 0x00;
                    outputByte[1] = subBytes[0];
                 } else {
                    outputByte[0] = subBytes[0];
                    outputByte[1] = subBytes[1];
                 }
              } else {
                badInputLength = 1;
                throw new UnknownCharacterException();
              }
           }

           if (outputByte[0] == 0x00)
              spaceNeeded = 1;
           else
              spaceNeeded = 2;

           if (byteOff + spaceNeeded > outEnd)
              throw new ConversionBufferFullException();

           if (spaceNeeded == 1)
              output[byteOff++] = outputByte[1];
           else {
              output[byteOff++] = outputByte[0];
              output[byteOff++] = outputByte[1];
           }

           charOff += inputSize;
        }

        return byteOff - outOff;
    }

    /**
     * Resets converter to its initial state.
     */
    public void reset() {
       charOff = byteOff = 0;
       highHalfZoneCode = 0;
    }

    /**
     * Returns the maximum number of bytes needed to convert a char.
     */
    public int getMaxBytesPerChar() {
        return 2;
    }


    /**
     * Returns true if the given character can be converted to the
     * target character encoding.
     */
    public boolean canConvert(char ch) {
       int  index;
       int  theBytes;

       index = index1[((ch & mask1) >> shift)] + (ch & mask2);
       if (index < 15000)
         theBytes = (int)(index2.charAt(index));
       else
         theBytes = (int)(index2a.charAt(index-15000));

       if (theBytes != 0)
         return (true);

       // only return true if input char was unicode null - all others are
       //     undefined
       return( ch == '\u0000');

    }

}
