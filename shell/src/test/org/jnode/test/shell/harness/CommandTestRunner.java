/**
 * 
 */
package org.jnode.test.shell.harness;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.jnode.shell.CommandInfo;
import org.jnode.shell.CommandInvoker;
import org.jnode.shell.CommandLine;
import org.jnode.shell.CommandShell;
import org.jnode.shell.ShellUtils;
import org.jnode.shell.ThreadCommandInvoker;
import org.jnode.shell.alias.AliasManager;
import org.jnode.shell.alias.NoSuchAliasException;


/**
 * This TestRunner runs a class by calling its 'static void main(String[])' entry
 * point.  Note that classes that call System.exit(status) are problematic.
 * 
 * @author crawley@jnode.org
 */
class CommandTestRunner implements TestRunnable {

    private ByteArrayOutputStream outBucket;
    private ByteArrayOutputStream errBucket;
    
    private final TestSpecification spec;
    private final TestHarness harness;
    private final CommandShell shell;
    
    @SuppressWarnings("unused")
    private final boolean usingEmu;
    
    public CommandTestRunner(TestSpecification spec, TestHarness harness) {
        this.spec = spec;
        this.harness = harness;
        this.usingEmu = TestEmu.initEmu(harness.getRoot());
        this.shell = TestEmu.getShell();
    }

    @Override
    public int run() throws Exception {
        String[] args = spec.getArgs().toArray(new String[0]);
        // FIXME change this to a shell provided by getShell???
        AliasManager aliasMgr = ShellUtils.getAliasManager();
        CommandInvoker invoker = new ThreadCommandInvoker(shell);
        CommandLine cmdLine = new CommandLine(spec.getCommand(), args);
        CommandInfo cmdInfo;
        try {
            Class<?> cls = aliasMgr.getAliasClass(spec.getCommand());
            cmdInfo = new CommandInfo(cls, aliasMgr.isInternal(spec.getCommand()));
        } catch (NoSuchAliasException ex) {
            final ClassLoader cl = 
                Thread.currentThread().getContextClassLoader();
            cmdInfo = new CommandInfo(cl.loadClass(spec.getCommand()), false);
        }
        int rc = invoker.invoke(cmdLine, cmdInfo);
        return check(rc) ? 0 : 1;
    }

    private boolean check(int rc) {
        return 
            harness.expect(rc, spec.getRc(), "return code") && 
            harness.expect(outBucket.toString(), spec.getOutputContent(), "output content") && 
            harness.expect(errBucket.toString(), spec.getErrorContent(), "err content");
    }
    
    @Override
    public void cleanup() {
    }

    @Override
    public void setup() {
        System.setIn(new ByteArrayInputStream(spec.getInputContent().getBytes()));
        outBucket = new ByteArrayOutputStream();
        errBucket = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outBucket));
        System.setErr(new PrintStream(errBucket));
    }
    
}