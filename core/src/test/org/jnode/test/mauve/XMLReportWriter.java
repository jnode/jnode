package org.jnode.test.mauve;

import static org.jnode.test.mauve.XMLReportConstants.CHECK_ACTUAL;
import static org.jnode.test.mauve.XMLReportConstants.CHECK_EXPECTED;
import static org.jnode.test.mauve.XMLReportConstants.CHECK_LOG;
import static org.jnode.test.mauve.XMLReportConstants.CHECK_NUMBER;
import static org.jnode.test.mauve.XMLReportConstants.CHECK_PASSED;
import static org.jnode.test.mauve.XMLReportConstants.CHECK_POINT;
import static org.jnode.test.mauve.XMLReportConstants.CHECK_RESULT;
import static org.jnode.test.mauve.XMLReportConstants.CLASS_NAME;
import static org.jnode.test.mauve.XMLReportConstants.CLASS_RESULT;
import static org.jnode.test.mauve.XMLReportConstants.PACKAGE_NAME;
import static org.jnode.test.mauve.XMLReportConstants.PACKAGE_RESULT;
import static org.jnode.test.mauve.XMLReportConstants.RUN_NAME;
import static org.jnode.test.mauve.XMLReportConstants.RUN_RESULT;
import static org.jnode.test.mauve.XMLReportConstants.TEST_ERROR;
import static org.jnode.test.mauve.XMLReportConstants.TEST_NAME;
import static org.jnode.test.mauve.XMLReportConstants.TEST_RESULT;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Iterator;

/**
 * XML Writer for mauve reports.
 * 
 * @author fabien
 *
 */
public class XMLReportWriter {
    private static final String INDENT = "  ";
    
    /**
     * Write the given result in xml format.
     * 
     * @param result
     * @param output
     * @throws FileNotFoundException
     */
    public void write(RunResult result, File output) throws FileNotFoundException {
        PrintWriter ps = null;

        try {
            ps = new PrintWriter(new FileOutputStream(output));
            write(result, ps);
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }
    
    /**
     * Write the given result in xml format.
     * 
     * @param result
     * @param ps
     */
    public void write(RunResult result, PrintWriter ps) {
        int level = 0;        
        runResult(ps, level, result);
        level++;
        for (Iterator<?> itPackage = result.getPackageIterator(); itPackage.hasNext(); ) {
            PackageResult pkg = (PackageResult) itPackage.next();

            packageResult(ps, level, pkg);
            level++;
            for (Iterator<?> itClass = pkg.getClassIterator(); itClass.hasNext(); ) {
                ClassResult cls = (ClassResult) itClass.next();

                classResult(ps, level, cls);
                level++;
                for (Iterator<?> itTest = cls.getTestIterator(); itTest.hasNext(); ) {
                    TestResult test = (TestResult) itTest.next();
                    
                    test(ps, level, test);
                    level++;
                    for (Iterator<?> itCheck = test.getCheckIterator(); itCheck.hasNext(); ) {
                        CheckResult check = (CheckResult) itCheck.next();

                        check(ps, level, check);
                    }
                    level--;
                    endTag(ps, level, TEST_RESULT);
                    
                }
                level--;
                endTag(ps, level, CLASS_RESULT);
            }
            level--;
            endTag(ps, level, PACKAGE_RESULT);
            
        }
        level--;
        endTag(ps, level, RUN_RESULT);
        
        ps.flush();
    }
    
    private void check(PrintWriter ps, int level, CheckResult check) {
        beginTag(ps, level, CHECK_RESULT, CHECK_NUMBER, check.getNumber(), 
                CHECK_POINT, check.getCheckPoint(), 
                CHECK_PASSED, check.getPassed(), 
                CHECK_EXPECTED, check.getExpected(), 
                CHECK_ACTUAL, check.getActual());

        text(ps, level + 1, CHECK_LOG, check.getLog());
        
        endTag(ps, level, CHECK_RESULT);
    }

    private void test(PrintWriter ps, int level, TestResult test) {
        beginTag(ps, level, TEST_RESULT, TEST_NAME, test.getName());
        text(ps, level + 1, TEST_ERROR, test.getFailedMessage());
    }

    private void classResult(PrintWriter ps, int level, ClassResult cr) {
        beginTag(ps, level, CLASS_RESULT, CLASS_NAME, cr.getName());
    }

    private void packageResult(PrintWriter ps, int level, PackageResult pr) {
        beginTag(ps, level, PACKAGE_RESULT, PACKAGE_NAME, pr.getName());
    }

    private void runResult(PrintWriter ps, int level, RunResult rr) {
        beginTag(ps, level, RUN_RESULT, RUN_NAME, rr.getName());
    }
    
    private PrintWriter text(PrintWriter ps, int level, String tag, String text) {
        beginTag(ps, level, tag);
        
        if (text != null) {
            text = text.trim();
            if (!text.isEmpty()) { 
                ps.append(text).append('\n');
            }
        }
        
        return endTag(ps, level, tag);
    }

    private PrintWriter beginTag(PrintWriter ps, int level, String tag, Object... attributes) {
        tag(ps, level, tag, true);
        for (int i = 0; i < attributes.length; i += 2) {
            ps.append(' ').append(String.valueOf(attributes[i]));
            
            Object value = attributes[i + 1];
            ps.append("=\"").append((value == null) ? "" : value.toString()).append('\"');
        }
        
        ps.append(">\n");
        return ps;
    }
    
    private PrintWriter endTag(PrintWriter ps, int level, String tag) {
        return tag(ps, level, tag, false);
    }
    
    private PrintWriter tag(PrintWriter ps, int level, String tag, boolean begin) {
        indent(ps, level).append(begin ? "<" : "</").append(tag);
        if (!begin) {
            ps.append(">\n");
        }
        
        return ps;
    }

    private PrintWriter indent(PrintWriter ps, int level) {
        for (int i = 0; i < level; i++) {
            ps.print(INDENT);
        }
        
        return ps;
    }
}
