package org.jnode.fs.hfsplus.command;

import org.jnode.fs.Formatter;
import org.jnode.fs.command.AbstractFormatCommand;
import org.jnode.fs.hfsplus.HFSPlusParams;
import org.jnode.fs.hfsplus.HfsPlusConstants;
import org.jnode.fs.hfsplus.HfsPlusFileSystem;
import org.jnode.fs.hfsplus.HfsPlusFileSystemFormatter;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.StringArgument;

public class FormatHfsPlusCommand extends AbstractFormatCommand<HfsPlusFileSystem> {
    
    private final StringArgument ARG_VOLUME_NAME =
        new StringArgument("volumename", Argument.OPTIONAL, "set volume name");

    public FormatHfsPlusCommand() {
        super("Format a block device with HFS+ filesystem");
        registerArguments(ARG_VOLUME_NAME);
    }

    public static void main(String[] args) throws Exception {
        new FormatHfsPlusCommand().execute(args);
    }
    
    @Override
    protected Formatter<HfsPlusFileSystem> getFormatter() {
        HFSPlusParams params = new HFSPlusParams();
        params.setVolumeName(ARG_VOLUME_NAME.getValue());
        params.setBlockSize(params.OPTIMAL_BLOCK_SIZE);
        params.setJournaled(false);
        params.setJournalSize(params.DEFAULT_JOURNAL_SIZE);
        return new HfsPlusFileSystemFormatter(params);
    }
}
