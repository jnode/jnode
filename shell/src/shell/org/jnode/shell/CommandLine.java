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
 
package org.jnode.shell;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.io.InputStream;
import java.io.IOException;

/**
 * This class represents the command line as an iterator. A trailing space leads
 * to an empty token to be appended.
 * 
 * @author epr
 * @author qades
 * @author Martin Husted Hartvig (hagar@jnode.org)
 */
public class CommandLine {

    public static final int LITERAL = 0;

    public static final int STRING = 1;

    public static final int CLOSED = 2;

    public static final char ESCAPE_CHAR = '\\';

    public static final char FULL_ESCAPE_CHAR = '\'';

    public static final char QUOTE_CHAR = '"';

    public static final char SPACE_CHAR = ' ';

    public static final char SEND_OUTPUT_TO_CHAR = '>';

    public static final char COMMENT_CHAR = '#';

    private static final char ESCAPE_B = '\b';

    private static final char B = 'b';

    private static final char ESCAPE_N = '\n';

    private static final char N = 'n';

    private static final char ESCAPE_R = '\r';

    private static final char R = 'r';

    private static final char ESCAPE_T = '\t';

    private static final char T = 't';

    private String s;

    private int pos = 0;

    private int type = LITERAL;

    private static final char linebreak = "\n".charAt(0);

    // private boolean inEscape = false;
    private boolean inFullEscape = false;

    private boolean inQuote = false;

    private String outFileName = null;

    private void setCommandLine(String s) {
        int send_to_file = s.indexOf(SEND_OUTPUT_TO_CHAR);

        if (send_to_file != -1) {
            if (send_to_file < s.length()) {
                setOutFileName((s.substring(send_to_file + 1)).trim());

                this.s = s.substring(0, send_to_file);
            } else {
                this.s = s.substring(send_to_file);
            }
        } else {
            this.s = s;
        }
    }

    /**
     * Creates a new instance.
     */
    public CommandLine(String s) {
        setCommandLine(s);
    }

    /**
     * Create a new instance.
     * 
     * @param args
     */
    public CommandLine(String[] args) {
        this(escape(args));
    }

    public CommandLine(InputStream in) {
        try {
            int avaliable = in.available();
            StringBuilder stringBuilder = new StringBuilder(avaliable);

            if (avaliable > 0) {
                int data = in.read();
                char ch;
                while (data > -1) {
                    ch = (char) data;
                    if (ch != linebreak)
                        stringBuilder.append(ch);

                    data = in.read();
                }
            }

            setCommandLine(stringBuilder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reset, so a call to nextToken will retreive the first token.
     */
    public void reset() {
        pos = 0;
    }

    /**
     * Returns if there is another token on the command list.
     * 
     * @return <code>true</code> if there is another token; <code>false</code>
     *         otherwise
     */
    public boolean hasNext() {
        return pos < s.length();
    }

    /**
     * Go to the next token and return it.
     * 
     * @return the next token
     */
    public Token nextToken() throws NoSuchElementException {
        int start = pos;
        String token = next();
        int end = pos;
        return new Token(token, getTokenType(), start, end);
    }

    /**
     * Go to the next token string and return it.
     * 
     * @return the next token
     */
    public String next() throws NoSuchElementException {
        if (!hasNext())
            throw new NoSuchElementException();

        type = LITERAL;

        StringBuilder token = new StringBuilder(5);
        char currentChar;

        boolean finished = false;

        while (!finished && pos < s.length()) {
            currentChar = s.charAt(pos++);

            switch (currentChar) {
            case ESCAPE_CHAR:
            	if(pos >= s.length())
            	{
            		throw new IllegalArgumentException("escape char ('\\') not followed by a character");
            	}
            	
                token.append(CommandLine.unescape(s.charAt(pos++)));
                break;

            case FULL_ESCAPE_CHAR:
                if (inQuote) {
                    token.append(currentChar);
                } else {
                    inFullEscape = !inFullEscape; // just a toggle
                    type = STRING;
                    if (!inFullEscape)
                        type |= CLOSED;
                }
                break;
            case QUOTE_CHAR:
                if (inFullEscape) {
                    token.append(currentChar);
                } else {
                    inQuote = !inQuote;
                    type = STRING;
                    if (!inQuote)
                        type |= CLOSED;
                }
                break;
            case SPACE_CHAR:
                if (inFullEscape || inQuote) {
                    token.append(currentChar);
                } else {
                    if (token.length() != 0) { // don't return an empty token
                        finished = true;
                        pos--; // to return trailing space as empty last token
                    }
                }
                break;
            case COMMENT_CHAR:
                if (inFullEscape || inQuote) {
                    token.append(currentChar);
                } else {
                    finished = true;
                    pos = s.length(); // ignore EVERYTHING
                }
                break;
            /*
             * case SEND_OUTPUT_TO_CHAR: next(); break;
             */
            default:
                token.append(currentChar);
            }
        }

        String collectedToken = token.toString();
        token = null;

        return collectedToken;
    }

    /**
     * Gets the type of this token.
     * 
     * @return the type, <code>LITERAL</code> if it cannot be determined
     */
    public int getTokenType() {
        return type;
    }

    /**
     * Get the remaining CommandLine.
     * 
     * @return the remainder
     */
    public CommandLine getRemainder() {
        return new CommandLine(s.substring(pos));
    }

    /**
     * Get the entire command line as String[].
     * 
     * @return the command line as String[]
     */
    public String[] toStringArray() {
        final List<String> res = new ArrayList<String>();
        CommandLine line = new CommandLine(s);

        while (line.hasNext()) {
            res.add(line.next());
        }

        String[] result = (String[]) res.toArray(new String[res.size()]);
        return result;
    }

    /**
     * Returns the entire command line as a string.
     * 
     * @return the entire command line
     */
    public String toString() {
        return escape(toStringArray()); // perform all possible conversions
    }

    /**
     * Gets the next token without stepping through the list.
     * 
     * @return the next token, or an empty string if there are no tokens left
     */
    public String peek() {
        if (!hasNext()) {
            return "";
        }
        return getRemainder().next();
    }

    /**
     * Gets the remaining number of parts
     * 
     * @return the remaining number of parts
     */
    public int getLength() {
        if (!hasNext())
            return 0;

        CommandLine remainder = getRemainder();
        int result = 0;
        while (remainder.hasNext()) {
            result++;
            remainder.next();
        }
        return result;
    }

    public static class Token {
        public final String token;

        public final int tokenType;

        public final int start;

        public final int end;

        Token(String token, int type, int start, int end) {
            this.token = token;
            this.tokenType = type;
            this.start = start;
            this.end = end;
        }
    }

    // escape and unescape methods

    private static final Escape[] escapes = {
    // plain escaped
            new Escape(ESCAPE_CHAR, ESCAPE_CHAR), new Escape(ESCAPE_B, B),
            new Escape(ESCAPE_N, N), new Escape(ESCAPE_R, R),
            new Escape(ESCAPE_T, T),
            new Escape(FULL_ESCAPE_CHAR, FULL_ESCAPE_CHAR) };

    /**
     * Escape a single command line argument for the Shell. Same as calling
     * escape(arg, <code>false</code>)
     * 
     * @param arg
     *            the unescaped argument
     * @return the escaped argument
     */
    public static String escape(String arg) {
        return escape(arg, false); // don't force quotation
    }

    /**
     * Escape a single command line argument for the Shell.
     * 
     * @param arg
     *            the unescaped argument
     * @param forceQuote
     *            if <code>true</code>, forces the argument to be returned in
     *            quotes even if not necessary
     * @return the escaped argument
     */
    public static String escape(String arg, boolean forceQuote) {
        String s = null;
        StringBuilder stringBuilder = new StringBuilder(arg.length() > 0 ? 5
                : 0);

        // one-character escapes
        for (int i = 0; i < arg.length(); i++) {
            char c = arg.charAt(i);
            for (int j = 0; j < escapes.length; j++)
                if (escapes[j].plain == c) {
                    stringBuilder.append(ESCAPE_CHAR);
                    c = escapes[j].escaped;
                    break;
                }
            stringBuilder.append(c);
        }

        s = stringBuilder.toString();

        if (s.indexOf(QUOTE_CHAR) != -1) { // full escape needed
            stringBuilder.insert(0, FULL_ESCAPE_CHAR);
            stringBuilder.append(FULL_ESCAPE_CHAR);
            s = stringBuilder.toString();
        } else if (forceQuote || (s.indexOf(SPACE_CHAR) != -1)) { // normal
                                                                    // quote if
                                                                    // needed or
                                                                    // forced
            stringBuilder.insert(0, QUOTE_CHAR);
            stringBuilder.append(QUOTE_CHAR);
            s = stringBuilder.toString();
        }
        // debug output do show how escapes are translated
        // System.out.println();
        // System.out.println("escaped \"" + arg + "\" as \"" + s + "\"");
        return s;
    }

    /**
     * Escape a command line for the Shell.
     * 
     * @param args
     *            the unescaped command line
     * @return the escaped argument
     */
    public static String escape(String[] args) {
        StringBuilder stringBuilder = new StringBuilder(args.length > 0 ? 5 : 0);

        for (int i = 0; i < args.length; i++) {
            stringBuilder.append(escape(args[i])); // escape the argument
            if (i != args.length - 1)
                stringBuilder.append(SPACE_CHAR); // escape the argument
        }
        return stringBuilder.toString();
    }

    public boolean sendToOutFile() {
        return outFileName != null;
    }

    public String getOutFileName() {
        return outFileName;
    }

    void setOutFileName(String outFileName) {
        this.outFileName = outFileName;
    }

    /**
     * Unescape a single character
     */
    private static char unescape(char arg) {
        // one-character escapes
        for (int i = 0; i < escapes.length; i++) {
            Escape e = escapes[i];
            if (e.escaped == arg)
                return e.plain;
        }
        return arg;
    }

    private static class Escape {
        final char plain;

        final char escaped;

        Escape(char plain, char escaped) {
            this.plain = plain;
            this.escaped = escaped;
        }
    }

}
