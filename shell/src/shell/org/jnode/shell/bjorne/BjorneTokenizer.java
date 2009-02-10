/*
 * $Id$
 *
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
 
package org.jnode.shell.bjorne;

import static org.jnode.shell.bjorne.BjorneToken.RULE_1_CONTEXT;
import static org.jnode.shell.bjorne.BjorneToken.RULE_5_CONTEXT;
import static org.jnode.shell.bjorne.BjorneToken.RULE_6_CONTEXT;
import static org.jnode.shell.bjorne.BjorneToken.RULE_7a_CONTEXT;
import static org.jnode.shell.bjorne.BjorneToken.RULE_7b_CONTEXT;
import static org.jnode.shell.bjorne.BjorneToken.RULE_8_CONTEXT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_AMP;
import static org.jnode.shell.bjorne.BjorneToken.TOK_AND_IF;
import static org.jnode.shell.bjorne.BjorneToken.TOK_ASSIGNMENT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_BANG;
import static org.jnode.shell.bjorne.BjorneToken.TOK_BAR;
import static org.jnode.shell.bjorne.BjorneToken.TOK_CASE;
import static org.jnode.shell.bjorne.BjorneToken.TOK_CLOBBER;
import static org.jnode.shell.bjorne.BjorneToken.TOK_DGREAT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_DLESS;
import static org.jnode.shell.bjorne.BjorneToken.TOK_DLESSDASH;
import static org.jnode.shell.bjorne.BjorneToken.TOK_DO;
import static org.jnode.shell.bjorne.BjorneToken.TOK_DONE;
import static org.jnode.shell.bjorne.BjorneToken.TOK_DSEMI;
import static org.jnode.shell.bjorne.BjorneToken.TOK_ELIF;
import static org.jnode.shell.bjorne.BjorneToken.TOK_ELSE;
import static org.jnode.shell.bjorne.BjorneToken.TOK_END_OF_LINE;
import static org.jnode.shell.bjorne.BjorneToken.TOK_END_OF_STREAM;
import static org.jnode.shell.bjorne.BjorneToken.TOK_ESAC;
import static org.jnode.shell.bjorne.BjorneToken.TOK_FI;
import static org.jnode.shell.bjorne.BjorneToken.TOK_FOR;
import static org.jnode.shell.bjorne.BjorneToken.TOK_GREAT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_GREATAND;
import static org.jnode.shell.bjorne.BjorneToken.TOK_IF;
import static org.jnode.shell.bjorne.BjorneToken.TOK_IN;
import static org.jnode.shell.bjorne.BjorneToken.TOK_IO_NUMBER;
import static org.jnode.shell.bjorne.BjorneToken.TOK_LBRACE;
import static org.jnode.shell.bjorne.BjorneToken.TOK_LESS;
import static org.jnode.shell.bjorne.BjorneToken.TOK_LESSAND;
import static org.jnode.shell.bjorne.BjorneToken.TOK_LESSGREAT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_LPAREN;
import static org.jnode.shell.bjorne.BjorneToken.TOK_NAME;
import static org.jnode.shell.bjorne.BjorneToken.TOK_OR_IF;
import static org.jnode.shell.bjorne.BjorneToken.TOK_RBRACE;
import static org.jnode.shell.bjorne.BjorneToken.TOK_RPAREN;
import static org.jnode.shell.bjorne.BjorneToken.TOK_SEMI;
import static org.jnode.shell.bjorne.BjorneToken.TOK_THEN;
import static org.jnode.shell.bjorne.BjorneToken.TOK_UNTIL;
import static org.jnode.shell.bjorne.BjorneToken.TOK_WHILE;
import static org.jnode.shell.bjorne.BjorneToken.TOK_WORD;

import org.jnode.shell.IncompleteCommandException;
import org.jnode.shell.ShellFailureException;

public class BjorneTokenizer {

    private final char[] chars;

    private final int len;

    private int pos;

    private BjorneToken prev, current, next;


    private static final int EOS = -1;

    private final boolean debug;

    /**
     * Create a tokenizer for the supplied shell input text.
     * @param text the text to be tokenized
     * @throws IncompleteCommandException if the text ends with a line continuation
     */
    public BjorneTokenizer(String text) throws IncompleteCommandException {
        this(text, false);
    }

    /**
     * Create a tokenizer for the supplied shell input text.
     * @param text the text to be tokenized
     * @param debug if {@code true}, produce debug output
     * @throws IncompleteCommandException if the text ends with a line continuation
     */
    public BjorneTokenizer(String text, boolean debug) throws IncompleteCommandException {
        chars = foldContinuations(text);
        len = chars.length;
        this.debug = debug;
    }

    /**
     * Rewrite the supplied text to fold any line continuations.
     * 
     * @param text the text to be processed
     * @return the characters of text with any line continuations removed.
     * @throws IncompleteCommandException
     */
    private char[] foldContinuations(String text) throws IncompleteCommandException {
        if (text.indexOf('\\') == -1) {
            return text.toCharArray();
        }
        int len = text.length();
        StringBuilder sb = new StringBuilder(len);
        boolean escape = false;
        for (int i = 0; i < len; i++) {
            char ch = text.charAt(i);
            switch (ch) {
                case '\\':
                    if (escape) {
                        sb.append('\\');
                    } else if (i == len - 1) {
                        // If we get a continuation sequence at the end of the
                        // text, the simplest thing is to ask for more input.
                        throw new IncompleteCommandException(
                                "More input required after '\\<newline>'", " > ");
                    }
                    escape = !escape;
                    break;
                case '\n':
                    if (!escape) {
                        sb.append('\n');
                    } else {
                        escape = false;
                    }
                    break;
                default:
                    if (escape) {
                        sb.append('\\');
                        escape = false;
                    }
                    sb.append(ch);
            }
        }
        return sb.toString().toCharArray();
    }

    /**
     * Get the next token without advancing.  The default tokenization
     * rules are used.
     * 
     * @return the next token
     */
    public BjorneToken peek() {
        if (current == null) {
            current = advance();
        }
        if (debug) {
            System.err.println("peek() -> " + current);
        }
        return current;
    }

    /**
     * Get the next token without advancing, using the tokenization
     * rules corresponding to the supplied 'context'.
     * 
     * @param context gives the tokenization rules
     * @return the next token
     */
    public BjorneToken peek(int context) {
        BjorneToken res = reinterpret(peek(), context);
        if (debug) {
            System.err.println("--> " + res);
        }
        return res;
    }

    /**
     * Test if {@link #next()} will return something other that EOS.
     * @return <code>true</code> if there are more tokens to be delivered.
     */
    public boolean hasNext() {
        return peek().getTokenType() != TOK_END_OF_STREAM;
    }

    /**
     * Get the next token and advance.  The default tokenization
     * rules are used.
     * 
     * @return the next token
     */
    public BjorneToken next() {
        if (current == null) {
            prev = advance();
        } else {
            prev = current;
            current = next;
            next = null;
        }
        if (debug) {
            System.err.println("next() -> " + prev);
        }
        return prev;
    }

    /**
     * Backup one token in the token sequence.  Calling this method twice without
     * an intervening {@link #next()} call is invalid.
     */
    public void backup() {
        if (prev == null) {
            throw new ShellFailureException("incorrect backup");
        }
        if (debug) {
            System.err.println("backup() ... {" + prev + "," + current + ","
                    + next + "}");
        }
        next = current;
        current = prev;
        prev = null;
    }

    /**
     * Get the next token and advance, using the tokenization
     * rules corresponding to the supplied 'context'.
     * 
     * @param context gives the tokenization rules
     * @return the next token
     */
    public BjorneToken next(int context) {
        BjorneToken res = reinterpret(next(), context);
        if (debug) {
            System.err.println("--> " + res);
        }
        return res;
    }

    /**
     * This operation is not supported.
     */
    public void remove() {
        throw new UnsupportedOperationException("remove not supported");
    }

    /**
     * Parse and return the next token
     * 
     * @return
     */
    private BjorneToken advance() {
        if (debug) {
            System.err.print("advance() ... {" + prev + "," + current + ","
                    + next + "} ...");
        }
        int ch = nextCh();
        while (ch == '\t' || ch == ' ') {
            ch = nextCh();
        }
        int start = pos - 1;
        switch (ch) {
            case EOS:
                return makeToken(TOK_END_OF_STREAM, start);
            case '\n':
                return makeToken(TOK_END_OF_LINE, start);
            case '#':
                while ((ch = nextCh()) != EOS) {
                    if (ch == '\n') {
                        return makeToken(TOK_END_OF_LINE, start);
                    }
                }
                return makeToken(TOK_END_OF_STREAM, start);
            case '(':
                return makeToken(TOK_LPAREN, start);
            case ')':
                return makeToken(TOK_RPAREN, start);
            case '<':
            case '>':
            case ';':
            case '&':
            case '|':
                return parseOperator();
            default:
                return parseWord();
        }
    }

    private BjorneToken makeToken(int tokenType, int start) {
        return new BjorneToken(tokenType, "", start, pos);
    }

    private BjorneToken makeToken(int tokenType, String value, int start) {
        return new BjorneToken(tokenType, value, start, pos);
    }

    private BjorneToken parseWord() {
        int quoteChar = 0;
        StringBuffer sb = new StringBuffer();
        int ch = prevCh();
        int start = pos - 1;
    LOOP: 
        while (true) {
            switch (ch) {
                case EOS:
                case '\n':
                    break LOOP;
                case '(':
                case ')':
                case '<':
                case '>':
                case ';':
                case '&':
                case '|':
                case ' ':
                case '\t':
                    if (quoteChar == 0) {
                        break LOOP;
                    }
                    break;
                case '"':
                case '\'':
                case '`':
                    if (quoteChar == 0) {
                        quoteChar = ch;
                    } else if (quoteChar == ch) {
                        quoteChar = 0;
                    }
                    break;
                case '\\':
                    ch = nextCh();
                    if (ch == '\n') {
                        ch = nextCh();
                        continue;
                    } else {
                        sb.append('\\');
                        if (ch == EOS) {
                            break LOOP;
                        }
                    }
                    break;
                default:
                    /* empty */
                    break;
            }
            sb.append((char) ch);
            ch = nextCh();
        }
        if (ch != EOS) {
            backupCh();
        }
        if (ch == '<' || ch == '>') {
            boolean allDigits = true;
            for (int i = 0; i < sb.length(); i++) {
                ch = sb.charAt(i);
                if (ch < '0' || ch > '9') {
                    allDigits = false;
                    break;
                }
            }
            if (allDigits) {
                return makeToken(TOK_IO_NUMBER, sb.toString(), start);
            }
        }
        return makeToken(TOK_WORD, sb.toString(), start);
    }

    private BjorneToken parseOperator() {
        int start = pos - 1;
        switch (prevCh()) {
            case '<':
                switch (peekCh()) {
                    case '<':
                        nextCh();
                        if (peekCh() == '-') {
                            nextCh();
                            return makeToken(TOK_DLESSDASH, start);
                        }
                        return makeToken(TOK_DLESS, start);
                    case '>':
                        nextCh();
                        return makeToken(TOK_LESSGREAT, start);
                    case '&':
                        nextCh();
                        return makeToken(TOK_LESSAND, start);
                    default:
                        return makeToken(TOK_LESS, start);
                }
            case '>':
                switch (peekCh()) {
                    case '|':
                        nextCh();
                        return makeToken(TOK_CLOBBER, start);
                    case '>':
                        nextCh();
                        return makeToken(TOK_DGREAT, start);
                    case '&':
                        nextCh();
                        return makeToken(TOK_GREATAND, start);
                    default:
                        return makeToken(TOK_GREAT, start);
                }
            case ';':
                if (peekCh() == ';') {
                    nextCh();
                    return makeToken(TOK_DSEMI, start);
                }
                return makeToken(TOK_SEMI, start);
            case '&':
                if (peekCh() == '&') {
                    nextCh();
                    return makeToken(TOK_AND_IF, start);
                }
                return makeToken(TOK_AMP, start);
            case '|':
                if (peekCh() == '|') {
                    nextCh();
                    return makeToken(TOK_OR_IF, start);
                }
                return makeToken(TOK_BAR, start);
        }
        throw new ShellFailureException("bad lexer state");
    }

    private int nextCh() {
        return (pos >= len) ? EOS : chars[pos++];
    }

    private int peekCh() {
        return (pos >= len) ? EOS : chars[pos];
    }

    private int prevCh() {
        if (pos <= 0) {
            throw new ShellFailureException("nextCh not called yet");
        }
        return chars[pos - 1];
    }

    private void backupCh() {
        if (pos == 0) {
            throw new ShellFailureException("cannot backup");
        }
        pos--;
    }

    /**
     * Reinterpret a token according to the context-sensitive tokenization rule
     * for a given context. WORD tokens may be mapped to reserved words, NAME or
     * ASSIGNMENT. Other tokens are left alone.
     * 
     * @param token
     * @param context
     * @return
     */
    private BjorneToken reinterpret(BjorneToken token, int context) {
        if (token.getTokenType() != TOK_WORD) {
            return token;
        }
        switch (context) {
            case RULE_1_CONTEXT: {
                BjorneToken tmp = toReservedWordToken(token);
                if (tmp != null) {
                    return tmp;
                }
                return token;
            }

            case RULE_5_CONTEXT:
                if (token.isName()) {
                    return remakeToken(TOK_NAME, token);
                }
                return token;

            case RULE_6_CONTEXT:
                if (token.getText().equals("in")) {
                    return remakeToken(TOK_IN, token);
                }
                return token;

            case RULE_7a_CONTEXT:
                if (token.getText().indexOf('=') == -1) {
                    return reinterpret(token, RULE_1_CONTEXT);
                }
                // DROP THROUGH TO RULE 7b

            case RULE_7b_CONTEXT:
                int pos = token.getText().indexOf('=');
                if (pos <= 0
                        || !BjorneToken.isName(token.getText()
                                .substring(0, pos - 1))) {
                    return token;
                }
                return remakeToken(TOK_ASSIGNMENT, token);

            case RULE_8_CONTEXT:
                BjorneToken tmp = toReservedWordToken(token);
                if (tmp != null) {
                    return tmp;
                }
                if (token.isName()) {
                    return remakeToken(TOK_NAME, token);
                }
                return reinterpret(token, RULE_7b_CONTEXT);

            default:
                return token;
        }
    }

    private BjorneToken remakeToken(int tokenType, BjorneToken token) {
        return new BjorneToken(tokenType, token.getText(), token.start,
                token.end);
    }

    private BjorneToken toReservedWordToken(BjorneToken token) {
        String str = token.getText();
        if (str.equals("for")) {
            return remakeToken(TOK_FOR, token);
        } else if (str.equals("while")) {
            return remakeToken(TOK_WHILE, token);
        } else if (str.equals("until")) {
            return remakeToken(TOK_UNTIL, token);
        } else if (str.equals("do")) {
            return remakeToken(TOK_DO, token);
        } else if (str.equals("done")) {
            return remakeToken(TOK_DONE, token);
        } else if (str.equals("if")) {
            return remakeToken(TOK_IF, token);
        } else if (str.equals("then")) {
            return remakeToken(TOK_THEN, token);
        } else if (str.equals("else")) {
            return remakeToken(TOK_ELSE, token);
        } else if (str.equals("elif")) {
            return remakeToken(TOK_ELIF, token);
        } else if (str.equals("fi")) {
            return remakeToken(TOK_FI, token);
        } else if (str.equals("case")) {
            return remakeToken(TOK_CASE, token);
        } else if (str.equals("esac")) {
            return remakeToken(TOK_ESAC, token);
        } else if (str.equals("{")) {
            return remakeToken(TOK_LBRACE, token);
        } else if (str.equals("}")) {
            return remakeToken(TOK_RBRACE, token);
        } else if (str.equals("!")) {
            return remakeToken(TOK_BANG, token);
        }
        return null;
    }
}
