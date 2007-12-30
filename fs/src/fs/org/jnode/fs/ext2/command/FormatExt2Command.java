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

package org.jnode.fs.ext2.command;

import org.jnode.fs.command.AbstractFormatCommand;
import org.jnode.fs.ext2.Ext2FileSystem;
import org.jnode.fs.ext2.Ext2FileSystemFormatter;
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
public class FormatExt2Command extends AbstractFormatCommand<Ext2FileSystem> {
    static final OptionArgument BS_VAL = new OptionArgument("blocksize",
            "block size for ext2 filesystem", new OptionArgument.Option[] {
                    new OptionArgument.Option("1", "1Kb"),
                    new OptionArgument.Option("2", "2Kb"),
                    new OptionArgument.Option("4", "4Kb"), });

    static final Parameter PARAM_BS_VAL = new Parameter(BS_VAL,
            Parameter.OPTIONAL);

    public static Help.Info HELP_INFO = new Help.Info("format",
            new Syntax[] { new Syntax(
                    "Format a block device with ext2 filesystem",
                    new Parameter[] { PARAM_DEVICE, PARAM_BS_VAL }) });

    public static void main(String[] args) throws Exception {
    	new FormatExt2Command().execute(args);
    }

	@Override
	protected Ext2FileSystemFormatter getFormatter(ParsedArguments cmdLine) {
        int bsize;
        try {
            bsize = Integer.parseInt(BS_VAL.getValue(cmdLine));
        } catch (NumberFormatException nfe) {
            bsize = 4;
        }

        return new Ext2FileSystemFormatter(bsize);
	}

	@Override
	protected ParsedArguments parse(CommandLine commandLine)
			throws SyntaxErrorException {
		return HELP_INFO.parse(commandLine);
	}
}
