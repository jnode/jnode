
package org.jnode.driver.sound.speaker;
import  org.jnode.driver.DeviceAPI;

/** This API defines how a speaker should be interfaced with JNode.
 *  @author Matt Paine
 **/
public interface SpeakerAPI extends DeviceAPI
{

	/** Plays a simple beep **/
	public void beep();

	/** Plays a single Note.
	 *  @param n The note to play
	 **/
	public void playNote (Note n);

	/** Plays a series of notes.
	 *  @param n The arraw of notes to play
	 **/
	public void playNote (Note[] n);

}

