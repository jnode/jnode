/*
 * $Id$
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
 
package org.jnode.shell.command.bsh;

import gnu.java.io.NullOutputStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;

import bsh.ConsoleInterface;
import bsh.EvalError;
import bsh.Interpreter;

/**
 * User: Sam Reid Date: Jan 1, 2004 Time: 10:40:34 AM Copyright (c) Jan 1, 2004
 * by Sam Reid
 */
public class BeanshellCommand {

    private static Interpreter interpreter;
    /*static {
        //ConsoleInterface ci=new BshConsole();
    }*/

    static class BshPrintStream extends PrintStream {

        public BshPrintStream() {
            super(new NullOutputStream());
        }

        public void print(boolean b) {
            //            System.out.print
        }

        public void print(char c) {
            super.print(c);
        }

        public void print(int i) {
            super.print(i);
        }

        public void print(long l) {
            super.print(l);
        }

        public void print(float f) {
            super.print(f);
        }

        public void print(double d) {
            super.print(d);
        }

        public void print(char[] s) {
            super.print(s);
        }

        public void print(String s) {
            super.print(s);
        }

        public void print(Object obj) {
            super.print(obj);
        }

        public void println() {
            super.println();
        }

        public void println(boolean b) {
            super.println(b);
        }

        public void println(char c) {
            super.println(c);
        }

        public void println(int i) {
            super.println(i);
        }

        public void println(long l) {
            super.println(l);
        }

        public void println(float f) {
            super.println(f);
        }

        public void println(double d) {
            super.println(d);
        }

        public void println(char[] s) {
            super.println(s);
        }

        public void println(String s) {
            super.println(s);
        }

        public void println(Object obj) {
            super.println(obj);
        }
    }

    static class BshConsole implements ConsoleInterface {

        StringReader in = new StringReader("234");

        public Reader getIn() {
            return in;//dummy
        }

        public PrintStream getOut() {
            return null;
        }

        public PrintStream getErr() {
            return null;
        }

        public void println(Object o) {
        }

        public void print(Object o) {
        }

        public void error(Object o) {
        }

    }

    /**
     * Normal usage will be BeanshellCommand filename.bsh But you could also
     * inline java code like this: BeanshellCommand -code "new
     * JFrame().setVisible(true)"; <p/>BeanshellCommand should start an
     * interactive session with beanshell. BeanshellCommand file.bsh should
     * interpret and run the script. BeanshellCommand -code "code" should
     * interpret and run the code.
     */
    public static void showHelp() {
        System.err
                .println("Usage: \nbsh: interactive beanshell.\n"
                        + "bsh -help: this usage menu\n"
                        + "bsh -code CODE: evaluate the following shell code.\n"
                        + "bsh FILE: run the beanshell interpreter on the source FILE.");
    }

    private static void evaluateFile(String arg) {
        try {
            System.err.println("Evaluating beanshell source=" + arg);
            new Interpreter().source(arg);
        } catch (IOException e) {
            e.printStackTrace(); //To change body of catch statement use
                                 // Options | File Templates.
        } catch (EvalError evalError) {
            evalError.printStackTrace(); //To change body of catch statement
                                         // use Options | File Templates.
        }
    }

    public static void main(String[] args) {
        //        System.out.println("Started beanshell command.");
        if (args.length == 0) {
            //run interactive shell
            //            System.err.println("Interactive shell not yet written.");
            runInteractiveShell();
            return;
        } else if (args.length == 1 && args[ 0].toLowerCase().equals("-help")) {
            System.err
                    .println("Usage: bsh: interactive beanshell. <not yet working>\n"
                            + "bsh FILE: run the beanshell interpreter on the source FILE. <not yet written>\n"
                            + "bsh -help: this usage menu\n"
                            + "bsh -code CODE: evaluate the following shell code.");
        } else if (args.length == 1) {
            evaluateFile(args[ 0]);
        } else if (args.length == 2 && args[ 0].toLowerCase().equals("-code")) {
            String sourcecode = args[ 1];
            evaluateSourceString(sourcecode);
        } else if (args.length == 1 && args[ 0].toLowerCase().equals("-charva")) {
            //            runCharvaShell();
        } else {
            showHelp();
        }
    }

    private static void runInteractiveShell() {
        System.err.println("Starting Interactive beanshell.");
        Reader reader = new InputStreamReader(System.in);
        Reader r2 = new BufferedReader(reader);
        PrintStream out = new PrintStream(System.out);
        PrintStream err = new PrintStream(System.err);

        Interpreter interpreter = new Interpreter(r2, out, err, true);
        interpreter.run();
        System.err.println("Finished Interactive beanshell.");
    }

    private static void evaluateSourceString(String sourcecode) {
        System.out.println("Evaluating source string=" + sourcecode);
        try {
            Object out = interpreter.eval(sourcecode);
            System.out.println(out);
        } catch (EvalError evalError) {
            evalError.printStackTrace(); //To change body of catch statement
                                         // use Options | File Templates.
        }
    }

}
