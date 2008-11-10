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
package org.jnode.test.shell.help;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import junit.framework.TestCase;

import org.jnode.shell.help.def.DefaultHelp;

/**
 * Unit tests for the DefaultHelp implementation of the Help
 *
 * @author crawley@jnode.org
 */
public class DefaultHelpTest extends TestCase {

    static class MyDefaultHelp extends DefaultHelp {
        static class MyCell extends Cell {

            MyCell(int margin, int width) {
                super(margin, width);
            }

            public String fit(String text) {
                return super.fit(text);
            }

            public String stamp(String text) {
                return super.stamp(text);
            }
        }

        public void format(PrintWriter out, MyCell[] cells, String[] texts) {
            super.format(out, cells, texts);
        }
    }

    public void testConstructor() {
        new DefaultHelp();
    }

    public void testCellFit() {
        String msg = "The quick brown fox jumped over the lazy dog.";
        for (int i = 1; i < msg.length() + 5; i++) {
            String m = new MyDefaultHelp.MyCell(5, i).fit(msg);
            assertTrue("fit length", m.length() <= i);
            assertTrue("text starts with fit ", msg.startsWith(m));
        }

        assertEquals("   Hello  ", new MyDefaultHelp.MyCell(5, 10).fit("   Hello   "));
    }

    public void testCellStamp() {
        String msg = "Hello Mum";
        String m = new MyDefaultHelp.MyCell(5, 10).stamp(msg);
        assertEquals(m, "     Hello Mum ");
    }

    public void testFormat() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintWriter ps = new PrintWriter(bos);
        new MyDefaultHelp().format(ps,
            new MyDefaultHelp.MyCell[]{
                new MyDefaultHelp.MyCell(3, 5), new MyDefaultHelp.MyCell(3, 20)},
            new String[]{
                "12345678901234567890",
                "The quick brown fox jumped over the lazy dog." +
                    "The quick brown fox jumped over the lazy dog." +
                    "The quick brown fox jumped over the lazy dog."
            });
        assertEquals(
            "   12345   The quick brown fox \n" +
                "   67890   jumped over the lazy\n" +
                "   12345   dog.The quick brown \n" +
                "   67890   fox jumped over     \n" +
                "           the lazy dog.The    \n" +
                "           quick brown fox     \n" +
                "           jumped over the     \n" +
                "           lazy dog.           \n",
            bos.toString());
    }

}
