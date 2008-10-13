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

import org.jnode.driver.console.TextConsole;
import org.jnode.driver.input.KeyboardEvent;
import org.jnode.driver.input.KeyboardListener;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.Shell;
import org.jnode.shell.ShellUtils;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintWriter;
import java.io.Reader;

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

    private PrintWriter out;
    private PrintWriter err;
    private TextConsole console;
    private int screenHeight;
    private int screenWidth;
    
    // This pipe passes characters from the system thread that calls 
    // our 'keyPressed' event method to the thread that runs the Page command.
    private PipedReader pr;
    private PipedWriter pw;
    
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
        out = getOutput().getPrintWriter();
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
            page(r);
            
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
        console.removeKeyboardListener(this);
        pw.close();
        pr.close();
    }

    private void setup() throws NameNotFoundException, IOException {
        Shell shell = ShellUtils.getCurrentShell();
        try {
            console = (TextConsole) shell.getConsole();
        } catch (ClassCastException ex) {
            err.println("Page is only supported with a TextConsole");
            exit(1);
        }
//        this.screenHeight = console.getHeight();
//        this.screenWidth = console.getWidth();
        this.screenHeight = 24;
        this.screenWidth = 80;
//        err.println("screen height - " + screenHeight);
//        err.println("screen width - " + screenWidth);
        
        pw = new PipedWriter();
        pr = new PipedReader();
        pr.connect(pw);
        
        console.addKeyboardListener(this);
    }

    /**
     * Do the paging, reading from our private console input
     * pipe to figure out what to do next.
     * 
     * @param r the source of data to be paged.
     * @throws IOException
     */
    private void page(Reader r) throws IOException {
        BufferedReader br = new BufferedReader(r);
        
        // Output first page.
        boolean eof = outputPage(br);
        
        // Until we get
        while (!eof) {
            int ch = pr.read();
            switch (ch) {
                case -1:
                    eof = true;
                    break;
                case ' ':
                    eof = outputPage(br);
                    break;
                case '\n':
                    eof = outputLine(br);
                    break;
                default:
                    // ignore
            }
        }
    }

    /**
     * Output the next line.
     * @param br the source of the data being paged.
     * @return <code>true</code> if we reach the EOF.
     * @throws IOException
     */
    private boolean outputLine(BufferedReader br) throws IOException {
        String line = br.readLine();
        if (line == null) {
            return true;
        } else {
            out.println(line);
            return false;
        }
    }

    /**
     * Output the next page. 
     * 
     * @param br the source of the data being paged.
     * @return <code>true</code> if we reach the EOF.
     * @throws IOException
     */
    private boolean outputPage(BufferedReader br) throws IOException {
        int lineCount = 0;
        String line = null;
        while (lineCount < screenHeight && (line = br.readLine()) != null) {
            lineCount += (line.length() / screenWidth) + 1;
            out.println(line);
        }
        return line == null;
    }

    /**
     * Capture keyboard input and stuff characters input the
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
}
