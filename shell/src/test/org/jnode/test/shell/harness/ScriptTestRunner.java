/**
 * 
 */
package org.jnode.test.shell.harness;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;


/**
 * This TestRunner runs a script
 * 
 * @author crawley@jnode.org
 */
class ScriptTestRunner extends JNodeTestRunnerBase implements TestRunnable {

    
    
    private File tempScriptFile;
    
    public ScriptTestRunner(TestSpecification spec, TestHarness harness) {
        super(spec, harness);
    }

    @Override
    public int run() throws Exception {
//      String[] args = spec.getArgs().toArray(new String[0]);
//      CommandLine cmdLine = new CommandLine(spec.getCommand(), args);
        tempScriptFile = new File(System.getProperty("java.io.tmpdir"), spec.getCommand());
        Writer w = null;
        try {
            w = new FileWriter(tempScriptFile);
            w.write(spec.getScriptContent());
            w.write('\n');
        } finally {
            w.close();
        }
        int rc = getShell().runCommandFile(tempScriptFile);
        return check(rc) ? 0 : 1;
    }

    private boolean check(int rc) {
        return 
            // harness.expect(rc, spec.getRc(), "return code") && 
            harness.expect(outBucket.toString(), spec.getOutputContent(), "output content") && 
            harness.expect(errBucket.toString(), spec.getErrorContent(), "err content");
    }
    
    @Override
    public void cleanup() {
        if (tempScriptFile != null) {
            tempScriptFile.delete();
        }
        super.cleanup();
    }
    
}