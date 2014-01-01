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
 
package org.jnode.test.shell.syntax;

import org.jnode.shell.syntax.MuAlternation;
import org.jnode.shell.syntax.MuArgument;
import org.jnode.shell.syntax.MuBackReference;
import org.jnode.shell.syntax.MuSequence;
import org.jnode.shell.syntax.MuSymbol;
import org.jnode.shell.syntax.MuSyntax;
import org.jnode.shell.syntax.SyntaxFailureException;
import org.junit.Assert;
import org.junit.Test;

public class MuSyntaxTest {

    @Test
    public void testMuSymbolConstructor() {
        new MuSymbol("hi");
        new MuSymbol("prod1", "hi");
        try {
            new MuSymbol(null, null);
            Assert.fail("expected NPE");
        } catch (NullPointerException ex) {
            // expected
        }
        try {
            new MuSymbol(null, "");
            Assert.fail("expected IAE");
        } catch (IllegalArgumentException ex) {
            // expected
        }
        try {
            new MuSymbol("", "hi");
            Assert.fail("expected IAE");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    @Test
    public void testMuArgumentConstructor() {
        new MuArgument("arg1");
        new MuArgument("label", "arg1", 0);

        try {
            new MuArgument(null, null, 0);
            Assert.fail("expected NPE");
        } catch (NullPointerException ex) {
            // expected
        }
        try {
            new MuArgument(null, "", 0);
            Assert.fail("expected IAE");
        } catch (IllegalArgumentException ex) {
            // expected
        }
        try {
            new MuArgument("", "arg1", 0);
            Assert.fail("expected IAE");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    @Test
    public void testSequenceConstructor() {
        new MuSequence(new MuSymbol("hi"), new MuSymbol("mom"));
        try {
            new MuSequence(new MuSymbol("hi"), null);
            Assert.fail("expected NPE");
        } catch (NullPointerException ex) {
            // expected
        }
    }

    @Test
    public void testAlternationConstructor() {
        new MuAlternation(new MuSymbol("hi"), new MuSymbol("mom"));
        new MuAlternation(new MuSymbol("hi"), null);
    }

    @Test
    public void testBackReferenceConstructor() {
        new MuBackReference("hi");
        try {
            new MuBackReference(null);
            Assert.fail("expected NPE");
        } catch (NullPointerException ex) {
            // expected
        }
        try {
            new MuBackReference("");
            Assert.fail("expected IAE");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    @Test
    public void testResolveBackReferences() {
        MuSyntax syntax = new MuAlternation("root", new MuBackReference("root"), null);
        syntax.resolveBackReferences();
        Assert.assertEquals(((MuAlternation) syntax).getAlternatives()[0], syntax);

        syntax =
                new MuAlternation("root", new MuSequence(new MuBackReference("root"),
                        new MuBackReference("root")), null);
        syntax.resolveBackReferences();
        MuSyntax[] tmp = ((MuAlternation) syntax).getAlternatives();
        Assert.assertEquals(((MuSequence) tmp[0]).getElements()[0], syntax);
        Assert.assertEquals(((MuSequence) tmp[0]).getElements()[1], syntax);

        try {
            syntax = new MuAlternation("root", new MuBackReference("foo"), null);
            syntax.resolveBackReferences();
            Assert.fail("expected SFE");
        } catch (SyntaxFailureException ex) {
            // expected
        }
    }

    @Test
    public void testFormat() {
        Assert.assertEquals("<*Start*> ::= <<arg1>>", new MuArgument("arg1").format());
        Assert.assertEquals("<l1> ::= <<arg1>>", new MuArgument("l1", "arg1", 0).format());

        Assert.assertEquals("<*Start*> ::= 'hi'", new MuSymbol("hi").format());
        Assert.assertEquals("<l1> ::= 'hi'", new MuSymbol("l1", "hi").format());

        Assert.assertEquals("<*Start*> ::= 'hi' 'mum'", new MuSequence(new MuSymbol("hi"),
                new MuSymbol("mum")).format());
        Assert.assertEquals("<l1> ::= 'hi' 'mum'", new MuSequence("l1", new MuSymbol("hi"),
                new MuSymbol("mum")).format());
        Assert.assertEquals("<l1> ::= <l2> 'mum'\n<l2> ::= 'hi'", new MuSequence("l1",
                new MuSymbol("l2", "hi"), new MuSymbol("mum")).format());

        Assert.assertEquals("<*Start*> ::= ( 'hi' | 'mum' )", new MuAlternation(new MuSymbol("hi"),
                new MuSymbol("mum")).format());
        Assert.assertEquals("<*Start*> ::= ( 'hi' |  )",
                new MuAlternation(new MuSymbol("hi"), null).format());
        Assert.assertEquals("<l1> ::= ( 'hi' | 'mum' )", new MuAlternation("l1",
                new MuSymbol("hi"), new MuSymbol("mum")).format());
        Assert.assertEquals("<l1> ::= ( <l2> | 'mum' )\n<l2> ::= 'hi'", new MuAlternation("l1",
                new MuSymbol("l2", "hi"), new MuSymbol("mum")).format());
    }

    @Test
    public void testFormat2() {
        MuSyntax syntax = new MuAlternation("root", new MuBackReference("root"), null);
        Assert.assertEquals("<root> ::= ( <[root]> |  )\n<[root]> ::= <root>", syntax.format());
        syntax.resolveBackReferences();
        Assert.assertEquals("<root> ::= ( <root> |  )", syntax.format());
    }
}
