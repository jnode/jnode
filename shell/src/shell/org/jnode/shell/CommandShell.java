/**
 * $Id$
 */
package org.jnode.shell;

import gnu.java.security.actions.GetPropertyAction;
import gnu.java.security.actions.SetPropertyAction;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.PrintStream;
import java.io.StringReader;
import java.security.AccessController;
import java.text.DateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import javax.naming.NameNotFoundException;

import org.apache.log4j.Logger;
import org.apache.tools.ant.taskdefs.UpToDate;
import org.jnode.driver.console.Console;
import org.jnode.driver.console.ConsoleManager;
import org.jnode.driver.console.ConsoleOutputStream;
import org.jnode.driver.console.x86.ScrollableShellConsole;
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
import org.jnode.vm.VmSystem;

/**
 * @author epr
 * @author Fabien DUMINY
 */
public class CommandShell implements Runnable, Shell, KeyboardListener {

    public static final String PROMPT_PROPERTY_NAME = "jnode.prompt";

    /** My logger */
    private final Logger log = Logger.getLogger(getClass());

    private PrintStream out;

    private PrintStream err;

    private AliasManager aliasMgr;

    /** Keeps a reference to the console this CommandShell is using * */
    private ScrollableShellConsole console = null;

    /** Contains the archive of commands. * */
    private CommandHistory history = new CommandHistory();

    /**
     * Contains an index to the current history line. 0 = first historical
     * command. 2 = next historical command. -1 = the current command line.
     */
    private int historyIndex = -1;

    /** Contains the current line * */
    private Line currentLine = new Line();

    /** Contains the newest command being typed in * */
    private String newestLine = "";

    /** Flag to know if the shell is active to take the keystrokes * */
    private boolean isActive = false;

    private String currentPrompt = null;

    /**
     * Flag to know when to wait (while input is happening). This is (hopefully)
     * a thread safe implementation. *
     */
    private volatile boolean threadSuspended = false;

    private static String DEFAULT_PROMPT = "JNode $P$G";

    //private static final Class[] MAIN_ARG_TYPES = new Class[] {
    // String[].class };

    private CommandInvoker commandInvoker;

    private ThreadCommandInvoker threadCommandInvoker;

    private DefaultCommandInvoker defaultCommandInvoker;

    public Console getConsole() {
        return console;
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
        this((ScrollableShellConsole) ((ConsoleManager) InitialNaming
                .lookup(ConsoleManager.NAME)).getFocus());
    }

    public CommandShell(ScrollableShellConsole cons) throws ShellException {
        try {
            this.console = cons;
            this.out = this.console.getOut();
            this.err = this.console.getErr();

            //  listen to the keyboard
            this.console.addKeyboardListener(this);
            defaultCommandInvoker = new DefaultCommandInvoker(this);
            threadCommandInvoker = new ThreadCommandInvoker(this);
            this.commandInvoker = threadCommandInvoker; //default to separate
            // threads for commands.
            aliasMgr = ((AliasManager) InitialNaming.lookup(AliasManager.NAME))
                    .createAliasManager();
            AccessController.doPrivileged(new SetPropertyAction(
                    PROMPT_PROPERTY_NAME, DEFAULT_PROMPT));
            ShellUtils.getShellManager().registerShell(this);
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
        // Run commands from the JNode commandline first
        final String cmdLine = (String) AccessController
                .doPrivileged(new GetPropertyAction("jnode.cmdline", ""));
        final StringTokenizer tok = new StringTokenizer(cmdLine);
        while (tok.hasMoreTokens()) {
            final String e = tok.nextToken();
            try {
                if (e.startsWith("cmd=")) {
                    final String cmd = e.substring("cmd=".length());
                    currentPrompt = prompt();
                    out.println(currentPrompt + cmd);
                    processCommand(cmd);
                }
            } catch (Throwable ex) {
                ex.printStackTrace(err);
            }
        }

        // Now become interactive
        boolean halt = false;
        while (!halt) {
            try {
                synchronized (this) {
                    //  Catch keyboard events
                    isActive = true;
                    currentPrompt = prompt();
                    out.print(currentPrompt);
                    currentLine.start(console);
                    
                    //  wait until enter is hit
                    threadSuspended = true;
                    while (threadSuspended) {
                        wait();
                    }
                    
                    String line = currentLine.getContent().trim();
                    if (line.length() > 0) {
                        processCommand(line);
                    }

                    if (VmSystem.isShuttingDown()) {
                        halt = true;
                    }

                    //if (currentLine.trim().equals("halt")) halt = true;
                    historyIndex = -1;
                }

            } catch (Throwable ex) {
                ex.printStackTrace(err);
            }
        }
    }

    protected void processCommand(String cmdLineStr) {
        commandInvoker.invoke(cmdLineStr);
    }

    //	/**
    //	 * Execute a single command line.
    //	 *
    //	 * @param cmdLineStr
    //	 */
    //	protected void processCommand(String cmdLineStr) {
    //
    //		final CommandLine cmdLine = new CommandLine(cmdLineStr);
    //		if (!cmdLine.hasNext())
    //			return;
    //		String cmdName = cmdLine.next();
    //
    //		// Add this command to the history.
    //		if (!cmdLineStr.equals(newestLine))
    //			history.addCommand(cmdLineStr);
    //
    //		try {
    //			Class cmdClass = getCommandClass(cmdName);
    //			final Method main = cmdClass.getMethod("main", MAIN_ARG_TYPES);
    //			try {
    //				main.invoke(null, new Object[] {
    // cmdLine.getRemainder().toStringArray()});
    //			} catch (InvocationTargetException ex) {
    //				Throwable tex = ex.getTargetException();
    //				if (tex instanceof SyntaxError) {
    //					Help.getInfo(cmdClass).usage();
    //					err.println(tex.getMessage());
    //				} else {
    //					err.println("Exception in command");
    //					tex.printStackTrace(err);
    //				}
    //			} catch (Exception ex) {
    //				err.println("Exception in command");
    //				ex.printStackTrace(err);
    //			} catch (Error ex) {
    //				err.println("Fatal error in command");
    //				ex.printStackTrace(err);
    //			}
    //		} catch (NoSuchMethodException ex) {
    //			err.println("Alias class has no main method " + cmdName);
    //		} catch (ClassNotFoundException ex) {
    //			err.println("Unknown alias class " + ex.getMessage());
    //		} catch (ClassCastException ex) {
    //			err.println("Invalid command " + cmdName);
    //		} catch (Exception ex) {
    //			err.println("I FOUND AN ERROR: " + ex);
    //		}
    //	}

    protected Class getCommandClass(String cmd) throws ClassNotFoundException {
        try {
            return aliasMgr.getAliasClass(cmd);
        } catch (NoSuchAliasException ex) {
            final ClassLoader cl = Thread.currentThread()
                    .getContextClassLoader();
            return cl.loadClass(cmd);
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

    //********** KeyboardListener implementation **********//
    /**
     * Method keyPressed
     * 
     * @param ke a KeyboardEvent
     * 
     * @version 2/5/2004
     */
    public void keyPressed(KeyboardEvent ke) {
        //  make sure we are ready to intercept the keyboard
        if (!isActive) return;

        switch (ke.getKeyCode()) {
        //  intercept the up and down arrow keys
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
            else if (historyIndex == -1) newestLine = currentLine.getContent();
            historyIndex++;
            redisplay();
            break;
        case KeyEvent.VK_LEFT:
            //Left the cursor goes left
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
        //  if its a backspace we want to remove one from the end of our current
        // line
        case KeyEvent.VK_BACK_SPACE:
            ke.consume();
            if (currentLine.backspace()) {
                refreshCurrentLine();
            }
            break;

        //  if its a delete we want to remove one under the cursor
        case KeyEvent.VK_DELETE:
            ke.consume();
        	currentLine.delete();
            refreshCurrentLine();
            break;

        //  if its an enter key we want to process the command, and then resume
        // the thread
        case KeyEvent.VK_ENTER:
            out.print(ke.getKeyChar());
            ke.consume();
            currentLine.moveBegin();			
            synchronized (this) {
                isActive = false;
                threadSuspended = false;
                notifyAll();
            }
            break;

        // if it's the tab key, we want to trigger command line completion
        case KeyEvent.VK_TAB:
            ke.consume();
        	if(currentLine.complete(console, this))
        		refreshCurrentLine();
            break;

        default:
            //  if its a useful key we want to add it to our current line
        	char ch = ke.getKeyChar();
            if (!Character.isISOControl(ch)) {
                ke.consume();
                currentLine.appendChar(ch);
                refreshCurrentLine();
            }
        }
    }

    private void refreshCurrentLine() {
    	currentLine.refreshCurrentLine(console, currentPrompt, out);
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
                            Class cmdClass = getCommandClass(cmd);
                            Help.Info info = Help.getInfo(cmdClass);

                            // perform completion
                            result = cmd + " " + info.complete(cl); // prepend
                            // command
                            // name and
                            // space
                            // again
                        } catch (ClassNotFoundException ex) {
                            throw new CompletionException(
                                    "Command class not found");
                        } catch (HelpException ex) {
                            ex.printStackTrace();
                            throw new CompletionException(
                                    "Command class not found");
                        }
            }
            if (result == null) // assume this is the alias to be called
                    result = defaultArg.complete(cmd);

            if (!partial.equals(result) && !completion.hasItems())
            { 
            	// performed direct
                // completion without listing
            	completion.setCompleted(result);
				completion.setNewPrompt(false);            	
            }
        } catch (CompletionException ex) {
            out.println(); // next line
            err.println(ex.getMessage()); // print the error (optional)
            // this debug output is to trace where the Exception came from
            //ex.printStackTrace(err);
            // restore old value and need a new prompt 
            completion.setCompleted(partial);
            completion.setNewPrompt(true);
        }

		if(completion.hasItems())
		{
			out.println();
			String[] list = completion.getItems();
			for(int i = 0 ; i < list.length ; i++)
				out.println(list[i]);
		}
		
		if(completion.needNewPrompt())
		{			
	        out.print(currentPrompt);
	        currentLine.start(console);				
		}
		
        return completion;
    }

    public void list(String[] items) {
    	completion.setItems(items);
    }

    public void addCommandToHistory(String cmdLineStr) {
        //  Add this command to the history.
        if (!cmdLineStr.equals(newestLine)) history.addCommand(cmdLineStr);
    }

    public PrintStream getErrorStream() {
        return err;
    }
}

/**
 * A class that handles the content of the current command line in the shell.
 * That can be :
 * - a new command that the user is beeing editing
 * - an existing command (from the command history)
 * 
 * This class also handles the current cursor position in the command line and
 * keep trace of the position (consoleX, consoleY) of the first character of the 
 * command line (to handle commands that are multilines).  
 * 
 * @author Fabien DUMINY
 */
class Line
{
	private int consoleX;
	private int consoleY;
	
    /**
     * Contains the current position of the cursor on the currentLine
     */
    private int posOnCurrentLine = 0;    

    /** Contains the current line * */
    private StringBuffer currentLine = new StringBuffer(80);
    
    private boolean shortened = true;
    private int oldLength = 0;
    private int maxLength = 0;    
    
    public void start(ScrollableShellConsole console)
    {
    	consoleX = console.getCursorX();
    	consoleY = console.getCursorY();
    	setContent("");
    }
    
    public String getContent()
    {
    	return currentLine.toString();
    }
    
    public void setContent(String content)
    {
    	startModif();
    	currentLine.setLength(0);
    	currentLine.append(content);
    	moveEnd();
    	endModif();
    }
    
    public boolean moveLeft()
    {
        if (posOnCurrentLine > 0) {
            posOnCurrentLine--;
            return true;
        }    	
        return false;
    }
    
    public boolean moveRight()
    {
        if (posOnCurrentLine < currentLine.length()) {
            posOnCurrentLine++;
            return true;
        }
        return false;
    }

    public void moveEnd()
    {
        posOnCurrentLine = currentLine.length();
    }
    
    public void moveBegin()
    {
        posOnCurrentLine = 0;
    }
    
    public boolean backspace()
    {
        if (posOnCurrentLine > 0) {
        	moveLeft();
        	delete();
            return true;
        }    	
        return false;
    }
    
    public void delete()
    {
        if ((posOnCurrentLine >= 0) && (posOnCurrentLine < currentLine.length())) 
        {
        	startModif();
            currentLine.deleteCharAt(posOnCurrentLine);
            endModif();
        }    	
    }
    
    public boolean complete(ScrollableShellConsole console, CommandShell shell)
    {
    	CompletionInfo info;
		boolean completed = false;
        if (posOnCurrentLine != currentLine.length()) {
            String ending = currentLine.substring(posOnCurrentLine);
            info = shell.complete(currentLine.substring(0,
                    posOnCurrentLine)); 
            if(info.getCompleted() != null)
            {
				setContent(info.getCompleted() + ending);
	            posOnCurrentLine = currentLine.length() - ending.length();
				completed = true;	            
	        }
        } else {
        	info = shell.complete(currentLine.toString());
        	if(info.getCompleted() != null)
        	{ 
				setContent(info.getCompleted());
	            posOnCurrentLine = currentLine.length();
				completed = true;	            
			}
        }
            	
		return completed;  
	}
    
    public void appendChar(char c)
    {
    	startModif();
        if (posOnCurrentLine == currentLine.length()) {
            currentLine.append(c);
        } else {
            currentLine.insert(posOnCurrentLine, c);
        }
        posOnCurrentLine++;    	
    	endModif();
    }

    protected void startModif()
    {
    	shortened = false;
    	oldLength = currentLine.length();
    }

    protected void endModif()
    {
    	maxLength = Math.max(oldLength, currentLine.length());
    	shortened = oldLength > currentLine.length();
    	oldLength = 0;
    }

    public void refreshCurrentLine(ScrollableShellConsole console, 
    		String currentPrompt, PrintStream out) {
        try {
        	int x = consoleX;
        	int width = console.getWidth();
        	int nbLines = ((x + maxLength) / width);
        	
        	if(((x + maxLength) % width) != 0)
        		nbLines++;
        	
        	// if the line has not been shortened (delete, backspace...)
        	if(!shortened)
            	// scroll up the buffer if necessary, and get the new y
        		consoleY = console.ensureFreeLines(consoleY, nbLines, 0);
        		
        	for(int i = 0 ; i < nbLines ; i++)
        		console.clearLine(consoleY + i);
        	
        	// print the prompt and the command line
        	console.setCursor(0, consoleY);
        	out.print(currentPrompt + currentLine);
        	
        	int posCurX = x + posOnCurrentLine;
        	int posCurY = consoleY;
        	if(posCurX >= width)
        	{        		
        		posCurY += posCurX / width;        		
        		posCurX = (posCurX % width); 
        	}
        	console.setCursor(posCurX, posCurY);
        } catch (Exception e) {
        }        
    }
}

class CompletionInfo
{
	private String[] items = null;  
	private String completed = null;
	private boolean newPrompt = false;

	/**
	 * @return Returns the completed.
	 */
	public String getCompleted() {
		return completed;
	}
	/**
	 * @param completed The completed to set.
	 */
	public void setCompleted(String completed) {
		this.items = null;
		this.completed = completed;
	}
	/**
	 * get the possible completions
	 * @return Returns the items.
	 */
	public String[] getItems() {
		return items;
	}
	/**
     * Specify the possible completions
	 * @param items The items to set.
	 */
	public void setItems(String[] items) {
		this.items = items;
		this.completed = null;
		this.newPrompt = true;
	}
	/**
	 * Do we have more than one possible completion ?
     * @return
	 */
	public boolean hasItems()
	{
		return items != null;
	}

	/**
     * Specify if we need a new prompt or not 
	 * @param newPrompt
     */
	public void setNewPrompt(boolean newPrompt)
	{
		this.newPrompt = newPrompt;
	}

	/**
     * @return true if we need to display a new prompt
     */	
	public boolean needNewPrompt()
	{
		return newPrompt;
	}
}