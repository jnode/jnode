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
import  org.jnode.shell.help.Syntax;

/** Plays a system beep.
 *  @author Matt Paine
 */
public class BeepCommand
{

	public static final Help.Info HELP_INFO = new Help.Info("beep",	//  The command name
		new Syntax[]												//	The first syntax option
		{
			new Syntax("Plays a system beep")								//	No parameters
		});

	/** The main method
	 *  @param args The arguments for this command. NOTE: This command does not take any arguments.
	 **/
	public static void main(String[] args)
	{

		SpeakerUtils.beep();
	}
}
