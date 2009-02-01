/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
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
 
package org.jnode.fs.hfsplus.command;

import org.jnode.fs.Formatter;
import org.jnode.fs.command.AbstractFormatCommand;
import org.jnode.fs.hfsplus.HFSPlusParams;
import org.jnode.fs.hfsplus.HfsPlusFileSystem;
import org.jnode.fs.hfsplus.HfsPlusFileSystemFormatter;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.StringArgument;

public class FormatHfsPlusCommand extends AbstractFormatCommand<HfsPlusFileSystem> {

    private final StringArgument ARG_VOLUME_NAME = new StringArgument("volumeName", Argument.OPTIONAL,
            "set volume name");

    public FormatHfsPlusCommand() {
        super("Format a block device with HFS+ filesystem");
        registerArguments(ARG_VOLUME_NAME);
    }

    public static void main(String[] args)
        throws Exception {
        new FormatHfsPlusCommand().execute(args);
    }

    @Override
    protected Formatter<HfsPlusFileSystem> getFormatter() {
        HFSPlusParams params = new HFSPlusParams();
        params.setVolumeName((ARG_VOLUME_NAME.isSet()) ? ARG_VOLUME_NAME.getValue() : "untitled");
        params.setBlockSize(params.OPTIMAL_BLOCK_SIZE);
        params.setJournaled(false);
        params.setJournalSize(params.DEFAULT_JOURNAL_SIZE);
        return new HfsPlusFileSystemFormatter(params);
    }
}
