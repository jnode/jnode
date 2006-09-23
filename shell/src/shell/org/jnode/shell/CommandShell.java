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

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.PrintStream;
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
import org.jnode.driver.console.ConsoleManager;
import org.jnode.driver.console.TextConsole;
import org.jnode.driver.input.KeyboardEvent;
import org.jnode.driver.input.KeyboardListener;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.alias.AliasManager;
import org.jnode.shell.alias.NoSuchAliasException;
import org.jnode.shell.help.AliasArgument;
import org.jnode.shell.help.Argument;
import org.jnode.shell.help.CompletionException;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.HelpException;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.FileArgument;
import org.jnode.vm.VmSystem;

/**
 * @author epr
 * @author Fabien DUMINY
 */
public class CommandShell implements Runnable, Shell, KeyboardListener {

    public static final String PROMPT_PROPERTY_NAME = "jnode.prompt";

    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(CommandShell.class);

    private PrintStream out;

    private PrintStream err;

    private AliasManager aliasMgr;

    /**
     * Keeps a reference to the console this CommandShell is using *
     */
    private final TextConsole console;

    /**
     * Contains the archive of commands. *
     */
    private CommandHistory history = new CommandHistory();

    /**
     * Contains an index to the current history line. 0 = first historical
     * command. 2 = next historical command. -1 = the current command line.
     */
    private int historyIndex = -1;

    /**
     * Contains the current line *
     */
    private Line currentLine;

    /**
     * Contains the newest command being typed in *
     */
    private String newestLine = "";

    /**
     * Flag to know if the shell is active to take the keystrokes *
     */
    private boolean isActive = false;

    private String currentPrompt = null;

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

    private boolean historyEnabled = true;

    private boolean exitted = false;

    public TextConsole getConsole() {
        return console;
    }
    
    public static void main(String[] args) throws NameNotFoundException, ShellException {
        CommandShell shell = new CommandShell();
        shell.run();
    }

    public void setThreadCommandInvoker() {
        this.commandInvoker = threadCommandInvoker;
    }

    public void setDefaultCommandInvoker() {
        this.commandInvoker = defaultCommandInvoker;
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
            this.currentLine = new Line(console, this, out);

            // listen to the keyboard
            this.console.addKeyboardListener(this);
            defaultCommandInvoker = new DefaultCommandInvoker(this);
            threadCommandInvoker = new ThreadCommandInvoker(this);
            this.commandInvoker = threadCommandInvoker; // default to separate
            // threads for commands.
            aliasMgr = ((AliasManager) InitialNaming.lookup(AliasManager.NAME))
                    .createAliasManager();
            AccessController.doPrivileged(new SetPropertyAction(
                    PROMPT_PROPERTY_NAME, DEFAULT_PROMPT));
            // ShellUtils.getShellManager().registerShell(this);
        } catch (NameNotFoundException ex) {
            throw new ShellException("Cannot find required resource", ex);
        }
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
                    currentPrompt = prompt();
                    out.println(currentPrompt + cmd);
                    processCommand(cmd);
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
                synchronized (this) {
                    // Catch keyboard events
                    isActive = true;
                    currentPrompt = prompt();
                    out.print(currentPrompt);
                    currentLine.start();

                    // wait until enter is hit
                    threadSuspended = true;
                    while (threadSuspended) {
                        wait();
                    }

                    String line = currentLine.getContent().trim();
                    if (line.length() > 0) {
                        processCommand(line);
                    }

                    // if (currentLine.trim().equals("halt")) halt = true;
                    historyIndex = -1;

                    if (VmSystem.isShuttingDown()) {
                        exitted = true;
                    }
                }

            } catch (Throwable ex) {
                ex.printStackTrace(err);
            }
        }
    }

    protected void processCommand(String cmdLineStr) {
        commandInvoker.invoke(cmdLineStr);
    }

    public void invokeCommand(String command) {
        processCommand(command);
    }

    // /**
    // * Execute a single command line.
    // *
    // * @param cmdLineStr
    // */
    // protected void processCommand(String cmdLineStr) {
    //
    // final CommandLine cmdLine = new CommandLine(cmdLineStr);
    // if (!cmdLine.hasNext())
    // return;
    // String cmdName = cmdLine.next();
    //
    // // Add this command to the history.
    // if (!cmdLineStr.equals(newestLine))
    // history.addCommand(cmdLineStr);
    //
    // try {
    // Class cmdClass = getCommandClass(cmdName);
    // final Method main = cmdClass.getMethod("main", MAIN_ARG_TYPES);
    // try {
    // main.invoke(null, new Object[] {
    // cmdLine.getRemainder().toStringArray()});
    // } catch (InvocationTargetException ex) {
    // Throwable tex = ex.getTargetException();
    // if (tex instanceof SyntaxError) {
    // Help.getInfo(cmdClass).usage();
    // err.println(tex.getMessage());
    // } else {
    // err.println("Exception in command");
    // tex.printStackTrace(err);
    // }
    // } catch (Exception ex) {
    // err.println("Exception in command");
    // ex.printStackTrace(err);
    // } catch (Error ex) {
    // err.println("Fatal error in command");
    // ex.printStackTrace(err);
    // }
    // } catch (NoSuchMethodException ex) {
    // err.println("Alias class has no main method " + cmdName);
    // } catch (ClassNotFoundException ex) {
    // err.println("Unknown alias class " + ex.getMessage());
    // } catch (ClassCastException ex) {
    // err.println("Invalid command " + cmdName);
    // } catch (Exception ex) {
    // err.println("I FOUND AN ERROR: " + ex);
    // }
    // }

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
     * Gets the CommandHistory object associated with this shell.
     */
    public CommandHistory getCommandHistory() {
        return history;
    }

    /**
     * Gets the expanded prompt
     */
    protected String prompt() {
        String prompt = System.getProperty(PROMPT_PROPERTY_NAME);
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
                        result.append(new File(
                                (String) AccessController
                                        .doPrivileged(new GetPropertyAction(
                                                "user.dir"))));
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

    // ********** KeyboardListener implementation **********//
    /**
     * Method keyPressed
     * 
     * @param ke
     *            a KeyboardEvent <p/> 2/5/2004
     */
    public void keyPressed(KeyboardEvent ke) {
        // make sure we are ready to intercept the keyboard
        if (!isActive)
            return;

        switch (ke.getKeyCode()) {
        // intercept the up and down arrow keys
        case KeyEvent.VK_UP:
            ke.consume();
            if (historyIndex == -1) {
                newestLine = currentLine.getContent();
                historyIndex = history.size();
            }
            historyIndex--;
            redisplay();
            break;
        case KeyEvent.VK_DOWN:
            ke.consume();
            if (historyIndex == history.size() - 1)
                historyIndex = -2;
            else if (historyIndex == -1)
                newestLine = currentLine.getContent();
            historyIndex++;
            redisplay();
            break;
        case KeyEvent.VK_LEFT:
            // Left the cursor goes left
            ke.consume();
            if (currentLine.moveLeft()) {
                refreshCurrentLine();
            }
            break;
        case KeyEvent.VK_RIGHT:
            // Right the cursor goes right
            ke.consume();
            if (currentLine.moveRight()) {
                refreshCurrentLine();
            }
            break;
        case KeyEvent.VK_HOME:
            // The cursor goes at the start
            ke.consume();
            currentLine.moveBegin();
            refreshCurrentLine();
            break;
        case KeyEvent.VK_END:
            // the cursor goes at the end of line
            ke.consume();
            currentLine.moveEnd();
            refreshCurrentLine();
            break;
        // if its a backspace we want to remove one from the end of our current
        // line
        case KeyEvent.VK_BACK_SPACE:
            ke.consume();
            if (currentLine.backspace()) {
                refreshCurrentLine();
            }
            break;

        // if its a delete we want to remove one under the cursor
        case KeyEvent.VK_DELETE:
            ke.consume();
            currentLine.delete();
            refreshCurrentLine();
            break;

        // if its an enter key we want to process the command, and then resume
        // the thread
        case KeyEvent.VK_ENTER:
            ke.consume();

            currentLine.moveEnd();
            refreshCurrentLine();
            out.print(ke.getKeyChar());
            synchronized (this) {
                isActive = false;
                threadSuspended = false;
                notifyAll();
            }
            break;

        // if it's the tab key, we want to trigger command line completion
        case KeyEvent.VK_TAB:
            ke.consume();
            /* CompletionInfo info = */
            currentLine.complete(currentPrompt);

            if (completion.needNewPrompt()) {
                currentLine.start(true);
            }
            refreshCurrentLine();

            break;

        default:
            // if its a useful key we want to add it to our current line
            char ch = ke.getKeyChar();
            if (!Character.isISOControl(ch)) {
                if (ke.isControlDown() && ch == 'l') {
                    this.console.clear();
                    this.console.setCursor(
                            (currentPrompt != null ? currentPrompt.length()
                                    : this.console.getWidth() - 1), 1);
                    currentLine.start();
                    refreshCurrentLine();
                } else {
                    ke.consume();
                    currentLine.appendChar(ch);
                    refreshCurrentLine();
                }
            }
        }
    }

    private void refreshCurrentLine() {
        currentLine.refreshCurrentLine(currentPrompt);
    }

    public void keyReleased(KeyboardEvent ke) {
        // do nothing
    }

    private void redisplay() {
        if (historyIndex == -1)
            currentLine.setContent(newestLine);
        else
            currentLine.setContent(history.getCommand(historyIndex));

        refreshCurrentLine();
        currentLine.moveEnd();
    }

    // Command line completion

    private final Help.Info defaultParameter = new Help.Info("file",
            "default parameter for command line completion",
		new Parameter[]{
			new Parameter(new FileArgument("file", "a file", Argument.MULTI), Parameter.OPTIONAL)
		}
	);
    private final Argument defaultArg = new AliasArgument("command",
            "the command to be called");

    private CompletionInfo completion;

    CompletionInfo complete(String partial) {
        // workaround to set the currentShell to this shell
        try {
            ShellUtils.getShellManager().registerShell(this);
        } catch (NameNotFoundException ex) {
        }

        completion = new CompletionInfo();
        String result = null;
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
        // Add this command to the history.
        if (isHistoryEnabled() && !cmdLineStr.equals(newestLine))
            history.addCommand(cmdLineStr);
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
        exitted = true;
        console.close();
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

/**
 * A class that handles the content of the current command line in the shell.
 * That can be : - a new command that the user is beeing editing - an existing
 * command (from the command history)
 * 
 * This class also handles the current cursor position in the command line and
 * keep trace of the position (consoleX, consoleY) of the first character of the
 * command line (to handle commands that are multilines).
 * 
 * @author Fabien DUMINY
 */
