/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.test.shell.proclet;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

/**
 * This test application test the behavior of streams when System.setIn/Out/Err are used.
 * This is mostly aimed at exercising aspects of the proclet proxy
 * streams mechanisms.  It is a 'classic' Java application that does its owm
 * simple command argument handling.  It should be run in JNode; e.g. from the shell prompt. 
 */
public class ProcletStreamTest {

    public static void main(String[] args) throws Exception {
        new ProcletStreamTest().execute(args);
    }

    /**
     * Execute the command
     */
    public void execute(String[] args) throws Exception {
        String USAGE = "usage: <cmd> { in | out | err }";
        if (args.length == 0) {
            System.err.println(USAGE);
        } else if (args[0].equals("in")) {
            systemInputTests();
        } else if (args[0].equals("out")) {
            systemOutputTests();
        } else if (args[0].equals("err")) {
            systemErrorTests();
        } else {
            System.err.println(USAGE);
        }
    }
    
    private void systemInputTests() throws IOException {
        String data = "1234567890\n1234567890\n1234567890\n1234567890\n1234567890\n" +
                "1234567890\n1234567890\n1234567890\n1234567890\n1234567890\n";
        System.out.println("start testing");
        InputStream is = new ByteArrayInputStream(data.getBytes());
        System.out.println("initial stream -> 1234567890");
        readlineAndEcho(is);
        System.setIn(is);
        System.out.println("System.in -> 1234567890");
        readlineAndEcho(is);
        is = new CharFilterStream(System.in, '0');
        System.out.println("filtered -> 123456789");
        readlineAndEcho(is);
        System.setIn(is);
        System.out.println("System.in (filtered) -> 123456789");
        readlineAndEcho(System.in);
        InputStream savedSysIn = System.in;
        InputStream is2 = new CharFilterStream(System.in, '9');
        System.out.println("double filtered -> 12345678");
        readlineAndEcho(is2);
        System.setIn(is2);
        System.out.println("System.in (double filtered) -> 12345678");
        readlineAndEcho(System.in);
        System.out.println("saved System.in (single filtered) -> 123456789");
        readlineAndEcho(savedSysIn);
        System.out.println("finished testing");
    }

    private void readlineAndEcho(InputStream is) throws IOException {
        while (true) {
            int b = is.read(); 
            switch (b) {
                case -1:
                    return;
                case '\n':
                    System.err.println();
                    return;
                default:
                    System.err.print((char) b);
            }
        }
    }
    
    private class CharFilterStream extends FilterInputStream {
        private final char filterChar;

        protected CharFilterStream(InputStream in, char filterChar) {
            super(in);
            this.filterChar = filterChar;
        }

        @Override
        public int read() throws IOException {
            int ch;
            while ((ch = super.read()) == filterChar) {
                /* loop */
            }
            return ch;
        }
    }

    private void systemOutputTests() {
        System.err.println("start testing [line #1 red]");
        
        // Initial state
        System.out.println("-> System.out [line #2 white]");
        
        // Wrap the initial System.out and test
        PrintStream ps = new PrintStream(System.out);
        ps.println("-> after wrapping [line #3 white]");
        
        // Update System.out and test
        System.setOut(ps);
        System.out.println("-> System.out (wrapped) [line #4 white]");
        
        ps = new PrintStream(System.out);
        ps.println("-> after double wrapping [line #5 white]");
        System.setOut(ps);
        System.out.println("-> System.out (double wrapped) [line #6 white]");
        
        // Now check that output using the previously saved (and double wrapped)
        // System.out still goes to the same place after we update System.out.
        System.setOut(System.err);
        ps.println("-> double wrapped (old) System.out [line #7 white");
        System.out.println("-> System.out (really System.err) [line #8 red]");
        System.err.println("done testing [line #9 red]");
    }

    private void systemErrorTests() {
        System.out.println("start testing [line #1 white]");
        
        // Initial state
        System.err.println("-> System.error [line #2 red]");
        
        // Wrap the initial System.err and test
        PrintStream ps = new PrintStream(System.err);
        ps.println("-> after wrapping [line #3 red]");
        
        // Update System.err and test
        System.setErr(ps);
        System.err.println("-> System.error (wrapped) [line #4 red]");
        
        ps = new PrintStream(System.err);
        ps.println("-> after double wrapping [line #5 red]");
        System.setErr(ps);
        System.err.println("-> System.error (double wrapped) [line #6 red]");
        
        // Now check that output using the previously saved (and double wrapped)
        // System.err still goes to the same place after we update System.err.
        System.setErr(System.out);
        ps.println("-> double wrapped (old) System.err [line #7 red");
        System.err.println("-> System.error (really System.out) [line #8 white]");
        System.out.println("done testing [line #9 white]");
    }
}
