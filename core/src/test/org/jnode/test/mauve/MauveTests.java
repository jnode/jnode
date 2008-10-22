package org.jnode.test.mauve;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.jnode.test.mauve.compare.HTMLComparisonWriter;
import org.jnode.test.mauve.compare.ReportComparator;
import org.jnode.test.mauve.compare.RunComparison;
import org.jnode.test.mauve.compare.TextComparisonWriter;

/**
 * Contains a test program for various functions like xml import/export of mauve results,
 * comparison of 2 mauve results with results saved in xml or html format.
 * 
 * @author fabien
 *
 */
public class MauveTests {
    
    public static void main(String[] args) throws IOException {
        RunResult runResult = createRunResult(1, 2, 0, 3, 1);
        System.out.println("=========================");
        
        System.out.println("\n--- writing XML file ---");
        File f = File.createTempFile("XMLReport", ".xml");
        f.deleteOnExit();
        new XMLReportWriter().write(runResult, f);
        new XMLReportWriter().write(runResult, new PrintWriter(System.out));
        HTMLGenerator.createReport(runResult, f.getParentFile());
        System.out.println("========================");
        
        System.out.println("\n--- parsing XML file ---");
        RunResult rr = new XMLReportParser().parse(f);
        System.out.println("rr = " + rr);
        System.out.println("========================");
        
        RunResult runResult2 = createRunResult(2, 2, 1, 3, 0);
        System.out.println("========================\n");
        
        ReportComparator c = new ReportComparator(runResult, runResult2);
        RunComparison comp = c.compare();
        
        System.out.println("\n--- comparison result in text ---");
        new TextComparisonWriter().write(comp, new PrintWriter(System.out));
        System.out.println("========================\n");
        
        System.out.println("\n--- comparison result in html ---");
        new HTMLComparisonWriter().write(comp, new PrintWriter(System.out));
        //new HTMLComparisonWriter().write(comp, new File("/tmp/HTMLComparison.html"));
        System.out.println("========================\n");
    }
    
    private static RunResult createRunResult(int runNumber, int nbTestsClass1, int nbPassed1,
            int nbTestsClass2, int nbPassed2) {
        String runName = "run" + runNumber;
        System.out.println("\n--- creating '" + runName + "' ---");        
        RunResult runResult = new RunResult(runName);

        PackageResult pkg = new PackageResult("package");
        runResult.add(pkg);

        ClassResult cls = new ClassResult("class1");
        pkg.add(cls);
        addTests(cls, "testA", nbTestsClass1, nbPassed1);

        cls = new ClassResult("class2");
        pkg.add(cls);
        addTests(cls, "testB", nbTestsClass2, nbPassed2);

        return runResult;
    }
    
    private static void addTests(ClassResult cls, String testPrefix, int nbTests, int nbPassed) {
        TestResult test;
        for (int i = 0; i < nbTests; i++) {
            test = new TestResult(testPrefix + i);

            CheckResult check;
            if (i < nbPassed) {
                check = new CheckResult(1, true);
                check.appendToLog("a log with\nmultiple lines");
                
                test.add(check);
            } else {
                check = new CheckResult(1, false);
                test.add(check);
                test.failed(new Exception("error" + i, new Exception("nested error")));
            }
            
            cls.add(test);
            
            System.out.print("Added " + test.getName());
            System.out.print("\t\t" + test.getCheckCount(true));
            System.out.println("\t\t" + check.getCheckPoint());
        }
    }
}
