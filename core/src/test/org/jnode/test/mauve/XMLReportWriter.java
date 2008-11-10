// Copyright (c) 2008 Fabien DUMINY (fduminy@jnode.org)
// Modified by Levente S\u00e1ntha (lsantha@jnode.org)

// This file is part of Mauve.

// Mauve is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2, or (at your option)
// any later version.

// Mauve is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with Mauve; see the file COPYING.  If not, write to
// the Free Software Foundation, 59 Temple Place - Suite 330,
// Boston, MA 02111-1307, USA.  */

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
    private static final String NOT_APPLIABLE = "n/a";
    
    private final boolean compactMode;

    public XMLReportWriter() {
        // by default, not in compact mode
        this(false);
    }
    
    public XMLReportWriter(boolean compactMode) {
        this.compactMode = compactMode;
    }
    
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
        String log = getNullIfBlank(check.getLog());
        boolean closeTag = (log == null);
        
        beginTag(ps, level, CHECK_RESULT, closeTag, CHECK_NUMBER, check.getNumber(), 
                CHECK_POINT, check.getCheckPoint(), 
                CHECK_PASSED, check.getPassed(), 
                CHECK_EXPECTED, check.getExpected(), 
                CHECK_ACTUAL, check.getActual());
        
        if (!closeTag) {
            text(ps, level + 1, CHECK_LOG, log);
            
            endTag(ps, level, CHECK_RESULT);
        }
    }

    private void test(PrintWriter ps, int level, TestResult test) {
        beginTag(ps, level, TEST_RESULT, false, TEST_NAME, test.getName());
        text(ps, level + 1, TEST_ERROR, test.getFailedMessage());
    }

    private void classResult(PrintWriter ps, int level, ClassResult cr) {
        beginTag(ps, level, CLASS_RESULT, false, CLASS_NAME, cr.getName());
    }

    private void packageResult(PrintWriter ps, int level, PackageResult pr) {
        beginTag(ps, level, PACKAGE_RESULT, false, PACKAGE_NAME, pr.getName());
    }

    private void runResult(PrintWriter ps, int level, RunResult rr) {
        beginTag(ps, level, RUN_RESULT, false, RUN_NAME, rr.getName());
    }
    
    private PrintWriter text(PrintWriter ps, int level, String tag, String text) {
        text = getNullIfBlank(text);
        if (text != null) {
            beginTag(ps, level, tag, false);                
            ps.append(protect(text));
            appendCarriageReturn(ps);
            endTag(ps, level, tag);
        }
        
        return ps;
    }

    private PrintWriter beginTag(PrintWriter ps, int level, String tag, boolean closeTag, Object... attributes) {
        tag(ps, level, tag, true);
        for (int i = 0; i < attributes.length; i += 2) {
            String value = getNullIfBlank(attributes[i + 1]);
            
            if (value != null) {
                ps.append(' ').append(String.valueOf(attributes[i]));
                
                ps.append("=\"").append(protect(value)).append('\"');
            }
        }
        
        ps.append(closeTag ? "/>" : ">");
        
        appendCarriageReturn(ps);
        return ps;
    }
    
    public static String protect(String text) {
        if (text == null) {
            return text;
        }

        final int size = text.length();
        final StringBuilder sb = new StringBuilder(size);
        boolean changed = false;
        for (int i = 0; i < size; i++) {
            final char c = text.charAt(i);
            switch (c) {
                case '&' :
                    sb.append("&amp;");
                    changed = true;
                    break;

                case '<' :
                    sb.append("&lt;");
                    changed = true;
                    break;

                case '>' :
                    sb.append("&gt;");
                    changed = true;
                    break;

                case '\'' :
                    sb.append("&apos;");
                    changed = true;
                    break;

                case '"' :
                    sb.append("&quot;");
                    changed = true;
                    break;

                default:
                    sb.append(c);
            }
        }

        return changed ? sb.toString() : text;
    }
    
    private PrintWriter endTag(PrintWriter ps, int level, String tag) {
        return tag(ps, level, tag, false);
    }
    
    private PrintWriter tag(PrintWriter ps, int level, String tag, boolean begin) {
        indent(ps, level).append(begin ? "<" : "</").append(tag);
        if (!begin) {
            ps.append('>');
            appendCarriageReturn(ps);
        }
        
        return ps;
    }
    
    private PrintWriter appendCarriageReturn(PrintWriter pw) {
        if (!compactMode) {
            pw.append('\n');
        }
        
        return pw;
    }

    private PrintWriter indent(PrintWriter ps, int level) {
        if (!compactMode) {
            for (int i = 0; i < level; i++) {
                ps.print(INDENT);
            }
        }
        
        return ps;
    }
    
    private String getNullIfBlank(Object text) {
        String result = null;
        
        if (text != null) {
            result = text.toString().trim();
            
            // We assume here that the corresponding attribute
            // is defaulted to NOT_APPLIABLE when it's null.
            //
            // It's the case for CheckResult.getExpected() and 
            // CheckResult.getActual())   
            if (result.isEmpty() || NOT_APPLIABLE.equals(result)) {
                result = null;
            }
        }
        
        return result;
    }
    
}
