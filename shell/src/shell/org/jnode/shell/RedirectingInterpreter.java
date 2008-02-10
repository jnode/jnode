/*
 * $Id: ThreadCommandInvoker.java 3374 2007-08-02 18:15:27Z lsantha $
 *
 * JNode.org
 * Copyright (C) 2003-2007 JNode.org
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
package org.jnode.shell;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.help.Argument;
import org.jnode.shell.help.CompletionException;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.argument.FileArgument;

/**
 * This command interpreter supports simple input and output redirection and
 * pipelines.
 * 
 * @author crawley@jnode.org
 */
public class RedirectingInterpreter extends DefaultInterpreter implements
        ThreadExitListener {

    private static final int COMPLETE_ALIAS = 1;
    private static final int COMPLETE_ARG = 2;
    private static final int COMPLETE_INPUT = 3;
    private static final int COMPLETE_OUTPUT = 4;
    private static final int COMPLETE_PIPE = 5;

    static final Factory FACTORY = new Factory() {
        public CommandInterpreter create() {
            return new RedirectingInterpreter();
        }

        public String getName() {
            return "redirecting";
        }
    };

    private int completionContext;

    public String getName() {
        return "redirecting";
    }

    public int interpret(CommandShell shell, String line) throws ShellException {
        Tokenizer tokenizer = new Tokenizer(line, REDIRECTS_FLAG);
        List<CommandDescriptor> commands = parse(tokenizer, line, false);
        int len = commands.size();
        if (len == 0) {
            return 0; // empty command line.
        }
        shell.addCommandToHistory(line);
        if (len == 1) {
            return runCommand(shell, commands.get(0));
        } else {
            return runPipeline(shell, commands);
        }
    }

    public Completable parsePartial(CommandShell shell, String line)
            throws ShellSyntaxException {
        Tokenizer tokenizer = new Tokenizer(line, REDIRECTS_FLAG);
        List<CommandDescriptor> commands = parse(tokenizer, line, true);
        int nosCommands = commands.size();
        if (nosCommands == 0) {
            return new CommandLine("", null);
        } else {
            CommandDescriptor lastDesc = commands.get(nosCommands - 1);
            CommandLine lastCommand = lastDesc.commandLine;
            CommandLine.Token lastToken = tokenizer.last();
            boolean whitespaceAfter = tokenizer.whitespaceAfter(lastToken);
            lastCommand.setArgumentAnticipated(whitespaceAfter);
            switch (completionContext) {
            case COMPLETE_ALIAS:
            case COMPLETE_ARG:
            case COMPLETE_PIPE:
                break;
            case COMPLETE_INPUT:
                if (lastDesc.fromFileName == null || !whitespaceAfter) {
                    return new RedirectionCompleter(line, lastToken.start);
                }
                break;
            case COMPLETE_OUTPUT:
                if (lastDesc.toFileName == null || !whitespaceAfter) {
                    return new RedirectionCompleter(line, lastToken.start);
                }
                break;
            default:
                throw new ShellFailureException("bad completion context ("
                        + completionContext + ")");
            }
            return new SubcommandCompleter(lastCommand, line,
                    whitespaceAfter ? lastToken.end : lastToken.start);
        }
    }

    private List<CommandDescriptor> parse(Tokenizer tokenizer, String line,
            boolean allowPartial) throws ShellSyntaxException {
        LinkedList<CommandDescriptor> commands = new LinkedList<CommandDescriptor>();
        boolean pipeTo = false;
        while (tokenizer.hasNext()) {
            completionContext = COMPLETE_ALIAS;
            CommandLine.Token commandToken = tokenizer.next();
            if (commandToken.tokenType == SPECIAL) {
                throw new ShellSyntaxException("Misplaced '"
                        + commandToken.token + "': expected a command name");
            }
            CommandLine.Token fromFileName = null;
            CommandLine.Token toFileName = null;
            LinkedList<CommandLine.Token> args = new LinkedList<CommandLine.Token>();
            pipeTo = false;
            while (tokenizer.hasNext()) {
                completionContext = COMPLETE_ARG;
                CommandLine.Token token = tokenizer.next();
                if (token.tokenType == SPECIAL) {
                    if (token.token.equals("<")) {
                        fromFileName = parseFileName(tokenizer, "<");
                        if (fromFileName == null && !allowPartial) {
                            throw new ShellSyntaxException(
                                    "no filename after '<'");
                        }
                        completionContext = COMPLETE_INPUT;
                        continue;
                    } else if (token.token.equals(">")) {
                        toFileName = parseFileName(tokenizer, ">");
                        if (toFileName == null && !allowPartial) {
                            throw new ShellSyntaxException(
                                    "no filename after '>'");
                        }
                        completionContext = COMPLETE_OUTPUT;
                        continue;
                    } else if (token.token.equals("|")) {
                        pipeTo = true;
                        completionContext = COMPLETE_PIPE;
                        break;
                    } else {
                        throw new ShellSyntaxException("unrecognized symbol: '"
                                + token + "'");
                    }
                } else {
                    args.add(token);
                }
            }
            CommandLine.Token[] argVec = args
                    .toArray(new CommandLine.Token[args.size()]);

            CommandLine commandLine = new CommandLine(commandToken, argVec,
                    null);
            commands.add(new CommandDescriptor(commandLine, fromFileName,
                    toFileName, pipeTo));
        }
        if (pipeTo && !allowPartial) {
            throw new ShellSyntaxException("no command after '<'");
        }
        return commands;
    }

    private CommandLine.Token parseFileName(Tokenizer tokenizer, String special)
            throws ShellSyntaxException {
        if (!tokenizer.hasNext()) {
            return null;
        }
        CommandLine.Token token = tokenizer.next();
        if (token.tokenType == SPECIAL) {
            throw new ShellSyntaxException("misplaced '" + token + "'");
        }
        if (token.token.isEmpty()) {
            throw new ShellSyntaxException("empty '" + special + "' file name");
        }
        return token;
    }

    private int runCommand(CommandShell shell, CommandDescriptor desc)
            throws ShellException {
        Closeable in = CommandLine.DEFAULT_STDIN;
        Closeable out = CommandLine.DEFAULT_STDOUT;
        Closeable err = CommandLine.DEFAULT_STDERR;
        try {
            try {
                if (desc.fromFileName != null) {
                    in = new FileInputStream(desc.fromFileName.token);
                }
            } catch (IOException ex) {
                throw new ShellInvocationException("cannot open '"
                        + desc.fromFileName.token + "': " + ex.getMessage());
            }
            try {
                if (desc.toFileName != null) {
                    out = new FileOutputStream(desc.toFileName.token);
                }
            } catch (IOException ex) {
                throw new ShellInvocationException("cannot open '"
                        + desc.toFileName.token + "': " + ex.getMessage());
            }
            desc.commandLine.setStreams(new Closeable[] { in, out, err });
            return shell.invoke(desc.commandLine);
        } finally {
            try {
                if (desc.fromFileName != null) {
                    in.close();
                }
            } catch (IOException ex) {
                // squash
            }
            try {
                if (desc.toFileName != null && out != null) {
                    out.close();
                }
            } catch (IOException ex) {
                // squash
            }
        }
    }

    private synchronized int runPipeline(CommandShell shell,
            List<CommandDescriptor> descs) throws ShellException {
        int nosStages = descs.size();
        try {
            // Create all the threads for the pipeline, wiring up their input
            // and output streams.
            int stageNo = 0;
            PipedOutputStream pipeOut = null;
            for (CommandDescriptor desc : descs) {
                Closeable in = CommandLine.DEFAULT_STDIN;
                Closeable out = CommandLine.DEFAULT_STDOUT;
                Closeable err = CommandLine.DEFAULT_STDERR;
                desc.openedStreams = new ArrayList<Closeable>(2);
                try {
                    // redirect from
                    if (desc.fromFileName != null) {
                        in = new FileInputStream(desc.fromFileName.token);
                        desc.openedStreams.add(in);
                    }
                } catch (IOException ex) {
                    throw new ShellInvocationException("cannot open '"
                            + desc.fromFileName.token + "': " + ex.getMessage());
                }
                try {
                    // redirect to
                    if (desc.toFileName != null) {
                        out = new FileOutputStream(desc.toFileName.token);
                        desc.openedStreams.add(out);
                    }
                } catch (IOException ex) {
                    throw new ShellInvocationException("cannot open '"
                            + desc.toFileName + "': " + ex.getMessage());
                }
                if (stageNo > 0) {
                    // pipe from
                    if (pipeOut != null) {
                        // the previous stage is sending stdout to the pipe
                        if (in == CommandLine.DEFAULT_STDIN) {
                            // this stage is going to read from the pipe
                            PipedInputStream pipeIn = new PipedInputStream();
                            try {
                                pipeIn.connect(pipeOut);
                            } catch (IOException ex) {
                                throw new ShellInvocationException(
                                        "Problem connecting pipe", ex);
                            }
                            in = pipeIn;
                            desc.openedStreams.add(pipeIn);
                        } else {
                            // this stage has redirected stdin from a file ...
                            // so go back and
                            // replace the previous stage's pipeOut with devnull
                            CommandDescriptor prev = descs.get(stageNo - 1);
                            Closeable[] ps = prev.commandLine.getStreams();
                            try {
                                pipeOut.close();
                            } catch (IOException ex) {
                                // squash
                            }
                            prev.commandLine.setStreams(new Closeable[] {
                                    ps[0], CommandLine.DEVNULL, ps[2] });
                        }
                    } else {
                        // the previous stage has explicitly redirected stdout
                        if (in == CommandLine.DEFAULT_STDIN) {
                            // this stage hasn't redirected stdin, so we need to
                            // give
                            // it a NullInputStream to suck on.
                            in = CommandLine.DEVNULL;
                        }
                    }
                }
                if (stageNo < nosStages - 1) {
                    // this stage is not the last one, and it hasn't redirected
                    // its stdout, so it will write to a pipe
                    if (out == CommandLine.DEFAULT_STDOUT) {
                        pipeOut = new PipedOutputStream();
                        out = new PrintStream(pipeOut);
                        desc.openedStreams.add(out);
                    }
                }
                desc.commandLine.setStreams(new Closeable[] { in, out, err });
                try {
                    desc.thread = shell.invokeAsynchronous(desc.commandLine);
                } catch (UnsupportedOperationException ex) {
                    throw new ShellInvocationException(
                            "The current invoker does not support pipelines",
                            ex);
                }
                stageNo++;
            }

            currentDescriptors = descs;
            threadsLeft = descs.size();

            // Start all threads.
            for (CommandDescriptor desc : descs) {
                desc.thread.start(this);
            }

            // Wait until they have finished
            while (threadsLeft > 0) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    return -1;
                }
            }
            Runnable runner = descs.get(nosStages - 1).thread.getRunner();
            return (runner instanceof CommandRunner) ? ((CommandRunner) runner)
                    .getRC() : 0;
        } finally {
            // Close any remaining streams.
            for (CommandDescriptor desc : descs) {
                if (desc.openedStreams != null) {
                    for (Closeable stream : desc.openedStreams) {
                        try {
                            stream.close();
                        } catch (IOException ex) {
                            // squash
                        }
                    }
                }
            }
            // TODO deal with any left over threads
        }
    }

    private List<CommandDescriptor> currentDescriptors;
    private int threadsLeft;

    /**
     * Callback to deal with a thread that has exited.
     * 
     * @param thread
     */
    public synchronized void notifyThreadExitted(CommandThread thread) {
        // If the thread owned any input or output streams, they need to
        // be closed. In particular, this will cause the next downstream
        // command in a pipeline to see an "end of file".
        for (CommandDescriptor desc : currentDescriptors) {
            if (thread == desc.thread) {
                if (desc.openedStreams == null) {
                    throw new ShellFailureException("bad thread exit callback");
                }
                for (Closeable stream : desc.openedStreams) {
                    try {
                        stream.close();
                    } catch (IOException ex) {
                        // squash
                    }
                }
                desc.openedStreams = null;
                threadsLeft--;
                notify();
                break;
            }
        }
    }

    private static class CommandDescriptor {
        public final CommandLine commandLine;
        public final CommandLine.Token fromFileName;
        public final CommandLine.Token toFileName;
        public final boolean pipeTo;
        public List<Closeable> openedStreams;
        public CommandThread thread;

        public CommandDescriptor(CommandLine commandLine,
                CommandLine.Token fromFileName, CommandLine.Token toFileName,
                boolean pipeTo) {
            super();
            this.commandLine = commandLine;
            this.fromFileName = fromFileName;
            this.toFileName = toFileName;
            this.pipeTo = pipeTo;
        }
    }

    private abstract class EmbeddedCompleter implements Completable {
        private final String partial;
        private final int startPos;

        public EmbeddedCompleter(String partial, int startPos) {
            this.partial = partial;
            this.startPos = startPos;
        }

        public String getCompletableString() {
            return partial.substring(startPos);
        }

        public void setFullCompleted(CompletionInfo completion, String completed) {
            completion.setCompleted(partial.substring(0, startPos) + completed);
        }
    }

    private class SubcommandCompleter extends EmbeddedCompleter {
        private final CommandLine subcommand;

        public SubcommandCompleter(CommandLine subcommand, String partial,
                int startPos) {
            super(partial, startPos);
            this.subcommand = subcommand;
        }

        public void complete(CompletionInfo completion, CommandShell shell)
                throws CompletionException {
            subcommand.complete(completion, shell);
            setFullCompleted(completion, completion.getCompleted());
        }
    }

    private class RedirectionCompleter extends EmbeddedCompleter {

        private final Help.Info fileParameter = new Help.Info("file",
                "default parameter for file redirection completion",
                new Parameter(new FileArgument("file", "a file",
                        Argument.SINGLE), Parameter.MANDATORY));

        public RedirectionCompleter(String partial, int startPos) {
            super(partial, startPos);
        }

        public void complete(CompletionInfo completion, CommandShell shell)
                throws CompletionException {
            CommandLine command = new CommandLine("?",
                    new String[] { getCompletableString() });
            String result = fileParameter.complete(command);
            setFullCompleted(completion, result);
        }
    }
}
