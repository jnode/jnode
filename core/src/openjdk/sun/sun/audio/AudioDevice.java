/*
 * Copyright 1999-2003 Sun Microsystems, Inc.  All Rights Reserved.
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

package sun.audio;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;

import javax.sound.sampled.*;
import javax.sound.midi.*;
import com.sun.media.sound.DataPusher;
import com.sun.media.sound.Toolkit;

/**
 * This class provides an interface to the Headspace Audio engine through
 * the Java Sound API.
 *
 * This class emulates systems with multiple audio channels, mixing
 * multiple streams for the workstation's single-channel device.
 *
 * @see AudioData
 * @see AudioDataStream
 * @see AudioStream
 * @see AudioStreamSequence
 * @see ContinuousAudioDataStream
 * @author David Rivas
 * @author Kara Kytle
 * @author Jan Borgersen
 * @author Florian Bomers
 */

public class
    AudioDevice {

    private boolean DEBUG = false  /*true*/ ;

    /** Hashtable of audio clips / input streams. */
    private Hashtable clipStreams;

    private Vector infos;

    /** Are we currently playing audio? */
    private boolean playing = false;

    /** Handle to the JS audio mixer. */
    private Mixer mixer = null;



    /**
     * The default audio player. This audio player is initialized
     * automatically.
     */
    public static final AudioDevice device = new AudioDevice();

    /**
     * Create an AudioDevice instance.
     */
    private AudioDevice() {

	clipStreams = new Hashtable();
	infos = new Vector();
    }


    private synchronized void startSampled( AudioInputStream as,
					    InputStream in ) throws UnsupportedAudioFileException,
				  LineUnavailableException {

	Info info = null;
	DataPusher datapusher = null;
	DataLine.Info lineinfo = null;
	SourceDataLine sourcedataline = null;

	// if ALAW or ULAW, we must convert....
	as = Toolkit.getPCMConvertedAudioInputStream(as);

	if( as==null ) {
	    // could not convert
	    return;
	}

	lineinfo = new DataLine.Info(SourceDataLine.class,
				     as.getFormat());
	if( !(AudioSystem.isLineSupported(lineinfo))) {
	    return;
	}
	sourcedataline = (SourceDataLine)AudioSystem.getLine(lineinfo);
	datapusher = new DataPusher(sourcedataline, as);

	info = new Info( null, in, datapusher );
	infos.addElement( info );

	datapusher.start();
    }

    private synchronized void startMidi( InputStream bis,
					 InputStream in ) throws InvalidMidiDataException,
				  MidiUnavailableException  {

	Sequencer sequencer = null;
	Info info = null;

	sequencer = MidiSystem.getSequencer( );
	sequencer.open();
	try {
	    sequencer.setSequence( bis );
	} catch( IOException e ) {
	    throw new InvalidMidiDataException( e.getMessage() );
	}

	info = new Info( sequencer, in, null );

	infos.addElement( info );

	// fix for bug 4302884: Audio device is not released when AudioClip stops
	sequencer.addMetaEventListener(info);

	sequencer.start();

    }



    /**
     *  Open an audio channel.
     */
    public synchronized void openChannel(InputStream in) {


	if(DEBUG) {
	    System.out.println("AudioDevice: openChannel");
	    System.out.println("input stream =" + in);
	}

	Info info = null;

	// is this already playing?  if so, then just return
	for(int i=0; i<infos.size(); i++) {
	    info = (AudioDevice.Info)infos.elementAt(i);
	    if( info.in == in ) {

		return;
	    }
	}


	AudioInputStream as = null;

	if( in instanceof AudioStream ) {

	    if ( ((AudioStream)in).midiformat != null ) {

		// it's a midi file
		try {
		    startMidi( ((AudioStream)in).stream, in );
		} catch (Exception e) {
		    return;
		}


	    } else if( ((AudioStream)in).ais != null ) {

		// it's sampled audio
		try {
		    startSampled( ((AudioStream)in).ais, in );
		} catch (Exception e) {
		    return;
		}

	    }
	} else if (in instanceof AudioDataStream ) {
	    if (in instanceof ContinuousAudioDataStream) {
		try {
		    AudioInputStream ais = new AudioInputStream(in,
								((AudioDataStream)in).getAudioData().format,
								AudioSystem.NOT_SPECIFIED);
		    startSampled(ais, in );
		} catch (Exception e) {
		    return;
		}
	    }
	    else {
		try {
		    AudioInputStream ais = new AudioInputStream(in,
								((AudioDataStream)in).getAudioData().format,
								((AudioDataStream)in).getAudioData().buffer.length);
		    startSampled(ais, in );
		} catch (Exception e) {
		    return;
		}
	    }
	} else {
	    BufferedInputStream bis = new BufferedInputStream( in, 1024 );

	    try {

		try {
		    as = AudioSystem.getAudioInputStream(bis);
		} catch(IOException ioe) {
		    return;
		}

		startSampled( as, in );

	    } catch( UnsupportedAudioFileException e ) {

		try {
		    try {
			MidiFileFormat mff =
			    MidiSystem.getMidiFileFormat( bis );
		    } catch(IOException ioe1) {
			return;
		    }

		    startMidi( bis, in );


		} catch( InvalidMidiDataException e1 ) {

		    // $$jb:08.01.99: adding this section to make some of our other
		    // legacy classes work.....
		    // not MIDI either, special case handling for all others

		    AudioFormat defformat = new AudioFormat( AudioFormat.Encoding.ULAW,
							     8000, 8, 1, 1, 8000, true );
		    try {
			AudioInputStream defaif = new AudioInputStream( bis,
									defformat, AudioSystem.NOT_SPECIFIED);
			startSampled( defaif, in );
		    } catch (UnsupportedAudioFileException es) {
			return;
		    } catch (LineUnavailableException es2) {
			return;
		    }

		} catch( MidiUnavailableException e2 ) {

		    // could not open sequence
		    return;
		}

	    } catch( LineUnavailableException e ) {

		return;
	    }
	}

	// don't forget adjust for a new stream.
	notify();
    }


    /**
     *  Close an audio channel.
     */
    public synchronized void closeChannel(InputStream in) {

	if(DEBUG) {
	    System.out.println("AudioDevice.closeChannel");
	}

	if (in == null) return;		// can't go anywhere here!

	Info info;

	for(int i=0; i<infos.size(); i++) {

	    info = (AudioDevice.Info)infos.elementAt(i);

	    if( info.in == in ) {

		if( info.sequencer != null ) {

		    info.sequencer.stop();
		    //info.sequencer.close();
		    infos.removeElement( info );

		} else if( info.datapusher != null ) {

		    info.datapusher.stop();
		    infos.removeElement( info );
		}
	    }
	}
	notify();
    }


    /**
     * Open the device (done automatically)
     */
    public synchronized void open() {

	// $$jb: 06.24.99: This is done on a per-stream
	// basis using the new JS API now.
    }


    /**
     * Close the device (done automatically)
     */
    public synchronized void close() {

	// $$jb: 06.24.99: This is done on a per-stream
	// basis using the new JS API now.

    }


    /**
     * Play open audio stream(s)
     */
    public void play() {

	// $$jb: 06.24.99:  Holdover from old architechture ...
	// we now open/close the devices as needed on a per-stream
	// basis using the JavaSound API.

	if (DEBUG) {
	    System.out.println("exiting play()");
	}
    }

    /**
     * Close streams
     */
    public synchronized void closeStreams() {

	Info info;

	for(int i=0; i<infos.size(); i++) {

	    info = (AudioDevice.Info)infos.elementAt(i);

	    if( info.sequencer != null ) {

		info.sequencer.stop();
		info.sequencer.close();
		infos.removeElement( info );

	    } else if( info.datapusher != null ) {

		info.datapusher.stop();
		infos.removeElement( info );
	    }
	}


	if (DEBUG) {
	    System.err.println("Audio Device: Streams all closed.");
	}
	// Empty the hash table.
	clipStreams = new Hashtable();
	infos = new Vector();
    }

    /**
     * Number of channels currently open.
     */
    public int openChannels() {
	return infos.size();
    }

    /**
     * Make the debug info print out.
     */
    void setVerbose(boolean v) {
	DEBUG = v;
    }






    // INFO CLASS

    class Info implements MetaEventListener {

	Sequencer   sequencer;
	InputStream in;
	DataPusher  datapusher;
	
	Info( Sequencer sequencer, InputStream in, DataPusher datapusher ) {
	    
	    this.sequencer  = sequencer;
	    this.in         = in;
	    this.datapusher = datapusher;
	}

	public void meta(MetaMessage event) {
	    if (event.getType() == 47 && sequencer != null) {
		sequencer.close();
	    }
	}
    }



}
