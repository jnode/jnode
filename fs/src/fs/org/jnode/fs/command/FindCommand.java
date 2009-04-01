package org.jnode.fs.command;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.LongArgument;
import org.jnode.shell.syntax.StringArgument;

public class FindCommand extends AbstractCommand {

    private class Walker extends AbstractDirectoryWalker {

        @Override
        public void handleDir(File f) {
            out.println(f);
        }

        @Override
        public void handleFile(File f) {
            out.println(f);
        }

    }

    private final StringArgument nameArg = new StringArgument("name", Argument.OPTIONAL);
    private final StringArgument inameArg = new StringArgument("iname", Argument.OPTIONAL);
    private final LongArgument maxdepthArg = new LongArgument("maxdepth", Argument.OPTIONAL);
    private final LongArgument mindepthArg = new LongArgument("mindepth", Argument.OPTIONAL);
    private final StringArgument typeArg = new StringArgument("type", Argument.OPTIONAL);
    private final FileArgument dirArg =
        new FileArgument("directory", Argument.MANDATORY | Argument.EXISTING
            | Argument.MULTIPLE);
    private PrintWriter out = null;

    public FindCommand() {
        super("Find files and directories");
        registerArguments(dirArg, mindepthArg, maxdepthArg, inameArg, nameArg, typeArg);
    }

    public static void main(String[] args) throws IOException {

        new FindCommand().execute();
    }

    public void execute() throws IOException {
        out = getOutput().getPrintWriter();
        final Walker walker = new Walker();

        if (maxdepthArg.isSet()) {
            walker.setMaxDepth(maxdepthArg.getValue());
        }

        if (mindepthArg.isSet()) {
            walker.setMinDepth(mindepthArg.getValue());
        }

        if (nameArg.isSet()) {
            final String value = nameArg.getValue();
            walker.addFilter(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    Pattern p = Pattern.compile(value);
                    Matcher m = p.matcher(file.getName());
                    return m.matches();
                }
            });
        }

        if (inameArg.isSet()) {
            final String value = inameArg.getValue();
            walker.addFilter(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    Pattern p = Pattern.compile(value, Pattern.CASE_INSENSITIVE);
                    Matcher m = p.matcher(file.getName());
                    return m.matches();
                }
            });
        }

        if (typeArg.isSet()) {
            final Character value = typeArg.getValue().charAt(0);
            if (value.equals(Character.valueOf('f'))) {
                walker.addFilter(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.isFile();
                    }
                });
            } else if (value.equals(Character.valueOf('d'))) {
                walker.addFilter(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.isDirectory();
                    }
                });
            }
        }
        walker.walk(dirArg.getValues());
    }
}
