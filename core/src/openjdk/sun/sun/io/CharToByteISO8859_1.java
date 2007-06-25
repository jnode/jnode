/*
 * Copyright 1996-1998 Sun Microsystems, Inc.  All Rights Reserved.
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

public class CharToByteISO8859_1 extends CharToByteConverter {

    // Return the character set ID
    public String getCharacterEncoding()
    {
        return "ISO8859_1";
    }

    private char highHalfZoneCode;
    
    public int flush(byte[] output, int outStart, int outEnd)
	throws MalformedInputException
    {
	if (highHalfZoneCode != 0) {
	    highHalfZoneCode = 0;
	    throw new MalformedInputException
		("String ends with <High Half Zone code> of UTF16");
	}
	byteOff = charOff = 0;
	return 0;
    }

    /*
    * Character conversion
    */
    public int convert(char[] input, int inOff, int inEnd,
		       byte[] output, int outOff, int outEnd)
        throws MalformedInputException,
               UnknownCharacterException,
               ConversionBufferFullException

    {

        char    inputChar;          // Input character to be converted
        byte[]  outputByte;         // Output byte written to output
	byte[]  tmpArray = new byte[1];
        int     inputSize;          // Size of input
	int	outputSize;	    // Size of output	

        // Record beginning offsets
        charOff = inOff;
        byteOff = outOff;

	if (highHalfZoneCode != 0) {
	    inputChar = highHalfZoneCode;
	    highHalfZoneCode = 0;
	    if (input[inOff] >= 0xdc00 && input[inOff] <= 0xdfff) {
		// This is legal UTF16 sequence.
		if (subMode) { 
		    outputSize = subBytes.length;
		    if (byteOff + outputSize > outEnd)
			throw new ConversionBufferFullException();
		    for(int i = 0; i < outputSize; i++)
			output[byteOff++] = subBytes[i];
		    charOff += 1;
		} else {
		    badInputLength = 1;
		    throw new UnknownCharacterException();
		}
	    } else {
		// This is illegal UTF16 sequence.
		badInputLength = 0;
		throw new MalformedInputException
		    ("Previous converted string ends with " +
		     "<High Half Zone Code> of UTF16 " +
		     ", but this string is not begin with <Low Half Zone>");
	    }
	}

        // Loop until we hit the end of the input
        while(charOff < inEnd) {
	    outputByte = tmpArray;

            // Get the input character
            inputChar = input[charOff];

	    // default outputSize
	    outputSize = 1;

            // Assume this is a simple character
            inputSize = 1;

            // Is this a high surrogate?
            if(inputChar >= '\uD800' && inputChar <= '\uDBFF') {
                // Is this the last character in the input?
                if (charOff + 1 == inEnd) {
		    highHalfZoneCode = inputChar;
		    break;
                }

                // Is there a low surrogate following?
                inputChar = input[charOff + 1];
                if (inputChar >= '\uDC00' && inputChar <= '\uDFFF') {
                    // We have a valid surrogate pair.  Too bad we don't map
                    //  surrogates.  Is substitution enabled?
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
            }
            // Not part of a surrogate, so try to convert
            else {
                // Is this character mappable?
                if (inputChar <= '\u00FF') {
                	outputByte[0] = (byte)inputChar;
                } else {
                    // Is substitution enabled?
                    if (subMode) {
                        outputByte = subBytes;
			outputSize = subBytes.length;
		    } else {
                        badInputLength = 1;
                        throw new UnknownCharacterException();
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
        return byteOff-outOff;
    }

    // Determine if a character is mappable or not
    public boolean canConvert(char ch)
    {
        return (ch <= '\u00FF');
    }

    // Reset the converter
    public void reset()
    {
	byteOff = charOff = 0;
	highHalfZoneCode = 0;
    }

    /**
    * returns the maximum number of bytes needed to convert a char
    */
    public int getMaxBytesPerChar()
    {
        return 1;
    }


}
