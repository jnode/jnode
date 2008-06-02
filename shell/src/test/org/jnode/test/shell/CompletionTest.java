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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeSet;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import junit.framework.TestCase;
import org.jnode.shell.CommandShell;
import org.jnode.shell.DefaultCommandInvoker;
import org.jnode.shell.DefaultInterpreter;
import org.jnode.shell.ProcletCommandInvoker;
import org.jnode.shell.RedirectingInterpreter;
import org.jnode.shell.ShellUtils;
import org.jnode.shell.ThreadCommandInvoker;
import org.jnode.shell.alias.AliasManager;
import org.jnode.shell.syntax.ArgumentSyntax;
import org.jnode.shell.syntax.EmptySyntax;
import org.jnode.shell.syntax.OptionSyntax;
import org.jnode.shell.syntax.SequenceSyntax;
import org.jnode.shell.syntax.SyntaxBundle;
import org.jnode.shell.syntax.SyntaxManager;
import static org.jnode.test.shell.CompletionHelper.checkCompletions;
import org.jnode.test.shell.syntax.TestAliasManager;
import org.jnode.test.shell.syntax.TestSyntaxManager;

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
        } catch (NamingException ex) {
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
            sm.add(new SyntaxBundle("set",
                new SequenceSyntax(new ArgumentSyntax("key"), new ArgumentSyntax("value"))));
            sm.add(new SyntaxBundle("duh", new ArgumentSyntax("path")));
            sm.add(new SyntaxBundle("cpuid", new SequenceSyntax()));
            sm.add(new SyntaxBundle("alias",
                new EmptySyntax(null, "Print all available aliases and corresponding classnames"),
                new SequenceSyntax(null, "Set an aliases for given classnames",
                    new ArgumentSyntax("alias"), new ArgumentSyntax("classname")),
                new OptionSyntax("remove", 'r', "Remove an alias")));
        }
    }

    public void testDefaultInterpreterOldSyntax() throws Exception {
        TestCommandShell cs = new TestCommandShell();
        cs.setCommandInterpreter("default");

        checkCompletions(cs, "", aliasCompletions, -1);
        checkCompletions(cs, "di", new String[]{"dir "}, 0);
        checkCompletions(cs, "dir", new String[]{"dir "}, 0);
        checkCompletions(cs, "dir ", new String[]{"Four/", "One ", "Three ", "Two "}, -1);
        checkCompletions(cs, "dir T", new String[]{"Three ", "Two "}, 4);

        // The default interpreter doesn't recognize '>' '<' or '|' as anything special.
        // Therefore it should just try to complete them as filenames ... and fail.
        checkCompletions(cs, "dir |", new String[]{}, -1);
        checkCompletions(cs, "dir | ", new String[]{}, -1); // dir takes one argument only
        checkCompletions(cs, "cat | ", new String[]{"Four/", "One ", "Three ", "Two "}, -1);
        checkCompletions(cs, "cat | ca", new String[]{}, -1);
        checkCompletions(cs, "cat >", new String[]{}, -1);
        checkCompletions(cs, "cat > ", new String[]{"Four/", "One ", "Three ", "Two "}, -1);
        checkCompletions(cs, "cat <", new String[]{}, -1);
        checkCompletions(cs, "cat < ", new String[]{"Four/", "One ", "Three ", "Two "}, -1);
    }

    public void testOldSyntaxOptions() throws Exception {
        TestCommandShell cs = new TestCommandShell();
        cs.setCommandInterpreter("default");

        // For bug #4418
        checkCompletions(cs, "cat -", new String[]{"-u "}, -2);
        checkCompletions(cs, "cat -u", new String[]{"-u "}, -2);

        // And some more ...
    }

    public void testRedirectingInterpreterOldSyntax() throws Exception {
        TestCommandShell cs = new TestCommandShell();
        cs.setCommandInterpreter("redirecting");

        checkCompletions(cs, "", aliasCompletions, -1);
        checkCompletions(cs, "di", new String[]{"dir "}, 0);
        checkCompletions(cs, "dir", new String[]{"dir "}, 0);
        checkCompletions(cs, "dir ", new String[]{"Four/", "One ", "Three ", "Two "}, -1);
        checkCompletions(cs, "dir T", new String[]{"Three ", "Two "}, 4);
        checkCompletions(cs, "dir |", aliasCompletions, -1);
        checkCompletions(cs, "dir | ", aliasCompletions, -1);
        checkCompletions(cs, "cat |", aliasCompletions, -1);
        checkCompletions(cs, "cat | ", aliasCompletions, -1);
        checkCompletions(cs, "cat | ca", new String[]{"cat "}, 6);
        checkCompletions(cs, "cat >", new String[]{"Four/", "One ", "Three ", "Two "}, -1);
        checkCompletions(cs, "cat > ", new String[]{"Four/", "One ", "Three ", "Two "}, -1);
        checkCompletions(cs, "cat <", new String[]{"Four/", "One ", "Three ", "Two "}, -1);
        checkCompletions(cs, "cat < ", new String[]{"Four/", "One ", "Three ", "Two "}, -1);
    }

    public void testDefaultInterpreterNewSyntax() throws Exception {
        TestCommandShell cs = new TestCommandShell();
        cs.setCommandInterpreter("default");

        final String[] propertyCompletions = getExpectedPropertyNameCompletions();

        checkCompletions(cs, "set ", propertyCompletions, -1);
        checkCompletions(cs, "set a", new String[]{}, -1);
        checkCompletions(cs, "set u", new String[]{
            "user.country ", "user.dir ", "user.home ",
            "user.language ", "user.name ", "user.timezone "}, 4);
        checkCompletions(cs, "set a ", new String[]{}, -1);
        checkCompletions(cs, "set a b", new String[]{}, 6);
        checkCompletions(cs, "set a b ", new String[]{}, -1);

        checkCompletions(cs, "cpuid ", new String[]{}, -1);

        checkCompletions(cs, "duh ", new String[]{"Four/", "One ", "Three ", "Two "}, -1);
        checkCompletions(cs, "duh T", new String[]{"Three ", "Two "}, 4);

        checkCompletions(cs, "alias -", new String[]{"-r "}, 6);

        String[] aliasesPlusR = new String[aliasCompletions.length + 1];
        System.arraycopy(aliasCompletions, 0, aliasesPlusR, 1, aliasCompletions.length);
        aliasesPlusR[0] = "-r ";
        checkCompletions(cs, "alias ", aliasesPlusR, -1);
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

}
