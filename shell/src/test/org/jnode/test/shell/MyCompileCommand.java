package org.jnode.test.shell;

import java.io.InputStream;
import java.io.PrintStream;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.ClassNameArgument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.IntegerArgument;

public class MyCompileCommand extends AbstractCommand {
    
    private final int maxLevel = 5;
    
    private final ClassNameArgument ARG_CLASS = 
        new ClassNameArgument("className", Argument.MANDATORY, "the class file to compile");
    private final IntegerArgument ARG_LEVEL = 
        new IntegerArgument("level", Argument.OPTIONAL, 0, maxLevel, "the optimization level");
    private final FlagArgument ARG_TEST = 
        new FlagArgument("test", Argument.OPTIONAL, "when the test versions of the compilers will be used");
    
    public MyCompileCommand() {
        super("compile a Java class (bytecodes) to native code");
        registerArguments(ARG_CLASS, ARG_LEVEL, ARG_TEST);
    }
    
    @Override
    public void execute(CommandLine commandLine, InputStream in,
            PrintStream out, PrintStream err) 
    throws Exception {
        // nothing to see here, move along
    }

}
