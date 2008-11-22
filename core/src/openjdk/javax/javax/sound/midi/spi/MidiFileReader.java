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
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;

import javax.sound.midi.MidiFileFormat;
import javax.sound.midi.Sequence;
import javax.sound.midi.InvalidMidiDataException;

/**
 * A <code>MidiFileReader</code> supplies MIDI file-reading services.  Classes implementing this
 * interface can parse the format information from one or more types of
 * MIDI file, and can produce a <code>Sequence</code> object from files of these types.
 *
 * @author Kara Kytle
 * @since 1.3
 */
public abstract class MidiFileReader {

    /**
     * Obtains the MIDI file format of the input stream provided.  The stream must
     * point to valid MIDI file data.  In general, MIDI file readers may 
     * need to read some data from the stream before determining whether they
     * support it.  These parsers must
     * be able to mark the stream, read enough data to determine whether they 
     * support the stream, and, if not, reset the stream's read pointer to its original 
     * position.  If the input stream does not support this, this method may fail
     * with an <code>IOException</code>. 
     * @param stream the input stream from which file format information should be
     * extracted
     * @return a <code>MidiFileFormat</code> object describing the MIDI file format
     * @throws InvalidMidiDataException if the stream does not point to valid MIDI
     * file data recognized by the system
     * @throws IOException if an I/O exception occurs
     * @see InputStream#markSupported
     * @see InputStream#mark
     */
    public abstract MidiFileFormat getMidiFileFormat(InputStream stream) throws InvalidMidiDataException, IOException;


    /**
     * Obtains the MIDI file format of the URL provided.  The URL must
     * point to valid MIDI file data.
     * @param url the URL from which file format information should be
     * extracted
     * @return a <code>MidiFileFormat</code> object describing the MIDI file format
     * @throws InvalidMidiDataException if the URL does not point to valid MIDI
     * file data recognized by the system
     * @throws IOException if an I/O exception occurs
     */
    public abstract MidiFileFormat getMidiFileFormat(URL url) throws InvalidMidiDataException, IOException;


    /**
     * Obtains the MIDI file format of the <code>File</code> provided.  
     * The <code>File</code> must point to valid MIDI file data.
     * @param file the <code>File</code> from which file format information should be
     * extracted
     * @return a <code>MidiFileFormat</code> object describing the MIDI file format
     * @throws InvalidMidiDataException if the <code>File</code> does not point to valid MIDI
     * file data recognized by the system
     * @throws IOException if an I/O exception occurs
     */
    public abstract MidiFileFormat getMidiFileFormat(File file) throws InvalidMidiDataException, IOException;


    /**
     * Obtains a MIDI sequence from the input stream provided.  The stream must
     * point to valid MIDI file data.  In general, MIDI file readers may 
     * need to read some data from the stream before determining whether they
     * support it.  These parsers must
     * be able to mark the stream, read enough data to determine whether they 
     * support the stream, and, if not, reset the stream's read pointer to its original 
     * position.  If the input stream does not support this, this method may fail
     * with an IOException. 
     * @param stream the input stream from which the <code>Sequence</code> should be
     * constructed
     * @return a <code>Sequence</code> object based on the MIDI file data contained
     * in the input stream.
     * @throws InvalidMidiDataException if the stream does not point to valid MIDI
     * file data recognized by the system
     * @throws IOException if an I/O exception occurs
     * @see InputStream#markSupported
     * @see InputStream#mark
     */
    public abstract Sequence getSequence(InputStream stream) throws InvalidMidiDataException, IOException;


    /**
     * Obtains a MIDI sequence from the URL provided.  The URL must
     * point to valid MIDI file data.
     * @param url the URL for which the <code>Sequence</code> should be
     * constructed
     * @return a <code>Sequence</code> object based on the MIDI file data pointed
     * to by the URL
     * @throws InvalidMidiDataException if the URL does not point to valid MIDI
     * file data recognized by the system
     * @throws IOException if an I/O exception occurs
     */
    public abstract Sequence getSequence(URL url) throws InvalidMidiDataException, IOException;


    /**
     * Obtains a MIDI sequence from the <code>File</code> provided.  The <code>File</code> must
     * point to valid MIDI file data.
     * @param file the <code>File</code> from which the <code>Sequence</code> should be
     * constructed
     * @return a <code>Sequence</code> object based on the MIDI file data pointed
     * to by the <code>File</code>
     * @throws InvalidMidiDataException if the <code>File</code> does not point to valid MIDI
     * file data recognized by the system
     * @throws IOException if an I/O exception occurs
     */
    public abstract Sequence getSequence(File file) throws InvalidMidiDataException, IOException;
}
