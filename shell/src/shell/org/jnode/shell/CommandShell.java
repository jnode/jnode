/*
 * $Id$
 *
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
 
package org.jnode.shell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.naming.NameNotFoundException;

import org.apache.log4j.Logger;
import org.jnode.driver.console.CompletionInfo;
import org.jnode.driver.console.ConsoleEvent;
import org.jnode.driver.console.ConsoleListener;
import org.jnode.driver.console.ConsoleManager;
import org.jnode.driver.console.InputHistory;
import org.jnode.driver.console.TextConsole;
import org.jnode.driver.console.textscreen.KeyboardReader;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.alias.AliasManager;
import org.jnode.shell.alias.NoSuchAliasException;
import org.jnode.shell.io.CommandIO;
import org.jnode.shell.io.CommandInput;
import org.jnode.shell.io.CommandInputOutput;
import org.jnode.shell.io.CommandOutput;
import org.jnode.shell.io.FanoutWriter;
import org.jnode.shell.io.NullInputStream;
import org.jnode.shell.io.NullOutputStream;
import org.jnode.shell.isolate.IsolateCommandInvoker;
import org.jnode.shell.proclet.ProcletCommandInvoker;
import org.jnode.shell.syntax.ArgumentBundle;
import org.jnode.shell.syntax.CommandSyntaxException;
import org.jnode.shell.syntax.SyntaxManager;
import org.jnode.shell.syntax.CommandSyntaxException.Context;
import org.jnode.util.ReaderInputStream;
import org.jnode.util.SystemInputStream;
import org.jnode.vm.VmSystem;

/**
 * This is the primary implementation of the {@link Shell} interface.  In 
 * addition to core Shell functionality, this implementation supports 
 * command-line completion, command and application input history,
 * switch-able interpreters and invokers, and initialization scripting.
 * 
 * @author epr
 * @author Fabien DUMINY
 * @author crawley@jnode.org
 */
public class CommandShell implements Runnable, Shell, ConsoleListener {

    public static final String PROMPT_PROPERTY_NAME = "jnode.prompt";
    public static final String INTERPRETER_PROPERTY_NAME = "jnode.interpreter";
    public static final String INVOKER_PROPERTY_NAME = "jnode.invoker";
    public static final String CMDLINE_PROPERTY_NAME = "jnode.cmdline";

    public static final String DEBUG_PROPERTY_NAME = "jnode.debug";
    public static final String DEBUG_DEFAULT = "false";
    public static final String HISTORY_PROPERTY_NAME = "jnode.history";
    public static final String HISTORY_DEFAULT = "true";

    public static final String USER_HOME_PROPERTY_NAME = "user.home";
    public static final String JAVA_HOME_PROPERTY_NAME = "java.home";
    public static final String DIRECTORY_PROPERTY_NAME = "user.dir";

    public static final String INITIAL_INVOKER = "proclet";
    public static final String INITIAL_INTERPRETER = "redirecting";
    public static final String FALLBACK_INVOKER = "default";
    public static final String FALLBACK_INTERPRETER = "default";

    private static String DEFAULT_PROMPT = "JNode $P$G";
    private static final String COMMAND_KEY = "cmd=";

    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(CommandShell.class);

    private CommandInput cin;
    
    private CommandOutput cout;
    
    private CommandOutput cerr;
    
    private Reader in;

    private PrintWriter outPW;
    
    private PrintWriter errPW;
    
    private AliasManager aliasMgr;

    private SyntaxManager syntaxMgr;

    /**
     * Keeps a reference to the console this CommandShell is using *
     */
    private TextConsole console;

    /**
     * Contains the archive of commands. *
     */
    private InputHistory commandHistory = new InputHistory();

    /**
     * Contains the application input history for the current thread.
     */
    private static InheritableThreadLocal<InputHistory> applicationHistory = 
        new InheritableThreadLocal<InputHistory>();

    /**
     * When true, {@link complete(String)} performs command completion.
     */
    private boolean readingCommand;

    /**
     * Contains the last command entered
     */
    private String lastCommandLine = "";

    /**
     * Contains the last application input line entered
     */
    private String lastInputLine = "";

    private SimpleCommandInvoker invoker;
    private String invokerName;

    private CommandInterpreter interpreter;
    private String interpreterName;

    private CompletionInfo completion;

    private boolean historyEnabled;
    
    private boolean debugEnabled;

    private boolean exited = false;

    private Thread ownThread;

    private boolean bootShell;

    public TextConsole getConsole() {
        return console;
    }

    public static void main(String[] args) 
        throws NameNotFoundException, ShellException {
        CommandShell shell = new CommandShell(
                (TextConsole) (InitialNaming.lookup(ConsoleManager.NAME)).getFocus());
        for (String arg : args) {
            if ("boot".equals(arg)) {
                shell.bootShell = true;
                break;
            }
        }
        shell.run();
    }

    public CommandShell(TextConsole cons) throws ShellException {
        debugEnabled = true;
        try {
            console = cons;
            Reader in = console.getIn();
            if (in == null) {
                throw new ShellException("console input stream is null");
            }
            setupStreams(in, console.getOut(), console.getErr());
            SystemInputStream.getInstance().initialize(new ReaderInputStream(in));
            cons.setCompleter(this);

            console.addConsoleListener(this);
            aliasMgr = ShellUtils.getAliasManager().createAliasManager();
            syntaxMgr = ShellUtils.getSyntaxManager().createSyntaxManager();
            System.setProperty(PROMPT_PROPERTY_NAME, DEFAULT_PROMPT);
        } catch (NameNotFoundException ex) {
            throw new ShellException("Cannot find required resource", ex);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Create a CommandShell that doesn't use a TextConsole or the ConsoleManager.
     * 
     * @throws ShellException
     */
    public CommandShell() throws ShellException {
        debugEnabled = true;
        try {
            setupStreams(new InputStreamReader(System.in), 
                    new OutputStreamWriter(System.out), 
                    new OutputStreamWriter(System.err));
            aliasMgr = ShellUtils.getAliasManager().createAliasManager();
            syntaxMgr = ShellUtils.getSyntaxManager().createSyntaxManager();
            System.setProperty(PROMPT_PROPERTY_NAME, DEFAULT_PROMPT);
        } catch (NameNotFoundException ex) {
            throw new ShellException("Cannot find required resource", ex);
        } catch (Exception ex) {
            throw new ShellFailureException("CommandShell initialization failed", ex);
        }
    }
    
    private void setupStreams(Reader in, Writer out, Writer err) {
        this.cout = new CommandOutput(out);
        this.cerr = new CommandOutput(err);
        this.cin = new CommandInput(in);
        this.in = cin.getReader();
        this.outPW = cout.getPrintWriter();
        this.errPW = cerr.getPrintWriter();
    }

    
    /**
     * This constructor builds a partial command shell for test purposes only.
     * 
     * @param aliasMgr test framework supplies an alias manager
     * @param syntaxMgr test framework supplies a syntax manager
     */
    protected CommandShell(AliasManager aliasMgr, SyntaxManager syntaxMgr) {
        this.aliasMgr = aliasMgr;
        this.syntaxMgr = syntaxMgr;
        this.debugEnabled = true;
        setupStreams(
                new InputStreamReader(System.in), 
                new OutputStreamWriter(System.out), 
                new OutputStreamWriter(System.err));
        this.readingCommand = true;
    }

    /**
     * Run this shell until exit.
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {
        // Here, we are running in the CommandShell (main) Thread
        // so, we can register ourself as the current shell
        // (it will also be the current shell for all children Thread)
        
        configureShell();

        // Run commands from the JNode command line first
        final String cmdLine = System.getProperty(CMDLINE_PROPERTY_NAME, "");
        final StringTokenizer tok = new StringTokenizer(cmdLine);

        while (tok.hasMoreTokens()) {
            final String e = tok.nextToken();
            try {
                if (e.startsWith(COMMAND_KEY)) {
                    final String cmd = e.substring(COMMAND_KEY.length());
                    outPW.println(prompt() + cmd);
                    runCommand(cmd, false, this.interpreter);
                }
            } catch (Throwable ex) {
                errPW.println("Error while processing bootarg commands: "
                        + ex.getMessage());
                stackTrace(ex);
            }
        }

        if (bootShell) {
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                public Void run() {
                    final String java_home = System.getProperty(JAVA_HOME_PROPERTY_NAME, "");
                    final String name = "jnode.ini";
                    final File jnode_ini = new File(java_home + '/' + name);
                    try {
                        if (jnode_ini.exists()) {
                            runCommandFile(jnode_ini, null, null);
                        } else if (getClass().getResource(name) != null) {
                            runCommandResource(name, null);
                        }
                    } catch (ShellException ex) {
                        errPW.println("Error while processing " + jnode_ini + ": " + ex.getMessage());
                        stackTrace(ex);
                    }
                    return null;
                }
            });
        }

        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                final String user_home = System.getProperty(USER_HOME_PROPERTY_NAME, "");
                final String name = "shell.ini";
                final File shell_ini = new File(user_home + '/' + name);
                try {
                    if (shell_ini.exists()) {
                        runCommandFile(shell_ini, null, null);
                    } else if (getClass().getResource(name) != null) {
                        runCommandResource(name, null);
                    }
                } catch (ShellException ex) {
                    errPW.println("Error while processing " + shell_ini + ": " + ex.getMessage());
                    stackTrace(ex);
                }
                return null;
            }
        });

        while (!isExited()) {
            String input = null;
            try {
                refreshFromProperties();
                clearEof();
                outPW.print(prompt());
                readingCommand = true;
                input = readInputLine();
                if (input.length() > 0) {
                    // This hairy bit of code deals with shell commands that span multiple
                    // input lines.  If an interpreter encounters the end of the line that
                    // we have given it and it requires more input to get a complete command,
                    // it may throw IncompleteCommandException.  The shell responds by 
                    // outputting a different prompt (supplied in the exception), and then
                    // attempting to get the next line.  If that succeeds, the line is 
                    // appended to the input we gave the interpreter last time, and the
                    // interpreter is called again.  This continues until either the
                    // interpreter manages to run the command, or we get some other
                    // shell syntax exception.
                    boolean done = false;
                    do {
                        try {
                            runCommand(input, true, this.interpreter);
                            done = true;
                        } catch (IncompleteCommandException ex) {
                            String continuation = null;
                            // (Tell completer to use command history not app. history)
                            readingCommand = true;
                            if (this.interpreter.supportsMultilineCommands()) {
                                String prompt = ex.getPrompt();
                                if (prompt != null) {
                                    outPW.print(prompt);
                                }
                                continuation = readInputLine();
                            }
                            if (continuation == null) {
                                diagnose(ex);
                                break;
                            } else {
                                input = input + "\n" + continuation;
                            }
                        } catch (ShellException ex) {
                            diagnose(ex);
                            done = true;
                        }
                    } while (!done);
                }

                if (VmSystem.isShuttingDown()) {
                    exited = true;
                }
            } catch (Throwable ex) {
                errPW.println("Uncaught exception while processing command(s): "
                        + ex.getMessage());
                stackTrace(ex);
            } finally {
                if (input != null && input.trim().length() > 0) {
                    String lines[] = input.split("\\n");
                    for (String line : lines) {
                        addToCommandHistory(line);
                    }
                }
            }
        }
    }

    private void diagnose(ShellException ex) {
        Throwable cause = ex.getCause();
        // Try to turn this into something that is moderately intelligible
        // for the common cases ...
        if (cause != null) {
            errPW.println(ex.getMessage());
            if (cause instanceof CommandSyntaxException) {
                List<Context> argErrors = ((CommandSyntaxException) cause).getArgErrors();
                if (argErrors != null) {
                    // The parser can produce many errors as each of the alternatives
                    // in the tree are explored.  The following assumes that errors
                    // produced when we get farthest along in the token stream are most
                    // likely to be the "real" errors.
                    int rightmostPos = 0;
                    for (Context context : argErrors) {
                        if (context.sourcePos > rightmostPos) {
                            rightmostPos = context.sourcePos;
                        }
                    }
                    for (Context context : argErrors) {
                        if (context.sourcePos < rightmostPos) {
                            continue;
                        }
                        if (context.token != null) {
                            errPW.println("   " + context.exception.getMessage() + ": " +
                                    context.token.text);
                        } else {
                            errPW.println("   " + context.exception.getMessage() + ": " +
                                    context.syntax.format());
                        }
                    }
                }
            } else {
                errPW.println(cause.getMessage());
            }
        } else {
            errPW.println("Shell exception: " + ex.getMessage());
        }
        stackTrace(ex);
    }

    public void configureShell() {
        try {
            ShellUtils.getShellManager().registerShell(this);

            ShellUtils.registerCommandInvoker(DefaultCommandInvoker.FACTORY);
            ShellUtils.registerCommandInvoker(ThreadCommandInvoker.FACTORY);
            ShellUtils.registerCommandInvoker(ProcletCommandInvoker.FACTORY);
            ShellUtils.registerCommandInvoker(IsolateCommandInvoker.FACTORY);
            ShellUtils.registerCommandInterpreter(DefaultInterpreter.FACTORY);
            ShellUtils
                    .registerCommandInterpreter(RedirectingInterpreter.FACTORY);
        } catch (NameNotFoundException e1) {
            e1.printStackTrace();
        }

        // Configure the shell based on System properties.
        setupFromProperties();

        // Now become interactive
        ownThread = Thread.currentThread();
    }

    private void setupFromProperties() {
        debugEnabled = Boolean.parseBoolean(System.getProperty(
                DEBUG_PROPERTY_NAME, DEBUG_DEFAULT));
        historyEnabled = Boolean.parseBoolean(System.getProperty(
                HISTORY_PROPERTY_NAME, HISTORY_DEFAULT));
        try {
            setCommandInvoker(System.getProperty(INVOKER_PROPERTY_NAME,
                    INITIAL_INVOKER));
        } catch (Exception ex) {
            errPW.println(ex.getMessage());
            stackTrace(ex);
            // Use the fallback invoker
            setCommandInvoker(FALLBACK_INVOKER);
        }
        try {
            setCommandInterpreter(System.getProperty(INTERPRETER_PROPERTY_NAME,
                    INITIAL_INTERPRETER));
        } catch (Exception ex) {
            errPW.println(ex.getMessage());
            stackTrace(ex);
            // Use the fallback interpreter
            setCommandInterpreter(FALLBACK_INTERPRETER);
        }
        invoker.setDebugEnabled(debugEnabled);
    }

    private void refreshFromProperties() {
        debugEnabled = Boolean.parseBoolean(System.getProperty(
                DEBUG_PROPERTY_NAME, DEBUG_DEFAULT));
        historyEnabled = Boolean.parseBoolean(System.getProperty(
                HISTORY_PROPERTY_NAME, HISTORY_DEFAULT));
        try {
            setCommandInterpreter(System.getProperty(INTERPRETER_PROPERTY_NAME, ""));
        } catch (Exception ex) {
            errPW.println(ex.getMessage());
            stackTrace(ex);
        }
        try {
            setCommandInvoker(System.getProperty(INVOKER_PROPERTY_NAME, ""));
        } catch (Exception ex) {
            errPW.println(ex.getMessage());
            stackTrace(ex);
        }
        invoker.setDebugEnabled(debugEnabled);
    }

    public synchronized void setCommandInvoker(String name) throws IllegalArgumentException {
        if (!name.equals(this.invokerName)) {
            this.invoker = ShellUtils.createInvoker(name, this);
            if (this.invokerName != null) {
                outPW.println("Switched to " + name + " invoker");
            }
            this.invokerName = name;
            System.setProperty(INVOKER_PROPERTY_NAME, name);
        }
    }

    public synchronized void setCommandInterpreter(String name) throws IllegalArgumentException {
        if (!name.equals(this.interpreterName)) {
            this.interpreter = ShellUtils.createInterpreter(name);
            if (this.interpreterName != null) {
                outPW.println("Switched to " + name + " interpreter");
            }
            this.interpreterName = name;
            System.setProperty(INTERPRETER_PROPERTY_NAME, name);
        }
    }

    private void stackTrace(Throwable ex) {
        if (this.debugEnabled) {
            ex.printStackTrace(errPW);
        }
    }

    private String readInputLine() throws IOException {
        StringBuffer sb = new StringBuffer(40);
        while (true) {
            int ch = in.read();
            if (ch == -1 || ch == '\n') {
                return sb.toString();
            }
            sb.append((char) ch);
        }
    }

    private void clearEof() {
        if (in instanceof KeyboardReader) {
            ((KeyboardReader) in).clearSoftEOF();
        }
    }
        
    private int runCommand(String cmdLineStr, boolean interactive,
            CommandInterpreter interpreter) throws ShellException {
        try {
            if (interactive) {
                clearEof();
                readingCommand = false;
                // Each interactive command is launched with a fresh history
                // for input completion
                applicationHistory.set(new InputHistory());
            }
            return interpreter.interpret(this, cmdLineStr);
        } finally {
            if (interactive) {
                applicationHistory.set(null);
            }
        }
    }

    /**
     * Parse and run a command line using the CommandShell's current
     * interpreter.
     * 
     * @param command the command line.
     * @throws ShellException
     */
    public int runCommand(String command) throws ShellException {
        return runCommand(command, false, this.interpreter);
    }

    /**
     * Run a command encoded as a CommandLine object. The command line will give
     * the command name (alias), the argument list and the IO stream. The
     * command is run using the CommandShell's current invoker.
     * 
     * @param cmdLine the CommandLine object.
     * @param env 
     * @param sysProps 
     * @return the command's return code
     * @throws ShellException
     */
    public int invoke(CommandLine cmdLine, CommandInfo cmdInfo, 
            Properties sysProps, Map<String, String> env) 
        throws ShellException {
        if (this.invoker instanceof CommandInvoker) {
            return ((CommandInvoker) this.invoker).invoke(cmdLine, cmdInfo, sysProps, env);
        } else {
            return this.invoker.invoke(cmdLine, cmdInfo);
        }
    }

    /**
     * Prepare a CommandThread to run a command encoded as a CommandLine object.
     * When the thread's "start" method is called, the command will be executed
     * using the CommandShell's current (now) invoker.
     * 
     * @param cmdLine the CommandLine object.
     * @return the command's return code
     * @throws ShellException
     */
    public CommandThread invokeAsynchronous(CommandLine cmdLine, CommandInfo cmdInfo)
        throws ShellException {
        return this.invoker.invokeAsynchronous(cmdLine, cmdInfo);
    }

    public CommandInfo getCommandInfo(String cmd) throws ShellException {
        try {
            Class<?> cls = aliasMgr.getAliasClass(cmd);
            return new CommandInfo(cls, aliasMgr.isInternal(cmd));
        } catch (ClassNotFoundException ex) {
            throw new ShellException("Cannot the load command class for alias '" + cmd + "'", ex);
        } catch (NoSuchAliasException ex) {
            try {
                final ClassLoader cl = 
                    Thread.currentThread().getContextClassLoader();
                return new CommandInfo(cl.loadClass(cmd), false);
            } catch (ClassNotFoundException ex2) {
                throw new ShellException("Cannot find an alias or load a command class for '" + cmd + "'", ex);
            }
        }
    }
    
    protected ArgumentBundle getCommandArgumentBundle(CommandInfo commandInfo) {
        if (Command.class.isAssignableFrom(commandInfo.getCommandClass())) {
            try {
                Command cmd = (Command) (commandInfo.getCommandClass().newInstance());
                return cmd.getArgumentBundle();
            } catch (Exception ex) {
                // drop through
            }
        }
        return null;
    }

    boolean isDebugEnabled() {
        return debugEnabled;
    }

    /**
     * Gets the alias manager of this shell
     */
    public AliasManager getAliasManager() {
        return aliasMgr;
    }

    /**
     * Gets the shell's command InputHistory object.
     */
    public InputHistory getCommandHistory() {
        return commandHistory;
    }

    /**
     * Gets the shell's currently active InputHistory object.
     */
    public InputHistory getInputHistory() {
        if (readingCommand) {
            return commandHistory;
        } else {
            return CommandShell.applicationHistory.get();
        }
    }

    /**
     * Gets the expanded prompt
     */
    protected String prompt() {
        String prompt = System
                .getProperty(PROMPT_PROPERTY_NAME, DEFAULT_PROMPT);
        final StringBuffer result = new StringBuffer();
        boolean commandMode = false;
        try {
            StringReader reader = new StringReader(prompt);
            int i;
            while ((i = reader.read()) != -1) {
                char c = (char) i;
                if (commandMode) {
                    switch (c) {
                        case 'P':
                            result.append(new File(System.getProperty(DIRECTORY_PROPERTY_NAME, "")));
                            break;
                        case 'G':
                            result.append("> ");
                            break;
                        case 'D':
                            final Date now = new Date();
                            DateFormat.getDateTimeInstance().format(now, result, null);
                            break;
                        default:
                            result.append(c);
                    }
                    commandMode = false;
                } else {
                    switch (c) {
                        case '$':
                            commandMode = true;
                            break;
                        default:
                            result.append(c);
                    }
                }
            }
        } catch (Exception ioex) {
            // This should never occur
            log.error("Error in prompt()", ioex);
        }
        return result.toString();
    }
    
    /**
     * This method is called by the console input driver to perform command line
     * completion in response to a 
     * {@link org.jnode.driver.console.textscreen.KeyboardReaderAction#KR_COMPLETE} 
     * action; typically a TAB character.
     */
    public CompletionInfo complete(String partial) {
        if (!readingCommand) {
            // dummy completion behavior for application input.
            return new CommandCompletions();
        }

        // workaround to set the currentShell to this shell
        // FIXME is this needed?
        try {
            ShellUtils.getShellManager().registerShell(this);
        } catch (NameNotFoundException ex) {
            log.error("Cannot find shell manager", ex);
        }

        // do command completion
        completion = new CommandCompletions(interpreter);
        try {
            Completable cl = interpreter.parsePartial(this, partial);
            if (cl != null) {
                cl.complete(completion, this);
            }
        } catch (ShellException ex) {
            outPW.println(); // next line
            errPW.println("Cannot parse: " + ex.getMessage());
            stackTrace(ex);
        } catch (Throwable ex) {
            outPW.println(); // next line
            errPW.println("Problem in completer: " + ex.getMessage());
            stackTrace(ex);
        }   

        // Make sure that the shell's completion context gets nulled.
        CompletionInfo myCompletion = completion;
        completion = null;
        return myCompletion;
    }
    
    /**
     * This method is responsible for generating incremental help in response
     * to a @link org.jnode.driver.console.textscreen.KeyboardReaderAction#KR_HELP}
     * action.
     */
    public boolean help(String partial, PrintWriter pw) {
        if (!readingCommand) {
            return false;
        }
        try {
            return interpreter.help(this, partial, pw);
        } catch (ShellException ex) {
            outPW.println(); // next line
            errPW.println("Cannot parse: " + ex.getMessage());
            stackTrace(ex);
            return false;
        } catch (Throwable ex) {
            outPW.println(); // next line
            errPW.println("Problem in incremental help: " + ex.getMessage());
            stackTrace(ex);
            return false;
        } 
    }

    private void addToCommandHistory(String line) {
        // Add this line to the command history.
        if (isHistoryEnabled() && !line.equals(lastCommandLine)) {
            commandHistory.addLine(line);
            lastCommandLine = line;
        }
    }

    private void addToInputHistory(String line) {
        // Add this line to the application input history.
        if (isHistoryEnabled() && !line.equals(lastInputLine)) {
            InputHistory history = applicationHistory.get();
            if (history != null) {
                history.addLine(line);
                lastInputLine = line;
            }
        }
    }

    private CommandInput getInputStream() {
        if (isHistoryEnabled()) {
            // Insert a filter on the input stream that adds completed input
            // lines
            // to the application input history. (Since the filter is stateless,
            // it doesn't really matter if we do this multiple times.)
            // FIXME if we partition the app history by application, we will
            // need
            // to bind the history object in the history input stream
            // constructor.
            return new CommandInput(new HistoryInputStream(cin.getInputStream()));
        } else {
            return cin;
        }
    }

    /**
     * This subtype of FilterInputStream captures the console input for an
     * application in the application input history.
     */
    private class HistoryInputStream extends FilterInputStream {
        // TODO - replace with a Reader
        private StringBuilder line = new StringBuilder();

        public HistoryInputStream(InputStream in) {
            super(in);
        }

        @Override
        public int read() throws IOException {
            int res = super.read();
            if (res != -1) {
                filter((byte) res);
            }
            return res;
        }

        @Override
        public int read(byte[] buf, int offset, int len) throws IOException {
            int res = super.read(buf, offset, len);
            for (int i = 0; i < res; i++) {
                filter(buf[offset + i]);
            }
            return res;
        }

        @Override
        public int read(byte[] buf) throws IOException {
            return read(buf, 0, buf.length);
        }

        private void filter(byte b) {
            if (b == '\n') {
                addToInputHistory(line.toString());
                line.setLength(0);
            } else {
                line.append((char) b);
            }
        }
    }

    public SimpleCommandInvoker getDefaultCommandInvoker() {
        return ShellUtils.createInvoker("default", this);
    }

    public int runCommandFile(File file, String alias, String[] args) throws ShellException {
        // FIXME extend to allow arguments to be passed to the script.
        boolean enabled = setHistoryEnabled(false);
        try {
            CommandInterpreter interpreter = createInterpreter(new FileReader(file));
            if (alias == null) {
                alias = file.getAbsolutePath();
            }
            return interpreter.interpret(this, file, alias, args);
        } catch (IOException ex) {
            throw new ShellException("Cannot open command file: " + ex.getMessage(), ex);
        } finally {
            setHistoryEnabled(enabled);
        }
    }

    public int runCommandResource(String resource, String[] args) throws ShellException {
        boolean enabled = setHistoryEnabled(false);
        try {
            int result;
            // FIXME throw ShellException if resource or interpreter not found
            InputStream input = getClass().getResourceAsStream(resource);
            if (input == null) {
                throw new ShellException("Cannot find resource '" + resource + "'");
            } else {
                CommandInterpreter interpreter = createInterpreter(new InputStreamReader(input));
                Reader reader = new InputStreamReader(getClass().getResourceAsStream(resource));
                result = interpreter.interpret(this, reader, resource, args);
            }
            return result;
        } finally {
            setHistoryEnabled(enabled);
        }
    }
    
    public CommandInterpreter createInterpreter(Reader reader) throws ShellException {
        try {
            final BufferedReader br = new BufferedReader(reader);
            CommandInterpreter interpreter;
            String line = br.readLine();
            if (line != null && line.startsWith("#!")) {
                String name = line.substring(2);
                interpreter = ShellUtils.createInterpreter(name);
                if (interpreter == null) {
                    throw new ShellException("Cannot execute script: no '" + 
                            name + "' interpreter is registered");
                }
            } else {
                interpreter = this.interpreter;
            }
            return interpreter;
        } catch (IOException ex) {
            throw new ShellException("Cannot open command file: " + ex.getMessage(), ex);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    // ignore
                }
            }
        }
    }
    
    public void exit() {
        exit0();
        console.close();
    }

    public void consoleClosed(ConsoleEvent event) {
        if (!exited) {
            if (Thread.currentThread() == ownThread) {
                exit0();
            } else {
                synchronized (this) {
                    exit0();
                    notifyAll();
                }
            }
        }
    }

    private void exit0() {
        exited = true;
    }

    private synchronized boolean isExited() {
        return exited;
    }

    private boolean isHistoryEnabled() {
        return historyEnabled;
    }

    private boolean setHistoryEnabled(boolean historyEnabled) {
        boolean res = this.historyEnabled;
        this.historyEnabled = historyEnabled;
        return res;
    }

    /**
     * This helper does the work of mapping stream marker objects to the streams
     * that they denote. A real stream maps to itself, and <code>null</code>
     * maps to a NullInputStream or NullOutputStream.
     * 
     * @param stream A real stream or a stream marker
     * @return the real stream that the first argument maps to.
     */
    protected CommandIO resolveStream(CommandIO stream) {
        if (stream == CommandLine.DEFAULT_STDIN) {
            return getInputStream();
        } else if (stream == CommandLine.DEFAULT_STDOUT) {
            return cout;
        } else if (stream == CommandLine.DEFAULT_STDERR) {
            return cerr;
        } else if (stream == CommandLine.DEVNULL || stream == null) {
            return new CommandInputOutput(new NullInputStream(), new NullOutputStream());
        } else {
            return stream;
        }
    }

    public void resolveStreams(CommandIO[] ios) {
        for (int i = 0; i < ios.length; i++) {
            ios[i] = resolveStream(ios[i]);
        }
    }

    public PrintStream resolvePrintStream(CommandIO io) {
        CommandIO tmp = resolveStream(io);
        return ((CommandOutput) tmp).getPrintStream();
    }

    public InputStream resolveInputStream(CommandIO io) {
        CommandIO tmp = resolveStream(io);
        return ((CommandInput) tmp).getInputStream();
    }

    public SyntaxManager getSyntaxManager() {
        return syntaxMgr;
    }

    public PrintWriter getOut() {
        return outPW;
    }

    public PrintWriter getErr() {
        return errPW;
    }

    @Override
    public void addConsoleOuputRecorder(Writer writer) {
        // FIXME do security check
        Writer out = cout.getWriter();
        Writer err = cerr.getWriter();
        if (out instanceof FanoutWriter) {
            ((FanoutWriter) out).addStream(writer);
            ((FanoutWriter) err).addStream(writer);
        } else {
            cout = new CommandOutput(new FanoutWriter(true, out, writer));
            outPW = cout.getPrintWriter();
            cerr = new CommandOutput(new FanoutWriter(true, err, writer));
            errPW = cerr.getPrintWriter();
        }
        errPW.println("Testing");
    }

    @Override
    public String escapeWord(String word) {
        return interpreter.escapeWord(word);
    }
}
