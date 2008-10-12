package org.jnode.test.mauve;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Constants for tags used by {@link XMLReportParser} and {@link XMLReportWriter}.
 * Also contains a test program : @see {@link XMLReportConstants#main(String[])}. 
 * @author fabien
 *
 */
public class XMLReportConstants {
    protected static final String RUN_RESULT = "run";
    protected static final String RUN_NAME = "name";
    
    protected static final String PACKAGE_RESULT = "package";
    protected static final String PACKAGE_NAME = "name";
    
    protected static final String CLASS_RESULT = "class";
    protected static final String CLASS_NAME = "name";
    
    protected static final String TEST_RESULT = "test";
    protected static final String TEST_NAME = "name";
    protected static final String TEST_ERROR = "error";
    
    protected static final String CHECK_RESULT = "check";
    protected static final String CHECK_NUMBER = "number";
    protected static final String CHECK_POINT = "check-point";
    protected static final String CHECK_PASSED = "passed";
    protected static final String CHECK_EXPECTED = "expected";
    protected static final String CHECK_ACTUAL = "actual";
    protected static final String CHECK_LOG = "log";
    
    public static void main(String[] args) throws IOException {
        RunResult runResult = new RunResult("run");
        
        PackageResult pkg = new PackageResult("package");
        runResult.add(pkg);
        
        ClassResult cls = new ClassResult("class");
        pkg.add(cls);
        
        TestResult test = new TestResult("test");
        test.failed(new Exception("error", new Exception("nested error")));
        cls.add(test);
        
        CheckResult check = new CheckResult(1, true);
        check.appendToLog("a log with\nmultiple lines");
        test.add(check);      
        
        File f = File.createTempFile("XMLReport", "xml");
        f.deleteOnExit();
        new XMLReportWriter().write(runResult, f);
        new XMLReportWriter().write(runResult, new PrintWriter(System.out));
        
        RunResult rr = new XMLReportParser().parse(f);
        System.out.println("rr = " + rr);
    }
}
