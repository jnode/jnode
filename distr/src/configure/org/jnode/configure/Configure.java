/*
 * $Id $
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
package org.jnode.configure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;


/**
 * The main class for the JNode configuration tool (command line version).
 * <p>
 * The command currently does not use the JNode Command / Syntax APIs.  This
 * should be addressed when we have a compatibility library.  A version with
 * a GUI-based interface would be a good idea too.
 * 
 * @author crawley@jnode.org
 */
public class Configure {
    public static final int DISPLAY_NORMAL = 0;
    public static final int DISPLAY_HIGHLIGHT = 1;
    public static final int DISPLAY_PROMPT = 2;
    
    public static final String NEW_LINE = System.getProperty("line.separator");
    public static final int TAB_WIDTH = 8;
    
    private final BufferedReader in;
    private final PrintStream out;
    private final PrintStream err;
    private String scriptFile;
    private boolean debug;
    
    private Configure() {
        this.in = new BufferedReader(new InputStreamReader(System.in));
        this.out = System.out;
        this.err = System.err;
    }
    
    private void run(String[] args) {
        try {
            parseArguments(args);
            ConfigureScript script = new ScriptParser().loadScript(scriptFile);
            for (PropertySet propFile : script.getPropsFiles()) {
                propFile.load(this);
            }
            script.execute(this);
            for (PropertySet propFile : script.getPropsFiles()) {
                saveProperties(propFile);
            }
            output("Done.");
        } catch (ConfigureException ex) {
            error(ex.getMessage());
            if (debug) {
                ex.printStackTrace(err);
            }
            System.exit(1);
        }
    }
    
    /**
     * Parse the command line.
     * 
     * @param args
     */
    private void parseArguments(String[] args) throws ConfigureException {
        int i;
        for (i = 0; i < args.length; i++) {
            String arg = args[i];
            if (!arg.startsWith("-")) {
                break;
            }
            if (arg.equals("--debug")) {
                debug = true;
            } else {
                throw new ConfigureException("Unrecognized option: " + args[i]);
            }
        }
        if (i >= args.length) {
            throw new ConfigureException("Missing script file argument");
        }
        scriptFile = args[i++];
        if (i < args.length) {
            throw new ConfigureException("Unexpected command argument: " + scriptFile);
        }
    }
    
    public String input(String prompt) throws ConfigureException {
        output(prompt, DISPLAY_PROMPT);
        try {
            return in.readLine();
        } catch (IOException ex) {
            throw new ConfigureException("Unexpected IO exception", ex);
        }
    }

    public void output(String message, int displayAttributes) {
        format(err, message, displayAttributes);
    }

    public void output(String message) {
        format(err, message, DISPLAY_NORMAL);
    }

    public void error(String message) {
        format(err, message, DISPLAY_NORMAL);
    }
    
    private void format(PrintStream stream, String text, int displayAttributes) {
        switch (displayAttributes) {
            case DISPLAY_PROMPT:
            case DISPLAY_HIGHLIGHT | DISPLAY_PROMPT:
                stream.print(text + " ");
                break;
            case DISPLAY_HIGHLIGHT:
                stream.println(text);
                int textWidth = computeTextWidth(text);
                StringBuffer sb = new StringBuffer(textWidth);
                for (int i = 0; i < textWidth; i++) {
                    sb.append('-');
                }
                stream.println(sb.toString());
                break;
            case DISPLAY_NORMAL:
            default:
                stream.println(text);
                break;
        }
    }

    private int computeTextWidth(String text) {
        int count = 0;
        int len = text.length();
        for (int i = 0; i < len; i++) {
            char ch = text.charAt(i);
            if (ch == '\t') {
                count = ((count / TAB_WIDTH) + 1) * TAB_WIDTH;
                break;
            } else if (ch == '\n' || ch == '\r') {
                if (i + NEW_LINE.length() != len) {
                    throw new IllegalArgumentException("Embedded CR or NL in line text");
                }
            } else if (ch < ' ' || ch == '\177') {
                throw new IllegalArgumentException("Bad character in line text");
            } else {
                count++;
            }
        }
        return count;
    }

    private void saveProperties(PropertySet propSet) throws ConfigureException {
        propSet.save(this);
    }

    public static void main(String[] args) {
        new Configure().run(args);
    }
    
}
