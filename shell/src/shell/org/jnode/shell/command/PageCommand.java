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

import javax.naming.NameNotFoundException;

/**
 * This command is a simple analog of the UNIX/Linux 'more' and 'less' commands.
 * Its current reportoire is:
 * <dl>
 *   <dt>SP</dt><dd>Output the next page.</dd>
 *   <dt>NL</dt><dd>Output the next line.</dd>
 * </dl>
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
    
    // This is the line number of the top (data source) line displayed on the
    // screen page.
    private int topLineNo;
    private int topSublineNo;
    
    
    // This is the line number of the bottom (data source) line displayed on the
    // screen page.
    private int bottomLineNo;
    private int bottomSublineNo;
    
    // This is where we build the screen image prior to sending it to the console
    private char[] buffer;
    
    // This pipe passes characters from the system thread that calls 
    // our 'keyPressed' event method to the thread that runs the Page command.
    private PipedReader pr;
    private PipedWriter pw;

    private TextScreenConsoleManager manager;

    private int tabSize = 8;

    private int pageSize;

    private LineStore lineStore;
    
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
        buffer = new char[pageSize];
        
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

    private void help() throws IOException {
        String[] help = new String[] {
            "Move forwards 1 page: ' ', 'f'",
            "Move backwards 1 page: 'b'",
            "Move forwards 1 line: ENTER",
            "Move backwards 1 line: 'k', 'y'",
            "Go to start of data: '<'",
            "Go to end of data: '>'",
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
        prompt(lineStore.isLastLineNo(bottomLineNo) ? "(END)" :
                (topLineNo + ", " + bottomLineNo + ": "));
    }

    private void prompt(String text) {
        console.clearRow(this.pageHeight);
        console.setChar(0, this.pageHeight, text.toCharArray(), 0x07);
        console.setCursor(0, this.pageHeight);
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
        console.setChar(0, 0, buffer, 0, pageSize, 0x7);
    }

    /**
     * Prepare lines for output by painting them to our private buffer
     * starting at the supplied bufferLineOffset
     * 
     * @param startLineNo
     */
    private void prepare(int startLineNo) {
        int lineNo = startLineNo;
        RenderCursor cursor = new RenderCursor(buffer);
        String line = null;
        while (cursor.getEndOffset() < pageSize) {
            line = lineStore.getLine(lineNo);
            if (line == null) {
                if (startLineNo > 0) {
                    prepare(startLineNo - 1);
                    return;
                }
                while (cursor.getEndOffset() < pageSize) {
                    cursor.putChar(' ');
                }
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
        RenderCursor cursor = new RenderCursor(buffer);
        RenderCursor measureCursor = new RenderCursor(null);
        String line = null;
        int lineOffset = pageSize;
        while (lineOffset > 0 && lineNo >= 0) {
            line = lineStore.getLine(lineNo--);
            measureCursor.setStartOffset(0);
            render(line, 0, measureCursor);
            lineOffset -= measureCursor.getEndOffset();
            cursor.setStartOffset(lineOffset);
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
        for (int i = 0; i < len; i++) {
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
                    cursor.putChar(' ');
                    pos++;
                    while (pos % pageWidth % tabSize != 0) {
                        cursor.putChar(' ');
                        pos++;
                    }
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
        while (pos % pageWidth != 0) {
            cursor.putChar(' ');
            pos++;
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
     * This class provides an in-memory buffer of lines from the data source 
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
        
        private String getLine(int lineNo) {
            if (lineNo < lines.size()) {
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
        
        private boolean isLastLineNo(int lineNo) {
            return reachedEOF && lineNo == lines.size() - 1;
        }
    }
    
    private final class RenderCursor {
        int pos;
        int startOffset;
        char[] buffer;
        
        private RenderCursor(char[] buffer) {
            this.buffer = buffer;
            this.startOffset = 0;
        }
        
        public void setStartOffset(int startOffset) {
            this.startOffset = startOffset;
        }

        private void putChar(char ch) {
            if (buffer == null || pos + startOffset < 0 || pos + startOffset > buffer.length) {
                pos++;
            } else {
                buffer[pos++ + startOffset] = ch;
            }
        }
        
        private int getEndOffset() {
            return pos + startOffset;
        }
    }
}
