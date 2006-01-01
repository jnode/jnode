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
 * You should have received a copy of the GNU General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.driver.sound.command;
import  org.jnode.driver.sound.speaker.SpeakerUtils;
import  org.jnode.shell.help.Help;
import  org.jnode.shell.help.Parameter;
import  org.jnode.shell.help.Argument;

/** Plays a song.
 *  @author Matt Paine
 */
public class PlayCommand
{

	public static final Help.Info HELP_INFO = new Help.Info("play",		//  The command name
		"Plays a song",													//  description
		new Parameter[]													//  The parameters
		{
			new Parameter(new Argument("aaf", "Plays Advance Australia Fair"), Parameter.OPTIONAL)
		});

	/** The main method
	 *  @param args The arguments for this command.
	 **/
	public static void main(String[] args)
	{
		if (args.length == 0)
			SpeakerUtils.play (SpeakerUtils.SCALE);

		else if ("aaf".equals(args[0]))
			SpeakerUtils.play(SpeakerUtils.AAF);
		else
			SpeakerUtils.play(SpeakerUtils.SCALE);
	}
}
