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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.naming.NameNotFoundException;

/**
 * This command is a simple analog of the UNIX/Linux 'more' and 'less' commands.
 * 
 * @author crawley@jnode.org
 */
public class PageCommand extends AbstractCommand implements KeyboardListener {
    
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
    
    // This is where we build the screen image prior to sending it to the console
    private char[] charBuffer;
    private int[] colorBuffer;
    
    // This pipe passes characters from the system thread that calls 
    // our 'keyPressed' event method to the thread that runs the Page command.
    private PipedReader pr;
    private PipedWriter pw;

    private TextScreenConsoleManager manager;

    private LineStore lineStore;

    private Pattern regex;
    private Matcher matcher;

    private String prompt;
    
    
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
                err.println("Paging piped from the console is not supported");
                exit(1);
            } else {
                r = getInput().getReader();
            }
            setup();
            lineStore = new LineStore(r);
            pager();
            
        } catch (IOException ex) {
            err.println(ex.getMessage());
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

    private void tearDown() throws IOException {
        manager.unregisterConsole(console);
        pw.close();
        pr.close();
    }

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
        charBuffer = new char[pageSize];
        colorBuffer = new int[pageSize];
        
        pw = new PipedWriter();
        pr = new PipedReader();
        pr.connect(pw);
        
        console.addKeyboardListener(this);
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
                prepareReverse(lineNo);
                output();
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
                prepare(lineNo);
                output();
                return;
            }
            lineNo++;
        }
    }

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
            "Move forwards 1 page:      ' ', 'f'",
            "Move backwards 1 page:     'b'",
            "Move forwards 1 line:      ENTER",
            "Move backwards 1 line:     'k', 'y'",
            "Go to start of data:       '<'",
            "Go to end of data:         '>'",
            "Search forwards:           '/regex'",
            "Repeat search forwards:    '/'",
            "Search backwards:          '?regex'",
            "Repeat search backwards:   '?'",
        };
        StringBuffer sb = new StringBuffer(pageSize);
        for (int i = 0; i < pageHeight; i++) {
            if (i < help.length) {
                sb.append(help[i]);
            } else {
                sb.append(' ');
            }
            while (sb.length() % pageWidth != 0) {
                sb.append(' ');
            }
        }
        console.setChar(0, 0, sb.toString().toCharArray(), 0, pageSize, 0x7);
        prompt("Hit any key to continue");
        int ch = pr.read();
        prompt();
        output();
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
        console.setChar(0, this.pageHeight, text.toCharArray(), 0x07);
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
        prepare(bottomLineNo + 1);
        output();
    }
    
    private void prevPage() throws IOException {
        if (topLineNo > 0) {
            prepareReverse(topLineNo - 1);
            output();
        }
    }
    
    private void gotoPage(int firstLineNo) throws IOException {
        prepare(firstLineNo);
        output();
    }
    
    private void gotoLastPage() throws IOException {
        prepareReverse(lineStore.getLastLineNo());
        output();
    }
    
    /**
     * Page forward by one line.  We use the 'prepare' and 'output'
     * strategy as described in {@link nextPage}.
     * 
     * @throws IOException
     */
    private void nextLine() throws IOException {
        prepare(topLineNo + 1);
        output();
    }

    /**
     * Page backwards by one line.  We use the 'prepare' and 'output'
     * strategy as described in {@link nextPage}.
     * 
     * @throws IOException
     */
    private void prevLine() throws IOException {
        prepare((topLineNo > 0) ? topLineNo - 1 : topLineNo);
        output();
    }

    private void output() {
        console.setChar(0, 0, charBuffer, 0, pageSize, 0x7);
        for (int i = 0; i < pageSize; i++) {
            if (colorBuffer[i] != 0x07) {
                console.setChar(i % pageWidth, i / pageWidth, charBuffer, i, 1, colorBuffer[i]);
            }
        }
    }

    /**
     * Prepare lines for output by painting them to our private buffer
     * starting at the supplied bufferLineOffset
     * 
     * @param startLineNo
     */
    private void prepare(int startLineNo) {
        int lineNo = startLineNo;
        RenderCursor cursor = new RenderCursor(charBuffer, colorBuffer);
        String line = null;
        while (cursor.getPos() < pageSize) {
            line = lineStore.getLine(lineNo);
            if (line == null) {
                if (startLineNo > 0) {
                    prepare(startLineNo - 1);
                    return;
                }
                cursor.putSpaces(pageSize - cursor.getPos());
                break;
            }
            lineNo++;
            render(line, 0, cursor);
        }
        if (lineNo > startLineNo) {
            topLineNo = startLineNo;
            bottomLineNo = lineNo - 1;
        }
    }

    /**
     * Prepare lines for output by painting them to our private buffer
     * starting at the supplied bufferLineOffset
     * 
     * @param bufferLineOffset the buffer offset in screen lines to start
     *        painting
     */
    private void prepareReverse(int endLineNo) {
        int lineNo = endLineNo;
        RenderCursor cursor = new RenderCursor(charBuffer, colorBuffer);
        RenderCursor measureCursor = new RenderCursor(null, null);
        String line = null;
        int lineOffset = pageSize;
        while (lineOffset > 0 && lineNo >= 0) {
            line = lineStore.getLine(lineNo--);
            measureCursor.setPos(0);
            render(line, 0, measureCursor);
            lineOffset -= measureCursor.getPos();
            cursor.setPos(lineOffset);
            render(line, 0, cursor);
        }
        if (lineOffset <= 0) {
            topLineNo = Math.max(0, lineNo);
            bottomLineNo = endLineNo;
        } else {
            prepare(0);
        }
    }
    
    /**
     * Prepare lines for output by painting them to our private buffer
     * starting at the supplied bufferLineOffset
     * 
     * @param line the line to be prepared
     * @param bufferLineOffset the buffer offset in screen lines to start
     *        painting
     */
    private void render(String line, int firstSubline, RenderCursor cursor) {
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
                cursor.setColor(0x04);
            } else if (i == endMatchPos) {
                cursor.setColor(0x07);
                if (i + 1 < len && matcher.find(i + 1)) {
                    startMatchPos = matcher.start();
                    endMatchPos = matcher.end();
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
                case '\b':
                    if (pos > 0) {
                        pos--;
                    }
                    break;
                case '\t':
                    int fill = tabSize - pos % pageWidth % tabSize;
                    cursor.putSpaces(fill);
                    pos += fill;
                    break;
                default:
                    if (ch > ' ' && ch <= '\377' && ch != '\177') {
                        cursor.putChar(ch);
                    } else {
                        cursor.putChar('?');
                    }
                    pos++;
            }
        }
        cursor.setColor(0x07);
        int fill = pageWidth - pos % pageWidth;
        if (fill != pageWidth) {
            cursor.putSpaces(fill);
            pos += fill;
        }
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
     * The RenderCursor class is used for storing characters in a region of a buffer,
     * or counting characters.
     * 
     * @author crawley@jnode.org
     */
    private final class RenderCursor {
        int pos;
        char[] charBuffer;
        int[] colorBuffer;
        int color;
        
        /**
         * Create a render cursor for filling a buffer.  If the supplied buffer
         * is <code>null</code> the cursor will just count the characters.
         * 
         * @param charBuffer The buffer to hold the characters, or <code>null</code>
         * @param colorBuffer The buffer to hold the corresponding colors, or <code>null</code>
         */
        private RenderCursor(char[] charBuffer, int[] colorBuffer) {
            this.charBuffer = charBuffer;
            this.colorBuffer = colorBuffer;
            this.pos = 0;
            this.color = 0x07;
        }
        
        /**
         * Set or reset the cursor's offset for putting characters.
         * @param pos the number of characters from the start of the
         * buffer.  This may be negative.
         */
        public void setPos(int pos) {
            this.pos = pos;
        }

        /**
         * Set the current color for characters.
         */
        public void setColor(int color) {
            this.color = color;
        }

        /**
         * Put a character into the buffer and advance the cursor.  If the 
         * current cursor position is outside of the buffer bounds, the
         * character is quietly dropped, but the cursor is moved anyway.
         * @param ch the character to be put into the buffer.
         */
        private void putChar(char ch) {
            if (charBuffer == null || pos < 0 || pos >= charBuffer.length) {
                pos++;
            } else {
                colorBuffer[pos] = color;
                charBuffer[pos++] = ch;
            }
        }
        
        /**
         * Put multiple spaces into the buffer.  This is logically equivalent
         * to calling {@line #putChar(char)} <code>count</code> times.
         * @param count the number of spaces to put into the buffer.
         */
        private void putSpaces(int count) {
            if (charBuffer != null) {
                int pos1 = Math.max(pos, 0);
                int pos2 = Math.min(pos + count, charBuffer.length);
                for (int i = pos1; i < pos2; i++) {
                    colorBuffer[i] = color;
                    charBuffer[i] = ' ';
                }
            }
            pos += count;
        }
        
        /**
         * Get the current cursor position.
         * @return the position.
         */
        private int getPos() {
            return pos;
        }
    }
}
