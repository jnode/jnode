package org.jnode.driver.console.textscreen;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import javax.naming.NameNotFoundException;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceListener;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.console.CompletionInfo;
import org.jnode.driver.console.InputCompleter;
import org.jnode.driver.console.TextConsole;
import org.jnode.driver.input.KeyboardAPI;
import org.jnode.driver.input.KeyboardEvent;
import org.jnode.driver.input.KeyboardListener;
import org.jnode.naming.InitialNaming;
import org.jnode.system.BootLog;
import org.jnode.system.event.FocusEvent;
import org.jnode.system.event.FocusListener;
import org.jnode.util.Queue;


/**
 * KeyInputStream maps keyboard events into a stream of characters.  Current functionality includes:
 * <ul>
 * <li>line buffering and line editing, using a text console,
 * <li>integrated input history and completion,
 * <li>CTRL-D is interpretted as a 'soft' EOF mark,KeyboardInputStream
 * <li>listens to keyboard focus events.
 * </ul>
 * 
 * Future enhancements include:
 * <ul>
 * <li>a "raw" mode in which characters and other keyboard events are delivered without line editing,
 * <li>a "no echo" mode in which line editting occurs without echoing of input characters,
 * <li>making the active characters and keycodes "soft",
 * <li>making completion and history context sensitive; e.g. when switching between a shell and 
 * an application, and
 * <li>code refactoring to support classical terminal devices and remote consoles.
 * </ul>
 * 
 * Bugs:
 * <ul>
 * <li>The current method of echoing the input is suboptimal, and is broken in the case where an 
 * application outputs a prompt string to stdout or stderr.
 * </ul>
 * 
 * @author crawley@jnode.org
 *
 */
public class KeyboardInputStream extends InputStream 
implements KeyboardListener, FocusListener, DeviceListener {

	public static final byte CTRL_L = 12;
	public static final byte CTRL_D = 4;
	
	private boolean eof;
	
	private byte[] buffer;
	private int pos;
	private int lim;
	
	private static final char NO_CHAR = 0;

	private KeyboardAPI api;
	private DeviceManager devMan;
	
	/** The queue of keyboard events */
	private final Queue<KeyboardEvent> queue = new Queue<KeyboardEvent>();
	
	private final Line currentLine;
	private final TextConsole console;
	private InputCompleter completer;
	private final PrintStream out;
	private boolean hasFocus;
	
	private String currentPrompt;
	
	/**
     * Contains an index to the current history line, counting from zero.  The value
     * -1 denotes the current line.
     */
    private int historyIndex = -1;

    /**
     * Contains the current line; i.e. the text being entered by the user.
     */
    private String savedCurrentLine;

    public KeyboardInputStream(KeyboardAPI api, TextConsole console) {
		if (api != null) {
			this.api = api;
			this.api.addKeyboardListener(this);
		}
		else {
			try {
	            this.devMan = InitialNaming.lookup(DeviceManager.NAME);
	            this.devMan.addListener(this);
	        } 
			catch (NameNotFoundException ex) {
				BootLog.error("DeviceManager not found", ex);
	        }
		}
		this.console = console;
		this.out = console.getOut();
		this.currentLine = new Line(console);
		this.pos = this.lim = 0;
	}
	
	private void registerKeyboardApi(Device device) {
		if (this.api == null) {
			try {
				this.api = device.getAPI(KeyboardAPI.class);
				this.api.addKeyboardListener(this);
			}
			catch (ApiNotFoundException ex) {
				BootLog.error("KeyboardAPI not found", ex);
			}
			this.devMan.removeListener(this);
		}
	}
	
	public boolean isSoftEOF() {
		return eof;
	}

	public void clearSoftEOF() {
		eof = false;
	}

	@Override
	public int available() throws IOException {
		if (eof) {
			return 0; /* per the JDK 1.6 API docs */
		}
		else {
			// FIXME - what about the case where the line buffer is empty
			// and there are unconsumed input events in the queue?
			return currentLine.getLineLength();
		}
	}
	
	/**
	 * @see org.jnode.driver.input.KeyboardListener#keyPressed(org.jnode.driver.input.KeyboardEvent)
	 */
	public void keyPressed(KeyboardEvent event) {
		if (hasFocus) {
			queue.add(event);
		}
	}

	/**
	 * @see org.jnode.driver.input.KeyboardListener#keyReleased(org.jnode.driver.input.KeyboardEvent)
	 */
	public void keyReleased(KeyboardEvent event) {
	}

	/**
	 * @see java.io.InputStream#close()
	 */
	public void close() throws IOException {
		if (api != null) {
			api.removeKeyboardListener(this);
		}
		super.close();
	}

    /**
	 * Pull a keyboard event from the queue and process it.
     * @return true if the event was processed
	 */
	private boolean processEvent() {
		KeyboardEvent event = queue.get();
		if (!event.isConsumed()) {
			char ch = event.getKeyChar();
			if (ch != NO_CHAR) {
				event.consume();
				return !processChar(ch);
			} else {
				int kc = event.getKeyCode();
				int mods = event.getModifiers();
				if (processVirtualKeystroke(kc, mods)) {
					event.consume();
				}
				return true;
			}
		} else {
		    return true;
		}
	}
	
	/**
	 * Process a keystroke interpretted as a character.
	 * 
	 * @param ch the character to process
	 * @return <code>true</code> if the character should cause the current line 
	 * buffer contents to be returned to the user.
	 */
	private boolean processChar(char ch) {
		boolean breakChar = false;
        switch (ch) {
        // if its a backspace we want to remove one from the end of our current
        // line
        case KeyEvent.VK_BACK_SPACE:
            if (currentLine.backspace()) {
                refreshCurrentLine();
            }
            break;
        // if its an enter key we want to process the command, and then resume
        // the thread
        case '\n':
            currentLine.moveEnd();
            refreshCurrentLine();
            out.println();
            currentLine.appendChar(ch);
            breakChar = true;
            historyIndex = -1;
            break;
        // if it's the tab key, we want to trigger command line completion
        case '\t':
        	if (completer != null) {
        		if (currentLine.complete()) {
        			currentLine.start(true);
        		}
        		out.print(currentPrompt);
        		refreshCurrentLine();
        	}
            break;
        // interpret ^D as a soft EOF
        // FIXME - behavior correct?  cf bash's treatment of ^D
        case CTRL_D:
        	currentLine.moveEnd();
            refreshCurrentLine();
            out.println();
            eof = true;
            breakChar = true;
            break;
        // ^L means kill current line and redraw screen.
        // FIXME - is this behavior useful?  
        case CTRL_L:
        	this.console.clear();
        	this.console.setCursor(0, 0);
        	out.print(currentPrompt);
        	currentLine.start();
        	refreshCurrentLine();
        	break;
        default: 
        	// otherwise add it to our current line
        	currentLine.appendChar(ch);
            refreshCurrentLine();
            historyIndex = -1;                            
        }
        return breakChar;
    }
	
	/**
	 * Process a keystroke that doesn't have an associated char value.
	 * @param code key code
	 * @param modifiers key modifiers
	 * @return <code>true</code> if the keystroke has been recognized and 
	 * acted on, <code>false</code> otherwise.
	 */
	private boolean processVirtualKeystroke(int code, int modifiers) {
		if (modifiers != 0) {
			return false;
		}
		switch (code) {
        case KeyEvent.VK_UP:
        	// Previous history item
        	if (completer != null) {
        		if (historyIndex == -1) {
        			historyIndex = completer.getInputHistory().size();
                    savedCurrentLine = currentLine.getContent();
                }
        		historyIndex--;

                updateCurrentLine();
            }
            break;
        case KeyEvent.VK_DOWN:
        	// Next history item
        	if (completer != null) {
                if (historyIndex == -1)
                    savedCurrentLine = currentLine.getContent();

                if (historyIndex == completer.getInputHistory().size() - 1)
        			historyIndex = -2;

                historyIndex++;

                updateCurrentLine();

            }
            break;
        case KeyEvent.VK_LEFT:
            // Left the cursor goes left
            if (currentLine.moveLeft()) {
                refreshCurrentLine();
            }
            break;
        case KeyEvent.VK_RIGHT:
            // Right the cursor goes right
            if (currentLine.moveRight()) {
                refreshCurrentLine();
            }
            break;
        case KeyEvent.VK_HOME:
            // The cursor goes at the start
            currentLine.moveBegin();
            refreshCurrentLine();
            break;
        case KeyEvent.VK_END:
            // the cursor goes at the end of line
            currentLine.moveEnd();
            refreshCurrentLine();
            break;
        // if its a delete we want to remove one under the cursor
        case KeyEvent.VK_DELETE:
            currentLine.delete();
            refreshCurrentLine();
            break;
        default: 
        	// ignore other virtual keys.
        	return false;
        }
        return true;
    }

    private void updateCurrentLine() {
        if (historyIndex > -1) {
            currentLine.setContent(completer.getInputHistory().getLineAt(historyIndex));
        } else {
            currentLine.setContent(savedCurrentLine);
        }
        refreshCurrentLine();
        currentLine.moveEnd();
    }

    private void refreshCurrentLine() {
    	currentLine.refreshCurrentLine();
    }

    private boolean fillBuffer() throws IOException {
    	int x = console.getCursorX();
    	int y = console.getCursorY();
    	StringBuffer sb = new StringBuffer(x);
    	for (int i = 0; i < x; i++) {
    		sb.append(console.getChar(i, y));
    	}
    	currentPrompt = sb.toString();
    		
    	currentLine.start();
    	while (processEvent()) { /* */ }
    	buffer = currentLine.consumeBytes();
    	lim = buffer.length;
    	pos = 0;
    	return pos < lim;
    }

	@Override
	public int read() throws IOException {
		if (pos >= lim) {
			if (eof || !fillBuffer()) {
				return -1;
			}
		}
		return buffer[pos++];
	}

	@Override
	public int read(byte[] buff, int off, int len) throws IOException {
    	int nosRead = 0;
        if (pos >= lim) {
        	if (eof || !fillBuffer()) {
        		return -1;
        	}
        }
        while (nosRead < len && pos < lim) {
        	buff[nosRead + off] = buffer[pos];
        	nosRead++; 
        	pos++;
        }
        return nosRead;
	}

	@Override
	public int read(byte[] buff) throws IOException {
		return read(buff, 0, buff.length);
	}

	@Override
	public void mark(int readLimit) {
	}

	@Override
	public boolean markSupported() {
		return false;
	}

	@Override
	public void reset() throws IOException {
		throw new UnsupportedOperationException("Mark/reset not supported");
	}

	@Override
	public long skip(long n) throws IOException {
		// I don't expect this method will be used much.
		for (long i = 0; i < n; i++) {
			if (read() == -1) {
				return i;
			}
		}
		return n;
	}

	public InputCompleter getCompleter() {
		return completer;
	}

	public void setCompleter(InputCompleter completer) {
		this.completer = completer;
	}

	public void focusGained(FocusEvent event) {
		hasFocus = true;
	}

	public void focusLost(FocusEvent event) {
		hasFocus = false;
	}
	
	/**
	 * @see org.jnode.driver.DeviceListener#deviceStarted(org.jnode.driver.Device)
	 */
	public void deviceStarted(Device device) {
		if (device.implementsAPI(KeyboardAPI.class)) {
			registerKeyboardApi(device);
		}
	}

	/**
	 * @see org.jnode.driver.DeviceListener#deviceStop(org.jnode.driver.Device)
	 */
	public void deviceStop(Device device) {
		/* Do nothing */
	}
}
