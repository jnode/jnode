/**
 * 
 */
package org.jnode.test.shell.harness;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.jnode.shell.CommandShell;


/**
 * This TestRunner runs a class by calling its 'static void main(String[])' entry
 * point.  Note that classes that call System.exit(status) are problematic.
 * 
 * @author crawley@jnode.org
 */
class CommandTestRunner extends JNodeTestRunnerBase implements TestRunnable {

    private ByteArrayOutputStream outBucket;
    private ByteArrayOutputStream errBucket;
    
        
    public CommandTestRunner(TestSpecification spec, TestHarness harness) {
        super(spec, harness);
    }

    @Override
    public int run() throws Exception {
        StringBuffer sb = new StringBuffer();
        CommandShell shell = getShell();
        sb.append(shell.escapeWord(spec.getCommand()));
        for (String arg : spec.getArgs()) {
            sb.append(" ").append(shell.escapeWord(arg));
        }
        int rc = shell.runCommand(sb.toString());
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