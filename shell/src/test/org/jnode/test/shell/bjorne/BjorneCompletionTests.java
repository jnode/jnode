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

import java.util.Set;

import javax.naming.NamingException;

import junit.framework.TestCase;

import org.jnode.shell.CommandCompletions;
import org.jnode.shell.Completable;
import org.jnode.shell.ShellSyntaxException;
import org.jnode.shell.ShellUtils;
import org.jnode.shell.alias.AliasManager;
import org.jnode.shell.bjorne.BjorneInterpreter;
import org.jnode.shell.help.CompletionException;
import org.jnode.shell.syntax.ArgumentSyntax;
import org.jnode.shell.syntax.EmptySyntax;
import org.jnode.shell.syntax.OptionSyntax;
import org.jnode.shell.syntax.RepeatSyntax;
import org.jnode.shell.syntax.SequenceSyntax;
import org.jnode.shell.syntax.SyntaxBundle;
import org.jnode.shell.syntax.SyntaxManager;
import org.jnode.test.shell.Cassowary;
import org.jnode.test.shell.syntax.TestShell;

/**
 * Tests for completion in the bjorne interpreter.  Some of the sample commands are 
 * nonsensical ... but that's OK because we're only interested in completion behavior.
 * 
 * @author crawley@jnode.org
 */
public class BjorneCompletionTests extends TestCase {
    
    static TestShell shell;
    static {
        try {
            Cassowary.initEnv();
            shell = new TestShell();
            ShellUtils.getShellManager().registerShell(shell);
            
            AliasManager am = shell.getAliasManager();
            am.add("gc", "org.jnode.command.system.GcCommand");
            am.add("cpuid", "org.jnode.test.shell.MyCpuIDCommand");
            am.add("set", "org.jnode.command.system.SetCommand");
            am.add("dir", "org.jnode.test.shell.MyDirCommand");
            am.add("duh", "org.jnode.test.shell.MyDuhCommand");
            am.add("cat", "org.jnode.test.shell.MyCatCommand");
            am.add("echo", "org.jnode.test.shell.MyEchoCommand");
            am.add("alias", "org.jnode.test.shell.MyAliasCommand");

            SyntaxManager sm = shell.getSyntaxManager();
            sm.add(new SyntaxBundle("set",
                new SequenceSyntax(new ArgumentSyntax("key"), new ArgumentSyntax("value"))));
            sm.add(new SyntaxBundle("duh", new ArgumentSyntax("path")));
            sm.add(new SyntaxBundle("echo", new RepeatSyntax(new ArgumentSyntax("text"))));
            sm.add(new SyntaxBundle("cpuid", new SequenceSyntax()));
            sm.add(new SyntaxBundle("alias",
                new EmptySyntax(null, "Print all available aliases and corresponding classnames"),
                new SequenceSyntax(null, "Set an aliases for given classnames",
                    new ArgumentSyntax("alias"), new ArgumentSyntax("classname")),
                new OptionSyntax("remove", 'r', null, "Remove an alias")));
            
        } catch (NamingException ex) {
            throw new RuntimeException(ex);
        }
    }
    

    public void testSimpleCommand() throws ShellSyntaxException, CompletionException {
        doCompletionTest("echo hi", "TE");
    }

    public void testListCommand() throws ShellSyntaxException, CompletionException {
        doCompletionTest("echo hi ; echo", "TETT");
    }

    public void testAndCommand() throws ShellSyntaxException, CompletionException {
        doCompletionTest("echo hi && echo", "TETT");
    }

    public void testPipeCommand() throws ShellSyntaxException, CompletionException {
        doCompletionTest("echo hi | echo", "TETT");
    }

    public void testPipe2Command() throws ShellSyntaxException, CompletionException {
        doCompletionTest("echo hi |\necho", "TETT");
    }

    public void testIfCommand() throws ShellSyntaxException, CompletionException {
        doCompletionTest("if cpuid ; then echo hi ; fi", "TTTTTETT");
    }

    public void testIf2Command() throws ShellSyntaxException, CompletionException {
        doCompletionTest("if\ncpuid ; then echo hi ; fi", "TTTTTETT");
    }

    public void testIf3Command() throws ShellSyntaxException, CompletionException {
        doCompletionTest("if cpuid\nthen echo hi ; fi", "TTTTETT");
    }

    public void testIf4Command() throws ShellSyntaxException, CompletionException {
        doCompletionTest("if cpuid ; then\necho hi ; fi", "TTTTTETT");
    }

    public void testIf5Command() throws ShellSyntaxException, CompletionException {
        doCompletionTest("if cpuid ; then\necho hi\nfi", "TTTTTET");
    }

    public void testIfElseCommand() throws ShellSyntaxException, CompletionException {
        doCompletionTest("if cpuid ; then echo hi ; else echo ho ; fi", "TTTTTETTTETT");
    }

    public void testIfElse2Command() throws ShellSyntaxException, CompletionException {
        doCompletionTest("if cpuid ; then echo hi ; else\necho ho ; fi", "TTTTTETTTETT");
    }

    public void testIfElse3Command() throws ShellSyntaxException, CompletionException {
        doCompletionTest("if cpuid ; then echo hi ; else echo ho\nfi", "TTTTTETTTET");
    }

    public void testIfElifCommand() throws ShellSyntaxException, CompletionException {
        doCompletionTest("if cpuid ; then echo hi ; elif cpuid ; then echo ho ; fi", "TTTTTETTTTTTETT");
    }

    public void testIfElif2Command() throws ShellSyntaxException, CompletionException {
        doCompletionTest("if cpuid ; then echo hi ; elif\ncpuid ; then echo ho ; fi", "TTTTTETTTTTTETT");
    }

    public void testIfElif3Command() throws ShellSyntaxException, CompletionException {
        doCompletionTest("if cpuid ; then echo hi ; elif cpuid\nthen echo ho ; fi", "TTTTTETTTTTETT");
    }

    public void testIfElif4Command() throws ShellSyntaxException, CompletionException {
        doCompletionTest("if cpuid ; then echo hi ; elif cpuid ; then\necho ho ; fi", "TTTTTETTTTTTETT");
    }

    public void testWhileCommand() throws ShellSyntaxException, CompletionException {
        doCompletionTest("while cpuid ; do echo hi ; done", "TTTTTETT");
    }

    public void testWhile2Command() throws ShellSyntaxException, CompletionException {
        doCompletionTest("while\ncpuid ; do echo hi ; done", "TTTTTETT");
    }
    
    public void testWhile3Command() throws ShellSyntaxException, CompletionException {
        doCompletionTest("while cpuid\ndo echo hi ; done", "TTTTETT");
    }

    public void testWhile4Command() throws ShellSyntaxException, CompletionException {
        doCompletionTest("while cpuid ; do\necho hi ; done", "TTTTTETT");
    }
    
    public void testWhile5Command() throws ShellSyntaxException, CompletionException {
        doCompletionTest("while cpuid ; do echo hi\ndone", "TTTTTET");
    }
   
    public void testForCommand() throws ShellSyntaxException, CompletionException {
        doCompletionTest("for X in 1 2 3 ; do echo hi ; done", "TFTEEETTTETT");
    }
   
    public void testFor2Command() throws ShellSyntaxException, CompletionException {
        doCompletionTest("for X in 1 2 3\ndo echo hi ; done", "TFTEEETTETT");
    }

    public void testFor3Command() throws ShellSyntaxException, CompletionException {
        doCompletionTest("for X in 1 2 3 ; do\necho hi ; done", "TFTEEETTTETT");
    }
   
    public void testFor4Command() throws ShellSyntaxException, CompletionException {
        doCompletionTest("for X in 1 2 3 ; do echo hi\ndone", "TFTEEETTTET");
    }
   
    public void testCaseCommand() throws ShellSyntaxException, CompletionException {
        doCompletionTest("case 3 in ( 1 | 2 ) echo hi ;; 3 ) echo bye ; esac", "TFTTFTFTTETETTETT");
    }
    
    public void testCase2Command() throws ShellSyntaxException, CompletionException {
        doCompletionTest("case 3\nin ( 1 | 2 ) echo hi ;; 3 ) echo bye ; esac", "TFTTFTFTTETETTETT");
    }
    
    public void testCase3Command() throws ShellSyntaxException, CompletionException {
        doCompletionTest("case 3 in\n( 1 | 2 ) echo hi ;; 3 ) echo bye ; esac", "TFTTFTFTTETETTETT");
    }
    
    public void testCase4Command() throws ShellSyntaxException, CompletionException {
        doCompletionTest("case 3 in ( 1 | 2 )\necho hi ;; 3 ) echo bye ; esac", "TFTTFTFTTETETTETT");
    }
    
    public void testCase5Command() throws ShellSyntaxException, CompletionException {
        doCompletionTest("case 3 in ( 1 | 2 ) echo hi\n;; 3 ) echo bye ; esac", "TFTTFTFTTETETTETT");
    }
    
    public void testCase6Command() throws ShellSyntaxException, CompletionException {
        doCompletionTest("case 3 in ( 1 | 2 ) echo hi ;;\n3 ) echo bye ; esac", "TFTTFTFTTETETTETT");
    }
    
    public void testCase7Command() throws ShellSyntaxException, CompletionException {
        doCompletionTest("case 3 in ( 1 | 2 ) echo hi ;; 3 )\necho bye ; esac", "TFTTFTFTTETETTETT");
    }
    
    public void testCase8Command() throws ShellSyntaxException, CompletionException {
        doCompletionTest("case 3 in ( 1 | 2 ) echo hi ;; 3 ) echo bye\nesac", "TFTTFTFTTETETTET");
    }
    
    public void testBadCommand() throws ShellSyntaxException, CompletionException {
        doCompletionTest("cpuid hi", "TE");
    }
    
    public void testBad2Command() throws ShellSyntaxException, CompletionException {
        try {
            doCompletionTest("if fi ;", "T??");
        } catch (CompletionException ex) {
            assertEquals("Cannot find an alias or load a command class for 'fi'", ex.getMessage());
        }
    }
    
    public void testRedirCommand() throws ShellSyntaxException, CompletionException {
        doCompletionTest("echo hi > /", "TETZ");
    }
   
    private void doCompletionTest(String input, String flags) 
        throws ShellSyntaxException, CompletionException {
        BjorneInterpreter interpreter = new BjorneInterpreter();
        for (int i = 0; i <= input.length(); i++) {
            String partial = input.substring(0, i);
            int inWord = 0;
            int wordStart = 0;
            for (int j = 0; j < i; j++) {
                if (Character.isWhitespace(partial.charAt(j))) {
                    inWord++;
                    wordStart = j + 1;
                }
            }
            String lastWord = partial.substring(wordStart);
            Completable completable = interpreter.parsePartial(shell, partial);
            CommandCompletions completions = new CommandCompletions();
            completable.complete(completions, shell);
            Set<String> completionWords = completions.getCompletions();
            switch (flags.charAt(inWord)) {
                case 'T':
                    // Expect completions
                    assertTrue("got no completions: " + diag(partial, completions), 
                            completionWords.size() > 0);
                    break;
                case 'F':
                    // Expect no completions
                    assertTrue("got unexpected completions: " + diag(partial, completions),
                            completionWords.size() == 0);
                    break;
                case 'E':
                    // Expect completions if the last char is ' ', otherwise not
                    if (wordStart >= partial.length()) {
                        assertTrue("got no completions: " + diag(partial, completions),
                                completionWords.size() > 0);
                    } else {
                        assertTrue("got unexpected completions: " + diag(partial, completions), 
                                completionWords.size() == 0);
                    }
                    break;
                case 'Z':
                    // Expect completions if the last char is NOT ' '
                    if (wordStart >= partial.length()) {
                        //
                    } else {
                        assertTrue("got no completions: " + diag(partial, completions), 
                                completionWords.size() > 0);
                    }
                    break;
                case '?':
                    // Maybe completions, maybe not
            }
            for (String completionWord : completionWords) {
                if (!completionWord.startsWith(lastWord)) {
                    fail("completion(s) don't start with '" + lastWord + "': " + 
                            diag(partial, completions));
                }
            }
        }
    }

    private String diag(String partial, CommandCompletions completions) {
        return "partial = '" + partial + "', completions = " + completions; 
    }
}
