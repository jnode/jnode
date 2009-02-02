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
 
package org.jnode.test.shell.bjorne;

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
import junit.framework.TestCase;

import org.jnode.shell.bjorne.BjorneToken;
import org.jnode.shell.bjorne.BjorneTokenizer;

public class BjorneTokenizerTests extends TestCase {

    public void testBjorneTokenizer() {
        new BjorneTokenizer("hello");
    }

    public void testEmpty() {
        BjorneTokenizer tokenizer = new BjorneTokenizer("");
        BjorneToken token = tokenizer.peek();
        assertEquals(TOK_END_OF_STREAM, token.getTokenType());
        token = tokenizer.peek(RULE_1_CONTEXT);
        assertEquals(TOK_END_OF_STREAM, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_END_OF_STREAM, token.getTokenType());
        token = tokenizer.next(RULE_1_CONTEXT);
        assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }

    public void testNewline() {
        BjorneTokenizer tokenizer = new BjorneTokenizer("\n");
        BjorneToken token = tokenizer.next();
        assertEquals(TOK_END_OF_LINE, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }

    public void testBlanksAndNewlines() {
        BjorneTokenizer tokenizer = new BjorneTokenizer("  \n\t\n  ");
        BjorneToken token = tokenizer.next();
        assertEquals(TOK_END_OF_LINE, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_END_OF_LINE, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }

    public void testComments() {
        BjorneTokenizer tokenizer = new BjorneTokenizer(
                "# comment\n  #comment 2\n # comment # 3");
        BjorneToken token = tokenizer.next();
        assertEquals(TOK_END_OF_LINE, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_END_OF_LINE, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }

    public void testSymbols() {
        BjorneTokenizer tokenizer = new BjorneTokenizer("; | & < > ( )");
        BjorneToken token = tokenizer.next();
        assertEquals(TOK_SEMI, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_BAR, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_AMP, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_LESS, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_GREAT, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_LPAREN, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_RPAREN, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }

    public void testSymbols2() {
        BjorneTokenizer tokenizer = new BjorneTokenizer(
                "; ;; | || & && < << > >>");
        BjorneToken token = tokenizer.next();
        assertEquals(TOK_SEMI, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_DSEMI, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_BAR, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_OR_IF, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_AMP, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_AND_IF, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_LESS, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_DLESS, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_GREAT, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_DGREAT, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }

    public void testSymbols3() {
        BjorneTokenizer tokenizer = new BjorneTokenizer(";;;|||&&&<<<>>>");
        BjorneToken token = tokenizer.next();
        assertEquals(TOK_DSEMI, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_SEMI, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_OR_IF, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_BAR, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_AND_IF, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_AMP, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_DLESS, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_LESSGREAT, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_DGREAT, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }

    public void testSymbols4() {
        BjorneTokenizer tokenizer = new BjorneTokenizer(
                "< << <<- <& <> > >> >| >&");
        BjorneToken token = tokenizer.next();
        assertEquals(TOK_LESS, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_DLESS, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_DLESSDASH, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_LESSAND, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_LESSGREAT, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_GREAT, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_DGREAT, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_CLOBBER, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_GREATAND, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }

    public void testWords() {
        BjorneTokenizer tokenizer = new BjorneTokenizer("hello there");
        BjorneToken token = tokenizer.next();
        assertEquals(TOK_WORD, token.getTokenType());
        assertEquals("hello", token.getText());
        token = tokenizer.next();
        assertEquals(TOK_WORD, token.getTokenType());
        assertEquals("there", token.getText());
        token = tokenizer.next();
        assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }

    public void testWords2() {
        BjorneTokenizer tokenizer = new BjorneTokenizer(
                "hello\\ there\\\n friend");
        BjorneToken token = tokenizer.next();
        assertEquals(TOK_WORD, token.getTokenType());
        assertEquals("hello\\ there", token.getText());
        token = tokenizer.next();
        assertEquals(TOK_WORD, token.getTokenType());
        assertEquals("friend", token.getText());
        token = tokenizer.next();
        assertEquals(TOK_END_OF_STREAM, token.getTokenType());

        tokenizer = new BjorneTokenizer("hello\\ there\\\n\\ friend");
        token = tokenizer.next();
        assertEquals(TOK_WORD, token.getTokenType());
        assertEquals("hello\\ there\\ friend", token.getText());
        token = tokenizer.next();
        assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }

    public void testWords3() {
        BjorneTokenizer tokenizer = new BjorneTokenizer("'1 2' \"3 4\" `5 6`");
        BjorneToken token = tokenizer.next();
        assertEquals(TOK_WORD, token.getTokenType());
        assertEquals("'1 2'", token.getText());
        token = tokenizer.next();
        assertEquals(TOK_WORD, token.getTokenType());
        assertEquals("\"3 4\"", token.getText());
        token = tokenizer.next();
        assertEquals(TOK_WORD, token.getTokenType());
        assertEquals("`5 6`", token.getText());
        token = tokenizer.next();
        assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }

    public void testWords4() {
        BjorneTokenizer tokenizer = new BjorneTokenizer("'1 \"2\"' \"3\\\"4\"");
        BjorneToken token = tokenizer.next();
        assertEquals(TOK_WORD, token.getTokenType());
        assertEquals("'1 \"2\"'", token.getText());
        token = tokenizer.next();
        assertEquals(TOK_WORD, token.getTokenType());
        assertEquals("\"3\\\"4\"", token.getText());
        token = tokenizer.next();
        assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }

    public void testWords5() {
        BjorneTokenizer tokenizer = new BjorneTokenizer("1<2>3&4;5|6)7");
        BjorneToken token = tokenizer.next();
        assertEquals(TOK_IO_NUMBER, token.getTokenType());
        assertEquals("1", token.getText());
        token = tokenizer.next();
        assertEquals(TOK_LESS, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_IO_NUMBER, token.getTokenType());
        assertEquals("2", token.getText());
        token = tokenizer.next();
        assertEquals(TOK_GREAT, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_WORD, token.getTokenType());
        assertEquals("3", token.getText());
        token = tokenizer.next();
        assertEquals(TOK_AMP, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_WORD, token.getTokenType());
        assertEquals("4", token.getText());
        token = tokenizer.next();
        assertEquals(TOK_SEMI, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_WORD, token.getTokenType());
        assertEquals("5", token.getText());
        token = tokenizer.next();
        assertEquals(TOK_BAR, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_WORD, token.getTokenType());
        assertEquals("6", token.getText());
        token = tokenizer.next();
        assertEquals(TOK_RPAREN, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_WORD, token.getTokenType());
        assertEquals("7", token.getText());
        token = tokenizer.next();
        assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }

    public void testRule1() {
        BjorneTokenizer tokenizer = new BjorneTokenizer(
                "if then else elif fi for done while until "
                        + "case { } ! do in esac");
        BjorneToken token = tokenizer.next(RULE_1_CONTEXT);
        assertEquals(TOK_IF, token.getTokenType());
        token = tokenizer.next(RULE_1_CONTEXT);
        assertEquals(TOK_THEN, token.getTokenType());
        token = tokenizer.next(RULE_1_CONTEXT);
        assertEquals(TOK_ELSE, token.getTokenType());
        token = tokenizer.next(RULE_1_CONTEXT);
        assertEquals(TOK_ELIF, token.getTokenType());
        token = tokenizer.next(RULE_1_CONTEXT);
        assertEquals(TOK_FI, token.getTokenType());
        token = tokenizer.next(RULE_1_CONTEXT);
        assertEquals(TOK_FOR, token.getTokenType());
        token = tokenizer.next(RULE_1_CONTEXT);
        assertEquals(TOK_DONE, token.getTokenType());
        token = tokenizer.next(RULE_1_CONTEXT);
        assertEquals(TOK_WHILE, token.getTokenType());
        token = tokenizer.next(RULE_1_CONTEXT);
        assertEquals(TOK_UNTIL, token.getTokenType());
        token = tokenizer.next(RULE_1_CONTEXT);
        assertEquals(TOK_CASE, token.getTokenType());
        token = tokenizer.next(RULE_1_CONTEXT);
        assertEquals(TOK_LBRACE, token.getTokenType());
        token = tokenizer.next(RULE_1_CONTEXT);
        assertEquals(TOK_RBRACE, token.getTokenType());
        token = tokenizer.next(RULE_1_CONTEXT);
        assertEquals(TOK_BANG, token.getTokenType());
        token = tokenizer.next(RULE_1_CONTEXT);
        assertEquals(TOK_DO, token.getTokenType());
        token = tokenizer.next(RULE_1_CONTEXT);
        assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_1_CONTEXT);
        assertEquals(TOK_ESAC, token.getTokenType());
        token = tokenizer.next(RULE_1_CONTEXT);
        assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }

    public void testRule5() {
        BjorneTokenizer tokenizer = new BjorneTokenizer(
                "if a a1 9a a_b a,b AB A=b");
        BjorneToken token = tokenizer.next(RULE_5_CONTEXT);
        assertEquals(TOK_NAME, token.getTokenType());
        token = tokenizer.next(RULE_5_CONTEXT);
        assertEquals(TOK_NAME, token.getTokenType());
        token = tokenizer.next(RULE_5_CONTEXT);
        assertEquals(TOK_NAME, token.getTokenType());
        token = tokenizer.next(RULE_5_CONTEXT);
        assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_5_CONTEXT);
        assertEquals(TOK_NAME, token.getTokenType());
        token = tokenizer.next(RULE_5_CONTEXT);
        assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_5_CONTEXT);
        assertEquals(TOK_NAME, token.getTokenType());
        token = tokenizer.next(RULE_5_CONTEXT);
        assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_5_CONTEXT);
        assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }

    public void testRule6() {
        BjorneTokenizer tokenizer = new BjorneTokenizer("if in do");
        BjorneToken token = tokenizer.next(RULE_6_CONTEXT);
        assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_6_CONTEXT);
        assertEquals(TOK_IN, token.getTokenType());
        token = tokenizer.next(RULE_6_CONTEXT);
        assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_6_CONTEXT);
        assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }

    public void testRule7a() {
        BjorneTokenizer tokenizer = new BjorneTokenizer(
                "if then else elif fi for done while until "
                        + "case { } ! do in esac a= a=b 1a=b =c");
        BjorneToken token = tokenizer.next(RULE_7a_CONTEXT);
        assertEquals(TOK_IF, token.getTokenType());
        token = tokenizer.next(RULE_7a_CONTEXT);
        assertEquals(TOK_THEN, token.getTokenType());
        token = tokenizer.next(RULE_7a_CONTEXT);
        assertEquals(TOK_ELSE, token.getTokenType());
        token = tokenizer.next(RULE_7a_CONTEXT);
        assertEquals(TOK_ELIF, token.getTokenType());
        token = tokenizer.next(RULE_7a_CONTEXT);
        assertEquals(TOK_FI, token.getTokenType());
        token = tokenizer.next(RULE_7a_CONTEXT);
        assertEquals(TOK_FOR, token.getTokenType());
        token = tokenizer.next(RULE_7a_CONTEXT);
        assertEquals(TOK_DONE, token.getTokenType());
        token = tokenizer.next(RULE_7a_CONTEXT);
        assertEquals(TOK_WHILE, token.getTokenType());
        token = tokenizer.next(RULE_7a_CONTEXT);
        assertEquals(TOK_UNTIL, token.getTokenType());
        token = tokenizer.next(RULE_7a_CONTEXT);
        assertEquals(TOK_CASE, token.getTokenType());
        token = tokenizer.next(RULE_7a_CONTEXT);
        assertEquals(TOK_LBRACE, token.getTokenType());
        token = tokenizer.next(RULE_7a_CONTEXT);
        assertEquals(TOK_RBRACE, token.getTokenType());
        token = tokenizer.next(RULE_7a_CONTEXT);
        assertEquals(TOK_BANG, token.getTokenType());
        token = tokenizer.next(RULE_7a_CONTEXT);
        assertEquals(TOK_DO, token.getTokenType());
        token = tokenizer.next(RULE_7a_CONTEXT);
        assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7a_CONTEXT);
        assertEquals(TOK_ESAC, token.getTokenType());
        token = tokenizer.next(RULE_7a_CONTEXT);
        assertEquals(TOK_ASSIGNMENT, token.getTokenType());
        token = tokenizer.next(RULE_7a_CONTEXT);
        assertEquals(TOK_ASSIGNMENT, token.getTokenType());
        token = tokenizer.next(RULE_7a_CONTEXT);
        assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7a_CONTEXT);
        assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7a_CONTEXT);
        assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }

    public void testRule7b() {
        BjorneTokenizer tokenizer = new BjorneTokenizer(
                "if then else elif fi for done while until "
                        + "case { } ! do in esac a= a=b 1a=b =c");
        BjorneToken token = tokenizer.next(RULE_7b_CONTEXT);
        assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7b_CONTEXT);
        assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7b_CONTEXT);
        assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7b_CONTEXT);
        assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7b_CONTEXT);
        assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7b_CONTEXT);
        assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7b_CONTEXT);
        assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7b_CONTEXT);
        assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7b_CONTEXT);
        assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7b_CONTEXT);
        assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7b_CONTEXT);
        assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7b_CONTEXT);
        assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7b_CONTEXT);
        assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7b_CONTEXT);
        assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7b_CONTEXT);
        assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7b_CONTEXT);
        assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7b_CONTEXT);
        assertEquals(TOK_ASSIGNMENT, token.getTokenType());
        token = tokenizer.next(RULE_7b_CONTEXT);
        assertEquals(TOK_ASSIGNMENT, token.getTokenType());
        token = tokenizer.next(RULE_7b_CONTEXT);
        assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7b_CONTEXT);
        assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7b_CONTEXT);
        assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }

    public void testRule8() {
        BjorneTokenizer tokenizer = new BjorneTokenizer(
                "if then else elif fi for done while until "
                        + "case { } ! do in esac a a_b a= a=b 1a=b =c");
        BjorneToken token = tokenizer.next(RULE_8_CONTEXT);
        assertEquals(TOK_IF, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        assertEquals(TOK_THEN, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        assertEquals(TOK_ELSE, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        assertEquals(TOK_ELIF, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        assertEquals(TOK_FI, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        assertEquals(TOK_FOR, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        assertEquals(TOK_DONE, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        assertEquals(TOK_WHILE, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        assertEquals(TOK_UNTIL, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        assertEquals(TOK_CASE, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        assertEquals(TOK_LBRACE, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        assertEquals(TOK_RBRACE, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        assertEquals(TOK_BANG, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        assertEquals(TOK_DO, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        assertEquals(TOK_NAME, token.getTokenType()); // yes: in -> NAME
        token = tokenizer.next(RULE_8_CONTEXT);
        assertEquals(TOK_ESAC, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        assertEquals(TOK_NAME, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        assertEquals(TOK_NAME, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        assertEquals(TOK_ASSIGNMENT, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        assertEquals(TOK_ASSIGNMENT, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }

    public void testRegress() {
        BjorneTokenizer tokenizer = new BjorneTokenizer("ls -l");
        BjorneToken token = tokenizer.peek(RULE_7a_CONTEXT);
        assertEquals(TOK_WORD, token.getTokenType());
        assertEquals("ls", token.getText());
        token = tokenizer.next();
        assertEquals(TOK_WORD, token.getTokenType());
        assertEquals("ls", token.getText());
        token = tokenizer.peek();
        assertEquals(TOK_WORD, token.getTokenType());
        assertEquals("-l", token.getText());
        token = tokenizer.next();
        assertEquals(TOK_WORD, token.getTokenType());
        assertEquals("-l", token.getText());
        token = tokenizer.peek();
        assertEquals(TOK_END_OF_STREAM, token.getTokenType());
        token = tokenizer.next();
        assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }
}
