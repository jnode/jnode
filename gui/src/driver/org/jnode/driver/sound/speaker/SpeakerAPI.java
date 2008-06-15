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

package org.jnode.driver.sound.speaker;

import org.jnode.driver.DeviceAPI;

/**
 * This API defines how a speaker should be interfaced with JNode.
 * 
 * @author Matt Paine
 */
public interface SpeakerAPI extends DeviceAPI {

    /** Plays a simple beep * */
    public void beep();

    /**
     * Plays a single Note.
     * 
     * @param n The note to play
     */
    public void playNote(Note n);

    /**
     * Plays a series of notes.
     * 
     * @param n The arraw of notes to play
     */
    public void playNote(Note[] n);

}
