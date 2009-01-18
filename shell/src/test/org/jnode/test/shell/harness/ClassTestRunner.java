/**
 * 
 */
package org.jnode.test.shell.harness;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;

/**
 * This TestRunner runs a a class by calling its 'static void main(Sting[])' entry
 * point.  Note that classes that call System.exit(status) are problematic.
 * 
 * @author crawley@jnode.org
 */
class ClassTestRunner implements TestRunnable {

    private ByteArrayOutputStream outBucket;
    private ByteArrayOutputStream errBucket;
    
    private final TestSpecification spec;
    private final TestHarness harness;
    
    public ClassTestRunner(TestSpecification spec, TestHarness harness) {
        this.spec = spec;
        this.harness = harness;
    }

    @Override
    public int run() throws Exception {
        Class<?> commandClass = Class.forName(spec.getCommand());
        Method method = commandClass.getMethod("main", String[].class);
        String[] args = spec.getArgs().toArray(new String[0]);
        method.invoke(null, (Object) args);
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