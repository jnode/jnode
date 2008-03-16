/*
 * $Id$
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

package org.jnode.shell;

import java.io.Closeable;
import java.io.File;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import javax.naming.NameNotFoundException;

import org.apache.log4j.Logger;
import org.jnode.driver.console.InputHistory;
import org.jnode.driver.console.CompletionInfo;
import org.jnode.driver.console.ConsoleManager;
import org.jnode.driver.console.TextConsole;
import org.jnode.driver.console.ConsoleListener;
import org.jnode.driver.console.ConsoleEvent;
import org.jnode.driver.console.textscreen.KeyboardInputStream;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.alias.AliasManager;
import org.jnode.shell.alias.NoSuchAliasException;
import org.jnode.shell.help.CompletionException;
import org.jnode.shell.syntax.ArgumentBundle;
import org.jnode.shell.syntax.SyntaxManager;
import org.jnode.util.SystemInputStream;
import org.jnode.vm.VmSystem;

/**
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

    public static final String HOME_PROPERTY_NAME = "user.home";
    public static final String DIRECTORY_PROPERTY_NAME = "user.dir";

    public static final String INITIAL_INVOKER = "thread";
    public static final String INITIAL_INTERPRETER = "redirecting";
    public static final String FALLBACK_INVOKER = "default";
    public static final String FALLBACK_INTERPRETER = "default";

    private static String DEFAULT_PROMPT = "JNode $P$G";
    private static final String COMMAND_KEY = "cmd=";

    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(CommandShell.class);

    private PrintStream out;

    private PrintStream err;

    private InputStream in;

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
    private static InheritableThreadLocal<InputHistory> applicationHistory = new InheritableThreadLocal<InputHistory>();

    private boolean readingCommand;

    /**
     * Contains the last command entered
     */
    private String lastCommandLine = "";

    /**
     * Contains the last application input line entered
     */
    private String lastInputLine = "";

    /**
     * Flag to know when to wait (while input is happening). This is (hopefully)
     * a thread safe implementation. *
     */
    private volatile boolean threadSuspended = false;

    private CommandInvoker invoker;
    private String invokerName;

    private CommandInterpreter interpreter;
    private String interpreterName;

    private CompletionInfo completion;

    private boolean historyEnabled;

    private boolean debugEnabled;

    private boolean exited = false;

    private Thread ownThread;

    public TextConsole getConsole() {
        return console;
    }

    public static void main(String[] args) throws NameNotFoundException,
            ShellException {
        CommandShell shell = new CommandShell();
        shell.run();
    }

    /**
     * Create a new instance
     * 
     * @see java.lang.Object
     */
    public CommandShell() throws NameNotFoundException, ShellException {
        this((TextConsole) ((ConsoleManager) InitialNaming
                .lookup(ConsoleManager.NAME)).getFocus());
    }

    public CommandShell(TextConsole cons) throws ShellException {
        try {
            console = cons;
            out = console.getOut();
            err = console.getErr();
            in = console.getIn();
            SystemInputStream.getInstance().initialize(this.in);
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

    protected CommandShell(TextConsole console, InputStream in,
            PrintStream out, PrintStream err) throws ShellException {
        try {
            this.console = console;
            this.out = out;
            this.err = err;
            this.in = in;
            SystemInputStream.getInstance().initialize(this.in);
            // cons.setCompleter(this);

            // console.addConsoleListener(this);
            aliasMgr = ((AliasManager) InitialNaming.lookup(AliasManager.NAME))
                    .createAliasManager();
            System.setProperty(PROMPT_PROPERTY_NAME, DEFAULT_PROMPT);
        } catch (NameNotFoundException ex) {
            throw new ShellException("Cannot find required resource", ex);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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

        // FIXME - At one point, the 'current shell' had something to do with
        // dispatching keyboard input to the right application. Now this is
        // handled by the console layer. Is 'current shell' a meaningful /
        // useful concept anymore?
        try {
            ShellUtils.getShellManager().registerShell(this);

            ShellUtils.registerCommandInvoker(DefaultCommandInvoker.FACTORY);
            ShellUtils.registerCommandInvoker(ThreadCommandInvoker.FACTORY);
            ShellUtils.registerCommandInvoker(ProcletCommandInvoker.FACTORY);
            ShellUtils.registerCommandInterpreter(DefaultInterpreter.FACTORY);
            ShellUtils
                    .registerCommandInterpreter(RedirectingInterpreter.FACTORY);
        } catch (NameNotFoundException e1) {
            e1.printStackTrace();
        }

        // Configure the shell based on Syetsm properties.
        setupFromProperties();

        // Now become interactive
        ownThread = Thread.currentThread();

        // Run commands from the JNode commandline first
        final String cmdLine = System.getProperty(CMDLINE_PROPERTY_NAME, "");
        final StringTokenizer tok = new StringTokenizer(cmdLine);

        while (tok.hasMoreTokens()) {
            final String e = tok.nextToken();
            try {
                if (e.startsWith(COMMAND_KEY)) {
                    final String cmd = e.substring(COMMAND_KEY.length());
                    out.println(prompt() + cmd);
                    processCommand(cmd, false);
                }
            } catch (Throwable ex) {
                err.println("Error while processing bootarg commands: "
                        + ex.getMessage());
                stackTrace(ex);
            }
        }

        final String user_home = System.getProperty(HOME_PROPERTY_NAME, "");

        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                final File shell_ini = new File(user_home + "/shell.ini");
                try {
                    if (shell_ini.exists()) {
                        executeFile(shell_ini);
                    }
                } catch (IOException ex) {
                    err.println("Error while reading " + shell_ini + ": "
                            + ex.getMessage());
                    stackTrace(ex);
                }
                return null;
            }
        });

        while (!isExited()) {
            try {
                refreshFromProperties();

                clearEof();
                out.print(prompt());
                readingCommand = true;
                String line = readInputLine().trim();
                if (line.length() > 0) {
                    processCommand(line, true);
                }

                if (VmSystem.isShuttingDown()) {
                    exited = true;
                }
            } catch (Throwable ex) {
                err.println("Uncaught exception while processing command(s): "
                        + ex.getMessage());
                stackTrace(ex);
            }
        }
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
            err.println(ex.getMessage());
            stackTrace(ex);
            // Use the fallback invoker
            setCommandInvoker(FALLBACK_INVOKER);
        }
        try {
            setCommandInterpreter(System.getProperty(INTERPRETER_PROPERTY_NAME,
                    INITIAL_INTERPRETER));
        } catch (Exception ex) {
            err.println(ex.getMessage());
            stackTrace(ex);
            // Use the fallback interpreter
            setCommandInterpreter(FALLBACK_INTERPRETER);
        }
    }

    private void refreshFromProperties() {
        debugEnabled = Boolean.parseBoolean(System.getProperty(
                DEBUG_PROPERTY_NAME, DEBUG_DEFAULT));
        historyEnabled = Boolean.parseBoolean(System.getProperty(
                HISTORY_PROPERTY_NAME, HISTORY_DEFAULT));
        try {
            setCommandInterpreter(System.getProperty(INTERPRETER_PROPERTY_NAME,
                    ""));
        } catch (Exception ex) {
            err.println(ex.getMessage());
            stackTrace(ex);
        }
        try {
            setCommandInvoker(System.getProperty(INVOKER_PROPERTY_NAME, ""));
        } catch (Exception ex) {
            err.println(ex.getMessage());
            stackTrace(ex);
        }
    }

    public synchronized void setCommandInvoker(String name)
            throws IllegalArgumentException {
        if (!name.equals(this.invokerName)) {
            this.invoker = ShellUtils.createInvoker(name, this);
            err.println("Switched to " + name + " invoker");
            this.invokerName = name;
            System.setProperty(INVOKER_PROPERTY_NAME, name);
        }
    }

    public synchronized void setCommandInterpreter(String name)
            throws IllegalArgumentException {
        if (!name.equals(this.interpreterName)) {
            this.interpreter = ShellUtils.createInterpreter(name);
            err.println("Switched to " + name + " interpreter");
            this.interpreterName = name;
            System.setProperty(INTERPRETER_PROPERTY_NAME, name);
        }
    }

    private void stackTrace(Throwable ex) {
        if (this.debugEnabled) {
            ex.printStackTrace(err);
        }
    }

    private String readInputLine() throws IOException {
        StringBuffer sb = new StringBuffer(40);
        Reader r = new InputStreamReader(in);
        while (true) {
            int ch = r.read();
            if (ch == -1 || ch == '\n') {
                return sb.toString();
            }
            sb.append((char) ch);
        }
    }

    private void clearEof() {
        if (in instanceof KeyboardInputStream) {
            ((KeyboardInputStream) in).clearSoftEOF();
        }
    }

    protected void processCommand(String cmdLineStr, boolean interactive) {
        clearEof();
        if (interactive) {
            readingCommand = false;
            // Each interactive command is launched with a fresh history
            // for input completion
            applicationHistory.set(new InputHistory());
        }
        try {
            interpreter.interpret(this, cmdLineStr);
        } catch (ShellException ex) {
            err.println("Shell exception: " + ex.getMessage());
            stackTrace(ex);
        }

        if (interactive) {
            applicationHistory.set(null);
        }
    }

    /**
     * Parse and run a command line using the CommandShell's current
     * interpreter.
     * 
     * @param command the command line.
     * @throws ShellException
     */
    public void invokeCommand(String command) throws ShellException {
        processCommand(command, false);
    }

    /**
     * Run a command encoded as a CommandLine object. The command line will give
     * the command name (alias), the argument list and the IO stream. The
     * command is run using the CommandShell's current invoker.
     * 
     * @param cmdLine the CommandLine object.
     * @return the command's return code
     * @throws ShellException
     */
    public int invoke(CommandLine cmdLine, Command command) throws ShellException {
        return this.invoker.invoke(cmdLine, command);
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
    public CommandThread invokeAsynchronous(CommandLine cmdLine, Command command)
            throws ShellException {
        return this.invoker.invokeAsynchronous(cmdLine, command);
    }

    protected CommandInfo getCommandClass(String cmd)
            throws ClassNotFoundException {
        try {
            Class cls = aliasMgr.getAliasClass(cmd);
            return new CommandInfo(cls, aliasMgr.isInternal(cmd));
        } catch (NoSuchAliasException ex) {
            final ClassLoader cl = Thread.currentThread()
                    .getContextClassLoader();
            return new CommandInfo(cl.loadClass(cmd), false);
        }
    }
    
    protected ArgumentBundle getCommandArgumentBundle(CommandInfo commandInfo) {
		if (Command.class.isAssignableFrom(commandInfo.getCommandClass())) {
			try {
				Command cmd = (Command) (commandInfo.getCommandClass().newInstance());
				return cmd.getArgumentBundle();
			}
			catch (Exception ex) {
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
                        result.append(new File(System.getProperty(
                                DIRECTORY_PROPERTY_NAME, "")));
                        break;
                    case 'G':
                        result.append("> ");
                        break;
                    case 'D':
                        final Date now = new Date();
                        DateFormat.getDateTimeInstance().format(now, result,
                                null);
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

    public Completable parseCommandLine(String cmdLineStr)
            throws ShellSyntaxException {
        return interpreter.parsePartial(this, cmdLineStr);
    }
    
    /**
     * This method is called by the console input driver to perform command line
     * completion in response to a TAB character.
     */
    public CompletionInfo complete(String partial) {
        if (!readingCommand) {
            // dummy completion behavior for application input.
            CompletionInfo completion = new CompletionInfo();
            completion.setCompleted(partial);
            completion.setNewPrompt(true);
            return completion;
        }

        // workaround to set the currentShell to this shell
        // FIXME is this needed?
        try {
            ShellUtils.getShellManager().registerShell(this);
        } catch (NameNotFoundException ex) {
        }

        // do command completion
        completion = new CompletionInfo();
        boolean success = false;
        try {
            Completable cl = parseCommandLine(partial);
            if (cl != null) {
                cl.complete(completion, this);
                if (!partial.equals(completion.getCompleted())
                        && !completion.hasItems()) {
                    // we performed direct completion without listing
                    completion.setNewPrompt(false);
                }
                success = true;
            }
        } catch (ShellSyntaxException ex) {
            out.println(); // next line
            err.println("Cannot parse: " + ex.getMessage());

        } catch (CompletionException ex) {
            out.println(); // next line
            err.println("Problem in completer: " + ex.getMessage());
        }

        if (!success) {
            // Make sure the caller knows to repaint the prompt
            completion.setCompleted(partial);
            completion.setNewPrompt(true);
        }

        // Make sure that the shell's completion context gets nulled.
        CompletionInfo myCompletion = completion;
        completion = null;
        return myCompletion;
    }

    public void list(String[] items) {
        if (completion == null) {
            throw new ShellFailureException(
                    "list called when no completion is in progress");
        } else {
            completion.setItems(items);
        }
    }

    public void addCommandToHistory(String cmdLineStr) {
        // Add this command to the command history.
        if (isHistoryEnabled() && !cmdLineStr.equals(lastCommandLine)) {
            commandHistory.addLine(cmdLineStr);
            lastCommandLine = cmdLineStr;
        }
    }

    public void addInputToHistory(String inputLine) {
        // Add this input to the application input history.
        if (isHistoryEnabled() && !inputLine.equals(lastInputLine)) {
            InputHistory history = applicationHistory.get();
            if (history != null) {
                history.addLine(inputLine);
                lastInputLine = inputLine;
            }
        }
    }

    private InputStream getInputStream() {
        if (isHistoryEnabled()) {
            // Insert a filter on the input stream that adds completed input
            // lines
            // to the application input history. (Since the filter is stateless,
            // it doesn't really matter if we do this multiple times.)
            // FIXME if we partition the app history by application, we will
            // need
            // to bind the history object in the history input stream
            // constructor.
            return new HistoryInputStream(in);
        } else {
            return in;
        }
    }

    /**
     * This subtype of FilterInputStream captures the console input for an
     * application in the application input history.
     */
    private class HistoryInputStream extends FilterInputStream {
        // TODO - revisit for support of multi-byte character encodings.
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
            int res = super.read(buf);
            for (int i = 0; i < res; i++) {
                filter(buf[i]);
            }
            return res;
        }

        private void filter(byte b) {
            if (b == '\n') {
                addInputToHistory(line.toString());
                line.setLength(0);
            } else {
                line.append((char) b);
            }
        }
    }

    public CommandInvoker getDefaultCommandInvoker() {
        return ShellUtils.createInvoker("default", this);
    }

    public void executeFile(File file) throws IOException {
        if (!file.exists()) {
            err.println("File does not exist: " + file);
            return;
        }
        try {
            setHistoryEnabled(false);
            final BufferedReader br = new BufferedReader(new FileReader(file));
            for (String line = br.readLine(); line != null; line = br
                    .readLine()) {
                line = line.trim();

                if (line.startsWith("#") || line.equals("")) {
                    continue;
                }
                try {
                    invokeCommand(line);
                } catch (ShellException ex) {
                    err.println("Shell exception: " + ex.getMessage());
                    stackTrace(ex);
                }
            }
            br.close();
        } finally {
            setHistoryEnabled(true);
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
        threadSuspended = false;
    }

    private synchronized boolean isExited() {
        return exited;
    }

    private boolean isHistoryEnabled() {
        return historyEnabled;
    }

    private void setHistoryEnabled(boolean historyEnabled) {
        this.historyEnabled = historyEnabled;
    }

    /**
     * This helper does the work of mapping stream marker objects to the streams
     * that they denote. A real stream maps to itself, and <code>null</code>
     * maps to a NullInputStream or NullOutputStream.
     * 
     * @param stream A real stream or a stream marker
     * @param input If <code>true</code>, we want an input stream.
     * @return the real stream that the first argument maps to.
     */
    private Object resolveStream(Closeable stream, boolean input) {
        if (stream == CommandLine.DEFAULT_STDIN) {
            return getInputStream();
        } else if (stream == CommandLine.DEFAULT_STDOUT) {
            return out;
        } else if (stream == CommandLine.DEFAULT_STDERR) {
            return err;
        } else if (stream == CommandLine.DEVNULL || stream == null) {
            return input ? new NullInputStream() : new NullOutputStream();
        } else {
            return stream;
        }
    }

    /**
     * Resolve a stream as a real (usable) InputStream.
     * 
     * @param stream the stream to be resolved
     * @return the resolved InputStream.
     */
    public InputStream resolveInputStream(Closeable stream) {
        return (InputStream) resolveStream(stream, true);
    }

    /**
     * Resolve a stream as a real (usable) PrintStream. If the argument is an
     * OutputStream, we wrap it in a PrintStream. This means that if you call
     * this method twice on the same stream argument, you may get different
     * result objects.
     * 
     * @param stream the stream to be resolved
     * @return the resolved PrintStream.
     */
    public PrintStream resolvePrintStream(Closeable stream) {
        Object tmp = resolveStream(stream, false);
        if (tmp instanceof PrintStream) {
            return (PrintStream) tmp;
        } else {
            // We could try to maintain a cache of PrintStream wrappers,
            // but this is liable to extend the lifetime of the wrapped streams,
            // which is a bad thing.
            return new PrintStream((OutputStream) tmp);
        }
    }

    public SyntaxManager getSyntaxManager() {
        return syntaxMgr;
    }
}
