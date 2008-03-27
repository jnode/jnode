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
package org.jnode.test.shell;

import java.util.Arrays;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.CommandShell;
import org.jnode.shell.DefaultCommandInvoker;
import org.jnode.shell.DefaultInterpreter;
import org.jnode.shell.ProcletCommandInvoker;
import org.jnode.shell.RedirectingInterpreter;
import org.jnode.shell.ShellUtils;
import org.jnode.shell.ThreadCommandInvoker;
import org.jnode.shell.alias.AliasManager;
import org.jnode.shell.syntax.AlternativesSyntax;
import org.jnode.shell.syntax.ArgumentSyntax;
import org.jnode.shell.syntax.EmptySyntax;
import org.jnode.shell.syntax.OptionSyntax;
import org.jnode.shell.syntax.SequenceSyntax;
import org.jnode.shell.syntax.SyntaxManager;
import org.jnode.test.shell.syntax.TestAliasManager;
import org.jnode.test.shell.syntax.TestSyntaxManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import junit.framework.TestCase;

/**
 * Test command completion using various interpreters and commands.
 * 
 * @author crawley@jnode.org
 */
public class CompletionTest extends TestCase {
    
    private String userDirName = System.getProperty("user.dir");
    private File testDir;
    private String[] aliasCompletions;
    
    static {
        try {
            Cassowary.initEnv();
        }
        catch (NamingException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected void setUp() throws Exception {
        // Setup a temporary home directory for filename completion
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        testDir = new File(tempDir, "CompletionTestDir");
        testDir.mkdir();
        touch(testDir, "One");
        touch(testDir, "Two");
        touch(testDir, "Three");
        new File(testDir, "Four").mkdir();
        System.setProperty("user.dir", testDir.getAbsolutePath());
    }
    
    private void touch(File dir, String name) throws IOException {
        File file = new File(dir, name);
        FileWriter fw = new FileWriter(file);
        fw.close();
    }

    @Override
    protected void tearDown() throws Exception {
        for (File f : testDir.listFiles()) {
            f.delete();
        }
        testDir.delete();
        System.setProperty("user.dir", userDirName);
    }

    public class TestCommandShell extends CommandShell {
        
        public TestCommandShell() throws NameNotFoundException {
            super(new TestAliasManager(), new TestSyntaxManager());
            ShellUtils.getShellManager().registerShell(this);

            ShellUtils.registerCommandInvoker(DefaultCommandInvoker.FACTORY);
            ShellUtils.registerCommandInvoker(ThreadCommandInvoker.FACTORY);
            ShellUtils.registerCommandInvoker(ProcletCommandInvoker.FACTORY);
            ShellUtils.registerCommandInterpreter(DefaultInterpreter.FACTORY);
            ShellUtils.registerCommandInterpreter(RedirectingInterpreter.FACTORY);

            AliasManager am = this.getAliasManager();
            am.add("gc", "org.jnode.shell.command.GcCommand");
            am.add("cpuid", "org.jnode.shell.command.system.CpuIDCommand");
            am.add("set", "org.jnode.shell.command.SetCommand");
            am.add("dir", "org.jnode.test.shell.MyDirCommand");
            am.add("duh", "org.jnode.test.shell.MyDuhCommand");
            am.add("cat", "org.jnode.test.shell.MyCatCommand");
            am.add("alias", "org.jnode.test.shell.MyAliasCommand");
            aliasCompletions = new String[]{"alias ", "cat ", "cpuid ", "dir ", "duh ", "gc ", "set "};
            
            SyntaxManager sm = this.getSyntaxManager();
            sm.add("set", new SequenceSyntax(new ArgumentSyntax("key"), new ArgumentSyntax("value")));
            sm.add("duh", new ArgumentSyntax("path"));
            sm.add("cpuid", new SequenceSyntax());
            sm.add("alias", new AlternativesSyntax(
                    new EmptySyntax(null, "Print all available aliases and corresponding classnames"),
                    new SequenceSyntax(null, "Set an aliases for given classnames",
                            new ArgumentSyntax("alias"), new ArgumentSyntax("classname")),
                    new OptionSyntax("remove", 'r', "Remove an alias")));
        }
    }
    
    public void testDefaultInterpreterOldSyntax() throws Exception {
        TestCommandShell cs = new TestCommandShell();
        cs.setCommandInterpreter("default");
        
        checkCompletions(cs.complete(""), aliasCompletions, 0);
        checkCompletions(cs.complete("di"), new String[]{"dir "}, 0);
        checkCompletions(cs.complete("dir"), new String[]{"dir "}, 0);
        checkCompletions(cs.complete("dir "), new String[]{"Four/", "One ", "Three ", "Two ", }, 4);
        checkCompletions(cs.complete("dir T"), new String[]{"Three ", "Two "}, 4);
        
        // The default interpreter doesn't recognize '>' '<' or '|' as anything special.
        // Therefore it should just try to complete them as filenames ... and fail.
        checkCompletions(cs.complete("dir |"), new String[]{}, 4);
        checkCompletions(cs.complete("dir | "), new String[]{}, 6); // dir takes one argument only
        checkCompletions(cs.complete("cat | "), new String[]{"Four/", "One ", "Three ", "Two ", }, 6);
        checkCompletions(cs.complete("cat | ca"), new String[]{}, 6);
        checkCompletions(cs.complete("cat >"), new String[]{}, 4);
        checkCompletions(cs.complete("cat > "), new String[]{"Four/", "One ", "Three ", "Two ", }, 6);
        checkCompletions(cs.complete("cat <"), new String[]{}, 4);
        checkCompletions(cs.complete("cat < "), new String[]{"Four/", "One ", "Three ", "Two ", }, 6);
    }
    
    public void testOldSyntaxOptions() throws Exception {
        TestCommandShell cs = new TestCommandShell();
        cs.setCommandInterpreter("default");
        
        // For bug #4418
        checkCompletions(cs.complete("cat -"), new String[]{"-u "}, -2);
        checkCompletions(cs.complete("cat -u"), new String[]{"-u "}, -2);
        
        // And some more ...
    }
    
    public void testRedirectingInterpreterOldSyntax() throws Exception {
        TestCommandShell cs = new TestCommandShell();
        cs.setCommandInterpreter("redirecting");
        
        checkCompletions(cs.complete(""), aliasCompletions, 0);
        checkCompletions(cs.complete("di"), new String[]{"dir "}, 0);
        checkCompletions(cs.complete("dir"), new String[]{"dir "}, 0);
        checkCompletions(cs.complete("dir "), new String[]{"Four/", "One ", "Three ", "Two ", }, 4);
        checkCompletions(cs.complete("dir T"), new String[]{"Three ", "Two "}, 4);
        checkCompletions(cs.complete("dir |"), aliasCompletions, 5);
        checkCompletions(cs.complete("dir | "), aliasCompletions, 6);
        checkCompletions(cs.complete("cat |"), aliasCompletions, 5);
        checkCompletions(cs.complete("cat | "), aliasCompletions, 6);
        checkCompletions(cs.complete("cat | ca"), new String[]{"cat "}, 6);
        checkCompletions(cs.complete("cat >"), new String[]{"Four/", "One ", "Three ", "Two ", }, 5);
        checkCompletions(cs.complete("cat > "), new String[]{"Four/", "One ", "Three ", "Two ", }, 6);
        checkCompletions(cs.complete("cat <"), new String[]{"Four/", "One ", "Three ", "Two ", }, 5);
        checkCompletions(cs.complete("cat < "), new String[]{"Four/", "One ", "Three ", "Two ", }, 6);
    }
    
    public void testDefaultInterpreterNewSyntax() throws Exception {
        TestCommandShell cs = new TestCommandShell();
        cs.setCommandInterpreter("default");
        
        final String[] propertyCompletions = getExpectedPropertyNameCompletions();

        checkCompletions(cs.complete("set "), propertyCompletions, 4);
        checkCompletions(cs.complete("set a"), new String[]{}, 4);
        checkCompletions(cs.complete("set u"), new String[]{
            "user.country ", "user.dir ", "user.home ", 
            "user.language ", "user.name ", "user.timezone ", }, 4);
        checkCompletions(cs.complete("set a "), new String[]{}, 6);
        checkCompletions(cs.complete("set a b"), new String[]{}, 6);
        checkCompletions(cs.complete("set a b "), new String[]{}, 8);
        
        checkCompletions(cs.complete("cpuid "), new String[]{}, 6);
        
        checkCompletions(cs.complete("duh "), new String[]{"Four/", "One ", "Three ", "Two ", }, 4);
        checkCompletions(cs.complete("duh T"), new String[]{"Three ", "Two "}, 4);
        
        checkCompletions(cs.complete("alias -"), new String[]{"-r "}, 6);
        
        String[] aliasesPlusR = new String[aliasCompletions.length + 1];
        System.arraycopy(aliasCompletions, 0, aliasesPlusR, 1, aliasCompletions.length);
        aliasesPlusR[0] = "-r ";
        checkCompletions(cs.complete("alias "), aliasesPlusR, 6);
    }
    
    /**
     * Snarf the system property names in the form we expect for
     * property name completion.
     */
    private String[] getExpectedPropertyNameCompletions() {
        TreeSet<String> tmp = new TreeSet<String>();
        for (Object key : System.getProperties().keySet()) {
            tmp.add(key + " ");
        }
        return tmp.toArray(new String[tmp.size()]);
    }

    private void checkCompletions(CompletionInfo ci, String[] expected, int startPos) {
        SortedSet<String> completions = ci.getCompletions();
        if (completions.size() != expected.length) {
            err("Wrong number of completions", expected, completions);
        }
        int i = 0;
        for (String completion : completions) {
            if (!completion.equals(expected[i])) {
                err("Mismatch for completion #" + i, expected, completions);
            }
            i++;
        }
        assertEquals(startPos, ci.getCompletionStart());
    }
    
    private void err(String message, String[] expected, Collection<String> actual) {
        System.err.println(message);
        System.err.println("Expected completions:");
        showList(Arrays.asList(expected));
        System.err.println("Actual completions:");
        showList(actual);
        fail(message);
    }
    
    private void showList(Collection<String> list) {
        for (String element : list) {
            System.err.println("    '" + element + "'");
        }
    }
    
}
