package org.jnode.test.mauve.compare;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jnode.nanoxml.XMLParseException;
import org.jnode.test.mauve.CheckResult;
import org.jnode.test.mauve.ClassResult;
import org.jnode.test.mauve.PackageResult;
import org.jnode.test.mauve.Result;
import org.jnode.test.mauve.RunResult;
import org.jnode.test.mauve.TestResult;
import org.jnode.test.mauve.XMLReportParser;

/**
 * Comparator of 2 {@link RunResult}s
 * 
 * @author fabien
 *
 */
public class ReportComparator {

    private final RunResult result1;
    private final RunResult result2;
    
    public static void main(String[] args) throws XMLParseException, IOException {
        if (args.length < 3) {
            System.out.println("usage : java " + ReportComparator.class.getName() + 
                    " <report1.xml> <report2.xml> [html|text]");
        } else {
            File report1 = new File(args[0]);
            File report2 = new File(args[1]);
            String format = args[2];
            compare(report1, report2, format);
        }
    }
    
    public static File compare(File report1, File report2, String format) throws XMLParseException, IOException {
        XMLReportParser parser = new XMLReportParser();
        RunResult result1 = parser.parse(report1);
        RunResult result2 = parser.parse(report2);
        
        ReportComparator comparator = new ReportComparator(result1, result2);
        RunComparison comparison = comparator.compare();
        
        final ComparisonWriter writer;
        final String extension;
        if ("text".equals(format)) {
            writer = new TextComparisonWriter();
            extension = "txt";
        } else if ("html".equals(format)) {
            writer = new HTMLComparisonWriter();
            extension = "html";
        } else {
            extension = "txt";
            writer = new TextComparisonWriter();
        }
        
        int i = 0;
        File output = new File(report2.getParent(), "comp_" + i + "." + extension);
        while (output.exists()) {
            i++;
            output = new File(report2.getParent(), "comp_" + i + "." + extension);
        }
        writer.write(comparison, output);
        System.out.println("Comparison wrote to " + output.getAbsolutePath());
        
        return output;
    }
    
    public ReportComparator(RunResult result1, RunResult result2) {
        this.result1 = result1;
        this.result2 = result2;
    }

    /**
     * TODO handle case of added/removed package/class/test/check results ?
     * 
     * @return
     */
    public RunComparison compare() {
        RunComparison cr = new RunComparison(result1, result2);
        
        for (Iterator<?> itPackage1 = result1.getPackageIterator(); itPackage1.hasNext(); ) {
            PackageResult pkg1 = (PackageResult) itPackage1.next();
            PackageResult pkg2 = getResult(PackageResult.class, pkg1, result2.getPackageIterator()); 

            if (pkg2 == null) {
                continue;
            }

            for (Iterator<?> itClass1 = pkg1.getClassIterator(); itClass1.hasNext(); ) {
                ClassResult cls1 = (ClassResult) itClass1.next();
                ClassResult cls2 = getResult(ClassResult.class, cls1, pkg2.getClassIterator()); 

                if (cls2 == null) {
                    continue;
                }
                
                for (Iterator<?> itTest1 = cls1.getTestIterator(); itTest1.hasNext(); ) {
                    TestResult test1 = (TestResult) itTest1.next();
                    TestResult test2 = getResult(TestResult.class, test1, cls2.getTestIterator()); 

                    compare(test1, pkg2, cls2, test2, cr);
                }
            }
        }
        
        return cr;
    }
    
    private void compare(TestResult test1, PackageResult pkg2, ClassResult cls2, TestResult test2,
            RunComparison cr) {
        if ((test2 == null) || (test1.getCheckCount() != test2.getCheckCount())) {
            return;
        }

        List<CheckResult> reachedCheckResults1 = getReachedCheckResults(test1);
        List<CheckResult> reachedCheckResults2 = getReachedCheckResults(test2);

        final int size1 = reachedCheckResults1.size();
        final int size2 = reachedCheckResults2.size();
        
        CheckResult check2 = null;
        if (!reachedCheckResults2.isEmpty()) {
            check2 = reachedCheckResults2.get(reachedCheckResults2.size() - 1);
        }
        
        cr.setProgression(pkg2, cls2, test2, check2, size2 - size1);
    }
    
    private List<CheckResult> getReachedCheckResults(TestResult test) {
        List<CheckResult> checkResults = new ArrayList<CheckResult>();
        
        for (Iterator<?> itCheck = test.getCheckIterator(); itCheck.hasNext(); ) {
            CheckResult check = (CheckResult) itCheck.next();
            if (!check.getPassed()) {
                break;
            }
                
            checkResults.add(check);
        }
        
        return checkResults;
    }
    
    @SuppressWarnings("unchecked")
    private <T extends Result> T getResult(Class<T> resultClass, T result1, Iterator<?> results2) {
        final String name1 = result1.getName();
        T result2 = null;
        
        while (results2.hasNext()) {
            T res2 = (T) results2.next();
            if (name1.equals(res2.getName())) {
                result2 = res2;
                break;
            }
        }
        
        return result2;
    }
}
