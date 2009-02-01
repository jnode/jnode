/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.driver.sound.command;

import java.util.HashMap;

import org.jnode.driver.sound.speaker.Note;
import org.jnode.driver.sound.speaker.SpeakerUtils;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.MappedArgument;

/**
 * This command plays a tune.
 * 
 * @author Matt Paine
 */
public class PlayCommand extends AbstractCommand {

    private static final HashMap<String, Note[]> TUNES = new HashMap<String, Note[]>();

    static {
        TUNES.put("scale", SpeakerUtils.SCALE);
        TUNES.put("aaf", SpeakerUtils.AAF);
        TUNES.put("beep", SpeakerUtils.stdBeep);
    }

    private static class TuneArgument extends MappedArgument<Note[]> {
        public TuneArgument(String label, int flags, String description) {
            super(label, flags, new Note[0][], TUNES, false, description);
        }

        @Override
        protected String argumentKind() {
            return "tune";
        }
    }

    private final TuneArgument ARG_TUNE =
            new TuneArgument("tune", Argument.OPTIONAL, "The name of the tune to be played");

    public PlayCommand() {
        super("Plays a tune");
        registerArguments(ARG_TUNE);
    }

    public static void main(String[] args) throws Exception {
        new PlayCommand().execute(args);
    }

    public void execute() {
        Note[] tune = ARG_TUNE.isSet() ? ARG_TUNE.getValue() : SpeakerUtils.SCALE;
        SpeakerUtils.play(tune);
    }
}
