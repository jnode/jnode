/**
 * 
 */
package org.jnode.test.shell.harness;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Method;

import org.jnode.shell.CommandInfo;
import org.jnode.shell.CommandInvoker;
import org.jnode.shell.CommandLine;
import org.jnode.shell.CommandShell;
import org.jnode.shell.ShellUtils;
import org.jnode.shell.ThreadCommandInvoker;
import org.jnode.shell.alias.AliasManager;
import org.jnode.shell.alias.NoSuchAliasException;


/**
 * This TestRunner runs a a class by calling its 'static void main(Sting[])' entry
 * point.  Note that classes that call System.exit(status) are problematic.
 * 
 * @author crawley@jnode.org
 */
class CommandTestRunner implements TestRunnable {

    private ByteArrayOutputStream outBucket;
    private ByteArrayOutputStream errBucket;
    
    private final TestSpecification spec;
    private final TestHarness harness;
    
    @SuppressWarnings("unused")
    private final boolean usingEmu;

    private static boolean emuInitialized;
    private static boolean emuAvailable;
    private static CommandShell shell;
    
    public CommandTestRunner(TestSpecification spec, TestHarness harness) {
        this.spec = spec;
        this.harness = harness;
        this.usingEmu = initEmu(harness.getRoot());
    }

    private static synchronized boolean initEmu(File root) {
        if (!emuInitialized) {
            // This is a bit of a hack.  We don't want class loader dependencies
            // on the Emu code because that won't work when we run on JNode.  But
            // we need to use Emu if we are running tests on the dev't platform.
            // The following infers that we are running on the dev't platform if 
            // the 'Emu' class is not loadable.
            try {
                Class<?> cls = Class.forName("org.jnode.emu.Emu");
                Method initMethod = cls.getMethod("initEnv", File.class);
                initMethod.invoke(null, root);
                emuAvailable = true;
            } catch (Throwable ex) {
                // debug ...
                ex.printStackTrace(System.err);
                emuAvailable = false;
            }
            try {
                if (emuAvailable) {
                    shell = new CommandShell();
                } else {
                    shell = (CommandShell) ShellUtils.getCurrentShell();
                }
            } catch (Exception ex) {
                // debug ...
                ex.printStackTrace(System.err);
                throw new RuntimeException(ex);
            }
            emuInitialized = true;
        }
        return emuAvailable;
    }

    @Override
    public int run() throws Exception {
        String[] args = spec.getArgs().toArray(new String[0]);
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
        invoker.invoke(cmdLine, cmdInfo);
        return check() ? 0 : 1;
    }

    private boolean check() {
        // When a class is run this way we cannot capture the RC.
        return 
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