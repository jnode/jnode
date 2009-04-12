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
 
package org.jnode.test.shell;

import junit.framework.TestCase;

import org.jnode.shell.PathnamePattern;

/**
 * Test key methods of the PathnamePattern class.
 *
 * @author crawley@jnode.org
 */
public class PathnamePatternTest extends TestCase {
    
    private static final int DF = PathnamePattern.DEFAULT_FLAGS;

    public void testIsPattern() {
        assertTrue(PathnamePattern.isPattern("*"));
        assertTrue(PathnamePattern.isPattern("?"));
        assertTrue(PathnamePattern.isPattern("[abc]"));
        assertTrue(PathnamePattern.isPattern("\"hi\""));
        assertTrue(PathnamePattern.isPattern("\'hi\'"));
        assertTrue(PathnamePattern.isPattern("hi\\ there"));
        assertFalse(PathnamePattern.isPattern("hi there"));
        assertFalse(PathnamePattern.isPattern(""));
        assertFalse(PathnamePattern.isPattern(" "));
    }
    
    public void testCompilePosixShellPattern() {
        assertEquals("abc", PathnamePattern.compilePosixShellPattern("abc", 0).toString());
        assertEquals("abc", PathnamePattern.compilePosixShellPattern("abc", DF).toString());
        assertEquals(".", PathnamePattern.compilePosixShellPattern("?", 0).toString());
        assertEquals("[^\\.]", PathnamePattern.compilePosixShellPattern("?", DF).toString());
        assertEquals(".*?", PathnamePattern.compilePosixShellPattern("*", 0).toString());
        assertEquals("(|[^\\.].*?)", PathnamePattern.compilePosixShellPattern("*", DF).toString());
        assertEquals(".*?a.*?", PathnamePattern.compilePosixShellPattern("*a*", 0).toString());
        assertEquals("(|[^\\.].*?)a.*?", PathnamePattern.compilePosixShellPattern("*a*", DF).toString());
        assertEquals("\".*?a.*?\"", PathnamePattern.compilePosixShellPattern("\"*a*\"", 0).toString());
        assertEquals("\\*a\\*", PathnamePattern.compilePosixShellPattern("\"*a*\"", DF).toString());
        assertEquals("\'.*?a.*?\'", PathnamePattern.compilePosixShellPattern("\'*a*\'", 0).toString());
        assertEquals("\\*a\\*", PathnamePattern.compilePosixShellPattern("\'*a*\'", DF).toString());
        assertEquals("\\\\.*?a.*?", PathnamePattern.compilePosixShellPattern("\\*a*", 0).toString());
        assertEquals("\\*a.*?", PathnamePattern.compilePosixShellPattern("\\*a*", DF).toString());
    }
}
