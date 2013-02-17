/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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

import java.io.File;
import java.util.LinkedList;

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
        
        assertEquals("a.*?a.*?a", PathnamePattern.compilePosixShellPattern("a*a*a", 0).toString());
        assertEquals("a.*?a.*?a", PathnamePattern.compilePosixShellPattern("a*a*a", DF).toString());
        
        assertEquals("\".*?a.*?\"", PathnamePattern.compilePosixShellPattern("\"*a*\"", 0).toString());
        assertEquals("\\*a\\*", PathnamePattern.compilePosixShellPattern("\"*a*\"", DF).toString());
        
        assertEquals("\'.*?a.*?\'", PathnamePattern.compilePosixShellPattern("\'*a*\'", 0).toString());
        assertEquals("\\*a\\*", PathnamePattern.compilePosixShellPattern("\'*a*\'", DF).toString());
        
        assertEquals("\\\\.*?a.*?", PathnamePattern.compilePosixShellPattern("\\*a*", 0).toString());
        assertEquals("\\*a.*?", PathnamePattern.compilePosixShellPattern("\\*a*", DF).toString());
    }
    
    public void testCompilePathPattern() {
        assertEquals("PathnamePattern{source='abc',absolute=false,patterns=['abc']}", 
                PathnamePattern.compilePathPattern("abc", DF).toRegexString());

        assertEquals("PathnamePattern{source='?',absolute=false,patterns=['^[^\\.]$']}", 
                PathnamePattern.compilePathPattern("?", DF).toRegexString());

        // The following (which matches an empty pathname component) is suboptimal but 
        // not incorrect.  In practice, we should never encounter an empty pathname component.
        assertEquals("PathnamePattern{source='*',absolute=false,patterns=['^(|[^\\.].*)$']}", 
                PathnamePattern.compilePathPattern("*", DF).toRegexString());
        
        assertEquals("PathnamePattern{source='\"*\"',absolute=false,patterns=['^\\*$']}", 
                PathnamePattern.compilePathPattern("\"*\"", DF).toRegexString());

        assertEquals("PathnamePattern{source='a/b',absolute=false,patterns=['a','b']}", 
                PathnamePattern.compilePathPattern("a/b", DF).toRegexString());
        
        assertEquals("PathnamePattern{source='a/*',absolute=false,patterns=['a','^(|[^\\.].*)$']}", 
                PathnamePattern.compilePathPattern("a/*", DF).toRegexString());
        
        assertEquals("PathnamePattern{source='/a/*',absolute=true,patterns=['a','^(|[^\\.].*)$']}", 
                PathnamePattern.compilePathPattern("/a/*", DF).toRegexString());

        assertEquals("PathnamePattern{source='/a/\\*',absolute=true,patterns=['a','^\\*$']}", 
                PathnamePattern.compilePathPattern("/a/\\*", DF).toRegexString());
        
        assertEquals("PathnamePattern{source='a//\"*\"',absolute=false,patterns=['a','^\\*$']}", 
                PathnamePattern.compilePathPattern("a//\"*\"", DF).toRegexString());
        
        assertEquals("PathnamePattern{source='/a/\"*\"',absolute=true,patterns=['a','^\\*$']}", 
                PathnamePattern.compilePathPattern("/a/\"*\"", DF).toRegexString());

        assertEquals("PathnamePattern{source='\"/a/*\"',absolute=true,patterns=['a','^\\*$']}", 
                PathnamePattern.compilePathPattern("\"/a/*\"", DF).toRegexString());

        assertEquals("PathnamePattern{source='\"/a/*\"',absolute=true,patterns=['a','^\\*$']}", 
                PathnamePattern.compilePathPattern("\"/a/*\"", DF).toRegexString());
        
        assertEquals("PathnamePattern{source='{print \\$1}',absolute=false,patterns=['^\\{print \\$1\\}$']}", 
                PathnamePattern.compilePathPattern("{print \\$1}", DF).toRegexString());
    }
    
    public void testExpand() {
        PathnamePattern pat = PathnamePattern.compilePathPattern("/tmp/*");
        LinkedList<String> list = pat.expand(new File("."));
        for (String path : list) {
            assertTrue(new File(path).exists());
        }
        pat = PathnamePattern.compilePathPattern("*");
        list = pat.expand(new File("."));
        for (String path : list) {
            assertTrue(new File(path).exists());
        }
    }
}
