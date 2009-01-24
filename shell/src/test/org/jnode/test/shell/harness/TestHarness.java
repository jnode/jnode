package org.jnode.test.shell.harness;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;

import net.n3.nanoxml.XMLException;

/**
 * This is the entry point class for the command test harness.  Its
 * purpose is to run 'black box' tests on commands and the like.
 * 
 * @author crawley@jnode
 */
public class TestHarness {
    
    private final String commandName = this.getClass().getCanonicalName();

    private final String[] args;
    
    private PrintWriter reportWriter;
    private int testCount;
    private int failureCount;
    private int exceptionCount;
    private TestSpecification spec = null;
    private InputStream savedIn;
    private PrintStream savedOut;
    private PrintStream savedErr;

    private boolean verbose;

    public TestHarness(String[] args) {
        this.args = args;
        this.reportWriter = new PrintWriter(System.err);
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        new TestHarness(args).run();
    }

    private void run() throws Exception {
        // FIXME ... this argument handling is very 'interim'.
        boolean useResources = false;
        int firstArg = 0;
        TestSpecificationParser parser = new TestSpecificationParser();
        List<TestSpecification> specs;
        if (args.length == 0) {
            usage();
            return;
        } else if (args[0].equals("--resource") || 
                args[0].equals("--resources") || 
                args[0].equals("-r")) {
            useResources = true;
            firstArg++;
        } else if (args[0].startsWith("-")) {
            System.err.println("Unrecognized option '" + args[0] + "'");
            usage();
            return;
        }

        if (args.length <= firstArg) {
            System.err.println("Missing arguments");
            usage();
            return;
        }

        for (int i = firstArg; i < args.length; i++) {
            String arg = args[i];
            InputStream is = null;
            try {
                if (useResources) {
                    is = this.getClass().getResourceAsStream(arg);
                } else {
                    is = new FileInputStream(arg);
                }
                specs = parser.parse(is);
                execute(specs);
            } catch (Exception ex) {
                diagnose(ex, arg);
            } 
            finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ex) {
                        // ignore
                    }
                }
            }
        }
        report("Ran " + testCount + " tests with " + failureCount + 
                " test failures and " + exceptionCount + " errors (exceptions)");
    }
    
    private void usage() {
        System.err.println(commandName + " // run tests from builtin specs");
        System.err.println(commandName + " --package <java-package> // run tests from specs on classpath");
        System.err.println(commandName + " <spec-file> ... // run tests from specs read from file system");
    }
    
    private void execute(List<TestSpecification> specs) {
        for (TestSpecification spec : specs) {
            execute(spec);
        }
    }

    private void execute(TestSpecification spec) {
        this.spec = spec;
        reportVerbose("Running test '" + spec.getTitle() + "'");
        testCount++;
        try {
            TestRunnable runner;
            switch (spec.getRunMode()) {
                case AS_CLASS:
                    runner = new ClassTestRunner(spec, this);
                    break;
                case AS_ALIAS:
                    runner = new CommandTestRunner(spec, this);
                    break;
                case AS_SCRIPT:
                    runner = new ScriptTestRunner(spec, this);
                    break;
                default:
                    reportVerbose("Run mode '" + spec.getRunMode() + "' not implemented");
                    return;
            }
            try {
                setup();
                runner.setup();
                failureCount += runner.run();
            } finally {
                runner.cleanup();
                cleanup();
            }
        } catch (Throwable ex) {
            report("Uncaught exception in test '" + spec.getTitle() + "': stacktrace follows.");
            ex.printStackTrace(reportWriter);
            exceptionCount++;
        }
        reportVerbose("Completed test '" + spec.getTitle() + "'");
    }

    private void diagnose(Exception ex, String fileName) throws Exception {
        if (ex instanceof IOException) {
            report("IO error while reading test specification: " + ex.getMessage());
        } else if (ex instanceof XMLException) {
            String msg = ex.getMessage();
            if (msg.equals("Nested Exception")) {
                msg = ((XMLException) ex).getException().getMessage();
            }
            report("XML error in test specification '" + fileName + "' : " + msg);
        } else if (ex instanceof TestSpecificationException) {
            report("Invalid test specification '" + fileName + ": " + ex.getMessage());
        } else {
            throw ex;
        }
    }

    /**
     * Restore the system system streams
     */
    private void cleanup() {
        System.setIn(savedIn);
        System.setOut(savedOut);
        System.setErr(savedErr);
    }

    /**
     * Save the System streams so that they can be restored.
     */
    private void setup() {
        savedIn = System.in;
        savedOut = System.out;
        savedErr = System.err;
    }

    public void report(String message) {
        reportWriter.println(message);
        reportWriter.flush();
    }
    
    public void reportVerbose(String message) {
        if (verbose) {
            report(message);
        }
    }

    public boolean expect(Object actual, Object expected, String desc) {
        if (expected.equals(actual)) {
            return true;
        }
        report("Incorrect test result for " + asString(desc) + " in test " + asString(spec.getTitle()));
        report("    expected " + asString(expected) + ": got " + asString(actual) + ".");
        return false;
    }
    
    private String asString(Object obj) {
        return (obj == null) ? "null" : ("'" + obj + "'");
    }

    public File getRoot() {
        // FIXME ... this should be the workspace root.
        return new File("..");
    }

}
