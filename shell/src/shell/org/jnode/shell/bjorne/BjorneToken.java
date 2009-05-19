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

import org.jnode.shell.CommandLine;

public class BjorneToken extends CommandLine.Token {

    public static final int TOK_END_OF_STREAM = 1;
    public static final long TOK_END_OF_STREAM_BIT = (1 << TOK_END_OF_STREAM);

    public static final int TOK_END_OF_LINE = 2;
    public static final long TOK_END_OF_LINE_BIT = (1 << TOK_END_OF_LINE);

    // See The Open Group Base Specifications Issue 6
    // IEEE Std 1003.1, 2004 Edition
    // Chapter 2 Section 2.10.2
    public static final int TOK_SEMI = 3; // ;
    public static final long TOK_SEMI_BIT = (1L << TOK_SEMI);

    public static final int TOK_AMP = 4; // &
    public static final long TOK_AMP_BIT = (1L << TOK_AMP);

    public static final int TOK_BAR = 5; // |
    public static final long TOK_BAR_BIT = (1L << TOK_BAR);

    public static final int TOK_LESS = 6; // <
    public static final long TOK_LESS_BIT = (1L << TOK_LESS);

    public static final int TOK_GREAT = 7; // >
    public static final long TOK_GREAT_BIT = (1L << TOK_GREAT);

    public static final int TOK_LPAREN = 8; // (
    public static final long TOK_LPAREN_BIT = (1L << TOK_LPAREN);

    public static final int TOK_RPAREN = 9; // )
    public static final long TOK_RPAREN_BIT = (1L << TOK_RPAREN);

    public static final int TOK_LBRACE = 10; // { reserved word
    public static final long TOK_LBRACE_BIT = (1L << TOK_LBRACE);

    public static final int TOK_RBRACE = 11; // } reserved word
    public static final long TOK_RBRACE_BIT = (1L << TOK_RBRACE);

    public static final int TOK_BANG = 12; // ! reserved word
    public static final long TOK_BANG_BIT = (1L << TOK_BANG);

    public static final int TOK_AND_IF = 13; // &&
    public static final long TOK_AND_IF_BIT = (1L << TOK_AND_IF);

    public static final int TOK_OR_IF = 14; // ||
    public static final long TOK_OR_IF_BIT = (1L << TOK_OR_IF);

    public static final int TOK_DSEMI = 15; // ;;
    public static final long TOK_DSEMI_BIT = (1L << TOK_DSEMI);

    public static final int TOK_DLESS = 16; // <<
    public static final long TOK_DLESS_BIT = (1L << TOK_DLESS);

    public static final int TOK_DGREAT = 17; // >>
    public static final long TOK_DGREAT_BIT = (1L << TOK_DGREAT);

    public static final int TOK_LESSAND = 18; // <&
    public static final long TOK_LESSAND_BIT = (1L << TOK_LESSAND);

    public static final int TOK_GREATAND = 19; // >&
    public static final long TOK_GREATAND_BIT = (1L << TOK_GREATAND);

    public static final int TOK_LESSGREAT = 20; // <>
    public static final long TOK_LESSGREAT_BIT = (1L << TOK_LESSGREAT);

    public static final int TOK_DLESSDASH = 21; // <<-
    public static final long TOK_DLESSDASH_BIT = (1L << TOK_DLESSDASH);

    public static final int TOK_CLOBBER = 22; // >|
    public static final long TOK_CLOBBER_BIT = (1L << TOK_CLOBBER);

    public static final int TOK_WORD = 23;
    public static final long TOK_WORD_BIT = (1L << TOK_WORD);

    public static final int TOK_NAME = 24;
    public static final long TOK_NAME_BIT = (1L << TOK_NAME);

    public static final int TOK_ASSIGNMENT = 25;
    public static final long TOK_ASSIGNMENT_BIT = (1L << TOK_ASSIGNMENT);

    public static final int TOK_IO_NUMBER = 26;
    public static final long TOK_IO_NUMBER_BIT = (1L << TOK_IO_NUMBER);

    public static final int TOK_IF = 27;
    public static final long TOK_IF_BIT = (1L << TOK_IF);

    public static final int TOK_THEN = 28;
    public static final long TOK_THEN_BIT = (1L << TOK_THEN);

    public static final int TOK_ELSE = 29;
    public static final long TOK_ELSE_BIT = (1L << TOK_ELSE);

    public static final int TOK_ELIF = 30;
    public static final long TOK_ELIF_BIT = (1L << TOK_ELIF);

    public static final int TOK_FI = 31;
    public static final long TOK_FI_BIT = (1L << TOK_FI);

    public static final int TOK_DO = 32;
    public static final long TOK_DO_BIT = (1L << TOK_DO);

    public static final int TOK_DONE = 33;
    public static final long TOK_DONE_BIT = (1L << TOK_DONE);

    public static final int TOK_CASE = 34;
    public static final long TOK_CASE_BIT = (1L << TOK_CASE);

    public static final int TOK_ESAC = 35;
    public static final long TOK_ESAC_BIT = (1L << TOK_ESAC);

    public static final int TOK_WHILE = 36;
    public static final long TOK_WHILE_BIT = (1L << TOK_WHILE);

    public static final int TOK_UNTIL = 37;
    public static final long TOK_UNTIL_BIT = (1L << TOK_UNTIL);

    public static final int TOK_FOR = 38;
    public static final long TOK_FOR_BIT = (1L << TOK_FOR);

    public static final int TOK_IN = 39;
    public static final long TOK_IN_BIT = (1L << TOK_IN);

    public static final int BASE_CONTEXT = 0;

    public static final int RULE_1_CONTEXT = 1;

    public static final int RULE_5_CONTEXT = 5;

    public static final int RULE_6_CONTEXT = 6;

    public static final int RULE_7a_CONTEXT = 7;

    public static final int RULE_7b_CONTEXT = 8;

    public static final int RULE_8_CONTEXT = 9;

    /*
     * We don't need / use use rules 2, 3 or 9 in a call to next(..) or peek(..)
     * because the tokenizer doesn't perform expansion or assignment.
     */

    public BjorneToken(final int tokenType, final String text, int start, int end) {
        super(text == null ? "" : text, tokenType, start, end);
        validate();
    }
    
    public BjorneToken(final String text) {
        super(text == null ? "" : text, TOK_WORD, 0, 0);
        validate();
    }
    
    public BjorneToken remake(CharSequence newText) {
        if (newText.length() == 0) {
            return null;
        } else {
            return new BjorneToken(this.tokenType, newText.toString(), this.start, this.end);
        }
    }

    private void validate() {
//        switch (tokenType) {
//            case TOK_WORD:
//            case TOK_IO_NUMBER:
//            case TOK_NAME:
//            case TOK_ASSIGNMENT:
//                if (text == null || text.length() == 0) {
//                    throw new IllegalArgumentException("null or empty text");
//                }
//                break;
//
//            default:
//                if (text != null && text.length() > 0) {
//                    throw new IllegalArgumentException("non-empty text");
//                }
//        }
    }

    public String getText() {
        return text;
    }

    public int getTokenType() {
        return tokenType;
    }

    public boolean isName() {
        return text != null && isName(text);
    }

    public static boolean isName(String str) {
        int len = str.length();
        if (len == 0) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            char ch = str.charAt(i);
            switch (ch) {
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                case 'Z':
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                case 'g':
                case 'h':
                case 'i':
                case 'j':
                case 'k':
                case 'l':
                case 'm':
                case 'n':
                case 'o':
                case 'p':
                case 'q':
                case 'r':
                case 's':
                case 't':
                case 'u':
                case 'v':
                case 'w':
                case 'x':
                case 'y':
                case 'z':
                case '_':
                    break;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    if (i == 0) {
                        return false;
                    }
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    public String toString() {
        switch (tokenType) {
            case TOK_WORD:
                return "WORD{" + text + "}";
            case TOK_NAME:
                return "NAME{" + text + "}";
            case TOK_ASSIGNMENT:
                return "ASSIGNMENT{" + text + "}";
            case TOK_IO_NUMBER:
                return "IO_NUMBER{" + text + "}";
            default:
                String str = toString(tokenType);
                return str == null ? "?UNKNOWN?" : str;
        }
    }

    public static String toString(int tt) {
        switch (tt) {
            case TOK_SEMI:
                return ";";
            case TOK_AMP:
                return "&";
            case TOK_BAR:
                return "|";
            case TOK_LESS:
                return "<";
            case TOK_GREAT:
                return ">";
            case TOK_LPAREN:
                return "(";
            case TOK_RPAREN:
                return ")";
            case TOK_LBRACE:
                return "{";
            case TOK_RBRACE:
                return "}";
            case TOK_BANG:
                return "!";
            case TOK_AND_IF:
                return "&&";
            case TOK_OR_IF:
                return "||";
            case TOK_DSEMI:
                return ";;";
            case TOK_DLESS:
                return "<<";
            case TOK_DGREAT:
                return ">>";
            case TOK_LESSAND:
                return "<&";
            case TOK_GREATAND:
                return ">&";
            case TOK_LESSGREAT:
                return "<>";
            case TOK_DLESSDASH:
                return "<<-";
            case TOK_CLOBBER:
                return "|>";
            case TOK_IF:
                return "if";
            case TOK_THEN:
                return "then";
            case TOK_ELSE:
                return "else";
            case TOK_ELIF:
                return "elif";
            case TOK_FI:
                return "fi";
            case TOK_DO:
                return "do";
            case TOK_DONE:
                return "done";
            case TOK_CASE:
                return "case";
            case TOK_ESAC:
                return "esac";
            case TOK_WHILE:
                return "while";
            case TOK_UNTIL:
                return "until";
            case TOK_FOR:
                return "for";
            case TOK_IN:
                return "in";
            case TOK_END_OF_LINE:
                return "end of line";
            case TOK_END_OF_STREAM:
                return "end of file";
            case TOK_WORD:
                return "<word>";
            case TOK_NAME:
                return "<name>";
            case TOK_ASSIGNMENT:
                return "<assignment>";
            case TOK_IO_NUMBER:
                return "<io number>";
            default:
                return "unknown (" + tt + ")";
        }
    }
}
