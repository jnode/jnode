package org.jnode.test.shell.harness;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.jnode.naming.InitialNaming;
import org.jnode.plugin.PluginManager;
import org.jnode.shell.CommandShell;
import org.jnode.shell.ShellException;
import org.jnode.test.shell.harness.TestSpecification.PluginSpec;

public abstract class JNodeTestRunnerBase implements TestRunnable {
    protected ByteArrayOutputStream outBucket;
    protected ByteArrayOutputStream errBucket;
    
    protected final TestSpecification spec;
    protected final TestHarness harness;
    
    protected final boolean usingEmu;

    public JNodeTestRunnerBase(TestSpecification spec, TestHarness harness) {
        super();
        this.spec = spec;
        this.harness = harness;
        this.usingEmu = TestEmu.initEmu(harness.getRoot());
    }
    
    public CommandShell getShell() throws ShellException {
        CommandShell shell = TestEmu.getShell(); 
        if (shell == null) {
            shell = new TestCommandShell(System.in, System.out, System.err);
            shell.configureShell();
        }
        return shell;
    }
    
    @Override
    public void cleanup() {
    }

    @Override
    public void setup() {
        for (PluginSpec plugin : spec.getRequiredPlugins()) {
            ensurePluginLoaded(plugin);
        }
        System.setIn(new ByteArrayInputStream(spec.getInputContent().getBytes()));
        outBucket = new ByteArrayOutputStream();
        errBucket = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outBucket));
        System.setErr(new PrintStream(errBucket));
    }
    
    protected void ensurePluginLoaded(PluginSpec pluginSpec) {
        if (usingEmu) {
            TestEmu.loadPseudoPlugin(pluginSpec.pseudoPluginClassName);
        } else {
            String ver = (pluginSpec.pluginVersion.length() == 0) ? 
                    System.getProperty("os.version") : pluginSpec.pluginVersion;
            try {
                PluginManager mgr = InitialNaming.lookup(PluginManager.NAME);
                mgr.getRegistry().loadPlugin(mgr.getLoaderManager(), pluginSpec.pluginId, ver);
            } catch (Exception ex) {
                throw new RuntimeException("Cannot load plugin '" + pluginSpec.pluginId + "/" + ver + "'");
            }
        }
    }
}
