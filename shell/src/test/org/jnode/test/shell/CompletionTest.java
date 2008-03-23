package org.jnode.test.shell;

import java.util.Arrays;
import java.util.Collection;
import java.util.SortedSet;

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
    
    @Override
    protected void setUp() throws Exception {
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

    public static class TestCommandShell extends CommandShell {
        
        static {
            try {
                Cassowary.initEnv();
            }
            catch (NamingException ex) {
                throw new RuntimeException(ex);
            }
        }

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
            am.add("dir", "org.jnode.test.shell.MyDirCommand");
            am.add("cat", "org.jnode.test.shell.MyCatCommand");
        }
    }
    
    public void testDefaultInterpreterOldSyntax() throws Exception {
        TestCommandShell cs = new TestCommandShell();
        cs.setCommandInterpreter("default");
        
        checkCompletions(cs.complete(""), new String[]{"cat ", "dir ", "gc "});
        checkCompletions(cs.complete("di"), new String[]{"dir "});
        checkCompletions(cs.complete("dir"), new String[]{"dir "});
        checkCompletions(cs.complete("dir "), new String[]{"Four/", "One ", "Three ", "Two ", });
        checkCompletions(cs.complete("dir T"), new String[]{"Three ", "Two "});
        
        // The default interpreter doesn't recognize '>' '<' or '|' as anything special.
        // Therefore it should just try to complete them as filenames ... and fail.
        checkCompletions(cs.complete("dir |"), new String[]{});
        checkCompletions(cs.complete("dir | "), new String[]{}); // dir takes one argument only
        checkCompletions(cs.complete("cat | "), new String[]{"Four/", "One ", "Three ", "Two ", });
        checkCompletions(cs.complete("cat | ca"), new String[]{});
        checkCompletions(cs.complete("cat >"), new String[]{});
        checkCompletions(cs.complete("cat > "), new String[]{"Four/", "One ", "Three ", "Two ", });
        checkCompletions(cs.complete("cat <"), new String[]{});
        checkCompletions(cs.complete("cat < "), new String[]{"Four/", "One ", "Three ", "Two ", });
    }
    
    public void testRedirectingInterpreterOldSyntax() throws Exception {
        TestCommandShell cs = new TestCommandShell();
        cs.setCommandInterpreter("redirecting");
        
        checkCompletions(cs.complete(""), new String[]{"cat ", "dir ", "gc "});
        checkCompletions(cs.complete("di"), new String[]{"dir "});
        checkCompletions(cs.complete("dir"), new String[]{"dir "});
        checkCompletions(cs.complete("dir "), new String[]{"Four/", "One ", "Three ", "Two ", });
        checkCompletions(cs.complete("dir T"), new String[]{"Three ", "Two "});
        checkCompletions(cs.complete("dir |"), new String[]{"cat ", "dir ", "gc "});
        checkCompletions(cs.complete("dir | "), new String[]{"cat ", "dir ", "gc "});
        checkCompletions(cs.complete("cat |"), new String[]{"cat ", "dir ", "gc "});
        checkCompletions(cs.complete("cat | "), new String[]{"cat ", "dir ", "gc "});
        checkCompletions(cs.complete("cat | ca"), new String[]{"cat "});
        checkCompletions(cs.complete("cat >"), new String[]{"Four/", "One ", "Three ", "Two ", });
        checkCompletions(cs.complete("cat > "), new String[]{"Four/", "One ", "Three ", "Two ", });
        checkCompletions(cs.complete("cat <"), new String[]{"Four/", "One ", "Three ", "Two ", });
        checkCompletions(cs.complete("cat < "), new String[]{"Four/", "One ", "Three ", "Two ", });
    }
    
    private void checkCompletions(CompletionInfo ci, String[] expected) {
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
