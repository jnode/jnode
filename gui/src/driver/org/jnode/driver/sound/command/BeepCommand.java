
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
