/*
 * $Id: FormatCommand.java 3585 2007-11-13 13:31:18Z galatnm $
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

package org.jnode.fs.fat.command;

import org.jnode.fs.command.AbstractFormatCommand;
import org.jnode.fs.fat.Fat;
import org.jnode.fs.fat.FatFileSystem;
import org.jnode.fs.fat.FatFileSystemFormatter;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.Syntax;
import org.jnode.shell.help.SyntaxErrorException;
import org.jnode.shell.help.argument.OptionArgument;

/**
 * @author gbin
 */
public class FormatFatCommand extends AbstractFormatCommand<FatFileSystem> {

    static final OptionArgument FS = new OptionArgument("fstype",
            "FAT type", new OptionArgument.Option[] {
                    new OptionArgument.Option("fat16", "FAT 16 filesystem"),
                    new OptionArgument.Option("fat12", "FAT 12 filesystem")});

    static final Parameter PARAM_FS = new Parameter(FS, Parameter.MANDATORY);

    public static Help.Info HELP_INFO = new Help.Info("format",
            new Syntax[] { new Syntax(
                    "Format a block device with fat filesystem",
                    new Parameter[] { PARAM_DEVICE, PARAM_FS}) });

    public static void main(String[] args) throws Exception {
    	new FormatFatCommand().execute(args);
    }

	@Override
	protected FatFileSystemFormatter getFormatter(ParsedArguments cmdLine) {
        String FSType = FS.getValue(cmdLine).intern();

        FatFileSystemFormatter formatter = null;
        if (FSType == "fat16") {
            formatter = new FatFileSystemFormatter(Fat.FAT16);
        } else if (FSType == "fat12") {
            formatter = new FatFileSystemFormatter(Fat.FAT32);
        } else
            throw new IllegalArgumentException(
                    "invalid fat type");

        return formatter;
	}

	@Override
	protected ParsedArguments parse(CommandLine commandLine)
			throws SyntaxErrorException {
		return HELP_INFO.parse(commandLine);
	}
}
