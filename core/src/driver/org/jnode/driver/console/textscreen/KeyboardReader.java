package org.jnode.driver.console.textscreen;

import static org.jnode.driver.console.textscreen.ConsoleKeyEventBindings.KR_COMPLETE;
import static org.jnode.driver.console.textscreen.ConsoleKeyEventBindings.KR_CONSUME;
import static org.jnode.driver.console.textscreen.ConsoleKeyEventBindings.KR_CURSOR_LEFT;
import static org.jnode.driver.console.textscreen.ConsoleKeyEventBindings.KR_CURSOR_RIGHT;
import static org.jnode.driver.console.textscreen.ConsoleKeyEventBindings.KR_CURSOR_TO_END;
import static org.jnode.driver.console.textscreen.ConsoleKeyEventBindings.KR_CURSOR_TO_START;
import static org.jnode.driver.console.textscreen.ConsoleKeyEventBindings.KR_DELETE_AFTER;
import static org.jnode.driver.console.textscreen.ConsoleKeyEventBindings.KR_DELETE_BEFORE;
import static org.jnode.driver.console.textscreen.ConsoleKeyEventBindings.KR_ENTER;
import static org.jnode.driver.console.textscreen.ConsoleKeyEventBindings.KR_HISTORY_UP;
import static org.jnode.driver.console.textscreen.ConsoleKeyEventBindings.KR_IGNORE;
import static org.jnode.driver.console.textscreen.ConsoleKeyEventBindings.KR_INSERT;
import static org.jnode.driver.console.textscreen.ConsoleKeyEventBindings.KR_KILL_LINE;
import static org.jnode.driver.console.textscreen.ConsoleKeyEventBindings.KR_SOFT_EOF;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.jnode.driver.console.InputCompleter;
import org.jnode.driver.console.KeyEventBindings;
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
 * <li>binding of keyboard events to various actions is soft,
 * <li>listens to keyboard focus events.
 * </ul>
 * <p/>
 * Possible future enhancements include:
 * <ul>
 * <li>a "raw" mode in which characters and other keyboard events are delivered without line editing,
 * <li>a "no echo" mode in which line editing occurs without echoing of input characters,
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
    
    /**
     * This KR code causes the next history line to be selected.
     */
    public static final byte KR_HISTORY_DOWN = 16;

    public static final byte CTRL_L = 12;
    public static final byte CTRL_D = 4;

    private boolean eof;

    private char[] buffer;
    private int pos;
    private int lim;

    private static final char NO_CHAR = KeyEvent.CHAR_UNDEFINED;

    private final Line currentLine;
    private final TextConsole console;
    private InputCompleter completer;
    private final Writer out;
    
    private KeyEventBindings bindings = ConsoleKeyEventBindings.createDefault();

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
    
    /**
     * Get a snapshot of the reader's key event bindings.
     * 
     * @return a copy of the current bindings.
     */
    public KeyEventBindings getKeyEventBindings() {
        return new KeyEventBindings(bindings);
    }
    
    /**
     * Set the reader's key event bindings.
     * 
     * @param bindings the new bindings.
     */
    public void setKeyEventBindings(KeyEventBindings bindings) {
        this.bindings = new KeyEventBindings(bindings);
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
     * @return true if the event should commit the characters in the
     *    input (line editing) buffer to the Reader's character stream.
     */
    private boolean processEvent() throws IOException {
        KeyboardEvent event = keyboardHandler.getEvent();
        if (!event.isConsumed()) {
            int action = bindings.getKeyboardEventAction(event);
            boolean breakChar = false;
            boolean consume = true;
            switch (action) {
                case KR_DELETE_BEFORE:
                    // Delete character before cursor
                    if (currentLine.backspace()) {
                        refreshCurrentLine();
                    }
                    break;
                case KR_ENTER:
                    // Append event character to the line and commit.
                    currentLine.moveEnd();
                    refreshCurrentLine();
                    out.write('\n');
                    currentLine.appendChar(event.getKeyChar());
                    breakChar = true;
                    historyIndex = -1;
                    break;
                case KR_COMPLETE:
                    // Perform completion
                    if (completer != null) {
                        if (currentLine.complete()) {
                            currentLine.start(true);
                        }
                        out.write(currentPrompt);
                        refreshCurrentLine();
                    }
                    break;
                case KR_SOFT_EOF:
                    // Set soft EOF status and commit
                    currentLine.moveEnd();
                    refreshCurrentLine();
                    out.write('\n');
                    eof = true;
                    breakChar = true;
                    break;
                case KR_KILL_LINE:
                    // Kill the current input line (and clear the screen)
                    this.console.clear();
                    this.console.setCursor(0, 0);
                    out.write(currentPrompt);
                    currentLine.start();
                    refreshCurrentLine();
                    break;
                case KR_INSERT:
                    // Insert event's character
                    currentLine.appendChar(event.getKeyChar());
                    refreshCurrentLine();
                    historyIndex = -1;
                    break;
                case KR_HISTORY_UP:
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
                case KR_HISTORY_DOWN:
                    // Next history item
                    if (completer != null) {
                        if (historyIndex == -1) {
                            savedCurrentLine = currentLine.getContent();
                        }
                        if (historyIndex == completer.getInputHistory().size() - 1) {
                            historyIndex = -2;
                        }
                        historyIndex++;
                        updateCurrentLine();
                    }
                    break;
                case KR_CURSOR_LEFT:
                    // Move the cursor left
                    if (currentLine.moveLeft()) {
                        refreshCurrentLine();
                    }
                    break;
                case KR_CURSOR_RIGHT:
                    // Move the cursor right
                    if (currentLine.moveRight()) {
                        refreshCurrentLine();
                    }
                    break;
                case KR_CURSOR_TO_START:
                    // Move the cursor to the start of the line
                    currentLine.moveBegin();
                    refreshCurrentLine();
                    break;
                case KR_CURSOR_TO_END:
                    // Move the cursor to the end of the line
                    currentLine.moveEnd();
                    refreshCurrentLine();
                    break;
                case KR_DELETE_AFTER:
                    // Delete the character after the cursor
                    currentLine.delete();
                    refreshCurrentLine();
                    break;
                case KR_CONSUME:
                    // Comsume (and ignore) the event
                    break;
                case KR_IGNORE:
                    // Leave the event unconsumed.
                    consume = false;
                    break;
            }
            if (consume) {
                event.consume();
            }
            return breakChar;
        } else {
            return false;
        }
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
        while (!processEvent()) { /* */
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
