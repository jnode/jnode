/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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
 
package org.jnode.driver.sound.speaker;

import javax.naming.NameNotFoundException;

import org.apache.log4j.Logger;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceNotFoundException;
import org.jnode.naming.InitialNaming;

/**
 * A static helper class for easy access to the system speaker.
 * 
 * @author Matt Paine
 */
public class SpeakerUtils {

    /** My logger */
    private static final Logger log = Logger.getLogger(SpeakerUtils.class);

    /** The length of a standard interval * */
    public static final int INTERVAL = 500;

    /** A full length note * */
    public static final int FULL = INTERVAL;

    /** A half length note * */
    public static final int HALF = INTERVAL / 2;

    /** A quarter length note * */
    public static final int QUART = INTERVAL / 4;

    /** A three-quater length note * */
    public static final int THREEQUART = (int) (INTERVAL * 0.75);

    /** Sounds a beep on the system speaker * */
    public static void beep() {
        play(stdBeep);
    }

    /**
     * Plays a series of notes through the default speaker.
     * Null argument or zero length array plays the standard beep.
     */
    public static void play(Note[] n) {
        try {
            DeviceManager dm = InitialNaming.lookup(DeviceManager.NAME);
            Device dev = dm.getDevice("speaker0");
            SpeakerAPI s = dev.getAPI(SpeakerAPI.class);
            if (n == null || n.length == 0) {
                s.beep();
            } else {
                s.playNote(n);
            }
        } catch (ApiNotFoundException anfex) {
            log.error("Unable to beep: ", anfex);
        } catch (DeviceNotFoundException dnfex) {
            log.error("Unable to beep: ", dnfex);
        } catch (NameNotFoundException nnfex) {
            log.debug("Unable to beep: ", nnfex);
        }

    }

    /** What to play as the standard beep (null = a simple beep) * */
    public static Note[] stdBeep = new Note[0];

    /** Defines a scale * */
    public static final Note[] SCALE =
            new Note[] {new Note(Note.NOTE_C4, HALF), new Note(Note.NOTE_D4, HALF),
                new Note(Note.NOTE_E4, HALF), new Note(Note.NOTE_F4, HALF),
                new Note(Note.NOTE_G4, HALF), new Note(Note.NOTE_A4, HALF),
                new Note(Note.NOTE_B4, HALF), new Note(Note.NOTE_C5, HALF),
                new Note(Note.NOTE_D5, HALF), new Note(Note.NOTE_E5, HALF),
                new Note(Note.NOTE_F5, HALF), new Note(Note.NOTE_G5, HALF),
                new Note(Note.NOTE_A5, HALF), new Note(Note.NOTE_B5, HALF),
                new Note(Note.NOTE_C6, HALF)};

    /** Advance Australia Fair * */
    public static final Note[] AAF =
            new Note[] {
                new Note(Note.NOTE_G4, HALF), // australians
                new Note(Note.NOTE_C5, HALF),
                new Note(Note.NOTE_G4, HALF),
                new Note(Note.NOTE_E4, HALF),
                new Note(Note.NOTE_G4, HALF),
                new Note(Note.NOTE_C5, THREEQUART),
                new Note(Note.NOTE_C5, QUART),
                new Note(Note.NOTE_C5, HALF),
                new Note(Note.NOTE_E5, HALF), // for
                new Note(Note.NOTE_D5, HALF),
                new Note(Note.NOTE_C5, HALF),
                new Note(Note.NOTE_B4, HALF),
                new Note(Note.NOTE_C5, HALF),
                new Note(Note.NOTE_D5, FULL + THREEQUART),
                new Note(Note.NOTE_G4, HALF), // with
                new Note(Note.NOTE_C5, HALF),
                new Note(Note.NOTE_G4, HALF),
                new Note(Note.NOTE_E4, HALF),
                new Note(Note.NOTE_C4, HALF),
                new Note(Note.NOTE_G4, THREEQUART),
                new Note(Note.NOTE_G4, QUART),
                new Note(Note.NOTE_G4, HALF),
                new Note(Note.NOTE_E5, HALF), // our home
                new Note(Note.NOTE_D5, HALF),
                new Note(Note.NOTE_C5, HALF),
                new Note(Note.NOTE_B4, HALF),
                new Note(Note.NOTE_A4, HALF),
                new Note(Note.NOTE_G4, FULL + THREEQUART),
                new Note(Note.NOTE_G4, HALF), // our lands
                new Note(Note.NOTE_A4, THREEQUART),
                new Note(Note.NOTE_B4, QUART),
                new Note(Note.NOTE_C5, HALF),
                new Note(Note.NOTE_A4, HALF),
                new Note(Note.NOTE_G4, THREEQUART),
                new Note(Note.NOTE_E4, QUART),
                new Note(Note.NOTE_E4, HALF),
                new Note(Note.NOTE_G4, HALF), // of beuty
                new Note(Note.NOTE_A4, HALF),
                new Note(Note.NOTE_C5, HALF),
                new Note(Note.NOTE_F5, HALF),
                new Note(Note.NOTE_E5, HALF),
                new Note(Note.NOTE_D5, FULL + THREEQUART),
                new Note(Note.NOTE_G4, HALF), // in history
                new Note(Note.NOTE_A4, THREEQUART),
                new Note(Note.NOTE_B4, QUART),
                new Note(Note.NOTE_C5, HALF),
                new Note(Note.NOTE_A4, HALF),
                new Note(Note.NOTE_G4, THREEQUART),
                new Note(Note.NOTE_C5, QUART),
                new Note(Note.NOTE_C5, HALF),
                new Note(Note.NOTE_D5, HALF), // advance
                new Note(Note.NOTE_E5, THREEQUART),
                new Note(Note.NOTE_C5, QUART),
                new Note(Note.NOTE_D5, THREEQUART),
                new Note(Note.NOTE_B4, QUART),
                new Note(Note.NOTE_C5, FULL + THREEQUART),
                new Note(Note.NOTE_E5, HALF), // in joyful
                new Note(Note.NOTE_F5, HALF), new Note(Note.NOTE_E5, HALF),
                new Note(Note.NOTE_D5, HALF), new Note(Note.NOTE_C5, HALF),
                new Note(Note.NOTE_B4, HALF),
                new Note(Note.NOTE_A4, HALF),
                new Note(Note.NOTE_G4, HALF),
                new Note(Note.NOTE_C5, HALF), // advance
                new Note(Note.NOTE_E5, THREEQUART), new Note(Note.NOTE_C5, QUART),
                new Note(Note.NOTE_D5, THREEQUART), new Note(Note.NOTE_B4, QUART),
                new Note(Note.NOTE_C5, FULL + THREEQUART)};

}
