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

import gnu.java.security.action.GetPropertyAction;
import gnu.java.security.action.SetPropertyAction;

import java.io.File;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.DateFormat;
import java.util.Date;
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
import org.jnode.shell.help.Argument;
import org.jnode.shell.help.CompletionException;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.HelpException;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.argument.AliasArgument;
import org.jnode.shell.help.argument.FileArgument;
import org.jnode.util.SystemInputStream;
import org.jnode.vm.VmSystem;

/**
 * @author epr
 * @author Fabien DUMINY
 * @authod crawley
 */
public class CommandShell implements Runnable, Shell, ConsoleListener {

    public static final String PROMPT_PROPERTY_NAME = "jnode.prompt";

    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(CommandShell.class);

    private PrintStream out;
    
    private PrintStream err;
    
    private InputStream in;

    private AliasManager aliasMgr;

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
    	new InheritableThreadLocal<InputHistory> ();
    
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

    private static String DEFAULT_PROMPT = "JNode $P$G";

    private static final String command = "cmd=";

    // private static final Class[] MAIN_ARG_TYPES = new Class[] {
    // String[].class };

    private CommandInvoker commandInvoker;

    private ThreadCommandInvoker threadCommandInvoker;

    private DefaultCommandInvoker defaultCommandInvoker;
    
    private ProcletCommandInvoker procletCommandInvoker;

    private boolean historyEnabled = true;

    private boolean exitted = false;

    private Thread ownThread;

    public TextConsole getConsole() {
        return console;
    }
    
    public static void main(String[] args) throws NameNotFoundException, ShellException {
    	CommandShell shell = new CommandShell();
        shell.run();
    }

    public void setThreadCommandInvoker() {
    	if (this.commandInvoker != threadCommandInvoker) {
    		err.println("Switched to thread invoker");
            this.commandInvoker = threadCommandInvoker;
    	}
    }

    public void setDefaultCommandInvoker() {
    	if (this.commandInvoker != defaultCommandInvoker) {
    		err.println("Switched to default invoker");
    		this.commandInvoker = defaultCommandInvoker;
    	}
    }

    public void setProcletCommandInvoker() {
    	if (this.commandInvoker != procletCommandInvoker) {
    		err.println("Switched to proclet invoker");
    		this.commandInvoker = procletCommandInvoker;
    	}
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
            this.console = cons;
            this.out = this.console.getOut();
            this.err = this.console.getErr();
        	this.in = this.console.getIn();
        	SystemInputStream.getInstance().initialize(this.in);
        	cons.setCompleter(this);

            defaultCommandInvoker = new DefaultCommandInvoker(this);
            threadCommandInvoker = new ThreadCommandInvoker(this);
            procletCommandInvoker = new ProcletCommandInvoker(this);
            setThreadCommandInvoker(); // default to separate
            this.console.addConsoleListener(this);
            // threads for commands.
            aliasMgr = ((AliasManager) InitialNaming.lookup(AliasManager.NAME))
                    .createAliasManager();
            AccessController.doPrivileged(new SetPropertyAction(
                    PROMPT_PROPERTY_NAME, DEFAULT_PROMPT));
        	// ShellUtils.getShellManager().registerShell(this);
        } catch (NameNotFoundException ex) {
            throw new ShellException("Cannot find required resource", ex);
        }
        catch (Exception ex) {
        	ex.printStackTrace();
        }
    }

    /**
     * Run this shell until exit.
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {
        ownThread = Thread.currentThread();
        // Here, we are running in the CommandShell (main) Thread
        // so, we can register ourself as the current shell
        // (it will also be the current shell for all children Thread)
        try {
            ShellUtils.getShellManager().registerShell(this);
        } catch (NameNotFoundException e1) {
            e1.printStackTrace();
        }

        // Run commands from the JNode commandline first
        final String cmdLine = (String) AccessController
                .doPrivileged(new GetPropertyAction("jnode.cmdline", ""));
        final StringTokenizer tok = new StringTokenizer(cmdLine);

        while (tok.hasMoreTokens()) {
            final String e = tok.nextToken();
            try {
                if (e.startsWith(command)) {
                    final String cmd = e.substring(command.length());
                    out.println(prompt() + cmd);
                    processCommand(cmd, false);
                }
            } catch (Throwable ex) {
                ex.printStackTrace(err);
            }
        }

        final String user_home = (String) AccessController.doPrivileged(new GetPropertyAction("user.home", ""));
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                try {
                    final File shell_ini = new File(user_home + "/shell.ini");
                    if(shell_ini.exists())
                    executeFile(shell_ini);
                } catch(IOException ioe){
                    ioe.printStackTrace();
                }
                return null;
            }
        });

        // Now become interactive
        while (!isExitted()) {
            try {
            	// Temporary mechanism for switching invokers
            	String invokerName = System.getProperty("jnode.invoker", "");
            	if (invokerName.equalsIgnoreCase("default")) {
            		setDefaultCommandInvoker();
            	}
            	else if (invokerName.equalsIgnoreCase("thread")) {
            		setThreadCommandInvoker();
            	}
            	else if (invokerName.equalsIgnoreCase("proclet")) {
            		setProcletCommandInvoker();
            	}
            	
            	clearEof();
            	out.print(prompt());
            	readingCommand = true;
            	String line = readInputLine().trim();
            	if (line.length() > 0) {
            		processCommand(line, true);
            	}

            	if (VmSystem.isShuttingDown()) {
            		exitted = true;
            	}
            } catch (Throwable ex) {
                ex.printStackTrace(err);
            }
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
    	commandInvoker.invoke(cmdLineStr);
    	if (interactive) {
        	applicationHistory.set(null);
    	}
    }

    public void invokeCommand(String command) {
        processCommand(command, false);
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
    	}
    	else {
    		return CommandShell.applicationHistory.get();
    	}
    }

    /**
     * Gets the expanded prompt
     */
    protected String prompt() {
        String prompt = (String) AccessController.doPrivileged(
                new GetPropertyAction(PROMPT_PROPERTY_NAME));
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
                        result.append(new File((String) AccessController
                                .doPrivileged(new GetPropertyAction("user.dir"))));
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
    
    private final Help.Info defaultParameter = new Help.Info("file",
            "default parameter for command line completion",
			new Parameter(new FileArgument("file", "a file", Argument.MULTI), Parameter.OPTIONAL)
	);
    private final Argument defaultArg = new AliasArgument("command",
            "the command to be called");

    private CompletionInfo completion;

    public CompletionInfo complete(String partial) {
        if (!readingCommand) {
        	// dummy completion behavior for application input.
        	CompletionInfo completion = new CompletionInfo();
            completion.setCompleted(partial);
            completion.setNewPrompt(true);
        	return completion;
        }
        
        // workaround to set the currentShell to this shell
        try {
            ShellUtils.getShellManager().registerShell(this);
        } catch (NameNotFoundException ex) {
        }

        // do command completion
        String result = null;
        completion = new CompletionInfo();
        try {
            CommandLine cl = new CommandLine(partial);
            String cmd = "";
            if (cl.hasNext()) {
                cmd = cl.next();
                if (!cmd.trim().equals("") && cl.hasNext())
                    try {
                        // get command's help info
                        CommandInfo cmdClass = getCommandClass(cmd);

                        Help.Info info = defaultParameter;
                        try {
                            info = Help.getInfo(cmdClass
                                    .getCommandClass());
                        }catch (HelpException ex) {
                            /*ex.printStackTrace();
                            throw new CompletionException("Command class not found");*/

                            //assuming default completion which is multiple files
                        }

                        // perform completion
                        result = cmd + " " + info.complete(cl); // prepend
                        completion.setCompleted(result);
                        // command
                        // name and
                        // space
                        // again
                    } catch (ClassNotFoundException ex) {
                        throw new CompletionException("Command class not found");
                    }
            }
            if (result == null) // assume this is the alias to be called
                result = defaultArg.complete(cmd);

            if (!partial.equals(result) && !completion.hasItems()) {
                // performed direct
                // completion without listing
                completion.setCompleted(result);
                completion.setNewPrompt(false);
            }
        } catch (CompletionException ex) {
            out.println(); // next line
            err.println(ex.getMessage()); // print the error (optional)
            // this debug output is to trace where the Exception came from
            // ex.printStackTrace(err);
            // restore old value and need a new prompt
            completion.setCompleted(partial);
            completion.setNewPrompt(true);
        }

        return completion;
    }

    public void list(String[] items) {
        completion.setItems(items);
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

    public InputStream getInputStream() {
    	if (isHistoryEnabled()) {
    		// Insert a filter on the input stream that adds completed input lines
    		// to the application input history.
    		return new HistoryInputStream(in);
    	}
    	else {
            return in;
    	}
    }
    
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
			}
			else {
				line.append((char) b);
			}
		}
	}

    public PrintStream getOutputStream() {
        return out;
    }

    public PrintStream getErrorStream() {
        return err;
    }

    public DefaultCommandInvoker getDefaultCommandInvoker() {
        return defaultCommandInvoker;
    }

    public void executeFile(File file) throws IOException {
        if(!file.exists()){
            System.err.println( "File does not exist: " + file);
            return;
        }
        try{
            setHistoryEnabled(false);
            final BufferedReader br = new BufferedReader(new FileReader(file));
            for(String line = br.readLine(); line != null; line = br.readLine()){
                line = line.trim();

                if(line.startsWith("#") || line.equals(""))
                    continue;

                invokeCommand(line);
            }
            br.close();
        }finally{
            setHistoryEnabled(true);
        }
    }

    public void exit(){

        exit0();
        console.close();

    }

    public void consoleClosed(ConsoleEvent event) {
        if(!exitted)
            if(Thread.currentThread() == ownThread){
                exit0();
            } else {
                synchronized(this){
                    exit0();
                    notifyAll();
                }
            }

    }

    private void exit0() {
        exitted = true;
        threadSuspended = false;
    }

    private synchronized boolean isExitted(){
        return exitted;
    }

    private boolean isHistoryEnabled() {
        return historyEnabled;
    }

    private void setHistoryEnabled(boolean historyEnabled) {
        this.historyEnabled = historyEnabled;
    }
}

