package org.jnode.driver.console.textscreen;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.jnode.driver.console.InputCompleter;
import org.jnode.driver.console.TextConsole;
import org.jnode.driver.input.KeyboardEvent;
import org.jnode.system.event.FocusEvent;
import org.jnode.system.event.FocusListener;
import org.jnode.util.ConsoleStream;


/**
 * KeyboardInputStream maps keyboard events into a stream of characters.  Current functionality includes:
 * <ul>
 * <li>line buffering and line editing, using a text console,
 * <li>integrated input history and completion,
 * <li>CTRL-D is interpreted as a 'soft' EOF mark,
 * <li>listens to keyboard focus events.
 * </ul>
 * <p/>
 * Possible future enhancements include:
 * <ul>
 * <li>a "raw" mode in which characters and other keyboard events are delivered without line editing,
 * <li>a "no echo" mode in which line editing occurs without echoing of input characters,
 * <li>making the active characters and keycodes "soft",
 * <li>making completion and history context sensitive; e.g. when switching between a shell and 
 * an application, and
 * <li>code refactoring to support classical terminal devices and remote consoles.
 * </ul>
 * <p/>
 * Bugs:
 * <ul>
 * <li>The current method of echoing the input is suboptimal, and is broken in the case where an 
 * application outputs a prompt string to stdout or stderr.
 * </ul>
 * 
 * @author crawley@jnode.org
 */
public class KeyboardReader extends Reader 
    implements FocusListener, ConsoleStream {

    public static final byte CTRL_L = 12;
    public static final byte CTRL_D = 4;

    private boolean eof;

    private char[] buffer;
    private int pos;
    private int lim;

    private static final char NO_CHAR = 0;

    private final Line currentLine;
    private final TextConsole console;
    private InputCompleter completer;
    private final Writer out;

    private String currentPrompt;

    /**
     * Contains an index to the current history line, counting from zero. The
     * value -1 denotes the current line.
     */
    private int historyIndex = -1;

    /**
     * Contains the current line; i.e. the text being entered by the user.
     */
    private String savedCurrentLine;

    private final KeyboardHandler keyboardHandler;
    private final FocusListener focusListener;

    public KeyboardReader(KeyboardHandler kbHandler, TextConsole console) {
        this.keyboardHandler = kbHandler;
        this.console = console;
        this.out = console.getOut();
        this.currentLine = new Line(console);
        this.pos = this.lim = 0;

        if (keyboardHandler instanceof FocusListener) {
            this.focusListener = (FocusListener) keyboardHandler;
        } else {
            this.focusListener = null;
        }
    }

    /**
     * When we process a 'soft EOF' character; e.g. CTRL-D we go into a state where 
     * all subsequent 'read' calls will return <code>-1</code> until the {@link #clearSoftEOF()}
     * method is called.  This method tests if we are in that state.
     * 
     * @return Return <code>true</code> if we are in 'soft EOF' state; otherwise <code>false</code>.
     */
    public boolean isSoftEOF() {
        return eof;
    }

    /**
     * Clear the 'soft EOF' state; see {@link #isSoftEOF()}.
     */
    public void clearSoftEOF() {
        eof = false;
    }

    @Override
    public boolean ready() throws IOException {
        return eof || currentLine.getLineLength() > 0;
    }

    /**
     * @see java.io.Reader#close()
     */
    public void close() throws IOException {
        keyboardHandler.close();
    }

    /**
     * Pull a keyboard event from the queue and process it.
     * 
     * @return true if the event was processed
     */
    private boolean processEvent() throws IOException {
        KeyboardEvent event = keyboardHandler.getEvent();
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
     * Process a keystroke interpreted as a character.
     * 
     * @param ch the character to process
     * @return <code>true</code> if the character should cause the current
     *         line buffer contents to be returned to the user.
     */
    private boolean processChar(char ch) throws IOException {
        boolean breakChar = false;
        switch (ch) {
            // if its a backspace we want to remove one from the end of our
            // current
            // line
            case KeyEvent.VK_BACK_SPACE:
                if (currentLine.backspace()) {
                    refreshCurrentLine();
                }
                break;
            // if its an enter key we want to process the command, and then
            // resume
            // the thread
            case '\n':
                currentLine.moveEnd();
                refreshCurrentLine();
                out.write('\n');
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
                    out.write(currentPrompt);
                    refreshCurrentLine();
                }
                break;
            // interpret ^D as a soft EOF
            // FIXME - behavior correct? cf bash's treatment of ^D
            case CTRL_D:
                currentLine.moveEnd();
                refreshCurrentLine();
                out.write('\n');
                eof = true;
                breakChar = true;
                break;
            // ^L means kill current line and redraw screen.
            // FIXME - is this behavior useful?
            case CTRL_L:
                this.console.clear();
                this.console.setCursor(0, 0);
                out.write(currentPrompt);
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
     * 
     * @param code key code
     * @param modifiers key modifiers
     * @return <code>true</code> if the keystroke has been recognized and
     *         acted on, <code>false</code> otherwise.
     * @throws IOException 
     */
    private boolean processVirtualKeystroke(int code, int modifiers) throws IOException {
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

    private void updateCurrentLine() throws IOException {
        if (historyIndex > -1) {
            currentLine.setContent(completer.getInputHistory().getLineAt(historyIndex));
        } else {
            currentLine.setContent(savedCurrentLine);
        }
        refreshCurrentLine();
        currentLine.moveEnd();
    }

    private void refreshCurrentLine() throws IOException {
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
        while (processEvent()) { /* */
        }
        buffer = currentLine.consumeChars();
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
    public int read(char[] buff, int off, int len) throws IOException {
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
    public int read(char[] buff) throws IOException {
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
    
    /**
     * Get the TextConsole associated with this KeyboardReader.
     * 
     * @return the associated TextConsole object.
     */
    public TextConsole getTextConsole() {
        return console;
    }

    /**
     * Get the InputCompleter associated with this KeyboardReader.  This
     * is typically the CommandShell.
     * 
     * @return the associated InputCompleter object.
     */
    public InputCompleter getCompleter() {
        return completer;
    }

    /**
     * Set this KeyboardReader's InputCompleter.
     * 
     * @param completer the new completer value
     */
    public void setCompleter(InputCompleter completer) {
        this.completer = completer;
    }

    public void focusGained(FocusEvent event) {
        if (focusListener != null) {
            focusListener.focusGained(event);
        }
    }

    public void focusLost(FocusEvent event) {
        if (focusListener != null) {
            focusListener.focusLost(event);
        }
    }
}
