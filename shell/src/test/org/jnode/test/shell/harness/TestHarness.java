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
 
package org.jnode.test.shell.harness;

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
    // TODO - if someone feels motivated, they could replace the error
    // reporting with something that generates (say) XML that can be 
    // processed by a fancy error report generator.

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

    private boolean debug;
    private boolean verbose;
    private boolean stopOnError;
    private boolean stopOnFailure;
    private boolean useResources;
    private File root;

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
        // Do argument handling the classic Java way to minimize dependencies
        // on JNode functionality that we might be testing with the harness.
        int firstArg = 0;
        TestSetSpecification specs;
        if (args.length == 0) {
            usage();
            return;
        }
        for (int i = 0; i < args.length && args[i].startsWith("-"); i++) {
            String optName = args[i];
            if (optName.equals("-r") || optName.equals("--resource") || optName.equals("--resources")) {
                useResources = true;
            } else if (optName.equals("-v") || optName.equals("--verbose")) {
                verbose = true;
            } else if (optName.equals("-d") || optName.equals("--debug")) {
                debug = true;
            } else if (optName.equals("-F") || optName.equals("--stopOnFailure")) {
                stopOnFailure = true;
            } else if (optName.equals("-E") || optName.equals("--stopOnError")) {
                stopOnError = true;
            } else if (optName.equals("-s") || optName.equals("--sandbox")) {
                if (i++ >= args.length) {
                    System.err.println("No pathname after sandbox option");
                    usage();
                    return;
                }
                root = new File(args[i]);
            } else {
                System.err.println("Unrecognized option '" + optName + "'");
                usage();
                return;
            }
            firstArg = i + 1;
        }

        if (args.length <= firstArg) {
            System.err.println("Missing arguments");
            usage();
            return;
        }

        for (int i = firstArg; i < args.length; i++) {
            String arg = args[i];
            try {
                specs = loadTestSetSpecification(arg, useResources ? "/" : ".");
                if (specs != null) {
                    execute(specs);
                }
            } catch (TestsAbandonedException ex) {
                report(ex.getMessage());
                break;
            } catch (Exception ex) {
                diagnose(ex, arg);
            } 
        }
        report("Ran " + testCount + " tests with " + failureCount +
            " test failures and " + exceptionCount + " errors (exceptions)");
    }
    

    public TestSetSpecification loadTestSetSpecification(String specName, String base) throws Exception {
        TestSpecificationParser parser = new TestSpecificationParser();
        InputStream is = null;
        File file = new File(base, specName);
        try {
            if (useResources) {
                String resourceName = file.getPath();
                is = this.getClass().getResourceAsStream(resourceName);
                if (is == null) {
                    report("Cannot find resource for '" + resourceName + "'");
                    return null;
                }
            } else {
                is = new FileInputStream(file);
            }
            return parser.parse(this, is, file.getParent());
        } catch (Exception ex) {
            diagnose(ex, specName);
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    // ignore
                }
            }
        }
    }

    private void usage() {
        System.err.println(commandName + " [ <opt> ...] <spec-file> ... ");
        System.err.println("where <opt> is one of: ");
        System.err.println("    --verbose | -v             output more information about tests run");
        System.err.println("    --debug | -d               enable extra debug support");
        System.err.println("    --stopOnError | -E         stop at the first error");
        System.err.println("    --stopOnFailure | -F       stop at the first test failure");
        System.err.println("    --sandbox | -s <dir-name>  specifies the dev't sandbox root directory");
        System.err.println("    --resource | -r            looks for <spec-file> as a resource on the CLASSPATH");
    }

    private void execute(TestSetSpecification specs) throws TestsAbandonedException {
        for (TestSpecification spec : specs.getSpecs()) {
            execute(spec);
        } 
        for (TestSetSpecification set : specs.getSets()) {
            execute(set);
        }
    }

    /**
     * Run a test.
     * @param spec the specification of the test
     * @throws TestsAbandonedException 
     */
    private void execute(TestSpecification spec) throws TestsAbandonedException {
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
                    throw new TestsAbandonedException("Run mode '" + spec.getRunMode() + "' not implemented");
            }
            try {
                setup();
                runner.setup();
                int tmp = runner.run();
                failureCount += tmp;
                if (tmp > 0 && stopOnFailure) {
                    throw new TestsAbandonedException("Stopped due to test failure");
                }
            } finally {
                runner.cleanup();
                cleanup();
            }
        } catch (TestsAbandonedException ex) {
            throw ex;
        } catch (Throwable ex) {
            report("Uncaught exception in test '" + spec.getTitle() + "': stacktrace follows.");
            ex.printStackTrace(reportWriter);
            exceptionCount++;
            if (stopOnError) {
                throw new TestsAbandonedException("Stopped due to test error");
            }
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
        if (root != null) {
            return root;
        } else {
            // FIXME ... could try to find the workspace root by examining ".", ".." and
            // so on until we find a directory that looks like a sandbox.
            return new File("..");
        }
    }

    public boolean isDebug() {
        return debug;
    }

    public File tempFile(File file) {
        return new File(System.getProperty("java.io.tmpdir"), file.toString());
    }

    public boolean fail(String msg) {
        report("Incorrect test result in test " + asString(spec.getTitle()) + ": " + msg);
        return false;
    }
}
