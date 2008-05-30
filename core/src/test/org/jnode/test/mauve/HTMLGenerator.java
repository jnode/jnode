// Copyright (C) 2004 by Object Refinery Limited
// Written by David Gilbert (david.gilbert@object-refinery.com)

// This file is part of Mauve Reporter.

// Mauve Reporter is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.

// Mauve Reporter is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with Mauve Reporter; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
package org.jnode.test.mauve;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;

/**
 * Generates a collection of HTML files that summarise the results
 * of a Mauve run.  This is a quick-and-dirty implementation!!
 */
public class HTMLGenerator {

    /**
     * Creates an HTML report in the specified directory.
     *
     * @param run           the Mauve run results.
     * @param rootDirectory the root directory.
     */
    public static void createReport(RunResult run, File rootDirectory) throws IOException {
        // write basic HTML with info about package
        File summaryFile = new File(rootDirectory, "index.html");
        Writer out = new OutputStreamWriter(new FileOutputStream(summaryFile), "UTF-8");
        PrintWriter writer = new PrintWriter(out);
        writer.println("<HTML>");
        writer.println("<HEAD><TITLE>Mauve Run: " + run.getName() + "</TITLE>");
        writer.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" /></HEAD>");
        writer.println("<BODY>");
        writer.println("<h1>Mauve Run</h1>");
        writer.println("<h2>Summary:</h2>");
        int checkCount = run.getCheckCount();
        int passed = run.getCheckCount(true);
        int failed = checkCount - passed;
        writer.println("Run Date: " +
            DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(new Date()) + "<br>");
        writer.println("Passed: " + passed + "<br>");
        writer.println("Failed: " + failed + "<p>");

        writer.println("<h2>Environment:</h2>");

        writer.println("<table BORDER=\"0\" CELLPADDING=\"0\">");
        writer.println("<tr>");
        writer.println("<td bgcolor=\"black\" VALIGN=\"TOP\">");
        writer.println("<table BORDER=\"0\" WIDTH=\"100%\" CELLSPACING=\"1\" CELLPADDING=\"3\">");
        writer.println("<tr>");
        writer.println("<td bgcolor=\"lightGray\">Property:</td>");
        writer.println("<td bgcolor=\"lightGray\">Value:</td>");
        writer.println("</tr>");

        writePropertyRow("java.version", System.getProperty("java.version"), writer);
        writePropertyRow("java.vendor", System.getProperty("java.vendor"), writer);
        writePropertyRow("java.vendor.url", System.getProperty("java.vendor.url"), writer);
        writePropertyRow("os.name", System.getProperty("os.name"), writer);
        writePropertyRow("os.arch", System.getProperty("os.arch"), writer);
        writePropertyRow("os.version", System.getProperty("os.version"), writer);

        writePropertyRow("java.vm.specification.version", System.getProperty("java.vm.specification.version"), writer);
        writePropertyRow("java.vm.specification.vendor", System.getProperty("java.vm.specification.vendor"), writer);
        writePropertyRow("java.vm.specification.name", System.getProperty("java.vm.specification.name"), writer);
        writePropertyRow("java.vm.version", System.getProperty("java.vm.version"), writer);
        writePropertyRow("java.vm.vendor", System.getProperty("java.vm.vendor"), writer);
        writePropertyRow("java.vm.name", System.getProperty("java.vm.name"), writer);
        writePropertyRow("java.specification.version", System.getProperty("java.specification.version"), writer);
        writePropertyRow("java.specification.vendor", System.getProperty("java.specification.vendor"), writer);
        writePropertyRow("java.specification.name", System.getProperty("java.specification.name"), writer);
        writePropertyRow("java.class.version", System.getProperty("java.class.version"), writer);

        writer.println("</table>");
        writer.println("</td>");
        writer.println("</tr>");
        writer.println("</table><p>");

        writer.println("<h2>Results:</h2>");

        writer.println("<table BORDER=\"0\" width=\"100%\" CELLPADDING=\"0\">");
        writer.println("<tr>");
        writer.println("<td bgcolor=\"black\" VALIGN=\"TOP\">");
        writer.println("<table BORDER=\"0\" WIDTH=\"100%\" CELLSPACING=\"1\" CELLPADDING=\"3\">");
        writer.println("<tr>");
        writer.println("<td bgcolor=\"lightGray\">Package:</td>");
        writer.println("<td bgcolor=\"lightGray\">Passed:</td>");
        writer.println("<td bgcolor=\"lightGray\">Failed:</td>");
        writer.println("<td bgcolor=\"lightGray\">Total:</td>");
        writer.println("</tr>");

        // loop through tests writing test results
        String top = null;
        Iterator iterator = run.getPackageIterator();
        while (iterator.hasNext()) {
            PackageResult packageResult = (PackageResult) iterator.next();
            String packageName = packageResult.getName().replace('.', '/');
            String name;
            System.out.println("Generating " + packageName);
            if (top != null && packageName.startsWith(top))
                name = "&nbsp;&nbsp;&nbsp;+&nbsp;" + packageName.substring(top.length() + 1);
            else {
                top = packageName;
                name = packageName;
            }
            // (1) write the summary line for the class HTML file
            writer.println("<tr>");
            writer.println(
                "<td bgcolor=\"white\"><a href=\"" + packageName + "/package_index.html\"" + ">" + name + "</a></td>");
            writer.println("<td bgcolor=\"white\">" + packageResult.getCheckCount(true) + "</td>");
            writer.println("<td bgcolor=\"white\">" + packageResult.getCheckCount(false) + "</td>");
            writer.println("<td bgcolor=\"white\">" + packageResult.getCheckCount() + "</td>");
            writer.println("</tr>");
            // (2) generate an HTML page for the test and subfiles
            //     for the tests
            try {
                HTMLGenerator.createPackageReport(packageResult, rootDirectory);
            } catch (Exception e) {
                String temp = packageResult.getName().replace('.', '/');
                System.err.println("Couldn't create package report for " + temp);
                File tempDir = new File(rootDirectory, packageName);
                tempDir.mkdirs();
                File tempFile = new File(tempDir, "package_index.html");
                tempFile.createNewFile();
            }
            System.gc();
        }
        writer.println("</table>");
        writer.println("</td>");
        writer.println("</tr>");
        writer.println("</table>");
        writer.println("<p>");
        Iterator missing = run.getMissingTestsIterator();
        Iterator failures = run.getFaultyTestsIterator();
        if (missing.hasNext() || failures.hasNext()) {
            writer.println("<h2>Unrunnable tests:</h2>");

            writer.println("<table BORDER=\"0\" width=\"100%\" CELLPADDING=\"0\">");
            writer.println("<tr>");
            writer.println("<td bgcolor=\"black\" VALIGN=\"TOP\">");
            writer.println("<table BORDER=\"0\" WIDTH=\"100%\" CELLSPACING=\"1\" CELLPADDING=\"3\">");
            writer.println("<tr>");
            writer.println("<td bgcolor=\"lightGray\">name:</td>");
            writer.println("<td bgcolor=\"lightGray\">problem:</td>");
            writer.println("</tr>");
            while (missing.hasNext())
                writer.println("<tr><td bgcolor=\"white\">" + (String) missing.next() +
                    "</td><td bgcolor=\"white\">Class not found</td></tr>");
            while (failures.hasNext()) {
                String[] fail = (String[]) failures.next();
                writer.println("<tr><td bgcolor=\"white\">" + fail[0] + "</td><td bgcolor=\"white\">" +
                    fail[1] + "</td></tr>");
            }
            writer.println("</table>");
            writer.println("</td>");
            writer.println("</tr>");
            writer.println("</table>");
        }

        writer.println("</BODY>");
        writer.println("</HTML>");
        writer.close();
    }

    /**
     * Writes a row in a table for a pair of strings.
     *
     * @param property the property key.
     * @param value    the property value.
     * @param writer   the output stream.
     */
    private static void writePropertyRow(String property, String value, PrintWriter writer) {
        writer.println("<tr>");
        writer.println("<td bgcolor=\"white\">" + property + "</td>");
        writer.println("<td bgcolor=\"white\">" + value + "</td>");
        writer.println("</tr>");
    }

    /**
     * Returns the number of directory levels in the specified package name.
     *
     * @param name the name.
     * @return The number of directory levels.
     */
    private static int countLevels(String name) {
        int result = 1;
        for (int i = 0; i < name.length(); i++) {
            if (name.charAt(i) == '/') result++;
        }
        return result;
    }

    /**
     * Creates an HTML page that summaries a package, and processes all the classes within
     * the package.
     *
     * @param packageResult the package result.
     * @param rootDirectory the root directory.
     */
    public static void createPackageReport(PackageResult packageResult, File rootDirectory) throws IOException {
        // create directory for package
        String packageName = packageResult.getName().replace('.', '/');
        String prefix = "";
        int levels = countLevels(packageName);
        for (int i = 0; i < levels; i++)
            prefix += "../";
        File packageDirectory = new File(rootDirectory, packageName);
        packageDirectory.mkdirs();

        // write basic HTML with info about package
        File summaryFile = new File(packageDirectory, "package_index.html");
        OutputStream out = new BufferedOutputStream(new FileOutputStream(summaryFile));
        PrintWriter writer = new PrintWriter(out);
        writer.println("<HTML>");
        writer.println("<HEAD><TITLE>Package Summary: " + packageResult.getName() + "</TITLE></HEAD>");
        writer.println("<BODY>");
        writer.println("<h2>Package: " + packageResult.getName() + "</h2>");
        writer.println("<a href=\"" + prefix + "index.html\">Summary page</a><p>");
        int checkCount = packageResult.getCheckCount();
        int passed = packageResult.getCheckCount(true);
        int failed = checkCount - passed;
        writer.println("Passed: " + passed + "<br>");
        writer.println("Failed: " + failed + "<p>");
        writer.println("<table BORDER=\"0\" width=\"100%\" CELLPADDING=\"0\">");
        writer.println("<tr>");
        writer.println("<td bgcolor=\"black\" VALIGN=\"TOP\">");
        writer.println("<table BORDER=\"0\" WIDTH=\"100%\" CELLSPACING=\"1\" CELLPADDING=\"3\">");
        writer.println("<tr>");
        writer.println("<td bgcolor=\"lightGray\">Class:</td>");
        writer.println("<td bgcolor=\"lightGray\">Passed:</td>");
        writer.println("<td bgcolor=\"lightGray\">Failed:</td>");
        writer.println("<td bgcolor=\"lightGray\">Total:</td>");
        writer.println("</tr>");

        // loop through tests writing test results
        Iterator iterator = packageResult.getClassIterator();
        while (iterator.hasNext()) {
            ClassResult classResult = (ClassResult) iterator.next();
            // (1) write the summary line for the class HTML file
            writer.println("<tr>");
            writer.println("<td bgcolor=\"white\"><a href=\"" + classResult.getName() + "/class_index.html\"" + ">" +
                classResult.getName() + "</a></td>");
            writer.println("<td bgcolor=\"white\">" + classResult.getCheckCount(true) + "</td>");
            writer.println("<td bgcolor=\"white\">" + classResult.getCheckCount(false) + "</td>");
            writer.println("<td bgcolor=\"white\">" + classResult.getCheckCount() + "</td>");
            writer.println("</tr>");
            // (2) generate an HTML page for the test and subfiles
            //     for the tests
            HTMLGenerator.createClassReport(classResult, packageResult.getName(), packageDirectory);
        }
        // close the class file
        writer.println("</table>");
        writer.println("</td>");
        writer.println("</tr>");
        writer.println("</table>");
        writer.println("</BODY>");
        writer.println("</HTML>");
        writer.close();
    }

    /**
     * Creates an HTML page summarising the results for a class, and processes all the tests for
     * the class.
     *
     * @param classResult      the class results.
     * @param packageName      the package name.
     * @param packageDirectory the package directory.
     */
    public static void createClassReport(ClassResult classResult, String packageName, File packageDirectory)
        throws IOException {
        // create directory for class
        File classDirectory = new File(packageDirectory, classResult.getName());
        classDirectory.mkdirs();

        // write basic HTML with info about class
        File testFile = new File(classDirectory, "class_index.html");
        OutputStream out = new BufferedOutputStream(new FileOutputStream(testFile));
        PrintWriter writer = new PrintWriter(out);
        writer.println("<HTML>");
        writer.println("<HEAD><TITLE>Class Summary: " + packageName + "." + classResult.getName() + "</TITLE></HEAD>");
        writer.println("<BODY>");
        writer.println("<h2>Class: " + "<a href=\"../package_index.html\">" + packageName + "</a>." +
            classResult.getName() + "</h2>");
        int checkCount = classResult.getCheckCount();
        int passed = classResult.getCheckCount(true);
        int failed = checkCount - passed;
        writer.println("Passed: " + passed + "<br>");
        writer.println("Failed: " + failed + "<p>");
        writer.println("<table BORDER=\"0\" width=\"100%\" CELLPADDING=\"0\">");
        writer.println("<tr>");
        writer.println("<td bgcolor=\"black\" VALIGN=\"TOP\">");
        writer.println("<table BORDER=\"0\" WIDTH=\"100%\" CELLSPACING=\"1\" CELLPADDING=\"3\">");
        writer.println("<tr>");
        writer.println("<td bgcolor=\"lightGray\">Test:</td>");
        writer.println("<td bgcolor=\"lightGray\">Passed:</td>");
        writer.println("<td bgcolor=\"lightGray\">Failed:</td>");
        writer.println("<td bgcolor=\"lightGray\">Total:</td>");
        writer.println("</tr>");

        // loop through tests writing test results
        Iterator iterator = classResult.getTestIterator();
        while (iterator.hasNext()) {
            TestResult testResult = (TestResult) iterator.next();
            // (1) write the summary line for the class HTML file
            writer.println("<tr>");
            writer.println("<td bgcolor=\"white\"><a href=\"" + testResult.getName() + ".html\"" + ">" +
                testResult.getName() + "</a></td>");
            writer.println("<td bgcolor=\"white\">" + testResult.getCheckCount(true) + "</td>");
            writer.println("<td bgcolor=\"white\">" + testResult.getCheckCount(false) + "</td>");
            writer.println("<td bgcolor=\"white\">" + testResult.getCheckCount() + "</td>");
            writer.println("</tr>");
            // (2) generate an HTML page for the test and subfiles
            //     for the tests
            HTMLGenerator.createTestReport(testResult, classResult.getName(), classDirectory);
        }
        // close the class file
        writer.println("</table>");
        writer.println("</td>");
        writer.println("</tr>");
        writer.println("</table>");
        writer.println("</BODY>");
        writer.println("</HTML>");
        writer.close();
    }

    /**
     * Creates an HTML page that summarises a test.
     *
     * @param testResult     the test result.
     * @param className      the class name.
     * @param classDirectory the class directory.
     */
    public static void createTestReport(TestResult testResult, String className, File classDirectory)
        throws IOException {

        // write basic HTML for test
        File testFile = new File(classDirectory, testResult.getName() + ".html");
        Writer out = new OutputStreamWriter(new FileOutputStream(testFile), "UTF-8");
        PrintWriter writer = new PrintWriter(out);
        writer.println("<HTML>");
        writer.println("<HEAD><TITLE>Test Summary: " + className + "." + testResult.getName() + "</TITLE>\n");
        writer.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" /></HEAD>");
        writer.println("<BODY>");
        writer
            .println("<h2>Test: <a href=\"class_index.html\">" + className + "</a>." + testResult.getName() + "</h2>");
        int checkCount = testResult.getCheckCount();
        int passed = testResult.getCheckCount(true);
        int failed = checkCount - passed;
        writer.println("Passed: " + passed + "<br>");
        writer.println("Failed: " + failed + "<p>");
        writer.println("<table BORDER=\"0\" width=\"100%\" CELLPADDING=\"0\">");
        writer.println("<tr>");
        writer.println("<td bgcolor=\"black\" VALIGN=\"TOP\">");
        writer.println("<table BORDER=\"0\" WIDTH=\"100%\" CELLSPACING=\"1\" CELLPADDING=\"3\">");
        writer.println("<tr>");
        writer.println("<td bgcolor=\"lightGray\">Check Number:</td>");
        writer.println("<td bgcolor=\"lightGray\">Check Point:</td>");
        writer.println("<td bgcolor=\"lightGray\">Passed?:</td>");
        writer.println("<td bgcolor=\"lightGray\">Expected:</td>");
        writer.println("<td bgcolor=\"lightGray\">Actual:</td>");
        writer.println("</tr>");

        // loop through checks adding a summary line for each check
        Iterator iterator = testResult.getCheckIterator();
        while (iterator.hasNext()) {
            CheckResult check = (CheckResult) iterator.next();
            // write a summary line (ID, pass/fail, actual, expected);
            writer.println("<tr><td bgcolor=\"white\">" + check.getNumber() +
                "</td><td bgcolor=\"white\">" + check.getCheckPoint() +
                "</td><td bgcolor=\"" + (check.getPassed() ? "white" : "red") + "\">" +
                check.getPassed() + "</td><td bgcolor=\"white\">" + check.getExpected() +
                "</td><td bgcolor=\"white\">" + check.getActual() + "</td>");
            if (!check.getPassed()) {
                try {
                    createLogReport(check, className, testResult.getName(), classDirectory);
                } catch (Exception e) {
                    System.err.println("Couldn't write report for class " + className);
                    File temp = new File(classDirectory, testResult.getName() + "_log.html");
                    temp.createNewFile();
                }
            }
            writer.println("</td>");
            writer.println("</tr>");
        }
        writer.println("</table>");
        writer.println("</td>");
        writer.println("</tr>");
        writer.println("</table>");
        if (testResult.isFailed()) {
            writer.println("<h2>Run aborted due to exception</h2>");
            writer.println("<pre>" + testResult.getFailedMessage() + "</pre>");
        }
        writer.println("</BODY>");
        writer.println("</HTML>");
        writer.close();
    }

    /**
     * Creates an HTML page that summarises the log for a check.
     *
     * @param checkResult    the test result.
     * @param className      the class name.
     * @param testName       the test name.
     * @param classDirectory the class directory.
     */
    public static void createLogReport(CheckResult checkResult, String className, String testName, File classDirectory)
        throws IOException {

        // write basic HTML for test
        File logFile = new File(classDirectory, testName + "_log.html");
        OutputStream out = new BufferedOutputStream(new FileOutputStream(logFile));
        PrintWriter writer = new PrintWriter(out);
        writer.println("<HTML>");
        writer.println("<HEAD><TITLE>Log: " + testName + "</TITLE>");
        writer.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" /></HEAD>");
        writer.println("<BODY>");
        writer.println(checkResult.getLog());
        writer.println("</BODY>");
        writer.println("</HTML>");

        writer.close();
    }
}
