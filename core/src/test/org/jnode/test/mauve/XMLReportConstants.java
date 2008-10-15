package org.jnode.test.mauve;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.jnode.test.mauve.compare.ComparisonWriter;
import org.jnode.test.mauve.compare.ReportComparator;
import org.jnode.test.mauve.compare.RunComparison;

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
        RunResult runResult = createRunResult(3, 1, 2, 0);
        
        File f = File.createTempFile("XMLReport", "xml");
        f.deleteOnExit();
        new XMLReportWriter().write(runResult, f);
        new XMLReportWriter().write(runResult, new PrintWriter(System.out));
        HTMLGenerator.createReport(runResult, f.getParentFile());
        
        RunResult rr = new XMLReportParser().parse(f);
        System.out.println("rr = " + rr);
        
        RunResult rr2 = createRunResult(3, 0, 2, 1);
        ReportComparator c = new ReportComparator(rr, rr2);
        RunComparison comp = c.compare();
        new ComparisonWriter().write(comp, new PrintWriter(System.out));
        
    }
    
    private static RunResult createRunResult(int nbTestsClass1, int nbPassed1,
            int nbTestsClass2, int nbPassed2) {
        RunResult runResult = new RunResult("run");

        PackageResult pkg = new PackageResult("package");
        runResult.add(pkg);

        ClassResult cls = new ClassResult("class1");
        pkg.add(cls);
        addTests(cls, nbTestsClass1, nbPassed1);

        cls = new ClassResult("class2");
        pkg.add(cls);
        addTests(cls, nbTestsClass2, nbPassed2);

        return runResult;
    }
    
    private static void addTests(ClassResult cls, int nbTests, int nbPassed) {
        TestResult test;
        for (int i = 0; i < nbTests; i++) {
            test = new TestResult("test" + i);

            if (i < nbPassed) {
                CheckResult check = new CheckResult(1, true);
                check.appendToLog("a log with\nmultiple lines");

                test.add(check);
            } else {
                test.add(new CheckResult(1, false));
                test.failed(new Exception("error" + i, new Exception("nested error")));
            }

            cls.add(test);
        }
    }
}
