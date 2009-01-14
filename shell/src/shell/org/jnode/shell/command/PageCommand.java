/*
 * $Id: CommandLine.java 4611 2008-10-07 12:55:32Z crawley $
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
package org.jnode.shell.command;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.naming.NameNotFoundException;

import org.jnode.driver.console.ConsoleManager;
import org.jnode.driver.console.TextConsole;
import org.jnode.driver.console.textscreen.TextScreenConsoleManager;
import org.jnode.driver.input.KeyboardEvent;
import org.jnode.driver.input.KeyboardListener;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.ShellManager;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;

/**
 * This command is a simple analog of the UNIX/Linux 'more' and 'less' commands.
 * 
 * @author crawley@jnode.org
 */
public class PageCommand extends AbstractCommand implements KeyboardListener {
    private static boolean DEBUG = false;
    private static int LAST_SUBLINE = Integer.MAX_VALUE;
    private static int DEFAULT_COLOR = 0x07;
    private static int MATCH_COLOR = 0x04;
    
    private final FileArgument ARG_FILE = 
        new FileArgument("file", Argument.OPTIONAL, "the file to be paged");

    private PrintWriter err;
    private TextConsole console;
    
    private int pageHeight;
    private int pageWidth;
    private int pageSize;
    private int tabSize;
    
    // This is the line number of the top (data source) line displayed on the
    // screen page.
    private int topLineNo;
    private int topSublineNo;
    
    
    // This is the line number of the bottom (data source) line displayed on the
    // screen page.
    private int bottomLineNo;
    private int bottomSublineNo;
    
    // This pipe passes characters from the system thread that calls 
    // our 'keyPressed' event method to the thread that runs the Page command.
    private PipedReader pr;
    private PipedWriter pw;

    private TextScreenConsoleManager manager;

    private LineStore lineStore;

    private Pattern regex;
    private Matcher matcher;

    private String prompt;

    private ScreenBuffer currentBuffer;
    
    
    public PageCommand() {
        super("output a file to the console one 'page' at a time");
        registerArguments(ARG_FILE);
    }

    /**
     * Classic java entry point
     */
    public static void main(String[] args) throws Exception {
        new PageCommand().execute(args);
    }

    /**
     * JNode command entry point.
     */
    @Override
    public void execute() throws Exception {
        err = getError().getPrintWriter();
        Reader r = null;
        boolean opened = false;
        try {
            if (ARG_FILE.isSet()) {
                r = new FileReader(ARG_FILE.getValue());
                opened = true;
            } else if (getInput().isTTY()) {
                // We cannot do this.  We need to use the console as the
                // source of command characters for the Page command.
                debugln("Paging piped from the console is not supported");
                exit(1);
            } else {
                r = getInput().getReader();
            }
            setup();
            lineStore = new LineStore(r);
            pager();
            
        } catch (IOException ex) {
            debugln(ex.getMessage());
            exit(1);
        } finally {
            if (r != null && opened) {
                try {
                    r.close();
                } catch (IOException ex) {
                    // ignore
                }
            }
            tearDown();
        }
    }
    
    /**
     * Set up the pager's console and command pipe.
     */
    private void setup() throws NameNotFoundException, IOException {
        ShellManager sm = InitialNaming.lookup(ShellManager.NAME);
        manager = (TextScreenConsoleManager) sm.getCurrentShell().getConsole().getManager();
        console = manager.createConsole(
            "page",
            (ConsoleManager.CreateOptions.TEXT |
                ConsoleManager.CreateOptions.STACKED |
                ConsoleManager.CreateOptions.NO_LINE_EDITTING |
                ConsoleManager.CreateOptions.NO_SYSTEM_OUT_ERR));
        manager.focus(console);
        
        pageHeight = console.getDeviceHeight() - 1;
        pageWidth = console.getDeviceWidth();
        pageSize = pageHeight * pageWidth;
        tabSize = console.getTabSize();
        
        pw = new PipedWriter();
        pr = new PipedReader();
        pr.connect(pw);
        
        console.addKeyboardListener(this);
    }
    
    /**
     * Tear down the console and pipe.
     * @throws IOException
     */
    private void tearDown() throws IOException {
        if (manager != null && console != null) {
            manager.unregisterConsole(console);
        }
        if (pw != null) {
            pw.close();
        }
        if (pr != null) {
            pr.close();
        }
    }

    /**
     * Do the paging, reading commands from our private console input
     * pipe to figure out what to do next.
     * 
     * @param r the source of data to be paged.
     * @throws IOException
     */
    private void pager() throws IOException {
        // Output first page.
        console.clear();
        bottomLineNo = -1;
        boolean exit = false;
        nextPage();
        
        // Process commands until we reach the EOF on the data source or
        // the command pipe.
        while (!exit) {
            prompt();
            int ch = pr.read();
            erasePrompt();
            switch (ch) {
                case -1:
                    exit = true;
                    break;
                case ' ':
                case 'f':
                    if (lineStore.isLastLineNo(bottomLineNo)) {
                        exit = true;
                    } else {
                        nextPage();
                    }
                    break;
                case 'b':
                    prevPage();
                    break;
                case 'k':
                case 'y':
                    prevLine();
                    break;
                case '\n':
                    if (lineStore.isLastLineNo(bottomLineNo)) {
                        exit = true;
                    } else {
                        nextLine();
                    }
                    break;
                case 'u':
                    prevScreenLine();
                    break;
                case 'd':
                    nextScreenLine();
                    break;
                case '<':
                    gotoPage(0);
                    break;
                case '>':
                    gotoLastPage();
                    break;
                case '/':
                    searchForwards();
                    break;
                case '?':
                    searchBackwards();
                    break;
                case '\004': // ^D
                case 'q':
                    exit = true;
                    break;
                case 'h':
                    help();
                default:
                    // ignore
            }
        }
    }

    private void searchBackwards() throws IOException {
        String input = readLine('?');
        int lineNo = bottomLineNo;
        if (input.length() <= 1) {
            if (regex == null) {
                setPrompt("No previous search");
                return;
            }
            lineNo--;
        } else {
            try {
                regex = Pattern.compile(input.substring(1));
            } catch (PatternSyntaxException ex) {
                setPrompt("Invalid regex");
                return;
            }
            matcher = regex.matcher("");
        }
        while (true) {
            String line = lineStore.getLine(lineNo);
            if (line == null) {
                gotoPage(0);
                setPrompt("Not found");
                return;
            }
            matcher.reset(line);
            if (matcher.find()) {
                prepareReverse(lineNo, LAST_SUBLINE).output();
                return;
            }
            lineNo--;
        }
    }

    private void searchForwards() throws IOException {
        String input = readLine('/');
        int lineNo = topLineNo;
        if (input.length() <= 1) {
            if (regex == null) {
                setPrompt("No previous search");
                return;
            }
            lineNo++;
        } else {
            try {
                regex = Pattern.compile(input.substring(1));
            } catch (PatternSyntaxException ex) {
                setPrompt("Invalid regex");
                return;
            }
            matcher = regex.matcher("");
        }
        
        while (true) {
            String line = lineStore.getLine(lineNo);
            if (line == null) {
                gotoLastPage();
                setPrompt("Not found");
                return;
            }
            matcher.reset(line);
            if (matcher.find()) {
                prepare(lineNo, 0).output();
                return;
            }
            lineNo++;
        }
    }

    /**
     * Read a line up to the next newline and return it as a String.  The
     * line is echoed at the prompt location.
     * 
     * @param ch a preread character.
     * @return
     * @throws IOException
     */
    private String readLine(int ch) throws IOException {
        StringBuffer sb = new StringBuffer();
        String line;
        do {
            sb.append((char) ch);
            line = sb.toString();
            prompt(line);
            ch = pr.read();
        } while (ch != -1 && ch != '\n');
        return line;
    }

    private void help() throws IOException {
        String[] help = new String[] {
            "Move forwards 1 page:         SPACE, f",
            "Move backwards 1 page:        b",
            "Move forwards 1 data line:    ENTER",
            "Move backwards 1 data line:   k, y",
            "Move forwards 1 screen line:  d",
            "Move backwards 1 screen line: u",
            "Go to start of data:          <",
            "Go to end of data:            >",
            "Search forwards:              /regex",
            "Repeat search forwards:       /",
            "Search backwards:             ?regex",
            "Repeat search backwards:      ?",
            "Display this help screen:     h",
            "Quit:                         q, CTRL-D",
        };
        // Remember the 'current' buffer so that we can repaint it
        // when we are done.
        ScreenBuffer prevBuffer = this.currentBuffer;
        
        // Prepare and paint the help screen
        ScreenBuffer buffer = new ScreenBuffer(true);
        for (int i = 0; i < help.length; i++) {
            prepareLine(help[i], i, buffer);
        }
        buffer.adjust(0, 0);
        buffer.output();
        prompt("Hit any key to continue");
        
        // Wait till the user is done, then repaint the previous screen.
        pr.read();
        prompt();
        prevBuffer.output();
    }

    private void prompt() {
        if (prompt == null) {
            prompt(lineStore.isLastLineNo(bottomLineNo) ? "(END)" :
                (topLineNo + ", " + bottomLineNo + ": "));
        } else {
            prompt(prompt);
            prompt = null;
        }
    }

    private void prompt(String text) {
        console.clearRow(this.pageHeight);
        console.setChar(0, this.pageHeight, text.toCharArray(), DEFAULT_COLOR);
        console.setCursor(0, this.pageHeight);
    }
    
    private void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    private void erasePrompt() {
        console.clearRow(this.pageHeight);
    }

    /**
     * Page forward by one page.  The implementation strategy is to 'prepare'
     * lines one at a time until we have got (at least) a whole screen full.
     * The preparation process involves copying characters to 'buffer', padding
     * lines with spaces, and doing BS and TAB interpretation.  Then we output 
     * the prepared characters to the screen.  The 'output' process deals with
     * scrolling if we prepared less than a full screen, or truncation if we
     * prepared more than a full screen.
     * 
     * @throws IOException
     */
    private void nextPage() throws IOException {
        prepare(bottomLineNo + 1, 0).output();
    }
    
    /**
     * Page backwards by one page.  The implementation strategy is similar to
     * page forward except that we use prepareReverse which paints lines starting
     * with the nominated line and working backwards.
     * 
     * @throws IOException
     */
    private void prevPage() throws IOException {
        if (topLineNo > 0) {
            prepareReverse(topLineNo - 1, LAST_SUBLINE).output();
        }
    }
    
    private void gotoPage(int firstLineNo) throws IOException {
        prepare(firstLineNo, 0).output();
    }
    
    private void gotoLastPage() throws IOException {
        prepareReverse(lineStore.getLastLineNo(), LAST_SUBLINE).output();
    }
    
    /**
     * Page forward by one line.  We use the 'prepare' and 'output'
     * strategy as described in {@link nextPage}.
     * 
     * @throws IOException
     */
    private void nextLine() throws IOException {
        prepare(topLineNo + 1, 0).output();
    }

    /**
     * Page backwards by one line.  We use the 'prepare' and 'output'
     * strategy as described in {@link nextPage}.
     * 
     * @throws IOException
     */
    private void prevLine() throws IOException {
        prepareReverse(bottomLineNo - 1, LAST_SUBLINE).output();
    }
    
    private void nextScreenLine() throws IOException {
        prepare(topLineNo, topSublineNo + 1).output();
    }
    
    private void prevScreenLine() throws IOException {
        if (bottomSublineNo == 0) {
            prepareReverse(bottomLineNo - 1, LAST_SUBLINE).output();
        } else {
            prepareReverse(bottomLineNo, bottomSublineNo + 1).output();
        }
    }

    /**
     * Prepare lines for output by painting them to our private buffer in the forward
     * direction starting at a given line number and (rendered) subline number.
     * 
     * @param startLineNo
     */
    private ScreenBuffer prepare(int startLineNo, int startSublineNo) {
        ScreenBuffer buffer = new ScreenBuffer(true);
        int lineNo = startLineNo;
        boolean more;
        do {
            String line = lineStore.getLine(lineNo);
            if (line == null) {
                break;
            }
            more = prepareLine(line, lineNo, buffer);
            lineNo++;
        } while (more);
        if (buffer.adjust(startLineNo, startSublineNo) || startLineNo == 0) {
            return buffer;
        } else {
            return prepare(startLineNo - 1, LAST_SUBLINE);
        }
    }

    /**
     * 
     * Prepare lines for output by painting them to our private buffer in the reverse
     * direction starting at a given end line number and (rendered) subline number.
     */
    private ScreenBuffer prepareReverse(int endLineNo, int endSublineNo) {
        ScreenBuffer buffer = new ScreenBuffer(false);
        int lineNo = endLineNo;
        String line = null;
        boolean more = true;
        while (more && lineNo >= 0) {
            line = lineStore.getLine(lineNo);
            more = prepareLine(line, lineNo, buffer);
            lineNo--;
        }
        if (buffer.adjust(endLineNo, endSublineNo)) {
            return buffer;
        } else {
            return prepare(0, 0);
        }
    }
    
    /**
     * Prepare lines for output by painting them to our private buffer
     * starting at the supplied bufferLineOffset
     * 
     * @param line the line to be prepared
     * @param lineNo the line's line number
     * @param buffer the ScreenBuffer we are preparing 
     */
    private boolean prepareLine(String line, int lineNo, ScreenBuffer buffer) {
        buffer.startLine(lineNo);
        int pos = 0;
        int len = line.length();
        int startMatchPos = len;
        int endMatchPos = len;
        if (matcher != null) {
            matcher.reset(line);
            if (matcher.find(0)) {
                startMatchPos = matcher.start();
                endMatchPos = matcher.end();
            }
        } 
        for (int i = 0; i < len; i++) {
            if (i == startMatchPos) {
                buffer.setColor(MATCH_COLOR);
            } else if (i == endMatchPos) {
                if (matcher.find(i)) {
                    startMatchPos = matcher.start();
                    endMatchPos = matcher.end();
                    if (startMatchPos > i) {
                        buffer.setColor(DEFAULT_COLOR);
                    }
                } else {
                    buffer.setColor(DEFAULT_COLOR);
                }
            }
            // FIXME - support different renderings, including ones where
            // control characters are rendered as visible characters?
            char ch = line.charAt(i);
            switch (ch) {
                case '\n':
                    throw new AssertionError("no newlines expected");
                case '\r':
                    // ignore bare CRs.
                    break;
                case '\t':
                    int fill = tabSize - pos % pageWidth % tabSize;
                    for (int j = 0; j < fill; j++) {
                        buffer.putChar(' ');
                    }
                    pos += fill;
                    break;
                default:
                    if (ch >= ' ' && ch <= '\377' && ch != '\177') {
                        buffer.putChar(ch);
                    } else {
                        buffer.putChar('?');
                    }
                    pos++;
            }
        }
        buffer.setColor(DEFAULT_COLOR);
        buffer.endLine();
        return !buffer.isComplete();
    }

    /**
     * Capture keyboard input events and write all character data to the
     * private pipe.  The command thread will read them from
     * the other end as required.
     */
    @Override
    public void keyPressed(KeyboardEvent event) {
        if (!event.isConsumed()) {
            char ch = event.getKeyChar();
            if (ch != KeyboardEvent.NO_CHAR) {
                try {
                    pw.write(ch);
                    pw.flush();
                } catch (IOException ex) {
                    // ignore it
                }
                event.consume();
            }
        }
    }

    @Override
    public void keyReleased(KeyboardEvent event) {
        // ignore
    }
    
    private void debugln(String msg) {
        if (DEBUG) {
            err.println(msg);
        }
    }
    
    /**
     * This class provides an in-memory buffer for lines read from the data source 
     * being paged.  In the future, this could be enhanced to cut down on memory
     * usage.  When paging a seekable Reader, it could use seek/tell to record file
     * offsets rather than actual file lines.  When paging a non-seekable Reader
     * (e.g. a pipe) it could store the lines read from the reader in a temporary file.
     * 
     * @author crawley@jnode.org
     */
    private final class LineStore {
        private final BufferedReader reader;
        private boolean reachedEOF;
        private List<String> lines = new ArrayList<String>(100);
        
        private LineStore(Reader reader) {
            this.reader = new BufferedReader(reader);
        }
        
        /**
         * Get a line identified by line number.
         * @param lineNo the line number
         * @return the requested line, or <code>null</code> if the EOF
         * was reached before the requested line could be reached.
         */
        private String getLine(int lineNo) {
            if (lineNo < 0) {
                return null;
            } else if (lineNo < lines.size()) {
                return lines.get(lineNo);
            } else if (lineNo > lines.size()) {
                throw new AssertionError(
                        "Reading at wrong place (" + lineNo + " > " + lines.size() + ")");
            } else if (reachedEOF) {
                return null;
            } else {
                try {
                    String line = reader.readLine();
                    if (line == null) {
                        reachedEOF = true;
                    } else {
                        lines.add(line);
                    }
                    return line;
                } catch (IOException ex) {
                    ex.printStackTrace(err);
                    reachedEOF = true;
                    return null;
                }
            }
        }
        
        /**
         * Get the last line number for the data source.  This requires that
         * all lines of the data source are read up to the EOF position.
         * @return the last line number
         * @throws IOException
         */
        private int getLastLineNo() throws IOException {
            while (!reachedEOF) {
                String line = reader.readLine();
                if (line == null) {
                    reachedEOF = true;
                } else {
                    lines.add(line);
                }
            }
            return lines.size() - 1;
        }
        
        /**
         * Check if a given line number is known to be the last line
         * of the data source.  This method does not do any reading
         * ahead of the data source to find the last line.
         * @param lineNo the line number to test
         * @return Returns <code>true</code> if the given line number 
         * is known to be the last line, and <code>false</code> if it is
         * not, or if we don't know.
         */
        private boolean isLastLineNo(int lineNo) {
            return reachedEOF && lineNo == lines.size() - 1;
        }
    }
    
    /**
     * The ScreenBuffer class holds the screen image that we are building.
     * It takes care of wrapping long lines over multiple screen lines.
     * <p>
     * In the javadoc and embedded comments, a 'line' refers to an arbitrarily
     * long data source line, and a 'subline' refers to a screen line.  Sublines
     * are 'pageWidth' characters wide and are filled with SP characters when
     * completed.
     * 
     * @author crawley@jnode.org
     */
    private final class ScreenBuffer {
        private final class ScreenLine {
            private final char[] chars = new char[pageWidth];
            private final int[] colors = new int[pageWidth];
            private final int lineNo;
            private final int sublineNo;
            
            private ScreenLine(int lineNo, int sublineNo) {
                this.lineNo = lineNo;
                this.sublineNo = sublineNo;
            }
        }
        
        ArrayList<ScreenLine> lines = new ArrayList<ScreenLine>();
        
        // The direction of filling ...
        private final boolean forwards;
        
        // The current color.
        private int color;
        
        // The character pos in the current subline
        private int charPos;
        
        // The current subline no
        private int linePos;
        
        // The current data source line number
        private int lineNo;
        private int sublineNo;
        private int firstLinePos;
        private int lastLinePos;
        
        ScreenBuffer(boolean forwards) {
            this.forwards = forwards;
            this.linePos = 0;
            this.color = DEFAULT_COLOR;
        }

        void setColor(int color) {
            this.color = color;
        }

        /**
         * Start a new line.  If we are filling forwards, this will be
         * after the last subline of the current line.  If we are filling
         * backwards it will be before first subline of the current line.
         * It is an error to start a new line when the buffer is full, so 
         * this method won't grow the buffer.
         * 
         * @param lineNo the line number of the datastream line we are starting.
         */
        void startLine(int lineNo) throws IllegalArgumentException, IllegalStateException {
            if (lineNo < 0) {
                throw new IllegalArgumentException("lineNo < 0");
            }
            
            // Record current line number and allocate the first screen line.
            this.lineNo = lineNo;
            this.sublineNo = 0;
            lines.add(linePos, new ScreenLine(lineNo, 0));
            charPos = 0;
        }
        
        /**
         * End the current line.  This fills the remainder of the subline, then
         * moves 'linePos' to the position for the next line.
         */
        void endLine() {
            // Fill to the end of the current subline with a spaces
            ScreenLine line = lines.get(linePos);
            while (charPos < pageWidth) {
                line.chars[charPos] = ' ';
                line.colors[charPos] = color;
                charPos++;
            }
            // Move 'linepos' to the position for the next line.
            if (forwards) {
                linePos++;
            } else {
                while (linePos > 0 && lines.get(linePos - 1).lineNo == lineNo) {
                    linePos--;
                }
            }
        }
        
        /**
         * Put a character to the current line, allocating a new subline
         * if we wrap past 'pageWidth'.
         * 
         * @param ch
         */
        void putChar(char ch) {
            if (charPos >= pageWidth) {
                newSubline();
                charPos = 0;
            }
            ScreenLine line = lines.get(linePos);
            line.chars[charPos] = ch;
            line.colors[charPos] = color;
            charPos++;
        }
        
        /**
         * Add a new subline for the current line.  The subline
         * goes into the buffer after the current subline.  If
         * we are filling forward, the current subline stays where it is.
         * If we are filling backwards, other sublines in the current line
         * are moves 'upwards' to make space.
         */
        private void newSubline() {
            sublineNo++;
            linePos++;
            lines.add(linePos, new ScreenLine(lineNo, sublineNo));
        }
        
        /**
         * This method calculates the adjusted start/end linePos values corresponding
         * to the supplied lineNo/sublineNo and the opposite end of the screen buffer.
         * Then it resets the top/bottom lineNo and sublineNo fields.
         * 
         * @param lineNo
         * @param sublineNo
         */
        boolean adjust(final int lineNo, final int sublineNo) {
            debugln(lineNo + ", " + sublineNo);
            debugln(topLineNo + ", " + topSublineNo + ", " +
                    bottomLineNo + ", " + bottomSublineNo);
            int linePos;
            int len = lines.size();
            if (len == 0) {
                firstLinePos = 0;
                lastLinePos = -1;
                topLineNo = 0;
                topSublineNo = 0;
                bottomLineNo = -1;
                bottomSublineNo = 0;
            } else {
                for (linePos = 0; linePos < len - 1; linePos++) {
                    ScreenLine line = lines.get(linePos);
                    if (line.lineNo == lineNo && line.sublineNo == sublineNo) {
                        break;
                    }
                    if (line.lineNo > lineNo) {
                        linePos--;
                        break;
                    }
                }
                debugln(linePos + " : " + len);
                firstLinePos = forwards ? linePos : Math.max(0, linePos - pageHeight + 1);
                lastLinePos = forwards ? Math.min(len, linePos + pageHeight) - 1 : linePos;

                if (lastLinePos >= len) {
                    firstLinePos = Math.max(0, firstLinePos - (len - lastLinePos));
                    lastLinePos = len - 1;
                }

                debugln(firstLinePos + ", " + lastLinePos);
                ScreenLine topLine = lines.get(firstLinePos);
                topLineNo = topLine.lineNo;
                topSublineNo = topLine.sublineNo;
                ScreenLine bottomLine = lines.get(lastLinePos);
                bottomLineNo = bottomLine.lineNo;
                bottomSublineNo = bottomLine.sublineNo;
            }
            debugln(topLineNo + ", " + topSublineNo + ", " +
                    bottomLineNo + ", " + bottomSublineNo);
            return lastLinePos - firstLinePos == (pageHeight - 1);
        }

        /**
         * Test if the buffer is 'full'; i.e. if 'pageHeight' lines
         * have been populated.  This should only be called after
         * we have called {@line #endLine()}.
         * 
         * @return
         */
        boolean isComplete() {
            if (charPos != pageWidth) {
                throw new IllegalStateException(
                        "line is still active (" + charPos + ", " + pageWidth + ")");
            }
            // Since we've called endLine(), linePos should be at the point where
            // the next line will be added to the buffer.
            if (forwards) {
                return linePos >= pageHeight;
            } else {
                return lines.size() - linePos - 1 >= pageHeight;
            }
        }

        /**
         * Output the buffer to the screen.  When we're done, we make this buffer
         * the 'current' buffer. 
         */
        void output() {
            // This is probably the best I can do given the current console APIs.  The 
            // problem are:
            // 1) there is no 'console.setChar(x, y, chars, colors, x, 1)' method, and
            // 2) a call to setChar(...) will sync the screen, which currently repaints
            //    every character to the screen device.
            
            // First we build a single char array for all characters on the screen, populate
            // from the lines, and pad out with spaces to the screen height.
            char[] tmp = new char[pageSize];
            debugln("output: " + firstLinePos + ", " + lastLinePos);
            for (int y = firstLinePos; y <= lastLinePos; y++) {
                ScreenLine line = lines.get(y);
                System.arraycopy(line.chars, 0, tmp, (y - firstLinePos) * pageWidth, pageWidth);
            }
            Arrays.fill(tmp, (lastLinePos - firstLinePos + 1) * pageWidth, pageSize, ' ');
            
            // Next, output the characters in the default color
            console.setChar(0, 0, tmp, 0, pageSize, 0x7);
            
            // Finally, go back and repaint any characters that have a different color
            // to the default.  We do this in runs, to avoid doing too many screen syncs. 
            int color = DEFAULT_COLOR;
            int colorStartX = -1;
            int colorStartY = -1;
            int colorStartPos = -1;
            for (int y = firstLinePos; y <= lastLinePos; y++) {
                ScreenLine line = lines.get(y);
                int[] colors = line.colors;
                for (int x = 0; x < pageWidth; x++) {
                    if (colors[x] != color) {
                        if (color != DEFAULT_COLOR) {
                            int pos = x + y * pageWidth;
                            console.setChar(colorStartX, colorStartY,
                                    tmp, colorStartPos, pos - colorStartPos, color);
                        } else {
                            colorStartX = x;
                            colorStartY = y;
                            colorStartPos = colorStartX + colorStartY * pageWidth;
                        }
                        color = colors[x];
                    }
                }
                if (color != DEFAULT_COLOR) {
                    console.setChar(colorStartX, colorStartY,
                            tmp, colorStartPos, pageSize - colorStartPos, color);
                }
            }
            currentBuffer = this;
        }
    }
}
