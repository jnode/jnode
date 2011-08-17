package org.jnode.command.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;

/**
 * Compare two files and report the first difference.
 *
 * @author Levente S\u00e1ntha
 */
public class CmpCommand extends AbstractCommand {

    private static final int BUFFER_SIZE = 64 * 1024;

    private static final String HELP_SUPER = "Compare two files";
    private static final String HELP_FILE = "a file to compare";
    private static final String ERR_FILE_INVALID = "%s is not a file%n";
    private static final String MSG_DIFFER = "%s %s differ: byte %d, line %d%n";
    private static final String MSG_EOF = "cmp: EOF on %s%n";

    private final FileArgument file1Arg;
    private final FileArgument file2Arg;

    public CmpCommand() {
        super(HELP_SUPER);

        file1Arg = new FileArgument("file1", Argument.MANDATORY | Argument.EXISTING, HELP_FILE);
        file2Arg = new FileArgument("file2", Argument.MANDATORY | Argument.EXISTING, HELP_FILE);

        registerArguments(file1Arg, file2Arg);
    }

    public static void main(String[] args) throws Exception {
        new CmpCommand().execute(args);
    }

    @Override
    public void execute() throws IOException {

        File file1 = file1Arg.getValue();
        File file2 = file2Arg.getValue();

        PrintWriter err = getError().getPrintWriter();

        if (!file1.isFile()) {
            err.format(ERR_FILE_INVALID, file1);
            exit(1);
        }

        if (!file2.isFile()) {
            err.format(ERR_FILE_INVALID, file2);
            exit(1);
        }

        BufferedInputStream bis1 = null;
        BufferedInputStream bis2 = null;

        try {
            bis1 = new BufferedInputStream(new FileInputStream(file1), BUFFER_SIZE);
            bis2 = new BufferedInputStream(new FileInputStream(file2), BUFFER_SIZE);

            long bc = 1;
            long lc = 1;

            while (true) {
                int b1 = bis1.read();
                int b2 = bis2.read();

                if (b1 == -1 && b2 == -1)
                    //done
                    break;

                if (b1 == -1) {
                    PrintWriter out = getOutput().getPrintWriter();
                    out.format(MSG_EOF, file1.toString());
                    exit(1);
                    return;
                }

                if (b2 == -1) {
                    PrintWriter out = getOutput().getPrintWriter();
                    out.format(MSG_EOF, file2.toString());
                    exit(1);
                    return;
                }

                if (b1 != b2) {
                    PrintWriter out = getOutput().getPrintWriter();
                    out.format(MSG_DIFFER, file1.toString(), file2.toString(), bc, lc);
                    exit(1);
                    return;
                }

                bc++;

                if (b1 == (byte) '\n')
                    lc++;
            }
        } finally {
            if (bis1 != null) {
                try {
                    bis1.close();
                } catch (IOException x) {
                    //ignore
                }
            }

            if (bis2 != null) {
                try {
                    bis2.close();
                } catch (IOException x) {
                    //ignore
                }
            }
        }
    }
}
