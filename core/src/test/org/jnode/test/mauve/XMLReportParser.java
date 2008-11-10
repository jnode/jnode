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
import static org.jnode.test.mauve.XMLReportConstants.CHECK_NUMBER;
import static org.jnode.test.mauve.XMLReportConstants.CHECK_LOG;
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
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.util.List;

import org.jnode.nanoxml.XMLElement;
import org.jnode.nanoxml.XMLParseException;

/**
 * XML parser for mauve reports.
 * 
 * @author fabien
 *
 */
public class XMLReportParser {
    /**
     * Parse the given file and return the corresponding mauve result. 
     * 
     * @param input
     * @return
     * @throws XMLParseException
     * @throws IOException
     */
    public RunResult parse(File input) throws XMLParseException, IOException {
        XMLElement xmlRun = new XMLElement();
        BufferedReader reader = null;
        
        try {
            reader = new BufferedReader(new FileReader(input));
            xmlRun.parseFromReader(reader);
    
            checkTag(xmlRun, RUN_RESULT);
            
            String attr;
            RunResult run = new RunResult(getValue(xmlRun, RUN_NAME, ""));
            
            for (XMLElement xmlPkg : xmlRun.getChildren()) {
                checkTag(xmlPkg, PACKAGE_RESULT);
                
                attr = getValue(xmlPkg, PACKAGE_NAME, "");
                PackageResult pkg = new PackageResult(attr);             
                run.add(pkg);
                
                for (XMLElement xmlCls : xmlPkg.getChildren()) {
                    checkTag(xmlCls, CLASS_RESULT);
                    
                    attr = getValue(xmlCls, CLASS_NAME, "");
                    ClassResult cls = new ClassResult(attr);
                    pkg.add(cls);
                    
                    for (XMLElement xmlTest : xmlCls.getChildren()) {
                        checkTag(xmlTest, TEST_RESULT);
                        
                        attr = getValue(xmlTest, TEST_NAME, "");
                        TestResult test = new TestResult(attr);
                        cls.add(test);
                        
                        for (XMLElement xmlCheck : xmlTest.getChildren()) {
                            if (TEST_ERROR.equals(xmlCheck.getName())) {
                                test.setFailedMessage(xmlCheck.getContent());
                            } else {
                                checkTag(xmlCheck, CHECK_RESULT);
                                
                                test.add(createCheck(xmlCheck));
                            }
                        }
                    }
                }
            }
            
            return run;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }
    
    private CheckResult createCheck(XMLElement xmlCheck) {
        String attr = getValue(xmlCheck, CHECK_NUMBER, "-1");
        int number = Integer.valueOf(attr);
        
        attr = getValue(xmlCheck, CHECK_PASSED, "false");
        boolean passed = Boolean.valueOf(attr);
        
        CheckResult check = new CheckResult(number, passed);
        
        attr = getValue(xmlCheck, CHECK_POINT, "");
        check.setCheckPoint(attr);
        
        attr = getValue(xmlCheck, CHECK_EXPECTED, "");
        check.setExpected(attr);
        
        attr = getValue(xmlCheck, CHECK_ACTUAL, "");
        check.setActual(attr);
        
        // get the log if any
        List<XMLElement> children = xmlCheck.getChildren();
        if (!children.isEmpty()) {
            XMLElement firstChild = children.get(0);
            checkTag(firstChild, CHECK_LOG);
            
            String content = firstChild.getContent(); 
            if (content != null) {
                check.appendToLog(attr);
            }
        }
    
        return check;
    }

    private String getValue(XMLElement xml, String attributeName, String defaultValue) {
        Object attr = xml.getAttribute(attributeName, defaultValue);
        return String.valueOf(attr);        
    }
    
    private void checkTag(XMLElement xml, String tag) {
        final String actualTag = xml.getName();
        if (!tag.equals(actualTag)) {
            throw new XMLParseException("", "tag is not '" + tag + "' (actual: '" + actualTag + "')");
        }        
    }
}
