/**
 * $Id$
 */
package org.jnode.shell;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.PrintStream;
import java.io.StringReader;
import java.text.DateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import javax.naming.NameNotFoundException;
import org.apache.log4j.Logger;
import org.jnode.driver.console.Console;
import org.jnode.driver.console.ConsoleManager;
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

/**
 * @author epr
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
	 * Contains an index to the current history line. 0 = first historical command. 2 = next historical command. -1 = the current command line.
	 */
	private int historyIndex = -1;
	
	/**
	 * Contains the current position of the cursor on the currentLine
	 */
	private int posOnCurrentLine = 0;
	
	/** Contains the current line * */
	private String currentLine = "";
	
	/** Contains the newest command being typed in * */
	private String newestLine = "";
	
	/** Flag to know if the shell is active to take the keystrokes * */
	private boolean isActive = false;
	
	private String currentPrompt = null;
	
	/**
	 * Flag to know when to wait (while input is happening). This is (hopefully) a thread safe implementation. *
	 */
	private volatile boolean threadSuspended = false;
	
	private static String DEFAULT_PROMPT = "JNode $P$G";
	
	//private static final Class[] MAIN_ARG_TYPES = new Class[] { String[].class };
	
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
	public CommandShell() throws NameNotFoundException, ShellException 
	{
		this((ScrollableShellConsole) ((ConsoleManager) InitialNaming.lookup(ConsoleManager.NAME)).getFocus());
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
			this.commandInvoker = threadCommandInvoker; //default to separate threads for commands.
			aliasMgr = ((AliasManager) InitialNaming.lookup(AliasManager.NAME)).createAliasManager();
			System.getProperties().setProperty(PROMPT_PROPERTY_NAME, DEFAULT_PROMPT);
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
		final String cmdLine = System.getProperty("jnode.cmdline", "");
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
					//  wait until enter is hit
					threadSuspended = true;
					while (threadSuspended)
						wait();
					if (currentLine.length() > 0)
						processCommand(currentLine.trim());
					if (currentLine.trim().equals("halt"))
						halt = true;
					currentLine = "";
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
	//				main.invoke(null, new Object[] { cmdLine.getRemainder().toStringArray()});
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
			final ClassLoader cl = Thread.currentThread().getContextClassLoader();
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
						case 'P' :
							result.append(new File(System.getProperty("user.dir")));
							break;
						case 'G' :
							result.append("> ");
							break;
						case 'D' :
							final Date now = new Date();
							DateFormat.getDateTimeInstance().format(now, result, null);
							break;
						default :
							result.append(c);
					}
					commandMode = false;
				} else {
					switch (c) {
						case '$' :
							commandMode = true;
							break;
						default :
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
	 * @param    ke                  a  KeyboardEvent
	 *
	 * @version  2/5/2004
	 */
	public void keyPressed(KeyboardEvent ke) {
		//  make sure we are ready to intercept the keyboard
		if (!isActive)
			return;
		
		switch(ke.getKeyCode()) {
			//  intercept the up and down arrow keys
			case KeyEvent.VK_UP:
				ke.consume();
				if (historyIndex == -1) {
					newestLine = currentLine;
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
					newestLine = currentLine;
				historyIndex++;
				redisplay();
				break;
			case KeyEvent.VK_LEFT: //Left the cursor goes left
				ke.consume();
				if(posOnCurrentLine > 0) {
					posOnCurrentLine--;
					refreshCurrentLine();
				}
				break;
			case KeyEvent.VK_RIGHT: // Right the cursor goes right
				ke.consume();
				if(posOnCurrentLine < currentLine.length()) {
					posOnCurrentLine++;
					refreshCurrentLine();
				}
				break;
			case KeyEvent.VK_HOME: // The cursor goes at the start
				ke.consume();
				posOnCurrentLine = 0;
				refreshCurrentLine();
				break;
			case KeyEvent.VK_END: // the cursor goes at the end of line
				ke.consume();
				posOnCurrentLine = currentLine.length();
				refreshCurrentLine();
				break;
				//  if its a backspace we want to remove one from the end of our current line
			case KeyEvent.VK_BACK_SPACE:
				ke.consume();
				if (currentLine.length() != 0 && posOnCurrentLine > 0) {
					posOnCurrentLine --;
					currentLine = currentLine.substring(0, posOnCurrentLine)+ currentLine.substring(posOnCurrentLine+1, currentLine.length());
					
					refreshCurrentLine();
				}
				break;
				
				//  if its a delete we want to remove one under the cursor
			case KeyEvent.VK_DELETE:
				ke.consume();
				if(posOnCurrentLine == 0 && currentLine.length() > 0) {
					currentLine = currentLine.substring(1);
				} else if(posOnCurrentLine < currentLine.length()) {
					currentLine = currentLine.substring(0, posOnCurrentLine)+ currentLine.substring(posOnCurrentLine+1, currentLine.length());
				}
				refreshCurrentLine();
				break;
				
				//  if its an enter key we want to process the command, and then resume the thread
			case KeyEvent.VK_ENTER:
				out.print(ke.getKeyChar());
				ke.consume();
				posOnCurrentLine = 0;
				synchronized (this) {
					isActive = false;
					threadSuspended = false;
					notifyAll();
				}
				break;
				
				// if it's the tab key, we want to trigger command line completion
			case KeyEvent.VK_TAB:
				ke.consume();
				if(posOnCurrentLine != currentLine.length()) {
					String ending = currentLine.substring(posOnCurrentLine);
					currentLine = complete(currentLine.substring(0, posOnCurrentLine))+ending;
					posOnCurrentLine = currentLine.length() - ending.length();
				} else {
					currentLine = complete(currentLine);
					posOnCurrentLine = currentLine.length();
				}
				break;
				
			default:
				//  if its a useful key we want to add it to our current line
				if (!Character.isISOControl(ke.getKeyChar())) {
					ke.consume();
					if(posOnCurrentLine == currentLine.length()) {
						currentLine += ke.getKeyChar();
					} else {
						currentLine = currentLine.substring(0, posOnCurrentLine) +
							ke.getKeyChar() + currentLine.substring(posOnCurrentLine);
					}
					posOnCurrentLine++;
					refreshCurrentLine();
				}
		}
	}
	
	private void refreshCurrentLine() {
		try {
			int y = console.getCursorY();
			console.clearLine(y);
			/* Uncomment this to display cursor position
			console.clearLine(y-25);
			console.setCursor(0, y-1);
			err.print("Cursor pos : x = "+currentPrompt.length() + posOnCurrentLine+", y = "+y);
			console.setCursor(0, y);
			 */
			out.print(currentPrompt + currentLine + " ");
			console.setCursor(currentPrompt.length() + posOnCurrentLine, console.getCursorY());
		}
		catch (Exception e) {
		}
	}
	
	public void keyReleased(KeyboardEvent ke) {
		// do nothing
	}
	
	private void redisplay() {
		//  clear the line
		if (console != null) {
			console.clearLine(console.getCursorY());
		}
		//  display the prompt
		out.print(currentPrompt);
		//  display the required history/current line
		if (historyIndex == -1)
			currentLine = newestLine;
		else
			currentLine = history.getCommand(historyIndex);
		out.print(currentLine);
		posOnCurrentLine = currentLine.length();
	}
	
	// Command line completion
	
	private final Argument defaultArg = new AliasArgument("command", "the command to be called");
	private boolean dirty = false;
	private String complete(String partial) {
		// workaround to set the currentShell to this shell
		try {
			ShellUtils.getShellManager().registerShell(this);
		} catch (NameNotFoundException ex) {
		}
		
		dirty = false;
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
						result = cmd + " " + info.complete(cl); // prepend command name and space
						// again
					} catch (ClassNotFoundException ex) {
					throw new CompletionException("Command class not found");
				}
			}
			if (result == null) // assume this is the alias to be called
				result = defaultArg.complete(cmd);
			
			if (!partial.equals(result) && !dirty) { // performed direct completion without listing
				dirty = true; // indicate we want to have a new prompt
				for (int i = 0; i < partial.length() + currentPrompt.length(); i++)
					System.out.print("\b"); // clear line (cheap approach)
			}
		} catch (CompletionException ex) {
			System.out.println(); // next line
			System.err.println(ex.getMessage()); // print the error (optional)
			// this debug output is to trace where the Exception came from
			//ex.printStackTrace(System.err);
			result = partial; // restore old value
			dirty = true; // we need a new prompt
		}
		
		if (dirty) {
			dirty = false;
			System.out.print(currentPrompt + result); // print the prompt and go on with normal
			// operation
		}
		return result;
	}
	
	public void list(String[] items) {
		System.out.println();
		for (int i = 0; i < items.length; i++)
			System.out.println(items[i]);
		dirty = true;
	}
	
	public void addCommandToHistory(String cmdLineStr) {
		//  Add this command to the history.
		if (!cmdLineStr.equals(newestLine))
			history.addCommand(cmdLineStr);
	}
	
	public PrintStream getErrorStream() {
		return err;
	}
}
