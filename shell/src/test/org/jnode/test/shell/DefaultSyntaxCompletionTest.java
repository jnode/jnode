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

import static org.jnode.test.shell.CompletionHelper.checkCompletions;

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
import org.jnode.shell.RedirectingInterpreter;
import org.jnode.shell.ShellUtils;
import org.jnode.shell.ThreadCommandInvoker;
import org.jnode.shell.alias.AliasManager;
import org.jnode.shell.proclet.ProcletCommandInvoker;
import org.jnode.test.shell.syntax.TestAliasManager;
import org.jnode.test.shell.syntax.TestSyntaxManager;

/**
 * Test command completion using the default syntax created by a command's ArgumentBundle.
 *
 * @author crawley@jnode.org
 */
public class DefaultSyntaxCompletionTest extends TestCase {

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
            am.add("duh", "org.jnode.test.shell.MyDuhCommand");
            am.add("alias", "org.jnode.test.shell.MyAliasCommand");
            am.add("compile", "org.jnode.test.shell.MyCompileCommand");
            aliasCompletions = new String[]{"alias ", "compile ", "cpuid ", "duh ", "gc ", "set "};
        }
    }

    public void testDefaultSyntax() throws Exception {
        TestCommandShell cs = new TestCommandShell();
        cs.setCommandInterpreter("default");

        final String[] propertyCompletions = getExpectedPropertyNameCompletions();

        checkCompletions(cs, "set ", new String[]{"--key ", "--value "}, -1);
        checkCompletions(cs, "set -", new String[]{"--key ", "--value "}, 4);
        checkCompletions(cs, "set --v", new String[]{"--value "}, 4);
        checkCompletions(cs, "set --key", new String[]{"--key "}, 4);
        checkCompletions(cs, "set --key ", propertyCompletions, -1);
        checkCompletions(cs, "set --key foo --v", new String[]{"--value "}, 14);

        checkCompletions(cs, "set --key u", new String[]{
            "user.country ", "user.dir ", "user.home ",
            "user.language ", "user.name ", "user.timezone "}, 10);

        checkCompletions(cs, "cpuid ", new String[]{}, -1);

        checkCompletions(cs, "duh ", new String[]{"--path "}, -1);
        checkCompletions(cs, "duh -", new String[]{"--path "}, 4);
        checkCompletions(cs, "duh --path ", new String[]{"Four/", "One ", "Three ", "Two "}, -1);
        checkCompletions(cs, "duh --path T", new String[]{"Three ", "Two "}, 11);

        checkCompletions(cs, "alias ", new String[]{"--alias ", "--classname ", "--remove "}, -1);
        checkCompletions(cs, "alias --remove ", aliasCompletions, -1);

        checkCompletions(cs, "compile ", new String[]{"--className ", "--level ", "--test "}, -1);
        checkCompletions(cs, "compile --level ", new String[]{"0 ", "1 ", "2 ", "3 ", "4 ", "5 "}, -1);
        checkCompletions(cs, "compile --test ", new String[]{"--className ", "--level ", "--test "}, -1);
        checkCompletions(cs, "compile --className", new String[]{"--className "}, 8);
        checkCompletions(cs, "compile --className ", new String[]{}, -1);
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
