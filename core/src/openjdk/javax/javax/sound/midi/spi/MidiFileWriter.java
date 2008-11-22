/*
 * Copyright 1999-2002 Sun Microsystems, Inc.  All Rights Reserved.
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

package javax.sound.midi.spi;		  	 

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.sound.midi.Sequence;
import javax.sound.midi.MidiFileFormat;

/**
 * A <code>MidiFileWriter</code> supplies MIDI file-writing services.  Classes 
 * that implement this interface can write one or more types of MIDI file from 
 * a <code>{@link Sequence}</code> object.
 *
 * @author Kara Kytle
 * @since 1.3
 */
public abstract class MidiFileWriter {


    /**
     * Obtains the set of MIDI file types for which file writing support is 
     * provided by this file writer.
     * @return array of file types.  If no file types are supported, 
     * an array of length 0 is returned.
     */
    public abstract int[] getMidiFileTypes();


    /**
     * Obtains the file types that this file writer can write from the
     * sequence specified.
     * @param sequence the sequence for which MIDI file type support
     * is queried
     * @return array of file types.  If no file types are supported, 
     * returns an array of length 0.
     */
    public abstract int[] getMidiFileTypes(Sequence sequence);


    /**
     * Indicates whether file writing support for the specified MIDI file type 
     * is provided by this file writer.
     * @param fileType the file type for which write capabilities are queried
     * @return <code>true</code> if the file type is supported, 
     * otherwise <code>false</code>
     */
    public boolean isFileTypeSupported(int fileType) {

	int types[] = getMidiFileTypes();
	for(int i=0; i<types.length; i++) {
	    if( fileType == types[i] ) {
		return true;
	    }
	}
	return false;
    }


    /**
     * Indicates whether a MIDI file of the file type specified can be written
     * from the sequence indicated.
     * @param fileType the file type for which write capabilities are queried
     * @param sequence  the sequence for which file writing support is queried
     * @return <code>true</code> if the file type is supported for this sequence, 
     * otherwise <code>false</code>
     */
    public boolean isFileTypeSupported(int fileType, Sequence sequence) {

	int types[] = getMidiFileTypes( sequence );
	for(int i=0; i<types.length; i++) {
	    if( fileType == types[i] ) {
		return true;
	    }
	}
	return false;
    }


    /**
     * Writes a stream of bytes representing a MIDI file of the file type
     * indicated to the output stream provided. 
     * @param in sequence containing MIDI data to be written to the file
     * @param fileType type of the file to be written to the output stream
     * @param out stream to which the file data should be written
     * @return the number of bytes written to the output stream
     * @throws IOException if an I/O exception occurs
     * @throws IllegalArgumentException if the file type is not supported by
     * this file writer
     * @see #isFileTypeSupported(int, Sequence)
     * @see	#getMidiFileTypes(Sequence)
     */
    public abstract int write(Sequence in, int fileType, OutputStream out) throws IOException;


    /**
     * Writes a stream of bytes representing a MIDI file of the file type
     * indicated to the external file provided.
     * @param in sequence containing MIDI data to be written to the external file
     * @param fileType type of the file to be written to the external file
     * @param out external file to which the file data should be written
     * @return the number of bytes written to the file
     * @throws IOException if an I/O exception occurs
     * @throws IllegalArgumentException if the file type is not supported by
     * this file writer
     * @see #isFileTypeSupported(int, Sequence)
     * @see	#getMidiFileTypes(Sequence)
     */
    public abstract int write(Sequence in, int fileType, File out) throws IOException;
}
