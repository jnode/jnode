
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
