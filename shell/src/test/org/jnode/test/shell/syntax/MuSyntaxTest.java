/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2007-2008 JNode.org
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

import junit.framework.TestCase;

import org.jnode.shell.syntax.MuAlternation;
import org.jnode.shell.syntax.MuArgument;
import org.jnode.shell.syntax.MuBackReference;
import org.jnode.shell.syntax.MuSequence;
import org.jnode.shell.syntax.MuSymbol;
import org.jnode.shell.syntax.MuSyntax;
import org.jnode.shell.syntax.SyntaxFailureException;


public class MuSyntaxTest extends TestCase {

    public void testMuSymbolConstructor() {
        new MuSymbol("hi");
        new MuSymbol("prod1", "hi");
        try {
            new MuSymbol(null, null);
            fail("expected NPE");
        }
        catch (NullPointerException ex) {
            // expected
        }
        try {
            new MuSymbol(null, "");
            fail("expected IAE");
        }
        catch (IllegalArgumentException ex) {
            // expected
        }
        try {
            new MuSymbol("", "hi");
            fail("expected IAE");
        }
        catch (IllegalArgumentException ex) {
            // expected
        }
    }
    
    public void testMuArgumentConstructor() {
        new MuArgument("arg1");
        new MuArgument("label", "arg1");

        try {
            new MuArgument(null, null);
            fail("expected NPE");
        }
        catch (NullPointerException ex) {
            // expected
        }
        try {
            new MuArgument(null, "");
            fail("expected IAE");
        }
        catch (IllegalArgumentException ex) {
            // expected
        }
        try {
            new MuArgument("", "arg1");
            fail("expected IAE");
        }
        catch (IllegalArgumentException ex) {
            // expected
        }
    }
    
    public void testSequenceConstructor() {
        new MuSequence(new MuSymbol("hi"), new MuSymbol("mom"));
        try {
            new MuSequence(new MuSymbol("hi"), null);
            fail("expected NPE");
        }
        catch (NullPointerException ex) {
            // expected
        }
    }
    
    public void testAlternationConstructor() {
        new MuAlternation(new MuSymbol("hi"), new MuSymbol("mom"));
        try {
            new MuAlternation(new MuSymbol("hi"));
            fail("expected IAE");
        }
        catch (IllegalArgumentException ex) {
            // expected
        }

        new MuAlternation(new MuSymbol("hi"), null);
    }
    
    public void testBackReferenceConstructor() {
        new MuBackReference("hi");
        try {
            new MuBackReference(null);
            fail("expected NPE");
        }
        catch (NullPointerException ex) {
            // expected
        }
        try {
            new MuBackReference("");
            fail("expected IAE");
        }
        catch (IllegalArgumentException ex) {
            // expected
        }
    }
    
    public void testResolveBackReferences() {
        MuSyntax syntax = 
            new MuAlternation("root", new MuBackReference("root"), null);
        syntax.resolveBackReferences();
        assertEquals(((MuAlternation) syntax).getAlternatives()[0], syntax);
        
        syntax = new MuAlternation("root", 
                new MuSequence(new MuBackReference("root"), new MuBackReference("root")), null);
        syntax.resolveBackReferences();
        MuSyntax[] tmp = ((MuAlternation) syntax).getAlternatives();
        assertEquals(((MuSequence) tmp[0]).getElements()[0], syntax);
        assertEquals(((MuSequence) tmp[0]).getElements()[1], syntax);
        
        try {
            syntax = new MuAlternation("root", new MuBackReference("foo"), null);
            syntax.resolveBackReferences();
            fail("expected SFE");
        }
        catch (SyntaxFailureException ex) {
            // expected
        }
    }
    
    public void testFormat() {
        assertEquals("<*Start*> ::= <<arg1>>", new MuArgument("arg1").format());
        assertEquals("<l1> ::= <<arg1>>", new MuArgument("l1", "arg1").format());
        
        assertEquals("<*Start*> ::= 'hi'", new MuSymbol("hi").format());
        assertEquals("<l1> ::= 'hi'", new MuSymbol("l1", "hi").format());
        
        assertEquals("<*Start*> ::= 'hi' 'mum'", 
                new MuSequence(
                        new MuSymbol("hi"), new MuSymbol("mum")).format());
        assertEquals("<l1> ::= 'hi' 'mum'", 
                new MuSequence("l1", 
                        new MuSymbol("hi"), new MuSymbol("mum")).format());
        assertEquals("<l1> ::= <l2> 'mum'\n<l2> ::= 'hi'", 
                new MuSequence("l1", 
                        new MuSymbol("l2", "hi"), new MuSymbol("mum")).format());
        
        assertEquals("<*Start*> ::= ( 'hi' | 'mum' )", 
                new MuAlternation(
                        new MuSymbol("hi"), new MuSymbol("mum")).format());
        assertEquals("<*Start*> ::= ( 'hi' |  )", 
                new MuAlternation(
                        new MuSymbol("hi"), null).format());
        assertEquals("<l1> ::= ( 'hi' | 'mum' )", 
                new MuAlternation("l1", 
                        new MuSymbol("hi"),new MuSymbol("mum")).format());
        assertEquals("<l1> ::= ( <l2> | 'mum' )\n<l2> ::= 'hi'", 
                new MuAlternation("l1", 
                        new MuSymbol("l2", "hi"), new MuSymbol("mum")).format());
    }
     
    
    public void testFormat2() {
        MuSyntax syntax = 
            new MuAlternation("root", new MuBackReference("root"), null);
        assertEquals("<root> ::= ( <[root]> |  )\n<[root]> ::= <root>",  syntax.format());
        syntax.resolveBackReferences();
        assertEquals("<root> ::= ( <root> |  )",  syntax.format());
    }
}
