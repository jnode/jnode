/*
 * Copyright 2000 Sun Microsystems, Inc.  All Rights Reserved.
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

/**
 * Class for converting characters to bytes for the EUC-JP encoding in
 * linux. This converter supports the JIS0201 and the JIS0208 encoding and
 * omits support for the JIS212 encoding.
 * 
 * @author Naveen Sanjeeva
 */

public class CharToByteEUC_JP_LINUX extends CharToByteJIS0208 {
    CharToByteJIS0201 cbJIS0201 = new CharToByteJIS0201();

    public String getCharacterEncoding() {
        return "EUC_JP_LINUX";
    }

    protected int convSingleByte(char inputChar, byte[] outputByte) {
	byte b;

	if (inputChar == 0) {
	    outputByte[0] = (byte)0;
	    return 1;
	}

	if ((b = cbJIS0201.getNative(inputChar)) == 0)
	    return 0;

	if (b > 0 && b < 128) {
	    outputByte[0] = b;
	    return 1;
	}
	outputByte[0] = (byte)0x8E;
	outputByte[1] = b;
	return 2;
    }

    protected int getNative(char ch) {
	int offset = index1[((ch & 0xff00) >> 8 )] << 8;
	int r = index2[offset >> 12].charAt((offset & 0xfff) + (ch & 0xff));
	if (r != 0)
	    return r + 0x8080;
	return r;
    }

    /**
     * Converts characters to sequences of bytes.
     * Conversions that result in Exceptions can be restarted by calling
     * convert again, with appropriately modified parameters.
     * @return the characters written to output.
     * @param input char array containing text in Unicode
     * @param inStart offset in input array
     * @param inEnd offset of last byte to be converted
     * @param output byte array to receive conversion result
     * @param outStart starting offset
     * @param outEnd offset of last byte to be written to
     * @throw UnsupportedCharacterException for any character 
     * that cannot be converted to the external character set.
     */
    public int convert(char[] input, int inOff, int inEnd,
		       byte[] output, int outOff, int outEnd)
        throws MalformedInputException, UnknownCharacterException,
               ConversionBufferFullException
    {
        char    inputChar;          	   // Input character to be converted
        byte[]  outputByte; 		   // Output byte written to output
        int     inputSize = 0;      	   // Size of input
	int	outputSize = 0;	    	   // Size of output
	byte[]	tmpbuf = new byte[4];  

        // Record beginning offsets
        charOff = inOff;
        byteOff = outOff;

	if (highHalfZoneCode != 0) {
            inputChar = highHalfZoneCode;
            highHalfZoneCode = 0;
            if (input[inOff] >= 0xdc00 && input[inOff] <= 0xdfff) {
                // This is legal UTF16 sequence.
                badInputLength = 1;
                throw new UnknownCharacterException();
            } else {
                // This is illegal UTF16 sequence.
                badInputLength = 0;
                throw new MalformedInputException();
            }
        }

        // Loop until we hit the end of the input
        while(charOff < inEnd) {
            inputSize = 1;
	    outputByte = tmpbuf;
	    inputChar = input[charOff]; // Get the input character

            // Is this a high surrogate?
            if(inputChar >= '\uD800' && inputChar <= '\uDBFF') {
                // Is this the last character of the input?
                if (charOff + 1 >= inEnd) {
                    highHalfZoneCode = inputChar;
                    break;
                }

                // Is there a low surrogate following?
                inputChar = input[charOff + 1];
                if (inputChar >= '\uDC00' && inputChar <= '\uDFFF') {
                    // We have a valid surrogate pair.  Too bad we don't do
                    // surrogates.  Is substitution enabled?
                    if (subMode) {
                        outputByte = subBytes;
                        outputSize = subBytes.length;
                        inputSize = 2;
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
            else if (inputChar >= '\uDC00' && inputChar <= '\uDFFF') {
                badInputLength = 1;
                throw new MalformedInputException();
            } else {
		outputSize = convSingleByte(inputChar, outputByte);
		if (outputSize == 0) { // DoubleByte
	            int ncode = getNative(inputChar);
		    if (ncode != 0 && ((ncode & 0xFF0000) == 0)) {
		            outputByte[0] = (byte) ((ncode & 0xff00) >> 8);
		            outputByte[1] = (byte) (ncode & 0xff);
		            outputSize = 2;
            	    } else { 
		        if (subMode) {
		    	    outputByte = subBytes;
            	    	    outputSize = subBytes.length;
	                } else {
			    badInputLength = 1;
			    throw new UnknownCharacterException();
		        }
	    	    }
	        }
	    }

	    // If we don't have room for the output, throw an exception
	    if (byteOff + outputSize > outEnd)
		throw new ConversionBufferFullException();

	    // Put the byte in the output buffer
	    for (int i = 0; i < outputSize; i++) {
		output[byteOff++] = outputByte[i];
	    }
	    charOff += inputSize;
	}
        // Return the length written to the output buffer
        return byteOff - outOff;
    }


    /**
     * the maximum number of bytes needed to hold a converted char
     * @returns the maximum number of bytes needed for a converted char
     */
    public int getMaxBytesPerChar() {
        return 2;
    }
}
