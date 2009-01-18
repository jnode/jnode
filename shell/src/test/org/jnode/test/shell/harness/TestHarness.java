package org.jnode.test.shell.harness;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

import net.n3.nanoxml.XMLException;

/**
 * This is the entry point class for the command test harness.  Its
 * purpose is to run 'black box' tests on commands and the like.
 * 
 * @author crawley@jnode
 */
public class TestHarness {
    
    private final String commandName = this.getClass().getCanonicalName();

    String[] TESTS = new String[] {
        "<testSpec>" +
            "<title>Hi mum</title>" +
            "<command>org.jnode.test.shell.harness.Test</command>" +
            "<output>Hi mum\n</output>" +
            "</testSpec>",
//        "<testSpec>" +
//            "<title>exit</title>" +
//            "<command>org.jnode.test.shell.harness.Test</command>" +
//            "<args><arg>System.exit</arg></args>" +
//            "</testSpec>"
    };

    private final String[] args;
    
    private PrintWriter reportWriter;
    private int testCount;
    private int errorCount;
    private int failureCount;
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
        if (args.length == 0) {
            for (String test : TESTS) {
                try {
                    InputStream is = new ByteArrayInputStream(test.getBytes());
                    spec = new TestSpecification().load(is);
                    execute(spec);
                } catch (Exception ex) {
                    diagnose(ex, "<built-in-tests>");
                }
            }
        } else if (args[0].equals("--package")) {
            if (args.length != 2) {
                usage();
            }
            InputStream is = null;
            try {
                is = this.getClass().getResourceAsStream(args[1]);
                spec = new TestSpecification().load(is);
                execute(spec);
            } catch (Exception ex) {
                diagnose(ex, args[1]);
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
        } else if (args[0].startsWith("-")) {
            System.err.println("Unrecognized option '" + args[0] + "'");
            usage();
            return;
        } else {
            for (String arg : args) {
                File file = new File(arg);
                InputStream is = null;
                try {
                    is = new FileInputStream(file);
                    spec = new TestSpecification().load(is);
                    execute(spec);
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
        }
        report("Ran " + testCount + " tests with " + errorCount + 
                " errors and " + failureCount + " failures");
    }
    
    private void usage() {
        System.err.println(commandName + " // run tests from builtin specs");
        System.err.println(commandName + " --package <java-package> // run tests from specs on classpath");
        System.err.println(commandName + " <spec-file> ... // run tests from specs read from file system");
    }

    private void execute(TestSpecification spec) {
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
                default:
                    reportVerbose("Run mode '" + spec.getRunMode() + "' not implemented");
                    return;
            }
            try {
                setup();
                runner.setup();
                errorCount += runner.run();
            } finally {
                runner.cleanup();
                cleanup();
            }
        } catch (Throwable ex) {
            ex.printStackTrace(reportWriter);
            failureCount++;
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

    public boolean expect(Object expected, Object actual, String desc) {
        if (expected.equals(actual)) {
            return true;
        }
        report("Incorrect test result for '" + desc + "' in test '" + spec.getTitle() + "'");
        report("   expected '" + expected + "': got '" + actual + "'");
        return false;
    }

    public File getRoot() {
        // FIXME ... this should be the workspace root.
        return new File("..");
    }

}
