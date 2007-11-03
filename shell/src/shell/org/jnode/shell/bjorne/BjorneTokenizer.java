package org.jnode.shell.bjorne;

import static org.jnode.shell.bjorne.BjorneToken.TOK_END_OF_LINE;
import static org.jnode.shell.bjorne.BjorneToken.TOK_END_OF_STREAM;
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

import org.jnode.shell.ShellFailureException;

public class BjorneTokenizer {

    private final char[] chars;

    private final int len;

    private int pos;

    private BjorneToken prev, current, next;

    private static final BjorneToken END_OF_STREAM = new BjorneToken(
            TOK_END_OF_STREAM);

    private static final BjorneToken END_OF_LINE = new BjorneToken(
            TOK_END_OF_LINE);

    private static final BjorneToken LPAREN = new BjorneToken(TOK_LPAREN);

    private static final BjorneToken RPAREN = new BjorneToken(TOK_RPAREN);

    private static final BjorneToken SEMI = new BjorneToken(TOK_SEMI);

    private static final BjorneToken DSEMI = new BjorneToken(TOK_DSEMI);

    private static final BjorneToken AMP = new BjorneToken(TOK_AMP);

    private static final BjorneToken AND_IF = new BjorneToken(TOK_AND_IF);

    private static final BjorneToken BAR = new BjorneToken(TOK_BAR);

    private static final BjorneToken OR_IF = new BjorneToken(TOK_OR_IF);

    private static final BjorneToken GREAT = new BjorneToken(TOK_GREAT);

    private static final BjorneToken DGREAT = new BjorneToken(TOK_DGREAT);

    private static final BjorneToken GREATAND = new BjorneToken(TOK_GREATAND);

    private static final BjorneToken LESS = new BjorneToken(TOK_LESS);

    private static final BjorneToken DLESS = new BjorneToken(TOK_DLESS);

    private static final BjorneToken DLESSDASH = new BjorneToken(TOK_DLESSDASH);

    private static final BjorneToken LESSAND = new BjorneToken(TOK_LESSAND);

    private static final BjorneToken LESSGREAT = new BjorneToken(TOK_LESSGREAT);

    private static final BjorneToken CLOBBER = new BjorneToken(TOK_CLOBBER);

    private static final BjorneToken FOR = new BjorneToken(TOK_FOR);

    private static final BjorneToken IN = new BjorneToken(TOK_IN);

    private static final BjorneToken WHILE = new BjorneToken(TOK_WHILE);

    private static final BjorneToken UNTIL = new BjorneToken(TOK_UNTIL);

    private static final BjorneToken DO = new BjorneToken(TOK_DO);

    private static final BjorneToken DONE = new BjorneToken(TOK_DONE);

    private static final BjorneToken IF = new BjorneToken(TOK_IF);

    private static final BjorneToken THEN = new BjorneToken(TOK_THEN);

    private static final BjorneToken ELSE = new BjorneToken(TOK_ELSE);

    private static final BjorneToken ELIF = new BjorneToken(TOK_ELIF);

    private static final BjorneToken FI = new BjorneToken(TOK_FI);

    private static final BjorneToken CASE = new BjorneToken(TOK_CASE);

    private static final BjorneToken ESAC = new BjorneToken(TOK_ESAC);

    private static final BjorneToken LBRACE = new BjorneToken(TOK_LBRACE);

    private static final BjorneToken RBRACE = new BjorneToken(TOK_RBRACE);

    private static final BjorneToken BANG = new BjorneToken(TOK_BANG);

    private static final int EOS = -1;

    private final boolean debug;

    public BjorneTokenizer(String text) {
        this(text, false);
    }

    public BjorneTokenizer(String text, boolean debug) {
        chars = text.toCharArray();
        len = chars.length;
        this.debug = debug;
    }

    public BjorneToken peek() {
        if (current == null) {
            current = advance();
        }
        if (debug) {
            System.err.println("peek() -> " + current);
        }
        return current;
    }

    public BjorneToken peek(int context) {
        BjorneToken res = reinterpret(peek(), context);
        if (debug) {
            System.err.println("--> " + res);
        }
        return res;
    }

    public boolean hasNext() {
        return peek().getTokenType() != TOK_END_OF_STREAM;
    }

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

    public BjorneToken next(int context) {
        BjorneToken res = reinterpret(next(), context);
        if (debug) {
            System.err.println("--> " + res);
        }
        return res;
    }

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
        switch (ch) {
        case EOS:
            return END_OF_STREAM;
        case '\n':
            return END_OF_LINE;
        case '#':
            while ((ch = nextCh()) != EOS) {
                if (ch == '\n') {
                    return END_OF_LINE;
                }
            }
            return END_OF_STREAM;
        case '(':
            return LPAREN;
        case ')':
            return RPAREN;
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

    private BjorneToken parseWord() {
        int quoteChar = 0;
        StringBuffer sb = new StringBuffer();
        int ch = prevCh();
        LOOP: while (true) {
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
            case '#':
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
                return new BjorneToken(TOK_IO_NUMBER, sb.toString());
            }
        }
        return new BjorneToken(TOK_WORD, sb.toString());
    }

    private BjorneToken parseOperator() {
        switch (prevCh()) {
        case '<':
            switch (peekCh()) {
            case '<':
                nextCh();
                if (peekCh() == '-') {
                    nextCh();
                    return DLESSDASH;
                }
                return DLESS;
            case '>':
                nextCh();
                return LESSGREAT;
            case '&':
                nextCh();
                return LESSAND;
            default:
                return LESS;
            }
        case '>':
            switch (peekCh()) {
            case '|':
                nextCh();
                return CLOBBER;
            case '>':
                nextCh();
                return DGREAT;
            case '&':
                nextCh();
                return GREATAND;
            default:
                return GREAT;
            }
        case ';':
            if (peekCh() == ';') {
                nextCh();
                return DSEMI;
            }
            return SEMI;
        case '&':
            if (peekCh() == '&') {
                nextCh();
                return AND_IF;
            }
            return AMP;
        case '|':
            if (peekCh() == '|') {
                nextCh();
                return OR_IF;
            }
            return BAR;
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
            BjorneToken tmp = toReservedWordToken(token.getText());
            if (tmp != null) {
                return tmp;
            }
            return token;
        }

        case RULE_5_CONTEXT:
            if (token.isName()) {
                return new BjorneToken(TOK_NAME, token.getText());
            }
            return token;

        case RULE_6_CONTEXT:
            if (token.getText().equals("in")) {
                return IN;
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
            return new BjorneToken(TOK_ASSIGNMENT, token.getText());

        case RULE_8_CONTEXT:
            BjorneToken tmp = toReservedWordToken(token.getText());
            if (tmp != null) {
                return tmp;
            }
            if (token.isName()) {
                return new BjorneToken(TOK_NAME, token.getText());
            }
            return reinterpret(token, RULE_7b_CONTEXT);

        default:
            return token;
        }
    }

    private BjorneToken toReservedWordToken(String str) {
        if (str.equals("for")) {
            return FOR;
        } else if (str.equals("while")) {
            return WHILE;
        } else if (str.equals("until")) {
            return UNTIL;
        } else if (str.equals("do")) {
            return DO;
        } else if (str.equals("done")) {
            return DONE;
        } else if (str.equals("if")) {
            return IF;
        } else if (str.equals("then")) {
            return THEN;
        } else if (str.equals("else")) {
            return ELSE;
        } else if (str.equals("elif")) {
            return ELIF;
        } else if (str.equals("fi")) {
            return FI;
        } else if (str.equals("case")) {
            return CASE;
        } else if (str.equals("esac")) {
            return ESAC;
        } else if (str.equals("{")) {
            return LBRACE;
        } else if (str.equals("}")) {
            return RBRACE;
        } else if (str.equals("!")) {
            return BANG;
        }
        return null;
    }
}
