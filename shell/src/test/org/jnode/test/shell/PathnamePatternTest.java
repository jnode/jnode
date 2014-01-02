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
 
package org.jnode.test.shell;

import java.io.File;
import java.util.LinkedList;

import org.jnode.shell.PathnamePattern;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test key methods of the PathnamePattern class.
 *
 * @author crawley@jnode.org
 */
public class PathnamePatternTest {
    
    private static final int DF = PathnamePattern.DEFAULT_FLAGS;

    @Test
    public void testIsPattern() {
        Assert.assertTrue(PathnamePattern.isPattern("*"));
        Assert.assertTrue(PathnamePattern.isPattern("?"));
        Assert.assertTrue(PathnamePattern.isPattern("[abc]"));
        Assert.assertTrue(PathnamePattern.isPattern("\"hi\""));
        Assert.assertTrue(PathnamePattern.isPattern("\'hi\'"));
        Assert.assertTrue(PathnamePattern.isPattern("hi\\ there"));
        Assert.assertFalse(PathnamePattern.isPattern("hi there"));
        Assert.assertFalse(PathnamePattern.isPattern(""));
        Assert.assertFalse(PathnamePattern.isPattern(" "));
    }
    
    @Test
    public void testCompilePosixShellPattern() {
        Assert.assertEquals("abc", PathnamePattern.compilePosixShellPattern("abc", 0).toString());
        Assert.assertEquals("abc", PathnamePattern.compilePosixShellPattern("abc", DF).toString());
        
        Assert.assertEquals(".", PathnamePattern.compilePosixShellPattern("?", 0).toString());
        Assert.assertEquals("[^\\.]", PathnamePattern.compilePosixShellPattern("?", DF).toString());
        
        Assert.assertEquals(".*?", PathnamePattern.compilePosixShellPattern("*", 0).toString());
        Assert.assertEquals("(|[^\\.].*?)", PathnamePattern.compilePosixShellPattern("*", DF)
                .toString());

        Assert.assertEquals(".*?a.*?", PathnamePattern.compilePosixShellPattern("*a*", 0)
                .toString());
        Assert.assertEquals("(|[^\\.].*?)a.*?", PathnamePattern.compilePosixShellPattern("*a*", DF)
                .toString());

        Assert.assertEquals("a.*?a.*?a", PathnamePattern.compilePosixShellPattern("a*a*a", 0)
                .toString());
        Assert.assertEquals("a.*?a.*?a", PathnamePattern.compilePosixShellPattern("a*a*a", DF)
                .toString());

        Assert.assertEquals("\".*?a.*?\"", PathnamePattern.compilePosixShellPattern("\"*a*\"", 0)
                .toString());
        Assert.assertEquals("\\*a\\*", PathnamePattern.compilePosixShellPattern("\"*a*\"", DF)
                .toString());

        Assert.assertEquals("\'.*?a.*?\'", PathnamePattern.compilePosixShellPattern("\'*a*\'", 0)
                .toString());
        Assert.assertEquals("\\*a\\*", PathnamePattern.compilePosixShellPattern("\'*a*\'", DF)
                .toString());

        Assert.assertEquals("\\\\.*?a.*?", PathnamePattern.compilePosixShellPattern("\\*a*", 0)
                .toString());
        Assert.assertEquals("\\*a.*?", PathnamePattern.compilePosixShellPattern("\\*a*", DF)
                .toString());
    }

    @Test
    public void testCompilePathPattern() {
        Assert.assertEquals("PathnamePattern{source='abc',absolute=false,patterns=['abc']}",
                PathnamePattern.compilePathPattern("abc", DF).toRegexString());

        Assert.assertEquals("PathnamePattern{source='?',absolute=false,patterns=['^[^\\.]$']}",
            PathnamePattern.compilePathPattern("?", DF).toRegexString());

        // The following (which matches an empty pathname component) is
        // suboptimal but
        // not incorrect. In practice, we should never encounter an empty
        // pathname component.
        Assert.assertEquals(
                "PathnamePattern{source='*',absolute=false,patterns=['^(|[^\\.].*)$']}",
                PathnamePattern.compilePathPattern("*", DF).toRegexString());

        Assert.assertEquals("PathnamePattern{source='\"*\"',absolute=false,patterns=['^\\*$']}",
                PathnamePattern.compilePathPattern("\"*\"", DF).toRegexString());

        Assert.assertEquals("PathnamePattern{source='a/b',absolute=false,patterns=['a','b']}",
                PathnamePattern.compilePathPattern("a/b", DF).toRegexString());

        Assert.assertEquals(
                "PathnamePattern{source='a/*',absolute=false,patterns=['a','^(|[^\\.].*)$']}",
                PathnamePattern.compilePathPattern("a/*", DF).toRegexString());

        Assert.assertEquals(
                "PathnamePattern{source='/a/*',absolute=true,patterns=['a','^(|[^\\.].*)$']}",
                PathnamePattern.compilePathPattern("/a/*", DF).toRegexString());

        Assert.assertEquals(
                "PathnamePattern{source='/a/\\*',absolute=true,patterns=['a','^\\*$']}",
                PathnamePattern.compilePathPattern("/a/\\*", DF).toRegexString());

        Assert.assertEquals(
                "PathnamePattern{source='a//\"*\"',absolute=false,patterns=['a','^\\*$']}",
                PathnamePattern.compilePathPattern("a//\"*\"", DF).toRegexString());

        Assert.assertEquals(
                "PathnamePattern{source='/a/\"*\"',absolute=true,patterns=['a','^\\*$']}",
                PathnamePattern.compilePathPattern("/a/\"*\"", DF).toRegexString());

        Assert.assertEquals(
                "PathnamePattern{source='\"/a/*\"',absolute=true,patterns=['a','^\\*$']}",
                PathnamePattern.compilePathPattern("\"/a/*\"", DF).toRegexString());

        Assert.assertEquals(
                "PathnamePattern{source='\"/a/*\"',absolute=true,patterns=['a','^\\*$']}",
                PathnamePattern.compilePathPattern("\"/a/*\"", DF).toRegexString());
        
        Assert.assertEquals("PathnamePattern{source='{print \\$1}',absolute=false,patterns=['^\\{print \\$1\\}$']}",
                PathnamePattern.compilePathPattern("{print \\$1}", DF).toRegexString());
    }
    
    @Test
    public void testExpand() {
        PathnamePattern pat = PathnamePattern.compilePathPattern("/tmp/*");
        LinkedList<String> list = pat.expand(new File("."));
        for (String path : list) {
            Assert.assertTrue(new File(path).exists());
        }
        pat = PathnamePattern.compilePathPattern("*");
        list = pat.expand(new File("."));
        for (String path : list) {
            Assert.assertTrue(new File(path).exists());
        }
    }
}
