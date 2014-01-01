/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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

import org.jnode.shell.ShellSyntaxException;
import org.jnode.shell.bjorne.BjorneToken;
import org.jnode.shell.bjorne.BjorneTokenizer;
import org.junit.Assert;
import org.junit.Test;

public class BjorneTokenizerTest {

    @Test
    public void testBjorneTokenizer() throws ShellSyntaxException {
        new BjorneTokenizer("hello");
    }

    @Test
    public void testEmpty() throws ShellSyntaxException {
        BjorneTokenizer tokenizer = new BjorneTokenizer("");
        BjorneToken token = tokenizer.peek();
        Assert.assertEquals(TOK_END_OF_STREAM, token.getTokenType());
        token = tokenizer.peek(RULE_1_CONTEXT);
        Assert.assertEquals(TOK_END_OF_STREAM, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_END_OF_STREAM, token.getTokenType());
        token = tokenizer.next(RULE_1_CONTEXT);
        Assert.assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }

    @Test
    public void testNewline() throws ShellSyntaxException {
        BjorneTokenizer tokenizer = new BjorneTokenizer("\n");
        BjorneToken token = tokenizer.next();
        Assert.assertEquals(TOK_END_OF_LINE, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }

    @Test
    public void testBlanksAndNewlines() throws ShellSyntaxException {
        BjorneTokenizer tokenizer = new BjorneTokenizer("  \n\t\n  ");
        BjorneToken token = tokenizer.next();
        Assert.assertEquals(TOK_END_OF_LINE, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_END_OF_LINE, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }

    @Test
    public void testComments() throws ShellSyntaxException {
        BjorneTokenizer tokenizer = new BjorneTokenizer("# comment\n  #comment 2\n # comment # 3");
        BjorneToken token = tokenizer.next();
        Assert.assertEquals(TOK_END_OF_LINE, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_END_OF_LINE, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }

    @Test
    public void testContinuation() throws ShellSyntaxException {
        BjorneTokenizer tokenizer = new BjorneTokenizer("hello\\\nthere");
        BjorneToken token = tokenizer.next();
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        Assert.assertEquals("hellothere", token.getText());
        token = tokenizer.next();
        Assert.assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }

    @Test
    public void testBackslashAtEnd() throws ShellSyntaxException {
        BjorneTokenizer tokenizer = new BjorneTokenizer("hello\\");
        BjorneToken token = tokenizer.next();
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        Assert.assertEquals("hello", token.getText());
        token = tokenizer.next();
        Assert.assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }

    @Test
    public void testSymbols() throws ShellSyntaxException {
        BjorneTokenizer tokenizer = new BjorneTokenizer("; | & < > ( )");
        BjorneToken token = tokenizer.next();
        Assert.assertEquals(TOK_SEMI, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_BAR, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_AMP, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_LESS, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_GREAT, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_LPAREN, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_RPAREN, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }

    @Test
    public void testSymbols2() throws ShellSyntaxException {
        BjorneTokenizer tokenizer = new BjorneTokenizer("; ;; | || & && < << > >>");
        BjorneToken token = tokenizer.next();
        Assert.assertEquals(TOK_SEMI, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_DSEMI, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_BAR, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_OR_IF, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_AMP, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_AND_IF, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_LESS, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_DLESS, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_GREAT, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_DGREAT, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }

    @Test
    public void testSymbols3() throws ShellSyntaxException {
        BjorneTokenizer tokenizer = new BjorneTokenizer(";;;|||&&&<<<>>>");
        BjorneToken token = tokenizer.next();
        Assert.assertEquals(TOK_DSEMI, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_SEMI, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_OR_IF, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_BAR, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_AND_IF, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_AMP, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_DLESS, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_LESSGREAT, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_DGREAT, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }

    @Test
    public void testSymbols4() throws ShellSyntaxException {
        BjorneTokenizer tokenizer = new BjorneTokenizer("< << <<- <& <> > >> >| >&");
        BjorneToken token = tokenizer.next();
        Assert.assertEquals(TOK_LESS, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_DLESS, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_DLESSDASH, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_LESSAND, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_LESSGREAT, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_GREAT, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_DGREAT, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_CLOBBER, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_GREATAND, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }

    @Test
    public void testWords() throws ShellSyntaxException {
        BjorneTokenizer tokenizer = new BjorneTokenizer("hello there");
        BjorneToken token = tokenizer.next();
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        Assert.assertEquals("hello", token.getText());
        token = tokenizer.next();
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        Assert.assertEquals("there", token.getText());
        token = tokenizer.next();
        Assert.assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }

    @Test
    public void testWords2() throws ShellSyntaxException {
        BjorneTokenizer tokenizer = new BjorneTokenizer("hello\\ there\\\n friend");
        BjorneToken token = tokenizer.next();
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        Assert.assertEquals("hello\\ there", token.getText());
        token = tokenizer.next();
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        Assert.assertEquals("friend", token.getText());
        token = tokenizer.next();
        Assert.assertEquals(TOK_END_OF_STREAM, token.getTokenType());

        tokenizer = new BjorneTokenizer("hello\\ there\\\n\\ friend");
        token = tokenizer.next();
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        Assert.assertEquals("hello\\ there\\ friend", token.getText());
        token = tokenizer.next();
        Assert.assertEquals(TOK_END_OF_STREAM, token.getTokenType());

        tokenizer = new BjorneTokenizer("hello\\\nthere\\\n friend");
        token = tokenizer.next();
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        Assert.assertEquals("hellothere", token.getText());
        token = tokenizer.next();
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        Assert.assertEquals("friend", token.getText());
        token = tokenizer.next();
        Assert.assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }

    @Test
    public void testWords3() throws ShellSyntaxException {
        BjorneTokenizer tokenizer = new BjorneTokenizer("'1 2' \"3 4\" `5 6`");
        BjorneToken token = tokenizer.next();
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        Assert.assertEquals("'1 2'", token.getText());
        token = tokenizer.next();
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        Assert.assertEquals("\"3 4\"", token.getText());
        token = tokenizer.next();
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        Assert.assertEquals("`5 6`", token.getText());
        token = tokenizer.next();
        Assert.assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }

    @Test
    public void testWords4() throws ShellSyntaxException {
        BjorneTokenizer tokenizer = new BjorneTokenizer("'1 \"2\"' \"3\\\"4\"");
        BjorneToken token = tokenizer.next();
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        Assert.assertEquals("'1 \"2\"'", token.getText());
        token = tokenizer.next();
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        Assert.assertEquals("\"3\\\"4\"", token.getText());
        token = tokenizer.next();
        Assert.assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }

    @Test
    public void testWords5() throws ShellSyntaxException {
        BjorneTokenizer tokenizer = new BjorneTokenizer("1<2>3&4;5|6)7");
        BjorneToken token = tokenizer.next();
        Assert.assertEquals(TOK_IO_NUMBER, token.getTokenType());
        Assert.assertEquals("1", token.getText());
        token = tokenizer.next();
        Assert.assertEquals(TOK_LESS, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_IO_NUMBER, token.getTokenType());
        Assert.assertEquals("2", token.getText());
        token = tokenizer.next();
        Assert.assertEquals(TOK_GREAT, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        Assert.assertEquals("3", token.getText());
        token = tokenizer.next();
        Assert.assertEquals(TOK_AMP, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        Assert.assertEquals("4", token.getText());
        token = tokenizer.next();
        Assert.assertEquals(TOK_SEMI, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        Assert.assertEquals("5", token.getText());
        token = tokenizer.next();
        Assert.assertEquals(TOK_BAR, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        Assert.assertEquals("6", token.getText());
        token = tokenizer.next();
        Assert.assertEquals(TOK_RPAREN, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        Assert.assertEquals("7", token.getText());
        token = tokenizer.next();
        Assert.assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }

    @Test
    public void testRule1() throws ShellSyntaxException {
        BjorneTokenizer tokenizer =
                new BjorneTokenizer(
                        "if then else elif fi for done while until case { } ! do in esac");
        BjorneToken token = tokenizer.next(RULE_1_CONTEXT);
        Assert.assertEquals(TOK_IF, token.getTokenType());
        token = tokenizer.next(RULE_1_CONTEXT);
        Assert.assertEquals(TOK_THEN, token.getTokenType());
        token = tokenizer.next(RULE_1_CONTEXT);
        Assert.assertEquals(TOK_ELSE, token.getTokenType());
        token = tokenizer.next(RULE_1_CONTEXT);
        Assert.assertEquals(TOK_ELIF, token.getTokenType());
        token = tokenizer.next(RULE_1_CONTEXT);
        Assert.assertEquals(TOK_FI, token.getTokenType());
        token = tokenizer.next(RULE_1_CONTEXT);
        Assert.assertEquals(TOK_FOR, token.getTokenType());
        token = tokenizer.next(RULE_1_CONTEXT);
        Assert.assertEquals(TOK_DONE, token.getTokenType());
        token = tokenizer.next(RULE_1_CONTEXT);
        Assert.assertEquals(TOK_WHILE, token.getTokenType());
        token = tokenizer.next(RULE_1_CONTEXT);
        Assert.assertEquals(TOK_UNTIL, token.getTokenType());
        token = tokenizer.next(RULE_1_CONTEXT);
        Assert.assertEquals(TOK_CASE, token.getTokenType());
        token = tokenizer.next(RULE_1_CONTEXT);
        Assert.assertEquals(TOK_LBRACE, token.getTokenType());
        token = tokenizer.next(RULE_1_CONTEXT);
        Assert.assertEquals(TOK_RBRACE, token.getTokenType());
        token = tokenizer.next(RULE_1_CONTEXT);
        Assert.assertEquals(TOK_BANG, token.getTokenType());
        token = tokenizer.next(RULE_1_CONTEXT);
        Assert.assertEquals(TOK_DO, token.getTokenType());
        token = tokenizer.next(RULE_1_CONTEXT);
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_1_CONTEXT);
        Assert.assertEquals(TOK_ESAC, token.getTokenType());
        token = tokenizer.next(RULE_1_CONTEXT);
        Assert.assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }

    @Test
    public void testRule5() throws ShellSyntaxException {
        BjorneTokenizer tokenizer = new BjorneTokenizer("if a a1 9a a_b a,b AB A=b");
        BjorneToken token = tokenizer.next(RULE_5_CONTEXT);
        Assert.assertEquals(TOK_NAME, token.getTokenType());
        token = tokenizer.next(RULE_5_CONTEXT);
        Assert.assertEquals(TOK_NAME, token.getTokenType());
        token = tokenizer.next(RULE_5_CONTEXT);
        Assert.assertEquals(TOK_NAME, token.getTokenType());
        token = tokenizer.next(RULE_5_CONTEXT);
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_5_CONTEXT);
        Assert.assertEquals(TOK_NAME, token.getTokenType());
        token = tokenizer.next(RULE_5_CONTEXT);
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_5_CONTEXT);
        Assert.assertEquals(TOK_NAME, token.getTokenType());
        token = tokenizer.next(RULE_5_CONTEXT);
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_5_CONTEXT);
        Assert.assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }

    @Test
    public void testRule6() throws ShellSyntaxException {
        BjorneTokenizer tokenizer = new BjorneTokenizer("if in do");
        BjorneToken token = tokenizer.next(RULE_6_CONTEXT);
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_6_CONTEXT);
        Assert.assertEquals(TOK_IN, token.getTokenType());
        token = tokenizer.next(RULE_6_CONTEXT);
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_6_CONTEXT);
        Assert.assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }

    @Test
    public void testRule7a() throws ShellSyntaxException {
        BjorneTokenizer tokenizer =
                new BjorneTokenizer(
                        "if then else elif fi for done while until case { } ! do in esac a= a=b 1a=b =c");
        BjorneToken token = tokenizer.next(RULE_7a_CONTEXT);
        Assert.assertEquals(TOK_IF, token.getTokenType());
        token = tokenizer.next(RULE_7a_CONTEXT);
        Assert.assertEquals(TOK_THEN, token.getTokenType());
        token = tokenizer.next(RULE_7a_CONTEXT);
        Assert.assertEquals(TOK_ELSE, token.getTokenType());
        token = tokenizer.next(RULE_7a_CONTEXT);
        Assert.assertEquals(TOK_ELIF, token.getTokenType());
        token = tokenizer.next(RULE_7a_CONTEXT);
        Assert.assertEquals(TOK_FI, token.getTokenType());
        token = tokenizer.next(RULE_7a_CONTEXT);
        Assert.assertEquals(TOK_FOR, token.getTokenType());
        token = tokenizer.next(RULE_7a_CONTEXT);
        Assert.assertEquals(TOK_DONE, token.getTokenType());
        token = tokenizer.next(RULE_7a_CONTEXT);
        Assert.assertEquals(TOK_WHILE, token.getTokenType());
        token = tokenizer.next(RULE_7a_CONTEXT);
        Assert.assertEquals(TOK_UNTIL, token.getTokenType());
        token = tokenizer.next(RULE_7a_CONTEXT);
        Assert.assertEquals(TOK_CASE, token.getTokenType());
        token = tokenizer.next(RULE_7a_CONTEXT);
        Assert.assertEquals(TOK_LBRACE, token.getTokenType());
        token = tokenizer.next(RULE_7a_CONTEXT);
        Assert.assertEquals(TOK_RBRACE, token.getTokenType());
        token = tokenizer.next(RULE_7a_CONTEXT);
        Assert.assertEquals(TOK_BANG, token.getTokenType());
        token = tokenizer.next(RULE_7a_CONTEXT);
        Assert.assertEquals(TOK_DO, token.getTokenType());
        token = tokenizer.next(RULE_7a_CONTEXT);
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7a_CONTEXT);
        Assert.assertEquals(TOK_ESAC, token.getTokenType());
        token = tokenizer.next(RULE_7a_CONTEXT);
        Assert.assertEquals(TOK_ASSIGNMENT, token.getTokenType());
        token = tokenizer.next(RULE_7a_CONTEXT);
        Assert.assertEquals(TOK_ASSIGNMENT, token.getTokenType());
        token = tokenizer.next(RULE_7a_CONTEXT);
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7a_CONTEXT);
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7a_CONTEXT);
        Assert.assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }

    @Test
    public void testRule7b() throws ShellSyntaxException {
        BjorneTokenizer tokenizer =
                new BjorneTokenizer(
                        "if then else elif fi for done while until case { } ! do in esac a= a=b 1a=b =c");
        BjorneToken token = tokenizer.next(RULE_7b_CONTEXT);
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7b_CONTEXT);
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7b_CONTEXT);
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7b_CONTEXT);
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7b_CONTEXT);
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7b_CONTEXT);
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7b_CONTEXT);
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7b_CONTEXT);
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7b_CONTEXT);
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7b_CONTEXT);
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7b_CONTEXT);
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7b_CONTEXT);
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7b_CONTEXT);
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7b_CONTEXT);
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7b_CONTEXT);
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7b_CONTEXT);
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7b_CONTEXT);
        Assert.assertEquals(TOK_ASSIGNMENT, token.getTokenType());
        token = tokenizer.next(RULE_7b_CONTEXT);
        Assert.assertEquals(TOK_ASSIGNMENT, token.getTokenType());
        token = tokenizer.next(RULE_7b_CONTEXT);
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7b_CONTEXT);
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_7b_CONTEXT);
        Assert.assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }

    @Test
    public void testRule8() throws ShellSyntaxException {
        BjorneTokenizer tokenizer =
                new BjorneTokenizer(
                        "if then else elif fi for done while until case { } ! do in esac a a_b a= a=b 1a=b =c");
        BjorneToken token = tokenizer.next(RULE_8_CONTEXT);
        Assert.assertEquals(TOK_IF, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        Assert.assertEquals(TOK_THEN, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        Assert.assertEquals(TOK_ELSE, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        Assert.assertEquals(TOK_ELIF, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        Assert.assertEquals(TOK_FI, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        Assert.assertEquals(TOK_FOR, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        Assert.assertEquals(TOK_DONE, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        Assert.assertEquals(TOK_WHILE, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        Assert.assertEquals(TOK_UNTIL, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        Assert.assertEquals(TOK_CASE, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        Assert.assertEquals(TOK_LBRACE, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        Assert.assertEquals(TOK_RBRACE, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        Assert.assertEquals(TOK_BANG, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        Assert.assertEquals(TOK_DO, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        Assert.assertEquals(TOK_NAME, token.getTokenType()); // yes: in -> NAME
        token = tokenizer.next(RULE_8_CONTEXT);
        Assert.assertEquals(TOK_ESAC, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        Assert.assertEquals(TOK_NAME, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        Assert.assertEquals(TOK_NAME, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        Assert.assertEquals(TOK_ASSIGNMENT, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        Assert.assertEquals(TOK_ASSIGNMENT, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        token = tokenizer.next(RULE_8_CONTEXT);
        Assert.assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }

    @Test
    public void testRegress() throws ShellSyntaxException {
        BjorneTokenizer tokenizer = new BjorneTokenizer("ls -l");
        BjorneToken token = tokenizer.peek(RULE_7a_CONTEXT);
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        Assert.assertEquals("ls", token.getText());
        token = tokenizer.next();
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        Assert.assertEquals("ls", token.getText());
        token = tokenizer.peek();
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        Assert.assertEquals("-l", token.getText());
        token = tokenizer.next();
        Assert.assertEquals(TOK_WORD, token.getTokenType());
        Assert.assertEquals("-l", token.getText());
        token = tokenizer.peek();
        Assert.assertEquals(TOK_END_OF_STREAM, token.getTokenType());
        token = tokenizer.next();
        Assert.assertEquals(TOK_END_OF_STREAM, token.getTokenType());
    }
}
