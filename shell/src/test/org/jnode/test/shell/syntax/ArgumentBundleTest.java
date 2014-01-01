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

import java.io.File;

import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.ArgumentBundle;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.Syntax;
import org.jnode.shell.syntax.SyntaxFailureException;
import org.junit.Assert;
import org.junit.Test;

public class ArgumentBundleTest {

    @Test
    public void testArgumentBundle() {
        new ArgumentBundle();
        new ArgumentBundle(new FileArgument("arg1", 0));
        try {
            new ArgumentBundle(new FileArgument("arg1", 0), new FileArgument("arg1", 0));
            Assert.fail("Didn't throw an IllegalArgumentException for duplicate labels");
        } catch (IllegalArgumentException ex) {
            // expected ...
        }
        try {
            new ArgumentBundle((Argument<?>) null);
            Assert.fail("Didn't throw an NullPointerException for null argument");
        } catch (NullPointerException ex) {
            // expected ...
        }
        try {
            new ArgumentBundle(new FileArgument(null, 0));
            Assert.fail("Didn't throw an NullPointerException for null label");
        } catch (NullPointerException ex) {
            // expected ...
        }
    }

    @Test
    public void testGetArgumentString() {
        Argument<File> arg1 = new FileArgument("arg1", 0);
        Argument<File> arg2 = new FileArgument("arg2", 0);
        ArgumentBundle b = new ArgumentBundle(arg1, arg2);
        Assert.assertEquals(arg1, b.getArgument("arg1"));
        Assert.assertEquals(arg2, b.getArgument("arg2"));
        try {
            b.getArgument("arg3");
            Assert.fail("didn't throw exception");
        } catch (SyntaxFailureException ex) {
            // expected
        }
    }

    // Expose protected methods for testing ...
    private class MyArgumentBundle extends ArgumentBundle {
        public MyArgumentBundle(Argument<?>... elements) {
            super(elements);
        }

        public Syntax testCreateDefaultSyntax() {
            return super.createDefaultSyntax();
        }
    }

    @Test
    public void testCreateDefaultSyntax() {
        Argument<File> arg1 = new FileArgument("arg1", 0);
        Argument<File> arg2 = new FileArgument("arg2", 0);
        MyArgumentBundle b = new MyArgumentBundle(arg1, arg2);

        b.testCreateDefaultSyntax();
    }
}
